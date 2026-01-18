# Crear archivo de instalación
@'
# ProjectManager - Installation Script for Windows
# Run this after compiling with: mvn clean package

Write-Host "=== ProjectManager Installer ===" -ForegroundColor Cyan
Write-Host ""

# 1. Verificar que existe el JAR
$jarPath = "$PSScriptRoot\..\target\projectmanager-1.0.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "Error: JAR no encontrado en $jarPath" -ForegroundColor Red
    Write-Host "Por favor ejecuta primero: mvn clean package" -ForegroundColor Yellow
    exit 1
}

Write-Host "Encontrado: $jarPath" -ForegroundColor Green

# 2. Crear directorio bin en home del usuario
$binDir = "$env:USERPROFILE\bin"
if (-not (Test-Path $binDir)) {
    New-Item -Path $binDir -ItemType Directory -Force | Out-Null
    Write-Host "Creado: $binDir" -ForegroundColor Green
}

# 3. Crear pm.bat
$pmBat = "$binDir\pm.bat"
@"
@echo off
java -jar "$jarPath" %*
"@ | Out-File -FilePath $pmBat -Encoding ASCII -Force

Write-Host "Creado: $pmBat" -ForegroundColor Green

# 4. Agregar bin al PATH si no está
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
if ($currentPath -notlike "*$binDir*") {
    [Environment]::SetEnvironmentVariable("Path", "$currentPath;$binDir", "User")
    Write-Host "Agregado al PATH: $binDir" -ForegroundColor Green
} else {
    Write-Host "Ya en PATH: $binDir" -ForegroundColor Yellow
}

# 5. Verificar Java
Write-Host ""
Write-Host "Verificando Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    Write-Host "Java OK: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "ADVERTENCIA: Java no encontrado en PATH" -ForegroundColor Red
    Write-Host "Instala Java desde: https://adoptium.net/" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Instalacion Completa ===" -ForegroundColor Green
Write-Host ""
Write-Host "Cierra y abre PowerShell de nuevo, luego ejecuta:" -ForegroundColor Yellow
Write-Host "  pm help" -ForegroundColor Cyan
Write-Host ""
'@ | Out-File -FilePath scripts\install.ps1 -Encoding UTF8

Write-Host "✅ Creado: scripts\install.ps1" -ForegroundColor Green