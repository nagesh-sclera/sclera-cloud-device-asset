package io.sclera.scheduler.subscriber;

import io.dapr.client.DaprClient;
import io.sclera.dapr.events.VdmsLifecycleEvent;
import io.sclera.scheduler.service.PerVdmsRegistrar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VdmsLifecycleSubscriberTest {

    @Mock DaprClient dapr;
    @Mock PerVdmsRegistrar registrar;

    VdmsLifecycleSubscriber subscriber() {
        return new VdmsLifecycleSubscriber(dapr, registrar);
    }

    @Test
    void activatedEventRegistersVdms() {
        subscriber().handleEvent(new VdmsLifecycleEvent("vdms-1", "UTC", "ACTIVATED"));
        verify(registrar).onVdmsActivated("vdms-1", "UTC");
    }

    @Test
    void deactivatedEventTearsDownVdms() {
        subscriber().handleEvent(new VdmsLifecycleEvent("vdms-1", null, "DEACTIVATED"));
        verify(registrar).onVdmsDeactivated("vdms-1");
    }

    @Test
    void unknownStatusThrows() {
        assertThatThrownBy(() ->
                subscriber().handleEvent(new VdmsLifecycleEvent("vdms-1", "UTC", "WAT")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingVdmsIdThrows() {
        assertThatThrownBy(() ->
                subscriber().handleEvent(new VdmsLifecycleEvent(null, "UTC", "ACTIVATED")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
