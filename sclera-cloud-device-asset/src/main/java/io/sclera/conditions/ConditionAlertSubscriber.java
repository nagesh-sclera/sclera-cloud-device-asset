package io.sclera.conditions;

import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import io.sclera.service.ConditionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Subscribes to {@code device.condition-alert}, published by the measuring-instrument service on
 * its hot per-reading path. Reconstructs the monolith's exact in-process call
 * {@code conditionsService.updateConditionAlert("measuring_instrument", id, "", value, "", "sync")}.
 * Idempotent (last-value-wins), so redelivery is safe; never throws.
 */
@RestController
public class ConditionAlertSubscriber {

    private static final Logger log = LoggerFactory.getLogger(ConditionAlertSubscriber.class);

    private final ConditionsService conditionsService;

    public ConditionAlertSubscriber(ConditionsService conditionsService) {
        this.conditionsService = conditionsService;
    }

    @Topic(name = "device.condition-alert", pubsubName = "pubsub")
    @PostMapping("/api/v1/device-asset-service/internal/device-condition-alert")
    public ResponseEntity<Void> onConditionAlert(@RequestBody CloudEvent<Map<String, String>> event) {
        log.info("onConditionAlert eventId={}", event != null ? event.getId() : null);
        handleEvent(event != null ? event.getData() : null);
        return ResponseEntity.ok().build();
    }

    /** Applies the alert update. Package-visible for unit testing without a CloudEvent envelope. */
    void handleEvent(Map<String, String> data) {
        if (data == null) {
            log.warn("device.condition-alert with null payload; ignoring");
            return;
        }
        String measuringInstrumentId = data.get("measuringInstrumentId");
        String value = data.get("value");
        conditionsService.updateConditionAlert("measuring_instrument", measuringInstrumentId, "", value, "", "sync");
    }
}
