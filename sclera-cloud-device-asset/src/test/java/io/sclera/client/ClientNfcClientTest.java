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
import static org.mockito.Mockito.*;

class ClientNfcClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        ClientNfcClient client = new ClientNfcClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void getLocationIdsTaggedToClientNfc_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("clientNfc/getLocationIdsTaggedToClientNfc"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        ClientNfcClient client = new ClientNfcClient(dapr);
        client.getLocationIdsTaggedToClientNfc("id-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-integrations"), eq("clientNfc/getLocationIdsTaggedToClientNfc"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void getClientNfcCountByDeviceId_usesCorrectPath() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("clientNfc/getClientNfcCountByDeviceId"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        ClientNfcClient client = new ClientNfcClient(dapr);
        Integer result = client.getClientNfcCountByDeviceId("device-001");
        assertThat(result).isEqualTo(0);
    }

    @Test
    void returnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
                .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        ClientNfcClient client = new ClientNfcClient(dapr);
        assertThat(client.getClientNfcCountByDeviceId("device-001")).isEqualTo(0);
        assertThat(client.getLocationIdsTaggedToClientNfc("id-001")).isNotNull();
        // void methods swallow
        client.syncAllClientNfc("vdms-001");
        client.syncClientNfc("vdms-001");
    }
}
