# ProjectManager 🛠️

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-En%20Desarrollo-yellow.svg)]()

**ProjectManager** es una herramienta CLI para gestionar múltiples proyectos de desarrollo desde un solo lugar. Detecta automáticamente el tipo de proyecto y unifica los comandos de build, ejecución y testing.

> ⚠️ **En Desarrollo Activo:** Este proyecto está en construcción y muchas funcionalidades están siendo implementadas.

[🇬🇧 Read in English](README.md)

---

## ✨ Características

- 🔍 **Detección automática** de tipo de proyecto (Gradle, Maven, Node.js, .NET, Python)
- 🎯 **Comandos unificados** - usa `pm build` sin importar si es Maven o Gradle
- 📦 **Gestión centralizada** - todos tus proyectos en un solo lugar
- ⚡ **Ejecución rápida** de builds, tests y comandos personalizados
- 💾 **Persistencia** - configuración guardada en JSON
- 🌿 **Integración Git** - ve branch, status y commits pendientes
- 🔧 **Variables de entorno** - configura variables específicas por proyecto
- 🌐 **Multi-plataforma** - Windows, Linux y Mac

---

## 📋 Requisitos

- Java 17 o superior (recomendado: Java 21 LTS)
- Maven 3.6 o superior
- Git (opcional, para mostrar información de repositorios)

---

## 🚀 Instalación Rápida
```bash
# 1. Clonar repositorio
git clone https://github.com/SoftDryzz/ProjectManager.git
cd ProjectManager

# 2. Compilar
mvn clean package

# 3. Instalar (Windows)
.\scripts\install.ps1

# 3. Instalar (Linux/Mac)
chmod +x scripts/install.sh && ./scripts/install.sh

# 4. Verificar
pm version
```

---

## 💻 Uso

### Comandos Disponibles

| Comando | Descripción |
|---------|-------------|
| `pm add <nombre> --path <ruta>` | Registrar un nuevo proyecto |
| `pm add <nombre> --path <ruta> --env "CLAVE=valor,..."` | Registrar proyecto con variables de entorno |
| `pm list` | Listar todos los proyectos |
| `pm build <nombre>` | Compilar un proyecto |
| `pm run <nombre>` | Ejecutar un proyecto |
| `pm test <nombre>` | Ejecutar tests |
| `pm commands <nombre>` | Ver comandos disponibles |
| `pm info <nombre>` | Ver información detallada |
| `pm remove <nombre>` | Eliminar proyecto |
| `pm scan <nombre>` | 🚧 Escanear comandos (en desarrollo) |
| `pm help` | Mostrar ayuda |
| `pm version` | Mostrar versión |

### Ejemplos
```bash
# Registrar un proyecto (detección automática)
pm add minecraft-client --path ~/projects/minecraft-client

# Registrar proyecto con variables de entorno
pm add mi-api --path ~/projects/mi-api --env "PORT=8080,DEBUG=true,API_KEY=secreto"

# Listar proyectos registrados
pm list

# Compilar proyecto
pm build minecraft-client

# Ejecutar proyecto (usa las variables de entorno configuradas automáticamente)
pm run mi-api

# Ver comandos disponibles
pm commands minecraft-client

# Ver información completa con Git
pm info minecraft-client
```

**Ejemplo de salida con Git y Variables de Entorno:**
```
Project Information
───────────────────

mi-api (Maven)
  Path: /home/user/projects/mi-api
  Modified: hace 5 minutos
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
  API_KEY = secreto
```

---

## 🗂️ Tipos de Proyecto Soportados

| Tipo | Archivos de Detección | Comandos por Defecto |
|------|----------------------|---------------------|
| **Gradle** | `build.gradle`, `build.gradle.kts` | build, run, test, clean |
| **Maven** | `pom.xml` | package, exec:java, test, clean |
| **Node.js** | `package.json` | build, start, test |
| **.NET** | `*.csproj`, `*.fsproj` | build, run, test |
| **Python** | `requirements.txt` | (manual) |

---

## 🔧 Variables de Entorno

### ¿Para Qué Sirven?

Las variables de entorno permiten configurar ajustes específicos para cada proyecto que se aplican automáticamente al ejecutar comandos.

### Casos de Uso Comunes

**API con puerto configurable:**
```bash
pm add mi-api --path ~/mi-api --env "PORT=8080,HOST=localhost"
pm run mi-api  # Usa automáticamente PORT=8080
```

**Proyecto con claves API:**
```bash
pm add backend --path ~/backend --env "API_KEY=abc123,DB_HOST=localhost,DEBUG=true"
pm run backend  # Todas las variables están disponibles para el proceso
```

**Proyecto Java con opciones JVM:**
```bash
pm add proyecto-grande --path ~/proyecto-grande --env "MAVEN_OPTS=-Xmx4G,-XX:+UseG1GC"
pm build proyecto-grande  # Usa las opciones JVM configuradas
```

### Cómo Funciona

1. **Registra el proyecto con variables:**
```bash
   pm add miproyecto --path /ruta/al/proyecto --env "VAR1=valor1,VAR2=valor2"
```

2. **Las variables se guardan** en la configuración (`.projectmanager/projects.json`)

3. **Se usan automáticamente** al ejecutar:
   - `pm build miproyecto`
   - `pm run miproyecto`
   - `pm test miproyecto`

4. **Ver variables configuradas:**
```bash
   pm info miproyecto
```

---

## 📁 Estructura del Proyecto
```
ProjectManager/
├── src/main/java/pm/
│   ├── ProjectManager.java       # Clase principal
│   ├── core/                     # Modelos (Project, CommandInfo)
│   ├── cli/                      # Interfaz CLI
│   ├── detector/                 # Detección de tipo
│   ├── executor/                 # Ejecución de comandos
│   ├── storage/                  # Persistencia JSON
│   └── util/                     # Utilidades (Git, Adapters)
├── scripts/
│   ├── install.ps1               # Instalador Windows
│   ├── install.sh                # Instalador Linux/Mac
│   └── INSTALL.md                # Documentación
├── docs/
│   └── USER_GUIDE.md             # Guía de usuario completa
└── pom.xml
```

---

## 🛠️ Configuración

Los proyectos se guardan en:
- **Windows:** `C:\Users\Usuario\.projectmanager\projects.json`
- **Linux/Mac:** `~/.projectmanager/projects.json`

---

## 🌿 Integración Git

ProjectManager muestra automáticamente información de Git cuando usas `pm info`:

- **Branch actual** - saber en qué rama estás trabajando
- **Estado del working tree** - archivos modificados, staged, sin seguimiento
- **Commits pendientes** - cuántos commits no has pusheado

**Solo se muestra si el proyecto es un repositorio Git.**

---

## 🚧 Roadmap

### ✅ Completado
- [x] Sistema de registro de proyectos
- [x] Detección automática de tipo
- [x] Comandos: add, list, build, run, test, info, remove
- [x] Persistencia en JSON
- [x] Instaladores multi-plataforma
- [x] Guía de usuario completa
- [x] Integración con Git (branch, status, commits pendientes)
- [x] GitHub Actions (CI/CD)
- [x] Variables de entorno por proyecto

### 🔨 En Desarrollo
- [ ] Comando `scan` para detectar anotaciones @Command
- [ ] Alias de comandos personalizados
- [ ] Hooks pre/post comandos
- [ ] Tests unitarios

---

## 🤝 Contribuir

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama (`git checkout -b feature/nueva-funcionalidad`)
3. Commit tus cambios (`git commit -m 'feat: agregar nueva funcionalidad'`)
4. Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver archivo `LICENSE` para más detalles.

---

## 👤 Autor

**SoftDryzz**

- GitHub: [@SoftDryzz](https://github.com/SoftDryzz)

---


**⭐ Si te gusta este proyecto, dale una estrella en GitHub!**
