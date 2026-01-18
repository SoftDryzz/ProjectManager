package pm.core;

import pm.detector.ProjectType;

import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Representa un proyecto registrado en ProjectManager.
 *
 * <p>Un proyecto contiene:
 * <ul>
 *   <li>Nombre único identificador</li>
 *   <li>Ruta en el sistema de archivos</li>
 *   <li>Tipo de proyecto (Gradle, Maven, etc)</li>
 *   <li>Mapa de comandos disponibles (build, run, test, etc)</li>
 *   <li>Timestamp de última modificación</li>
 * </ul>
 *
 * <p>La clase es inmutable en sus campos core (name, path, type).
 * Los comandos pueden ser agregados/modificados dinámicamente.
 *
 * <p>Ejemplo de uso:
 * <pre>{@code
 * Path projectPath = Paths.get("/home/user/myapp");
 * Project project = new Project("myapp", projectPath, ProjectType.GRADLE);
 * project.addCommand("build", "gradle build");
 * project.addCommand("run", "gradle run");
 *
 * String buildCmd = project.getCommand("build"); // "gradle build"
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Project {

    /**
     * Nombre único del proyecto.
     * Usado como identificador para ejecutar comandos (ej: pm build myapp).
     * Inmutable después de la creación.
     */
    private final String name;

    /**
     * Ruta absoluta del proyecto en el sistema de archivos.
     * Apunta al directorio raíz del proyecto.
     * Inmutable después de la creación.
     */
    private final Path path;

    /**
     * Tipo de proyecto (Gradle, Maven, Node.js, etc).
     * Determina qué comandos por defecto se usan.
     * Inmutable después de la creación.
     */
    private final ProjectType type;

    /**
     * Mapa de comandos disponibles para este proyecto.
     * Key: nombre del comando (ej: "build", "run", "test")
     * Value: comando shell a ejecutar (ej: "gradle build")
     *
     * Puede ser modificado con addCommand() y removeCommand().
     */
    private final Map<String, String> commands;

    /**
     * Timestamp de la última modificación del proyecto.
     * Se actualiza automáticamente cuando se agregan/quitan comandos.
     */
    private Instant lastModified;

    /**
     * Crea un nuevo proyecto.
     *
     * @param name nombre único del proyecto (no puede ser null)
     * @param path ruta absoluta del proyecto (no puede ser null)
     * @param type tipo de proyecto (no puede ser null)
     * @throws NullPointerException si algún parámetro es null
     */
    public Project(String name, Path path, ProjectType type) {
        this.name = Objects.requireNonNull(name, "Project name cannot be null");
        this.path = Objects.requireNonNull(path, "Project path cannot be null");
        this.type = Objects.requireNonNull(type, "Project type cannot be null");
        this.commands = new HashMap<>();
        this.lastModified = Instant.now();
    }

    // ============================================================
    // GETTERS (estilo moderno sin prefijo 'get')
    // ============================================================

    /**
     * Obtiene el nombre del proyecto.
     *
     * @return nombre del proyecto
     */
    public String name() {
        return name;
    }

    /**
     * Obtiene la ruta del proyecto.
     *
     * @return ruta absoluta del directorio del proyecto
     */
    public Path path() {
        return path;
    }

    /**
     * Obtiene el tipo de proyecto.
     *
     * @return tipo de proyecto (GRADLE, MAVEN, etc)
     */
    public ProjectType type() {
        return type;
    }

    /**
     * Obtiene una copia inmutable del mapa de comandos.
     *
     * Los cambios en el Map retornado NO afectan el proyecto.
     * Usar addCommand() para agregar comandos.
     *
     * @return copia inmutable de los comandos disponibles
     */
    public Map<String, String> commands() {
        return Map.copyOf(commands);
    }

    /**
     * Obtiene el timestamp de última modificación.
     *
     * @return instante de la última modificación
     */
    public Instant lastModified() {
        return lastModified;
    }

    // ============================================================
    // GESTIÓN DE COMANDOS
    // ============================================================

    /**
     * Agrega un comando al proyecto.
     *
     * Si ya existe un comando con el mismo nombre, se sobrescribe.
     * Actualiza automáticamente el timestamp de modificación.
     *
     * @param commandName nombre del comando (ej: "build", "run")
     * @param commandLine línea de comando a ejecutar (ej: "gradle build")
     * @throws NullPointerException si algún parámetro es null
     */
    public void addCommand(String commandName, String commandLine) {
        Objects.requireNonNull(commandName, "Command name cannot be null");
        Objects.requireNonNull(commandLine, "Command line cannot be null");

        commands.put(commandName, commandLine);
        lastModified = Instant.now();
    }

    /**
     * Obtiene el comando asociado a un nombre.
     *
     * @param commandName nombre del comando a buscar
     * @return línea de comando, o null si no existe
     */
    public String getCommand(String commandName) {
        return commands.get(commandName);
    }

    /**
     * Verifica si existe un comando con el nombre dado.
     *
     * @param commandName nombre del comando a verificar
     * @return true si el comando existe, false en caso contrario
     */
    public boolean hasCommand(String commandName) {
        return commands.containsKey(commandName);
    }

    /**
     * Elimina un comando del proyecto.
     *
     * Si el comando no existe, no hace nada.
     * Actualiza el timestamp de modificación.
     *
     * @param commandName nombre del comando a eliminar
     */
    public void removeCommand(String commandName) {
        commands.remove(commandName);
        lastModified = Instant.now();
    }

    /**
     * Obtiene la cantidad de comandos registrados.
     *
     * @return número de comandos disponibles
     */
    public int commandCount() {
        return commands.size();
    }

    // ============================================================
    // MÉTODOS OBJECT
    // ============================================================

    /**
     * Representación en string del proyecto.
     *
     * Formato: Project{name='...', type=..., path=...}
     *
     * @return representación textual del proyecto
     */
    @Override
    public String toString() {
        return "Project{name='%s', type=%s, path=%s, commands=%d}"
                .formatted(name, type.displayName(), path, commands.size());
    }

    /**
     * Compara este proyecto con otro objeto.
     *
     * Dos proyectos son iguales si tienen el mismo nombre y path.
     * El tipo y comandos no afectan la igualdad.
     *
     * @param obj objeto a comparar
     * @return true si son iguales, false en caso contrario
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Project other)) return false;
        return Objects.equals(name, other.name) &&
                Objects.equals(path, other.path);
    }

    /**
     * Genera el código hash del proyecto.
     *
     * Basado en name y path (consistente con equals).
     *
     * @return código hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }
}