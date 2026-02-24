# ProjectManager - Installation Guide

## Prerequisites

- Java 17 or higher ([Download](https://adoptium.net/))

> Maven is only required if you want to build from source.

---

## Option A: Install from Release (Recommended)

### 1. Download the latest release

Go to [GitHub Releases](https://github.com/SoftDryzz/ProjectManager/releases/latest) and download the source code (ZIP).

### 2. Extract the ZIP file

### 3. Run the installer

**Windows (PowerShell):**
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install.ps1
```

> **Why `-ExecutionPolicy Bypass`?** Windows blocks scripts downloaded from the internet by default. This flag allows the script to run safely. The script only creates a `pm.bat` wrapper in `%USERPROFILE%\bin` and adds it to your PATH.

**Linux/Mac:**
```bash
chmod +x scripts/install.sh
./scripts/install.sh
```

### 4. Restart your terminal

Close and reopen your terminal/PowerShell.

### 5. Verify installation
```bash
pm help
pm version
```

---

## Option B: Build from Source

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### 1. Clone and compile
```bash
git clone https://github.com/SoftDryzz/ProjectManager.git
cd ProjectManager
mvn clean package
```

### 2. Run installer

**Windows (PowerShell):**
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install.ps1
```

**Linux/Mac:**
```bash
chmod +x scripts/install.sh
./scripts/install.sh
```

### 3. Restart your terminal

### 4. Verify installation
```bash
pm help
pm version
```

---

## Usage
```bash
# Register a project
pm add myproject --path /path/to/project

# List projects
pm list

# Build a project
pm build myproject

# Run a project
pm run myproject

# Diagnose environment
pm doctor
```

---

## Troubleshooting

### Windows: "No se puede cargar el archivo... no está firmado digitalmente"

This is a Windows Execution Policy error. Run the installer with:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install.ps1
```

### Windows: Alternative - Unblock the file first
```powershell
Unblock-File .\scripts\install.ps1
.\scripts\install.ps1
```

### Java not found

Install Java 17+ from [Adoptium](https://adoptium.net/) or via winget:
```powershell
winget install EclipseAdoptium.Temurin.17.JDK
```

---

## Uninstall

**Windows:**
```powershell
Remove-Item $env:USERPROFILE\bin\pm.bat
```

**Linux/Mac:**
```bash
rm ~/bin/pm
```

Then remove `~/bin` from your PATH in `.bashrc` or `.zshrc`.
