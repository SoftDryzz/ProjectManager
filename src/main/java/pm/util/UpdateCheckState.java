package pm.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

/**
 * Result of the last update check, persisted so GitHub is queried at most
 * once a day instead of on every command.
 *
 * <p>Stored at {@code ~/.projectmanager/update-check.json}, separate from
 * {@code config.json}, which is owned by the telemetry settings.
 *
 * @param lastChecked   ISO-8601 instant of the last check attempt, or null if never checked
 * @param succeeded     whether that attempt reached GitHub and parsed a version
 * @param latestVersion latest release version seen (without 'v'), or null if unknown
 * @author SoftDryzz
 * @since 2.0.1
 */
record UpdateCheckState(String lastChecked, boolean succeeded, String latestVersion) {

    /** Wait after a successful check before asking GitHub again. */
    static final Duration SUCCESS_INTERVAL = Duration.ofHours(24);

    /** Wait after a failed check (offline, timeout...) before retrying. */
    static final Duration FAILURE_INTERVAL = Duration.ofHours(1);

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** State for a user who has never checked. */
    static UpdateCheckState never() {
        return new UpdateCheckState(null, false, null);
    }

    /** State after a check that returned {@code version}. */
    static UpdateCheckState success(Instant now, String version) {
        return new UpdateCheckState(now.toString(), true, version);
    }

    /** State after a failed check; keeps the last version already known. */
    UpdateCheckState failure(Instant now) {
        return new UpdateCheckState(now.toString(), false, latestVersion);
    }

    /**
     * Whether GitHub should be queried now. A missing, unreadable or future
     * timestamp (clock moved back) always triggers a check.
     */
    boolean isCheckDue(Instant now) {
        if (lastChecked == null) {
            return true;
        }
        Instant last;
        try {
            last = Instant.parse(lastChecked);
        } catch (DateTimeParseException e) {
            return true;
        }
        if (last.isAfter(now)) {
            return true;
        }
        Duration interval = succeeded ? SUCCESS_INTERVAL : FAILURE_INTERVAL;
        return !now.isBefore(last.plus(interval));
    }

    /**
     * Loads the state from disk. A missing or corrupt file counts as never
     * checked, so the worst case is one extra network request.
     */
    static UpdateCheckState load(Path file) {
        if (!Files.exists(file)) {
            return never();
        }
        try {
            UpdateCheckState state = GSON.fromJson(Files.readString(file), UpdateCheckState.class);
            return state != null ? state : never();
        } catch (Exception e) {
            return never();
        }
    }

    /** Saves the state with an atomic write. Failures are ignored: it is only a cache. */
    void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(temp, GSON.toJson(this));
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Non-critical: the next run simply checks again
        }
    }
}
