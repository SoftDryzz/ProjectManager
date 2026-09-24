package pm.repos;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds git repositories under the configured root folders.
 *
 * <p>A repository may sit at most {@value #MAX_DEPTH} levels below a root
 * ({@code root/Personal/app} is level 2). Symbolic links and junctions are not followed,
 * hidden folders and {@code node_modules} are skipped, unreadable folders are
 * ignored, and the scan never descends into a repository. Only files are
 * read; no process is started.
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class LocalRepoScanner {

    static final int MAX_DEPTH = 4;

    /**
     * Scans the roots and adds registered project folders that are repos.
     *
     * @param roots           configured root folders
     * @param registeredPaths paths of projects registered in pm
     */
    public ScanResult scan(List<Path> roots, Collection<Path> registeredPaths) {
        Map<String, LocalClone> found = new LinkedHashMap<>();
        List<Path> missing = new ArrayList<>();
        for (Path root : roots) {
            if (!Files.isDirectory(root)) {
                missing.add(root);
                continue;
            }
            walk(root.toAbsolutePath().normalize(), 0, found);
        }
        for (Path registered : registeredPaths) {
            Path path = registered.toAbsolutePath().normalize();
            if (isRepo(path)) {
                found.putIfAbsent(PathKey.of(path), read(path));
            }
        }
        List<LocalClone> clones = new ArrayList<>(found.values());
        clones.sort(Comparator.comparing(clone -> PathKey.of(clone.path())));
        return new ScanResult(List.copyOf(clones), List.copyOf(missing));
    }

    private void walk(Path dir, int depth, Map<String, LocalClone> found) {
        if (isRepo(dir)) {
            found.putIfAbsent(PathKey.of(dir), read(dir));
            return;
        }
        if (depth >= MAX_DEPTH) {
            return;
        }
        List<Path> children = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path child : stream) {
                children.add(child);
            }
        } catch (IOException | DirectoryIteratorException | SecurityException e) {
            return; // unreadable folder: skip it
        }
        Path realDir;
        try {
            realDir = dir.toRealPath();
        } catch (IOException | SecurityException e) {
            return;
        }
        for (Path child : children) {
            String name = child.getFileName().toString();
            if (name.startsWith(".") || name.equals("node_modules")) {
                continue;
            }
            if (isPlainDirectory(child, realDir)) {
                walk(child, depth + 1, found);
            }
        }
    }

    /**
     * True for a real directory: not a symbolic link, not another reparse
     * point such as a Windows junction (which Java reports as a directory
     * with NOFOLLOW_LINKS), so the scan never leaves the roots (spec S11).
     * A reparse point is detected because its real path is not the real path
     * of its parent plus its own name.
     *
     * @param realParent real path of the folder that contains {@code child}
     */
    static boolean isPlainDirectory(Path child, Path realParent) {
        try {
            BasicFileAttributes attributes =
                    Files.readAttributes(child, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (!attributes.isDirectory() || attributes.isSymbolicLink() || attributes.isOther()) {
                return false;
            }
            Path expected = realParent.resolve(child.getFileName().toString());
            return PathKey.of(child.toRealPath()).equals(PathKey.of(expected));
        } catch (IOException | SecurityException e) {
            return false;
        }
    }

    static boolean isRepo(Path dir) {
        return Files.exists(dir.resolve(".git"), LinkOption.NOFOLLOW_LINKS);
    }

    private static LocalClone read(Path repo) {
        String url = GitConfig.originUrl(repo).orElse(null);
        String key = url == null ? null : RemoteUrl.githubKey(url).orElse(null);
        return new LocalClone(repo, url, key);
    }
}
