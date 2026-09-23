package pm.repos;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Finds a GitHub token without ever storing one: the GitHub CLI session
 * ({@code gh auth token --hostname github.com}), then {@code GH_TOKEN}, then
 * {@code GITHUB_TOKEN}. Malformed values are ignored.
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class GitHubToken {

    /** Runs a command and returns its stdout, or null on failure. */
    @FunctionalInterface
    interface CommandRunner {
        String run(List<String> command);
    }

    private static final Duration GH_TIMEOUT = Duration.ofSeconds(3);

    private final Map<String, String> env;
    private final Optional<Path> gh;
    private final CommandRunner runner;

    GitHubToken(Map<String, String> env, Optional<Path> gh, CommandRunner runner) {
        this.env = env;
        this.gh = gh;
        this.runner = runner;
    }

    /** Token source for this machine. */
    public static GitHubToken fromSystem() {
        return new GitHubToken(System.getenv(), ExecutableResolver.find("gh"),
                command -> BoundedProcess.run(command, GH_TIMEOUT, 4096, Map.of()));
    }

    /** The first well-formed token found, or empty. */
    public Optional<Token> resolve() {
        if (gh.isPresent()) {
            String output = runner.run(List.of(gh.get().toString(), "auth", "token", "--hostname", "github.com"));
            String candidate = output == null ? null : output.strip();
            if (Token.isWellFormed(candidate)) {
                return Optional.of(new Token(candidate, "gh"));
            }
        }
        for (String variable : List.of("GH_TOKEN", "GITHUB_TOKEN")) {
            String value = env.get(variable);
            String candidate = value == null ? null : value.strip();
            if (Token.isWellFormed(candidate)) {
                return Optional.of(new Token(candidate, variable));
            }
        }
        return Optional.empty();
    }
}
