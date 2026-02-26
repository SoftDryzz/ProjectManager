package pm.detector;

import pm.util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Automatically detects the project type by analyzing files.
 *
 * <p>Detection strategy (in priority order):
 * <ul>
 * <li>Gradle: presence of build.gradle or build.gradle.kts</li>
 * <li>Maven: presence of pom.xml</li>
 * <li>Rust: presence of Cargo.toml</li>
 * <li>Go: presence of go.mod</li>
 * <li>Flutter: presence of pubspec.yaml</li>
 * <li>pnpm: presence of pnpm-lock.yaml (+ package.json)</li>
 * <li>Bun: presence of bun.lockb or bun.lock (+ package.json)</li>
 * <li>Yarn: presence of yarn.lock (+ package.json)</li>
 * <li>Node.js: presence of package.json (fallback for JS/TS projects)</li>
 * <li>.NET: presence of *.csproj or *.fsproj files</li>
 * <li>Python: presence of requirements.txt or setup.py</li>
 * <li>Unknown: none of the above</li>
 * </ul>
 *
 * @author SoftDryzz
 * @version 1.3.3
 * @since 1.0.0
 */
public class ProjectTypeDetector {

    /**
     * Detects the project type by analyzing files in the directory.
     *
     * @param projectPath path to the project directory
     * @return detected project type (never null, returns UNKNOWN if not detected)
     * @throws IllegalArgumentException if projectPath is null or does not exist
     */
    public static ProjectType detect(Path projectPath) {
        // Validate arguments
        if (projectPath == null) {
            throw new IllegalArgumentException("Project path cannot be null");
        }

        if (!Files.exists(projectPath)) {
            throw new IllegalArgumentException("Project path does not exist: " + projectPath);
        }

        if (!Files.isDirectory(projectPath)) {
            throw new IllegalArgumentException("Project path is not a directory: " + projectPath);
        }

        // Detect in priority order

        // 1. Gradle (build.gradle or build.gradle.kts)
        if (fileExists(projectPath, Constants.FILE_BUILD_GRADLE) ||
                fileExists(projectPath, Constants.FILE_BUILD_GRADLE_KTS)) {
            return ProjectType.GRADLE;
        }

        // 2. Maven (pom.xml)
        if (fileExists(projectPath, Constants.FILE_POM_XML)) {
            return ProjectType.MAVEN;
        }

        // 3. Rust (Cargo.toml)
        if (fileExists(projectPath, Constants.FILE_CARGO_TOML)) {
            return ProjectType.RUST;
        }

        // 4. Go (go.mod)
        if (fileExists(projectPath, Constants.FILE_GO_MOD)) {
            return ProjectType.GO;
        }

        // 5. Flutter (pubspec.yaml)
        if (fileExists(projectPath, Constants.FILE_PUBSPEC_YAML)) {
            return ProjectType.FLUTTER;
        }

        // 6. pnpm (pnpm-lock.yaml + package.json)
        if (fileExists(projectPath, Constants.FILE_PNPM_LOCK)) {
            return ProjectType.PNPM;
        }

        // 7. Bun (bun.lockb or bun.lock)
        if (fileExists(projectPath, Constants.FILE_BUN_LOCKB) ||
                fileExists(projectPath, Constants.FILE_BUN_LOCK)) {
            return ProjectType.BUN;
        }

        // 8. Yarn (yarn.lock)
        if (fileExists(projectPath, Constants.FILE_YARN_LOCK)) {
            return ProjectType.YARN;
        }

        // 9. Node.js (package.json - fallback for JS/TS without specific lock file)
        if (fileExists(projectPath, Constants.FILE_PACKAGE_JSON)) {
            return ProjectType.NODEJS;
        }

        // 10. .NET (*.csproj o *.fsproj)
        if (hasCsprojFile(projectPath)) {
            return ProjectType.DOTNET;
        }

        // 11. Python (requirements.txt o setup.py)
        if (fileExists(projectPath, Constants.FILE_REQUIREMENTS_TXT) ||
                fileExists(projectPath, "setup.py")) {
            return ProjectType.PYTHON;
        }

        // 12. Docker (docker-compose.yml or docker-compose.yaml)
        if (fileExists(projectPath, Constants.FILE_DOCKER_COMPOSE_YML) ||
                fileExists(projectPath, Constants.FILE_DOCKER_COMPOSE_YAML)) {
            return ProjectType.DOCKER;
        }

        // Could not detect
        return ProjectType.UNKNOWN;
    }

    /**
     * Checks if a file with a specific name exists in the directory.
     *
     * @param directory directory to search in
     * @param fileName name of the file to search for
     * @return true if the file exists, false otherwise
     */
    private static boolean fileExists(Path directory, String fileName) {
        Path filePath = directory.resolve(fileName);
        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }

    /**
     * Checks if any .csproj file exists in the directory.
     *
     * <p>Searches for files ending in .csproj or .fsproj (C# and F#).
     *
     * @param directory directory to search in
     * @return true if at least one .NET project file is found
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