package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.PmsAttributesDTO;
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

class PmsClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        PmsClient client = new PmsClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertions ────────────────────────────

    @Test
    void getLocationIdsByRoomStatus_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("pms/getLocationIdsByRoomStatus"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        PmsClient client = new PmsClient(dapr);
        Set<String> result = client.getLocationIdsByRoomStatus("vdms-001", "occupied");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("pms/getLocationIdsByRoomStatus"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEmpty();
    }

    @Test
    void getPmsAttributesByLocationIds_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("pms/getPmsAttributesByLocationIds"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        PmsClient client = new PmsClient(dapr);
        Set<PmsAttributesDTO> result = client.getPmsAttributesByLocationIds(Collections.singleton("loc-001"));

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("pms/getPmsAttributesByLocationIds"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEmpty();
    }

    @Test
    void updatePmsAttributesByLocationId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("pms/updatePmsAttributesByLocationId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        PmsClient client = new PmsClient(dapr);
        client.updatePmsAttributesByLocationId("loc-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("pms/updatePmsAttributesByLocationId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        PmsClient client = new PmsClient(dapr);
        assertThat(client.getLocationIdsByRoomStatus("vdms-001", "occupied")).isEmpty();
        assertThat(client.getPmsAttributesByLocationIds(Collections.singleton("loc-001"))).isEmpty();
        client.updatePmsAttributesByLocationId("loc-001"); // must not throw
    }
}
