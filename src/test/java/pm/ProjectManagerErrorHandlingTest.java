package pm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectManager - Error Handling")
class ProjectManagerErrorHandlingTest {

    /**
     * Access the private describeIOError method via reflection for testing.
     */
    private String callDescribeIOError(IOException e) throws Exception {
        Method method = ProjectManager.class.getDeclaredMethod("describeIOError", IOException.class);
        method.setAccessible(true);
        return (String) method.invoke(null, e);
    }

    // ============================================================
    // describeIOError — maps IOException to user-friendly messages
    // ============================================================

    @Test
    @DisplayName("describeIOError identifies corrupted JSON messages")
    void describeIOErrorCorrupted() throws Exception {
        IOException e = new IOException("projects.json is corrupted and no backup was found.");
        String result = callDescribeIOError(e);

        assertTrue(result.contains("corrupted"));
    }

    @Test
    @DisplayName("describeIOError identifies disk full")
    void describeIOErrorDiskFull() throws Exception {
        IOException e = new IOException("No space left on device");
        String result = callDescribeIOError(e);

        assertTrue(result.contains("Disk is full"));
    }

    @Test
    @DisplayName("describeIOError identifies disk full (Windows)")
    void describeIOErrorDiskFullWindows() throws Exception {
        IOException e = new IOException("Not enough space on the disk");
        String result = callDescribeIOError(e);

        assertTrue(result.contains("Disk is full"));
    }

    @Test
    @DisplayName("describeIOError identifies permission denied")
    void describeIOErrorPermission() throws Exception {
        IOException e = new IOException("Permission denied: /home/user/.projectmanager/projects.json");
        String result = callDescribeIOError(e);

        assertTrue(result.contains("Permission denied"));
    }

    @Test
    @DisplayName("describeIOError identifies access denied (Windows)")
    void describeIOErrorAccessDenied() throws Exception {
        IOException e = new IOException("Access denied to file");
        String result = callDescribeIOError(e);

        assertTrue(result.contains("Permission denied"));
    }

    @Test
    @DisplayName("describeIOError identifies file not found")
    void describeIOErrorFileNotFound() throws Exception {
        IOException e = new IOException("No such file or directory");
        String result = callDescribeIOError(e);

        assertTrue(result.contains("File not found"));
    }

    @Test
    @DisplayName("describeIOError identifies cannot find file (Windows)")
    void describeIOErrorCannotFind() throws Exception {
        IOException e = new IOException("The system cannot find the path specified");
        String result = callDescribeIOError(e);

        assertTrue(result.contains("File not found"));
    }

    @Test
    @DisplayName("describeIOError returns generic message for unknown errors")
    void describeIOErrorGeneric() throws Exception {
        IOException e = new IOException("Something unexpected happened");
        String result = callDescribeIOError(e);

        assertTrue(result.contains("Failed"));
        assertTrue(result.contains("Something unexpected happened"));
    }

    @Test
    @DisplayName("describeIOError handles null message")
    void describeIOErrorNullMessage() throws Exception {
        IOException e = new IOException((String) null);
        String result = callDescribeIOError(e);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ============================================================
    // containsShellMetacharacters — detects shell special chars (v1.3.8)
    // ============================================================

    private boolean callContainsShellMetacharacters(String command) throws Exception {
        Method method = ProjectManager.class.getDeclaredMethod("containsShellMetacharacters", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, command);
    }

    private String callGetFoundMetacharacters(String command) throws Exception {
        Method method = ProjectManager.class.getDeclaredMethod("getFoundMetacharacters", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, command);
    }

    @Test
    @DisplayName("detects ampersand in command")
    void detectsAmpersand() throws Exception {
        assertTrue(callContainsShellMetacharacters("npm build && npm serve"));
    }

    @Test
    @DisplayName("detects pipe in command")
    void detectsPipe() throws Exception {
        assertTrue(callContainsShellMetacharacters("cat log.txt | grep error"));
    }

    @Test
    @DisplayName("detects semicolon in command")
    void detectsSemicolon() throws Exception {
        assertTrue(callContainsShellMetacharacters("echo hello; echo world"));
    }

    @Test
    @DisplayName("detects dollar sign in command")
    void detectsDollarSign() throws Exception {
        assertTrue(callContainsShellMetacharacters("echo $HOME"));
    }

    @Test
    @DisplayName("returns false for safe command")
    void returnsFalseForSafeCommand() throws Exception {
        assertFalse(callContainsShellMetacharacters("gradle build"));
    }

    @Test
    @DisplayName("returns false for command with spaces and flags")
    void returnsFalseForFlagsAndSpaces() throws Exception {
        assertFalse(callContainsShellMetacharacters("npm run build --production"));
    }

    @Test
    @DisplayName("getFoundMetacharacters lists found characters")
    void listsFoundMetacharacters() throws Exception {
        String found = callGetFoundMetacharacters("echo $HOME && rm -rf /tmp");
        assertTrue(found.contains("'&'"));
        assertTrue(found.contains("'$'"));
    }

    @Test
    @DisplayName("getFoundMetacharacters returns empty for safe command")
    void emptyForSafeCommand() throws Exception {
        String found = callGetFoundMetacharacters("gradle build");
        assertEquals("", found);
    }
}
