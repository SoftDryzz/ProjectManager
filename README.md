# ProjectManager 🛠️

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-In%20Development-yellow.svg)]()

**ProjectManager** is a CLI tool to manage multiple development projects from a single place. It automatically detects project types and unifies build, run, and test commands.

> ⚠️ **Active Development:** This project is under construction and many features are being implemented.

[🇪🇸 Leer en Español](README_ES.md)

---

## ✨ Features

- 🔍 **Automatic detection** of project type (Gradle, Maven, Node.js, .NET, Python)
- 🎯 **Unified commands** - use `pm build` regardless of Maven or Gradle
- 📦 **Centralized management** - all your projects in one place
- ⚡ **Fast execution** of builds, tests, and custom commands
- 💾 **Persistence** - configuration saved in JSON
- 🌿 **Git integration** - see branch, status, and pending commits
- 🔧 **Environment variables** - configure per-project environment variables
- 🌐 **Multi-platform** - Windows, Linux, and Mac

---

## 📋 Requirements

- Java 17 or higher (recommended: Java 21 LTS)
- Maven 3.6 or higher
- Git (optional, to show repository information)

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

---

## 💻 Usage

### Available Commands

| Command | Description |
|---------|-------------|
| `pm add <name> --path <path>` | Register a new project |
| `pm add <name> --path <path> --env "KEY=value,..."` | Register project with environment variables |
| `pm list` | List all projects |
| `pm build <name>` | Build a project |
| `pm run <name>` | Run a project |
| `pm test <name>` | Run tests |
| `pm commands <name>` | View available commands |
| `pm info <name>` | View detailed information |
| `pm remove <name>` | Remove project |
| `pm scan <name>` | 🚧 Scan commands (in development) |
| `pm help` | Show help |
| `pm version` | Show version |

### Examples
```bash
# Register a project (automatic detection)
pm add my-api --path ~/projects/my-api

# Register project with environment variables
pm add my-api --path ~/projects/my-api --env "PORT=8080,DEBUG=true,API_KEY=secret"

# List registered projects
pm list

# Build project
pm build my-api

# Run project (uses configured environment variables automatically)
pm run my-api

# View available commands
pm commands my-api

# View complete information with Git
pm info my-api
```

**Example output with Git and Environment Variables:**
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
| **Python** | `requirements.txt` | (manual) |

---

## 🔧 Environment Variables

### What Are They For?

Environment variables allow you to configure specific settings for each project that are automatically applied when running commands.

### Common Use Cases

**API with configurable port:**
```bash
pm add my-api --path ~/my-api --env "PORT=8080,HOST=localhost"
pm run my-api  # Automatically uses PORT=8080
```

**Project with API keys:**
```bash
pm add backend --path ~/backend --env "API_KEY=abc123,DB_HOST=localhost,DEBUG=true"
pm run backend  # All variables are available to the process
```

**Java project with JVM options:**
```bash
pm add big-project --path ~/big-project --env "MAVEN_OPTS=-Xmx4G,-XX:+UseG1GC"
pm build big-project  # Uses the configured JVM options
```

### How It Works

1. **Register project with variables:**
```bash
   pm add myproject --path /path/to/project --env "VAR1=value1,VAR2=value2"
```

2. **Variables are saved** in the configuration (`.projectmanager/projects.json`)

3. **Automatically used** when executing:
   - `pm build myproject`
   - `pm run myproject`
   - `pm test myproject`

4. **View configured variables:**
```bash
   pm info myproject
```

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
│   └── INSTALL.md                # Documentation
├── docs/
│   └── USER_GUIDE.md             # Complete user guide
└── pom.xml
```

---

## 🛠️ Configuration

Projects are saved in:
- **Windows:** `C:\Users\User\.projectmanager\projects.json`
- **Linux/Mac:** `~/.projectmanager/projects.json`

---

## 🌿 Git Integration

ProjectManager automatically shows Git information when using `pm info`:

- **Current branch** - know which branch you're working on
- **Working tree status** - modified, staged, untracked files
- **Pending commits** - how many commits you haven't pushed

**Only shown if the project is a Git repository.**

---

## 🚧 Roadmap

### ✅ Completed
- [x] Project registration system
- [x] Automatic type detection
- [x] Commands: add, list, build, run, test, info, remove
- [x] JSON persistence
- [x] Multi-platform installers
- [x] Complete user guide
- [x] Git integration (branch, status, pending commits)
- [x] GitHub Actions (CI/CD)
- [x] Environment variables per project

### 🔨 In Development
- [ ] `scan` command to detect @Command annotations
- [ ] Command aliases
- [ ] Pre/post command hooks
- [ ] Unit tests

---

## 🤝 Contributing

Contributions are welcome. Please:

1. Fork the project
2. Create a branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -m 'feat: add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Open a Pull Request

---

## 📄 License

This project is under the MIT License. See `LICENSE` file for details.

---

## 👤 Author

**SoftDryzz**

- GitHub: [@SoftDryzz](https://github.com/SoftDryzz)

---


**⭐ If you like this project, give it a star on GitHub!**
