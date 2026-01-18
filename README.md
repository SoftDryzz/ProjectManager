# ProjectManager 🛠️

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-In%20Development-yellow.svg)]()

**ProjectManager** is a CLI tool to manage multiple development projects from a single place. It automatically detects the project type, unifies build/run/test commands, displays Git information, and supports per-project environment variables.

> ⚠️ **Actively in Development:** This project is under construction and many features are still being implemented.

---

## ✨ Features

* 🔍 **Automatic detection** of project type (Gradle, Maven, Node.js, .NET, Python)
* 🎯 **Unified commands** – use `pm build` whether it’s Maven or Gradle
* 📦 **Centralized management** – all your projects in one place
* ⚡ **Fast execution** of builds, tests, and custom commands
* 💾 **Persistence** – configuration stored in JSON
* 🌿 **Git integration** – view branch, status, and unpushed commits
* 🔧 **Environment variables** – configure variables per project
* 🌐 **Cross-platform** – Windows, Linux, and Mac

---

## 📋 Requirements

* Java 17 or higher (recommended: Java 21 LTS)
* Maven 3.6 or higher
* Git (optional, to display repository information)

---

## 🚀 Quick Installation

```bash
# 1. Clone repository
git clone https://github.com/SoftDryzz/ProjectManager.git
cd ProjectManager

# 2. Build
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

| Command                                      | Description                                                |
| -------------------------------------------- | ---------------------------------------------------------- |
| `pm add <name> --path <path> [--env <vars>]` | Register a new project with optional environment variables |
| `pm list`                                    | List all projects                                          |
| `pm build <name>`                            | Build a project                                            |
| `pm run <name>`                              | Run a project                                              |
| `pm test <name>`                             | Run tests                                                  |
| `pm commands <name>`                         | Show available commands                                    |
| `pm info <name>`                             | Show detailed information (includes Git and variables)     |
| `pm remove <name>`                           | Remove a project                                           |
| `pm scan <name>`                             | 🚧 Scan commands (in development)                          |
| `pm help`                                    | Show help                                                  |
| `pm version`                                 | Show version                                               |

### Examples

```bash
# Register a project (automatic detection)
pm add web-api --path ~/projects/web-api

# Register with environment variables
pm add web-api --path ~/web-api --env "PORT=8080,DEBUG=true,web-api_KEY=secret"

# List registered projects
pm list

# Build project (automatically uses environment variables)
pm build web-api

# View full info with Git and variables
pm info web-api
```

**Example output with Git and Variables:**

```
Project Information
───────────────────

web-api (Maven)
  Path: /home/user/projects/web-api
  Modified: 5 minutes ago
  Commands: 4
  Environment Variables: 3

  Git:
    Branch: feature/new-endpoint
    Status: 2 modified, 1 untracked
    Unpushed: 3 commits

Available Commands for web-api
────────────────────────────

  build  →  mvn package
  run    →  mvn exec:java
  test   →  mvn test
  clean  →  mvn clean

Environment Variables
─────────────────────

  PORT    = 8080
  DEBUG   = true
  API_KEY = secret
```

---

## 🗂️ Supported Project Types

| Type        | Detection Files                    | Default Commands                |
| ----------- | ---------------------------------- | ------------------------------- |
| **Gradle**  | `build.gradle`, `build.gradle.kts` | build, run, test, clean         |
| **Maven**   | `pom.xml`                          | package, exec:java, test, clean |
| **Node.js** | `package.json`                     | build, start, test              |
| **.NET**    | `*.csproj`, `*.fsproj`             | build, run, test                |
| **Python**  | `requirements.txt`                 | (manual)                        |

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
│   └── USER_GUIDE.md             # Full user guide
└── pom.xml
```

---

## 🛠️ Configuration

Projects are stored in:

* **Windows:** `C:\Users\User\.projectmanager\projects.json`
* **Linux/Mac:** `~/.projectmanager/projects.json`

---

## 🌿 Git Integration

ProjectManager automatically displays Git information when you use `pm info`:

* **Current branch** – know which branch you are working on
* **Working tree status** – modified, staged, and untracked files
* **Unpushed commits** – how many commits haven’t been pushed

**Only shown if the project is a Git repository.**

---

## 🔧 Environment Variables

Configure environment variables specific to each project, automatically injected when executing commands:

```bash
# Register with variables
pm add backend --path ~/backend --env "PORT=3000,NODE_ENV=development,DB_HOST=localhost"

# Variables are used automatically
pm run backend  # Runs with PORT, NODE_ENV, and DB_HOST configured
```

**Benefits:**

* No need to remember which variables each project requires
* Different configurations for different projects
* Automatic injection for build/run/test

---

## 🚧 Roadmap

### ✅ Completed

* [x] Project registration system
* [x] Automatic type detection
* [x] Commands: add, list, build, run, test, info, remove
* [x] JSON persistence
* [x] Cross-platform installers
* [x] Complete user guide
* [x] Git integration (branch, status, unpushed commits)
* [x] Per-project environment variables
* [x] GitHub Actions (CI/CD)

### 🔨 In Development

* [ ] `scan` command to detect @Command annotations
* [ ] Custom command aliases
* [ ] Pre/post command hooks
* [ ] Unit tests
* [ ] Project templates support

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

This project is licensed under the MIT License. See the `LICENSE` file for details.

---

## 👤 Author

**SoftDryzz**

* GitHub: [@SoftDryzz](https://github.com/SoftDryzz)

---

## 🙏 Acknowledgements

* Anthropic Claude for development assistance
* The Java and Maven community

---

**⭐ If you like this project, give it a star on GitHub!**
