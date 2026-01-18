package pm.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Constantes globales de la aplicación ProjectManager.
 *
 * Esta clase centraliza todos los valores constantes usados en el proyecto:
 * - Rutas de archivos y directorios de configuración
 * - Comandos por defecto para diferentes tipos de proyecto
 * - Nombres de archivos para detección de tipo de proyecto
 *
 * La clase es final y tiene constructor privado para prevenir instanciación
 * (patrón utility class).
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Constants {

    /**
     * Constructor privado para prevenir instanciación.
     * Esta clase solo contiene constantes estáticas.
     *
     * @throws AssertionError siempre, para indicar que no se debe instanciar
     */
    private Constants() {
        throw new AssertionError("Constants class cannot be instantiated");
    }

    // ============================================================
    // VERSIÓN DE LA APLICACIÓN
    // ============================================================

    /**
     * Versión actual de ProjectManager.
     * Sigue Semantic Versioning (MAJOR.MINOR.PATCH).
     */
    public static final String VERSION = "1.0.0";

    // ============================================================
    // DIRECTORIOS Y ARCHIVOS DEL SISTEMA
    // ============================================================

    /**
     * Directorio home del usuario actual.
     * Obtenido de la propiedad del sistema "user.home".
     *
     * Ejemplos:
     * - Linux/Mac: /home/usuario o /Users/usuario
     * - Windows: C:\Users\ usuario
     */
    public static final Path HOME = Paths.get(System.getProperty("user.home"));

    /**
     * Directorio de configuración de ProjectManager.
     * Se crea en: ~/.projectmanager/
     *
     * Aquí se almacenan:
     * - projects.json (lista de proyectos registrados)
     * - cache/ (caché de escaneos)
     * - logs/ (logs de ejecución)
     */
    public static final Path CONFIG_DIR = HOME.resolve(".projectmanager");

    /**
     * Archivo JSON que contiene la lista de proyectos registrados.
     * Ruta: ~/.projectmanager/projects.json
     *
     * Estructura del archivo:
     * {
     *   "minecraft-client": {
     *     "name": "minecraft-client",
     *     "path": "/home/user/projects/minecraft-client",
     *     "type": "GRADLE",
     *     "commands": {
     *       "build": "gradle build",
     *       "run": "gradle runClient"
     *     }
     *   }
     * }
     */
    public static final Path PROJECTS_FILE = CONFIG_DIR.resolve("projects.json");

    /**
     * Directorio de caché para almacenar resultados de escaneos.
     * Ruta: ~/.projectmanager/cache/
     *
     * Se usa para evitar re-escanear proyectos grandes.
     */
    public static final Path CACHE_DIR = CONFIG_DIR.resolve("cache");

    // ============================================================
    // COMANDOS POR DEFECTO - GRADLE
    // ============================================================

    /**
     * Comando por defecto para compilar proyectos Gradle.
     * Ejecuta todas las tareas de compilación y genera el JAR.
     */
    public static final String BUILD_GRADLE = "gradle build";

    /**
     * Comando por defecto para ejecutar proyectos Gradle.
     * Usa la tarea "run" definida en build.gradle.
     */
    public static final String RUN_GRADLE = "gradle run";

    /**
     * Comando por defecto para ejecutar tests en proyectos Gradle.
     * Ejecuta todos los tests con JUnit.
     */
    public static final String TEST_GRADLE = "gradle test";

    /**
     * Comando para limpiar archivos generados en proyectos Gradle.
     * Borra el directorio build/.
     */
    public static final String CLEAN_GRADLE = "gradle clean";

    // ============================================================
    // COMANDOS POR DEFECTO - MAVEN
    // ============================================================

    /**
     * Comando por defecto para compilar proyectos Maven.
     * Ejecuta las fases: compile, test, package.
     */
    public static final String BUILD_MAVEN = "mvn package";

    /**
     * Comando por defecto para ejecutar proyectos Maven.
     * Usa el plugin exec-maven-plugin.
     * Requiere configuración en pom.xml.
     */
    public static final String RUN_MAVEN = "mvn exec:java";

    /**
     * Comando por defecto para ejecutar tests en proyectos Maven.
     * Ejecuta la fase test (JUnit/TestNG).
     */
    public static final String TEST_MAVEN = "mvn test";

    /**
     * Comando para limpiar archivos generados en proyectos Maven.
     * Borra el directorio target/.
     */
    public static final String CLEAN_MAVEN = "mvn clean";

    // ============================================================
    // COMANDOS POR DEFECTO - NODE.JS
    // ============================================================

    /**
     * Comando por defecto para compilar proyectos Node.js.
     * Ejecuta el script "build" definido en package.json.
     */
    public static final String BUILD_NPM = "npm run build";

    /**
     * Comando por defecto para ejecutar proyectos Node.js.
     * Ejecuta el script "start" definido en package.json.
     */
    public static final String RUN_NPM = "npm start";

    /**
     * Comando por defecto para ejecutar tests en proyectos Node.js.
     * Ejecuta el script "test" (típicamente Jest, Mocha, etc).
     */
    public static final String TEST_NPM = "npm test";

    // ============================================================
    // COMANDOS POR DEFECTO - .NET
    // ============================================================

    /**
     * Comando por defecto para compilar proyectos .NET (C#/F#).
     * Compila el proyecto y genera DLL/EXE.
     */
    public static final String BUILD_DOTNET = "dotnet build";

    /**
     * Comando por defecto para ejecutar proyectos .NET.
     * Ejecuta la aplicación compilada.
     */
    public static final String RUN_DOTNET = "dotnet run";

    /**
     * Comando por defecto para ejecutar tests en proyectos .NET.
     * Usa xUnit, NUnit o MSTest.
     */
    public static final String TEST_DOTNET = "dotnet test";

    // ============================================================
    // ARCHIVOS DE DETECCIÓN DE TIPO DE PROYECTO
    // ============================================================

    /**
     * Nombre del archivo de configuración de Gradle.
     * Si existe este archivo en la raíz del proyecto, es un proyecto Gradle.
     */
    public static final String FILE_BUILD_GRADLE = "build.gradle";

    /**
     * Nombre del archivo de configuración de Gradle con Kotlin DSL.
     * Alternativa a build.gradle usando sintaxis Kotlin.
     */
    public static final String FILE_BUILD_GRADLE_KTS = "build.gradle.kts";

    /**
     * Nombre del archivo de configuración de Maven.
     * Si existe este archivo en la raíz del proyecto, es un proyecto Maven.
     */
    public static final String FILE_POM_XML = "pom.xml";

    /**
     * Nombre del archivo de configuración de Node.js.
     * Contiene metadata del proyecto npm y scripts.
     */
    public static final String FILE_PACKAGE_JSON = "package.json";

    /**
     * Extensión de archivos de proyecto .NET.
     * Ejemplos: MyApp.csproj, MyLib.fsproj
     */
    public static final String FILE_CSPROJ = ".csproj";

    /**
     * Archivo de dependencias de Python.
     * Contiene lista de paquetes pip necesarios.
     */
    public static final String FILE_REQUIREMENTS_TXT = "requirements.txt";
}