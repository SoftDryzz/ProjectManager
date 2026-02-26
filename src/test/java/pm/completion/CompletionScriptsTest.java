package pm.completion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompletionScripts")
class CompletionScriptsTest {

    // ============================================================
    // BASH
    // ============================================================

    @Test
    @DisplayName("bash script contains completion function")
    void bashContainsFunction() {
        String script = CompletionScripts.bash();

        assertTrue(script.contains("_pm_completions()"));
        assertTrue(script.contains("COMPREPLY"));
        assertTrue(script.contains("compgen"));
    }

    @Test
    @DisplayName("bash script contains complete command")
    void bashContainsCompleteCommand() {
        String script = CompletionScripts.bash();

        assertTrue(script.contains("complete"));
        assertTrue(script.contains("-F _pm_completions pm"));
    }

    @Test
    @DisplayName("bash script calls --complete callback")
    void bashCallsCallback() {
        String script = CompletionScripts.bash();

        assertTrue(script.contains("pm --complete --"));
        assertTrue(script.contains("COMP_CWORD"));
        assertTrue(script.contains("COMP_WORDS"));
    }

    @Test
    @DisplayName("bash script suppresses stderr")
    void bashSuppressesStderr() {
        String script = CompletionScripts.bash();

        assertTrue(script.contains("2>/dev/null"));
    }

    // ============================================================
    // ZSH
    // ============================================================

    @Test
    @DisplayName("zsh script contains completion function")
    void zshContainsFunction() {
        String script = CompletionScripts.zsh();

        assertTrue(script.contains("_pm()"));
        assertTrue(script.contains("compadd"));
    }

    @Test
    @DisplayName("zsh script contains compdef")
    void zshContainsCompdef() {
        String script = CompletionScripts.zsh();

        assertTrue(script.contains("compdef _pm pm"));
    }

    @Test
    @DisplayName("zsh script calls --complete callback")
    void zshCallsCallback() {
        String script = CompletionScripts.zsh();

        assertTrue(script.contains("pm --complete --"));
    }

    // ============================================================
    // FISH
    // ============================================================

    @Test
    @DisplayName("fish script contains completion function")
    void fishContainsFunction() {
        String script = CompletionScripts.fish();

        assertTrue(script.contains("__pm_completions"));
        assertTrue(script.contains("commandline"));
    }

    @Test
    @DisplayName("fish script registers complete command")
    void fishRegistersComplete() {
        String script = CompletionScripts.fish();

        assertTrue(script.contains("complete -c pm"));
    }

    @Test
    @DisplayName("fish script calls --complete callback")
    void fishCallsCallback() {
        String script = CompletionScripts.fish();

        assertTrue(script.contains("pm --complete --"));
    }

    // ============================================================
    // POWERSHELL
    // ============================================================

    @Test
    @DisplayName("powershell script registers argument completer")
    void powershellRegistersCompleter() {
        String script = CompletionScripts.powershell();

        assertTrue(script.contains("Register-ArgumentCompleter"));
        assertTrue(script.contains("-Native"));
        assertTrue(script.contains("-CommandName pm"));
    }

    @Test
    @DisplayName("powershell script calls --complete callback")
    void powershellCallsCallback() {
        String script = CompletionScripts.powershell();

        assertTrue(script.contains("'--complete'"));
    }

    @Test
    @DisplayName("powershell script returns CompletionResult objects")
    void powershellReturnsCompletionResult() {
        String script = CompletionScripts.powershell();

        assertTrue(script.contains("CompletionResult"));
    }

    // ============================================================
    // GENERAL
    // ============================================================

    @Test
    @DisplayName("all scripts are non-empty")
    void allScriptsNonEmpty() {
        assertFalse(CompletionScripts.bash().isBlank());
        assertFalse(CompletionScripts.zsh().isBlank());
        assertFalse(CompletionScripts.fish().isBlank());
        assertFalse(CompletionScripts.powershell().isBlank());
    }

    @Test
    @DisplayName("all scripts suppress stderr on callback")
    void allScriptsSuppressStderr() {
        assertTrue(CompletionScripts.bash().contains("2>/dev/null"));
        assertTrue(CompletionScripts.zsh().contains("2>/dev/null"));
        assertTrue(CompletionScripts.fish().contains("2>/dev/null"));
        assertTrue(CompletionScripts.powershell().contains("2>$null"));
    }
}
