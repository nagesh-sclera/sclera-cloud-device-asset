package io.sclera.dapr;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public abstract class DaprEventSubscriber<T> {

    private static final Logger log = LoggerFactory.getLogger(DaprEventSubscriber.class);
    private static final String IDEMPOTENCY_STORE = "statestore-idempotency";

    private final DaprClient dapr;
    protected final String topic;

    protected DaprEventSubscriber(DaprClient dapr, String topic) {
        this.dapr = dapr;
        this.topic = topic;
    }

    public final ResponseEntity<Map<String, String>> onEvent(CloudEvent<T> event) {
        if (event == null) {
            log.warn("Received null event on topic={}", topic);
            return drop();
        }
        String eventId = event.getId();

        if (eventId != null && isAlreadyProcessed(eventId)) {
            log.info("Idempotency hit topic={} eventId={}", topic, eventId);
            return success();
        }

        try {
            handleEvent(event.getData());
            if (eventId != null) markProcessed(eventId);
            log.info("Processed event topic={} eventId={}", topic, eventId);
            return success();
        } catch (Exception e) {
            if (isPermanentError(e)) {
                log.error("Permanent failure topic={} eventId={} error={}", topic, eventId, e.getMessage(), e);
                return drop();
            }
            log.warn("Transient failure topic={} eventId={} error={}", topic, eventId, e.getMessage());
            return retry();
        }
    }

    protected abstract void handleEvent(T data);

    protected boolean isPermanentError(Exception e) {
        return e instanceof DataIntegrityViolationException
            || e instanceof IllegalArgumentException
            || e instanceof NullPointerException;
    }

    private boolean isAlreadyProcessed(String eventId) {
        try {
            State<Boolean> s = dapr.getState(IDEMPOTENCY_STORE, eventId, Boolean.class).block();
            return s != null && Boolean.TRUE.equals(s.getValue());
        } catch (Exception e) {
            log.debug("Idempotency lookup failed eventId={}: {}", eventId, e.getMessage());
            return false;
        }
    }

    private void markProcessed(String eventId) {
        try {
            dapr.saveState(IDEMPOTENCY_STORE, eventId, Boolean.TRUE).block();
        } catch (Exception e) {
            log.debug("Idempotency write failed eventId={}: {}", eventId, e.getMessage());
        }
    }

    private static ResponseEntity<Map<String, String>> success() {
        return ResponseEntity.ok(Map.of("status", "SUCCESS"));
    }

    private static ResponseEntity<Map<String, String>> retry() {
        return ResponseEntity.ok(Map.of("status", "RETRY"));
    }

    private static ResponseEntity<Map<String, String>> drop() {
        return ResponseEntity.ok(Map.of("status", "DROP"));
    }
}
