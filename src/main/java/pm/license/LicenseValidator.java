package pm.license;

import com.google.gson.Gson;
import pm.util.Constants;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/**
 * Validates license keys using RSA-SHA256 signature verification.
 * Uses the embedded public key from Constants — can only verify,
 * never create license keys.
 *
 * @author SoftDryzz
 * @version 1.9.0
 * @since 1.9.0
 */
public final class LicenseValidator {

    private static final Gson GSON = new Gson();

    private LicenseValidator() {
        throw new AssertionError("LicenseValidator cannot be instantiated");
    }

    /**
     * Result of a license validation attempt.
     *
     * @param valid Whether the license is valid and not expired
     * @param key   The decoded license key (may be present even if invalid, e.g. expired)
     * @param error Human-readable error message, or null if valid
     */
    public record ValidationResult(boolean valid, LicenseKey key, String error) {

        public static ValidationResult success(LicenseKey key) {
            return new ValidationResult(true, key, null);
        }

        public static ValidationResult failure(String error) {
            return new ValidationResult(false, null, error);
        }

        public static ValidationResult expired(LicenseKey key) {
            return new ValidationResult(false, key, "License expired on " + key.expires());
        }
    }

    /**
     * Validates a raw license key using the production public key.
     *
     * @param rawKey The full license key string (base64-payload.base64-signature)
     * @return Validation result with decoded key on success
     */
    public static ValidationResult validate(String rawKey) {
        return validate(rawKey, Constants.LICENSE_PUBLIC_KEY);
    }

    /**
     * Validates a raw license key using a custom public key.
     * Package-private for testing with test keypairs.
     *
     * @param rawKey           The full license key string
     * @param publicKeyBase64  Base64-encoded RSA public key (X.509/DER format)
     * @return Validation result
     */
    static ValidationResult validate(String rawKey, String publicKeyBase64) {
        if (rawKey == null || rawKey.isBlank()) {
            return ValidationResult.failure("License key is empty");
        }

        String[] parts = rawKey.split("\\.", 2);
        if (parts.length != 2) {
            return ValidationResult.failure("Invalid license key format");
        }

        byte[] payloadBytes;
        byte[] signatureBytes;
        try {
            payloadBytes = Base64.getDecoder().decode(parts[0]);
            signatureBytes = Base64.getDecoder().decode(parts[1]);
        } catch (IllegalArgumentException e) {
            return ValidationResult.failure("Invalid license key encoding");
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(keySpec);

            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(payloadBytes);

            if (!sig.verify(signatureBytes)) {
                return ValidationResult.failure("Invalid license key signature");
            }
        } catch (Exception e) {
            return ValidationResult.failure("License key verification failed");
        }

        LicenseKey key;
        try {
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
            key = GSON.fromJson(payloadJson, LicenseKey.class);
        } catch (Exception e) {
            return ValidationResult.failure("License key payload is corrupted");
        }

        if (key == null || key.id() == null || key.edition() == null) {
            return ValidationResult.failure("License key payload is incomplete");
        }

        if (key.expires() != null) {
            try {
                LocalDate expiryDate = LocalDate.parse(key.expires());
                if (expiryDate.isBefore(LocalDate.now())) {
                    return ValidationResult.expired(key);
                }
            } catch (DateTimeParseException e) {
                return ValidationResult.failure("Invalid expiry date format in license");
            }
        }

        return ValidationResult.success(key);
    }
}
