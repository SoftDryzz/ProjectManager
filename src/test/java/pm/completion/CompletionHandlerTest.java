package pm.completion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompletionHandler")
class CompletionHandlerTest {

    // ============================================================
    // LEVEL 1: TOP-LEVEL COMMAND COMPLETIONS (pm <TAB>)
    // ============================================================

    @Test
    @DisplayName("cword 1 returns all top-level commands")
    void cword1ReturnsAllCommands() {
        String[] words = {"pm", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 1);

        assertTrue(result.contains("build"));
        assertTrue(result.contains("run"));
        assertTrue(result.contains("test"));
        assertTrue(result.contains("add"));
        assertTrue(result.contains("list"));
        assertTrue(result.contains("commands"));
        assertTrue(result.contains("env"));
        assertTrue(result.contains("hooks"));
        assertTrue(result.contains("completions"));
        assertTrue(result.contains("doctor"));
        assertTrue(result.contains("update"));
        assertTrue(result.contains("help"));
        assertTrue(result.contains("version"));
    }

    @Test
    @DisplayName("cword 1 with prefix filters commands")
    void cword1WithPrefixFilters() {
        // parseAndComplete does the filtering; computeCompletions returns all candidates
        String[] args = {"--complete", "--", "1", "pm", "b"};
        List<String> result = CompletionHandler.parseAndComplete(args);

        assertTrue(result.contains("build"));
        assertFalse(result.contains("run"));
        assertFalse(result.contains("add"));
    }

    @Test
    @DisplayName("cword 0 returns empty (completing 'pm' itself)")
    void cword0ReturnsEmpty() {
        String[] words = {"pm"};
        List<String> result = CompletionHandler.computeCompletions(words, 0);

        assertTrue(result.isEmpty());
    }

    // ============================================================
    // LEVEL 2: PROJECT NAME / SUBCOMMAND COMPLETIONS
    // ============================================================

    @Test
    @DisplayName("build command suggests project names")
    void buildSuggestsProjects() {
        String[] words = {"pm", "build", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);

        // Can't assert specific project names (depends on store), but should not crash
        assertNotNull(result);
    }

    @Test
    @DisplayName("run command suggests project names")
    void runSuggestsProjects() {
        String[] words = {"pm", "run", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);
        assertNotNull(result);
    }

    @Test
    @DisplayName("test command suggests project names")
    void testSuggestsProjects() {
        String[] words = {"pm", "test", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);
        assertNotNull(result);
    }

    @Test
    @DisplayName("remove command suggests project names")
    void removeSuggestsProjects() {
        String[] words = {"pm", "remove", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);
        assertNotNull(result);
    }

    @Test
    @DisplayName("info command suggests project names")
    void infoSuggestsProjects() {
        String[] words = {"pm", "info", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);
        assertNotNull(result);
    }

    @Test
    @DisplayName("commands command suggests project names and --all")
    void commandsSuggestsProjectsAndAll() {
        String[] words = {"pm", "commands", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);

        assertTrue(result.contains("--all"));
    }

    @Test
    @DisplayName("hooks command suggests project names and --all")
    void hooksSuggestsProjectsAndAll() {
        String[] words = {"pm", "hooks", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);

        assertTrue(result.contains("--all"));
    }

    @Test
    @DisplayName("refresh command suggests project names and --all")
    void refreshSuggestsProjectsAndAll() {
        String[] words = {"pm", "refresh", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);

        assertTrue(result.contains("--all"));
    }

    @Test
    @DisplayName("env command suggests subcommands")
    void envSuggestsSubcommands() {
        String[] words = {"pm", "env", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);

        assertEquals(CompletionHandler.ENV_SUBCOMMANDS, result);
    }

    @Test
    @DisplayName("completions command suggests shell names")
    void completionsSuggestsShellNames() {
        String[] words = {"pm", "completions", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);

        assertEquals(CompletionHandler.SHELL_NAMES, result);
    }

    @Test
    @DisplayName("add command returns empty (freeform name)")
    void addReturnsEmpty() {
        String[] words = {"pm", "add", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("unknown command returns empty")
    void unknownCommandReturnsEmpty() {
        String[] words = {"pm", "nonexistent", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);

        assertTrue(result.isEmpty());
    }

    // ============================================================
    // LEVEL 3: DEEPER COMPLETIONS
    // ============================================================

    @Test
    @DisplayName("env set suggests project names")
    void envSetSuggestsProjects() {
        String[] words = {"pm", "env", "set", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 3);

        // Returns project names (may be empty if no projects registered)
        assertNotNull(result);
    }

    @Test
    @DisplayName("env get suggests project names")
    void envGetSuggestsProjects() {
        String[] words = {"pm", "env", "get", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 3);
        assertNotNull(result);
    }

    @Test
    @DisplayName("commands <project> suggests add/remove")
    void commandsProjectSuggestsSubcommands() {
        String[] words = {"pm", "commands", "myproject", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 3);

        assertEquals(CompletionHandler.MANAGEMENT_SUBCOMMANDS, result);
    }

    @Test
    @DisplayName("hooks <project> suggests add/remove")
    void hooksProjectSuggestsSubcommands() {
        String[] words = {"pm", "hooks", "myproject", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 3);

        assertEquals(CompletionHandler.MANAGEMENT_SUBCOMMANDS, result);
    }

    @Test
    @DisplayName("add <name> suggests flags")
    void addNameSuggestsFlags() {
        String[] words = {"pm", "add", "myproject", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 3);

        assertTrue(result.contains("--path"));
        assertTrue(result.contains("--type"));
        assertTrue(result.contains("--env"));
    }

    // ============================================================
    // LEVEL 4+: DEEP COMPLETIONS
    // ============================================================

    @Test
    @DisplayName("hooks <project> add suggests slot names")
    void hooksAddSuggestsSlots() {
        String[] words = {"pm", "hooks", "myproject", "add", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 4);

        assertTrue(result.contains("pre-build"));
        assertTrue(result.contains("post-build"));
        assertTrue(result.contains("pre-run"));
        assertTrue(result.contains("post-run"));
        assertTrue(result.contains("pre-test"));
        assertTrue(result.contains("post-test"));
    }

    // ============================================================
    // PARSE AND COMPLETE (full pipeline)
    // ============================================================

    @Test
    @DisplayName("parseAndComplete with valid args returns completions")
    void parseAndCompleteValid() {
        String[] args = {"--complete", "--", "1", "pm", ""};
        List<String> result = CompletionHandler.parseAndComplete(args);

        assertFalse(result.isEmpty());
        assertTrue(result.contains("build"));
    }

    @Test
    @DisplayName("parseAndComplete with missing -- returns empty")
    void parseAndCompleteMissingSeparator() {
        String[] args = {"--complete", "1", "pm", ""};
        List<String> result = CompletionHandler.parseAndComplete(args);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("parseAndComplete with invalid cword returns empty")
    void parseAndCompleteInvalidCword() {
        String[] args = {"--complete", "--", "abc", "pm", ""};
        List<String> result = CompletionHandler.parseAndComplete(args);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("parseAndComplete with too few args returns empty")
    void parseAndCompleteTooFewArgs() {
        String[] args = {"--complete"};
        List<String> result = CompletionHandler.parseAndComplete(args);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("parseAndComplete filters by prefix")
    void parseAndCompleteFiltersByPrefix() {
        String[] args = {"--complete", "--", "1", "pm", "re"};
        List<String> result = CompletionHandler.parseAndComplete(args);

        assertTrue(result.contains("remove"));
        assertTrue(result.contains("rename"));
        assertTrue(result.contains("refresh"));
        assertFalse(result.contains("build"));
    }

    @Test
    @DisplayName("parseAndComplete is case-insensitive")
    void parseAndCompleteCaseInsensitive() {
        String[] args = {"--complete", "--", "1", "pm", "BU"};
        List<String> result = CompletionHandler.parseAndComplete(args);

        assertTrue(result.contains("build"));
    }

    // ============================================================
    // HANDLE (no crash guarantee)
    // ============================================================

    @Test
    @DisplayName("handle does not crash on empty args")
    void handleNoCrashEmpty() {
        assertDoesNotThrow(() -> CompletionHandler.handle(new String[]{"--complete"}));
    }

    @Test
    @DisplayName("handle does not crash on malformed args")
    void handleNoCrashMalformed() {
        assertDoesNotThrow(() -> CompletionHandler.handle(new String[]{"--complete", "garbage"}));
    }

    @Test
    @DisplayName("handle does not crash on valid args")
    void handleNoCrashValid() {
        assertDoesNotThrow(() -> CompletionHandler.handle(
                new String[]{"--complete", "--", "1", "pm", ""}));
    }

    // ============================================================
    // CONFIG COMMAND COMPLETIONS
    // ============================================================

    @Test
    @DisplayName("config is in top-level commands")
    void configInTopLevel() {
        assertTrue(CompletionHandler.TOP_LEVEL_COMMANDS.contains("config"));
    }

    @Test
    @DisplayName("config command suggests telemetry subcommand")
    void configSuggestsTelemetry() {
        String[] words = {"pm", "config", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);
        assertTrue(result.contains("telemetry"));
    }

    @Test
    @DisplayName("config telemetry suggests on/off")
    void configTelemetrySuggestsOnOff() {
        String[] words = {"pm", "config", "telemetry", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 3);
        assertTrue(result.contains("on"));
        assertTrue(result.contains("off"));
    }

    // ============================================================
    // LICENSE COMPLETIONS
    // ============================================================

    @Test
    @DisplayName("license is in top-level commands")
    void licenseInTopLevel() {
        assertTrue(CompletionHandler.TOP_LEVEL_COMMANDS.contains("license"));
    }

    @Test
    @DisplayName("license command suggests subcommands")
    void licenseSuggestsSubcommands() {
        String[] words = {"pm", "license", ""};
        List<String> result = CompletionHandler.computeCompletions(words, 2);
        assertTrue(result.contains("info"));
        assertTrue(result.contains("activate"));
        assertTrue(result.contains("deactivate"));
    }
}
