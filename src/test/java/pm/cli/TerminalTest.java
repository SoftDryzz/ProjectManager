package pm.cli;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Terminal")
class TerminalTest {

    @Test
    @DisplayName("colors on in a terminal with no overrides")
    void colorsInTerminal() {
        assertTrue(Terminal.colorsEnabled(true, Map.of()));
    }

    @Test
    @DisplayName("colors off when output is redirected")
    void noColorsWhenRedirected() {
        assertFalse(Terminal.colorsEnabled(false, Map.of()));
    }

    @Test
    @DisplayName("NO_COLOR disables colors in a terminal")
    void noColorDisables() {
        assertFalse(Terminal.colorsEnabled(true, Map.of("NO_COLOR", "1")));
    }

    @Test
    @DisplayName("empty NO_COLOR is ignored, as no-color.org specifies")
    void emptyNoColorIgnored() {
        assertTrue(Terminal.colorsEnabled(true, Map.of("NO_COLOR", "")));
    }

    @Test
    @DisplayName("FORCE_COLOR enables colors when redirected")
    void forceColorEnables() {
        assertTrue(Terminal.colorsEnabled(false, Map.of("FORCE_COLOR", "1")));
    }

    @Test
    @DisplayName("FORCE_COLOR=0 does not force colors")
    void forceColorZero() {
        assertFalse(Terminal.colorsEnabled(false, Map.of("FORCE_COLOR", "0")));
    }

    @Test
    @DisplayName("NO_COLOR wins over FORCE_COLOR")
    void noColorWins() {
        assertFalse(Terminal.colorsEnabled(true, Map.of("NO_COLOR", "1", "FORCE_COLOR", "1")));
    }
}
