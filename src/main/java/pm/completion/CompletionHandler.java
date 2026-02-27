package pm.completion;

import pm.core.Project;
import pm.detector.ProjectType;
import pm.storage.ProjectStore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles the hidden {@code --complete} callback for shell autocompletion.
 *
 * <p>Invoked as: {@code pm --complete -- <cword> <word0> <word1> ...}
 * <p>Outputs matching completions to stdout, one per line.
 * <p>Must NOT print banner, NOT check updates, NOT use ANSI colors.
 *
 * @author SoftDryzz
 * @version 1.6.0
 * @since 1.6.0
 */
public final class CompletionHandler {

    /** All top-level commands available in pm. */
    static final List<String> TOP_LEVEL_COMMANDS = List.of(
            "add", "list", "ls", "build", "run", "test", "scan",
            "commands", "cmd", "remove", "rm", "rename", "info",
            "env", "hooks", "refresh", "update", "doctor", "secure",
            "audit", "ci", "lint", "fmt", "modules", "migrate", "export", "import", "help", "version", "completions"
    );

    /** Commands that take a project name as their next argument. */
    private static final Set<String> PROJECT_COMMANDS = Set.of(
            "scan", "remove", "rm", "rename", "info", "ci", "lint", "fmt", "modules", "migrate"
    );

    /** Commands that take a project name OR --all flag. */
    private static final Set<String> PROJECT_OR_ALL_COMMANDS = Set.of(
            "build", "run", "test", "commands", "cmd", "hooks", "refresh"
    );

    /** Env subcommands. */
    static final List<String> ENV_SUBCOMMANDS = List.of(
            "set", "get", "list", "remove", "clear", "files", "show", "switch"
    );

    /** Subcommands for commands/hooks management. */
    static final List<String> MANAGEMENT_SUBCOMMANDS = List.of(
            "add", "remove"
    );

    /** Supported shell names for completions command. */
    static final List<String> SHELL_NAMES = List.of(
            "bash", "zsh", "fish", "powershell"
    );

    /** Standard hook slot prefixes. */
    private static final List<String> HOOK_SLOT_PREFIXES = List.of(
            "pre-build", "post-build", "pre-run", "post-run",
            "pre-test", "post-test", "pre-clean", "post-clean",
            "pre-stop", "post-stop"
    );

    private CompletionHandler() {}

    /**
     * Entry point for completion handling.
     *
     * @param args full args array starting with "--complete"
     *             Format: --complete -- cword word0 word1 ...
     */
    public static void handle(String[] args) {
        try {
            List<String> completions = parseAndComplete(args);
            completions.forEach(System.out::println);
        } catch (Exception e) {
            // Never crash during completion — silently return nothing
        }
    }

    /**
     * Parses the --complete arguments and computes completions.
     * Package-private for testing.
     */
    static List<String> parseAndComplete(String[] args) {
        // Find the "--" separator
        int dashDashIndex = -1;
        for (int i = 1; i < args.length; i++) {
            if ("--".equals(args[i])) {
                dashDashIndex = i;
                break;
            }
        }
        if (dashDashIndex == -1 || dashDashIndex + 1 >= args.length) {
            return List.of();
        }

        int cword;
        try {
            cword = Integer.parseInt(args[dashDashIndex + 1]);
        } catch (NumberFormatException e) {
            return List.of();
        }

        // Extract COMP_WORDS: everything after the cword index
        String[] words = Arrays.copyOfRange(args, dashDashIndex + 2, args.length);

        // Current word being completed
        String current = (cword < words.length) ? words[cword] : "";

        List<String> candidates = computeCompletions(words, cword);

        // Filter by prefix
        String prefix = current.toLowerCase();
        return candidates.stream()
                .filter(c -> c.toLowerCase().startsWith(prefix))
                .sorted()
                .toList();
    }

    /**
     * Core completion state machine.
     * Determines candidates based on word position and context.
     */
    static List<String> computeCompletions(String[] words, int cword) {
        // cword 0 = "pm" itself, no completions
        if (cword <= 0) {
            return List.of();
        }

        // cword 1: completing the command (pm <TAB>)
        if (cword == 1) {
            return new ArrayList<>(TOP_LEVEL_COMMANDS);
        }

        String cmd = words[1].toLowerCase();

        // cword 2: completing after the command (pm build <TAB>)
        if (cword == 2) {
            return completionsForPosition2(cmd);
        }

        // cword 3: completing third position (pm env set <TAB>)
        if (cword == 3) {
            String arg2 = words.length > 2 ? words[2] : "";
            return completionsForPosition3(cmd, arg2);
        }

        // cword 4+: deeper completions
        if (cword >= 4) {
            String arg2 = words.length > 2 ? words[2] : "";
            String arg3 = words.length > 3 ? words[3] : "";
            return completionsForPosition4Plus(cmd, arg2, arg3, words, cword);
        }

        return List.of();
    }

    private static List<String> completionsForPosition2(String cmd) {
        if (PROJECT_COMMANDS.contains(cmd)) {
            return getProjectNames();
        }
        if (PROJECT_OR_ALL_COMMANDS.contains(cmd)) {
            List<String> result = new ArrayList<>(getProjectNames());
            result.add("--all");
            return result;
        }
        if ("export".equals(cmd)) {
            List<String> result = new ArrayList<>(getProjectNames());
            result.add("--file");
            return result;
        }
        return switch (cmd) {
            case "env" -> new ArrayList<>(ENV_SUBCOMMANDS);
            case "completions" -> new ArrayList<>(SHELL_NAMES);
            case "doctor" -> new ArrayList<>(List.of("--score"));
            case "secure" -> new ArrayList<>(List.of("--fix"));
            default -> List.of();
        };
    }

    private static List<String> completionsForPosition3(String cmd, String arg2) {
        // pm env <subcmd> <TAB> → project names
        if ("env".equals(cmd) && ENV_SUBCOMMANDS.contains(arg2.toLowerCase())) {
            return getProjectNames();
        }
        // pm commands <project> <TAB> → add, remove
        if ("commands".equals(cmd) || "cmd".equals(cmd)) {
            return new ArrayList<>(MANAGEMENT_SUBCOMMANDS);
        }
        // pm hooks <project> <TAB> → add, remove
        if ("hooks".equals(cmd)) {
            return new ArrayList<>(MANAGEMENT_SUBCOMMANDS);
        }
        // pm migrate <project> <TAB> → status
        if ("migrate".equals(cmd)) {
            return new ArrayList<>(List.of("status"));
        }
        // pm export <name> <TAB> → more project names + --file
        if ("export".equals(cmd)) {
            List<String> result = new ArrayList<>(getProjectNames());
            result.add("--file");
            return result;
        }
        // pm add <name> <TAB> → flags
        if ("add".equals(cmd)) {
            return List.of("--path", "--type", "--env");
        }
        return List.of();
    }

    private static List<String> completionsForPosition4Plus(String cmd, String arg2, String arg3, String[] words, int cword) {
        // pm hooks <project> add <TAB> → slot suggestions
        if ("hooks".equals(cmd) && "add".equals(arg3.toLowerCase())) {
            List<String> slots = new ArrayList<>(HOOK_SLOT_PREFIXES);
            // Also add slots for custom commands
            List<String> customCmdSlots = getProjectCommandSlots(arg2);
            slots.addAll(customCmdSlots);
            return slots;
        }
        // pm env get <project> <TAB> → env var keys
        // pm env <subcmd> <project> <KEY> → env var keys
        if ("env".equals(cmd) && cword == 4) {
            String envSubcmd = arg2.toLowerCase();
            if ("get".equals(envSubcmd) || "remove".equals(envSubcmd)) {
                return getProjectEnvVarKeys(arg3);
            }
        }
        // pm add <name> --path <TAB> → let shell handle filesystem
        // pm add <name> --type <TAB> → project type names
        if ("add".equals(cmd)) {
            String prevWord = (cword > 0 && cword - 1 < words.length) ? words[cword - 1] : "";
            if ("--type".equals(prevWord)) {
                return getProjectTypeNames();
            }
            if ("--path".equals(prevWord)) {
                return List.of(); // let shell handle filesystem
            }
            // Might still need more flags
            return List.of("--path", "--type", "--env");
        }
        return List.of();
    }

    // ============================================================
    // DATA ACCESS (lazy, error-safe)
    // ============================================================

    static List<String> getProjectNames() {
        try {
            ProjectStore store = new ProjectStore();
            Map<String, Project> projects = store.load();
            return new ArrayList<>(projects.keySet());
        } catch (IOException e) {
            return List.of();
        }
    }

    private static List<String> getProjectCommandSlots(String projectName) {
        try {
            ProjectStore store = new ProjectStore();
            Project project = store.findProject(projectName);
            if (project == null) return List.of();
            List<String> slots = new ArrayList<>();
            for (String cmd : project.commands().keySet()) {
                slots.add("pre-" + cmd);
                slots.add("post-" + cmd);
            }
            return slots;
        } catch (IOException e) {
            return List.of();
        }
    }

    private static List<String> getProjectEnvVarKeys(String projectName) {
        try {
            ProjectStore store = new ProjectStore();
            Project project = store.findProject(projectName);
            if (project == null) return List.of();
            return new ArrayList<>(project.envVars().keySet());
        } catch (IOException e) {
            return List.of();
        }
    }

    private static List<String> getProjectTypeNames() {
        List<String> types = new ArrayList<>();
        for (ProjectType type : ProjectType.values()) {
            if (type != ProjectType.UNKNOWN) {
                types.add(type.name().toLowerCase());
            }
        }
        return types;
    }
}
