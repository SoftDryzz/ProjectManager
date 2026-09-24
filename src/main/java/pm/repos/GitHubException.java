package pm.repos;

import java.time.Instant;
import java.util.Optional;

/**
 * A failed GitHub request. Messages are written by pm and never include the
 * token or response bodies.
 *
 * @author SoftDryzz
 * @version 2.1.0
 * @since 2.1.0
 */
public final class GitHubException extends Exception {

    public enum Kind { UNAUTHORIZED, RATE_LIMITED, FORBIDDEN, NOT_FOUND, BAD_RESPONSE, REFUSED, NETWORK }

    private final Kind kind;
    private final Instant retryAt;

    GitHubException(Kind kind, String message, Instant retryAt, Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.retryAt = retryAt;
    }

    public Kind kind() {
        return kind;
    }

    /** When a rate limit ends, if GitHub said so. */
    public Optional<Instant> retryAt() {
        return Optional.ofNullable(retryAt);
    }
}
