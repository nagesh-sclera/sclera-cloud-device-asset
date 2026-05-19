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

    @Test
    void clientConstructsCleanly() {
        DaprClient dapr = mock(DaprClient.class);
        TicketClient client = new TicketClient(dapr);
        assertThat(client).isNotNull();
    }

    // ── happy-path: exact path and verb assertions ────────────────────────────

    @Test
    void getTicketCountByDeviceId_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("ticket/getTicketCountByDeviceId"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        TicketClient client = new TicketClient(dapr);
        Integer result = client.getTicketCountByDeviceId("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("ticket/getTicketCountByDeviceId"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isEqualTo(1);
    }

    @Test
    void getOpenTicketStatus_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("ticket/getOpenTicketStatus"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        TicketClient client = new TicketClient(dapr);
        Boolean result = client.getOpenTicketStatus("device-001");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("ticket/getOpenTicketStatus"),
                any(),
                extCaptor.capture());
        assertThat(extCaptor.getValue().getMethod()).isEqualTo(DaprHttp.HttpMethods.GET);
        assertThat(result).isFalse();
    }

    @Test
    void updateTicketAssigneeByUserEmail_usesCorrectPathAndVerb() {
        DaprClient dapr = mock(DaprClient.class);
        when(dapr.invokeMethod(
                eq("sclera-workorders"),
                eq("ticket/updateTicketAssigneeByUserEmail"),
                any(),
                any(HttpExtension.class)))
            .thenReturn(Mono.empty());

        TicketClient client = new TicketClient(dapr);
        client.updateTicketAssigneeByUserEmail("user@test.com");

        ArgumentCaptor<HttpExtension> extCaptor = ArgumentCaptor.forClass(HttpExtension.class);
        verify(dapr).invokeMethod(
                eq("sclera-workorders"),
                eq("ticket/updateTicketAssigneeByUserEmail"),
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

        TicketClient client = new TicketClient(dapr);
        assertThat(client.getTicketCountByDeviceId("device-001")).isEqualTo(1);
        assertThat(client.getOpenTicketStatus("device-001")).isFalse();
        client.updateTicketAssigneeByUserEmail("user@test.com"); // must not throw
    }
}
