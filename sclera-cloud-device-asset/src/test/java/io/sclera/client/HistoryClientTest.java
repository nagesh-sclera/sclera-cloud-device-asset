package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.HistoryDTO;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HistoryClientTest {

    @Test
    void clientHandlesNullReturnGracefully() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-audit"), any(String.class), any(), any(HttpExtension.class), any(Class.class)))
            .thenReturn(Mono.justOrEmpty(null));

        HistoryClient client = new HistoryClient(dapr);
        assertThat(client).isNotNull();

        // addHistory with null result from Dapr — should not throw
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
        // Also stub the void overload
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        HistoryClient client = new HistoryClient(dapr);
        assertThat(client).isNotNull();

        // All void methods must swallow the exception and return normally
        HistoryDTO dto = new HistoryDTO();
        client.addHistory(dto);
        client.addHistoryWithTimestamp(dto);
        client.updateHistoryDeviceId("old-id", "new-id");
        client.insertDeviceStatusHistory(1, "10.0.0.1", null, null, "dev-xyz");
    }
}
