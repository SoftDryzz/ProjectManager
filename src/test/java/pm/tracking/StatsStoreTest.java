package pm.tracking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatsStoreTest {

    @TempDir
    Path tempDir;
    private Path statsFile;
    private StatsStore store;

    @BeforeEach
    void setUp() {
        statsFile = tempDir.resolve("stats.json");
        store = new StatsStore(statsFile);
    }

    @Test
    void recordCreatesFileIfNotExists() {
        store.record("my-api", "build", 12340, true);
        assertTrue(Files.exists(statsFile));
    }

    @Test
    void recordAndRetrieveSingleEntry() {
        store.record("my-api", "build", 12340, true);

        Map<String, List<StatsRecord>> stats = store.getStats("my-api");
        assertNotNull(stats);
        assertEquals(1, stats.get("build").size());
        assertEquals(12340, stats.get("build").get(0).durationMs());
        assertTrue(stats.get("build").get(0).success());
    }

    @Test
    void recordMultipleCommandsSameProject() {
        store.record("my-api", "build", 12340, true);
        store.record("my-api", "test", 45000, false);

        Map<String, List<StatsRecord>> stats = store.getStats("my-api");
        assertEquals(1, stats.get("build").size());
        assertEquals(1, stats.get("test").size());
        assertFalse(stats.get("test").get(0).success());
    }

    @Test
    void recordTrimsToMaxEntries() {
        for (int i = 0; i < 25; i++) {
            store.record("my-api", "build", 1000 + i, true);
        }

        Map<String, List<StatsRecord>> stats = store.getStats("my-api");
        List<StatsRecord> builds = stats.get("build");
        assertEquals(20, builds.size());
        // Oldest entries removed (FIFO), newest kept
        assertEquals(1005, builds.get(0).durationMs());
        assertEquals(1024, builds.get(19).durationMs());
    }

    @Test
    void getStatsReturnsNullForUnknownProject() {
        Map<String, List<StatsRecord>> stats = store.getStats("nonexistent");
        assertNull(stats);
    }

    @Test
    void getAllStatsReturnsAllProjects() {
        store.record("api", "build", 1000, true);
        store.record("frontend", "test", 2000, true);

        Map<String, Map<String, List<StatsRecord>>> all = store.getAllStats();
        assertEquals(2, all.size());
        assertTrue(all.containsKey("api"));
        assertTrue(all.containsKey("frontend"));
    }

    @Test
    void corruptedFileReturnsEmpty() throws IOException {
        Files.writeString(statsFile, "not valid json {{{");

        Map<String, Map<String, List<StatsRecord>>> all = store.getAllStats();
        assertTrue(all.isEmpty());
    }

    @Test
    void emptyFileReturnsEmpty() throws IOException {
        Files.writeString(statsFile, "");

        Map<String, Map<String, List<StatsRecord>>> all = store.getAllStats();
        assertTrue(all.isEmpty());
    }

    @Test
    void missingFileReturnsEmpty() {
        Map<String, Map<String, List<StatsRecord>>> all = store.getAllStats();
        assertTrue(all.isEmpty());
    }
}
