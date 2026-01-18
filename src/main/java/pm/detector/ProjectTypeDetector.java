package pm.detector;

import pm.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Detecta automáticamente el tipo de proyecto analizando archivos.
 *
 * <p>Estrategia de detección:
 * <ul>
 *   <li>Gradle: presencia de build.gradle o build.gradle.kts</li>
 *   <li>Maven: presencia de pom.xml</li>
 *   <li>Node.js: presencia de package.json</li>
 *   <li>.NET: presencia de archivos *.csproj o *.fsproj</li>
 *   <li>Python: presencia de requirements.txt o setup.py</li>
 *   <li>Unknown: ninguno de los anteriores</li>
 * </ul>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProjectTypeDetector {

    /**
     * Detecta el tipo de proyecto analizando archivos en el directorio.
     *
     * @param projectPath ruta al directorio del proyecto
     * @return tipo de proyecto detectado (nunca null, retorna UNKNOWN si no detecta)
     * @throws IllegalArgumentException si projectPath es null o no existe
     */
    public static ProjectType detect(Path projectPath) {
        // Validar argumentos
        if (projectPath == null) {
            throw new IllegalArgumentException("Project path cannot be null");
        }

        if (!Files.exists(projectPath)) {
            throw new IllegalArgumentException("Project path does not exist: " + projectPath);
        }

        if (!Files.isDirectory(projectPath)) {
            throw new IllegalArgumentException("Project path is not a directory: " + projectPath);
        }

        // Detectar en orden de prioridad

        // 1. Gradle (build.gradle o build.gradle.kts)
        if (fileExists(projectPath, Constants.FILE_BUILD_GRADLE) ||
                fileExists(projectPath, Constants.FILE_BUILD_GRADLE_KTS)) {
            return ProjectType.GRADLE;
        }

        // 2. Maven (pom.xml)
        if (fileExists(projectPath, Constants.FILE_POM_XML)) {
            return ProjectType.MAVEN;
        }

        // 3. Node.js (package.json)
        if (fileExists(projectPath, Constants.FILE_PACKAGE_JSON)) {
            return ProjectType.NODEJS;
        }

        // 4. .NET (*.csproj o *.fsproj)
        if (hasCsprojFile(projectPath)) {
            return ProjectType.DOTNET;
        }

        // 5. Python (requirements.txt o setup.py)
        if (fileExists(projectPath, Constants.FILE_REQUIREMENTS_TXT) ||
                fileExists(projectPath, "setup.py")) {
            return ProjectType.PYTHON;
        }

        // No se pudo detectar
        return ProjectType.UNKNOWN;
    }

    /**
     * Verifica si existe un archivo con nombre específico en el directorio.
     *
     * @param directory directorio donde buscar
     * @param fileName nombre del archivo a buscar
     * @return true si el archivo existe, false en caso contrario
     */
    private static boolean fileExists(Path directory, String fileName) {
        Path filePath = directory.resolve(fileName);
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }

    /**
     * Verifica si existe algún archivo .csproj en el directorio.
     *
     * <p>Busca archivos que terminen en .csproj o .fsproj (C# y F#).
     *
     * @param directory directorio donde buscar
     * @return true si encuentra al menos un archivo de proyecto .NET
     */
    private static boolean hasCsprojFile(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files.anyMatch(path -> {
                String name = path.getFileName().toString().toLowerCase();
                return name.endsWith(".csproj") || name.endsWith(".fsproj");
            });
        } catch (IOException e) {
            // Si falla la lectura del directorio, asumir que no hay .csproj
            return false;
        }
    }
}