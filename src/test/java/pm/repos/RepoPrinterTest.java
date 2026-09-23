package pm.repos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RepoPrinter")
class RepoPrinterTest {

    private static final Path HOME = Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().resolve("home");
    private static final Instant NOW = Instant.parse("2026-09-23T12:00:00Z");

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final RepoPrinter printer = new RepoPrinter(new PrintStream(buffer, true, StandardCharsets.UTF_8), HOME);

    private String output() {
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static RemoteRepo repo(String owner, boolean org, String name, String description) {
        return new RemoteRepo(owner, org, name, false, description, "main",
                NOW.minus(Duration.ofDays(2)), false, false, "https://github.com/" + owner + "/" + name, "admin");
    }

    private static LocalClone clone(String relative, String owner, String name) {
        return new LocalClone(HOME.resolve(relative), "https://github.com/" + owner + "/" + name, RemoteUrl.key(owner, name));
    }

    @Test
    @DisplayName("prints group headers with counts, rows and the legend")
    void list() {
        CatalogEntry cloned = new CatalogEntry(repo("octo-user", false, "ProjectManager", null),
                List.of(clone("repos/Personal/ProjectManager", "octo-user", "ProjectManager")), "pm-project");
        CatalogEntry remoteOnly = new CatalogEntry(repo("octo-user", false, "Spectra", null), List.of(), null);
        printer.printList(List.of(new RepoGroup(RepoGroup.Kind.OWN, "octo-user", List.of(cloned, remoteOnly))));

        String out = output();
        assertTrue(out.contains("octo-user (your account)"));
        assertTrue(out.contains("2 repos · 1 cloned"));
        assertTrue(out.contains("● ProjectManager"));
        assertTrue(out.contains("~" + java.io.File.separator + "repos"));
        assertTrue(out.contains("[pm]"));
        assertTrue(out.contains("○ Spectra"));
        assertTrue(out.contains("not cloned"));
        assertTrue(out.contains("● cloned  ○ not cloned  [pm] registered in pm"));
    }

    @Test
    @DisplayName("shows how many extra clones a repo has")
    void showsExtraCloneCount() {
        CatalogEntry entry = new CatalogEntry(repo("octo-user", false, "app", null),
                List.of(clone("a/app", "octo-user", "app"), clone("b/app", "octo-user", "app")), null);
        printer.printList(List.of(new RepoGroup(RepoGroup.Kind.OWN, "octo-user", List.of(entry))));
        assertTrue(output().contains("(+1 more)"));
    }

    @Test
    @DisplayName("collaboration rows show the owner; organization headers say organization")
    void ownersAndOrganizations() {
        CatalogEntry shared = new CatalogEntry(repo("other-user", false, "shared", null), List.of(), null);
        CatalogEntry orgRepo = new CatalogEntry(repo("acme-org", true, "FindMatch", null), List.of(), null);
        printer.printList(List.of(
                new RepoGroup(RepoGroup.Kind.ORGANIZATION, "acme-org", List.of(orgRepo)),
                new RepoGroup(RepoGroup.Kind.COLLABORATIONS, null, List.of(shared))));
        assertTrue(output().contains("acme-org (organization)"));
        assertTrue(output().contains("Collaborations"));
        assertTrue(output().contains("other-user"));
    }

    @Test
    @DisplayName("long names are cut at the column cap with an ellipsis")
    void longNamesCapped() {
        String longName = "a".repeat(50);
        CatalogEntry entry = new CatalogEntry(repo("octo-user", false, longName, null), List.of(), null);
        printer.printList(List.of(new RepoGroup(RepoGroup.Kind.OWN, "octo-user", List.of(entry))));
        assertTrue(output().contains("a".repeat(RepoPrinter.NAME_WIDTH_CAP - 1) + "…"));
        assertFalse(output().contains("a".repeat(RepoPrinter.NAME_WIDTH_CAP + 1)));
    }

    @Test
    @DisplayName("an empty catalog says so")
    void emptyCatalog() {
        printer.printList(List.of());
        assertTrue(output().contains("No repositories found."));
    }

    @Test
    @DisplayName("hostile text from GitHub is printed without control characters")
    void sanitizesEverything() {
        CatalogEntry entry = new CatalogEntry(repo("acme-org", true, "Find\u001B[2JMatch", "desc \u001B]0;pwned\u0007 ‮txt"),
                List.of(), null);
        printer.printList(List.of(new RepoGroup(RepoGroup.Kind.ORGANIZATION, "acme-org", List.of(entry))));
        printer.printDetail(entry, List.of(new Collaborator("evil\u001B[1m", "admin")), null, Map.of(), NOW);
        String out = output();
        assertFalse(out.contains("\u001B[2J"));
        assertFalse(out.contains("\u001B]0;"));
        assertFalse(out.contains("\u0007"));
        assertFalse(out.contains("‮"));
    }

    @Test
    @DisplayName("detail view shows repo facts, collaborators, clones and pm project")
    void detail() {
        LocalClone c = clone("repos/Personal/FindMatch", "acme-org", "FindMatch");
        CatalogEntry entry = new CatalogEntry(repo("acme-org", true, "FindMatch", "Matchmaking app"), List.of(c), "findmatch");
        printer.printDetail(entry, List.of(new Collaborator("octo-user", "admin"), new Collaborator("teammate", "write")),
                null, Map.of(c.path(), new CloneStatus("main", 0, 0)), NOW);
        String out = output();
        assertTrue(out.contains("FindMatch"));
        assertTrue(out.contains("acme-org (organization)"));
        assertTrue(out.contains("https://github.com/acme-org/FindMatch"));
        assertTrue(out.contains("Matchmaking app"));
        assertTrue(out.contains("2 days ago"));
        assertTrue(out.contains("admin"));
        assertTrue(out.contains("teammate"));
        assertTrue(out.contains("main, clean, up to date"));
        assertTrue(out.contains("pm project: findmatch"));
    }

    @Test
    @DisplayName("detail view explains hidden collaborators and suggests registering")
    void detailNotesAndHint() {
        LocalClone c = clone("repos/app", "octo-user", "app");
        CatalogEntry entry = new CatalogEntry(repo("octo-user", false, "app", null), List.of(c), null);
        printer.printDetail(entry, null, "not visible (requires push access)",
                Map.of(c.path(), new CloneStatus("dev", 3, 2)), NOW);
        String out = output();
        assertTrue(out.contains("not visible (requires push access)"));
        assertTrue(out.contains("dev, 3 changed, 2 unpushed"));
        assertTrue(out.contains("pm add app --path"));
    }

    @Test
    @DisplayName("local-only detail redacts credentials in the remote URL")
    void localOnlyRedacts() {
        LocalClone c = new LocalClone(HOME.resolve("mirror"), "https://user:SECRET@git.example/o/r.git", null);
        printer.printDetail(new CatalogEntry(null, List.of(c), null), null, null, Map.of(), NOW);
        assertFalse(output().contains("SECRET"));
        assertTrue(output().contains("https://git.example/o/r.git"));
    }

    @Test
    @DisplayName("ago formats minutes, hours, days, months and years")
    void agoFormatting() {
        assertEquals("just now", RepoPrinter.ago(NOW.minusSeconds(10), NOW));
        assertEquals("5 minutes ago", RepoPrinter.ago(NOW.minus(Duration.ofMinutes(5)), NOW));
        assertEquals("1 hour ago", RepoPrinter.ago(NOW.minus(Duration.ofHours(1)), NOW));
        assertEquals("2 days ago", RepoPrinter.ago(NOW.minus(Duration.ofDays(2)), NOW));
        assertEquals("3 months ago", RepoPrinter.ago(NOW.minus(Duration.ofDays(95)), NOW));
        assertEquals("2 years ago", RepoPrinter.ago(NOW.minus(Duration.ofDays(800)), NOW));
        assertEquals("unknown", RepoPrinter.ago(null, NOW));
    }
}
