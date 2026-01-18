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
 * Project persistence manager using JSON.
 */
public class ProjectStore {

    private final Gson gson;

    public ProjectStore() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Saves all projects to JSON.
     */
    public void save(Map<String, Project> projects) throws IOException {
        ensureConfigDirExists();

        // Convert Projects to DTOs
        Map<String, ProjectDTO> dtos = new HashMap<>();
        for (Map.Entry<String, Project> entry : projects.entrySet()) {
            dtos.put(entry.getKey(), ProjectDTO.fromProject(entry.getValue()));
        }

        String json = gson.toJson(dtos);
        Files.writeString(PROJECTS_FILE, json);
    }

    /**
     * Loads all projects from JSON.
     */
    public Map<String, Project> load() throws IOException {
        if (!Files.exists(PROJECTS_FILE)) {
            return new HashMap<>();
        }

        String json = Files.readString(PROJECTS_FILE);

        TypeToken<Map<String, ProjectDTO>> typeToken =
                new TypeToken<Map<String, ProjectDTO>>() {};

        Map<String, ProjectDTO> dtos = gson.fromJson(json, typeToken.getType());

        // Convert DTOs to Projects
        Map<String, Project> projects = new HashMap<>();
        if (dtos != null) {
            for (Map.Entry<String, ProjectDTO> entry : dtos.entrySet()) {
                projects.put(entry.getKey(), entry.getValue().toProject());
            }
        }

        return projects;
    }

    /**
     * Saves a specific project.
     */
    public void saveProject(Project project) throws IOException {
        Map<String, Project> projects = load();
        projects.put(project.name(), project);
        save(projects);
    }

    /**
     * Deletes a project.
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
     * Finds a project by name.
     */
    public Project findProject(String projectName) throws IOException {
        Map<String, Project> projects = load();
        return projects.get(projectName);
    }

    /**
     * Creates the configuration directory if it does not exist.
     */
    private void ensureConfigDirExists() throws IOException {
        if (!Files.exists(CONFIG_DIR)) {
            Files.createDirectories(CONFIG_DIR);
        }
    }

    /**
     * DTO (Data Transfer Object) for JSON serialization.
     * Uses simple types that Gson can handle without issues.
     */
    private static class ProjectDTO {
        String name;
        String path;          // String instead of Path
        String type;          // String instead of ProjectType
        Map<String, String> commands;
        String lastModified;  // String instead of Instant
        Map<String, String> envVars;

        /**
         * Converts a Project to DTO.
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
         * Converts a DTO to Project.
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