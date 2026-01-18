package pm.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Parser de argumentos de línea de comandos.
 *
 * <p>Parsea argumentos en formato:
 * <pre>
 * pm add myproject --path /home/user/project --type gradle
 * </pre>
 *
 * <p>Extrae:
 * <ul>
 *   <li>Argumentos posicionales (sin --): ["add", "myproject"]</li>
 *   <li>Flags con valor (--path /home/...): {path: "/home/..."}</li>
 *   <li>Flags booleanos (--force): {force: "true"}</li>
 * </ul>
 *
 * <p>Ejemplo de uso:
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
     * Argumentos posicionales (sin --).
     * Índice 0 = comando, índice 1 = primer argumento, etc.
     */
    private final String[] positional;

    /**
     * Flags con sus valores.
     * Key: nombre del flag (sin --)
     * Value: valor del flag (o "true" si es booleano)
     */
    private final Map<String, String> flags;

    /**
     * Constructor.
     * Parsea los argumentos y los separa en posicionales y flags.
     *
     * @param args argumentos de línea de comandos
     */
    public ArgsParser(String[] args) {
        this.flags = new HashMap<>();

        // Contar cuántos argumentos posicionales hay
        int positionalCount = 0;
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                positionalCount++;
            } else {
                break; // Los posicionales siempre van primero
            }
        }

        // Extraer argumentos posicionales
        this.positional = new String[positionalCount];
        System.arraycopy(args, 0, positional, 0, positionalCount);

        // Parsear flags (--key value o --flag)
        for (int i = positionalCount; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                // Remover el prefijo "--"
                String key = arg.substring(2);

                // Verificar si tiene valor (siguiente argumento no empieza con --)
                if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    // Flag con valor: --path /home/user
                    String value = args[i + 1];
                    flags.put(key, value);
                    i++; // Saltar el valor
                } else {
                    // Flag booleano: --force
                    flags.put(key, "true");
                }
            }
        }
    }

    /**
     * Obtiene un argumento posicional por índice.
     *
     * @param index índice (0 = primer posicional)
     * @return argumento o null si no existe
     */
    public String getPositional(int index) {
        if (index >= 0 && index < positional.length) {
            return positional[index];
        }
        return null;
    }

    /**
     * Obtiene el valor de un flag.
     *
     * @param key nombre del flag (sin --)
     * @return valor del flag o null si no existe
     */
    public String getFlag(String key) {
        return flags.get(key);
    }

    /**
     * Obtiene el valor de un flag con valor por defecto.
     *
     * @param key nombre del flag
     * @param defaultValue valor por defecto si no existe
     * @return valor del flag o defaultValue
     */
    public String getFlag(String key, String defaultValue) {
        return flags.getOrDefault(key, defaultValue);
    }

    /**
     * Verifica si existe un flag.
     *
     * @param key nombre del flag
     * @return true si el flag existe
     */
    public boolean hasFlag(String key) {
        return flags.containsKey(key);
    }

    /**
     * Obtiene un flag como boolean.
     *
     * @param key nombre del flag
     * @return true si el flag existe y no es "false", false en caso contrario
     */
    public boolean getBooleanFlag(String key) {
        String value = flags.get(key);
        return value != null && !value.equalsIgnoreCase("false");
    }

    /**
     * Obtiene la cantidad de argumentos posicionales.
     *
     * @return cantidad de posicionales
     */
    public int positionalCount() {
        return positional.length;
    }
}