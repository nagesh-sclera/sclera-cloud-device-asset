package io.sclera.dapr;

import io.dapr.client.DaprClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DaprEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DaprEventPublisher.class);
    private final DaprClient dapr;

    public DaprEventPublisher(DaprClient dapr) {
        this.dapr = dapr;
    }

    public PublishResult publish(String pubsubName, String topic, Object payload) {
        String eventId = UUID.randomUUID().toString();
        try {
            dapr.publishEvent(pubsubName, topic, payload).block();
            log.info("Published event topic={} eventId={}", topic, eventId);
            return new PublishResult(true, eventId, null);
        } catch (Exception e) {
            log.error("Publish failed topic={} eventId={} error={}", topic, eventId, e.getMessage());
            return new PublishResult(false, eventId, e.getMessage());
        }
    }
}
