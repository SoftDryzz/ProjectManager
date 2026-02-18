package pm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pm.detector.ProjectType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RuntimeChecker")
class RuntimeCheckerTest {

    // ============================================================
    // isRuntimeAvailable
    // ============================================================

    @Test
    @DisplayName("Java and Maven are available (this machine runs Maven tests)")
    void javaAndMavenAreAvailable() {
        // We know Java and Maven are installed because we're running this test with mvn
        assertTrue(RuntimeChecker.isRuntimeAvailable(ProjectType.MAVEN));
    }

    @Test
    @DisplayName("Gradle availability depends on installation")
    void gradleAvailabilityDependsOnInstallation() {
        // Gradle may or may not be installed - just verify it returns a boolean
        boolean result = RuntimeChecker.isRuntimeAvailable(ProjectType.GRADLE);
        // If gradle is not installed, should return false even though java is available
        if (!RuntimeChecker.isCommandAvailable("gradle", "--version")) {
            assertFalse(result);
        } else {
            assertTrue(result);
        }
    }

    @Test
    @DisplayName("UNKNOWN type always returns true")
    void unknownAlwaysAvailable() {
        assertTrue(RuntimeChecker.isRuntimeAvailable(ProjectType.UNKNOWN));
    }

    @Test
    @DisplayName("null type returns true")
    void nullTypeReturnsTrue() {
        assertTrue(RuntimeChecker.isRuntimeAvailable(null));
    }

    // ============================================================
    // isCommandAvailable
    // ============================================================

    @Test
    @DisplayName("java -version is available")
    void javaCommandAvailable() {
        assertTrue(RuntimeChecker.isCommandAvailable("java", "-version"));
    }

    @Test
    @DisplayName("nonexistent command is not available")
    void nonexistentCommandNotAvailable() {
        assertFalse(RuntimeChecker.isCommandAvailable("nonexistent_cmd_xyz", "--version"));
    }

    // ============================================================
    // getVersion
    // ============================================================

    @Test
    @DisplayName("getVersion returns non-null for java")
    void getVersionJava() {
        String version = RuntimeChecker.getVersion("java", "-version");
        assertNotNull(version);
        assertFalse(version.isBlank());
    }

    @Test
    @DisplayName("getVersion returns null for nonexistent command")
    void getVersionNonexistent() {
        String version = RuntimeChecker.getVersion("nonexistent_cmd_xyz", "--version");
        assertNull(version);
    }

    // ============================================================
    // checkRuntime (should not exit for available runtimes)
    // ============================================================

    @Test
    @DisplayName("checkRuntime does not exit for UNKNOWN")
    void checkRuntimeUnknown() {
        // Should return normally without calling System.exit
        RuntimeChecker.checkRuntime(ProjectType.UNKNOWN);
    }

    @Test
    @DisplayName("checkRuntime does not exit for null")
    void checkRuntimeNull() {
        RuntimeChecker.checkRuntime(null);
    }

    @Test
    @DisplayName("checkRuntime does not exit for MAVEN (Java is available)")
    void checkRuntimeMaven() {
        // Java is available since we're running this test
        RuntimeChecker.checkRuntime(ProjectType.MAVEN);
    }

    // ============================================================
    // Cannot instantiate
    // ============================================================

    @Test
    @DisplayName("Cannot be instantiated")
    void cannotInstantiate() throws NoSuchMethodException {
        Constructor<RuntimeChecker> constructor = RuntimeChecker.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}
