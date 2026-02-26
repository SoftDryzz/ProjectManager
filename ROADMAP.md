# ProjectManager - Roadmap

> Ideas and planned features for future versions.
>
> Some ideas inspired by analyzing [FindMatch](https://github.com/AXIOM-ZER0/FindMatch), a real-world multi-stack project (Flutter + Rust + Docker + PostgreSQL + Redis).
>
> **Versioning:** Follows [Semantic Versioning](https://semver.org/). Each release corresponds to a GitHub Release with tag `vX.Y.Z` and asset `projectmanager-X.Y.Z.jar`.

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

## v1.6.0 — Shell Autocompletion

### `pm completions` command
- Generate completion scripts for bash, zsh, fish, and PowerShell
- `pm completions <shell>` — output completion script for the specified shell
- Autocomplete project names, commands, and flags
- Zero dependencies — generates static shell scripts

---

## v1.6.1 — Doctor Health Score

### `pm doctor` expanded
- Health score: **A/B/C/D/F** rating based on project best practices
- Checks: `.gitignore` exists, README present, tests configured, CI detected, dependencies up to date
- Actionable recommendations per check
- `pm doctor` — show full health report with score
- `pm doctor --score` — show only the letter grade

---

## v1.6.2 — Security Scan

### `pm secure` command
- Best practices security scan (filesystem patterns only, no secret management)
- Checks:
  - Dockerfile runs as non-root user
  - `.env` files are in `.gitignore`
  - No hardcoded `http://` URLs in config files (should be `https://`)
  - Sensitive files (`.pem`, `.key`) are in `.gitignore`
  - Dependencies lockfile exists
- `pm secure` — run all checks and show report
- `pm secure --fix` — auto-fix what can be fixed (e.g., add entries to `.gitignore`)

---

## v1.6.3 — Dependency Audit

### `pm audit` command
- Check for known vulnerabilities using native ecosystem tools:
  - npm: `npm audit`
  - Cargo: `cargo audit`
  - Go: `govulncheck`
  - Python: `pip-audit`
  - Maven: OWASP dependency-check
- Show summary with severity levels
- **Suggest** fixes where available — never auto-update dependencies
- The developer decides whether to update; PM only informs

> **Important:** `pm audit` is read-only. It reports vulnerabilities and suggests what *could* be updated, but never modifies `package.json`, `Cargo.toml`, or any dependency file. The developer may be using specific versions intentionally.

---

## v1.6.4 — Export & Import

### `pm export` / `pm import` commands
- `pm export` — export all registered projects to a portable JSON file
- `pm import <file>` — import projects from an exported file
- Useful for migrating between machines or sharing team setups
- Validates paths on import and warns about missing directories

---

## v1.6.5 — CI/CD Detection

### CI/CD awareness
- Detect GitHub Actions (`.github/workflows/`)
- Detect GitLab CI (`.gitlab-ci.yml`)
- Detect Jenkins (`Jenkinsfile`)
- Show CI status in `pm info`
- `pm ci` — open CI dashboard in browser or show last run status

### Deployment awareness
- Detect deployment configs: `fly.toml`, `vercel.json`, `netlify.toml`, `railway.json`
- `pm deploy` — trigger deployment to detected platform
- `pm deploy status` — show deployment state

---

## v1.6.6 — Linting & Formatting

### `pm lint` / `pm fmt` commands
- Detect linters per project type:
  - Rust: `cargo fmt`, `cargo clippy`
  - Go: `gofmt`, `golangci-lint`
  - Node.js: `eslint`, `prettier`
  - Python: `ruff`, `black`, `flake8`
  - Java: `checkstyle`, `spotless`
- `pm lint` — run detected linter
- `pm fmt` — run detected formatter

---

## v1.6.7 — Code Generation & Team Workflows

### Code generation detection
- Detect build_runner (Flutter/Dart), protobuf, OpenAPI generators
- `pm codegen` — run detected code generation tools
- Warn if generated files are outdated

### Team workflow support
- Detect number of contributors from git log
- `pm team` — show active contributors and their recent areas
- Branch naming convention detection and validation
- PR template detection

---

## v1.7.0 — Multi-project Workspaces

### Monorepo support
- Detect monorepo structures:
  - Cargo workspaces (`[workspace]` in Cargo.toml)
  - npm/pnpm/yarn workspaces
  - Go multi-module repos
  - Gradle multi-project builds
- `pm list-modules` — show all sub-projects
- `pm build --all` — build all modules
- `pm test --all` — test all modules
- `pm run <module>` — run specific module

### Multi-language project detection
- Detect projects that use multiple languages (e.g., Rust backend + Flutter frontend)
- Show all detected types: `pm info` → "Types: RUST, FLUTTER, DOCKER"
- Run commands per component: `pm build backend`, `pm test mobile`

---

## v1.7.1 — Project Templates

### `pm init` command
- `pm init <type>` — scaffold a new project from templates
- Built-in templates: Java (Maven/Gradle), Node.js, Rust, Go, Python, .NET
- Support custom templates from GitHub repos

---

## v1.8.0 — Environments, Secrets & Databases

### Environment management
- Detect `.env`, `.env.local`, `.env.production` files
- `pm env` — show current environment variables (masked secrets)
- `pm env switch <name>` — switch between environment files
- Warn if `.env` is not in `.gitignore`

### Secrets detection
- Scan for common secret patterns (API keys, tokens, passwords)
- Warn on `pm doctor` if secrets are committed
- Optional [Vaultic](https://crates.io/crates/vaultic) detection (not a dependency — PM works fully without it):
  - **Not installed** → recommend install options: `cargo install vaultic` (requires [Rust toolchain](https://rustup.rs)) or download binary from [GitHub Releases](https://github.com/SoftDryzz/Vaultic/releases). Always link to [Vaultic repo](https://github.com/SoftDryzz/Vaultic) for docs
  - **Installed but not initialized** → suggest `vaultic init` with link to docs so the user understands what it does before running it
  - **Installed and configured** → show enhanced secret management hints (e.g., `vaultic encrypt .env`)

### Database migration awareness
- Detect migration tools: SQLx, Flyway, Liquibase, Prisma, Diesel, Alembic
- `pm migrate` — run pending migrations
- `pm migrate status` — show migration state

---

## v2.0.0 — Dashboard & Analytics

### Interactive TUI dashboard
- Real-time project status dashboard (using a TUI library)
- Show: build status, test results, git status, dependencies
- Navigate between registered projects
- Quick actions (build, test, clean) from dashboard

### Performance tracking
- Track build times across runs
- `pm stats` — show build/test time trends
- Identify slow builds and suggest optimizations

---

## Future — Multi-ecosystem Install

### Install scripts per ecosystem
Create dedicated install scripts/commands so users can install ProjectManager using their preferred package manager:

| Ecosystem | Install Method | Status |
|-----------|---------------|--------|
| npm/npx | `npx projectmanager` or `npm i -g projectmanager` | Planned |
| pnpm | `pnpm add -g projectmanager` | Planned |
| Cargo | `cargo install projectmanager` | Planned |
| Homebrew | `brew install projectmanager` | Planned |
| Scoop (Windows) | `scoop install projectmanager` | Planned |
| Go | `go install github.com/SoftDryzz/ProjectManager@latest` | Planned |
| Bun | `bun add -g projectmanager` | Planned |
| Yarn | `yarn global add projectmanager` | Planned |

> **Note:** Each ecosystem install would wrap the JAR (or compile a native binary via GraalVM).

---

## Contributing

Have an idea? Open an issue at [GitHub Issues](https://github.com/SoftDryzz/ProjectManager/issues) with the `enhancement` label.
