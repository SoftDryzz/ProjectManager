package pm.repos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@DisplayName("LocalGitInfo")
class LocalGitInfoTest {

    @Test
    @DisplayName("parses branch, changed files and commits ahead")
    void parseStatus() {
        String porcelain = """
                # branch.oid 0123456789abcdef
                # branch.head main
                # branch.upstream origin/main
                # branch.ab +3 -1
                1 .M N... 100644 100644 100644 abc abc src/A.java
                ? notes.txt
                """;
        assertEquals(new CloneStatus("main", 2, 3), LocalGitInfo.parseStatus("fallback", porcelain));
    }

    @Test
    @DisplayName("no upstream means ahead is unknown; detached keeps the HEAD branch text")
    void noUpstream() {
        assertEquals(new CloneStatus("abc1234 (detached)", 0, null),
                LocalGitInfo.parseStatus("abc1234 (detached)", "# branch.head (detached)\n"));
    }

    @Test
    @DisplayName("reads the branch from HEAD without running git")
    void branchFromHead(@TempDir Path tmp) throws IOException {
        Path repo = RepoFixtures.repo(tmp.resolve("app"), null);
        assertEquals("main", LocalGitInfo.branchFromHead(repo));
        Files.writeString(repo.resolve(".git/HEAD"), "0123456789abcdef0123456789abcdef01234567\n");
        assertEquals("0123456 (detached)", LocalGitInfo.branchFromHead(repo));
    }

    @Test
    @DisplayName("without git, only the branch is known")
    void withoutGit(@TempDir Path tmp) throws IOException {
        Path repo = RepoFixtures.repo(tmp.resolve("app"), null);
        assertEquals(new CloneStatus("main", null, null), new LocalGitInfo(Optional.empty()).read(repo));
    }

    @Test
    @DisplayName("counts changes in a real repository")
    void realRepository(@TempDir Path tmp) throws IOException {
        Optional<Path> git = ExecutableResolver.find("git");
        assumeTrue(git.isPresent(), "git not installed");
        Path repo = Files.createDirectories(tmp.resolve("real"));
        assumeTrue(runGit(git.get(), repo, "init", "-q", "-b", "main") != null, "git init failed");
        Files.writeString(repo.resolve("new.txt"), "x");

        CloneStatus status = new LocalGitInfo(git).read(repo);

        assertEquals("main", status.branch());
        assertEquals(1, status.changedFiles());
    }

    @Test
    @DisplayName("a repository's core.fsmonitor command is not run")
    void fsmonitorIsDisabled(@TempDir Path tmp) throws IOException {
        Optional<Path> git = ExecutableResolver.find("git");
        assumeTrue(git.isPresent(), "git not installed");
        Path repo = Files.createDirectories(tmp.resolve("hostile"));
        assumeTrue(runGit(git.get(), repo, "init", "-q") != null, "git init failed");
        runGit(git.get(), repo, "config", "core.fsmonitor", "echo ran > fsmonitor-ran #");
        Path marker = repo.resolve("fsmonitor-ran");

        // Positive control: plain git status does run it on this git version
        runGit(git.get(), repo, "status");
        assumeTrue(Files.exists(marker), "this git version does not run fsmonitor hooks");
        Files.delete(marker);

        new LocalGitInfo(git).read(repo);

        assertFalse(Files.exists(marker), "fsmonitor hook ran during pm repos");
    }

    private static String runGit(Path git, Path repo, String... args) {
        List<String> command = new ArrayList<>(List.of(git.toString(), "-C", repo.toString()));
        command.addAll(List.of(args));
        return BoundedProcess.run(command, Duration.ofSeconds(10), 64 * 1024, Map.of());
    }
}
