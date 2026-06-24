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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TicketClientTest {

    private static final String APP_ID = "sclera-workorders";
    // Full Dapr method strings include the workorder service servlet context-path.
    private static final String COUNT_PATH =
            "api/v1/workorder-service/ticket/device/device-001/ticketcount";
    private static final String STATUS_PATH =
            "api/v1/workorder-service/ticket/device/device-001/openticketstatus";
    private static final String ASSIGNEE_PATH =
            "api/v1/workorder-service/ticket/assignee/user@test.com/synctickets";

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        TicketClient client = new TicketClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: the real sidecar response is deserialized and returned ─────

    @Test
    void getTicketCountByDeviceId_returnsDeserializedCountFromSidecar() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq(APP_ID), eq(COUNT_PATH), any(),
                any(HttpExtension.class), eq(Integer.class)))
            .thenReturn(Mono.just(7));

        TicketClient client = new TicketClient(dapr);
        Integer result = client.getTicketCountByDeviceId("device-001");

        ArgumentCaptor<HttpExtension> ext = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq(APP_ID), eq(COUNT_PATH), any(),
                ext.capture(), eq(Integer.class));
        assertThat(ext.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEqualTo(7);
    }

    @Test
    void getOpenTicketStatus_returnsDeserializedBooleanFromSidecar() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq(APP_ID), eq(STATUS_PATH), any(),
                any(HttpExtension.class), eq(Boolean.class)))
            .thenReturn(Mono.just(true));

        TicketClient client = new TicketClient(dapr);
        Boolean result = client.getOpenTicketStatus("device-001");

        ArgumentCaptor<HttpExtension> ext = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq(APP_ID), eq(STATUS_PATH), any(),
                ext.capture(), eq(Boolean.class));
        assertThat(ext.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isTrue();
    }

    @Test
    void updateTicketAssigneeByUserEmail_postsToAssigneeSyncPath() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(eq(APP_ID), eq(ASSIGNEE_PATH), any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        TicketClient client = new TicketClient(dapr);
        client.updateTicketAssigneeByUserEmail("user@test.com");

        ArgumentCaptor<HttpExtension> ext = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(eq(APP_ID), eq(ASSIGNEE_PATH), any(), ext.capture());
        assertThat(ext.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.POST);
    }

    // ── resilience: safe defaults on sidecar failure ──────────────────────────

    @Test
    void getTicketCountByDeviceId_returnsZeroOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(),
                any(HttpExtension.class), eq(Integer.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        TicketClient client = new TicketClient(dapr);
        assertThat(client.getTicketCountByDeviceId("device-001")).isZero();
    }

    @Test
    void getOpenTicketStatus_returnsFalseOnDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(),
                any(HttpExtension.class), eq(Boolean.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        TicketClient client = new TicketClient(dapr);
        assertThat(client.getOpenTicketStatus("device-001")).isFalse();
    }

    @Test
    void updateTicketAssigneeByUserEmail_swallowsDaprException() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(any(String.class), any(String.class), any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.error(new RuntimeException("sidecar down")));

        TicketClient client = new TicketClient(dapr);
        client.updateTicketAssigneeByUserEmail("user@test.com"); // must not throw
    }
}
