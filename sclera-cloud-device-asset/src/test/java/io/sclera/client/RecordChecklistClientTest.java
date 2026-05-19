package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.RecordChecklistDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecordChecklistClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        RecordChecklistClient client = new RecordChecklistClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void getRecordChecklistStatusByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("recordChecklist/getRecordChecklistStatusByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        RecordChecklistClient client = new RecordChecklistClient(dapr);
        String result = client.getRecordChecklistStatusByDeviceId("device-001", "status");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("recordChecklist/getRecordChecklistStatusByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isNull();
    }

    @Test
    void getChecklistStatusCountDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("recordChecklist/getChecklistStatusCountDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        RecordChecklistClient client = new RecordChecklistClient(dapr);
        Integer result = client.getChecklistStatusCountDeviceId("a", "b", "c");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("recordChecklist/getChecklistStatusCountDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEqualTo(0);
    }

    @Test
    void deleteAllRecordChecklistByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-inspection"),
                eq("recordChecklist/deleteAllRecordChecklistByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        RecordChecklistClient client = new RecordChecklistClient(dapr);
        List<String> result = client.deleteAllRecordChecklistByDeviceId("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-inspection"),
                eq("recordChecklist/deleteAllRecordChecklistByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEmpty();
    }

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        RecordChecklistClient client = new RecordChecklistClient(dapr);
        assertThat(client.deleteAllRecordChecklistByDeviceId("d")).isEmpty();
        assertThat(client.getRecordChecklistStatusByDeviceId("d", "x")).isNull();
        assertThat(client.getChecklistStatusCountDeviceId("a", "b", "c")).isEqualTo(0);
        Set<RecordChecklistDTO> buildings = client.getAllRecordChecklistByBuildings(
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertThat(buildings).isEmpty();
        assertThat(client.getRecordChecklistStatusByLocationId("loc", "s")).isNull();
        assertThat(client.getChecklistStatusCountLocationId("loc", "a", "b")).isEqualTo(0);
        assertThat(client.deleteAllRecordChecklistByLocationId("loc")).isNull();
        // void methods must not throw
        client.updateRecordChecklistDeviceAndIsRemoved(Collections.emptySet());
        client.deleteRecordChecklistInBatch(Collections.emptyList());
        client.deleteAllRecordChecklistImagesByUrls(Collections.emptyList());
        client.updateRecordChecklistByDeviceId("old", "new", Collections.emptySet());
        client.updateRecordChecklistLocationAndIsRemoved(Collections.emptySet());
        client.deleteRecordChecklistByLocationId("loc");
        client.updateRecordChecklist("user@example.com");
    }
}
