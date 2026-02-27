package pm.license;

/**
 * Represents a decoded license key payload.
 *
 * @param holder          Organization or individual name
 * @param edition         "PRO" or "COMMUNITY"
 * @param issued          ISO date string (e.g., "2026-02-27")
 * @param expires         ISO date string, or null for perpetual licenses
 * @param id              Unique license UUID
 * @param maxActivations  Maximum number of machines this license can be activated on
 *
 * @author SoftDryzz
 * @version 1.9.0
 * @since 1.9.0
 */
public record LicenseKey(
        String holder,
        String edition,
        String issued,
        String expires,
        String id,
        Integer maxActivations
) {

    public static final int DEFAULT_MAX_ACTIVATIONS = 2;

    public static final String EDITION_PRO = "PRO";
    public static final String EDITION_COMMUNITY = "COMMUNITY";

    /**
     * Returns true if this license represents a Pro edition.
     */
    public boolean isPro() {
        return EDITION_PRO.equalsIgnoreCase(edition);
    }
}
