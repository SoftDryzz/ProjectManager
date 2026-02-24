package pm.util;

import pm.cli.OutputFormatter;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Checks for updates and downloads the latest version from GitHub Releases.
 *
 * <p>Two modes of operation:
 * <ul>
 * <li>{@link #checkForUpdates()} - Quick, non-blocking check shown at startup</li>
 * <li>{@link #performUpdate()} - Downloads and installs the latest JAR</li>
 * </ul>
 *
 * <p>Uses the GitHub API to query the latest release:
 * {@code https://api.github.com/repos/SoftDryzz/ProjectManager/releases/latest}
 *
 * @author SoftDryzz
 * @version 1.3.0
 * @since 1.2.0
 */
public final class UpdateChecker {

    private static final String GITHUB_OWNER = "SoftDryzz";
    private static final String GITHUB_REPO = "ProjectManager";
    private static final String API_URL =
            "https://api.github.com/repos/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases/latest";
    private static final String JAR_NAME = "projectmanager-%s.jar";

    /** Connection timeout for the update check (2 seconds). */
    private static final int CHECK_TIMEOUT_MS = 2000;

    /** Connection timeout for the download (30 seconds). */
    private static final int DOWNLOAD_TIMEOUT_MS = 30000;

    private UpdateChecker() {
        throw new AssertionError("UpdateChecker cannot be instantiated");
    }

    /**
     * Checks if a newer version is available on GitHub.
     * Designed to be fast and non-blocking — if anything fails, it silently returns.
     * Called automatically at startup.
     */
    public static void checkForUpdates() {
        try {
            String latestVersion = fetchLatestVersion(CHECK_TIMEOUT_MS);
            if (latestVersion == null) {
                return;
            }

            if (isNewerVersion(latestVersion, Constants.VERSION)) {
                System.out.println("  " + OutputFormatter.YELLOW + "Update available: " +
                        Constants.VERSION + " -> " + latestVersion + OutputFormatter.RESET);
                System.out.println("  Run: " + OutputFormatter.CYAN + "pm update" +
                        OutputFormatter.RESET + " to update");
                System.out.println();
            }
        } catch (Exception e) {
            // Silently ignore — never block the user's command
        }
    }

    /**
     * Downloads and installs the latest version.
     * Called explicitly via {@code pm update}.
     */
    public static void performUpdate() {
        System.out.println();
        OutputFormatter.info("Checking for updates...");
        System.out.println();

        String latestVersion;
        try {
            latestVersion = fetchLatestVersion(DOWNLOAD_TIMEOUT_MS);
        } catch (Exception e) {
            OutputFormatter.error("Failed to check for updates: " + e.getMessage());
            System.out.println("  Check your internet connection and try again.");
            System.exit(1);
            return;
        }

        if (latestVersion == null) {
            OutputFormatter.error("Could not determine the latest version.");
            System.out.println("  Check: https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases");
            System.exit(1);
            return;
        }

        System.out.println("  Current version: " + Constants.VERSION);
        System.out.println("  Latest version:  " + latestVersion);
        System.out.println();

        if (!isNewerVersion(latestVersion, Constants.VERSION)) {
            OutputFormatter.success("You are already on the latest version!");
            return;
        }

        // Download the new JAR
        String jarFileName = String.format(JAR_NAME, latestVersion);
        String downloadUrl = "https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO +
                "/releases/download/v" + latestVersion + "/" + jarFileName;

        OutputFormatter.info("Downloading " + jarFileName + "...");

        Path installDir = Constants.CONFIG_DIR;
        Path targetJar = installDir.resolve("projectmanager.jar");
        Path tempJar = installDir.resolve("projectmanager.jar.tmp");

        try {
            // Ensure directory exists
            Files.createDirectories(installDir);

            // Download to temp file first
            downloadFile(downloadUrl, tempJar);

            // Verify download (at least check it's not empty)
            long fileSize = Files.size(tempJar);
            if (fileSize < 1000) {
                Files.deleteIfExists(tempJar);
                OutputFormatter.error("Downloaded file is too small (" + fileSize + " bytes). The download may have failed.");
                System.out.println("  Try downloading manually from:");
                System.out.println("  https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases");
                System.exit(1);
                return;
            }

            // Replace old JAR with new one
            Files.move(tempJar, targetJar, StandardCopyOption.REPLACE_EXISTING);

            System.out.println();
            OutputFormatter.success("Updated to version " + latestVersion + "!");
            System.out.println();
            System.out.println("  Installed to: " + targetJar);
            System.out.println("  Size: " + formatFileSize(fileSize));
            System.out.println();

        } catch (IOException e) {
            // Clean up temp file if it exists
            try {
                Files.deleteIfExists(tempJar);
            } catch (IOException ignored) {
            }
            OutputFormatter.error("Failed to download update: " + e.getMessage());
            System.out.println("  Try downloading manually from:");
            System.out.println("  https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases");
            System.exit(1);
        }
    }

    /**
     * Fetches the latest release version tag from GitHub API.
     *
     * @param timeoutMs connection timeout in milliseconds
     * @return version string (without 'v' prefix), or null if unavailable
     * @throws IOException if the request fails
     */
    static String fetchLatestVersion(int timeoutMs) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) URI.create(API_URL).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "ProjectManager/" + Constants.VERSION);
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return null;
            }

            // Read response and extract tag_name manually (no JSON library dependency)
            String body = readStream(conn.getInputStream());
            return extractTagName(body);

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Downloads a file from a URL to a local path.
     * Follows redirects (GitHub releases redirect to CDN).
     *
     * @param url        download URL
     * @param targetPath local file path to save to
     * @throws IOException if the download fails
     */
    private static void downloadFile(String url, Path targetPath) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setRequestProperty("User-Agent", "ProjectManager/" + Constants.VERSION);
            conn.setConnectTimeout(DOWNLOAD_TIMEOUT_MS);
            conn.setReadTimeout(DOWNLOAD_TIMEOUT_MS);
            conn.setInstanceFollowRedirects(true);

            int responseCode = conn.getResponseCode();

            // Handle manual redirect if needed
            if (responseCode == 302 || responseCode == 301) {
                String redirectUrl = conn.getHeaderField("Location");
                conn.disconnect();
                if (redirectUrl != null) {
                    conn = (HttpURLConnection) URI.create(redirectUrl).toURL().openConnection();
                    conn.setRequestProperty("User-Agent", "ProjectManager/" + Constants.VERSION);
                    conn.setConnectTimeout(DOWNLOAD_TIMEOUT_MS);
                    conn.setReadTimeout(DOWNLOAD_TIMEOUT_MS);
                    responseCode = conn.getResponseCode();
                }
            }

            if (responseCode != 200) {
                throw new IOException("HTTP " + responseCode + " when downloading from: " + url);
            }

            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Extracts the "tag_name" value from a GitHub API JSON response.
     * Simple string parsing to avoid adding a JSON library dependency.
     *
     * @param json the raw JSON string
     * @return version string (without 'v' prefix), or null
     */
    static String extractTagName(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        // Look for "tag_name":"v1.2.3" or "tag_name": "v1.2.3"
        String key = "\"tag_name\"";
        int idx = json.indexOf(key);
        if (idx == -1) {
            return null;
        }

        // Find the value after the colon
        int colonIdx = json.indexOf(':', idx + key.length());
        if (colonIdx == -1) {
            return null;
        }

        // Find the opening quote of the value
        int openQuote = json.indexOf('"', colonIdx + 1);
        if (openQuote == -1) {
            return null;
        }

        // Find the closing quote
        int closeQuote = json.indexOf('"', openQuote + 1);
        if (closeQuote == -1) {
            return null;
        }

        String tag = json.substring(openQuote + 1, closeQuote).trim();

        // Remove 'v' prefix if present
        if (tag.startsWith("v") || tag.startsWith("V")) {
            tag = tag.substring(1);
        }

        return tag.isEmpty() ? null : tag;
    }

    /**
     * Compares two semantic version strings.
     * Returns true if {@code latest} is newer than {@code current}.
     *
     * @param latest  the latest version (e.g., "1.3.0")
     * @param current the current version (e.g., "1.2.0")
     * @return true if latest > current
     */
    static boolean isNewerVersion(String latest, String current) {
        if (latest == null || current == null) {
            return false;
        }

        int[] latestParts = parseVersion(latest);
        int[] currentParts = parseVersion(current);

        for (int i = 0; i < 3; i++) {
            if (latestParts[i] > currentParts[i]) {
                return true;
            }
            if (latestParts[i] < currentParts[i]) {
                return false;
            }
        }

        return false; // Equal versions
    }

    /**
     * Parses a version string into [major, minor, patch].
     *
     * @param version version string (e.g., "1.2.0")
     * @return array of [major, minor, patch]
     */
    private static int[] parseVersion(String version) {
        int[] parts = {0, 0, 0};
        String[] split = version.split("\\.");

        for (int i = 0; i < Math.min(split.length, 3); i++) {
            try {
                parts[i] = Integer.parseInt(split[i].trim());
            } catch (NumberFormatException e) {
                parts[i] = 0;
            }
        }

        return parts;
    }

    /**
     * Reads an InputStream into a String.
     */
    private static String readStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    /**
     * Formats a file size in human-readable format.
     */
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
