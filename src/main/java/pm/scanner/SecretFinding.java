package pm.scanner;

/**
 * Represents a detected secret in an environment file.
 *
 * @param file        relative filename (e.g., ".env", ".env.production")
 * @param key         the environment variable key (e.g., "AWS_SECRET_KEY")
 * @param pattern     the pattern that matched
 * @param maskedValue first 3 chars + "****" (or "****" if short)
 *
 * @author SoftDryzz
 * @version 1.7.1
 * @since 1.7.1
 */
public record SecretFinding(
        String file,
        String key,
        SecretPattern pattern,
        String maskedValue
) {}
