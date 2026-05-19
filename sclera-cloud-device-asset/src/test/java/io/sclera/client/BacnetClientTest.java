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

class BacnetClientTest {

    @Test
    void getDeviceIdByBacnetObjectId_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("bacnet/getDeviceIdByBacnetObjectId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        BacnetClient client = new BacnetClient(dapr);
        String result = client.getDeviceIdByBacnetObjectId("dev-001", "obj-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("bacnet/getDeviceIdByBacnetObjectId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getBacnetObjectCountByDeviceId_returnsZeroOnException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        BacnetClient client = new BacnetClient(dapr);
        assertThat(client.getBacnetObjectCountByDeviceId("dev-001")).isEqualTo(0);
    }

    @Test
    void getBacnetObjectAlertStatusByDeviceId_usesCorrectPath() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("bacnet/getBacnetObjectAlertStatusByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        BacnetClient client = new BacnetClient(dapr);
        Boolean result = client.getBacnetObjectAlertStatusByDeviceId("dev-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("bacnet/getBacnetObjectAlertStatusByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEqualTo(Boolean.FALSE);
    }

    @Test
    void updateBacnetObjectDeviceId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        BacnetClient client = new BacnetClient(dapr);
        // must not throw
        client.updateBacnetObjectDeviceId("old", "new", java.util.Collections.emptySet());
    }
}
