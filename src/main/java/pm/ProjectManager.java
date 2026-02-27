package pm;

import pm.ci.CIDetector;
import pm.ci.CIProvider;
import pm.lint.FormatDetector;
import pm.lint.FormatTool;
import pm.lint.LintDetector;
import pm.lint.LintTool;
import pm.migration.MigrationDetector;
import pm.migration.MigrationTool;
import pm.scanner.EnvFileDetector;
import pm.workspace.WorkspaceDetector;
import pm.workspace.WorkspaceModule;
import pm.cli.OutputFormatter;
import pm.completion.CompletionHandler;
import pm.completion.CompletionScripts;
import pm.core.Project;
import pm.detector.ProjectType;
import pm.detector.ProjectTypeDetector;
import pm.doctor.HealthCheck;
import pm.doctor.HealthScorer;
import pm.audit.AuditReport;
import pm.audit.DependencyAuditor;
import pm.audit.Severity;
import pm.export.ExportResult;
import pm.export.ImportResult;
import pm.export.ProjectExporter;
import pm.security.SecurityCheck;
import pm.security.SecurityScorer;
import pm.executor.CommandExecutor;
import pm.storage.ProjectStore;
import pm.util.ArgsParser;
import pm.util.CommandConfigurator;
import pm.util.Constants;
import pm.util.GitIntegration;
import pm.util.RuntimeChecker;
import pm.telemetry.Telemetry;
import pm.util.UpdateChecker;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
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
 * pm refresh NAME | --all               Re-detect type and update commands
 * pm update                             Update to the latest version
 * pm help                               Show help
 * </pre>
 *
 * @author SoftDryzz
 * @version 1.3.3
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
        // Fast path: shell completion callback (no banner, no update check)
        if (args.length > 0 && "--complete".equals(args[0])) {
            CompletionHandler.handle(args);
            return;
        }

        printBanner();

        // Check for updates in the background (non-blocking, 2s timeout)
        UpdateChecker.checkForUpdates();

        // Initialize telemetry (first-run consent prompt if needed)
        Telemetry.init();

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
                case "rename" -> handleRename(args);
                case "info" -> handleInfo(args);
                case "env" -> handleEnv(args);
                case "hooks" -> handleHooks(args);
                case "refresh" -> handleRefresh(args);
                case "completions" -> handleCompletions(args);
                case "update" -> UpdateChecker.performUpdate();
                case "doctor" -> handleDoctor(args);
                case "secure" -> handleSecure(args);
                case "audit" -> handleAudit(args);
                case "ci" -> handleCI(args);
                case "lint" -> handleLint(args);
                case "fmt" -> handleFmt(args);
                case "modules" -> handleModules(args);
                case "migrate" -> handleMigrate(args);
                case "export" -> handleExport(args);
                case "import" -> handleImport(args);
                case "config" -> handleConfig(args);
                case "help", "-h", "--help" -> printHelp();
                case "version", "-v", "--version" -> printVersion();
                default -> handleGenericCommand(command, args);
            }
            Telemetry.trackCommand(command);
        } catch (Exception e) {
            handleFatalError(e);
        } finally {
            Telemetry.flush();
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
                System.out.println("Valid types: GRADLE, MAVEN, NODEJS, DOTNET, PYTHON, RUST, GO, PNPM, BUN, YARN");
                System.exit(1);
                return;
            }
        } else {
            detectedType = ProjectTypeDetector.detect(projectPath);
        }

        // Create project
        Project project = new Project(name, projectPath, detectedType);

        // Detect secondary types (e.g., Docker alongside Maven)
        List<ProjectType> allTypes = ProjectTypeDetector.detectAll(projectPath);
        allTypes.stream()
                .filter(t -> t != detectedType)
                .forEach(project::addSecondaryType);

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

        String projectName = parser.getPositional(1);

        if (projectName != null && !projectName.isBlank()) {
            // Single project build (existing behavior)
            buildSingleProject(projectName);
        } else if (parser.hasFlag("all")) {
            // Build all registered projects
            buildAllProjects();
        } else {
            OutputFormatter.error("Project name is required. Use --all to build all projects.");
            System.out.println("Usage: pm build <name>");
            System.out.println("       pm build --all");
            System.exit(1);
        }
    }

    private static void buildSingleProject(String projectName) {
        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.out.println("Use 'pm list' to see registered projects");
                System.exit(1);
            }

            checkTypeOutdated(project);
            validateProjectPath(project);

            String buildCommand = project.getCommand("build");
            if (buildCommand == null) {
                OutputFormatter.error("No 'build' command configured for this project");
                System.out.println("Use 'pm commands " + projectName + "' to see available commands");
                System.exit(1);
            }

            RuntimeChecker.checkRuntime(project.type());

            if (!executeHooks(project, "pre-build")) {
                OutputFormatter.error("Pre-build hook failed. Build aborted.");
                System.exit(1);
            }

            System.out.println();
            OutputFormatter.info("Building " + projectName + "...");
            System.out.println("Command: " + buildCommand);
            System.out.println("Directory: " + project.path());
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            CommandExecutor.ExecutionResult result;
            if (System.console() != null) {
                result = executor.executeWithInheritedIO(buildCommand, project.path(), 300, project.envVars());
            } else {
                if (project.envVarCount() > 0) {
                    result = executor.execute(buildCommand, project.path(), 300, project.envVars());
                } else {
                    result = executor.execute(buildCommand, project.path(), 300);
                }
            }

            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            if (result.success()) {
                if (!executeHooks(project, "post-build")) {
                    OutputFormatter.warning("Post-build hook failed.");
                }
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
            System.exit(130);
        }
    }

    private static void buildAllProjects() {
        try {
            Map<String, Project> projects = store.load();
            if (projects.isEmpty()) {
                OutputFormatter.section("Build All");
                System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                return;
            }

            OutputFormatter.section("Build All");
            int passed = 0;
            int total = 0;

            for (Project project : projects.values()) {
                String buildCommand = project.getCommand("build");
                if (buildCommand == null) {
                    System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                            " " + OutputFormatter.GRAY + "— no build command, skipped" + OutputFormatter.RESET);
                    continue;
                }

                if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
                    System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                            " " + OutputFormatter.RED + "— path not found" + OutputFormatter.RESET);
                    total++;
                    continue;
                }

                total++;
                System.out.println();
                System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                        " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);
                System.out.println("  " + "─".repeat(40));

                try {
                    executeHooks(project, "pre-build");

                    CommandExecutor.ExecutionResult result;
                    if (System.console() != null) {
                        result = executor.executeWithInheritedIO(buildCommand, project.path(), 300, project.envVars());
                    } else {
                        result = executor.execute(buildCommand, project.path(), 300, project.envVars());
                    }

                    System.out.println("  " + "─".repeat(40));
                    if (result.success()) {
                        executeHooks(project, "post-build");
                        System.out.println("  " + OutputFormatter.GREEN + "✓" + OutputFormatter.RESET +
                                " " + project.name() + " built (" + result.formattedDuration() + ")");
                        passed++;
                    } else {
                        System.out.println("  " + OutputFormatter.RED + "✗" + OutputFormatter.RESET +
                                " " + project.name() + " failed (exit code " + result.exitCode() + ")");
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("  " + "─".repeat(40));
                    System.out.println("  " + OutputFormatter.RED + "✗" + OutputFormatter.RESET +
                            " " + project.name() + " error: " + e.getMessage());
                }
            }

            System.out.println();
            System.out.println("  Result: " + passed + "/" + total + " projects built successfully");
            System.out.println();

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
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

            checkTypeOutdated(project);
            validateProjectPath(project);

            String runCommand = project.getCommand("run");
            if (runCommand == null) {
                OutputFormatter.error("No 'run' command configured for this project");
                System.exit(1);
            }

            // Check runtime is available before executing
            RuntimeChecker.checkRuntime(project.type());

            // Run pre-run hooks
            if (!executeHooks(project, "pre-run")) {
                OutputFormatter.error("Pre-run hook failed. Run aborted.");
                System.exit(1);
            }

            System.out.println();
            OutputFormatter.info("Running " + projectName + "...");
            System.out.println("Command: " + runCommand);
            System.out.println("Directory: " + project.path());
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            // Execute: use inherited IO when running in a real terminal (interactive mode)
            CommandExecutor.ExecutionResult result;
            if (System.console() != null) {
                result = executor.executeWithInheritedIO(runCommand, project.path(), 0, project.envVars());
            } else {
                if (project.envVarCount() > 0) {
                    result = executor.execute(runCommand, project.path(), 0, project.envVars());
                } else {
                    result = executor.execute(runCommand, project.path(), 0);
                }
            }

            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            if (result.success()) {
                // Run post-run hooks
                if (!executeHooks(project, "post-run")) {
                    OutputFormatter.warning("Post-run hook failed.");
                }
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

        if (projectName != null && !projectName.isBlank()) {
            testSingleProject(projectName);
        } else if (parser.hasFlag("all")) {
            testAllProjects();
        } else {
            OutputFormatter.error("Project name is required. Use --all to test all projects.");
            System.out.println("Usage: pm test <name>");
            System.out.println("       pm test --all");
            System.exit(1);
        }
    }

    private static void testSingleProject(String projectName) {
        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            checkTypeOutdated(project);
            validateProjectPath(project);

            String testCommand = project.getCommand("test");
            if (testCommand == null) {
                OutputFormatter.error("No 'test' command configured for this project");
                System.exit(1);
            }

            RuntimeChecker.checkRuntime(project.type());

            if (!executeHooks(project, "pre-test")) {
                OutputFormatter.error("Pre-test hook failed. Tests aborted.");
                System.exit(1);
            }

            System.out.println();
            OutputFormatter.info("Running tests for " + projectName + "...");
            System.out.println("Command: " + testCommand);
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            CommandExecutor.ExecutionResult result;
            if (System.console() != null) {
                result = executor.executeWithInheritedIO(testCommand, project.path(), 600, project.envVars());
            } else {
                if (project.envVarCount() > 0) {
                    result = executor.execute(testCommand, project.path(), 600, project.envVars());
                } else {
                    result = executor.execute(testCommand, project.path(), 600);
                }
            }

            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            if (result.success()) {
                if (!executeHooks(project, "post-test")) {
                    OutputFormatter.warning("Post-test hook failed.");
                }
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

    private static void testAllProjects() {
        try {
            Map<String, Project> projects = store.load();
            if (projects.isEmpty()) {
                OutputFormatter.section("Test All");
                System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                return;
            }

            OutputFormatter.section("Test All");
            int passed = 0;
            int total = 0;

            for (Project project : projects.values()) {
                String testCommand = project.getCommand("test");
                if (testCommand == null) {
                    System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                            " " + OutputFormatter.GRAY + "— no test command, skipped" + OutputFormatter.RESET);
                    continue;
                }

                if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
                    System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                            " " + OutputFormatter.RED + "— path not found" + OutputFormatter.RESET);
                    total++;
                    continue;
                }

                total++;
                System.out.println();
                System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                        " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);
                System.out.println("  " + "─".repeat(40));

                try {
                    executeHooks(project, "pre-test");

                    CommandExecutor.ExecutionResult result;
                    if (System.console() != null) {
                        result = executor.executeWithInheritedIO(testCommand, project.path(), 600, project.envVars());
                    } else {
                        result = executor.execute(testCommand, project.path(), 600, project.envVars());
                    }

                    System.out.println("  " + "─".repeat(40));
                    if (result.success()) {
                        executeHooks(project, "post-test");
                        System.out.println("  " + OutputFormatter.GREEN + "✓" + OutputFormatter.RESET +
                                " " + project.name() + " passed (" + result.formattedDuration() + ")");
                        passed++;
                    } else {
                        System.out.println("  " + OutputFormatter.RED + "✗" + OutputFormatter.RESET +
                                " " + project.name() + " failed (exit code " + result.exitCode() + ")");
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("  " + "─".repeat(40));
                    System.out.println("  " + OutputFormatter.RED + "✗" + OutputFormatter.RESET +
                            " " + project.name() + " error: " + e.getMessage());
                }
            }

            System.out.println();
            System.out.println("  Result: " + passed + "/" + total + " projects tested successfully");
            System.out.println();

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
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
        // Handle --all flag: pm commands --all
        if (args.length >= 2 && args[1].equals("--all")) {
            handleCommandsAll();
            return;
        }

        // Need at least a project name
        if (args.length < 2) {
            printCommandsHelp();
            return;
        }

        String projectName = args[1];

        // Check for subcommands: pm commands <project> add|remove
        if (args.length >= 3) {
            String subcommand = args[2].toLowerCase();
            switch (subcommand) {
                case "add" -> {
                    handleCommandsAdd(args, projectName);
                    return;
                }
                case "remove", "rm" -> {
                    handleCommandsRemove(args, projectName);
                    return;
                }
                default -> {
                    // Unknown subcommand — fall through to show commands
                }
            }
        }

        // Default: show commands for this project
        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            checkTypeOutdated(project);
            OutputFormatter.printCommands(project);

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Adds a custom command to a project.
     *
     * <p>Usage: {@code pm commands <project> add <name> "<command>"}
     *
     * @param args command arguments
     * @param projectName target project name
     */
    private static void handleCommandsAdd(String[] args, String projectName) {
        if (args.length < 5) {
            OutputFormatter.error("Command name and command line are required");
            System.out.println("Usage: pm commands <project> add <name> \"<command>\"");
            System.out.println("Example: pm commands my-app add tunnel \"npx expo start --tunnel\"");
            System.exit(1);
        }

        String commandName = args[3];
        // Join remaining args as the command value (supports unquoted multi-word commands)
        String commandLine = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            boolean existed = project.hasCommand(commandName);
            project.addCommand(commandName, commandLine);
            store.saveProject(project);

            System.out.println();
            if (existed) {
                OutputFormatter.success("Command '" + commandName + "' updated in '" + projectName + "'");
            } else {
                OutputFormatter.success("Command '" + commandName + "' added to '" + projectName + "'");
            }
            System.out.println("  " + OutputFormatter.GREEN + commandName + OutputFormatter.RESET
                    + " → " + OutputFormatter.CYAN + commandLine + OutputFormatter.RESET);

            // Warn about shell metacharacters (informational, does not block)
            if (containsShellMetacharacters(commandLine)) {
                System.out.println();
                OutputFormatter.warning("Command contains shell special characters: " +
                        getFoundMetacharacters(commandLine));
                System.out.println("  This is fine if intentional (e.g., chaining commands with '&&').");
                System.out.println("  If your command includes file paths with special characters,");
                System.out.println("  make sure they are properly quoted.");
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to update project: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Removes a custom command from a project.
     *
     * <p>Usage: {@code pm commands <project> remove <name>}
     *
     * @param args command arguments
     * @param projectName target project name
     */
    private static void handleCommandsRemove(String[] args, String projectName) {
        if (args.length < 4) {
            OutputFormatter.error("Command name is required");
            System.out.println("Usage: pm commands <project> remove <name>");
            System.exit(1);
        }

        String commandName = args[3];

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            if (!project.hasCommand(commandName)) {
                OutputFormatter.error("Command '" + commandName + "' not found in project '" + projectName + "'");
                System.exit(1);
            }

            project.removeCommand(commandName);
            store.saveProject(project);

            System.out.println();
            OutputFormatter.success("Command '" + commandName + "' removed from '" + projectName + "'");

        } catch (IOException e) {
            OutputFormatter.error("Failed to update project: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Lists commands for all registered projects.
     *
     * <p>Usage: {@code pm commands --all}
     */
    private static void handleCommandsAll() {
        try {
            Map<String, Project> projects = store.load();

            if (projects.isEmpty()) {
                OutputFormatter.info("No projects registered yet.");
                System.out.println("Add your first project with:");
                System.out.println("  pm add <name> --path <path>");
                return;
            }

            OutputFormatter.printAllCommands(projects);

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prints help for the commands subcommand.
     */
    private static void printCommandsHelp() {
        System.out.println("""
        Usage: pm commands <project> [subcommand]

        Subcommands:
          (none)                        Show available commands for the project
          add <name> "<command>"        Add a custom command
          remove <name>                 Remove a command

        Flags:
          --all                         Show commands for all registered projects

        Examples:
          pm commands my-app
          pm commands my-app add tunnel "npx expo start --tunnel"
          pm commands my-app add lint "npm run lint"
          pm commands my-app remove tunnel
          pm commands --all
        """);
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
    // COMMAND: RENAME (Rename project or update path)
    // ============================================================

    /**
     * Handler for the "rename" command.
     * Renames a project and/or updates its path.
     *
     * <p>Usage:
     * <ul>
     * <li>{@code pm rename old-name new-name} — rename project</li>
     * <li>{@code pm rename name --path /new/path} — update path</li>
     * <li>{@code pm rename old-name new-name --path /new/path} — both</li>
     * </ul>
     *
     * @param args command arguments
     */
    private static void handleRename(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        String currentName = parser.getPositional(1);
        String newName = parser.getPositional(2);
        String newPath = parser.getFlag("path");

        if (currentName == null || currentName.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm rename <current-name> [new-name] [--path <new-path>]");
            System.exit(1);
        }

        // At least one change must be specified
        if ((newName == null || newName.isBlank()) && (newPath == null || newPath.isBlank())) {
            OutputFormatter.error("Specify a new name, a new path with --path, or both");
            System.out.println("Usage: pm rename <current-name> [new-name] [--path <new-path>]");
            System.exit(1);
        }

        try {
            Project project = store.findProject(currentName);
            if (project == null) {
                OutputFormatter.error("Project '" + currentName + "' not found");
                System.exit(1);
            }

            String finalName = (newName != null && !newName.isBlank()) ? newName : currentName;
            Path finalPath = project.path();

            // Validate new path if provided
            if (newPath != null && !newPath.isBlank()) {
                String expandedPath = newPath.replace("~", System.getProperty("user.home"));
                finalPath = Paths.get(expandedPath).toAbsolutePath().normalize();

                if (!Files.exists(finalPath)) {
                    OutputFormatter.error("Path does not exist: " + finalPath);
                    System.exit(1);
                }
                if (!Files.isDirectory(finalPath)) {
                    OutputFormatter.error("Path is not a directory: " + finalPath);
                    System.exit(1);
                }
            }

            // Check if new name conflicts with existing project
            if (!finalName.equals(currentName)) {
                Project conflict = store.findProject(finalName);
                if (conflict != null) {
                    OutputFormatter.error("A project named '" + finalName + "' already exists");
                    System.exit(1);
                }
            }

            // Build the renamed/moved project
            Project updated = new Project(finalName, finalPath, project.type());
            project.commands().forEach(updated::addCommand);
            project.envVars().forEach(updated::addEnvVar);

            // Remove old, save new
            store.removeProject(currentName);
            store.saveProject(updated);

            // Show result
            System.out.println();
            OutputFormatter.success("Project updated");
            System.out.println();
            if (!finalName.equals(currentName)) {
                System.out.println("  Name: " + OutputFormatter.YELLOW + currentName +
                        OutputFormatter.RESET + " → " + OutputFormatter.GREEN + finalName + OutputFormatter.RESET);
            }
            if (!finalPath.equals(project.path())) {
                System.out.println("  Path: " + OutputFormatter.YELLOW + project.path() +
                        OutputFormatter.RESET + " → " + OutputFormatter.GREEN + finalPath + OutputFormatter.RESET);
            }
            System.out.println();

        } catch (IOException e) {
            OutputFormatter.error("Failed to rename project: " + e.getMessage());
            System.exit(1);
        }
    }

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

            checkTypeOutdated(project);

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
            case "files" -> handleEnvFiles(args);
            case "show" -> handleEnvShow(args);
            case "switch" -> handleEnvSwitch(args);
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

    private static void handleEnvFiles(String[] args) {
        if (args.length < 3) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm env files <name>");
            System.exit(1);
        }

        String projectName = args[2];

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            List<Path> envFiles = EnvFileDetector.detectEnvFiles(project.path());
            if (envFiles.isEmpty()) {
                OutputFormatter.info("No .env files found in '" + projectName + "'");
                return;
            }

            OutputFormatter.section("Env Files — " + projectName);
            System.out.println();
            for (Path envFile : envFiles) {
                String name = envFile.getFileName().toString();
                Map<String, String> entries = EnvFileDetector.parseEnvFile(envFile);
                long size;
                try {
                    size = Files.size(envFile);
                } catch (IOException ex) {
                    size = 0;
                }
                System.out.println("  " + OutputFormatter.CYAN + name + OutputFormatter.RESET
                        + "  " + OutputFormatter.GRAY + entries.size() + " vars, "
                        + formatFileSize(size) + OutputFormatter.RESET);
            }
            System.out.println();
            System.out.println("  " + envFiles.size() + " file" + (envFiles.size() != 1 ? "s" : "") + " detected");

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleEnvShow(String[] args) {
        if (args.length < 4) {
            OutputFormatter.error("Project name and filename are required");
            System.out.println("Usage: pm env show <name> <filename> [--show]");
            System.exit(1);
        }

        String projectName = args[2];
        String filename = args[3];
        boolean showValues = Arrays.asList(args).contains("--show");

        if (!filename.startsWith(".env")) {
            OutputFormatter.error("Filename must start with '.env'");
            System.exit(1);
        }

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            Path envFile = project.path().resolve(filename);
            if (!Files.isRegularFile(envFile)) {
                OutputFormatter.error("File '" + filename + "' not found in project directory");
                System.exit(1);
            }

            Map<String, String> entries = EnvFileDetector.parseEnvFile(envFile);
            if (entries.isEmpty()) {
                OutputFormatter.info("File '" + filename + "' is empty or has no variables");
                return;
            }

            OutputFormatter.section(filename + " — " + projectName);
            System.out.println();

            int maxKeyLen = entries.keySet().stream().mapToInt(String::length).max().orElse(10);
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                String key = entry.getKey();
                String value = showValues ? entry.getValue()
                        : EnvFileDetector.maskValue(key, entry.getValue());
                System.out.printf("  %-" + maxKeyLen + "s = %s%n", key, value);
            }

            System.out.println();
            System.out.println("  " + entries.size() + " variable" + (entries.size() != 1 ? "s" : ""));
            if (!showValues) {
                System.out.println("  " + OutputFormatter.GRAY
                        + "Use --show to reveal sensitive values" + OutputFormatter.RESET);
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleEnvSwitch(String[] args) {
        if (args.length < 4) {
            OutputFormatter.error("Project name and environment name are required");
            System.out.println("Usage: pm env switch <name> <env-name>");
            System.exit(1);
        }

        String projectName = args[2];
        String envName = args[3];

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            Path source = project.path().resolve(".env." + envName);
            Path target = project.path().resolve(".env");

            if (!Files.isRegularFile(source)) {
                OutputFormatter.error("File '.env." + envName + "' not found in project directory");
                System.exit(1);
            }

            if (Files.exists(target)) {
                System.out.println("About to overwrite .env with .env." + envName
                        + " in project '" + projectName + "'");
                System.out.print("Are you sure? (y/n): ");

                String response = System.console() != null
                        ? System.console().readLine()
                        : "n";

                if (!response.toLowerCase().startsWith("y")) {
                    System.out.println("Aborted.");
                    return;
                }
            }

            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            OutputFormatter.success("Switched to .env." + envName + " for '" + projectName + "'");

        } catch (IOException e) {
            OutputFormatter.error("Failed to switch environment: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
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
          files <name>                         List .env files in project directory
          show <name> <file> [--show]          Show .env file contents (masked)
          switch <name> <env-name>             Copy .env.<env-name> to .env

        Examples:
          pm env set my-api PORT=8080,DEBUG=true
          pm env get my-api PORT
          pm env list my-api --show
          pm env files my-api
          pm env show my-api .env.production
          pm env switch my-api production
        """);
    }

    // ============================================================
    // COMMAND: HOOKS (Manage pre-/post-command hooks)
    // ============================================================

    /**
     * Handler for the "hooks" command.
     * Manages pre-/post-command hooks for a project.
     *
     * <p>Subcommands:
     * <ul>
     * <li>{@code pm hooks <project>} — list all hooks</li>
     * <li>{@code pm hooks <project> add <slot> "<script>"} — add a hook</li>
     * <li>{@code pm hooks <project> remove <slot> "<script>"} — remove a hook</li>
     * <li>{@code pm hooks --all} — list hooks for all projects</li>
     * </ul>
     *
     * @param args command arguments
     */
    private static void handleHooks(String[] args) {
        // Handle --all flag: pm hooks --all
        if (args.length >= 2 && args[1].equals("--all")) {
            handleHooksAll();
            return;
        }

        // Need at least a project name
        if (args.length < 2) {
            printHooksHelp();
            return;
        }

        String projectName = args[1];

        // Check for subcommands: pm hooks <project> add|remove
        if (args.length >= 3) {
            String subcommand = args[2].toLowerCase();
            switch (subcommand) {
                case "add" -> {
                    handleHooksAdd(args, projectName);
                    return;
                }
                case "remove", "rm" -> {
                    handleHooksRemove(args, projectName);
                    return;
                }
                default -> {
                    // Unknown subcommand — fall through to show hooks
                }
            }
        }

        // Default: show hooks for this project
        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            OutputFormatter.printHooks(projectName, project.hooks());

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Adds a hook to a project.
     *
     * <p>Usage: {@code pm hooks <project> add <slot> "<script>"}
     */
    private static void handleHooksAdd(String[] args, String projectName) {
        if (args.length < 5) {
            OutputFormatter.error("Hook slot and script are required");
            System.out.println("Usage: pm hooks <project> add <slot> \"<script>\"");
            System.out.println("Example: pm hooks my-app add pre-build \"npm run lint\"");
            System.exit(1);
        }

        String slot = args[3].toLowerCase();

        // Validate slot format: must be pre-<command> or post-<command>
        if (!slot.startsWith("pre-") && !slot.startsWith("post-")) {
            OutputFormatter.error("Invalid hook slot: " + slot);
            System.out.println("Slots must start with 'pre-' or 'post-' (e.g., pre-build, post-test)");
            System.exit(1);
        }

        String hookCommand = slot.startsWith("pre-") ? slot.substring(4) : slot.substring(5);
        if (hookCommand.isBlank()) {
            OutputFormatter.error("Invalid hook slot: " + slot);
            System.out.println("Slots must include a command name (e.g., pre-build, post-test)");
            System.exit(1);
        }

        // Join remaining args as the script (supports unquoted multi-word commands)
        String script = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            // Validate that the hook command matches an existing command on the project
            if (!project.hasCommand(hookCommand)) {
                OutputFormatter.warning("Project '" + projectName + "' has no '" + hookCommand +
                        "' command configured. The hook will run when the command is added.");
            }

            project.addHook(slot, script);
            store.saveProject(project);

            System.out.println();
            OutputFormatter.success("Hook added to '" + projectName + "'");
            System.out.println("  " + OutputFormatter.GREEN + slot + OutputFormatter.RESET +
                    " → " + OutputFormatter.CYAN + script + OutputFormatter.RESET);

        } catch (IOException e) {
            OutputFormatter.error("Failed to update project: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Removes a hook from a project by exact content match.
     *
     * <p>Usage: {@code pm hooks <project> remove <slot> "<script>"}
     */
    private static void handleHooksRemove(String[] args, String projectName) {
        if (args.length < 5) {
            OutputFormatter.error("Hook slot and script are required");
            System.out.println("Usage: pm hooks <project> remove <slot> \"<script>\"");
            System.out.println("Use 'pm hooks <project>' to see current hooks");
            System.exit(1);
        }

        String slot = args[3].toLowerCase();
        String script = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            boolean removed = project.removeHook(slot, script);
            if (!removed) {
                OutputFormatter.error("Hook not found in slot '" + slot + "': " + script);
                System.out.println("Use 'pm hooks " + projectName + "' to see current hooks");
                System.exit(1);
            }

            store.saveProject(project);

            System.out.println();
            OutputFormatter.success("Hook removed from '" + projectName + "'");
            System.out.println("  " + OutputFormatter.GRAY + slot + " → " + script + OutputFormatter.RESET);

        } catch (IOException e) {
            OutputFormatter.error("Failed to update project: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Lists hooks for all registered projects.
     */
    private static void handleHooksAll() {
        try {
            Map<String, Project> projects = store.load();

            if (projects.isEmpty()) {
                OutputFormatter.info("No projects registered");
                return;
            }

            boolean anyHooks = false;
            for (Project project : projects.values()) {
                if (project.hasHooks()) {
                    OutputFormatter.printHooks(project.name(), project.hooks());
                    anyHooks = true;
                }
            }

            if (!anyHooks) {
                System.out.println();
                OutputFormatter.info("No hooks configured in any project");
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Executes all hooks for a given slot.
     *
     * @param project the project whose hooks to run
     * @param slot hook slot (e.g., "pre-build", "post-run")
     * @return true if all hooks succeeded (or none existed), false if any failed
     */
    private static boolean executeHooks(Project project, String slot) {
        List<String> scripts = project.getHooks(slot);
        if (scripts.isEmpty()) {
            return true;
        }

        OutputFormatter.info("Running " + slot + " hooks...");

        for (String script : scripts) {
            try {
                CommandExecutor.ExecutionResult result = executor.execute(
                        script, project.path(), Constants.HOOK_TIMEOUT, project.envVars());

                if (!result.success()) {
                    OutputFormatter.error(slot + " hook failed: " + script);
                    if (result.message() != null && !result.message().isBlank()) {
                        System.out.println("  " + result.message());
                    }
                    return false;
                }
            } catch (IOException | InterruptedException e) {
                OutputFormatter.error(slot + " hook error: " + e.getMessage());
                return false;
            }
        }

        return true;
    }

    /**
     * Handles execution of any command not handled by specific handlers.
     * Enables running clean, stop, and custom commands with hook support.
     *
     * @param commandName the command name (e.g., "clean", "stop", or a custom command)
     * @param args full command arguments
     */
    private static void handleGenericCommand(String commandName, String[] args) {
        // Need a project name as second argument
        if (args.length < 2) {
            OutputFormatter.error("Unknown command: " + commandName);
            System.out.println("Run 'pm help' for usage information");
            System.exit(1);
        }

        String projectName = args[1];

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                // Not a known project — this is truly an unknown command
                OutputFormatter.error("Unknown command: " + commandName);
                System.out.println("Run 'pm help' for usage information");
                System.exit(1);
            }

            String cmdLine = project.getCommand(commandName);
            if (cmdLine == null) {
                OutputFormatter.error("No '" + commandName + "' command configured for project '" + projectName + "'");
                System.out.println("Use 'pm commands " + projectName + "' to see available commands");
                System.exit(1);
            }

            validateProjectPath(project);

            // Run pre-hooks
            if (!executeHooks(project, "pre-" + commandName)) {
                OutputFormatter.error("Pre-" + commandName + " hook failed. Command aborted.");
                System.exit(1);
            }

            System.out.println();
            OutputFormatter.info("Running '" + commandName + "' on " + projectName + "...");
            System.out.println("Command: " + cmdLine);
            System.out.println("Directory: " + project.path());
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            // Execute
            CommandExecutor.ExecutionResult result;
            if (System.console() != null) {
                result = executor.executeWithInheritedIO(cmdLine, project.path(), 300, project.envVars());
            } else {
                if (project.envVarCount() > 0) {
                    result = executor.execute(cmdLine, project.path(), 300, project.envVars());
                } else {
                    result = executor.execute(cmdLine, project.path(), 300);
                }
            }

            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            if (result.success()) {
                // Run post-hooks
                if (!executeHooks(project, "post-" + commandName)) {
                    OutputFormatter.warning("Post-" + commandName + " hook failed.");
                }
                OutputFormatter.success("'" + commandName + "' completed successfully");
                System.out.println("Duration: " + result.formattedDuration());
            } else {
                OutputFormatter.error("'" + commandName + "' failed");
                System.out.println("Exit code: " + result.exitCode());
                System.exit(1);
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        } catch (InterruptedException e) {
            OutputFormatter.warning("Command interrupted");
            System.exit(130);
        }
    }

    private static void printHooksHelp() {
        System.out.println("""
        Usage: pm hooks <project> [subcommand] [options]

        Subcommands:
          (none)                                   List all hooks for the project
          add <slot> "<script>"                    Add a hook
          remove <slot> "<script>"                 Remove a hook by exact content
          --all                                    List hooks for all projects

        Slots:
          pre-<command>    Runs before the command (failure aborts the command)
          post-<command>   Runs after the command (failure shows a warning)

        Examples:
          pm hooks my-api
          pm hooks my-api add pre-build "npm run lint"
          pm hooks my-api add post-build "echo Build done"
          pm hooks my-api remove pre-build "npm run lint"
          pm hooks --all
        """);
    }

    // ============================================================
    // COMMAND: REFRESH (Re-detect type and update commands)
    // ============================================================

    /**
     * Handler for the "refresh" command.
     * Re-detects the project type and replaces commands with the new defaults.
     *
     * <p>Usage:
     * <ul>
     * <li>{@code pm refresh <name>} — refresh a single project</li>
     * <li>{@code pm refresh --all} — refresh all registered projects</li>
     * </ul>
     */
    private static void handleRefresh(String[] args) {
        ArgsParser parser = new ArgsParser(args);
        boolean refreshAll = parser.hasFlag("all");
        String name = parser.getPositional(1);

        if (!refreshAll && (name == null || name.isBlank())) {
            OutputFormatter.error("Specify a project name or use --all");
            System.out.println("Usage:");
            System.out.println("  pm refresh <name>    Refresh a specific project");
            System.out.println("  pm refresh --all     Refresh all registered projects");
            System.exit(1);
        }

        try {
            if (refreshAll) {
                refreshAllProjects();
            } else {
                refreshSingleProject(name);
            }
        } catch (IOException e) {
            OutputFormatter.error("Failed to refresh: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void refreshSingleProject(String name) throws IOException {
        Project project = store.findProject(name);
        if (project == null) {
            OutputFormatter.error("Project not found: " + name);
            System.exit(1);
            return;
        }

        if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
            OutputFormatter.error("Project path does not exist: " + project.path());
            System.exit(1);
            return;
        }

        ProjectType oldType = project.type();
        ProjectType newType = ProjectTypeDetector.detect(project.path());

        // Create new project with detected type, preserving env vars
        Project refreshed = new Project(project.name(), project.path(), newType);
        CommandConfigurator.configureDefaultCommands(refreshed);

        // Detect secondary types
        List<ProjectType> allTypes = ProjectTypeDetector.detectAll(project.path());
        allTypes.stream()
                .filter(t -> t != newType)
                .forEach(refreshed::addSecondaryType);

        // Copy environment variables
        for (Map.Entry<String, String> entry : project.envVars().entrySet()) {
            refreshed.addEnvVar(entry.getKey(), entry.getValue());
        }

        store.saveProject(refreshed);

        System.out.println();
        if (oldType != newType) {
            OutputFormatter.success("Project '" + name + "' refreshed");
            System.out.println();
            System.out.println("  Type changed: " + OutputFormatter.YELLOW + oldType.displayName() +
                    OutputFormatter.RESET + " → " + OutputFormatter.GREEN + newType.displayName() + OutputFormatter.RESET);

            // Show old commands (if any)
            if (project.commandCount() > 0) {
                System.out.println();
                System.out.println("  " + OutputFormatter.YELLOW + "Old commands removed:" + OutputFormatter.RESET);
                for (Map.Entry<String, String> cmd : project.commands().entrySet()) {
                    System.out.println("    " + OutputFormatter.RED + "- " + cmd.getKey() + OutputFormatter.RESET +
                            " → " + OutputFormatter.GRAY + cmd.getValue() + OutputFormatter.RESET);
                }
            }

            // Show new commands
            System.out.println();
            System.out.println("  " + OutputFormatter.GREEN + "New commands configured:" + OutputFormatter.RESET);
            for (Map.Entry<String, String> cmd : refreshed.commands().entrySet()) {
                System.out.println("    " + OutputFormatter.GREEN + "+ " + cmd.getKey() + OutputFormatter.RESET +
                        " → " + OutputFormatter.CYAN + cmd.getValue() + OutputFormatter.RESET);
            }
        } else {
            OutputFormatter.success("Project '" + name + "' refreshed (commands updated)");
            System.out.println();
            System.out.println("  Type: " + newType.displayName() + " (unchanged)");

            // Show current commands
            System.out.println();
            System.out.println("  " + OutputFormatter.GREEN + "Commands:" + OutputFormatter.RESET);
            for (Map.Entry<String, String> cmd : refreshed.commands().entrySet()) {
                System.out.println("    " + OutputFormatter.GREEN + cmd.getKey() + OutputFormatter.RESET +
                        " → " + OutputFormatter.CYAN + cmd.getValue() + OutputFormatter.RESET);
            }
        }
        System.out.println();
    }

    private static void refreshAllProjects() throws IOException {
        Map<String, Project> projects = store.load();

        if (projects.isEmpty()) {
            System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
            return;
        }

        System.out.println();
        OutputFormatter.info("Checking " + projects.size() + " projects...");
        System.out.println();

        int updated = 0;
        int upToDate = 0;
        int errors = 0;

        for (Project project : projects.values()) {
            String name = project.name();

            if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
                System.out.println("  " + OutputFormatter.RED + "SKIP" + OutputFormatter.RESET +
                        "  " + padRight(name, 20) + OutputFormatter.GRAY + "path not found" + OutputFormatter.RESET);
                errors++;
                continue;
            }

            ProjectType oldType = project.type();
            ProjectType newType = ProjectTypeDetector.detect(project.path());

            if (oldType != newType) {
                // Create refreshed project
                Project refreshed = new Project(name, project.path(), newType);
                CommandConfigurator.configureDefaultCommands(refreshed);

                // Detect secondary types
                List<ProjectType> allTypes = ProjectTypeDetector.detectAll(project.path());
                allTypes.stream()
                        .filter(t -> t != newType)
                        .forEach(refreshed::addSecondaryType);

                // Copy environment variables
                for (Map.Entry<String, String> entry : project.envVars().entrySet()) {
                    refreshed.addEnvVar(entry.getKey(), entry.getValue());
                }

                store.saveProject(refreshed);

                System.out.println("  " + OutputFormatter.GREEN + "UPD " + OutputFormatter.RESET +
                        "  " + padRight(name, 20) +
                        OutputFormatter.YELLOW + oldType.displayName() + OutputFormatter.RESET +
                        " → " + OutputFormatter.GREEN + newType.displayName() + OutputFormatter.RESET);
                for (Map.Entry<String, String> cmd : refreshed.commands().entrySet()) {
                    System.out.println("        " + padRight("", 20) +
                            OutputFormatter.GREEN + "+ " + cmd.getKey() + OutputFormatter.RESET +
                            " → " + OutputFormatter.CYAN + cmd.getValue() + OutputFormatter.RESET);
                }
                updated++;
            } else {
                // Re-apply default commands (in case new commands were added to the type)
                Project refreshed = new Project(name, project.path(), newType);
                CommandConfigurator.configureDefaultCommands(refreshed);

                for (Map.Entry<String, String> entry : project.envVars().entrySet()) {
                    refreshed.addEnvVar(entry.getKey(), entry.getValue());
                }

                store.saveProject(refreshed);

                System.out.println("  " + OutputFormatter.GREEN + "OK  " + OutputFormatter.RESET +
                        "  " + padRight(name, 20) +
                        OutputFormatter.GRAY + newType.displayName() + OutputFormatter.RESET);
                upToDate++;
            }
        }

        System.out.println();
        System.out.println("  " + OutputFormatter.GREEN + updated + " updated" + OutputFormatter.RESET +
                ", " + upToDate + " refreshed" +
                (errors > 0 ? ", " + OutputFormatter.RED + errors + " skipped" + OutputFormatter.RESET : ""));
        System.out.println();
    }

    // ============================================================
    // COMMAND: DOCTOR (Environment check)
    // ============================================================

    /**
     * Handler for the "doctor" command.
     * Checks runtimes, validates projects, and shows health scores.
     *
     * <p>Usage:
     * <ul>
     *   <li>{@code pm doctor} — full report with health details</li>
     *   <li>{@code pm doctor --score} — compact grade-only output</li>
     * </ul>
     */
    private static void handleDoctor(String[] args) {
        boolean scoreOnly = Arrays.asList(args).contains("--score");

        if (!scoreOnly) {
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
                    {"Rust",    "cargo",   "--version"},
                    {"Go",      "go",      "version"},
                    {"pnpm",    "pnpm",    "--version"},
                    {"Bun",     "bun",     "--version"},
                    {"Yarn",    "yarn",    "--version"},
                    {"Flutter", "flutter", "--version"},
                    {"Docker",  "docker",  "--version"},
            };

            for (String[] rt : runtimes) {
                String name = rt[0];
                String command = rt[1];
                String flag = rt[2];

                String version = RuntimeChecker.getVersion(command, flag);
                if (version != null) {
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
        }

        try {
            Map<String, Project> projects = store.load();

            if (projects.isEmpty()) {
                System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                return;
            }

            if (!scoreOnly) {
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
            }

            // Health Report
            if (scoreOnly) {
                OutputFormatter.section("Health Scores");
            } else {
                System.out.println();
                OutputFormatter.section("Health Report");
            }

            for (Project project : projects.values()) {
                boolean pathExists = Files.exists(project.path()) && Files.isDirectory(project.path());
                if (!pathExists) {
                    if (scoreOnly) {
                        System.out.println("  " + OutputFormatter.RED + "-" + OutputFormatter.RESET +
                                "  " + project.name() + OutputFormatter.GRAY + "  (path not found)" + OutputFormatter.RESET);
                    } else {
                        System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                                " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);
                        System.out.println("    " + OutputFormatter.RED + "Cannot evaluate — path not found" + OutputFormatter.RESET);
                        System.out.println();
                    }
                    continue;
                }

                List<HealthCheck> checks = HealthScorer.evaluate(project);
                char grade = HealthScorer.grade(checks);
                String color = HealthScorer.gradeColor(grade);
                long passed = checks.stream().filter(HealthCheck::passed).count();

                if (scoreOnly) {
                    System.out.println("  " + color + grade + OutputFormatter.RESET +
                            "  " + project.name());
                } else {
                    System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                            " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);

                    for (HealthCheck check : checks) {
                        if (check.passed()) {
                            System.out.println("    " + OutputFormatter.GREEN + "\u2713" + OutputFormatter.RESET +
                                    " " + check.description());
                        } else {
                            System.out.println("    " + OutputFormatter.RED + "\u2717" + OutputFormatter.RESET +
                                    " " + check.description() +
                                    OutputFormatter.GRAY + " — " + check.recommendation() + OutputFormatter.RESET);
                        }
                    }

                    System.out.println("    Score: " + color + grade + OutputFormatter.RESET +
                            " (" + passed + "/" + checks.size() + ")");
                    System.out.println();
                }
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
        }

        System.out.println();
    }

    // ============================================================
    // COMMAND: SECURE (Security scan)
    // ============================================================

    /**
     * Handler for the "secure" command.
     * Scans projects for common security misconfigurations.
     *
     * <p>Usage:
     * <ul>
     *   <li>{@code pm secure} — full security report</li>
     *   <li>{@code pm secure --fix} — auto-fix .gitignore issues</li>
     * </ul>
     */
    private static void handleSecure(String[] args) {
        boolean fix = Arrays.asList(args).contains("--fix");

        OutputFormatter.section("Security Scan");

        try {
            Map<String, Project> projects = store.load();

            if (projects.isEmpty()) {
                System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                return;
            }

            for (Project project : projects.values()) {
                boolean pathExists = Files.exists(project.path()) && Files.isDirectory(project.path());

                System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                        " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);

                if (!pathExists) {
                    System.out.println("    " + OutputFormatter.RED + "Cannot evaluate — path not found" + OutputFormatter.RESET);
                    System.out.println();
                    continue;
                }

                List<SecurityCheck> checks = SecurityScorer.evaluate(project);
                List<String> fixActions = fix ? SecurityScorer.fix(project) : List.of();

                long passed = checks.stream().filter(SecurityCheck::passed).count();

                for (SecurityCheck check : checks) {
                    if (check.passed()) {
                        System.out.println("    " + OutputFormatter.GREEN + "\u2713" + OutputFormatter.RESET +
                                " " + padRight(check.description(), 18) +
                                OutputFormatter.GRAY + passedMessage(check) + OutputFormatter.RESET);
                    } else {
                        String fixNote = "";
                        if (fix && check.fixable()) {
                            String action = fixActions.stream()
                                    .filter(a -> a.toLowerCase().contains(check.name().contains("env") ? ".env" : "*.pem"))
                                    .findFirst().orElse("");
                            if (!action.isEmpty() && !action.startsWith("Failed")) {
                                fixNote = " " + OutputFormatter.GREEN + "\u2713 Fixed: " + action + OutputFormatter.RESET;
                            }
                        }

                        if (!fixNote.isEmpty()) {
                            System.out.println("    " + OutputFormatter.RED + "\u2717" + OutputFormatter.RESET +
                                    " " + padRight(check.description(), 18) + fixNote);
                        } else {
                            System.out.println("    " + OutputFormatter.RED + "\u2717" + OutputFormatter.RESET +
                                    " " + padRight(check.description(), 18) +
                                    OutputFormatter.GRAY + "— " + check.recommendation() + OutputFormatter.RESET);
                        }
                    }
                }

                String resultColor;
                if (passed == checks.size()) {
                    resultColor = OutputFormatter.GREEN;
                } else if (passed >= 3) {
                    resultColor = OutputFormatter.YELLOW;
                } else {
                    resultColor = OutputFormatter.RED;
                }

                long fixedCount = fix ? fixActions.stream()
                        .filter(a -> !a.startsWith("Failed")).count() : 0;
                String fixSuffix = fixedCount > 0
                        ? " (" + fixedCount + " auto-fixed)"
                        : "";

                System.out.println("    Result: " + resultColor + passed + "/" + checks.size() +
                        " checks passed" + OutputFormatter.RESET + fixSuffix);
                System.out.println();
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Returns a short message for a passing security check.
     */
    private static String passedMessage(SecurityCheck check) {
        return switch (check.name()) {
            case "dockerfile-root" -> "Non-root or N/A";
            case "env-gitignore" -> ".env covered in .gitignore";
            case "https-only" -> "No insecure HTTP URLs detected";
            case "sensitive-files" -> "Private keys covered in .gitignore";
            case "lockfile" -> "Dependencies lockfile present";
            default -> "OK";
        };
    }

    // ============================================================
    // COMMAND: AUDIT (Dependency vulnerability audit)
    // ============================================================

    /**
     * Handler for the {@code audit} command.
     *
     * <p>Runs native dependency audit tools on all registered projects
     * and displays a unified summary with severity breakdown.
     *
     * <p>Usage: {@code pm audit}
     */
    private static void handleAudit(String[] args) {
        OutputFormatter.section("Dependency Audit");

        try {
            Map<String, Project> projects = store.load();

            if (projects.isEmpty()) {
                System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                return;
            }

            DependencyAuditor auditor = new DependencyAuditor(executor);

            for (Project project : projects.values()) {
                boolean pathExists = Files.exists(project.path()) && Files.isDirectory(project.path());

                System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                        " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);

                if (!pathExists) {
                    System.out.println("    " + OutputFormatter.RED + "Cannot audit \u2014 path not found" + OutputFormatter.RESET);
                    System.out.println();
                    continue;
                }

                String toolName = DependencyAuditor.toolDisplayName(project.type());
                if (toolName != null) {
                    System.out.print("    " + OutputFormatter.GRAY + "Running " + toolName + "..." + OutputFormatter.RESET);
                    System.out.flush();
                }

                AuditReport report = auditor.audit(project.type(), project.path());

                if (toolName != null) {
                    // Clear the "Running..." line
                    System.out.print("\r" + " ".repeat(60) + "\r");
                }

                printAuditReport(report, project.type());
                System.out.println();
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Prints the audit report for a single project.
     */
    private static void printAuditReport(AuditReport report, ProjectType type) {
        switch (report.status()) {
            case CLEAN -> System.out.println("    " + OutputFormatter.GREEN + "\u2713" + OutputFormatter.RESET +
                    " No known vulnerabilities");

            case VULNERABLE -> {
                int total = report.totalVulnerabilities();
                System.out.println("    " + OutputFormatter.YELLOW + "\u26A0 " + total +
                        " vulnerabilit" + (total == 1 ? "y" : "ies") + " found" + OutputFormatter.RESET);

                // Severity breakdown
                java.util.Map<Severity, Long> counts = report.severityCounts();
                StringBuilder breakdown = new StringBuilder("      ");
                boolean first = true;
                for (Severity sev : Severity.values()) {
                    long count = counts.getOrDefault(sev, 0L);
                    if (count > 0) {
                        if (!first) breakdown.append(", ");
                        breakdown.append(count).append(" ").append(sev.name().toLowerCase());
                        first = false;
                    }
                }
                System.out.println(OutputFormatter.GRAY + breakdown + OutputFormatter.RESET);

                if (!report.suggestion().isEmpty()) {
                    System.out.println("    Suggestion: " + OutputFormatter.CYAN +
                            report.suggestion() + OutputFormatter.RESET);
                }
            }

            case TOOL_NOT_INSTALLED -> {
                System.out.println("    " + OutputFormatter.RED + "\u2717" + OutputFormatter.RESET +
                        " " + report.message());
                if (!report.suggestion().isEmpty()) {
                    System.out.println("      Install: " + OutputFormatter.CYAN +
                            report.suggestion() + OutputFormatter.RESET);
                }
            }

            case NO_TOOL -> {
                System.out.println("    " + OutputFormatter.BLUE + "\u2139" + OutputFormatter.RESET +
                        " No native audit tool for " + type.displayName());
                if (!report.message().isEmpty()) {
                    System.out.println("      " + OutputFormatter.GRAY + report.message() + OutputFormatter.RESET);
                }
            }

            case SKIPPED -> System.out.println("    " + OutputFormatter.GRAY +
                    "Skipped \u2014 no audit tool available" + OutputFormatter.RESET);

            case ERROR -> System.out.println("    " + OutputFormatter.RED + "\u2717" + OutputFormatter.RESET +
                    " Audit failed: " + report.message());
        }
    }

    // ============================================================
    // COMMAND: CI (Show CI/CD pipelines and dashboard URLs)
    // ============================================================

    /**
     * Handler for the {@code ci} command.
     *
     * <p>Shows detected CI/CD pipelines and dashboard URLs.
     * If a project name is given, shows CI for that project only.
     * If no name, shows CI for all projects.
     *
     * <p>Usage: {@code pm ci [name]}
     */
    private static void handleCI(String[] args) {
        ArgsParser parser = new ArgsParser(args);
        String projectName = parser.getPositional(1);

        try {
            if (projectName != null && !projectName.isBlank()) {
                // Single project
                Project project = store.findProject(projectName);
                if (project == null) {
                    OutputFormatter.error("Project '" + projectName + "' not found");
                    System.exit(1);
                }

                OutputFormatter.section("CI/CD — " + project.name());
                printCIForProject(project);
            } else {
                // All projects
                Map<String, Project> projects = store.load();
                if (projects.isEmpty()) {
                    OutputFormatter.section("CI/CD");
                    System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                    System.out.println();
                    return;
                }

                OutputFormatter.section("CI/CD");
                for (Project project : projects.values()) {
                    System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                            " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);

                    if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
                        System.out.println("    " + OutputFormatter.RED + "Path not found" + OutputFormatter.RESET);
                        System.out.println();
                        continue;
                    }

                    List<CIProvider> providers = CIDetector.detect(project.path());
                    if (providers.isEmpty()) {
                        System.out.println("    " + OutputFormatter.GRAY + "No CI/CD configured" + OutputFormatter.RESET);
                    } else {
                        String remoteUrl = GitIntegration.getRemoteUrl(project.path());
                        for (CIProvider provider : providers) {
                            String url = CIDetector.dashboardUrl(provider, remoteUrl);
                            String urlSuffix = url != null ? " — " + OutputFormatter.CYAN + url + OutputFormatter.RESET : "";
                            System.out.println("    " + OutputFormatter.GREEN + "✓" + OutputFormatter.RESET +
                                    " " + provider.displayName() + urlSuffix);
                        }
                    }
                    System.out.println();
                }
            }
        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Prints CI/CD details for a single project including dashboard URLs.
     */
    private static void printCIForProject(Project project) {
        if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
            System.out.println("  " + OutputFormatter.RED + "Path not found: " + project.path() + OutputFormatter.RESET);
            System.out.println();
            return;
        }

        List<CIProvider> providers = CIDetector.detect(project.path());
        if (providers.isEmpty()) {
            System.out.println("  " + OutputFormatter.GRAY + "No CI/CD configured" + OutputFormatter.RESET);
            System.out.println();
            return;
        }

        String remoteUrl = GitIntegration.getRemoteUrl(project.path());

        for (CIProvider provider : providers) {
            String detail = "";
            if (provider == CIProvider.GITHUB_ACTIONS) {
                int count = CIDetector.workflowCount(project.path());
                if (count > 0) {
                    detail = " (" + count + " workflow" + (count > 1 ? "s" : "") + ")";
                }
            }
            System.out.println("  " + OutputFormatter.GREEN + "✓" + OutputFormatter.RESET +
                    " " + provider.displayName() + detail);

            String url = CIDetector.dashboardUrl(provider, remoteUrl);
            if (url != null) {
                System.out.println("    " + OutputFormatter.CYAN + url + OutputFormatter.RESET);
            } else if (provider == CIProvider.JENKINS) {
                System.out.println("    " + OutputFormatter.GRAY + "Open your Jenkins server to view pipelines" + OutputFormatter.RESET);
            } else if (remoteUrl == null) {
                System.out.println("    " + OutputFormatter.GRAY + "No git remote — URL unavailable" + OutputFormatter.RESET);
            }
        }
        System.out.println();
    }

    // ============================================================
    // COMMAND: LINT (Run linters on project)
    // ============================================================

    private static void handleLint(String[] args) {
        ArgsParser parser = new ArgsParser(args);
        String projectName = parser.getPositional(1);

        try {
            if (projectName != null && !projectName.isBlank()) {
                Project project = store.findProject(projectName);
                if (project == null) {
                    OutputFormatter.error("Project '" + projectName + "' not found");
                    System.exit(1);
                }

                OutputFormatter.section("Lint \u2014 " + project.name());
                runLintTools(project);
            } else {
                Map<String, Project> projects = store.load();
                if (projects.isEmpty()) {
                    OutputFormatter.section("Lint");
                    System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                    System.out.println();
                    return;
                }

                OutputFormatter.section("Lint");
                for (Project project : projects.values()) {
                    System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                            " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);

                    if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
                        System.out.println("    " + OutputFormatter.RED + "Path not found" + OutputFormatter.RESET);
                        System.out.println();
                        continue;
                    }

                    runLintTools(project);
                    System.out.println();
                }
            }
        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
        }

        System.out.println();
    }

    private static void runLintTools(Project project) {
        List<LintTool> tools = LintDetector.detect(project.type(), project.path());

        if (tools.isEmpty()) {
            System.out.println("  " + OutputFormatter.GRAY + "No lint tools detected" + OutputFormatter.RESET);
            return;
        }

        int passed = 0;
        for (LintTool tool : tools) {
            System.out.println();
            System.out.println("  Running " + tool.displayName() + "...");
            System.out.println("  " + "\u2500".repeat(40));

            try {
                CommandExecutor.ExecutionResult result;
                if (System.console() != null) {
                    result = executor.executeWithInheritedIO(tool.command(), project.path(), 300, project.envVars());
                } else {
                    result = executor.execute(tool.command(), project.path(), 300, project.envVars());
                }

                System.out.println("  " + "\u2500".repeat(40));
                if (result.success()) {
                    System.out.println("  " + OutputFormatter.GREEN + "\u2713" + OutputFormatter.RESET +
                            " " + tool.displayName() + " passed (" + result.formattedDuration() + ")");
                    passed++;
                } else {
                    System.out.println("  " + OutputFormatter.RED + "\u2717" + OutputFormatter.RESET +
                            " " + tool.displayName() + " failed (exit code " + result.exitCode() + ")");
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("  " + "\u2500".repeat(40));
                System.out.println("  " + OutputFormatter.RED + "\u2717" + OutputFormatter.RESET +
                        " " + tool.displayName() + " error: " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("  Result: " + passed + "/" + tools.size() + " tools passed");
    }

    // ============================================================
    // COMMAND: FMT (Run formatters on project)
    // ============================================================

    private static void handleFmt(String[] args) {
        ArgsParser parser = new ArgsParser(args);
        String projectName = parser.getPositional(1);

        try {
            if (projectName != null && !projectName.isBlank()) {
                Project project = store.findProject(projectName);
                if (project == null) {
                    OutputFormatter.error("Project '" + projectName + "' not found");
                    System.exit(1);
                }

                OutputFormatter.section("Format \u2014 " + project.name());
                runFormatTools(project);
            } else {
                Map<String, Project> projects = store.load();
                if (projects.isEmpty()) {
                    OutputFormatter.section("Format");
                    System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                    System.out.println();
                    return;
                }

                OutputFormatter.section("Format");
                for (Project project : projects.values()) {
                    System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                            " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);

                    if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
                        System.out.println("    " + OutputFormatter.RED + "Path not found" + OutputFormatter.RESET);
                        System.out.println();
                        continue;
                    }

                    runFormatTools(project);
                    System.out.println();
                }
            }
        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
        }

        System.out.println();
    }

    private static void runFormatTools(Project project) {
        List<FormatTool> tools = FormatDetector.detect(project.type(), project.path());

        if (tools.isEmpty()) {
            System.out.println("  " + OutputFormatter.GRAY + "No format tools detected" + OutputFormatter.RESET);
            return;
        }

        int passed = 0;
        for (FormatTool tool : tools) {
            System.out.println();
            System.out.println("  Running " + tool.displayName() + "...");
            System.out.println("  " + "\u2500".repeat(40));

            try {
                CommandExecutor.ExecutionResult result;
                if (System.console() != null) {
                    result = executor.executeWithInheritedIO(tool.command(), project.path(), 300, project.envVars());
                } else {
                    result = executor.execute(tool.command(), project.path(), 300, project.envVars());
                }

                System.out.println("  " + "\u2500".repeat(40));
                if (result.success()) {
                    System.out.println("  " + OutputFormatter.GREEN + "\u2713" + OutputFormatter.RESET +
                            " " + tool.displayName() + " done (" + result.formattedDuration() + ")");
                    passed++;
                } else {
                    System.out.println("  " + OutputFormatter.RED + "\u2717" + OutputFormatter.RESET +
                            " " + tool.displayName() + " failed (exit code " + result.exitCode() + ")");
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("  " + "\u2500".repeat(40));
                System.out.println("  " + OutputFormatter.RED + "\u2717" + OutputFormatter.RESET +
                        " " + tool.displayName() + " error: " + e.getMessage());
            }
        }

        System.out.println();
        System.out.println("  Result: " + passed + "/" + tools.size() + " tools completed");
    }

    // ============================================================
    // COMMAND: MODULES (Show workspace modules)
    // ============================================================

    private static void handleModules(String[] args) {
        ArgsParser parser = new ArgsParser(args);
        String projectName = parser.getPositional(1);

        try {
            if (projectName != null && !projectName.isBlank()) {
                Project project = store.findProject(projectName);
                if (project == null) {
                    OutputFormatter.error("Project '" + projectName + "' not found");
                    System.exit(1);
                }

                OutputFormatter.section("Workspace Modules — " + project.name());
                printWorkspaceModules(project);
            } else {
                Map<String, Project> projects = store.load();
                if (projects.isEmpty()) {
                    OutputFormatter.section("Workspace Modules");
                    System.out.println("  " + OutputFormatter.GRAY + "No projects registered" + OutputFormatter.RESET);
                    System.out.println();
                    return;
                }

                OutputFormatter.section("Workspace Modules");
                boolean anyModules = false;
                for (Project project : projects.values()) {
                    if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
                        continue;
                    }

                    List<WorkspaceModule> modules = WorkspaceDetector.detect(project.type(), project.path());
                    if (!modules.isEmpty()) {
                        anyModules = true;
                        System.out.println("  " + OutputFormatter.BOLD + project.name() + OutputFormatter.RESET +
                                " " + OutputFormatter.GRAY + "(" + project.type().displayName() + ")" + OutputFormatter.RESET);
                        printModuleTable(modules);
                        System.out.println();
                    }
                }

                if (!anyModules) {
                    System.out.println("  " + OutputFormatter.GRAY + "No workspaces detected" + OutputFormatter.RESET);
                }
            }
        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
        }

        System.out.println();
    }

    private static void printWorkspaceModules(Project project) {
        if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
            System.out.println("  " + OutputFormatter.RED + "Path not found" + OutputFormatter.RESET);
            return;
        }

        List<WorkspaceModule> modules = WorkspaceDetector.detect(project.type(), project.path());

        if (modules.isEmpty()) {
            System.out.println("  " + OutputFormatter.GRAY + "No workspace modules detected" + OutputFormatter.RESET);
            System.out.println();
            return;
        }

        printModuleTable(modules);
        System.out.println();
        System.out.println("  " + modules.size() + " module" + (modules.size() != 1 ? "s" : "") + " detected");
    }

    private static void printModuleTable(List<WorkspaceModule> modules) {
        // Calculate column widths
        int nameWidth = Math.max(4, modules.stream().mapToInt(m -> m.name().length()).max().orElse(4));
        int pathWidth = Math.max(4, modules.stream().mapToInt(m -> m.relativePath().length()).max().orElse(4));

        System.out.println();
        System.out.println("  " + padRight("Name", nameWidth + 2) + padRight("Path", pathWidth + 2) + "Type");
        System.out.println("  " + "─".repeat(nameWidth + pathWidth + 16));

        for (WorkspaceModule module : modules) {
            System.out.println("  " + padRight(module.name(), nameWidth + 2) +
                    padRight(module.relativePath(), pathWidth + 2) +
                    module.type().displayName());
        }
    }

    // ============================================================
    // COMMAND: MIGRATE (Database migration awareness)
    // ============================================================

    /**
     * Handler for the "migrate" command.
     *
     * <ul>
     *   <li>{@code pm migrate} — list detected migration tools for all projects</li>
     *   <li>{@code pm migrate <name>} — run migration with confirmation</li>
     *   <li>{@code pm migrate <name> status} — show migration status (read-only)</li>
     * </ul>
     */
    private static void handleMigrate(String[] args) {
        if (args.length < 2) {
            // pm migrate — list all
            handleMigrateList();
            return;
        }

        String projectName = args[1];

        try {
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            List<MigrationTool> tools = MigrationDetector.detect(project.path());
            if (tools.isEmpty()) {
                OutputFormatter.info("No migration tools detected for '" + projectName + "'");
                return;
            }

            boolean isStatus = args.length >= 3 && "status".equalsIgnoreCase(args[2]);

            if (isStatus) {
                handleMigrateStatus(project, tools);
            } else {
                handleMigrateRun(project, tools);
            }

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleMigrateList() {
        try {
            Map<String, Project> projects = store.load();
            if (projects.isEmpty()) {
                OutputFormatter.info("No projects registered");
                return;
            }

            OutputFormatter.section("Migration Tools");
            System.out.println();

            boolean anyFound = false;
            for (Project project : projects.values()) {
                List<MigrationTool> tools = MigrationDetector.detect(project.path());
                if (!tools.isEmpty()) {
                    anyFound = true;
                    String toolNames = tools.stream()
                            .map(MigrationTool::displayName)
                            .collect(java.util.stream.Collectors.joining(", "));
                    System.out.println("  " + OutputFormatter.BOLD + project.name()
                            + OutputFormatter.RESET + " " + OutputFormatter.GRAY
                            + "(" + project.type().displayName() + ")" + OutputFormatter.RESET
                            + " → " + toolNames);
                }
            }

            if (!anyFound) {
                System.out.println("  " + OutputFormatter.GRAY
                        + "No migration tools detected in any project" + OutputFormatter.RESET);
            }
            System.out.println();

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleMigrateRun(Project project, List<MigrationTool> tools) {
        MigrationTool tool = tools.get(0);

        if (!RuntimeChecker.isCommandAvailable(tool.binary(), "--version")) {
            OutputFormatter.error(tool.displayName() + " is not installed or not in PATH");
            System.out.println("  Install " + tool.displayName() + " and try again.");
            System.exit(1);
        }

        System.out.println();
        System.out.println("About to run: " + OutputFormatter.CYAN + tool.migrateCommand()
                + OutputFormatter.RESET);
        System.out.println("Project: " + OutputFormatter.BOLD + project.name()
                + OutputFormatter.RESET + " (" + project.path() + ")");
        System.out.print("\nAre you sure? (y/n): ");

        String response = System.console() != null
                ? System.console().readLine()
                : "n";

        if (!response.toLowerCase().startsWith("y")) {
            System.out.println("Aborted.");
            return;
        }

        System.out.println();

        try {
            CommandExecutor executor = new CommandExecutor();
            var result = executor.executeWithInheritedIO(
                    tool.migrateCommand(), project.path(), 300, project.envVars());

            System.out.println();
            if (result.success()) {
                OutputFormatter.success("Migration completed for '" + project.name() + "'");
            } else {
                OutputFormatter.error("Migration failed for '" + project.name()
                        + "' (exit code " + result.exitCode() + ")");
            }
        } catch (IOException | InterruptedException e) {
            OutputFormatter.error("Migration execution failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void handleMigrateStatus(Project project, List<MigrationTool> tools) {
        MigrationTool tool = tools.get(0);

        if (!RuntimeChecker.isCommandAvailable(tool.binary(), "--version")) {
            OutputFormatter.error(tool.displayName() + " is not installed or not in PATH");
            System.out.println("  Install " + tool.displayName() + " and try again.");
            System.exit(1);
        }

        OutputFormatter.section("Migration Status — " + project.name()
                + " (" + tool.displayName() + ")");
        System.out.println();

        try {
            CommandExecutor executor = new CommandExecutor();
            var result = executor.executeWithInheritedIO(
                    tool.statusCommand(), project.path(), 60, project.envVars());

            if (!result.success()) {
                System.out.println();
                OutputFormatter.warning("Status command exited with code " + result.exitCode());
            }
        } catch (IOException | InterruptedException e) {
            OutputFormatter.error("Status check failed: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // COMMAND: EXPORT (Export projects to JSON file)
    // ============================================================

    /**
     * Handler for the {@code export} command.
     *
     * <p>Exports all or selected projects to a portable JSON file.
     *
     * <p>Usage: {@code pm export [names...] [--file <path>]}
     */
    private static void handleExport(String[] args) {
        ArgsParser parser = new ArgsParser(args);
        String fileFlag = parser.getFlag("file");
        Path outputFile = Paths.get(fileFlag != null ? fileFlag : "pm-export.json").toAbsolutePath().normalize();

        // Collect project names (positional args after "export")
        List<String> projectNames = new java.util.ArrayList<>();
        for (int i = 1; i < parser.positionalCount(); i++) {
            projectNames.add(parser.getPositional(i));
        }

        OutputFormatter.section("Export");

        try {
            ProjectExporter exporter = new ProjectExporter(store);
            ExportResult result = exporter.export(outputFile, projectNames.isEmpty() ? null : projectNames);

            OutputFormatter.success("Exported " + result.exported() + " project" +
                    (result.exported() != 1 ? "s" : "") + " to " + result.outputFile().getFileName());

            for (String name : result.notFound()) {
                OutputFormatter.warning("Project not found: " + name);
            }
        } catch (IOException e) {
            OutputFormatter.error("Export failed: " + e.getMessage());
        }

        System.out.println();
    }

    // ============================================================
    // COMMAND: IMPORT (Import projects from JSON file)
    // ============================================================

    /**
     * Handler for the {@code import} command.
     *
     * <p>Imports projects from a previously exported JSON file.
     *
     * <p>Usage: {@code pm import <file>}
     */
    private static void handleImport(String[] args) {
        ArgsParser parser = new ArgsParser(args);
        String filePath = parser.getPositional(1);

        if (filePath == null || filePath.isBlank()) {
            OutputFormatter.error("File path is required");
            System.out.println("Usage: pm import <file>");
            System.exit(1);
        }

        // Expand ~ to home directory
        String expandedPath = filePath.replace("~", System.getProperty("user.home"));
        Path inputFile = Paths.get(expandedPath).toAbsolutePath().normalize();

        if (!Files.exists(inputFile)) {
            OutputFormatter.error("File not found: " + inputFile);
            System.exit(1);
        }

        OutputFormatter.section("Import");

        try {
            ProjectExporter exporter = new ProjectExporter(store);
            ImportResult result = exporter.importProjects(inputFile);

            OutputFormatter.success("Imported " + result.imported() + " project" +
                    (result.imported() != 1 ? "s" : ""));

            for (String name : result.skipped()) {
                OutputFormatter.warning("Skipped '" + name + "' — already exists");
            }

            for (String warning : result.warnings()) {
                OutputFormatter.warning(warning);
            }
        } catch (IllegalArgumentException e) {
            OutputFormatter.error("Invalid export file: " + e.getMessage());
        } catch (IOException e) {
            OutputFormatter.error("Import failed: " + e.getMessage());
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

    /**
     * Checks if a project's stored type differs from what would be detected now.
     * If so, prints a hint suggesting the user run pm refresh.
     */
    private static void checkTypeOutdated(Project project) {
        try {
            if (!Files.exists(project.path()) || !Files.isDirectory(project.path())) {
                return;
            }
            ProjectType detected = ProjectTypeDetector.detect(project.path());
            if (detected != project.type() && detected != ProjectType.UNKNOWN) {
                System.out.println("  " + OutputFormatter.YELLOW + "hint:" + OutputFormatter.RESET +
                        " detected type is " + OutputFormatter.GREEN + detected.displayName() + OutputFormatter.RESET +
                        " but project is registered as " + OutputFormatter.YELLOW + project.type().displayName() + OutputFormatter.RESET);
                System.out.println("  Run 'pm refresh " + project.name() + "' to update");
                System.out.println();
            }
        } catch (Exception ignored) {
            // Don't fail the main command because of the hint
        }
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

    // ============================================================
    // COMMAND: CONFIG (User preferences)
    // ============================================================

    private static void handleConfig(String[] args) {
        if (args.length < 2) {
            printConfigHelp();
            return;
        }

        String key = args[1].toLowerCase();
        switch (key) {
            case "telemetry" -> {
                if (args.length < 3) {
                    System.out.println("  Telemetry: " + (Telemetry.isEnabled() ? "on" : "off"));
                    return;
                }
                String value = args[2].toLowerCase();
                switch (value) {
                    case "on" -> {
                        Telemetry.setEnabled(true);
                        OutputFormatter.success("Telemetry enabled.");
                    }
                    case "off" -> {
                        Telemetry.setEnabled(false);
                        OutputFormatter.success("Telemetry disabled.");
                    }
                    default -> {
                        OutputFormatter.error("Invalid value: " + args[2]);
                        System.out.println("Usage: pm config telemetry [on|off]");
                    }
                }
            }
            default -> {
                OutputFormatter.error("Unknown config key: " + key);
                printConfigHelp();
            }
        }
    }

    private static void printConfigHelp() {
        System.out.println("""
        Usage: pm config <key> [value]

        Keys:
          telemetry [on|off]     Enable/disable anonymous usage statistics
        """);
    }

    private static void printHelp() {
        System.out.println("""
        Usage: pm <command> [options]

        Commands:
          add <name> --path <path> [--env <vars>]  Register a new project
          list, ls                                  List all projects
          build <name> [--all]                       Build project (or all with --all)
          run <name>                                Run project
          test <name> [--all]                        Run tests (or all with --all)
          <cmd> <name>                              Run any registered command
          scan <name>                               Scan for commands in code
          commands, cmd <name>                      List available commands
          commands <name> add <cmd> "<line>"         Add a custom command
          commands <name> remove <cmd>               Remove a command
          commands --all                              List commands for all projects
          hooks <name>                              List hooks for a project
          hooks <name> add <slot> "<script>"         Add a pre-/post-command hook
          hooks <name> remove <slot> "<script>"      Remove a hook
          hooks --all                                List hooks for all projects
          remove, rm <name>                         Remove project
          rename <name> [new-name] [--path <path>]   Rename project or update path
          info <name>                               Show project details
          env <subcommand> <name> [options]         Manage environment variables
          refresh <name>                            Re-detect type and update commands
          refresh --all                             Refresh all registered projects
          completions <shell>                       Generate completion script (bash/zsh/fish/powershell)
          update                                    Update to the latest version
          doctor [--score]                            Check environment, runtimes, and project health (A/B/C/D/F)
          secure [--fix]                              Scan projects for security misconfigurations
          audit                                     Audit dependencies for known vulnerabilities
          ci [name]                                   Show CI/CD pipelines and dashboard URLs
          lint [name]                                 Run linters on project(s)
          fmt [name]                                  Run formatters on project(s)
          modules [name]                              Show workspace modules
          migrate                                     List detected migration tools
          migrate <name>                              Run database migration (with confirmation)
          migrate <name> status                       Check migration status
          export [names...] [--file <path>]           Export projects to JSON file
          import <file>                               Import projects from JSON file
          config telemetry [on|off]                    Manage telemetry settings
          help                                      Show this help
          version                                   Show version

        Environment Variables (pm env):
          env set <name> KEY=VALUE[,KEY2=VALUE2]    Set variables
          env get <name> KEY                        Get a variable value
          env list <name> [--show]                  List variables (masked by default)
          env remove <name> KEY                     Remove a variable
          env clear <name>                          Remove all variables
          env files <name>                          List .env files in project directory
          env show <name> <file> [--show]           Show .env file contents (masked)
          env switch <name> <env-name>              Copy .env.<env-name> to .env

        Hooks (pm hooks):
          hooks <name>                              List all hooks
          hooks <name> add pre-<cmd> "<script>"     Add a pre-command hook
          hooks <name> add post-<cmd> "<script>"    Add a post-command hook
          hooks <name> remove <slot> "<script>"     Remove a hook by exact content
          hooks --all                               List hooks for all projects

        Examples:
          pm add backend-api --path ~/projects/backend-api
          pm add web-server --path ~/projects/web-server --env "PORT=3000,DEBUG=true"
          pm list
          pm build backend-api
          pm run web-server
          pm env set web-server PORT=3000,DEBUG=true
          pm env list web-server
          pm commands backend-api
          pm commands backend-api add deploy "docker compose up -d"
          pm commands --all
          pm hooks my-api add pre-build "npm run lint"
          pm hooks my-api
          pm info web-server
          pm export
          pm export backend-api web-server --file my-setup.json
          pm import pm-export.json
        """);
    }

    private static void handleCompletions(String[] args) {
        if (args.length < 2) {
            OutputFormatter.error("Shell name required.");
            System.out.println("Usage: pm completions <shell>");
            System.out.println("Supported shells: bash, zsh, fish, powershell");
            System.out.println();
            System.out.println("Setup:");
            System.out.println("  Bash:       eval \"$(pm completions bash)\"       # add to ~/.bashrc");
            System.out.println("  Zsh:        eval \"$(pm completions zsh)\"        # add to ~/.zshrc");
            System.out.println("  Fish:       pm completions fish > ~/.config/fish/completions/pm.fish");
            System.out.println("  PowerShell: pm completions powershell | Out-String | Invoke-Expression  # add to $PROFILE");
            return;
        }

        String shell = args[1].toLowerCase();
        String script = switch (shell) {
            case "bash" -> CompletionScripts.bash();
            case "zsh" -> CompletionScripts.zsh();
            case "fish" -> CompletionScripts.fish();
            case "powershell", "pwsh" -> CompletionScripts.powershell();
            default -> null;
        };

        if (script == null) {
            OutputFormatter.error("Unsupported shell: " + args[1]);
            System.out.println("Supported: bash, zsh, fish, powershell");
            return;
        }

        // Print raw script to stdout (no ANSI, suitable for eval/sourcing)
        System.out.print(script);
    }

    private static void printVersion() {
        System.out.println("ProjectManager " + Constants.VERSION);
        System.out.println("Java " + System.getProperty("java.version"));
    }

    // Characters that have special meaning in both sh and cmd.exe
    private static final String SHELL_METACHARACTERS = "&|;<>`$(){}";

    /**
     * Validates that a project's directory exists before command execution.
     * Prints a descriptive error and exits if the directory is missing.
     *
     * @param project the project to validate
     */
    private static void validateProjectPath(Project project) {
        if (!Files.exists(project.path())) {
            OutputFormatter.error("Project directory not found: " + project.path());
            System.out.println("The directory may have been moved, renamed, or deleted.");
            System.out.println("To update the path, run:");
            System.out.println("  pm rename " + project.name() + " --path <new-path>");
            System.exit(1);
        }
        if (!Files.isDirectory(project.path())) {
            OutputFormatter.error("Project path is not a directory: " + project.path());
            System.out.println("The registered path points to a file, not a directory.");
            System.out.println("To update the path, run:");
            System.out.println("  pm rename " + project.name() + " --path <new-path>");
            System.exit(1);
        }
    }

    /**
     * Checks if a command string contains shell metacharacters that could
     * cause unexpected behavior if not intentional.
     *
     * @param command the command string to check
     * @return true if metacharacters are found
     */
    private static boolean containsShellMetacharacters(String command) {
        for (int i = 0; i < SHELL_METACHARACTERS.length(); i++) {
            if (command.indexOf(SHELL_METACHARACTERS.charAt(i)) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a readable list of which shell metacharacters were found.
     *
     * @param command the command string to scan
     * @return comma-separated list of found characters, e.g. "'|', ';'"
     */
    private static String getFoundMetacharacters(String command) {
        StringBuilder found = new StringBuilder();
        for (int i = 0; i < SHELL_METACHARACTERS.length(); i++) {
            char c = SHELL_METACHARACTERS.charAt(i);
            if (command.indexOf(c) >= 0) {
                if (found.length() > 0) found.append(", ");
                found.append("'").append(c).append("'");
            }
        }
        return found.toString();
    }

    /**
     * Produces a user-friendly description for an IOException.
     * Maps common I/O failure causes to actionable messages.
     *
     * @param e the IOException
     * @return human-readable error description
     */
    private static String describeIOError(IOException e) {
        String message = e.getMessage();
        if (message == null) {
            message = e.getClass().getSimpleName();
        }

        // Corrupted JSON (our ProjectStore wraps these)
        if (message.contains("corrupted")) {
            return message;
        }

        // Disk full
        String lower = message.toLowerCase();
        if (lower.contains("no space") || lower.contains("disk full") || lower.contains("not enough space")) {
            return "Disk is full — free some space and try again.";
        }

        // Permission denied
        if (lower.contains("permission denied") || lower.contains("access denied")) {
            return "Permission denied — check file permissions on ~/.projectmanager/";
        }

        // File not found
        if (lower.contains("no such file") || lower.contains("cannot find")) {
            return "File not found: " + message;
        }

        return "Failed: " + message;
    }

    /**
     * Handles any unhandled exception that escapes a command handler.
     * Dispatches to specific messages based on exception type.
     *
     * @param e the exception
     */
    static void handleFatalError(Exception e) {
        if (e instanceof AccessDeniedException ade) {
            OutputFormatter.error("Permission denied: " + ade.getFile());
            System.out.println("Check that you have read/write access to the file.");
            System.out.println("On Linux/Mac, try: chmod 644 " + ade.getFile());
        } else if (e instanceof FileSystemException fse) {
            String msg = fse.getReason() != null ? fse.getReason() : fse.getMessage();
            OutputFormatter.error("File system error: " + msg);
            System.out.println("This may indicate a full disk, a read-only filesystem, or a locked file.");
            System.out.println("File: " + fse.getFile());
        } else if (e instanceof IOException ioe) {
            OutputFormatter.error(describeIOError(ioe));
        } else {
            OutputFormatter.error("Unexpected error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
            System.out.println("If this persists, run 'pm doctor' to diagnose your environment.");
        }
        System.exit(1);
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