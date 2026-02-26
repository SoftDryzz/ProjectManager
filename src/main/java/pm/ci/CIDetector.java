package pm.ci;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects CI/CD providers by scanning project directories for
 * configuration files and generates dashboard URLs.
 *
 * @author SoftDryzz
 * @version 1.6.5
 * @since 1.6.5
 */
public final class CIDetector {

    /** SSH remote pattern: git@host:owner/repo.git */
    private static final Pattern SSH_PATTERN =
            Pattern.compile("git@[^:]+:(.+?)(?:\\.git)?$");

    /** HTTPS remote pattern: https://host/owner/repo.git */
    private static final Pattern HTTPS_PATTERN =
            Pattern.compile("https?://[^/]+/(.+?)(?:\\.git)?$");

    private CIDetector() {}

    /**
     * Detects all CI/CD providers configured in the given project directory.
     *
     * @param projectRoot the project root directory
     * @return list of detected providers (may be empty, never null)
     */
    public static List<CIProvider> detect(Path projectRoot) {
        if (projectRoot == null || !Files.isDirectory(projectRoot)) {
            return List.of();
        }

        List<CIProvider> providers = new ArrayList<>();

        if (Files.isDirectory(projectRoot.resolve(".github").resolve("workflows"))) {
            providers.add(CIProvider.GITHUB_ACTIONS);
        }
        if (Files.exists(projectRoot.resolve(".gitlab-ci.yml"))) {
            providers.add(CIProvider.GITLAB_CI);
        }
        if (Files.exists(projectRoot.resolve("Jenkinsfile"))) {
            providers.add(CIProvider.JENKINS);
        }
        if (Files.exists(projectRoot.resolve(".travis.yml"))) {
            providers.add(CIProvider.TRAVIS_CI);
        }
        Path circleConfig = projectRoot.resolve(".circleci").resolve("config.yml");
        if (Files.exists(circleConfig)) {
            providers.add(CIProvider.CIRCLECI);
        }

        return providers;
    }

    /**
     * Returns the number of workflow files in {@code .github/workflows/}.
     *
     * @param projectRoot the project root directory
     * @return count of .yml and .yaml files, or 0 if not applicable
     */
    public static int workflowCount(Path projectRoot) {
        Path workflowsDir = projectRoot.resolve(".github").resolve("workflows");
        if (!Files.isDirectory(workflowsDir)) {
            return 0;
        }

        int count = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workflowsDir)) {
            for (Path entry : stream) {
                String name = entry.getFileName().toString().toLowerCase();
                if (name.endsWith(".yml") || name.endsWith(".yaml")) {
                    count++;
                }
            }
        } catch (IOException e) {
            return 0;
        }
        return count;
    }

    /**
     * Generates the CI/CD dashboard URL for the given provider and git remote URL.
     *
     * @param provider  the CI provider
     * @param remoteUrl the git remote URL (SSH or HTTPS)
     * @return the dashboard URL, or null if URL cannot be generated
     */
    public static String dashboardUrl(CIProvider provider, String remoteUrl) {
        String ownerRepo = parseOwnerRepo(remoteUrl);
        if (ownerRepo == null) {
            return null;
        }

        return switch (provider) {
            case GITHUB_ACTIONS -> "https://github.com/" + ownerRepo + "/actions";
            case GITLAB_CI -> "https://gitlab.com/" + ownerRepo + "/-/pipelines";
            case TRAVIS_CI -> "https://app.travis-ci.com/github/" + ownerRepo;
            case CIRCLECI -> "https://app.circleci.com/pipelines/github/" + ownerRepo;
            case JENKINS -> null;
        };
    }

    /**
     * Parses the owner/repo from a git remote URL.
     *
     * @param remoteUrl SSH or HTTPS git remote URL
     * @return "owner/repo" or null if unparseable
     */
    static String parseOwnerRepo(String remoteUrl) {
        if (remoteUrl == null || remoteUrl.isBlank()) {
            return null;
        }

        Matcher sshMatcher = SSH_PATTERN.matcher(remoteUrl.trim());
        if (sshMatcher.matches()) {
            return sshMatcher.group(1);
        }

        Matcher httpsMatcher = HTTPS_PATTERN.matcher(remoteUrl.trim());
        if (httpsMatcher.matches()) {
            return httpsMatcher.group(1);
        }

        return null;
    }
}
