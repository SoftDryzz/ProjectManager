# ProjectManager 🛠️

**ProjectManager** es una herramienta de línea de comandos para **gestionar proyectos de software** de manera sencilla. Actualmente se encuentra en desarrollo y creciendo, por lo que muchas funcionalidades están en construcción.  

---

## 🚀 Características

- Gestión de proyectos en múltiples tecnologías: **Gradle, Maven, Node.js, .NET, Python**.
- Comandos para **agregar, listar, construir, ejecutar y escanear** proyectos.
- Sistema de detección automática de tipo de proyecto según archivos de configuración (`build.gradle`, `pom.xml`, `package.json`).
- Registro de comandos personalizados para cada proyecto.
- **Plataforma de crecimiento:** nuevas funciones en desarrollo continuo.

> Nota: Muchas funcionalidades aún están en construcción y próximamente se agregarán mejoras.  

---

## 💻 Comandos disponibles

Actualmente, ProjectManager soporta los siguientes comandos:

```bash
pm add <name> --path <path>    # Agregar un proyecto
pm list                        # Listar proyectos
pm build <name>                # Construir proyecto
pm run <name>                  # Ejecutar proyecto
pm scan <name>                 # Escanear comandos
pm help                        # Mostrar ayuda
pm version                     # Mostrar versión
