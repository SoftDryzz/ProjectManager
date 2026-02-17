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
        assertEquals(6, values.length);

        assertNotNull(ProjectType.GRADLE);
        assertNotNull(ProjectType.MAVEN);
        assertNotNull(ProjectType.NODEJS);
        assertNotNull(ProjectType.DOTNET);
        assertNotNull(ProjectType.PYTHON);
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
        assertEquals(ProjectType.UNKNOWN, ProjectType.valueOf("UNKNOWN"));
    }
}
