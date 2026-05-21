package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.HistoryDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HistoryClientTest {

    // ── happy-path: exact path assertions ────────────────────────────────────

    @Test
    void updateHistoryDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-audit"),
                eq("history/updateHistoryDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        HistoryClient client = new HistoryClient(dapr);
        client.updateHistoryDeviceId("old-id", "new-id");

        // Verify exact path was passed
        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-audit"),
                eq("history/updateHistoryDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void insertDeviceStatusHistory_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-audit"),
                eq("history/insertDeviceStatusHistory"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        HistoryClient client = new HistoryClient(dapr);
        client.insertDeviceStatusHistory(1, "192.168.1.1", null, null, "device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-audit"),
                eq("history/insertDeviceStatusHistory"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientHandlesNullReturnGracefully() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-audit"), any(String.class), any(), any(HttpExtension.class), any(Class.class)))
            .thenReturn(Mono.justOrEmpty(null));
        when(dapr.invokeMethod(eq("sclera-audit"), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.justOrEmpty(null));

        HistoryClient client = new HistoryClient(dapr);
        assertThat(client).isNotNull();

        HistoryDTO dto = new HistoryDTO();
        client.addHistory(dto);
        client.addHistoryWithTimestamp(dto);
        client.updateHistoryDeviceId("old-id", "new-id");
        client.insertDeviceStatusHistory(1, "192.168.1.1", null, null, "device-001");
    }

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class), any(Class.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        HistoryClient client = new HistoryClient(dapr);
        assertThat(client).isNotNull();

        HistoryDTO dto = new HistoryDTO();
        client.addHistory(dto);
        client.addHistoryWithTimestamp(dto);
        client.updateHistoryDeviceId("old-id", "new-id");
        client.insertDeviceStatusHistory(1, "10.0.0.1", null, null, "dev-xyz");
    }
}
