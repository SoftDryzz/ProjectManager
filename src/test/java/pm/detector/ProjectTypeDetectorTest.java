package pm.detector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectTypeDetector")
class ProjectTypeDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Detects Gradle project (build.gradle)")
    void detectsGradle() throws IOException {
        Files.createFile(tempDir.resolve("build.gradle"));
        assertEquals(ProjectType.GRADLE, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Gradle Kotlin DSL project (build.gradle.kts)")
    void detectsGradleKts() throws IOException {
        Files.createFile(tempDir.resolve("build.gradle.kts"));
        assertEquals(ProjectType.GRADLE, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Maven project (pom.xml)")
    void detectsMaven() throws IOException {
        Files.createFile(tempDir.resolve("pom.xml"));
        assertEquals(ProjectType.MAVEN, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Node.js project (package.json)")
    void detectsNodejs() throws IOException {
        Files.createFile(tempDir.resolve("package.json"));
        assertEquals(ProjectType.NODEJS, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects .NET project (.csproj)")
    void detectsDotnetCsproj() throws IOException {
        Files.createFile(tempDir.resolve("MyApp.csproj"));
        assertEquals(ProjectType.DOTNET, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects .NET project (.fsproj)")
    void detectsDotnetFsproj() throws IOException {
        Files.createFile(tempDir.resolve("MyApp.fsproj"));
        assertEquals(ProjectType.DOTNET, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Python project (requirements.txt)")
    void detectsPythonRequirements() throws IOException {
        Files.createFile(tempDir.resolve("requirements.txt"));
        assertEquals(ProjectType.PYTHON, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Python project (setup.py)")
    void detectsPythonSetup() throws IOException {
        Files.createFile(tempDir.resolve("setup.py"));
        assertEquals(ProjectType.PYTHON, ProjectTypeDetector.detect(tempDir));
    }

    // ============================================================
    // NEW RUNTIMES: Rust, Go, pnpm, Bun, Yarn
    // ============================================================

    @Test
    @DisplayName("Detects Rust project (Cargo.toml)")
    void detectsRust() throws IOException {
        Files.createFile(tempDir.resolve("Cargo.toml"));
        assertEquals(ProjectType.RUST, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Go project (go.mod)")
    void detectsGo() throws IOException {
        Files.createFile(tempDir.resolve("go.mod"));
        assertEquals(ProjectType.GO, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects pnpm project (pnpm-lock.yaml)")
    void detectsPnpm() throws IOException {
        Files.createFile(tempDir.resolve("pnpm-lock.yaml"));
        Files.createFile(tempDir.resolve("package.json"));
        assertEquals(ProjectType.PNPM, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Bun project (bun.lockb)")
    void detectsBunLockb() throws IOException {
        Files.createFile(tempDir.resolve("bun.lockb"));
        Files.createFile(tempDir.resolve("package.json"));
        assertEquals(ProjectType.BUN, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Bun project (bun.lock)")
    void detectsBunLock() throws IOException {
        Files.createFile(tempDir.resolve("bun.lock"));
        Files.createFile(tempDir.resolve("package.json"));
        assertEquals(ProjectType.BUN, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Yarn project (yarn.lock)")
    void detectsYarn() throws IOException {
        Files.createFile(tempDir.resolve("yarn.lock"));
        Files.createFile(tempDir.resolve("package.json"));
        assertEquals(ProjectType.YARN, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Detects Flutter project (pubspec.yaml)")
    void detectsFlutter() throws IOException {
        Files.createFile(tempDir.resolve("pubspec.yaml"));
        assertEquals(ProjectType.FLUTTER, ProjectTypeDetector.detect(tempDir));
    }

    // ============================================================
    // UNKNOWN & EDGE CASES
    // ============================================================

    @Test
    @DisplayName("Returns UNKNOWN for empty directory")
    void returnsUnknownForEmpty() {
        assertEquals(ProjectType.UNKNOWN, ProjectTypeDetector.detect(tempDir));
    }

    // ============================================================
    // PRIORITY TESTS
    // ============================================================

    @Test
    @DisplayName("Gradle has priority over Maven when both exist")
    void gradlePriorityOverMaven() throws IOException {
        Files.createFile(tempDir.resolve("build.gradle"));
        Files.createFile(tempDir.resolve("pom.xml"));

        assertEquals(ProjectType.GRADLE, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Maven has priority over Node.js when both exist")
    void mavenPriorityOverNodejs() throws IOException {
        Files.createFile(tempDir.resolve("pom.xml"));
        Files.createFile(tempDir.resolve("package.json"));

        assertEquals(ProjectType.MAVEN, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("pnpm has priority over Node.js (package.json + pnpm-lock.yaml)")
    void pnpmPriorityOverNodejs() throws IOException {
        Files.createFile(tempDir.resolve("package.json"));
        Files.createFile(tempDir.resolve("pnpm-lock.yaml"));

        assertEquals(ProjectType.PNPM, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Bun has priority over Node.js (package.json + bun.lockb)")
    void bunPriorityOverNodejs() throws IOException {
        Files.createFile(tempDir.resolve("package.json"));
        Files.createFile(tempDir.resolve("bun.lockb"));

        assertEquals(ProjectType.BUN, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Yarn has priority over Node.js (package.json + yarn.lock)")
    void yarnPriorityOverNodejs() throws IOException {
        Files.createFile(tempDir.resolve("package.json"));
        Files.createFile(tempDir.resolve("yarn.lock"));

        assertEquals(ProjectType.YARN, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Rust has priority over Go when both exist")
    void rustPriorityOverGo() throws IOException {
        Files.createFile(tempDir.resolve("Cargo.toml"));
        Files.createFile(tempDir.resolve("go.mod"));

        assertEquals(ProjectType.RUST, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("pnpm has priority over Yarn when both lock files exist")
    void pnpmPriorityOverYarn() throws IOException {
        Files.createFile(tempDir.resolve("package.json"));
        Files.createFile(tempDir.resolve("pnpm-lock.yaml"));
        Files.createFile(tempDir.resolve("yarn.lock"));

        assertEquals(ProjectType.PNPM, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("pnpm has priority over Bun when both lock files exist")
    void pnpmPriorityOverBun() throws IOException {
        Files.createFile(tempDir.resolve("package.json"));
        Files.createFile(tempDir.resolve("pnpm-lock.yaml"));
        Files.createFile(tempDir.resolve("bun.lockb"));

        assertEquals(ProjectType.PNPM, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Flutter has priority over Node.js (pubspec.yaml + package.json)")
    void flutterPriorityOverNodejs() throws IOException {
        Files.createFile(tempDir.resolve("pubspec.yaml"));
        Files.createFile(tempDir.resolve("package.json"));

        assertEquals(ProjectType.FLUTTER, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Bun has priority over Yarn when both lock files exist")
    void bunPriorityOverYarn() throws IOException {
        Files.createFile(tempDir.resolve("package.json"));
        Files.createFile(tempDir.resolve("bun.lockb"));
        Files.createFile(tempDir.resolve("yarn.lock"));

        assertEquals(ProjectType.BUN, ProjectTypeDetector.detect(tempDir));
    }

    @Test
    @DisplayName("Throws on null path")
    void throwsOnNullPath() {
        assertThrows(IllegalArgumentException.class,
                () -> ProjectTypeDetector.detect(null));
    }

    @Test
    @DisplayName("Throws on non-existent path")
    void throwsOnNonExistentPath() {
        Path fake = tempDir.resolve("nonexistent");
        assertThrows(IllegalArgumentException.class,
                () -> ProjectTypeDetector.detect(fake));
    }

    @Test
    @DisplayName("Throws on file instead of directory")
    void throwsOnFilePath() throws IOException {
        Path file = Files.createFile(tempDir.resolve("file.txt"));
        assertThrows(IllegalArgumentException.class,
                () -> ProjectTypeDetector.detect(file));
    }
}
