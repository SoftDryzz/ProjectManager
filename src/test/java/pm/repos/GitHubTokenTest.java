package pm.repos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GitHubToken")
class GitHubTokenTest {

    private static final String GH = "gho_" + "a".repeat(36);
    private static final String ENV_GH = "ghp_" + "b".repeat(36);
    private static final String ENV_GITHUB = "ghs_" + "c".repeat(36);
    private static final Optional<Path> GH_PATH = Optional.of(Path.of("/usr/bin/gh").toAbsolutePath());

    @Test
    @DisplayName("prefers the gh session")
    void prefersGh() {
        List<List<String>> calls = new ArrayList<>();
        GitHubToken source = new GitHubToken(Map.of("GH_TOKEN", ENV_GH), GH_PATH, cmd -> {
            calls.add(cmd);
            return GH + "\n";
        });
        Token token = source.resolve().orElseThrow();
        assertEquals(GH, token.value());
        assertEquals("gh", token.source());
        assertEquals(List.of(GH_PATH.get().toString(), "auth", "token", "--hostname", "github.com"), calls.get(0));
    }

    @Test
    @DisplayName("falls back to GH_TOKEN, then GITHUB_TOKEN")
    void envFallback() {
        assertEquals(ENV_GH, new GitHubToken(Map.of("GH_TOKEN", ENV_GH, "GITHUB_TOKEN", ENV_GITHUB),
                Optional.empty(), cmd -> null).resolve().orElseThrow().value());
        assertEquals(ENV_GITHUB, new GitHubToken(Map.of("GITHUB_TOKEN", ENV_GITHUB),
                Optional.empty(), cmd -> null).resolve().orElseThrow().value());
    }

    @Test
    @DisplayName("a failing gh falls through to the environment")
    void ghFails() {
        Token token = new GitHubToken(Map.of("GITHUB_TOKEN", ENV_GITHUB), GH_PATH, cmd -> null)
                .resolve().orElseThrow();
        assertEquals("GITHUB_TOKEN", token.source());
    }

    @Test
    @DisplayName("malformed values are ignored (header injection, spaces, too short)")
    void rejectsMalformed() {
        String injected = "ghp_" + "d".repeat(30) + "\r\nX-Injected: 1";
        GitHubToken source = new GitHubToken(Map.of("GH_TOKEN", injected, "GITHUB_TOKEN", "short"),
                GH_PATH, cmd -> "has spaces in it and more text");
        assertTrue(source.resolve().isEmpty());
    }

    @Test
    @DisplayName("nothing available means no token")
    void none() {
        assertTrue(new GitHubToken(Map.of(), Optional.empty(), cmd -> null).resolve().isEmpty());
    }

    @Test
    @DisplayName("toString never reveals the token")
    void toStringHidesValue() {
        Token token = new Token(GH, "gh");
        assertFalse(token.toString().contains(GH));
        assertTrue(token.toString().contains("gh"));
    }

    @Test
    @DisplayName("BoundedProcess returns null for a missing program")
    void boundedProcessMissingProgram() {
        assertNull(BoundedProcess.run(List.of(Path.of("does-not-exist-" + System.nanoTime()).toAbsolutePath().toString()),
                java.time.Duration.ofSeconds(2), 1024, Map.of()));
    }
}
