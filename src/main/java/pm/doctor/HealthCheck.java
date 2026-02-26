package pm.doctor;

/**
 * Represents the result of a single health check on a project.
 *
 * @param name           short identifier (e.g. "gitignore", "readme")
 * @param passed         whether the check passed
 * @param description    human-readable label shown in the report
 * @param recommendation actionable fix suggestion (shown only when failed)
 * @version 1.6.1
 */
public record HealthCheck(
        String name,
        boolean passed,
        String description,
        String recommendation
) {}
