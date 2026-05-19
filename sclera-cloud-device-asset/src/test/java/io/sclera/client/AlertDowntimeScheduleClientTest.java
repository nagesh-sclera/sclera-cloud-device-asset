package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertDowntimeScheduleClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        AlertDowntimeScheduleClient client = new AlertDowntimeScheduleClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertion ─────────────────────────────

    @Test
    void checkAlertDowntime_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("alertDowntimeSchedule/checkAlertDowntime"),
                any(),
                any(HttpExtension.class),
                eq(Boolean.class)))
            .thenReturn(Mono.justOrEmpty(Boolean.FALSE));

        AlertDowntimeScheduleClient client = new AlertDowntimeScheduleClient(dapr);
        Boolean result = client.checkAlertDowntime("device-001", "profile-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("alertDowntimeSchedule/checkAlertDowntime"),
                any(),
                extCaptor.capture(),
                eq(Boolean.class));
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isFalse();
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class), any(Class.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        AlertDowntimeScheduleClient client = new AlertDowntimeScheduleClient(dapr);
        Boolean result = client.checkAlertDowntime("device-001", "profile-001");
        assertThat(result).isFalse();
    }
}
