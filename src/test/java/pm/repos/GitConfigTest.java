package pm.repos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GitConfig")
class GitConfigTest {

    @Test
    @DisplayName("reads the origin url and ignores other remotes")
    void parsesOrigin() {
        List<String> lines = List.of(
                "[core]", "\tbare = false",
                "[remote \"upstream\"]", "\turl = https://github.com/other/fork.git",
                "# comment", "[remote \"origin\"]", "\turl = git@github.com:octo-user/app.git",
                "[branch \"main\"]", "\tremote = origin");
        assertEquals(Optional.of("git@github.com:octo-user/app.git"), GitConfig.parseOrigin(lines));
    }

    @Test
    @DisplayName("accepts quoted values and spacing variations")
    void quotedValue() {
        assertEquals(Optional.of("https://github.com/o/r.git"),
                GitConfig.parseOrigin(List.of("[remote  \"origin\"]", "  url=\"https://github.com/o/r.git\"")));
    }

    @Test
    @DisplayName("no origin section means no url")
    void noOrigin() {
        assertTrue(GitConfig.parseOrigin(List.of("[core]", "bare = false")).isEmpty());
    }

    @Test
    @DisplayName("follows a .git file pointer with commondir (worktrees)")
    void followsGitFile(@TempDir Path tmp) throws IOException {
        Path main = RepoFixtures.repo(tmp.resolve("main"), "https://github.com/octo-user/app.git");
        Path worktreeGitDir = Files.createDirectories(main.resolve(".git/worktrees/feature"));
        Files.writeString(worktreeGitDir.resolve("commondir"), "../..\n");
        Files.writeString(worktreeGitDir.resolve("HEAD"), "ref: refs/heads/feature\n");
        Path worktree = Files.createDirectories(tmp.resolve("feature"));
        Files.writeString(worktree.resolve(".git"), "gitdir: " + worktreeGitDir + "\n");

        assertEquals(worktreeGitDir.normalize(), GitConfig.gitDir(worktree));
        assertEquals(Optional.of("https://github.com/octo-user/app.git"), GitConfig.originUrl(worktree));
    }

    @Test
    @DisplayName("a .git file without gitdir pointer is ignored")
    void badGitFile(@TempDir Path tmp) throws IOException {
        Path repo = Files.createDirectories(tmp.resolve("odd"));
        Files.writeString(repo.resolve(".git"), "not a pointer");
        assertNull(GitConfig.gitDir(repo));
        assertTrue(GitConfig.originUrl(repo).isEmpty());
    }
}
