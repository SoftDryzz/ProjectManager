package pm.audit;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Result of auditing a single project's dependencies.
 *
 * @param status          outcome of the audit
 * @param vulnerabilities list of found vulnerabilities (empty if clean or skipped)
 * @param message         human-readable status detail or error info
 * @param suggestion      actionable fix or install command for the user
 *
 * @author SoftDryzz
 * @version 1.6.3
 * @since 1.6.3
 */
public record AuditReport(
        Status status,
        List<Vulnerability> vulnerabilities,
        String message,
        String suggestion
) {

    /**
     * Outcome status of a dependency audit.
     */
    public enum Status {
        /** Audit ran successfully, no vulnerabilities found. */
        CLEAN,
        /** Audit ran successfully, vulnerabilities found. */
        VULNERABLE,
        /** The audit tool is not installed on this system. */
        TOOL_NOT_INSTALLED,
        /** No native audit tool exists for this ecosystem (e.g. Maven, Gradle). */
        NO_TOOL,
        /** Ecosystem skipped — not applicable (e.g. Bun, Flutter, Docker, Unknown). */
        SKIPPED,
        /** Audit tool failed or produced unparseable output. */
        ERROR
    }

    /**
     * Returns a severity-count summary map.
     *
     * @return map of severity → count (only includes severities with count &gt; 0)
     */
    public Map<Severity, Long> severityCounts() {
        return vulnerabilities.stream()
                .collect(Collectors.groupingBy(Vulnerability::severity, Collectors.counting()));
    }

    /**
     * Total number of vulnerabilities found.
     *
     * @return vulnerability count
     */
    public int totalVulnerabilities() {
        return vulnerabilities.size();
    }
}
