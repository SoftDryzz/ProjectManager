package pm.detector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectTypeDetector — detectAll")
class ProjectTypeDetectorSecondaryTest {

    @TempDir
    Path tempDir;

    @Nested
    @DisplayName("detectAll")
    class DetectAll {

        @Test
        @DisplayName("Maven + Docker returns both types")
        void mavenAndDocker() throws IOException {
            Files.createFile(tempDir.resolve("pom.xml"));
            Files.writeString(tempDir.resolve("docker-compose.yml"), "version: '3'");
            List<ProjectType> types = ProjectTypeDetector.detectAll(tempDir);
            assertTrue(types.contains(ProjectType.MAVEN));
            assertTrue(types.contains(ProjectType.DOCKER));
            assertEquals(ProjectType.MAVEN, types.get(0), "Primary type should be first");
        }

        @Test
        @DisplayName("Rust only returns single type")
        void rustOnly() throws IOException {
            Files.createFile(tempDir.resolve("Cargo.toml"));
            List<ProjectType> types = ProjectTypeDetector.detectAll(tempDir);
            assertEquals(List.of(ProjectType.RUST), types);
        }

        @Test
        @DisplayName("Node.js + Docker returns both types")
        void nodejsAndDocker() throws IOException {
            Files.writeString(tempDir.resolve("package.json"), "{}");
            Files.writeString(tempDir.resolve("docker-compose.yml"), "version: '3'");
            List<ProjectType> types = ProjectTypeDetector.detectAll(tempDir);
            assertTrue(types.contains(ProjectType.NODEJS));
            assertTrue(types.contains(ProjectType.DOCKER));
        }

        @Test
        @DisplayName("Gradle + Docker returns both types")
        void gradleAndDocker() throws IOException {
            Files.createFile(tempDir.resolve("build.gradle"));
            Files.writeString(tempDir.resolve("docker-compose.yaml"), "version: '3'");
            List<ProjectType> types = ProjectTypeDetector.detectAll(tempDir);
            assertTrue(types.contains(ProjectType.GRADLE));
            assertTrue(types.contains(ProjectType.DOCKER));
            assertEquals(ProjectType.GRADLE, types.get(0));
        }

        @Test
        @DisplayName("pnpm project detects PNPM and NODEJS (both package.json and lock)")
        void pnpmDetectsBoth() throws IOException {
            Files.createFile(tempDir.resolve("package.json"));
            Files.createFile(tempDir.resolve("pnpm-lock.yaml"));
            List<ProjectType> types = ProjectTypeDetector.detectAll(tempDir);
            assertTrue(types.contains(ProjectType.PNPM));
            assertTrue(types.contains(ProjectType.NODEJS));
        }

        @Test
        @DisplayName("empty directory returns empty list")
        void emptyDir() {
            List<ProjectType> types = ProjectTypeDetector.detectAll(tempDir);
            assertTrue(types.isEmpty());
        }

        @Test
        @DisplayName("UNKNOWN never appears in results")
        void unknownNeverAppears() throws IOException {
            Files.createFile(tempDir.resolve("pom.xml"));
            List<ProjectType> types = ProjectTypeDetector.detectAll(tempDir);
            assertFalse(types.contains(ProjectType.UNKNOWN));
        }

        @Test
        @DisplayName("primary type is always first in list")
        void primaryFirst() throws IOException {
            Files.createFile(tempDir.resolve("Cargo.toml"));
            Files.writeString(tempDir.resolve("docker-compose.yml"), "version: '3'");
            List<ProjectType> types = ProjectTypeDetector.detectAll(tempDir);
            // Rust has higher priority than Docker
            assertEquals(ProjectType.RUST, types.get(0));
        }

        @Test
        @DisplayName("throws on null path")
        void throwsOnNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> ProjectTypeDetector.detectAll(null));
        }

        @Test
        @DisplayName("Go + Docker returns both")
        void goAndDocker() throws IOException {
            Files.createFile(tempDir.resolve("go.mod"));
            Files.writeString(tempDir.resolve("docker-compose.yml"), "version: '3'");
            List<ProjectType> types = ProjectTypeDetector.detectAll(tempDir);
            assertTrue(types.contains(ProjectType.GO));
            assertTrue(types.contains(ProjectType.DOCKER));
        }
    }
}
