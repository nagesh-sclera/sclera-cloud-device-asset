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

class SnmpClientTest {

    @Test
    void getDeviceIdBySnmpDeviceId_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("snmp/getDeviceIdBySnmpDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        SnmpClient client = new SnmpClient(dapr);
        String result = client.getDeviceIdBySnmpDeviceId("snmp-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("snmp/getDeviceIdBySnmpDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getSnmpDeviceAlertStatusByDeviceId_returnsFalseOnException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        SnmpClient client = new SnmpClient(dapr);
        assertThat(client.getSnmpDeviceAlertStatusByDeviceId("dev-001")).isEqualTo(Boolean.FALSE);
    }

    @Test
    void updateSnmpObjectDeviceId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        SnmpClient client = new SnmpClient(dapr);
        client.updateSnmpObjectDeviceId("old", "new", Collections.emptySet());
    }

    @Test
    void deleteGlobalSnmpByDeviceId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        SnmpClient client = new SnmpClient(dapr);
        client.deleteGlobalSnmpByDeviceId("dev-001");
    }
}
