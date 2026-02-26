# ProjectManager 🛠️

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://adoptium.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![Status](https://img.shields.io/badge/Estado-Activo-green.svg)]()

**Un comando para todos tus proyectos. Sin importar la tecnología.**

> Deja de perder tiempo recordando si es `gradle build`, `mvn package`, `cargo build`, `flutter build` o `npm run build`. Solo usa `pm build`.

[🇬🇧 Read in English](README.md)

---

## 📑 Tabla de Contenidos

- [¿Por Qué ProjectManager?](#-por-qué-projectmanager)
- [Ejemplo de Ganancia Rápida](#-ejemplo-de-ganancia-rápida)
- [Características](#-características)
- [Requisitos](#-requisitos)
- [Instalación](#-instalación)
- [Uso](#-uso)
- [Tipos de Proyecto Soportados](#️-tipos-de-proyecto-soportados)
- [Variables de Entorno](#-variables-de-entorno)
- [Integración Git](#-integración-git)
- [Cómo Se Compara](#-cómo-se-compara)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Configuración](#️-configuración)
- [Roadmap](#-roadmap)
- [Contribuir](#-contribuir)

---

## 🎯 ¿Por Qué ProjectManager?

### El Problema que Enfrentas Diariamente

**Eres un desarrollador con múltiples proyectos:**
```bash
# Proyecto 1 (Gradle)
cd ~/projects/api-usuarios
gradle build
# Espera... ¿era gradle o gradlew?

# Proyecto 2 (Maven)
cd ~/projects/backend
mvn clean package
# ¿O era mvn install?

# Proyecto 3 (npm)
cd ~/projects/frontend
npm run build
# Necesito PORT=3000... ¿o era 3001?

# Revisar git status en todos lados
cd ~/projects/api-usuarios && git status
cd ~/projects/backend && git status
cd ~/projects/frontend && git status
```

**Resultado:**
- ⏰ **Más de 30 minutos perdidos al día** navegando carpetas y buscando comandos
- 🧠 **Sobrecarga mental** recordando diferentes sistemas de build
- 😫 **Cambio de contexto** entre 5+ proyectos diferentes
- 🐛 **Errores** por usar comandos o configuraciones incorrectas

---

### La Forma ProjectManager

**Mismo desarrollador, mismos proyectos:**
```bash
# Desde cualquier lugar, cualquier carpeta
pm build api-usuarios
pm build backend
pm build frontend

# Ejecutar con configuración correcta automáticamente
pm run api-usuarios    # Usa PORT=3000
pm run backend         # Usa PORT=8080

# Revisar todos los repos git instantáneamente
pm info api-usuarios   # Branch: main, 2 modificados
pm info backend        # Branch: dev, ✓ limpio
pm info frontend       # Branch: feature/ui, 3 commits sin pushear
```

**Resultado:**
- ✅ **5 segundos** por comando
- ✅ **Sin pensar** requerido
- ✅ **Trabajar desde cualquier lugar**
- ✅ **Nunca olvidar** configuraciones

---

### Impacto Real

**Tiempo ahorrado por semana:**
- Búsqueda de comandos: ~2 horas
- Navegación de carpetas: ~1 hora
- Errores de configuración: ~30 min
- Revisión de git status: ~45 min

**Total: ~4 horas/semana = 16 horas/mes = 2 días laborales completos**

---

### ¿Quién Se Beneficia Más?

✅ **Desarrolladores full-stack** - Múltiples tecnologías diariamente  
✅ **Líderes de equipo** - Estandarizar comandos en el equipo  
✅ **Estudiantes** - Aprender nuevas tecnologías sin confusión de comandos  
✅ **Ingenieros DevOps** - Gestionar múltiples microservicios  
✅ **Cualquiera con 3+ proyectos** - Simplificar tu flujo de trabajo  

---

## ⚡ Ejemplo de Ganancia Rápida

### Antes de ProjectManager

**Lunes por la mañana, 3 APIs para iniciar:**
```bash
cd ~/work/servicio-usuarios
cat README.md  # Buscar instrucciones
export PORT=3001
export DB_HOST=localhost
mvn spring-boot:run

cd ~/work/servicio-productos
npm install  # Por si acaso
PORT=3002 npm start

cd ~/work/servicio-pedidos
# ¿Era Gradle o Maven?
ls  # Buscar pom.xml o build.gradle
gradle bootRun --args='--server.port=3003'
```

**Tiempo:** 10-15 minutos (si todo funciona)  
**Carga mental:** Alta  
**Riesgo de error:** Medio  

---

### Después de ProjectManager

**Lunes por la mañana, mismas 3 APIs:**
```bash
pm run servicio-usuarios
pm run servicio-productos
pm run servicio-pedidos
```

**Tiempo:** 15 segundos  
**Carga mental:** Cero  
**Riesgo de error:** Ninguno  

**Tiempo de configuración:** 5 minutos (una sola vez)  
**Tiempo ahorrado:** Todos los días  

---

## ✨ Características

- 🔍 **Detección automática** - Detecta Gradle, Maven, Node.js, .NET, Python, Rust, Go, pnpm, Bun, Yarn, Docker automáticamente
- 🎯 **Comandos unificados** - Mismos comandos para todos los proyectos: `pm build`, `pm run`, `pm test`
- 📦 **Gestión centralizada** - Todos los proyectos en un lugar, accesibles desde cualquier parte
- ⚡ **Ejecución rápida** - Sin navegación de carpetas, ejecución instantánea de comandos
- 💾 **Persistencia** - Configuración guardada en JSON, sobrevive reinicios
- 🌿 **Integración Git** - Ve branch, status y commits sin pushear en `pm info`
- 🔧 **Variables de entorno** - Configura variables por proyecto (PORT, DEBUG, API_KEY, etc)
- 🩺 **Runtime checker** - Detecta runtimes faltantes antes de ejecutar, muestra instrucciones de instalación
- 🏥 **pm doctor** - Diagnostica tu entorno: verifica herramientas instaladas y valida rutas de proyectos
- 🔄 **Auto-actualización** - Comprueba actualizaciones al arrancar, actualiza con `pm update`
- 🔃 **Refrescar proyectos** - Re-detecta tipos y actualiza comandos con `pm refresh`, avisa cuando los proyectos están desactualizados
- ✏️ **Renombrar y actualizar ruta** - Renombra proyectos o actualiza rutas con `pm rename`, preservando todos los datos
- 🎨 **Comandos personalizados** - Añade tus propios comandos con `pm commands add` (tunnel, lint, deploy, etc.)
- 🛡️ **Seguridad de datos** - Escritura atómica, backup automático y recuperación de JSON corrupto
- 🔒 **Ejecución segura** - Validación de directorio antes de ejecutar comandos, avisos de metacaracteres
- 🛡️ **Auto-update robusto** - Validación de integridad de descarga, protección contra loops de redirección, mensajes de error de red descriptivos
- 🐳 **Soporte Docker** - Detecta proyectos Docker Compose, comandos por defecto (build, up, down, clean)
- 🪝 **Hooks pre-/post-comando** - Ejecuta scripts personalizados antes o después de cualquier comando con `pm hooks`
- 🔤 **Autocompletado en shell** - Completado con TAB para bash, zsh, fish y PowerShell con `pm completions`
- 🌐 **Multi-plataforma** - Funciona en Windows, Linux y Mac

---

## 📋 Requisitos

- Java 17 o superior (recomendado: Java 21 LTS)
- Maven 3.6 o superior
- Git (opcional, para información de repositorios)

---

## 🚀 Instalación

**Inicio rápido** — descarga la última release y ejecuta el instalador:

```bash
# Windows (PowerShell)
powershell -ExecutionPolicy Bypass -File .\scripts\install.ps1

# Linux/Mac
chmod +x scripts/install.sh && ./scripts/install.sh
```

📖 **Guía de instalación completa** (paso a paso, compilar desde código fuente, solución de problemas, desinstalar): **[INSTALL.md](scripts/INSTALL.md)**

---

## 💻 Uso

### Comandos Disponibles

| Comando | Descripción |
|---------|-------------|
| `pm add <nombre> --path <ruta>` | Registrar un nuevo proyecto |
| `pm add <nombre> --path <ruta> --env "CLAVE=valor,..."` | Registrar con variables de entorno |
| `pm list` | Listar todos los proyectos |
| `pm build <nombre>` | Compilar un proyecto |
| `pm run <nombre>` | Ejecutar un proyecto |
| `pm test <nombre>` | Ejecutar tests |
| `pm commands <nombre>` | Ver comandos disponibles |
| `pm commands <nombre> add <cmd> "<línea>"` | Añadir un comando personalizado |
| `pm commands <nombre> remove <cmd>` | Eliminar un comando |
| `pm commands --all` | Ver comandos de todos los proyectos |
| `pm info <nombre>` | Ver información detallada (incluyendo estado Git) |
| `pm remove <nombre>` | Eliminar proyecto |
| `pm env set <nombre> KEY=VALUE` | Configurar variables de entorno |
| `pm env get <nombre> KEY` | Obtener valor de una variable |
| `pm env list <nombre> [--show]` | Listar variables (valores sensibles enmascarados) |
| `pm env remove <nombre> KEY` | Eliminar una variable |
| `pm env clear <nombre>` | Eliminar todas las variables |
| `pm hooks <nombre>` | Listar hooks de un proyecto |
| `pm hooks <nombre> add <slot> "<script>"` | Añadir un hook pre-/post-comando |
| `pm hooks <nombre> remove <slot> "<script>"` | Eliminar un hook |
| `pm hooks --all` | Listar hooks de todos los proyectos |
| `pm completions <shell>` | Generar script de autocompletado (bash/zsh/fish/powershell) |
| `pm rename <viejo> <nuevo>` | Renombrar un proyecto |
| `pm rename <nombre> --path <ruta>` | Actualizar ruta del proyecto |
| `pm refresh <nombre>` | Re-detectar tipo de proyecto y actualizar comandos |
| `pm refresh --all` | Refrescar todos los proyectos registrados |
| `pm update` | Actualizar a la última versión |
| `pm doctor` | Diagnosticar entorno (runtimes, rutas) |
| `pm help` | Mostrar ayuda |
| `pm version` | Mostrar versión |

### Ejemplos
```bash
# Registrar un proyecto (detección automática)
pm add mi-api --path ~/projects/mi-api

# Registrar con variables de entorno
pm add mi-api --path ~/projects/mi-api --env "PORT=8080,DEBUG=true,API_KEY=secreto"

# Listar todos los proyectos
pm list

# Compilar cualquier proyecto
pm build mi-api

# Ejecutar con variables de entorno (automático)
pm run mi-api

# Ver información del proyecto + estado Git
pm info mi-api
```

**Ejemplo de salida:**
```
Project Information
───────────────────

mi-api (Maven)
  Path: /home/user/projects/mi-api
  Modified: hace 5 minutos
  Commands: 5
  Environment Variables: 3

  Git:
    Branch: feature/nuevo-endpoint
    Status: 2 modificados, 1 sin seguimiento
    Unpushed: 3 commits

Commands for mi-api (Maven)

  Default
  build  →  mvn package
  run    →  mvn exec:java
  test   →  mvn test
  clean  →  mvn clean

  Custom
  lint   →  mvn checkstyle:check

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
| **Rust** | `Cargo.toml` | build, run, test, clean |
| **Go** | `go.mod` | build, run, test, clean |
| **Flutter** | `pubspec.yaml` | build, run, test, clean |
| **pnpm** | `pnpm-lock.yaml` | build, dev, test |
| **Bun** | `bun.lockb`, `bun.lock` | build, dev, test |
| **Yarn** | `yarn.lock` | build, start, test |
| **Node.js** | `package.json` (fallback) | build, start, test |
| **.NET** | `*.csproj`, `*.fsproj` | build, run, test |
| **Python** | `requirements.txt`, `setup.py` | (configuración manual) |
| **Docker** | `docker-compose.yml`, `docker-compose.yaml` | build, up, down, clean |

> **Prioridad de detección:** Los tipos de lenguaje siempre tienen prioridad. Cuando un proyecto tiene `pom.xml` y `docker-compose.yml`, se detecta como Maven (no Docker). Docker solo se detecta cuando no se encuentra ningún tipo de lenguaje. Para proyectos JS, los package managers específicos (pnpm, Bun, Yarn) tienen prioridad sobre Node.js genérico.

**¿No encuentras tu tecnología?** ProjectManager funciona con cualquier proyecto - solo configura comandos manualmente.

---

## 🔧 Variables de Entorno

### ¿Para Qué Sirven?

Deja de configurar variables de entorno manualmente cada vez. Configura una vez, usa para siempre.

### Casos de Uso Comunes

**API con puerto configurable:**
```bash
pm add mi-api --path ~/mi-api --env "PORT=8080,HOST=localhost"
pm run mi-api  # Usa automáticamente PORT=8080
```

**Proyecto con claves API:**
```bash
pm add backend --path ~/backend --env "API_KEY=abc123,DB_HOST=localhost,DEBUG=true"
pm run backend  # Todas las variables disponibles
```

**Proyecto Java con opciones JVM:**
```bash
pm add proyecto-grande --path ~/proyecto-grande --env "MAVEN_OPTS=-Xmx4G -XX:+UseG1GC"
pm build proyecto-grande  # Usa 4GB RAM automáticamente
```

### Gestiona Variables en Cualquier Momento

```bash
pm env set mi-api PORT=8080,DEBUG=true     # Establecer variables
pm env get mi-api PORT                     # Obtener un valor
pm env list mi-api                         # Listar (valores sensibles enmascarados)
pm env list mi-api --show                  # Listar (todos los valores revelados)
pm env remove mi-api DEBUG                 # Eliminar una variable
pm env clear mi-api                        # Eliminar todas las variables
```

### Cómo Funciona

1. **Registra una vez** con variables (o agrégalas después con `pm env set`)
2. **Variables guardadas** en configuración
3. **Inyectadas automáticamente** cuando ejecutas `pm build`, `pm run` o `pm test`
4. **Ver en cualquier momento** con `pm info` o `pm env list`

---

## 🌿 Integración Git

Conoce el estado de tu repositorio sin salir de tu carpeta actual.

**Lo que ves en `pm info`:**
- **Branch actual** - En qué rama estás trabajando
- **Estado del working tree** - Archivos modificados, staged, sin seguimiento
- **Commits sin pushear** - Cuántos commits necesitan ser pusheados

**Beneficios:**
- ✅ Revisar múltiples repos instantáneamente
- ✅ Nunca olvidar hacer commit/push
- ✅ Ver en qué branch estás sin `git status`

---

## 🔄 Cómo Se Compara

| Tarea | Sin ProjectManager | Con ProjectManager |
|-------|-------------------|-------------------|
| Compilar proyecto | `cd carpeta && gradle build` | `pm build miproyecto` |
| Ejecutar con config | `cd carpeta && PORT=8080 mvn exec:java` | `pm run miproyecto` |
| Revisar git status | `cd carpeta && git status` | `pm info miproyecto` |
| Cambiar proyectos | `cd ../otro && ...` | `pm build otro` |
| Recordar comandos | Revisar docs/README | `pm commands miproyecto` |

**vs Otras Herramientas:**
- **Make/Task runners:** Requiere configuración por proyecto, sin soporte multi-tecnología
- **Alias de shell:** Funcionalidad limitada, configuración manual por proyecto
- **IDE:** Bloqueado a un editor, sin flujo CLI
- **ProjectManager:** ✅ Universal, ✅ Portable, ✅ Configuración de 5 minutos

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
│   └── INSTALL.md                # Guía de instalación
├── docs/
│   └── es/                       # Documentación en español
│       ├── README.md
│       ├── User-Guide.md
│       ├── ROADMAP.md
│       └── SECURITY.md
├── User-Guide.md                 # Guía completa (Inglés)
├── ROADMAP.md                    # Planes futuros e ideas
├── SECURITY.md                   # Política de seguridad
├── CONTRIBUTING.md               # Guía de contribución
└── pom.xml
```

---

## 🛠️ Configuración

Los proyectos se guardan en:
- **Windows:** `C:\Users\Usuario\.projectmanager\projects.json`
- **Linux/Mac:** `~/.projectmanager/projects.json`

**Edición manual soportada** (solo usuarios avanzados)

---

## 🚧 Roadmap

### ✅ Completado
- **Core** — Registro de proyectos, auto-detección (12 tipos), comandos unificados, persistencia JSON
- **CLI** — `pm doctor`, `pm env`, `pm refresh`, `pm rename`, `pm update`, `pm commands add/remove`
- **Runtimes** — Gradle, Maven, Node.js, .NET, Python, Rust, Go, pnpm, Bun, Yarn, Flutter, Docker
- **Integraciones** — Estado Git, TTY interactivo, instaladores multi-plataforma, GitHub Actions
- **Fiabilidad** — Escritura atómica, backup/recuperación, validación de directorio, integridad de descarga, 402 tests

> Última release: **v1.6.0** (Autocompletado en Shell) — Historial completo en [ROADMAP.md](ROADMAP.md)

### 💡 Ideas Futuras
- [ ] `pm run-all` / `pm build-all` - Ejecutar comandos en todos los proyectos
- [ ] Grupos de proyectos (`pm group create backend api-users product-service`, `pm run-group backend`)
- [ ] Autocompletado de shell (tab completion para bash/zsh/PowerShell)
- [ ] Instaladores multi-ecosistema (npm, Cargo, Homebrew, Scoop, etc.)

> Ver [ROADMAP.md](ROADMAP.md) para el roadmap completo con planes detallados.

---

## 🐛 ¿Encontraste un Bug?

¡Tomamos los bugs en serio! Si encuentras un problema:

1. **Revisa issues existentes:** [Issues Abiertos](https://github.com/SoftDryzz/ProjectManager/issues)
2. **Reporta un nuevo bug:** [Crear Reporte de Bug](https://github.com/SoftDryzz/ProjectManager/issues/new/choose)

**Qué incluir en tu reporte:**
- Descripción clara del problema
- Pasos para reproducir
- Comportamiento esperado vs actual
- Salida de `pm version`
- Sistema operativo
- Mensajes de error (si los hay)

**Ejemplo:**
```
Bug: pm build falla en Windows con espacios en la ruta

Pasos:
1. pm add miproyecto --path "C:\Mis Proyectos\test"
2. pm build miproyecto
3. Error: Ruta no encontrada

Esperado: Build exitoso
Actual: Error con rutas que contienen espacios
```

---

## 💡 Solicitudes de Funcionalidades

¿Tienes una idea para mejorar ProjectManager? ¡Nos encantaría escucharla!

[Enviar Solicitud de Funcionalidad](https://github.com/SoftDryzz/ProjectManager/issues/new/choose)

---

## 🤝 Contribuir

¡Las contribuciones son bienvenidas! Por favor:

1. Haz fork del proyecto
2. Crea una rama de funcionalidad (`git checkout -b feature/funcionalidad-increible`)
3. Haz commit de tus cambios (`git commit -m 'feat: agregar funcionalidad increíble'`)
4. Haz push a la rama (`git push origin feature/funcionalidad-increible`)
5. Abre un Pull Request

Consulta [CONTRIBUTING.md](CONTRIBUTING.md) para más detalles.

---

## 📄 Licencia

Este proyecto está bajo la **GNU Affero General Public License v3.0 (AGPLv3)**. Ver [`LICENSE`](LICENSE) para detalles.

Licencias comerciales disponibles para organizaciones que requieran términos alternativos. Ver [`COMMERCIAL.md`](COMMERCIAL.md) para detalles o contactar: **legal@softdryzz.com**

---

## 👤 Autor

**SoftDryzz**

- GitHub: [@SoftDryzz](https://github.com/SoftDryzz)

---

**⭐ Si ProjectManager te ahorra tiempo, ¡dale una estrella en GitHub!**

**💬 ¿Preguntas? Abre un issue o consulta la [Guía de Usuario](User-Guide.md)**
