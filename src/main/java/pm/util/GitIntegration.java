package pm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utilidad para obtener información de Git de un proyecto.
 *
 * <p>Proporciona información como:
 * <ul>
 *   <li>Branch actual</li>
 *   <li>Estado de archivos (modificados, sin seguimiento)</li>
 *   <li>Commits pendientes de push</li>
 * </ul>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class GitIntegration {

    /**
     * Verifica si un directorio es un repositorio Git.
     *
     * @param projectPath ruta del proyecto
     * @return true si existe la carpeta .git
     */
    public static boolean isGitRepository(Path projectPath) {
        Path gitDir = projectPath.resolve(".git");
        return Files.exists(gitDir) && Files.isDirectory(gitDir);
    }

    /**
     * Obtiene el nombre del branch actual.
     *
     * @param projectPath ruta del proyecto
     * @return nombre del branch o null si falla
     */
    public static String getCurrentBranch(Path projectPath) {
        try {
            String output = executeGitCommand(projectPath, "git", "rev-parse", "--abbrev-ref", "HEAD");
            return output != null ? output.trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene el estado del repositorio (archivos modificados, sin seguimiento, etc).
     *
     * @param projectPath ruta del proyecto
     * @return objeto GitStatus con la información
     */
    public static GitStatus getStatus(Path projectPath) {
        try {
            String output = executeGitCommand(projectPath, "git", "status", "--porcelain");

            if (output == null || output.isBlank()) {
                return new GitStatus(0, 0, 0, true);
            }

            int modified = 0;
            int untracked = 0;
            int staged = 0;

            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.length() < 2) continue;

                char statusCode = line.charAt(0);
                char workingTreeCode = line.charAt(1);

                // Staged (index)
                if (statusCode != ' ' && statusCode != '?') {
                    staged++;
                }

                // Modified (working tree)
                if (workingTreeCode == 'M') {
                    modified++;
                }

                // Untracked
                if (statusCode == '?' && workingTreeCode == '?') {
                    untracked++;
                }
            }

            boolean clean = (modified == 0 && untracked == 0 && staged == 0);
            return new GitStatus(modified, untracked, staged, clean);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene la cantidad de commits pendientes de push.
     *
     * @param projectPath ruta del proyecto
     * @return número de commits o -1 si falla
     */
    public static int getCommitsAhead(Path projectPath) {
        try {
            String output = executeGitCommand(projectPath, "git", "rev-list", "--count", "@{u}..");

            if (output == null || output.isBlank()) {
                return 0;
            }

            return Integer.parseInt(output.trim());

        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Ejecuta un comando de Git y retorna el output.
     *
     * @param workingDir directorio donde ejecutar
     * @param command comando y argumentos
     * @return output del comando o null si falla
     */
    private static String executeGitCommand(Path workingDir, String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workingDir.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor();

            if (process.exitValue() != 0) {
                return null;
            }

            return output.toString();

        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    /**
     * Clase para representar el estado de Git.
     */
    public static class GitStatus {
        private final int modified;
        private final int untracked;
        private final int staged;
        private final boolean clean;

        public GitStatus(int modified, int untracked, int staged, boolean clean) {
            this.modified = modified;
            this.untracked = untracked;
            this.staged = staged;
            this.clean = clean;
        }

        public int modified() { return modified; }
        public int untracked() { return untracked; }
        public int staged() { return staged; }
        public boolean isClean() { return clean; }

        @Override
        public String toString() {
            if (clean) {
                return "Clean working tree";
            }

            StringBuilder sb = new StringBuilder();
            if (staged > 0) sb.append(staged).append(" staged, ");
            if (modified > 0) sb.append(modified).append(" modified, ");
            if (untracked > 0) sb.append(untracked).append(" untracked");

            String result = sb.toString();
            if (result.endsWith(", ")) {
                result = result.substring(0, result.length() - 2);
            }

            return result;
        }
    }
}