package pm.telemetry;

import com.google.gson.Gson;
import pm.util.Constants;

import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * HTTP client for sending telemetry events to PostHog.
 *
 * All sends are fire-and-forget on a background thread:
 * - 3-second timeout (never blocks the CLI)
 * - All exceptions swallowed silently
 * - If the CLI exits before send completes, the event is lost (acceptable)
 *
 * @author SoftDryzz
 * @version 1.8.0
 * @since 1.8.0
 */
public final class TelemetryClient {

    private static final int TIMEOUT_MS = 3000;
    private static final Gson GSON = new Gson();
    private static volatile Thread pendingSender;

    private TelemetryClient() {
        throw new AssertionError("TelemetryClient cannot be instantiated");
    }

    /**
     * Sends a telemetry event to PostHog on a background thread.
     *
     * @param event      The event to send
     * @param apiKey     PostHog project API key
     * @param distinctId Anonymous user UUID
     */
    public static void send(TelemetryEvent event, String apiKey, String distinctId) {
        Thread sender = new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection)
                        URI.create(Constants.POSTHOG_ENDPOINT).toURL().openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setDoOutput(true);

                String json = GSON.toJson(event.toPostHogPayload(apiKey, distinctId));
                conn.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception ignored) {
                // Telemetry NEVER blocks or crashes the CLI
            } finally {
                pendingSender = null;
            }
        }, "pm-telemetry");
        sender.setDaemon(true);
        pendingSender = sender;
        sender.start();
    }

    /**
     * Waits for any pending telemetry send to complete.
     * Called at the end of main() to ensure the event is delivered
     * before the JVM exits.
     *
     * @param timeoutMs Maximum time to wait in milliseconds
     */
    public static void flush(long timeoutMs) {
        Thread sender = pendingSender;
        if (sender != null && sender.isAlive()) {
            try {
                sender.join(timeoutMs);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
