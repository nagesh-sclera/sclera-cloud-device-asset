package io.sclera.scheduler.web;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.scheduler.service.RunRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobCallbackControllerTest {

    @Mock RunRecorder recorder;
    @Mock DaprEventPublisher publisher;

    JobCallbackController controller() {
        JobCallbackController c = new JobCallbackController(recorder, publisher);
        c.setPubsubName("pubsub");
        c.setTriggerTopic("scheduler.trigger");
        return c;
    }

    @Test
    void onFireRecordsRunAndPublishesTrigger() {
        when(publisher.publish(anyString(), anyString(), any()))
            .thenReturn(new PublishResult(true, "evt", null));

        controller().onJobFired("snmpSync");

        verify(recorder).recordFired(eq("snmpSync"), any(), eq(false));
        verify(publisher).publish(eq("pubsub"), eq("scheduler.trigger"), any());
    }
}
