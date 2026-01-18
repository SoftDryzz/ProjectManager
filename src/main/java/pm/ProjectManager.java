package pm;

import pm.util.Constants;

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
 * <pre>
 * pm add <nombre> --path <ruta>    Registrar nuevo proyecto
 * pm list                          Listar todos los proyectos
 * pm build <nombre>                Compilar proyecto
 * pm run <nombre>                  Ejecutar proyecto
 * pm scan <nombre>                 Escanear comandos en código
 * pm help                          Mostrar ayuda
 * </pre>
 *
 * <p><b>Ejemplo de uso típico:</b>
 * <pre>{@code
 * // 1. Registrar un proyecto
 * $ pm add minecraft --path ~/projects/minecraft-client
 *
 * // 2. Compilar
 * $ pm build minecraft
 *
 * // 3. Escanear comandos
 * $ pm scan minecraft
 * Found 5 commands:
 *   .fly     FlyCommand.java:12
 *   .speed   SpeedCommand.java:8
 *   ...
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProjectManager {

    /**
     * Punto de entrada de la aplicación.
     *
     * <p>Flujo de ejecución:
     * <ol>
     *   <li>Muestra banner de bienvenida</li>
     *   <li>Valida que hay argumentos</li>
     *   <li>Parsea el comando (primer argumento)</li>
     *   <li>Ejecuta el handler correspondiente</li>
     * </ol>
     *
     * @param args argumentos de línea de comandos
     *             args[0] = comando (add, list, build, etc)
     *             args[1..n] = argumentos del comando
     */
    public static void main(String[] args) {
        // Mostrar banner de bienvenida
        printBanner();

        // Validar que se proporcionó al menos un comando
        if (args.length == 0) {
            printHelp();
            return;
        }

        // Extraer comando y convertir a minúsculas para case-insensitive
        String command = args[0].toLowerCase();

        // Switch expression (Java 14+)
        // Más limpio que switch tradicional con breaks
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
                // Comando no reconocido
                System.err.println("❌ Unknown command: " + command);
                System.err.println("Run 'pm help' for usage information");
                System.exit(1);
            }
        }
    }

    // ============================================================
    // HANDLERS DE COMANDOS (implementar en próximos pasos)
    // ============================================================

    /**
     * Handler para el comando "add".
     * Registra un nuevo proyecto en ProjectManager.
     *
     * <p>Uso: pm add <nombre> --path <ruta> [--type <tipo>]
     *
     * <p>TODO: Implementar
     * - Validar argumentos
     * - Detectar tipo de proyecto
     * - Crear objeto Project
     * - Guardar en projects.json
     *
     * @param args argumentos del comando
     */
    private static void handleAdd(String[] args) {
        System.out.println("🔨 Add command - Coming soon");
        System.out.println("   Will register a new project");
    }

    /**
     * Handler para el comando "list".
     * Lista todos los proyectos registrados.
     *
     * <p>Uso: pm list
     *
     * <p>TODO: Implementar
     * - Leer projects.json
     * - Formatear y mostrar lista de proyectos
     * - Mostrar info básica (nombre, tipo, path)
     *
     * @param args argumentos del comando
     */
    private static void handleList(String[] args) {
        System.out.println("📋 List command - Coming soon");
        System.out.println("   Will show all registered projects");
    }

    /**
     * Handler para el comando "build".
     * Compila el proyecto especificado.
     *
     * <p>Uso: pm build <nombre>
     *
     * <p>TODO: Implementar
     * - Buscar proyecto por nombre
     * - Obtener comando "build"
     * - Ejecutar en el directorio del proyecto
     * - Mostrar output en tiempo real
     *
     * @param args argumentos del comando
     */
    private static void handleBuild(String[] args) {
        System.out.println("🏗️  Build command - Coming soon");
        System.out.println("   Will build the specified project");
    }

    /**
     * Handler para el comando "run".
     * Ejecuta el proyecto especificado.
     *
     * <p>Uso: pm run <nombre>
     *
     * <p>TODO: Implementar
     * - Buscar proyecto
     * - Ejecutar comando "run"
     * - Mantener proceso activo
     * - Capturar Ctrl+C para terminar limpiamente
     *
     * @param args argumentos del comando
     */
    private static void handleRun(String[] args) {
        System.out.println("▶️  Run command - Coming soon");
        System.out.println("   Will run the specified project");
    }

    /**
     * Handler para el comando "test".
     * Ejecuta los tests del proyecto.
     *
     * <p>Uso: pm test <nombre>
     *
     * <p>TODO: Implementar
     * - Ejecutar comando "test"
     * - Parsear resultados (passed/failed)
     * - Mostrar resumen de tests
     *
     * @param args argumentos del comando
     */
    private static void handleTest(String[] args) {
        System.out.println("🧪 Test command - Coming soon");
        System.out.println("   Will run tests for the project");
    }

    /**
     * Handler para el comando "scan".
     * Escanea el código fuente buscando comandos.
     *
     * <p>Uso: pm scan <nombre>
     *
     * <p>Busca anotaciones @Command en archivos Java.
     * Útil para proyectos como mods de Minecraft.
     *
     * <p>TODO: Implementar
     * - Leer archivos .java del proyecto
     * - Buscar anotaciones @Command
     * - Parsear nombre, descripción, archivo, línea
     * - Mostrar lista formateada
     * - Cachear resultados
     *
     * @param args argumentos del comando
     */
    private static void handleScan(String[] args) {
        System.out.println("🔍 Scan command - Coming soon");
        System.out.println("   Will scan for @Command annotations");
    }

    /**
     * Handler para el comando "commands".
     * Lista los comandos disponibles para un proyecto.
     *
     * <p>Uso: pm commands <nombre>
     *
     * <p>TODO: Implementar
     * - Buscar proyecto
     * - Listar comandos del Map<String, String>
     * - Mostrar formato: nombre → comando shell
     *
     * @param args argumentos del comando
     */
    private static void handleCommands(String[] args) {
        System.out.println("📜 Commands command - Coming soon");
        System.out.println("   Will list available commands");
    }

    /**
     * Handler para el comando "remove".
     * Elimina un proyecto del registro.
     *
     * <p>Uso: pm remove <nombre>
     *
     * <p>TODO: Implementar
     * - Buscar proyecto
     * - Confirmar eliminación (opcional: flag --force)
     * - Eliminar de projects.json
     * - Limpiar cache
     *
     * @param args argumentos del comando
     */
    private static void handleRemove(String[] args) {
        System.out.println("🗑️  Remove command - Coming soon");
        System.out.println("   Will remove project from registry");
    }

    /**
     * Handler para el comando "info".
     * Muestra información detallada de un proyecto.
     *
     * <p>Uso: pm info <nombre>
     *
     * <p>TODO: Implementar
     * - Buscar proyecto
     * - Mostrar todos los campos
     * - Listar comandos disponibles
     * - Mostrar última modificación
     *
     * @param args argumentos del comando
     */
    private static void handleInfo(String[] args) {
        System.out.println("ℹ️  Info command - Coming soon");
        System.out.println("   Will show detailed project info");
    }

    // ============================================================
    // OUTPUT Y AYUDA
    // ============================================================

    /**
     * Muestra el banner de bienvenida.
     *
     * Usa text blocks (Java 15+) para strings multilínea.
     * El formato está diseñado para terminal de 80 columnas.
     */
    private static void printBanner() {
        // Text block: strings multilínea sin concatenación
        // .formatted() inserta variables (similar a String.format)
        System.out.println("""
            ╔════════════════════════════════╗
            ║  ProjectManager v%-12s ║
            ║  Manage your projects          ║
            ╚════════════════════════════════╝
            """.formatted(Constants.VERSION));
    }

    /**
     * Muestra el mensaje de ayuda con todos los comandos disponibles.
     *
     * Incluye:
     * - Sintaxis de cada comando
     * - Descripción breve
     * - Ejemplos de uso
     */
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
              pm scan minecraft
              pm commands minecraft
            """);
    }

    /**
     * Muestra la versión de ProjectManager y Java.
     *
     * Útil para debugging y reportar issues.
     */
    private static void printVersion() {
        System.out.println("ProjectManager " + Constants.VERSION);
        System.out.println("Java " + System.getProperty("java.version"));
    }
}