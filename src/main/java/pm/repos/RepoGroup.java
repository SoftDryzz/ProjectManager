package pm.repos;

import java.util.List;

/**
 * A titled group of catalog entries.
 *
 * @param owner the account or organization login for OWN, ORGANIZATION and PUBLIC_USER; null otherwise
 */
public record RepoGroup(Kind kind, String owner, List<CatalogEntry> entries) {

    public enum Kind { OWN, ORGANIZATION, COLLABORATIONS, PUBLIC_USER, LOCAL_ONLY }

    public long clonedCount() {
        return entries.stream().filter(CatalogEntry::cloned).count();
    }
}
