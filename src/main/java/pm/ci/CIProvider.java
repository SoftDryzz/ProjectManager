package pm.ci;

/**
 * Supported CI/CD providers that ProjectManager can detect.
 *
 * @author SoftDryzz
 * @version 1.6.5
 * @since 1.6.5
 */
public enum CIProvider {

    GITHUB_ACTIONS("GitHub Actions"),
    GITLAB_CI("GitLab CI"),
    JENKINS("Jenkins"),
    TRAVIS_CI("Travis CI"),
    CIRCLECI("CircleCI");

    private final String displayName;

    CIProvider(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Human-readable name for display in CLI output.
     *
     * @return the provider display name
     */
    public String displayName() {
        return displayName;
    }
}
