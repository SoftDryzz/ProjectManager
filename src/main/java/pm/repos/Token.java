package pm.repos;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A GitHub token and where it came from. {@link #toString()} never includes
 * the value, so a token can't leak through logging or string concatenation.
 *
 * @param value  the token (only ever sent in the Authorization header)
 * @param source "gh", "GH_TOKEN" or "GITHUB_TOKEN"
 */
public record Token(String value, String source) {

    /** Accepted format; anything else (spaces, CR/LF, huge values) is rejected. */
    private static final Pattern FORMAT = Pattern.compile("[A-Za-z0-9_]{20,255}");

    public Token {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(source, "source");
    }

    static boolean isWellFormed(String value) {
        return value != null && FORMAT.matcher(value).matches();
    }

    @Override
    public String toString() {
        return "Token[source=" + source + "]";
    }
}
