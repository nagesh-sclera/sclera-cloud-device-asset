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

class DaintreeClientTest {

    @Test
    void getDeviceIdByDaintreeDeviceId_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("daintree/getDeviceIdByDaintreeDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        DaintreeClient client = new DaintreeClient(dapr);
        String result = client.getDeviceIdByDaintreeDeviceId("dev-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("daintree/getDeviceIdByDaintreeDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getDaintreeAlertStatusByDeviceId_returnsFalseOnException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        DaintreeClient client = new DaintreeClient(dapr);
        assertThat(client.getDaintreeAlertStatusByDeviceId("dev-001")).isEqualTo(Boolean.FALSE);
    }

    @Test
    void updateDaintreeDeviceByDeviceId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        DaintreeClient client = new DaintreeClient(dapr);
        client.updateDaintreeDeviceByDeviceId("old", "new", java.util.Collections.emptySet());
    }
}
