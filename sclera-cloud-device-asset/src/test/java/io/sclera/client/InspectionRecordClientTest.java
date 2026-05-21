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

class InspectionRecordClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        InspectionRecordClient client = new InspectionRecordClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void updateInspectionRecordStatus_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("inspectionRecord/updateInspectionRecordStatus"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        InspectionRecordClient client = new InspectionRecordClient(dapr);
        client.updateInspectionRecordStatus("user", "vdms", "id-001", false);

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("inspectionRecord/updateInspectionRecordStatus"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void updateInspectionRecord_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("inspectionRecord/updateInspectionRecord"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        InspectionRecordClient client = new InspectionRecordClient(dapr);
        client.updateInspectionRecord("user@example.com");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("inspectionRecord/updateInspectionRecord"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.POST);
    }

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        InspectionRecordClient client = new InspectionRecordClient(dapr);
        // void methods must not throw
        client.updateInspectionRecordStatus("u", "v", "id", false);
        client.updateInspectionStatusOnDeviceArchive(Collections.emptySet());
        client.updateInspectionRecord("user@example.com");
    }
}
