package pm.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.Map;

/**
 * Executes operating system commands.
 *
 * <p>Responsibilities:
 * <ul>
 * <li>Execute shell commands (build, run, test)</li>
 * <li>Capture stdout and stderr in real-time</li>
 * <li>Handle exit codes</li>
 * <li>Execute in the correct directory</li>
 * <li>Handle timeouts for long-running commands</li>
 * </ul>
 *
 * @author SoftDryzz
 * @version 1.0.0
 * @since 1.0.0
 */
public class CommandExecutor {

    /**
     * Executes a system command.
     *
     * @param command command to execute (e.g., "gradle build")
     * @param workingDirectory directory where to execute
     * @param timeoutSeconds timeout in seconds (0 = no timeout)
     * @return execution result
     * @throws IOException if execution fails
     * @throws InterruptedException if the process is interrupted
     */
    public ExecutionResult execute(String command, Path workingDirectory, long timeoutSeconds)
            throws IOException, InterruptedException {

        // Validate parameters
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Command cannot be null or blank");
        }

        if (workingDirectory == null) {
            throw new IllegalArgumentException("Working directory cannot be null");
        }

        // Detect operating system to use the correct shell
        String[] shellCommand = getShellCommand(command);

        // Create ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(shellCommand);

        // Set working directory
                processBuilder.directory(workingDirectory.toFile());

        // Redirect stderr to stdout to capture all output
                processBuilder.redirectErrorStream(true);

        // Start process
        long startTime = System.currentTimeMillis();
        Process process = processBuilder.start();

        // Create thread to read output in real-time
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

        // Start output reading
        outputReader.start();

        // Variable to store the exit code
        int exitCode;
        boolean finished;

        // Wait for process to finish (with timeout if specified)
        if (timeoutSeconds > 0) {
            finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                // Timeout reached, forcibly kill process
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

            // Process finished, get exit code
            exitCode = process.waitFor();
        } else {
            // No timeout, wait indefinitely and get exit code
            exitCode = process.waitFor();
            finished = true;
        }

        // Wait for output reading to finish
        outputReader.join(1000); // Max 1 segundo

        // Calculate duration
        long duration = System.currentTimeMillis() - startTime;

        // Create result
        return new ExecutionResult(
                exitCode == 0,  // success si exitCode es 0
                exitCode,
                duration,
                exitCode == 0 ? "Command completed successfully" : "Command failed"
        );
    }

    /**
     * Executes a command without a timeout.
     *
     * @param command command to execute
     * @param workingDirectory directory where to execute
     * @return execution result
     * @throws IOException if execution fails
     * @throws InterruptedException if the process is interrupted
     */
    public ExecutionResult execute(String command, Path workingDirectory)
            throws IOException, InterruptedException {
        return execute(command, workingDirectory, 0);
    }

    /**
     * Gets the shell command based on the operating system.
     *
     * <p>Uses cmd.exe on Windows, sh on Unix.
     *
     * @param command command to execute
     * @return array with the appropriate shell command
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
     * Executes a system command with custom environment variables.
     *
     * @param command command to execute
     * @param workingDirectory directory where to execute
     * @param timeoutSeconds timeout in seconds (0 = no timeout)
     * @param envVars additional environment variables
     * @return execution result
     * @throws IOException if execution fails
     * @throws InterruptedException if the process is interrupted
     */
    public ExecutionResult execute(String command, Path workingDirectory, long timeoutSeconds, Map<String, String> envVars)
            throws IOException, InterruptedException {

        // Validate parameters
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Command cannot be null or blank");
        }

        if (workingDirectory == null) {
            throw new IllegalArgumentException("Working directory cannot be null");
        }

        // Detect operating system to use the correct shell
        String[] shellCommand = getShellCommand(command);

        // Create ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(shellCommand);

        // Set working directory
        processBuilder.directory(workingDirectory.toFile());

        // Add custom environment variables
        if (envVars != null && !envVars.isEmpty()) {
            Map<String, String> environment = processBuilder.environment();
            environment.putAll(envVars);
        }

        // Redirect stderr to stdout to capture all output
        processBuilder.redirectErrorStream(true);

        // Start process
        long startTime = System.currentTimeMillis();
        Process process = processBuilder.start();

        // Create thread to read output in real-time
        Thread outputReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    // Show output in real-time
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.err.println("Error reading process output: " + e.getMessage());
            }
        });

        // Start output reading
        outputReader.start();

        // Variable to store the exit code
        int exitCode;
        boolean finished;

        // Wait for process to finish (with timeout if specified)
        if (timeoutSeconds > 0) {
            finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);

            if (!finished) {
                // Timeout reached, forcibly kill process
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

            // Process finished, get exit code
            exitCode = process.waitFor();
        } else {
            // No timeout, wait indefinitely and get exit code
            exitCode = process.waitFor();
            finished = true;
        }

        // Wait for output reading to finish
        outputReader.join(1000); // Max 1 segundo

        // Calculate duration
        long duration = System.currentTimeMillis() - startTime;

        // Create result
        return new ExecutionResult(
                exitCode == 0,  // success si exitCode es 0
                exitCode,
                duration,
                exitCode == 0 ? "Command completed successfully" : "Command failed"
        );
    }

    /**
     * Result of a command execution.
     *
     * <p>Immutable record containing:
     * <ul>
     * <li>success - if the command finished successfully</li>
     * <li>exitCode - process exit code</li>
     * <li>durationMs - duration in milliseconds</li>
     * <li>message - descriptive message</li>
     * </ul>
     *
     * @param success true if exitCode == 0
     * @param exitCode process exit code
     * @param durationMs duration in milliseconds
     * @param message descriptive message
     */
    public record ExecutionResult(
            boolean success,
            int exitCode,
            long durationMs,
            String message
    ) {
        /**
         * Gets the duration in seconds.
         *
         * @return duration in seconds (rounded)
         */
        public long durationSeconds() {
            return durationMs / 1000;
        }

        /**
         * Formats the duration in a readable way.
         *
         * @return string with format "Xs" or "Xm Ys"
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