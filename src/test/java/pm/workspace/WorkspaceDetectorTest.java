package pm.workspace;

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

@DisplayName("WorkspaceDetector")
class WorkspaceDetectorTest {

    @TempDir
    Path tempDir;

    // ============================================================
    // Cargo workspaces
    // ============================================================

    @Nested
    @DisplayName("Cargo workspaces")
    class CargoWorkspace {

        @Test
        @DisplayName("detects workspace with members")
        void detectsMembers() throws IOException {
            Files.writeString(tempDir.resolve("Cargo.toml"),
                    "[workspace]\nmembers = [\"app\", \"lib\"]\n");
            Files.createDirectories(tempDir.resolve("app"));
            Files.createFile(tempDir.resolve("app/Cargo.toml"));
            Files.createDirectories(tempDir.resolve("lib"));
            Files.createFile(tempDir.resolve("lib/Cargo.toml"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.RUST, tempDir);
            assertEquals(2, modules.size());
            assertTrue(modules.stream().anyMatch(m -> m.name().equals("app")));
            assertTrue(modules.stream().anyMatch(m -> m.name().equals("lib")));
        }

        @Test
        @DisplayName("returns empty when no workspace section")
        void noWorkspaceSection() throws IOException {
            Files.writeString(tempDir.resolve("Cargo.toml"),
                    "[package]\nname = \"myapp\"\n");
            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.RUST, tempDir);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("returns empty for empty members list")
        void emptyMembers() throws IOException {
            Files.writeString(tempDir.resolve("Cargo.toml"),
                    "[workspace]\nmembers = []\n");
            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.RUST, tempDir);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("handles nested paths")
        void nestedPaths() throws IOException {
            Files.writeString(tempDir.resolve("Cargo.toml"),
                    "[workspace]\nmembers = [\"crates/core\", \"crates/cli\"]\n");
            Files.createDirectories(tempDir.resolve("crates/core"));
            Files.createFile(tempDir.resolve("crates/core/Cargo.toml"));
            Files.createDirectories(tempDir.resolve("crates/cli"));
            Files.createFile(tempDir.resolve("crates/cli/Cargo.toml"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.RUST, tempDir);
            assertEquals(2, modules.size());
            assertTrue(modules.stream().anyMatch(m -> m.relativePath().equals("crates/core")));
        }

        @Test
        @DisplayName("skips non-existent member directories")
        void skipsNonExistent() throws IOException {
            Files.writeString(tempDir.resolve("Cargo.toml"),
                    "[workspace]\nmembers = [\"app\", \"missing\"]\n");
            Files.createDirectories(tempDir.resolve("app"));
            Files.createFile(tempDir.resolve("app/Cargo.toml"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.RUST, tempDir);
            assertEquals(1, modules.size());
            assertEquals("app", modules.get(0).name());
        }
    }

    // ============================================================
    // npm/pnpm/yarn workspaces
    // ============================================================

    @Nested
    @DisplayName("npm workspaces")
    class NpmWorkspace {

        @Test
        @DisplayName("detects array format workspaces")
        void arrayFormat() throws IOException {
            Files.writeString(tempDir.resolve("package.json"),
                    "{\"name\": \"monorepo\", \"workspaces\": [\"packages/ui\", \"packages/api\"]}");
            Files.createDirectories(tempDir.resolve("packages/ui"));
            Files.writeString(tempDir.resolve("packages/ui/package.json"), "{}");
            Files.createDirectories(tempDir.resolve("packages/api"));
            Files.writeString(tempDir.resolve("packages/api/package.json"), "{}");

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.NODEJS, tempDir);
            assertEquals(2, modules.size());
            assertTrue(modules.stream().anyMatch(m -> m.name().equals("ui")));
            assertTrue(modules.stream().anyMatch(m -> m.name().equals("api")));
        }

        @Test
        @DisplayName("detects glob pattern workspaces")
        void globPattern() throws IOException {
            Files.writeString(tempDir.resolve("package.json"),
                    "{\"workspaces\": [\"packages/*\"]}");
            Files.createDirectories(tempDir.resolve("packages/lib-a"));
            Files.createDirectories(tempDir.resolve("packages/lib-b"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.NODEJS, tempDir);
            assertEquals(2, modules.size());
        }

        @Test
        @DisplayName("detects object format workspaces")
        void objectFormat() throws IOException {
            Files.writeString(tempDir.resolve("package.json"),
                    "{\"workspaces\": {\"packages\": [\"apps/web\"]}}");
            Files.createDirectories(tempDir.resolve("apps/web"));
            Files.writeString(tempDir.resolve("apps/web/package.json"), "{}");

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.NODEJS, tempDir);
            assertEquals(1, modules.size());
            assertEquals("web", modules.get(0).name());
        }

        @Test
        @DisplayName("returns empty when no workspaces key")
        void noWorkspaces() throws IOException {
            Files.writeString(tempDir.resolve("package.json"),
                    "{\"name\": \"simple-app\"}");
            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.NODEJS, tempDir);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("works with pnpm type")
        void pnpmType() throws IOException {
            Files.writeString(tempDir.resolve("package.json"),
                    "{\"workspaces\": [\"packages/*\"]}");
            Files.createDirectories(tempDir.resolve("packages/core"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.PNPM, tempDir);
            assertEquals(1, modules.size());
        }

        @Test
        @DisplayName("works with yarn type")
        void yarnType() throws IOException {
            Files.writeString(tempDir.resolve("package.json"),
                    "{\"workspaces\": [\"libs/shared\"]}");
            Files.createDirectories(tempDir.resolve("libs/shared"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.YARN, tempDir);
            assertEquals(1, modules.size());
        }
    }

    // ============================================================
    // Gradle multi-project
    // ============================================================

    @Nested
    @DisplayName("Gradle multi-project")
    class GradleWorkspace {

        @Test
        @DisplayName("detects include directives in settings.gradle")
        void settingsGroovy() throws IOException {
            Files.writeString(tempDir.resolve("settings.gradle"),
                    "rootProject.name = 'myapp'\ninclude 'app', 'lib'\n");
            Files.createDirectories(tempDir.resolve("app"));
            Files.createDirectories(tempDir.resolve("lib"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.GRADLE, tempDir);
            assertEquals(2, modules.size());
            assertTrue(modules.stream().anyMatch(m -> m.name().equals("app")));
        }

        @Test
        @DisplayName("detects include in settings.gradle.kts")
        void settingsKts() throws IOException {
            Files.writeString(tempDir.resolve("settings.gradle.kts"),
                    "rootProject.name = \"myapp\"\ninclude(\"app\", \"lib\")\n");
            Files.createDirectories(tempDir.resolve("app"));
            Files.createDirectories(tempDir.resolve("lib"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.GRADLE, tempDir);
            assertEquals(2, modules.size());
        }

        @Test
        @DisplayName("handles colon-prefixed module names")
        void colonPrefix() throws IOException {
            Files.writeString(tempDir.resolve("settings.gradle.kts"),
                    "include(\":app\", \":lib\")\n");
            Files.createDirectories(tempDir.resolve("app"));
            Files.createDirectories(tempDir.resolve("lib"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.GRADLE, tempDir);
            assertEquals(2, modules.size());
        }

        @Test
        @DisplayName("returns empty when no settings file")
        void noSettings() {
            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.GRADLE, tempDir);
            assertTrue(modules.isEmpty());
        }
    }

    // ============================================================
    // Go multi-module
    // ============================================================

    @Nested
    @DisplayName("Go multi-module")
    class GoWorkspace {

        @Test
        @DisplayName("detects sub-modules with go.mod")
        void subModules() throws IOException {
            Files.createFile(tempDir.resolve("go.mod"));
            Files.createDirectories(tempDir.resolve("cmd/server"));
            Files.createFile(tempDir.resolve("cmd/server/go.mod"));
            Files.createDirectories(tempDir.resolve("pkg/utils"));
            Files.createFile(tempDir.resolve("pkg/utils/go.mod"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.GO, tempDir);
            assertEquals(2, modules.size());
            assertTrue(modules.stream().allMatch(m -> m.type() == ProjectType.GO));
        }

        @Test
        @DisplayName("returns empty for single root go.mod")
        void singleRoot() throws IOException {
            Files.createFile(tempDir.resolve("go.mod"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.GO, tempDir);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("excludes root go.mod from results")
        void excludesRoot() throws IOException {
            Files.createFile(tempDir.resolve("go.mod"));
            Files.createDirectories(tempDir.resolve("internal/pkg"));
            Files.createFile(tempDir.resolve("internal/pkg/go.mod"));

            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.GO, tempDir);
            assertEquals(1, modules.size());
            assertEquals("pkg", modules.get(0).name());
        }
    }

    // ============================================================
    // Null safety and edge cases
    // ============================================================

    @Nested
    @DisplayName("null safety")
    class NullSafety {

        @Test
        @DisplayName("returns empty for null type")
        void nullType() {
            List<WorkspaceModule> modules = WorkspaceDetector.detect(null, tempDir);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("returns empty for null path")
        void nullPath() {
            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.RUST, null);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("returns empty for UNKNOWN type")
        void unknownType() {
            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.UNKNOWN, tempDir);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("returns empty for Docker type")
        void dockerType() {
            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.DOCKER, tempDir);
            assertTrue(modules.isEmpty());
        }

        @Test
        @DisplayName("returns empty for Python type")
        void pythonType() {
            List<WorkspaceModule> modules = WorkspaceDetector.detect(ProjectType.PYTHON, tempDir);
            assertTrue(modules.isEmpty());
        }
    }

    // ============================================================
    // Parsing helpers
    // ============================================================

    @Nested
    @DisplayName("parseCargoMembers")
    class CargoMembers {

        @Test
        @DisplayName("parses standard members array")
        void standard() {
            String content = "[workspace]\nmembers = [\"app\", \"lib\"]\n";
            List<String> members = WorkspaceDetector.parseCargoMembers(content);
            assertEquals(List.of("app", "lib"), members);
        }

        @Test
        @DisplayName("parses multiline members")
        void multiline() {
            String content = "[workspace]\nmembers = [\n  \"app\",\n  \"lib\",\n]\n";
            List<String> members = WorkspaceDetector.parseCargoMembers(content);
            assertEquals(List.of("app", "lib"), members);
        }

        @Test
        @DisplayName("returns empty for no members key")
        void noMembers() {
            String content = "[workspace]\n";
            List<String> members = WorkspaceDetector.parseCargoMembers(content);
            assertTrue(members.isEmpty());
        }
    }

    @Nested
    @DisplayName("parseGradleIncludes")
    class GradleIncludes {

        @Test
        @DisplayName("parses double-quoted includes")
        void doubleQuoted() {
            String content = "include(\"app\", \"lib\")\n";
            List<String> includes = WorkspaceDetector.parseGradleIncludes(content);
            assertEquals(List.of("app", "lib"), includes);
        }

        @Test
        @DisplayName("parses single-quoted includes")
        void singleQuoted() {
            String content = "include 'app', 'lib'\n";
            List<String> includes = WorkspaceDetector.parseGradleIncludes(content);
            assertEquals(List.of("app", "lib"), includes);
        }

        @Test
        @DisplayName("strips leading colon from module names")
        void stripsColon() {
            String content = "include(\":app\", \":lib\")\n";
            List<String> includes = WorkspaceDetector.parseGradleIncludes(content);
            assertEquals(List.of("app", "lib"), includes);
        }
    }
}
