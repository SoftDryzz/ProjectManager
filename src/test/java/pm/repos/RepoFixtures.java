package pm.repos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Creates fake repositories on disk (a .git folder with a config), no git needed. */
final class RepoFixtures {

    private RepoFixtures() {
    }

    static Path repo(Path dir, String originUrl) throws IOException {
        Path gitDir = Files.createDirectories(dir.resolve(".git"));
        StringBuilder config = new StringBuilder("[core]\n\trepositoryformatversion = 0\n");
        if (originUrl != null) {
            config.append("[remote \"origin\"]\n\turl = ").append(originUrl)
                    .append("\n\tfetch = +refs/heads/*:refs/remotes/origin/*\n");
        }
        Files.writeString(gitDir.resolve("config"), config.toString());
        Files.writeString(gitDir.resolve("HEAD"), "ref: refs/heads/main\n");
        return dir;
    }
}
