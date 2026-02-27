package pm.security;

import pm.core.Project;
import pm.detector.ProjectType;
import pm.scanner.EnvFileDetector;
import pm.scanner.SecretFinding;
import pm.scanner.SecretScanner;
import pm.util.RuntimeChecker;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scans projects for common security misconfigurations.
 *
 * <p>Runs 7 filesystem-only checks:
 * <ol>
 *   <li>Dockerfile runs as non-root user</li>
 *   <li>.env files protected by .gitignore</li>
 *   <li>No hardcoded http:// URLs in config files</li>
 *   <li>Sensitive files (.pem, .key) in .gitignore</li>
 *   <li>Dependencies lockfile present</li>
 *   <li>No hardcoded secret patterns in .env files</li>
 *   <li>Vaultic secret encryption (when .env files present)</li>
 * </ol>
 *
 * @author SoftDryzz
 * @version 1.6.2
 * @since 1.6.2
 */
public final class SecurityScorer {

    private SecurityScorer() {}

    /**
     * Runs all security checks on a project.
     *
     * @param project project to evaluate
     * @return list of check results (always 7 items)
     */
    public static List<SecurityCheck> evaluate(Project project) {
        Path root = project.path();
        List<SecurityCheck> checks = new ArrayList<>();

        checks.add(checkDockerfileRoot(root));
        checks.add(checkEnvInGitignore(root));
        checks.add(checkHttpUrls(root));
        checks.add(checkSensitiveFilesInGitignore(root));
        checks.add(checkLockfile(root, project.type()));
        checks.add(checkSecretPatterns(root));
        checks.add(checkVaultic(root));

        return checks;
    }

    /**
     * Applies auto-fixes for fixable failed checks.
     *
     * @param project project to fix
     * @return list of human-readable actions taken (empty if nothing to fix)
     */
    public static List<String> fix(Project project) {
        Path root = project.path();
        List<SecurityCheck> checks = evaluate(project);
        List<String> actions = new ArrayList<>();

        boolean needsEnvFix = false;
        boolean needsSensitiveFix = false;

        for (SecurityCheck check : checks) {
            if (!check.passed() && check.fixable()) {
                if ("env-gitignore".equals(check.name())) {
                    needsEnvFix = true;
                } else if ("sensitive-files".equals(check.name())) {
                    needsSensitiveFix = true;
                }
            }
        }

        if (!needsEnvFix && !needsSensitiveFix) {
            return actions;
        }

        Path gitignorePath = root.resolve(".gitignore");
        Set<String> existingEntries = parseGitignore(gitignorePath);
        StringBuilder toAppend = new StringBuilder();

        if (needsEnvFix) {
            if (!existingEntries.contains(".env") && !existingEntries.contains(".env*")
                    && !existingEntries.contains(".env.*")) {
                toAppend.append("\n# Environment files\n");
                toAppend.append(".env\n");
                toAppend.append(".env.*\n");
                actions.add("Added .env entries to .gitignore");
            }
        }

        if (needsSensitiveFix) {
            boolean hasPem = existingEntries.contains("*.pem");
            boolean hasKey = existingEntries.contains("*.key");
            if (!hasPem || !hasKey) {
                toAppend.append("\n# Private keys and certificates\n");
                if (!hasPem) toAppend.append("*.pem\n");
                if (!hasKey) toAppend.append("*.key\n");
                if (!existingEntries.contains("*.p12")) toAppend.append("*.p12\n");
                if (!existingEntries.contains("*.pfx")) toAppend.append("*.pfx\n");
                actions.add("Added *.pem, *.key entries to .gitignore");
            }
        }

        if (!toAppend.isEmpty()) {
            try {
                if (!Files.exists(gitignorePath)) {
                    Files.createFile(gitignorePath);
                }
                Files.writeString(gitignorePath,
                        Files.readString(gitignorePath) + toAppend);
            } catch (IOException e) {
                actions.clear();
                actions.add("Failed to update .gitignore: " + e.getMessage());
            }
        }

        return actions;
    }

    // ============================================================
    // INDIVIDUAL CHECKS
    // ============================================================

    static SecurityCheck checkDockerfileRoot(Path root) {
        Path dockerfile = root.resolve("Dockerfile");
        if (!Files.exists(dockerfile)) {
            return new SecurityCheck(
                    "dockerfile-root",
                    true,
                    "Dockerfile",
                    "",
                    false
            );
        }

        try {
            List<String> lines = Files.readAllLines(dockerfile);
            boolean hasUserDirective = false;
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.toUpperCase().startsWith("USER ")
                        && !trimmed.toUpperCase().matches("USER\\s+ROOT\\s*")) {
                    hasUserDirective = true;
                    break;
                }
            }
            return new SecurityCheck(
                    "dockerfile-root",
                    hasUserDirective,
                    "Dockerfile",
                    "Add a USER directive to your Dockerfile to avoid running as root",
                    false
            );
        } catch (IOException e) {
            return new SecurityCheck(
                    "dockerfile-root",
                    false,
                    "Dockerfile",
                    "Cannot read Dockerfile: " + e.getMessage(),
                    false
            );
        }
    }

    static SecurityCheck checkEnvInGitignore(Path root) {
        Path gitignore = root.resolve(".gitignore");
        if (!Files.exists(gitignore)) {
            return new SecurityCheck(
                    "env-gitignore",
                    false,
                    "Env protection",
                    "Add .env to your .gitignore to prevent leaking secrets",
                    true
            );
        }

        Set<String> entries = parseGitignore(gitignore);
        boolean covered = entries.contains(".env")
                || entries.contains(".env*")
                || entries.contains(".env.*");

        return new SecurityCheck(
                "env-gitignore",
                covered,
                "Env protection",
                "Add .env to your .gitignore to prevent leaking secrets",
                true
        );
    }

    static SecurityCheck checkHttpUrls(Path root) {
        if (!Files.isDirectory(root)) {
            return new SecurityCheck(
                    "https-only",
                    true,
                    "HTTPS only",
                    "",
                    false
            );
        }

        Set<String> configExtensions = Set.of(
                ".yml", ".yaml", ".properties", ".json", ".toml",
                ".xml", ".conf", ".cfg", ".ini"
        );

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path entry : stream) {
                if (!Files.isRegularFile(entry)) continue;

                String name = entry.getFileName().toString().toLowerCase();
                boolean isConfig = configExtensions.stream().anyMatch(name::endsWith)
                        || name.startsWith(".env");

                if (!isConfig) continue;

                String content = Files.readString(entry);
                if (containsInsecureHttp(content)) {
                    return new SecurityCheck(
                            "https-only",
                            false,
                            "HTTPS only",
                            "Replace http:// with https:// in config files for secure connections",
                            false
                    );
                }
            }
        } catch (IOException e) {
            // Cannot scan directory — assume OK
        }

        return new SecurityCheck(
                "https-only",
                true,
                "HTTPS only",
                "",
                false
        );
    }

    static SecurityCheck checkSensitiveFilesInGitignore(Path root) {
        Path gitignore = root.resolve(".gitignore");
        if (!Files.exists(gitignore)) {
            return new SecurityCheck(
                    "sensitive-files",
                    false,
                    "Sensitive files",
                    "Add *.pem and *.key to .gitignore to protect private keys",
                    true
            );
        }

        Set<String> entries = parseGitignore(gitignore);
        boolean hasPem = entries.contains("*.pem");
        boolean hasKey = entries.contains("*.key");

        return new SecurityCheck(
                "sensitive-files",
                hasPem && hasKey,
                "Sensitive files",
                "Add *.pem and *.key to .gitignore to protect private keys",
                true
        );
    }

    static SecurityCheck checkLockfile(Path root, ProjectType type) {
        boolean exists = switch (type) {
            case NODEJS -> Files.exists(root.resolve("package-lock.json"));
            case PNPM -> Files.exists(root.resolve("pnpm-lock.yaml"));
            case BUN -> Files.exists(root.resolve("bun.lockb"))
                    || Files.exists(root.resolve("bun.lock"));
            case YARN -> Files.exists(root.resolve("yarn.lock"));
            case RUST -> Files.exists(root.resolve("Cargo.lock"));
            case GO -> Files.exists(root.resolve("go.sum"));
            case FLUTTER -> Files.exists(root.resolve("pubspec.lock"));
            case DOTNET -> Files.exists(root.resolve("packages.lock.json"));
            case MAVEN, GRADLE, PYTHON, DOCKER, UNKNOWN -> true;
        };
        return new SecurityCheck(
                "lockfile",
                exists,
                "Lockfile",
                "Commit your lockfile to ensure reproducible builds and prevent supply-chain attacks",
                false
        );
    }

    static SecurityCheck checkSecretPatterns(Path root) {
        List<SecretFinding> findings = SecretScanner.scan(root);
        if (findings.isEmpty()) {
            return new SecurityCheck(
                    "secret-patterns", true, "Secret patterns",
                    "No hardcoded secret patterns detected", false
            );
        }
        return new SecurityCheck(
                "secret-patterns", false, "Secret patterns",
                findings.size() + " hardcoded secret(s) found in .env files — "
                        + "use environment injection or a vault", false
        );
    }

    static SecurityCheck checkVaultic(Path root) {
        List<Path> envFiles = EnvFileDetector.detectEnvFiles(root);
        if (envFiles.isEmpty()) {
            return new SecurityCheck(
                    "vaultic", true, "Vaultic",
                    "No .env files — Vaultic not needed", false
            );
        }

        boolean installed = RuntimeChecker.isCommandAvailable("vaultic", "--version");
        boolean initialized = Files.isDirectory(root.resolve(".vaultic"));

        if (installed && initialized) {
            return new SecurityCheck(
                    "vaultic", true, "Vaultic",
                    "Protected by Vaultic", false
            );
        } else if (installed) {
            return new SecurityCheck(
                    "vaultic", false, "Vaultic",
                    "Vaultic installed but not initialized — run 'vaultic init' in project root "
                            + "(see https://github.com/SoftDryzz/Vaultic)", false
            );
        } else {
            return new SecurityCheck(
                    "vaultic", false, "Vaultic",
                    "Install Vaultic for secret encryption: cargo install vaultic "
                            + "or https://github.com/SoftDryzz/Vaultic/releases", false
            );
        }
    }

    // ============================================================
    // UTILITIES
    // ============================================================

    /**
     * Parses a .gitignore file and returns its non-empty, non-comment entries.
     */
    static Set<String> parseGitignore(Path gitignore) {
        Set<String> entries = new HashSet<>();
        if (!Files.exists(gitignore)) {
            return entries;
        }
        try {
            for (String line : Files.readAllLines(gitignore)) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    entries.add(trimmed);
                }
            }
        } catch (IOException e) {
            // Cannot read — return empty
        }
        return entries;
    }

    /**
     * Checks if content contains insecure http:// URLs (not localhost or schemas).
     */
    static boolean containsInsecureHttp(String content) {
        int index = 0;
        while ((index = content.indexOf("http://", index)) != -1) {
            String after = content.substring(index + 7);
            if (!after.startsWith("localhost")
                    && !after.startsWith("127.0.0.1")
                    && !after.startsWith("0.0.0.0")
                    && !after.startsWith("schemas.")
                    && !after.startsWith("www.w3.org")
                    && !after.startsWith("xmlns.")
                    && !after.startsWith("[::1]")) {
                return true;
            }
            index += 7;
        }
        return false;
    }
}
