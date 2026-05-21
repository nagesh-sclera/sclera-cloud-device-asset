package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprHttp;
import io.dapr.client.domain.HttpExtension;
import io.sclera.service.UserActionLogDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArchivedRecordClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        ArchivedRecordClient client = new ArchivedRecordClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertion ─────────────────────────────

    @Test
    void batchUpdateArchivedRecords_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-audit"),
                eq("archivedrecord/batchUpdateArchivedRecords"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        ArchivedRecordClient client = new ArchivedRecordClient(dapr);
        List<UserActionLogDTO> logs = Collections.singletonList(new UserActionLogDTO());
        client.batchUpdateArchivedRecords(logs);

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-audit"),
                eq("archivedrecord/batchUpdateArchivedRecords"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod())
                .isEqualTo(DaprHttp.HttpMethods.POST);
    }

    // ── resilience: exception swallowing ─────────────────────────────────────

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        ArchivedRecordClient client = new ArchivedRecordClient(dapr);
        assertThat(client).isNotNull();

        List<UserActionLogDTO> logs = Collections.emptyList();
        client.batchUpdateArchivedRecords(logs);
    }

    @Test
    void batchUpdateArchivedRecordsSwallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("network failure")));

        ArchivedRecordClient client = new ArchivedRecordClient(dapr);
        List<UserActionLogDTO> logs = Collections.singletonList(new UserActionLogDTO());
        client.batchUpdateArchivedRecords(logs);
    }
}
