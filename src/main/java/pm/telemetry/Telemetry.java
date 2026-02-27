package pm.telemetry;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import pm.util.Constants;

import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Main telemetry facade. Handles initialization, consent prompting,
 * and event tracking.
 *
 * Usage from ProjectManager.main():
 *   Telemetry.init();                    // after UpdateChecker
 *   Telemetry.trackCommand("build");     // after command dispatch
 *
 * @author SoftDryzz
 * @version 1.8.0
 * @since 1.8.0
 */
public final class Telemetry {

    private static TelemetryConfig config;

    private Telemetry() {
        throw new AssertionError("Telemetry cannot be instantiated");
    }

    /**
     * Initializes telemetry. Call once at startup.
     * Loads config and shows first-run consent prompt if needed.
     */
    public static void init() {
        try {
            config = TelemetryConfig.load();
            if (!config.isPrompted()) {
                promptConsent();
            }
        } catch (Exception e) {
            // Telemetry init failure is non-critical
            config = new TelemetryConfig();
            config.setPrompted(true);
        }
    }

    /**
     * Tracks a command execution event (fire-and-forget).
     *
     * @param command     The command name (e.g., "build", "test")
     * @param success     Whether the command completed without exception
     * @param elapsedMs   Execution time in milliseconds
     */
    public static void trackCommand(String command, boolean success, long elapsedMs) {
        if (config == null || !config.isTelemetryEnabled()) {
            return;
        }
        try {
            Map<String, Object> props = new HashMap<>();
            props.put("command", command);
            props.put("version", Constants.VERSION);
            props.put("os", System.getProperty("os.name"));
            props.put("os_version", System.getProperty("os.version"));
            props.put("java_version", System.getProperty("java.version"));
            props.put("arch", System.getProperty("os.arch"));
            props.put("locale", java.util.Locale.getDefault().toString());
            props.put("terminal", System.getenv("TERM") != null ? System.getenv("TERM") : "unknown");
            props.put("project_count", getProjectCount());
            props.put("project_types", getProjectTypes());
            props.put("success", success);
            props.put("execution_ms", elapsedMs);

            TelemetryEvent event = new TelemetryEvent("command_executed", props);
            TelemetryClient.send(event, Constants.POSTHOG_KEY, config.getDistinctId());
        } catch (Exception ignored) {
            // Telemetry tracking failure is non-critical
        }
    }

    /**
     * Enables or disables telemetry. Generates a UUID if enabling for first time.
     */
    public static void setEnabled(boolean enabled) {
        if (config == null) {
            config = TelemetryConfig.load();
        }
        config.setTelemetryEnabled(enabled);
        if (enabled && config.getDistinctId() == null) {
            config.setDistinctId(UUID.randomUUID().toString());
        }
        config.setPrompted(true);
        config.save();
    }

    /**
     * Returns whether telemetry is currently enabled.
     */
    public static boolean isEnabled() {
        if (config == null) {
            config = TelemetryConfig.load();
        }
        return config.isTelemetryEnabled();
    }

    /**
     * First-run consent prompt. Opt-in only.
     * Skips prompt if no console is available (CI/CD, piped).
     */
    private static void promptConsent() {
        if (System.console() == null) {
            config.setPrompted(true);
            config.save();
            return;
        }

        System.out.println();
        System.out.println("  Help improve ProjectManager!");
        System.out.println("  Allow anonymous usage statistics? (version, OS, commands)");
        System.out.println("  No personal data is collected. Toggle anytime: pm config telemetry off");
        System.out.print("  Enable telemetry? (y/n): ");

        String response = System.console().readLine();
        boolean accepted = response != null && response.trim().toLowerCase().startsWith("y");

        config.setTelemetryEnabled(accepted);
        config.setPrompted(true);
        if (accepted) {
            config.setDistinctId(UUID.randomUUID().toString());
        }
        config.save();

        if (accepted) {
            System.out.println("  Thanks! Telemetry enabled. Disable anytime: pm config telemetry off");
        } else {
            System.out.println("  No problem. You can enable it later: pm config telemetry on");
        }
        System.out.println();
    }

    /**
     * Counts registered projects without depending on ProjectStore.
     * Reads projects.json directly and counts top-level keys.
     */
    private static int getProjectCount() {
        try {
            return loadProjectsMap().size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Extracts unique project types (e.g., ["Maven", "Rust", "Flutter"]).
     * No project names or paths — only the type field.
     */
    private static List<String> getProjectTypes() {
        try {
            Map<String, Map<String, Object>> projects = loadProjectsMap();
            return projects.values().stream()
                    .map(p -> p.getOrDefault("type", "UNKNOWN").toString())
                    .distinct()
                    .sorted()
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private static Map<String, Map<String, Object>> loadProjectsMap() {
        try {
            if (!Files.exists(Constants.PROJECTS_FILE)) {
                return Map.of();
            }
            String json = Files.readString(Constants.PROJECTS_FILE);
            Map<String, Map<String, Object>> projects = new Gson().fromJson(json,
                    new TypeToken<Map<String, Map<String, Object>>>() {}.getType());
            return projects != null ? projects : Map.of();
        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * Waits for any pending telemetry event to be sent.
     * Call at the end of main() to ensure delivery before JVM exit.
     */
    public static void flush() {
        if (config != null && config.isTelemetryEnabled()) {
            TelemetryClient.flush(2000);
        }
    }

    /**
     * Resets telemetry state (for testing).
     */
    static void reset() {
        config = null;
    }
}
