package pm.util;

import pm.core.Project;
import pm.detector.ProjectType;

/**
 * Configures default commands based on the project type.
 *
 * <p>Each project type has standard commands:
 * <ul>
 * <li>Gradle: gradle build, gradle run, gradle test</li>
 * <li>Maven: mvn package, mvn exec:java, mvn test</li>
 * <li>Node.js: npm run build, npm start, npm test</li>
 * <li>.NET: dotnet build, dotnet run, dotnet test</li>
 * </ul>
 *
 * <p>Commands are automatically added when registering a project.
 * The user can overwrite them later if desired.
 *
 * <p>Usage example:
 * <pre>{@code
 * Project project = new Project("myapp", path, ProjectType.GRADLE);
 * CommandConfigurator.configure(project);
 *
 * // Now project has commands:
 * // build -> "gradle build"
 * // run   -> "gradle run"
 * // test  -> "gradle test"
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.3.3
 * @since 1.0.0
 */
public class CommandConfigurator {

    /**
     * Private constructor - utility class.
     */
    private CommandConfigurator() {
        throw new AssertionError("CommandConfigurator cannot be instantiated");
    }

    /**
     * Configures default commands based on the project type.
     *
     * <p>If the project already has commands, they are NOT overwritten.
     * Only missing ones are added.
     *
     * @param project project to add commands to
     * @throws IllegalArgumentException if project is null
     */
    public static void configureDefaultCommands(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        ProjectType type = project.type();

        switch (type) {
            case GRADLE -> configureGradle(project);
            case MAVEN -> configureMaven(project);
            case NODEJS -> configureNodeJS(project);
            case DOTNET -> configureDotNet(project);
            case PYTHON -> configurePython(project);
            case RUST -> configureRust(project);
            case GO -> configureGo(project);
            case PNPM -> configurePnpm(project);
            case BUN -> configureBun(project);
            case YARN -> configureYarn(project);
            case FLUTTER -> configureFlutter(project);
            case DOCKER -> configureDocker(project);
            case UNKNOWN -> {
                // Do not configure commands for unknown projects
                // The user will have to add them manually
            }
        }
    }

    /**
     * Configures commands for Gradle projects.
     *
     * @param project Gradle project
     */
    private static void configureGradle(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_GRADLE);
        addIfAbsent(project, "run", Constants.RUN_GRADLE);
        addIfAbsent(project, "test", Constants.TEST_GRADLE);
        addIfAbsent(project, "clean", Constants.CLEAN_GRADLE);
    }

    /**
     * Configures commands for Maven projects.
     *
     * @param project Maven project
     */
    private static void configureMaven(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_MAVEN);
        addIfAbsent(project, "run", Constants.RUN_MAVEN);
        addIfAbsent(project, "test", Constants.TEST_MAVEN);
        addIfAbsent(project, "clean", Constants.CLEAN_MAVEN);
    }

    /**
     * Configures commands for Node.js projects.
     *
     * @param project Node.js project
     */
    private static void configureNodeJS(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_NPM);
        addIfAbsent(project, "run", Constants.RUN_NPM);
        addIfAbsent(project, "test", Constants.TEST_NPM);
    }

    /**
     * Configures commands for .NET projects.
     *
     * @param project .NET project
     */
    private static void configureDotNet(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_DOTNET);
        addIfAbsent(project, "run", Constants.RUN_DOTNET);
        addIfAbsent(project, "test", Constants.TEST_DOTNET);
    }

    /**
     * Configures commands for Python projects.
     *
     * @param project Python project
     */
    private static void configurePython(Project project) {
        // Python does not have universal standard commands
        // It depends heavily on the project (Django, Flask, simple script, etc.)
        // Let the user configure them manually
    }

    private static void configureRust(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_CARGO);
        addIfAbsent(project, "run", Constants.RUN_CARGO);
        addIfAbsent(project, "test", Constants.TEST_CARGO);
        addIfAbsent(project, "clean", Constants.CLEAN_CARGO);
    }

    private static void configureGo(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_GO);
        addIfAbsent(project, "run", Constants.RUN_GO);
        addIfAbsent(project, "test", Constants.TEST_GO);
        addIfAbsent(project, "clean", Constants.CLEAN_GO);
    }

    private static void configurePnpm(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_PNPM);
        addIfAbsent(project, "run", Constants.RUN_PNPM);
        addIfAbsent(project, "test", Constants.TEST_PNPM);
    }

    private static void configureBun(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_BUN);
        addIfAbsent(project, "run", Constants.RUN_BUN);
        addIfAbsent(project, "test", Constants.TEST_BUN);
    }

    private static void configureYarn(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_YARN);
        addIfAbsent(project, "run", Constants.RUN_YARN);
        addIfAbsent(project, "test", Constants.TEST_YARN);
    }

    private static void configureFlutter(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_FLUTTER);
        addIfAbsent(project, "run", Constants.RUN_FLUTTER);
        addIfAbsent(project, "test", Constants.TEST_FLUTTER);
        addIfAbsent(project, "clean", Constants.CLEAN_FLUTTER);
    }

    /**
     * Configures commands for Docker Compose projects.
     *
     * @param project Docker Compose project
     */
    private static void configureDocker(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_DOCKER);
        addIfAbsent(project, "run", Constants.RUN_DOCKER);
        addIfAbsent(project, "stop", Constants.STOP_DOCKER);
        addIfAbsent(project, "clean", Constants.CLEAN_DOCKER);
    }

    /**
     * Adds a command only if it doesn't already exist.
     *
     * <p>Prevents overwriting user's custom commands.
     *
     * @param project project
     * @param commandName command name
     * @param commandLine command line
     */
    private static void addIfAbsent(Project project, String commandName, String commandLine) {
        if (!project.hasCommand(commandName)) {
            project.addCommand(commandName, commandLine);
        }
    }
}
