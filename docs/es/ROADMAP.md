# ProjectManager - Hoja de Ruta

> Ideas y funcionalidades planificadas para futuras versiones.
>
> Algunas ideas inspiradas en el análisis de [FindMatch](https://github.com/AXIOM-ZER0/FindMatch), un proyecto real multi-stack (Flutter + Rust + Docker + PostgreSQL + Redis).
>
> **Versionado:** Sigue [Semantic Versioning](https://semver.org/). Cada release corresponde a un GitHub Release con tag `vX.Y.Z` y asset `projectmanager-X.Y.Z.jar`.

---

## v1.3.2 — Refresh de Proyectos y Detección de Desactualización ✅

### Comando `pm refresh`
Re-detecta tipos de proyecto y actualiza comandos para proyectos ya registrados. Resuelve el problema donde proyectos registrados antes de añadir un nuevo tipo no tienen comandos.

| Funcionalidad | Estado |
|---------------|--------|
| `pm refresh <nombre>` — refrescar un proyecto específico | ✅ Hecho |
| `pm refresh --all` — refrescar todos los proyectos registrados | ✅ Hecho |
| Output detallado antes/después de comandos | ✅ Hecho |
| Avisos automáticos de tipo desactualizado en `build`, `run`, `test`, `commands`, `info` | ✅ Hecho |

---

## v1.3.3 — Corrección de Update y Actualización del ROADMAP ✅

| Funcionalidad | Estado |
|---------------|--------|
| Corregir mensaje post-actualización: "Run any pm command to use the new version" | ✅ Hecho |
| Reorganización del ROADMAP con nuevas funcionalidades planificadas | ✅ Hecho |

---

## v1.3.4 — Renombrar y Actualizar Ruta ✅

### Comando `pm rename`
Renombra proyectos y/o actualiza su ruta registrada sin perder comandos ni variables de entorno.

| Funcionalidad | Estado |
|---------------|--------|
| `pm rename nombre-viejo nombre-nuevo` — renombrar proyecto | ✅ Hecho |
| `pm rename nombre --path /nueva/ruta` — actualizar ruta | ✅ Hecho |
| `pm rename nombre-viejo nombre-nuevo --path /nueva/ruta` — ambos | ✅ Hecho |
| Preserva comandos, variables de entorno y tipo de proyecto | ✅ Hecho |

---

## v1.3.5 — Soporte para CLI Interactiva ✅

### `inheritIO` para procesos interactivos
Usa `ProcessBuilder.inheritIO()` para conectar stdin/stdout/stderr directamente al terminal del usuario. Auto-detecta presencia de TTY para usar modo buffered en CI/CD.

| Funcionalidad | Estado |
|---------------|--------|
| Método `executeWithInheritedIO()` en CommandExecutor | ✅ Hecho |
| Auto-detect TTY (`System.console()`) para `pm run`, `pm build`, `pm test` | ✅ Hecho |
| Preservar métricas de `ExecutionResult` (exit code, duración) | ✅ Hecho |
| Fallback graceful a modo buffered si el terminal no es TTY | ✅ Hecho |

---

## v1.3.6 — Comandos Personalizados ✅

### Gestión de comandos personalizados
Permitir a los usuarios crear, eliminar y listar comandos personalizados por proyecto. Los comandos por defecto (build, run, test, clean) se siguen auto-detectando — los comandos personalizados los extienden.

| Funcionalidad | Estado |
|---------------|--------|
| `pm commands <proyecto> add <nombre> "<comando>"` — crear un comando personalizado | ✅ Hecho |
| `pm commands <proyecto> remove <nombre>` — eliminar un comando personalizado | ✅ Hecho |
| `pm commands <proyecto>` — listar comandos de un proyecto específico (ya existe) | ✅ Hecho |
| `pm commands --all` — listar todos los comandos de todos los proyectos registrados | ✅ Hecho |
| Los comandos personalizados se persisten en `projects.json` junto a los por defecto | ✅ Hecho |
| Actualizar comandos existentes re-añadiéndolos con nuevo valor | ✅ Hecho |

---

## v1.3.7 — Manejo de Errores y Seguridad de Datos ✅

### Almacenamiento robusto de proyectos
Prevenir pérdida de datos y eliminar mensajes de error crípticos. El usuario nunca debería ver un stack trace de Java ni perder sus proyectos registrados.

| Funcionalidad | Estado |
|---------------|--------|
| Escritura atómica de archivos (escribir en archivo temporal, luego renombrar) | ✅ Hecho |
| Backup automático de `projects.json` antes de escribir | ✅ Hecho |
| Recuperación de JSON corrupto (cargar backup automáticamente) | ✅ Hecho |
| Validar campos requeridos al cargar (nombre, ruta o tipo nulos) | ✅ Hecho |
| Manejo graceful de valores `ProjectType` inválidos en JSON | ✅ Hecho |

### Mensajes de error amigables
| Funcionalidad | Estado |
|---------------|--------|
| Eliminar `e.printStackTrace()` del main — no mostrar stack traces al usuario | ✅ Hecho |
| Mensajes de error específicos: permisos, disco lleno, archivo no encontrado, JSON corrupto | ✅ Hecho |
| Orientación accionable en mensajes de error (ej. "Ejecuta `pm doctor` para diagnosticar") | ✅ Hecho |
| Feedback en operaciones Git — mostrar por qué falta la info de git en vez de ocultarla | ✅ Hecho |

---

## v1.3.8 — Ejecución Segura de Comandos ✅

### Seguridad de rutas en comandos shell
Previene que los comandos fallen cuando los directorios del proyecto no existen o fueron movidos. Avisa sobre metacaracteres shell en comandos personalizados.

| Funcionalidad | Estado |
|---------------|--------|
| Validar que el directorio de trabajo existe antes de ejecutar | ✅ Hecho |
| Mensaje de error claro cuando el directorio del proyecto falta o fue movido | ✅ Hecho |
| Validación de directorio de defensa en profundidad en CommandExecutor | ✅ Hecho |
| Aviso de metacaracteres shell en `pm commands add` | ✅ Hecho |

---

## v1.3.9 — Auto-Update Robusto ✅

### Integridad de descarga y resiliencia de red
Asegurar que el auto-updater maneje casos límite con gracia: descargas parciales, loops de redirección y fallos de red con feedback claro.

| Funcionalidad | Estado |
|---------------|--------|
| Validar integridad del JAR descargado (tamaño esperado desde la respuesta del API) | ✅ Hecho |
| Detectar y cortar loops de redirección (máximo 5 redirecciones) | ✅ Hecho |
| Distinguir errores de red: timeout vs fallo DNS vs firewall | ✅ Hecho |
| Mensaje claro cuando no hay conexión: "Sin conexión a internet — verificación de actualización omitida" | ✅ Hecho |
| Prevenir instalación de JAR parcial/corrupto | ✅ Hecho |

---

## v1.4.0 — Soporte Docker ✅

### Detección de proyectos Docker
Detecta proyectos Docker Compose y configura comandos por defecto. Los tipos de lenguaje siempre tienen prioridad — DOCKER solo se asigna cuando no se detecta ningún tipo específico de lenguaje.

| Funcionalidad | Estado |
|---------------|--------|
| Detectar `docker-compose.yml` / `docker-compose.yaml` en la raíz del proyecto | ✅ Hecho |
| Nuevo tipo de proyecto: `DOCKER` | ✅ Hecho |
| Comandos por defecto: build, run, stop, clean (docker compose) | ✅ Hecho |
| Los tipos de lenguaje tienen prioridad sobre Docker cuando ambos existen | ✅ Hecho |
| Verificación de runtime Docker (`pm doctor`, pre-ejecución) | ✅ Hecho |
| `stop` clasificado como comando por defecto en la salida de `pm commands` | ✅ Hecho |
| Separación de comandos default/custom en la salida de `pm commands` | ✅ Hecho |

---

## v1.5.0 — Hooks Pre/Post Comandos ✅

### Comando `pm hooks`
Ejecuta scripts personalizados automáticamente antes o después de cualquier comando. Los hooks son por proyecto y los configura el usuario.

| Funcionalidad | Estado |
|---------------|--------|
| `pm hooks <proyecto> add pre-<cmd> "<script>"` — añadir un pre-hook | ✅ Hecho |
| `pm hooks <proyecto> add post-<cmd> "<script>"` — añadir un post-hook | ✅ Hecho |
| `pm hooks <proyecto>` — listar todos los hooks de un proyecto | ✅ Hecho |
| `pm hooks <proyecto> remove <slot> "<script>"` — eliminar un hook por contenido exacto | ✅ Hecho |
| `pm hooks --all` — listar hooks de todos los proyectos | ✅ Hecho |
| Múltiples hooks por slot (ejecución encadenada) | ✅ Hecho |
| Fallo en pre-hook aborta el comando principal | ✅ Hecho |
| Fallo en post-hook solo muestra advertencia | ✅ Hecho |
| Los hooks se ejecutan en el directorio del proyecto | ✅ Hecho |
| Los hooks heredan las variables de entorno del proyecto | ✅ Hecho |
| Timeout fijo de 60s para scripts de hooks | ✅ Hecho |
| Ejecución genérica de comandos (`pm <cmd> <proyecto>`) con soporte de hooks | ✅ Hecho |

---

## v1.6.0 — Autocompletado en Shell ✅

### Comando `pm completions`

| Funcionalidad | Estado |
|---------------|--------|
| `pm completions bash` — generar script de autocompletado para Bash | ✅ Hecho |
| `pm completions zsh` — generar script de autocompletado para Zsh | ✅ Hecho |
| `pm completions fish` — generar script de autocompletado para Fish | ✅ Hecho |
| `pm completions powershell` — generar script de autocompletado para PowerShell | ✅ Hecho |
| Autocompletar comandos, nombres de proyecto, subcomandos y flags | ✅ Hecho |
| Completado contextual (slots de hooks, claves de env vars, tipos de proyecto) | ✅ Hecho |
| Callback oculto `--complete` omite banner/update para rendimiento | ✅ Hecho |
| Sin dependencias — genera scripts de shell estáticos | ✅ Hecho |

---

## v1.6.1 — Puntuación de Salud del Doctor ✅

### `pm doctor` ampliado

| Funcionalidad | Estado |
|---------------|--------|
| Puntuación de salud: calificación **A/B/C/D/F** basada en buenas prácticas | ✅ Hecho |
| Verificación: `.gitignore` existe en la raíz del proyecto | ✅ Hecho |
| Verificación: README presente (sin distinción de mayúsculas) | ✅ Hecho |
| Verificación: Tests configurados (comando `test` existe) | ✅ Hecho |
| Verificación: CI/CD detectado (GitHub Actions, GitLab CI, Jenkins) | ✅ Hecho |
| Verificación: Lockfile de dependencias presente (por tipo de proyecto) | ✅ Hecho |
| Recomendaciones accionables por verificación fallida | ✅ Hecho |
| `pm doctor` — reporte completo con detalles de salud por proyecto | ✅ Hecho |
| `pm doctor --score` — salida compacta solo con calificaciones | ✅ Hecho |

---

## v1.6.2 — Escaneo de Seguridad ✅

### Comando `pm secure`

| Funcionalidad | Estado |
|---------------|--------|
| Escaneo de buenas prácticas de seguridad (solo patrones del sistema de archivos) | ✅ Hecho |
| Verificación: Dockerfile ejecuta como usuario no-root | ✅ Hecho |
| Verificación: Archivos `.env` están en `.gitignore` | ✅ Hecho |
| Verificación: No hay URLs `http://` hardcodeadas en config (deberían ser `https://`) | ✅ Hecho |
| Verificación: Archivos sensibles (`.pem`, `.key`) están en `.gitignore` | ✅ Hecho |
| Verificación: Existe lockfile de dependencias | ✅ Hecho |
| `pm secure` — ejecutar todas las verificaciones y mostrar reporte | ✅ Hecho |
| `pm secure --fix` — auto-corregir problemas de `.gitignore` | ✅ Hecho |
| Auto-fix crea `.gitignore` si no existe | ✅ Hecho |

---

## v1.6.3 — Auditoría de Dependencias ✅

### Comando `pm audit`

| Característica | Estado |
|---------------|--------|
| Ejecutar herramientas de auditoría nativas y mostrar resumen unificado | ✅ Hecho |
| npm: `npm audit --json` | ✅ Hecho |
| pnpm: `pnpm audit --json` | ✅ Hecho |
| Yarn: `yarn audit --json` | ✅ Hecho |
| Cargo: `cargo audit --json` | ✅ Hecho |
| Go: `govulncheck -json ./...` | ✅ Hecho |
| Python: `pip-audit --format=json` | ✅ Hecho |
| .NET: `dotnet list package --vulnerable --format json` | ✅ Hecho |
| Maven/Gradle: mensaje informativo (recomendar plugin OWASP) | ✅ Hecho |
| Niveles de severidad unificados (CRITICAL/HIGH/MEDIUM/LOW) | ✅ Hecho |
| Manejo elegante de herramientas no instaladas con instrucciones | ✅ Hecho |
| Nuevo `captureOutput()` en CommandExecutor para captura silenciosa de JSON | ✅ Hecho |
| Solo lectura — nunca modifica archivos de dependencias | ✅ Hecho |

> **Importante:** `pm audit` es solo lectura. Reporta vulnerabilidades y sugiere qué *podría* actualizarse, pero nunca modifica archivos de dependencias.

---

## v1.6.4 — Exportar e Importar ✅

### Comandos `pm export` / `pm import`
Exporta todos o proyectos seleccionados a un archivo JSON portable e impórtalos en otra máquina.

| Característica | Estado |
|----------------|--------|
| `pm export` — exportar todos los proyectos registrados a JSON | ✅ Hecho |
| `pm export nombre1 nombre2` — exportar proyectos específicos | ✅ Hecho |
| `pm export --file <ruta>` — archivo de salida personalizado (por defecto: `pm-export.json`) | ✅ Hecho |
| `pm import <archivo>` — importar proyectos desde un archivo exportado | ✅ Hecho |
| Formato JSON auto-descriptivo con metadatos de versión | ✅ Hecho |
| Omitir proyectos existentes al importar (nunca sobrescribe) | ✅ Hecho |
| Avisar sobre rutas faltantes al importar con sugerencia `pm rename` | ✅ Hecho |
| Tipo inválido por defecto a UNKNOWN con aviso | ✅ Hecho |
| Autocompletado en shell para comandos export/import | ✅ Hecho |

---

## v1.6.5 — Detección de CI/CD ✅

### Conciencia de CI/CD

| Funcionalidad | Estado |
|---------------|--------|
| Detectar GitHub Actions (`.github/workflows/`) | ✅ Hecho |
| Detectar GitLab CI (`.gitlab-ci.yml`) | ✅ Hecho |
| Detectar Jenkins (`Jenkinsfile`) | ✅ Hecho |
| Detectar Travis CI (`.travis.yml`) | ✅ Hecho |
| Detectar CircleCI (`.circleci/config.yml`) | ✅ Hecho |
| Mostrar proveedores CI/CD en `pm info` con conteo de workflows | ✅ Hecho |
| `pm ci [nombre]` — mostrar URLs de dashboard CI para proyectos | ✅ Hecho |
| Parsear URLs remotas SSH y HTTPS de git para enlaces de dashboard | ✅ Hecho |
| Autocompletado en shell para comando `pm ci` | ✅ Hecho |

> **Nota:** La conciencia de despliegue (fly.toml, vercel.json, etc.) se difiere a una versión posterior.

---

## v1.6.6 — Linting y Formateo ✅

### Comandos `pm lint` / `pm fmt`

| Funcionalidad | Estado |
|---------------|--------|
| `pm lint [nombre]` — ejecutar linters detectados en proyecto(s) | ✅ Hecho |
| `pm fmt [nombre]` — ejecutar formateadores detectados en proyecto(s) | ✅ Hecho |
| 10 herramientas de lint: ESLint, Clippy, go vet, golangci-lint, Ruff, Flake8, dart analyze, dotnet format, Checkstyle (Maven/Gradle) | ✅ Hecho |
| 9 herramientas de formato: Prettier, cargo fmt, gofmt, Ruff Format, Black, dart format, dotnet format, Spotless (Maven/Gradle) | ✅ Hecho |
| Detección en tres niveles: integrado al toolchain, archivo de configuración, verificación de binario | ✅ Hecho |
| Ejecutar todas las herramientas detectadas en secuencia con salida en tiempo real | ✅ Hecho |
| Autocompletado en shell para comandos `pm lint` y `pm fmt` | ✅ Hecho |

---

## v1.7.0 — Espacios de Trabajo Multi-proyecto ✅

### Monorepo y detección multi-lenguaje

| Funcionalidad | Estado |
|---------------|--------|
| Detección multi-lenguaje: `detectAll()` encuentra todos los tipos en un directorio | ✅ Hecho |
| Tipos secundarios en `pm info` ("Also detected: Docker, Node.js") | ✅ Hecho |
| Tipos secundarios persistidos en `projects.json` (compatible hacia atrás) | ✅ Hecho |
| `pm build --all` — compilar todos los proyectos registrados con resumen | ✅ Hecho |
| `pm test --all` — testear todos los proyectos registrados con resumen | ✅ Hecho |
| Continuar-en-fallo: `--all` ejecuta todos, muestra resumen éxito/fallo | ✅ Hecho |
| Detección de workspaces Cargo (`[workspace]` members en Cargo.toml) | ✅ Hecho |
| Detección de workspaces npm/pnpm/yarn (array, objeto, patrones glob) | ✅ Hecho |
| Detección multi-proyecto Gradle (`include()` en settings.gradle/kts) | ✅ Hecho |
| Detección multi-módulo Go (archivos `go.mod` anidados) | ✅ Hecho |
| `pm modules [nombre]` — mostrar módulos del workspace | ✅ Hecho |
| Conteo de módulos en `pm info` | ✅ Hecho |
| Autocompletado en shell para `modules`, `build --all`, `test --all` | ✅ Hecho |

---

## v1.7.1 — Plantillas de Proyecto

### Comando `pm init`
- `pm init <tipo>` — crear un nuevo proyecto desde plantillas
- Plantillas incluidas: Java (Maven/Gradle), Node.js, Rust, Go, Python, .NET
- Soporte de plantillas personalizadas desde repos de GitHub

---

## v1.8.0 — Entornos, Secretos y Bases de Datos

### Gestión de entornos
- Detectar archivos `.env`, `.env.local`, `.env.production`
- `pm env` — mostrar variables de entorno actuales (secretos enmascarados)
- `pm env switch <nombre>` — cambiar entre archivos de entorno
- Avisar si `.env` no está en `.gitignore`

### Detección de secretos
- Escanear patrones comunes de secretos (API keys, tokens, contraseñas)
- Avisar en `pm doctor` si hay secretos comiteados
- Detección opcional de [Vaultic](https://crates.io/crates/vaultic) (no es una dependencia — PM funciona completamente sin él):
  - **No instalado** → recomendar opciones de instalación: `cargo install vaultic` (requiere [toolchain de Rust](https://rustup.rs)) o descargar binario desde [GitHub Releases](https://github.com/SoftDryzz/Vaultic/releases). Siempre enlazar al [repo de Vaultic](https://github.com/SoftDryzz/Vaultic) para docs
  - **Instalado pero no inicializado** → sugerir `vaultic init` con enlace a docs para que el usuario entienda qué hace antes de ejecutarlo
  - **Instalado y configurado** → mostrar hints avanzados de gestión de secretos (ej. `vaultic encrypt .env`)

### Conciencia de migraciones de base de datos
- Detectar herramientas de migración: SQLx, Flyway, Liquibase, Prisma, Diesel, Alembic
- `pm migrate` — ejecutar migraciones pendientes
- `pm migrate status` — mostrar estado de migraciones

---

## v2.0.0 — Dashboard y Analíticas

### Dashboard TUI interactivo
- Dashboard de estado de proyectos en tiempo real (usando una librería TUI)
- Mostrar: estado de build, resultados de tests, estado de git, dependencias
- Navegar entre proyectos registrados
- Acciones rápidas (build, test, clean) desde el dashboard

### Seguimiento de rendimiento
- Rastrear tiempos de build entre ejecuciones
- `pm stats` — mostrar tendencias de tiempo de build/test
- Identificar builds lentos y sugerir optimizaciones

---

## Futuro — Instalación Multi-ecosistema

### Scripts de instalación por ecosistema
Crear scripts/comandos dedicados para que los usuarios puedan instalar ProjectManager con su gestor de paquetes preferido:

| Ecosistema | Método de Instalación | Estado |
|------------|----------------------|--------|
| npm/npx | `npx projectmanager` o `npm i -g projectmanager` | Planificado |
| pnpm | `pnpm add -g projectmanager` | Planificado |
| Cargo | `cargo install projectmanager` | Planificado |
| Homebrew | `brew install projectmanager` | Planificado |
| Scoop (Windows) | `scoop install projectmanager` | Planificado |
| Go | `go install github.com/SoftDryzz/ProjectManager@latest` | Planificado |
| Bun | `bun add -g projectmanager` | Planificado |
| Yarn | `yarn global add projectmanager` | Planificado |

> **Nota:** Cada instalación por ecosistema envolvería el JAR (o compilaría un binario nativo via GraalVM).

---

## Contribuir

¿Tienes una idea? Abre un issue en [GitHub Issues](https://github.com/SoftDryzz/ProjectManager/issues) con la etiqueta `enhancement`.
