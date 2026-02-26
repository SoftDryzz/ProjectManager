package pm.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import pm.core.Project;
import pm.detector.ProjectType;
import pm.storage.ProjectStore;
import pm.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exports and imports projects to/from portable JSON files.
 *
 * <p>Export format is self-describing JSON with metadata:
 * <pre>{@code
 * {
 *   "version": "1.6.4",
 *   "exportedAt": "2026-02-26T18:00:00Z",
 *   "projectCount": 3,
 *   "projects": { ... }
 * }
 * }</pre>
 *
 * <p>Uses Gson {@link JsonObject} directly to avoid coupling to the
 * internal serialization format in {@link ProjectStore}.
 *
 * @author SoftDryzz
 * @version 1.6.4
 * @since 1.6.4
 */
public final class ProjectExporter {

    private final ProjectStore store;
    private final Gson gson;

    public ProjectExporter(ProjectStore store) {
        this.store = store;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // ============================================================
    // EXPORT
    // ============================================================

    /**
     * Exports projects to a JSON file.
     *
     * @param outputFile    destination file path
     * @param projectNames  specific project names to export; null or empty means all
     * @return result with count of exported projects and any not-found names
     * @throws IOException if the store cannot be loaded or the file cannot be written
     */
    public ExportResult export(Path outputFile, List<String> projectNames) throws IOException {
        Map<String, Project> allProjects = store.load();
        List<String> notFound = new ArrayList<>();

        Map<String, Project> toExport;
        if (projectNames == null || projectNames.isEmpty()) {
            toExport = allProjects;
        } else {
            toExport = new LinkedHashMap<>();
            for (String name : projectNames) {
                Project p = allProjects.get(name);
                if (p != null) {
                    toExport.put(name, p);
                } else {
                    notFound.add(name);
                }
            }
        }

        JsonObject root = new JsonObject();
        root.addProperty("version", Constants.VERSION);
        root.addProperty("exportedAt", Instant.now().toString());
        root.addProperty("projectCount", toExport.size());

        JsonObject projectsJson = new JsonObject();
        for (Map.Entry<String, Project> entry : toExport.entrySet()) {
            projectsJson.add(entry.getKey(), projectToJson(entry.getValue()));
        }
        root.add("projects", projectsJson);

        Files.writeString(outputFile, gson.toJson(root));

        return new ExportResult(toExport.size(), outputFile, notFound);
    }

    // ============================================================
    // IMPORT
    // ============================================================

    /**
     * Imports projects from a previously exported JSON file.
     *
     * <p>Projects that already exist in the store are skipped.
     * Projects whose paths do not exist on disk are imported with a warning.
     *
     * @param inputFile path to the export file
     * @return result with counts and warnings
     * @throws IOException if the file cannot be read or the store cannot be accessed
     * @throws IllegalArgumentException if the file contains invalid JSON
     */
    public ImportResult importProjects(Path inputFile) throws IOException {
        String content = Files.readString(inputFile);

        JsonObject root;
        try {
            root = JsonParser.parseString(content).getAsJsonObject();
        } catch (JsonSyntaxException | IllegalStateException e) {
            throw new IllegalArgumentException("Invalid export file: not valid JSON");
        }

        if (!root.has("projects") || !root.get("projects").isJsonObject()) {
            throw new IllegalArgumentException(
                    "Invalid export file: missing 'projects' object");
        }

        JsonObject projectsJson = root.getAsJsonObject("projects");
        Map<String, Project> existingProjects = store.load();

        int imported = 0;
        List<String> skipped = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (Map.Entry<String, JsonElement> entry : projectsJson.entrySet()) {
            String key = entry.getKey();

            if (existingProjects.containsKey(key)) {
                skipped.add(key);
                continue;
            }

            try {
                JsonObject projJson = entry.getValue().getAsJsonObject();
                Project project = jsonToProject(key, projJson, warnings);

                if (project == null) {
                    continue;
                }

                if (!Files.exists(project.path())) {
                    warnings.add("'" + key + "' path does not exist: " + project.path());
                }

                store.saveProject(project);
                existingProjects.put(key, project);
                imported++;

            } catch (Exception e) {
                warnings.add("Skipped '" + key + "': " + e.getMessage());
            }
        }

        return new ImportResult(imported, skipped, warnings);
    }

    // ============================================================
    // JSON CONVERSION
    // ============================================================

    /**
     * Converts a Project to a JsonObject for export.
     */
    JsonObject projectToJson(Project project) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", project.name());
        obj.addProperty("path", project.path().toString());
        obj.addProperty("type", project.type().name());

        JsonObject cmds = new JsonObject();
        project.commands().forEach(cmds::addProperty);
        obj.add("commands", cmds);

        JsonObject envs = new JsonObject();
        project.envVars().forEach(envs::addProperty);
        obj.add("envVars", envs);

        JsonObject hooksJson = new JsonObject();
        project.hooks().forEach((slot, scripts) -> {
            JsonArray arr = new JsonArray();
            scripts.forEach(arr::add);
            hooksJson.add(slot, arr);
        });
        obj.add("hooks", hooksJson);

        obj.addProperty("lastModified", project.lastModified().toString());

        return obj;
    }

    /**
     * Converts a JsonObject back to a Project.
     *
     * @param key      the map key (used as fallback name)
     * @param json     the project JSON object
     * @param warnings list to accumulate non-fatal warnings
     * @return the Project, or null if fatally invalid (missing path)
     */
    Project jsonToProject(String key, JsonObject json, List<String> warnings) {
        String name = json.has("name") && !json.get("name").isJsonNull()
                && !json.get("name").getAsString().isBlank()
                ? json.get("name").getAsString()
                : key;

        if (!json.has("path") || json.get("path").isJsonNull()
                || json.get("path").getAsString().isBlank()) {
            warnings.add("Skipped '" + name + "': missing path");
            return null;
        }
        Path path = Path.of(json.get("path").getAsString());

        ProjectType type;
        if (!json.has("type") || json.get("type").isJsonNull()
                || json.get("type").getAsString().isBlank()) {
            warnings.add("'" + name + "': missing type, defaulting to UNKNOWN");
            type = ProjectType.UNKNOWN;
        } else {
            try {
                type = ProjectType.valueOf(json.get("type").getAsString());
            } catch (IllegalArgumentException e) {
                warnings.add("'" + name + "': unknown type '"
                        + json.get("type").getAsString() + "', defaulting to UNKNOWN");
                type = ProjectType.UNKNOWN;
            }
        }

        Project project = new Project(name, path, type);

        if (json.has("commands") && json.get("commands").isJsonObject()) {
            for (Map.Entry<String, JsonElement> cmd : json.getAsJsonObject("commands").entrySet()) {
                if (cmd.getValue().isJsonPrimitive()) {
                    project.addCommand(cmd.getKey(), cmd.getValue().getAsString());
                }
            }
        }

        if (json.has("envVars") && json.get("envVars").isJsonObject()) {
            for (Map.Entry<String, JsonElement> env : json.getAsJsonObject("envVars").entrySet()) {
                if (env.getValue().isJsonPrimitive()) {
                    project.addEnvVar(env.getKey(), env.getValue().getAsString());
                }
            }
        }

        if (json.has("hooks") && json.get("hooks").isJsonObject()) {
            for (Map.Entry<String, JsonElement> hook : json.getAsJsonObject("hooks").entrySet()) {
                if (hook.getValue().isJsonArray()) {
                    for (JsonElement script : hook.getValue().getAsJsonArray()) {
                        if (script.isJsonPrimitive() && !script.getAsString().isBlank()) {
                            project.addHook(hook.getKey(), script.getAsString());
                        }
                    }
                }
            }
        }

        return project;
    }
}
