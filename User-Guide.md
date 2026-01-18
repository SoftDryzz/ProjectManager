# 📖 Guía de Usuario - ProjectManager
## 📑 Índice

- [¿Qué es ProjectManager?](#-qué-es-projectmanager)
- [Inicio Rápido (5 minutos)](#-inicio-rápido-5-minutos)
  - [Paso 1: Verificar Instalación](#paso-1-verificar-instalación)
  - [Paso 2: Registrar Tu Primer Proyecto](#paso-2-registrar-tu-primer-proyecto)
  - [Paso 3: Ver Tus Proyectos](#paso-3-ver-tus-proyectos)
  - [Paso 4: Compilar Tu Proyecto](#paso-4-compilar-tu-proyecto)
- [Referencia de Comandos](#-referencia-de-comandos)
  - [Gestión de Proyectos](#-gestión-de-proyectos)
  - [Ejecución de Comandos](#-ejecución-de-comandos)
  - [Ayuda y Versión](#-ayuda-y-versión)
- [Variables de Entorno](#-variables-de-entorno)
  - [¿Qué Son?](#qué-son)
  - [¿Cómo Funcionan en ProjectManager?](#cómo-funcionan-en-projectmanager)
  - [Ejemplos de Uso](#ejemplos-de-uso)
  - [Ver Variables Configuradas](#ver-variables-configuradas)
  - [Modificar Variables](#modificar-variables)
- [Integración Git](#-integración-git)
  - [¿Qué es?](#qué-es)
  - [Información que Muestra](#información-que-muestra)
  - [Ejemplo Completo](#ejemplo-completo)
  - [Casos de Uso Git Integration](#casos-de-uso-git-integration)
  - [Proyectos Sin Git](#proyectos-sin-git)
  - [Requisitos](#requisitos)
- [Casos de Uso](#-casos-de-uso)
- [Tipos de Proyecto Soportados](#-tipos-de-proyecto-soportados)
- [Configuración Avanzada](#-configuración-avanzada)
  - [Ubicación del Archivo de Configuración](#ubicación-del-archivo-de-configuración)
  - [Estructura del Archivo projects.json](#estructura-del-archivo-projectsjson)
  - [Edición Manual](#edición-manual-avanzado)
- [Preguntas Frecuentes (FAQ)](#-preguntas-frecuentes-faq)
- [Solución de Problemas](#-solución-de-problemas)
- [Cheatsheet Rápido](#-cheatsheet-rápido)
- [Flujo de Trabajo Completo](#-flujo-de-trabajo-completo)
- [Siguientes Pasos](#-siguientes-pasos)
- [Recursos Adicionales](#-recursos-adicionales)
## 🎯 ¿Qué es ProjectManager?

ProjectManager es una herramienta de línea de comandos que te permite **gestionar todos tus proyectos de desarrollo desde un solo lugar**, sin necesidad de recordar si cada proyecto usa Gradle, Maven, npm u otra herramienta de build.

---

## 🚀 Inicio Rápido (5 minutos)

### Paso 1: Verificar Instalación

Si ya ejecutaste el script de instalación, verifica que funciona:
```bash
pm version
```

Deberías ver algo como:
```
ProjectManager 1.0.0
Java 25.0.1
```

---

### Paso 2: Registrar Tu Primer Proyecto
```bash
pm add nombre-proyecto --path C:\ruta\a\tu\proyecto
```

**ProjectManager detecta automáticamente** el tipo de proyecto (Gradle, Maven, Node.js, etc.)

**Ejemplo:**
```bash
pm add minecraft-client --path C:\Users\PcVIP\projects\minecraft-client
```

**Salida esperada:**
```
╔════════════════════════════════╗
║  ProjectManager v1.0.0        ║
║  Manage your projects          ║
╚════════════════════════════════╝

ℹ️  Detecting project type...

✅ Project 'minecraft-client' registered successfully

  Name: minecraft-client
  Type: Gradle
  Path: C:\Users\PcVIP\projects\minecraft-client
  Commands: 4 configured

Use 'pm commands minecraft-client' to see available commands
```

---

### Paso 3: Ver Tus Proyectos
```bash
pm list
```

**Salida:**
```
Registered Projects (1)
───────────────────────

minecraft-client (Gradle)
  Path: C:\Users\PcVIP\projects\minecraft-client
  Modified: 2 minutes ago
  Commands: 4
```

---

### Paso 4: Compilar Tu Proyecto
```bash
pm build minecraft-client
```

ProjectManager ejecuta el comando de build apropiado (ej: `gradle build`) sin que tengas que recordarlo.

---

## 📚 Referencia de Comandos

### 🔹 Gestión de Proyectos

#### Registrar un proyecto (detección automática)
```bash
pm add <nombre> --path <ruta>
```

**Ejemplo:**
```bash
pm add mi-api --path C:\projects\mi-api
```

---

#### Registrar un proyecto con variables de entorno
```bash
pm add <nombre> --path <ruta> --env "KEY1=value1,KEY2=value2"
```

**Ejemplo:**
```bash
pm add backend --path C:\projects\backend --env "PORT=3000,DEBUG=true,DB_HOST=localhost"
```

**Las variables se configuran una sola vez y se usan automáticamente** en todos los comandos (build, run, test).

---

#### Registrar un proyecto (especificando tipo)
```bash
pm add <nombre> --path <ruta> --type <tipo>
```

**Tipos válidos:** `GRADLE`, `MAVEN`, `NODEJS`, `DOTNET`, `PYTHON`

**Ejemplo:**
```bash
pm add mi-app --path C:\projects\app --type MAVEN
```

---

#### Listar todos los proyectos
```bash
pm list
```

o
```bash
pm ls
```

---

#### Ver información detallada de un proyecto
```bash
pm info <nombre>
```

**Ejemplo:**
```bash
pm info minecraft-client
```

**Muestra:**
- Nombre del proyecto
- Tipo (Gradle, Maven, etc.)
- Ruta completa
- Última modificación
- Comandos disponibles
- Variables de entorno configuradas
- Estado de Git (si es repositorio)

---

#### Ver comandos disponibles para un proyecto
```bash
pm commands <nombre>
```

o
```bash
pm cmd <nombre>
```

**Ejemplo:**
```bash
pm commands minecraft-client
```

**Salida:**
```
Available Commands for minecraft-client
────────────────────────────────────────

  build  →  gradle build
  run    →  gradle runClient
  test   →  gradle test
  clean  →  gradle clean
```

---

#### Eliminar un proyecto

**Con confirmación:**
```bash
pm remove <nombre>
```

**Sin confirmación:**
```bash
pm remove <nombre> --force
```

o
```bash
pm rm <nombre> --force
```

---

### 🔹 Ejecución de Comandos

#### Compilar un proyecto
```bash
pm build <nombre>
```

**Ejemplo:**
```bash
pm build minecraft-client
```

Ejecuta el comando de build configurado (ej: `gradle build`, `mvn package`, `npm run build`) **con las variables de entorno automáticamente**.

---

#### Ejecutar un proyecto
```bash
pm run <nombre>
```

**Ejemplo:**
```bash
pm run minecraft-client
```

Ejecuta el comando de ejecución configurado (ej: `gradle run`, `mvn exec:java`, `npm start`) **con las variables de entorno automáticamente**.

---

#### Ejecutar tests
```bash
pm test <nombre>
```

**Ejemplo:**
```bash
pm test mi-api
```

Ejecuta los tests del proyecto (ej: `gradle test`, `mvn test`, `npm test`) **con las variables de entorno automáticamente**.

---

### 🔹 Ayuda y Versión

#### Ver ayuda
```bash
pm help
```

o
```bash
pm --help
pm -h
```

---

#### Ver versión
```bash
pm version
```

o
```bash
pm --version
pm -v
```

---

## 🔧 Variables de Entorno

### ¿Qué Son?

Las variables de entorno son configuraciones que tu aplicación necesita para ejecutarse, como puertos, claves de API, URLs de bases de datos, etc.

---

### ¿Cómo Funcionan en ProjectManager?

1. **Registras el proyecto con variables:**
```bash
   pm add api --path ~/api --env "PORT=8080,DEBUG=true"
```

2. **Las variables se guardan en la configuración del proyecto**

3. **Se inyectan automáticamente** cuando ejecutas `pm build`, `pm run` o `pm test`

---

### Ejemplos de Uso

#### Proyecto con Puerto Configurable
```bash
# Registrar con puerto
pm add web-server --path ~/server --env "PORT=3000"

# Ejecutar (usa PORT=3000 automáticamente)
pm run web-server
```

---

#### Proyecto con Múltiples Variables
```bash
# API con varias configuraciones
pm add backend --path ~/backend --env "PORT=8080,DB_HOST=localhost,DB_USER=admin,API_KEY=secret123"

# Compilar (variables disponibles en tiempo de compilación)
pm build backend

# Ejecutar (variables disponibles en tiempo de ejecución)
pm run backend
```

---

#### Maven con Configuración de Memoria
```bash
# Configurar memoria para Maven
pm add large-project --path ~/project --env "MAVEN_OPTS=-Xms512m -Xmx2048m"

# Maven usará esa configuración
pm build large-project
```

---

### Ver Variables Configuradas
```bash
pm info nombre-proyecto
```

**Muestra:**
```
Environment Variables
─────────────────────

  PORT    = 8080
  DEBUG   = true
  API_KEY = secret123
```

---

### Modificar Variables

**Por ahora:** Editar manualmente el archivo `projects.json`

**Ubicación:**
- Windows: `C:\Users\Usuario\.projectmanager\projects.json`
- Linux/Mac: `~/.projectmanager/projects.json`

🚧 **Feature planeada:** `pm env add/remove/update` para gestionar variables desde CLI.

---

## 🌿 Integración Git

### ¿Qué es?

ProjectManager detecta automáticamente si tu proyecto es un repositorio Git y muestra información útil cuando ejecutas `pm info`.

---

### Información que Muestra

#### 1. Branch Actual
```bash
pm info myproject
```

**Muestra:**
```
Git:
  Branch: feature/new-feature
```

**Útil para:** Saber en qué rama estás sin hacer `git branch`

---

#### 2. Estado del Working Tree

**Posibles estados:**

**Working tree limpio:**
```
Git:
  Status: ✓ Clean working tree
```

**Con cambios:**
```
Git:
  Status: 3 staged, 2 modified, 1 untracked
```

**Significado:**
- **staged:** Archivos agregados con `git add` (listos para commit)
- **modified:** Archivos modificados pero NO agregados todavía
- **untracked:** Archivos nuevos que Git no rastrea

---

#### 3. Commits Pendientes de Push

**Sin commits pendientes:**
```
Git:
  Unpushed: ✓ Up to date
```

**Con commits pendientes:**
```
Git:
  Unpushed: 3 commits
```

**Útil para:** Recordar hacer push antes de cerrar la PC

---

### Ejemplo Completo
```bash
pm info minecraft-client
```

**Salida:**
```
╔════════════════════════════════╗
║  ProjectManager v1.0.0         ║
║  Manage your projects          ║
╚════════════════════════════════╝


Project Information
───────────────────

minecraft-client (Gradle)
  Path: C:\projects\minecraft-client
  Modified: 2 hours ago
  Commands: 4
  Environment Variables: 2

  Git:
    Branch: feature/new-commands
    Status: 2 modified, 1 untracked
    Unpushed: 3 commits


Available Commands for minecraft-client
────────────────────────────────────────

  build  →  gradle build
  run    →  gradle runClient
  test   →  gradle test
  clean  →  gradle clean

Environment Variables
─────────────────────

  DEBUG      = true
  GAME_MODE  = creative
```

---

### Casos de Uso Git Integration

#### Caso 1: Verificar Branch Antes de Trabajar
```bash
# ¿En qué branch estoy?
pm info myproject

# Git:
#   Branch: master  ← ¡Cuidado! Estás en master
```

**Evita:** Hacer cambios en la rama equivocada

---

#### Caso 2: Recordar Hacer Commit
```bash
pm info myproject

# Git:
#   Status: 5 modified  ← Tienes cambios sin commitear
```

**Recuerda:** Hacer commit antes de cerrar sesión

---

#### Caso 3: Recordar Hacer Push
```bash
pm info myproject

# Git:
#   Unpushed: 7 commits  ← ¡Tienes trabajo sin subir!
```

**Evita:** Perder trabajo si se daña tu PC

---

### Proyectos Sin Git

Si un proyecto **no es un repositorio Git**, simplemente no se muestra la sección Git:
```
Project Information
───────────────────

myproject (Maven)
  Path: C:\projects\myproject
  Modified: 1 day ago
  Commands: 4

Available Commands for myproject
  build  →  mvn package
  ...
```

---

### Requisitos

- **Git instalado** en tu sistema
- **Proyecto debe ser un repositorio Git** (tener carpeta `.git`)

**Verificar que Git está instalado:**
```bash
git --version
```

Si no está instalado: https://git-scm.com/downloads

---

## 💡 Casos de Uso

### Caso 1: Múltiples Proyectos con Diferentes Tecnologías

**Problema:** Tienes 5 proyectos, cada uno con diferente build system.

**Sin ProjectManager:**
```bash
# Proyecto 1 (Gradle)
cd C:\projects\proyecto1
gradle build

# Proyecto 2 (Maven)
cd C:\projects\proyecto2
mvn package

# Proyecto 3 (npm)
cd C:\projects\proyecto3
npm run build
```

**Con ProjectManager:**
```bash
pm build proyecto1
pm build proyecto2
pm build proyecto3
```

✅ **Mismo comando para todos, sin cambiar de carpeta**

---

### Caso 2: Olvidaste los Comandos de un Proyecto

**Problema:** No recuerdas si un proyecto usa `gradle run` o `gradle runClient`.

**Solución:**
```bash
pm commands proyecto1
```

Te muestra todos los comandos disponibles.

---

### Caso 3: Trabajo en Equipo

**Problema:** Cada developer usa comandos diferentes.

**Solución:** Todo el equipo registra los proyectos con ProjectManager:
```bash
pm build api
pm test api
pm run frontend
```

✅ **Comandos consistentes para todo el equipo**

---

### Caso 4: Diferentes Configuraciones por Proyecto

**Problema:** Tienes 3 APIs con diferentes puertos y necesitas recordar cuál usa cuál.

**Con ProjectManager:**
```bash
# Registrar cada una con su puerto
pm add api-users --path ~/api-users --env "PORT=3000"
pm add api-products --path ~/api-products --env "PORT=3001"
pm add api-orders --path ~/api-orders --env "PORT=3002"

# Ejecutar cualquiera (usa su puerto automáticamente)
pm run api-users     # Puerto 3000
pm run api-products  # Puerto 3001
pm run api-orders    # Puerto 3002
```

✅ **No recordar configuraciones, todo automático**

---

## 🗂️ Tipos de Proyecto Soportados

| Tipo | Archivos de Detección | Comandos Configurados |
|------|----------------------|---------------------|
| **Gradle** | `build.gradle`, `build.gradle.kts` | `build`, `run`, `test`, `clean` |
| **Maven** | `pom.xml` | `build` (package), `run` (exec:java), `test`, `clean` |
| **Node.js** | `package.json` | `build`, `run` (start), `test` |
| **.NET** | `*.csproj`, `*.fsproj` | `build`, `run`, `test` |
| **Python** | `requirements.txt` | (configuración manual) |

---

## 🛠️ Configuración Avanzada

### Ubicación del Archivo de Configuración

ProjectManager guarda la información de tus proyectos en:

- **Windows:** `C:\Users\TuUsuario\.projectmanager\projects.json`
- **Linux/Mac:** `~/.projectmanager/projects.json`

### Estructura del Archivo `projects.json`
```json
{
  "minecraft-client": {
    "name": "minecraft-client",
    "path": "C:\\Users\\PcVIP\\projects\\minecraft-client",
    "type": "GRADLE",
    "commands": {
      "build": "gradle build",
      "run": "gradle runClient",
      "test": "gradle test",
      "clean": "gradle clean"
    },
    "envVars": {
      "DEBUG": "true",
      "GAME_MODE": "creative"
    },
    "lastModified": "2025-01-18T15:30:00Z"
  }
}
```

### Edición Manual (Avanzado)

⚠️ **No recomendado para usuarios normales**

Si necesitas modificar comandos o variables manualmente:

1. Abre el archivo `projects.json`
2. Modifica el campo `commands` o `envVars`
3. Guarda el archivo

**Ejemplo - Agregar variable de entorno:**
```json
"envVars": {
  "DEBUG": "true",
  "PORT": "8080",
  "NEW_VAR": "new_value"  ← Agregado
}
```

---

## ❓ Preguntas Frecuentes (FAQ)

### ¿Dónde se guardan mis proyectos?

En un archivo JSON ubicado en:
- Windows: `C:\Users\TuUsuario\.projectmanager\projects.json`
- Linux/Mac: `~/.projectmanager/projects.json`

### ¿Puedo editar el archivo JSON directamente?

Sí, pero **no es recomendado**. Es mejor usar los comandos de `pm` para evitar errores de sintaxis.

### ¿Las variables de entorno son seguras?

Las variables se guardan en **texto plano** en el archivo JSON. **No guardes claves secretas o contraseñas** en producción. Para desarrollo local está bien.

### ¿Qué pasa si muevo un proyecto a otra carpeta?

Debes actualizar la ruta:
```bash
pm remove proyecto-viejo
pm add proyecto-viejo --path C:\nueva\ruta --env "VAR1=value1"
```

### ¿Puedo cambiar los comandos por defecto?

Por ahora, solo editando manualmente el archivo `projects.json`.

🚧 **Feature planeada:** comando `pm config` para modificar comandos desde CLI.

### ¿Funciona con cualquier tipo de proyecto?

ProjectManager detecta automáticamente:
- Java (Gradle, Maven)
- JavaScript/TypeScript (npm)
- C# (.NET)
- Python (básico)

Para otros tipos, usa `--type UNKNOWN` y configura comandos manualmente.

### ¿Cómo desinstalo ProjectManager?

**Windows:**
```powershell
Remove-Item $env:USERPROFILE\bin\pm.bat
Remove-Item $env:USERPROFILE\.projectmanager -Recurse
```

**Linux/Mac:**
```bash
rm ~/bin/pm
rm -rf ~/.projectmanager
```

Luego elimina `~/bin` del PATH en tu `.bashrc` o `.zshrc`.

---

## 🆘 Solución de Problemas

### Error: "pm no se reconoce como comando"

**Causa:** El alias `pm` no está en el PATH.

**Solución:**

1. Verifica que ejecutaste el script de instalación:
```powershell
   .\scripts\install.ps1
```

2. Reinicia PowerShell completamente (cierra y abre de nuevo)

3. Verifica que `C:\Users\TuUsuario\bin` está en el PATH:
```powershell
   echo $env:Path
```

4. Si no está, vuelve a ejecutar el script de instalación

---

### Error: "Project not found"

**Causa:** El nombre del proyecto no está registrado o está mal escrito.

**Solución:**

1. Lista todos los proyectos registrados:
```bash
   pm list
```

2. Verifica que el nombre sea exacto (case-sensitive)

3. Si no aparece, regístralo:
```bash
   pm add nombre-proyecto --path C:\ruta
```

---

### Error: "No 'build' command configured for this project"

**Causa:** El proyecto no tiene un comando `build` configurado.

**Solución:**

1. Ver qué comandos están disponibles:
```bash
   pm commands nombre-proyecto
```

2. Usa un comando disponible (ej: `run`, `test`)

3. Si el proyecto no tiene comandos, fue detectado como tipo UNKNOWN. Regístralo especificando el tipo:
```bash
   pm remove nombre-proyecto
   pm add nombre-proyecto --path C:\ruta --type GRADLE
```

---

### Error: "Path does not exist"

**Causa:** La ruta especificada no existe o está mal escrita.

**Solución:**

1. Verifica que la ruta existe:
```powershell
   dir C:\ruta\al\proyecto
```

2. Usa la ruta completa (no relativa):
```bash
   # ❌ Mal
   pm add proyecto --path .\mi-proyecto

   # ✅ Bien
   pm add proyecto --path C:\Users\PcVIP\projects\mi-proyecto
```

3. Si usas `~`, usa la ruta completa en Windows:
```bash
   # ❌ En Windows no funciona bien
   pm add proyecto --path ~/projects/proyecto

   # ✅ Usa esto
   pm add proyecto --path C:\Users\PcVIP\projects\proyecto
```

---

### Error: "java no se reconoce como comando"

**Causa:** Java no está instalado o no está en el PATH.

**Solución:**

1. Verifica que Java está instalado:
```bash
   java -version
```

2. Si no está instalado, descarga desde: https://adoptium.net/

3. Asegúrate de marcar "Add to PATH" durante la instalación

4. Reinicia PowerShell después de instalar

---

### Las variables de entorno no se están usando

**Causa:** Puede que el comando no esté usando el método correcto.

**Verificación:**

1. Confirma que las variables están configuradas:
```bash
   pm info nombre-proyecto
```

2. Las variables deberían aparecer en la sección "Environment Variables"

3. Si no aparecen, registra el proyecto de nuevo con `--env`

---

## 📝 Cheatsheet Rápido
```bash
# === GESTIÓN ===
pm add <name> --path <path>                    # Registrar proyecto
pm add <name> --path <path> --env "K=v,K2=v2" # Registrar con variables
pm list                                        # Listar todos
pm info <name>                                 # Ver detalles completos
pm commands <name>                             # Ver comandos disponibles
pm remove <name>                               # Eliminar (con confirmación)
pm remove <name> --force                       # Eliminar (sin confirmación)

# === EJECUCIÓN ===
pm build <name>                                # Compilar (con env vars)
pm run <name>                                  # Ejecutar (con env vars)
pm test <name>                                 # Ejecutar tests (con env vars)

# === AYUDA ===
pm help                                        # Ayuda general
pm version                                     # Ver versión
```

---

## 🎬 Flujo de Trabajo Completo

### Primera Vez (Configuración Inicial)
```bash
# 1. Instalar ProjectManager
.\scripts\install.ps1

# 2. Reiniciar PowerShell

# 3. Verificar instalación
pm version

# 4. Registrar tus proyectos
pm add proyecto1 --path C:\projects\proyecto1
pm add proyecto2 --path C:\projects\proyecto2 --env "PORT=8080"
pm add proyecto3 --path C:\projects\proyecto3 --env "DEBUG=true,API_URL=localhost"

# 5. Verificar que se registraron
pm list
```

---

### Uso Diario
```bash
# Ver todos los proyectos
pm list

# Compilar un proyecto
pm build proyecto1

# Ejecutar un proyecto (usa variables automáticamente)
pm run proyecto2

# Ver información de un proyecto (incluye variables y Git)
pm info proyecto1

# Ver comandos disponibles
pm commands proyecto1

# Desde cualquier carpeta, todo funciona igual
```

---

## 🚀 Siguientes Pasos

Ahora que conoces ProjectManager:

1. **Registra todos tus proyectos actuales**
```bash
   pm add proyecto1 --path C:\projects\proyecto1
   pm add proyecto2 --path C:\projects\proyecto2
```

2. **Agrega variables de entorno donde las necesites**
```bash
   pm add api --path C:\projects\api --env "PORT=3000,DEBUG=true"
```

3. **Úsalo en tu workflow diario**
```bash
   pm build proyecto1
   pm run proyecto1
```

4. **Explora la integración con Git**
```bash
   pm info proyecto1  # Ve branch, cambios y commits pendientes
```

5. **Comparte con tu equipo**
   - Todos usan los mismos comandos
   - Configuraciones consistentes con variables de entorno
   - Onboarding más rápido

---

## 📚 Recursos Adicionales

- **README principal:** [README.md](../README.md)
- **Guía de instalación:** [INSTALL.md](../scripts/INSTALL.md)
- **Código fuente:** [src/main/java/pm/](../src/main/java/pm/)

---

## 🤝 ¿Necesitas Ayuda?

Si tienes problemas o preguntas:

1. Revisa la sección de [Solución de Problemas](#-solución-de-problemas)
2. Consulta las [Preguntas Frecuentes](#-preguntas-frecuentes-faq)
3. Abre un issue en GitHub

---

**¡Feliz desarrollo con ProjectManager! 🎉**
