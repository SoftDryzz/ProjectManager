package pm.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuditReport")
class AuditReportTest {

    @Test
    @DisplayName("totalVulnerabilities returns correct count")
    void totalVulnerabilities() {
        var vulns = List.of(
                new Vulnerability("V1", "pkg-a", Severity.HIGH, ""),
                new Vulnerability("V2", "pkg-b", Severity.LOW, ""),
                new Vulnerability("V3", "pkg-c", Severity.CRITICAL, "")
        );
        var report = new AuditReport(AuditReport.Status.VULNERABLE, vulns, "", "");
        assertEquals(3, report.totalVulnerabilities());
    }

    @Test
    @DisplayName("totalVulnerabilities returns 0 for empty list")
    void totalVulnerabilitiesEmpty() {
        var report = new AuditReport(AuditReport.Status.CLEAN, List.of(), "", "");
        assertEquals(0, report.totalVulnerabilities());
    }

    @Test
    @DisplayName("severityCounts groups by severity")
    void severityCounts() {
        var vulns = List.of(
                new Vulnerability("", "a", Severity.HIGH, ""),
                new Vulnerability("", "b", Severity.HIGH, ""),
                new Vulnerability("", "c", Severity.LOW, ""),
                new Vulnerability("", "d", Severity.CRITICAL, "")
        );
        var report = new AuditReport(AuditReport.Status.VULNERABLE, vulns, "", "");

        Map<Severity, Long> counts = report.severityCounts();
        assertEquals(1L, counts.get(Severity.CRITICAL));
        assertEquals(2L, counts.get(Severity.HIGH));
        assertEquals(1L, counts.get(Severity.LOW));
        assertNull(counts.get(Severity.MEDIUM));
    }

    @Test
    @DisplayName("severityCounts returns empty map for no vulnerabilities")
    void severityCountsEmpty() {
        var report = new AuditReport(AuditReport.Status.CLEAN, List.of(), "", "");
        assertTrue(report.severityCounts().isEmpty());
    }

    @Test
    @DisplayName("CLEAN report has expected fields")
    void cleanReport() {
        var report = new AuditReport(AuditReport.Status.CLEAN, List.of(), "No issues", "");
        assertEquals(AuditReport.Status.CLEAN, report.status());
        assertTrue(report.vulnerabilities().isEmpty());
        assertEquals("No issues", report.message());
    }

    @Test
    @DisplayName("VULNERABLE report has vulnerabilities and suggestion")
    void vulnerableReport() {
        var vulns = List.of(new Vulnerability("V1", "pkg", Severity.HIGH, "title"));
        var report = new AuditReport(AuditReport.Status.VULNERABLE, vulns, "", "Run npm audit fix");
        assertEquals(AuditReport.Status.VULNERABLE, report.status());
        assertEquals(1, report.totalVulnerabilities());
        assertEquals("Run npm audit fix", report.suggestion());
    }
}
