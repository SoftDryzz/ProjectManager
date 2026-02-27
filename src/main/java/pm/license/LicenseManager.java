package pm.license;

import pm.license.LicenseValidator.ValidationResult;

import java.nio.file.Path;

/**
 * Main license facade. Handles activation, deactivation,
 * and edition detection.
 *
 * Usage from ProjectManager:
 *   LicenseManager.getEditionLabel()          // for banner
 *   LicenseManager.activate(rawKey)           // pm license activate
 *   LicenseManager.deactivate()               // pm license deactivate
 *
 * @author SoftDryzz
 * @version 1.9.0
 * @since 1.9.0
 */
public final class LicenseManager {

    private static ValidationResult cachedResult;
    private static boolean loaded = false;

    private LicenseManager() {
        throw new AssertionError("LicenseManager cannot be instantiated");
    }

    /**
     * Result of a license activation attempt.
     *
     * @param success Whether activation succeeded
     * @param message Human-readable result message
     * @param key     The decoded license key (null on failure)
     */
    public record ActivationResult(boolean success, String message, LicenseKey key) {}

    /**
     * Activates a license key. Validates the signature, checks expiry,
     * and persists to disk if valid.
     *
     * @param rawKey The full license key string
     * @return Activation result with success/failure and message
     */
    public static ActivationResult activate(String rawKey) {
        ValidationResult result = LicenseValidator.validate(rawKey);
        if (result.valid()) {
            LicenseStore store = new LicenseStore();
            store.setKey(rawKey);
            store.save();
            cachedResult = result;
            loaded = true;
            return new ActivationResult(true, "License activated successfully", result.key());
        }
        return new ActivationResult(false, result.error(), result.key());
    }

    /**
     * Activates a license key using custom paths and public key (for testing).
     */
    static ActivationResult activate(String rawKey, Path licenseFile, String publicKey) {
        ValidationResult result = LicenseValidator.validate(rawKey, publicKey);
        if (result.valid()) {
            LicenseStore store = new LicenseStore();
            store.setKey(rawKey);
            store.save(licenseFile);
            cachedResult = result;
            loaded = true;
            return new ActivationResult(true, "License activated successfully", result.key());
        }
        return new ActivationResult(false, result.error(), result.key());
    }

    /**
     * Deactivates the current license. Deletes license.json
     * and reverts to Community Edition.
     */
    public static void deactivate() {
        LicenseStore.delete();
        cachedResult = null;
        loaded = true;
    }

    /**
     * Deactivates using a custom path (for testing).
     */
    static void deactivate(Path licenseFile) {
        LicenseStore.delete(licenseFile);
        cachedResult = null;
        loaded = true;
    }

    /**
     * Returns the edition label for display (e.g., in the banner).
     *
     * @return "Pro" if a valid Pro license is active, "Community Edition" otherwise
     */
    public static String getEditionLabel() {
        ensureLoaded();
        if (cachedResult != null && cachedResult.valid() && cachedResult.key().isPro()) {
            return "Pro";
        }
        return "Community Edition";
    }

    /**
     * Returns whether a valid Pro license is active.
     */
    public static boolean isPro() {
        ensureLoaded();
        return cachedResult != null && cachedResult.valid() && cachedResult.key().isPro();
    }

    /**
     * Returns the current license key info, or null if no valid license.
     */
    public static LicenseKey getLicenseInfo() {
        ensureLoaded();
        if (cachedResult != null && cachedResult.valid()) {
            return cachedResult.key();
        }
        return null;
    }

    /**
     * Returns the current validation result (for displaying errors).
     */
    public static ValidationResult getValidationResult() {
        ensureLoaded();
        return cachedResult;
    }

    /**
     * Resets license state (for testing).
     */
    static void reset() {
        cachedResult = null;
        loaded = false;
    }

    /**
     * Lazy-loads the license from disk and validates it.
     * Called once per session.
     */
    private static void ensureLoaded() {
        if (loaded) {
            return;
        }
        try {
            LicenseStore store = LicenseStore.load();
            if (store.hasKey()) {
                cachedResult = LicenseValidator.validate(store.getKey());
            }
        } catch (Exception e) {
            cachedResult = null;
        }
        loaded = true;
    }
}
