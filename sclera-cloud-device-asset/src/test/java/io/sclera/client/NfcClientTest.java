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

class NfcClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        NfcClient client = new NfcClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void getLocationIdsTaggedToNfc_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("nfc/getLocationIdsTaggedToNfc"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        NfcClient client = new NfcClient(dapr);
        client.getLocationIdsTaggedToNfc("nfc-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq("sclera-integrations"), eq("nfc/getLocationIdsTaggedToNfc"), any(), extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void getQrNfcCountByDeviceId_usesCorrectPath() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq("sclera-integrations"), eq("nfc/getQrNfcCountByDeviceId"), any(), any(HttpExtension.class)))
                .thenReturn(Mono.empty());

        NfcClient client = new NfcClient(dapr);
        Integer result = client.getQrNfcCountByDeviceId("device-001");
        assertThat(result).isEqualTo(0);
    }

    @Test
    void returnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
                .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        NfcClient client = new NfcClient(dapr);
        assertThat(client.getQrNfcCountByDeviceId("device-001")).isEqualTo(0);
        assertThat(client.getNfcsByLocationIds(java.util.Collections.emptySet())).isEmpty();
        // void methods swallow
        client.syncAllNfc("vdms-001");
        client.syncNfc("vdms-001");
    }
}
