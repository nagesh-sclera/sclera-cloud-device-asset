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

class KNXClientTest {

    @Test
    void getDeviceIdByKNXGroupAddress_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("kNX/getDeviceIdByKNXGroupAddress"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        KNXClient client = new KNXClient(dapr);
        String result = client.getDeviceIdByKNXGroupAddress("1.1.1", "1.1.1/0");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("kNX/getDeviceIdByKNXGroupAddress"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getKNXGroupCountByDeviceId_returnsZeroOnException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        KNXClient client = new KNXClient(dapr);
        assertThat(client.getKNXGroupCountByDeviceId("dev-001")).isEqualTo(0);
    }

    @Test
    void updateKnxGroupDeviceId_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        KNXClient client = new KNXClient(dapr);
        client.updateKnxGroupDeviceId("old", "new", Collections.emptySet());
    }
}
