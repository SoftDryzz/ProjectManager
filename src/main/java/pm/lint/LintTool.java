package pm.lint;

/**
 * Supported lint tools that ProjectManager can detect and run.
 * Each tool has a human-readable display name and the CLI command to execute.
 */
public enum LintTool {

    ESLINT("ESLint", "npx eslint ."),
    CLIPPY("Clippy", "cargo clippy"),
    GO_VET("go vet", "go vet ./..."),
    GOLANGCI_LINT("golangci-lint", "golangci-lint run"),
    RUFF_CHECK("Ruff", "ruff check ."),
    FLAKE8("Flake8", "flake8 ."),
    DART_ANALYZE("dart analyze", "dart analyze"),
    DOTNET_FORMAT_CHECK("dotnet format", "dotnet format --verify-no-changes"),
    CHECKSTYLE_MAVEN("Checkstyle", "mvn checkstyle:check"),
    CHECKSTYLE_GRADLE("Checkstyle", "gradle checkstyleMain");

    private final String displayName;
    private final String command;

    LintTool(String displayName, String command) {
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
