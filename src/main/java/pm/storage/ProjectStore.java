package pm.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pm.core.Project;
import pm.detector.ProjectType;
import pm.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestor de persistencia de proyectos.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Guardar proyectos en JSON (~/.projectmanager/projects.json)</li>
 *   <li>Cargar proyectos desde JSON</li>
 *   <li>Crear directorio de configuración si no existe</li>
 *   <li>Serializar/deserializar objetos Project</li>
 * </ul>
 *
 * <p>Formato del archivo JSON:
 * <pre>
 * {
 *   "minecraft-client": {
 *     "name": "minecraft-client",
 *     "path": "/home/user/projects/minecraft-client",
 *     "type": "GRADLE",
 *     "commands": {
 *       "build": "gradle build",
 *       "run": "gradle runClient"
 *     },
 *     "lastModified": "2025-01-18T10:30:00Z"
 *   },
 *   "webapp": {
 *     ...
 *   }
 * }
 * </pre>
 *
 * <p>Thread-safety: Esta clase NO es thread-safe.
 * Si múltiples procesos modifican el archivo simultáneamente, puede corromperse.
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProjectStore {

    /**
     * Instancia de Gson para serialización/deserialización JSON.
     * Configurado con pretty printing para legibilidad.
     */
    private final Gson gson;

    /**
     * Constructor.
     * Inicializa Gson con configuración personalizada.
     */
    public ProjectStore() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()  // JSON formateado con indentación
                .create();
    }

    /**
     * Guarda todos los proyectos en el archivo JSON.
     *
     * <p>Proceso:
     * <ol>
     *   <li>Crea directorio de configuración si no existe</li>
     *   <li>Serializa el Map de proyectos a JSON</li>
     *   <li>Escribe al archivo projects.json</li>
     * </ol>
     *
     * <p>Si el archivo ya existe, se sobrescribe completamente.
     *
     * @param projects mapa de proyectos (key: nombre, value: Project)
     * @throws IOException si falla la escritura del archivo
     */
    public void save(Map<String, Project> projects) throws IOException {
        // Crear directorio de configuración si no existe
        ensureConfigDirExists();

        // Convertir Map a JSON con pretty printing
        String json = gson.toJson(projects);

        // Escribir al archivo
        Files.writeString(Constants.PROJECTS_FILE, json);
    }

    /**
     * Carga todos los proyectos desde el archivo JSON.
     *
     * <p>Si el archivo no existe, retorna un Map vacío.
     * <p>Si el archivo está corrupto, lanza IOException.
     *
     * @return mapa de proyectos cargados (puede estar vacío)
     * @throws IOException si falla la lectura o el JSON es inválido
     */
    public Map<String, Project> load() throws IOException {
        // Si el archivo no existe, retornar Map vacío
        if (!Files.exists(Constants.PROJECTS_FILE)) {
            return new HashMap<>();
        }

        // Leer contenido del archivo
        String json = Files.readString(Constants.PROJECTS_FILE);

        // Deserializar JSON a Map
        // TypeToken es necesario porque Gson necesita saber el tipo genérico
        TypeToken<Map<String, ProjectData>> typeToken =
                new TypeToken<Map<String, ProjectData>>() {};

        Map<String, ProjectData> dataMap = gson.fromJson(json, typeToken.getType());

        // Convertir ProjectData a Project
        // ProjectData es una clase interna para deserialización
        return convertToProjects(dataMap);
    }

    /**
     * Agrega o actualiza un proyecto específico.
     *
     * <p>Proceso:
     * <ol>
     *   <li>Carga todos los proyectos existentes</li>
     *   <li>Agrega/actualiza el proyecto en el Map</li>
     *   <li>Guarda de nuevo todos los proyectos</li>
     * </ol>
     *
     * @param project proyecto a guardar
     * @throws IOException si falla la operación
     */
    public void saveProject(Project project) throws IOException {
        Map<String, Project> projects = load();
        projects.put(project.name(), project);
        save(projects);
    }

    /**
     * Elimina un proyecto del almacenamiento.
     *
     * @param projectName nombre del proyecto a eliminar
     * @return true si se eliminó, false si no existía
     * @throws IOException si falla la operación
     */
    public boolean removeProject(String projectName) throws IOException {
        Map<String, Project> projects = load();
        boolean removed = projects.remove(projectName) != null;

        if (removed) {
            save(projects);
        }

        return removed;
    }

    /**
     * Busca un proyecto por nombre.
     *
     * @param projectName nombre del proyecto
     * @return el proyecto si existe, null en caso contrario
     * @throws IOException si falla la lectura
     */
    public Project findProject(String projectName) throws IOException {
        Map<String, Project> projects = load();
        return projects.get(projectName);
    }

    /**
     * Crea el directorio de configuración si no existe.
     *
     * @throws IOException si falla la creación del directorio
     */
    private void ensureConfigDirExists() throws IOException {
        if (!Files.exists(Constants.CONFIG_DIR)) {
            Files.createDirectories(Constants.CONFIG_DIR);
        }
    }

    /**
     * Convierte el Map de ProjectData a Map de Project.
     *
     * ProjectData es una clase auxiliar para deserialización desde JSON.
     *
     * @param dataMap mapa de ProjectData deserializado
     * @return mapa de objetos Project
     */
    private Map<String, Project> convertToProjects(Map<String, ProjectData> dataMap) {
        Map<String, Project> projects = new HashMap<>();

        if (dataMap == null) {
            return projects;
        }

        for (Map.Entry<String, ProjectData> entry : dataMap.entrySet()) {
            ProjectData data = entry.getValue();

            // Crear objeto Project desde ProjectData
            Project project = new Project(
                    data.name,
                    Path.of(data.path),
                    ProjectType.valueOf(data.type)
            );

            // Agregar comandos
            if (data.commands != null) {
                data.commands.forEach(project::addCommand);
            }

            projects.put(entry.getKey(), project);
        }

        return projects;
    }

    /**
     * Clase interna para deserialización de JSON.
     *
     * Gson no puede deserializar directamente a Project porque:
     * - Project tiene campos final
     * - Path no se serializa directamente
     *
     * Esta clase auxiliar tiene campos simples que Gson puede manejar.
     */
    private static class ProjectData {
        String name;
        String path;  // String en vez de Path
        String type;  // String en vez de ProjectType
        Map<String, String> commands;
        String lastModified;  // String en vez de Instant
    }
}