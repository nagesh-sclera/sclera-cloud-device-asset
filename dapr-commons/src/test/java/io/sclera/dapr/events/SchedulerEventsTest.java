package io.sclera.dapr.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SchedulerEventsTest {

    @Test
    void triggerEventCarriesJobNameAndRunId() {
        SchedulerTriggerEvent e = new SchedulerTriggerEvent("snmpSync", "run-1", 123L);
        assertEquals("snmpSync", e.jobName());
        assertEquals("run-1", e.runId());
        assertEquals(123L, e.firedAtEpochMs());
    }

    @Test
    void resultEventCarriesOutcome() {
        SchedulerResultEvent e =
            new SchedulerResultEvent("snmpSync", "run-1", "SUCCESS", 250L, null);
        assertEquals("run-1", e.runId());
        assertEquals("SUCCESS", e.status());
        assertEquals(250L, e.durationMs());
    }
}
