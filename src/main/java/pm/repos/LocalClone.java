package pm.repos;

import java.nio.file.Path;

/**
 * A repository found on disk.
 *
 * @param path      absolute path of the working folder
 * @param remoteUrl raw {@code origin} URL, or null (may contain credentials: redact before printing)
 * @param githubKey lowercase {@code owner/name} if origin is on github.com, else null
 */
public record LocalClone(Path path, String remoteUrl, String githubKey) {

    /** Folder name, used as the display name of a clone without a GitHub repo. */
    public String folderName() {
        Path name = path.getFileName();
        return name == null ? path.toString() : name.toString();
    }
}
