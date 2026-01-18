package pm.core;

/**
 * Información de un comando encontrado en el código fuente.
 *
 * <p>Se usa para representar comandos descubiertos al escanear código,
 * por ejemplo anotaciones @Command en mods de Minecraft.
 *
 * <p>Es un Record (Java 14+) lo que proporciona:
 * <ul>
 *   <li>Inmutabilidad automática (no se puede modificar después de crear)</li>
 *   <li>equals(), hashCode(), toString() generados automáticamente</li>
 *   <li>Getters automáticos (sin prefijo 'get')</li>
 * </ul>
 *
 * <p>Ejemplo de uso:
 * <pre>{@code
 * CommandInfo cmd = new CommandInfo(
 *     "fly",                      // nombre del comando
 *     "FlyCommand.java",          // archivo donde está
 *     12,                         // línea del archivo
 *     "Toggle flight mode"        // descripción
 * );
 *
 * System.out.println(cmd.fullCommand()); // ".fly"
 * System.out.println(cmd.display());     // "  .fly            FlyCommand.java:12"
 * }</pre>
 *
 * @param name nombre del comando (ej: "fly", "speed")
 * @param file nombre del archivo donde se encontró (ej: "FlyCommand.java")
 * @param line número de línea en el archivo (1-indexed)
 * @param description descripción del comando (puede ser null)
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
     * Constructor compacto con validación.
     *
     * Los records permiten este constructor especial que valida
     * los parámetros antes de asignarlos.
     *
     * @throws IllegalArgumentException si name o file están vacíos, o line es negativo
     */
    public CommandInfo {
        // Validación de 'name'
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Command name cannot be null or blank");
        }

        // Validación de 'file'
        if (file == null || file.isBlank()) {
            throw new IllegalArgumentException("File cannot be null or blank");
        }

        // Validación de 'line'
        if (line < 1) {
            throw new IllegalArgumentException("Line must be positive (1-indexed)");
        }

        // 'description' puede ser null (comando sin descripción)
    }

    /**
     * Obtiene el comando completo con prefijo.
     *
     * En muchos clientes de Minecraft, los comandos empiezan con punto.
     *
     * @return comando con prefijo "." (ej: ".fly")
     */
    public String fullCommand() {
        return "." + name;
    }

    /**
     * Formato compacto para mostrar en consola.
     *
     * Formato: "  .comando        Archivo.java:123"
     * El comando se alinea a 15 caracteres para columnas ordenadas.
     *
     * @return string formateado para display
     */
    public String display() {
        return "  %-15s  %s:%d".formatted(fullCommand(), file, line);
    }

    /**
     * Formato completo con descripción.
     *
     * Incluye el comando, archivo, línea y descripción en múltiples líneas.
     * Si no hay descripción, muestra "No description".
     *
     * @return string formateado con toda la información
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
     * Verifica si el comando tiene descripción.
     *
     * @return true si tiene descripción no vacía, false en caso contrario
     */
    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }
}