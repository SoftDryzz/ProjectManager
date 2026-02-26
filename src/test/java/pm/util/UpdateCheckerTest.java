package pm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UpdateChecker")
class UpdateCheckerTest {

    // ============================================================
    // extractTagName - JSON parsing
    // ============================================================

    @Test
    @DisplayName("Extracts tag_name from GitHub API JSON")
    void extractsTagName() {
        String json = """
                {"tag_name":"v1.3.0","name":"Release 1.3.0","draft":false}""";
        assertEquals("1.3.0", UpdateChecker.extractTagName(json));
    }

    @Test
    @DisplayName("Extracts tag_name with spaces around colon")
    void extractsTagNameWithSpaces() {
        String json = """
                {"tag_name" : "v2.0.1", "name": "Release 2.0.1"}""";
        assertEquals("2.0.1", UpdateChecker.extractTagName(json));
    }

    @Test
    @DisplayName("Extracts tag_name without v prefix")
    void extractsTagNameWithoutV() {
        String json = """
                {"tag_name":"1.5.0","name":"Release 1.5.0"}""";
        assertEquals("1.5.0", UpdateChecker.extractTagName(json));
    }

    @Test
    @DisplayName("Returns null for null JSON")
    void extractTagNameNull() {
        assertNull(UpdateChecker.extractTagName(null));
    }

    @Test
    @DisplayName("Returns null for empty JSON")
    void extractTagNameEmpty() {
        assertNull(UpdateChecker.extractTagName(""));
    }

    @Test
    @DisplayName("Returns null for JSON without tag_name")
    void extractTagNameMissing() {
        String json = """
                {"name":"Release 1.0.0","draft":false}""";
        assertNull(UpdateChecker.extractTagName(json));
    }

    @Test
    @DisplayName("Handles tag_name in middle of large JSON")
    void extractTagNameLargeJson() {
        String json = "{\"url\":\"https://api.github.com/repos/SoftDryzz/ProjectManager/releases/1\"," +
                "\"assets_url\":\"https://example.com\"," +
                "\"tag_name\":\"v1.2.0\"," +
                "\"target_commitish\":\"main\"," +
                "\"name\":\"v1.2.0\"}";
        assertEquals("1.2.0", UpdateChecker.extractTagName(json));
    }

    // ============================================================
    // extractAssetSize — JAR size from GitHub API JSON (v1.3.9)
    // ============================================================

    @Test
    @DisplayName("Extracts asset size from typical GitHub API response")
    void extractsAssetSizeNormal() {
        String json = "{\"tag_name\":\"v1.3.9\",\"assets\":[{\"name\":\"projectmanager-1.3.9.jar\"," +
                "\"content_type\":\"application/java-archive\",\"size\":5242880," +
                "\"browser_download_url\":\"https://github.com/SoftDryzz/ProjectManager/releases/download/v1.3.9/projectmanager-1.3.9.jar\"}]}";
        assertEquals(5242880, UpdateChecker.extractAssetSize(json, "projectmanager-1.3.9.jar"));
    }

    @Test
    @DisplayName("Returns -1 when JAR name not found in JSON")
    void extractAssetSizeMissingJar() {
        String json = "{\"tag_name\":\"v1.3.9\",\"assets\":[{\"name\":\"other-file.zip\",\"size\":1000}]}";
        assertEquals(-1, UpdateChecker.extractAssetSize(json, "projectmanager-1.3.9.jar"));
    }

    @Test
    @DisplayName("Returns -1 for null JSON")
    void extractAssetSizeNullJson() {
        assertEquals(-1, UpdateChecker.extractAssetSize(null, "projectmanager-1.3.9.jar"));
    }

    @Test
    @DisplayName("Returns -1 for empty JSON")
    void extractAssetSizeEmptyJson() {
        assertEquals(-1, UpdateChecker.extractAssetSize("", "projectmanager-1.3.9.jar"));
    }

    @Test
    @DisplayName("Returns -1 for null JAR filename")
    void extractAssetSizeNullFilename() {
        assertEquals(-1, UpdateChecker.extractAssetSize("{\"size\":100}", null));
    }

    @Test
    @DisplayName("Extracts correct asset from multiple assets")
    void extractAssetSizeMultipleAssets() {
        String json = "{\"assets\":[" +
                "{\"name\":\"source.zip\",\"size\":1000}," +
                "{\"name\":\"projectmanager-1.3.9.jar\",\"size\":5242880}," +
                "{\"name\":\"checksums.txt\",\"size\":256}" +
                "]}";
        assertEquals(5242880, UpdateChecker.extractAssetSize(json, "projectmanager-1.3.9.jar"));
    }

    @Test
    @DisplayName("Handles size field before name field in asset object")
    void extractAssetSizeSizeBeforeName() {
        String json = "{\"assets\":[{\"size\":5242880,\"name\":\"projectmanager-1.3.9.jar\"}]}";
        assertEquals(5242880, UpdateChecker.extractAssetSize(json, "projectmanager-1.3.9.jar"));
    }

    @Test
    @DisplayName("Handles size with spaces around colon")
    void extractAssetSizeWithSpaces() {
        String json = "{\"assets\":[{\"name\":\"projectmanager-1.3.9.jar\", \"size\" : 5242880}]}";
        assertEquals(5242880, UpdateChecker.extractAssetSize(json, "projectmanager-1.3.9.jar"));
    }

    @Test
    @DisplayName("Returns 0 when asset size is zero")
    void extractAssetSizeZero() {
        String json = "{\"assets\":[{\"name\":\"projectmanager-1.3.9.jar\",\"size\":0}]}";
        assertEquals(0, UpdateChecker.extractAssetSize(json, "projectmanager-1.3.9.jar"));
    }

    // ============================================================
    // describeNetworkError — error classification (v1.3.9)
    // ============================================================

    @Test
    @DisplayName("Classifies UnknownHostException as no internet")
    void describeErrorUnknownHost() {
        String result = UpdateChecker.describeNetworkError(new UnknownHostException("api.github.com"));
        assertTrue(result.contains("No internet"), "Should mention 'No internet' but was: " + result);
    }

    @Test
    @DisplayName("Classifies SocketTimeoutException as timed out")
    void describeErrorTimeout() {
        String result = UpdateChecker.describeNetworkError(new SocketTimeoutException("Read timed out"));
        assertTrue(result.contains("timed out"), "Should mention 'timed out' but was: " + result);
    }

    @Test
    @DisplayName("Classifies ConnectException as connection refused")
    void describeErrorConnectionRefused() {
        String result = UpdateChecker.describeNetworkError(new ConnectException("Connection refused"));
        assertTrue(result.contains("refused"), "Should mention 'refused' but was: " + result);
    }

    @Test
    @DisplayName("Classifies SSLException as SSL error")
    void describeErrorSSL() {
        String result = UpdateChecker.describeNetworkError(new SSLException("Certificate error"));
        assertTrue(result.contains("SSL"), "Should mention 'SSL' but was: " + result);
    }

    @Test
    @DisplayName("Includes message for generic IOException")
    void describeErrorGeneric() {
        String result = UpdateChecker.describeNetworkError(new IOException("Something weird happened"));
        assertTrue(result.contains("Something weird happened"),
                "Should include original message but was: " + result);
    }

    @Test
    @DisplayName("Handles null message in exception")
    void describeErrorNullMessage() {
        String result = UpdateChecker.describeNetworkError(new IOException((String) null));
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ============================================================
    // validateDownloadSize — integrity check (v1.3.9)
    // ============================================================

    @Test
    @DisplayName("Returns null for valid download matching expected size")
    void validateSizeValid() {
        assertNull(UpdateChecker.validateDownloadSize(5242880, 5242880, 5242880));
    }

    @Test
    @DisplayName("Returns null when expected size unknown (-1)")
    void validateSizeUnknownExpected() {
        assertNull(UpdateChecker.validateDownloadSize(5242880, -1, -1));
    }

    @Test
    @DisplayName("Returns error for file too small")
    void validateSizeTooSmall() {
        String error = UpdateChecker.validateDownloadSize(500, -1, -1);
        assertNotNull(error);
        assertTrue(error.contains("too small"), "Should mention 'too small' but was: " + error);
    }

    @Test
    @DisplayName("Returns error when size mismatches expected")
    void validateSizeMismatchExpected() {
        String error = UpdateChecker.validateDownloadSize(3000000, 5242880, -1);
        assertNotNull(error);
        assertTrue(error.contains("mismatch"), "Should mention 'mismatch' but was: " + error);
    }

    @Test
    @DisplayName("Returns error when size mismatches Content-Length")
    void validateSizeMismatchContentLength() {
        String error = UpdateChecker.validateDownloadSize(3000000, -1, 5242880);
        assertNotNull(error);
        assertTrue(error.contains("incomplete"), "Should mention 'incomplete' but was: " + error);
    }

    @Test
    @DisplayName("Expected size takes priority over Content-Length")
    void validateSizeExpectedPriority() {
        // actual matches Content-Length but not expected size
        String error = UpdateChecker.validateDownloadSize(3000000, 5242880, 3000000);
        assertNotNull(error);
        assertTrue(error.contains("mismatch"), "Expected size check should trigger first: " + error);
    }

    // ============================================================
    // ReleaseInfo record (v1.3.9)
    // ============================================================

    @Test
    @DisplayName("ReleaseInfo record accessors work")
    void releaseInfoAccessors() {
        var info = new UpdateChecker.ReleaseInfo("1.3.9", 5242880);
        assertEquals("1.3.9", info.version());
        assertEquals(5242880, info.expectedSize());
    }

    @Test
    @DisplayName("ReleaseInfo with unknown size uses -1")
    void releaseInfoUnknownSize() {
        var info = new UpdateChecker.ReleaseInfo("1.3.9", -1);
        assertEquals(-1, info.expectedSize());
    }

    // ============================================================
    // isNewerVersion - Semantic version comparison
    // ============================================================

    @Test
    @DisplayName("Detects newer major version")
    void newerMajorVersion() {
        assertTrue(UpdateChecker.isNewerVersion("2.0.0", "1.2.0"));
    }

    @Test
    @DisplayName("Detects newer minor version")
    void newerMinorVersion() {
        assertTrue(UpdateChecker.isNewerVersion("1.3.0", "1.2.0"));
    }

    @Test
    @DisplayName("Detects newer patch version")
    void newerPatchVersion() {
        assertTrue(UpdateChecker.isNewerVersion("1.2.1", "1.2.0"));
    }

    @Test
    @DisplayName("Same version is not newer")
    void sameVersionNotNewer() {
        assertFalse(UpdateChecker.isNewerVersion("1.2.0", "1.2.0"));
    }

    @Test
    @DisplayName("Older version is not newer")
    void olderVersionNotNewer() {
        assertFalse(UpdateChecker.isNewerVersion("1.1.0", "1.2.0"));
    }

    @Test
    @DisplayName("Older major is not newer even with higher minor")
    void olderMajorNotNewer() {
        assertFalse(UpdateChecker.isNewerVersion("0.9.9", "1.0.0"));
    }

    @Test
    @DisplayName("Handles null latest version")
    void nullLatestVersion() {
        assertFalse(UpdateChecker.isNewerVersion(null, "1.2.0"));
    }

    @Test
    @DisplayName("Handles null current version")
    void nullCurrentVersion() {
        assertFalse(UpdateChecker.isNewerVersion("1.3.0", null));
    }

    @Test
    @DisplayName("Handles version with only major.minor")
    void versionWithoutPatch() {
        assertTrue(UpdateChecker.isNewerVersion("1.3", "1.2.0"));
    }

    @Test
    @DisplayName("Handles single number version")
    void singleNumberVersion() {
        assertTrue(UpdateChecker.isNewerVersion("2", "1.2.0"));
    }

    // ============================================================
    // checkForUpdates - should not throw or block
    // ============================================================

    @Test
    @DisplayName("checkForUpdates does not throw even without internet")
    void checkForUpdatesNoThrow() {
        // This should return silently regardless of network state
        assertDoesNotThrow(UpdateChecker::checkForUpdates);
    }

    // ============================================================
    // Cannot instantiate
    // ============================================================

    @Test
    @DisplayName("Cannot be instantiated")
    void cannotInstantiate() throws NoSuchMethodException {
        Constructor<UpdateChecker> constructor = UpdateChecker.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}
