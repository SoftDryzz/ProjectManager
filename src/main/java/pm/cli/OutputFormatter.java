package pm.cli;

import pm.core.Project;
import pm.util.GitIntegration;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Formateador de salida con colores ANSI para la interfaz CLI.
 *
 * <p>Proporciona métodos para mostrar mensajes formateados con colores
 * y estilos consistentes en toda la aplicación.
 *
 * <p>Colores disponibles:
 * <ul>
 *   <li>Verde: mensajes de éxito</li>
 *   <li>Rojo: mensajes de error</li>
 *   <li>Amarillo: advertencias</li>
 *   <li>Azul: información</li>
 *   <li>Cyan: datos destacados (rutas, valores)</li>
 *   <li>Gris: información secundaria</li>
 * </ul>
 *
 * <p>Ejemplo de uso:
 * <pre>{@code
 * OutputFormatter.success("Proyecto creado exitosamente");
 * OutputFormatter.error("No se pudo encontrar el archivo");
 * OutputFormatter.info("Compilando proyecto...");
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class OutputFormatter {

    // Códigos de color ANSI
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String GRAY = "\u001B[90m";
    public static final String BOLD = "\u001B[1m";
    public static final String RESET = "\u001B[0m";

    /**
     * Muestra un mensaje de éxito (verde, con ✓).
     *
     * @param message mensaje a mostrar
     */
    public static void success(String message) {
        System.out.println(GREEN + "✅ " + message + RESET);
    }

    /**
     * Muestra un mensaje de error (rojo, con ✗).
     *
     * @param message mensaje a mostrar
     */
    public static void error(String message) {
        System.out.println(RED + "❌ " + message + RESET);
    }

    /**
     * Muestra una advertencia (amarillo, con ⚠).
     *
     * @param message mensaje a mostrar
     */
    public static void warning(String message) {
        System.out.println(YELLOW + "⚠️  " + message + RESET);
    }

    /**
     * Muestra un mensaje informativo (azul, con ℹ).
     *
     * @param message mensaje a mostrar
     */
    public static void info(String message) {
        System.out.println(BLUE + "ℹ️  " + message + RESET);
    }

    /**
     * Muestra un encabezado de sección (negrita, cyan, con subrayado).
     *
     * @param title título de la sección
     */
    public static void section(String title) {
        System.out.println();
        System.out.println(BOLD + CYAN + title + RESET);
        System.out.println("─".repeat(title.length()));
        System.out.println();
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

        // Mostrar variables de entorno
        if (project.envVarCount() > 0) {
            System.out.println("  Environment Variables: " + project.envVarCount());
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
     * Muestra una lista de proyectos en formato tabla.
     *
     * @param projects mapa de proyectos (nombre → proyecto)
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
     * Muestra los comandos disponibles de un proyecto.
     *
     * @param project proyecto del cual mostrar comandos
     */
    public static void printCommands(Project project) {
        if (project.commandCount() == 0) {
            System.out.println(GRAY + "No commands configured for this project." + RESET);
            return;
        }

        System.out.println();
        System.out.println("Available Commands for " + BOLD + project.name() + RESET);
        System.out.println("─".repeat(Math.max(40, "Available Commands for ".length() + project.name().length())));
        System.out.println();

        // Calcular padding para alinear las flechas
        int maxCommandLength = project.commands().keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        // Mostrar cada comando
        project.commands().forEach((name, command) -> {
            String padding = " ".repeat(maxCommandLength - name.length());
            System.out.println("  " + GREEN + name + RESET + padding + " → " + CYAN + command + RESET);
        });

        // Mostrar variables de entorno si existen
        printEnvVars(project);
    }

    /**
     * Muestra las variables de entorno de un proyecto.
     *
     * @param project proyecto del cual mostrar variables
     */
    public static void printEnvVars(Project project) {
        if (project.envVarCount() == 0) {
            return;
        }

        System.out.println();
        System.out.println(BOLD + "Environment Variables" + RESET);
        System.out.println("─".repeat(Math.max(40, "Environment Variables".length())));
        System.out.println();

        // Calcular padding para alinear
        int maxKeyLength = project.envVars().keySet().stream()
                .mapToInt(String::length)
                .max()
                .orElse(0);

        // Mostrar cada variable
        project.envVars().forEach((key, value) -> {
            String padding = " ".repeat(maxKeyLength - key.length());
            System.out.println("  " + GREEN + key + RESET + padding + " = " + CYAN + value + RESET);
        });
    }

    /**
     * Formatea una duración en un string legible.
     *
     * <p>Ejemplos:
     * <ul>
     *   <li>45 segundos → "45 seconds"</li>
     *   <li>2 minutos → "2 minutes"</li>
     *   <li>1 hora → "1 hour"</li>
     *   <li>3 días → "3 days"</li>
     * </ul>
     *
     * @param duration duración a formatear
     * @return string formateado
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