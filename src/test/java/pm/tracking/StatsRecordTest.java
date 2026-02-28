package pm.tracking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatsRecordTest {

    @Test
    void constructorStoresAllFields() {
        StatsRecord record = new StatsRecord(12340, true, "2026-02-28T15:30:00Z");
        assertEquals(12340, record.durationMs());
        assertTrue(record.success());
        assertEquals("2026-02-28T15:30:00Z", record.timestamp());
    }

    @Test
    void formattedDurationShowsSeconds() {
        StatsRecord record = new StatsRecord(45000, true, "2026-02-28T15:30:00Z");
        assertEquals("45s", record.formattedDuration());
    }

    @Test
    void formattedDurationShowsMinutesAndSeconds() {
        StatsRecord record = new StatsRecord(95000, true, "2026-02-28T15:30:00Z");
        assertEquals("1m 35s", record.formattedDuration());
    }

    @Test
    void formattedDurationZero() {
        StatsRecord record = new StatsRecord(500, true, "2026-02-28T15:30:00Z");
        assertEquals("0s", record.formattedDuration());
    }
}
