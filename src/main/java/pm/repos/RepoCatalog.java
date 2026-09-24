package pm.repos;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Crosses GitHub repos, local clones and registered pm projects into ordered
 * groups. Pure logic: no network, no disk.
 *
 * @author SoftDryzz
 * @since 2.1.0
 */
public final class RepoCatalog {

    private static final Comparator<CatalogEntry> ENTRY_ORDER = Comparator
            .comparing((CatalogEntry e) -> !e.cloned())
            .thenComparing(e -> e.remote() == null ? null : e.remote().pushedAt(),
                    Comparator.nullsLast(Comparator.<Instant>reverseOrder()))
            .thenComparing(e -> e.name().toLowerCase(Locale.ROOT));

    private RepoCatalog() {
    }

    /**
     * @param login           authenticated login, or the {@code --user} login in public mode; may be null
     * @param remotes         repos from GitHub (empty when GitHub was not reached)
     * @param clones          clones found on disk
     * @param pmProjects      registered pm projects: name to path
     * @param includeArchived show archived repos that are not cloned
     * @param publicUserMode  {@code --user}: every remote repo goes in one PUBLIC_USER group
     * @return non-empty groups in display order
     */
    public static List<RepoGroup> build(String login, List<RemoteRepo> remotes, List<LocalClone> clones,
                                        Map<String, Path> pmProjects, boolean includeArchived,
                                        boolean publicUserMode) {
        Map<String, List<LocalClone>> clonesByKey = new HashMap<>();
        for (LocalClone clone : clones) {
            if (clone.githubKey() != null) {
                clonesByKey.computeIfAbsent(clone.githubKey(), k -> new ArrayList<>()).add(clone);
            }
        }
        Map<String, String> projectByPath = new HashMap<>();
        pmProjects.forEach((name, path) -> projectByPath.putIfAbsent(PathKey.of(path), name));

        Set<String> remoteKeys = new HashSet<>();
        List<CatalogEntry> own = new ArrayList<>();
        List<CatalogEntry> collaborations = new ArrayList<>();
        List<CatalogEntry> publicUser = new ArrayList<>();
        Map<String, List<CatalogEntry>> organizations = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (RemoteRepo remote : remotes) {
            remoteKeys.add(remote.key());
            List<LocalClone> repoClones = sortedByPath(clonesByKey.getOrDefault(remote.key(), List.of()));
            if (remote.archived() && !includeArchived && repoClones.isEmpty()) {
                continue;
            }
            CatalogEntry entry = new CatalogEntry(remote, repoClones, projectFor(repoClones, projectByPath));
            if (publicUserMode) {
                publicUser.add(entry);
            } else if (login != null && remote.owner().equalsIgnoreCase(login)) {
                own.add(entry);
            } else if (remote.ownerIsOrganization()) {
                organizations.computeIfAbsent(remote.owner(), k -> new ArrayList<>()).add(entry);
            } else {
                collaborations.add(entry);
            }
        }

        List<CatalogEntry> localOnly = new ArrayList<>();
        Map<String, List<LocalClone>> localByKey = new HashMap<>();
        List<LocalClone> noKey = new ArrayList<>();

        for (LocalClone clone : clones) {
            if (clone.githubKey() == null) {
                noKey.add(clone);
            } else if (!remoteKeys.contains(clone.githubKey())) {
                localByKey.computeIfAbsent(clone.githubKey(), k -> new ArrayList<>()).add(clone);
            }
        }

        // Group clones with the same githubKey into one entry
        for (List<LocalClone> groupClones : localByKey.values()) {
            List<LocalClone> sorted = sortedByPath(groupClones);
            localOnly.add(new CatalogEntry(null, sorted, projectFor(sorted, projectByPath)));
        }

        // Each clone with no key gets its own entry
        for (LocalClone clone : noKey) {
            localOnly.add(new CatalogEntry(null, List.of(clone), projectFor(List.of(clone), projectByPath)));
        }

        List<RepoGroup> groups = new ArrayList<>();
        addGroup(groups, RepoGroup.Kind.OWN, login, own);
        organizations.forEach((owner, entries) -> addGroup(groups, RepoGroup.Kind.ORGANIZATION, owner, entries));
        addGroup(groups, RepoGroup.Kind.COLLABORATIONS, null, collaborations);
        addGroup(groups, RepoGroup.Kind.PUBLIC_USER, login, publicUser);
        localOnly.sort(Comparator.comparing(e -> PathKey.of(e.clones().get(0).path())));
        if (!localOnly.isEmpty()) {
            groups.add(new RepoGroup(RepoGroup.Kind.LOCAL_ONLY, null, List.copyOf(localOnly)));
        }
        return List.copyOf(groups);
    }

    /**
     * Entries matching the query, ignoring case: by name; with a slash, by
     * {@code owner/name} first. A query that looks like a path (contains a
     * separator or starts with {@code ~}) is then compared with the clone
     * paths. Pure matching against the catalog: the query is never read from
     * disk (spec S8).
     *
     * @param home folder that a leading {@code ~} stands for
     */
    public static List<CatalogEntry> find(List<RepoGroup> groups, String query, Path home) {
        String q = query.strip();
        List<CatalogEntry> entries = groups.stream().flatMap(g -> g.entries().stream()).toList();
        boolean slash = q.contains("/");
        List<CatalogEntry> matches = entries.stream()
                .filter(e -> slash
                        ? e.remote() != null && e.remote().fullName().equalsIgnoreCase(q)
                        : e.name().equalsIgnoreCase(q))
                .toList();
        if (!matches.isEmpty() || !looksLikePath(q)) {
            return matches;
        }
        String key = pathKey(q, home);
        if (key == null) {
            return List.of();
        }
        return entries.stream()
                .filter(e -> e.clones().stream().anyMatch(c -> PathKey.of(c.path()).equals(key)))
                .toList();
    }

    private static boolean looksLikePath(String query) {
        return query.contains("/") || query.contains("\\") || query.startsWith("~");
    }

    private static String pathKey(String query, Path home) {
        try {
            Path path;
            if (query.equals("~")) {
                path = home;
            } else if (query.startsWith("~/") || query.startsWith("~\\")) {
                path = home.resolve(query.substring(2));
            } else {
                path = Path.of(query);
            }
            return PathKey.of(path);
        } catch (InvalidPathException e) {
            return null;
        }
    }

    /** Up to five entries whose name contains the query, ignoring case. */
    public static List<String> suggest(List<RepoGroup> groups, String query) {
        String q = query.strip().toLowerCase(Locale.ROOT);
        return groups.stream().flatMap(g -> g.entries().stream())
                .filter(e -> e.name().toLowerCase(Locale.ROOT).contains(q))
                .map(CatalogEntry::displayId)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .limit(5)
                .toList();
    }

    private static void addGroup(List<RepoGroup> groups, RepoGroup.Kind kind, String owner, List<CatalogEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        List<CatalogEntry> sorted = new ArrayList<>(entries);
        sorted.sort(ENTRY_ORDER);
        groups.add(new RepoGroup(kind, owner, List.copyOf(sorted)));
    }

    private static List<LocalClone> sortedByPath(List<LocalClone> clones) {
        List<LocalClone> sorted = new ArrayList<>(clones);
        sorted.sort(Comparator.comparing(c -> PathKey.of(c.path())));
        return List.copyOf(sorted);
    }

    private static String projectFor(List<LocalClone> clones, Map<String, String> projectByPath) {
        for (LocalClone clone : clones) {
            String name = projectByPath.get(PathKey.of(clone.path()));
            if (name != null) {
                return name;
            }
        }
        return null;
    }
}
