## 📑 Table of Contents

* [What is ProjectManager?](#-what-is-projectmanager)

* [Quick Start (5 minutes)](#-quick-start-5-minutes)

  * [Step 1: Verify Installation](#step-1-verify-installation)
  * [Step 2: Register Your First Project](#step-2-register-your-first-project)
  * [Step 3: View Your Projects](#step-3-view-your-projects)
  * [Step 4: Build Your Project](#step-4-build-your-project)

* [Command Reference](#-command-reference)

  * [Project Management](#-project-management)
  * [Command Execution](#-command-execution)
  * [Help and Version](#-help-and-version)

* [Environment Variables](#-environment-variables)

  * [What Are They?](#what-are-they)
  * [How Do They Work in ProjectManager?](#how-do-they-work-in-projectmanager)
  * [Usage Examples](#usage-examples)
  * [View Configured Variables](#view-configured-variables)
  * [Modify Variables](#modify-variables)

* [Git Integration](#-git-integration)

  * [What is it?](#what-is-it)
  * [Information Displayed](#information-displayed)
  * [Full Example](#full-example)
  * [Git Integration Use Cases](#git-integration-use-cases)
  * [Projects Without Git](#projects-without-git)
  * [Requirements](#requirements)

* [Use Cases](#-use-cases)

* [Supported Project Types](#-supported-project-types)

* [Advanced Configuration](#-advanced-configuration)

  * [Config File Location](#config-file-location)
  * [projects.json File Structure](#projectsjson-file-structure)
  * [Manual Editing](#manual-editing-advanced)

* [Frequently Asked Questions (FAQ)](#-frequently-asked-questions-faq)

* [Troubleshooting](#-troubleshooting)

* [Quick Cheatsheet](#-quick-cheatsheet)

* [Complete Workflow](#-complete-workflow)

* [Next Steps](#-next-steps)

* [Additional Resources](#-additional-resources)


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
ProjectManager 1.0.0
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
║  ProjectManager v1.0.0         ║
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

**Valid types:** `GRADLE`, `MAVEN`, `NODEJS`, `DOTNET`, `PYTHON`

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

* Project Name
* Type (Gradle, Maven, etc.)
* Full Path
* Last Modified
* Available Commands
* Configured Environment Variables
* Git Status (if it's a repository)

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

---

### How Do They Work in ProjectManager?

1. **Register the project with variables:**
```bash
pm add api --path ~/api --env "PORT=8080,DEBUG=true"

```


2. **Variables are saved in the project configuration.**
3. **They are injected automatically** when you run `pm build`, `pm run`, or `pm test`.

---

### Usage Examples

#### Project with Configurable Port

```bash
# Register with port
pm add web-server --path ~/server --env "PORT=3000"

# Run (uses PORT=3000 automatically)
pm run web-server

```

---

#### Project with Multiple Variables

```bash
# API with several settings
pm add backend --path ~/backend --env "PORT=8080,DB_HOST=localhost,DB_USER=admin,API_KEY=secret123"

# Build (variables available at build time)
pm build backend

# Run (variables available at runtime)
pm run backend

```

---

#### Maven with Memory Configuration

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

### Modify Variables

**Currently:** Manually edit the `projects.json` file.

**Location:**

* Windows: `C:\Users\User\.projectmanager\projects.json`
* Linux/Mac: `~/.projectmanager/projects.json`

🚧 **Planned Feature:** `pm env add/remove/update` to manage variables from the CLI.

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

* **staged:** Files added with `git add` (ready for commit).
* **modified:** Files modified but NOT yet added.
* **untracked:** New files that Git is not tracking.

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
║  ProjectManager v1.0.0         ║
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

  DEBUG      = true
  GAME_MODE  = creative

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

* **Git installed** on your system.
* **Project must be a Git repository** (must have a `.git` folder).

**Verify Git is installed:**

```bash
git --version

```

If not installed: [https://git-scm.com/downloads](https://git-scm.com/downloads)

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
| --- | --- | --- |
| **Gradle** | `build.gradle`, `build.gradle.kts` | `build`, `run`, `test`, `clean` |
| **Maven** | `pom.xml` | `build` (package), `run` (exec:java), `test`, `clean` |
| **Node.js** | `package.json` | `build`, `run` (start), `test` |
| **.NET** | `*.csproj`, `*.fsproj` | `build`, `run`, `test` |
| **Python** | `requirements.txt` | (manual configuration) |

---

## 🛠️ Advanced Configuration

### Config File Location

ProjectManager saves your project information in:

* **Windows:** `C:\Users\YourUser\.projectmanager\projects.json`
* **Linux/Mac:** `~/.projectmanager/projects.json`

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
      "DEBUG": "true",
      "GAME_MODE": "creative"
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

* Windows: `C:\Users\YourUser\.projectmanager\projects.json`
* Linux/Mac: `~/.projectmanager/projects.json`

### Can I edit the JSON file directly?

Yes, but **it is not recommended**. It is better to use `pm` commands to avoid syntax errors.

### Are environment variables secure?

Variables are saved in **plain text** in the JSON file. **Do not save secret keys or passwords** for production. It is fine for local development.

### What happens if I move a project to another folder?

You must update the path:

```bash
pm remove old-project
pm add old-project --path C:\new\path --env "VAR1=value1"

```

### Can I change the default commands?

Currently, only by manually editing the `projects.json` file.

🚧 **Planned Feature:** `pm config` command to modify commands from the CLI.

### Does it work with any type of project?

ProjectManager automatically detects:

* Java (Gradle, Maven)
* JavaScript/TypeScript (npm)
* C# (.NET)
* Python (basic)

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
* ❌ Bad: `pm add project --path .\my-project`
* ✅ Good: `pm add project --path C:\Users\User\projects\my-project`


3. If using `~`, use the full path on Windows (tildes don't always resolve correctly in all shells).

---

### Error: "java is not recognized as a command"

**Cause:** Java is not installed or not in the PATH.
**Solution:**

1. Verify Java is installed: `java -version`.
2. If not installed, download from: [https://adoptium.net/](https://adoptium.net/).
3. Ensure you check "Add to PATH" during installation.
4. Restart PowerShell after installing.

---

### Environment variables are not being used

**Cause:** The command might not be using the correct injection method.
**Verification:**

1. Confirm variables are configured: `pm info project-name`.
2. Variables should appear in the "Environment Variables" section.
3. If they don't appear, register the project again with `--env`.

---

## 📝 Quick Cheatsheet

```bash
# === MANAGEMENT ===
pm add <name> --path <path>                    # Register project
pm add <name> --path <path> --env "K=v,K2=v2"  # Register with variables
pm list                                        # List all
pm info <name>                                 # View full details
pm commands <name>                             # View available commands
pm remove <name>                               # Remove (with confirmation)
pm remove <name> --force                       # Remove (without confirmation)

# === EXECUTION ===
pm build <name>                                # Build (with env vars)
pm run <name>                                  # Run (with env vars)
pm test <name>                                 # Test (with env vars)

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

* **Main README:** [README.md](https://www.google.com/search?q=README.md)
* **Installation Guide:** [INSTALL.md](https://www.google.com/search?q=/scripts/INSTALL.md)
* **Source Code:** [src/main/java/pm/](https://www.google.com/search?q=/src/main/java/pm/)

---

## 🤝 Need Help?

If you have issues or questions:

1. Check the [Troubleshooting](https://www.google.com/search?q=%23-troubleshooting) section.
2. Consult the [FAQ](https://www.google.com/search?q=%23-frequently-asked-questions-faq).
3. Open an issue on GitHub.

---

**Happy coding with ProjectManager! 🎉**
