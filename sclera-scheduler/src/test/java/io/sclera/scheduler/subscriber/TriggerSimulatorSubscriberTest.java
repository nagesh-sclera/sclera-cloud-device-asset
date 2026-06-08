package io.sclera.scheduler.subscriber;

import io.dapr.client.DaprClient;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.domain.JobEntity;
import io.sclera.scheduler.domain.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerSimulatorSubscriberTest {

    @Mock DaprClient dapr;
    @Mock DaprEventPublisher publisher;
    @Mock JobRepository jobRepository;

    private TriggerSimulatorSubscriber sub() {
        TriggerSimulatorSubscriber s = new TriggerSimulatorSubscriber(dapr, publisher, jobRepository);
        s.setPubsubName("pubsub");
        s.setResultTopic("scheduler.result");
        return s;
    }

    /** Constructs a JobEntity stub via the no-arg protected ctor (same package test access)
     *  and sets owner via the public setter. */
    private void owner(String job, String owner) {
        JobEntity e = new JobEntity("dummy", "@every 1h", owner, "scheduler.trigger",
                io.sclera.scheduler.domain.JobState.ENABLED);
        when(jobRepository.findById(job)).thenReturn(Optional.of(e));
    }

    @Test
    void deviceAssetJob_isSkipped() {
        owner("offlineDeviceCheck", "device-asset");
        sub().handleEvent(new SchedulerTriggerEvent("offlineDeviceCheck", "r1", 0L));
        verifyNoInteractions(publisher);
    }

    @Test
    void otherOwnerJob_publishesSimulatedSuccess() {
        owner("snmpSync", "integrations");
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e1", null));
        sub().handleEvent(new SchedulerTriggerEvent("snmpSync", "r2", 0L));
        verify(publisher).publish(eq("pubsub"), eq("scheduler.result"),
            argThat(p -> p instanceof SchedulerResultEvent r
                && r.status().equals("SUCCESS") && r.runId().equals("r2")));
    }
}
