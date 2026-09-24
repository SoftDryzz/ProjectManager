package pm.repos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@DisplayName("ExecutableResolver")
class ExecutableResolverTest {

    private static final boolean WINDOWS_HOST = File.separatorChar == '\\';

    private static Path program(Path dir, String fileName) throws IOException {
        Files.createDirectories(dir);
        Path file = Files.writeString(dir.resolve(fileName), "x");
        file.toFile().setExecutable(true);
        return file;
    }

    @Test
    @DisplayName("finds a .exe in an absolute PATH entry (Windows rules)")
    void findsExeWindowsRules(@TempDir Path tmp) throws IOException {
        Path bin = tmp.resolve("bin");
        Path gh = program(bin, "gh.exe");
        Optional<Path> found = ExecutableResolver.find("gh", bin.toString(), true);
        assertEquals(Optional.of(gh.toAbsolutePath().normalize()), found);
    }

    @Test
    @DisplayName("ignores .bat and .cmd on Windows")
    void ignoresBatchFiles(@TempDir Path tmp) throws IOException {
        Path bin = tmp.resolve("bin");
        program(bin, "gh.bat");
        program(bin, "gh.cmd");
        assertTrue(ExecutableResolver.find("gh", bin.toString(), true).isEmpty());
    }

    @Test
    @DisplayName("never uses relative PATH entries such as '.'")
    void skipsRelativeEntries() {
        assertTrue(ExecutableResolver.find("gh", ".;bin;tools\\gh", true).isEmpty());
    }

    @Test
    @DisplayName("returns the first match in PATH order")
    void firstMatchWins(@TempDir Path tmp) throws IOException {
        Path first = program(tmp.resolve("first"), "git.exe");
        program(tmp.resolve("second"), "git.exe");
        String path = tmp.resolve("first") + ";" + tmp.resolve("second");
        assertEquals(Optional.of(first.toAbsolutePath().normalize()), ExecutableResolver.find("git", path, true));
    }

    @Test
    @DisplayName("strips quotes around Windows PATH entries")
    void stripsQuotes(@TempDir Path tmp) throws IOException {
        Path gh = program(tmp.resolve("with space"), "gh.exe");
        String path = "\"" + tmp.resolve("with space") + "\"";
        assertEquals(Optional.of(gh.toAbsolutePath().normalize()), ExecutableResolver.find("gh", path, true));
    }

    @Test
    @DisplayName("empty or missing PATH finds nothing")
    void emptyPath() {
        assertTrue(ExecutableResolver.find("gh", null, true).isEmpty());
        assertTrue(ExecutableResolver.find("gh", "  ", false).isEmpty());
    }

    @Test
    @DisplayName("on Unix, finds an executable file without extension")
    void unixRules(@TempDir Path tmp) throws IOException {
        assumeFalse(WINDOWS_HOST, "Unix PATH rules use ':' which conflicts with Windows drive letters");
        Path gh = program(tmp.resolve("bin"), "gh");
        assertEquals(Optional.of(gh.toAbsolutePath().normalize()),
                ExecutableResolver.find("gh", "relative:" + tmp.resolve("bin"), false));
    }
}
