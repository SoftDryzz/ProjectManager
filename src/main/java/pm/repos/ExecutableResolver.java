package pm.repos;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/**
 * Finds a program on the {@code PATH} without ever using the current folder.
 *
 * <p>Running {@code "gh"} by bare name leaves the lookup to the operating
 * system and the JDK; on Windows that search has historically included the
 * current folder, so a {@code gh.exe} planted in a cloned repository could run
 * instead of the real one. This resolver only accepts absolute {@code PATH}
 * entries and, on Windows, only {@code .exe} files ({@code .bat}/{@code .cmd}
 * would run through {@code cmd.exe}). Callers then run the returned absolute
 * path.
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class ExecutableResolver {

    private ExecutableResolver() {
    }

    /** Finds {@code name} on this machine's PATH. */
    public static Optional<Path> find(String name) {
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        return find(name, System.getenv("PATH"), windows);
    }

    /**
     * Finds {@code name} in the given PATH value.
     *
     * @param name    program name without extension, e.g. "gh"
     * @param pathEnv PATH value; entries separated by ';' (Windows) or ':' (Unix)
     * @param windows whether Windows rules apply
     * @return absolute, normalized path of the first match
     */
    static Optional<Path> find(String name, String pathEnv, boolean windows) {
        if (pathEnv == null || pathEnv.isBlank()) {
            return Optional.empty();
        }
        String fileName = windows ? name + ".exe" : name;
        for (String entry : pathEnv.split(windows ? ";" : ":")) {
            String dir = entry.trim();
            if (dir.length() >= 2 && dir.startsWith("\"") && dir.endsWith("\"")) {
                dir = dir.substring(1, dir.length() - 1);
            }
            if (dir.isEmpty()) {
                continue;
            }
            Path dirPath;
            try {
                dirPath = Path.of(dir);
            } catch (InvalidPathException e) {
                continue;
            }
            if (!dirPath.isAbsolute()) {
                continue; // "." and relative entries resolve against the current folder
            }
            Path candidate = dirPath.resolve(fileName);
            if (Files.isRegularFile(candidate) && (windows || Files.isExecutable(candidate))) {
                return Optional.of(candidate.toAbsolutePath().normalize());
            }
        }
        return Optional.empty();
    }
}
