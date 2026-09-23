package pm.cli;

import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Knows whether output goes to an interactive terminal and adapts to it.
 *
 * <p>When output is redirected to a file or another process:
 * <ul>
 * <li>ANSI colors are disabled, so files and CI logs contain no escape codes</li>
 * <li>output is written as UTF-8, so box-drawing characters and symbols are
 *     not replaced by '?' (on Windows, Java would otherwise use the ANSI code
 *     page, e.g. cp1252, which lacks them)</li>
 * </ul>
 *
 * <p>Colors also follow the {@code NO_COLOR} (https://no-color.org) and
 * {@code FORCE_COLOR} conventions. {@code FORCE_COLOR} helps in terminals
 * that Java cannot detect, such as Git Bash (mintty) on Windows.
 *
 * @author SoftDryzz
 * @version 2.0.1
 * @since 2.0.1
 */
public final class Terminal {

    private Terminal() {
    }

    /**
     * Whether the process is attached to an interactive terminal.
     *
     * <p>Since JDK 22, {@code System.console()} is non-null even when output is
     * redirected and {@code Console.isTerminal()} must be used instead. The
     * project targets Java 17, so that method is called reflectively.
     */
    public static boolean isTerminal() {
        Console console = System.console();
        if (console == null) {
            return false;
        }
        try {
            Method isTerminal = Console.class.getMethod("isTerminal");
            return (boolean) isTerminal.invoke(console);
        } catch (NoSuchMethodException e) {
            return true; // JDK < 22: a non-null console is a terminal
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    /**
     * Decides whether ANSI colors should be used.
     * {@code NO_COLOR} wins over {@code FORCE_COLOR}, which wins over detection.
     *
     * @param terminal whether output is an interactive terminal
     * @param env      environment variables
     * @return true if colors should be emitted
     */
    static boolean colorsEnabled(boolean terminal, Map<String, String> env) {
        if (isSet(env.get("NO_COLOR"))) {
            return false;
        }
        String force = env.get("FORCE_COLOR");
        if (isSet(force) && !"0".equals(force.trim())) {
            return true;
        }
        return terminal;
    }

    /**
     * Switches stdout and stderr to UTF-8 when output is not a terminal.
     * Terminal output keeps the console's own encoding, which is what the
     * console expects to display.
     */
    public static void useUtf8WhenRedirected() {
        if (isTerminal()) {
            return;
        }
        System.out.flush();
        System.err.flush();
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
    }

    /** no-color.org: a variable counts only when present and not empty. */
    private static boolean isSet(String value) {
        return value != null && !value.isEmpty();
    }
}
