package pm.core;

/**
 * Information about a command found in the source code.
 *
 * <p>Used to represent commands discovered while scanning code,
 * for example @Command annotations in Minecraft mods.
 *
 * <p>It is a Record (Java 14+), which provides:
 * <ul>
 * <li>Automatic immutability (cannot be modified after creation)</li>
 * <li>Automatically generated equals(), hashCode(), and toString()</li>
 * <li>Automatic getters (without the 'get' prefix)</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * CommandInfo cmd = new CommandInfo(
 * "fly",                      // command name
 * "FlyCommand.java",          // file where it is located
 * 12,                         // line number in the file
 * "Toggle flight mode"        // description
 * );
 *
 * System.out.println(cmd.fullCommand()); // ".fly"
 * System.out.println(cmd.display());     // "  .fly            FlyCommand.java:12"
 * }</pre>
 *
 * @param name command name (e.g., "fly", "speed")
 * @param file name of the file where it was found (e.g., "FlyCommand.java")
 * @param line line number in the file (1-indexed)
 * @param description command description (can be null)
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public record CommandInfo(
        String name,
        String file,
        int line,
        String description
) {

    /**
     * Compact constructor with validation.
     *
     * Records allow this special constructor to validate
     * parameters before assigning them.
     *
     * @throws IllegalArgumentException if name or file are empty, or line is negative
     */
    public CommandInfo {
        // Validation for 'name'
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Command name cannot be null or blank");
        }

        // Validation for 'file'
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException("File cannot be null or blank");
        }

        // Validation for 'line'
        if (line < 1) {
            throw new IllegalArgumentException("Line must be positive (1-indexed)");
        }

        // 'description' can be null (command without description)
    }

    /**
     * Gets the full command with a prefix.
     *
     * In many Minecraft clients, commands start with a dot.
     *
     * @return command with "." prefix (e.g., ".fly")
     */
    public String fullCommand() {
        return "." + name;
    }

    /**
     * Compact format for console display.
     *
     * Format: "  .command        File.java:123"
     * The command is aligned to 15 characters for organized columns.
     *
     * @return formatted string for display
     */
    public String display() {
        return "  %-15s  %s:%d".formatted(fullCommand(), file, line);
    }

    /**
     * Full format with description.
     *
     * Includes the command, file, line, and description across multiple lines.
     * If there is no description, it shows "No description".
     *
     * @return formatted string with all information
     */
    public String fullDisplay() {
        String desc = (description != null && !description.isBlank())
                ? description
                : "No description";

        return """
               %s
                 File: %s:%d
                 Description: %s
               """.formatted(fullCommand(), file, line, desc);
    }

    /**
     * Checks if the command has a description.
     *
     * @return true if it has a non-empty description, false otherwise
     */
    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }
}