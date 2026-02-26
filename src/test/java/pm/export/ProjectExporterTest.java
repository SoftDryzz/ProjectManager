package pm.export;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import pm.core.Project;
import pm.detector.ProjectType;
import pm.storage.ProjectStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectExporter")
class ProjectExporterTest {

    @TempDir
    Path tempDir;

    private StubProjectStore store;
    private ProjectExporter exporter;

    /**
     * In-memory stub of ProjectStore to avoid Mockito issues on Java 25.
     */
    static class StubProjectStore extends ProjectStore {
        private final Map<String, Project> projects = new LinkedHashMap<>();
        private final List<Project> saved = new ArrayList<>();

        void addProject(Project p) {
            projects.put(p.name(), p);
        }

        List<Project> getSaved() {
            return saved;
        }

        @Override
        public Map<String, Project> load() {
            return new LinkedHashMap<>(projects);
        }

        @Override
        public void saveProject(Project project) {
            projects.put(project.name(), project);
            saved.add(project);
        }
    }

    @BeforeEach
    void setUp() {
        store = new StubProjectStore();
        exporter = new ProjectExporter(store);
    }

    private Project createProject(String name, String path, ProjectType type) {
        Project p = new Project(name, Paths.get(path), type);
        p.addCommand("build", "gradle build");
        p.addCommand("test", "gradle test");
        return p;
    }

    private Project createFullProject() {
        Project p = new Project("backend", Paths.get("/home/user/backend"), ProjectType.NODEJS);
        p.addCommand("build", "npm run build");
        p.addCommand("test", "npm test");
        p.addEnvVar("PORT", "3000");
        p.addEnvVar("NODE_ENV", "production");
        p.addHook("pre-build", "npm run lint");
        p.addHook("post-test", "npm run coverage");
        return p;
    }

    // ============================================================
    // PROJECT TO JSON
    // ============================================================

    @Nested
    @DisplayName("projectToJson")
    class ProjectToJson {

        @Test
        @DisplayName("converts full project with all fields")
        void fullProject() {
            Project project = createFullProject();
            JsonObject json = exporter.projectToJson(project);

            assertEquals("backend", json.get("name").getAsString());
            assertEquals(Paths.get("/home/user/backend").toString(), json.get("path").getAsString());
            assertEquals("NODEJS", json.get("type").getAsString());
            assertEquals("npm run build", json.getAsJsonObject("commands").get("build").getAsString());
            assertEquals("npm test", json.getAsJsonObject("commands").get("test").getAsString());
            assertEquals("3000", json.getAsJsonObject("envVars").get("PORT").getAsString());
            assertEquals("production", json.getAsJsonObject("envVars").get("NODE_ENV").getAsString());
            assertNotNull(json.get("lastModified"));
        }

        @Test
        @DisplayName("converts project with empty collections")
        void emptyCollections() {
            Project project = new Project("empty", Paths.get("/tmp/empty"), ProjectType.UNKNOWN);
            JsonObject json = exporter.projectToJson(project);

            assertEquals("empty", json.get("name").getAsString());
            assertEquals(0, json.getAsJsonObject("commands").size());
            assertEquals(0, json.getAsJsonObject("envVars").size());
            assertEquals(0, json.getAsJsonObject("hooks").size());
        }

        @Test
        @DisplayName("converts hooks as arrays")
        void hooksAsArrays() {
            Project project = new Project("hooked", Paths.get("/tmp/h"), ProjectType.GRADLE);
            project.addHook("pre-build", "echo first");
            project.addHook("pre-build", "echo second");
            JsonObject json = exporter.projectToJson(project);

            JsonArray preBuild = json.getAsJsonObject("hooks").getAsJsonArray("pre-build");
            assertEquals(2, preBuild.size());
            assertEquals("echo first", preBuild.get(0).getAsString());
            assertEquals("echo second", preBuild.get(1).getAsString());
        }

        @Test
        @DisplayName("path is serialized as string")
        void pathAsString() {
            Project project = new Project("test", Paths.get("/a/b/c"), ProjectType.MAVEN);
            JsonObject json = exporter.projectToJson(project);

            assertTrue(json.get("path").isJsonPrimitive());
            assertTrue(json.get("path").getAsString().contains("a"));
        }

        @Test
        @DisplayName("type is serialized as enum name")
        void typeAsEnumName() {
            Project project = new Project("test", Paths.get("/tmp"), ProjectType.FLUTTER);
            JsonObject json = exporter.projectToJson(project);

            assertEquals("FLUTTER", json.get("type").getAsString());
        }
    }

    // ============================================================
    // JSON TO PROJECT
    // ============================================================

    @Nested
    @DisplayName("jsonToProject")
    class JsonToProject {

        @Test
        @DisplayName("parses full JSON into Project")
        void fullJson() {
            JsonObject json = new JsonObject();
            json.addProperty("name", "api");
            json.addProperty("path", "/home/user/api");
            json.addProperty("type", "RUST");
            JsonObject cmds = new JsonObject();
            cmds.addProperty("build", "cargo build");
            json.add("commands", cmds);
            JsonObject envs = new JsonObject();
            envs.addProperty("RUST_LOG", "debug");
            json.add("envVars", envs);

            List<String> warnings = new ArrayList<>();
            Project project = exporter.jsonToProject("api", json, warnings);

            assertNotNull(project);
            assertEquals("api", project.name());
            assertEquals(Paths.get("/home/user/api"), project.path());
            assertEquals(ProjectType.RUST, project.type());
            assertEquals("cargo build", project.commands().get("build"));
            assertEquals("debug", project.envVars().get("RUST_LOG"));
            assertTrue(warnings.isEmpty());
        }

        @Test
        @DisplayName("uses key as fallback when name is missing")
        void missingNameFallback() {
            JsonObject json = new JsonObject();
            json.addProperty("path", "/tmp/project");
            json.addProperty("type", "GO");

            List<String> warnings = new ArrayList<>();
            Project project = exporter.jsonToProject("my-key", json, warnings);

            assertNotNull(project);
            assertEquals("my-key", project.name());
        }

        @Test
        @DisplayName("returns null when path is missing")
        void missingPath() {
            JsonObject json = new JsonObject();
            json.addProperty("name", "no-path");
            json.addProperty("type", "NODEJS");

            List<String> warnings = new ArrayList<>();
            Project project = exporter.jsonToProject("no-path", json, warnings);

            assertNull(project);
            assertTrue(warnings.stream().anyMatch(w -> w.contains("missing path")));
        }

        @Test
        @DisplayName("defaults to UNKNOWN when type is missing")
        void missingType() {
            JsonObject json = new JsonObject();
            json.addProperty("name", "typeless");
            json.addProperty("path", "/tmp/typeless");

            List<String> warnings = new ArrayList<>();
            Project project = exporter.jsonToProject("typeless", json, warnings);

            assertNotNull(project);
            assertEquals(ProjectType.UNKNOWN, project.type());
            assertTrue(warnings.stream().anyMatch(w -> w.contains("missing type")));
        }

        @Test
        @DisplayName("defaults to UNKNOWN for invalid type value")
        void invalidType() {
            JsonObject json = new JsonObject();
            json.addProperty("name", "invalid");
            json.addProperty("path", "/tmp/invalid");
            json.addProperty("type", "NONEXISTENT_TYPE");

            List<String> warnings = new ArrayList<>();
            Project project = exporter.jsonToProject("invalid", json, warnings);

            assertNotNull(project);
            assertEquals(ProjectType.UNKNOWN, project.type());
            assertTrue(warnings.stream().anyMatch(w -> w.contains("unknown type")));
        }

        @Test
        @DisplayName("handles missing optional fields gracefully")
        void missingOptionalFields() {
            JsonObject json = new JsonObject();
            json.addProperty("name", "minimal");
            json.addProperty("path", "/tmp/minimal");
            json.addProperty("type", "GRADLE");

            List<String> warnings = new ArrayList<>();
            Project project = exporter.jsonToProject("minimal", json, warnings);

            assertNotNull(project);
            assertEquals(0, project.commandCount());
            assertEquals(0, project.envVarCount());
            assertEquals(0, project.hookCount());
            assertTrue(warnings.isEmpty());
        }

        @Test
        @DisplayName("preserves hook order from JSON array")
        void hookOrder() {
            JsonObject json = new JsonObject();
            json.addProperty("name", "ordered");
            json.addProperty("path", "/tmp/ordered");
            json.addProperty("type", "NODEJS");
            JsonObject hooks = new JsonObject();
            JsonArray arr = new JsonArray();
            arr.add("first");
            arr.add("second");
            arr.add("third");
            hooks.add("pre-build", arr);
            json.add("hooks", hooks);

            List<String> warnings = new ArrayList<>();
            Project project = exporter.jsonToProject("ordered", json, warnings);

            assertNotNull(project);
            List<String> preBuild = project.hooks().get("pre-build");
            assertEquals(3, preBuild.size());
            assertEquals("first", preBuild.get(0));
            assertEquals("second", preBuild.get(1));
            assertEquals("third", preBuild.get(2));
        }
    }

    // ============================================================
    // EXPORT FILE FORMAT
    // ============================================================

    @Nested
    @DisplayName("Export File Format")
    class ExportFileFormat {

        @Test
        @DisplayName("export file contains version metadata")
        void versionMetadata() throws IOException {
            Path outputFile = tempDir.resolve("test-export.json");
            exporter.export(outputFile, null);

            String content = Files.readString(outputFile);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            assertTrue(root.has("version"));
            assertTrue(root.has("exportedAt"));
            assertFalse(root.get("version").getAsString().isEmpty());
            assertFalse(root.get("exportedAt").getAsString().isEmpty());
        }

        @Test
        @DisplayName("export file has correct projectCount")
        void correctProjectCount() throws IOException {
            store.addProject(createProject("a", "/tmp/a", ProjectType.GRADLE));
            store.addProject(createProject("b", "/tmp/b", ProjectType.MAVEN));

            Path outputFile = tempDir.resolve("test-export.json");
            exporter.export(outputFile, null);

            String content = Files.readString(outputFile);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            assertEquals(2, root.get("projectCount").getAsInt());
        }

        @Test
        @DisplayName("export file is valid parseable JSON")
        void validJson() throws IOException {
            store.addProject(createFullProject());

            Path outputFile = tempDir.resolve("test-export.json");
            exporter.export(outputFile, null);

            String content = Files.readString(outputFile);
            assertDoesNotThrow(() -> JsonParser.parseString(content).getAsJsonObject());
        }
    }

    // ============================================================
    // EXPORT SCENARIOS
    // ============================================================

    @Nested
    @DisplayName("Export Scenarios")
    class ExportScenarios {

        @Test
        @DisplayName("exports all projects when no names specified")
        void exportAll() throws IOException {
            store.addProject(createProject("a", "/tmp/a", ProjectType.GRADLE));
            store.addProject(createProject("b", "/tmp/b", ProjectType.NODEJS));
            store.addProject(createProject("c", "/tmp/c", ProjectType.RUST));

            Path outputFile = tempDir.resolve("all.json");
            ExportResult result = exporter.export(outputFile, null);

            assertEquals(3, result.exported());
            assertEquals(outputFile, result.outputFile());
            assertTrue(result.notFound().isEmpty());
        }

        @Test
        @DisplayName("exports only selected projects")
        void exportSelective() throws IOException {
            store.addProject(createProject("a", "/tmp/a", ProjectType.GRADLE));
            store.addProject(createProject("b", "/tmp/b", ProjectType.NODEJS));
            store.addProject(createProject("c", "/tmp/c", ProjectType.RUST));

            Path outputFile = tempDir.resolve("selective.json");
            ExportResult result = exporter.export(outputFile, List.of("a", "c"));

            assertEquals(2, result.exported());
            assertTrue(result.notFound().isEmpty());

            String content = Files.readString(outputFile);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            JsonObject projs = root.getAsJsonObject("projects");
            assertTrue(projs.has("a"));
            assertFalse(projs.has("b"));
            assertTrue(projs.has("c"));
        }

        @Test
        @DisplayName("tracks not-found project names")
        void notFoundNames() throws IOException {
            store.addProject(createProject("existing", "/tmp/e", ProjectType.GO));

            Path outputFile = tempDir.resolve("notfound.json");
            ExportResult result = exporter.export(outputFile, List.of("existing", "ghost", "phantom"));

            assertEquals(1, result.exported());
            assertEquals(2, result.notFound().size());
            assertTrue(result.notFound().contains("ghost"));
            assertTrue(result.notFound().contains("phantom"));
        }

        @Test
        @DisplayName("exports empty store produces zero count")
        void emptyStore() throws IOException {
            Path outputFile = tempDir.resolve("empty.json");
            ExportResult result = exporter.export(outputFile, null);

            assertEquals(0, result.exported());
            assertTrue(result.notFound().isEmpty());
        }
    }

    // ============================================================
    // IMPORT SCENARIOS
    // ============================================================

    @Nested
    @DisplayName("Import Scenarios")
    class ImportScenarios {

        @Test
        @DisplayName("imports valid projects from file")
        void validImport() throws IOException {
            Path importFile = tempDir.resolve("import.json");
            JsonObject root = new JsonObject();
            root.addProperty("version", "1.6.4");
            root.addProperty("exportedAt", "2026-02-26T18:00:00Z");
            root.addProperty("projectCount", 1);
            JsonObject projects = new JsonObject();
            JsonObject proj = new JsonObject();
            proj.addProperty("name", "imported");
            proj.addProperty("path", tempDir.toString());
            proj.addProperty("type", "GRADLE");
            projects.add("imported", proj);
            root.add("projects", projects);
            Files.writeString(importFile, root.toString());

            ImportResult result = exporter.importProjects(importFile);

            assertEquals(1, result.imported());
            assertTrue(result.skipped().isEmpty());
            assertEquals(1, store.getSaved().size());
            assertEquals("imported", store.getSaved().get(0).name());
        }

        @Test
        @DisplayName("skips existing projects")
        void skipExisting() throws IOException {
            store.addProject(createProject("taken", "/tmp/taken", ProjectType.NODEJS));

            Path importFile = tempDir.resolve("import.json");
            JsonObject root = new JsonObject();
            root.addProperty("version", "1.6.4");
            JsonObject projects = new JsonObject();
            JsonObject proj = new JsonObject();
            proj.addProperty("name", "taken");
            proj.addProperty("path", "/tmp/taken");
            proj.addProperty("type", "NODEJS");
            projects.add("taken", proj);
            root.add("projects", projects);
            Files.writeString(importFile, root.toString());

            ImportResult result = exporter.importProjects(importFile);

            assertEquals(0, result.imported());
            assertEquals(1, result.skipped().size());
            assertEquals("taken", result.skipped().get(0));
            assertTrue(store.getSaved().isEmpty());
        }

        @Test
        @DisplayName("warns about missing paths on import")
        void missingPathWarning() throws IOException {
            Path importFile = tempDir.resolve("import.json");
            JsonObject root = new JsonObject();
            root.addProperty("version", "1.6.4");
            JsonObject projects = new JsonObject();
            JsonObject proj = new JsonObject();
            proj.addProperty("name", "faraway");
            proj.addProperty("path", "/nonexistent/path/that/does/not/exist");
            proj.addProperty("type", "RUST");
            projects.add("faraway", proj);
            root.add("projects", projects);
            Files.writeString(importFile, root.toString());

            ImportResult result = exporter.importProjects(importFile);

            assertEquals(1, result.imported());
            assertTrue(result.warnings().stream().anyMatch(w -> w.contains("does not exist")));
        }

        @Test
        @DisplayName("throws on malformed JSON")
        void malformedJson() throws IOException {
            Path importFile = tempDir.resolve("bad.json");
            Files.writeString(importFile, "this is not json {{{");

            assertThrows(IllegalArgumentException.class,
                    () -> exporter.importProjects(importFile));
        }

        @Test
        @DisplayName("throws when projects key is missing")
        void missingProjectsKey() throws IOException {
            Path importFile = tempDir.resolve("noprojects.json");
            JsonObject root = new JsonObject();
            root.addProperty("version", "1.6.4");
            Files.writeString(importFile, root.toString());

            assertThrows(IllegalArgumentException.class,
                    () -> exporter.importProjects(importFile));
        }

        @Test
        @DisplayName("handles empty projects object")
        void emptyProjects() throws IOException {
            Path importFile = tempDir.resolve("empty.json");
            JsonObject root = new JsonObject();
            root.addProperty("version", "1.6.4");
            root.add("projects", new JsonObject());
            Files.writeString(importFile, root.toString());

            ImportResult result = exporter.importProjects(importFile);

            assertEquals(0, result.imported());
            assertTrue(result.skipped().isEmpty());
            assertTrue(result.warnings().isEmpty());
        }
    }

    // ============================================================
    // ROUND TRIP
    // ============================================================

    @Nested
    @DisplayName("Round Trip")
    class RoundTrip {

        @Test
        @DisplayName("export then import preserves all project data")
        void fullRoundTrip() throws IOException {
            Project original = createFullProject();
            store.addProject(original);

            Path file = tempDir.resolve("roundtrip.json");
            exporter.export(file, null);

            // Clear store and re-import
            StubProjectStore freshStore = new StubProjectStore();
            ProjectExporter freshExporter = new ProjectExporter(freshStore);
            ImportResult result = freshExporter.importProjects(file);

            assertEquals(1, result.imported());

            Project imported = freshStore.getSaved().get(0);
            assertEquals(original.name(), imported.name());
            assertEquals(original.path(), imported.path());
            assertEquals(original.type(), imported.type());
            assertEquals(original.commands(), imported.commands());
            assertEquals(original.envVars(), imported.envVars());
            assertEquals(original.hooks(), imported.hooks());
        }

        @Test
        @DisplayName("hooks order is preserved through round trip")
        void hooksOrderPreserved() throws IOException {
            Project project = new Project("hooked", Paths.get("/tmp/h"), ProjectType.GRADLE);
            project.addHook("pre-build", "step-1");
            project.addHook("pre-build", "step-2");
            project.addHook("pre-build", "step-3");
            store.addProject(project);

            Path file = tempDir.resolve("hooks-order.json");
            exporter.export(file, null);

            StubProjectStore freshStore = new StubProjectStore();
            ProjectExporter freshExporter = new ProjectExporter(freshStore);
            freshExporter.importProjects(file);

            List<String> hooks = freshStore.getSaved().get(0).hooks().get("pre-build");
            assertEquals(List.of("step-1", "step-2", "step-3"), hooks);
        }

        @Test
        @DisplayName("all project types survive round trip")
        void allProjectTypes() throws IOException {
            for (ProjectType type : ProjectType.values()) {
                String name = type.name().toLowerCase();
                store.addProject(new Project(name, Paths.get("/tmp/" + name), type));
            }

            Path file = tempDir.resolve("all-types.json");
            exporter.export(file, null);

            StubProjectStore freshStore = new StubProjectStore();
            ProjectExporter freshExporter = new ProjectExporter(freshStore);
            ImportResult result = freshExporter.importProjects(file);

            assertEquals(ProjectType.values().length, result.imported());
        }
    }
}
