# ProjectManager 🛠️

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Status-In%20Development-yellow.svg)]()

**ProjectManager** es una herramienta CLI para gestionar múltiples proyectos de desarrollo desde un solo lugar. Detecta automáticamente el tipo de proyecto y unifica los comandos de build, ejecución y testing.

> ⚠️ **En Desarrollo Activo:** Este proyecto está en construcción y muchas funcionalidades están siendo implementadas.

---

## ✨ Características

- 🔍 **Detección automática** de tipo de proyecto (Gradle, Maven, Node.js, .NET, Python)
- 🎯 **Comandos unificados** - usa `pm build` sin importar si es Maven o Gradle
- 📦 **Gestión centralizada** - todos tus proyectos en un solo lugar
- ⚡ **Ejecución rápida** de builds, tests y comandos personalizados
- 💾 **Persistencia** - configuración guardada en JSON
- 🌐 **Multi-plataforma** - Windows, Linux y Mac

---

## 📋 Requisitos

- Java 17 o superior
- Maven 3.6 o superior

---

## 🚀 Instalación Rápida
```bash
# 1. Clonar repositorio
git clone https://github.com/tu-usuario/ProjectManager.git
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
| `pm add <name> --path <path>` | Registrar un nuevo proyecto |
| `pm list` | Listar todos los proyectos |
| `pm build <name>` | Compilar un proyecto |
| `pm run <name>` | Ejecutar un proyecto |
| `pm test <name>` | Ejecutar tests |
| `pm commands <name>` | Ver comandos disponibles |
| `pm info <name>` | Ver información detallada |
| `pm remove <name>` | Eliminar proyecto |
| `pm scan <name>` | 🚧 Escanear comandos (en desarrollo) |
| `pm help` | Mostrar ayuda |
| `pm version` | Mostrar versión |

### Ejemplos
```bash
# Registrar un proyecto (detección automática)
pm add minecraft-client --path ~/projects/minecraft-client

# Listar proyectos registrados
pm list

# Compilar proyecto
pm build minecraft-client

# Ejecutar proyecto
pm run minecraft-client

# Ver comandos disponibles
pm commands minecraft-client

# Ver información completa
pm info minecraft-client
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
│   └── util/                     # Utilidades
├── scripts/
│   ├── install.ps1               # Instalador Windows
│   ├── install.sh                # Instalador Linux/Mac
│   └── INSTALL.md                # Documentación
└── pom.xml
```

---

## 🛠️ Configuración

Los proyectos se guardan en:
- **Windows:** `C:\Users\Usuario\.projectmanager\projects.json`
- **Linux/Mac:** `~/.projectmanager/projects.json`

---

## 🚧 Roadmap

### ✅ Completado
- [x] Sistema de registro de proyectos
- [x] Detección automática de tipo
- [x] Comandos: add, list, build, run, test, info, remove
- [x] Persistencia en JSON
- [x] Instaladores multi-plataforma

### 🔨 En Desarrollo
- [ ] Comando `scan` para detectar anotaciones @Command
- [ ] Integración con Git
- [ ] Variables de entorno por proyecto
- [ ] Hooks pre/post comandos
- [ ] Alias de comandos personalizados
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

## 🙏 Agradecimientos

- Anthropic Claude por asistencia en desarrollo
- Comunidad de Java y Maven

---

**⭐ Si te gusta este proyecto, dale una estrella en GitHub!**