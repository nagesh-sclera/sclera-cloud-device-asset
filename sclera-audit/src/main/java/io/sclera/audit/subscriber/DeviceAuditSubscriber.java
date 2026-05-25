package io.sclera.audit.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PreDestroy;
import java.util.Map;

@RestController
public class DeviceAuditSubscriber {
    private static final Logger log = LoggerFactory.getLogger(DeviceAuditSubscriber.class);
    private static final String IDEMPOTENCY_STORE = "statestore-idempotency";

    private final DaprClient dapr = new DaprClientBuilder().build();

    @PreDestroy
    public void close() {
        try { dapr.close(); } catch (Exception ignored) { }
    }

    @Topic(name = "device.audit-recorded", pubsubName = "pubsub")
    @PostMapping("/internal/device-audit")
    public ResponseEntity<Void> onDeviceAudit(@RequestBody CloudEvent<Map<String, Object>> evt) {
        String eventId = evt.getId();
        if (eventId != null && isAlreadyProcessed(eventId)) {
            log.info("[skeleton] idempotency hit, skipping eventId={}", eventId);
            return ResponseEntity.ok().build();
        }

        log.info("[skeleton] received {}: id={}", evt.getType(), eventId);
        // Real audit-write would go here.

        if (eventId != null) markProcessed(eventId);
        return ResponseEntity.ok().build();
    }

    private boolean isAlreadyProcessed(String eventId) {
        try {
            State<Boolean> s = dapr.getState(IDEMPOTENCY_STORE, eventId, Boolean.class).block();
            return s != null && Boolean.TRUE.equals(s.getValue());
        } catch (Exception e) {
            log.debug("idempotency lookup failed for {}: {}", eventId, e.getMessage());
            return false;
        }
    }

    private void markProcessed(String eventId) {
        try {
            dapr.saveState(IDEMPOTENCY_STORE, eventId, Boolean.TRUE).block();
        } catch (Exception e) {
            log.debug("idempotency write failed for {}: {}", eventId, e.getMessage());
        }
    }
}
