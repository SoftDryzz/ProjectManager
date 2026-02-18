package pm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectManager - maskValue")
class ProjectManagerEnvTest {

    // ============================================================
    // Sensitive keys are masked
    // ============================================================

    @Test
    @DisplayName("API_KEY value is masked")
    void apiKeyIsMasked() {
        String result = ProjectManager.maskValue("API_KEY", "abc123xyz");
        assertEquals("abc***yz", result);
    }

    @Test
    @DisplayName("SECRET value is masked")
    void secretIsMasked() {
        String result = ProjectManager.maskValue("DB_SECRET", "mysecretvalue");
        assertEquals("mys***ue", result);
    }

    @Test
    @DisplayName("PASSWORD value is masked")
    void passwordIsMasked() {
        String result = ProjectManager.maskValue("PASSWORD", "hunter42");
        assertEquals("hun***42", result);
    }

    @Test
    @DisplayName("TOKEN value is masked")
    void tokenIsMasked() {
        String result = ProjectManager.maskValue("AUTH_TOKEN", "tk-abcdef123");
        assertEquals("tk-***23", result);
    }

    @Test
    @DisplayName("PRIVATE key value is masked")
    void privateKeyIsMasked() {
        String result = ProjectManager.maskValue("PRIVATE_KEY", "pk_live_abc123");
        assertEquals("pk_***23", result);
    }

    @Test
    @DisplayName("CREDENTIAL value is masked")
    void credentialIsMasked() {
        String result = ProjectManager.maskValue("CREDENTIAL", "cred-xyz-456");
        assertEquals("cre***56", result);
    }

    // ============================================================
    // Non-sensitive keys are NOT masked
    // ============================================================

    @Test
    @DisplayName("PORT value is not masked")
    void portNotMasked() {
        String result = ProjectManager.maskValue("PORT", "8080");
        assertEquals("8080", result);
    }

    @Test
    @DisplayName("DEBUG value is not masked")
    void debugNotMasked() {
        String result = ProjectManager.maskValue("DEBUG", "true");
        assertEquals("true", result);
    }

    @Test
    @DisplayName("HOST value is not masked")
    void hostNotMasked() {
        String result = ProjectManager.maskValue("DB_HOST", "localhost");
        assertEquals("localhost", result);
    }

    // ============================================================
    // Short values use *** only
    // ============================================================

    @Test
    @DisplayName("Short sensitive value is fully masked")
    void shortValueFullyMasked() {
        String result = ProjectManager.maskValue("API_KEY", "abc");
        assertEquals("***", result);
    }

    @Test
    @DisplayName("5-char sensitive value is fully masked")
    void fiveCharValueFullyMasked() {
        String result = ProjectManager.maskValue("TOKEN", "abcde");
        assertEquals("***", result);
    }

    @Test
    @DisplayName("6-char sensitive value is partially masked")
    void sixCharValuePartiallyMasked() {
        String result = ProjectManager.maskValue("SECRET", "abcdef");
        assertEquals("abc***ef", result);
    }

    // ============================================================
    // Case insensitive key matching
    // ============================================================

    @Test
    @DisplayName("lowercase key is still detected as sensitive")
    void lowercaseKeyDetected() {
        String result = ProjectManager.maskValue("api_key", "abc123xyz");
        assertEquals("abc***yz", result);
    }

    @Test
    @DisplayName("mixed case key is still detected as sensitive")
    void mixedCaseKeyDetected() {
        String result = ProjectManager.maskValue("Auth_Token", "abc123xyz");
        assertEquals("abc***yz", result);
    }
}
