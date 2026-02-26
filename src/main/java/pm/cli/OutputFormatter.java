package pm.cli;

import pm.core.Project;
import pm.util.GitIntegration;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Output formatter with ANSI colors for the CLI interface.
 *
 * <p>Provides methods for displaying formatted messages with consistent
 * colors and styles throughout the application.
 *
 * <p>Available colors:
 * <ul>
 * <li>Green: success messages</li>
 * <li>Red: error messages</li>
 * <li>Yellow: warnings</li>
 * <li>Blue: information</li>
 * <li>Cyan: highlighted data (paths, values)</li>
 * <li>Gray: secondary information</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * OutputFormatter.success("Project created successfully");
 * OutputFormatter.error("Could not find the file");
 * OutputFormatter.info("Compiling project...");
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.3.3
 * @since 1.0.0
 */
public class OutputFormatter {

    // ANSI color codes
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String GRAY = "\u001B[90m";
    public static final String BOLD = "\u001B[1m";
    public static final String RESET = "\u001B[0m";

    /** Command names that are considered "default" (auto-configured by CommandConfigurator). */
    private static final Set<String> DEFAULT_COMMANDS = Set.of("build", "run", "test", "clean", "stop");

    /**
     * Displays a success message (green, with ✓).
     *
     * @param message message to display
     */
    public static void success(String message) {
        System.out.println(GREEN + "✅ " + message + RESET);
    }

    /**
     * Displays an error message (red, with ✗).
     *
     * @param message message to display
     */
    public static void error(String message) {
        System.out.println(RED + "❌ " + message + RESET);
    }

    /**
     * Displays a warning (yellow, with ⚠).
     *
     * @param message message to display
     */
    public static void warning(String message) {
        System.out.println(YELLOW + "⚠️  " + message + RESET);
    }

    /**
     * Displays an informational message (blue, with ℹ).
     *
     * @param message message to display
     */
    public static void info(String message) {
        System.out.println(BLUE + "ℹ️  " + message + RESET);
    }

    /**
     * Displays a section header (bold, cyan, with underline).
     *
     * @param title section title
     */
    public static void section(String title) {
        System.out.println();
        System.out.println(BOLD + CYAN + title + RESET);
        System.out.println("─".repeat(title.length()));
        System.out.println();
    }

    /**
     * Displays project information in a readable format.
     *
     * @param project project to display
     */
    public static void printProject(Project project) {
        System.out.println(BOLD + project.name() + RESET + " " +
                GRAY + "(" + project.type().displayName() + ")" + RESET);
        System.out.println("  Path: " + CYAN + project.path() + RESET);

        // Show last modification
        Instant lastMod = project.lastModified();
        Duration ago = Duration.between(lastMod, Instant.now());
        String timeAgo = formatDuration(ago);
        System.out.println("  Modified: " + GRAY + timeAgo + " ago" + RESET);

        // Show commands
        if (project.commandCount() > 0) {
            System.out.println("  Commands: " + project.commandCount());
        }

        // Show environment variables
        if (project.envVarCount() > 0) {
            System.out.println("  Environment Variables: " + project.envVarCount());
        }

        // Show hooks
        if (project.hookCount() > 0) {
            System.out.println("  Hooks: " + project.hookCount());
        }

        // Show Git information (if it's a repo)
        printGitInfo(project.path());

        System.out.println();
    }

    /**
     * Displays Git information if the project is a repository.
     *
     * @param projectPath project path
     */
    private static void printGitInfo(Path projectPath) {
        // Check if it's a Git repository
        if (!GitIntegration.isGitRepository(projectPath)) {
            System.out.println();
            System.out.println("  " + GRAY + "Git: not a repository" + RESET);
            return;
        }

        System.out.println();
        System.out.println("  " + BOLD + "Git:" + RESET);

        // Current Branch
        String branch = GitIntegration.getCurrentBranch(projectPath);
        if (branch != null) {
            System.out.println("    Branch: " + GREEN + branch + RESET);
        } else {
            System.out.println("    Branch: " + GRAY + "could not read (is git installed?)" + RESET);
        }

        // Status (modified files, etc.)
        GitIntegration.GitStatus status = GitIntegration.getStatus(projectPath);
        if (status != null) {
            if (status.isClean()) {
                System.out.println("    Status: " + GREEN + "✓ Clean working tree" + RESET);
            } else {
                System.out.println("    Status: " + YELLOW + status.toString() + RESET);
            }
        } else {
            System.out.println("    Status: " + GRAY + "could not read" + RESET);
        }

        // Commits pending push
        int commitsAhead = GitIntegration.getCommitsAhead(projectPath);
        if (commitsAhead > 0) {
            System.out.println("    Unpushed: " + YELLOW + commitsAhead + " commit" +
                    (commitsAhead > 1 ? "s" : "") + RESET);
        } else if (commitsAhead == 0) {
            System.out.println("    Unpushed: " + GREEN + "✓ Up to date" + RESET);
        } else {
            System.out.println("    Unpushed: " + GRAY + "no remote tracking branch" + RESET);
        }
    }

    /**
     * Displays a list of projects in table format.
     *
     * @param projects map of projects (name → project)
     */
    public static void printProjectList(Map<String, Project> projects) {
        if (projects.isEmpty()) {
            System.out.println(GRAY + "No projects registered yet." + RESET);
            System.out.println();
            System.out.println("Add your first project with:");
            System.out.println("  " + CYAN + "pm add <name> --path <path>" + RESET);
            return;
        }

        System.out.println();
        System.out.println("Registered Projects (" + projects.size() + ")");
        System.out.println("─".repeat(23));
        System.out.println();

        for (Project project : projects.values()) {
            printProject(project);
        }
    }

    /**
     * Displays available commands for a project.
     *
     * @param project project to show commands for
     */
    public static void printCommands(Project project) {
        if (project.commandCount() == 0) {
            System.out.println(GRAY + "No commands configured for this project." + RESET);
            return;
        }

        System.out.println();
        System.out.println("Commands for " + BOLD + project.name() + RESET +
                " " + GRAY + "(" + project.type().displayName() + ")" + RESET);
        System.out.println("─".repeat(Math.max(40, "Commands for ".length() + project.name().length())));

        printCommandSections(project.commands());

        // Show environment variables if they exist
        printEnvVars(project);
    }

    /**
     * Displays commands for all registered projects.
     *
     * @param projects map of all projects (name → project)
     */
    public static void printAllCommands(Map<String, Project> projects) {
        System.out.println();
        System.out.println("Commands for All Projects (" + projects.size() + ")");
        System.out.println("─".repeat(40));

        for (Project project : projects.values()) {
            System.out.println();
            System.out.println(BOLD + project.name() + RESET + " " +
                    GRAY + "(" + project.type().displayName() + ")" + RESET);

            if (project.commandCount() == 0) {
                System.out.println("  " + GRAY + "No commands configured" + RESET);
                continue;
            }

            printCommandSections(project.commands());
        }

        System.out.println();
    }

    /**
     * Prints commands split into "Default" and "Custom" sections.
     *
     * <p>Default commands are: build, run, test, clean (auto-configured).
     * Everything else is considered a custom command added by the user.
     *
     * @param commands map of command name → command line
     */
    private static void printCommandSections(Map<String, String> commands) {
        Map<String, String> defaults = new LinkedHashMap<>();
        Map<String, String> custom = new LinkedHashMap<>();

        commands.forEach((name, cmd) -> {
            if (DEFAULT_COMMANDS.contains(name)) {
                defaults.put(name, cmd);
            } else {
                custom.put(name, cmd);
            }
        });

        // Global padding across both sections for alignment
        int maxLen = commands.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        if (!defaults.isEmpty()) {
            System.out.println();
            System.out.println("  " + BOLD + "Default" + RESET);
            defaults.forEach((name, cmd) -> {
                String padding = " ".repeat(maxLen - name.length());
                System.out.println("  " + GREEN + name + RESET + padding + " → " + CYAN + cmd + RESET);
            });
        }

        if (!custom.isEmpty()) {
            System.out.println();
            System.out.println("  " + BOLD + "Custom" + RESET);
            custom.forEach((name, cmd) -> {
                String padding = " ".repeat(maxLen - name.length());
                System.out.println("  " + GREEN + name + RESET + padding + " → " + CYAN + cmd + RESET);
            });
        }
    }

    /**
     * Displays the environment variables of a project.
     *
     * @param project project to show variables for
     */
    public static void printEnvVars(Project project) {
        if (project.envVarCount() == 0) {
            return;
        }

        System.out.println();
        System.out.println(BOLD + "Environment Variables" + RESET);
        System.out.println("─".repeat(Math.max(40, "Environment Variables".length())));
        System.out.println();

        // Calculate padding to align
        int maxKeyLength = project.envVars().keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        // Show each variable
        project.envVars().forEach((key, value) -> {
            String padding = " ".repeat(maxKeyLength - key.length());
            System.out.println("  " + GREEN + key + RESET + padding + " = " + CYAN + value + RESET);
        });
    }

    /**
     * Displays hooks for a project grouped by slot.
     *
     * @param projectName project name for the header
     * @param hooks map of slot → list of scripts
     */
    public static void printHooks(String projectName, Map<String, List<String>> hooks) {
        if (hooks.isEmpty()) {
            System.out.println();
            info("No hooks configured for '" + projectName + "'");
            return;
        }

        System.out.println();
        System.out.println("Hooks for " + BOLD + projectName + RESET);
        System.out.println("─".repeat(Math.max(40, "Hooks for ".length() + projectName.length())));

        hooks.forEach((slot, scripts) -> {
            System.out.println();
            System.out.println("  " + BOLD + slot + RESET);
            for (int i = 0; i < scripts.size(); i++) {
                System.out.println("  " + GRAY + (i + 1) + "." + RESET + " " +
                        CYAN + scripts.get(i) + RESET);
            }
        });

        System.out.println();
    }

    /**
     * Formats a duration into a readable string.
     *
     * <p>Examples:
     * <ul>
     * <li>45 seconds → "45 seconds"</li>
     * <li>2 minutes → "2 minutes"</li>
     * <li>1 hour → "1 hour"</li>
     * <li>3 days → "3 days"</li>
     * </ul>
     *
     * @param duration duration to format
     * @return formatted string
     */
    private static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        }

        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        }

        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        }

        long days = hours / 24;
        return days + " day" + (days != 1 ? "s" : "");
    }
}