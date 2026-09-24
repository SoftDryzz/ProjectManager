package pm.repos;

import pm.cli.OutputFormatter;

import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The {@code pm repos} command. Returns an exit code instead of calling
 * {@code System.exit}, so the whole flow can be tested.
 *
 * @author SoftDryzz
 * @version 2.1.0
 * @since 2.1.0
 */
public final class ReposCommand {

    private static final String USAGE = """
            Usage: pm repos [name] [--archived]
                   pm repos --user <login>
            """;

    private final Function<Token, GitHubClient> clientFactory;
    private final Supplier<Optional<Token>> tokenSource;
    private final RepoRoots roots;
    private final Map<String, Path> pmProjects;
    private final LocalRepoScanner scanner;
    private final LocalGitInfo gitInfo;
    private final PrintStream out;
    private final RepoPrinter printer;
    private final Path home;
    private final Clock clock;

    public ReposCommand(Function<Token, GitHubClient> clientFactory, Supplier<Optional<Token>> tokenSource,
                        RepoRoots roots, Map<String, Path> pmProjects, LocalRepoScanner scanner,
                        LocalGitInfo gitInfo, PrintStream out, Path home, Clock clock) {
        this.clientFactory = clientFactory;
        this.tokenSource = tokenSource;
        this.roots = roots;
        this.pmProjects = pmProjects;
        this.scanner = scanner;
        this.gitInfo = gitInfo;
        this.out = out;
        this.printer = new RepoPrinter(out, home);
        this.home = home.toAbsolutePath().normalize();
        this.clock = clock;
    }

    /** Command wired to this machine. */
    public static ReposCommand standard(Map<String, Path> pmProjects) {
        return new ReposCommand(token -> new GitHubClient(GitHubClient.DEFAULT_BASE_URL, token),
                () -> GitHubToken.fromSystem().resolve(), RepoRoots.standard(), pmProjects,
                new LocalRepoScanner(), LocalGitInfo.fromSystem(), System.out,
                Path.of(System.getProperty("user.home")), Clock.systemDefaultZone());
    }

    private record Options(String name, String user, boolean archived) {
    }

    /** @param args arguments after {@code repos} */
    public int run(String[] args) {
        Options options = parse(args);
        if (options == null) {
            out.print(USAGE);
            return 1;
        }
        if (options.user() != null && !GitHubClient.isValidLogin(options.user())) {
            error("Invalid GitHub login: " + Sanitizer.clean(options.user()));
            return 1;
        }

        List<Path> configuredRoots = roots.load();
        ScanResult scan;
        try {
            scan = scanner.scan(configuredRoots, pmProjects.values());
        } catch (RuntimeException e) {
            warn("Scan failed: " + Sanitizer.clean(String.valueOf(e.getMessage())));
            scan = new ScanResult(List.of(), List.of());
        }
        for (Path missing : scan.missingRoots()) {
            warn("Folder not found: " + Sanitizer.clean(missing.toString()));
        }

        boolean publicMode = options.user() != null;
        String login = options.user();
        List<RemoteRepo> remotes = List.of();
        GitHubClient client = null;

        Optional<Token> token = tokenSource.get();
        if (!publicMode && token.isEmpty()) {
            warn("Not signed in to GitHub: showing local clones only.");
            out.println("  To see your repositories: gh auth login  (or set GH_TOKEN)");
            out.println("  Public repositories of an account: pm repos --user <login>");
        } else {
            try {
                client = clientFactory.apply(token.orElse(null));
                Listing listing;
                if (publicMode) {
                    listing = client.publicRepos(login);
                } else {
                    login = client.currentLogin();
                    listing = client.myRepos();
                }
                remotes = listing.repos();
                if (listing.truncated()) {
                    warn("Showing the first " + remotes.size() + " repositories (listing limit reached).");
                }
                if (listing.ssoHiddenOrganizations() > 0) {
                    warn(listing.ssoHiddenOrganizations() + " organization(s) hidden: authorize your token for SSO"
                            + " (gh auth refresh).");
                }
            } catch (GitHubException e) {
                reportGitHubProblem(e, publicMode, login);
            } catch (RuntimeException e) {
                // never a stack trace: keep going with what we have
                remotes = List.of();
                warn("GitHub error: " + Sanitizer.clean(String.valueOf(e.getMessage())) + " Showing local clones only.");
            }
        }

        if (configuredRoots.isEmpty() && options.name() == null) {
            out.println(OutputFormatter.GRAY + "  Tip: pm config repos add <folder> to find clones outside registered projects."
                    + OutputFormatter.RESET);
        }

        boolean detail = options.name() != null;
        List<RepoGroup> groups = RepoCatalog.build(login, remotes, scan.clones(), pmProjects,
                options.archived() || detail, publicMode);
        if (!detail) {
            printer.printList(groups);
            return 0;
        }
        return printDetail(groups, options.name(), client, publicMode);
    }

    private int printDetail(List<RepoGroup> groups, String name, GitHubClient client, boolean publicMode) {
        List<CatalogEntry> matches = RepoCatalog.find(groups, name, home);
        if (matches.isEmpty()) {
            error("Repository not found: " + Sanitizer.clean(name));
            List<String> suggestions = RepoCatalog.suggest(groups, name);
            if (!suggestions.isEmpty()) {
                out.println("  Similar: " + Sanitizer.clean(String.join(", ", suggestions)));
            }
            return 1;
        }
        if (matches.size() > 1) {
            error("Several repositories are called " + Sanitizer.clean(name) + ":");
            CatalogEntry remoteExample = null;
            CatalogEntry localExample = null;
            for (CatalogEntry entry : matches) {
                if (entry.isLocalOnly()) {
                    out.println("    " + printer.shortPath(entry.clones().get(0).path()));
                    localExample = localExample == null ? entry : localExample;
                } else {
                    out.println("    " + Sanitizer.clean(entry.displayId()));
                    remoteExample = remoteExample == null ? entry : remoteExample;
                }
            }
            if (remoteExample != null) {
                out.println("  Use owner/name, e.g. pm repos " + Sanitizer.clean(remoteExample.displayId()));
            }
            if (localExample != null) {
                out.println("  Local-only folders can be opened by path, e.g. pm repos \""
                        + printer.shortPath(localExample.clones().get(0).path()) + "\"");
            }
            return 1;
        }

        CatalogEntry entry = matches.get(0);
        List<Collaborator> collaborators = null;
        String note = null;
        // An entry has a remote only when the listing succeeded, so client is set here
        if (entry.remote() != null) {
            if (publicMode) {
                note = "not available with --user";
            } else {
                try {
                    collaborators = client.collaborators(entry.remote().owner(), entry.remote().name());
                } catch (GitHubException e) {
                    note = (e.kind() == GitHubException.Kind.FORBIDDEN || e.kind() == GitHubException.Kind.NOT_FOUND)
                            ? "not visible (requires push access)"
                            : "unavailable (" + e.getMessage() + ")";
                } catch (RuntimeException e) {
                    note = "unavailable";
                }
            }
        }
        Map<Path, CloneStatus> statuses = new LinkedHashMap<>();
        for (LocalClone clone : entry.clones()) {
            statuses.put(clone.path(), gitInfo.read(clone.path()));
        }
        printer.printDetail(entry, collaborators, note, statuses, clock.instant());
        return 0;
    }

    private void reportGitHubProblem(GitHubException e, boolean publicMode, String login) {
        String suffix = " Showing local clones only.";
        switch (e.kind()) {
            case UNAUTHORIZED -> warn("GitHub rejected your token. Run: gh auth refresh (or set a new GH_TOKEN)." + suffix);
            case RATE_LIMITED -> warn("GitHub rate limit reached"
                    + e.retryAt().map(t -> " until " + LocalTime.ofInstant(t, clock.getZone())
                    .format(DateTimeFormatter.ofPattern("HH:mm"))).orElse("") + "." + suffix);
            case NETWORK -> warn("GitHub not reachable: " + Sanitizer.clean(e.getMessage()) + suffix);
            case NOT_FOUND -> warn(publicMode
                    ? "GitHub account not found: " + Sanitizer.clean(login) + "."
                    : "GitHub returned 'not found'." + suffix);
            default -> warn("GitHub error: " + Sanitizer.clean(e.getMessage()) + suffix);
        }
    }

    private static Options parse(String[] args) {
        String name = null;
        String user = null;
        boolean archived = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--archived")) {
                archived = true;
            } else if (arg.equals("--user")) {
                if (i + 1 >= args.length) {
                    return null;
                }
                user = args[++i];
            } else if (arg.startsWith("-") || name != null) {
                return null;
            } else {
                name = arg;
            }
        }
        return new Options(name, user, archived);
    }

    private void warn(String message) {
        out.println("  " + OutputFormatter.YELLOW + message + OutputFormatter.RESET);
    }

    private void error(String message) {
        out.println("  " + OutputFormatter.RED + message + OutputFormatter.RESET);
    }
}
