package pm.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Ejecuta comandos del sistema operativo.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Ejecutar comandos shell (build, run, test)</li>
 *   <li>Capturar stdout y stderr en tiempo real</li>
 *   <li>Manejar códigos de salida</li>
 *   <li>Ejecutar en el directorio correcto</li>
 *   <li>Manejar timeouts para comandos largos</li>
 * </ul>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class CommandExecutor {

    /**
     * Ejecuta un comando del sistema.
     *
     * @param command comando a ejecutar (ej: "gradle build")
     * @param workingDirectory directorio donde ejecutar
     * @param timeoutSeconds timeout en segundos (0 = sin timeout)
     * @return resultado de la ejecución
     * @throws IOException si falla la ejecución
     * @throws InterruptedException si el proceso es interrumpido
     */
    public ExecutionResult execute(String command, Path workingDirectory, long timeoutSeconds)
            throws IOException, InterruptedException {

        // Validar parámetros
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Command cannot be null or blank");
        }

        if (workingDirectory == null) {
            throw new IllegalArgumentException("Working directory cannot be null");
        }

        // Detectar sistema operativo para usar el shell correcto
        String[] shellCommand = getShellCommand(command);

        // Crear ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(shellCommand);

        // Configurar directorio de trabajo
        processBuilder.directory(workingDirectory.toFile());

        // Redirigir stderr a stdout para capturar todo el output
        processBuilder.redirectErrorStream(true);

        // Iniciar proceso
        long startTime = System.currentTimeMillis();
        Process process = processBuilder.start();

        // Crear thread para leer output en tiempo real
        Thread outputReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    // Mostrar output en tiempo real
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.err.println("Error reading process output: " + e.getMessage());
            }
        });

        // Iniciar lectura de output
        outputReader.start();

        // Variable para almacenar el exit code
        int exitCode;
        boolean finished;

        // Esperar a que termine el proceso (con timeout si se especifica)
        if (timeoutSeconds > 0) {
            finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                // Timeout alcanzado, matar proceso
                process.destroyForcibly();
                outputReader.interrupt();

                long duration = System.currentTimeMillis() - startTime;
                return new ExecutionResult(
                        false,
                        -1,
                        duration,
                        "Process timed out after " + timeoutSeconds + " seconds"
                );
            }

            // Proceso terminó, obtener exit code
            exitCode = process.waitFor();
        } else {
            // Sin timeout, esperar indefinidamente y obtener exit code
            exitCode = process.waitFor();
            finished = true;
        }

        // Esperar a que termine de leer el output
        outputReader.join(1000); // Max 1 segundo

        // Calcular duración
        long duration = System.currentTimeMillis() - startTime;

        // Crear resultado
        return new ExecutionResult(
                exitCode == 0,  // success si exitCode es 0
                exitCode,
                duration,
                exitCode == 0 ? "Command completed successfully" : "Command failed"
        );
    }

    /**
     * Ejecuta un comando sin timeout.
     *
     * @param command comando a ejecutar
     * @param workingDirectory directorio donde ejecutar
     * @return resultado de la ejecución
     * @throws IOException si falla la ejecución
     * @throws InterruptedException si el proceso es interrumpido
     */
    public ExecutionResult execute(String command, Path workingDirectory)
            throws IOException, InterruptedException {
        return execute(command, workingDirectory, 0);
    }

    /**
     * Obtiene el comando shell según el sistema operativo.
     *
     * <p>En Windows usa cmd.exe, en Unix usa sh.
     *
     * @param command comando a ejecutar
     * @return array con el comando shell apropiado
     */
    private String[] getShellCommand(String command) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("windows")) {
            // Windows: usar cmd.exe
            return new String[]{"cmd.exe", "/c", command};
        } else {
            // Linux/Mac: usar sh
            return new String[]{"sh", "-c", command};
        }
    }

    /**
     * Resultado de la ejecución de un comando.
     *
     * <p>Record inmutable que contiene:
     * <ul>
     *   <li>success - si el comando terminó exitosamente</li>
     *   <li>exitCode - código de salida del proceso</li>
     *   <li>durationMs - duración en milisegundos</li>
     *   <li>message - mensaje descriptivo</li>
     * </ul>
     *
     * @param success true si exitCode == 0
     * @param exitCode código de salida del proceso
     * @param durationMs duración en milisegundos
     * @param message mensaje descriptivo
     */
    public record ExecutionResult(
            boolean success,
            int exitCode,
            long durationMs,
            String message
    ) {
        /**
         * Obtiene la duración en segundos.
         *
         * @return duración en segundos (redondeado)
         */
        public long durationSeconds() {
            return durationMs / 1000;
        }

        /**
         * Formatea la duración de forma legible.
         *
         * @return string con formato "Xs" o "Xm Ys"
         */
        public String formattedDuration() {
            long seconds = durationMs / 1000;

            if (seconds < 60) {
                return seconds + "s";
            } else {
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;
                return minutes + "m " + remainingSeconds + "s";
            }
        }
    }
}