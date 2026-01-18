package pm.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Command-line argument parser.
 *
 * <p>Parses arguments in the format:
 * <pre>
 * pm add myproject --path /home/user/project --type gradle
 * </pre>
 *
 * <p>Extracts:
 * <ul>
 * <li>Positional arguments (without --): ["add", "myproject"]</li>
 * <li>Value flags (--path /home/...): {path: "/home/..."}</li>
 * <li>Boolean flags (--force): {force: "true"}</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * String[] args = {"add", "myapp", "--path", "/home/user/myapp", "--force"};
 * ArgsParser parser = new ArgsParser(args);
 *
 * String command = parser.getPositional(0);  // "add"
 * String name = parser.getPositional(1);     // "myapp"
 * String path = parser.getFlag("path");      // "/home/user/myapp"
 * boolean force = parser.hasFlag("force");   // true
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArgsParser {

    /**
     * Positional arguments (without --).
     * Index 0 = command, index 1 = first argument, etc.
     */
    private final String[] positional;

    /**
     * Flags with their values.
     * Key: flag name (without --)
     * Value: flag value (or "true" if it is boolean)
     */
    private final Map<String, String> flags;

    /**
     * Constructor.
     * Parses the arguments and separates them into positionals and flags.
     *
     * @param args command-line arguments
     */
    public ArgsParser(String[] args) {
        this.flags = new HashMap<>();

        // Count how many positional arguments exist
        int positionalCount = 0;
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                positionalCount++;
            } else {
                break; // Positionals always come first
            }
        }

        // Extract positional arguments
        this.positional = new String[positionalCount];
        System.arraycopy(args, 0, positional, 0, positionalCount);

        // Parse flags (--key value or --flag)
        for (int i = positionalCount; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                // Remove the "--" prefix
                String key = arg.substring(2);

                // Check if it has a value (next argument does not start with --)
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    // Value flag: --path /home/user
                    String value = args[i + 1];
                    flags.put(key, value);
                    i++; // Skip the value
                } else {
                    // Boolean flag: --force
                    flags.put(key, "true");
                }
            }
        }
    }

    /**
     * Gets a positional argument by index.
     *
     * @param index index (0 = first positional)
     * @return argument or null if it doesn't exist
     */
    public String getPositional(int index) {
        if (index >= 0 && index < positional.length) {
            return positional[index];
        }
        return null;
    }

    /**
     * Gets the value of a flag.
     *
     * @param key flag name (without --)
     * @return flag value or null if it doesn't exist
     */
    public String getFlag(String key) {
        return flags.get(key);
    }

    /**
     * Gets the value of a flag with a default value.
     *
     * @param key flag name
     * @param defaultValue default value if it doesn't exist
     * @return flag value or defaultValue
     */
    public String getFlag(String key, String defaultValue) {
        return flags.getOrDefault(key, defaultValue);
    }

    /**
     * Checks if a flag exists.
     *
     * @param key flag name
     * @return true if the flag exists
     */
    public boolean hasFlag(String key) {
        return flags.containsKey(key);
    }

    /**
     * Gets a flag as a boolean.
     *
     * @param key flag name
     * @return true if the flag exists and is not "false", false otherwise
     */
    public boolean getBooleanFlag(String key) {
        String value = flags.get(key);
        return value != null && !value.equalsIgnoreCase("false");
    }

    /**
     * Gets the total count of positional arguments.
     *
     * @return count of positionals
     */
    public int positionalCount() {
        return positional.length;
    }
}