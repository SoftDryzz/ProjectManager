package pm.completion;

/**
 * Generates shell-specific completion scripts for pm.
 *
 * <p>Each method returns a script that, when sourced by the user's shell,
 * enables TAB completion for the {@code pm} command. The scripts call
 * back into {@code pm --complete} to get dynamic completions.
 *
 * @author SoftDryzz
 * @version 1.6.0
 * @since 1.6.0
 */
public final class CompletionScripts {

    private CompletionScripts() {}

    /**
     * Returns the Bash completion script.
     *
     * <p>Setup: {@code eval "$(pm completions bash)"}
     */
    public static String bash() {
        return """
                _pm_completions() {
                    local completions
                    completions=$(pm --complete -- "$COMP_CWORD" "${COMP_WORDS[@]}" 2>/dev/null)
                    COMPREPLY=($(compgen -W "$completions" -- "${COMP_WORDS[COMP_CWORD]}"))
                }
                complete -o default -F _pm_completions pm
                """;
    }

    /**
     * Returns the Zsh completion script.
     *
     * <p>Setup: {@code eval "$(pm completions zsh)"}
     */
    public static String zsh() {
        return """
                _pm() {
                    local -a completions
                    completions=("${(@f)$(pm --complete -- $((CURRENT - 1)) "${words[@]}" 2>/dev/null)}")
                    compadd -a completions
                }
                compdef _pm pm
                """;
    }

    /**
     * Returns the Fish completion script.
     *
     * <p>Setup: {@code pm completions fish > ~/.config/fish/completions/pm.fish}
     */
    public static String fish() {
        return """
                function __pm_completions
                    set -l tokens (commandline -opc)
                    set -l current (commandline -ct)
                    set -l count (count $tokens)
                    pm --complete -- $count $tokens "$current" 2>/dev/null
                end
                complete -c pm -f -a '(__pm_completions)'
                """;
    }

    /**
     * Returns the PowerShell completion script.
     *
     * <p>Setup: {@code pm completions powershell | Out-String | Invoke-Expression}
     */
    public static String powershell() {
        return """
                Register-ArgumentCompleter -Native -CommandName pm -ScriptBlock {
                    param($wordToComplete, $commandAst, $cursorPosition)
                    $tokens = $commandAst.ToString().Split()
                    $cword = $tokens.Length
                    if ($wordToComplete -eq '') { $cword = $cword + 1 }
                    $args = @('--complete', '--', $cword) + $tokens
                    pm @args 2>$null | ForEach-Object {
                        [System.Management.Automation.CompletionResult]::new(
                            $_, $_, 'ParameterValue', $_
                        )
                    }
                }
                """;
    }
}
