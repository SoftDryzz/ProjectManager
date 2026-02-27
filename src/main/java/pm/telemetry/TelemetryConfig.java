package pm.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pm.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Manages telemetry configuration stored at ~/.projectmanager/config.json.
 *
 * Handles:
 * - Loading/saving telemetry preferences (enabled, distinctId, prompted)
 * - Atomic writes to prevent corruption
 * - Default values for first-time users
 *
 * @author SoftDryzz
 * @version 1.8.0
 * @since 1.8.0
 */
public final class TelemetryConfig {

    private boolean telemetryEnabled;
    private String distinctId;
    private boolean prompted;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public TelemetryConfig() {
        this.telemetryEnabled = false;
        this.distinctId = null;
        this.prompted = false;
    }

    /**
     * Loads the telemetry config from disk.
     * Returns default config if file doesn't exist or is corrupt.
     */
    public static TelemetryConfig load() {
        Path configFile = Constants.CONFIG_FILE;
        if (!Files.exists(configFile)) {
            return new TelemetryConfig();
        }
        try {
            String json = Files.readString(configFile);
            TelemetryConfig config = GSON.fromJson(json, TelemetryConfig.class);
            return config != null ? config : new TelemetryConfig();
        } catch (Exception e) {
            return new TelemetryConfig();
        }
    }

    /**
     * Loads telemetry config from a custom path (for testing).
     */
    public static TelemetryConfig load(Path configFile) {
        if (!Files.exists(configFile)) {
            return new TelemetryConfig();
        }
        try {
            String json = Files.readString(configFile);
            TelemetryConfig config = GSON.fromJson(json, TelemetryConfig.class);
            return config != null ? config : new TelemetryConfig();
        } catch (Exception e) {
            return new TelemetryConfig();
        }
    }

    /**
     * Saves config to disk using atomic write (temp file + move).
     */
    public void save() {
        save(Constants.CONFIG_FILE);
    }

    /**
     * Saves config to a custom path (for testing).
     */
    public void save(Path configFile) {
        try {
            Files.createDirectories(configFile.getParent());
            Path tempFile = configFile.resolveSibling("config.json.tmp");
            String json = GSON.toJson(this);
            Files.writeString(tempFile, json);
            Files.move(tempFile, configFile,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Config save failure is non-critical — telemetry will re-prompt next time
        }
    }

    public boolean isTelemetryEnabled() {
        return telemetryEnabled;
    }

    public void setTelemetryEnabled(boolean telemetryEnabled) {
        this.telemetryEnabled = telemetryEnabled;
    }

    public String getDistinctId() {
        return distinctId;
    }

    public void setDistinctId(String distinctId) {
        this.distinctId = distinctId;
    }

    public boolean isPrompted() {
        return prompted;
    }

    public void setPrompted(boolean prompted) {
        this.prompted = prompted;
    }
}
