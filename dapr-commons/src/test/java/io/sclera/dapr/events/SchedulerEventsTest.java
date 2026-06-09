package io.sclera.dapr.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SchedulerEventsTest {

    @Test
    void triggerEventCarriesJobNameRunIdAndVdmsId() {
        SchedulerTriggerEvent e = new SchedulerTriggerEvent("vdmsSystemHealth", "run-1", "vdms-7", 123L);
        assertEquals("vdmsSystemHealth", e.jobName());
        assertEquals("run-1", e.runId());
        assertEquals("vdms-7", e.vdmsId());
        assertEquals(123L, e.firedAtEpochMs());
    }

    @Test
    void legacyThreeArgConstructorLeavesVdmsIdNull() {
        SchedulerTriggerEvent e = new SchedulerTriggerEvent("snmpSync", "run-1", 123L);
        assertNull(e.vdmsId());
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
