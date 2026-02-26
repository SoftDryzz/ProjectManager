package pm.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pm.core.Project;
import pm.detector.ProjectType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityScorer")
class SecurityScorerTest {

    @TempDir
    Path tempDir;

    private Project createProject(String name, ProjectType type) {
        return new Project(name, tempDir, type);
    }

    // ============================================================
    // EVALUATE — always returns 5 checks
    // ============================================================

    @Test
    @DisplayName("evaluate returns exactly 5 checks")
    void evaluateReturnsFiveChecks() {
        Project project = createProject("test", ProjectType.NODEJS);
        List<SecurityCheck> checks = SecurityScorer.evaluate(project);
        assertEquals(5, checks.size());
    }

    @Test
    @DisplayName("evaluate returns checks with expected names")
    void evaluateReturnsExpectedNames() {
        Project project = createProject("test", ProjectType.GRADLE);
        List<SecurityCheck> checks = SecurityScorer.evaluate(project);

        Set<String> names = Set.of(
                "dockerfile-root", "env-gitignore", "https-only",
                "sensitive-files", "lockfile"
        );
        for (SecurityCheck check : checks) {
            assertTrue(names.contains(check.name()), "Unexpected check: " + check.name());
        }
    }

    // ============================================================
    // CHECK 1: Dockerfile non-root
    // ============================================================

    @Nested
    @DisplayName("Dockerfile non-root check")
    class DockerfileCheck {

        @Test
        @DisplayName("passes when no Dockerfile exists")
        void passesWhenNoDockerfile() {
            SecurityCheck check = SecurityScorer.checkDockerfileRoot(tempDir);
            assertTrue(check.passed());
            assertFalse(check.fixable());
        }

        @Test
        @DisplayName("passes when Dockerfile has USER directive")
        void passesWithUserDirective() throws IOException {
            Files.writeString(tempDir.resolve("Dockerfile"),
                    "FROM node:18\nRUN npm install\nUSER node\nCMD [\"node\", \"app.js\"]\n");
            SecurityCheck check = SecurityScorer.checkDockerfileRoot(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("fails when Dockerfile has no USER directive")
        void failsWithoutUserDirective() throws IOException {
            Files.writeString(tempDir.resolve("Dockerfile"),
                    "FROM node:18\nRUN npm install\nCMD [\"node\", \"app.js\"]\n");
            SecurityCheck check = SecurityScorer.checkDockerfileRoot(tempDir);
            assertFalse(check.passed());
            assertNotNull(check.recommendation());
        }

        @Test
        @DisplayName("fails when Dockerfile only has USER root")
        void failsWithUserRoot() throws IOException {
            Files.writeString(tempDir.resolve("Dockerfile"),
                    "FROM node:18\nUSER root\nRUN npm install\nCMD [\"node\", \"app.js\"]\n");
            SecurityCheck check = SecurityScorer.checkDockerfileRoot(tempDir);
            assertFalse(check.passed());
        }

        @Test
        @DisplayName("passes when Dockerfile has USER with non-root user after USER root")
        void passesWithUserSwitchFromRoot() throws IOException {
            Files.writeString(tempDir.resolve("Dockerfile"),
                    "FROM node:18\nUSER root\nRUN apt-get update\nUSER node\nCMD [\"node\", \"app.js\"]\n");
            SecurityCheck check = SecurityScorer.checkDockerfileRoot(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("USER directive is case-insensitive")
        void userDirectiveCaseInsensitive() throws IOException {
            Files.writeString(tempDir.resolve("Dockerfile"),
                    "FROM node:18\nuser appuser\nCMD [\"node\", \"app.js\"]\n");
            SecurityCheck check = SecurityScorer.checkDockerfileRoot(tempDir);
            assertTrue(check.passed());
        }
    }

    // ============================================================
    // CHECK 2: .env in .gitignore
    // ============================================================

    @Nested
    @DisplayName("Env in .gitignore check")
    class EnvGitignoreCheck {

        @Test
        @DisplayName("fails when no .gitignore exists")
        void failsWhenNoGitignore() {
            SecurityCheck check = SecurityScorer.checkEnvInGitignore(tempDir);
            assertFalse(check.passed());
            assertTrue(check.fixable());
        }

        @Test
        @DisplayName("passes when .gitignore contains .env")
        void passesWithDotEnv() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), "node_modules/\n.env\n");
            SecurityCheck check = SecurityScorer.checkEnvInGitignore(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes when .gitignore contains .env*")
        void passesWithDotEnvStar() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), ".env*\n");
            SecurityCheck check = SecurityScorer.checkEnvInGitignore(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes when .gitignore contains .env.*")
        void passesWithDotEnvDotStar() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), ".env\n.env.*\n");
            SecurityCheck check = SecurityScorer.checkEnvInGitignore(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("fails when .gitignore has no .env pattern")
        void failsWithoutEnvPattern() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), "node_modules/\n*.log\n");
            SecurityCheck check = SecurityScorer.checkEnvInGitignore(tempDir);
            assertFalse(check.passed());
        }
    }

    // ============================================================
    // CHECK 3: No http:// URLs
    // ============================================================

    @Nested
    @DisplayName("HTTPS-only check")
    class HttpsOnlyCheck {

        @Test
        @DisplayName("passes when no config files exist")
        void passesWithNoConfigFiles() {
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes when config files have only https")
        void passesWithHttps() throws IOException {
            Files.writeString(tempDir.resolve("config.yml"), "url: https://api.example.com\n");
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("fails when config file has http:// URL")
        void failsWithHttp() throws IOException {
            Files.writeString(tempDir.resolve("config.yml"), "url: http://api.example.com\n");
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertFalse(check.passed());
            assertFalse(check.fixable());
        }

        @Test
        @DisplayName("passes when http:// is localhost")
        void passesWithLocalhost() throws IOException {
            Files.writeString(tempDir.resolve("config.json"),
                    "{\"url\": \"http://localhost:3000\"}\n");
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes when http:// is 127.0.0.1")
        void passesWithLoopback() throws IOException {
            Files.writeString(tempDir.resolve("app.properties"),
                    "server.url=http://127.0.0.1:8080\n");
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes when http:// is XML schema")
        void passesWithXmlSchema() throws IOException {
            Files.writeString(tempDir.resolve("pom.xml"),
                    "<project xmlns=\"http://schemas.example.org\">\n</project>\n");
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes when http:// is W3C namespace")
        void passesWithW3cNamespace() throws IOException {
            Files.writeString(tempDir.resolve("web.xml"),
                    "xmlns=\"http://www.w3.org/2001/XMLSchema\"\n");
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("ignores non-config files")
        void ignoresNonConfigFiles() throws IOException {
            Files.writeString(tempDir.resolve("main.java"),
                    "String url = \"http://insecure.example.com\";\n");
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("scans .env files")
        void scansEnvFiles() throws IOException {
            Files.writeString(tempDir.resolve(".env"),
                    "API_URL=http://insecure.example.com\n");
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertFalse(check.passed());
        }

        @Test
        @DisplayName("passes when http:// is 0.0.0.0")
        void passesWithAllInterfaces() throws IOException {
            Files.writeString(tempDir.resolve("config.toml"),
                    "bind = \"http://0.0.0.0:8080\"\n");
            SecurityCheck check = SecurityScorer.checkHttpUrls(tempDir);
            assertTrue(check.passed());
        }
    }

    // ============================================================
    // CHECK 4: Sensitive files in .gitignore
    // ============================================================

    @Nested
    @DisplayName("Sensitive files in .gitignore check")
    class SensitiveFilesCheck {

        @Test
        @DisplayName("fails when no .gitignore exists")
        void failsWhenNoGitignore() {
            SecurityCheck check = SecurityScorer.checkSensitiveFilesInGitignore(tempDir);
            assertFalse(check.passed());
            assertTrue(check.fixable());
        }

        @Test
        @DisplayName("passes when .gitignore covers *.pem and *.key")
        void passesWithBothPatterns() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), "*.pem\n*.key\n");
            SecurityCheck check = SecurityScorer.checkSensitiveFilesInGitignore(tempDir);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("fails when .gitignore has only *.pem")
        void failsWithOnlyPem() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), "*.pem\n");
            SecurityCheck check = SecurityScorer.checkSensitiveFilesInGitignore(tempDir);
            assertFalse(check.passed());
        }

        @Test
        @DisplayName("fails when .gitignore has only *.key")
        void failsWithOnlyKey() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), "*.key\n");
            SecurityCheck check = SecurityScorer.checkSensitiveFilesInGitignore(tempDir);
            assertFalse(check.passed());
        }

        @Test
        @DisplayName("fails when .gitignore has neither pattern")
        void failsWithNeitherPattern() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), "node_modules/\n.env\n");
            SecurityCheck check = SecurityScorer.checkSensitiveFilesInGitignore(tempDir);
            assertFalse(check.passed());
        }
    }

    // ============================================================
    // CHECK 5: Dependencies lockfile
    // ============================================================

    @Nested
    @DisplayName("Lockfile check")
    class LockfileCheck {

        @Test
        @DisplayName("passes for NODEJS with package-lock.json")
        void passesNodejs() throws IOException {
            Files.createFile(tempDir.resolve("package-lock.json"));
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.NODEJS);
            assertTrue(check.passed());
            assertFalse(check.fixable());
        }

        @Test
        @DisplayName("fails for NODEJS without lockfile")
        void failsNodejs() {
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.NODEJS);
            assertFalse(check.passed());
        }

        @Test
        @DisplayName("passes for RUST with Cargo.lock")
        void passesRust() throws IOException {
            Files.createFile(tempDir.resolve("Cargo.lock"));
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.RUST);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes for GO with go.sum")
        void passesGo() throws IOException {
            Files.createFile(tempDir.resolve("go.sum"));
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.GO);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("always passes for MAVEN")
        void alwaysPassesMaven() {
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.MAVEN);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("always passes for GRADLE")
        void alwaysPassesGradle() {
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.GRADLE);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("always passes for PYTHON")
        void alwaysPassesPython() {
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.PYTHON);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("always passes for DOCKER")
        void alwaysPassesDocker() {
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.DOCKER);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("always passes for UNKNOWN")
        void alwaysPassesUnknown() {
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.UNKNOWN);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes for PNPM with pnpm-lock.yaml")
        void passesPnpm() throws IOException {
            Files.createFile(tempDir.resolve("pnpm-lock.yaml"));
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.PNPM);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes for BUN with bun.lockb")
        void passesBunLockb() throws IOException {
            Files.createFile(tempDir.resolve("bun.lockb"));
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.BUN);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes for BUN with bun.lock")
        void passesBunLock() throws IOException {
            Files.createFile(tempDir.resolve("bun.lock"));
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.BUN);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes for YARN with yarn.lock")
        void passesYarn() throws IOException {
            Files.createFile(tempDir.resolve("yarn.lock"));
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.YARN);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes for FLUTTER with pubspec.lock")
        void passesFlutter() throws IOException {
            Files.createFile(tempDir.resolve("pubspec.lock"));
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.FLUTTER);
            assertTrue(check.passed());
        }

        @Test
        @DisplayName("passes for DOTNET with packages.lock.json")
        void passesDotnet() throws IOException {
            Files.createFile(tempDir.resolve("packages.lock.json"));
            SecurityCheck check = SecurityScorer.checkLockfile(tempDir, ProjectType.DOTNET);
            assertTrue(check.passed());
        }
    }

    // ============================================================
    // FIX — Auto-remediation
    // ============================================================

    @Nested
    @DisplayName("Fix auto-remediation")
    class FixTests {

        @Test
        @DisplayName("creates .gitignore with env entries when missing")
        void createsGitignoreWithEnvEntries() throws IOException {
            Project project = createProject("test", ProjectType.NODEJS);

            List<String> actions = SecurityScorer.fix(project);

            assertTrue(actions.stream().anyMatch(a -> a.contains(".env")));
            String gitignore = Files.readString(tempDir.resolve(".gitignore"));
            assertTrue(gitignore.contains(".env"));
            assertTrue(gitignore.contains(".env.*"));
        }

        @Test
        @DisplayName("creates .gitignore with sensitive file entries when missing")
        void createsGitignoreWithSensitiveEntries() throws IOException {
            Project project = createProject("test", ProjectType.NODEJS);

            List<String> actions = SecurityScorer.fix(project);

            assertTrue(actions.stream().anyMatch(a -> a.contains("*.pem")));
            String gitignore = Files.readString(tempDir.resolve(".gitignore"));
            assertTrue(gitignore.contains("*.pem"));
            assertTrue(gitignore.contains("*.key"));
            assertTrue(gitignore.contains("*.p12"));
            assertTrue(gitignore.contains("*.pfx"));
        }

        @Test
        @DisplayName("appends to existing .gitignore without duplicating")
        void appendsWithoutDuplicating() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), "node_modules/\n.env\n");
            Project project = createProject("test", ProjectType.NODEJS);

            List<String> actions = SecurityScorer.fix(project);

            // .env already exists, should NOT add env entries
            assertTrue(actions.stream().noneMatch(a -> a.contains(".env")));
            // But should add sensitive file entries
            assertTrue(actions.stream().anyMatch(a -> a.contains("*.pem")));
        }

        @Test
        @DisplayName("returns empty list when nothing to fix")
        void returnsEmptyWhenNothingToFix() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"),
                    ".env\n.env.*\n*.pem\n*.key\n*.p12\n*.pfx\n");
            Project project = createProject("test", ProjectType.MAVEN);

            List<String> actions = SecurityScorer.fix(project);

            assertTrue(actions.isEmpty());
        }

        @Test
        @DisplayName("fix does not duplicate existing *.pem entry")
        void doesNotDuplicatePem() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"), "*.pem\n");
            Project project = createProject("test", ProjectType.NODEJS);

            SecurityScorer.fix(project);

            String content = Files.readString(tempDir.resolve(".gitignore"));
            // Count occurrences of *.pem
            long count = content.lines().filter(l -> l.trim().equals("*.pem")).count();
            assertEquals(1, count, "*.pem should appear only once");
        }
    }

    // ============================================================
    // UTILITIES
    // ============================================================

    @Nested
    @DisplayName("Utility methods")
    class UtilityTests {

        @Test
        @DisplayName("parseGitignore returns non-empty non-comment lines")
        void parseGitignoreBasic() throws IOException {
            Files.writeString(tempDir.resolve(".gitignore"),
                    "# Comment\n\nnode_modules/\n.env\n  *.log  \n");
            Set<String> entries = SecurityScorer.parseGitignore(tempDir.resolve(".gitignore"));
            assertEquals(3, entries.size());
            assertTrue(entries.contains("node_modules/"));
            assertTrue(entries.contains(".env"));
            assertTrue(entries.contains("*.log"));
        }

        @Test
        @DisplayName("parseGitignore returns empty set when file missing")
        void parseGitignoreMissing() {
            Set<String> entries = SecurityScorer.parseGitignore(tempDir.resolve(".gitignore"));
            assertTrue(entries.isEmpty());
        }

        @Test
        @DisplayName("containsInsecureHttp detects insecure URLs")
        void detectsInsecureHttp() {
            assertTrue(SecurityScorer.containsInsecureHttp("url: http://api.example.com"));
        }

        @Test
        @DisplayName("containsInsecureHttp allows localhost")
        void allowsLocalhost() {
            assertFalse(SecurityScorer.containsInsecureHttp("url: http://localhost:3000"));
        }

        @Test
        @DisplayName("containsInsecureHttp allows 127.0.0.1")
        void allowsLoopback() {
            assertFalse(SecurityScorer.containsInsecureHttp("url: http://127.0.0.1:8080"));
        }

        @Test
        @DisplayName("containsInsecureHttp allows XML schemas")
        void allowsSchemas() {
            assertFalse(SecurityScorer.containsInsecureHttp("xmlns=\"http://schemas.microsoft.com\""));
        }

        @Test
        @DisplayName("containsInsecureHttp allows W3C URIs")
        void allowsW3c() {
            assertFalse(SecurityScorer.containsInsecureHttp("http://www.w3.org/2001/XMLSchema"));
        }

        @Test
        @DisplayName("containsInsecureHttp allows IPv6 loopback")
        void allowsIpv6Loopback() {
            assertFalse(SecurityScorer.containsInsecureHttp("http://[::1]:8080"));
        }

        @Test
        @DisplayName("containsInsecureHttp returns false for no http")
        void returnsFalseNoHttp() {
            assertFalse(SecurityScorer.containsInsecureHttp("https://secure.example.com"));
        }
    }
}
