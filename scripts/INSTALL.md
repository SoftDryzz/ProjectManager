# ProjectManager - Installation Guide

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Installation

### 1. Compile the project
```bash
mvn clean package
```

### 2. Run installer

**Windows (PowerShell):**
```powershell
.\scripts\install.ps1
```

**Linux/Mac:**
```bash
chmod +x scripts/install.sh
./scripts/install.sh
```

### 3. Restart your terminal

Close and reopen your terminal/PowerShell.

### 4. Verify installation
```bash
pm help
pm version
```

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
```

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
