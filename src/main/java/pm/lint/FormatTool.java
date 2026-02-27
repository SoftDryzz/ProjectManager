package pm.lint;

/**
 * Supported formatting tools that ProjectManager can detect and run.
 * Each tool has a human-readable display name and the CLI command to execute.
 */
public enum FormatTool {

    PRETTIER("Prettier", "npx prettier --write ."),
    CARGO_FMT("cargo fmt", "cargo fmt"),
    GOFMT("gofmt", "gofmt -w ."),
    RUFF_FORMAT("Ruff Format", "ruff format ."),
    BLACK("Black", "black ."),
    DART_FORMAT("dart format", "dart format ."),
    DOTNET_FORMAT("dotnet format", "dotnet format"),
    SPOTLESS_MAVEN("Spotless", "mvn spotless:apply"),
    SPOTLESS_GRADLE("Spotless", "gradle spotlessApply");

    private final String displayName;
    private final String command;

    FormatTool(String displayName, String command) {
        this.displayName = displayName;
        this.command = command;
    }

    public String displayName() {
        return displayName;
    }

    public String command() {
        return command;
    }
}
