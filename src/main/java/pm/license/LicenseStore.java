package pm.license;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import pm.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Manages license key persistence at ~/.projectmanager/license.json.
 *
 * Handles:
 * - Loading/saving the raw license key string
 * - Atomic writes to prevent corruption
 * - Deletion for license deactivation
 *
 * @author SoftDryzz
 * @version 1.9.0
 * @since 1.9.0
 */
public final class LicenseStore {

    private String key;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public LicenseStore() {
        this.key = null;
    }

    /**
     * Loads the license store from the default path.
     */
    public static LicenseStore load() {
        return load(Constants.LICENSE_FILE);
    }

    /**
     * Loads the license store from a custom path (for testing).
     */
    public static LicenseStore load(Path licenseFile) {
        if (!Files.exists(licenseFile)) {
            return new LicenseStore();
        }
        try {
            String json = Files.readString(licenseFile);
            LicenseStore store = GSON.fromJson(json, LicenseStore.class);
            return store != null ? store : new LicenseStore();
        } catch (Exception e) {
            return new LicenseStore();
        }
    }

    /**
     * Saves the license store to the default path.
     */
    public void save() {
        save(Constants.LICENSE_FILE);
    }

    /**
     * Saves the license store to a custom path (for testing).
     * Uses atomic write (temp file + move).
     */
    public void save(Path licenseFile) {
        try {
            Files.createDirectories(licenseFile.getParent());
            Path tempFile = licenseFile.resolveSibling("license.json.tmp");
            String json = GSON.toJson(this);
            Files.writeString(tempFile, json);
            Files.move(tempFile, licenseFile,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // License save failure is non-critical
        }
    }

    /**
     * Deletes the license file (for deactivation).
     */
    public static void delete() {
        delete(Constants.LICENSE_FILE);
    }

    /**
     * Deletes a license file at a custom path (for testing).
     */
    public static void delete(Path licenseFile) {
        try {
            Files.deleteIfExists(licenseFile);
        } catch (IOException e) {
            // Deletion failure is non-critical
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean hasKey() {
        return key != null && !key.isBlank();
    }
}
