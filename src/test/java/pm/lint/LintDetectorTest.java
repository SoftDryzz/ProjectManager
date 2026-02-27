package pm.lint;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pm.detector.ProjectType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LintDetector")
class LintDetectorTest {

    @TempDir
    Path tempDir;

    // ============================================================
    // detect()
    // ============================================================

    @Nested
    @DisplayName("detect")
    class Detection {

        @Test
        @DisplayName("returns ESLINT for Node.js project with .eslintrc.json")
        void eslintWithConfig() throws IOException {
            Files.createFile(tempDir.resolve(".eslintrc.json"));
            List<LintTool> tools = LintDetector.detect(ProjectType.NODEJS, tempDir);
            assertEquals(List.of(LintTool.ESLINT), tools);
        }

        @Test
        @DisplayName("returns ESLINT for pnpm project with eslint.config.js")
        void eslintFlatConfig() throws IOException {
            Files.createFile(tempDir.resolve("eslint.config.js"));
            List<LintTool> tools = LintDetector.detect(ProjectType.PNPM, tempDir);
            assertEquals(List.of(LintTool.ESLINT), tools);
        }

        @Test
        @DisplayName("returns empty for Node.js project without ESLint config")
        void noEslintConfig() {
            List<LintTool> tools = LintDetector.detect(ProjectType.NODEJS, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns CLIPPY for Rust project")
        void rustClippy() {
            List<LintTool> tools = LintDetector.detect(ProjectType.RUST, tempDir);
            assertEquals(List.of(LintTool.CLIPPY), tools);
        }

        @Test
        @DisplayName("returns GO_VET for Go project (always available)")
        void goVet() {
            List<LintTool> tools = LintDetector.detect(ProjectType.GO, tempDir);
            assertTrue(tools.contains(LintTool.GO_VET));
        }

        @Test
        @DisplayName("returns DART_ANALYZE for Flutter project")
        void flutterDartAnalyze() {
            List<LintTool> tools = LintDetector.detect(ProjectType.FLUTTER, tempDir);
            assertEquals(List.of(LintTool.DART_ANALYZE), tools);
        }

        @Test
        @DisplayName("returns DOTNET_FORMAT_CHECK for .NET project")
        void dotnetFormat() {
            List<LintTool> tools = LintDetector.detect(ProjectType.DOTNET, tempDir);
            assertEquals(List.of(LintTool.DOTNET_FORMAT_CHECK), tools);
        }

        @Test
        @DisplayName("returns CHECKSTYLE_MAVEN when pom.xml contains checkstyle")
        void mavenCheckstyle() throws IOException {
            Files.writeString(tempDir.resolve("pom.xml"),
                    "<project><build><plugins><plugin>" +
                    "<artifactId>maven-checkstyle-plugin</artifactId>" +
                    "</plugin></plugins></build></project>");
            List<LintTool> tools = LintDetector.detect(ProjectType.MAVEN, tempDir);
            assertEquals(List.of(LintTool.CHECKSTYLE_MAVEN), tools);
        }

        @Test
        @DisplayName("returns empty for Maven project without checkstyle")
        void mavenNoCheckstyle() throws IOException {
            Files.writeString(tempDir.resolve("pom.xml"),
                    "<project><build></build></project>");
            List<LintTool> tools = LintDetector.detect(ProjectType.MAVEN, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns CHECKSTYLE_GRADLE when build.gradle contains checkstyle")
        void gradleCheckstyle() throws IOException {
            Files.writeString(tempDir.resolve("build.gradle"),
                    "plugins { id 'checkstyle' }");
            List<LintTool> tools = LintDetector.detect(ProjectType.GRADLE, tempDir);
            assertEquals(List.of(LintTool.CHECKSTYLE_GRADLE), tools);
        }

        @Test
        @DisplayName("returns CHECKSTYLE_GRADLE when build.gradle.kts contains checkstyle")
        void gradleKtsCheckstyle() throws IOException {
            Files.writeString(tempDir.resolve("build.gradle.kts"),
                    "plugins { id(\"checkstyle\") }");
            List<LintTool> tools = LintDetector.detect(ProjectType.GRADLE, tempDir);
            assertEquals(List.of(LintTool.CHECKSTYLE_GRADLE), tools);
        }

        @Test
        @DisplayName("returns empty for Docker project")
        void dockerEmpty() {
            List<LintTool> tools = LintDetector.detect(ProjectType.DOCKER, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns empty for UNKNOWN project type")
        void unknownEmpty() {
            List<LintTool> tools = LintDetector.detect(ProjectType.UNKNOWN, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns empty for null type")
        void nullType() {
            List<LintTool> tools = LintDetector.detect(null, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns empty for null path")
        void nullPath() {
            List<LintTool> tools = LintDetector.detect(ProjectType.NODEJS, null);
            assertTrue(tools.isEmpty());
        }
    }

    // ============================================================
    // hasEslintConfig()
    // ============================================================

    @Nested
    @DisplayName("hasEslintConfig")
    class EslintConfig {

        @Test
        @DisplayName("detects .eslintrc")
        void eslintrc() throws IOException {
            Files.createFile(tempDir.resolve(".eslintrc"));
            assertTrue(LintDetector.hasEslintConfig(tempDir));
        }

        @Test
        @DisplayName("detects .eslintrc.yml")
        void eslintrcYml() throws IOException {
            Files.createFile(tempDir.resolve(".eslintrc.yml"));
            assertTrue(LintDetector.hasEslintConfig(tempDir));
        }

        @Test
        @DisplayName("detects eslint.config.mjs")
        void eslintConfigMjs() throws IOException {
            Files.createFile(tempDir.resolve("eslint.config.mjs"));
            assertTrue(LintDetector.hasEslintConfig(tempDir));
        }

        @Test
        @DisplayName("detects eslint.config.ts")
        void eslintConfigTs() throws IOException {
            Files.createFile(tempDir.resolve("eslint.config.ts"));
            assertTrue(LintDetector.hasEslintConfig(tempDir));
        }

        @Test
        @DisplayName("returns false when no ESLint config exists")
        void noConfig() {
            assertFalse(LintDetector.hasEslintConfig(tempDir));
        }
    }

    // ============================================================
    // fileContains()
    // ============================================================

    @Nested
    @DisplayName("fileContains")
    class FileContainsTest {

        @Test
        @DisplayName("returns true when file contains text")
        void containsText() throws IOException {
            Path file = tempDir.resolve("pom.xml");
            Files.writeString(file, "<plugin>maven-checkstyle-plugin</plugin>");
            assertTrue(LintDetector.fileContains(file, "checkstyle"));
        }

        @Test
        @DisplayName("returns true case-insensitively")
        void caseInsensitive() throws IOException {
            Path file = tempDir.resolve("build.gradle");
            Files.writeString(file, "apply plugin: 'Checkstyle'");
            assertTrue(LintDetector.fileContains(file, "checkstyle"));
        }

        @Test
        @DisplayName("returns false when file does not contain text")
        void doesNotContain() throws IOException {
            Path file = tempDir.resolve("pom.xml");
            Files.writeString(file, "<project><build></build></project>");
            assertFalse(LintDetector.fileContains(file, "checkstyle"));
        }

        @Test
        @DisplayName("returns false for non-existent file")
        void nonExistent() {
            assertFalse(LintDetector.fileContains(tempDir.resolve("missing.xml"), "checkstyle"));
        }
    }

    // ============================================================
    // LintTool enum
    // ============================================================

    @Nested
    @DisplayName("LintTool enum")
    class LintToolEnumTest {

        @Test
        @DisplayName("all values have non-empty displayName")
        void displayNames() {
            for (LintTool tool : LintTool.values()) {
                assertNotNull(tool.displayName());
                assertFalse(tool.displayName().isBlank());
            }
        }

        @Test
        @DisplayName("all values have non-empty command")
        void commands() {
            for (LintTool tool : LintTool.values()) {
                assertNotNull(tool.command());
                assertFalse(tool.command().isBlank());
            }
        }

        @Test
        @DisplayName("has expected number of values")
        void count() {
            assertEquals(10, LintTool.values().length);
        }
    }
}
