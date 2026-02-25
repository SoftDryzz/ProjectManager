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

## v1.3.8 — Ejecución Segura de Comandos

### Seguridad de rutas en comandos shell
Corregir que comandos fallen cuando las rutas del proyecto contienen espacios o caracteres especiales. También previene posible inyección de shell desde rutas crafteadas.

**Escenarios afectados:**
- `C:\Users\Mi Usuario\proyectos` — espacios en rutas de Windows
- `/home/user/mi&proyecto` — `&` interpretado como operador shell
- Rutas con `(`, `)`, `|`, `;` — rompen el parsing del shell

| Funcionalidad | Estado |
|---------------|--------|
| Escapar/entrecomillar correctamente rutas pasadas a comandos shell | Planificado |
| Validar que el directorio de trabajo existe antes de ejecutar | Planificado |
| Prevenir inyección de metacaracteres shell desde rutas de proyecto | Planificado |
| Mensaje de error claro cuando el directorio del proyecto falta o fue movido | Planificado |

---

## v1.3.9 — Auto-Update Robusto

### Integridad de descarga y resiliencia de red
Asegurar que el auto-updater maneje casos límite con gracia: descargas parciales, loops de redirección y fallos de red con feedback claro.

| Funcionalidad | Estado |
|---------------|--------|
| Validar integridad del JAR descargado (tamaño esperado desde la respuesta del API) | Planificado |
| Detectar y cortar loops de redirección (máximo 5 redirecciones) | Planificado |
| Distinguir errores de red: timeout vs fallo DNS vs firewall | Planificado |
| Mensaje claro cuando no hay conexión: "Sin conexión a internet — verificación de actualización omitida" | Planificado |
| Prevenir instalación de JAR parcial/corrupto | Planificado |

---

## v1.4.0 — Soporte Docker

### Detección de proyectos Docker
- Detectar `Dockerfile`, `docker-compose.yml`, `docker-compose.yaml` en la raíz del proyecto
- Nuevo tipo de proyecto: `DOCKER`
- Comandos por defecto:
  - `build` → `docker compose build`
  - `run` → `docker compose up`
  - `stop` → `docker compose down`
  - `clean` → `docker compose down -v --rmi all`

---

## v1.4.1 — Orquestación de Servicios

### Servicios Docker Compose
- Detectar Docker Compose multi-servicio y listar servicios
- `pm services` — mostrar servicios en ejecución/detenidos
- `pm run <servicio>` — iniciar servicios individuales
- `pm logs <servicio>` — ver logs de un servicio específico
- Integración con health checks (endpoints `/health`, `/ready`)

---

## v1.5.0 — Autocompletado en Shell

### Comando `pm completions`
- Generar scripts de autocompletado para bash, zsh, fish y PowerShell
- `pm completions <shell>` — generar script de autocompletado para el shell especificado
- Autocompletar nombres de proyecto, comandos y flags
- Sin dependencias — genera scripts de shell estáticos

---

## v1.5.1 — Puntuación de Salud del Doctor

### `pm doctor` ampliado
- Puntuación de salud: calificación **A/B/C/D/F** basada en buenas prácticas del proyecto
- Verificaciones: `.gitignore` existe, README presente, tests configurados, CI detectado, dependencias actualizadas
- Recomendaciones accionables por verificación
- `pm doctor` — mostrar reporte completo con puntuación
- `pm doctor --score` — mostrar solo la calificación

---

## v1.5.2 — Escaneo de Seguridad

### Comando `pm secure`
- Escaneo de buenas prácticas de seguridad (solo patrones del sistema de archivos, sin gestión de secretos)
- Verificaciones:
  - Dockerfile ejecuta como usuario no-root
  - Archivos `.env` están en `.gitignore`
  - No hay URLs `http://` hardcodeadas en archivos de configuración (deberían ser `https://`)
  - Archivos sensibles (`.pem`, `.key`) están en `.gitignore`
  - Existe lockfile de dependencias
- `pm secure` — ejecutar todas las verificaciones y mostrar reporte
- `pm secure --fix` — auto-corregir lo que se pueda (ej. añadir entradas a `.gitignore`)

---

## v1.5.3 — Auditoría de Dependencias

### Comando `pm audit`
- Verificar vulnerabilidades conocidas usando herramientas nativas del ecosistema:
  - npm: `npm audit`
  - Cargo: `cargo audit`
  - Go: `govulncheck`
  - Python: `pip-audit`
  - Maven: OWASP dependency-check
- Mostrar resumen con niveles de severidad
- **Sugerir** correcciones cuando estén disponibles — nunca auto-actualizar dependencias
- El desarrollador decide si actualizar; PM solo informa

> **Importante:** `pm audit` es solo lectura. Reporta vulnerabilidades y sugiere qué *podría* actualizarse, pero nunca modifica `package.json`, `Cargo.toml`, ni ningún archivo de dependencias. El desarrollador puede estar usando versiones específicas intencionalmente.

---

## v1.5.4 — Exportar e Importar

### Comandos `pm export` / `pm import`
- `pm export` — exportar todos los proyectos registrados a un archivo JSON portable
- `pm import <archivo>` — importar proyectos desde un archivo exportado
- Útil para migrar entre máquinas o compartir configuraciones de equipo
- Valida rutas al importar y avisa sobre directorios faltantes

---

## v1.6.0 — Detección de CI/CD

### Conciencia de CI/CD
- Detectar GitHub Actions (`.github/workflows/`)
- Detectar GitLab CI (`.gitlab-ci.yml`)
- Detectar Jenkins (`Jenkinsfile`)
- Mostrar estado de CI en `pm info`
- `pm ci` — abrir dashboard de CI en el navegador o mostrar estado de la última ejecución

### Conciencia de despliegue
- Detectar configs de despliegue: `fly.toml`, `vercel.json`, `netlify.toml`, `railway.json`
- `pm deploy` — lanzar despliegue a la plataforma detectada
- `pm deploy status` — mostrar estado del despliegue

---

## v1.6.1 — Linting y Formateo

### Comandos `pm lint` / `pm fmt`
- Detectar linters por tipo de proyecto:
  - Rust: `cargo fmt`, `cargo clippy`
  - Go: `gofmt`, `golangci-lint`
  - Node.js: `eslint`, `prettier`
  - Python: `ruff`, `black`, `flake8`
  - Java: `checkstyle`, `spotless`
- `pm lint` — ejecutar linter detectado
- `pm fmt` — ejecutar formateador detectado

---

## v1.6.2 — Generación de Código y Flujos de Equipo

### Detección de generación de código
- Detectar build_runner (Flutter/Dart), protobuf, generadores OpenAPI
- `pm codegen` — ejecutar herramientas de generación de código detectadas
- Avisar si los archivos generados están desactualizados

### Soporte de flujos de equipo
- Detectar número de colaboradores desde el log de git
- `pm team` — mostrar colaboradores activos y sus áreas recientes
- Detección y validación de convenciones de nombres de rama
- Detección de templates de PR

---

## v1.7.0 — Espacios de Trabajo Multi-proyecto

### Soporte monorepo
- Detectar estructuras monorepo:
  - Cargo workspaces (`[workspace]` en Cargo.toml)
  - npm/pnpm/yarn workspaces
  - Go repos multi-módulo
  - Gradle builds multi-proyecto
- `pm list-modules` — mostrar todos los sub-proyectos
- `pm build --all` — compilar todos los módulos
- `pm test --all` — testear todos los módulos
- `pm run <módulo>` — ejecutar módulo específico

### Detección de proyectos multi-lenguaje
- Detectar proyectos que usan múltiples lenguajes (ej. backend Rust + frontend Flutter)
- Mostrar todos los tipos detectados: `pm info` → "Types: RUST, FLUTTER, DOCKER"
- Ejecutar comandos por componente: `pm build backend`, `pm test mobile`

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
