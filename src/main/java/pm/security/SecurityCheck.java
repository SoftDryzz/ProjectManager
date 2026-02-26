package pm.security;

/**
 * Represents the result of a single security check.
 *
 * @param name           short identifier (e.g. "env-gitignore", "dockerfile-root")
 * @param passed         whether the check passed
 * @param description    human-readable label shown in the report
 * @param recommendation actionable fix shown when the check fails
 * @param fixable        whether {@code --fix} can auto-remediate this failure
 *
 * @author SoftDryzz
 * @version 1.6.2
 * @since 1.6.2
 */
public record SecurityCheck(
        String name,
        boolean passed,
        String description,
        String recommendation,
        boolean fixable
) {}
