package pm.repos;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Reads what {@code pm repos} needs from a repository's git files as plain
 * text. No git process is started, so a repository's own configuration can
 * never run commands during a scan (spec S7).
 */
final class GitConfig {

    private static final long MAX_FILE_BYTES = 1024 * 1024;

    private GitConfig() {
    }

    /**
     * The repository's git directory: {@code .git} itself, or the target of
     * the {@code gitdir:} pointer when {@code .git} is a file (worktrees,
     * submodules).
     *
     * @return the git directory, or null if it cannot be determined
     */
    static Path gitDir(Path repo) {
        Path dotGit = repo.resolve(".git");
        if (Files.isDirectory(dotGit, LinkOption.NOFOLLOW_LINKS)) {
            return dotGit;
        }
        if (!Files.isRegularFile(dotGit, LinkOption.NOFOLLOW_LINKS)) {
            return null;
        }
        try {
            if (Files.size(dotGit) > 4096) {
                return null;
            }
            String content = Files.readString(dotGit, StandardCharsets.UTF_8).trim();
            if (!content.startsWith("gitdir:")) {
                return null;
            }
            Path target = repo.resolve(content.substring("gitdir:".length()).trim()).normalize();
            return Files.isDirectory(target) ? target : null;
        } catch (IOException | InvalidPathException e) {
            return null;
        }
    }

    /** The {@code origin} remote URL, if any. */
    static Optional<String> originUrl(Path repo) {
        Path config = configFile(repo);
        if (config == null) {
            return Optional.empty();
        }
        try {
            if (Files.size(config) > MAX_FILE_BYTES) {
                return Optional.empty();
            }
            return parseOrigin(Files.readAllLines(config, StandardCharsets.UTF_8));
        } catch (IOException | UncheckedIOException e) {
            return Optional.empty();
        }
    }

    /** The shared config file, following {@code commondir} for worktrees. */
    private static Path configFile(Path repo) {
        Path gitDir = gitDir(repo);
        if (gitDir == null) {
            return null;
        }
        Path base = gitDir;
        Path commonDirFile = gitDir.resolve("commondir");
        try {
            if (Files.isRegularFile(commonDirFile) && Files.size(commonDirFile) <= 4096) {
                base = gitDir.resolve(Files.readString(commonDirFile, StandardCharsets.UTF_8).trim()).normalize();
            }
        } catch (IOException | InvalidPathException e) {
            return null;
        }
        Path config = base.resolve("config");
        return Files.isRegularFile(config) ? config : null;
    }

    /** Finds {@code url} inside the {@code [remote "origin"]} section. */
    static Optional<String> parseOrigin(List<String> lines) {
        boolean inOrigin = false;
        for (String raw : lines) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith(";")) {
                continue;
            }
            if (line.startsWith("[")) {
                inOrigin = line.replaceAll("\\s+", " ").equalsIgnoreCase("[remote \"origin\"]");
                continue;
            }
            if (!inOrigin) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq < 0 || !line.substring(0, eq).strip().equalsIgnoreCase("url")) {
                continue;
            }
            String value = line.substring(eq + 1).strip();
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value.isEmpty() ? Optional.empty() : Optional.of(value);
        }
        return Optional.empty();
    }
}
