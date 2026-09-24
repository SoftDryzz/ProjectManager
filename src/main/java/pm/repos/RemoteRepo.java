package pm.repos;

import java.time.Instant;

/**
 * A repository as listed by GitHub.
 *
 * @param permission the user's highest permission ("admin", "maintain", "write",
 *                   "triage", "read"), or null when unknown ({@code --user} mode)
 */
public record RemoteRepo(String owner, boolean ownerIsOrganization, String name, boolean isPrivate,
                         String description, String defaultBranch, Instant pushedAt, boolean archived,
                         boolean fork, String htmlUrl, String permission) {

    /** {@code owner/name} as GitHub writes it. */
    public String fullName() {
        return owner + "/" + name;
    }

    /** Lowercase {@code owner/name}, matching {@link LocalClone#githubKey()}. */
    public String key() {
        return RemoteUrl.key(owner, name);
    }
}
