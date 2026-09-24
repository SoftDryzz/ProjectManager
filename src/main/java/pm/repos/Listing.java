package pm.repos;

import java.util.List;

/**
 * Repositories returned by one listing.
 *
 * @param truncated              true if the page cap stopped the listing early
 * @param ssoHiddenOrganizations organizations whose repos GitHub hid until the token is authorized for SSO
 */
public record Listing(List<RemoteRepo> repos, boolean truncated, int ssoHiddenOrganizations) {
}
