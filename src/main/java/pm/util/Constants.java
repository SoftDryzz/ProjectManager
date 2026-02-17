package pm.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Global constants for the ProjectManager application.
 *
 * This class centralizes all constant values used in the project:
 * - File paths and configuration directories
 * - Default commands for different project types
 * - File names for project type detection
 *
 * The class is final and has a private constructor to prevent instantiation
 * (utility class pattern).
 *
 * @author SoftDryzz
 * @version 1.1.0
 * @since 1.0.0
 */
public final class Constants {

    /**
     * Private constructor to prevent instantiation.
     * This class only contains static constants.
     *
     * @throws AssertionError always, to indicate that it should not be instantiated
     */
    private Constants() {
        throw new AssertionError("Constants class cannot be instantiated");
    }

    // ============================================================
    // APPLICATION VERSION
    // ============================================================

    /**
     * Current version of ProjectManager.
     * Follows Semantic Versioning (MAJOR.MINOR.PATCH).
     */
    public static final String VERSION = "1.1.0";

    // ============================================================
    // SYSTEM DIRECTORIES AND FILES
    // ============================================================

    /**
     * Home directory of the current user.
     * Obtained from the system property "user.home".
     *
     * Examples:
     * - Linux/Mac: /home/user or /Users/user
     * - Windows: C:\Users\ user
     */
    public static final Path HOME = Paths.get(System.getProperty("user.home"));

    /**
     * Configuration directory for ProjectManager.
     * Created at: ~/.projectmanager/
     *
     * Stored here:
     * - projects.json (list of registered projects)
     * - cache/ (scan cache)
     * - logs/ (execution logs)
     */
    public static final Path CONFIG_DIR = HOME.resolve(".projectmanager");

    /**
     * JSON file containing the list of registered projects.
     * Path: ~/.projectmanager/projects.json
     *
     * File structure:
     * {
     *   "backend-api": {
     *     "name": "backend-api",
     *     "path": "/home/user/projects/backend-api",
     *     "type": "MAVEN",
     *     "commands": {
     *       "build": "mvn package",
     *       "run": "mvn exec:java"
     *     },
     *     "envVars": {
     *       "PORT": "8080",
     *       "DEBUG": "true"
     *     }
     *   }
     * }
     */
    public static final Path PROJECTS_FILE = CONFIG_DIR.resolve("projects.json");
    /**
     * Cache directory to store scanning results.
     * Path: ~/.projectmanager/cache/
     *
     * Used to avoid re-scanning large projects.
     */
    public static final Path CACHE_DIR = CONFIG_DIR.resolve("cache");

    // ============================================================
    // DEFAULT COMMANDS - GRADLE
    // ============================================================

    /**
     * Default command to compile Gradle projects.
     * Executes all build tasks and generates the JAR.
     */
    public static final String BUILD_GRADLE = "gradle build";

    /**
     * Default command to run Gradle projects.
     * Uses the "run" task defined in build.gradle.
     */
    public static final String RUN_GRADLE = "gradle run";

    /**
     * Default command to run tests in Gradle projects.
     * Executes all tests using JUnit.
     */
    public static final String TEST_GRADLE = "gradle test";

    /**
     * Command to clean generated files in Gradle projects.
     * Deletes the build/ directory.
     */
    public static final String CLEAN_GRADLE = "gradle clean";

    // ============================================================
    // DEFAULT COMMANDS - MAVEN
    // ============================================================

    /**
     * Default command to compile Maven projects.
     * Executes phases: compile, test, package.
     */
    public static final String BUILD_MAVEN = "mvn package";

    /**
     * Default command to run Maven projects.
     * Uses the exec-maven-plugin.
     * Requires configuration in pom.xml.
     */
    public static final String RUN_MAVEN = "mvn exec:java";

    /**
     * Default command to run tests in Maven projects.
     * Executes the test phase (JUnit/TestNG).
     */
    public static final String TEST_MAVEN = "mvn test";

    /**
     * Command to clean generated files in Maven projects.
     * Deletes the target/ directory.
     */
    public static final String CLEAN_MAVEN = "mvn clean";

    // ============================================================
    // DEFAULT COMMANDS - NODE.JS
    // ============================================================

    /**
     * Default command to compile Node.js projects.
     * Executes the "build" script defined in package.json.
     */
    public static final String BUILD_NPM = "npm run build";

    /**
     * Default command to run Node.js projects.
     * Executes the "start" script defined in package.json.
     */
    public static final String RUN_NPM = "npm start";

    /**
     * Default command to run tests in Node.js projects.
     * Executes the "test" script (typically Jest, Mocha, etc.).
     */
    public static final String TEST_NPM = "npm test";

    // ============================================================
    // DEFAULT COMMANDS - .NET
    // ============================================================

    /**
     * Default command to compile .NET projects (C#/F#).
     * Compiles the project and generates DLL/EXE.
     */
    public static final String BUILD_DOTNET = "dotnet build";

    /**
     * Default command to run .NET projects.
     * Runs the compiled application.
     */
    public static final String RUN_DOTNET = "dotnet run";

    /**
     * Default command to run tests in .NET projects.
     * Uses xUnit, NUnit, or MSTest.
     */
    public static final String TEST_DOTNET = "dotnet test";

    // ============================================================
    // PROJECT TYPE DETECTION FILES
    // ============================================================

    /**
     * Gradle configuration file name.
     * If this file exists in the project root, it is a Gradle project.
     */
    public static final String FILE_BUILD_GRADLE = "build.gradle";

    /**
     * Gradle configuration file name using Kotlin DSL.
     * Alternative to build.gradle using Kotlin syntax.
     */
    public static final String FILE_BUILD_GRADLE_KTS = "build.gradle.kts";

    /**
     * Maven configuration file name.
     * If this file exists in the project root, it is a Maven project.
     */
    public static final String FILE_POM_XML = "pom.xml";

    /**
     * Node.js configuration file name.
     * Contains npm project metadata and scripts.
     */
    public static final String FILE_PACKAGE_JSON = "package.json";

    /**
     * Extension for .NET project files.
     * Examples: MyApp.csproj, MyLib.fsproj
     */
    public static final String FILE_CSPROJ = ".csproj";

    /**
     * Python dependency file.
     * Contains the list of required pip packages.
     */
    public static final String FILE_REQUIREMENTS_TXT = "requirements.txt";
}