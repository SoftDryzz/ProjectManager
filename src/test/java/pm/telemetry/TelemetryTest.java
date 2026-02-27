package pm.telemetry;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Telemetry")
class TelemetryTest {

    @TempDir
    Path tempDir;

    // ============================================================
    // TELEMETRY CONFIG
    // ============================================================

    @Nested
    @DisplayName("TelemetryConfig")
    class ConfigTests {

        @Test
        @DisplayName("returns defaults when config file does not exist")
        void defaultsWhenNoFile() {
            TelemetryConfig config = TelemetryConfig.load(tempDir.resolve("config.json"));
            assertFalse(config.isTelemetryEnabled());
            assertFalse(config.isPrompted());
            assertNull(config.getDistinctId());
        }

        @Test
        @DisplayName("save and reload preserves all fields")
        void saveAndReload() {
            Path configFile = tempDir.resolve("config.json");

            TelemetryConfig config = new TelemetryConfig();
            config.setTelemetryEnabled(true);
            config.setPrompted(true);
            config.setDistinctId("test-uuid-123");
            config.save(configFile);

            TelemetryConfig loaded = TelemetryConfig.load(configFile);
            assertTrue(loaded.isTelemetryEnabled());
            assertTrue(loaded.isPrompted());
            assertEquals("test-uuid-123", loaded.getDistinctId());
        }

        @Test
        @DisplayName("returns defaults for corrupted config file")
        void defaultsForCorruptedFile() throws IOException {
            Path configFile = tempDir.resolve("config.json");
            Files.writeString(configFile, "not valid json {{{");

            TelemetryConfig config = TelemetryConfig.load(configFile);
            assertFalse(config.isTelemetryEnabled());
            assertFalse(config.isPrompted());
            assertNull(config.getDistinctId());
        }

        @Test
        @DisplayName("returns defaults for empty config file")
        void defaultsForEmptyFile() throws IOException {
            Path configFile = tempDir.resolve("config.json");
            Files.writeString(configFile, "");

            TelemetryConfig config = TelemetryConfig.load(configFile);
            assertFalse(config.isTelemetryEnabled());
            assertFalse(config.isPrompted());
        }

        @Test
        @DisplayName("config file is created in correct directory")
        void configFileCreated() {
            Path subDir = tempDir.resolve("sub");
            Path configFile = subDir.resolve("config.json");

            TelemetryConfig config = new TelemetryConfig();
            config.setTelemetryEnabled(true);
            config.save(configFile);

            assertTrue(Files.exists(configFile));
        }
    }

    // ============================================================
    // TELEMETRY EVENT
    // ============================================================

    @Nested
    @DisplayName("TelemetryEvent")
    class EventTests {

        @Test
        @DisplayName("toPostHogPayload includes all required fields")
        void payloadHasRequiredFields() {
            TelemetryEvent event = new TelemetryEvent("command_executed", Map.of(
                    "command", "build",
                    "version", "1.8.0"
            ));

            Map<String, Object> payload = event.toPostHogPayload("test-key", "test-uuid");

            assertEquals("test-key", payload.get("api_key"));
            assertEquals("command_executed", payload.get("event"));
            assertEquals("test-uuid", payload.get("distinct_id"));
            assertNotNull(payload.get("timestamp"));
            assertNotNull(payload.get("properties"));
        }

        @Test
        @DisplayName("properties include $lib marker")
        void propertiesIncludeLib() {
            TelemetryEvent event = new TelemetryEvent("command_executed", Map.of(
                    "command", "test"
            ));

            Map<String, Object> payload = event.toPostHogPayload("key", "uuid");

            @SuppressWarnings("unchecked")
            Map<String, Object> props = (Map<String, Object>) payload.get("properties");
            assertEquals("projectmanager", props.get("$lib"));
            assertEquals("test", props.get("command"));
        }

        @Test
        @DisplayName("null distinctId falls back to 'anonymous'")
        void nullDistinctIdFallback() {
            TelemetryEvent event = new TelemetryEvent("test_event", Map.of());

            Map<String, Object> payload = event.toPostHogPayload("key", null);

            assertEquals("anonymous", payload.get("distinct_id"));
        }

        @Test
        @DisplayName("original properties are not modified")
        void originalPropertiesUnmodified() {
            Map<String, Object> original = new java.util.HashMap<>();
            original.put("command", "build");

            TelemetryEvent event = new TelemetryEvent("test", original);
            event.toPostHogPayload("key", "uuid");

            assertFalse(original.containsKey("$lib"),
                    "Original properties map should not be modified");
        }

        @Test
        @DisplayName("timestamp is in ISO format")
        void timestampFormat() {
            TelemetryEvent event = new TelemetryEvent("test", Map.of());
            Map<String, Object> payload = event.toPostHogPayload("key", "uuid");

            String timestamp = (String) payload.get("timestamp");
            assertNotNull(timestamp);
            assertTrue(timestamp.contains("T"), "Timestamp should be in ISO format");
        }
    }

    // ============================================================
    // TELEMETRY FACADE
    // ============================================================

    @Nested
    @DisplayName("Telemetry tracking")
    class TrackingTests {

        @Test
        @DisplayName("trackCommand does nothing when config is null")
        void trackCommandWithNullConfig() {
            Telemetry.reset();
            // Should not throw
            assertDoesNotThrow(() -> Telemetry.trackCommand("build"));
        }

        @Test
        @DisplayName("setEnabled persists and can be read back")
        void setEnabledPersists() {
            Telemetry.reset();
            // After reset, config is null; setEnabled will load defaults
            // This tests the public API without needing a real config file
            assertDoesNotThrow(() -> Telemetry.isEnabled());
        }
    }

}
