package pm.tracking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import pm.util.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

/**
 * Persists command execution stats to stats.json.
 * Thread-safe for single-JVM usage (synchronized methods).
 *
 * @author SoftDryzz
 * @version 2.0.0
 * @since 2.0.0
 */
public class StatsStore {

    private final Path statsFile;
    private final Path tempFile;
    private final Gson gson;

    private static final Type STATS_TYPE =
            new TypeToken<Map<String, Map<String, List<StatsRecord>>>>() {}.getType();

    public StatsStore() {
        this(Constants.STATS_FILE);
    }

    public StatsStore(Path statsFile) {
        this.statsFile = statsFile;
        this.tempFile = statsFile.resolveSibling("stats.json.tmp");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    /**
     * Record a command execution.
     *
     * @param projectName the project name
     * @param command     the command type (build, test, run)
     * @param durationMs  execution time in milliseconds
     * @param success     whether the command succeeded
     */
    public synchronized void record(String projectName, String command, long durationMs, boolean success) {
        try {
            Map<String, Map<String, List<StatsRecord>>> allStats = load();

            Map<String, List<StatsRecord>> projectStats =
                    allStats.computeIfAbsent(projectName, k -> new HashMap<>());
            List<StatsRecord> records =
                    projectStats.computeIfAbsent(command, k -> new ArrayList<>());

            records.add(new StatsRecord(durationMs, success, Instant.now().toString()));

            while (records.size() > Constants.STATS_MAX_ENTRIES) {
                records.remove(0);
            }

            save(allStats);
        } catch (IOException e) {
            // Stats are non-critical — silently ignore write failures
        }
    }

    /**
     * Get stats for a specific project.
     *
     * @param projectName the project name
     * @return map of command to records, or null if project not found
     */
    public Map<String, List<StatsRecord>> getStats(String projectName) {
        Map<String, Map<String, List<StatsRecord>>> allStats = load();
        return allStats.get(projectName);
    }

    /**
     * Get stats for all projects.
     *
     * @return full stats map (project to command to records)
     */
    public Map<String, Map<String, List<StatsRecord>>> getAllStats() {
        return load();
    }

    private Map<String, Map<String, List<StatsRecord>>> load() {
        if (!Files.exists(statsFile)) {
            return new HashMap<>();
        }
        try {
            String json = Files.readString(statsFile);
            if (json == null || json.isBlank()) {
                return new HashMap<>();
            }
            Map<String, Map<String, List<StatsRecord>>> result = gson.fromJson(json, STATS_TYPE);
            return result != null ? result : new HashMap<>();
        } catch (IOException | JsonSyntaxException e) {
            return new HashMap<>();
        }
    }

    private void save(Map<String, Map<String, List<StatsRecord>>> allStats) throws IOException {
        Path parent = statsFile.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        String json = gson.toJson(allStats, STATS_TYPE);
        Files.writeString(tempFile, json);

        try {
            Files.move(tempFile, statsFile,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, statsFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
