package pm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectManager - Help output")
class ProjectManagerHelpTest {

    /** A help row: indented usage, a gap of 2+ spaces, then the description. */
    private static final Pattern ROW = Pattern.compile("^(\\s+)(\\S.*?\\S)\\s{2,}(\\S.*)$");

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
    @DisplayName("all command descriptions start at the same column")
    void descriptionsAligned() {
        ProjectManager.printHelp();
        List<String> misaligned = new ArrayList<>();
        int column = -1;

        for (String line : captured.toString(StandardCharsets.UTF_8).split("\\R")) {
            if (line.startsWith("Examples:")) {
                break;
            }
            Matcher m = ROW.matcher(line);
            if (!m.matches()) {
                continue;
            }
            int start = m.start(3);
            if (column < 0) {
                column = start;
            } else if (start != column) {
                misaligned.add(start + ": " + line.strip());
            }
        }

        assertTrue(column > 0, "no command rows found in help");
        assertTrue(misaligned.isEmpty(),
                "expected descriptions at column " + column + ", misaligned rows:\n" + String.join("\n", misaligned));
    }

    @Test
    @DisplayName("help lists the repos command")
    void listsRepos() {
        ProjectManager.printHelp();
        String help = captured.toString(StandardCharsets.UTF_8);
        assertTrue(help.contains("repos [name]"));
        assertTrue(help.contains("repos --user <login>"));
    }
}
