package pm.repos;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocalRepoScanner")
class LocalRepoScannerTest {

    @TempDir
    Path root;

    private final LocalRepoScanner scanner = new LocalRepoScanner();

    private Set<String> foundNames(ScanResult result) {
        return result.clones().stream().map(LocalClone::folderName).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("finds repos at the root and inside folders such as Personal")
    void findsNested() throws IOException {
        RepoFixtures.repo(root.resolve("top"), "https://github.com/octo-user/top.git");
        RepoFixtures.repo(root.resolve("Personal/ProjectManager"), "git@github.com:octo-user/ProjectManager.git");
        ScanResult result = scanner.scan(List.of(root), List.of());
        assertEquals(Set.of("top", "ProjectManager"), foundNames(result));
    }

    @Test
    @DisplayName("reads origin url and GitHub key of each clone")
    void readsRemote() throws IOException {
        RepoFixtures.repo(root.resolve("app"), "git@github.com:Octo-User/App.git");
        LocalClone clone = scanner.scan(List.of(root), List.of()).clones().get(0);
        assertEquals("git@github.com:Octo-User/App.git", clone.remoteUrl());
        assertEquals("octo-user/app", clone.githubKey());
    }

    @Test
    @DisplayName("a repo without origin has no url and no key")
    void noRemote() throws IOException {
        RepoFixtures.repo(root.resolve("scratch"), null);
        LocalClone clone = scanner.scan(List.of(root), List.of()).clones().get(0);
        assertNull(clone.remoteUrl());
        assertNull(clone.githubKey());
    }

    @Test
    @DisplayName("finds a repo 4 levels deep but not 5")
    void depthLimit() throws IOException {
        RepoFixtures.repo(root.resolve("a/b/c/four"), null);
        RepoFixtures.repo(root.resolve("a/b/c/d/five"), null);
        assertEquals(Set.of("four"), foundNames(scanner.scan(List.of(root), List.of())));
    }

    @Test
    @DisplayName("a deep tree is bounded by the depth limit")
    void deepTreeIsBoundedByDepth() throws IOException {
        Path deep = root;
        for (int i = 0; i < 12; i++) {
            deep = deep.resolve("level" + i);
        }
        Files.createDirectories(deep);
        RepoFixtures.repo(deep, null);
        long start = System.nanoTime();
        ScanResult result = scanner.scan(List.of(root), List.of());
        assertTrue(result.clones().isEmpty());
        assertTrue(System.nanoTime() - start < 5_000_000_000L, "scan took too long");
    }

    @Test
    @DisplayName("does not descend into a repo")
    void stopsAtRepo() throws IOException {
        RepoFixtures.repo(root.resolve("outer"), null);
        RepoFixtures.repo(root.resolve("outer/vendor/inner"), null);
        assertEquals(Set.of("outer"), foundNames(scanner.scan(List.of(root), List.of())));
    }

    @Test
    @DisplayName("skips node_modules and hidden folders")
    void skipsNoise() throws IOException {
        RepoFixtures.repo(root.resolve("node_modules/pkg"), null);
        RepoFixtures.repo(root.resolve(".cache/pkg"), null);
        assertTrue(scanner.scan(List.of(root), List.of()).clones().isEmpty());
    }

    @Test
    @DisplayName("reports missing roots and keeps scanning the others")
    void missingRoot() throws IOException {
        RepoFixtures.repo(root.resolve("real"), null);
        Path missing = root.resolve("does-not-exist");
        ScanResult result = scanner.scan(List.of(missing, root), List.of());
        assertEquals(List.of(missing), result.missingRoots());
        assertEquals(Set.of("real"), foundNames(result));
    }

    @Test
    @DisplayName("adds registered projects outside the roots, once")
    void registeredPaths(@TempDir Path elsewhere) throws IOException {
        Path registered = RepoFixtures.repo(elsewhere.resolve("registered"), null);
        Path inRoot = RepoFixtures.repo(root.resolve("inroot"), null);
        Path notARepo = Files.createDirectories(elsewhere.resolve("plain"));
        ScanResult result = scanner.scan(List.of(root), List.of(registered, inRoot, notARepo));
        assertEquals(Set.of("registered", "inroot"), foundNames(result));
        assertEquals(2, result.clones().size());
    }

    @Test
    @DisplayName("does not follow a symbolic link out of the root")
    void skipsSymlink(@TempDir Path outside) throws IOException {
        RepoFixtures.repo(outside.resolve("target/secret"), null);
        RepoFixtures.repo(root.resolve("real"), null);
        try {
            Files.createSymbolicLink(root.resolve("link"), outside.resolve("target"));
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            Assumptions.abort("cannot create symbolic links here: " + e);
        }
        assertEquals(Set.of("real"), foundNames(scanner.scan(List.of(root), List.of())));
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    @DisplayName("does not follow a Windows junction out of the root")
    void skipsJunction(@TempDir Path outside) throws Exception {
        RepoFixtures.repo(outside.resolve("target/secret"), null);
        RepoFixtures.repo(root.resolve("real"), null);
        String cmd = System.getenv("ComSpec");
        Assumptions.assumeTrue(cmd != null && Path.of(cmd).isAbsolute(), "ComSpec not set");
        Process process = new ProcessBuilder(cmd, "/c", "mklink", "/J",
                root.resolve("junction").toString(), outside.resolve("target").toString())
                .redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        Path junction = root.resolve("junction");
        try {
            Assumptions.assumeTrue(process.waitFor() == 0 && Files.isDirectory(junction.resolve("secret")),
                    "could not create a junction");
            assertEquals(Set.of("real"), foundNames(scanner.scan(List.of(root), List.of())));
        } finally {
            Files.deleteIfExists(junction); // removes the link only, before the temp folders are cleaned
        }
    }
}
