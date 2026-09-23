package pm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UpdateCheckState")
class UpdateCheckStateTest {

    private static final Instant T0 = Instant.parse("2026-09-23T10:00:00Z");

    @Nested
    @DisplayName("isCheckDue")
    class IsCheckDue {

        @Test
        @DisplayName("checks when it has never checked")
        void neverChecked() {
            assertTrue(UpdateCheckState.never().isCheckDue(T0));
        }

        @Test
        @DisplayName("does not check again within 24 hours of a success")
        void withinSuccessInterval() {
            UpdateCheckState state = UpdateCheckState.success(T0, "2.0.0");
            assertFalse(state.isCheckDue(T0.plus(Duration.ofHours(23)).plusSeconds(3599)));
        }

        @Test
        @DisplayName("checks again 24 hours after a success")
        void afterSuccessInterval() {
            UpdateCheckState state = UpdateCheckState.success(T0, "2.0.0");
            assertTrue(state.isCheckDue(T0.plus(Duration.ofHours(24))));
        }

        @Test
        @DisplayName("does not retry within 1 hour of a failure")
        void withinFailureInterval() {
            UpdateCheckState state = UpdateCheckState.never().failure(T0);
            assertFalse(state.isCheckDue(T0.plus(Duration.ofMinutes(59))));
        }

        @Test
        @DisplayName("retries 1 hour after a failure")
        void afterFailureInterval() {
            UpdateCheckState state = UpdateCheckState.never().failure(T0);
            assertTrue(state.isCheckDue(T0.plus(Duration.ofHours(1))));
        }

        @Test
        @DisplayName("checks when the last check is in the future (clock moved back)")
        void futureTimestamp() {
            UpdateCheckState state = UpdateCheckState.success(T0.plus(Duration.ofDays(2)), "2.0.0");
            assertTrue(state.isCheckDue(T0));
        }

        @Test
        @DisplayName("checks when the stored timestamp is unreadable")
        void corruptTimestamp() {
            assertTrue(new UpdateCheckState("yesterday", true, "2.0.0").isCheckDue(T0));
        }
    }

    @Nested
    @DisplayName("state transitions")
    class Transitions {

        @Test
        @DisplayName("a failure keeps the last known version")
        void failureKeepsVersion() {
            UpdateCheckState failed = UpdateCheckState.success(T0, "2.1.0").failure(T0.plusSeconds(60));
            assertEquals("2.1.0", failed.latestVersion());
            assertFalse(failed.succeeded());
        }

        @Test
        @DisplayName("a success records the version and time")
        void successRecords() {
            UpdateCheckState state = UpdateCheckState.success(T0, "2.1.0");
            assertEquals("2.1.0", state.latestVersion());
            assertTrue(state.succeeded());
            assertEquals(T0.toString(), state.lastChecked());
        }
    }

    @Nested
    @DisplayName("persistence")
    class Persistence {

        @TempDir
        Path dir;

        @Test
        @DisplayName("save then load returns the same state")
        void roundTrip() {
            Path file = dir.resolve("update-check.json");
            UpdateCheckState state = UpdateCheckState.success(T0, "2.1.0");
            state.save(file);
            assertEquals(state, UpdateCheckState.load(file));
        }

        @Test
        @DisplayName("save creates missing parent directories")
        void createsParent() {
            Path file = dir.resolve("nested/dir/update-check.json");
            UpdateCheckState.success(T0, "2.1.0").save(file);
            assertTrue(Files.exists(file));
        }

        @Test
        @DisplayName("missing file loads as never checked")
        void missingFile() {
            assertEquals(UpdateCheckState.never(), UpdateCheckState.load(dir.resolve("absent.json")));
        }

        @Test
        @DisplayName("corrupt file loads as never checked")
        void corruptFile() throws IOException {
            Path file = dir.resolve("update-check.json");
            Files.writeString(file, "not json {{{");
            assertEquals(UpdateCheckState.never(), UpdateCheckState.load(file));
        }

        @Test
        @DisplayName("empty file loads as never checked")
        void emptyFile() throws IOException {
            Path file = dir.resolve("update-check.json");
            Files.writeString(file, "");
            assertEquals(UpdateCheckState.never(), UpdateCheckState.load(file));
        }
    }
}
