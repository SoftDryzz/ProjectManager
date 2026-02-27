package pm.telemetry;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single telemetry event to be sent to PostHog.
 *
 * @param event      The event name (e.g., "command_executed")
 * @param properties Event properties (command, version, OS, etc.)
 *
 * @author SoftDryzz
 * @version 1.8.0
 * @since 1.8.0
 */
public record TelemetryEvent(String event, Map<String, Object> properties) {

    /**
     * Builds the full JSON payload that PostHog expects.
     *
     * @param apiKey     PostHog project API key
     * @param distinctId Anonymous user UUID
     * @return Map representing the JSON body for POST /capture/
     */
    public Map<String, Object> toPostHogPayload(String apiKey, String distinctId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("api_key", apiKey);
        payload.put("event", event);
        payload.put("distinct_id", distinctId != null ? distinctId : "anonymous");
        payload.put("timestamp", Instant.now().toString());

        Map<String, Object> props = new HashMap<>(properties);
        props.put("$lib", "projectmanager");
        props.put("$ip", null);
        payload.put("properties", props);

        return payload;
    }
}
