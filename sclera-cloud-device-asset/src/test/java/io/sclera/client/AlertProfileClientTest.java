package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.AlertProfileDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlertProfileClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        AlertProfileClient client = new AlertProfileClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertions ────────────────────────────

    @Test
    void getAlertProfileById_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("alertProfile/getAlertProfileById"),
                any(),
                any(HttpExtension.class),
                eq(AlertProfileDTO.class)))
            .thenReturn(Mono.justOrEmpty(null));

        AlertProfileClient client = new AlertProfileClient(dapr);
        AlertProfileDTO result = client.getAlertProfileById("profile-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("alertProfile/getAlertProfileById"),
                any(),
                extCaptor.capture(),
                eq(AlertProfileDTO.class));
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getAlertProfileDetailsById_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-alerts"),
                eq("alertProfile/getAlertProfileDetailsById"),
                any(),
                any(HttpExtension.class),
                eq(AlertProfileDTO.class)))
            .thenReturn(Mono.justOrEmpty(null));

        AlertProfileClient client = new AlertProfileClient(dapr);
        AlertProfileDTO result = client.getAlertProfileDetailsById(null, null, "device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-alerts"),
                eq("alertProfile/getAlertProfileDetailsById"),
                any(),
                extCaptor.capture(),
                eq(AlertProfileDTO.class));
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class), any(Class.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        AlertProfileClient client = new AlertProfileClient(dapr);
        AlertProfileDTO result1 = client.getAlertProfileById("profile-001");
        AlertProfileDTO result2 = client.getAlertProfileDetailsById(null, null, "device-001");
        assertThat(result1).isNull();
        assertThat(result2).isNull();
    }
}
