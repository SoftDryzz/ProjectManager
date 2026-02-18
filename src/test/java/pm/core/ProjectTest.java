package pm.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pm.detector.ProjectType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Project")
class ProjectTest {

    private static final String NAME = "test-project";
    private static final Path PATH = Paths.get("/home/user/project");
    private static final ProjectType TYPE = ProjectType.GRADLE;

    private Project createProject() {
        return new Project(NAME, PATH, TYPE);
    }

    // ============================================================
    // CONSTRUCTOR TESTS
    // ============================================================

    @Test
    @DisplayName("Constructor creates project with valid parameters")
    void constructorCreatesProject() {
        Project project = createProject();

        assertEquals(NAME, project.name());
        assertEquals(PATH, project.path());
        assertEquals(TYPE, project.type());
        assertNotNull(project.lastModified());
        assertEquals(0, project.commandCount());
        assertEquals(0, project.envVarCount());
    }

    @Test
    @DisplayName("Constructor throws on null name")
    void constructorThrowsOnNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Project(null, PATH, TYPE));
    }

    @Test
    @DisplayName("Constructor throws on blank name")
    void constructorThrowsOnBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Project("   ", PATH, TYPE));
    }

    @Test
    @DisplayName("Constructor throws on null path")
    void constructorThrowsOnNullPath() {
        assertThrows(IllegalArgumentException.class,
                () -> new Project(NAME, null, TYPE));
    }

    @Test
    @DisplayName("Constructor throws on null type")
    void constructorThrowsOnNullType() {
        assertThrows(IllegalArgumentException.class,
                () -> new Project(NAME, PATH, null));
    }

    // ============================================================
    // COMMAND MANAGEMENT TESTS
    // ============================================================

    @Test
    @DisplayName("addCommand adds and retrieves command")
    void addCommandWorks() {
        Project project = createProject();
        project.addCommand("build", "gradle build");

        assertEquals("gradle build", project.getCommand("build"));
        assertTrue(project.hasCommand("build"));
        assertEquals(1, project.commandCount());
    }

    @Test
    @DisplayName("addCommand overwrites existing command")
    void addCommandOverwrites() {
        Project project = createProject();
        project.addCommand("build", "gradle build");
        project.addCommand("build", "gradle build --info");

        assertEquals("gradle build --info", project.getCommand("build"));
        assertEquals(1, project.commandCount());
    }

    @Test
    @DisplayName("addCommand throws on null command name")
    void addCommandThrowsOnNullName() {
        Project project = createProject();
        assertThrows(NullPointerException.class,
                () -> project.addCommand(null, "gradle build"));
    }

    @Test
    @DisplayName("addCommand throws on null command line")
    void addCommandThrowsOnNullLine() {
        Project project = createProject();
        assertThrows(NullPointerException.class,
                () -> project.addCommand("build", null));
    }

    @Test
    @DisplayName("addCommand updates lastModified")
    void addCommandUpdatesTimestamp() throws InterruptedException {
        Project project = createProject();
        Instant before = project.lastModified();
        Thread.sleep(10);
        project.addCommand("build", "gradle build");

        assertTrue(project.lastModified().isAfter(before));
    }

    @Test
    @DisplayName("getCommand returns null for non-existent command")
    void getCommandReturnsNull() {
        Project project = createProject();
        assertNull(project.getCommand("nonexistent"));
    }

    @Test
    @DisplayName("hasCommand returns false for non-existent command")
    void hasCommandReturnsFalse() {
        Project project = createProject();
        assertFalse(project.hasCommand("nonexistent"));
    }

    @Test
    @DisplayName("removeCommand removes existing command")
    void removeCommandWorks() {
        Project project = createProject();
        project.addCommand("build", "gradle build");
        project.removeCommand("build");

        assertFalse(project.hasCommand("build"));
        assertEquals(0, project.commandCount());
    }

    @Test
    @DisplayName("commands() returns immutable copy")
    void commandsReturnsImmutableCopy() {
        Project project = createProject();
        project.addCommand("build", "gradle build");

        Map<String, String> commands = project.commands();
        assertThrows(UnsupportedOperationException.class,
                () -> commands.put("run", "gradle run"));
    }

    // ============================================================
    // ENVIRONMENT VARIABLE TESTS
    // ============================================================

    @Test
    @DisplayName("addEnvVar adds and retrieves variable")
    void addEnvVarWorks() {
        Project project = createProject();
        project.addEnvVar("PORT", "8080");

        assertEquals("8080", project.getEnvVar("PORT"));
        assertTrue(project.hasEnvVar("PORT"));
        assertEquals(1, project.envVarCount());
    }

    @Test
    @DisplayName("addEnvVar throws on null key")
    void addEnvVarThrowsOnNullKey() {
        Project project = createProject();
        assertThrows(IllegalArgumentException.class,
                () -> project.addEnvVar(null, "value"));
    }

    @Test
    @DisplayName("addEnvVar throws on blank key")
    void addEnvVarThrowsOnBlankKey() {
        Project project = createProject();
        assertThrows(IllegalArgumentException.class,
                () -> project.addEnvVar("  ", "value"));
    }

    @Test
    @DisplayName("addEnvVar throws on null value")
    void addEnvVarThrowsOnNullValue() {
        Project project = createProject();
        assertThrows(IllegalArgumentException.class,
                () -> project.addEnvVar("PORT", null));
    }

    @Test
    @DisplayName("removeEnvVar removes existing variable")
    void removeEnvVarWorks() {
        Project project = createProject();
        project.addEnvVar("PORT", "8080");

        assertTrue(project.removeEnvVar("PORT"));
        assertFalse(project.hasEnvVar("PORT"));
    }

    @Test
    @DisplayName("removeEnvVar returns false for non-existent variable")
    void removeEnvVarReturnsFalse() {
        Project project = createProject();
        assertFalse(project.removeEnvVar("NONEXISTENT"));
    }

    @Test
    @DisplayName("envVars() returns immutable copy")
    void envVarsReturnsImmutableCopy() {
        Project project = createProject();
        project.addEnvVar("PORT", "8080");

        Map<String, String> vars = project.envVars();
        assertThrows(UnsupportedOperationException.class,
                () -> vars.put("DEBUG", "true"));
    }

    @Test
    @DisplayName("clearEnvVars removes all variables")
    void clearEnvVarsRemovesAll() {
        Project project = createProject();
        project.addEnvVar("PORT", "8080");
        project.addEnvVar("DEBUG", "true");
        assertEquals(2, project.envVarCount());

        project.clearEnvVars();

        assertEquals(0, project.envVarCount());
        assertFalse(project.hasEnvVar("PORT"));
        assertFalse(project.hasEnvVar("DEBUG"));
    }

    // ============================================================
    // EQUALS / HASHCODE / TOSTRING
    // ============================================================

    @Test
    @DisplayName("equals returns true for same name and path")
    void equalsWorks() {
        Project p1 = new Project(NAME, PATH, ProjectType.GRADLE);
        Project p2 = new Project(NAME, PATH, ProjectType.MAVEN);

        assertEquals(p1, p2);
    }

    @Test
    @DisplayName("equals returns false for different name")
    void equalsDifferentName() {
        Project p1 = new Project("project-a", PATH, TYPE);
        Project p2 = new Project("project-b", PATH, TYPE);

        assertNotEquals(p1, p2);
    }

    @Test
    @DisplayName("equals returns false for different path")
    void equalsDifferentPath() {
        Project p1 = new Project(NAME, Paths.get("/path/a"), TYPE);
        Project p2 = new Project(NAME, Paths.get("/path/b"), TYPE);

        assertNotEquals(p1, p2);
    }

    @Test
    @DisplayName("hashCode is consistent with equals")
    void hashCodeConsistent() {
        Project p1 = new Project(NAME, PATH, ProjectType.GRADLE);
        Project p2 = new Project(NAME, PATH, ProjectType.MAVEN);

        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    @DisplayName("toString contains project info")
    void toStringContainsInfo() {
        Project project = createProject();
        String str = project.toString();

        assertTrue(str.contains(NAME));
        assertTrue(str.contains(TYPE.displayName()));
    }
}
