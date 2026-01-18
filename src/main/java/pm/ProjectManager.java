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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Clase principal de ProjectManager - CLI para gestionar múltiples proyectos.
 *
 * <p><b>¿Qué hace ProjectManager?</b>
 * <ul>
 *   <li>Registra proyectos de diferentes tipos (Java, C#, Node.js, etc)</li>
 *   <li>Detecta automáticamente el tipo de proyecto</li>
 *   <li>Ejecuta comandos (build, run, test) sin recordar sintaxis específica</li>
 *   <li>Escanea código fuente para encontrar comandos (ej: @Command en Minecraft)</li>
 *   <li>Mantiene un registro centralizado de todos tus proyectos</li>
 * </ul>
 *
 * <p><b>Comandos disponibles:</b>
 * * <pre>
 *  * pm add NAME --path PATH          Register new project
 *  * pm list                          List all projects
 *  * pm build NAME                    Build project
 *  * pm run NAME                      Run project
 *  * pm test NAME                     Run tests
 *  * pm scan NAME                     Scan for commands in code
 *  * pm commands NAME                 List available commands
 *  * pm remove NAME                   Remove project
 *  * pm info NAME                     Show project information
 *  * pm help                          Show help
 *  * </pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProjectManager {

    // Instancias de servicios (pattern: dependency injection manual)
    private static final ProjectStore store = new ProjectStore();
    private static final ProjectTypeDetector detector = new ProjectTypeDetector();
    private static final CommandExecutor executor = new CommandExecutor();

    /**
     * Punto de entrada de la aplicación.
     *
     * @param args argumentos de línea de comandos
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
                case "help", "-h", "--help" -> printHelp();
                case "version", "-v", "--version" -> printVersion();
                default -> {
                    OutputFormatter.error("Unknown command: " + command);
                    System.out.println("Run 'pm help' for usage information");
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            // Capturar cualquier excepción no manejada
            OutputFormatter.error("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // ============================================================
    // COMANDO: ADD (Registrar nuevo proyecto)
    // ============================================================

    /**
     * Handler para el comando "add".
     * Registra un nuevo proyecto en ProjectManager.
     *
     * <p>Usage: {@code pm add NAME --path PATH [--type TYPE]}
     *
     * <p>Proceso:
     * <ol>
     *   <li>Validar argumentos (nombre y path son obligatorios)</li>
     *   <li>Validar que el path existe y es un directorio</li>
     *   <li>Detectar tipo de proyecto (o usar --type si se especifica)</li>
     *   <li>Crear objeto Project</li>
     *   <li>Configurar comandos por defecto</li>
     *   <li>Guardar en projects.json</li>
     *   <li>Confirmar al usuario</li>
     * </ol>
     *
     * @param args argumentos del comando
     */
    private static void handleAdd(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        // Validar argumentos
        // args[0] = "add", args[1] = nombre del proyecto
        String projectName = parser.getPositional(1);
        if (projectName == null || projectName.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm add <name> --path <path> [--type <type>]");
            System.exit(1);
        }

        // Obtener path del proyecto
        String pathStr = parser.getFlag("path");
        if (pathStr == null || pathStr.isBlank()) {
            OutputFormatter.error("Project path is required");
            System.out.println("Usage: pm add <name> --path <path> [--type <type>]");
            System.exit(1);
        }

        // Convertir a Path y expandir ~ si es necesario
        Path projectPath = Paths.get(pathStr);
        if (pathStr.startsWith("~")) {
            // Expandir ~ a home directory
            projectPath = Paths.get(System.getProperty("user.home") + pathStr.substring(1));
        }

        // Validar que el path existe
        if (!Files.exists(projectPath)) {
            OutputFormatter.error("Path does not exist: " + projectPath);
            System.exit(1);
        }

        if (!Files.isDirectory(projectPath)) {
            OutputFormatter.error("Path is not a directory: " + projectPath);
            System.exit(1);
        }

        try {
            // Verificar si ya existe un proyecto con ese nombre
            Project existing = store.findProject(projectName);
            if (existing != null) {
                OutputFormatter.error("Project '" + projectName + "' already exists");
                System.out.println("Use 'pm remove " + projectName + "' to remove it first");
                System.exit(1);
            }

            // Detectar tipo de proyecto
            ProjectType type;
            String typeFlag = parser.getFlag("type");

            if (typeFlag != null) {
                // Usuario especificó el tipo manualmente
                try {
                    type = ProjectType.valueOf(typeFlag.toUpperCase());
                } catch (IllegalArgumentException e) {
                    OutputFormatter.error("Invalid project type: " + typeFlag);
                    System.out.println("Valid types: GRADLE, MAVEN, NODEJS, DOTNET, PYTHON");
                    System.exit(1);
                    return;
                }
            } else {
                // Auto-detectar tipo
                OutputFormatter.info("Detecting project type...");
                type = detector.detect(projectPath);

                if (type == ProjectType.UNKNOWN) {
                    OutputFormatter.warning("Could not detect project type");
                    System.out.println("You can specify it manually with --type <type>");
                    System.out.println("Valid types: GRADLE, MAVEN, NODEJS, DOTNET, PYTHON");
                    System.out.println();
                    System.out.print("Continue with UNKNOWN type? (y/n): ");

                    // Leer respuesta del usuario
                    String response = System.console() != null
                            ? System.console().readLine()
                            : "n";

                    if (!response.toLowerCase().startsWith("y")) {
                        System.out.println("Aborted.");
                        System.exit(0);
                    }
                }
            }

            // Crear proyecto
            Project project = new Project(projectName, projectPath, type);

            // Configurar comandos por defecto según el tipo
            CommandConfigurator.configure(project);

            // Guardar proyecto
            store.saveProject(project);

            // Confirmar al usuario
            System.out.println();
            OutputFormatter.success("Project '" + projectName + "' registered successfully");
            System.out.println();
            System.out.println("  Name: " + projectName);
            System.out.println("  Type: " + type.displayName());
            System.out.println("  Path: " + projectPath);
            System.out.println("  Commands: " + project.commandCount() + " configured");
            System.out.println();
            System.out.println("Use 'pm commands " + projectName + "' to see available commands");

        } catch (IOException e) {
            OutputFormatter.error("Failed to save project: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // COMANDO: LIST (Listar proyectos)
    // ============================================================

    /**
     * Handler para el comando "list".
     * Lista todos los proyectos registrados.
     *
     * <p>Uso: pm list
     *
     * @param args argumentos del comando
     */
    private static void handleList(String[] args) {
        try {
            // Cargar todos los proyectos
            Map<String, Project> projects = store.load();

            // Usar OutputFormatter para mostrar
            OutputFormatter.printProjectList(projects);

        } catch (IOException e) {
            OutputFormatter.error("Failed to load projects: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // COMANDO: BUILD (Compilar proyecto)
    // ============================================================

    /**
     * Handler para el comando "build".
     * Compila el proyecto especificado.
     *
     * <p>Usage: {@code pm build NAME}
     *
     * <p>Proceso:
     * <ol>
     *   <li>Buscar proyecto por nombre</li>
     *   <li>Obtener comando "build"</li>
     *   <li>Ejecutar en el directorio del proyecto</li>
     *   <li>Mostrar output en tiempo real</li>
     *   <li>Reportar éxito/fallo</li>
     * </ol>
     *
     * @param args argumentos del comando
     */
    private static void handleBuild(String[] args) {
        ArgsParser parser = new ArgsParser(args);

        // Obtener nombre del proyecto
        String projectName = parser.getPositional(1);
        if (projectName == null || projectName.isBlank()) {
            OutputFormatter.error("Project name is required");
            System.out.println("Usage: pm build <name>");
            System.exit(1);
        }

        try {
            // Buscar proyecto
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.out.println("Use 'pm list' to see registered projects");
                System.exit(1);
            }

            // Obtener comando build
            String buildCommand = project.getCommand("build");
            if (buildCommand == null) {
                OutputFormatter.error("No 'build' command configured for this project");
                System.out.println("Use 'pm commands " + projectName + "' to see available commands");
                System.exit(1);
            }

            // Mostrar información
            System.out.println();
            OutputFormatter.info("Building " + projectName + "...");
            System.out.println("Command: " + buildCommand);
            System.out.println("Directory: " + project.path());
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            // Ejecutar comando
            CommandExecutor.ExecutionResult result = executor.execute(
                    buildCommand,
                    project.path(),
                    300  // timeout: 5 minutos
            );

            // Mostrar resultado
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
    // COMANDO: RUN (Ejecutar proyecto)
    // ============================================================

    /**
     * Handler para el comando "run".
     * Ejecuta el proyecto especificado.
     *
     * <p>Usage: {@code pm run NAME}
     *
     * @param args argumentos del comando
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

            System.out.println();
            OutputFormatter.info("Running " + projectName + "...");
            System.out.println("Command: " + runCommand);
            System.out.println("Directory: " + project.path());
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            // Ejecutar sin timeout (puede correr indefinidamente)
            CommandExecutor.ExecutionResult result = executor.execute(
                    runCommand,
                    project.path(),
                    0  // sin timeout
            );

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
    // COMANDO: TEST (Ejecutar tests)
    // ============================================================

    /**
     * Handler para el comando "test".
     * Ejecuta los tests del proyecto.
     *
     * <p>Usage: {@code pm test NAME}
     *
     * @param args argumentos del comando
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

            System.out.println();
            OutputFormatter.info("Running tests for " + projectName + "...");
            System.out.println("Command: " + testCommand);
            System.out.println();
            System.out.println("─".repeat(60));
            System.out.println();

            CommandExecutor.ExecutionResult result = executor.execute(
                    testCommand,
                    project.path(),
                    600  // timeout: 10 minutos para tests
            );

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
    // COMANDO: SCAN (Escanear comandos en código)
    // ============================================================

    /**
     * Handler para el comando "scan".
     * Escanea el código fuente buscando comandos.
     *
     * <p>Usage: {@code pm scan NAME}
     *
     * <p>TODO: Implementar scanner de anotaciones @Command
     *
     * @param args argumentos del comando
     */
    private static void handleScan(String[] args) {
        OutputFormatter.info("Scan command - Coming soon");
        System.out.println("This will scan for @Command annotations in your code");
        System.out.println("Useful for Minecraft mods and similar projects");
    }

    // ============================================================
    // COMANDO: COMMANDS (Listar comandos disponibles)
    // ============================================================

    /**
     * Handler para el comando "commands".
     * Lista los comandos disponibles para un proyecto.
     *
     * <p>Usage: {@code pm commands NAME}
     *
     * @param args argumentos del comando
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

            // Usar OutputFormatter para mostrar comandos
            OutputFormatter.printCommands(project);

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // COMANDO: REMOVE (Eliminar proyecto)
    // ============================================================

    /**
     * Handler para el comando "remove".
     * Elimina un proyecto del registro.
     *
     * <p>Usage: {@code pm remove NAME}
     *
     * @param args argumentos del comando
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
            // Verificar que existe
            Project project = store.findProject(projectName);
            if (project == null) {
                OutputFormatter.error("Project '" + projectName + "' not found");
                System.exit(1);
            }

            // Confirmar eliminación (a menos que se use --force)
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

            // Eliminar
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
    // COMANDO: INFO (Mostrar información del proyecto)
    // ============================================================

    /**
     * Handler para el comando "info".
     * Muestra información detallada de un proyecto.
     *
     * <p>Usage: {@code pm info NAME}
     *
     * @param args argumentos del comando
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

            // Mostrar información detallada
            OutputFormatter.section("Project Information");
            OutputFormatter.printProject(project);
            OutputFormatter.printCommands(project);

        } catch (IOException e) {
            OutputFormatter.error("Failed to load project: " + e.getMessage());
            System.exit(1);
        }
    }

    // ============================================================
    // OUTPUT Y AYUDA
    // ============================================================

    private static void printBanner() {
        System.out.println("""
            ╔════════════════════════════════╗
            ║  ProjectManager v%-12s ║
            ║  Manage your projects          ║
            ╚════════════════════════════════╝
            """.formatted(Constants.VERSION));
    }

    private static void printHelp() {
        System.out.println("""
            Usage: pm <command> [options]
            
            Commands:
              add <name> --path <path>    Register a new project
              list, ls                    List all projects
              build <name>                Build project
              run <name>                  Run project
              test <name>                 Run tests
              scan <name>                 Scan for commands in code
              commands, cmd <name>        List available commands
              remove, rm <name>           Remove project
              info <name>                 Show project details
              help                        Show this help
              version                     Show version
            
            Examples:
              pm add minecraft --path ~/projects/minecraft-client
              pm list
              pm build minecraft
              pm run minecraft
              pm commands minecraft
              pm info minecraft
            """);
    }

    private static void printVersion() {
        System.out.println("ProjectManager " + Constants.VERSION);
        System.out.println("Java " + System.getProperty("java.version"));
    }
}