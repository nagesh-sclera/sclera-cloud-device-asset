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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultSubscriberTest {

    @Mock DaprClient dapr;
    @Mock RunRecorder recorder;

    ResultSubscriber subscriber() {
        return new ResultSubscriber(dapr, recorder);
    }

    @Test
    void handleEventRecordsFailedResultByRunId() {
        UUID runId = UUID.randomUUID();

        subscriber().handleEvent(new SchedulerResultEvent("snmpSync", runId.toString(), "FAILED", 42L, "x"));

        verify(recorder).recordResult(runId, RunStatus.FAILED, 42L, "x");
    }

    @Test
    void handleEventRecordsSuccessResultWithNullError() {
        UUID runId = UUID.randomUUID();

        subscriber().handleEvent(new SchedulerResultEvent("snmpSync", runId.toString(), "SUCCESS", 10L, null));

        verify(recorder).recordResult(runId, RunStatus.SUCCESS, 10L, null);
    }

    @Test
    void handleEventRejectsNonTerminalFiredStatus() {
        // IllegalArgumentException -> base class classifies as permanent -> DROP to DLQ.
        assertThatThrownBy(() -> subscriber().handleEvent(
                new SchedulerResultEvent("snmpSync", UUID.randomUUID().toString(), "FIRED", 0L, null)))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(recorder);
    }

    @Test
    void handleEventRejectsUnparseableStatus() {
        assertThatThrownBy(() -> subscriber().handleEvent(
                new SchedulerResultEvent("snmpSync", UUID.randomUUID().toString(), "BOGUS", 0L, null)))
            .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(recorder);
    }
}
