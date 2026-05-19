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

class GlobalInspectionRecordClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        GlobalInspectionRecordClient client = new GlobalInspectionRecordClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void updateGlobalInspectionByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("globalInspectionRecord/updateGlobalInspectionByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        GlobalInspectionRecordClient client = new GlobalInspectionRecordClient(dapr);
        client.updateGlobalInspectionByDeviceId("primary-001", "existing-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("globalInspectionRecord/updateGlobalInspectionByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void updateGlobalInspectionRecord_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("globalInspectionRecord/updateGlobalInspectionRecord"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        GlobalInspectionRecordClient client = new GlobalInspectionRecordClient(dapr);
        client.updateGlobalInspectionRecord("user@example.com");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("globalInspectionRecord/updateGlobalInspectionRecord"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
    }

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        GlobalInspectionRecordClient client = new GlobalInspectionRecordClient(dapr);
        // void methods must not throw
        client.updateGlobalInspectionRelationDeviceAndIsRemoved(Collections.emptySet());
        client.deleteGlobalInspectionRelationInBatch(Collections.emptyList());
        client.updateGlobalInspectionByDeviceId("primary", "existing");
        client.updateGlobalInspectionRelationLocationAndIsRemoved(Collections.emptySet());
        client.updateGlobalInspectionRecord("user@example.com");
    }
}
