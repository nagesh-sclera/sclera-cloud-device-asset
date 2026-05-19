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

class DisruptiveClientTest {

    @Test
    void getDeviceIdByDisruptiveSensorId_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("disruptive/getDeviceIdByDisruptiveSensorId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        DisruptiveClient client = new DisruptiveClient(dapr);
        String result = client.getDeviceIdByDisruptiveSensorId("sensor-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("disruptive/getDeviceIdByDisruptiveSensorId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getDisruptiveSensorCountByDeviceId_returnsZeroOnException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        DisruptiveClient client = new DisruptiveClient(dapr);
        assertThat(client.getDisruptiveSensorCountByDeviceId("dev-001")).isEqualTo(0);
    }

    @Test
    void updateDisruptiveSensorDeviceId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        DisruptiveClient client = new DisruptiveClient(dapr);
        client.updateDisruptiveSensorDeviceId("old", "new", java.util.Collections.emptySet());
    }
}
