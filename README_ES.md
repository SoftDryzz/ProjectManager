# ProjectManager 🛠️

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-In%20Development-yellow.svg)]()

**ProjectManager** es una herramienta CLI para gestionar múltiples proyectos de desarrollo desde un solo lugar. Detecta automáticamente el tipo de proyecto, unifica los comandos de build/ejecución/testing, muestra información de Git, y soporta variables de entorno por proyecto.

> ⚠️ **En Desarrollo Activo:** Este proyecto está en construcción y muchas funcionalidades están siendo implementadas.

---

## ✨ Características

- 🔍 **Detección automática** de tipo de proyecto (Gradle, Maven, Node.js, .NET, Python)
- 🎯 **Comandos unificados** - usa `pm build` sin importar si es Maven o Gradle
- 📦 **Gestión centralizada** - todos tus proyectos en un solo lugar
- ⚡ **Ejecución rápida** de builds, tests y comandos personalizados
- 💾 **Persistencia** - configuración guardada en JSON
- 🌿 **Integración Git** - ve branch, status y commits pendientes
- 🔧 **Variables de entorno** - configura variables por proyecto
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
| `pm add <name> --path <path> [--env <vars>]` | Registrar un nuevo proyecto con variables de entorno opcionales |
| `pm list` | Listar todos los proyectos |
| `pm build <name>` | Compilar un proyecto |
| `pm run <name>` | Ejecutar un proyecto |
| `pm test <name>` | Ejecutar tests |
| `pm commands <name>` | Ver comandos disponibles |
| `pm info <name>` | Ver información detallada (incluye Git y variables) |
| `pm remove <name>` | Eliminar proyecto |
| `pm scan <name>` | 🚧 Escanear comandos (en desarrollo) |
| `pm help` | Mostrar ayuda |
| `pm version` | Mostrar versión |

### Ejemplos
```bash
# Registrar un proyecto (detección automática)
pm add minecraft-client --path ~/projects/minecraft-client

# Registrar con variables de entorno
pm add web-api --path ~/web-api --env "PORT=8080,DEBUG=true,web-api_KEY=secret"

# Listar proyectos registrados
pm list

# Compilar proyecto (usa variables de entorno automáticamente)
pm build web-api

# Ver información completa con Git y variables
pm info web-api
```

**Ejemplo de salida con Git y Variables:**
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

## 🗂️ Tipos de Proyecto Soportados

| Tipo | Archivos de Detección | Comandos por Defecto |
|------|----------------------|---------------------|
| **Gradle** | `build.gradle`, `build.gradle.kts` | build, run, test, clean |
| **Maven** | `pom.xml` | package, exec:java, test, clean |
| **Node.js** | `package.json` | build, start, test |
| **.NET** | `*.csproj`, `*.fsproj` | build, run, test |
| **Python** | `requirements.txt` | (manual) |

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

- **Branch actual** - sabe en qué rama estás trabajando
- **Working tree status** - archivos modificados, staged, sin seguimiento
- **Commits pendientes** - cuántos commits no has pusheado

**Solo se muestra si el proyecto es un repositorio Git.**

---

## 🔧 Variables de Entorno

Configura variables de entorno específicas por proyecto que se inyectan automáticamente al ejecutar comandos:
```bash
# Registrar con variables
pm add backend --path ~/backend --env "PORT=3000,NODE_ENV=development,DB_HOST=localhost"

# Las variables se usan automáticamente
pm run backend  # Se ejecuta con PORT, NODE_ENV y DB_HOST configurados
```

**Beneficios:**
- No recordar qué variables necesita cada proyecto
- Diferentes configuraciones para diferentes proyectos
- Inyección automática en build/run/test

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
- [x] Variables de entorno por proyecto
- [x] GitHub Actions (CI/CD)

### 🔨 En Desarrollo
- [ ] Comando `scan` para detectar anotaciones @Command
- [ ] Alias de comandos personalizados
- [ ] Hooks pre/post comandos
- [ ] Tests unitarios
- [ ] Soporte para templates de proyectos

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

## 🙏 Agradecimientos

- Anthropic Claude por asistencia en desarrollo
- Comunidad de Java y Maven

---

**⭐ Si te gusta este proyecto, dale una estrella en GitHub!**
