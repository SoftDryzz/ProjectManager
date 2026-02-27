package pm.scanner;

import java.util.regex.Pattern;

/**
 * Known secret patterns for detecting hardcoded credentials in .env files.
 *
 * @author SoftDryzz
 * @version 1.7.1
 * @since 1.7.1
 */
public enum SecretPattern {

    AWS_ACCESS_KEY("AWS Access Key", "AKIA[0-9A-Z]{16}"),
    GITHUB_TOKEN("GitHub Token", "gh[ps]_[A-Za-z0-9_]{36,}"),
    GITHUB_FINE_GRAINED("GitHub Fine-Grained Token", "github_pat_[A-Za-z0-9_]{22,}"),
    SLACK_TOKEN("Slack Token", "xox[baprs]-[0-9A-Za-z\\-]+"),
    GENERIC_SECRET("Generic Secret", null);

    private final String displayName;
    private final Pattern regex;

    SecretPattern(String displayName, String regex) {
        this.displayName = displayName;
        this.regex = regex != null ? Pattern.compile(regex) : null;
    }

    public String displayName() {
        return displayName;
    }

    public Pattern regex() {
        return regex;
    }

    /**
     * Tests if a value matches this pattern's regex.
     * GENERIC_SECRET always returns false (handled separately).
     */
    public boolean matches(String value) {
        return regex != null && regex.matcher(value).find();
    }
}
