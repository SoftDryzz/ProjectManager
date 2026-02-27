package pm.license;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("License")
class LicenseTest {

    private static String testPublicKeyBase64;
    private static PrivateKey testPrivateKey;
    private static final Gson GSON = new Gson();

    @TempDir
    Path tempDir;

    @BeforeAll
    static void generateTestKeypair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        testPrivateKey = pair.getPrivate();
        testPublicKeyBase64 = Base64.getEncoder()
                .encodeToString(pair.getPublic().getEncoded());
    }

    /**
     * Signs a JSON payload with the test private key and returns the full license key string.
     */
    private static String createTestLicenseKey(String payloadJson) throws Exception {
        byte[] payloadBytes = payloadJson.getBytes(StandardCharsets.UTF_8);
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(testPrivateKey);
        sig.update(payloadBytes);
        byte[] signatureBytes = sig.sign();
        return Base64.getEncoder().encodeToString(payloadBytes) + "."
                + Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * Creates a signed license key from a LicenseKey record.
     */
    private static String createSignedKey(LicenseKey key) throws Exception {
        String json = GSON.toJson(key);
        return createTestLicenseKey(json);
    }

    // ============================================================
    // LICENSE KEY RECORD
    // ============================================================

    @Nested
    @DisplayName("LicenseKey")
    class LicenseKeyTests {

        @Test
        @DisplayName("isPro returns true for PRO edition")
        void isProTrue() {
            LicenseKey key = new LicenseKey("Acme", "PRO", "2026-01-01", "2027-01-01", "uuid-1");
            assertTrue(key.isPro());
        }

        @Test
        @DisplayName("isPro returns false for COMMUNITY edition")
        void isProFalse() {
            LicenseKey key = new LicenseKey("Acme", "COMMUNITY", "2026-01-01", null, "uuid-2");
            assertFalse(key.isPro());
        }

        @Test
        @DisplayName("isPro is case-insensitive")
        void isProCaseInsensitive() {
            LicenseKey key = new LicenseKey("Acme", "pro", "2026-01-01", "2027-01-01", "uuid-3");
            assertTrue(key.isPro());
        }

        @Test
        @DisplayName("edition constants are defined")
        void editionConstants() {
            assertEquals("PRO", LicenseKey.EDITION_PRO);
            assertEquals("COMMUNITY", LicenseKey.EDITION_COMMUNITY);
        }
    }

    // ============================================================
    // LICENSE VALIDATOR
    // ============================================================

    @Nested
    @DisplayName("LicenseValidator")
    class ValidatorTests {

        @Test
        @DisplayName("valid key returns success with correct fields")
        void validKey() throws Exception {
            LicenseKey original = new LicenseKey("Acme Corp", "PRO", "2026-01-01", "2099-12-31", "test-id-1");
            String rawKey = createSignedKey(original);

            LicenseValidator.ValidationResult result = LicenseValidator.validate(rawKey, testPublicKeyBase64);

            assertTrue(result.valid());
            assertNotNull(result.key());
            assertEquals("Acme Corp", result.key().holder());
            assertEquals("PRO", result.key().edition());
            assertEquals("test-id-1", result.key().id());
            assertNull(result.error());
        }

        @Test
        @DisplayName("null key returns failure")
        void nullKey() {
            LicenseValidator.ValidationResult result = LicenseValidator.validate(null, testPublicKeyBase64);
            assertFalse(result.valid());
            assertNotNull(result.error());
            assertTrue(result.error().contains("empty"));
        }

        @Test
        @DisplayName("empty key returns failure")
        void emptyKey() {
            LicenseValidator.ValidationResult result = LicenseValidator.validate("", testPublicKeyBase64);
            assertFalse(result.valid());
            assertTrue(result.error().contains("empty"));
        }

        @Test
        @DisplayName("key without dot separator returns failure")
        void noDotSeparator() {
            LicenseValidator.ValidationResult result = LicenseValidator.validate("nodothere", testPublicKeyBase64);
            assertFalse(result.valid());
            assertTrue(result.error().contains("format"));
        }

        @Test
        @DisplayName("invalid base64 returns failure")
        void invalidBase64() {
            LicenseValidator.ValidationResult result = LicenseValidator.validate("not!base64.also!not", testPublicKeyBase64);
            assertFalse(result.valid());
            assertTrue(result.error().contains("encoding"));
        }

        @Test
        @DisplayName("tampered payload fails signature verification")
        void tamperedPayload() throws Exception {
            LicenseKey original = new LicenseKey("Acme", "PRO", "2026-01-01", "2099-12-31", "id-1");
            String rawKey = createSignedKey(original);

            // Tamper: replace payload with different holder, keep original signature
            String[] parts = rawKey.split("\\.", 2);
            LicenseKey tampered = new LicenseKey("Evil Corp", "PRO", "2026-01-01", "2099-12-31", "id-1");
            String tamperedPayload = Base64.getEncoder().encodeToString(
                    GSON.toJson(tampered).getBytes(StandardCharsets.UTF_8));
            String tamperedKey = tamperedPayload + "." + parts[1];

            LicenseValidator.ValidationResult result = LicenseValidator.validate(tamperedKey, testPublicKeyBase64);
            assertFalse(result.valid());
            assertTrue(result.error().contains("signature"));
        }

        @Test
        @DisplayName("wrong public key fails verification")
        void wrongPublicKey() throws Exception {
            LicenseKey key = new LicenseKey("Acme", "PRO", "2026-01-01", "2099-12-31", "id-1");
            String rawKey = createSignedKey(key);

            // Generate a different keypair
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(2048);
            KeyPair otherPair = gen.generateKeyPair();
            String otherPublicKey = Base64.getEncoder()
                    .encodeToString(otherPair.getPublic().getEncoded());

            LicenseValidator.ValidationResult result = LicenseValidator.validate(rawKey, otherPublicKey);
            assertFalse(result.valid());
            assertTrue(result.error().contains("signature"));
        }

        @Test
        @DisplayName("expired license returns expired result")
        void expiredLicense() throws Exception {
            LicenseKey key = new LicenseKey("Acme", "PRO", "2020-01-01", "2020-12-31", "id-expired");
            String rawKey = createSignedKey(key);

            LicenseValidator.ValidationResult result = LicenseValidator.validate(rawKey, testPublicKeyBase64);
            assertFalse(result.valid());
            assertNotNull(result.key());
            assertEquals("id-expired", result.key().id());
            assertTrue(result.error().contains("expired"));
        }

        @Test
        @DisplayName("null expiry means perpetual license")
        void perpetualLicense() throws Exception {
            LicenseKey key = new LicenseKey("Acme", "PRO", "2026-01-01", null, "id-perpetual");
            String rawKey = createSignedKey(key);

            LicenseValidator.ValidationResult result = LicenseValidator.validate(rawKey, testPublicKeyBase64);
            assertTrue(result.valid());
            assertNull(result.key().expires());
        }

        @Test
        @DisplayName("missing required fields returns failure")
        void missingRequiredFields() throws Exception {
            // Missing id and edition
            String json = "{\"holder\":\"Acme\",\"issued\":\"2026-01-01\"}";
            String rawKey = createTestLicenseKey(json);

            LicenseValidator.ValidationResult result = LicenseValidator.validate(rawKey, testPublicKeyBase64);
            assertFalse(result.valid());
            assertTrue(result.error().contains("incomplete"));
        }
    }

    // ============================================================
    // LICENSE STORE
    // ============================================================

    @Nested
    @DisplayName("LicenseStore")
    class StoreTests {

        @Test
        @DisplayName("returns empty store when file does not exist")
        void emptyWhenNoFile() {
            LicenseStore store = LicenseStore.load(tempDir.resolve("license.json"));
            assertFalse(store.hasKey());
            assertNull(store.getKey());
        }

        @Test
        @DisplayName("save and reload preserves key")
        void saveAndReload() {
            Path file = tempDir.resolve("license.json");

            LicenseStore store = new LicenseStore();
            store.setKey("test-key-abc.signature-xyz");
            store.save(file);

            LicenseStore loaded = LicenseStore.load(file);
            assertTrue(loaded.hasKey());
            assertEquals("test-key-abc.signature-xyz", loaded.getKey());
        }

        @Test
        @DisplayName("returns empty store for corrupted file")
        void corruptedFile() throws Exception {
            Path file = tempDir.resolve("license.json");
            Files.writeString(file, "{{not valid json");

            LicenseStore store = LicenseStore.load(file);
            assertFalse(store.hasKey());
        }

        @Test
        @DisplayName("delete removes the file")
        void deleteRemovesFile() throws Exception {
            Path file = tempDir.resolve("license.json");
            Files.writeString(file, "{\"key\":\"test\"}");
            assertTrue(Files.exists(file));

            LicenseStore.delete(file);
            assertFalse(Files.exists(file));
        }

        @Test
        @DisplayName("delete on missing file does not throw")
        void deleteNonExistent() {
            Path file = tempDir.resolve("nonexistent.json");
            assertDoesNotThrow(() -> LicenseStore.delete(file));
        }

        @Test
        @DisplayName("save creates parent directories")
        void createsParentDirs() {
            Path file = tempDir.resolve("sub/dir/license.json");

            LicenseStore store = new LicenseStore();
            store.setKey("test");
            store.save(file);

            assertTrue(Files.exists(file));
        }
    }

    // ============================================================
    // LICENSE MANAGER
    // ============================================================

    @Nested
    @DisplayName("LicenseManager")
    class ManagerTests {

        @BeforeEach
        void resetManager() {
            LicenseManager.reset();
        }

        @Test
        @DisplayName("activate with valid key succeeds and saves file")
        void activateValid() throws Exception {
            LicenseKey key = new LicenseKey("Test Corp", "PRO", "2026-01-01", "2099-12-31", "mgr-id-1");
            String rawKey = createSignedKey(key);
            Path file = tempDir.resolve("license.json");

            LicenseManager.ActivationResult result = LicenseManager.activate(rawKey, file, testPublicKeyBase64);

            assertTrue(result.success());
            assertNotNull(result.key());
            assertEquals("Test Corp", result.key().holder());
            assertTrue(Files.exists(file));
        }

        @Test
        @DisplayName("activate with invalid key fails without saving")
        void activateInvalid() {
            Path file = tempDir.resolve("license.json");

            LicenseManager.ActivationResult result = LicenseManager.activate(
                    "invalid.key", file, testPublicKeyBase64);

            assertFalse(result.success());
            assertNotNull(result.message());
            assertFalse(Files.exists(file));
        }

        @Test
        @DisplayName("deactivate removes file and reverts edition")
        void deactivate() throws Exception {
            // First activate
            LicenseKey key = new LicenseKey("Test", "PRO", "2026-01-01", "2099-12-31", "mgr-id-2");
            String rawKey = createSignedKey(key);
            Path file = tempDir.resolve("license.json");
            LicenseManager.activate(rawKey, file, testPublicKeyBase64);
            assertTrue(Files.exists(file));

            // Then deactivate
            LicenseManager.deactivate(file);
            assertFalse(Files.exists(file));
            assertEquals("Community Edition", LicenseManager.getEditionLabel());
        }

        @Test
        @DisplayName("getEditionLabel returns Community Edition when no license")
        void defaultEdition() {
            assertEquals("Community Edition", LicenseManager.getEditionLabel());
        }
    }
}
