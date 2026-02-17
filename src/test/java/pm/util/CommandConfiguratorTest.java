package pm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pm.core.Project;
import pm.detector.ProjectType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommandConfigurator")
class CommandConfiguratorTest {

    private Project createProject(ProjectType type) {
        return new Project("test-project", Paths.get("/home/user/project"), type);
    }

    // ============================================================
    // GRADLE
    // ============================================================

    @Test
    @DisplayName("Configures Gradle default commands")
    void configuresGradle() {
        Project project = createProject(ProjectType.GRADLE);
        CommandConfigurator.configureDefaultCommands(project);

        assertEquals(Constants.BUILD_GRADLE, project.getCommand("build"));
        assertEquals(Constants.RUN_GRADLE, project.getCommand("run"));
        assertEquals(Constants.TEST_GRADLE, project.getCommand("test"));
        assertEquals(Constants.CLEAN_GRADLE, project.getCommand("clean"));
        assertEquals(4, project.commandCount());
    }

    // ============================================================
    // MAVEN
    // ============================================================

    @Test
    @DisplayName("Configures Maven default commands")
    void configuresMaven() {
        Project project = createProject(ProjectType.MAVEN);
        CommandConfigurator.configureDefaultCommands(project);

        assertEquals(Constants.BUILD_MAVEN, project.getCommand("build"));
        assertEquals(Constants.RUN_MAVEN, project.getCommand("run"));
        assertEquals(Constants.TEST_MAVEN, project.getCommand("test"));
        assertEquals(Constants.CLEAN_MAVEN, project.getCommand("clean"));
        assertEquals(4, project.commandCount());
    }

    // ============================================================
    // NODE.JS
    // ============================================================

    @Test
    @DisplayName("Configures Node.js default commands")
    void configuresNodejs() {
        Project project = createProject(ProjectType.NODEJS);
        CommandConfigurator.configureDefaultCommands(project);

        assertEquals(Constants.BUILD_NPM, project.getCommand("build"));
        assertEquals(Constants.RUN_NPM, project.getCommand("run"));
        assertEquals(Constants.TEST_NPM, project.getCommand("test"));
        assertEquals(3, project.commandCount());
    }

    // ============================================================
    // .NET
    // ============================================================

    @Test
    @DisplayName("Configures .NET default commands")
    void configuresDotnet() {
        Project project = createProject(ProjectType.DOTNET);
        CommandConfigurator.configureDefaultCommands(project);

        assertEquals(Constants.BUILD_DOTNET, project.getCommand("build"));
        assertEquals(Constants.RUN_DOTNET, project.getCommand("run"));
        assertEquals(Constants.TEST_DOTNET, project.getCommand("test"));
        assertEquals(3, project.commandCount());
    }

    // ============================================================
    // PYTHON
    // ============================================================

    @Test
    @DisplayName("Python does not add default commands")
    void pythonNoDefaults() {
        Project project = createProject(ProjectType.PYTHON);
        CommandConfigurator.configureDefaultCommands(project);

        assertEquals(0, project.commandCount());
    }

    // ============================================================
    // UNKNOWN
    // ============================================================

    @Test
    @DisplayName("UNKNOWN does not add default commands")
    void unknownNoDefaults() {
        Project project = createProject(ProjectType.UNKNOWN);
        CommandConfigurator.configureDefaultCommands(project);

        assertEquals(0, project.commandCount());
    }

    // ============================================================
    // DOES NOT OVERWRITE EXISTING
    // ============================================================

    @Test
    @DisplayName("Does not overwrite existing commands")
    void doesNotOverwrite() {
        Project project = createProject(ProjectType.GRADLE);
        project.addCommand("build", "custom build command");

        CommandConfigurator.configureDefaultCommands(project);

        assertEquals("custom build command", project.getCommand("build"));
        assertEquals(Constants.RUN_GRADLE, project.getCommand("run"));
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    @Test
    @DisplayName("Throws on null project")
    void throwsOnNull() {
        assertThrows(IllegalArgumentException.class,
                () -> CommandConfigurator.configureDefaultCommands(null));
    }

    @Test
    @DisplayName("Cannot be instantiated")
    void cannotInstantiate() throws NoSuchMethodException {
        Constructor<CommandConfigurator> constructor =
                CommandConfigurator.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}
