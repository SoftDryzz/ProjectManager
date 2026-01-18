package pm.cli;

import pm.core.Project;
import pm.util.GitIntegration;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Formatea output para la consola con colores y estructura.
 *
 * <p>Proporciona métodos para:
 * <ul>
 *   <li>Mensajes de éxito/error con emojis</li>
 *   <li>Tablas formateadas</li>
 *   <li>Colores ANSI para terminal</li>
 *   <li>Formato consistente en toda la aplicación</li>
 * </ul>
 *
 * <p>Ejemplo de uso:
 * <pre>{@code
 * OutputFormatter.success("Project added successfully");
 * OutputFormatter.error("Project not found");
 * OutputFormatter.info("Building project...");
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class OutputFormatter {

    // Códigos ANSI para colores
    // Solo se usan en terminals que los soportan (la mayoría modernos)
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String GRAY = "\u001B[90m";
    private static final String BOLD = "\u001B[1m";

    /**
     * Constructor privado - clase utility.
     */
    private OutputFormatter() {
        throw new AssertionError("OutputFormatter cannot be instantiated");
    }

    /**
     * Muestra mensaje de éxito.
     *
     * @param message mensaje a mostrar
     */
    public static void success(String message) {
        System.out.println(GREEN + "✅ " + message + RESET);
    }

    /**
     * Muestra mensaje de error.
     *
     * @param message mensaje a mostrar
     */
    public static void error(String message) {
        System.err.println(RED + "❌ " + message + RESET);
    }

    /**
     * Muestra mensaje de advertencia.
     *
     * @param message mensaje a mostrar
     */
    public static void warning(String message) {
        System.out.println(YELLOW + "⚠️  " + message + RESET);
    }

    /**
     * Muestra mensaje informativo.
     *
     * @param message mensaje a mostrar
     */
    public static void info(String message) {
        System.out.println(BLUE + "ℹ️  " + message + RESET);
    }

    /**
     * Muestra título de sección.
     *
     * @param title título a mostrar
     */
    public static void section(String title) {
        System.out.println();
        System.out.println(BOLD + CYAN + title + RESET);
        System.out.println(CYAN + "─".repeat(title.length()) + RESET);
    }

    /**
     * Muestra información de un proyecto en formato legible.
     *
     * @param project proyecto a mostrar
     */
    public static void printProject(Project project) {
        System.out.println(BOLD + project.name() + RESET + " " +
                GRAY + "(" + project.type().displayName() + ")" + RESET);
        System.out.println("  Path: " + CYAN + project.path() + RESET);

        // Mostrar última modificación
        Instant lastMod = project.lastModified();
        Duration ago = Duration.between(lastMod, Instant.now());
        String timeAgo = formatDuration(ago);
        System.out.println("  Modified: " + GRAY + timeAgo + " ago" + RESET);

        // Mostrar comandos
        if (project.commandCount() > 0) {
            System.out.println("  Commands: " + project.commandCount());
        }

        // Mostrar información de Git (si es un repo)
        printGitInfo(project.path());

        System.out.println();
    }

    /**
     * Muestra información de Git si el proyecto es un repositorio.
     *
     * @param projectPath ruta del proyecto
     */
    private static void printGitInfo(Path projectPath) {
        // Verificar si es un repositorio Git
        if (!GitIntegration.isGitRepository(projectPath)) {
            return;
        }

        System.out.println();
        System.out.println("  " + BOLD + "Git:" + RESET);

        // Branch actual
        String branch = GitIntegration.getCurrentBranch(projectPath);
        if (branch != null) {
            System.out.println("    Branch: " + GREEN + branch + RESET);
        }

        // Estado (archivos modificados, etc)
        GitIntegration.GitStatus status = GitIntegration.getStatus(projectPath);
        if (status != null) {
            if (status.isClean()) {
                System.out.println("    Status: " + GREEN + "✓ Clean working tree" + RESET);
            } else {
                System.out.println("    Status: " + YELLOW + status.toString() + RESET);
            }
        }

        // Commits pendientes de push
        int commitsAhead = GitIntegration.getCommitsAhead(projectPath);
        if (commitsAhead > 0) {
            System.out.println("    Unpushed: " + YELLOW + commitsAhead + " commit" +
                    (commitsAhead > 1 ? "s" : "") + RESET);
        } else if (commitsAhead == 0) {
            System.out.println("    Unpushed: " + GREEN + "✓ Up to date" + RESET);
        }
    }

    /**
     * Muestra lista de proyectos en formato tabla.
     *
     * @param projects mapa de proyectos (key: nombre, value: Project)
     */
    public static void printProjectList(Map<String, Project> projects) {
        if (projects.isEmpty()) {
            info("No projects registered yet");
            System.out.println("Use 'pm add <name> --path <path>' to register a project");
            return;
        }

        section("Registered Projects (" + projects.size() + ")");

        for (Project project : projects.values()) {
            printProject(project);
        }
    }

    /**
     * Muestra lista de comandos de un proyecto.
     *
     * @param project proyecto
     */
    public static void printCommands(Project project) {
        Map<String, String> commands = project.commands();

        if (commands.isEmpty()) {
            warning("No commands configured for this project");
            return;
        }

        section("Available Commands for " + project.name());

        // Calcular ancho máximo del nombre de comando para alineación
        int maxNameLength = commands.keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(10);

        for (Map.Entry<String, String> entry : commands.entrySet()) {
            String name = entry.getKey();
            String command = entry.getValue();

            // Formatear con padding para alinear
            System.out.printf("  %-" + maxNameLength + "s  →  %s%s%s%n",
                    BOLD + name + RESET,
                    GRAY,
                    command,
                    RESET);
        }
    }

    /**
     * Formatea una duración de forma legible.
     *
     * @param duration duración
     * @return string formateado (ej: "2 hours", "30 minutes", "5 seconds")
     */
    private static String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else {
            long days = seconds / 86400;
            return days + " day" + (days != 1 ? "s" : "");
        }
    }
}
