package pm.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArgsParser")
class ArgsParserTest {

    // ============================================================
    // POSITIONAL ARGUMENTS
    // ============================================================

    @Test
    @DisplayName("Parses positional arguments")
    void parsesPositionalArgs() {
        ArgsParser parser = new ArgsParser(new String[]{"add", "myproject"});

        assertEquals("add", parser.getPositional(0));
        assertEquals("myproject", parser.getPositional(1));
        assertEquals(2, parser.positionalCount());
    }

    @Test
    @DisplayName("getPositional returns null for out-of-bounds index")
    void getPositionalOutOfBounds() {
        ArgsParser parser = new ArgsParser(new String[]{"add"});

        assertNull(parser.getPositional(1));
        assertNull(parser.getPositional(99));
    }

    @Test
    @DisplayName("getPositional returns null for negative index")
    void getPositionalNegativeIndex() {
        ArgsParser parser = new ArgsParser(new String[]{"add"});
        assertNull(parser.getPositional(-1));
    }

    @Test
    @DisplayName("Handles empty args")
    void handlesEmptyArgs() {
        ArgsParser parser = new ArgsParser(new String[]{});

        assertEquals(0, parser.positionalCount());
        assertNull(parser.getPositional(0));
    }

    // ============================================================
    // FLAGS
    // ============================================================

    @Test
    @DisplayName("Parses value flags")
    void parsesValueFlags() {
        ArgsParser parser = new ArgsParser(
                new String[]{"add", "myproject", "--path", "/home/user/project", "--type", "gradle"});

        assertEquals("/home/user/project", parser.getFlag("path"));
        assertEquals("gradle", parser.getFlag("type"));
    }

    @Test
    @DisplayName("Parses boolean flags")
    void parsesBooleanFlags() {
        ArgsParser parser = new ArgsParser(
                new String[]{"add", "myproject", "--force"});

        assertTrue(parser.hasFlag("force"));
        assertEquals("true", parser.getFlag("force"));
    }

    @Test
    @DisplayName("getFlag returns null for non-existent flag")
    void getFlagReturnsNull() {
        ArgsParser parser = new ArgsParser(new String[]{"add"});
        assertNull(parser.getFlag("nonexistent"));
    }

    @Test
    @DisplayName("getFlag returns default value when flag missing")
    void getFlagWithDefault() {
        ArgsParser parser = new ArgsParser(new String[]{"add"});
        assertEquals("default", parser.getFlag("missing", "default"));
    }

    @Test
    @DisplayName("getFlag returns actual value when flag exists")
    void getFlagIgnoresDefaultWhenPresent() {
        ArgsParser parser = new ArgsParser(
                new String[]{"add", "--path", "/home"});

        assertEquals("/home", parser.getFlag("path", "default"));
    }

    @Test
    @DisplayName("hasFlag returns false for non-existent flag")
    void hasFlagReturnsFalse() {
        ArgsParser parser = new ArgsParser(new String[]{"add"});
        assertFalse(parser.hasFlag("force"));
    }

    // ============================================================
    // BOOLEAN FLAGS
    // ============================================================

    @Test
    @DisplayName("getBooleanFlag returns true for present flag")
    void getBooleanFlagTrue() {
        ArgsParser parser = new ArgsParser(
                new String[]{"add", "--force"});

        assertTrue(parser.getBooleanFlag("force"));
    }

    @Test
    @DisplayName("getBooleanFlag returns false for non-existent flag")
    void getBooleanFlagFalseWhenMissing() {
        ArgsParser parser = new ArgsParser(new String[]{"add"});
        assertFalse(parser.getBooleanFlag("force"));
    }

    @Test
    @DisplayName("getBooleanFlag returns false when value is 'false'")
    void getBooleanFlagFalseExplicit() {
        ArgsParser parser = new ArgsParser(
                new String[]{"add", "--verbose", "false"});

        assertFalse(parser.getBooleanFlag("verbose"));
    }

    // ============================================================
    // MIXED ARGUMENTS
    // ============================================================

    @Test
    @DisplayName("Parses mixed positional and flags")
    void parsesMixedArgs() {
        ArgsParser parser = new ArgsParser(
                new String[]{"add", "myapp", "--path", "/home/user/myapp", "--force", "--type", "gradle"});

        assertEquals("add", parser.getPositional(0));
        assertEquals("myapp", parser.getPositional(1));
        assertEquals(2, parser.positionalCount());

        assertEquals("/home/user/myapp", parser.getFlag("path"));
        assertEquals("gradle", parser.getFlag("type"));
        assertTrue(parser.hasFlag("force"));
    }

    @Test
    @DisplayName("Positionals stop at first flag")
    void positionalsStopAtFlag() {
        ArgsParser parser = new ArgsParser(
                new String[]{"add", "--path", "/home", "extra"});

        assertEquals("add", parser.getPositional(0));
        assertEquals(1, parser.positionalCount());
        assertEquals("/home", parser.getFlag("path"));
    }

    @Test
    @DisplayName("Consecutive boolean flags parsed correctly")
    void consecutiveBooleanFlags() {
        ArgsParser parser = new ArgsParser(
                new String[]{"list", "--verbose", "--all"});

        assertTrue(parser.hasFlag("verbose"));
        assertTrue(parser.hasFlag("all"));
    }
}
