package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.PropertyServiceDTO;
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

class PropertyQrcodeClientTest {

    @Test
    void getPropertyServices_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("propertyQrcode/getPropertyServices"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        PropertyQrcodeClient client = new PropertyQrcodeClient(dapr);
        var result = client.getPropertyServices("user", "vdms-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("propertyQrcode/getPropertyServices"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEqualTo(Collections.emptySet());
    }

    @Test
    void upsertPropertyServiceDetails_returnsDtoOnException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        PropertyQrcodeClient client = new PropertyQrcodeClient(dapr);
        PropertyServiceDTO dto = new PropertyServiceDTO();
        assertThat(client.upsertPropertyServiceDetails("user", "vdms-001", dto)).isSameAs(dto);
    }

    @Test
    void deletePropertyService_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        PropertyQrcodeClient client = new PropertyQrcodeClient(dapr);
        client.deletePropertyService("user", "vdms-001", "svc-001");
    }
}
