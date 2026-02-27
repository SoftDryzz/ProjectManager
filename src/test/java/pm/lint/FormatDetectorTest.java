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

@DisplayName("FormatDetector")
class FormatDetectorTest {

    @TempDir
    Path tempDir;

    // ============================================================
    // detect()
    // ============================================================

    @Nested
    @DisplayName("detect")
    class Detection {

        @Test
        @DisplayName("returns PRETTIER for Node.js project with .prettierrc")
        void prettierWithConfig() throws IOException {
            Files.createFile(tempDir.resolve(".prettierrc"));
            List<FormatTool> tools = FormatDetector.detect(ProjectType.NODEJS, tempDir);
            assertEquals(List.of(FormatTool.PRETTIER), tools);
        }

        @Test
        @DisplayName("returns PRETTIER for Yarn project with prettier.config.js")
        void prettierConfigJs() throws IOException {
            Files.createFile(tempDir.resolve("prettier.config.js"));
            List<FormatTool> tools = FormatDetector.detect(ProjectType.YARN, tempDir);
            assertEquals(List.of(FormatTool.PRETTIER), tools);
        }

        @Test
        @DisplayName("returns empty for Node.js project without Prettier config")
        void noPrettierConfig() {
            List<FormatTool> tools = FormatDetector.detect(ProjectType.NODEJS, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns CARGO_FMT for Rust project")
        void rustCargoFmt() {
            List<FormatTool> tools = FormatDetector.detect(ProjectType.RUST, tempDir);
            assertEquals(List.of(FormatTool.CARGO_FMT), tools);
        }

        @Test
        @DisplayName("returns GOFMT for Go project")
        void goGofmt() {
            List<FormatTool> tools = FormatDetector.detect(ProjectType.GO, tempDir);
            assertEquals(List.of(FormatTool.GOFMT), tools);
        }

        @Test
        @DisplayName("returns DART_FORMAT for Flutter project")
        void flutterDartFormat() {
            List<FormatTool> tools = FormatDetector.detect(ProjectType.FLUTTER, tempDir);
            assertEquals(List.of(FormatTool.DART_FORMAT), tools);
        }

        @Test
        @DisplayName("returns DOTNET_FORMAT for .NET project")
        void dotnetFormat() {
            List<FormatTool> tools = FormatDetector.detect(ProjectType.DOTNET, tempDir);
            assertEquals(List.of(FormatTool.DOTNET_FORMAT), tools);
        }

        @Test
        @DisplayName("returns SPOTLESS_MAVEN when pom.xml contains spotless")
        void mavenSpotless() throws IOException {
            Files.writeString(tempDir.resolve("pom.xml"),
                    "<project><build><plugins><plugin>" +
                    "<artifactId>spotless-maven-plugin</artifactId>" +
                    "</plugin></plugins></build></project>");
            List<FormatTool> tools = FormatDetector.detect(ProjectType.MAVEN, tempDir);
            assertEquals(List.of(FormatTool.SPOTLESS_MAVEN), tools);
        }

        @Test
        @DisplayName("returns empty for Maven project without spotless")
        void mavenNoSpotless() throws IOException {
            Files.writeString(tempDir.resolve("pom.xml"),
                    "<project><build></build></project>");
            List<FormatTool> tools = FormatDetector.detect(ProjectType.MAVEN, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns SPOTLESS_GRADLE when build.gradle contains spotless")
        void gradleSpotless() throws IOException {
            Files.writeString(tempDir.resolve("build.gradle"),
                    "plugins { id 'com.diffplug.spotless' }");
            List<FormatTool> tools = FormatDetector.detect(ProjectType.GRADLE, tempDir);
            assertEquals(List.of(FormatTool.SPOTLESS_GRADLE), tools);
        }

        @Test
        @DisplayName("returns empty for Docker project")
        void dockerEmpty() {
            List<FormatTool> tools = FormatDetector.detect(ProjectType.DOCKER, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns empty for UNKNOWN project type")
        void unknownEmpty() {
            List<FormatTool> tools = FormatDetector.detect(ProjectType.UNKNOWN, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns empty for null type")
        void nullType() {
            List<FormatTool> tools = FormatDetector.detect(null, tempDir);
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("returns empty for null path")
        void nullPath() {
            List<FormatTool> tools = FormatDetector.detect(ProjectType.NODEJS, null);
            assertTrue(tools.isEmpty());
        }
    }

    // ============================================================
    // hasPrettierConfig()
    // ============================================================

    @Nested
    @DisplayName("hasPrettierConfig")
    class PrettierConfig {

        @Test
        @DisplayName("detects .prettierrc")
        void prettierrc() throws IOException {
            Files.createFile(tempDir.resolve(".prettierrc"));
            assertTrue(FormatDetector.hasPrettierConfig(tempDir));
        }

        @Test
        @DisplayName("detects .prettierrc.json")
        void prettierrcJson() throws IOException {
            Files.createFile(tempDir.resolve(".prettierrc.json"));
            assertTrue(FormatDetector.hasPrettierConfig(tempDir));
        }

        @Test
        @DisplayName("detects .prettierrc.toml")
        void prettierrcToml() throws IOException {
            Files.createFile(tempDir.resolve(".prettierrc.toml"));
            assertTrue(FormatDetector.hasPrettierConfig(tempDir));
        }

        @Test
        @DisplayName("detects prettier.config.mjs")
        void prettierConfigMjs() throws IOException {
            Files.createFile(tempDir.resolve("prettier.config.mjs"));
            assertTrue(FormatDetector.hasPrettierConfig(tempDir));
        }

        @Test
        @DisplayName("returns false when no Prettier config exists")
        void noConfig() {
            assertFalse(FormatDetector.hasPrettierConfig(tempDir));
        }
    }

    // ============================================================
    // FormatTool enum
    // ============================================================

    @Nested
    @DisplayName("FormatTool enum")
    class FormatToolEnumTest {

        @Test
        @DisplayName("all values have non-empty displayName")
        void displayNames() {
            for (FormatTool tool : FormatTool.values()) {
                assertNotNull(tool.displayName());
                assertFalse(tool.displayName().isBlank());
            }
        }

        @Test
        @DisplayName("all values have non-empty command")
        void commands() {
            for (FormatTool tool : FormatTool.values()) {
                assertNotNull(tool.command());
                assertFalse(tool.command().isBlank());
            }
        }

        @Test
        @DisplayName("has expected number of values")
        void count() {
            assertEquals(9, FormatTool.values().length);
        }
    }
}
