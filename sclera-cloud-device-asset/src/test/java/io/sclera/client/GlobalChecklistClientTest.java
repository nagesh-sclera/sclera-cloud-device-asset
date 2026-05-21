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

class GlobalChecklistClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        GlobalChecklistClient client = new GlobalChecklistClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void updateDeviceGlobalChecklistDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("globalChecklist/updateDeviceGlobalChecklistDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        GlobalChecklistClient client = new GlobalChecklistClient(dapr);
        client.updateDeviceGlobalChecklistDeviceId("old-001", "new-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("globalChecklist/updateDeviceGlobalChecklistDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void deleteGlobalChecklistByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("globalChecklist/deleteGlobalChecklistByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        GlobalChecklistClient client = new GlobalChecklistClient(dapr);
        client.deleteGlobalChecklistByDeviceId("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("globalChecklist/deleteGlobalChecklistByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        GlobalChecklistClient client = new GlobalChecklistClient(dapr);
        // void methods must not throw
        client.updateDeviceGlobalChecklistDeviceId("old", "new");
        client.deleteGlobalChecklistByDeviceId("device-001");
    }
}
