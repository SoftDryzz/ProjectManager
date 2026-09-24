package pm.repos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Sanitizer")
class SanitizerTest {

    @Test
    @DisplayName("removes ESC-based terminal sequences")
    void removesEscape() {
        String evil = "repo\u001B[2J\u001B]0;owned\u0007name";
        String clean = Sanitizer.clean(evil);
        assertFalse(clean.contains("\u001B"));
        assertFalse(clean.contains("\u0007"));
        assertEquals("repo[2J]0;ownedname", clean);
    }

    @Test
    @DisplayName("removes C1 controls such as the single-byte CSI")
    void removesC1() {
        assertEquals("ab", Sanitizer.clean("a\u009Bb"));
        assertEquals("ab", Sanitizer.clean("a\u007Fb"));
    }

    @Test
    @DisplayName("removes bidi controls used to disguise text")
    void removesBidi() {
        assertEquals("invoicefdp.exe", Sanitizer.clean("invoice‮fdp.exe"));
        assertEquals("ab", Sanitizer.clean("a⁦b"));
        assertEquals("ab", Sanitizer.clean("a‏b"));
    }

    @Test
    @DisplayName("turns tabs and line breaks into spaces")
    void flattensWhitespace() {
        assertEquals("a b c d", Sanitizer.clean("a\tb\nc\rd"));
    }

    @Test
    @DisplayName("keeps ordinary Unicode text")
    void keepsUnicode() {
        assertEquals("café ✓ 日本 ─", Sanitizer.clean("café ✓ 日本 ─"));
    }

    @Test
    @DisplayName("null becomes an empty string")
    void nullIsEmpty() {
        assertEquals("", Sanitizer.clean(null));
    }
}
