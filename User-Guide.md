# 📖 User Guide - ProjectManager

## 📑 Table of Contents

- [What is ProjectManager?](#-what-is-projectmanager)
- [Quick Start (5 minutes)](#-quick-start-5-minutes)
  - [Step 1: Verify Installation](#step-1-verify-installation)
  - [Step 2: Register Your First Project](#step-2-register-your-first-project)
  - [Step 3: View Your Projects](#step-3-view-your-projects)
  - [Step 4: Build Your Project](#step-4-build-your-project)
- [Command Reference](#-command-reference)
  - [Project Management](#-project-management)
  - [Command Execution](#-command-execution)
  - [Rename & Path Update](#-rename--path-update)
  - [Pre-/Post-Command Hooks](#-prepost-command-hooks)
  - [Shell Autocompletion](#-shell-autocompletion)
  - [Environment Variable Management](#-environment-variable-management)
  - [Diagnostics](#-diagnostics)
  - [Security Scan](#-security-scan)
  - [Dependency Audit](#-dependency-audit)
  - [Export & Import](#-export--import)
  - [CI/CD Detection](#-cicd-detection)
  - [Linting & Formatting](#-linting--formatting)
  - [Multi-project Workspaces](#-multi-project-workspaces)
  - [Database Migrations](#-database-migrations)
  - [License Key](#-license-key)
  - [Performance Tracking](#-performance-tracking)
  - [Help and Version](#-help-and-version)
- [Environment Variables](#-environment-variables)
  - [What Are They?](#what-are-they)
  - [How Do They Work in ProjectManager?](#how-do-they-work-in-projectmanager)
  - [Usage Examples](#usage-examples)
  - [View Configured Variables](#view-configured-variables)
  - [Manage Variables with pm env](#manage-variables-with-pm-env)
  - [Format Rules](#format-rules)
  - [Complete Practical Examples](#complete-practical-examples)
  - [Where They Are Saved](#where-they-are-saved)
  - [Frequently Asked Questions](#environment-variables-faq)
- [Git Integration](#-git-integration)
  - [What is it?](#what-is-it)
  - [Information Displayed](#information-displayed)
  - [Full Example](#full-example)
  - [Git Integration Use Cases](#git-integration-use-cases)
  - [Projects Without Git](#projects-without-git)
  - [Requirements](#requirements)
- [Use Cases](#-use-cases)
- [Supported Project Types](#-supported-project-types)
- [Advanced Configuration](#-advanced-configuration)
  - [Config File Location](#config-file-location)
  - [projects.json File Structure](#projectsjson-file-structure)
  - [Manual Editing](#manual-editing-advanced)
- [Frequently Asked Questions (FAQ)](#-frequently-asked-questions-faq)
- [Data Safety & Error Handling](#️-data-safety--error-handling)
- [Troubleshooting](#-troubleshooting)
- [Quick Cheatsheet](#-quick-cheatsheet)
- [Complete Workflow](#-complete-workflow)
- [Next Steps](#-next-steps)
- [Additional Resources](#-additional-resources)

---

## 🎯 What is ProjectManager?

ProjectManager is a command-line tool that allows you to **manage all your development projects from a single place**, without needing to remember whether each project uses Gradle, Maven, npm, or another build tool.

---

## 🚀 Quick Start (5 minutes)

### Step 1: Verify Installation

If you have already run the installation script, verify that it works:
```bash
pm version
```

You should see something like:
```
ProjectManager 1.3.4
Java 25.0.1
```

---

### Step 2: Register Your First Project
```bash
pm add project-name --path C:\path\to\your\project
```

**ProjectManager automatically detects** the project type (Gradle, Maven, Node.js, etc.).

**Example:**
```bash
pm add web-api --path C:\Users\User\projects\web-api
```

**Expected Output:**
```
╔════════════════════════════════╗
║  ProjectManager v1.3.4         ║
║  Manage your projects          ║
╚════════════════════════════════╝

ℹ️  Detecting project type...

✅ Project 'web-api' registered successfully

  Name: web-api
  Type: Gradle
  Path: C:\Users\User\projects\web-api
  Commands: 4 configured

Use 'pm commands web-api' to see available commands
```

---

### Step 3: View Your Projects
```bash
pm list
```

**Output:**
```
Registered Projects (1)
───────────────────────

web-api (Gradle)
  Path: C:\Users\User\projects\web-api
  Modified: 2 minutes ago
  Commands: 4
```

---

### Step 4: Build Your Project
```bash
pm build web-api
```

ProjectManager executes the appropriate build command (e.g., `gradle build`) without you having to remember it.

---

## 📚 Command Reference

### 🔹 Project Management

#### Register a project (auto-detection)
```bash
pm add <name> --path <path>
```

**Example:**
```bash
pm add my-api --path C:\projects\my-api
```

---

#### Register a project with environment variables
```bash
pm add <name> --path <path> --env "KEY1=value1,KEY2=value2"
```

**Example:**
```bash
pm add backend --path C:\projects\backend --env "PORT=3000,DEBUG=true,DB_HOST=localhost"
```

**Variables are configured once and used automatically** in all commands (build, run, test).

---

#### Register a project (specifying type)
```bash
pm add <name> --path <path> --type <type>
```

**Valid types:** `GRADLE`, `MAVEN`, `NODEJS`, `DOTNET`, `PYTHON`, `RUST`, `GO`, `PNPM`, `BUN`, `YARN`, `FLUTTER`

**Example:**
```bash
pm add my-app --path C:\projects\app --type MAVEN
```

---

#### List all projects
```bash
pm list
```

or
```bash
pm ls
```

---

#### View detailed project information
```bash
pm info <name>
```

**Example:**
```bash
pm info web-api
```

**Shows:**

- Project Name
- Type (Gradle, Maven, etc.)
- Full Path
- Last Modified
- Available Commands
- Configured Environment Variables
- Git Status (if it's a repository)

---

#### View available commands for a project
```bash
pm commands <name>
```

or
```bash
pm cmd <name>
```

**Example:**
```bash
pm commands web-api
```

**Output:**
```
Available Commands for web-api
────────────────────────────────────────

  build  →  gradle build
  run    →  gradle run
  test   →  gradle test
  clean  →  gradle clean
```

---

#### Remove a project

**With confirmation:**
```bash
pm remove <name>
```

**Without confirmation:**
```bash
pm remove <name> --force
```

or
```bash
pm rm <name> --force
```

---

### 🔹 Command Execution

#### Build a project
```bash
pm build <name>
```

**Example:**
```bash
pm build web-api
```

Executes the configured build command (e.g., `gradle build`, `mvn package`, `npm run build`) **automatically with environment variables**.

---

#### Run a project
```bash
pm run <name>
```

**Example:**
```bash
pm run web-api
```

Executes the configured run command (e.g., `gradle run`, `mvn exec:java`, `npm start`) **automatically with environment variables**.

---

#### Run tests
```bash
pm test <name>
```

**Example:**
```bash
pm test my-api
```

Executes the project tests (e.g., `gradle test`, `mvn test`, `npm test`) **automatically with environment variables**.

---

### 🔹 Custom Commands

ProjectManager auto-detects default commands (build, run, test, clean) based on the project type. But you can also **add your own custom commands** for anything else you need.

#### Why custom commands?

Default commands cover the basics, but real projects often need more:
- Start a tunnel for mobile testing (`npx expo start --tunnel`)
- Lint your code (`npm run lint`)
- Deploy to production (`docker compose up -d`)
- Start a database (`docker run -d -p 5432:5432 postgres`)
- Generate code (`flutter pub run build_runner build`)
- Run a specific script (`npm run seed:db`)

Instead of remembering these long commands, save them once and run them with a short name.

---

#### Add a custom command
```bash
pm commands <name> add <command-name> "<command-line>"
```

**Examples:**
```bash
# Add a tunnel command for Expo
pm commands my-app add tunnel "npx expo start --tunnel"

# Add a lint command
pm commands my-app add lint "npm run lint"

# Add a deploy command
pm commands my-app add deploy "docker compose up -d"

# Add a database starter
pm commands my-app add start-db "docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=secret postgres:15"

# Add code generation
pm commands my-app add codegen "flutter pub run build_runner build --delete-conflicting-outputs"
```

After adding, the command is saved permanently in `projects.json` alongside the auto-detected defaults.

> **Tip:** If the command contains flags with `--`, wrap it in quotes so your shell passes it correctly.

---

#### Remove a custom command
```bash
pm commands <name> remove <command-name>
```

**Example:**
```bash
pm commands my-app remove tunnel
```

Removes the command from the project. This works for both custom commands and auto-detected defaults.

---

#### List commands for a project
```bash
pm commands <name>
```

Shows all available commands (default + custom):
```
Available Commands for my-app
────────────────────────────────────────

  build   → npm run build
  run     → npm start
  test    → npm test
  tunnel  → npx expo start --tunnel
  lint    → npm run lint
  deploy  → docker compose up -d
```

---

#### List commands for all projects
```bash
pm commands --all
```

Shows commands for every registered project at once:
```
Commands for All Projects (3)
────────────────────────────────────────

my-app (Node.js)
  build   → npm run build
  run     → npm start
  tunnel  → npx expo start --tunnel

backend-api (Maven)
  build  → mvn package
  run    → mvn exec:java
  test   → mvn test

rust-service (Rust)
  build  → cargo build
  run    → cargo run
  test   → cargo test
```

---

#### Update an existing command

To change a command, simply add it again with the new value:
```bash
pm commands my-app add run "npx expo start --tunnel"
```

This overwrites the previous value.

---

#### Common use cases

| Use Case | Command |
|----------|---------|
| Mobile tunnel | `pm commands app add tunnel "npx expo start --tunnel"` |
| Lint code | `pm commands app add lint "npm run lint"` |
| Format code | `pm commands app add fmt "npm run prettier -- --write ."` |
| Deploy | `pm commands app add deploy "docker compose up -d"` |
| Database | `pm commands app add db "docker run -d -p 5432:5432 postgres"` |
| Code generation | `pm commands app add codegen "flutter pub run build_runner build"` |
| Seed data | `pm commands app add seed "npm run seed:db"` |
| Type checking | `pm commands app add typecheck "npx tsc --noEmit"` |
| Watch mode | `pm commands app add watch "npm run dev -- --watch"` |
| Production build | `pm commands app add prod "npm run build:prod"` |

---

### 🔹 Pre-/Post-Command Hooks

Run custom scripts automatically before or after any command. Hooks are per-project and user-configured.

#### Add a hook
```bash
pm hooks <name> add <slot> "<script>"
```

**Slot format:** `pre-<command>` or `post-<command>` (e.g., `pre-build`, `post-test`).

**Examples:**
```bash
# Run linter before every build
pm hooks my-api add pre-build "npm run lint"

# Send notification after build
pm hooks my-api add post-build "echo Build completed!"

# Run migrations before running
pm hooks my-api add pre-run "npx prisma migrate deploy"

# Multiple hooks per slot (executed in order)
pm hooks my-api add pre-build "npm run format"
```

#### List hooks
```bash
pm hooks <name>          # List hooks for one project
pm hooks --all           # List hooks for all projects
```

#### Remove a hook
```bash
pm hooks <name> remove <slot> "<script>"
```

The script must match exactly. Use `pm hooks <name>` to see current hooks.

**Example:**
```bash
pm hooks my-api remove pre-build "npm run lint"
```

#### How hooks work

- **Pre-hooks** run before the main command. If any pre-hook fails (non-zero exit code), the main command is **aborted**.
- **Post-hooks** run after the main command succeeds. If a post-hook fails, a **warning** is shown but the command result is not affected.
- Hooks have a fixed **60-second timeout**.
- Hooks inherit the project's **environment variables**.
- Hooks work with all commands: `build`, `run`, `test`, `clean`, `stop`, and any custom command.

---

### 🔹 Shell Autocompletion

Enable TAB completion for all `pm` commands, project names, and subcommands.

#### Generate completion script
```bash
pm completions <shell>
```

#### Setup

**Bash** — add to `~/.bashrc`:
```bash
eval "$(pm completions bash)"
```

**Zsh** — add to `~/.zshrc`:
```bash
eval "$(pm completions zsh)"
```

**Fish** — save to completions directory:
```bash
pm completions fish > ~/.config/fish/completions/pm.fish
```

**PowerShell** — add to `$PROFILE`:
```powershell
pm completions powershell | Out-String | Invoke-Expression
```

#### What gets autocompleted?

- **Top-level commands** — `pm b<TAB>` → `build`
- **Project names** — `pm build <TAB>` → lists all registered projects
- **Subcommands** — `pm env <TAB>` → `set`, `get`, `list`, `remove`, `clear`
- **Flags** — `pm add myproject <TAB>` → `--path`, `--type`, `--env`
- **Hook slots** — `pm hooks myproject add <TAB>` → `pre-build`, `post-run`, etc.
- **Env var keys** — `pm env get myproject <TAB>` → lists configured variable names
- **Shell names** — `pm completions <TAB>` → `bash`, `zsh`, `fish`, `powershell`

---

### 🔹 Environment Variable Management

#### Set variables
```bash
pm env set <name> KEY=VALUE[,KEY2=VALUE2]
```

**Example:**
```bash
pm env set my-api PORT=8080,DEBUG=true,API_KEY=secret123
```

---

#### Get a variable
```bash
pm env get <name> KEY
```

**Example:**
```bash
pm env get my-api PORT
# Output: PORT=8080
```

---

#### List variables
```bash
pm env list <name>           # Sensitive values masked
pm env list <name> --show    # All values revealed
```

---

#### Remove a variable
```bash
pm env remove <name> KEY
```

---

#### Clear all variables
```bash
pm env clear <name>
```

---

#### List .env files
```bash
pm env files <name>
```

Discovers all `.env*` files (`.env`, `.env.local`, `.env.production`, etc.) in the project directory and shows their entry count and file size.

**Example output:**
```
Env Files — my-api

  File               Entries   Size
  ────────────────────────────────────
  .env               5         128 B
  .env.local         3         84 B
  .env.production    8         256 B

  3 env files found
```

---

#### Show .env file contents
```bash
pm env show <name> <file>           # Sensitive values masked
pm env show <name> <file> --show    # All values revealed
```

Displays the contents of a specific `.env` file. Sensitive values (PASSWORD, TOKEN, KEY, SECRET, AUTH) are masked by default — shows the first 3 characters followed by `****`.

---

#### Switch environment
```bash
pm env switch <name> <env-name>
```

Copies `.env.<env-name>` to `.env` in the project directory. Asks for confirmation if `.env` already exists.

**Example:**
```bash
pm env switch my-api production
# Copies .env.production → .env (with y/n confirmation)
```

---

### 🔹 Rename & Path Update

#### Rename a project
```bash
pm rename <old-name> <new-name>
```

**Example:**
```bash
pm rename backend-api my-api
```

Renames the project while preserving all commands, environment variables, and project type.

---

#### Update a project's path
```bash
pm rename <name> --path <new-path>
```

**Example:**
```bash
pm rename my-api --path /home/user/new-location/my-api
```

---

#### Rename and update path at once
```bash
pm rename <old-name> <new-name> --path <new-path>
```

---

### 🔹 Project Refresh

#### Refresh a specific project
```bash
pm refresh <name>
```

Re-detects the project type and updates its default commands. Useful when:
- A project was registered before its type was supported (e.g., Flutter added in v1.3.1)
- The project's build system changed (e.g., migrated from npm to pnpm)

Shows detailed before/after: old commands removed, new commands added, type change.

---

#### Refresh all projects
```bash
pm refresh --all
```

Re-detects and updates all registered projects at once. Shows a summary with updated, refreshed, and skipped counts.

---

#### Automatic outdated hints

When running `pm build`, `pm run`, `pm test`, `pm commands`, or `pm info`, ProjectManager automatically checks if the stored project type differs from what would be detected now. If outdated, it shows a hint:

```
hint: detected type is Flutter but project is registered as Unknown
Run 'pm refresh my-project' to update
```

---

### 🔹 Diagnostics

#### Check environment health
```bash
pm doctor
```

Verifies installed runtimes (Java, Node.js, .NET, Python, Gradle, Maven, Rust/Cargo, Go, pnpm, Bun, Yarn, Flutter), validates all registered project paths, and shows a **health report** for each project.

#### Health checks performed (per project)

| Check | Pass condition | Recommendation if failed |
|-------|---------------|--------------------------|
| `.gitignore` | File exists in project root | Create a .gitignore to exclude build artifacts |
| README | `README.md` or `README` exists (case-insensitive) | Add a README.md to document your project |
| Tests | Project has a `test` command configured | Configure tests with `pm commands add` |
| CI/CD | GitHub Actions, GitLab CI, or Jenkinsfile detected | Set up CI/CD for automated testing |
| Lockfile | Dependency lockfile exists for project type | Commit your lockfile for reproducible builds |
| Secrets | No hardcoded secrets found in `.env` files | Use environment injection or a vault instead |

Each project receives a **letter grade** based on how many checks pass:
- **A** = 6/6 — **B** = 5/6 — **C** = 4/6 — **D** = 3/6 — **F** = 0–2/6

#### Show only health grades (compact)
```bash
pm doctor --score
```

Shows just the letter grade per project without details:
```
  A  backend
  B  frontend
  F  legacy-api
```

---

### 🔹 Security Scan

#### Scan all projects for security issues
```bash
pm secure
```

Runs 7 filesystem-only security checks on each registered project:

| Check | Pass condition | Recommendation if failed |
|-------|---------------|--------------------------|
| Dockerfile non-root | No Dockerfile, or Dockerfile contains `USER` (not root) | Add a USER directive to avoid running as root |
| Env protection | `.gitignore` contains `.env` patterns | Add .env to .gitignore to prevent leaking secrets |
| HTTPS only | No `http://` URLs in config files (excluding localhost) | Replace http:// with https:// in config files |
| Sensitive files | `.gitignore` contains `*.pem` and `*.key` patterns | Add *.pem and *.key to .gitignore to protect keys |
| Lockfile | Dependency lockfile exists for project type | Commit your lockfile to prevent supply-chain attacks |
| Secret patterns | No known secret patterns (AWS keys, GitHub tokens, Slack tokens) in `.env` files | Remove hardcoded secrets and use environment injection |
| Vaultic | [Vaultic](https://github.com/SoftDryzz/Vaultic) installed and initialized (when `.env` files exist) | Install Vaultic to encrypt your `.env` files |

Result coloring: **7/7** = green, **5–6/7** = yellow, **0–4/7** = red

#### Auto-fix .gitignore issues
```bash
pm secure --fix
```

Automatically adds missing entries to `.gitignore`:
- `.env` and `.env.*` (environment files)
- `*.pem`, `*.key`, `*.p12`, `*.pfx` (private keys and certificates)

Creates `.gitignore` if it doesn't exist. Does **not** duplicate entries that are already present.

---

### 🔹 Dependency Audit

#### Audit all projects for known vulnerabilities
```bash
pm audit
```

Runs native ecosystem vulnerability tools on each registered project and displays a unified summary with severity levels.

| Project Type | Audit Tool | Requires Separate Install? |
|-------------|-----------|---------------------------|
| Node.js | `npm audit --json` | No (bundled with npm) |
| pnpm | `pnpm audit --json` | No (bundled) |
| Yarn | `yarn audit --json` | No (bundled) |
| Rust | `cargo audit --json` | Yes (`cargo install cargo-audit`) |
| Go | `govulncheck -json ./...` | Yes (`go install golang.org/x/vuln/cmd/govulncheck@latest`) |
| Python | `pip-audit --format=json` | Yes (`pip install pip-audit`) |
| .NET | `dotnet list package --vulnerable` | No (bundled with .NET SDK) |
| Maven/Gradle | N/A | Shows recommendation for OWASP plugin |
| Bun/Flutter/Docker | N/A | Skipped (no native tool) |

**Severity levels:** CRITICAL, HIGH, MEDIUM, LOW — mapped from each ecosystem's native levels.

**Read-only:** `pm audit` never modifies dependency files. It suggests fixes but the developer decides whether to update.

**Example output:**
```
=== Dependency Audit ===

  backend (Node.js)
    ⚠ 3 vulnerabilities found
      2 moderate, 1 high
    Suggestion: Run 'npm audit fix' to resolve

  api-server (Rust)
    ✓ No known vulnerabilities

  data-service (Maven)
    ℹ No native audit tool for Maven
      Consider: OWASP dependency-check plugin
```

---

### 🔹 Export & Import

Export your project configurations to a portable JSON file and import them on another machine.

#### Export all projects
```bash
pm export
```

Saves all registered projects to `pm-export.json` in the current directory.

#### Export selected projects
```bash
pm export backend-api web-server
```

#### Export to a custom file
```bash
pm export --file ~/backup/my-setup.json
pm export backend-api --file team-config.json
```

#### Import projects
```bash
pm import pm-export.json
pm import ~/backup/my-setup.json
```

**Behavior on import:**
- Projects that already exist are **skipped** (never overwritten)
- Projects with paths that don't exist on disk are imported with a **warning** — fix later with `pm rename <name> --path <new-path>`
- Invalid project types default to `UNKNOWN`

**Example output:**
```
Import
──────

  ✓ Imported 3 projects
  ⚠ Skipped 'backend' — already exists
  ⚠ 'api-server' path does not exist: /old/machine/path
    Update with: pm rename api-server --path <new-path>
```

---

### 🔹 CI/CD Detection

View detected CI/CD pipelines and their dashboard URLs for your projects.

#### Show CI/CD for a specific project
```bash
pm ci backend
```

#### Show CI/CD for all projects
```bash
pm ci
```

**Supported providers:**

| Provider | Detection | Dashboard URL |
|----------|-----------|---------------|
| GitHub Actions | `.github/workflows/` directory | `https://github.com/{owner}/{repo}/actions` |
| GitLab CI | `.gitlab-ci.yml` file | `https://gitlab.com/{owner}/{repo}/-/pipelines` |
| Jenkins | `Jenkinsfile` | No standard URL |
| Travis CI | `.travis.yml` file | `https://app.travis-ci.com/github/{owner}/{repo}` |
| CircleCI | `.circleci/config.yml` file | `https://app.circleci.com/pipelines/github/{owner}/{repo}` |

**Example output:**
```
CI/CD — backend
─────

  GitHub Actions (3 workflows)
    https://github.com/user/backend/actions

  GitLab CI
    https://gitlab.com/user/backend/-/pipelines
```

CI/CD information is also shown automatically in `pm info`:
```
  CI/CD:
    ✓ GitHub Actions (2 workflows)
    ✓ Jenkins
```

---

### 🔹 Linting & Formatting

Run linters and formatters auto-detected for your project type.

#### Lint a specific project
```bash
pm lint backend
```

#### Lint all projects
```bash
pm lint
```

#### Format a specific project
```bash
pm fmt backend
```

#### Format all projects
```bash
pm fmt
```

**Supported lint tools:**

| Project Type | Tool | Detection |
|---|---|---|
| Node.js/pnpm/Bun/Yarn | ESLint | Config file (`.eslintrc*`, `eslint.config.*`) |
| Rust | Clippy | Always (toolchain) |
| Go | go vet | Always (toolchain) |
| Go | golangci-lint | Binary installed |
| Python | Ruff | Binary installed |
| Python | Flake8 | Binary installed |
| Flutter | dart analyze | Always (toolchain) |
| .NET | dotnet format --verify | Always (toolchain) |
| Maven | Checkstyle | Plugin in pom.xml |
| Gradle | Checkstyle | Plugin in build.gradle |

**Supported format tools:**

| Project Type | Tool | Detection |
|---|---|---|
| Node.js/pnpm/Bun/Yarn | Prettier | Config file (`.prettierrc*`, `prettier.config.*`) |
| Rust | cargo fmt | Always (toolchain) |
| Go | gofmt | Always (toolchain) |
| Python | Ruff Format | Binary installed |
| Python | Black | Binary installed |
| Flutter | dart format | Always (toolchain) |
| .NET | dotnet format | Always (toolchain) |
| Maven | Spotless | Plugin in pom.xml |
| Gradle | Spotless | Plugin in build.gradle |

**Example output:**
```
Lint — backend
──────────────

  Running ESLint...
  ────────────────────────────────────────
  [eslint output...]
  ────────────────────────────────────────
  ✓ ESLint passed (3s)

  Result: 1/1 tools passed
```

---

### 🔹 Multi-project Workspaces

Detect monorepo modules and manage multi-language projects.

#### Show workspace modules for a project
```bash
pm modules backend
```

#### Show workspace modules for all projects
```bash
pm modules
```

**Supported workspace types:**

| Project Type | Detection | Module Source |
|---|---|---|
| Rust | `[workspace]` in `Cargo.toml` | `members` list |
| Node.js/pnpm/Yarn | `"workspaces"` in `package.json` | Array or object format, glob expansion |
| Gradle | `include()` in `settings.gradle` / `settings.gradle.kts` | Include directives |
| Go | Nested `go.mod` files | Subdirectories with `go.mod` (depth 3) |

**Example output:**
```
Workspace Modules — backend (Rust)

  Name          Path              Type
  ─────────────────────────────────────
  app           app/              Rust
  lib-core      lib/core/         Rust
  tools         tools/            Rust

  3 modules detected
```

#### Multi-language detection

When a project contains multiple technologies (e.g., `pom.xml` + `docker-compose.yml`), ProjectManager now detects all of them:

```
my-api (Maven)
  Also detected: Docker
```

Secondary types are shown in `pm info` and persisted in `projects.json`. Use `pm refresh` to re-detect secondary types for existing projects.

#### Build all projects
```bash
pm build --all
```

Builds every registered project. Projects without a `build` command are skipped. Shows a pass/fail summary at the end.

#### Test all projects
```bash
pm test --all
```

Tests every registered project. Continues on failure and shows a summary.

**Example output:**
```
=== Build All ===

  ✓ backend (Maven) — 12s
  ✓ frontend (Node.js) — 8s
  ✗ legacy-api (Gradle) — exit code 1

  Result: 2/3 projects built successfully
```

---

### 🔹 Database Migrations

Detect and manage database migration tools across your projects.

#### List detected migration tools
```bash
pm migrate
```

Scans all registered projects and shows which migration tools are detected:

| Tool | Detection | Migrate Command | Status Command |
|------|-----------|----------------|----------------|
| Prisma | `prisma/schema.prisma` | `npx prisma migrate deploy` | `npx prisma migrate status` |
| Alembic | `alembic.ini` or `alembic/` dir | `alembic upgrade head` | `alembic current` |
| Diesel | `diesel.toml` | `diesel migration run` | `diesel migration list` |
| Flyway | `flyway.conf` or `flyway.toml` | `flyway migrate` | `flyway info` |
| Liquibase | `liquibase.properties` | `liquibase update` | `liquibase status` |
| SQLx | `.sqlx/` dir | `sqlx migrate run` | `sqlx migrate info` |

#### Run a migration
```bash
pm migrate <name>
```

Detects the migration tool for the project, asks for confirmation (y/n), then executes the migration command. If multiple tools are detected, uses the first one found.

#### Check migration status
```bash
pm migrate <name> status
```

Runs the status command for the detected migration tool. This is read-only and does not require confirmation.

**Example output:**
```
Migration — my-api

  Tool: Prisma
  Command: npx prisma migrate status

  [Prisma output here...]
```

Migration tools are also shown in `pm info`:
```
  Migration: Prisma, Flyway
```

---

### 🔹 Configuration

#### Check telemetry status
```bash
pm config telemetry
```

#### Enable telemetry
```bash
pm config telemetry on
```

#### Disable telemetry
```bash
pm config telemetry off
```

> **Privacy**: Telemetry is opt-in (disabled by default). Only anonymous data is collected: PM version, OS, command name, and project count. No project names, file paths, secrets, or personal information is ever sent.

---

### 🔹 License Key

ProjectManager follows an Open Core model. By default, it runs as **Community Edition** with all features available. Activating a Pro license key changes the banner branding only — no features are restricted.

#### Show license status
```bash
pm license
```
or
```bash
pm license info
```

Displays the current license status: Community Edition or Pro, along with license details if activated.

#### Activate a license
```bash
pm license activate <key>
```

Validates the license key offline using RSA-SHA256 signatures (no server calls required) and activates Pro branding. The license is stored at `~/.projectmanager/license.json`.

Each license allows **2 activations** (e.g., desktop + laptop).

#### Deactivate a license
```bash
pm license deactivate
```

Removes the current license and reverts to Community Edition. This frees up one activation slot, allowing you to move the license to a different machine.

> **Note**: License keys are validated entirely offline. No network connection is required for activation or validation.

> **Changing machines?** Run `pm license deactivate` on the old machine first, then `pm license activate <key>` on the new one.

---

### 🔹 Performance Tracking

ProjectManager automatically records execution times for `build`, `test`, and `run` commands. View historical data per project or across all projects.

#### View stats for a project
```bash
pm stats my-api
```

Shows per-command details including last run time, average, fastest, and slowest execution times. Keeps the last 20 runs per command.

**Example output:**
```
Performance Stats — my-api
──────────────────────────

  build (5 runs)
    Last:    12s
    Average: 14s
    Fastest: 10s
    Slowest: 18s

  test (3 runs)
    Last:    45s
    Average: 42s
    Fastest: 38s
    Slowest: 45s
```

#### View stats for all projects
```bash
pm stats --all
```

Shows a summary table with build/test averages and total runs per project.

**Example output:**
```
Performance Stats — All Projects
─────────────────────────────────

  Project        Build Avg   Test Avg   Total Runs
  my-api         14s         42s        8
  frontend       8s          12s        15
  backend        22s         1m 5s      6
```

> **Note:** Stats are recorded automatically — no configuration needed. Data is stored in `~/.projectmanager/stats.json` and is non-critical (never blocks command execution).

---

### 🔹 Help and Version

#### View help
```bash
pm help
```

or
```bash
pm --help
pm -h
```

---

#### View version
```bash
pm version
```

or
```bash
pm --version
pm -v
```

---

## 🔧 Environment Variables

### What Are They?

Environment variables are settings your application needs to run, such as ports, API keys, database URLs, etc.

**Problem without environment variables:**
```bash
# You have to remember to configure each time:
cd ~/my-api
PORT=8080 DEBUG=true npm start
```

**With ProjectManager:**
```bash
# Register once with variables:
pm add my-api --path ~/my-api --env "PORT=8080,DEBUG=true"

# Always run the same way:
pm run my-api
# Automatically uses PORT=8080 and DEBUG=true
```

---

### How Do They Work in ProjectManager?

1. **Register the project with variables:**
```bash
pm add api --path ~/api --env "PORT=8080,DEBUG=true"
```

2. **Variables are saved** in the project configuration.

3. **They are injected automatically** when you run:
   - `pm build api`
   - `pm run api`
   - `pm test api`

4. **View configured variables:**
```bash
pm info api
```

---

### Usage Examples

#### Example 1: API with Configurable Port
```bash
# Register with port
pm add web-server --path ~/server --env "PORT=3000"

# Run (uses PORT=3000 automatically)
pm run web-server
```

---

#### Example 2: Project with Multiple Variables
```bash
# API with several settings
pm add backend --path ~/backend --env "PORT=8080,DB_HOST=localhost,DB_USER=admin,API_KEY=secret123"

# Build (variables available at build time)
pm build backend

# Run (variables available at runtime)
pm run backend
```

---

#### Example 3: Maven with Memory Configuration
```bash
# Configure memory for Maven
pm add large-project --path ~/project --env "MAVEN_OPTS=-Xms512m -Xmx2048m"

# Maven will use that configuration
pm build large-project
```

---

### View Configured Variables
```bash
pm info project-name
```

**Shows:**
```
Environment Variables
─────────────────────

  PORT    = 8080
  DEBUG   = true
  API_KEY = secret123
```

---

### Manage Variables with `pm env`

You can manage environment variables at any time using the `pm env` command:

#### Set variables
```bash
# Set one or more variables
pm env set my-api PORT=8080
pm env set my-api PORT=8080,DEBUG=true,API_KEY=secret123
```

#### Get a specific variable
```bash
pm env get my-api PORT
# Output: PORT=8080
```

#### List all variables
```bash
# List with sensitive values masked
pm env list my-api

# List showing all values
pm env list my-api --show
```

**Masking:** Values for keys containing `KEY`, `SECRET`, `PASSWORD`, `TOKEN`, `PRIVATE`, or `CREDENTIAL` are masked by default (e.g., `API_KEY = sk-***56`). Use `--show` to reveal all values.

#### Remove a specific variable
```bash
pm env remove my-api DEBUG
```

#### Clear all variables
```bash
pm env clear my-api
```

---

### Format Rules

**Correct format:**
```bash
# ✅ Correct
pm add project --path /path --env "VAR1=value1,VAR2=value2"

# ✅ With spaces (automatically removed)
pm add project --path /path --env "VAR1 = value1 , VAR2 = value2"

# ✅ Single variable
pm add project --path /path --env "PORT=8080"
```

**Incorrect format:**
```bash
# ❌ Without quotes
pm add project --path /path --env VAR1=value1,VAR2=value2

# ❌ Without = sign
pm add project --path /path --env "VAR1:value1"
```

---

### Complete Practical Examples

#### Example 1: Node.js Server
```bash
# Register
pm add node-server --path C:\projects\node-server --env "PORT=3000,NODE_ENV=development"

# Run (uses variables automatically)
pm run node-server
```

---

#### Example 2: Spring Boot Application
```bash
# Register with multiple variables
pm add spring-app --path ~/projects/spring-app --env "SERVER_PORT=8080,SPRING_PROFILES_ACTIVE=dev,DB_URL=jdbc:mysql://localhost:3306/mydb"

# Build
pm build spring-app

# Run
pm run spring-app
```

---

#### Example 3: Maven Project with Optimized JVM
```bash
# Configure memory options for Maven
pm add big-project --path ~/big-project --env "MAVEN_OPTS=-Xmx8G -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Maven will use 8GB RAM when building
pm build big-project
```

---

### Where They Are Saved

Variables are stored in the configuration file:

**Windows:** `C:\Users\YourUser\.projectmanager\projects.json`
**Linux/Mac:** `~/.projectmanager/projects.json`

**Example content:**
```json
{
  "my-api": {
    "name": "my-api",
    "path": "C:\\projects\\my-api",
    "type": "MAVEN",
    "commands": {
      "build": "mvn package",
      "run": "mvn exec:java",
      "test": "mvn test",
      "clean": "mvn clean"
    },
    "envVars": {
      "PORT": "8080",
      "DEBUG": "true",
      "API_KEY": "secret"
    },
    "lastModified": "2025-01-18T18:00:00Z"
  }
}
```

---

### Environment Variables FAQ

#### Can I change variables after registering?

**Yes!** Use the `pm env` command:
```bash
pm env set my-api PORT=9090          # Add or update a variable
pm env remove my-api OLD_VAR         # Remove a specific variable
pm env clear my-api                  # Remove all variables
```

---

#### Do variables affect other projects?

**No.** Each project has its own independent variables.

---

#### Can I use system variables?

**Yes.** ProjectManager variables are added to system variables. If there's a conflict, ProjectManager variables take priority.

---

#### Are variables secure?

**Warning:** Variables are saved in plain text in `projects.json`.

**Don't save:** Real passwords, production tokens, sensitive information.

**Use for:** Development configuration, ports, debug flags, local paths.

---

## 🌿 Git Integration

### What is it?

ProjectManager automatically detects if your project is a Git repository and shows useful information when you run `pm info`.

---

### Information Displayed

#### 1. Current Branch
```bash
pm info myproject
```

**Shows:**
```
Git:
  Branch: feature/new-feature
```

**Useful for:** Knowing which branch you're on without typing `git branch`.

---

#### 2. Working Tree Status

**Possible states:**

**Clean working tree:**
```
Git:
  Status: ✓ Clean working tree
```

**With changes:**
```
Git:
  Status: 3 staged, 2 modified, 1 untracked
```

**Meaning:**
- **staged:** Files added with `git add` (ready for commit).
- **modified:** Files modified but NOT yet added.
- **untracked:** New files that Git is not tracking.

---

#### 3. Unpushed Commits

**No pending commits:**
```
Git:
  Unpushed: ✓ Up to date
```

**With pending commits:**
```
Git:
  Unpushed: 3 commits
```

**Useful for:** Remembering to push before shutting down your PC.

---

### Full Example
```bash
pm info web-api
```

**Output:**
```
╔════════════════════════════════╗
║  ProjectManager v1.3.4         ║
║  Manage your projects          ║
╚════════════════════════════════╝


Project Information
───────────────────

web-api (Gradle)
  Path: C:\projects\web-api
  Modified: 2 hours ago
  Commands: 4
  Environment Variables: 2

  Git:
    Branch: feature/api-endpoints
    Status: 2 modified, 1 untracked
    Unpushed: 3 commits


Available Commands for web-api
────────────────────────────────────────

  build  →  gradle build
  run    →  gradle run
  test   →  gradle test
  clean  →  gradle clean

Environment Variables
─────────────────────

  PORT   = 8080
  DEBUG  = true
```

---

### Git Integration Use Cases

#### Case 1: Check Branch Before Working
```bash
# Which branch am I on?
pm info myproject

# Git:
#   Branch: master  ← Careful! You are on master
```

**Avoids:** Making changes on the wrong branch.

---

#### Case 2: Remember to Commit
```bash
pm info myproject

# Git:
#   Status: 5 modified  ← You have uncommitted changes
```

**Reminder:** Commit before ending your session.

---

#### Case 3: Remember to Push
```bash
pm info myproject

# Git:
#   Unpushed: 7 commits  ← You have unpushed work!
```

**Avoids:** Losing work if your PC fails.

---

### Projects Without Git

If a project **is not a Git repository**, the Git section is simply not shown:
```
Project Information
───────────────────

myproject (Maven)
  Path: C:\projects\myproject
  Modified: 1 day ago
  Commands: 4

Available Commands for myproject
  build  →  mvn package
  ...
```

---

### Requirements

- **Git installed** on your system.
- **Project must be a Git repository** (must have a `.git` folder).

**Verify Git is installed:**
```bash
git --version
```

If not installed: https://git-scm.com/downloads

---

## 💡 Use Cases

### Case 1: Multiple Projects with Different Technologies

**Problem:** You have 5 projects, each with a different build system.

**Without ProjectManager:**
```bash
# Project 1 (Gradle)
cd C:\projects\project1
gradle build

# Project 2 (Maven)
cd C:\projects\project2
mvn package

# Project 3 (npm)
cd C:\projects\project3
npm run build
```

**With ProjectManager:**
```bash
pm build project1
pm build project2
pm build project3
```

✅ **Same command for all, without changing folders.**

---

### Case 2: Forgot a Project's Commands

**Problem:** You don't remember if a project uses `gradle run`.

**Solution:**
```bash
pm commands project1
```

Shows you all available commands.

---

### Case 3: Teamwork

**Problem:** Every developer uses different commands.

**Solution:** The whole team registers projects with ProjectManager:
```bash
pm build api
pm test api
pm run frontend
```

✅ **Consistent commands for the whole team.**

---

### Case 4: Different Configurations per Project

**Problem:** You have 3 APIs with different ports and need to remember which uses which.

**With ProjectManager:**
```bash
# Register each one with its port
pm add api-users --path ~/api-users --env "PORT=3000"
pm add api-products --path ~/api-products --env "PORT=3001"
pm add api-orders --path ~/api-orders --env "PORT=3002"

# Run any (uses its port automatically)
pm run api-users     # Port 3000
pm run api-products  # Port 3001
pm run api-orders    # Port 3002
```

✅ **No need to remember configurations, everything is automatic.**

---

## 🗂️ Supported Project Types

| Type | Detection Files | Configured Commands |
|------|-----------------|---------------------|
| **Gradle** | `build.gradle`, `build.gradle.kts` | `build`, `run`, `test`, `clean` |
| **Maven** | `pom.xml` | `build` (package), `run` (exec:java), `test`, `clean` |
| **Rust** | `Cargo.toml` | `build`, `run`, `test`, `clean` |
| **Go** | `go.mod` | `build`, `run`, `test`, `clean` |
| **Flutter** | `pubspec.yaml` | `build`, `run`, `test`, `clean` |
| **pnpm** | `pnpm-lock.yaml` | `build`, `dev`, `test` |
| **Bun** | `bun.lockb`, `bun.lock` | `build`, `dev`, `test` |
| **Yarn** | `yarn.lock` | `build`, `start`, `test` |
| **Node.js** | `package.json` (fallback) | `build`, `start`, `test` |
| **.NET** | `*.csproj`, `*.fsproj` | `build`, `run`, `test` |
| **Python** | `requirements.txt`, `setup.py` | (manual configuration) |
| **Docker** | `docker-compose.yml`, `docker-compose.yaml` | `build`, `run` (up), `stop` (down), `clean` |

> **Detection priority:** Language types always take priority. When a project has both `pom.xml` and `docker-compose.yml`, it's detected as Maven (not Docker). Docker is only detected when no language-specific type is found. For JS projects, specific package managers (pnpm, Bun, Yarn) take priority over generic Node.js.

---

## 🛠️ Advanced Configuration

### Config File Location

ProjectManager saves your project information in:

- **Windows:** `C:\Users\YourUser\.projectmanager\projects.json`
- **Linux/Mac:** `~/.projectmanager/projects.json`

### `projects.json` File Structure
```json
{
  "web-api": {
    "name": "web-api",
    "path": "C:\\Users\\User\\projects\\web-api",
    "type": "GRADLE",
    "commands": {
      "build": "gradle build",
      "run": "gradle run",
      "test": "gradle test",
      "clean": "gradle clean"
    },
    "envVars": {
      "PORT": "8080",
      "DEBUG": "true"
    },
    "lastModified": "2025-01-18T15:30:00Z"
  }
}
```

### Manual Editing (Advanced)

⚠️ **Not recommended for normal users.**

If you need to modify commands or variables manually:

1. Open the `projects.json` file.
2. Modify the `commands` or `envVars` field.
3. Save the file.

**Example - Adding an environment variable:**
```json
"envVars": {
  "DEBUG": "true",
  "PORT": "8080",
  "NEW_VAR": "new_value"  ← Added
}
```

---

## ❓ Frequently Asked Questions (FAQ)

### Where are my projects saved?

In a JSON file located at:

- Windows: `C:\Users\YourUser\.projectmanager\projects.json`
- Linux/Mac: `~/.projectmanager/projects.json`

### Can I edit the JSON file directly?

Yes, but **it is not recommended**. It is better to use `pm` commands to avoid syntax errors.

### Are environment variables secure?

Variables are saved in **plain text** in the JSON file. **Do not save secret keys or passwords** for production. It is fine for local development.

### What happens if I move a project to another folder?

Update the path with `pm rename`:
```bash
pm rename my-project --path C:\new\path
```

All commands, environment variables, and project type are preserved.

### Can I change the default commands?

Currently, only by manually editing the `projects.json` file.

**Tip:** You can edit the `projects.json` file directly, or re-register the project with `pm remove` + `pm add`.

### Does it work with any type of project?

ProjectManager automatically detects:

- Java (Gradle, Maven)
- JavaScript/TypeScript (npm, pnpm, Bun, Yarn)
- C# / F# (.NET)
- Python (basic)
- Rust (Cargo)
- Go
- Flutter/Dart

For other types, use `--type UNKNOWN` and configure commands manually.

### How do I uninstall ProjectManager?

**Windows:**
```powershell
Remove-Item $env:USERPROFILE\bin\pm.bat
Remove-Item $env:USERPROFILE\.projectmanager -Recurse
```

**Linux/Mac:**
```bash
rm ~/bin/pm
rm -rf ~/.projectmanager
```

Then remove `~/bin` from the PATH in your `.bashrc` or `.zshrc`.

---

## 🛡️ Data Safety & Error Handling

ProjectManager protects your data with multiple layers of safety:

### Atomic Writes

Every time you modify a project (add, remove, rename, env set, commands add, etc.), ProjectManager writes to a **temporary file first**, then renames it to `projects.json`. This means:

- If your computer loses power mid-write, your data is safe
- If the disk runs out of space, the original file is untouched
- No partial or corrupted writes can happen

### Automatic Backup

Before every write, the current `projects.json` is backed up to `projects.json.bak`. This happens automatically — you don't need to do anything.

**Location:** `~/.projectmanager/projects.json.bak`

### Automatic Recovery

If `projects.json` becomes corrupted (e.g., manual editing error), ProjectManager automatically:

1. Detects the corruption on the next command
2. Loads the backup (`projects.json.bak`)
3. Restores the backup as the main file
4. Shows a warning: *"projects.json was corrupted — restored from backup (N projects recovered)"*

### Validation

When loading projects, ProjectManager validates each entry:

| Issue | Behavior |
|-------|----------|
| Missing path | Entry skipped with warning |
| Unknown project type (e.g., `"type": "INVALID"`) | Defaults to UNKNOWN with warning |
| Missing project name | Uses map key as fallback |
| Null commands/envVars | Treated as empty |

This means one corrupted entry won't prevent the rest from loading.

### Friendly Error Messages

ProjectManager never shows Java stack traces. Instead, you get clear messages with guidance:

| Error | Message |
|-------|---------|
| Permission denied | *"Permission denied: /path — check file permissions"* |
| Disk full | *"Disk is full — free some space and try again"* |
| Corrupted JSON (no backup) | *"projects.json is corrupted — Location: /path"* |
| Unexpected error | *"If this persists, run `pm doctor` to diagnose"* |

### Git Feedback

When viewing project info (`pm info`), git information now shows clear feedback instead of hiding failures:

| Situation | Display |
|-----------|---------|
| Not a git repository | `Git: not a repository` |
| Git not installed | `Branch: could not read (is git installed?)` |
| No remote tracking branch | `Unpushed: no remote tracking branch` |

### Safe Command Execution

When running `pm build`, `pm run`, or `pm test`, ProjectManager validates that the project directory exists **before** executing any command. If the directory is missing:

```
❌ Project directory not found: /home/user/old-project
The directory may have been moved, renamed, or deleted.
To update the path, run:
  pm rename my-project --path /new/path
```

When adding custom commands with `pm commands add`, ProjectManager checks for shell metacharacters (`&`, `|`, `;`, `$`, etc.) and shows a non-blocking warning:

```
⚠️  Command contains shell special characters: '&', '|'
  This is fine if intentional (e.g., chaining commands with '&&').
  If your command includes file paths with special characters,
  make sure they are properly quoted.
```

This warning is informational — it does **not** block the command from being saved. Commands like `npm build && npm serve` are perfectly valid.

### Robust Auto-Update

When running `pm update`, ProjectManager validates the downloaded JAR against the expected file size reported by the GitHub API. If the sizes don't match, the update is rejected to prevent installing a corrupted file:

```
❌ Download size mismatch: got 1.2 MB but expected 5.0 MB. The file may be incomplete or corrupted.
  The downloaded file may be incomplete or corrupted.
  Try again, or download manually from:
  https://github.com/SoftDryzz/ProjectManager/releases
```

Redirect loops are capped at 5 hops. If the download URL causes too many redirects, a clear error is shown instead of hanging indefinitely.

Network errors are classified with specific advice:

| Error | Message | Advice |
|-------|---------|--------|
| No internet | "No internet connection." | Check your connection and try again |
| Timeout | "Connection timed out." | Server may be slow, try later |
| Firewall | "Connection refused." | A firewall or proxy may be blocking |
| SSL error | "Secure connection failed." | Network may be intercepting connections |

At startup, if there's no internet connection, you'll see a brief non-blocking message instead of silent failure:
```
  Update check skipped (no internet connection)
```

---

## 🆘 Troubleshooting

### Error: "pm is not recognized as a command"

**Cause:** The `pm` alias is not in the PATH.

**Solution:**

1. Verify you ran the installation script: `.\scripts\install.ps1`.
2. Restart PowerShell completely (close and reopen).
3. Verify that `C:\Users\YourUser\bin` is in the PATH: `echo $env:Path`.
4. If it's not there, run the installation script again.

---

### Error: "Project not found"

**Cause:** The project name is not registered or is misspelled.

**Solution:**

1. List all registered projects: `pm list`.
2. Check that the name is exact (case-sensitive).
3. If it doesn't appear, register it: `pm add project-name --path C:\path`.

---

### Error: "No 'build' command configured for this project"

**Cause:** The project does not have a `build` command configured.

**Solution:**

1. See which commands are available: `pm commands project-name`.
2. Use an available command (e.g., `run`, `test`).
3. If the project has no commands, it was detected as type UNKNOWN. Re-register specifying the type:
```bash
pm remove project-name
pm add project-name --path C:\path --type GRADLE
```

---

### Error: "Path does not exist"

**Cause:** The specified path does not exist or is misspelled.

**Solution:**

1. Verify the path exists: `dir C:\path\to\project`.
2. Use the full path (not relative):
   - ❌ Bad: `pm add project --path .\my-project`
   - ✅ Good: `pm add project --path C:\Users\User\projects\my-project`
3. If using `~`, use the full path on Windows (tildes don't always resolve correctly in all shells).

---

### Error: "java is not recognized as a command"

**Cause:** Java is not installed or not in the PATH.

**Solution:**

1. Verify Java is installed: `java -version`.
2. If not installed, download from: https://adoptium.net/
3. Ensure you check "Add to PATH" during installation.
4. Restart PowerShell after installing.

---

### Environment variables are not being used

**Cause:** The command might not be using the correct injection method.

**Verification:**

1. Confirm variables are configured: `pm info project-name` or `pm env list project-name`.
2. Variables should appear in the "Environment Variables" section.
3. If they don't appear, add them with `pm env set project-name KEY=VALUE`.

---

## 📝 Quick Cheatsheet
```bash
# === MANAGEMENT ===
pm add <name> --path <path>                    # Register project
pm add <name> --path <path> --env "K=v,K2=v2"  # Register with variables
pm list                                        # List all
pm info <name>                                 # View full details
pm commands <name>                             # View available commands
pm commands <name> add <cmd> "<line>"          # Add a custom command
pm commands <name> remove <cmd>                # Remove a command
pm commands --all                              # View all commands (all projects)
pm remove <name>                               # Remove (with confirmation)
pm remove <name> --force                       # Remove (without confirmation)

# === EXECUTION ===
pm build <name>                                # Build (with env vars)
pm run <name>                                  # Run (with env vars)
pm test <name>                                 # Test (with env vars)

# === ENVIRONMENT VARIABLES ===
pm env set <name> KEY=VALUE[,K2=V2]            # Set variables
pm env get <name> KEY                          # Get a variable
pm env list <name>                             # List (masked)
pm env list <name> --show                      # List (revealed)
pm env remove <name> KEY                       # Remove a variable
pm env clear <name>                            # Remove all variables
pm env files <name>                            # List .env files in project
pm env show <name> .env                        # Show .env contents (masked)
pm env show <name> .env.local --show           # Show .env contents (revealed)
pm env switch <name> production                # Copy .env.production → .env

# === HOOKS ===
pm hooks <name>                                # List hooks
pm hooks <name> add pre-build "npm run lint"   # Add a pre-hook
pm hooks <name> add post-test "echo done"      # Add a post-hook
pm hooks <name> remove pre-build "npm run lint" # Remove a hook
pm hooks --all                                 # List all hooks

# === RENAME ===
pm rename <old> <new>                          # Rename project
pm rename <name> --path <path>                 # Update project path
pm rename <old> <new> --path <path>            # Both at once

# === REFRESH ===
pm refresh <name>                              # Re-detect type and update commands
pm refresh --all                               # Refresh all projects

# === SHELL AUTOCOMPLETION ===
pm completions bash                            # Generate Bash completion script
pm completions zsh                             # Generate Zsh completion script
pm completions fish                            # Generate Fish completion script
pm completions powershell                      # Generate PowerShell completion script

# === DIAGNOSTICS ===
pm doctor                                      # Full report: runtimes + project health (A-F)
pm doctor --score                              # Compact: only health grades per project

# === SECURITY ===
pm secure                                      # Scan all projects for security issues
pm secure --fix                                # Auto-fix .gitignore issues

# === AUDIT ===
pm audit                                       # Audit dependencies for known vulnerabilities

# === EXPORT & IMPORT ===
pm export                                      # Export all projects to pm-export.json
pm export backend web-server                   # Export selected projects
pm export --file backup.json                   # Export to custom file
pm import pm-export.json                       # Import projects from file

# === CI/CD ===
pm ci                                          # Show CI/CD for all projects
pm ci <name>                                   # Show CI/CD for a specific project

# === LINT & FORMAT ===
pm lint                                        # Run linters on all projects
pm lint <name>                                 # Run linters on a specific project
pm fmt                                         # Run formatters on all projects
pm fmt <name>                                  # Run formatters on a specific project

# === WORKSPACES ===
pm modules                                     # Show workspace modules for all projects
pm modules <name>                              # Show workspace modules for a project
pm build --all                                 # Build all registered projects
pm test --all                                  # Test all registered projects

# === DATABASE MIGRATIONS ===
pm migrate                                     # List migration tools per project
pm migrate <name>                              # Run migration (with confirmation)
pm migrate <name> status                       # Check migration status

# === CONFIGURATION ===
pm config telemetry                            # Check telemetry status
pm config telemetry on                         # Enable anonymous telemetry
pm config telemetry off                        # Disable telemetry

# === LICENSE ===
pm license                                     # Show license status
pm license info                                # Show license status
pm license activate <key>                      # Activate a Pro license key
pm license deactivate                          # Remove license, revert to Community

# === PERFORMANCE TRACKING ===
pm stats <name>                                # Show build/test/run time history
pm stats --all                                 # Show performance summary (all projects)

# === UPDATES ===
pm update                                      # Update to latest version

# === HELP ===
pm help                                        # General help
pm version                                     # View version
```

---

## 🎬 Complete Workflow

### First Time (Initial Setup)
```bash
# 1. Install ProjectManager
.\scripts\install.ps1

# 2. Restart PowerShell

# 3. Verify installation
pm version

# 4. Register your projects
pm add project1 --path C:\projects\project1
pm add project2 --path C:\projects\project2 --env "PORT=8080"
pm add project3 --path C:\projects\project3 --env "DEBUG=true,API_URL=localhost"

# 5. Verify they were registered
pm list
```

---

### Daily Use
```bash
# View all projects
pm list

# Build a project
pm build project1

# Run a project (automatically uses variables)
pm run project2

# View project info (includes variables and Git)
pm info project1

# View available commands
pm commands project1

# Everything works the same from any folder!
```

---

## 🚀 Next Steps

Now that you know ProjectManager:

1. **Register all your current projects.**
2. **Add environment variables where needed.**
3. **Use it in your daily workflow.**
4. **Explore Git integration** via `pm info`.
5. **Share with your team** so everyone uses consistent commands.

---

## 📚 Additional Resources

- **Main README:** [README.md](/README.md)
- **Installation Guide:** [scripts/INSTALL.md](/scripts/INSTALL.md)
- **Source Code:** [src/main/java/pm/](/src/main/java/pm/)

---

## 🤝 Need Help?

If you have issues or questions:

1. Check the [Troubleshooting](#-troubleshooting) section.
2. Consult the [FAQ](#-frequently-asked-questions-faq).
3. Open an issue on GitHub.

---

**Happy coding with ProjectManager! 🎉**
