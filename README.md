# ProjectManager 🛠️

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-Active-green.svg)]()

**One command for all your projects. No matter the stack.**

> Stop wasting time remembering if it's `gradle build`, `mvn package`, `cargo build`, `flutter build`, or `npm run build`. Just use `pm build`.

[🇪🇸 Leer en Español](docs/es/README.md)

---

## 📑 Table of Contents

- [Why ProjectManager?](#-why-projectmanager)
- [Quick Win Example](#-quick-win-example)
- [Features](#-features)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [Usage](#-usage)
- [Supported Project Types](#️-supported-project-types)
- [Environment Variables](#-environment-variables)
- [Git Integration](#-git-integration)
- [How It Compares](#-how-it-compares)
- [Project Structure](#-project-structure)
- [Configuration](#️-configuration)
- [Roadmap](#-roadmap)
- [Contributing](#-contributing)

---

## 🎯 Why ProjectManager?

### The Problem You Face Daily

**You're a developer with multiple projects:**
```bash
# Project 1 (Gradle)
cd ~/projects/api-users
gradle build
# Wait... was it gradle or gradlew?

# Project 2 (Maven)
cd ~/projects/backend
mvn clean package
# Or was it mvn install?

# Project 3 (npm)
cd ~/projects/frontend
npm run build
# Need to set PORT=3000... or was it 3001?

# Check git status everywhere
cd ~/projects/api-users && git status
cd ~/projects/backend && git status
cd ~/projects/frontend && git status
```

**Result:**
- ⏰ **30+ minutes wasted daily** navigating folders and looking up commands
- 🧠 **Mental overhead** remembering different build systems
- 😫 **Context switching** between 5+ different projects
- 🐛 **Errors** from using wrong commands or configurations

---

### The ProjectManager Way

**Same developer, same projects:**
```bash
# From anywhere, any folder
pm build api-users
pm build backend
pm build frontend

# Run with correct config automatically
pm run api-users    # Uses PORT=3000
pm run backend      # Uses PORT=8080

# Check all git repos instantly
pm info api-users   # Branch: main, 2 modified
pm info backend     # Branch: dev, ✓ clean
pm info frontend    # Branch: feature/ui, 3 commits unpushed
```

**Result:**
- ✅ **5 seconds** per command
- ✅ **No thinking** required
- ✅ **Work from anywhere**
- ✅ **Never forget** configurations

---

### Real Impact

**Time saved per week:**
- Command lookups: ~2 hours
- Folder navigation: ~1 hour
- Configuration mistakes: ~30 min
- Git status checking: ~45 min

**Total: ~4 hours/week = 16 hours/month = 2 full workdays**

---

### Who Benefits Most?

✅ **Full-stack developers** - Multiple technologies daily  
✅ **Team leads** - Standardize commands across team  
✅ **Students** - Learn new stacks without command confusion  
✅ **DevOps engineers** - Manage multiple microservices  
✅ **Anyone with 3+ projects** - Simplify your workflow  

---

## ⚡ Quick Win Example

### Before ProjectManager

**Monday morning, 3 APIs to start:**
```bash
cd ~/work/user-service
cat README.md  # Find instructions
export PORT=3001
export DB_HOST=localhost
mvn spring-boot:run

cd ~/work/product-service
npm install  # Just in case
PORT=3002 npm start

cd ~/work/order-service
# Was this Gradle or Maven?
ls  # Check for pom.xml or build.gradle
gradle bootRun --args='--server.port=3003'
```

**Time:** 10-15 minutes (if everything works)  
**Mental load:** High  
**Error risk:** Medium  

---

### After ProjectManager

**Monday morning, same 3 APIs:**
```bash
pm run user-service
pm run product-service
pm run order-service
```

**Time:** 15 seconds  
**Mental load:** Zero  
**Error risk:** None  

**Setup time:** 5 minutes (one time)  
**Time saved:** Every single day  

---

## ✨ Features

- 🔍 **Automatic detection** - Detects Gradle, Maven, Node.js, .NET, Python, Rust, Go, pnpm, Bun, Yarn, Docker automatically
- 🎯 **Unified commands** - Same commands for all projects: `pm build`, `pm run`, `pm test`
- 📦 **Centralized management** - All projects in one place, accessible from anywhere
- ⚡ **Fast execution** - No folder navigation, instant command execution
- 💾 **Persistence** - Configuration saved in JSON, survives restarts
- 🌿 **Git integration** - See branch, status, and unpushed commits in `pm info`
- 🔧 **Environment variables** - Configure per-project variables (PORT, DEBUG, API_KEY, etc)
- 🩺 **Runtime checker** - Detects missing runtimes before execution, shows install instructions
- 🏥 **pm doctor** - Diagnose your environment: verify installed tools, validate project paths, and get A–F health scores
- 📊 **Health scores** - Project health grading (A/B/C/D/F) based on best practices (.gitignore, README, tests, CI, lockfile)
- 🔄 **Auto-update** - Check for updates on startup, update with `pm update`
- 🔃 **Project refresh** - Re-detect types and update commands with `pm refresh`, warns when projects are outdated
- ✏️ **Rename & path update** - Rename projects or update paths with `pm rename`, preserving all data
- 🎨 **Custom commands** - Add your own commands with `pm commands add` (tunnel, lint, deploy, etc.)
- 🛡️ **Data safety** - Atomic writes, automatic backup, and recovery from corrupted JSON
- 🔒 **Safe execution** - Directory validation before running commands, metacharacter warnings
- 🛡️ **Robust auto-update** - Download integrity validation, redirect loop protection, descriptive network error messages
- 🐳 **Docker support** - Detect Docker Compose projects, default commands (build, up, down, clean)
- 🪝 **Pre-/post-command hooks** - Run custom scripts before or after any command with `pm hooks`
- 🔤 **Shell autocompletion** - TAB completion for bash, zsh, fish, and PowerShell with `pm completions`
- 🔐 **Security scan** - Detect misconfigurations (Dockerfile root, exposed secrets, insecure URLs) with `pm secure`, auto-fix with `--fix`
- 🔍 **Dependency audit** - Scan dependencies for known vulnerabilities with `pm audit` using native tools (npm audit, cargo audit, govulncheck, pip-audit, dotnet)
- 🌐 **Multi-platform** - Works on Windows, Linux, and Mac

---

## 📋 Requirements

- Java 17 or higher (recommended: Java 21 LTS)
- Maven 3.6 or higher
- Git (optional, for repository information)

---

## 🚀 Installation

**Quick start** — download the latest release and run the installer:

```bash
# Windows (PowerShell)
powershell -ExecutionPolicy Bypass -File .\scripts\install.ps1

# Linux/Mac
chmod +x scripts/install.sh && ./scripts/install.sh
```

📖 **Full installation guide** (step-by-step, build from source, troubleshooting, uninstall): **[INSTALL.md](scripts/INSTALL.md)**

---

## 💻 Usage

### Available Commands

| Command | Description |
|---------|-------------|
| `pm add <name> --path <path>` | Register a new project |
| `pm add <name> --path <path> --env "KEY=value,..."` | Register with environment variables |
| `pm list` | List all projects |
| `pm build <name>` | Build a project |
| `pm run <name>` | Run a project |
| `pm test <name>` | Run tests |
| `pm commands <name>` | View available commands |
| `pm commands <name> add <cmd> "<line>"` | Add a custom command |
| `pm commands <name> remove <cmd>` | Remove a command |
| `pm commands --all` | View commands for all projects |
| `pm info <name>` | View detailed information (including Git status) |
| `pm remove <name>` | Remove project |
| `pm env set <name> KEY=VALUE` | Set environment variables |
| `pm env get <name> KEY` | Get a variable value |
| `pm env list <name> [--show]` | List variables (sensitive values masked) |
| `pm env remove <name> KEY` | Remove a variable |
| `pm env clear <name>` | Remove all variables |
| `pm hooks <name>` | List hooks for a project |
| `pm hooks <name> add <slot> "<script>"` | Add a pre-/post-command hook |
| `pm hooks <name> remove <slot> "<script>"` | Remove a hook |
| `pm hooks --all` | List hooks for all projects |
| `pm completions <shell>` | Generate completion script (bash/zsh/fish/powershell) |
| `pm rename <old> <new>` | Rename a project |
| `pm rename <name> --path <path>` | Update project path |
| `pm refresh <name>` | Re-detect project type and update commands |
| `pm refresh --all` | Refresh all registered projects |
| `pm update` | Update to the latest version |
| `pm doctor` | Diagnose environment (runtimes, paths, health scores) |
| `pm doctor --score` | Show only health grades (A/B/C/D/F) per project |
| `pm secure` | Scan projects for security misconfigurations |
| `pm secure --fix` | Auto-fix .gitignore issues (add .env, *.pem, *.key entries) |
| `pm audit` | Audit dependencies for known vulnerabilities |
| `pm help` | Show help |
| `pm version` | Show version |

### Examples
```bash
# Register a project (automatic detection)
pm add my-api --path ~/projects/my-api

# Register with environment variables
pm add my-api --path ~/projects/my-api --env "PORT=8080,DEBUG=true,API_KEY=secret"

# List all projects
pm list

# Build any project
pm build my-api

# Run with environment variables (automatic)
pm run my-api

# View project info + Git status
pm info my-api
```

**Example output:**
```
Project Information
───────────────────

my-api (Maven)
  Path: /home/user/projects/my-api
  Modified: 5 minutes ago
  Commands: 5
  Environment Variables: 3

  Git:
    Branch: feature/new-endpoint
    Status: 2 modified, 1 untracked
    Unpushed: 3 commits

Commands for my-api (Maven)

  Default
  build  →  mvn package
  run    →  mvn exec:java
  test   →  mvn test
  clean  →  mvn clean

  Custom
  lint   →  mvn checkstyle:check

Environment Variables
  PORT    = 8080
  DEBUG   = true
  API_KEY = secret
```

---

## 🗂️ Supported Project Types

| Type | Detection Files | Default Commands |
|------|----------------|------------------|
| **Gradle** | `build.gradle`, `build.gradle.kts` | build, run, test, clean |
| **Maven** | `pom.xml` | package, exec:java, test, clean |
| **Rust** | `Cargo.toml` | build, run, test, clean |
| **Go** | `go.mod` | build, run, test, clean |
| **Flutter** | `pubspec.yaml` | build, run, test, clean |
| **pnpm** | `pnpm-lock.yaml` | build, dev, test |
| **Bun** | `bun.lockb`, `bun.lock` | build, dev, test |
| **Yarn** | `yarn.lock` | build, start, test |
| **Node.js** | `package.json` (fallback) | build, start, test |
| **.NET** | `*.csproj`, `*.fsproj` | build, run, test |
| **Python** | `requirements.txt`, `setup.py` | (manual configuration) |
| **Docker** | `docker-compose.yml`, `docker-compose.yaml` | build, up, down, clean |

> **Detection priority:** Language types always take priority. When a project has both `pom.xml` and `docker-compose.yml`, it's detected as Maven (not Docker). Docker is only detected when no language-specific type is found. For JS projects, specific package managers (pnpm, Bun, Yarn) take priority over generic Node.js.

**Can't find your stack?** ProjectManager works with any project - just configure commands manually.

---

## 🔧 Environment Variables

### What Are They For?

Stop setting environment variables manually every time. Configure once, use forever.

### Common Use Cases

**API with configurable port:**
```bash
pm add my-api --path ~/my-api --env "PORT=8080,HOST=localhost"
pm run my-api  # Automatically uses PORT=8080
```

**Project with API keys:**
```bash
pm add backend --path ~/backend --env "API_KEY=abc123,DB_HOST=localhost,DEBUG=true"
pm run backend  # All variables available
```

**Java project with JVM options:**
```bash
pm add big-project --path ~/big-project --env "MAVEN_OPTS=-Xmx4G -XX:+UseG1GC"
pm build big-project  # Uses 4GB RAM automatically
```

### Manage Variables Anytime

```bash
pm env set my-api PORT=8080,DEBUG=true     # Set variables
pm env get my-api PORT                     # Get a value
pm env list my-api                         # List (sensitive values masked)
pm env list my-api --show                  # List (all values revealed)
pm env remove my-api DEBUG                 # Remove a variable
pm env clear my-api                        # Remove all variables
```

### How It Works

1. **Register once** with variables (or add them later with `pm env set`)
2. **Variables saved** in configuration
3. **Automatically injected** when you run `pm build`, `pm run`, or `pm test`
4. **View anytime** with `pm info` or `pm env list`

---

## 🌿 Git Integration

Know your repository status without leaving your current folder.

**What you see in `pm info`:**
- **Current branch** - Which branch you're working on
- **Working tree status** - Modified, staged, untracked files
- **Unpushed commits** - How many commits need to be pushed

**Benefits:**
- ✅ Check multiple repos instantly
- ✅ Never forget to commit/push
- ✅ See which branch you're on without `git status`

---

## 🔄 How It Compares

| Task | Without ProjectManager | With ProjectManager |
|------|----------------------|---------------------|
| Build a project | `cd folder && gradle build` | `pm build myproject` |
| Run with config | `cd folder && PORT=8080 mvn exec:java` | `pm run myproject` |
| Check git status | `cd folder && git status` | `pm info myproject` |
| Switch projects | `cd ../other && ...` | `pm build other` |
| Remember commands | Check docs/README | `pm commands myproject` |

**vs Other Tools:**
- **Make/Task runners:** Requires per-project setup, no cross-technology support
- **Shell aliases:** Limited functionality, manual per-project configuration
- **IDE:** Locked to one editor, no CLI workflow
- **ProjectManager:** ✅ Universal, ✅ Portable, ✅ 5-minute setup

---

## 📁 Project Structure
```
ProjectManager/
├── src/main/java/pm/
│   ├── ProjectManager.java       # Main class
│   ├── core/                     # Models (Project, CommandInfo)
│   ├── cli/                      # CLI interface
│   ├── detector/                 # Type detection
│   ├── executor/                 # Command execution
│   ├── storage/                  # JSON persistence
│   └── util/                     # Utilities (Git, Adapters)
├── scripts/
│   ├── install.ps1               # Windows installer
│   ├── install.sh                # Linux/Mac installer
│   └── INSTALL.md                # Installation guide
├── docs/
│   └── es/                       # Spanish documentation
│       ├── README.md
│       ├── User-Guide.md
│       ├── ROADMAP.md
│       └── SECURITY.md
├── User-Guide.md                 # Complete user guide (English)
├── ROADMAP.md                    # Future plans and ideas
├── SECURITY.md                   # Security policy
├── CONTRIBUTING.md               # Contribution guidelines
└── pom.xml
```

---

## 🛠️ Configuration

Projects are saved in:
- **Windows:** `C:\Users\User\.projectmanager\projects.json`
- **Linux/Mac:** `~/.projectmanager/projects.json`

**Manual editing supported** (advanced users only)

---

## 🚧 Roadmap

### ✅ Completed
- **Core** — Project registration, auto-detection (12 types), unified commands, JSON persistence
- **CLI** — `pm doctor`, `pm env`, `pm refresh`, `pm rename`, `pm update`, `pm commands add/remove`
- **Runtimes** — Gradle, Maven, Node.js, .NET, Python, Rust, Go, pnpm, Bun, Yarn, Flutter, Docker
- **Integrations** — Git status, interactive TTY, multi-platform installers, GitHub Actions
- **Security** — `pm secure` scans for misconfigurations, `--fix` auto-remediates .gitignore issues
- **Audit** — `pm audit` checks dependencies for known vulnerabilities using native ecosystem tools
- **Reliability** — Atomic writes, backup/recovery, directory validation, download integrity, 554 tests

> Latest release: **v1.6.3** (Dependency Audit) — Full version history in [ROADMAP.md](ROADMAP.md)

### 💡 Future Ideas
- [ ] `pm run-all` / `pm build-all` - Execute commands across all projects
- [ ] Project groups (`pm group create backend api-users product-service`, `pm run-group backend`)
- [ ] Shell autocompletion (bash/zsh/PowerShell tab completion)
- [ ] Multi-ecosystem installers (npm, Cargo, Homebrew, Scoop, etc.)

> See [ROADMAP.md](ROADMAP.md) for the full roadmap with detailed plans.

---

## 🐛 Found a Bug?

We take bugs seriously! If you encounter a problem:

1. **Check existing issues:** [Open Issues](https://github.com/SoftDryzz/ProjectManager/issues)
2. **Report a new bug:** [Create Bug Report](https://github.com/SoftDryzz/ProjectManager/issues/new/choose)

**What to include in your bug report:**
- Clear description of the problem
- Steps to reproduce
- Expected vs actual behavior
- `pm version` output
- Operating system
- Error messages (if any)

**Example:**
```
Bug: pm build fails on Windows with spaces in path

Steps:
1. pm add myproject --path "C:\My Projects\test"
2. pm build myproject
3. Error: Path not found

Expected: Build succeeds
Actual: Error with path containing spaces
```

---

## 💡 Feature Requests

Have an idea to improve ProjectManager? We'd love to hear it!

[Submit Feature Request](https://github.com/SoftDryzz/ProjectManager/issues/new/choose)

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the project
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

---

## 📄 License

This project is licensed under the **GNU Affero General Public License v3.0 (AGPLv3)**. See [`LICENSE`](LICENSE) for details.

Commercial licensing is available for organizations that require alternative terms. See [`COMMERCIAL.md`](COMMERCIAL.md) for details or contact: **legal@softdryzz.com**

---

## 👤 Author

**SoftDryzz**

- GitHub: [@SoftDryzz](https://github.com/SoftDryzz)

---

**⭐ If ProjectManager saves you time, give it a star on GitHub!**

**💬 Questions? Open an issue or check the [User Guide](User-Guide.md)**
