package pm.audit;

/**
 * Unified vulnerability severity levels.
 *
 * <p>Maps all ecosystem-specific severity strings to a common scale:
 * <ul>
 *   <li>{@code "critical"} → {@link #CRITICAL}</li>
 *   <li>{@code "high"} → {@link #HIGH}</li>
 *   <li>{@code "moderate"}, {@code "medium"} → {@link #MEDIUM}</li>
 *   <li>{@code "low"} → {@link #LOW}</li>
 * </ul>
 *
 * @author SoftDryzz
 * @version 1.6.3
 * @since 1.6.3
 */
public enum Severity {

    CRITICAL, HIGH, MEDIUM, LOW;

    /**
     * Maps an ecosystem-specific severity string to a unified {@code Severity}.
     *
     * <p>Case-insensitive. Returns {@link #MEDIUM} for null, blank, or unknown values.
     *
     * @param raw the raw severity string (e.g. {@code "moderate"}, {@code "High"})
     * @return the unified severity level
     */
    public static Severity from(String raw) {
        if (raw == null || raw.isBlank()) {
            return MEDIUM;
        }
        return switch (raw.trim().toLowerCase()) {
            case "critical" -> CRITICAL;
            case "high" -> HIGH;
            case "moderate", "medium" -> MEDIUM;
            case "low" -> LOW;
            default -> MEDIUM;
        };
    }
}
