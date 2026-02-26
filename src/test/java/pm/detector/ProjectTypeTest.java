package pm.detector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectType")
class ProjectTypeTest {

    @Test
    @DisplayName("All enum values exist")
    void allValuesExist() {
        ProjectType[] values = ProjectType.values();
        assertEquals(13, values.length);

        assertNotNull(ProjectType.GRADLE);
        assertNotNull(ProjectType.MAVEN);
        assertNotNull(ProjectType.NODEJS);
        assertNotNull(ProjectType.DOTNET);
        assertNotNull(ProjectType.PYTHON);
        assertNotNull(ProjectType.RUST);
        assertNotNull(ProjectType.GO);
        assertNotNull(ProjectType.PNPM);
        assertNotNull(ProjectType.BUN);
        assertNotNull(ProjectType.YARN);
        assertNotNull(ProjectType.FLUTTER);
        assertNotNull(ProjectType.DOCKER);
        assertNotNull(ProjectType.UNKNOWN);
    }

    @Test
    @DisplayName("displayName returns correct names")
    void displayNameWorks() {
        assertEquals("Gradle", ProjectType.GRADLE.displayName());
        assertEquals("Maven", ProjectType.MAVEN.displayName());
        assertEquals("Node.js", ProjectType.NODEJS.displayName());
        assertEquals(".NET", ProjectType.DOTNET.displayName());
        assertEquals("Python", ProjectType.PYTHON.displayName());
        assertEquals("Rust", ProjectType.RUST.displayName());
        assertEquals("Go", ProjectType.GO.displayName());
        assertEquals("pnpm", ProjectType.PNPM.displayName());
        assertEquals("Bun", ProjectType.BUN.displayName());
        assertEquals("Yarn", ProjectType.YARN.displayName());
        assertEquals("Flutter", ProjectType.FLUTTER.displayName());
        assertEquals("Docker", ProjectType.DOCKER.displayName());
        assertEquals("Unknown", ProjectType.UNKNOWN.displayName());
    }

    @Test
    @DisplayName("isKnown returns true for known types")
    void isKnownTrue() {
        assertTrue(ProjectType.GRADLE.isKnown());
        assertTrue(ProjectType.MAVEN.isKnown());
        assertTrue(ProjectType.NODEJS.isKnown());
        assertTrue(ProjectType.DOTNET.isKnown());
        assertTrue(ProjectType.PYTHON.isKnown());
        assertTrue(ProjectType.RUST.isKnown());
        assertTrue(ProjectType.GO.isKnown());
        assertTrue(ProjectType.PNPM.isKnown());
        assertTrue(ProjectType.BUN.isKnown());
        assertTrue(ProjectType.YARN.isKnown());
        assertTrue(ProjectType.FLUTTER.isKnown());
        assertTrue(ProjectType.DOCKER.isKnown());
    }

    @Test
    @DisplayName("isKnown returns false for UNKNOWN")
    void isKnownFalse() {
        assertFalse(ProjectType.UNKNOWN.isKnown());
    }

    @Test
    @DisplayName("valueOf works for all types")
    void valueOfWorks() {
        assertEquals(ProjectType.GRADLE, ProjectType.valueOf("GRADLE"));
        assertEquals(ProjectType.MAVEN, ProjectType.valueOf("MAVEN"));
        assertEquals(ProjectType.NODEJS, ProjectType.valueOf("NODEJS"));
        assertEquals(ProjectType.DOTNET, ProjectType.valueOf("DOTNET"));
        assertEquals(ProjectType.PYTHON, ProjectType.valueOf("PYTHON"));
        assertEquals(ProjectType.RUST, ProjectType.valueOf("RUST"));
        assertEquals(ProjectType.GO, ProjectType.valueOf("GO"));
        assertEquals(ProjectType.PNPM, ProjectType.valueOf("PNPM"));
        assertEquals(ProjectType.BUN, ProjectType.valueOf("BUN"));
        assertEquals(ProjectType.YARN, ProjectType.valueOf("YARN"));
        assertEquals(ProjectType.FLUTTER, ProjectType.valueOf("FLUTTER"));
        assertEquals(ProjectType.DOCKER, ProjectType.valueOf("DOCKER"));
        assertEquals(ProjectType.UNKNOWN, ProjectType.valueOf("UNKNOWN"));
    }
}
