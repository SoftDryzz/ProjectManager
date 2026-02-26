package pm.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pm.detector.ProjectType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
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
        assertEquals(0, project.hookCount());
        assertFalse(project.hasHooks());
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
    // HOOK MANAGEMENT TESTS
    // ============================================================

    @Test
    @DisplayName("addHook adds and retrieves hook")
    void addHookWorks() {
        Project project = createProject();
        project.addHook("pre-build", "npm run lint");

        List<String> hooks = project.getHooks("pre-build");
        assertEquals(1, hooks.size());
        assertEquals("npm run lint", hooks.get(0));
        assertTrue(project.hasHooks());
        assertEquals(1, project.hookCount());
    }

    @Test
    @DisplayName("addHook supports multiple hooks per slot")
    void addHookMultiplePerSlot() {
        Project project = createProject();
        project.addHook("pre-build", "npm run lint");
        project.addHook("pre-build", "npm run format");

        List<String> hooks = project.getHooks("pre-build");
        assertEquals(2, hooks.size());
        assertEquals("npm run lint", hooks.get(0));
        assertEquals("npm run format", hooks.get(1));
        assertEquals(2, project.hookCount());
    }

    @Test
    @DisplayName("addHook supports multiple slots")
    void addHookMultipleSlots() {
        Project project = createProject();
        project.addHook("pre-build", "npm run lint");
        project.addHook("post-build", "echo done");

        assertEquals(1, project.getHooks("pre-build").size());
        assertEquals(1, project.getHooks("post-build").size());
        assertEquals(2, project.hookCount());
    }

    @Test
    @DisplayName("addHook throws on null slot")
    void addHookThrowsOnNullSlot() {
        Project project = createProject();
        assertThrows(NullPointerException.class,
                () -> project.addHook(null, "echo test"));
    }

    @Test
    @DisplayName("addHook throws on null script")
    void addHookThrowsOnNullScript() {
        Project project = createProject();
        assertThrows(NullPointerException.class,
                () -> project.addHook("pre-build", null));
    }

    @Test
    @DisplayName("addHook throws on blank slot")
    void addHookThrowsOnBlankSlot() {
        Project project = createProject();
        assertThrows(IllegalArgumentException.class,
                () -> project.addHook("  ", "echo test"));
    }

    @Test
    @DisplayName("addHook throws on blank script")
    void addHookThrowsOnBlankScript() {
        Project project = createProject();
        assertThrows(IllegalArgumentException.class,
                () -> project.addHook("pre-build", "  "));
    }

    @Test
    @DisplayName("addHook updates lastModified")
    void addHookUpdatesTimestamp() throws InterruptedException {
        Project project = createProject();
        Instant before = project.lastModified();
        Thread.sleep(10);
        project.addHook("pre-build", "echo test");

        assertTrue(project.lastModified().isAfter(before));
    }

    @Test
    @DisplayName("removeHook removes by exact content")
    void removeHookWorks() {
        Project project = createProject();
        project.addHook("pre-build", "npm run lint");
        project.addHook("pre-build", "npm run format");

        boolean removed = project.removeHook("pre-build", "npm run lint");

        assertTrue(removed);
        assertEquals(1, project.getHooks("pre-build").size());
        assertEquals("npm run format", project.getHooks("pre-build").get(0));
    }

    @Test
    @DisplayName("removeHook cleans up empty slot")
    void removeHookCleansUpSlot() {
        Project project = createProject();
        project.addHook("pre-build", "npm run lint");

        project.removeHook("pre-build", "npm run lint");

        assertFalse(project.hasHooks());
        assertEquals(0, project.hookCount());
        assertTrue(project.getHooks("pre-build").isEmpty());
    }

    @Test
    @DisplayName("removeHook returns false for non-existent hook")
    void removeHookReturnsFalse() {
        Project project = createProject();
        assertFalse(project.removeHook("pre-build", "nonexistent"));
    }

    @Test
    @DisplayName("removeHook returns false for non-existent slot")
    void removeHookReturnsFalseForMissingSlot() {
        Project project = createProject();
        project.addHook("pre-build", "echo test");
        assertFalse(project.removeHook("post-build", "echo test"));
    }

    @Test
    @DisplayName("getHooks returns empty list for non-existent slot")
    void getHooksReturnsEmpty() {
        Project project = createProject();
        List<String> hooks = project.getHooks("pre-build");
        assertNotNull(hooks);
        assertTrue(hooks.isEmpty());
    }

    @Test
    @DisplayName("getHooks returns unmodifiable list")
    void getHooksReturnsUnmodifiable() {
        Project project = createProject();
        project.addHook("pre-build", "echo test");

        List<String> hooks = project.getHooks("pre-build");
        assertThrows(UnsupportedOperationException.class,
                () -> hooks.add("another"));
    }

    @Test
    @DisplayName("hooks() returns unmodifiable copy")
    void hooksReturnsUnmodifiableCopy() {
        Project project = createProject();
        project.addHook("pre-build", "echo test");

        Map<String, List<String>> hooks = project.hooks();
        assertThrows(UnsupportedOperationException.class,
                () -> hooks.put("post-build", List.of("echo done")));
    }

    @Test
    @DisplayName("clearHooks removes all hooks")
    void clearHooksRemovesAll() {
        Project project = createProject();
        project.addHook("pre-build", "echo 1");
        project.addHook("post-build", "echo 2");
        assertEquals(2, project.hookCount());

        project.clearHooks();

        assertEquals(0, project.hookCount());
        assertFalse(project.hasHooks());
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
