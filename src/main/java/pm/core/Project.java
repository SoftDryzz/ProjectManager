package pm.core;

import pm.detector.ProjectType;


import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a project registered in ProjectManager.
 *
 * <p>A project contains:
 * <ul>
 * <li>Unique identifying name</li>
 * <li>Path in the file system</li>
 * <li>Project type (Gradle, Maven, etc.)</li>
 * <li>Map of available commands (build, run, test, etc.)</li>
 * <li>Last modification timestamp</li>
 * </ul>
 *
 * <p>The class is immutable in its core fields (name, path, type).
 * Commands can be added/modified dynamically.
 *
 * <p>Usage example:
 * <pre>{@code
 * Path projectPath = Paths.get("/home/user/myapp");
 * Project project = new Project("myapp", projectPath, ProjectType.GRADLE);
 * project.addCommand("build", "gradle build");
 * project.addCommand("run", "gradle run");
 *
 * String buildCmd = project.getCommand("build"); // "gradle build"
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Project {

    /**
     * Unique project name.
     * Used as an identifier to execute commands (e.g., pm build myapp).
     * Immutable after creation.
     */
    private final String name;

    /**
     * Absolute project path in the file system.
     * Points to the root directory of the project.
     * Immutable after creation.
     */
    private final Path path;

    /**
     * Project type (Gradle, Maven, Node.js, etc.).
     * Determines which default commands are used.
     * Immutable after creation.
     */
    private final ProjectType type;

    /**
     * Map of available commands for this project.
     * Key: command name (e.g., "build", "run", "test")
     * Value: shell command to execute (e.g., "gradle build")
     *
     * Can be modified using addCommand() and removeCommand().
     */
    private final Map<String, String> commands;

    /**
     * Timestamp of the project's last modification.
     * Automatically updated when commands are added or removed.
     */
    private Instant lastModified;
    private Map<String, String> envVars;

    /**
     * Creates a new project.
     *
     * @param name unique project name (cannot be null)
     * @param path absolute project path (cannot be null)
     * @param type project type (cannot be null)
     * @throws IllegalArgumentException if name or file are empty, or line is negative
     */
    public Project(String name, Path path, ProjectType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name cannot be null or blank");
        }
        if (path == null) {
            throw new IllegalArgumentException("Project path cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Project type cannot be null");
        }

        this.name = name;
        this.path = path;
        this.type = type;
        this.commands = new HashMap<>();
        this.envVars = new HashMap<>();
        this.lastModified = Instant.now();
    }

    // ============================================================
    // GETTERS (modern style without 'get' prefix)
    // ============================================================

    /**
     * Gets the project name.
     *
     * @return project name
     */
    public String name() {
        return name;
    }

    /**
     * Gets the project path.
     *
     * @return absolute path of the project directory
     */
    public Path path() {
        return path;
    }

    /**
     * Gets the project type.
     *
     * @return project type (GRADLE, MAVEN, etc.)
     */
    public ProjectType type() {
        return type;
    }

    /**
     * Gets an immutable copy of the commands map.
     *
     * Changes to the returned Map do NOT affect the project.
     * Use addCommand() to add commands.
     *
     * @return immutable copy of available commands
     */
    public Map<String, String> commands() {
        return Map.copyOf(commands);
    }

    /**
     * Gets the last modification timestamp.
     *
     * @return instant of the last modification
     */
    public Instant lastModified() {
        return lastModified;
    }

    // ============================================================
    // COMMAND MANAGEMENT
    // ============================================================

    /**
     * Adds a command to the project.
     *
     * If a command with the same name already exists, it is overwritten.
     * Automatically updates the modification timestamp.
     *
     * @param commandName command name (e.g., "build", "run")
     * @param commandLine command line to execute (e.g., "gradle build")
     * @throws NullPointerException if any parameter is null
     */
    public void addCommand(String commandName, String commandLine) {
        Objects.requireNonNull(commandName, "Command name cannot be null");
        Objects.requireNonNull(commandLine, "Command line cannot be null");

        commands.put(commandName, commandLine);
        lastModified = Instant.now();
    }

    /**
     * Gets the command associated with a name.
     *
     * @param commandName name of the command to find
     * @return command line, or null if it does not exist
     */
    public String getCommand(String commandName) {
        return commands.get(commandName);
    }

    /**
     * Checks if a command with the given name exists.
     *
     * @param commandName name of the command to check
     * @return true if the command exists, false otherwise
     */
    public boolean hasCommand(String commandName) {
        return commands.containsKey(commandName);
    }

    /**
     * Removes a command from the project.
     *
     * Does nothing if the command does not exist.
     * Updates the modification timestamp.
     *
     * @param commandName name of the command to remove
     */
    public void removeCommand(String commandName) {
        commands.remove(commandName);
        lastModified = Instant.now();
    }

    /**
     * Gets the number of registered commands.
     *
     * @return number of available commands
     */
    public int commandCount() {
        return commands.size();
    }

    // ============================================================
    // OBJECT METHODS
    // ============================================================

    /**
     * String representation of the project.
     *
     * Format: Project{name='...', type=..., path=...}
     *
     * @return textual representation of the project
     */
    @Override
    public String toString() {
        return "Project{name='%s', type=%s, path=%s, commands=%d}"
                .formatted(name, type.displayName(), path, commands.size());
    }

    /**
     * Compares this project with another object.
     *
     * Two projects are equal if they have the same name and path.
     * Type and commands do not affect equality.
     *
     * @param obj object to compare
     * @return true if they are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Project other)) return false;
        return Objects.equals(name, other.name) &&
                Objects.equals(path, other.path);
    }

    /**
     * Generates the project's hash code.
     *
     * Based on name and path (consistent with equals).
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }
    /**
     * Adds or updates an environment variable.
     *
     * @param key variable name
     * @param value variable value
     */
    public void addEnvVar(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Environment variable key cannot be null or blank");
        }
        if (value == null) {
            throw new IllegalArgumentException("Environment variable value cannot be null");
        }

        this.envVars.put(key, value);
        this.lastModified = Instant.now();
    }

    /**
     * Gets an environment variable.
     *
     * @param key variable name
     * @return variable value or null if it doesn't exist
     */
    public String getEnvVar(String key) {
        return this.envVars.get(key);
    }

    /**
     * Checks if an environment variable exists.
     *
     * @param key variable name
     * @return true if it exists
     */
    public boolean hasEnvVar(String key) {
        return this.envVars.containsKey(key);
    }

    /**
     * Removes an environment variable.
     *
     * @param key variable name
     * @return true if it was removed
     */
    public boolean removeEnvVar(String key) {
        boolean removed = this.envVars.remove(key) != null;
        if (removed) {
            this.lastModified = Instant.now();
        }
        return removed;
    }

    /**
     * Gets all environment variables.
     *
     * @return immutable map of variables
     */
    public Map<String, String> envVars() {
        return Map.copyOf(this.envVars);
    }

    /**
     * Gets the number of configured environment variables.
     *
     * @return count of variables
     */
    public int envVarCount() {
        return this.envVars.size();
    }
}