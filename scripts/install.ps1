# ProjectManager - Installation Script for Windows
# Works both from source (mvn clean package) and from GitHub Release download

$jarName = "projectmanager-1.6.4.jar"

Write-Host "=== ProjectManager Installer ===" -ForegroundColor Cyan
Write-Host ""

# 1. Search for JAR in multiple locations
$scriptDir = $PSScriptRoot
$searchPaths = @(
    "$scriptDir\..\target\$jarName",       # Built from source (mvn clean package)
    "$scriptDir\..\$jarName",              # JAR placed in project root
    "$scriptDir\$jarName",                 # JAR placed next to script
    "$scriptDir\..\..\$jarName"            # One level up (nested ZIP extraction)
)

$jarPath = $null
foreach ($path in $searchPaths) {
    if (Test-Path $path) {
        $jarPath = (Resolve-Path $path).Path
        break
    }
}

# Also search by pattern in case version differs
if (-not $jarPath) {
    $patterns = @(
        "$scriptDir\..\target\projectmanager-*.jar",
        "$scriptDir\..\projectmanager-*.jar",
        "$scriptDir\projectmanager-*.jar"
    )
    foreach ($pattern in $patterns) {
        $found = Get-ChildItem -Path $pattern -ErrorAction SilentlyContinue | Where-Object { $_.Name -notlike "*-javadoc*" -and $_.Name -notlike "*original-*" } | Select-Object -First 1
        if ($found) {
            $jarPath = $found.FullName
            break
        }
    }
}

if (-not $jarPath) {
    Write-Host "Error: JAR not found ($jarName)" -ForegroundColor Red
    Write-Host ""
    Write-Host "Searched in:" -ForegroundColor Yellow
    foreach ($path in $searchPaths) {
        Write-Host "  - $path" -ForegroundColor Gray
    }
    Write-Host ""
    Write-Host "Options:" -ForegroundColor Yellow
    Write-Host "  1. Download the JAR from the GitHub Release page and place it next to this script" -ForegroundColor Cyan
    Write-Host "     https://github.com/SoftDryzz/ProjectManager/releases/latest" -ForegroundColor Cyan
    Write-Host "  2. Build from source: mvn clean package" -ForegroundColor Cyan
    exit 1
}

Write-Host "Found: $jarPath" -ForegroundColor Green

# 2. Copy JAR to a permanent location
$installDir = "$env:USERPROFILE\.projectmanager"
if (-not (Test-Path $installDir)) {
    New-Item -Path $installDir -ItemType Directory -Force | Out-Null
}

$installedJar = "$installDir\projectmanager.jar"
Copy-Item -Path $jarPath -Destination $installedJar -Force
Write-Host "Installed: $installedJar" -ForegroundColor Green

# 3. Create bin directory
$binDir = "$env:USERPROFILE\bin"
if (-not (Test-Path $binDir)) {
    New-Item -Path $binDir -ItemType Directory -Force | Out-Null
    Write-Host "Created: $binDir" -ForegroundColor Green
}

# 4. Create pm.bat pointing to permanent location (with auto-update swap)
$pmBat = "$binDir\pm.bat"
@"
@echo off
set "PM_JAR=$installedJar"
set "PM_NEW=$installDir\projectmanager.jar.new"
if exist "%PM_NEW%" (
    del "%PM_JAR%" 2>nul
    move "%PM_NEW%" "%PM_JAR%" >nul
)
java -jar "%PM_JAR%" %*
"@ | Out-File -FilePath $pmBat -Encoding ASCII -Force

Write-Host "Created: $pmBat" -ForegroundColor Green

# 5. Add bin to PATH if not already there
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($currentPath -notlike "*$binDir*") {
    [Environment]::SetEnvironmentVariable("Path", "$currentPath;$binDir", "User")
    Write-Host "Added to PATH: $binDir" -ForegroundColor Green
} else {
    Write-Host "Already in PATH: $binDir" -ForegroundColor Yellow
}

# 6. Verify Java
Write-Host ""
Write-Host "Checking Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java OK: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "WARNING: Java not found in PATH" -ForegroundColor Red
    Write-Host "Install Java from: https://adoptium.net/" -ForegroundColor Yellow
    Write-Host "Or via winget: winget install EclipseAdoptium.Temurin.17.JDK" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Installation Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Close and reopen PowerShell, then run:" -ForegroundColor Yellow
Write-Host "  pm help" -ForegroundColor Cyan
Write-Host ""
