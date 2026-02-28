# ProjectManager - Roadmap

> Complete version history and planned features.
>
> Some ideas inspired by analyzing [FindMatch](https://github.com/AXIOM-ZER0/FindMatch), a real-world multi-stack project (Flutter + Rust + Docker + PostgreSQL + Redis).
>
> **Versioning:** Follows [Semantic Versioning](https://semver.org/). Each release corresponds to a GitHub Release with tag `vX.Y.Z` and asset `projectmanager-X.Y.Z.jar`.

---

## v1.0.0 — Initial Release ✅

### Core CLI & project management
First public release. Register, build, run, and test projects from a single CLI tool with automatic project type detection.

| Feature | Status |
|---------|--------|
| `pm add <name> <path>` — register a project | ✅ Done |
| `pm remove <name>` — unregister a project | ✅ Done |
| `pm list` — list all registered projects | ✅ Done |
| `pm info <name>` — show project details | ✅ Done |
| `pm build <name>` — build a project | ✅ Done |
| `pm run <name>` — run a project | ✅ Done |
| `pm test <name>` — test a project | ✅ Done |
| `pm clean <name>` — clean build artifacts | ✅ Done |
| Auto-detect project types: Gradle, Maven, Node.js, .NET, Python | ✅ Done |
| Git integration in `pm info` (branch, last commit, status) | ✅ Done |
| Environment variables per project (`pm env set/get/list/remove/clear`) | ✅ Done |
| Cross-platform install scripts (Windows PowerShell, Linux/Mac bash) | ✅ Done |
| Bilingual documentation (English + Spanish) | ✅ Done |

---

## v1.1.0 — Runtime Checker, Doctor & Unit Tests ✅

### Runtime verification & diagnostics

| Feature | Status |
|---------|--------|
| Runtime checker: detect missing runtimes before executing commands | ✅ Done |
| Friendly error messages with install instructions (winget + download URL) | ✅ Done |
| `pm doctor` — diagnose environment (installed runtimes, project path validation) | ✅ Done |
| 156 unit tests across 14 test classes | ✅ Done |

---

## v1.1.1 — RuntimeChecker Fix ✅

| Feature | Status |
|---------|--------|
| Fix: RuntimeChecker now verifies `gradle` and `mvn` are installed (not just Java) | ✅ Done |
| Friendly error with winget install command and download URL | ✅ Done |

---

## v1.2.0 — Environment Variable Management ✅

### Enhanced `pm env` command

| Feature | Status |
|---------|--------|
| `pm env set` — set environment variables (supports multiple: KEY=VALUE,KEY2=VALUE2) | ✅ Done |
| `pm env get` — get a specific variable value | ✅ Done |
| `pm env list` — list variables with sensitive value masking (`--show` to reveal) | ✅ Done |
| `pm env remove` — remove a specific variable | ✅ Done |
| `pm env clear` — remove all variables | ✅ Done |
| Smart install scripts: auto-find JAR, copy to permanent path | ✅ Done |
| 172 tests, 0 failures | ✅ Done |

---

## v1.3.0 — New Runtimes & Auto-Update ✅

### 5 new project types + automatic updates

| Feature | Status |
|---------|--------|
| Rust (Cargo) — auto-detect `Cargo.toml` | ✅ Done |
| Go — auto-detect `go.mod` | ✅ Done |
| pnpm — auto-detect `pnpm-lock.yaml` | ✅ Done |
| Bun — auto-detect `bun.lockb` / `bun.lock` | ✅ Done |
| Yarn — auto-detect `yarn.lock` | ✅ Done |
| Smart detection: lock file takes priority over generic Node.js | ✅ Done |
| Version check on startup with update notification | ✅ Done |
| `pm update` — download and install latest JAR from GitHub Releases | ✅ Done |
| `pm doctor` checks all 12 runtimes | ✅ Done |
| 219 tests passing | ✅ Done |

---

## v1.3.1 — Flutter/Dart Support ✅

| Feature | Status |
|---------|--------|
| Auto-detect Flutter projects via `pubspec.yaml` | ✅ Done |
| Default commands: `flutter build/run/test/clean` | ✅ Done |
| `pm doctor` checks Flutter SDK installation | ✅ Done |
| 223 tests passing | ✅ Done |

---

## v1.3.2 — Project Refresh & Outdated Detection ✅

### `pm refresh` command
Re-detect project types and update commands for already-registered projects. Solves the problem where projects registered before a new type was added have no commands.

| Feature | Status |
|---------|--------|
| `pm refresh <name>` — refresh a specific project | ✅ Done |
| `pm refresh --all` — refresh all registered projects | ✅ Done |
| Detailed before/after command output | ✅ Done |
| Automatic outdated type hints on `build`, `run`, `test`, `commands`, `info` | ✅ Done |

---

## v1.3.3 — Update Fix & ROADMAP Update ✅

| Feature | Status |
|---------|--------|
| Fix post-update message: "Run any pm command to use the new version" | ✅ Done |
| ROADMAP reorganization with new planned features | ✅ Done |

---

## v1.3.4 — Rename & Path Update ✅

### `pm rename` command
Rename projects and/or update their registered path without losing commands or environment variables.

| Feature | Status |
|---------|--------|
| `pm rename old-name new-name` — rename project | ✅ Done |
| `pm rename name --path /new/path` — update path | ✅ Done |
| `pm rename old-name new-name --path /new/path` — both | ✅ Done |
| Preserves commands, env vars, and project type | ✅ Done |

---

## v1.3.5 — Interactive CLI Support ✅

### `inheritIO` for interactive processes
Fix processes that require user input (interactive menus, prompts, selections) hanging indefinitely. Uses `ProcessBuilder.inheritIO()` to connect stdin/stdout/stderr directly to the user's terminal. Auto-detects TTY presence to fallback to buffered mode in CI/CD.

| Feature | Status |
|---------|--------|
| `executeWithInheritedIO()` method in CommandExecutor | ✅ Done |
| Auto-detect TTY (`System.console()`) for `pm run`, `pm build`, `pm test` | ✅ Done |
| Preserve `ExecutionResult` metrics (exit code, duration) | ✅ Done |
| Graceful fallback to buffered mode if terminal is not a TTY | ✅ Done |

---

## v1.3.6 — Custom Commands ✅

### Custom command management
Allow users to create, remove, and list custom commands per project. Default commands (build, run, test, clean) remain auto-detected — custom commands extend them.

| Feature | Status |
|---------|--------|
| `pm commands <project> add <name> "<command>"` — create a custom command | ✅ Done |
| `pm commands <project> remove <name>` — remove a custom command | ✅ Done |
| `pm commands <project>` — list commands for a specific project (already exists) | ✅ Done |
| `pm commands --all` — list all commands across all registered projects | ✅ Done |
| Custom commands are persisted in `projects.json` alongside defaults | ✅ Done |
| Update existing commands by re-adding with new value | ✅ Done |

---

## v1.3.7 — Error Handling & Data Safety ✅

### Robust project storage
Prevent data loss and eliminate cryptic error messages. The user should never see a Java stack trace or lose their registered projects.

| Feature | Status |
|---------|--------|
| Atomic file writes (write to temp file, then rename) | ✅ Done |
| Automatic backup of `projects.json` before write | ✅ Done |
| Recovery from corrupted JSON (load backup automatically) | ✅ Done |
| Validate required fields on load (null name, path, type) | ✅ Done |
| Graceful handling of invalid `ProjectType` values in JSON | ✅ Done |

### User-friendly error messages
| Feature | Status |
|---------|--------|
| Remove `e.printStackTrace()` from main — no stack traces shown to user | ✅ Done |
| Specific error messages: permissions, disk full, file not found, corrupted JSON | ✅ Done |
| Actionable guidance in error messages (e.g., "Run `pm doctor` to diagnose") | ✅ Done |
| Git operation feedback — show why git info is missing instead of hiding it | ✅ Done |

---

## v1.3.8 — Safe Command Execution ✅

### Path safety in shell commands
Prevent commands from failing when project directories are missing or moved. Warn about shell metacharacters in custom commands.

| Feature | Status |
|---------|--------|
| Validate working directory exists before execution | ✅ Done |
| Clear error message when project directory is missing or moved | ✅ Done |
| Defense-in-depth directory validation in CommandExecutor | ✅ Done |
| Shell metacharacter warning on `pm commands add` | ✅ Done |

---

## v1.3.9 — Robust Auto-Update ✅

### Download integrity and network resilience
Ensure the auto-updater handles edge cases gracefully: partial downloads, redirect loops, and network failures with clear feedback.

| Feature | Status |
|---------|--------|
| Validate downloaded JAR integrity (expected size from API response) | ✅ Done |
| Detect and break redirect loops (max 5 redirects) | ✅ Done |
| Distinguish network errors: timeout vs DNS failure vs firewall | ✅ Done |
| Clear message when offline: "No internet connection — update check skipped" | ✅ Done |
| Prevent partial/corrupted JAR from being installed | ✅ Done |

---

## v1.4.0 — Docker Support ✅

### Docker project detection
Detect Docker Compose projects and configure default commands. Language types always take priority — DOCKER is only assigned when no language-specific type (Gradle, Maven, Node.js, etc.) is detected.

| Feature | Status |
|---------|--------|
| Detect `docker-compose.yml` / `docker-compose.yaml` in project root | ✅ Done |
| New project type: `DOCKER` | ✅ Done |
| Default commands: build, run, stop, clean (docker compose) | ✅ Done |
| Language types take priority over Docker when both exist | ✅ Done |
| Docker runtime check (`pm doctor`, pre-execution) | ✅ Done |
| `stop` classified as default command in `pm commands` output | ✅ Done |
| Separate default/custom commands in `pm commands` output | ✅ Done |

---

## v1.5.0 — Pre-/Post-Command Hooks ✅

### `pm hooks` command
Run custom scripts automatically before or after any command. Hooks are per-project and user-configured.

| Feature | Status |
|---------|--------|
| `pm hooks <project> add pre-<cmd> "<script>"` — add a pre-hook | ✅ Done |
| `pm hooks <project> add post-<cmd> "<script>"` — add a post-hook | ✅ Done |
| `pm hooks <project>` — list all hooks for a project | ✅ Done |
| `pm hooks <project> remove <slot> "<script>"` — remove a hook by exact content | ✅ Done |
| `pm hooks --all` — list hooks for all projects | ✅ Done |
| Multiple hooks per slot (chained execution) | ✅ Done |
| Pre-hook failure aborts the main command | ✅ Done |
| Post-hook failure shows warning only | ✅ Done |
| Hooks execute in the project's working directory | ✅ Done |
| Hooks inherit project environment variables | ✅ Done |
| Fixed 60s timeout for hook scripts | ✅ Done |
| Generic command execution (`pm <cmd> <project>`) with hook support | ✅ Done |

---

## v1.6.0 — Shell Autocompletion ✅

### `pm completions` command

| Feature | Status |
|---------|--------|
| `pm completions bash` — generate Bash completion script | ✅ Done |
| `pm completions zsh` — generate Zsh completion script | ✅ Done |
| `pm completions fish` — generate Fish completion script | ✅ Done |
| `pm completions powershell` — generate PowerShell completion script | ✅ Done |
| Autocomplete top-level commands, project names, subcommands, and flags | ✅ Done |
| Context-aware completions (hook slots, env var keys, project types) | ✅ Done |
| Hidden `--complete` callback skips banner/update for performance | ✅ Done |
| Zero dependencies — generates static shell scripts | ✅ Done |

---

## v1.6.1 — Doctor Health Score ✅

### `pm doctor` expanded

| Feature | Status |
|---------|--------|
| Health score: **A/B/C/D/F** rating based on project best practices | ✅ Done |
| Check: `.gitignore` exists in project root | ✅ Done |
| Check: README present (case-insensitive) | ✅ Done |
| Check: Tests configured (`test` command exists) | ✅ Done |
| Check: CI/CD detected (GitHub Actions, GitLab CI, Jenkins) | ✅ Done |
| Check: Dependencies lockfile present (per project type) | ✅ Done |
| Actionable recommendations per failed check | ✅ Done |
| `pm doctor` — full report with health details per project | ✅ Done |
| `pm doctor --score` — compact grade-only output | ✅ Done |

---

## v1.6.2 — Security Scan ✅

### `pm secure` command

| Feature | Status |
|---------|--------|
| Best practices security scan (filesystem patterns only, no secret management) | ✅ Done |
| Check: Dockerfile runs as non-root user | ✅ Done |
| Check: `.env` files are in `.gitignore` | ✅ Done |
| Check: No hardcoded `http://` URLs in config files (should be `https://`) | ✅ Done |
| Check: Sensitive files (`.pem`, `.key`) are in `.gitignore` | ✅ Done |
| Check: Dependencies lockfile exists | ✅ Done |
| `pm secure` — run all checks and show report | ✅ Done |
| `pm secure --fix` — auto-fix what can be fixed (add entries to `.gitignore`) | ✅ Done |
| Auto-fix creates `.gitignore` if not present | ✅ Done |

---

## v1.6.3 — Dependency Audit ✅

### `pm audit` command

| Feature | Status |
|---------|--------|
| Run native ecosystem audit tools and show unified summary | ✅ Done |
| npm: `npm audit --json` | ✅ Done |
| pnpm: `pnpm audit --json` | ✅ Done |
| Yarn: `yarn audit --json` | ✅ Done |
| Cargo: `cargo audit --json` | ✅ Done |
| Go: `govulncheck -json ./...` | ✅ Done |
| Python: `pip-audit --format=json` | ✅ Done |
| .NET: `dotnet list package --vulnerable --format json` | ✅ Done |
| Maven/Gradle: informative message (recommend OWASP plugin) | ✅ Done |
| Unified severity levels (CRITICAL/HIGH/MEDIUM/LOW) | ✅ Done |
| Graceful handling of missing audit tools with install instructions | ✅ Done |
| New `captureOutput()` in CommandExecutor for silent JSON capture | ✅ Done |
| Read-only — never modifies dependency files | ✅ Done |

> **Important:** `pm audit` is read-only. It reports vulnerabilities and suggests what *could* be updated, but never modifies `package.json`, `Cargo.toml`, or any dependency file.

---

## v1.6.4 — Export & Import ✅

### `pm export` / `pm import` commands
Export all or selected projects to a portable JSON file and import them back on another machine.

| Feature | Status |
|---------|--------|
| `pm export` — export all registered projects to JSON | ✅ Done |
| `pm export name1 name2` — export specific projects | ✅ Done |
| `pm export --file <path>` — custom output file (default: `pm-export.json`) | ✅ Done |
| `pm import <file>` — import projects from an exported file | ✅ Done |
| Self-describing JSON format with version metadata | ✅ Done |
| Skip existing projects on import (never overwrite) | ✅ Done |
| Warn about missing paths on import with `pm rename` hint | ✅ Done |
| Invalid type defaults to UNKNOWN with warning | ✅ Done |
| Shell autocompletion for export/import commands | ✅ Done |

---

## v1.6.5 — CI/CD Detection ✅

### CI/CD awareness

| Feature | Status |
|---------|--------|
| Detect GitHub Actions (`.github/workflows/`) | ✅ Done |
| Detect GitLab CI (`.gitlab-ci.yml`) | ✅ Done |
| Detect Jenkins (`Jenkinsfile`) | ✅ Done |
| Detect Travis CI (`.travis.yml`) | ✅ Done |
| Detect CircleCI (`.circleci/config.yml`) | ✅ Done |
| Show CI/CD providers in `pm info` with workflow count | ✅ Done |
| `pm ci [name]` — show CI dashboard URLs for projects | ✅ Done |
| Parse SSH and HTTPS git remote URLs for dashboard links | ✅ Done |
| Shell autocompletion for `pm ci` command | ✅ Done |

> **Note:** Deployment awareness (fly.toml, vercel.json, etc.) deferred to a later version.

---

## v1.6.6 — Linting & Formatting ✅

### `pm lint` / `pm fmt` commands

| Feature | Status |
|---------|--------|
| `pm lint [name]` — run detected linters on project(s) | ✅ Done |
| `pm fmt [name]` — run detected formatters on project(s) | ✅ Done |
| 10 lint tools: ESLint, Clippy, go vet, golangci-lint, Ruff, Flake8, dart analyze, dotnet format, Checkstyle (Maven/Gradle) | ✅ Done |
| 9 format tools: Prettier, cargo fmt, gofmt, Ruff Format, Black, dart format, dotnet format, Spotless (Maven/Gradle) | ✅ Done |
| Three-tier detection: toolchain-bundled, config-file, binary-check | ✅ Done |
| Run all detected tools in sequence with real-time output | ✅ Done |
| Shell autocompletion for `pm lint` and `pm fmt` commands | ✅ Done |

---

## v1.7.0 — Multi-project Workspaces ✅

### Monorepo & multi-language detection

| Feature | Status |
|---------|--------|
| Multi-language detection: `detectAll()` finds all project types in a directory | ✅ Done |
| Secondary types shown in `pm info` ("Also detected: Docker, Node.js") | ✅ Done |
| Secondary types persisted in `projects.json` (backward compatible) | ✅ Done |
| `pm build --all` — build all registered projects with summary | ✅ Done |
| `pm test --all` — test all registered projects with summary | ✅ Done |
| Continue-on-failure: `--all` runs every project, shows pass/fail summary | ✅ Done |
| Cargo workspace detection (`[workspace]` members in Cargo.toml) | ✅ Done |
| npm/pnpm/yarn workspace detection (array, object, glob patterns) | ✅ Done |
| Gradle multi-project detection (`include()` in settings.gradle/kts) | ✅ Done |
| Go multi-module detection (nested `go.mod` files) | ✅ Done |
| `pm modules [name]` — show workspace modules for project(s) | ✅ Done |
| Workspace module count shown in `pm info` | ✅ Done |
| Shell autocompletion for `modules`, `build --all`, `test --all` | ✅ Done |

---

## v1.7.1 — Environments, Secrets & Databases ✅

### Environment file detection
| Feature | Status |
|---------|--------|
| `pm env files <name>` — list `.env` files in project directory | ✅ Done |
| `pm env show <name> <file>` — show env file contents (masked) | ✅ Done |
| `pm env show <name> <file> --show` — reveal all values | ✅ Done |
| `pm env switch <name> <env-name>` — copy `.env.<name>` to `.env` | ✅ Done |
| Show env files in `pm info` output | ✅ Done |

### Secret scanning
| Feature | Status |
|---------|--------|
| Detect AWS access keys, GitHub tokens, Slack tokens in `.env` files | ✅ Done |
| Generic secret detection (40+ char random values for sensitive keys) | ✅ Done |
| Health check in `pm doctor` (6th check: no exposed secrets) | ✅ Done |
| Security check in `pm secure` (secret-patterns + vaultic detection) | ✅ Done |
| [Vaultic](https://github.com/SoftDryzz/Vaultic) integration: detect installation and `.vaultic/` dir | ✅ Done |

### Database migration awareness
| Feature | Status |
|---------|--------|
| Detect 6 migration tools: Prisma, Alembic, Diesel, Flyway, Liquibase, SQLx | ✅ Done |
| `pm migrate` — list detected tools per project | ✅ Done |
| `pm migrate <name>` — run migration with y/n confirmation | ✅ Done |
| `pm migrate <name> status` — show migration state (read-only) | ✅ Done |
| Show migration tools in `pm info` output | ✅ Done |
| Shell completions for migrate command | ✅ Done |

---

## v1.8.0 — Telemetry

### Anonymous usage analytics (opt-in)

| Feature | Status |
|---------|--------|
| First-run consent prompt (opt-in, default disabled) | ✅ Done |
| Track: version, OS, command name, project count | ✅ Done |
| `pm config telemetry on/off` to toggle at any time | ✅ Done |
| Privacy-first: no personal data, no project names, no paths | ✅ Done |
| Transparent: documented in README, User-Guide, and `pm help` | ✅ Done |
| Backend: PostHog Cloud (free tier) | ✅ Done |
| Shell completions for config command | ✅ Done |

---

## v1.9.0 — License Key System ✅

### Open Core license validation (RSA-SHA256)

| Feature | Status |
|---------|--------|
| `pm license` / `pm license info` — show current license status | ✅ Done |
| `pm license activate <key>` — activate a Pro license key | ✅ Done |
| `pm license deactivate` — remove license, revert to Community Edition | ✅ Done |
| Banner shows "Community Edition" or "Pro" based on license status | ✅ Done |
| RSA-SHA256 offline validation (no server calls required) | ✅ Done |
| License stored at `~/.projectmanager/license.json` | ✅ Done |
| Branding only — no features are restricted | ✅ Done |

---

## v2.0.0 — Performance Tracking ✅

### Build & test time history
Track execution times for `build`, `test`, and `run` commands automatically. View historical data and trends per project.

| Feature | Status |
|---------|--------|
| Auto-record execution time for build/test/run commands | ✅ Done |
| Store history in `~/.projectmanager/stats.json` | ✅ Done |
| `pm stats [name]` — show time history for a project | ✅ Done |
| `pm stats --all` — show summary across all projects | ✅ Done |
| Average, min, max, last run time per command | ✅ Done |
| Last N runs history (configurable, default 20) | ✅ Done |
| Shell autocompletion for `pm stats` command | ✅ Done |

---

## Contributing

Have an idea? Open an issue at [GitHub Issues](https://github.com/SoftDryzz/ProjectManager/issues) with the `enhancement` label.
