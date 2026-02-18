package pm;

import pm.cli.OutputFormatter;
import pm.core.Project;
import pm.detector.ProjectType;
import pm.detector.ProjectTypeDetector;
import pm.executor.CommandExecutor;
import pm.storage.ProjectStore;
import pm.util.ArgsParser;
import pm.util.CommandConfigurator;
import pm.util.Constants;
import pm.util.RuntimeChecker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Main class of ProjectManager - CLI for managing multiple projects.
 *
 * <p><b>What does ProjectManager do?</b>
 * <ul>
 * <li>Registers projects of different types (Java, C#, Node.js, etc.)</li>
 * <li>Automatically detects the project type</li>
 * <li>Executes commands (build, run, test) without needing to remember specific syntax</li>
 * <li>Scans source code to find commands (e.g., @Command in Minecraft)</li>
 * <li>Maintains a centralized registry of all your projects</li>
 * </ul>
 *
 * <p><b>Available commands:</b>
 * <pre>
 * pm add NAME --path PATH [--env VARS]  Register new project
 * pm list                               List all projects
 * pm build NAME                         Build project
 * pm run NAME                           Run project
 * pm test NAME                          Run tests
 * pm scan NAME                          Scan for commands in code
 * pm commands NAME                      List available commands
 * pm remove NAME                        Remove project
 * pm info NAME                          Show project information
 * pm env SUBCOMMAND NAME [options]      Manage environment variables
 * pm help                               Show help
 * </pre>
 *
 * @author SoftDryzz
 * @version 1.2.0
 * @since 1.0.0
 */
public class ProjectManager {

    // Service instances (manual dependency injection pattern)
    private static final ProjectStore store = new ProjectStore();
    private static final ProjectTypeDetector detector = new ProjectTypeDetector();
    private static final CommandExecutor executor = new CommandExecutor();

    /**
     * Application entry point.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        printBanner();

        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0].toLowerCase();

        try {
            switch (command) {
                case "add" -> handleAdd(args);
                case "list", "ls" -> handleList(args);
                case "build" -> handleBuild(args);
                case "run" -> handleRun(args);
                case "test" -> handleTest(args);
                case "scan" -> handleScan(args);
                case "commands", "cmd" -> handleCommands(args);
                case "remove", "rm" -> handleRemove(args);
                case "info" -> handleInfo(args);
                case "env" -> handleEnv(args);
                case "doctor" -> handleDoctor();
                case "help", "-h", "--help" -> printHelp();
                case "version", "-v", "--version" -> printVersion();
                default -> {
                    OutputFormatter.error("Unknown command: " + command);
                    System.out.println("Run 'pm help' for usage information");
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            // Capture any unhandled exceptions
            OutputFormatter.error("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // ============================================================
    // COMMAND: ADD (Register new project)
    // ============================================================

    /**
     * Handler for the "add" command.
     * Registers a new project in ProjectManager.
     *
     * <p>Usage: {@code pm add NAME --path PATH [--type TYPE]}
     *
     * <p>Process:
     * <ol>
     * <li>Validate arguments (name and path are required)</li>
     * <li>Validate that path exists and is a directory</li>
     * <li>Detect project type (or use --type if specified)</li>
     * <li>Create Project object</li>
     * <li>Configure default commands</li>
     * <li>Save to projects.json</li>
     * <li>Confirm to the user</li>
     * </ol>
     *
     * @param args command arguments
     */
    private static void handleAdd(String[] args) {
        // Parse arguments
        ArgsParser parser = new ArgsParser(args);

        String name = parser.getPositional(1);
        String pathFlag = parser.getFlag("path");
        String typeFlag = parser.getFlag("type");

        if (name == null || name.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm add <name> --path <path> [--type <type>] [--env <vars>]");
            System.exit(1);
        }

        if (pathFlag == null || pathFlag.isBlank()) {
            OutputFormatter.error("Project path is required");
            System.out.println("Usage: pm add <name> --path <path> [--type <type>] [--env <vars>]");
            System.exit(1);
        }

        // Expand ~ to home directory
        String expandedPath = pathFlag.replace("~", System.getProperty("user.home"));
        Path projectPath = Paths.get(expandedPath).toAbsolutePath().normalize();

        // Verify path exists
        if (!Files.exists(projectPath)) {
            OutputFormatter.error("Path does not exist: " + projectPath);
            System.exit(1);
        }

        if (!Files.isDirectory(projectPath)) {
            OutputFormatter.error("Path is not a directory: " + projectPath);
            System.exit(1);
        }

        // Verify project doesn't already exist
        try {
            Project existing = store.findProject(name);
            if (existing != null) {
                OutputFormatter.error("Project '" + name + "' already exists");
                System.exit(1);
            }
        } catch (IOException e) {
            OutputFormatter.error("Failed to check existing projects: " + e.getMessage());
            System.exit(1);
        }

        System.out.println();
        OutputFormatter.info("Detecting project type...");
        System.out.println();

        // Detect project type
        ProjectType detectedType;
        if (typeFlag != null && !typeFlag.isBlank()) {
            try {
                detectedType = ProjectType.valueOf(typeFlag.toUpperCase());
            } catch (IllegalArgumentException e) {
                OutputFormatter.error("Invalid project type: " + typeFlag);
                System.out.println("Valid types: GRADLE, MAVEN, NODEJS, DOTNET, PYTHON");
                System.exit(1);
                return;
            }
        } else {
            detectedType = ProjectTypeDetector.detect(projectPath);
        }

        // Create project
        Project project = new Project(name, projectPath, detectedType);

        // Configure default commands based on type
        CommandConfigurator.configureDefaultCommands(project);

        // Configure environment variables if provided
        String envFlag = parser.getFlag("env");
        if (envFlag != null && !envFlag.isBlank()) {
            parseAndSetEnvVars(project, envFlag);
        }

        // Save project
        try {
            store.saveProject(project);

            System.out.println();
            OutputFormatter.success("Project '" + project.name() + "' registered successfully");
            System.out.println();
            System.out.println("  Name: " + project.name());
            System.out.println("  Type: " + project.type().displayName());
            System.out.println("  Path: " + project.path());
            System.out.println("  Commands: " + project.commandCount() + " configured");
            if (project.envVarCount() > 0) {
                System.out.println("  Environment Variables: " + project.envVarCount() + " configured");
            }
            System.out.println();
            System.out.println("Use 'pm commands " + project.name() + "' to see available commands");

        } catch (IOException e) {
            OutputFormatter.error("Failed to save project: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // COMMAND: LIST (List projects)
    // ============================================================

    /**
     * Handler for the "list" command.
     * Lists all registered projects.
     *
     * <p>Usage: pm list
     *
     * @param args command arguments
     */
    private static void handleList(String[] args) {
        try {
            // Load all projects
            Map<String, Project> projects = store.load();

            // Use OutputFormatter to display
            OutputFormatter.printProjectList(projects);

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // COMMAND: BUILD (Build project)
    // ============================================================

    /**
     * Handler for the "build" command.
     * Compiles the specified project.
     *
     * <p>Usage: {@code pm build NAME}
     *
     * <p>Process:
     * <ol>
     * <li>Find project by name</li>
     * <li>Retrieve "build" command</li>
     * <li>Execute in the project directory</li>
     * <li>Show output in real-time</li>
     * <li>Report success/failure</li>
     * </ol>
     *
     * @param args command arguments
     */
    private static void handleBuild(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        // Get project name
        String projectName = parser.getPositional(1);
        if (projectName == null || projectName.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm build <name>");
            System.exit(1);
        }

        try {
            // Find project
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.out.println("Use 'pm list' to see registered projects");
                System.exit(1);
            }

            // Get build command
            String buildCommand = project.getCommand("build");
            if (buildCommand == null) {
                OutputFormatter.error("No 'build' command configured for this project");
                System.out.println("Use 'pm commands " + projectName + "' to see available commands");
                System.exit(1);
            }

            // Check runtime is available before executing
            RuntimeChecker.checkRuntime(project.type());

            // Display info
            System.out.println();
            OutputFormatter.info("Building " + projectName + "...");
            System.out.println("Command: " + buildCommand);
            System.out.println("Directory: " + project.path());
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            // Execute with env vars if configured
            CommandExecutor.ExecutionResult result;
            if (project.envVarCount() > 0) {
                result = executor.execute(buildCommand, project.path(), 300, project.envVars());
            } else {
                result = executor.execute(buildCommand, project.path(), 300);
            }

            // Show result
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            if (result.success()) {
                OutputFormatter.success("Build completed successfully");
                System.out.println("Duration: " + result.formattedDuration());
            } else {
                OutputFormatter.error("Build failed");
                System.out.println("Exit code: " + result.exitCode());
                System.out.println("Duration: " + result.formattedDuration());
                System.exit(1);
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            OutputFormatter.warning("Build interrupted");
            System.exit(130);  // Standard exit code for Ctrl+C
        }
    }

    // ============================================================
    // COMMAND: RUN (Run project)
    // ============================================================

    /**
     * Handler for the "run" command.
     * Executes the specified project.
     *
     * <p>Usage: {@code pm run NAME}
     *
     * @param args command arguments
     */
    private static void handleRun(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        String projectName = parser.getPositional(1);
        if (projectName == null || projectName.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm run <name>");
            System.exit(1);
        }

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            String runCommand = project.getCommand("run");
            if (runCommand == null) {
                OutputFormatter.error("No 'run' command configured for this project");
                System.exit(1);
            }

            // Check runtime is available before executing
            RuntimeChecker.checkRuntime(project.type());

            System.out.println();
            OutputFormatter.info("Running " + projectName + "...");
            System.out.println("Command: " + runCommand);
            System.out.println("Directory: " + project.path());
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            // Execute with env vars if configured
            CommandExecutor.ExecutionResult result;
            if (project.envVarCount() > 0) {
                result = executor.execute(runCommand, project.path(), 0, project.envVars());
            } else {
                result = executor.execute(runCommand, project.path(), 0);
            }

            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            if (result.success()) {
                OutputFormatter.info("Process terminated");
                System.out.println("Duration: " + result.formattedDuration());
            } else {
                OutputFormatter.error("Process failed");
                System.out.println("Exit code: " + result.exitCode());
                System.exit(1);
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            OutputFormatter.warning("Process interrupted");
            System.exit(130);
        }
    }

    // ============================================================
    // COMMAND: TEST (Run tests)
    // ============================================================

    /**
     * Handler for the "test" command.
     * Executes the project's tests.
     *
     * <p>Usage: {@code pm test NAME}
     *
     * @param args command arguments
     */
    private static void handleTest(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        String projectName = parser.getPositional(1);
        if (projectName == null || projectName.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm test <name>");
            System.exit(1);
        }

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            String testCommand = project.getCommand("test");
            if (testCommand == null) {
                OutputFormatter.error("No 'test' command configured for this project");
                System.exit(1);
            }

            // Check runtime is available before executing
            RuntimeChecker.checkRuntime(project.type());

            System.out.println();
            OutputFormatter.info("Running tests for " + projectName + "...");
            System.out.println("Command: " + testCommand);
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();


            // Execute with env vars if configured
            CommandExecutor.ExecutionResult result;
            if (project.envVarCount() > 0) {
                result = executor.execute(testCommand, project.path(), 600, project.envVars());
            } else {
                result = executor.execute(testCommand, project.path(), 600); // timeout: 10 mins for tests
            }

            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            if (result.success()) {
                OutputFormatter.success("All tests passed");
                System.out.println("Duration: " + result.formattedDuration());
            } else {
                OutputFormatter.error("Tests failed");
                System.out.println("Exit code: " + result.exitCode());
                System.exit(1);
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            OutputFormatter.warning("Tests interrupted");
            System.exit(130);
        }
    }

    // ============================================================
    // COMMAND: SCAN (Scan commands in code)
    // ============================================================

    /**
     * Handler for the "scan" command.
     * Scans source code for commands.
     *
     * <p>Usage: {@code pm scan NAME}
     *
     * <p>TODO: Implement @Command annotation scanner
     *
     * @param args command arguments
     */
    private static void handleScan(String[] args) {
        OutputFormatter.info("Scan command - Coming soon");
        System.out.println("This will scan for @Command annotations in your code");
        System.out.println("Useful for Minecraft mods and similar projects");
    }

    // ============================================================
    // COMMAND: COMMANDS (List available commands)
    // ============================================================

    /**
     * Handler for the "commands" command.
     * Lists available commands for a project.
     *
     * <p>Usage: {@code pm commands NAME}
     *
     * @param args command arguments
     */
    private static void handleCommands(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        String projectName = parser.getPositional(1);
        if (projectName == null || projectName.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm commands <name>");
            System.exit(1);
        }

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            // Use OutputFormatter to show commands
            OutputFormatter.printCommands(project);

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // COMMAND: REMOVE (Remove project)
    // ============================================================

    /**
     * Handler for the "remove" command.
     * Removes a project from the registry.
     *
     * <p>Usage: {@code pm remove NAME}
     *
     * @param args command arguments
     */
    private static void handleRemove(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        String projectName = parser.getPositional(1);
        if (projectName == null || projectName.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm remove <name>");
            System.exit(1);
        }

        try {
            // Verify project exists
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            // Confirm removal (unless --force flag is used)
            if (!parser.hasFlag("force")) {
                System.out.println("About to remove project:");
                System.out.println("  Name: " + project.name());
                System.out.println("  Path: " + project.path());
                System.out.println();
                System.out.print("Are you sure? (y/n): ");

                String response = System.console() != null
                        ? System.console().readLine()
                        : "n";

                if (!response.toLowerCase().startsWith("y")) {
                    System.out.println("Aborted.");
                    return;
                }
            }

            // Remove project
            boolean removed = store.removeProject(projectName);

            if (removed) {
                OutputFormatter.success("Project '" + projectName + "' removed");
            } else {
                OutputFormatter.error("Failed to remove project");
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to remove project: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // COMMAND: INFO (Show project information)
    // ============================================================

    /**
     * Handler for the "info" command.
     * Shows detailed project information.
     *
     * <p>Usage: {@code pm info NAME}
     *
     * @param args command arguments
     */
    private static void handleInfo(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        String projectName = parser.getPositional(1);
        if (projectName == null || projectName.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm info <name>");
            System.exit(1);
        }

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            // Show detailed info
            OutputFormatter.section("Project Information");
            OutputFormatter.printProject(project);
            OutputFormatter.printCommands(project);

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // COMMAND: ENV (Manage environment variables)
    // ============================================================

    /**
     * Handler for the "env" command.
     * Manages environment variables for a project.
     *
     * <p>Subcommands:
     * <ul>
     * <li>{@code pm env set <name> KEY=VALUE[,KEY2=VALUE2]} - Set variables</li>
     * <li>{@code pm env get <name> KEY} - Get a variable value</li>
     * <li>{@code pm env list <name> [--show]} - List variables (masked by default)</li>
     * <li>{@code pm env remove <name> KEY} - Remove a variable</li>
     * <li>{@code pm env clear <name>} - Remove all variables</li>
     * </ul>
     *
     * @param args command arguments
     */
    private static void handleEnv(String[] args) {
        if (args.length < 2) {
            printEnvHelp();
            return;
        }

        String subcommand = args[1].toLowerCase();

        switch (subcommand) {
            case "set" -> handleEnvSet(args);
            case "get" -> handleEnvGet(args);
            case "list", "ls" -> handleEnvList(args);
            case "remove", "rm" -> handleEnvRemove(args);
            case "clear" -> handleEnvClear(args);
            default -> {
                OutputFormatter.error("Unknown env subcommand: " + subcommand);
                printEnvHelp();
                System.exit(1);
            }
        }
    }

    private static void handleEnvSet(String[] args) {
        if (args.length < 4) {
            OutputFormatter.error("Project name and KEY=VALUE are required");
            System.out.println("Usage: pm env set <name> KEY=VALUE[,KEY2=VALUE2]");
            System.exit(1);
        }

        String projectName = args[2];

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            // Join remaining args as the env string (supports spaces in values)
            String envString = String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length));
            parseAndSetEnvVars(project, envString);
            store.saveProject(project);

            System.out.println();
            OutputFormatter.success("Environment variables updated for '" + projectName + "'");

        } catch (IOException e) {
            OutputFormatter.error("Failed to update project: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleEnvGet(String[] args) {
        if (args.length < 4) {
            OutputFormatter.error("Project name and variable key are required");
            System.out.println("Usage: pm env get <name> KEY");
            System.exit(1);
        }

        String projectName = args[2];
        String key = args[3];

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            String value = project.getEnvVar(key);
            if (value == null) {
                OutputFormatter.error("Variable '" + key + "' not found in project '" + projectName + "'");
                System.exit(1);
            }

            System.out.println(key + "=" + value);

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleEnvList(String[] args) {
        if (args.length < 3) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm env list <name> [--show]");
            System.exit(1);
        }

        String projectName = args[2];
        ArgsParser parser = new ArgsParser(args);
        boolean showValues = parser.hasFlag("show");

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            Map<String, String> vars = project.envVars();

            if (vars.isEmpty()) {
                System.out.println();
                OutputFormatter.info("No environment variables configured for '" + projectName + "'");
                return;
            }

            System.out.println();
            OutputFormatter.section("Environment Variables - " + projectName);

            int maxKeyLength = vars.keySet().stream()
                    .mapToInt(String::length)
                    .max()
                    .orElse(0);

            vars.forEach((key, value) -> {
                String displayValue = showValues ? value : maskValue(key, value);
                String padding = " ".repeat(maxKeyLength - key.length());
                System.out.println("  " + OutputFormatter.GREEN + key + OutputFormatter.RESET +
                        padding + " = " + OutputFormatter.CYAN + displayValue + OutputFormatter.RESET);
            });

            if (!showValues) {
                System.out.println();
                System.out.println(OutputFormatter.GRAY +
                        "  Sensitive values are masked. Use --show to reveal all." +
                        OutputFormatter.RESET);
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleEnvRemove(String[] args) {
        if (args.length < 4) {
            OutputFormatter.error("Project name and variable key are required");
            System.out.println("Usage: pm env remove <name> KEY");
            System.exit(1);
        }

        String projectName = args[2];
        String key = args[3];

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            boolean removed = project.removeEnvVar(key);
            if (!removed) {
                OutputFormatter.error("Variable '" + key + "' not found in project '" + projectName + "'");
                System.exit(1);
            }

            store.saveProject(project);

            System.out.println();
            OutputFormatter.success("Variable '" + key + "' removed from '" + projectName + "'");

        } catch (IOException e) {
            OutputFormatter.error("Failed to update project: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleEnvClear(String[] args) {
        if (args.length < 3) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm env clear <name>");
            System.exit(1);
        }

        String projectName = args[2];

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            int count = project.envVarCount();
            if (count == 0) {
                OutputFormatter.info("No environment variables to clear for '" + projectName + "'");
                return;
            }

            project.clearEnvVars();
            store.saveProject(project);

            System.out.println();
            OutputFormatter.success("Cleared " + count + " variable" + (count > 1 ? "s" : "") +
                    " from '" + projectName + "'");

        } catch (IOException e) {
            OutputFormatter.error("Failed to update project: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Masks a value if the key suggests it is sensitive.
     * Sensitive keys contain: KEY, SECRET, PASSWORD, TOKEN, PRIVATE, CREDENTIAL.
     *
     * @param key   variable name
     * @param value variable value
     * @return masked or original value
     */
    static String maskValue(String key, String value) {
        String upperKey = key.toUpperCase();
        boolean sensitive = upperKey.contains("KEY") ||
                upperKey.contains("SECRET") ||
                upperKey.contains("PASSWORD") ||
                upperKey.contains("TOKEN") ||
                upperKey.contains("PRIVATE") ||
                upperKey.contains("CREDENTIAL");

        if (!sensitive) {
            return value;
        }

        if (value.length() < 6) {
            return "***";
        }

        return value.substring(0, 3) + "***" + value.substring(value.length() - 2);
    }

    private static void printEnvHelp() {
        System.out.println("""
        Usage: pm env <subcommand> <project> [options]

        Subcommands:
          set <name> KEY=VALUE[,KEY2=VALUE2]  Set environment variables
          get <name> KEY                       Get a variable value
          list <name> [--show]                 List variables (masked by default)
          remove <name> KEY                    Remove a variable
          clear <name>                         Remove all variables

        Examples:
          pm env set my-api PORT=8080,DEBUG=true
          pm env get my-api PORT
          pm env list my-api
          pm env list my-api --show
          pm env remove my-api DEBUG
          pm env clear my-api
        """);
    }

    // ============================================================
    // COMMAND: DOCTOR (Environment check)
    // ============================================================

    /**
     * Handler for the "doctor" command.
     * Checks if required runtimes are installed and validates registered projects.
     *
     * <p>Usage: {@code pm doctor}
     */
    private static void handleDoctor() {
        OutputFormatter.section("Environment Check");

        // Check each runtime
        String[][] runtimes = {
                {"Java",    "java",    "-version"},
                {"Maven",   "mvn",     "-version"},
                {"Gradle",  "gradle",  "-version"},
                {"Node.js", "node",    "--version"},
                {"npm",     "npm",     "--version"},
                {".NET",    "dotnet",  "--version"},
                {"Python",  "python",  "--version"},
        };

        for (String[] rt : runtimes) {
            String name = rt[0];
            String command = rt[1];
            String flag = rt[2];

            String version = RuntimeChecker.getVersion(command, flag);
            if (version != null) {
                // Trim version output to something short
                String shortVersion = version.length() > 40
                        ? version.substring(0, 40) + "..."
                        : version;
                System.out.println("  " + OutputFormatter.GREEN + "OK" + OutputFormatter.RESET +
                        "  " + padRight(name, 10) + OutputFormatter.GRAY + shortVersion + OutputFormatter.RESET);
            } else {
                System.out.println("  " + OutputFormatter.RED + "X " + OutputFormatter.RESET +
                        "  " + padRight(name, 10) + OutputFormatter.GRAY + "(not found)" + OutputFormatter.RESET);
            }
        }

        // Check registered projects
        System.out.println();
        OutputFormatter.section("Registered Projects");

        try {
            Map<String, Project> projects = store.load();

            if (projects.isEmpty()) {
                System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                return;
            }

            for (Project project : projects.values()) {
                boolean pathExists = Files.exists(project.path()) && Files.isDirectory(project.path());
                boolean runtimeOk = RuntimeChecker.isRuntimeAvailable(project.type());

                String status;
                if (pathExists && runtimeOk) {
                    status = OutputFormatter.GREEN + "OK" + OutputFormatter.RESET;
                } else if (!pathExists) {
                    status = OutputFormatter.RED + "PATH NOT FOUND" + OutputFormatter.RESET;
                } else {
                    status = OutputFormatter.YELLOW + "RUNTIME MISSING" + OutputFormatter.RESET;
                }

                System.out.println("  " + status + "  " +
                        padRight(project.name(), 20) +
                        OutputFormatter.GRAY + project.type().displayName() +
                        " -> " + project.path() + OutputFormatter.RESET);
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Right-pads a string to the specified length.
     */
    private static String padRight(String text, int length) {
        if (text.length() >= length) {
            return text + " ";
        }
        return text + " ".repeat(length - text.length());
    }

    // ============================================================
    // OUTPUT AND HELP
    // ============================================================

    private static void printBanner() {
        System.out.println("""
            ╔════════════════════════════════╗
            ║  ProjectManager v%-12s  ║
            ║  Manage your projects          ║
            ╚════════════════════════════════╝
            """.formatted(Constants.VERSION));
    }

    private static void printHelp() {
        System.out.println("""
        Usage: pm <command> [options]

        Commands:
          add <name> --path <path> [--env <vars>]  Register a new project
          list, ls                                  List all projects
          build <name>                              Build project
          run <name>                                Run project
          test <name>                               Run tests
          scan <name>                               Scan for commands in code
          commands, cmd <name>                      List available commands
          remove, rm <name>                         Remove project
          info <name>                               Show project details
          env <subcommand> <name> [options]         Manage environment variables
          doctor                                    Check environment and runtimes
          help                                      Show this help
          version                                   Show version

        Environment Variables (pm env):
          env set <name> KEY=VALUE[,KEY2=VALUE2]    Set variables
          env get <name> KEY                        Get a variable value
          env list <name> [--show]                  List variables (masked by default)
          env remove <name> KEY                     Remove a variable
          env clear <name>                          Remove all variables

        Examples:
          pm add backend-api --path ~/projects/backend-api
          pm add web-server --path ~/projects/web-server --env "PORT=3000,DEBUG=true"
          pm list
          pm build backend-api
          pm run web-server
          pm env set web-server PORT=3000,DEBUG=true
          pm env list web-server
          pm commands backend-api
          pm info web-server
        """);
    }

    private static void printVersion() {
        System.out.println("ProjectManager " + Constants.VERSION);
        System.out.println("Java " + System.getProperty("java.version"));
    }

    /**
     * Parses and configures environment variables from a string.
     *
     * Expected format: "KEY1=value1,KEY2=value2,KEY3=value3"
     *
     * @param project project to add variables to
     * @param envString string containing the variables
     */
    private static void parseAndSetEnvVars(Project project, String envString) {
        if (envString == null || envString.isBlank()) {
            return;
        }

        String[] pairs = envString.split(",");
        int added = 0;

        for (String pair : pairs) {
            pair = pair.trim();

            if (pair.isEmpty()) {
                continue;
            }

            int equalsIndex = pair.indexOf('=');

            if (equalsIndex == -1) {
                OutputFormatter.warning("Invalid environment variable format (missing '='): " + pair);
                continue;
            }

            String key = pair.substring(0, equalsIndex).trim();
            String value = pair.substring(equalsIndex + 1).trim();

            if (key.isEmpty()) {
                OutputFormatter.warning("Invalid environment variable (empty key): " + pair);
                continue;
            }

            project.addEnvVar(key, value);
            added++;
        }

        if (added > 0) {
            System.out.println(OutputFormatter.GRAY + "  Configured " + added +
                    " environment variable" + (added > 1 ? "s" : "") + OutputFormatter.RESET);
        }
    }
}