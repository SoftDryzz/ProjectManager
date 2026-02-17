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

    @Test
    @DisplayName("Returns UNKNOWN for empty directory")
    void returnsUnknownForEmpty() {
        assertEquals(ProjectType.UNKNOWN, ProjectTypeDetector.detect(tempDir));
    }

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
