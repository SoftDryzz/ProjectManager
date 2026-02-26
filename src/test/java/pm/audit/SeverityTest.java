package pm.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Severity")
class SeverityTest {

    @Test
    @DisplayName("from('critical') returns CRITICAL")
    void fromCritical() {
        assertEquals(Severity.CRITICAL, Severity.from("critical"));
    }

    @Test
    @DisplayName("from('high') returns HIGH")
    void fromHigh() {
        assertEquals(Severity.HIGH, Severity.from("high"));
    }

    @Test
    @DisplayName("from('moderate') returns MEDIUM (npm style)")
    void fromModerate() {
        assertEquals(Severity.MEDIUM, Severity.from("moderate"));
    }

    @Test
    @DisplayName("from('medium') returns MEDIUM")
    void fromMedium() {
        assertEquals(Severity.MEDIUM, Severity.from("medium"));
    }

    @Test
    @DisplayName("from('low') returns LOW")
    void fromLow() {
        assertEquals(Severity.LOW, Severity.from("low"));
    }

    @Test
    @DisplayName("from() is case-insensitive")
    void caseInsensitive() {
        assertEquals(Severity.CRITICAL, Severity.from("CRITICAL"));
        assertEquals(Severity.HIGH, Severity.from("High"));
        assertEquals(Severity.MEDIUM, Severity.from("MODERATE"));
        assertEquals(Severity.LOW, Severity.from("Low"));
    }

    @Test
    @DisplayName("from(null) defaults to MEDIUM")
    void fromNull() {
        assertEquals(Severity.MEDIUM, Severity.from(null));
    }

    @Test
    @DisplayName("from('') defaults to MEDIUM")
    void fromEmpty() {
        assertEquals(Severity.MEDIUM, Severity.from(""));
        assertEquals(Severity.MEDIUM, Severity.from("   "));
    }

    @Test
    @DisplayName("from('unknown') defaults to MEDIUM")
    void fromUnknown() {
        assertEquals(Severity.MEDIUM, Severity.from("unknown"));
        assertEquals(Severity.MEDIUM, Severity.from("info"));
    }
}
