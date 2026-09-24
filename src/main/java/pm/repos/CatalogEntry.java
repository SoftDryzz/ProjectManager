package pm.repos;

import java.util.List;

/**
 * One row of the catalog: a GitHub repo and its clones, or a local-only clone.
 *
 * @param remote    the GitHub repo, or null for a local-only clone
 * @param clones    clones on disk, sorted by path (never empty when remote is null)
 * @param pmProject name of the pm project registered at one of the clones, or null
 */
public record CatalogEntry(RemoteRepo remote, List<LocalClone> clones, String pmProject) {

    public boolean cloned() {
        return !clones.isEmpty();
    }

    public boolean isLocalOnly() {
        return remote == null;
    }

    /** Repo name, or folder name for a local-only clone. */
    public String name() {
        return remote != null ? remote.name() : clones.get(0).folderName();
    }

    /** {@code owner/name}, or folder name for a local-only clone. */
    public String displayId() {
        return remote != null ? remote.fullName() : clones.get(0).folderName();
    }
}
