package pm.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InstantAdapter")
class InstantAdapterTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantAdapter())
            .create();

    @Test
    @DisplayName("Serializes Instant to JSON string")
    void serializesInstant() {
        Instant instant = Instant.parse("2025-06-15T10:30:00Z");
        String json = gson.toJson(instant);

        assertEquals("\"2025-06-15T10:30:00Z\"", json);
    }

    @Test
    @DisplayName("Deserializes JSON string to Instant")
    void deserializesInstant() {
        Instant result = gson.fromJson("\"2025-06-15T10:30:00Z\"", Instant.class);

        assertEquals(Instant.parse("2025-06-15T10:30:00Z"), result);
    }

    @Test
    @DisplayName("Roundtrip serialization preserves value")
    void roundtripWorks() {
        Instant original = Instant.now();
        String json = gson.toJson(original);
        Instant restored = gson.fromJson(json, Instant.class);

        assertEquals(original, restored);
    }

    @Test
    @DisplayName("Serializes null as JSON null")
    void serializesNull() {
        String json = gson.toJson(null, Instant.class);
        assertEquals("null", json);
    }
}
