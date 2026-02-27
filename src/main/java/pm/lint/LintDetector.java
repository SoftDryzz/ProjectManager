package pm.lint;

import pm.detector.ProjectType;
import pm.util.RuntimeChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Detects available lint tools for a project based on its type and root directory.
 * Uses a three-tier detection strategy:
 * <ol>
 *   <li>Toolchain-bundled tools (always available if runtime is installed)</li>
 *   <li>Config-file-detected tools (config file exists in project root)</li>
 *   <li>Binary-check tools (tool installed globally on system)</li>
 * </ol>
 */
public final class LintDetector {

    private static final Set<String> ESLINT_CONFIG_PREFIXES = Set.of(
            ".eslintrc", ".eslintrc.js", ".eslintrc.cjs", ".eslintrc.json",
            ".eslintrc.yml", ".eslintrc.yaml",
            "eslint.config.js", "eslint.config.mjs", "eslint.config.cjs",
            "eslint.config.ts", "eslint.config.mts"
    );

    private LintDetector() {
        throw new AssertionError("Utility class");
    }

    /**
     * Detects all available lint tools for the given project.
     *
     * @param type        the project type
     * @param projectRoot the project root directory
     * @return list of detected lint tools (may be empty, never null)
     */
    public static List<LintTool> detect(ProjectType type, Path projectRoot) {
        if (type == null || projectRoot == null || !Files.isDirectory(projectRoot)) {
            return List.of();
        }

        List<LintTool> tools = new ArrayList<>();

        switch (type) {
            case NODEJS, PNPM, BUN, YARN -> {
                if (hasEslintConfig(projectRoot)) {
                    tools.add(LintTool.ESLINT);
                }
            }
            case RUST -> tools.add(LintTool.CLIPPY);
            case GO -> {
                tools.add(LintTool.GO_VET);
                if (RuntimeChecker.getVersion("golangci-lint", "--version") != null) {
                    tools.add(LintTool.GOLANGCI_LINT);
                }
            }
            case PYTHON -> {
                if (RuntimeChecker.getVersion("ruff", "--version") != null) {
                    tools.add(LintTool.RUFF_CHECK);
                }
                if (RuntimeChecker.getVersion("flake8", "--version") != null) {
                    tools.add(LintTool.FLAKE8);
                }
            }
            case FLUTTER -> tools.add(LintTool.DART_ANALYZE);
            case DOTNET -> tools.add(LintTool.DOTNET_FORMAT_CHECK);
            case MAVEN -> {
                if (fileContains(projectRoot.resolve("pom.xml"), "checkstyle")) {
                    tools.add(LintTool.CHECKSTYLE_MAVEN);
                }
            }
            case GRADLE -> {
                if (buildFileContains(projectRoot, "checkstyle")) {
                    tools.add(LintTool.CHECKSTYLE_GRADLE);
                }
            }
            default -> { /* DOCKER, UNKNOWN: no lint tools */ }
        }

        return tools;
    }

    /**
     * Checks if the project root contains any ESLint configuration file.
     */
    static boolean hasEslintConfig(Path projectRoot) {
        try (Stream<Path> files = Files.list(projectRoot)) {
            return files
                    .map(p -> p.getFileName().toString())
                    .anyMatch(ESLINT_CONFIG_PREFIXES::contains);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if a file contains the given text (case-insensitive).
     */
    static boolean fileContains(Path file, String text) {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        try {
            String content = Files.readString(file);
            return content.toLowerCase().contains(text.toLowerCase());
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks if build.gradle or build.gradle.kts contains the given text.
     */
    static boolean buildFileContains(Path projectRoot, String text) {
        return fileContains(projectRoot.resolve("build.gradle"), text)
                || fileContains(projectRoot.resolve("build.gradle.kts"), text);
    }
}
