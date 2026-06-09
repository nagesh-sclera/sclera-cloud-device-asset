package io.sclera.dapr.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VdmsLifecycleEventTest {
    @Test
    void carriesVdmsIdTimezoneAndStatus() {
        VdmsLifecycleEvent e = new VdmsLifecycleEvent("vdms-7", "America/New_York", "ACTIVATED");
        assertEquals("vdms-7", e.vdmsId());
        assertEquals("America/New_York", e.timezone());
        assertEquals("ACTIVATED", e.status());
    }
}
