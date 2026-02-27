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
 * Detects available formatting tools for a project based on its type and root directory.
 * Uses the same three-tier detection strategy as {@link LintDetector}.
 */
public final class FormatDetector {

    private static final Set<String> PRETTIER_CONFIG_FILES = Set.of(
            ".prettierrc", ".prettierrc.js", ".prettierrc.cjs", ".prettierrc.json",
            ".prettierrc.yml", ".prettierrc.yaml", ".prettierrc.toml",
            "prettier.config.js", "prettier.config.cjs", "prettier.config.mjs"
    );

    private FormatDetector() {
        throw new AssertionError("Utility class");
    }

    /**
     * Detects all available formatting tools for the given project.
     *
     * @param type        the project type
     * @param projectRoot the project root directory
     * @return list of detected formatting tools (may be empty, never null)
     */
    public static List<FormatTool> detect(ProjectType type, Path projectRoot) {
        if (type == null || projectRoot == null || !Files.isDirectory(projectRoot)) {
            return List.of();
        }

        List<FormatTool> tools = new ArrayList<>();

        switch (type) {
            case NODEJS, PNPM, BUN, YARN -> {
                if (hasPrettierConfig(projectRoot)) {
                    tools.add(FormatTool.PRETTIER);
                }
            }
            case RUST -> tools.add(FormatTool.CARGO_FMT);
            case GO -> tools.add(FormatTool.GOFMT);
            case PYTHON -> {
                if (RuntimeChecker.getVersion("ruff", "--version") != null) {
                    tools.add(FormatTool.RUFF_FORMAT);
                }
                if (RuntimeChecker.getVersion("black", "--version") != null) {
                    tools.add(FormatTool.BLACK);
                }
            }
            case FLUTTER -> tools.add(FormatTool.DART_FORMAT);
            case DOTNET -> tools.add(FormatTool.DOTNET_FORMAT);
            case MAVEN -> {
                if (LintDetector.fileContains(projectRoot.resolve("pom.xml"), "spotless")) {
                    tools.add(FormatTool.SPOTLESS_MAVEN);
                }
            }
            case GRADLE -> {
                if (LintDetector.buildFileContains(projectRoot, "spotless")) {
                    tools.add(FormatTool.SPOTLESS_GRADLE);
                }
            }
            default -> { /* DOCKER, UNKNOWN: no format tools */ }
        }

        return tools;
    }

    /**
     * Checks if the project root contains any Prettier configuration file.
     */
    static boolean hasPrettierConfig(Path projectRoot) {
        try (Stream<Path> files = Files.list(projectRoot)) {
            return files
                    .map(p -> p.getFileName().toString())
                    .anyMatch(PRETTIER_CONFIG_FILES::contains);
        } catch (IOException e) {
            return false;
        }
    }
}
