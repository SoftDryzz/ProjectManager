package pm.ci;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CIDetector")
class CIDetectorTest {

    @TempDir
    Path tempDir;

    // ============================================================
    // DETECTION
    // ============================================================

    @Nested
    @DisplayName("detect")
    class Detection {

        @Test
        @DisplayName("detects GitHub Actions when .github/workflows/ exists")
        void detectGitHubActions() throws IOException {
            Files.createDirectories(tempDir.resolve(".github").resolve("workflows"));
            List<CIProvider> providers = CIDetector.detect(tempDir);
            assertTrue(providers.contains(CIProvider.GITHUB_ACTIONS));
        }

        @Test
        @DisplayName("detects GitLab CI when .gitlab-ci.yml exists")
        void detectGitLabCI() throws IOException {
            Files.createFile(tempDir.resolve(".gitlab-ci.yml"));
            List<CIProvider> providers = CIDetector.detect(tempDir);
            assertTrue(providers.contains(CIProvider.GITLAB_CI));
        }

        @Test
        @DisplayName("detects Jenkins when Jenkinsfile exists")
        void detectJenkins() throws IOException {
            Files.createFile(tempDir.resolve("Jenkinsfile"));
            List<CIProvider> providers = CIDetector.detect(tempDir);
            assertTrue(providers.contains(CIProvider.JENKINS));
        }

        @Test
        @DisplayName("detects Travis CI when .travis.yml exists")
        void detectTravisCI() throws IOException {
            Files.createFile(tempDir.resolve(".travis.yml"));
            List<CIProvider> providers = CIDetector.detect(tempDir);
            assertTrue(providers.contains(CIProvider.TRAVIS_CI));
        }

        @Test
        @DisplayName("detects CircleCI when .circleci/config.yml exists")
        void detectCircleCI() throws IOException {
            Files.createDirectories(tempDir.resolve(".circleci"));
            Files.createFile(tempDir.resolve(".circleci").resolve("config.yml"));
            List<CIProvider> providers = CIDetector.detect(tempDir);
            assertTrue(providers.contains(CIProvider.CIRCLECI));
        }

        @Test
        @DisplayName("detects multiple providers simultaneously")
        void detectMultiple() throws IOException {
            Files.createDirectories(tempDir.resolve(".github").resolve("workflows"));
            Files.createFile(tempDir.resolve(".gitlab-ci.yml"));
            Files.createFile(tempDir.resolve("Jenkinsfile"));
            List<CIProvider> providers = CIDetector.detect(tempDir);
            assertEquals(3, providers.size());
            assertTrue(providers.contains(CIProvider.GITHUB_ACTIONS));
            assertTrue(providers.contains(CIProvider.GITLAB_CI));
            assertTrue(providers.contains(CIProvider.JENKINS));
        }

        @Test
        @DisplayName("returns empty list when no CI configured")
        void detectNone() {
            List<CIProvider> providers = CIDetector.detect(tempDir);
            assertTrue(providers.isEmpty());
        }

        @Test
        @DisplayName("returns empty list for null path")
        void detectNull() {
            List<CIProvider> providers = CIDetector.detect(null);
            assertTrue(providers.isEmpty());
        }
    }

    // ============================================================
    // WORKFLOW COUNT
    // ============================================================

    @Nested
    @DisplayName("workflowCount")
    class WorkflowCount {

        @Test
        @DisplayName("counts .yml and .yaml files in workflows directory")
        void countsWorkflows() throws IOException {
            Path workflows = tempDir.resolve(".github").resolve("workflows");
            Files.createDirectories(workflows);
            Files.createFile(workflows.resolve("ci.yml"));
            Files.createFile(workflows.resolve("deploy.yml"));
            Files.createFile(workflows.resolve("release.yaml"));
            Files.createFile(workflows.resolve("README.md")); // not a workflow

            assertEquals(3, CIDetector.workflowCount(tempDir));
        }

        @Test
        @DisplayName("returns zero for empty workflows directory")
        void emptyWorkflows() throws IOException {
            Files.createDirectories(tempDir.resolve(".github").resolve("workflows"));
            assertEquals(0, CIDetector.workflowCount(tempDir));
        }

        @Test
        @DisplayName("returns zero when workflows directory does not exist")
        void noWorkflowsDir() {
            assertEquals(0, CIDetector.workflowCount(tempDir));
        }
    }

    // ============================================================
    // DASHBOARD URL
    // ============================================================

    @Nested
    @DisplayName("dashboardUrl")
    class DashboardUrl {

        @Test
        @DisplayName("generates GitHub Actions URL from HTTPS remote")
        void gitHubHttps() {
            String url = CIDetector.dashboardUrl(CIProvider.GITHUB_ACTIONS,
                    "https://github.com/user/repo.git");
            assertEquals("https://github.com/user/repo/actions", url);
        }

        @Test
        @DisplayName("generates GitHub Actions URL from SSH remote")
        void gitHubSsh() {
            String url = CIDetector.dashboardUrl(CIProvider.GITHUB_ACTIONS,
                    "git@github.com:user/repo.git");
            assertEquals("https://github.com/user/repo/actions", url);
        }

        @Test
        @DisplayName("generates GitLab CI URL")
        void gitLabUrl() {
            String url = CIDetector.dashboardUrl(CIProvider.GITLAB_CI,
                    "https://gitlab.com/user/repo.git");
            assertEquals("https://gitlab.com/user/repo/-/pipelines", url);
        }

        @Test
        @DisplayName("generates Travis CI URL")
        void travisCIUrl() {
            String url = CIDetector.dashboardUrl(CIProvider.TRAVIS_CI,
                    "https://github.com/user/repo.git");
            assertEquals("https://app.travis-ci.com/github/user/repo", url);
        }

        @Test
        @DisplayName("generates CircleCI URL")
        void circleCIUrl() {
            String url = CIDetector.dashboardUrl(CIProvider.CIRCLECI,
                    "https://github.com/user/repo.git");
            assertEquals("https://app.circleci.com/pipelines/github/user/repo", url);
        }

        @Test
        @DisplayName("returns null for Jenkins (no standard URL)")
        void jenkinsNull() {
            String url = CIDetector.dashboardUrl(CIProvider.JENKINS,
                    "https://github.com/user/repo.git");
            assertNull(url);
        }

        @Test
        @DisplayName("returns null when remote URL is null")
        void nullRemote() {
            String url = CIDetector.dashboardUrl(CIProvider.GITHUB_ACTIONS, null);
            assertNull(url);
        }
    }

    // ============================================================
    // PARSE OWNER/REPO
    // ============================================================

    @Nested
    @DisplayName("parseOwnerRepo")
    class ParseOwnerRepo {

        @Test
        @DisplayName("parses SSH format: git@host:owner/repo.git")
        void sshFormat() {
            assertEquals("owner/repo", CIDetector.parseOwnerRepo("git@github.com:owner/repo.git"));
        }

        @Test
        @DisplayName("parses HTTPS format: https://host/owner/repo.git")
        void httpsFormat() {
            assertEquals("owner/repo", CIDetector.parseOwnerRepo("https://github.com/owner/repo.git"));
        }

        @Test
        @DisplayName("parses HTTPS without .git suffix")
        void httpsNoGitSuffix() {
            assertEquals("owner/repo", CIDetector.parseOwnerRepo("https://github.com/owner/repo"));
        }

        @Test
        @DisplayName("parses SSH without .git suffix")
        void sshNoGitSuffix() {
            assertEquals("owner/repo", CIDetector.parseOwnerRepo("git@github.com:owner/repo"));
        }

        @Test
        @DisplayName("returns null for null input")
        void nullInput() {
            assertNull(CIDetector.parseOwnerRepo(null));
        }

        @Test
        @DisplayName("returns null for empty input")
        void emptyInput() {
            assertNull(CIDetector.parseOwnerRepo(""));
        }

        @Test
        @DisplayName("returns null for unparseable format")
        void invalidFormat() {
            assertNull(CIDetector.parseOwnerRepo("not-a-url"));
        }
    }

    // ============================================================
    // CI PROVIDER ENUM
    // ============================================================

    @Nested
    @DisplayName("CIProvider")
    class ProviderEnum {

        @Test
        @DisplayName("all providers have a display name")
        void allHaveDisplayName() {
            for (CIProvider provider : CIProvider.values()) {
                assertNotNull(provider.displayName());
                assertFalse(provider.displayName().isEmpty());
            }
        }

        @Test
        @DisplayName("has expected number of providers")
        void providerCount() {
            assertEquals(5, CIProvider.values().length);
        }
    }
}
