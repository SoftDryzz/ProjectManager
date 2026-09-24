package pm.repos;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pm.repos.FakeGitHub.Reply;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GitHubClient")
class GitHubClientTest {

    static final String MY_REPOS = "/user/repos?affiliation=owner,organization_member,collaborator&per_page=100&sort=pushed";
    static final Token TOKEN = new Token("gho_" + "t".repeat(36), "gh");

    private FakeGitHub github;

    @BeforeEach
    void start() throws IOException {
        github = new FakeGitHub();
    }

    @AfterEach
    void stop() {
        github.close();
    }

    private GitHubClient client() {
        return new GitHubClient(github.baseUrl(), TOKEN, 2000, 1024 * 1024);
    }

    @Test
    @DisplayName("reads the authenticated login")
    void currentLogin() throws GitHubException {
        github.on("/user", Reply.json(FakeGitHub.fixture("user.json")));
        assertEquals("octo-user", client().currentLogin());
        assertEquals("Bearer " + TOKEN.value(), github.authorizations().get(0));
    }

    @Test
    @DisplayName("lists repositories across pages following the Link header")
    void pagination() throws GitHubException {
        github.on(MY_REPOS, Reply.json(FakeGitHub.fixture("repos-page1.json"))
                .header("Link", "<" + github.baseUrl() + "/user/repos?page=2>; rel=\"next\", <" + github.baseUrl() + "/user/repos?page=2>; rel=\"last\""));
        github.on("/user/repos?page=2", Reply.json(FakeGitHub.fixture("repos-page2.json")));

        Listing listing = client().myRepos();

        assertEquals(5, listing.repos().size());
        assertFalse(listing.truncated());
        RemoteRepo findMatch = listing.repos().get(2);
        assertEquals("acme-org", findMatch.owner());
        assertTrue(findMatch.ownerIsOrganization());
        assertEquals("write", findMatch.permission());
        assertEquals(Instant.parse("2026-09-21T10:00:00Z"), findMatch.pushedAt());
        assertEquals("acme-org/findmatch", findMatch.key());
        assertTrue(listing.repos().get(4).archived());
        assertEquals("admin", listing.repos().get(0).permission());
        assertEquals("read", listing.repos().get(4).permission());
    }

    @Test
    @DisplayName("stops at the page cap and reports truncation")
    void pageCap() throws GitHubException {
        github.on(MY_REPOS, Reply.json("[]").header("Link", "<" + github.baseUrl() + "/loop>; rel=\"next\""));
        github.on("/loop", Reply.json("[]").header("Link", "<" + github.baseUrl() + "/loop>; rel=\"next\""));
        Listing listing = client().myRepos();
        assertTrue(listing.truncated());
        assertEquals(GitHubClient.MAX_PAGES, github.requests().size());
    }

    @Test
    @DisplayName("counts organizations hidden by SSO")
    void ssoPartialResults() throws GitHubException {
        github.on(MY_REPOS, Reply.json("[]").header("X-GitHub-SSO", "partial-results; organizations=21955855,20582480"));
        assertEquals(2, client().myRepos().ssoHiddenOrganizations());
    }

    @Test
    @DisplayName("refuses a pagination link to another host (token never leaves)")
    void refusesCrossHostLink() throws IOException {
        try (FakeGitHub other = new FakeGitHub()) {
            github.on(MY_REPOS, Reply.json("[]").header("Link", "<" + other.baseUrl() + "/steal>; rel=\"next\""));
            GitHubException e = assertThrows(GitHubException.class, () -> client().myRepos());
            assertEquals(GitHubException.Kind.REFUSED, e.kind());
            assertTrue(other.requests().isEmpty());
        }
    }

    @Test
    @DisplayName("never follows redirects (token never leaves)")
    void refusesRedirect() throws IOException {
        try (FakeGitHub other = new FakeGitHub()) {
            github.on("/user", Reply.status(302).header("Location", other.baseUrl() + "/user"));
            GitHubException e = assertThrows(GitHubException.class, () -> client().currentLogin());
            assertEquals(GitHubException.Kind.BAD_RESPONSE, e.kind());
            assertTrue(other.requests().isEmpty());
        }
    }

    @Test
    @DisplayName("sends no Authorization header without a token")
    void anonymous() throws GitHubException {
        github.on("/users/octo-user/repos?type=owner&per_page=100&sort=pushed", Reply.json("[]"));
        new GitHubClient(github.baseUrl(), null, 2000, 1024).publicRepos("octo-user");
        assertEquals("null", github.authorizations().get(0));
    }

    @Test
    @DisplayName("maps 401 to UNAUTHORIZED")
    void unauthorized() {
        github.on("/user", Reply.status(401));
        assertEquals(GitHubException.Kind.UNAUTHORIZED,
                assertThrows(GitHubException.class, () -> client().currentLogin()).kind());
    }

    @Test
    @DisplayName("maps an exhausted rate limit to RATE_LIMITED with its reset time")
    void rateLimited() {
        github.on("/user", Reply.status(403).header("X-RateLimit-Remaining", "0").header("X-RateLimit-Reset", "1790000000"));
        GitHubException e = assertThrows(GitHubException.class, () -> client().currentLogin());
        assertEquals(GitHubException.Kind.RATE_LIMITED, e.kind());
        assertEquals(Instant.ofEpochSecond(1790000000L), e.retryAt().orElseThrow());
    }

    @Test
    @DisplayName("hostile rate-limit headers still give RATE_LIMITED, without a reset time")
    void hostileRateLimitHeaders() {
        github.on("/user", Reply.status(403).header("X-RateLimit-Remaining", "0")
                .header("X-RateLimit-Reset", "9223372036854775807"));
        GitHubException e = assertThrows(GitHubException.class, () -> client().currentLogin());
        assertEquals(GitHubException.Kind.RATE_LIMITED, e.kind());
        assertTrue(e.retryAt().isEmpty());

        github.on("/user", Reply.status(429).header("Retry-After", "9223372036854775807"));
        e = assertThrows(GitHubException.class, () -> client().currentLogin());
        assertEquals(GitHubException.Kind.RATE_LIMITED, e.kind());

        Instant now = Instant.parse("2026-09-23T12:00:00Z");
        assertNull(GitHubClient.rateLimitReset(null, null, "9223372036854775807", now));
        assertNull(GitHubClient.rateLimitReset("0", "9223372036854775807", null, now));
        assertNull(GitHubClient.rateLimitReset("0", "-9223372036854775808", null, now));
    }

    @Test
    @DisplayName("a repo returned on two pages is listed once")
    void duplicatesAcrossPages() throws GitHubException {
        String repo = """
                [{"name":"app","owner":{"login":"octo-user","type":"User"}}]""";
        String dupe = """
                [{"name":"App","owner":{"login":"Octo-User","type":"User"}},
                 {"name":"other","owner":{"login":"octo-user","type":"User"}}]""";
        github.on(MY_REPOS, Reply.json(repo).header("Link", "<" + github.baseUrl() + "/user/repos?page=2>; rel=\"next\""));
        github.on("/user/repos?page=2", Reply.json(dupe));
        assertEquals(List.of("app", "other"), client().myRepos().repos().stream().map(RemoteRepo::name).toList());
    }

    @Test
    @DisplayName("maps other 403 and 404 responses")
    void forbiddenAndNotFound() {
        github.on("/repos/acme-org/FindMatch/collaborators?per_page=100", Reply.status(403));
        assertEquals(GitHubException.Kind.FORBIDDEN,
                assertThrows(GitHubException.class, () -> client().collaborators("acme-org", "FindMatch")).kind());
        assertEquals(GitHubException.Kind.NOT_FOUND,
                assertThrows(GitHubException.class, () -> client().collaborators("acme-org", "Missing")).kind());
    }

    @Test
    @DisplayName("reads collaborators and their roles")
    void collaborators() throws GitHubException {
        github.on("/repos/acme-org/FindMatch/collaborators?per_page=100", Reply.json(FakeGitHub.fixture("collaborators.json")));
        assertEquals(List.of(new Collaborator("octo-user", "admin"), new Collaborator("teammate", "write")),
                client().collaborators("acme-org", "FindMatch"));
    }

    @Test
    @DisplayName("a slow server times out as a NETWORK error")
    void timeout() {
        github.on("/user", Reply.json("{}").delay(1500));
        GitHubClient slowClient = new GitHubClient(github.baseUrl(), TOKEN, 300, 1024);
        assertEquals(GitHubException.Kind.NETWORK,
                assertThrows(GitHubException.class, slowClient::currentLogin).kind());
    }

    @Test
    @DisplayName("rejects malformed JSON and oversized bodies")
    void badBodies() {
        github.on("/user", Reply.json("{not json"));
        assertEquals(GitHubException.Kind.BAD_RESPONSE,
                assertThrows(GitHubException.class, () -> client().currentLogin()).kind());
        github.on(MY_REPOS, Reply.json("[" + " ".repeat(5000) + "]"));
        GitHubClient tinyLimit = new GitHubClient(github.baseUrl(), TOKEN, 2000, 1000);
        assertEquals(GitHubException.Kind.BAD_RESPONSE,
                assertThrows(GitHubException.class, tinyLimit::myRepos).kind());
    }

    @Test
    @DisplayName("only https base URLs, or http on loopback")
    void baseUrlRules() {
        assertThrows(IllegalArgumentException.class, () -> new GitHubClient("http://api.github.com", TOKEN));
        assertThrows(IllegalArgumentException.class, () -> new GitHubClient("ftp://api.github.com", TOKEN));
        assertDoesNotThrow(() -> new GitHubClient("https://api.github.com", TOKEN));
        assertDoesNotThrow(() -> new GitHubClient("http://127.0.0.1:9", TOKEN));
        assertDoesNotThrow(() -> new GitHubClient("http://localhost:9", TOKEN));
        assertDoesNotThrow(() -> new GitHubClient("http://[::1]:9", TOKEN));
        assertThrows(IllegalArgumentException.class, () -> new GitHubClient("http://127.0.0.1.evil.com", TOKEN));
        assertThrows(IllegalArgumentException.class, () -> new GitHubClient("http://localhost.evil.com:9", TOKEN));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new GitHubClient("https://user:hunter2@api.github.com", TOKEN));
        assertFalse(e.getMessage().contains("hunter2"), "base URL echoed in the message");
    }

    @Test
    @DisplayName("same origin requires the same scheme, host (any case) and port, and no userinfo")
    void sameOrigin() {
        java.net.URI base = java.net.URI.create("https://api.github.com");
        assertTrue(GitHubClient.sameOrigin(base, java.net.URI.create("https://api.github.com/user/repos?page=2")));
        assertTrue(GitHubClient.sameOrigin(base, java.net.URI.create("https://API.GitHub.com/user/repos?page=2")));
        assertTrue(GitHubClient.sameOrigin(base, java.net.URI.create("https://api.github.com:443/user")));
        assertFalse(GitHubClient.sameOrigin(base, java.net.URI.create("https://evil.example/user")));
        assertFalse(GitHubClient.sameOrigin(base, java.net.URI.create("https://api.github.com.evil.example/user")));
        assertFalse(GitHubClient.sameOrigin(base, java.net.URI.create("http://api.github.com/user")));
        assertFalse(GitHubClient.sameOrigin(base, java.net.URI.create("http://api.github.com:443/user")));
        assertFalse(GitHubClient.sameOrigin(base, java.net.URI.create("https://user:pw@api.github.com/user")));
        assertFalse(GitHubClient.sameOrigin(base, java.net.URI.create("https://api.github.com:8443/user")));
        assertFalse(GitHubClient.sameOrigin(base, java.net.URI.create("/user/repos?page=2")));
        assertFalse(GitHubClient.sameOrigin(base, java.net.URI.create("//evil.example/user")));
    }

    @Test
    @DisplayName("refuses a pagination link to another host on the same port")
    void refusesSamePortOtherHost() {
        String sameport = github.baseUrl().replace("127.0.0.1", "localhost");
        github.on(MY_REPOS, Reply.json("[]").header("Link", "<" + sameport + "/steal>; rel=\"next\""));
        GitHubException e = assertThrows(GitHubException.class, () -> client().myRepos());
        assertEquals(GitHubException.Kind.REFUSED, e.kind());
        assertEquals(List.of(MY_REPOS), github.requests());
    }

    @Test
    @DisplayName("refuses a relative pagination link")
    void refusesRelativeLink() {
        github.on(MY_REPOS, Reply.json("[]").header("Link", "</steal>; rel=\"next\""));
        GitHubException e = assertThrows(GitHubException.class, () -> client().myRepos());
        assertEquals(GitHubException.Kind.REFUSED, e.kind());
        assertEquals(List.of(MY_REPOS), github.requests());
    }

    @Test
    @DisplayName("rejects crafted logins and repo names before any request")
    void rejectsCraftedInput() {
        assertThrows(IllegalArgumentException.class, () -> client().publicRepos("a/../b"));
        assertThrows(IllegalArgumentException.class, () -> client().publicRepos("-bad"));
        assertThrows(IllegalArgumentException.class, () -> client().collaborators("..", "x"));
        assertThrows(IllegalArgumentException.class, () -> client().collaborators("o", "a/b"));
        assertTrue(github.requests().isEmpty());
        assertTrue(GitHubClient.isValidLogin("octo-user"));
        assertFalse(GitHubClient.isValidLogin("octo--user"));
    }

    @Test
    @DisplayName("error messages never contain the token")
    void messagesHideToken() {
        github.on("/user", Reply.status(401));
        GitHubException e = assertThrows(GitHubException.class, () -> client().currentLogin());
        assertFalse(String.valueOf(e.getMessage()).contains(TOKEN.value()));
    }
}
