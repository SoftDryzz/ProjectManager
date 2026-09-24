package pm.repos;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs a short external command with a time limit and a size limit on its
 * output. The output is read on a separate thread so a program that never
 * closes its stdout can't block pm past the timeout.
 */
final class BoundedProcess {

    private BoundedProcess() {
    }

    /**
     * @param command  command and arguments; the program must be an absolute path
     * @param timeout  maximum run time
     * @param maxBytes maximum stdout size kept
     * @param extraEnv variables added to the child's environment
     * @return stdout as UTF-8, or null on start failure, timeout or non-zero exit
     */
    static String run(List<String> command, Duration timeout, int maxBytes, Map<String, String> extraEnv) {
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD);
            builder.environment().putAll(extraEnv);
            process = builder.start();
            process.getOutputStream().close();
            InputStream stdout = process.getInputStream();
            CompletableFuture<byte[]> output = CompletableFuture.supplyAsync(() -> {
                try {
                    return stdout.readNBytes(maxBytes);
                } catch (IOException e) {
                    return null;
                }
            });
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS) || process.exitValue() != 0) {
                return null;
            }
            byte[] bytes = output.get(2, TimeUnit.SECONDS);
            return bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException | ExecutionException | TimeoutException e) {
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
