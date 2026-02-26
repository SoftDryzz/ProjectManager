package pm.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import pm.cli.OutputFormatter;
import pm.core.Project;
import pm.detector.ProjectType;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pm.util.Constants.CONFIG_DIR;
import static pm.util.Constants.PROJECTS_FILE;

/**
 * Project persistence manager using JSON.
 *
 * <p>Provides atomic writes, automatic backup, and recovery from corrupted data.
 *
 * <p>Write safety strategy:
 * <ol>
 * <li>Backup current {@code projects.json} → {@code projects.json.bak}</li>
 * <li>Write new data to {@code projects.json.tmp}</li>
 * <li>Atomically rename {@code .tmp} → {@code projects.json}</li>
 * </ol>
 *
 * <p>If the JSON file is corrupted on load, the backup is automatically restored.
 *
 * @author SoftDryzz
 * @version 1.3.7
 * @since 1.0.0
 */
public class ProjectStore {

    private static final Path BACKUP_FILE = CONFIG_DIR.resolve("projects.json.bak");
    private static final Path TEMP_FILE = CONFIG_DIR.resolve("projects.json.tmp");

    private final Gson gson;

    public ProjectStore() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Saves all projects to JSON using atomic write with backup.
     *
     * <p>Steps:
     * <ol>
     * <li>Convert projects to DTOs</li>
     * <li>Backup current file (if it exists)</li>
     * <li>Write JSON to temp file</li>
     * <li>Atomically move temp file to projects.json</li>
     * </ol>
     *
     * @param projects map of projects to save
     * @throws IOException if backup, write, or rename fails
     */
    public void save(Map<String, Project> projects) throws IOException {
        ensureConfigDirExists();

        // Convert Projects to DTOs
        Map<String, ProjectDTO> dtos = new HashMap<>();
        for (Map.Entry<String, Project> entry : projects.entrySet()) {
            dtos.put(entry.getKey(), ProjectDTO.fromProject(entry.getValue()));
        }

        String json = gson.toJson(dtos);

        // 1. Backup current file before writing
        backupCurrentFile();

        // 2. Write to temp file first
        Files.writeString(TEMP_FILE, json);

        // 3. Atomic move: temp → projects.json
        try {
            Files.move(TEMP_FILE, PROJECTS_FILE,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Fallback: non-atomic move (some Windows filesystems)
            Files.move(TEMP_FILE, PROJECTS_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Loads all projects from JSON with automatic recovery from corruption.
     *
     * <p>If the main file is corrupted, attempts to load from the backup.
     * Invalid entries (null fields, unknown types) are skipped with warnings.
     *
     * @return map of valid projects
     * @throws IOException if both main and backup files are unreadable
     */
    public Map<String, Project> load() throws IOException {
        if (!Files.exists(PROJECTS_FILE)) {
            return new HashMap<>();
        }

        // Try loading main file
        try {
            String json = Files.readString(PROJECTS_FILE);
            return parseProjects(json);
        } catch (JsonSyntaxException e) {
            // Main file is corrupted — try backup
            return recoverFromBackup(e);
        }
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
     * Renames a project (creates a new entry with the new name, removes the old one).
     *
     * @param oldName current project name
     * @param newName desired new name
     * @return the renamed project, or null if oldName not found
     * @throws IllegalArgumentException if newName already exists
     */
    public Project renameProject(String oldName, String newName) throws IOException {
        Map<String, Project> projects = load();

        Project existing = projects.get(oldName);
        if (existing == null) {
            return null;
        }

        if (projects.containsKey(newName)) {
            throw new IllegalArgumentException("A project named '" + newName + "' already exists");
        }

        // Create new project with new name, copy everything else
        Project renamed = new Project(newName, existing.path(), existing.type());
        existing.commands().forEach(renamed::addCommand);
        existing.envVars().forEach(renamed::addEnvVar);
        existing.hooks().forEach((slot, scripts) ->
                scripts.forEach(script -> renamed.addHook(slot, script)));

        projects.remove(oldName);
        projects.put(newName, renamed);
        save(projects);

        return renamed;
    }

    // ============================================================
    // INTERNAL: Backup, Recovery, Validation
    // ============================================================

    /**
     * Creates a backup of the current projects.json file.
     * Does nothing if the file doesn't exist yet.
     */
    private void backupCurrentFile() throws IOException {
        if (Files.exists(PROJECTS_FILE)) {
            Files.copy(PROJECTS_FILE, BACKUP_FILE, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Attempts to recover projects from the backup file after the main file is corrupted.
     *
     * @param originalError the error from parsing the main file
     * @return recovered projects map
     * @throws IOException if backup is also unreadable or doesn't exist
     */
    private Map<String, Project> recoverFromBackup(JsonSyntaxException originalError) throws IOException {
        if (!Files.exists(BACKUP_FILE)) {
            throw new IOException(
                    "projects.json is corrupted and no backup was found.\n" +
                    "  The file may have been manually edited with invalid JSON.\n" +
                    "  Location: " + PROJECTS_FILE + "\n" +
                    "  Error: " + originalError.getMessage());
        }

        try {
            String backupJson = Files.readString(BACKUP_FILE);
            Map<String, Project> recovered = parseProjects(backupJson);

            // Restore backup → main file
            Files.copy(BACKUP_FILE, PROJECTS_FILE, StandardCopyOption.REPLACE_EXISTING);

            OutputFormatter.warning("projects.json was corrupted — restored from backup (" +
                    recovered.size() + " project" + (recovered.size() != 1 ? "s" : "") + " recovered)");

            return recovered;

        } catch (JsonSyntaxException backupError) {
            throw new IOException(
                    "Both projects.json and its backup are corrupted.\n" +
                    "  You may need to delete them and re-register your projects.\n" +
                    "  Main file: " + PROJECTS_FILE + "\n" +
                    "  Backup: " + BACKUP_FILE);
        }
    }

    /**
     * Parses JSON into a map of Projects, validating each entry.
     * Invalid entries are skipped with a warning instead of failing the entire load.
     *
     * @param json raw JSON string
     * @return map of valid projects
     */
    private Map<String, Project> parseProjects(String json) {
        TypeToken<Map<String, ProjectDTO>> typeToken =
                new TypeToken<Map<String, ProjectDTO>>() {};

        Map<String, ProjectDTO> dtos = gson.fromJson(json, typeToken.getType());

        Map<String, Project> projects = new HashMap<>();
        if (dtos == null) {
            return projects;
        }

        List<String> warnings = new ArrayList<>();

        for (Map.Entry<String, ProjectDTO> entry : dtos.entrySet()) {
            String key = entry.getKey();
            ProjectDTO dto = entry.getValue();

            try {
                Project project = dto.toProjectSafe(key, warnings);
                if (project != null) {
                    projects.put(key, project);
                }
            } catch (Exception e) {
                warnings.add("Skipped project '" + key + "': " + e.getMessage());
            }
        }

        // Print warnings after loading (if any)
        for (String warning : warnings) {
            OutputFormatter.warning(warning);
        }

        return projects;
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
    static class ProjectDTO {
        String name;
        String path;          // String instead of Path
        String type;          // String instead of ProjectType
        Map<String, String> commands;
        String lastModified;  // String instead of Instant
        Map<String, String> envVars;
        Map<String, List<String>> hooks;

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
            // Deep copy hooks: each list must be a new ArrayList
            dto.hooks = project.hooks().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArrayList<>(e.getValue())));
            return dto;
        }

        /**
         * Converts a DTO to Project with validation and graceful error handling.
         *
         * @param key      the map key (used as fallback name)
         * @param warnings list to accumulate non-fatal warnings
         * @return the Project, or null if the entry is fatally invalid
         */
        Project toProjectSafe(String key, List<String> warnings) {
            // Validate required fields
            String safeName = (name != null && !name.isBlank()) ? name : key;

            if (path == null || path.isBlank()) {
                warnings.add("Skipped project '" + safeName + "': missing path");
                return null;
            }

            // Handle invalid or missing ProjectType
            ProjectType projectType;
            if (type == null || type.isBlank()) {
                warnings.add("Project '" + safeName + "': missing type, defaulting to UNKNOWN");
                projectType = ProjectType.UNKNOWN;
            } else {
                try {
                    projectType = ProjectType.valueOf(type);
                } catch (IllegalArgumentException e) {
                    warnings.add("Project '" + safeName + "': unknown type '" + type + "', defaulting to UNKNOWN");
                    projectType = ProjectType.UNKNOWN;
                }
            }

            Project project = new Project(safeName, Paths.get(path), projectType);

            if (commands != null) {
                commands.forEach(project::addCommand);
            }

            if (envVars != null) {
                envVars.forEach(project::addEnvVar);
            }

            if (hooks != null) {
                hooks.forEach((slot, scripts) -> {
                    if (scripts != null) {
                        scripts.forEach(script -> project.addHook(slot, script));
                    }
                });
            }

            return project;
        }

        /**
         * Converts a DTO to Project (legacy method, kept for backward compatibility in tests).
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

            if (hooks != null) {
                hooks.forEach((slot, scripts) -> {
                    if (scripts != null) {
                        scripts.forEach(script -> project.addHook(slot, script));
                    }
                });
            }

            return project;
        }
    }
}
