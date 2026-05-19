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

class EcobeeClientTest {

    @Test
    void getEcobeeDevicesByDeviceId_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("ecobee/getEcobeeDevicesByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        EcobeeClient client = new EcobeeClient(dapr);
        var result = client.getEcobeeDevicesByDeviceId("user", "vdms-001", "docker-001", "dev-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("ecobee/getEcobeeDevicesByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEqualTo(Collections.emptySet());
    }

    @Test
    void getDeviceIdByEcobeeSensorId_returnsNullOnException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        EcobeeClient client = new EcobeeClient(dapr);
        assertThat(client.getDeviceIdByEcobeeSensorId("sensor-001")).isNull();
    }

    @Test
    void updateEcobeeSensorDeviceId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        EcobeeClient client = new EcobeeClient(dapr);
        client.updateEcobeeSensorDeviceId("old", "new", Collections.emptySet());
    }
}
