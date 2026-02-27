package pm.scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Detects and parses .env files in a project directory.
 *
 * @author SoftDryzz
 * @version 1.7.1
 * @since 1.7.1
 */
public final class EnvFileDetector {

    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "SECRET", "KEY", "TOKEN", "PASSWORD", "PASS", "AUTH",
            "PRIVATE", "CREDENTIAL"
    );

    private EnvFileDetector() {}

    /**
     * Lists all .env* files in the project root directory.
     *
     * @param projectRoot project root path
     * @return sorted list of .env file paths, empty if none found or path is invalid
     */
    public static List<Path> detectEnvFiles(Path projectRoot) {
        if (projectRoot == null || !Files.isDirectory(projectRoot)) {
            return Collections.emptyList();
        }

        List<Path> envFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(projectRoot, ".env*")) {
            for (Path entry : stream) {
                if (Files.isRegularFile(entry)) {
                    envFiles.add(entry);
                }
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }

        envFiles.sort((a, b) -> a.getFileName().toString().compareTo(b.getFileName().toString()));
        return envFiles;
    }

    /**
     * Parses a .env file into key-value pairs.
     * Skips blank lines and comments (lines starting with #).
     * Handles quoted values (single and double quotes).
     *
     * @param envFile path to the .env file
     * @return ordered map of key-value pairs
     */
    public static Map<String, String> parseEnvFile(Path envFile) {
        if (envFile == null || !Files.isRegularFile(envFile)) {
            return Collections.emptyMap();
        }

        Map<String, String> entries = new LinkedHashMap<>();
        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int eqIdx = trimmed.indexOf('=');
                if (eqIdx <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, eqIdx).trim();
                String value = trimmed.substring(eqIdx + 1).trim();

                // Strip surrounding quotes
                if (value.length() >= 2) {
                    char first = value.charAt(0);
                    char last = value.charAt(value.length() - 1);
                    if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                        value = value.substring(1, value.length() - 1);
                    }
                }

                entries.put(key, value);
            }
        } catch (IOException e) {
            return Collections.emptyMap();
        }

        return entries;
    }

    /**
     * Masks a value if its key name suggests it contains sensitive data.
     *
     * @param key   the environment variable key
     * @param value the environment variable value
     * @return masked value if sensitive, original value otherwise
     */
    public static String maskValue(String key, String value) {
        if (key == null || value == null || value.isEmpty()) {
            return value;
        }

        String upperKey = key.toUpperCase();
        boolean sensitive = SENSITIVE_KEYWORDS.stream().anyMatch(upperKey::contains);

        if (!sensitive) {
            return value;
        }

        if (value.length() <= 3) {
            return "****";
        }
        return value.substring(0, 3) + "****";
    }
}
