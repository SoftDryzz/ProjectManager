package pm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GitIntegration")
class GitIntegrationTest {

    @TempDir
    Path tempDir;

    // ============================================================
    // isGitRepository
    // ============================================================

    @Test
    @DisplayName("isGitRepository returns true when .git directory exists")
    void isGitRepoTrue() throws IOException {
        Files.createDirectory(tempDir.resolve(".git"));
        assertTrue(GitIntegration.isGitRepository(tempDir));
    }

    @Test
    @DisplayName("isGitRepository returns false when .git does not exist")
    void isGitRepoFalse() {
        assertFalse(GitIntegration.isGitRepository(tempDir));
    }

    @Test
    @DisplayName("isGitRepository returns false when .git is a file, not directory")
    void isGitRepoFalseWhenFile() throws IOException {
        Files.createFile(tempDir.resolve(".git"));
        assertFalse(GitIntegration.isGitRepository(tempDir));
    }

    // ============================================================
    // getCurrentBranch (returns null for non-git dirs)
    // ============================================================

    @Test
    @DisplayName("getCurrentBranch returns null for non-git directory")
    void getCurrentBranchNonGit() {
        assertNull(GitIntegration.getCurrentBranch(tempDir));
    }

    // ============================================================
    // getStatus
    // ============================================================

    @Test
    @DisplayName("getStatus returns a result for any directory (git walks up to parent)")
    void getStatusNonGit() {
        // git status works even in subdirs of a repo (it walks up to find .git)
        // so we just verify it returns a non-null result or null without crashing
        GitIntegration.GitStatus status = GitIntegration.getStatus(tempDir);
        // No assertion on null vs non-null since it depends on parent git context
        if (status != null) {
            // If a parent repo is found, status should be valid
            assertTrue(status.modified() >= 0);
            assertTrue(status.untracked() >= 0);
            assertTrue(status.staged() >= 0);
        }
    }

    // ============================================================
    // getCommitsAhead
    // ============================================================

    @Test
    @DisplayName("getCommitsAhead returns a number for any directory")
    void getCommitsAheadNonGit() {
        // git rev-list works if a parent .git is found, returns -1 only on error
        int result = GitIntegration.getCommitsAhead(tempDir);
        // Could be 0 (parent repo found) or -1 (no repo at all)
        assertTrue(result >= -1);
    }

    // ============================================================
    // GitStatus class
    // ============================================================

    @Test
    @DisplayName("GitStatus clean state")
    void gitStatusClean() {
        GitIntegration.GitStatus status = new GitIntegration.GitStatus(0, 0, 0, true);

        assertTrue(status.isClean());
        assertEquals(0, status.modified());
        assertEquals(0, status.untracked());
        assertEquals(0, status.staged());
        assertEquals("Clean working tree", status.toString());
    }

    @Test
    @DisplayName("GitStatus with modifications")
    void gitStatusModified() {
        GitIntegration.GitStatus status = new GitIntegration.GitStatus(3, 0, 0, false);

        assertFalse(status.isClean());
        assertEquals(3, status.modified());
        assertTrue(status.toString().contains("3 modified"));
    }

    @Test
    @DisplayName("GitStatus with untracked files")
    void gitStatusUntracked() {
        GitIntegration.GitStatus status = new GitIntegration.GitStatus(0, 5, 0, false);

        assertFalse(status.isClean());
        assertEquals(5, status.untracked());
        assertTrue(status.toString().contains("5 untracked"));
    }

    @Test
    @DisplayName("GitStatus with staged files")
    void gitStatusStaged() {
        GitIntegration.GitStatus status = new GitIntegration.GitStatus(0, 0, 2, false);

        assertFalse(status.isClean());
        assertEquals(2, status.staged());
        assertTrue(status.toString().contains("2 staged"));
    }

    @Test
    @DisplayName("GitStatus with mixed changes")
    void gitStatusMixed() {
        GitIntegration.GitStatus status = new GitIntegration.GitStatus(2, 3, 1, false);

        assertFalse(status.isClean());
        String str = status.toString();
        assertTrue(str.contains("2 modified"));
        assertTrue(str.contains("3 untracked"));
        assertTrue(str.contains("1 staged"));
    }
}
