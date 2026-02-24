package pm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UpdateChecker")
class UpdateCheckerTest {

    // ============================================================
    // extractTagName - JSON parsing
    // ============================================================

    @Test
    @DisplayName("Extracts tag_name from GitHub API JSON")
    void extractsTagName() {
        String json = """
                {"tag_name":"v1.3.0","name":"Release 1.3.0","draft":false}""";
        assertEquals("1.3.0", UpdateChecker.extractTagName(json));
    }

    @Test
    @DisplayName("Extracts tag_name with spaces around colon")
    void extractsTagNameWithSpaces() {
        String json = """
                {"tag_name" : "v2.0.1", "name": "Release 2.0.1"}""";
        assertEquals("2.0.1", UpdateChecker.extractTagName(json));
    }

    @Test
    @DisplayName("Extracts tag_name without v prefix")
    void extractsTagNameWithoutV() {
        String json = """
                {"tag_name":"1.5.0","name":"Release 1.5.0"}""";
        assertEquals("1.5.0", UpdateChecker.extractTagName(json));
    }

    @Test
    @DisplayName("Returns null for null JSON")
    void extractTagNameNull() {
        assertNull(UpdateChecker.extractTagName(null));
    }

    @Test
    @DisplayName("Returns null for empty JSON")
    void extractTagNameEmpty() {
        assertNull(UpdateChecker.extractTagName(""));
    }

    @Test
    @DisplayName("Returns null for JSON without tag_name")
    void extractTagNameMissing() {
        String json = """
                {"name":"Release 1.0.0","draft":false}""";
        assertNull(UpdateChecker.extractTagName(json));
    }

    @Test
    @DisplayName("Handles tag_name in middle of large JSON")
    void extractTagNameLargeJson() {
        String json = "{\"url\":\"https://api.github.com/repos/SoftDryzz/ProjectManager/releases/1\"," +
                "\"assets_url\":\"https://example.com\"," +
                "\"tag_name\":\"v1.2.0\"," +
                "\"target_commitish\":\"main\"," +
                "\"name\":\"v1.2.0\"}";
        assertEquals("1.2.0", UpdateChecker.extractTagName(json));
    }

    // ============================================================
    // isNewerVersion - Semantic version comparison
    // ============================================================

    @Test
    @DisplayName("Detects newer major version")
    void newerMajorVersion() {
        assertTrue(UpdateChecker.isNewerVersion("2.0.0", "1.2.0"));
    }

    @Test
    @DisplayName("Detects newer minor version")
    void newerMinorVersion() {
        assertTrue(UpdateChecker.isNewerVersion("1.3.0", "1.2.0"));
    }

    @Test
    @DisplayName("Detects newer patch version")
    void newerPatchVersion() {
        assertTrue(UpdateChecker.isNewerVersion("1.2.1", "1.2.0"));
    }

    @Test
    @DisplayName("Same version is not newer")
    void sameVersionNotNewer() {
        assertFalse(UpdateChecker.isNewerVersion("1.2.0", "1.2.0"));
    }

    @Test
    @DisplayName("Older version is not newer")
    void olderVersionNotNewer() {
        assertFalse(UpdateChecker.isNewerVersion("1.1.0", "1.2.0"));
    }

    @Test
    @DisplayName("Older major is not newer even with higher minor")
    void olderMajorNotNewer() {
        assertFalse(UpdateChecker.isNewerVersion("0.9.9", "1.0.0"));
    }

    @Test
    @DisplayName("Handles null latest version")
    void nullLatestVersion() {
        assertFalse(UpdateChecker.isNewerVersion(null, "1.2.0"));
    }

    @Test
    @DisplayName("Handles null current version")
    void nullCurrentVersion() {
        assertFalse(UpdateChecker.isNewerVersion("1.3.0", null));
    }

    @Test
    @DisplayName("Handles version with only major.minor")
    void versionWithoutPatch() {
        assertTrue(UpdateChecker.isNewerVersion("1.3", "1.2.0"));
    }

    @Test
    @DisplayName("Handles single number version")
    void singleNumberVersion() {
        assertTrue(UpdateChecker.isNewerVersion("2", "1.2.0"));
    }

    // ============================================================
    // checkForUpdates - should not throw or block
    // ============================================================

    @Test
    @DisplayName("checkForUpdates does not throw even without internet")
    void checkForUpdatesNoThrow() {
        // This should return silently regardless of network state
        assertDoesNotThrow(UpdateChecker::checkForUpdates);
    }

    // ============================================================
    // Cannot instantiate
    // ============================================================

    @Test
    @DisplayName("Cannot be instantiated")
    void cannotInstantiate() throws NoSuchMethodException {
        Constructor<UpdateChecker> constructor = UpdateChecker.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}
