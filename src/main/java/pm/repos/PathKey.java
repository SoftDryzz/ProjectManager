package pm.repos;

import java.nio.file.Path;
import java.util.Locale;

/**
 * Comparable form of a path: absolute, normalized and, on Windows (whose
 * file system ignores case), lowercase. Used to match scanned clones,
 * configured roots and registered project paths.
 */
final class PathKey {

    private static final boolean WINDOWS =
            System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

    private PathKey() {
    }

    static String of(Path path) {
        String text = path.toAbsolutePath().normalize().toString();
        return WINDOWS ? text.toLowerCase(Locale.ROOT) : text;
    }
}
