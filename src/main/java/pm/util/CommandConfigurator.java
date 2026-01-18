package pm.util;

import pm.core.Project;
import pm.detector.ProjectType;

/**
 * Configura comandos por defecto según el tipo de proyecto.
 *
 * <p>Cada tipo de proyecto tiene comandos estándar:
 * <ul>
 *   <li>Gradle: gradle build, gradle run, gradle test</li>
 *   <li>Maven: mvn package, mvn exec:java, mvn test</li>
 *   <li>Node.js: npm run build, npm start, npm test</li>
 *   <li>.NET: dotnet build, dotnet run, dotnet test</li>
 * </ul>
 *
 * <p>Los comandos se agregan automáticamente al registrar un proyecto.
 * El usuario puede sobrescribirlos después si lo desea.
 *
 * <p>Ejemplo de uso:
 * <pre>{@code
 * Project project = new Project("myapp", path, ProjectType.GRADLE);
 * CommandConfigurator.configure(project);
 *
 * // Ahora project tiene comandos:
 * // build -> "gradle build"
 * // run   -> "gradle run"
 * // test  -> "gradle test"
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class CommandConfigurator {

    /**
     * Constructor privado - clase utility.
     */
    private CommandConfigurator() {
        throw new AssertionError("CommandConfigurator cannot be instantiated");
    }

    /**
     * Configura comandos por defecto según el tipo de proyecto.
     *
     * <p>Si el proyecto ya tiene comandos, NO se sobrescriben.
     * Solo se agregan los que faltan.
     *
     * @param project proyecto al que agregar comandos
     * @throws IllegalArgumentException si project es null
     */
    public static void configure(Project project) {
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
            case UNKNOWN -> {
                // No configurar comandos para proyectos desconocidos
                // El usuario tendrá que agregarlos manualmente
            }
        }
    }

    /**
     * Configura comandos para proyectos Gradle.
     *
     * @param project proyecto Gradle
     */
    private static void configureGradle(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_GRADLE);
        addIfAbsent(project, "run", Constants.RUN_GRADLE);
        addIfAbsent(project, "test", Constants.TEST_GRADLE);
        addIfAbsent(project, "clean", Constants.CLEAN_GRADLE);
    }

    /**
     * Configura comandos para proyectos Maven.
     *
     * @param project proyecto Maven
     */
    private static void configureMaven(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_MAVEN);
        addIfAbsent(project, "run", Constants.RUN_MAVEN);
        addIfAbsent(project, "test", Constants.TEST_MAVEN);
        addIfAbsent(project, "clean", Constants.CLEAN_MAVEN);
    }

    /**
     * Configura comandos para proyectos Node.js.
     *
     * @param project proyecto Node.js
     */
    private static void configureNodeJS(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_NPM);
        addIfAbsent(project, "run", Constants.RUN_NPM);
        addIfAbsent(project, "test", Constants.TEST_NPM);
    }

    /**
     * Configura comandos para proyectos .NET.
     *
     * @param project proyecto .NET
     */
    private static void configureDotNet(Project project) {
        addIfAbsent(project, "build", Constants.BUILD_DOTNET);
        addIfAbsent(project, "run", Constants.RUN_DOTNET);
        addIfAbsent(project, "test", Constants.TEST_DOTNET);
    }

    /**
     * Configura comandos para proyectos Python.
     *
     * @param project proyecto Python
     */
    private static void configurePython(Project project) {
        // Python no tiene comandos estándar universales
        // Depende mucho del proyecto (Django, Flask, script simple, etc)
        // Dejar que el usuario los configure manualmente
    }

    /**
     * Agrega un comando solo si no existe ya.
     *
     * <p>Previene sobrescribir comandos personalizados del usuario.
     *
     * @param project proyecto
     * @param commandName nombre del comando
     * @param commandLine línea de comando
     */
    private static void addIfAbsent(Project project, String commandName, String commandLine) {
        if (!project.hasCommand(commandName)) {
            project.addCommand(commandName, commandLine);
        }
    }
}
