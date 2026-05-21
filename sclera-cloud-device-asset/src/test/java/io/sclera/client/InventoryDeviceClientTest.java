package io.sclera.client;

import com.alibaba.fastjson.JSONObject;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.InventoryDeviceSyncDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryDeviceClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        InventoryDeviceClient client = new InventoryDeviceClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertions ────────────────────────────

    @Test
    void retireInventoryDevice_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inventory"),
                eq("inventoryDevice/retireInventoryDevice"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        InventoryDeviceClient client = new InventoryDeviceClient(dapr);
        client.retireInventoryDevice("vdms-001", "device-001", "user@test.com", "desc", "inv-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inventory"),
                eq("inventoryDevice/retireInventoryDevice"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void upsertInventoryDevices_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inventory"),
                eq("inventoryDevice/upsertInventoryDevices"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        InventoryDeviceClient client = new InventoryDeviceClient(dapr);
        Set<DeviceDTO> result = client.upsertInventoryDevices(new JSONObject(), "vdms-001", "user@test.com", new InventoryDeviceSyncDTO());

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inventory"),
                eq("inventoryDevice/upsertInventoryDevices"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
        assertThat(result).isEmpty();
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        InventoryDeviceClient client = new InventoryDeviceClient(dapr);
        // void method swallows
        client.retireInventoryDevice("vdms-001", "device-001", "user@test.com", "desc", "inv-001");
        // returns empty set on error
        Set<DeviceDTO> result = client.upsertInventoryDevices(new JSONObject(), "vdms-001", "user@test.com", new InventoryDeviceSyncDTO());
        assertThat(result).isEmpty();
    }
}
