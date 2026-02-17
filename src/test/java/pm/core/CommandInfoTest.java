package pm.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommandInfo")
class CommandInfoTest {

    // ============================================================
    // CONSTRUCTOR VALIDATION
    // ============================================================

    @Test
    @DisplayName("Constructor creates record with valid parameters")
    void constructorWorks() {
        CommandInfo cmd = new CommandInfo("build", "BuildService.java", 42, "Compiles the project");

        assertEquals("build", cmd.name());
        assertEquals("BuildService.java", cmd.file());
        assertEquals(42, cmd.line());
        assertEquals("Compiles the project", cmd.description());
    }

    @Test
    @DisplayName("Constructor allows null description")
    void constructorAllowsNullDescription() {
        CommandInfo cmd = new CommandInfo("deploy", "DeployTask.java", 1, null);
        assertNull(cmd.description());
    }

    @Test
    @DisplayName("Constructor throws on null name")
    void constructorThrowsOnNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandInfo(null, "Service.java", 1, null));
    }

    @Test
    @DisplayName("Constructor throws on blank name")
    void constructorThrowsOnBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandInfo("  ", "Service.java", 1, null));
    }

    @Test
    @DisplayName("Constructor throws on null file")
    void constructorThrowsOnNullFile() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandInfo("build", null, 1, null));
    }

    @Test
    @DisplayName("Constructor throws on blank file")
    void constructorThrowsOnBlankFile() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandInfo("build", "  ", 1, null));
    }

    @Test
    @DisplayName("Constructor throws on zero line")
    void constructorThrowsOnZeroLine() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandInfo("build", "Service.java", 0, null));
    }

    @Test
    @DisplayName("Constructor throws on negative line")
    void constructorThrowsOnNegativeLine() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommandInfo("build", "Service.java", -1, null));
    }

    // ============================================================
    // METHOD TESTS
    // ============================================================

    @Test
    @DisplayName("fullCommand returns name with dot prefix")
    void fullCommandWorks() {
        CommandInfo cmd = new CommandInfo("deploy", "DeployTask.java", 10, null);
        assertEquals(".deploy", cmd.fullCommand());
    }

    @Test
    @DisplayName("display returns formatted string with file and line")
    void displayWorks() {
        CommandInfo cmd = new CommandInfo("build", "BuildService.java", 42, null);
        String display = cmd.display();

        assertTrue(display.contains(".build"));
        assertTrue(display.contains("BuildService.java"));
        assertTrue(display.contains("42"));
    }

    @Test
    @DisplayName("fullDisplay includes description when present")
    void fullDisplayWithDescription() {
        CommandInfo cmd = new CommandInfo("test", "TestRunner.java", 15, "Run all unit tests");
        String display = cmd.fullDisplay();

        assertTrue(display.contains(".test"));
        assertTrue(display.contains("TestRunner.java:15"));
        assertTrue(display.contains("Run all unit tests"));
    }

    @Test
    @DisplayName("fullDisplay shows 'No description' when null")
    void fullDisplayWithNullDescription() {
        CommandInfo cmd = new CommandInfo("clean", "CleanTask.java", 5, null);
        String display = cmd.fullDisplay();

        assertTrue(display.contains("No description"));
    }

    @Test
    @DisplayName("fullDisplay shows 'No description' when blank")
    void fullDisplayWithBlankDescription() {
        CommandInfo cmd = new CommandInfo("clean", "CleanTask.java", 5, "   ");
        String display = cmd.fullDisplay();

        assertTrue(display.contains("No description"));
    }

    @Test
    @DisplayName("hasDescription returns true when description exists")
    void hasDescriptionTrue() {
        CommandInfo cmd = new CommandInfo("build", "BuildService.java", 42, "Compiles the project");
        assertTrue(cmd.hasDescription());
    }

    @Test
    @DisplayName("hasDescription returns false when null")
    void hasDescriptionFalseNull() {
        CommandInfo cmd = new CommandInfo("build", "BuildService.java", 42, null);
        assertFalse(cmd.hasDescription());
    }

    @Test
    @DisplayName("hasDescription returns false when blank")
    void hasDescriptionFalseBlank() {
        CommandInfo cmd = new CommandInfo("build", "BuildService.java", 42, "   ");
        assertFalse(cmd.hasDescription());
    }
}
