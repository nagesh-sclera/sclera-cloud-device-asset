package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PelicanClientTest {

    @Test
    void getDeviceIdByPelicanSensorId_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("pelican/getDeviceIdByPelicanSensorId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        PelicanClient client = new PelicanClient(dapr);
        String result = client.getDeviceIdByPelicanSensorId("sensor-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("pelican/getDeviceIdByPelicanSensorId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getPelicanSensorCountByDeviceId_returnsZeroOnException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        PelicanClient client = new PelicanClient(dapr);
        assertThat(client.getPelicanSensorCountByDeviceId("dev-001")).isEqualTo(0);
    }

    @Test
    void updatePelicanSensorDeviceId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        PelicanClient client = new PelicanClient(dapr);
        client.updatePelicanSensorDeviceId("old", "new", Collections.emptySet());
    }
}
