package pm.repos;

import pm.cli.OutputFormatter;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Renders {@code pm repos} output. Every value goes through
 * {@link Sanitizer#clean} and remote URLs through {@link RemoteUrl#redact}
 * (spec S4, S5). Colors come from {@link OutputFormatter}, so they disappear
 * when output is redirected.
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class RepoPrinter {

    static final int NAME_WIDTH_CAP = 32;
    private static final int HEADER_WIDTH = 48;

    private final PrintStream out;
    private final Path home;

    public RepoPrinter(PrintStream out, Path home) {
        this.out = out;
        this.home = home.toAbsolutePath().normalize();
    }

    // ------------------------------------------------------------
    // List
    // ------------------------------------------------------------

    public void printList(List<RepoGroup> groups) {
        if (groups.isEmpty()) {
            out.println();
            out.println("  No repositories found.");
            out.println();
            return;
        }
        for (RepoGroup group : groups) {
            printGroup(group);
        }
        out.println(OutputFormatter.GRAY + "● cloned  ○ not cloned  [pm] registered in pm" + OutputFormatter.RESET);
        out.println();
    }

    private void printGroup(RepoGroup group) {
        String title = switch (group.kind()) {
            case OWN -> clean(group.owner()) + " (your account)";
            case ORGANIZATION -> clean(group.owner()) + " (organization)";
            case COLLABORATIONS -> "Collaborations";
            case PUBLIC_USER -> clean(group.owner()) + " (public repositories)";
            case LOCAL_ONLY -> "Local only";
        };
        int count = group.entries().size();
        String counts = count + " repo" + (count == 1 ? "" : "s");
        if (group.kind() != RepoGroup.Kind.LOCAL_ONLY) {
            counts += " · " + group.clonedCount() + " cloned";
        }
        out.println();
        out.println(OutputFormatter.BOLD + pad(title, HEADER_WIDTH) + OutputFormatter.RESET + "  " + counts);

        int width = Math.min(NAME_WIDTH_CAP,
                group.entries().stream().mapToInt(e -> clean(e.name()).length()).max().orElse(0));
        int ownerWidth = group.entries().stream().filter(e -> e.remote() != null)
                .mapToInt(e -> clean(e.remote().owner()).length()).max().orElse(0);
        for (CatalogEntry entry : group.entries()) {
            out.println(row(group.kind(), entry, width, ownerWidth));
        }
        out.println();
    }

    private String row(RepoGroup.Kind kind, CatalogEntry entry, int width, int ownerWidth) {
        StringBuilder line = new StringBuilder("  ");
        line.append(entry.cloned() ? OutputFormatter.GREEN + "●" : OutputFormatter.GRAY + "○")
                .append(OutputFormatter.RESET).append(' ')
                .append(pad(fit(clean(entry.name()), width), width)).append("  ");

        if (entry.isLocalOnly()) {
            LocalClone clone = entry.clones().get(0);
            line.append(pad(localOnlyLabel(clone), 18)).append(shortPath(clone.path()));
            if (entry.clones().size() > 1) {
                line.append(" (+").append(entry.clones().size() - 1).append(" more)");
            }
        } else {
            RemoteRepo remote = entry.remote();
            if (kind == RepoGroup.Kind.COLLABORATIONS) {
                line.append(pad(clean(remote.owner()), ownerWidth)).append("  ");
            }
            line.append(pad(remote.isPrivate() ? "private" : "public", 7)).append("  ");
            if (entry.cloned()) {
                line.append(shortPath(entry.clones().get(0).path()));
                if (entry.clones().size() > 1) {
                    line.append(" (+").append(entry.clones().size() - 1).append(" more)");
                }
            } else {
                line.append(OutputFormatter.GRAY).append("not cloned").append(OutputFormatter.RESET);
            }
            if (remote.fork()) {
                line.append("  fork");
            }
            if (remote.archived()) {
                line.append("  archived");
            }
        }
        if (entry.pmProject() != null) {
            line.append("  ").append(OutputFormatter.CYAN).append("[pm]").append(OutputFormatter.RESET);
        }
        return line.toString();
    }

    private static String localOnlyLabel(LocalClone clone) {
        if (clone.remoteUrl() == null) {
            return "no remote";
        }
        return clone.githubKey() == null ? "other remote" : "not in your repos";
    }

    // ------------------------------------------------------------
    // Detail
    // ------------------------------------------------------------

    /**
     * @param collaborators     collaborators to list, or null when not available
     * @param collaboratorsNote shown instead of the list when collaborators is null
     *                          (ignored for local-only entries)
     * @param statuses          working-copy state per clone path
     */
    public void printDetail(CatalogEntry entry, List<Collaborator> collaborators, String collaboratorsNote,
                            Map<Path, CloneStatus> statuses, Instant now) {
        out.println();
        if (entry.isLocalOnly()) {
            LocalClone clone = entry.clones().get(0);
            out.println(OutputFormatter.BOLD + clean(clone.folderName()) + OutputFormatter.RESET + "   local only");
            out.println("  Remote: " + (clone.remoteUrl() == null ? "none" : clean(RemoteUrl.redact(clone.remoteUrl()))));
        } else {
            RemoteRepo remote = entry.remote();
            String ownerLabel = clean(remote.owner()) + (remote.ownerIsOrganization() ? " (organization)" : "");
            out.println(OutputFormatter.BOLD + clean(remote.name()) + OutputFormatter.RESET + "   " + ownerLabel
                    + "   " + (remote.isPrivate() ? "private" : "public")
                    + (remote.archived() ? "   archived" : "") + (remote.fork() ? "   fork" : ""));
            if (remote.htmlUrl() != null) {
                out.println("  " + OutputFormatter.CYAN + clean(remote.htmlUrl()) + OutputFormatter.RESET);
            }
            if (remote.description() != null && !remote.description().isBlank()) {
                out.println("  " + clean(remote.description()));
            }
            out.println();
            out.println("  Default branch:  " + clean(orDash(remote.defaultBranch())));
            out.println("  Last push:       " + ago(remote.pushedAt(), now));
            if (remote.permission() != null) {
                out.println("  Your permission: " + clean(remote.permission()));
            }
            out.println();
            if (collaborators != null) {
                out.println("  Collaborators:");
                int width = collaborators.stream().mapToInt(c -> clean(c.login()).length()).max().orElse(0);
                for (Collaborator c : collaborators) {
                    out.println("    " + pad(clean(c.login()), width) + "   " + clean(c.role()));
                }
            } else {
                out.println("  Collaborators: " + clean(collaboratorsNote == null ? "unavailable" : collaboratorsNote));
            }
        }

        out.println();
        if (entry.clones().isEmpty()) {
            out.println("  Local clones: none");
        } else {
            out.println("  Local clones:");
            for (LocalClone clone : entry.clones()) {
                out.println("    " + shortPath(clone.path()) + "   " + describe(statuses.get(clone.path())));
            }
        }

        out.println();
        if (entry.pmProject() != null) {
            out.println("  pm project: " + clean(entry.pmProject()));
        } else if (entry.cloned()) {
            out.println("  pm project: not registered — pm add \"" + clean(entry.name())
                    + "\" --path \"" + clean(entry.clones().get(0).path().toString()) + "\"");
        } else {
            out.println("  pm project: not registered");
        }
        out.println();
    }

    private static String describe(CloneStatus status) {
        if (status == null) {
            return "status unknown";
        }
        StringBuilder text = new StringBuilder(status.branch() == null ? "unknown branch" : clean(status.branch()));
        if (status.changedFiles() == null) {
            text.append(", status unknown");
        } else if (status.changedFiles() == 0) {
            text.append(", clean");
        } else {
            text.append(", ").append(status.changedFiles()).append(" changed");
        }
        if (status.ahead() != null) {
            text.append(status.ahead() == 0 ? ", up to date" : ", " + status.ahead() + " unpushed");
        }
        return text.toString();
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    /** "2 days ago"; "unknown" for null. */
    static String ago(Instant then, Instant now) {
        if (then == null) {
            return "unknown";
        }
        long seconds = Math.max(0, Duration.between(then, now).getSeconds());
        if (seconds < 60) {
            return "just now";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return plural(minutes, "minute");
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return plural(hours, "hour");
        }
        long days = hours / 24;
        if (days < 30) {
            return plural(days, "day");
        }
        if (days < 365) {
            return plural(days / 30, "month");
        }
        return plural(days / 365, "year");
    }

    private static String plural(long n, String unit) {
        return n + " " + unit + (n == 1 ? "" : "s") + " ago";
    }

    /** Path for display, with the home folder shown as {@code ~}; sanitized. */
    String shortPath(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (normalized.startsWith(home)) {
            Path relative = home.relativize(normalized);
            return clean("~" + (relative.toString().isEmpty() ? "" : File.separator + relative));
        }
        return clean(normalized.toString());
    }

    private static String clean(String text) {
        return Sanitizer.clean(text);
    }

    private static String orDash(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }

    private static String fit(String text, int width) {
        return text.length() <= width ? text : text.substring(0, Math.max(0, width - 1)) + "…";
    }

    private static String pad(String text, int width) {
        return text.length() >= width ? text : text + " ".repeat(width - text.length());
    }
}
