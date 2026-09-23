package pm.repos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RepoCatalog")
class RepoCatalogTest {

    @TempDir
    Path tmp;

    private static RemoteRepo repo(String owner, boolean org, String name, String pushed, boolean archived) {
        return new RemoteRepo(owner, org, name, false, null, "main",
                pushed == null ? null : Instant.parse(pushed), archived, false,
                "https://github.com/" + owner + "/" + name, "admin");
    }

    private LocalClone clone(String folder, String owner, String name) {
        return new LocalClone(tmp.resolve(folder), "https://github.com/" + owner + "/" + name + ".git",
                owner == null ? null : RemoteUrl.key(owner, name));
    }

    private static List<RepoGroup.Kind> kinds(List<RepoGroup> groups) {
        return groups.stream().map(RepoGroup::kind).toList();
    }

    @Test
    @DisplayName("groups own, organization, collaboration and local-only repos in order")
    void groupsInOrder() {
        List<RemoteRepo> remotes = List.of(
                repo("zeta-org", true, "z", "2026-01-01T00:00:00Z", false),
                repo("other-user", false, "shared", "2026-01-01T00:00:00Z", false),
                repo("octo-user", false, "mine", "2026-01-01T00:00:00Z", false),
                repo("acme-org", true, "a", "2026-01-01T00:00:00Z", false));
        List<LocalClone> clones = List.of(new LocalClone(tmp.resolve("scratch"), null, null));

        List<RepoGroup> groups = RepoCatalog.build("octo-user", remotes, clones, Map.of(), false, false);

        assertEquals(List.of(RepoGroup.Kind.OWN, RepoGroup.Kind.ORGANIZATION, RepoGroup.Kind.ORGANIZATION,
                RepoGroup.Kind.COLLABORATIONS, RepoGroup.Kind.LOCAL_ONLY), kinds(groups));
        assertEquals("acme-org", groups.get(1).owner());
        assertEquals("zeta-org", groups.get(2).owner());
        assertEquals("scratch", groups.get(4).entries().get(0).name());
    }

    @Test
    @DisplayName("cloned repos first, then by most recent push")
    void orderWithinGroup() {
        List<RemoteRepo> remotes = List.of(
                repo("octo-user", false, "old-cloned", "2020-01-01T00:00:00Z", false),
                repo("octo-user", false, "new-remote", "2026-09-01T00:00:00Z", false),
                repo("octo-user", false, "newer-remote", "2026-09-10T00:00:00Z", false),
                repo("octo-user", false, "never-pushed", null, false));
        List<LocalClone> clones = List.of(clone("old-cloned", "octo-user", "old-cloned"));

        List<CatalogEntry> own = RepoCatalog.build("octo-user", remotes, clones, Map.of(), false, false).get(0).entries();

        assertEquals(List.of("old-cloned", "newer-remote", "new-remote", "never-pushed"),
                own.stream().map(CatalogEntry::name).toList());
        assertEquals(1, RepoCatalog.build("octo-user", remotes, clones, Map.of(), false, false).get(0).clonedCount());
    }

    @Test
    @DisplayName("matches a clone ignoring case differences")
    void matchesCloneIgnoringCase() {
        List<RemoteRepo> remotes = List.of(repo("octo-user", false, "ProjectManager", null, false));
        LocalClone upper = new LocalClone(tmp.resolve("pm"), "https://github.com/Octo-User/PROJECTMANAGER.git",
                RemoteUrl.githubKey("https://github.com/Octo-User/PROJECTMANAGER.git").orElseThrow());
        List<RepoGroup> groups = RepoCatalog.build("OCTO-USER", remotes, List.of(upper), Map.of(), false, false);
        assertEquals(1, groups.size());
        assertTrue(groups.get(0).entries().get(0).cloned());
    }

    @Test
    @DisplayName("two clones, one registered in pm")
    void twoClonesOneRegistered() {
        List<RemoteRepo> remotes = List.of(repo("octo-user", false, "app", null, false));
        LocalClone a = clone("a/app", "octo-user", "app");
        LocalClone b = clone("b/app", "octo-user", "app");
        List<RepoGroup> groups = RepoCatalog.build("octo-user", remotes, List.of(b, a),
                Map.of("my-app", b.path()), false, false);
        CatalogEntry entry = groups.get(0).entries().get(0);
        assertEquals(List.of(a, b), entry.clones());
        assertEquals("my-app", entry.pmProject());
    }

    @Test
    @DisplayName("a registered path written differently still matches")
    void registeredPathIsNormalized() {
        List<RemoteRepo> remotes = List.of(repo("octo-user", false, "app", null, false));
        LocalClone clone = clone("work/app", "octo-user", "app");
        Path written = tmp.resolve("work/./other/../app");
        List<RepoGroup> groups = RepoCatalog.build("octo-user", remotes, List.of(clone), Map.of("app", written), false, false);
        assertEquals("app", groups.get(0).entries().get(0).pmProject());
    }

    @Test
    @DisplayName("archived repos are hidden unless cloned or requested")
    void archived() {
        List<RemoteRepo> remotes = List.of(
                repo("octo-user", false, "gone", null, true),
                repo("octo-user", false, "kept", null, true));
        List<LocalClone> clones = List.of(clone("kept", "octo-user", "kept"));
        assertEquals(List.of("kept"), RepoCatalog.build("octo-user", remotes, clones, Map.of(), false, false)
                .get(0).entries().stream().map(CatalogEntry::name).toList());
        assertEquals(2, RepoCatalog.build("octo-user", remotes, clones, Map.of(), true, false)
                .get(0).entries().size());
    }

    @Test
    @DisplayName("clones of repos not in the catalog are local only")
    void unknownGithubRepoIsLocalOnly() {
        List<LocalClone> clones = List.of(clone("stranger", "someone", "else"));
        List<RepoGroup> groups = RepoCatalog.build("octo-user", List.of(), clones, Map.of(), false, false);
        assertEquals(List.of(RepoGroup.Kind.LOCAL_ONLY), kinds(groups));
    }

    @Test
    @DisplayName("public user mode puts everything in one group")
    void publicUserMode() {
        List<RemoteRepo> remotes = List.of(repo("octo-user", false, "a", null, false));
        List<RepoGroup> groups = RepoCatalog.build("octo-user", remotes, List.of(), Map.of(), false, true);
        assertEquals(List.of(RepoGroup.Kind.PUBLIC_USER), kinds(groups));
        assertEquals("octo-user", groups.get(0).owner());
    }

    @Test
    @DisplayName("empty input gives no groups")
    void empty() {
        assertTrue(RepoCatalog.build(null, List.of(), List.of(), Map.of(), false, false).isEmpty());
    }

    @Test
    @DisplayName("unknown repo cloned twice is one entry")
    void unknownRepoClonedTwiceIsOneEntry() {
        LocalClone x = clone("x/else", "someone", "else");
        LocalClone y = clone("y/else", "someone", "else");
        LocalClone noKey = new LocalClone(tmp.resolve("aaa/nokey"), null, null);
        List<RepoGroup> groups = RepoCatalog.build("octo-user", List.of(), List.of(y, x, noKey), Map.of(), false, false);
        assertEquals(List.of(RepoGroup.Kind.LOCAL_ONLY), kinds(groups));
        assertEquals(2, groups.get(0).entries().size());
        CatalogEntry noKeyEntry = groups.get(0).entries().get(0);
        assertEquals(List.of(noKey), noKeyEntry.clones());
        CatalogEntry elseEntry = groups.get(0).entries().get(1);
        assertEquals(List.of(x, y), elseEntry.clones());
    }

    @Test
    @DisplayName("find by name, by owner/name, and ambiguity")
    void find() {
        List<RemoteRepo> remotes = List.of(
                repo("octo-user", false, "app", null, false),
                repo("acme-org", true, "app", null, false),
                repo("acme-org", true, "FindMatch", null, false));
        List<RepoGroup> groups = RepoCatalog.build("octo-user", remotes, List.of(), Map.of(), true, false);
        assertEquals(2, RepoCatalog.find(groups, "APP").size());
        assertEquals("acme-org/app", RepoCatalog.find(groups, "Acme-Org/App").get(0).displayId());
        assertEquals(1, RepoCatalog.find(groups, "findmatch").size());
        assertTrue(RepoCatalog.find(groups, "nothing").isEmpty());
        assertEquals(List.of("acme-org/FindMatch"), RepoCatalog.suggest(groups, "match"));
    }
}
