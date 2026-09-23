package pm.repos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import pm.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Root folders scanned for cloned repositories, stored in
 * {@code ~/.projectmanager/repos.json}.
 *
 * <p>A separate file on purpose: {@code config.json} is rewritten by the
 * telemetry settings with only their own fields, which would erase these.
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class RepoRoots {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path file;

    public RepoRoots(Path file) {
        this.file = file;
    }

    /** Roots stored in the standard location. */
    public static RepoRoots standard() {
        return new RepoRoots(Constants.REPOS_FILE);
    }

    /** Configured roots; a missing or corrupt file counts as none. */
    public List<Path> load() {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            Data data = GSON.fromJson(Files.readString(file), Data.class);
            if (data == null || data.roots == null) {
                return List.of();
            }
            List<Path> roots = new ArrayList<>();
            for (String root : data.roots) {
                if (root == null || root.isBlank()) {
                    continue;
                }
                try {
                    roots.add(Path.of(root));
                } catch (InvalidPathException ignored) {
                    // unusable entry: skip it
                }
            }
            return List.copyOf(roots);
        } catch (IOException | JsonParseException e) {
            return List.of();
        }
    }

    /**
     * Adds a root, stored as an absolute normalized path.
     *
     * @return false if it was already configured
     */
    public boolean add(Path root) throws IOException {
        Path normalized = root.toAbsolutePath().normalize();
        String key = PathKey.of(normalized);
        List<Path> roots = new ArrayList<>(load());
        for (Path existing : roots) {
            if (PathKey.of(existing).equals(key)) {
                return false;
            }
        }
        roots.add(normalized);
        save(roots);
        return true;
    }

    /**
     * Removes a root.
     *
     * @return false if it was not configured
     */
    public boolean remove(Path root) throws IOException {
        String key = PathKey.of(root);
        List<Path> roots = new ArrayList<>(load());
        boolean removed = roots.removeIf(existing -> PathKey.of(existing).equals(key));
        if (removed) {
            save(roots);
        }
        return removed;
    }

    private void save(List<Path> roots) throws IOException {
        Files.createDirectories(file.getParent());
        Data data = new Data();
        data.roots = roots.stream().map(Path::toString).toList();
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, GSON.toJson(data));
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

    /** JSON shape: {@code {"roots": ["..."]}}. */
    private static final class Data {
        List<String> roots;
    }
}
