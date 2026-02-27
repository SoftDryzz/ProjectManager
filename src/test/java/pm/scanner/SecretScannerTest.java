package pm.scanner;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecretScanner & EnvFileDetector")
class SecretScannerTest {

    @TempDir
    Path tempDir;

    // ============================================================
    // ENV FILE DETECTION
    // ============================================================

    @Nested
    @DisplayName("EnvFileDetector.detectEnvFiles")
    class EnvFileDetection {

        @Test
        @DisplayName("returns empty list for empty directory")
        void emptyDir() {
            List<Path> files = EnvFileDetector.detectEnvFiles(tempDir);
            assertTrue(files.isEmpty());
        }

        @Test
        @DisplayName("finds .env file")
        void findsDotEnv() throws IOException {
            Files.createFile(tempDir.resolve(".env"));
            List<Path> files = EnvFileDetector.detectEnvFiles(tempDir);
            assertEquals(1, files.size());
            assertEquals(".env", files.get(0).getFileName().toString());
        }

        @Test
        @DisplayName("finds multiple .env files sorted")
        void findsMultipleEnvFiles() throws IOException {
            Files.createFile(tempDir.resolve(".env"));
            Files.createFile(tempDir.resolve(".env.local"));
            Files.createFile(tempDir.resolve(".env.production"));
            List<Path> files = EnvFileDetector.detectEnvFiles(tempDir);
            assertEquals(3, files.size());
            assertEquals(".env", files.get(0).getFileName().toString());
            assertEquals(".env.local", files.get(1).getFileName().toString());
            assertEquals(".env.production", files.get(2).getFileName().toString());
        }

        @Test
        @DisplayName("ignores non-.env files")
        void ignoresNonEnvFiles() throws IOException {
            Files.createFile(tempDir.resolve("README.md"));
            Files.createFile(tempDir.resolve("package.json"));
            Files.createFile(tempDir.resolve(".env"));
            List<Path> files = EnvFileDetector.detectEnvFiles(tempDir);
            assertEquals(1, files.size());
        }

        @Test
        @DisplayName("ignores .env directories")
        void ignoresEnvDirectories() throws IOException {
            Files.createDirectory(tempDir.resolve(".env.d"));
            Files.createFile(tempDir.resolve(".env"));
            List<Path> files = EnvFileDetector.detectEnvFiles(tempDir);
            assertEquals(1, files.size());
        }

        @Test
        @DisplayName("returns empty for null path")
        void nullPath() {
            assertTrue(EnvFileDetector.detectEnvFiles(null).isEmpty());
        }

        @Test
        @DisplayName("returns empty for nonexistent path")
        void nonexistentPath() {
            assertTrue(EnvFileDetector.detectEnvFiles(tempDir.resolve("nope")).isEmpty());
        }
    }

    // ============================================================
    // ENV FILE PARSING
    // ============================================================

    @Nested
    @DisplayName("EnvFileDetector.parseEnvFile")
    class EnvFileParsing {

        @Test
        @DisplayName("parses KEY=VALUE pairs")
        void parsesKeyValue() throws IOException {
            Path envFile = tempDir.resolve(".env");
            Files.writeString(envFile, "PORT=8080\nHOST=localhost\n");
            Map<String, String> entries = EnvFileDetector.parseEnvFile(envFile);
            assertEquals(2, entries.size());
            assertEquals("8080", entries.get("PORT"));
            assertEquals("localhost", entries.get("HOST"));
        }

        @Test
        @DisplayName("skips comments and blank lines")
        void skipsCommentsAndBlanks() throws IOException {
            Path envFile = tempDir.resolve(".env");
            Files.writeString(envFile, "# This is a comment\n\nPORT=8080\n\n# Another comment\n");
            Map<String, String> entries = EnvFileDetector.parseEnvFile(envFile);
            assertEquals(1, entries.size());
            assertEquals("8080", entries.get("PORT"));
        }

        @Test
        @DisplayName("strips surrounding quotes")
        void stripsQuotes() throws IOException {
            Path envFile = tempDir.resolve(".env");
            Files.writeString(envFile, "NAME=\"my app\"\nDESC='hello world'\n");
            Map<String, String> entries = EnvFileDetector.parseEnvFile(envFile);
            assertEquals("my app", entries.get("NAME"));
            assertEquals("hello world", entries.get("DESC"));
        }

        @Test
        @DisplayName("handles empty values")
        void handlesEmptyValues() throws IOException {
            Path envFile = tempDir.resolve(".env");
            Files.writeString(envFile, "EMPTY=\nNAME=test\n");
            Map<String, String> entries = EnvFileDetector.parseEnvFile(envFile);
            assertEquals("", entries.get("EMPTY"));
            assertEquals("test", entries.get("NAME"));
        }

        @Test
        @DisplayName("handles values with equals signs")
        void handlesEqualsInValue() throws IOException {
            Path envFile = tempDir.resolve(".env");
            Files.writeString(envFile, "URL=https://host.com?key=value&foo=bar\n");
            Map<String, String> entries = EnvFileDetector.parseEnvFile(envFile);
            assertEquals("https://host.com?key=value&foo=bar", entries.get("URL"));
        }

        @Test
        @DisplayName("returns empty map for null path")
        void nullPath() {
            assertTrue(EnvFileDetector.parseEnvFile(null).isEmpty());
        }
    }

    // ============================================================
    // VALUE MASKING
    // ============================================================

    @Nested
    @DisplayName("EnvFileDetector.maskValue")
    class ValueMasking {

        @Test
        @DisplayName("masks PASSWORD values")
        void masksPassword() {
            assertEquals("myS****", EnvFileDetector.maskValue("DB_PASSWORD", "mySecret123"));
        }

        @Test
        @DisplayName("masks TOKEN values")
        void masksToken() {
            assertEquals("ghp****", EnvFileDetector.maskValue("API_TOKEN", "ghp_abc123def456"));
        }

        @Test
        @DisplayName("masks KEY values")
        void masksKey() {
            assertEquals("AKI****", EnvFileDetector.maskValue("AWS_ACCESS_KEY", "AKIAIOSFODNN7EXAMPLE"));
        }

        @Test
        @DisplayName("masks SECRET values")
        void masksSecret() {
            assertEquals("wJa****", EnvFileDetector.maskValue("JWT_SECRET", "wJalrXUtnFEMI/K7MDENG"));
        }

        @Test
        @DisplayName("masks AUTH values")
        void masksAuth() {
            assertEquals("Bea****", EnvFileDetector.maskValue("AUTH_HEADER", "Bearer xyz123"));
        }

        @Test
        @DisplayName("does not mask non-sensitive values")
        void doesNotMaskNonSensitive() {
            assertEquals("8080", EnvFileDetector.maskValue("PORT", "8080"));
            assertEquals("localhost", EnvFileDetector.maskValue("HOST", "localhost"));
            assertEquals("production", EnvFileDetector.maskValue("NODE_ENV", "production"));
        }

        @Test
        @DisplayName("masks short sensitive values completely")
        void masksShortValues() {
            assertEquals("****", EnvFileDetector.maskValue("API_KEY", "ab"));
        }

        @Test
        @DisplayName("handles null and empty values")
        void handlesNullEmpty() {
            assertNull(EnvFileDetector.maskValue("KEY", null));
            assertEquals("", EnvFileDetector.maskValue("KEY", ""));
        }
    }

    // ============================================================
    // SECRET SCANNING
    // ============================================================

    @Nested
    @DisplayName("SecretScanner.scan")
    class SecretScanning {

        @Test
        @DisplayName("returns empty for directory without .env files")
        void noEnvFiles() {
            assertTrue(SecretScanner.scan(tempDir).isEmpty());
        }

        @Test
        @DisplayName("returns empty for .env with normal values")
        void normalValues() throws IOException {
            Files.writeString(tempDir.resolve(".env"), "PORT=8080\nHOST=localhost\n");
            assertTrue(SecretScanner.scan(tempDir).isEmpty());
        }

        @Test
        @DisplayName("detects AWS access key")
        void detectsAwsKey() throws IOException {
            Files.writeString(tempDir.resolve(".env"),
                    "AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE\n");
            List<SecretFinding> findings = SecretScanner.scan(tempDir);
            assertEquals(1, findings.size());
            assertEquals(SecretPattern.AWS_ACCESS_KEY, findings.get(0).pattern());
            assertEquals("AWS_ACCESS_KEY_ID", findings.get(0).key());
        }

        @Test
        @DisplayName("detects GitHub personal access token")
        void detectsGithubToken() throws IOException {
            Files.writeString(tempDir.resolve(".env"),
                    "GITHUB_TOKEN=ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn\n");
            List<SecretFinding> findings = SecretScanner.scan(tempDir);
            assertEquals(1, findings.size());
            assertEquals(SecretPattern.GITHUB_TOKEN, findings.get(0).pattern());
        }

        @Test
        @DisplayName("detects GitHub fine-grained token")
        void detectsGithubFineGrained() throws IOException {
            Files.writeString(tempDir.resolve(".env"),
                    "GH_TOKEN=github_pat_ABCDEFGHIJKLMNOPQRSTUVWXYZ\n");
            List<SecretFinding> findings = SecretScanner.scan(tempDir);
            assertEquals(1, findings.size());
            assertEquals(SecretPattern.GITHUB_FINE_GRAINED, findings.get(0).pattern());
        }

        @Test
        @DisplayName("detects Slack token")
        void detectsSlackToken() throws IOException {
            // Constructed at runtime to avoid GitHub Push Protection false positive
            String token = "xox" + "b-123456789012-1234567890123-AbCdEfGhIjKlMnOpQrStUvWx";
            Files.writeString(tempDir.resolve(".env"),
                    "SLACK_TOKEN=" + token + "\n");
            List<SecretFinding> findings = SecretScanner.scan(tempDir);
            assertEquals(1, findings.size());
            assertEquals(SecretPattern.SLACK_TOKEN, findings.get(0).pattern());
        }

        @Test
        @DisplayName("detects generic long secret for sensitive keys")
        void detectsGenericSecret() throws IOException {
            // 40+ char random string with sensitive key name
            String longSecret = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnop";
            Files.writeString(tempDir.resolve(".env"),
                    "JWT_SECRET=" + longSecret + "\n");
            List<SecretFinding> findings = SecretScanner.scan(tempDir);
            assertEquals(1, findings.size());
            assertEquals(SecretPattern.GENERIC_SECRET, findings.get(0).pattern());
        }

        @Test
        @DisplayName("does not flag long value for non-sensitive key")
        void noFalsePositiveForNonSensitiveKey() throws IOException {
            String longValue = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnop";
            Files.writeString(tempDir.resolve(".env"),
                    "APP_DESCRIPTION=" + longValue + "\n");
            assertTrue(SecretScanner.scan(tempDir).isEmpty());
        }

        @Test
        @DisplayName("scans multiple .env files")
        void scansMultipleFiles() throws IOException {
            Files.writeString(tempDir.resolve(".env"),
                    "AWS_KEY=AKIAIOSFODNN7EXAMPLE\n");
            Files.writeString(tempDir.resolve(".env.production"),
                    "GITHUB_TOKEN=ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmn\n");
            List<SecretFinding> findings = SecretScanner.scan(tempDir);
            assertEquals(2, findings.size());
        }

        @Test
        @DisplayName("returns empty for null path")
        void nullPath() {
            assertTrue(SecretScanner.scan(null).isEmpty());
        }

        @Test
        @DisplayName("skips empty values")
        void skipsEmptyValues() throws IOException {
            Files.writeString(tempDir.resolve(".env"),
                    "SECRET_KEY=\nAPI_TOKEN=\n");
            assertTrue(SecretScanner.scan(tempDir).isEmpty());
        }
    }

    // ============================================================
    // SECRET PATTERN MATCHING
    // ============================================================

    @Nested
    @DisplayName("SecretPattern.matches")
    class PatternMatching {

        @Test
        @DisplayName("AWS pattern matches valid key")
        void awsMatches() {
            assertTrue(SecretPattern.AWS_ACCESS_KEY.matches("AKIAIOSFODNN7EXAMPLE"));
        }

        @Test
        @DisplayName("AWS pattern rejects invalid key")
        void awsRejects() {
            assertFalse(SecretPattern.AWS_ACCESS_KEY.matches("not-an-aws-key"));
        }

        @Test
        @DisplayName("GENERIC_SECRET always returns false from matches")
        void genericAlwaysFalse() {
            assertFalse(SecretPattern.GENERIC_SECRET.matches("anything"));
        }
    }
}
