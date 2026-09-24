package pm.repos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RepoRoots")
class RepoRootsTest {

    @TempDir
    Path tmp;

    private RepoRoots roots() {
        return new RepoRoots(tmp.resolve("config/repos.json"));
    }

    @Test
    @DisplayName("add then load returns the normalized absolute path")
    void roundTrip() throws IOException {
        Path root = tmp.resolve("repos/./Personal");
        assertTrue(roots().add(root));
        assertEquals(List.of(root.toAbsolutePath().normalize()), roots().load());
    }

    @Test
    @DisplayName("adding the same folder twice is refused")
    void duplicate() throws IOException {
        Path root = tmp.resolve("repos");
        assertTrue(roots().add(root));
        assertFalse(roots().add(tmp.resolve("repos/../repos")));
        assertEquals(1, roots().load().size());
    }

    @Test
    @DisplayName("remove deletes a configured folder")
    void remove() throws IOException {
        Path a = tmp.resolve("a");
        Path b = tmp.resolve("b");
        roots().add(a);
        roots().add(b);
        assertTrue(roots().remove(a));
        assertEquals(List.of(b.toAbsolutePath().normalize()), roots().load());
    }

    @Test
    @DisplayName("removing an unknown folder returns false")
    void removeUnknown() throws IOException {
        assertFalse(roots().remove(tmp.resolve("nope")));
    }

    @Test
    @DisplayName("missing file loads as no roots")
    void missingFile() {
        assertEquals(List.of(), roots().load());
    }

    @Test
    @DisplayName("corrupt file loads as no roots")
    void corruptFile() throws IOException {
        Path file = tmp.resolve("config/repos.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{roots: [not json");
        assertEquals(List.of(), roots().load());
    }

    @Test
    @DisplayName("null and blank entries are ignored")
    void ignoresBlankEntries() throws IOException {
        Path file = tmp.resolve("config/repos.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{\"roots\": [null, \"  \", \"" + tmp.toString().replace("\\", "\\\\") + "\"]}");
        assertEquals(List.of(tmp), roots().load());
    }
}
