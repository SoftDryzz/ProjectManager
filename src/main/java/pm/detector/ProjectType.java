package pm.detector;

/**
 * Enumeration of project types supported by ProjectManager.
 *
 * Each type represents a different build system or development ecosystem.
 * ProjectManager can auto-detect the type based on files present
 * in the project directory.
 *
 * <p>Supported types:
 * <ul>
 * <li>{@link #GRADLE} - Java/Kotlin projects with Gradle</li>
 * <li>{@link #MAVEN} - Java projects with Maven</li>
 * <li>{@link #NODEJS} - JavaScript/TypeScript projects with npm</li>
 * <li>{@link #DOTNET} - C#/F# projects with dotnet CLI</li>
 * <li>{@link #PYTHON} - Python projects with pip</li>
 * <li>{@link #UNKNOWN} - Unidentified type</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * ProjectType type = ProjectType.GRADLE;
 * System.out.println(type.displayName()); // Output: "Gradle"
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ProjectType {

    /**
     * Java/Kotlin project using Gradle as the build system.
     *
     * <p>Detection: Presence of build.gradle or build.gradle.kts
     * <p>Typical commands: gradle build, gradle run, gradle test
     */
    GRADLE("Gradle"),

    /**
     * Java project using Maven as the build system.
     *
     * <p>Detection: Presence of pom.xml
     * <p>Typical commands: mvn package, mvn exec:java, mvn test
     */
    MAVEN("Maven"),

    /**
     * JavaScript/TypeScript project using npm as the package manager.
     *
     * <p>Detection: Presence of package.json
     * <p>Typical commands: npm run build, npm start, npm test
     */
    NODEJS("Node.js"),

    /**
     * C#/F# project using dotnet CLI.
     *
     * <p>Detection: Presence of *.csproj or *.fsproj files
     * <p>Typical commands: dotnet build, dotnet run, dotnet test
     */
    DOTNET(".NET"),

    /**
     * Python project using pip as the package manager.
     *
     * <p>Detection: Presence of requirements.txt or setup.py
     * <p>Typical commands: pip install -r requirements.txt, python main.py
     */
    PYTHON("Python"),

    /**
     * Unknown or unsupported project type.
     *
     * <p>Used when the type cannot be automatically determined.
     * The user can specify commands manually.
     */
    UNKNOWN("Unknown");

    /**
     * Readable name of the project type.
     * Used for display in the user interface.
     */
    private final String displayName;

    /**
     * Enum constructor.
     *
     * @param displayName readable name to show to the user
     */
    ProjectType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the readable name of the project type.
     *
     * @return project type name (e.g., "Gradle", "Maven")
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Checks if the type is known (not UNKNOWN).
     *
     * @return true if the type was correctly detected, false if it is UNKNOWN
     */
    public boolean isKnown() {
        return this != UNKNOWN;
    }
}