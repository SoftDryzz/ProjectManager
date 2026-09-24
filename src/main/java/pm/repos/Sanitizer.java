package pm.repos;

/**
 * Makes untrusted text safe to print in a terminal.
 *
 * <p>Repository names, descriptions, logins and local remote URLs come from
 * GitHub or from files on disk. Printed raw, a control character such as ESC
 * could inject terminal escape sequences (clear the screen, rewrite lines,
 * change the window title) and a bidi control could disguise text. Everything
 * {@code pm repos} prints goes through {@link #clean}.
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class Sanitizer {

    private Sanitizer() {
    }

    /**
     * Removes C0 and C1 control characters, DEL and Unicode bidi controls.
     * Tabs and line breaks become a space so every value stays on one line.
     *
     * @param text untrusted text, may be null
     * @return printable text, never null
     */
    public static String clean(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(text.length());
        text.codePoints().forEach(cp -> {
            if (cp == '\t' || cp == '\n' || cp == '\r') {
                sb.append(' ');
            } else if (!isUnsafe(cp)) {
                sb.appendCodePoint(cp);
            }
        });
        return sb.toString();
    }

    private static boolean isUnsafe(int cp) {
        return cp < 0x20                             // C0 controls, including ESC and BEL
                || (cp >= 0x7F && cp <= 0x9F)        // DEL and C1 controls (0x9B is a one-byte CSI)
                || (cp >= 0x202A && cp <= 0x202E)    // bidi embeddings and overrides
                || (cp >= 0x2066 && cp <= 0x2069)    // bidi isolates
                || cp == 0x200E || cp == 0x200F      // left-to-right / right-to-left marks
                || cp == 0x061C;                     // Arabic letter mark
    }
}
