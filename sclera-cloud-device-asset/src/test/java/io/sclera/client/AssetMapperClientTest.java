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

class AssetMapperClientTest {

    @Test
    void updateDeviceTypeForAllAsset_usesCorrectAppIdPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-integrations"),
                eq("assetMapper/updateDeviceTypeForAllAsset"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        AssetMapperClient client = new AssetMapperClient(dapr);
        client.updateDeviceTypeForAllAsset(Collections.emptyList());

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-integrations"),
                eq("assetMapper/updateDeviceTypeForAllAsset"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void updateDeviceTypeForAllAsset_swallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        AssetMapperClient client = new AssetMapperClient(dapr);
        // must not throw
        client.updateDeviceTypeForAllAsset(Collections.emptyList());
    }
}
