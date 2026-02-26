package pm.util;

import pm.cli.OutputFormatter;

import java.io.*;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.net.ssl.SSLException;

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
 * <p>Safety measures (since v1.3.9):
 * <ul>
 * <li>Downloaded JAR size validated against expected size from API response</li>
 * <li>Redirect loops detected and capped at {@value #MAX_REDIRECTS}</li>
 * <li>Network errors classified with descriptive messages (offline, timeout, firewall)</li>
 * </ul>
 *
 * @author SoftDryzz
 * @version 1.3.9
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

    /** Maximum number of HTTP redirects to follow before aborting. */
    private static final int MAX_REDIRECTS = 5;

    private UpdateChecker() {
        throw new AssertionError("UpdateChecker cannot be instantiated");
    }

    // ============================================================
    // RELEASE INFO RECORD
    // ============================================================

    /**
     * Information about a GitHub release.
     *
     * @param version      version string without 'v' prefix (e.g., "1.3.9")
     * @param expectedSize expected JAR size in bytes from API response, or -1 if unavailable
     */
    record ReleaseInfo(String version, long expectedSize) {}

    // ============================================================
    // PUBLIC API
    // ============================================================

    /**
     * Checks if a newer version is available on GitHub.
     * Designed to be fast and non-blocking — if anything fails, it silently returns.
     * Called automatically at startup.
     *
     * <p>Since v1.3.9: shows a brief message when offline instead of failing silently.
     */
    public static void checkForUpdates() {
        try {
            ReleaseInfo release = fetchLatestVersion(CHECK_TIMEOUT_MS);
            if (release == null) {
                return;
            }

            if (isNewerVersion(release.version(), Constants.VERSION)) {
                System.out.println("  " + OutputFormatter.YELLOW + "Update available: " +
                        Constants.VERSION + " -> " + release.version() + OutputFormatter.RESET);
                System.out.println("  Run: " + OutputFormatter.CYAN + "pm update" +
                        OutputFormatter.RESET + " to update");
                System.out.println();
            }
        } catch (UnknownHostException e) {
            // Brief offline notification — non-blocking, does not interrupt user's command
            System.out.println("  " + OutputFormatter.YELLOW +
                    "Update check skipped (no internet connection)" + OutputFormatter.RESET);
            System.out.println();
        } catch (Exception e) {
            // Silently ignore other errors — never block the user's command
        }
    }

    /**
     * Downloads and installs the latest version.
     * Called explicitly via {@code pm update}.
     *
     * <p>Since v1.3.9: validates download integrity against expected size,
     * distinguishes network errors, and caps redirect loops.
     */
    public static void performUpdate() {
        System.out.println();
        OutputFormatter.info("Checking for updates...");
        System.out.println();

        ReleaseInfo release;
        try {
            release = fetchLatestVersion(DOWNLOAD_TIMEOUT_MS);
        } catch (Exception e) {
            OutputFormatter.error(describeNetworkError(e));
            printNetworkErrorAdvice(e);
            System.exit(1);
            return;
        }

        if (release == null) {
            OutputFormatter.error("Could not determine the latest version.");
            System.out.println("  The GitHub API response could not be parsed.");
            System.out.println("  Check manually: https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases");
            System.exit(1);
            return;
        }

        System.out.println("  Current version: " + Constants.VERSION);
        System.out.println("  Latest version:  " + release.version());
        System.out.println();

        if (!isNewerVersion(release.version(), Constants.VERSION)) {
            OutputFormatter.success("You are already on the latest version!");
            return;
        }

        // Download the new JAR
        String jarFileName = String.format(JAR_NAME, release.version());
        String downloadUrl = "https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO +
                "/releases/download/v" + release.version() + "/" + jarFileName;

        OutputFormatter.info("Downloading " + jarFileName + "...");

        Path installDir = Constants.CONFIG_DIR;
        Path pendingJar = installDir.resolve("projectmanager.jar.new");
        Path tempJar = installDir.resolve("projectmanager.jar.tmp");

        try {
            // Ensure directory exists
            Files.createDirectories(installDir);

            // Download to temp file first
            long contentLength = downloadFile(downloadUrl, tempJar);

            // Verify download integrity
            long fileSize = Files.size(tempJar);
            String sizeError = validateDownloadSize(fileSize, release.expectedSize(), contentLength);

            if (sizeError != null) {
                Files.deleteIfExists(tempJar);
                OutputFormatter.error(sizeError);
                System.out.println("  The downloaded file may be incomplete or corrupted.");
                System.out.println("  Try again, or download manually from:");
                System.out.println("  https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases");
                System.exit(1);
                return;
            }

            // Save as .new — the wrapper script (pm/pm.bat) will swap it
            // on next launch when the JVM is NOT holding a file lock.
            Files.move(tempJar, pendingJar, StandardCopyOption.REPLACE_EXISTING);

            System.out.println();
            OutputFormatter.success("Update downloaded! Version " + release.version() + " will be active on next run.");
            System.out.println();
            System.out.println("  Downloaded: " + pendingJar);
            System.out.println("  Size: " + formatFileSize(fileSize));
            if (release.expectedSize() > 0) {
                System.out.println("  Integrity: " + OutputFormatter.GREEN + "verified" + OutputFormatter.RESET +
                        " (size matches expected " + formatFileSize(release.expectedSize()) + ")");
            }
            System.out.println();
            System.out.println("  " + OutputFormatter.YELLOW + "Run any pm command to use the new version." + OutputFormatter.RESET);
            System.out.println();

        } catch (IOException e) {
            // Clean up temp files if they exist
            try {
                Files.deleteIfExists(tempJar);
                Files.deleteIfExists(pendingJar);
            } catch (IOException ignored) {
            }
            OutputFormatter.error("Download failed: " + describeNetworkError(e));
            printNetworkErrorAdvice(e);
            System.exit(1);
        }
    }

    // ============================================================
    // NETWORK OPERATIONS
    // ============================================================

    /**
     * Fetches the latest release information from the GitHub API.
     *
     * @param timeoutMs connection timeout in milliseconds
     * @return release info with version and expected size, or null if unparseable
     * @throws IOException if the network request fails
     */
    static ReleaseInfo fetchLatestVersion(int timeoutMs) throws IOException {
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
                throw new IOException("GitHub API returned HTTP " + responseCode);
            }

            // Read response and extract tag_name + asset size manually (no JSON library dependency)
            String body = readStream(conn.getInputStream());
            String version = extractTagName(body);

            if (version == null) {
                return null;
            }

            String jarFileName = String.format(JAR_NAME, version);
            long expectedSize = extractAssetSize(body, jarFileName);

            return new ReleaseInfo(version, expectedSize);

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Downloads a file from a URL to a local path.
     * Follows redirects manually with a loop counter to prevent infinite redirect loops.
     * GitHub releases redirect to a CDN, so redirect handling is required.
     *
     * @param url        download URL
     * @param targetPath local file path to save to
     * @return Content-Length reported by the server, or -1 if unknown
     * @throws IOException if the download fails or too many redirects occur
     */
    private static long downloadFile(String url, Path targetPath) throws IOException {
        String currentUrl = url;
        int redirectCount = 0;

        while (redirectCount <= MAX_REDIRECTS) {
            HttpURLConnection conn = (HttpURLConnection) URI.create(currentUrl).toURL().openConnection();
            try {
                conn.setRequestProperty("User-Agent", "ProjectManager/" + Constants.VERSION);
                conn.setConnectTimeout(DOWNLOAD_TIMEOUT_MS);
                conn.setReadTimeout(DOWNLOAD_TIMEOUT_MS);
                conn.setInstanceFollowRedirects(false);

                int responseCode = conn.getResponseCode();

                // Handle redirects manually with counter
                if (responseCode == 301 || responseCode == 302 ||
                        responseCode == 307 || responseCode == 308) {
                    String location = conn.getHeaderField("Location");
                    conn.disconnect();

                    if (location == null || location.isBlank()) {
                        throw new IOException("Server sent redirect without Location header");
                    }

                    currentUrl = location;
                    redirectCount++;
                    continue;
                }

                if (responseCode != 200) {
                    throw new IOException("HTTP " + responseCode + " when downloading from: " + url);
                }

                long contentLength = conn.getContentLengthLong();

                try (InputStream in = conn.getInputStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }

                return contentLength;

            } finally {
                conn.disconnect();
            }
        }

        throw new IOException("Too many redirects (" + MAX_REDIRECTS +
                "). The download URL may be misconfigured.");
    }

    // ============================================================
    // DOWNLOAD VALIDATION
    // ============================================================

    /**
     * Validates the downloaded file size against expected values.
     *
     * @param actualSize    actual file size on disk
     * @param expectedSize  expected size from GitHub API (-1 if unknown)
     * @param contentLength Content-Length from HTTP response (-1 if unknown)
     * @return error message if validation fails, or null if valid
     */
    static String validateDownloadSize(long actualSize, long expectedSize, long contentLength) {
        // Baseline: file must be at least 1 KB
        if (actualSize < 1000) {
            return "Downloaded file is too small (" + actualSize + " bytes). The download may have failed.";
        }

        // Validate against expected size from GitHub API
        if (expectedSize > 0 && actualSize != expectedSize) {
            return "Download size mismatch: got " + formatFileSize(actualSize) +
                    " but expected " + formatFileSize(expectedSize) +
                    ". The file may be incomplete or corrupted.";
        }

        // Validate against Content-Length from HTTP response
        if (contentLength > 0 && actualSize != contentLength) {
            return "Download incomplete: got " + formatFileSize(actualSize) +
                    " but server reported " + formatFileSize(contentLength) +
                    ". The connection may have been interrupted.";
        }

        return null; // Valid
    }

    // ============================================================
    // JSON PARSING (no library dependency)
    // ============================================================

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
     * Extracts the file size of a specific asset from a GitHub API JSON response.
     * Searches for the asset name in the JSON, then finds the nearby "size" field.
     *
     * @param json        the raw JSON string from GitHub API
     * @param jarFileName the asset file name to look for (e.g., "projectmanager-1.3.9.jar")
     * @return file size in bytes, or -1 if not found
     */
    static long extractAssetSize(String json, String jarFileName) {
        if (json == null || jarFileName == null || json.isEmpty() || jarFileName.isEmpty()) {
            return -1;
        }

        // Find the JAR file name in the JSON
        int nameIdx = json.indexOf(jarFileName);
        if (nameIdx == -1) {
            return -1;
        }

        // Search for "size" near the asset name (within ±500 chars)
        String sizeKey = "\"size\"";

        // Try forward first
        int searchEnd = Math.min(nameIdx + 500, json.length());
        int sizeIdx = json.indexOf(sizeKey, nameIdx);

        if (sizeIdx == -1 || sizeIdx > searchEnd) {
            // Try backward
            int searchStart = Math.max(nameIdx - 500, 0);
            String window = json.substring(searchStart, nameIdx);
            int relIdx = window.lastIndexOf(sizeKey);
            if (relIdx == -1) {
                return -1;
            }
            sizeIdx = searchStart + relIdx;
        }

        // Find the colon after "size"
        int colonIdx = json.indexOf(':', sizeIdx + sizeKey.length());
        if (colonIdx == -1) {
            return -1;
        }

        // Skip whitespace after colon
        int numStart = colonIdx + 1;
        while (numStart < json.length() && Character.isWhitespace(json.charAt(numStart))) {
            numStart++;
        }

        // Read digits
        int numEnd = numStart;
        while (numEnd < json.length() && Character.isDigit(json.charAt(numEnd))) {
            numEnd++;
        }

        if (numStart == numEnd) {
            return -1;
        }

        try {
            return Long.parseLong(json.substring(numStart, numEnd));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // ============================================================
    // NETWORK ERROR CLASSIFICATION
    // ============================================================

    /**
     * Classifies a network exception into a user-friendly error description.
     *
     * @param e the exception to classify
     * @return human-readable error description
     */
    static String describeNetworkError(Exception e) {
        if (e instanceof UnknownHostException) {
            return "No internet connection.";
        }
        if (e instanceof SocketTimeoutException) {
            return "Connection timed out.";
        }
        if (e instanceof ConnectException) {
            return "Connection refused.";
        }
        if (e instanceof SSLException) {
            return "Secure connection failed (SSL/TLS error).";
        }
        String msg = e.getMessage();
        return "Network error: " + (msg != null ? msg : "unknown error");
    }

    /**
     * Prints specific advice based on the type of network error.
     *
     * @param e the exception to provide advice for
     */
    private static void printNetworkErrorAdvice(Exception e) {
        if (e instanceof UnknownHostException) {
            System.out.println("  Check your internet connection and try again.");
        } else if (e instanceof SocketTimeoutException) {
            System.out.println("  The server may be slow or unreachable. Try again later.");
        } else if (e instanceof ConnectException) {
            System.out.println("  A firewall or proxy may be blocking the connection.");
        } else if (e instanceof SSLException) {
            System.out.println("  Your network may be intercepting secure connections.");
            System.out.println("  If you're behind a corporate proxy, check its SSL settings.");
        } else {
            System.out.println("  Check your internet connection and try again.");
        }
        System.out.println("  You can also download manually from:");
        System.out.println("  https://github.com/" + GITHUB_OWNER + "/" + GITHUB_REPO + "/releases");
    }

    // ============================================================
    // VERSION COMPARISON
    // ============================================================

    /**
     * Compares two semantic version strings.
     * Returns true if {@code latest} is newer than {@code current}.
     *
     * @param latest  the latest version (e.g., "1.3.0")
     * @param current the current version (e.g., "1.2.0")
     * @return true if latest &gt; current
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

    // ============================================================
    // UTILITY METHODS
    // ============================================================

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
    static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
