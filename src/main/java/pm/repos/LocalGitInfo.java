package pm.repos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Branch and working-copy state of a clone, for {@code pm repos <name>}.
 *
 * <p>The branch is read from {@code HEAD} as a file. Changes and commits
 * ahead need {@code git status}, run as
 * {@code git -c core.fsmonitor=false status --porcelain=v2 --branch} by
 * absolute path with {@code GIT_OPTIONAL_LOCKS=0} (no index writes). Disabling
 * fsmonitor stops the most direct way a repository's own config could run a
 * command; git's safe.directory check stays on (spec S7).
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class LocalGitInfo {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_OUTPUT = 1024 * 1024;

    private final Optional<Path> git;

    public LocalGitInfo(Optional<Path> git) {
        this.git = git;
    }

    public static LocalGitInfo fromSystem() {
        return new LocalGitInfo(ExecutableResolver.find("git"));
    }

    public CloneStatus read(Path repo) {
        String branch = branchFromHead(repo);
        if (git.isEmpty()) {
            return new CloneStatus(branch, null, null);
        }
        String output = BoundedProcess.run(
                List.of(git.get().toString(), "-c", "core.fsmonitor=false", "-C", repo.toString(),
                        "status", "--porcelain=v2", "--branch"),
                TIMEOUT, MAX_OUTPUT, Map.of("GIT_OPTIONAL_LOCKS", "0", "GIT_TERMINAL_PROMPT", "0"));
        return output == null ? new CloneStatus(branch, null, null) : parseStatus(branch, output);
    }

    /** Branch from {@code HEAD}: "main", or "abc1234 (detached)"; null if unreadable. */
    static String branchFromHead(Path repo) {
        Path gitDir = GitConfig.gitDir(repo);
        if (gitDir == null) {
            return null;
        }
        try {
            Path head = gitDir.resolve("HEAD");
            if (!Files.isRegularFile(head) || Files.size(head) > 4096) {
                return null;
            }
            String content = Files.readString(head, StandardCharsets.UTF_8).trim();
            if (content.startsWith("ref: refs/heads/")) {
                return content.substring("ref: refs/heads/".length());
            }
            return content.length() >= 7 ? content.substring(0, 7) + " (detached)" : null;
        } catch (IOException e) {
            return null;
        }
    }

    /** Parses {@code git status --porcelain=v2 --branch}. */
    static CloneStatus parseStatus(String headBranch, String porcelain) {
        String branch = headBranch;
        Integer ahead = null;
        int changed = 0;
        for (String line : porcelain.split("\\R")) {
            if (line.startsWith("# branch.head ")) {
                String value = line.substring("# branch.head ".length()).strip();
                if (!value.equals("(detached)")) {
                    branch = value;
                }
            } else if (line.startsWith("# branch.ab ")) {
                String[] parts = line.substring("# branch.ab ".length()).strip().split(" ");
                if (parts.length > 0 && parts[0].startsWith("+")) {
                    try {
                        ahead = Integer.parseInt(parts[0].substring(1));
                    } catch (NumberFormatException ignored) {
                        // leave unknown
                    }
                }
            } else if (!line.isBlank() && !line.startsWith("#")) {
                changed++;
            }
        }
        return new CloneStatus(branch, changed, ahead);
    }
}
