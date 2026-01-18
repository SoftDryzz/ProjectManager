package pm.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pm.core.Project;
import pm.detector.ProjectType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static pm.util.Constants.CONFIG_DIR;
import static pm.util.Constants.PROJECTS_FILE;

/**
 * Gestor de persistencia de proyectos usando JSON.
 */
public class ProjectStore {

    private final Gson gson;

    public ProjectStore() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Guarda todos los proyectos en JSON.
     */
    public void save(Map<String, Project> projects) throws IOException {
        ensureConfigDirExists();

        // Convertir Projects a DTOs
        Map<String, ProjectDTO> dtos = new HashMap<>();
        for (Map.Entry<String, Project> entry : projects.entrySet()) {
            dtos.put(entry.getKey(), ProjectDTO.fromProject(entry.getValue()));
        }

        String json = gson.toJson(dtos);
        Files.writeString(PROJECTS_FILE, json);
    }

    /**
     * Carga todos los proyectos desde JSON.
     */
    public Map<String, Project> load() throws IOException {
        if (!Files.exists(PROJECTS_FILE)) {
            return new HashMap<>();
        }

        String json = Files.readString(PROJECTS_FILE);

        TypeToken<Map<String, ProjectDTO>> typeToken =
                new TypeToken<Map<String, ProjectDTO>>() {};

        Map<String, ProjectDTO> dtos = gson.fromJson(json, typeToken.getType());

        // Convertir DTOs a Projects
        Map<String, Project> projects = new HashMap<>();
        if (dtos != null) {
            for (Map.Entry<String, ProjectDTO> entry : dtos.entrySet()) {
                projects.put(entry.getKey(), entry.getValue().toProject());
            }
        }

        return projects;
    }

    /**
     * Guarda un proyecto específico.
     */
    public void saveProject(Project project) throws IOException {
        Map<String, Project> projects = load();
        projects.put(project.name(), project);
        save(projects);
    }

    /**
     * Elimina un proyecto.
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
     */
    public Project findProject(String projectName) throws IOException {
        Map<String, Project> projects = load();
        return projects.get(projectName);
    }

    /**
     * Crea el directorio de configuración si no existe.
     */
    private void ensureConfigDirExists() throws IOException {
        if (!Files.exists(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }
    }

    /**
     * DTO (Data Transfer Object) para serialización JSON.
     * Usa tipos simples que Gson puede manejar sin problemas.
     */
    private static class ProjectDTO {
        String name;
        String path;  // String en vez de Path
        String type;  // String en vez de ProjectType
        Map<String, String> commands;
        String lastModified;  // String en vez de Instant
        Map<String, String> envVars;

        /**
         * Convierte un Project a DTO.
         */
        static ProjectDTO fromProject(Project project) {
            ProjectDTO dto = new ProjectDTO();
            dto.name = project.name();
            dto.path = project.path().toString();
            dto.type = project.type().name();
            dto.commands = new HashMap<>(project.commands());
            dto.envVars = new HashMap<>(project.envVars());
            dto.lastModified = project.lastModified().toString();
            return dto;
        }

        /**
         * Convierte un DTO a Project.
         */
        Project toProject() {
            Project project = new Project(
                    name,
                    Paths.get(path),
                    ProjectType.valueOf(type)
            );

            if (commands != null) {
                commands.forEach(project::addCommand);
            }

            if (envVars != null) {
                envVars.forEach(project::addEnvVar);
            }

            return project;
        }
    }
}