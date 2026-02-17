package pm.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PathAdapter")
class PathAdapterTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Path.class, new PathAdapter())
            .create();

    @Test
    @DisplayName("Serializes Path to JSON string")
    void serializesPath() {
        Path path = Paths.get("/home/user/project");
        String json = gson.toJson(path, Path.class);

        assertNotNull(json);
        assertTrue(json.contains("home"));
        assertTrue(json.contains("project"));
    }

    @Test
    @DisplayName("Deserializes JSON string to Path")
    void deserializesPath() {
        Path result = gson.fromJson("\"/home/user/project\"", Path.class);

        assertEquals(Paths.get("/home/user/project"), result);
    }

    @Test
    @DisplayName("Roundtrip serialization preserves value")
    void roundtripWorks() {
        Path original = Paths.get("/home/user/my-project");
        String json = gson.toJson(original, Path.class);
        Path restored = gson.fromJson(json, Path.class);

        assertEquals(original, restored);
    }

    @Test
    @DisplayName("Serializes null as JSON null")
    void serializesNull() {
        String json = gson.toJson(null, Path.class);
        assertEquals("null", json);
    }
}
