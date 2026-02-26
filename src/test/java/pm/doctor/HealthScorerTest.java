package pm.doctor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pm.core.Project;
import pm.detector.ProjectType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HealthScorer")
class HealthScorerTest {

    @TempDir
    Path tempDir;

    private Project createProject(String name, ProjectType type) {
        return new Project(name, tempDir, type);
    }

    // ============================================================
    // EVALUATE — INDIVIDUAL CHECKS
    // ============================================================

    @Nested
    @DisplayName("Gitignore check")
    class GitignoreCheck {

        @Test
        @DisplayName("passes when .gitignore exists")
        void passesWhenGitignoreExists() throws IOException {
            Files.createFile(tempDir.resolve(".gitignore"));
            Project project = createProject("test", ProjectType.NODEJS);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck gitignore = checks.stream()
                    .filter(c -> "gitignore".equals(c.name())).findFirst().orElseThrow();

            assertTrue(gitignore.passed());
        }

        @Test
        @DisplayName("fails when .gitignore missing")
        void failsWhenGitignoreMissing() {
            Project project = createProject("test", ProjectType.NODEJS);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck gitignore = checks.stream()
                    .filter(c -> "gitignore".equals(c.name())).findFirst().orElseThrow();

            assertFalse(gitignore.passed());
            assertNotNull(gitignore.recommendation());
        }
    }

    @Nested
    @DisplayName("README check")
    class ReadmeCheck {

        @Test
        @DisplayName("passes when README.md exists")
        void passesWithReadmeMd() throws IOException {
            Files.createFile(tempDir.resolve("README.md"));
            Project project = createProject("test", ProjectType.GRADLE);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck readme = checks.stream()
                    .filter(c -> "readme".equals(c.name())).findFirst().orElseThrow();

            assertTrue(readme.passed());
        }

        @Test
        @DisplayName("passes when README exists (no extension)")
        void passesWithReadmeNoExtension() throws IOException {
            Files.createFile(tempDir.resolve("README"));
            Project project = createProject("test", ProjectType.GRADLE);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck readme = checks.stream()
                    .filter(c -> "readme".equals(c.name())).findFirst().orElseThrow();

            assertTrue(readme.passed());
        }

        @Test
        @DisplayName("passes case-insensitive (readme.md)")
        void passesCaseInsensitive() throws IOException {
            Files.createFile(tempDir.resolve("readme.md"));
            Project project = createProject("test", ProjectType.GRADLE);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck readme = checks.stream()
                    .filter(c -> "readme".equals(c.name())).findFirst().orElseThrow();

            assertTrue(readme.passed());
        }

        @Test
        @DisplayName("fails when no README present")
        void failsWhenNoReadme() {
            Project project = createProject("test", ProjectType.GRADLE);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck readme = checks.stream()
                    .filter(c -> "readme".equals(c.name())).findFirst().orElseThrow();

            assertFalse(readme.passed());
        }
    }

    @Nested
    @DisplayName("Tests check")
    class TestsCheck {

        @Test
        @DisplayName("passes when project has test command")
        void passesWithTestCommand() {
            Project project = createProject("test", ProjectType.MAVEN);
            project.addCommand("test", "mvn test");

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck tests = checks.stream()
                    .filter(c -> "tests".equals(c.name())).findFirst().orElseThrow();

            assertTrue(tests.passed());
        }

        @Test
        @DisplayName("fails when project has no test command")
        void failsWithoutTestCommand() {
            Project project = createProject("test", ProjectType.UNKNOWN);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck tests = checks.stream()
                    .filter(c -> "tests".equals(c.name())).findFirst().orElseThrow();

            assertFalse(tests.passed());
            assertTrue(tests.recommendation().contains("pm commands"));
        }
    }

    @Nested
    @DisplayName("CI check")
    class CICheck {

        @Test
        @DisplayName("passes with GitHub Actions")
        void passesWithGitHubActions() throws IOException {
            Files.createDirectories(tempDir.resolve(".github").resolve("workflows"));
            Project project = createProject("test", ProjectType.NODEJS);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck ci = checks.stream()
                    .filter(c -> "ci".equals(c.name())).findFirst().orElseThrow();

            assertTrue(ci.passed());
        }

        @Test
        @DisplayName("passes with GitLab CI")
        void passesWithGitLabCI() throws IOException {
            Files.createFile(tempDir.resolve(".gitlab-ci.yml"));
            Project project = createProject("test", ProjectType.NODEJS);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck ci = checks.stream()
                    .filter(c -> "ci".equals(c.name())).findFirst().orElseThrow();

            assertTrue(ci.passed());
        }

        @Test
        @DisplayName("passes with Jenkinsfile")
        void passesWithJenkinsfile() throws IOException {
            Files.createFile(tempDir.resolve("Jenkinsfile"));
            Project project = createProject("test", ProjectType.NODEJS);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck ci = checks.stream()
                    .filter(c -> "ci".equals(c.name())).findFirst().orElseThrow();

            assertTrue(ci.passed());
        }

        @Test
        @DisplayName("fails when no CI detected")
        void failsWhenNoCI() {
            Project project = createProject("test", ProjectType.NODEJS);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck ci = checks.stream()
                    .filter(c -> "ci".equals(c.name())).findFirst().orElseThrow();

            assertFalse(ci.passed());
        }
    }

    @Nested
    @DisplayName("Lockfile check")
    class LockfileCheck {

        @Test
        @DisplayName("passes for Node.js with package-lock.json")
        void passesNodejsLockfile() throws IOException {
            Files.createFile(tempDir.resolve("package-lock.json"));
            Project project = createProject("test", ProjectType.NODEJS);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("fails for Node.js without lockfile")
        void failsNodejsNoLockfile() {
            Project project = createProject("test", ProjectType.NODEJS);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertFalse(lockfile.passed());
        }

        @Test
        @DisplayName("passes for Rust with Cargo.lock")
        void passesRustLockfile() throws IOException {
            Files.createFile(tempDir.resolve("Cargo.lock"));
            Project project = createProject("test", ProjectType.RUST);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("passes for Go with go.sum")
        void passesGoLockfile() throws IOException {
            Files.createFile(tempDir.resolve("go.sum"));
            Project project = createProject("test", ProjectType.GO);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("passes for pnpm with pnpm-lock.yaml")
        void passesPnpmLockfile() throws IOException {
            Files.createFile(tempDir.resolve("pnpm-lock.yaml"));
            Project project = createProject("test", ProjectType.PNPM);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("passes for Yarn with yarn.lock")
        void passesYarnLockfile() throws IOException {
            Files.createFile(tempDir.resolve("yarn.lock"));
            Project project = createProject("test", ProjectType.YARN);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("passes for Bun with bun.lockb")
        void passesBunLockfile() throws IOException {
            Files.createFile(tempDir.resolve("bun.lockb"));
            Project project = createProject("test", ProjectType.BUN);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("passes for Flutter with pubspec.lock")
        void passesFlutterLockfile() throws IOException {
            Files.createFile(tempDir.resolve("pubspec.lock"));
            Project project = createProject("test", ProjectType.FLUTTER);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("passes for .NET with packages.lock.json")
        void passesDotnetLockfile() throws IOException {
            Files.createFile(tempDir.resolve("packages.lock.json"));
            Project project = createProject("test", ProjectType.DOTNET);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("always passes for Maven (pom.xml is the manifest)")
        void alwaysPassesMaven() {
            Project project = createProject("test", ProjectType.MAVEN);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("always passes for Gradle")
        void alwaysPassesGradle() {
            Project project = createProject("test", ProjectType.GRADLE);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("always passes for Python")
        void alwaysPassesPython() {
            Project project = createProject("test", ProjectType.PYTHON);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("always passes for Docker")
        void alwaysPassesDocker() {
            Project project = createProject("test", ProjectType.DOCKER);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }

        @Test
        @DisplayName("always passes for Unknown type")
        void alwaysPassesUnknown() {
            Project project = createProject("test", ProjectType.UNKNOWN);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            HealthCheck lockfile = checks.stream()
                    .filter(c -> "lockfile".equals(c.name())).findFirst().orElseThrow();

            assertTrue(lockfile.passed());
        }
    }

    // ============================================================
    // EVALUATE — GENERAL
    // ============================================================

    @Test
    @DisplayName("evaluate always returns exactly 5 checks")
    void evaluateReturnsFiveChecks() {
        Project project = createProject("test", ProjectType.NODEJS);
        List<HealthCheck> checks = HealthScorer.evaluate(project);
        assertEquals(5, checks.size());
    }

    @Test
    @DisplayName("evaluate returns checks with correct names")
    void evaluateReturnsCorrectNames() {
        Project project = createProject("test", ProjectType.NODEJS);
        List<HealthCheck> checks = HealthScorer.evaluate(project);

        List<String> names = checks.stream().map(HealthCheck::name).toList();
        assertEquals(List.of("gitignore", "readme", "tests", "ci", "lockfile"), names);
    }

    // ============================================================
    // GRADE
    // ============================================================

    @Nested
    @DisplayName("Grade calculation")
    class GradeCalculation {

        @Test
        @DisplayName("A when all 5 checks pass")
        void gradeA() throws IOException {
            // Set up all checks to pass
            Files.createFile(tempDir.resolve(".gitignore"));
            Files.createFile(tempDir.resolve("README.md"));
            Files.createDirectories(tempDir.resolve(".github").resolve("workflows"));
            // Maven always passes lockfile
            Project project = createProject("test", ProjectType.MAVEN);
            project.addCommand("test", "mvn test");

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            assertEquals('A', HealthScorer.grade(checks));
        }

        @Test
        @DisplayName("B when 4 checks pass")
        void gradeB() throws IOException {
            Files.createFile(tempDir.resolve(".gitignore"));
            Files.createFile(tempDir.resolve("README.md"));
            // No CI → 4/5
            Project project = createProject("test", ProjectType.MAVEN);
            project.addCommand("test", "mvn test");

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            assertEquals('B', HealthScorer.grade(checks));
        }

        @Test
        @DisplayName("C when 3 checks pass")
        void gradeC() throws IOException {
            Files.createFile(tempDir.resolve(".gitignore"));
            // No README, no CI → 3/5 (gitignore + lockfile(Maven) + test)
            Project project = createProject("test", ProjectType.MAVEN);
            project.addCommand("test", "mvn test");

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            assertEquals('C', HealthScorer.grade(checks));
        }

        @Test
        @DisplayName("D when 2 checks pass")
        void gradeD() throws IOException {
            Files.createFile(tempDir.resolve(".gitignore"));
            // No README, no CI, no test → 2/5 (gitignore + lockfile(Maven))
            Project project = createProject("test", ProjectType.MAVEN);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            assertEquals('D', HealthScorer.grade(checks));
        }

        @Test
        @DisplayName("F when 1 or fewer checks pass")
        void gradeF() {
            // No gitignore, no README, no CI, no test, no lockfile
            Project project = createProject("test", ProjectType.NODEJS);

            List<HealthCheck> checks = HealthScorer.evaluate(project);
            assertEquals('F', HealthScorer.grade(checks));
        }
    }

    // ============================================================
    // GRADE COLOR
    // ============================================================

    @Nested
    @DisplayName("Grade colors")
    class GradeColors {

        @Test
        @DisplayName("A is green")
        void aIsGreen() {
            assertTrue(HealthScorer.gradeColor('A').contains("32"));
        }

        @Test
        @DisplayName("B is green")
        void bIsGreen() {
            assertTrue(HealthScorer.gradeColor('B').contains("32"));
        }

        @Test
        @DisplayName("C is yellow")
        void cIsYellow() {
            assertTrue(HealthScorer.gradeColor('C').contains("33"));
        }

        @Test
        @DisplayName("D is red")
        void dIsRed() {
            assertTrue(HealthScorer.gradeColor('D').contains("31"));
        }

        @Test
        @DisplayName("F is red")
        void fIsRed() {
            assertTrue(HealthScorer.gradeColor('F').contains("31"));
        }
    }

    // ============================================================
    // HEALTH CHECK RECORD
    // ============================================================

    @Test
    @DisplayName("HealthCheck record stores all fields correctly")
    void healthCheckRecord() {
        HealthCheck check = new HealthCheck("test-check", true, "Test Description", "Fix it");

        assertEquals("test-check", check.name());
        assertTrue(check.passed());
        assertEquals("Test Description", check.description());
        assertEquals("Fix it", check.recommendation());
    }
}
