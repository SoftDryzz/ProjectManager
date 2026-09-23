package pm.repos;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses git remote URLs.
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class RemoteUrl {

    /** HTTPS/SSH/git URLs and scp-like {@code git@github.com:owner/name}. */
    private static final Pattern GITHUB = Pattern.compile(
            "^(?:(?:https?|git|ssh)://(?:[^@/]+@)?github\\.com(?::\\d+)?/|git@github\\.com:)"
                    + "([A-Za-z0-9-]+)/([A-Za-z0-9._-]+?)(?:\\.git)?/?$",
            Pattern.CASE_INSENSITIVE);

    /** {@code scheme://user:password@} at the start of a URL. */
    private static final Pattern USERINFO = Pattern.compile("^([A-Za-z][A-Za-z0-9+.-]*://)[^@/]+@");

    private RemoteUrl() {
    }

    /**
     * Case-insensitive key of a github.com remote.
     *
     * @return {@code owner/name} in lowercase, or empty for other hosts or malformed URLs
     */
    public static Optional<String> githubKey(String url) {
        if (url == null) {
            return Optional.empty();
        }
        Matcher m = GITHUB.matcher(url.trim());
        if (!m.matches()) {
            return Optional.empty();
        }
        return Optional.of(key(m.group(1), m.group(2)));
    }

    /** Lowercase {@code owner/name}; GitHub ignores case in both. */
    public static String key(String owner, String name) {
        return (owner + "/" + name).toLowerCase(Locale.ROOT);
    }

    /**
     * Removes credentials embedded in a URL, e.g.
     * {@code https://x-access-token:SECRET@github.com/o/r} becomes
     * {@code https://github.com/o/r}. Used before any remote URL is printed.
     */
    public static String redact(String url) {
        if (url == null) {
            return "";
        }
        return USERINFO.matcher(url.trim()).replaceFirst("$1");
    }
}
