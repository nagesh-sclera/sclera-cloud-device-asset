package io.sclera.scheduler;

import io.dapr.client.DaprClient;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerDispatchSubscriberTest {

    @Mock DaprClient dapr;
    @Mock DaprEventPublisher publisher;
    @Mock DeviceAssetJobHandlers handlers;

    private TriggerDispatchSubscriber subscriber() {
        TriggerDispatchSubscriber s = new TriggerDispatchSubscriber(dapr, publisher, handlers);
        s.setPubsubName("pubsub");
        s.setResultTopic("scheduler.result");
        return s;
    }

    @Test
    void ownedJob_runsHandler_andPublishesSuccess() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e1", null));
        subscriber().handleEvent(new SchedulerTriggerEvent("offlineDeviceCheck", "r1", 0L));
        verify(handlers).offlineDeviceCheck();
        ArgumentCaptor<SchedulerResultEvent> cap = ArgumentCaptor.forClass(SchedulerResultEvent.class);
        verify(publisher).publish(eq("pubsub"), eq("scheduler.result"), cap.capture());
        assertEquals("SUCCESS", cap.getValue().status());
        assertEquals("r1", cap.getValue().runId());
        assertNull(cap.getValue().error());
    }

    @Test
    void handlerThrows_publishesFailedWithError() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e1", null));
        doThrow(new RuntimeException("boom")).when(handlers).syncAssetCountToCloud();
        subscriber().handleEvent(new SchedulerTriggerEvent("syncAssetCountToCloud", "r2", 0L));
        ArgumentCaptor<SchedulerResultEvent> cap = ArgumentCaptor.forClass(SchedulerResultEvent.class);
        verify(publisher).publish(eq("pubsub"), eq("scheduler.result"), cap.capture());
        assertEquals("FAILED", cap.getValue().status());
        assertTrue(cap.getValue().error().contains("boom"));
    }

    @Test
    void nonOwnedJob_isIgnored_noHandlerNoResult() {
        subscriber().handleEvent(new SchedulerTriggerEvent("snmpSync", "r3", 0L));
        verifyNoInteractions(handlers);
        verifyNoInteractions(publisher);
    }

    @Test
    void perVdmsJob_passesVdmsIdToHandler() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e1", null));
        subscriber().handleEvent(new SchedulerTriggerEvent("vdmsSystemHealth", "r9", "vdms-7", 0L));
        verify(handlers).vdmsSystemHealth("vdms-7");
        ArgumentCaptor<SchedulerResultEvent> cap = ArgumentCaptor.forClass(SchedulerResultEvent.class);
        verify(publisher).publish(eq("pubsub"), eq("scheduler.result"), cap.capture());
        assertEquals("SUCCESS", cap.getValue().status());
    }
}
