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

class QrCodeClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        QrCodeClient client = new QrCodeClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void getQrCodeCountByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("qrCode/getQrCodeCountByDeviceId"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        QrCodeClient client = new QrCodeClient(dapr);
        Integer result = client.getQrCodeCountByDeviceId("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-integrations"), eq("qrCode/getQrCodeCountByDeviceId"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void getLocationIdsTaggedToQrCode_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("qrCode/getLocationIdsTaggedToQrCode"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        QrCodeClient client = new QrCodeClient(dapr);
        client.getLocationIdsTaggedToQrCode("qr-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-integrations"), eq("qrCode/getLocationIdsTaggedToQrCode"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void returnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
                .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        QrCodeClient client = new QrCodeClient(dapr);
        assertThat(client.getQrCodeCountByDeviceId("device-001")).isEqualTo(0);
        assertThat(client.getLocationIdsTaggedToQrCode("qr-001")).isNotNull();
        assertThat(client.syncQrCodes("vdms-001")).isEmpty();
    }
}
