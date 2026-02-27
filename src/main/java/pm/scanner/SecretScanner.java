package pm.scanner;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Scans .env files for hardcoded secret patterns.
 *
 * <p>Detects known secret formats (AWS keys, GitHub tokens, Slack tokens)
 * and generic long random values for sensitive keys.
 *
 * @author SoftDryzz
 * @version 1.7.1
 * @since 1.7.1
 */
public final class SecretScanner {

    private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
            "SECRET", "KEY", "TOKEN", "PASSWORD", "PASS", "AUTH",
            "PRIVATE", "CREDENTIAL"
    );

    private static final Pattern GENERIC_LONG_RANDOM = Pattern.compile("[A-Za-z0-9/+=]{40,}");

    private SecretScanner() {}

    /**
     * Scans all .env files in a project root for secret patterns.
     *
     * @param projectRoot project root path
     * @return list of findings, empty if clean or path is invalid
     */
    public static List<SecretFinding> scan(Path projectRoot) {
        if (projectRoot == null) {
            return Collections.emptyList();
        }

        List<Path> envFiles = EnvFileDetector.detectEnvFiles(projectRoot);
        if (envFiles.isEmpty()) {
            return Collections.emptyList();
        }

        List<SecretFinding> findings = new ArrayList<>();

        for (Path envFile : envFiles) {
            String fileName = envFile.getFileName().toString();
            Map<String, String> entries = EnvFileDetector.parseEnvFile(envFile);

            for (Map.Entry<String, String> entry : entries.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if (value == null || value.isEmpty()) {
                    continue;
                }

                SecretPattern matched = identifyPattern(key, value);
                if (matched != null) {
                    findings.add(new SecretFinding(
                            fileName,
                            key,
                            matched,
                            EnvFileDetector.maskValue(key, value)
                    ));
                }
            }
        }

        return findings;
    }

    /**
     * Identifies which secret pattern matches a key-value pair.
     *
     * @param key   environment variable key
     * @param value environment variable value
     * @return matched pattern, or null if no match
     */
    static SecretPattern identifyPattern(String key, String value) {
        // Check known patterns (AWS, GitHub, Slack) regardless of key name
        for (SecretPattern pattern : SecretPattern.values()) {
            if (pattern != SecretPattern.GENERIC_SECRET && pattern.matches(value)) {
                return pattern;
            }
        }

        // Check generic: sensitive key + long random value
        if (isSensitiveKey(key) && GENERIC_LONG_RANDOM.matcher(value).matches()) {
            return SecretPattern.GENERIC_SECRET;
        }

        return null;
    }

    private static boolean isSensitiveKey(String key) {
        String upper = key.toUpperCase();
        return SENSITIVE_KEYWORDS.stream().anyMatch(upper::contains);
    }
}
