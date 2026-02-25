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

**Limitaciones actuales (se abordarán en v1.3.8):**
- Las rutas de los proyectos no se escapan antes de pasarlas a comandos shell. Rutas que contengan metacaracteres shell (`&`, `|`, `;`, etc.) podrían llevar a ejecución de comandos no deseada. Evita registrar proyectos con caracteres especiales en sus rutas hasta la v1.3.8.

### Acceso a red

ProjectManager solo se conecta a internet para **dos propósitos**:

1. **Verificación de actualización** — En cada ejecución, consulta `https://api.github.com/repos/SoftDryzz/ProjectManager/releases/latest` para buscar nuevas versiones
2. **Descarga de auto-update** — Cuando se usa `pm update`, descarga el JAR desde GitHub Releases

Ambas conexiones usan HTTPS. No se transmiten tokens de autenticación ni datos personales.

**Limitaciones actuales (se abordarán en v1.3.9):**
- La integridad del JAR descargado se valida solo con un check de tamaño mínimo (> 1KB). Se planea una validación más robusta (tamaño esperado desde la respuesta del API).
- Los loops de redirección del API de GitHub no están limitados, lo que podría causar un cuelgue.

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
- **Estado:** Conocido, corrección planificada para v1.3.8
- **Riesgo:** Bajo — requiere que el usuario registre manualmente un proyecto con una ruta maliciosa
- **Mitigación:** No registrar proyectos cuyas rutas contengan metacaracteres shell (`&`, `|`, `;`, `` ` ``, `$`, etc.)

### Integridad del mecanismo de actualización
- **Estado:** Conocido, mejoras planificadas para v1.3.9
- **Riesgo:** Bajo — solo descarga desde GitHub Releases por HTTPS
- **Mitigación:** Verificar el JAR descargado manualmente si hay dudas (comparación con `sha256sum` de la página de releases)

### Permisos de archivos locales
- **Estado:** Aceptable
- **Riesgo:** Bajo — `projects.json` tiene permisos por defecto solo para el usuario. Sin embargo, en sistemas compartidos, otros usuarios con acceso a tu directorio home podrían leerlo o modificarlo.
- **Mitigación:** Asegurar que `~/.projectmanager/` tenga los permisos adecuados (`chmod 700` en Linux/Mac)

---

## Roadmap de Seguridad

| Versión | Mejora de Seguridad |
|---------|---------------------|
| v1.3.7 ✅ | Escritura atómica, backup automático, recuperación de datos corruptos, validación de campos al cargar, mensajes de error amigables (sin stack traces) |
| v1.3.8  | Escapar metacaracteres shell en rutas de proyecto; validar directorios antes de la ejecución |
| v1.3.9  | Validar integridad de descarga; limitar loops de redirección; distinguir tipos de error de red |
| v1.5.2  | Comando `pm secure` — escaneo de seguridad del sistema de archivos para buenas prácticas del proyecto |

---

## Buenas Prácticas para Usuarios

1. **Mantén ProjectManager actualizado** — Ejecuta `pm update` regularmente
2. **Usa nombres de proyecto descriptivos** — Evita nombres que coincidan con comandos de PM (`build`, `run`, `list`)
3. **Evita caracteres especiales en rutas de proyecto** — Hasta la v1.3.8, rutas con `&`, `|`, `;` pueden causar problemas
4. **Revisa los comandos personalizados** — Los comandos configurados con `pm commands set` se ejecutan con tus permisos. Revísalos antes de ejecutarlos.
5. **Protege tu directorio home** — En sistemas compartidos, asegura que `~/.projectmanager/` no sea legible por todos
