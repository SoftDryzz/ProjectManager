package pm.detector;

/**
 * Enumeración de tipos de proyecto soportados por ProjectManager.
 *
 * Cada tipo representa un sistema de build o ecosistema de desarrollo diferente.
 * ProjectManager puede auto-detectar el tipo basándose en archivos presentes
 * en el directorio del proyecto.
 *
 * <p>Tipos soportados:
 * <ul>
 *   <li>{@link #GRADLE} - Proyectos Java/Kotlin con Gradle</li>
 *   <li>{@link #MAVEN} - Proyectos Java con Maven</li>
 *   <li>{@link #NODEJS} - Proyectos JavaScript/TypeScript con npm</li>
 *   <li>{@link #DOTNET} - Proyectos C#/F# con dotnet CLI</li>
 *   <li>{@link #PYTHON} - Proyectos Python con pip</li>
 *   <li>{@link #UNKNOWN} - Tipo no identificado</li>
 * </ul>
 *
 * <p>Ejemplo de uso:
 * <pre>{@code
 * ProjectType type = ProjectType.GRADLE;
 * System.out.println(type.displayName()); // Output: "Gradle"
 * }</pre>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public enum ProjectType {

    /**
     * Proyecto Java/Kotlin que usa Gradle como sistema de build.
     *
     * <p>Detección: Presencia de build.gradle o build.gradle.kts
     * <p>Comandos típicos: gradle build, gradle run, gradle test
     */
    GRADLE("Gradle"),

    /**
     * Proyecto Java que usa Maven como sistema de build.
     *
     * <p>Detección: Presencia de pom.xml
     * <p>Comandos típicos: mvn package, mvn exec:java, mvn test
     */
    MAVEN("Maven"),

    /**
     * Proyecto JavaScript/TypeScript que usa npm como gestor de paquetes.
     *
     * <p>Detección: Presencia de package.json
     * <p>Comandos típicos: npm run build, npm start, npm test
     */
    NODEJS("Node.js"),

    /**
     * Proyecto C#/F# que usa dotnet CLI.
     *
     * <p>Detección: Presencia de archivos *.csproj o *.fsproj
     * <p>Comandos típicos: dotnet build, dotnet run, dotnet test
     */
    DOTNET(".NET"),

    /**
     * Proyecto Python que usa pip como gestor de paquetes.
     *
     * <p>Detección: Presencia de requirements.txt o setup.py
     * <p>Comandos típicos: pip install -r requirements.txt, python main.py
     */
    PYTHON("Python"),

    /**
     * Tipo de proyecto desconocido o no soportado.
     *
     * <p>Se usa cuando no se puede determinar el tipo automáticamente.
     * El usuario puede especificar comandos manualmente.
     */
    UNKNOWN("Unknown");

    /**
     * Nombre legible del tipo de proyecto.
     * Usado para mostrar en la interfaz de usuario.
     */
    private final String displayName;

    /**
     * Constructor del enum.
     *
     * @param displayName nombre legible para mostrar al usuario
     */
    ProjectType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Obtiene el nombre legible del tipo de proyecto.
     *
     * @return nombre del tipo de proyecto (ej: "Gradle", "Maven")
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Verifica si el tipo es conocido (no UNKNOWN).
     *
     * @return true si el tipo fue detectado correctamente, false si es UNKNOWN
     */
    public boolean isKnown() {
        return this != UNKNOWN;
    }
}