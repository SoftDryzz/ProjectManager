# 📖 Guía de Usuario - ProjectManager

## 📑 Tabla de Contenidos

- [¿Qué es ProjectManager?](#-qué-es-projectmanager)
- [Inicio Rápido (5 minutos)](#-inicio-rápido-5-minutos)
  - [Paso 1: Verificar Instalación](#paso-1-verificar-instalación)
  - [Paso 2: Registrar tu Primer Proyecto](#paso-2-registrar-tu-primer-proyecto)
  - [Paso 3: Ver tus Proyectos](#paso-3-ver-tus-proyectos)
  - [Paso 4: Compilar tu Proyecto](#paso-4-compilar-tu-proyecto)
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
  - [Reglas de Formato](#reglas-de-formato)
  - [Ejemplos Prácticos Completos](#ejemplos-prácticos-completos)
  - [Dónde se Guardan](#dónde-se-guardan)
  - [Preguntas Frecuentes sobre Variables](#preguntas-frecuentes-sobre-variables)
- [Integración Git](#-integración-git)
  - [¿Qué es?](#qué-es)
  - [Información que Muestra](#información-que-muestra)
  - [Ejemplo Completo](#ejemplo-completo)
  - [Casos de Uso de Integración Git](#casos-de-uso-de-integración-git)
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
- [Próximos Pasos](#-próximos-pasos)
- [Recursos Adicionales](#-recursos-adicionales)

---

## 🎯 ¿Qué es ProjectManager?

ProjectManager es una herramienta de línea de comandos que te permite **gestionar todos tus proyectos de desarrollo desde un solo lugar**, sin necesidad de recordar si cada proyecto usa Gradle, Maven, npm u otra herramienta de build.

---

## 🚀 Inicio Rápido (5 minutos)

### Paso 1: Verificar Instalación

Si ya ejecutaste el script de instalación, verifica que funcione:
```bash
pm version
```

Deberías ver algo como:
```
ProjectManager 1.0.0
Java 25.0.1
```

---

### Paso 2: Registrar tu Primer Proyecto
```bash
pm add nombre-proyecto --path C:\ruta\a\tu\proyecto
```

**ProjectManager detecta automáticamente** el tipo de proyecto (Gradle, Maven, Node.js, etc.).

**Ejemplo:**
```bash
pm add web-api --path C:\Users\Usuario\projects\web-api
```

**Salida Esperada:**
```
╔════════════════════════════════╗
║  ProjectManager v1.0.0         ║
║  Manage your projects          ║
╚════════════════════════════════╝

ℹ️  Detecting project type...

✅ Project 'web-api' registered successfully

  Name: web-api
  Type: Gradle
  Path: C:\Users\Usuario\projects\web-api
  Commands: 4 configured

Use 'pm commands web-api' to see available commands
```

---

### Paso 3: Ver tus Proyectos
```bash
pm list
```

**Salida:**
```
Registered Projects (1)
───────────────────────

web-api (Gradle)
  Path: C:\Users\Usuario\projects\web-api
  Modified: hace 2 minutos
  Commands: 4
```

---

### Paso 4: Compilar tu Proyecto
```bash
pm build web-api
```

ProjectManager ejecuta el comando de build apropiado (ej: `gradle build`) sin que tengas que recordarlo.

---

## 📚 Referencia de Comandos

### 🔹 Gestión de Proyectos

#### Registrar un proyecto (auto-detección)
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
pm add <nombre> --path <ruta> --env "CLAVE1=valor1,CLAVE2=valor2"
```

**Ejemplo:**
```bash
pm add backend --path C:\projects\backend --env "PORT=3000,DEBUG=true,DB_HOST=localhost"
```

**Las variables se configuran una vez y se usan automáticamente** en todos los comandos (build, run, test).

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
pm info web-api
```

**Muestra:**

- Nombre del Proyecto
- Tipo (Gradle, Maven, etc.)
- Ruta Completa
- Última Modificación
- Comandos Disponibles
- Variables de Entorno Configuradas
- Estado de Git (si es un repositorio)

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
pm commands web-api
```

**Salida:**
```
Available Commands for web-api
────────────────────────────────────────

  build  →  gradle build
  run    →  gradle run
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
pm build web-api
```

Ejecuta el comando de build configurado (ej: `gradle build`, `mvn package`, `npm run build`) **automáticamente con las variables de entorno**.

---

#### Ejecutar un proyecto
```bash
pm run <nombre>
```

**Ejemplo:**
```bash
pm run web-api
```

Ejecuta el comando de run configurado (ej: `gradle run`, `mvn exec:java`, `npm start`) **automáticamente con las variables de entorno**.

---

#### Ejecutar tests
```bash
pm test <nombre>
```

**Ejemplo:**
```bash
pm test mi-api
```

Ejecuta los tests del proyecto (ej: `gradle test`, `mvn test`, `npm test`) **automáticamente con las variables de entorno**.

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

Las variables de entorno son configuraciones que tu aplicación necesita para ejecutarse, como puertos, claves API, URLs de base de datos, etc.

**Problema sin variables de entorno:**
```bash
# Tienes que recordar configurar cada vez:
cd ~/mi-api
PORT=8080 DEBUG=true npm start
```

**Con ProjectManager:**
```bash
# Registras una vez con las variables:
pm add mi-api --path ~/mi-api --env "PORT=8080,DEBUG=true"

# Siempre ejecutas igual:
pm run mi-api
# Automáticamente usa PORT=8080 y DEBUG=true
```

---

### ¿Cómo Funcionan en ProjectManager?

1. **Registra el proyecto con variables:**
```bash
pm add api --path ~/api --env "PORT=8080,DEBUG=true"
```

2. **Las variables se guardan** en la configuración del proyecto.

3. **Se inyectan automáticamente** cuando ejecutas:
   - `pm build api`
   - `pm run api`
   - `pm test api`

4. **Ver variables configuradas:**
```bash
pm info api
```

---

### Ejemplos de Uso

#### Ejemplo 1: API con Puerto Configurable
```bash
# Registrar con puerto
pm add web-server --path ~/server --env "PORT=3000"

# Ejecutar (usa PORT=3000 automáticamente)
pm run web-server
```

---

#### Ejemplo 2: Proyecto con Múltiples Variables
```bash
# API con varias configuraciones
pm add backend --path ~/backend --env "PORT=8080,DB_HOST=localhost,DB_USER=admin,API_KEY=secret123"

# Compilar (variables disponibles en tiempo de compilación)
pm build backend

# Ejecutar (variables disponibles en tiempo de ejecución)
pm run backend
```

---

#### Ejemplo 3: Maven con Configuración de Memoria
```bash
# Configurar memoria para Maven
pm add proyecto-grande --path ~/proyecto --env "MAVEN_OPTS=-Xms512m -Xmx2048m"

# Maven usará esa configuración
pm build proyecto-grande
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

**Actualmente:** Editar manualmente el archivo `projects.json`.

**Ubicación:**
- Windows: `C:\Users\Usuario\.projectmanager\projects.json`
- Linux/Mac: `~/.projectmanager/projects.json`

🚧 **Próximamente:** `pm env add/remove/update` para gestionar variables desde CLI.

---

### Reglas de Formato

**Formato correcto:**
```bash
# ✅ Correcto
pm add proyecto --path /ruta --env "VAR1=valor1,VAR2=valor2"

# ✅ Con espacios (se eliminan automáticamente)
pm add proyecto --path /ruta --env "VAR1 = valor1 , VAR2 = valor2"

# ✅ Una sola variable
pm add proyecto --path /ruta --env "PORT=8080"
```

**Formato incorrecto:**
```bash
# ❌ Sin comillas
pm add proyecto --path /ruta --env VAR1=valor1,VAR2=valor2

# ❌ Sin el signo =
pm add proyecto --path /ruta --env "VAR1:valor1"
```

---

### Ejemplos Prácticos Completos

#### Ejemplo 1: Servidor Node.js
```bash
# Registrar
pm add node-server --path C:\projects\node-server --env "PORT=3000,NODE_ENV=development"

# Ejecutar (usa las variables automáticamente)
pm run node-server
```

---

#### Ejemplo 2: Aplicación Spring Boot
```bash
# Registrar con múltiples variables
pm add spring-app --path ~/projects/spring-app --env "SERVER_PORT=8080,SPRING_PROFILES_ACTIVE=dev,DB_URL=jdbc:mysql://localhost:3306/mydb"

# Compilar
pm build spring-app

# Ejecutar
pm run spring-app
```

---

#### Ejemplo 3: Proyecto Maven con JVM Optimizado
```bash
# Configurar opciones de memoria para Maven
pm add big-project --path ~/big-project --env "MAVEN_OPTS=-Xmx8G -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Maven usará 8GB de RAM al compilar
pm build big-project
```

---

### Dónde se Guardan

Las variables se almacenan en el archivo de configuración:

**Windows:** `C:\Users\TuUsuario\.projectmanager\projects.json`
**Linux/Mac:** `~/.projectmanager/projects.json`

**Ejemplo de contenido:**
```json
{
  "mi-api": {
    "name": "mi-api",
    "path": "C:\\projects\\mi-api",
    "type": "MAVEN",
    "commands": {
      "build": "mvn package",
      "run": "mvn exec:java",
      "test": "mvn test",
      "clean": "mvn clean"
    },
    "envVars": {
      "PORT": "8080",
      "DEBUG": "true",
      "API_KEY": "secreto"
    },
    "lastModified": "2025-01-18T18:00:00Z"
  }
}
```

---

### Preguntas Frecuentes sobre Variables

#### ¿Puedo cambiar las variables después de registrar?

Actualmente, necesitas editar manualmente el archivo `projects.json`.

🚧 **Próximamente:** Comando `pm config` para modificar variables desde CLI.

---

#### ¿Las variables afectan a otros proyectos?

**No.** Cada proyecto tiene sus propias variables independientes.

---

#### ¿Puedo usar variables del sistema?

**Sí.** Las variables de ProjectManager se agregan a las variables del sistema. Si hay conflicto, las de ProjectManager tienen prioridad.

---

#### ¿Son seguras las variables?

**Advertencia:** Las variables se guardan en texto plano en `projects.json`.

**No guardes:** Contraseñas reales, tokens de producción, información sensible.

**Usa para:** Configuración de desarrollo, puertos, flags de debug, rutas locales.

---

## 🌿 Integración Git

### ¿Qué es?

ProjectManager detecta automáticamente si tu proyecto es un repositorio Git y muestra información útil cuando ejecutas `pm info`.

---

### Información que Muestra

#### 1. Branch Actual
```bash
pm info miproyecto
```

**Muestra:**
```
Git:
  Branch: feature/nueva-funcionalidad
```

**Útil para:** Saber en qué rama estás sin escribir `git branch`.

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
- **staged:** Archivos agregados con `git add` (listos para commit).
- **modified:** Archivos modificados pero NO agregados todavía.
- **untracked:** Archivos nuevos que Git no rastrea.

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

**Útil para:** Recordar hacer push antes de cerrar la PC.

---

### Ejemplo Completo
```bash
pm info web-api
```

**Salida:**
```
╔════════════════════════════════╗
║  ProjectManager v1.0.0         ║
║  Manage your projects          ║
╚════════════════════════════════╝


Project Information
───────────────────

web-api (Gradle)
  Path: C:\projects\web-api
  Modified: hace 2 horas
  Commands: 4
  Environment Variables: 2

  Git:
    Branch: feature/api-endpoints
    Status: 2 modified, 1 untracked
    Unpushed: 3 commits


Available Commands for web-api
────────────────────────────────────────

  build  →  gradle build
  run    →  gradle run
  test   →  gradle test
  clean  →  gradle clean

Environment Variables
─────────────────────

  DEBUG      = true
  GAME_MODE  = creative
```

---

### Casos de Uso de Integración Git

#### Caso 1: Verificar Branch Antes de Trabajar
```bash
# ¿En qué branch estoy?
pm info miproyecto

# Git:
#   Branch: master  ← ¡Cuidado! Estás en master
```

**Evita:** Hacer cambios en la rama equivocada.

---

#### Caso 2: Recordar Hacer Commit
```bash
pm info miproyecto

# Git:
#   Status: 5 modified  ← Tienes cambios sin commitear
```

**Recuerda:** Hacer commit antes de cerrar sesión.

---

#### Caso 3: Recordar Hacer Push
```bash
pm info miproyecto

# Git:
#   Unpushed: 7 commits  ← ¡Tienes trabajo sin subir!
```

**Evita:** Perder trabajo si se daña tu PC.

---

### Proyectos Sin Git

Si un proyecto **no es un repositorio Git**, simplemente no se muestra la sección Git:
```
Project Information
───────────────────

miproyecto (Maven)
  Path: C:\projects\miproyecto
  Modified: hace 1 día
  Commands: 4

Available Commands for miproyecto
  build  →  mvn package
  ...
```

---

### Requisitos

- **Git instalado** en tu sistema.
- **Proyecto debe ser un repositorio Git** (tener carpeta `.git`).

**Verificar que Git está instalado:**
```bash
git --version
```

Si no está instalado: https://git-scm.com/downloads

---

## 💡 Casos de Uso

### Caso 1: Múltiples Proyectos con Diferentes Tecnologías

**Problema:** Tienes 5 proyectos, cada uno con un sistema de build diferente.

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

✅ **Mismo comando para todos, sin cambiar de carpeta.**

---

### Caso 2: Olvidaste los Comandos de un Proyecto

**Problema:** No recuerdas si un proyecto usa `gradle run`.

**Solución:**
```bash
pm commands proyecto1
```

Te muestra todos los comandos disponibles.

---

### Caso 3: Trabajo en Equipo

**Problema:** Cada desarrollador usa comandos diferentes.

**Solución:** Todo el equipo registra proyectos con ProjectManager:
```bash
pm build api
pm test api
pm run frontend
```

✅ **Comandos consistentes para todo el equipo.**

---

### Caso 4: Diferentes Configuraciones por Proyecto

**Problema:** Tienes 3 APIs con diferentes puertos y necesitas recordar cuál usa cuál.

**Con ProjectManager:**
```bash
# Registrar cada una con su puerto
pm add api-usuarios --path ~/api-usuarios --env "PORT=3000"
pm add api-productos --path ~/api-productos --env "PORT=3001"
pm add api-pedidos --path ~/api-pedidos --env "PORT=3002"

# Ejecutar cualquiera (usa su puerto automáticamente)
pm run api-usuarios   # Puerto 3000
pm run api-productos  # Puerto 3001
pm run api-pedidos    # Puerto 3002
```

✅ **No necesitas recordar configuraciones, todo es automático.**

---

## 🗂️ Tipos de Proyecto Soportados

| Tipo | Archivos de Detección | Comandos Configurados |
|------|----------------------|----------------------|
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
  "web-api": {
    "name": "web-api",
    "path": "C:\\Users\\Usuario\\projects\\web-api",
    "type": "GRADLE",
    "commands": {
      "build": "gradle build",
      "run": "gradle run",
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

⚠️ **No recomendado para usuarios normales.**

Si necesitas modificar comandos o variables manualmente:

1. Abre el archivo `projects.json`.
2. Modifica el campo `commands` o `envVars`.
3. Guarda el archivo.

**Ejemplo - Agregar una variable de entorno:**
```json
"envVars": {
  "DEBUG": "true",
  "PORT": "8080",
  "NUEVA_VAR": "nuevo_valor"  ← Agregada
}
```

---

## ❓ Preguntas Frecuentes (FAQ)

### ¿Dónde se guardan mis proyectos?

En un archivo JSON ubicado en:

- Windows: `C:\Users\TuUsuario\.projectmanager\projects.json`
- Linux/Mac: `~/.projectmanager/projects.json`

### ¿Puedo editar el archivo JSON directamente?

Sí, pero **no es recomendable**. Es mejor usar los comandos `pm` para evitar errores de sintaxis.

### ¿Son seguras las variables de entorno?

Las variables se guardan en **texto plano** en el archivo JSON. **No guardes claves secretas o contraseñas** de producción. Está bien para desarrollo local.

### ¿Qué pasa si muevo un proyecto a otra carpeta?

Debes actualizar la ruta:
```bash
pm remove proyecto-viejo
pm add proyecto-viejo --path C:\nueva\ruta --env "VAR1=valor1"
```

### ¿Puedo cambiar los comandos por defecto?

Actualmente, solo editando manualmente el archivo `projects.json`.

🚧 **Próximamente:** Comando `pm config` para modificar comandos desde CLI.

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

1. Verifica que ejecutaste el script de instalación: `.\scripts\install.ps1`.
2. Reinicia PowerShell completamente (cerrar y volver a abrir).
3. Verifica que `C:\Users\TuUsuario\bin` esté en el PATH: `echo $env:Path`.
4. Si no está, ejecuta el script de instalación nuevamente.

---

### Error: "Project not found"

**Causa:** El nombre del proyecto no está registrado o está mal escrito.

**Solución:**

1. Lista todos los proyectos registrados: `pm list`.
2. Verifica que el nombre sea exacto (sensible a mayúsculas/minúsculas).
3. Si no aparece, regístralo: `pm add nombre-proyecto --path C:\ruta`.

---

### Error: "No 'build' command configured for this project"

**Causa:** El proyecto no tiene un comando `build` configurado.

**Solución:**

1. Ve qué comandos están disponibles: `pm commands nombre-proyecto`.
2. Usa un comando disponible (ej: `run`, `test`).
3. Si el proyecto no tiene comandos, fue detectado como tipo UNKNOWN. Vuelve a registrarlo especificando el tipo:
```bash
pm remove nombre-proyecto
pm add nombre-proyecto --path C:\ruta --type GRADLE
```

---

### Error: "Path does not exist"

**Causa:** La ruta especificada no existe o está mal escrita.

**Solución:**

1. Verifica que la ruta existe: `dir C:\ruta\al\proyecto`.
2. Usa la ruta completa (no relativa):
   - ❌ Mal: `pm add proyecto --path .\mi-proyecto`
   - ✅ Bien: `pm add proyecto --path C:\Users\Usuario\projects\mi-proyecto`
3. Si usas `~`, usa la ruta completa en Windows (las tildes no siempre se resuelven correctamente en todos los shells).

---

### Error: "java is not recognized as a command"

**Causa:** Java no está instalado o no está en el PATH.

**Solución:**

1. Verifica que Java esté instalado: `java -version`.
2. Si no está instalado, descárgalo de: https://adoptium.net/
3. Asegúrate de marcar "Agregar al PATH" durante la instalación.
4. Reinicia PowerShell después de instalar.

---

### Las variables de entorno no se están usando

**Causa:** El comando podría no estar usando el método de inyección correcto.

**Verificación:**

1. Confirma que las variables están configuradas: `pm info nombre-proyecto`.
2. Las variables deberían aparecer en la sección "Environment Variables".
3. Si no aparecen, vuelve a registrar el proyecto con `--env`.

---

## 📝 Cheatsheet Rápido
```bash
# === GESTIÓN ===
pm add <nombre> --path <ruta>                    # Registrar proyecto
pm add <nombre> --path <ruta> --env "C=v,C2=v2"  # Registrar con variables
pm list                                          # Listar todos
pm info <nombre>                                 # Ver detalles completos
pm commands <nombre>                             # Ver comandos disponibles
pm remove <nombre>                               # Eliminar (con confirmación)
pm remove <nombre> --force                       # Eliminar (sin confirmación)

# === EJECUCIÓN ===
pm build <nombre>                                # Compilar (con vars)
pm run <nombre>                                  # Ejecutar (con vars)
pm test <nombre>                                 # Tests (con vars)

# === AYUDA ===
pm help                                          # Ayuda general
pm version                                       # Ver versión
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

# Ver info del proyecto (incluye variables y Git)
pm info proyecto1

# Ver comandos disponibles
pm commands proyecto1

# ¡Todo funciona igual desde cualquier carpeta!
```

---

## 🚀 Próximos Pasos

Ahora que conoces ProjectManager:

1. **Registra todos tus proyectos actuales.**
2. **Agrega variables de entorno donde las necesites.**
3. **Úsalo en tu flujo de trabajo diario.**
4. **Explora la integración Git** mediante `pm info`.
5. **Compártelo con tu equipo** para que todos usen comandos consistentes.

---

## 📚 Recursos Adicionales

- **README Principal:** [README.es.md](/README_ES.md)
- **Guía de Instalación:** [scripts/INSTALL.md](/scripts/INSTALL.md)
- **Código Fuente:** [src/main/java/pm/](/src/main/java/pm/)

---

## 🤝 ¿Necesitas Ayuda?

Si tienes problemas o preguntas:

1. Consulta la sección de [Solución de Problemas](#-solución-de-problemas).
2. Revisa las [Preguntas Frecuentes](#-preguntas-frecuentes-faq).
3. Abre un issue en GitHub.

---

**¡Feliz programación con ProjectManager! 🎉**
