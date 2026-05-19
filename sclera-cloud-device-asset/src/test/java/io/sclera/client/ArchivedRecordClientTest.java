package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.service.UserActionLogDTO;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchivedRecordClientTest {

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        ArchivedRecordClient client = new ArchivedRecordClient(dapr);
        assertThat(client).isNotNull();
    }

    @Test
    void clientReturnsDocumentedDefaultOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        ArchivedRecordClient client = new ArchivedRecordClient(dapr);
        assertThat(client).isNotNull();

        // batchUpdateArchivedRecords is void — must swallow exception and return normally
        List<UserActionLogDTO> logs = Collections.emptyList();
        client.batchUpdateArchivedRecords(logs);
    }

    @Test
    void batchUpdateArchivedRecordsSwallowsException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(), any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("network failure")));

        ArchivedRecordClient client = new ArchivedRecordClient(dapr);
        // Should not throw even when Dapr is down
        List<UserActionLogDTO> logs = Collections.singletonList(new UserActionLogDTO());
        client.batchUpdateArchivedRecords(logs);
    }
}
