# ProjectManager 🛠️

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-Active-green.svg)]()

**One command for all your projects. No matter the stack.**

> Stop wasting time remembering if it's `gradle build`, `mvn package`, or `npm run build`. Just use `pm build`.

[🇪🇸 Leer en Español](README_ES.md)

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

- 🔍 **Automatic detection** - Detects Gradle, Maven, Node.js, .NET, Python automatically
- 🎯 **Unified commands** - Same commands for all projects: `pm build`, `pm run`, `pm test`
- 📦 **Centralized management** - All projects in one place, accessible from anywhere
- ⚡ **Fast execution** - No folder navigation, instant command execution
- 💾 **Persistence** - Configuration saved in JSON, survives restarts
- 🌿 **Git integration** - See branch, status, and unpushed commits in `pm info`
- 🔧 **Environment variables** - Configure per-project variables (PORT, DEBUG, API_KEY, etc)
- 🌐 **Multi-platform** - Works on Windows, Linux, and Mac

---

## 📋 Requirements

- Java 17 or higher (recommended: Java 21 LTS)
- Maven 3.6 or higher
- Git (optional, for repository information)

---

## 🚀 Quick Installation
```bash
# 1. Clone repository
git clone https://github.com/SoftDryzz/ProjectManager.git
cd ProjectManager

# 2. Compile
mvn clean package

# 3. Install (Windows)
.\scripts\install.ps1

# 3. Install (Linux/Mac)
chmod +x scripts/install.sh && ./scripts/install.sh

# 4. Verify
pm version
```

**Setup time:** 5 minutes  
**Benefits:** Forever  

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
| `pm info <name>` | View detailed information (including Git status) |
| `pm remove <name>` | Remove project |
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
  Commands: 4
  Environment Variables: 3

  Git:
    Branch: feature/new-endpoint
    Status: 2 modified, 1 untracked
    Unpushed: 3 commits

Available Commands
  build  →  mvn package
  run    →  mvn exec:java
  test   →  mvn test
  clean  →  mvn clean

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
| **Node.js** | `package.json` | build, start, test |
| **.NET** | `*.csproj`, `*.fsproj` | build, run, test |
| **Python** | `requirements.txt` | (manual configuration) |

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

### How It Works

1. **Register once** with variables
2. **Variables saved** in configuration
3. **Automatically injected** when you run `pm build`, `pm run`, or `pm test`
4. **View anytime** with `pm info`

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
│   ├── USER_GUIDE.md             # Complete user guide (English)
│   └── User-Guide_ES.md          # Complete user guide (Spanish)
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
- [x] Project registration system
- [x] Automatic type detection
- [x] Commands: add, list, build, run, test, info, remove
- [x] JSON persistence
- [x] Multi-platform installers
- [x] Complete user guide (English + Spanish)
- [x] Git integration (branch, status, pending commits)
- [x] GitHub Actions (CI/CD)
- [x] Environment variables per project

### 🔨 Planned
- [ ] Command aliases for long project names
- [ ] `pm env` command to manage variables from CLI
- [ ] Pre- / post-command hooks
- [ ] Unit tests
- [ ] `scan` command to detect @Command annotations

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the project
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'feat: add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License. See `LICENSE` file for details.

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
## 👤 Author

**SoftDryzz**

- GitHub: [@SoftDryzz](https://github.com/SoftDryzz)

---

**⭐ If ProjectManager saves you time, give it a star on GitHub!**

**💬 Questions? Open an issue or check the [User Guide](docs/USER_GUIDE.md)**
