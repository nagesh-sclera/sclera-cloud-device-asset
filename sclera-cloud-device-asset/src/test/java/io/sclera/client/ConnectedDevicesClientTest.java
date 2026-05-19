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
import static org.mockito.Mockito.*;

class ConnectedDevicesClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        ConnectedDevicesClient client = new ConnectedDevicesClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void addConnectedDevices_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("connectedDevices/addConnectedDevices"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        ConnectedDevicesClient client = new ConnectedDevicesClient(dapr);
        client.addConnectedDevices("d1", "d2", "power");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-integrations"), eq("connectedDevices/addConnectedDevices"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void getConnectedSpecificationsByDeviceId_usesCorrectPath() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("connectedDevices/getConnectedSpecificationsByDeviceId"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        ConnectedDevicesClient client = new ConnectedDevicesClient(dapr);
        var result = client.getConnectedSpecificationsByDeviceId("device-001");
        assertThat(result).isEmpty();
    }

    @Test
    void getPowerSourceTopologyConnectionsCount_usesCorrectPath() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("connectedDevices/getPowerSourceTopologyConnectionsCount"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        ConnectedDevicesClient client = new ConnectedDevicesClient(dapr);
        Integer result = client.getPowerSourceTopologyConnectionsCount();
        assertThat(result).isEqualTo(0);
    }

    @Test
    void returnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
                .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        ConnectedDevicesClient client = new ConnectedDevicesClient(dapr);
        // void methods swallow
        client.addConnectedDevices("d1", "d2", "power");
        client.untagPowerSource("spec-1", "conn-1");
        client.deleteConnectedDevicesBySpecificationId("spec-1");
        // collection returns empty
        assertThat(client.getConnectedSpecificationsByDeviceId("device-001")).isEmpty();
        assertThat(client.getPowerSourceTopologyForDevice(Collections.emptySet())).isEmpty();
        assertThat(client.getPowerSourceTopologyConnectionsCount()).isEqualTo(0);
    }
}
