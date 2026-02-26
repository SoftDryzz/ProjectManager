package pm.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pm.core.Project;
import pm.detector.ProjectType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectStore")
class ProjectStoreTest {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Project createTestProject(String name, String path, ProjectType type) {
        Project project = new Project(name, Paths.get(path), type);
        project.addCommand("build", "gradle build");
        project.addCommand("run", "gradle run");
        project.addEnvVar("PORT", "8080");
        return project;
    }

    // ============================================================
    // JSON SERIALIZATION / DESERIALIZATION (DTO logic)
    // ============================================================

    @Test
    @DisplayName("Project serializes to JSON with correct structure")
    void projectSerializesToJson() {
        Project project = createTestProject("api-server", "/home/user/api", ProjectType.GRADLE);

        // Simulate what ProjectStore.save() does internally
        Map<String, Object> dto = new HashMap<>();
        dto.put("name", project.name());
        dto.put("path", project.path().toString());
        dto.put("type", project.type().name());
        dto.put("commands", new HashMap<>(project.commands()));
        dto.put("envVars", new HashMap<>(project.envVars()));
        dto.put("lastModified", project.lastModified().toString());

        String json = gson.toJson(Map.of("api-server", dto));

        assertNotNull(json);
        assertTrue(json.contains("api-server"));
        assertTrue(json.contains("GRADLE"));
        assertTrue(json.contains("gradle build"));
        assertTrue(json.contains("8080"));
    }

    @Test
    @DisplayName("JSON deserializes back to correct values")
    void jsonDeserializesCorrectly() {
        String json = """
                {
                  "web-app": {
                    "name": "web-app",
                    "path": "/home/user/webapp",
                    "type": "NODEJS",
                    "commands": {
                      "build": "npm run build",
                      "test": "npm test"
                    },
                    "envVars": {
                      "NODE_ENV": "development"
                    },
                    "lastModified": "2025-01-15T10:30:00Z"
                  }
                }
                """;

        TypeToken<Map<String, Map<String, Object>>> typeToken =
                new TypeToken<>() {};
        Map<String, Map<String, Object>> dtos = gson.fromJson(json, typeToken.getType());

        assertNotNull(dtos);
        assertTrue(dtos.containsKey("web-app"));

        Map<String, Object> dto = dtos.get("web-app");
        assertEquals("web-app", dto.get("name"));
        assertEquals("/home/user/webapp", dto.get("path"));
        assertEquals("NODEJS", dto.get("type"));
    }

    @Test
    @DisplayName("Multiple projects serialize and deserialize correctly")
    void multipleProjectsRoundtrip() {
        Project p1 = createTestProject("backend", "/home/user/backend", ProjectType.MAVEN);
        Project p2 = createTestProject("frontend", "/home/user/frontend", ProjectType.NODEJS);
        p2.addCommand("build", "npm run build");
        p2.addCommand("run", "npm start");

        // Build DTOs map
        Map<String, Map<String, Object>> dtosOut = new HashMap<>();
        for (Project p : new Project[]{p1, p2}) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("name", p.name());
            dto.put("path", p.path().toString());
            dto.put("type", p.type().name());
            dto.put("commands", new HashMap<>(p.commands()));
            dto.put("envVars", new HashMap<>(p.envVars()));
            dto.put("lastModified", p.lastModified().toString());
            dtosOut.put(p.name(), dto);
        }

        String json = gson.toJson(dtosOut);

        // Deserialize
        TypeToken<Map<String, Map<String, Object>>> typeToken =
                new TypeToken<>() {};
        Map<String, Map<String, Object>> dtosIn = gson.fromJson(json, typeToken.getType());

        assertEquals(2, dtosIn.size());
        assertTrue(dtosIn.containsKey("backend"));
        assertTrue(dtosIn.containsKey("frontend"));
        assertEquals("MAVEN", dtosIn.get("backend").get("type"));
        assertEquals("NODEJS", dtosIn.get("frontend").get("type"));
    }

    @Test
    @DisplayName("Empty JSON map returns empty map")
    void emptyJsonReturnsEmptyMap() {
        String json = "{}";

        TypeToken<Map<String, Map<String, Object>>> typeToken =
                new TypeToken<>() {};
        Map<String, Map<String, Object>> result = gson.fromJson(json, typeToken.getType());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("ProjectStore can be instantiated")
    void canInstantiate() {
        ProjectStore store = new ProjectStore();
        assertNotNull(store);
    }

    @Test
    @DisplayName("ProjectType valueOf works for all serialized types")
    void projectTypeValueOfWorks() {
        for (ProjectType type : ProjectType.values()) {
            String serialized = type.name();
            ProjectType deserialized = ProjectType.valueOf(serialized);
            assertEquals(type, deserialized);
        }
    }

    @Test
    @DisplayName("Path roundtrip preserves value")
    void pathRoundtrip() {
        Path original = Paths.get("/home/user/my-project");
        String serialized = original.toString();
        Path deserialized = Paths.get(serialized);

        assertEquals(original, deserialized);
    }

    // ============================================================
    // HOOKS SERIALIZATION TESTS
    // ============================================================

    @Test
    @DisplayName("Project with hooks serializes to JSON with hooks field")
    void projectWithHooksSerializesToJson() {
        Project project = createTestProject("api", "/home/user/api", ProjectType.GRADLE);
        project.addHook("pre-build", "npm run lint");
        project.addHook("post-build", "echo done");

        // Use ProjectDTO.fromProject
        ProjectStore.ProjectDTO dto = ProjectStore.ProjectDTO.fromProject(project);

        assertNotNull(dto.hooks);
        assertEquals(2, dto.hooks.size());
        assertTrue(dto.hooks.containsKey("pre-build"));
        assertTrue(dto.hooks.containsKey("post-build"));
        assertEquals(List.of("npm run lint"), dto.hooks.get("pre-build"));
        assertEquals(List.of("echo done"), dto.hooks.get("post-build"));
    }

    @Test
    @DisplayName("ProjectDTO with hooks deserializes back to Project correctly")
    void projectDtoWithHooksRoundtrip() {
        Project original = createTestProject("api", "/home/user/api", ProjectType.GRADLE);
        original.addHook("pre-build", "npm run lint");
        original.addHook("pre-build", "npm run format");
        original.addHook("post-test", "echo tests done");

        // Project → DTO → JSON → DTO → Project
        ProjectStore.ProjectDTO dto = ProjectStore.ProjectDTO.fromProject(original);
        String json = gson.toJson(Map.of("api", dto));

        TypeToken<Map<String, ProjectStore.ProjectDTO>> typeToken = new TypeToken<>() {};
        Map<String, ProjectStore.ProjectDTO> deserialized = gson.fromJson(json, typeToken.getType());

        ProjectStore.ProjectDTO restoredDto = deserialized.get("api");
        List<String> warnings = new ArrayList<>();
        Project restored = restoredDto.toProjectSafe("api", warnings);

        assertNotNull(restored);
        assertTrue(warnings.isEmpty());
        assertEquals(2, restored.getHooks("pre-build").size());
        assertEquals("npm run lint", restored.getHooks("pre-build").get(0));
        assertEquals("npm run format", restored.getHooks("pre-build").get(1));
        assertEquals(1, restored.getHooks("post-test").size());
        assertEquals(3, restored.hookCount());
    }

    @Test
    @DisplayName("ProjectDTO without hooks field deserializes safely")
    void projectDtoWithoutHooksDeserializesSafely() {
        // Simulates loading a projects.json from before hooks were added
        String json = """
                {
                  "old-project": {
                    "name": "old-project",
                    "path": "/home/user/old",
                    "type": "MAVEN",
                    "commands": {"build": "mvn package"},
                    "envVars": {},
                    "lastModified": "2025-01-01T00:00:00Z"
                  }
                }
                """;

        TypeToken<Map<String, ProjectStore.ProjectDTO>> typeToken = new TypeToken<>() {};
        Map<String, ProjectStore.ProjectDTO> deserialized = gson.fromJson(json, typeToken.getType());

        List<String> warnings = new ArrayList<>();
        Project project = deserialized.get("old-project").toProjectSafe("old-project", warnings);

        assertNotNull(project);
        assertTrue(warnings.isEmpty());
        assertEquals(0, project.hookCount());
        assertFalse(project.hasHooks());
    }

    @Test
    @DisplayName("Project with multiple hooks per slot preserves order")
    void hooksPreserveOrder() {
        Project project = createTestProject("api", "/home/user/api", ProjectType.GRADLE);
        project.addHook("pre-build", "first");
        project.addHook("pre-build", "second");
        project.addHook("pre-build", "third");

        ProjectStore.ProjectDTO dto = ProjectStore.ProjectDTO.fromProject(project);
        String json = gson.toJson(Map.of("api", dto));

        TypeToken<Map<String, ProjectStore.ProjectDTO>> typeToken = new TypeToken<>() {};
        Map<String, ProjectStore.ProjectDTO> deserialized = gson.fromJson(json, typeToken.getType());

        List<String> warnings = new ArrayList<>();
        Project restored = deserialized.get("api").toProjectSafe("api", warnings);

        List<String> hooks = restored.getHooks("pre-build");
        assertEquals(3, hooks.size());
        assertEquals("first", hooks.get(0));
        assertEquals("second", hooks.get(1));
        assertEquals("third", hooks.get(2));
    }
}
