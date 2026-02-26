package pm.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import pm.detector.ProjectType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DependencyAuditor")
class DependencyAuditorTest {

    // ============================================================
    // TOOL MAPPING
    // ============================================================

    @Nested
    @DisplayName("Tool Mapping")
    class ToolMapping {

        @Test
        @DisplayName("auditCommand returns correct command for supported types")
        void auditCommandSupported() {
            assertEquals("npm audit --json", DependencyAuditor.auditCommand(ProjectType.NODEJS));
            assertEquals("pnpm audit --json", DependencyAuditor.auditCommand(ProjectType.PNPM));
            assertEquals("yarn audit --json", DependencyAuditor.auditCommand(ProjectType.YARN));
            assertEquals("cargo audit --json", DependencyAuditor.auditCommand(ProjectType.RUST));
            assertEquals("govulncheck -json ./...", DependencyAuditor.auditCommand(ProjectType.GO));
            assertEquals("pip-audit --format=json", DependencyAuditor.auditCommand(ProjectType.PYTHON));
            assertNotNull(DependencyAuditor.auditCommand(ProjectType.DOTNET));
        }

        @Test
        @DisplayName("auditCommand returns null for unsupported types")
        void auditCommandUnsupported() {
            assertNull(DependencyAuditor.auditCommand(ProjectType.MAVEN));
            assertNull(DependencyAuditor.auditCommand(ProjectType.GRADLE));
            assertNull(DependencyAuditor.auditCommand(ProjectType.BUN));
            assertNull(DependencyAuditor.auditCommand(ProjectType.FLUTTER));
            assertNull(DependencyAuditor.auditCommand(ProjectType.DOCKER));
            assertNull(DependencyAuditor.auditCommand(ProjectType.UNKNOWN));
        }

        @Test
        @DisplayName("toolDisplayName returns name for supported types")
        void toolDisplayName() {
            assertEquals("npm audit", DependencyAuditor.toolDisplayName(ProjectType.NODEJS));
            assertEquals("cargo audit", DependencyAuditor.toolDisplayName(ProjectType.RUST));
            assertEquals("govulncheck", DependencyAuditor.toolDisplayName(ProjectType.GO));
            assertEquals("pip-audit", DependencyAuditor.toolDisplayName(ProjectType.PYTHON));
            assertNull(DependencyAuditor.toolDisplayName(ProjectType.MAVEN));
        }

        @Test
        @DisplayName("fixSuggestion returns non-empty for supported types")
        void fixSuggestion() {
            assertFalse(DependencyAuditor.fixSuggestion(ProjectType.NODEJS).isEmpty());
            assertFalse(DependencyAuditor.fixSuggestion(ProjectType.RUST).isEmpty());
            assertFalse(DependencyAuditor.fixSuggestion(ProjectType.GO).isEmpty());
            assertFalse(DependencyAuditor.fixSuggestion(ProjectType.PYTHON).isEmpty());
            assertFalse(DependencyAuditor.fixSuggestion(ProjectType.DOTNET).isEmpty());
        }

        @Test
        @DisplayName("installInstruction only for separate-install tools")
        void installInstruction() {
            assertNotNull(DependencyAuditor.installInstruction(ProjectType.RUST));
            assertNotNull(DependencyAuditor.installInstruction(ProjectType.GO));
            assertNotNull(DependencyAuditor.installInstruction(ProjectType.PYTHON));
            assertNull(DependencyAuditor.installInstruction(ProjectType.NODEJS));
            assertNull(DependencyAuditor.installInstruction(ProjectType.DOTNET));
        }

        @Test
        @DisplayName("noToolMessage returns message for Maven and Gradle")
        void noToolMessage() {
            assertNotNull(DependencyAuditor.noToolMessage(ProjectType.MAVEN));
            assertNotNull(DependencyAuditor.noToolMessage(ProjectType.GRADLE));
            assertNull(DependencyAuditor.noToolMessage(ProjectType.NODEJS));
        }

        @Test
        @DisplayName("isSkippedType for Bun, Flutter, Docker, Unknown")
        void isSkippedType() {
            assertTrue(DependencyAuditor.isSkippedType(ProjectType.BUN));
            assertTrue(DependencyAuditor.isSkippedType(ProjectType.FLUTTER));
            assertTrue(DependencyAuditor.isSkippedType(ProjectType.DOCKER));
            assertTrue(DependencyAuditor.isSkippedType(ProjectType.UNKNOWN));
            assertFalse(DependencyAuditor.isSkippedType(ProjectType.NODEJS));
            assertFalse(DependencyAuditor.isSkippedType(ProjectType.MAVEN));
        }
    }

    // ============================================================
    // NPM PARSER
    // ============================================================

    @Nested
    @DisplayName("npm JSON Parser")
    class NpmParser {

        @Test
        @DisplayName("parses standard npm audit JSON")
        void standard() {
            String json = """
                    {
                      "vulnerabilities": {
                        "lodash": { "severity": "high" },
                        "express": { "severity": "moderate" },
                        "axios": { "severity": "critical" }
                      }
                    }
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parseNpmJson(json);
            assertEquals(3, vulns.size());
            assertTrue(vulns.stream().anyMatch(v -> "lodash".equals(v.packageName()) && v.severity() == Severity.HIGH));
            assertTrue(vulns.stream().anyMatch(v -> "axios".equals(v.packageName()) && v.severity() == Severity.CRITICAL));
            assertTrue(vulns.stream().anyMatch(v -> "express".equals(v.packageName()) && v.severity() == Severity.MEDIUM));
        }

        @Test
        @DisplayName("empty vulnerabilities object returns empty list")
        void empty() {
            String json = """
                    { "vulnerabilities": {} }
                    """;
            assertTrue(DependencyAuditor.parseNpmJson(json).isEmpty());
        }

        @Test
        @DisplayName("missing vulnerabilities key returns empty list")
        void missingKey() {
            String json = """
                    { "metadata": { "dependencies": 42 } }
                    """;
            assertTrue(DependencyAuditor.parseNpmJson(json).isEmpty());
        }

        @Test
        @DisplayName("malformed JSON returns empty list")
        void malformed() {
            assertTrue(DependencyAuditor.parseNpmJson("not json").isEmpty());
            assertTrue(DependencyAuditor.parseNpmJson("").isEmpty());
        }

        @Test
        @DisplayName("single vulnerability with all severity levels")
        void severityMapping() {
            String json = """
                    {
                      "vulnerabilities": {
                        "pkg-low": { "severity": "low" },
                        "pkg-mod": { "severity": "moderate" },
                        "pkg-high": { "severity": "high" },
                        "pkg-crit": { "severity": "critical" }
                      }
                    }
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parseNpmJson(json);
            assertEquals(4, vulns.size());
        }
    }

    // ============================================================
    // PNPM PARSER
    // ============================================================

    @Nested
    @DisplayName("pnpm JSON Parser")
    class PnpmParser {

        @Test
        @DisplayName("parses pnpm audit JSON (same format as npm)")
        void standard() {
            String json = """
                    {
                      "vulnerabilities": {
                        "vue": { "severity": "low" }
                      }
                    }
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parsePnpmJson(json);
            assertEquals(1, vulns.size());
            assertEquals("vue", vulns.get(0).packageName());
            assertEquals(Severity.LOW, vulns.get(0).severity());
        }

        @Test
        @DisplayName("empty vulnerabilities returns empty list")
        void empty() {
            assertTrue(DependencyAuditor.parsePnpmJson("{ \"vulnerabilities\": {} }").isEmpty());
        }

        @Test
        @DisplayName("malformed JSON returns empty list")
        void malformed() {
            assertTrue(DependencyAuditor.parsePnpmJson("broken").isEmpty());
        }
    }

    // ============================================================
    // YARN PARSER
    // ============================================================

    @Nested
    @DisplayName("Yarn JSON Parser (NDJSON)")
    class YarnParser {

        @Test
        @DisplayName("parses NDJSON with auditAdvisory lines")
        void standard() {
            String json = """
                    {"type":"info","data":"No lockfile found."}
                    {"type":"auditAdvisory","data":{"advisory":{"id":1234,"title":"Prototype Pollution","severity":"high","module_name":"lodash"}}}
                    {"type":"auditAdvisory","data":{"advisory":{"id":5678,"title":"XSS","severity":"moderate","module_name":"marked"}}}
                    {"type":"auditSummary","data":{"vulnerabilities":{"high":1,"moderate":1}}}
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parseYarnJson(json);
            assertEquals(2, vulns.size());
            assertTrue(vulns.stream().anyMatch(v -> "lodash".equals(v.packageName()) && v.severity() == Severity.HIGH));
            assertTrue(vulns.stream().anyMatch(v -> "marked".equals(v.packageName()) && v.severity() == Severity.MEDIUM));
        }

        @Test
        @DisplayName("skips non-auditAdvisory lines")
        void skipNonAdvisory() {
            String json = """
                    {"type":"info","data":"some info"}
                    {"type":"auditSummary","data":{"vulnerabilities":{}}}
                    """;
            assertTrue(DependencyAuditor.parseYarnJson(json).isEmpty());
        }

        @Test
        @DisplayName("empty output returns empty list")
        void empty() {
            assertTrue(DependencyAuditor.parseYarnJson("").isEmpty());
            assertTrue(DependencyAuditor.parseYarnJson("\n\n").isEmpty());
        }

        @Test
        @DisplayName("malformed lines are skipped gracefully")
        void malformedLines() {
            String json = """
                    not json at all
                    {"type":"auditAdvisory","data":{"advisory":{"id":1,"title":"Bug","severity":"low","module_name":"pkg"}}}
                    another bad line
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parseYarnJson(json);
            assertEquals(1, vulns.size());
            assertEquals("pkg", vulns.get(0).packageName());
        }
    }

    // ============================================================
    // CARGO PARSER
    // ============================================================

    @Nested
    @DisplayName("Cargo JSON Parser")
    class CargoParser {

        @Test
        @DisplayName("parses cargo audit JSON with advisories")
        void standard() {
            String json = """
                    {
                      "vulnerabilities": {
                        "found": true,
                        "count": 2,
                        "list": [
                          {
                            "advisory": {
                              "id": "RUSTSEC-2024-001",
                              "title": "Memory safety issue",
                              "package": "tokio"
                            }
                          },
                          {
                            "advisory": {
                              "id": "RUSTSEC-2024-002",
                              "title": "Denial of service",
                              "package": "hyper"
                            }
                          }
                        ]
                      }
                    }
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parseCargoJson(json);
            assertEquals(2, vulns.size());
            assertEquals("RUSTSEC-2024-001", vulns.get(0).id());
            assertEquals("tokio", vulns.get(0).packageName());
            assertEquals("hyper", vulns.get(1).packageName());
        }

        @Test
        @DisplayName("empty list returns empty list")
        void emptyList() {
            String json = """
                    { "vulnerabilities": { "found": false, "count": 0, "list": [] } }
                    """;
            assertTrue(DependencyAuditor.parseCargoJson(json).isEmpty());
        }

        @Test
        @DisplayName("missing vulnerabilities key returns empty list")
        void missingKey() {
            String json = """
                    { "database": { "advisory-count": 500 } }
                    """;
            assertTrue(DependencyAuditor.parseCargoJson(json).isEmpty());
        }

        @Test
        @DisplayName("malformed JSON returns empty list")
        void malformed() {
            assertTrue(DependencyAuditor.parseCargoJson("not valid json").isEmpty());
        }
    }

    // ============================================================
    // GOVULNCHECK PARSER
    // ============================================================

    @Nested
    @DisplayName("govulncheck JSON Parser (NDJSON)")
    class GovulncheckParser {

        @Test
        @DisplayName("parses finding entries")
        void standard() {
            String json = """
                    {"osv":{"id":"GO-2024-001"}}
                    {"finding":{"osv":"GO-2024-002","trace":[{"module":"golang.org/x/net"}]}}
                    {"finding":{"osv":"GO-2024-003","trace":[{"module":"golang.org/x/text"}]}}
                    {"message":{"osv":"GO-2024-004"}}
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parseGovulncheckJson(json);
            assertEquals(2, vulns.size());
            assertEquals("GO-2024-002", vulns.get(0).id());
            assertEquals("GO-2024-003", vulns.get(1).id());
        }

        @Test
        @DisplayName("skips entries without finding key")
        void skipNonFinding() {
            String json = """
                    {"config":{"go_version":"1.21"}}
                    {"osv":{"id":"GO-2024-001"}}
                    """;
            assertTrue(DependencyAuditor.parseGovulncheckJson(json).isEmpty());
        }

        @Test
        @DisplayName("empty output returns empty list")
        void empty() {
            assertTrue(DependencyAuditor.parseGovulncheckJson("").isEmpty());
        }

        @Test
        @DisplayName("all findings default to HIGH severity")
        void defaultSeverity() {
            String json = """
                    {"finding":{"osv":"GO-2024-001"}}
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parseGovulncheckJson(json);
            assertEquals(1, vulns.size());
            assertEquals(Severity.HIGH, vulns.get(0).severity());
        }
    }

    // ============================================================
    // PIP-AUDIT PARSER
    // ============================================================

    @Nested
    @DisplayName("pip-audit JSON Parser")
    class PipAuditParser {

        @Test
        @DisplayName("parses array with vulnerable packages")
        void standard() {
            String json = """
                    [
                      {
                        "name": "requests",
                        "version": "2.25.0",
                        "vulns": [
                          { "id": "PYSEC-2023-001", "fix_versions": ["2.31.0"] },
                          { "id": "PYSEC-2023-002", "fix_versions": ["2.30.0"] }
                        ]
                      },
                      {
                        "name": "flask",
                        "version": "2.0.0",
                        "vulns": [
                          { "id": "PYSEC-2023-100", "fix_versions": ["2.3.0"] }
                        ]
                      }
                    ]
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parsePipAuditJson(json);
            assertEquals(3, vulns.size());
            assertEquals(2, vulns.stream().filter(v -> "requests".equals(v.packageName())).count());
            assertEquals(1, vulns.stream().filter(v -> "flask".equals(v.packageName())).count());
        }

        @Test
        @DisplayName("packages with no vulns are skipped")
        void noVulns() {
            String json = """
                    [
                      { "name": "safe-pkg", "version": "1.0.0", "vulns": [] },
                      { "name": "also-safe", "version": "2.0.0" }
                    ]
                    """;
            assertTrue(DependencyAuditor.parsePipAuditJson(json).isEmpty());
        }

        @Test
        @DisplayName("empty array returns empty list")
        void empty() {
            assertTrue(DependencyAuditor.parsePipAuditJson("[]").isEmpty());
        }

        @Test
        @DisplayName("malformed JSON returns empty list")
        void malformed() {
            assertTrue(DependencyAuditor.parsePipAuditJson("not json").isEmpty());
        }
    }

    // ============================================================
    // DOTNET PARSER
    // ============================================================

    @Nested
    @DisplayName("dotnet JSON Parser")
    class DotnetParser {

        @Test
        @DisplayName("parses nested project/framework/package structure")
        void standard() {
            String json = """
                    {
                      "projects": [
                        {
                          "path": "MyApp.csproj",
                          "frameworks": [
                            {
                              "framework": "net8.0",
                              "topLevelPackages": [
                                { "id": "System.Text.Json", "severity": "High" },
                                { "id": "Microsoft.Data.SqlClient", "severity": "Critical" }
                              ]
                            }
                          ]
                        }
                      ]
                    }
                    """;
            List<Vulnerability> vulns = DependencyAuditor.parseDotnetJson(json);
            assertEquals(2, vulns.size());
            assertTrue(vulns.stream().anyMatch(v ->
                    "System.Text.Json".equals(v.packageName()) && v.severity() == Severity.HIGH));
            assertTrue(vulns.stream().anyMatch(v ->
                    "Microsoft.Data.SqlClient".equals(v.packageName()) && v.severity() == Severity.CRITICAL));
        }

        @Test
        @DisplayName("missing frameworks key returns empty list")
        void missingFrameworks() {
            String json = """
                    { "projects": [ { "path": "MyApp.csproj" } ] }
                    """;
            assertTrue(DependencyAuditor.parseDotnetJson(json).isEmpty());
        }

        @Test
        @DisplayName("empty projects array returns empty list")
        void emptyProjects() {
            String json = """
                    { "projects": [] }
                    """;
            assertTrue(DependencyAuditor.parseDotnetJson(json).isEmpty());
        }

        @Test
        @DisplayName("malformed JSON returns empty list")
        void malformed() {
            assertTrue(DependencyAuditor.parseDotnetJson("broken").isEmpty());
        }
    }

    // ============================================================
    // AUDIT INTEGRATION (status routing)
    // ============================================================

    @Nested
    @DisplayName("Audit Status Routing")
    class AuditIntegration {

        private final DependencyAuditor auditor = new DependencyAuditor(
                new pm.executor.CommandExecutor());

        @Test
        @DisplayName("BUN returns SKIPPED")
        void bunSkipped() {
            AuditReport report = auditor.audit(ProjectType.BUN, java.nio.file.Path.of("."));
            assertEquals(AuditReport.Status.SKIPPED, report.status());
        }

        @Test
        @DisplayName("FLUTTER returns SKIPPED")
        void flutterSkipped() {
            AuditReport report = auditor.audit(ProjectType.FLUTTER, java.nio.file.Path.of("."));
            assertEquals(AuditReport.Status.SKIPPED, report.status());
        }

        @Test
        @DisplayName("DOCKER returns SKIPPED")
        void dockerSkipped() {
            AuditReport report = auditor.audit(ProjectType.DOCKER, java.nio.file.Path.of("."));
            assertEquals(AuditReport.Status.SKIPPED, report.status());
        }

        @Test
        @DisplayName("UNKNOWN returns SKIPPED")
        void unknownSkipped() {
            AuditReport report = auditor.audit(ProjectType.UNKNOWN, java.nio.file.Path.of("."));
            assertEquals(AuditReport.Status.SKIPPED, report.status());
        }

        @Test
        @DisplayName("MAVEN returns NO_TOOL with OWASP recommendation")
        void mavenNoTool() {
            AuditReport report = auditor.audit(ProjectType.MAVEN, java.nio.file.Path.of("."));
            assertEquals(AuditReport.Status.NO_TOOL, report.status());
            assertTrue(report.message().contains("OWASP"));
        }

        @Test
        @DisplayName("GRADLE returns NO_TOOL with OWASP recommendation")
        void gradleNoTool() {
            AuditReport report = auditor.audit(ProjectType.GRADLE, java.nio.file.Path.of("."));
            assertEquals(AuditReport.Status.NO_TOOL, report.status());
            assertTrue(report.message().contains("OWASP"));
        }
    }
}
