# Política de Seguridad

## Versiones Soportadas

| Versión | Soportada |
|---------|-----------|
| 1.3.x   | Sí        |
| < 1.3.0 | No        |

Solo la última release recibe actualizaciones de seguridad. Recomendamos usar siempre la última versión mediante `pm update`.

---

## Reportar una Vulnerabilidad

Si descubres una vulnerabilidad de seguridad, repórtala **de forma privada**:

1. **NO abras un issue público** — esto podría exponer la vulnerabilidad antes de que haya una corrección disponible
2. Contacta por email: **[security@softdryzz.com](mailto:security@softdryzz.com)**
3. Incluye:
   - Descripción de la vulnerabilidad
   - Pasos para reproducirla
   - Versión(es) afectada(s)
   - Impacto potencial

Recibirás una respuesta en un plazo de **72 horas**. Si la vulnerabilidad es confirmada, se publicará una corrección lo antes posible y serás acreditado en las notas de la release (salvo que prefieras permanecer anónimo).

---

## Modelo de Seguridad

### Qué hace ProjectManager

ProjectManager es una **herramienta CLI local** que gestiona metadatos de proyectos y ejecuta comandos shell en nombre del usuario. **No** ejecuta un servidor, no escucha en ningún puerto ni acepta conexiones remotas.

### Almacenamiento de datos

- Todos los datos se almacenan localmente en `~/.projectmanager/projects.json`
- **Escritura atómica** (desde v1.3.7) — los datos se escriben primero en un archivo temporal y luego se renombran. Ninguna escritura parcial puede corromper tus datos.
- **Backup automático** (desde v1.3.7) — se crea `projects.json.bak` antes de cada escritura. Si el archivo principal se corrompe, se restaura automáticamente desde el backup en el siguiente comando.
- No se envían datos a servidores externos (excepto al API de GitHub para verificar actualizaciones)
- Sin telemetría, analíticas ni rastreo de ningún tipo

### Ejecución de comandos

ProjectManager ejecuta comandos shell configurados por el usuario. Estos comandos se ejecutan con los **mismos permisos que el usuario** que invoca `pm`.

**Medidas de seguridad (desde v1.3.8):**
- Los comandos por defecto son cadenas estáticas (`gradle build`, `npm start`, etc.) que nunca incluyen rutas del proyecto. El directorio de trabajo se establece mediante la API `ProcessBuilder.directory()` de Java, sin interpolación en cadenas shell.
- El directorio de trabajo se valida antes de cada ejecución — si el directorio del proyecto no existe, se muestra un error claro con orientación en lugar de un fallo shell confuso.
- Al añadir comandos personalizados, PM avisa si se detectan metacaracteres shell, recordando al usuario que entrecomille rutas si es necesario.

### Acceso a red

ProjectManager solo se conecta a internet para **dos propósitos**:

1. **Verificación de actualización** — En cada ejecución, consulta `https://api.github.com/repos/SoftDryzz/ProjectManager/releases/latest` para buscar nuevas versiones
2. **Descarga de auto-update** — Cuando se usa `pm update`, descarga el JAR desde GitHub Releases

Ambas conexiones usan HTTPS. No se transmiten tokens de autenticación ni datos personales.

**Medidas de seguridad de descarga (desde v1.3.9):**
- La integridad del JAR descargado se valida contra el tamaño esperado desde la respuesta del API de GitHub.
- Los loops de redirección se limitan a 5 saltos para prevenir cadenas de redirección infinitas.
- Los errores de red se clasifican con mensajes específicos (sin conexión, timeout, firewall, SSL) en lugar de fallos genéricos.
- Las descargas parciales o corruptas se detectan y rechazan antes de la instalación.

### Acceso al sistema de archivos

ProjectManager lee y escribe:
- `~/.projectmanager/projects.json` — registro de proyectos
- `~/.projectmanager/projectmanager.jar` — la aplicación en sí (durante actualizaciones)
- Directorios de proyectos — solo para detectar tipos de proyecto (lee `pom.xml`, `package.json`, etc.). **Nunca modifica** archivos del proyecto.

### Dependencias

ProjectManager se construye como un fat JAR con estas dependencias:
- **Gson** (Google) — serialización JSON
- **Maven Shade Plugin** — solo en tiempo de compilación, para crear el fat JAR

No se descargan dependencias en tiempo de ejecución desde la red. La aplicación es completamente autónoma.

---

## Consideraciones de Seguridad Conocidas

### Inyección de comandos shell vía rutas de proyecto
- **Estado:** Abordado en v1.3.8
- **Riesgo:** Bajo — los comandos por defecto son cadenas estáticas que nunca incluyen rutas. El directorio de trabajo se establece vía `ProcessBuilder.directory()` (API de File), sin interpolación en cadenas shell.
- **Mejoras en v1.3.8:** Validación de directorio antes de la ejecución; aviso de metacaracteres al añadir comandos personalizados; mensajes de error claros para directorios que faltan.
- **Consideración restante:** Los comandos personalizados añadidos por usuarios se almacenan y ejecutan tal cual. Si un usuario incluye una ruta con caracteres especiales en un comando personalizado, debe entrecomillarla. PM ahora avisa de esto al añadir el comando.

### Integridad del mecanismo de actualización
- **Estado:** Abordado en v1.3.9
- **Riesgo:** Bajo — solo descarga desde GitHub Releases por HTTPS
- **Mejoras en v1.3.9:** Tamaño del JAR descargado validado contra el tamaño esperado desde la respuesta del API; loops de redirección limitados a 5; errores de red clasificados (sin conexión, timeout, firewall, SSL); descargas parciales detectadas y rechazadas.
- **Consideración restante:** Sin verificación de hash criptográfico (SHA-256). La validación de tamaño detecta descargas parciales pero no manipulación dirigida. Para máxima seguridad, verificar el JAR manualmente (comparación con `sha256sum` de la página de releases).
- **Instalación:** Para instrucciones detalladas de instalación y verificación, consulta la [Guía de Instalación](scripts/INSTALL.md)

### Permisos de archivos locales
- **Estado:** Aceptable
- **Riesgo:** Bajo — `projects.json` tiene permisos por defecto solo para el usuario. Sin embargo, en sistemas compartidos, otros usuarios con acceso a tu directorio home podrían leerlo o modificarlo.
- **Mitigación:** Asegurar que `~/.projectmanager/` tenga los permisos adecuados (`chmod 700` en Linux/Mac)

---

## Roadmap de Seguridad

| Versión | Mejora de Seguridad |
|---------|---------------------|
| v1.3.7 ✅ | Escritura atómica, backup automático, recuperación de datos corruptos, validación de campos al cargar, mensajes de error amigables (sin stack traces) |
| v1.3.8 ✅ | Validación de directorio antes de ejecución; avisos de metacaracteres en comandos personalizados; error claro para directorios ausentes |
| v1.3.9 ✅ | Validación de integridad de descarga (tamaño esperado del API); límite de loops de redirección (máx 5); clasificación de errores de red (sin conexión, timeout, firewall, SSL) |
| v1.5.2  | Comando `pm secure` — escaneo de seguridad del sistema de archivos para buenas prácticas del proyecto |

---

## Buenas Prácticas para Usuarios

1. **Mantén ProjectManager actualizado** — Ejecuta `pm update` regularmente
2. **Sigue la guía de instalación** — Consulta [INSTALL.md](scripts/INSTALL.md) para instrucciones completas de configuración y solución de problemas
3. **Usa nombres de proyecto descriptivos** — Evita nombres que coincidan con comandos de PM (`build`, `run`, `list`)
4. **Entrecomilla rutas en comandos personalizados** — Si tu comando personalizado incluye una ruta con espacios o caracteres especiales, envuélvela en comillas
5. **Revisa los comandos personalizados** — Los comandos configurados con `pm commands set` se ejecutan con tus permisos. Revísalos antes de ejecutarlos.
6. **Protege tu directorio home** — En sistemas compartidos, asegura que `~/.projectmanager/` no sea legible por todos
