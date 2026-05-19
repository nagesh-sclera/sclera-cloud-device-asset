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

class GlobalQrcodeClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        GlobalQrcodeClient client = new GlobalQrcodeClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void deleteGlobalQRCodeByLocationId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("globalQrcode/deleteGlobalQRCodeByLocationId"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        GlobalQrcodeClient client = new GlobalQrcodeClient(dapr);
        client.deleteGlobalQRCodeByLocationId("loc-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-integrations"), eq("globalQrcode/deleteGlobalQRCodeByLocationId"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void getDeviceQrcodeCountByDeviceId_usesCorrectPath() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("globalQrcode/getDeviceQrcodeCountByDeviceId"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        GlobalQrcodeClient client = new GlobalQrcodeClient(dapr);
        Integer result = client.getDeviceQrcodeCountByDeviceId("device-001");
        assertThat(result).isEqualTo(0);
    }

    @Test
    void returnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
                .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        GlobalQrcodeClient client = new GlobalQrcodeClient(dapr);
        assertThat(client.getDeviceQrcodeCountByDeviceId("device-001")).isEqualTo(0);
        // void method swallows
        client.deleteGlobalQRCodeByLocationId("loc-001");
    }
}
