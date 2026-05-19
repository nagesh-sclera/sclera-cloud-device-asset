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

class ClientQrCodeClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        ClientQrCodeClient client = new ClientQrCodeClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void getLocationIdsTaggedToClientQrCode_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("clientQrCode/getLocationIdsTaggedToClientQrCode"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        ClientQrCodeClient client = new ClientQrCodeClient(dapr);
        client.getLocationIdsTaggedToClientQrCode("id-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-integrations"), eq("clientQrCode/getLocationIdsTaggedToClientQrCode"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void getClientQrCodeCountByDeviceId_usesCorrectPath() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("clientQrCode/getClientQrCodeCountByDeviceId"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        ClientQrCodeClient client = new ClientQrCodeClient(dapr);
        Integer result = client.getClientQrCodeCountByDeviceId("device-001");
        assertThat(result).isEqualTo(0);
    }

    @Test
    void returnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
                .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        ClientQrCodeClient client = new ClientQrCodeClient(dapr);
        assertThat(client.getClientQrCodeCountByDeviceId("device-001")).isEqualTo(0);
        assertThat(client.getLocationIdsTaggedToClientQrCode("id-001")).isNotNull();
        assertThat(client.syncClientQrCodes("vdms-001")).isEmpty();
    }
}
