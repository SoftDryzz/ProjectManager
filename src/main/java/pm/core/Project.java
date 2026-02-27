package pm.core;

import pm.detector.ProjectType;


import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
 * @version 1.3.3
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
     * Map of hooks for this project.
     * Key: slot name (e.g., "pre-build", "post-run")
     * Value: ordered list of scripts to execute
     */
    private final Map<String, List<String>> hooks;

    /**
     * Secondary project types detected in the project directory.
     * For example, a Maven project may also have Docker and Node.js files.
     * The primary type is stored in the {@link #type} field.
     */
    private final List<ProjectType> secondaryTypes;

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
        this.hooks = new HashMap<>();
        this.secondaryTypes = new ArrayList<>();
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
     * Removes all commands from this project.
     */
    public void clearCommands() {
        commands.clear();
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
     * Removes all environment variables.
     */
    public void clearEnvVars() {
        this.envVars.clear();
        this.lastModified = Instant.now();
    }

    /**
     * Gets the number of configured environment variables.
     *
     * @return count of variables
     */
    public int envVarCount() {
        return this.envVars.size();
    }

    // ============================================================
    // HOOK MANAGEMENT
    // ============================================================

    /**
     * Adds a hook script to the specified slot.
     *
     * @param slot hook slot (e.g., "pre-build", "post-run")
     * @param script shell command to execute
     */
    public void addHook(String slot, String script) {
        Objects.requireNonNull(slot, "Hook slot cannot be null");
        Objects.requireNonNull(script, "Hook script cannot be null");
        if (slot.isBlank()) {
            throw new IllegalArgumentException("Hook slot cannot be blank");
        }
        if (script.isBlank()) {
            throw new IllegalArgumentException("Hook script cannot be blank");
        }

        hooks.computeIfAbsent(slot, k -> new ArrayList<>()).add(script);
        lastModified = Instant.now();
    }

    /**
     * Removes a hook script from the specified slot by exact content match.
     *
     * @param slot hook slot
     * @param script exact script to remove
     * @return true if the script was found and removed
     */
    public boolean removeHook(String slot, String script) {
        List<String> scripts = hooks.get(slot);
        if (scripts == null) {
            return false;
        }

        boolean removed = scripts.remove(script);
        if (removed) {
            if (scripts.isEmpty()) {
                hooks.remove(slot);
            }
            lastModified = Instant.now();
        }
        return removed;
    }

    /**
     * Gets the list of hook scripts for a slot.
     *
     * @param slot hook slot
     * @return unmodifiable list of scripts, or empty list if none
     */
    public List<String> getHooks(String slot) {
        List<String> scripts = hooks.get(slot);
        return scripts != null ? Collections.unmodifiableList(scripts) : List.of();
    }

    /**
     * Checks if any hooks are defined.
     *
     * @return true if at least one hook exists
     */
    public boolean hasHooks() {
        return !hooks.isEmpty();
    }

    /**
     * Gets an unmodifiable copy of the full hooks map.
     *
     * @return map of slot → scripts
     */
    public Map<String, List<String>> hooks() {
        Map<String, List<String>> copy = new HashMap<>();
        hooks.forEach((slot, scripts) -> copy.put(slot, Collections.unmodifiableList(scripts)));
        return Collections.unmodifiableMap(copy);
    }

    /**
     * Removes all hooks.
     */
    public void clearHooks() {
        hooks.clear();
        lastModified = Instant.now();
    }

    /**
     * Gets the total number of hook scripts across all slots.
     *
     * @return total hook count
     */
    public int hookCount() {
        return hooks.values().stream().mapToInt(List::size).sum();
    }

    // ============================================================
    // SECONDARY TYPE MANAGEMENT
    // ============================================================

    /**
     * Gets an unmodifiable copy of the secondary types list.
     *
     * @return list of secondary project types (may be empty, never null)
     */
    public List<ProjectType> secondaryTypes() {
        return List.copyOf(secondaryTypes);
    }

    /**
     * Adds a secondary type if not already present and different from the primary type.
     *
     * @param type the secondary type to add
     */
    public void addSecondaryType(ProjectType type) {
        Objects.requireNonNull(type, "Secondary type cannot be null");
        if (type != this.type && !secondaryTypes.contains(type)) {
            secondaryTypes.add(type);
            lastModified = Instant.now();
        }
    }

    /**
     * Replaces all secondary types.
     * Filters out the primary type and duplicates.
     *
     * @param types the new secondary types
     */
    public void setSecondaryTypes(List<ProjectType> types) {
        secondaryTypes.clear();
        if (types != null) {
            for (ProjectType t : types) {
                if (t != null && t != this.type && !secondaryTypes.contains(t)) {
                    secondaryTypes.add(t);
                }
            }
        }
        lastModified = Instant.now();
    }

    /**
     * Checks if any secondary types are detected.
     *
     * @return true if at least one secondary type exists
     */
    public boolean hasSecondaryTypes() {
        return !secondaryTypes.isEmpty();
    }
}