package io.sclera.dapr;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DaprEventSubscriberTest {

    private static final String IDEMPOTENCY_STORE = "statestore-idempotency";
    private static final String EVENT_ID = "test-event-id-123";

    @Mock
    private DaprClient daprClient;

    private TestSubscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriber = new TestSubscriber(daprClient);
    }

    @Test
    void duplicateEvent_returnsSuccess_withoutCallingHandleEvent() {
        State<Boolean> alreadyProcessed = new State<>(EVENT_ID, Boolean.TRUE, "etag", null, null);
        when(daprClient.getState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.class))
            .thenReturn(Mono.just(alreadyProcessed));

        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(EVENT_ID, "payload"));

        assertThat(response.getBody()).containsEntry("status", "SUCCESS");
        assertThat(subscriber.handleCalled).isFalse();
    }

    @Test
    void firstTimeSuccess_writesIdempotencyKey_returnsSuccess() {
        State<Boolean> notProcessed = new State<>(EVENT_ID, null, null, null, null);
        when(daprClient.getState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.class))
            .thenReturn(Mono.just(notProcessed));
        when(daprClient.saveState(eq(IDEMPOTENCY_STORE), eq(EVENT_ID), eq(Boolean.TRUE)))
            .thenReturn(Mono.empty());

        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(EVENT_ID, "payload"));

        assertThat(response.getBody()).containsEntry("status", "SUCCESS");
        assertThat(subscriber.handleCalled).isTrue();
        verify(daprClient).saveState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.TRUE);
    }

    @Test
    void transientError_returnsRetry_doesNotWriteIdempotencyKey() {
        State<Boolean> notProcessed = new State<>(EVENT_ID, null, null, null, null);
        when(daprClient.getState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.class))
            .thenReturn(Mono.just(notProcessed));
        subscriber.throwTransient = true;

        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(EVENT_ID, "payload"));

        assertThat(response.getBody()).containsEntry("status", "RETRY");
        verify(daprClient, never()).saveState(any(), any(), any());
    }

    @Test
    void permanentError_returnsDrop_doesNotWriteIdempotencyKey() {
        State<Boolean> notProcessed = new State<>(EVENT_ID, null, null, null, null);
        when(daprClient.getState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.class))
            .thenReturn(Mono.just(notProcessed));
        subscriber.throwPermanent = true;

        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(EVENT_ID, "payload"));

        assertThat(response.getBody()).containsEntry("status", "DROP");
        verify(daprClient, never()).saveState(any(), any(), any());
    }

    @Test
    void nullEventId_processesWithoutIdempotencyCheck() {
        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(null, "payload"));

        assertThat(response.getBody()).containsEntry("status", "SUCCESS");
        assertThat(subscriber.handleCalled).isTrue();
        verify(daprClient, never()).getState(any(String.class), any(String.class), any(Class.class));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CloudEvent<String> makeEvent(String id, String data) {
        CloudEvent<String> event = new CloudEvent<>();
        event.setId(id);
        event.setData(data);
        return event;
    }

    static class TestSubscriber extends DaprEventSubscriber<String> {
        boolean handleCalled = false;
        boolean throwTransient = false;
        boolean throwPermanent = false;

        TestSubscriber(DaprClient dapr) {
            super(dapr, "test-topic");
        }

        @Override
        protected void handleEvent(String data) {
            handleCalled = true;
            if (throwPermanent) throw new IllegalArgumentException("invalid data");
            if (throwTransient) throw new RuntimeException("db connection lost");
        }
    }
}
