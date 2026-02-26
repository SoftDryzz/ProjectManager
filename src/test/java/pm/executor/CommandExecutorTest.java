package pm.executor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommandExecutor")
class CommandExecutorTest {

    @TempDir
    Path tempDir;

    private final CommandExecutor executor = new CommandExecutor();

    // ============================================================
    // SUCCESSFUL EXECUTION
    // ============================================================

    @Test
    @DisplayName("Executes simple echo command successfully")
    void executesEchoCommand() throws IOException, InterruptedException {
        CommandExecutor.ExecutionResult result = executor.execute("echo hello", tempDir, 10);

        assertTrue(result.success());
        assertEquals(0, result.exitCode());
        assertTrue(result.durationMs() >= 0);
        assertEquals("Command completed successfully", result.message());
    }

    @Test
    @DisplayName("Executes command without timeout")
    void executesWithoutTimeout() throws IOException, InterruptedException {
        CommandExecutor.ExecutionResult result = executor.execute("echo test", tempDir);

        assertTrue(result.success());
        assertEquals(0, result.exitCode());
    }

    // ============================================================
    // FAILED EXECUTION
    // ============================================================

    @Test
    @DisplayName("Returns failure for invalid command")
    void failsOnInvalidCommand() throws IOException, InterruptedException {
        CommandExecutor.ExecutionResult result =
                executor.execute("nonexistent_command_12345", tempDir, 5);

        assertFalse(result.success());
        assertNotEquals(0, result.exitCode());
        assertEquals("Command failed", result.message());
    }

    // ============================================================
    // VALIDATION
    // ============================================================

    @Test
    @DisplayName("Throws on null command")
    void throwsOnNullCommand() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.execute(null, tempDir, 10));
    }

    @Test
    @DisplayName("Throws on blank command")
    void throwsOnBlankCommand() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.execute("  ", tempDir, 10));
    }

    @Test
    @DisplayName("Throws on null working directory")
    void throwsOnNullDirectory() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.execute("echo test", null, 10));
    }

    // ============================================================
    // EXECUTION RESULT RECORD
    // ============================================================

    @Test
    @DisplayName("ExecutionResult durationSeconds calculates correctly")
    void durationSecondsWorks() {
        var result = new CommandExecutor.ExecutionResult(true, 0, 5500, "OK");
        assertEquals(5, result.durationSeconds());
    }

    @Test
    @DisplayName("ExecutionResult formattedDuration shows seconds")
    void formattedDurationSeconds() {
        var result = new CommandExecutor.ExecutionResult(true, 0, 30000, "OK");
        assertEquals("30s", result.formattedDuration());
    }

    @Test
    @DisplayName("ExecutionResult formattedDuration shows minutes and seconds")
    void formattedDurationMinutes() {
        var result = new CommandExecutor.ExecutionResult(true, 0, 90000, "OK");
        assertEquals("1m 30s", result.formattedDuration());
    }

    @Test
    @DisplayName("ExecutionResult formattedDuration shows 0s for instant")
    void formattedDurationZero() {
        var result = new CommandExecutor.ExecutionResult(true, 0, 0, "OK");
        assertEquals("0s", result.formattedDuration());
    }

    // ============================================================
    // INHERITED IO EXECUTION — executeWithInheritedIO()
    // ============================================================

    @Test
    @DisplayName("executeWithInheritedIO runs echo command successfully")
    void inheritedIOExecutesEcho() throws IOException, InterruptedException {
        CommandExecutor.ExecutionResult result =
                executor.executeWithInheritedIO("echo hello", tempDir, 10);

        assertTrue(result.success());
        assertEquals(0, result.exitCode());
        assertTrue(result.durationMs() >= 0);
        assertEquals("Command completed successfully", result.message());
    }

    @Test
    @DisplayName("executeWithInheritedIO returns failure for invalid command")
    void inheritedIOFailsOnInvalidCommand() throws IOException, InterruptedException {
        CommandExecutor.ExecutionResult result =
                executor.executeWithInheritedIO("nonexistent_command_12345", tempDir, 5);

        assertFalse(result.success());
        assertNotEquals(0, result.exitCode());
        assertEquals("Command failed", result.message());
    }

    @Test
    @DisplayName("executeWithInheritedIO throws on null command")
    void inheritedIOThrowsOnNullCommand() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.executeWithInheritedIO(null, tempDir, 10));
    }

    @Test
    @DisplayName("executeWithInheritedIO throws on blank command")
    void inheritedIOThrowsOnBlankCommand() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.executeWithInheritedIO("  ", tempDir, 10));
    }

    @Test
    @DisplayName("executeWithInheritedIO throws on null working directory")
    void inheritedIOThrowsOnNullDirectory() {
        assertThrows(IllegalArgumentException.class,
                () -> executor.executeWithInheritedIO("echo test", null, 10));
    }

    @Test
    @DisplayName("executeWithInheritedIO works with environment variables")
    void inheritedIOWorksWithEnvVars() throws IOException, InterruptedException {
        Map<String, String> envVars = Map.of("TEST_VAR", "hello");
        CommandExecutor.ExecutionResult result =
                executor.executeWithInheritedIO("echo test", tempDir, 10, envVars);

        assertTrue(result.success());
        assertEquals(0, result.exitCode());
    }

    @Test
    @DisplayName("executeWithInheritedIO works with null env vars")
    void inheritedIOWorksWithNullEnvVars() throws IOException, InterruptedException {
        CommandExecutor.ExecutionResult result =
                executor.executeWithInheritedIO("echo test", tempDir, 10, null);

        assertTrue(result.success());
        assertEquals(0, result.exitCode());
    }

    @Test
    @DisplayName("executeWithInheritedIO works with empty env vars")
    void inheritedIOWorksWithEmptyEnvVars() throws IOException, InterruptedException {
        CommandExecutor.ExecutionResult result =
                executor.executeWithInheritedIO("echo test", tempDir, 10, Map.of());

        assertTrue(result.success());
        assertEquals(0, result.exitCode());
    }

    // ============================================================
    // REGRESSION — existing execute() still works
    // ============================================================

    @Test
    @DisplayName("Original execute with env vars still works")
    void executeWithEnvVarsStillWorks() throws IOException, InterruptedException {
        Map<String, String> envVars = Map.of("MY_VAR", "value");
        CommandExecutor.ExecutionResult result =
                executor.execute("echo test", tempDir, 10, envVars);

        assertTrue(result.success());
        assertEquals(0, result.exitCode());
    }

    // ============================================================
    // WORKING DIRECTORY VALIDATION (v1.3.8)
    // ============================================================

    @Test
    @DisplayName("execute throws IOException for non-existent working directory")
    void throwsOnNonExistentDirectory() {
        Path nonExistent = tempDir.resolve("does-not-exist");
        IOException ex = assertThrows(IOException.class,
                () -> executor.execute("echo test", nonExistent, 10));
        assertTrue(ex.getMessage().contains("does not exist"),
                "Message should mention 'does not exist' but was: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("pm rename"),
                "Message should suggest 'pm rename' but was: " + ex.getMessage());
    }

    @Test
    @DisplayName("execute throws IOException when path is a file, not a directory")
    void throwsOnFileAsDirectory() throws IOException {
        Path file = tempDir.resolve("not-a-dir.txt");
        Files.writeString(file, "content");
        IOException ex = assertThrows(IOException.class,
                () -> executor.execute("echo test", file, 10));
        assertTrue(ex.getMessage().contains("not a directory"),
                "Message should mention 'not a directory' but was: " + ex.getMessage());
    }

    @Test
    @DisplayName("executeWithInheritedIO throws IOException for non-existent directory")
    void inheritedIOThrowsOnNonExistentDirectory() {
        Path nonExistent = tempDir.resolve("gone");
        IOException ex = assertThrows(IOException.class,
                () -> executor.executeWithInheritedIO("echo test", nonExistent, 10));
        assertTrue(ex.getMessage().contains("does not exist"),
                "Message should mention 'does not exist' but was: " + ex.getMessage());
    }

    @Test
    @DisplayName("execute with env vars throws IOException for non-existent directory")
    void executeWithEnvVarsThrowsOnNonExistentDirectory() {
        Path nonExistent = tempDir.resolve("nope");
        IOException ex = assertThrows(IOException.class,
                () -> executor.execute("echo test", nonExistent, 10, Map.of("K", "V")));
        assertTrue(ex.getMessage().contains("does not exist"),
                "Message should mention 'does not exist' but was: " + ex.getMessage());
    }

    @Test
    @DisplayName("Succeeds with directory containing spaces in name")
    void succeedsWithSpacesInPath() throws Exception {
        Path spacePath = tempDir.resolve("my project dir");
        Files.createDirectories(spacePath);
        CommandExecutor.ExecutionResult result = executor.execute("echo hello", spacePath, 10);
        assertTrue(result.success(), "Command should succeed in directory with spaces");
    }

    @Test
    @DisplayName("Succeeds with directory containing parentheses in name")
    void succeedsWithSpecialCharsInPath() throws Exception {
        Path specialPath = tempDir.resolve("project (v2)");
        Files.createDirectories(specialPath);
        CommandExecutor.ExecutionResult result = executor.execute("echo hello", specialPath, 10);
        assertTrue(result.success(), "Command should succeed in directory with parentheses");
    }
}
