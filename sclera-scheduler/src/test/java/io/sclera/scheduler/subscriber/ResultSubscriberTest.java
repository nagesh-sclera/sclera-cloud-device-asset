package io.sclera.scheduler.subscriber;

import io.dapr.client.DaprClient;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.scheduler.domain.RunStatus;
import io.sclera.scheduler.service.RunRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultSubscriberTest {

    @Mock DaprClient dapr;
    @Mock RunRecorder recorder;

    @Test
    void handleEventRecordsResultByRunId() {
        ResultSubscriber sub = new ResultSubscriber(dapr, recorder);
        UUID runId = UUID.randomUUID();

        sub.handleEvent(new SchedulerResultEvent("snmpSync", runId.toString(), "FAILED", 42L, "x"));

        verify(recorder).recordResult(runId, RunStatus.FAILED, 42L, "x");
    }
}
