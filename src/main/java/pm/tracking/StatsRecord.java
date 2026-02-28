package pm.tracking;

/**
 * A single execution time record for a command.
 *
 * @param durationMs execution time in milliseconds
 * @param success    whether the command exited with code 0
 * @param timestamp  ISO-8601 UTC timestamp of the execution
 *
 * @author SoftDryzz
 * @version 2.0.0
 * @since 2.0.0
 */
public record StatsRecord(long durationMs, boolean success, String timestamp) {

    /**
     * Formats the duration in a readable way.
     *
     * @return string with format "Xs" or "Xm Ys"
     */
    public String formattedDuration() {
        long seconds = durationMs / 1000;
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return minutes + "m " + remainingSeconds + "s";
    }
}
