package pm.executor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

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
}
