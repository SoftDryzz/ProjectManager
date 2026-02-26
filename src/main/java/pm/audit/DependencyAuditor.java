package pm.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pm.detector.ProjectType;
import pm.executor.CommandExecutor;
import pm.executor.CommandExecutor.CapturedOutput;
import pm.util.RuntimeChecker;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runs native ecosystem dependency audit tools and produces a unified report.
 *
 * <p>For each {@link ProjectType}, determines the audit command, checks tool
 * availability, executes the command silently, and parses JSON output into
 * a unified {@link AuditReport}.
 *
 * <p>Supported ecosystems: Node.js (npm), pnpm, Yarn, Rust (cargo-audit),
 * Go (govulncheck), Python (pip-audit), .NET (dotnet).
 *
 * <p>Read-only: never modifies dependency files.
 *
 * @author SoftDryzz
 * @version 1.6.3
 * @since 1.6.3
 */
public final class DependencyAuditor {

    /** Timeout in seconds for audit commands. */
    static final int AUDIT_TIMEOUT = 120;

    private final CommandExecutor executor;

    public DependencyAuditor(CommandExecutor executor) {
        this.executor = executor;
    }

    /**
     * Audits a project's dependencies for known vulnerabilities.
     *
     * @param type        the project type
     * @param projectPath the project root directory
     * @return audit report with status and any vulnerabilities found
     */
    public AuditReport audit(ProjectType type, Path projectPath) {
        // Skip types without any audit tool
        if (isSkippedType(type)) {
            return new AuditReport(AuditReport.Status.SKIPPED, List.of(),
                    "No audit tool available", "");
        }

        // Maven/Gradle: no native tool
        String noToolMsg = noToolMessage(type);
        if (noToolMsg != null) {
            return new AuditReport(AuditReport.Status.NO_TOOL, List.of(),
                    noToolMsg, "");
        }

        // Check if the audit tool is installed
        if (!isAuditToolAvailable(type)) {
            String install = installInstruction(type);
            String toolName = toolDisplayName(type);
            return new AuditReport(AuditReport.Status.TOOL_NOT_INSTALLED, List.of(),
                    toolName + " not installed",
                    install != null ? install : "");
        }

        // Run the audit command
        try {
            String command = auditCommand(type);
            CapturedOutput output = executor.captureOutput(command, projectPath, AUDIT_TIMEOUT);

            String stdout = output.stdout();
            if (stdout == null || stdout.isBlank()) {
                return new AuditReport(AuditReport.Status.ERROR, List.of(),
                        "Audit tool produced no output (exit code: " + output.exitCode() + ")", "");
            }

            List<Vulnerability> vulns = parseOutput(type, stdout);

            if (vulns.isEmpty()) {
                return new AuditReport(AuditReport.Status.CLEAN, List.of(),
                        "No vulnerabilities found", "");
            }

            return new AuditReport(AuditReport.Status.VULNERABLE, vulns,
                    "", fixSuggestion(type));

        } catch (Exception e) {
            return new AuditReport(AuditReport.Status.ERROR, List.of(),
                    "Audit failed: " + e.getMessage(), "");
        }
    }

    // ============================================================
    // TOOL MAPPING
    // ============================================================

    /**
     * Returns the audit command for a project type.
     *
     * @return command string, or null if not supported
     */
    static String auditCommand(ProjectType type) {
        return switch (type) {
            case NODEJS -> "npm audit --json";
            case PNPM -> "pnpm audit --json";
            case YARN -> "yarn audit --json";
            case RUST -> "cargo audit --json";
            case GO -> "govulncheck -json ./...";
            case PYTHON -> "pip-audit --format=json";
            case DOTNET -> "dotnet list package --vulnerable --format json --output-version 1";
            default -> null;
        };
    }

    /**
     * Returns a user-facing display name for the audit tool.
     *
     * @param type the project type
     * @return display name (e.g. "npm audit"), or null if not applicable
     */
    public static String toolDisplayName(ProjectType type) {
        return switch (type) {
            case NODEJS -> "npm audit";
            case PNPM -> "pnpm audit";
            case YARN -> "yarn audit";
            case RUST -> "cargo audit";
            case GO -> "govulncheck";
            case PYTHON -> "pip-audit";
            case DOTNET -> "dotnet audit";
            default -> null;
        };
    }

    /**
     * Checks if the audit tool is available on this system.
     */
    static boolean isAuditToolAvailable(ProjectType type) {
        return switch (type) {
            case NODEJS -> RuntimeChecker.getVersion("npm", "--version") != null;
            case PNPM -> RuntimeChecker.getVersion("pnpm", "--version") != null;
            case YARN -> RuntimeChecker.getVersion("yarn", "--version") != null;
            case RUST -> RuntimeChecker.getVersion("cargo", "audit --version") != null;
            case GO -> RuntimeChecker.getVersion("govulncheck", "-h") != null;
            case PYTHON -> RuntimeChecker.getVersion("pip-audit", "--version") != null;
            case DOTNET -> RuntimeChecker.getVersion("dotnet", "--version") != null;
            default -> false;
        };
    }

    /**
     * Returns install instructions for tools that require separate installation.
     *
     * @return install command string, or null for bundled tools
     */
    static String installInstruction(ProjectType type) {
        return switch (type) {
            case RUST -> "cargo install cargo-audit";
            case GO -> "go install golang.org/x/vuln/cmd/govulncheck@latest";
            case PYTHON -> "pip install pip-audit";
            default -> null;
        };
    }

    /**
     * Returns a fix suggestion shown when vulnerabilities are found.
     */
    static String fixSuggestion(ProjectType type) {
        return switch (type) {
            case NODEJS -> "Run 'npm audit fix' to resolve";
            case PNPM -> "Run 'pnpm audit --fix' to resolve";
            case YARN -> "Review and update affected packages";
            case RUST -> "Run 'cargo update' to update dependencies";
            case GO -> "Run 'go get -u' to update dependencies";
            case PYTHON -> "Run 'pip-audit --fix' to resolve";
            case DOTNET -> "Update affected packages manually";
            default -> "";
        };
    }

    /**
     * Returns an informative message for types with no native audit tool.
     *
     * @return message string, or null if not a no-tool type
     */
    static String noToolMessage(ProjectType type) {
        return switch (type) {
            case MAVEN, GRADLE -> "Consider: OWASP dependency-check plugin";
            default -> null;
        };
    }

    /**
     * Checks if a type should be skipped entirely.
     */
    static boolean isSkippedType(ProjectType type) {
        return switch (type) {
            case BUN, FLUTTER, DOCKER, UNKNOWN -> true;
            default -> false;
        };
    }

    // ============================================================
    // PARSER DISPATCH
    // ============================================================

    private List<Vulnerability> parseOutput(ProjectType type, String json) {
        try {
            return switch (type) {
                case NODEJS -> parseNpmJson(json);
                case PNPM -> parsePnpmJson(json);
                case YARN -> parseYarnJson(json);
                case RUST -> parseCargoJson(json);
                case GO -> parseGovulncheckJson(json);
                case PYTHON -> parsePipAuditJson(json);
                case DOTNET -> parseDotnetJson(json);
                default -> List.of();
            };
        } catch (Exception e) {
            return List.of();
        }
    }

    // ============================================================
    // JSON PARSERS
    // ============================================================

    /**
     * Parses npm audit --json output.
     *
     * <p>Structure: {@code { "vulnerabilities": { "pkg": { "severity": "high" } } }}
     */
    static List<Vulnerability> parseNpmJson(String json) {
        List<Vulnerability> vulns = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("vulnerabilities")) return vulns;

            JsonObject vulnerabilities = root.getAsJsonObject("vulnerabilities");
            for (Map.Entry<String, JsonElement> entry : vulnerabilities.entrySet()) {
                String pkgName = entry.getKey();
                JsonObject vuln = entry.getValue().getAsJsonObject();
                String severity = vuln.has("severity") ? vuln.get("severity").getAsString() : "medium";

                vulns.add(new Vulnerability("", pkgName, Severity.from(severity), ""));
            }
        } catch (Exception e) {
            // Malformed JSON — return what we have
        }
        return vulns;
    }

    /**
     * Parses pnpm audit --json output. Same structure as npm.
     */
    static List<Vulnerability> parsePnpmJson(String json) {
        return parseNpmJson(json);
    }

    /**
     * Parses yarn audit --json output (NDJSON format).
     *
     * <p>Each line is a JSON object. Lines with {@code "type": "auditAdvisory"}
     * contain vulnerability data at {@code data.advisory}.
     */
    static List<Vulnerability> parseYarnJson(String json) {
        List<Vulnerability> vulns = new ArrayList<>();
        for (String line : json.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            try {
                JsonObject obj = JsonParser.parseString(trimmed).getAsJsonObject();
                if (!"auditAdvisory".equals(getStringOr(obj, "type", ""))) continue;

                JsonObject data = obj.getAsJsonObject("data");
                if (data == null) continue;
                JsonObject advisory = data.getAsJsonObject("advisory");
                if (advisory == null) continue;

                String title = getStringOr(advisory, "title", "");
                String severity = getStringOr(advisory, "severity", "medium");
                String moduleName = getStringOr(advisory, "module_name", "");
                String id = advisory.has("id") ? String.valueOf(advisory.get("id").getAsInt()) : "";

                vulns.add(new Vulnerability(id, moduleName, Severity.from(severity), title));
            } catch (Exception ignored) {
                // Skip unparseable lines
            }
        }
        return vulns;
    }

    /**
     * Parses cargo audit --json output.
     *
     * <p>Structure: {@code { "vulnerabilities": { "list": [ { "advisory": { ... } } ] } }}
     */
    static List<Vulnerability> parseCargoJson(String json) {
        List<Vulnerability> vulns = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("vulnerabilities")) return vulns;

            JsonObject vulnsObj = root.getAsJsonObject("vulnerabilities");
            if (!vulnsObj.has("list")) return vulns;

            JsonArray list = vulnsObj.getAsJsonArray("list");
            for (JsonElement el : list) {
                JsonObject item = el.getAsJsonObject();
                JsonObject advisory = item.getAsJsonObject("advisory");
                if (advisory == null) continue;

                String id = getStringOr(advisory, "id", "");
                String title = getStringOr(advisory, "title", "");
                String pkgName = getStringOr(advisory, "package", "");

                vulns.add(new Vulnerability(id, pkgName, Severity.HIGH, title));
            }
        } catch (Exception e) {
            // Malformed JSON — return what we have
        }
        return vulns;
    }

    /**
     * Parses govulncheck -json output (NDJSON format).
     *
     * <p>Each line is a JSON object. Lines with a {@code "finding"} key
     * contain vulnerability data with an OSV identifier.
     */
    static List<Vulnerability> parseGovulncheckJson(String json) {
        List<Vulnerability> vulns = new ArrayList<>();
        for (String line : json.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            try {
                JsonObject obj = JsonParser.parseString(trimmed).getAsJsonObject();
                if (!obj.has("finding")) continue;

                JsonObject finding = obj.getAsJsonObject("finding");
                String osv = getStringOr(finding, "osv", "");

                vulns.add(new Vulnerability(osv, "", Severity.HIGH, ""));
            } catch (Exception ignored) {
                // Skip non-JSON or unparseable lines
            }
        }
        return vulns;
    }

    /**
     * Parses pip-audit --format=json output.
     *
     * <p>Structure: {@code [ { "name": "pkg", "vulns": [ { "id": "PYSEC-..." } ] } ]}
     */
    static List<Vulnerability> parsePipAuditJson(String json) {
        List<Vulnerability> vulns = new ArrayList<>();
        try {
            JsonArray root = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement el : root) {
                JsonObject pkg = el.getAsJsonObject();
                String name = getStringOr(pkg, "name", "");

                if (!pkg.has("vulns")) continue;
                JsonArray pkgVulns = pkg.getAsJsonArray("vulns");

                for (JsonElement vulnEl : pkgVulns) {
                    JsonObject vuln = vulnEl.getAsJsonObject();
                    String id = getStringOr(vuln, "id", "");

                    vulns.add(new Vulnerability(id, name, Severity.HIGH, ""));
                }
            }
        } catch (Exception e) {
            // Malformed JSON — return what we have
        }
        return vulns;
    }

    /**
     * Parses dotnet list package --vulnerable --format json output.
     *
     * <p>Structure: {@code { "projects": [ { "frameworks": [ { "topLevelPackages": [...] } ] } ] }}
     */
    static List<Vulnerability> parseDotnetJson(String json) {
        List<Vulnerability> vulns = new ArrayList<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("projects")) return vulns;

            JsonArray projects = root.getAsJsonArray("projects");
            for (JsonElement projEl : projects) {
                JsonObject proj = projEl.getAsJsonObject();
                if (!proj.has("frameworks")) continue;

                JsonArray frameworks = proj.getAsJsonArray("frameworks");
                for (JsonElement fwEl : frameworks) {
                    JsonObject fw = fwEl.getAsJsonObject();
                    if (!fw.has("topLevelPackages")) continue;

                    JsonArray packages = fw.getAsJsonArray("topLevelPackages");
                    for (JsonElement pkgEl : packages) {
                        JsonObject pkg = pkgEl.getAsJsonObject();
                        String pkgId = getStringOr(pkg, "id", "");
                        String severity = getStringOr(pkg, "severity", "medium");

                        vulns.add(new Vulnerability("", pkgId, Severity.from(severity), ""));
                    }
                }
            }
        } catch (Exception e) {
            // Malformed JSON — return what we have
        }
        return vulns;
    }

    // ============================================================
    // UTILITIES
    // ============================================================

    private static String getStringOr(JsonObject obj, String key, String defaultValue) {
        return obj.has(key) && !obj.get(key).isJsonNull()
                ? obj.get(key).getAsString()
                : defaultValue;
    }
}
