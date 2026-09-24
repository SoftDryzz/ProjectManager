package pm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectManager - config repos")
class ProjectManagerConfigReposTest {

    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();

    @BeforeEach
    void captureOutput() {
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreOutput() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("invalid folder path is reported cleanly, not as an unexpected error")
    void invalidFolderPathReportedCleanly() throws Exception {
        ProjectManager.handleConfigRepos(new String[]{"config", "repos", "add", "bad\0path"});
        String output = captured.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Invalid folder"), "expected 'Invalid folder' in output:\n" + output);
        assertFalse(output.contains("Illegal char"), "should not leak raw InvalidPathException message:\n" + output);
    }

    @Test
    @DisplayName("unknown action prints usage")
    void unknownActionPrintsUsage() throws Exception {
        ProjectManager.handleConfigRepos(new String[]{"config", "repos", "move", "x"});
        String output = captured.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Usage: pm config repos"), "expected usage message in output:\n" + output);
    }
}
