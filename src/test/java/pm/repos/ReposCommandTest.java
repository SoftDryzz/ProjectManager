package pm.repos;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pm.repos.FakeGitHub.Reply;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReposCommand")
class ReposCommandTest {

    private static final String MY_REPOS = GitHubClientTest.MY_REPOS;
    private static final Token TOKEN = GitHubClientTest.TOKEN;

    @TempDir
    Path tmp;

    private FakeGitHub github;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private Path root;
    private RepoRoots roots;

    @BeforeEach
    void setUp() throws IOException {
        github = new FakeGitHub();
        github.on("/user", Reply.json(FakeGitHub.fixture("user.json")));
        github.on(MY_REPOS, Reply.json(FakeGitHub.fixture("repos-page1.json")));
        root = tmp.resolve("repos");
        RepoFixtures.repo(root.resolve("Personal/ProjectManager"), "git@github.com:octo-user/ProjectManager.git");
        RepoFixtures.repo(root.resolve("Personal/FindMatch"), "https://github.com/acme-org/FindMatch.git");
        RepoFixtures.repo(root.resolve("scratch"), null);
        roots = new RepoRoots(tmp.resolve("config/repos.json"));
        roots.add(root);
    }

    @AfterEach
    void tearDown() {
        github.close();
    }

    private ReposCommand command(Optional<Token> token, String baseUrl, Map<String, Path> projects) {
        return new ReposCommand(t -> new GitHubClient(baseUrl, t, 2000, 1024 * 1024), () -> token, roots, projects,
                new LocalRepoScanner(), new LocalGitInfo(Optional.empty()),
                new PrintStream(buffer, true, StandardCharsets.UTF_8), tmp,
                Clock.fixed(Instant.parse("2026-09-23T12:00:00Z"), ZoneOffset.UTC));
    }

    private ReposCommand signedIn() {
        return command(Optional.of(TOKEN), github.baseUrl(),
                Map.of("findmatch", root.resolve("Personal/FindMatch")));
    }

    private String output() {
        String out = buffer.toString(StandardCharsets.UTF_8);
        assertFalse(out.contains(TOKEN.value()), "token leaked into output");
        return out;
    }

    @Test
    @DisplayName("list: groups, clones and pm registration")
    void list() {
        assertEquals(0, signedIn().run(new String[0]));
        String out = output();
        assertTrue(out.contains("octo-user (your account)"));
        assertTrue(out.contains("acme-org (organization)"));
        assertTrue(out.contains("Collaborations"));
        assertTrue(out.contains("Local only"));
        assertTrue(out.contains("● ProjectManager"));
        assertTrue(out.contains("○ Spectra"));
        assertTrue(out.contains("[pm]"));
    }

    @Test
    @DisplayName("detail: collaborators from GitHub")
    void detail() {
        github.on("/repos/acme-org/FindMatch/collaborators?per_page=100", Reply.json(FakeGitHub.fixture("collaborators.json")));
        assertEquals(0, signedIn().run(new String[]{"findmatch"}));
        String out = output();
        assertTrue(out.contains("teammate"));
        assertTrue(out.contains("pm project: findmatch"));
        assertTrue(out.contains("Matchmaking [31mapp[0m"), "description shown with its escape codes removed");
        assertFalse(out.contains("\u001B"), "no escape byte may reach the output");
    }

    @Test
    @DisplayName("detail: collaborators hidden without push access")
    void detailForbidden() {
        github.on("/repos/octo-user/Spectra/collaborators?per_page=100", Reply.status(403));
        assertEquals(0, signedIn().run(new String[]{"Spectra"}));
        assertTrue(output().contains("not visible (requires push access)"));
    }

    @Test
    @DisplayName("detail: unknown name fails with suggestions")
    void unknownName() {
        assertEquals(1, signedIn().run(new String[]{"match"}));
        String out = output();
        assertTrue(out.contains("Repository not found: match"));
        assertTrue(out.contains("acme-org/FindMatch"));
    }

    @Test
    @DisplayName("detail: an ambiguous name lists the candidates")
    void ambiguousName() {
        github.on(MY_REPOS, Reply.json("""
                [{"name":"app","owner":{"login":"octo-user","type":"User"}},
                 {"name":"app","owner":{"login":"acme-org","type":"Organization"}}]"""));
        assertEquals(1, signedIn().run(new String[]{"app"}));
        String out = output();
        assertTrue(out.contains("octo-user/app"));
        assertTrue(out.contains("acme-org/app"));
    }

    @Test
    @DisplayName("no token: local clones only, hints, and no request to GitHub")
    void noToken() {
        assertEquals(0, command(Optional.empty(), github.baseUrl(), Map.of()).run(new String[0]));
        String out = output();
        assertTrue(out.contains("Not signed in to GitHub"));
        assertTrue(out.contains("gh auth login"));
        assertTrue(out.contains("Local only"));
        assertTrue(out.contains("ProjectManager"));
        assertTrue(github.requests().isEmpty());
    }

    @Test
    @DisplayName("--user: public repos of an account without calling /user")
    void publicUser() {
        github.on("/users/octo-user/repos?type=owner&per_page=100&sort=pushed", Reply.json(FakeGitHub.fixture("repos-page1.json")));
        assertEquals(0, command(Optional.empty(), github.baseUrl(), Map.of()).run(new String[]{"--user", "octo-user"}));
        assertTrue(output().contains("octo-user (public repositories)"));
        assertFalse(github.requests().contains("/user"));
    }

    @Test
    @DisplayName("--user rejects crafted logins before any request")
    void publicUserInvalid() {
        assertEquals(1, command(Optional.empty(), github.baseUrl(), Map.of()).run(new String[]{"--user", "../admin"}));
        assertTrue(github.requests().isEmpty());
    }

    @Test
    @DisplayName("offline: warns and shows local clones")
    void offline() {
        String url = github.baseUrl();
        github.close();
        assertEquals(0, command(Optional.of(TOKEN), url, Map.of()).run(new String[0]));
        String out = output();
        assertTrue(out.contains("GitHub not reachable"));
        assertTrue(out.contains("Local only"));
    }

    @Test
    @DisplayName("rate limited: warns and shows local clones")
    void rateLimited() {
        github.on("/user", Reply.status(403).header("X-RateLimit-Remaining", "0").header("X-RateLimit-Reset", "1790000000"));
        assertEquals(0, signedIn().run(new String[0]));
        assertTrue(output().contains("rate limit"));
    }

    @Test
    @DisplayName("missing roots are reported")
    void missingRoot() throws IOException {
        roots.add(tmp.resolve("gone"));
        signedIn().run(new String[0]);
        assertTrue(output().contains("Folder not found"));
    }

    @Test
    @DisplayName("unknown options fail with usage")
    void unknownOption() {
        assertEquals(1, signedIn().run(new String[]{"--nope"}));
        assertTrue(output().contains("Usage: pm repos"));
    }

    @Test
    @DisplayName("detail: local-only folders with the same name are listed by path and open by path")
    void localOnlySameName() throws IOException {
        RepoFixtures.repo(root.resolve("a/api"), null);
        RepoFixtures.repo(root.resolve("b/api"), null);
        String sep = java.io.File.separator;
        String first = "~" + sep + "repos" + sep + "a" + sep + "api";
        String second = "~" + sep + "repos" + sep + "b" + sep + "api";

        assertEquals(1, signedIn().run(new String[]{"api"}));
        String out = output();
        assertTrue(out.contains(first), out);
        assertTrue(out.contains(second), out);
        assertTrue(out.contains("by path"), out);

        buffer.reset();
        assertEquals(0, signedIn().run(new String[]{second}));
        out = output();
        assertTrue(out.contains("local only"), out);
        assertTrue(out.contains(second), out);
        assertFalse(out.contains(first), out);
    }
}
