package pm.doctor;

import pm.cli.OutputFormatter;
import pm.core.Project;
import pm.detector.ProjectType;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates project health based on best-practice checks and assigns A–F grades.
 *
 * <p>Each project is scored on 5 equally-weighted checks (20 pts each, 100 total):
 * <ol>
 *   <li>.gitignore exists</li>
 *   <li>README present</li>
 *   <li>Tests configured</li>
 *   <li>CI/CD detected</li>
 *   <li>Dependencies lockfile present</li>
 * </ol>
 *
 * @author SoftDryzz
 * @version 1.6.1
 * @since 1.6.1
 */
public final class HealthScorer {

    private HealthScorer() {}

    /**
     * Runs all health checks on a project and returns the results.
     *
     * @param project project to evaluate
     * @return list of check results (always 5 items)
     */
    public static List<HealthCheck> evaluate(Project project) {
        Path root = project.path();
        List<HealthCheck> checks = new ArrayList<>();

        checks.add(checkGitignore(root));
        checks.add(checkReadme(root));
        checks.add(checkTests(project));
        checks.add(checkCI(root));
        checks.add(checkLockfile(root, project.type()));

        return checks;
    }

    /**
     * Calculates the letter grade from a list of check results.
     *
     * @param checks list of health checks
     * @return grade character: 'A', 'B', 'C', 'D', or 'F'
     */
    public static char grade(List<HealthCheck> checks) {
        long passed = checks.stream().filter(HealthCheck::passed).count();
        return switch ((int) passed) {
            case 5 -> 'A';
            case 4 -> 'B';
            case 3 -> 'C';
            case 2 -> 'D';
            default -> 'F';
        };
    }

    /**
     * Returns the ANSI color code appropriate for a grade.
     *
     * @param grade the letter grade
     * @return ANSI color string
     */
    public static String gradeColor(char grade) {
        return switch (grade) {
            case 'A', 'B' -> OutputFormatter.GREEN;
            case 'C' -> OutputFormatter.YELLOW;
            default -> OutputFormatter.RED;
        };
    }

    // ============================================================
    // INDIVIDUAL CHECKS
    // ============================================================

    private static HealthCheck checkGitignore(Path root) {
        boolean exists = Files.exists(root.resolve(".gitignore"));
        return new HealthCheck(
                "gitignore",
                exists,
                ".gitignore",
                "Create a .gitignore to exclude build artifacts and sensitive files"
        );
    }

    private static HealthCheck checkReadme(Path root) {
        boolean exists = hasFileIgnoreCase(root, "readme.md")
                || hasFileIgnoreCase(root, "readme");
        return new HealthCheck(
                "readme",
                exists,
                "README",
                "Add a README.md to document your project"
        );
    }

    private static HealthCheck checkTests(Project project) {
        boolean hasTest = project.hasCommand("test");
        return new HealthCheck(
                "tests",
                hasTest,
                "Tests",
                "Configure tests: pm commands " + project.name() + " add test \"your-test-command\""
        );
    }

    private static HealthCheck checkCI(Path root) {
        boolean detected = Files.isDirectory(root.resolve(".github").resolve("workflows"))
                || Files.exists(root.resolve(".gitlab-ci.yml"))
                || Files.exists(root.resolve("Jenkinsfile"));
        return new HealthCheck(
                "ci",
                detected,
                "CI/CD",
                "Set up CI/CD for automated testing (GitHub Actions, GitLab CI, Jenkins)"
        );
    }

    private static HealthCheck checkLockfile(Path root, ProjectType type) {
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
            // Maven (pom.xml), Gradle (build.gradle), Python (requirements.txt), Docker
            // always pass — the detection file IS the dependency manifest
            case MAVEN, GRADLE, PYTHON, DOCKER, UNKNOWN -> true;
        };
        return new HealthCheck(
                "lockfile",
                exists,
                "Lockfile",
                "Commit your lockfile to ensure reproducible builds"
        );
    }

    // ============================================================
    // UTILITIES
    // ============================================================

    /**
     * Checks if a file exists in the directory, case-insensitive.
     */
    private static boolean hasFileIgnoreCase(Path dir, String filename) {
        if (!Files.isDirectory(dir)) {
            return false;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (entry.getFileName().toString().equalsIgnoreCase(filename)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // Cannot list directory — treat as not found
        }
        return false;
    }
}
