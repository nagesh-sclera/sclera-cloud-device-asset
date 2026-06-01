package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.VdmsPropertyUpdateEvent;
import io.sclera.vdms.repository.VdmsJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsPropertyUpdateSubscriber extends DaprEventSubscriber<VdmsPropertyUpdateEvent> {

    private static final Logger log = LoggerFactory.getLogger(VdmsPropertyUpdateSubscriber.class);
    private final VdmsJpaRepository repo;

    public VdmsPropertyUpdateSubscriber(DaprClient dapr, VdmsJpaRepository repo) {
        super(dapr, "vdms.update-property-details");
        this.repo = repo;
    }

    @Topic(name = "vdms.update-property-details", pubsubName = "pubsub",
           deadLetterTopic = "vdms.update-property-details.dlq")
    @PostMapping("/vdms/update-property-details")
    public ResponseEntity<Map<String, String>> onPropertyUpdate(
            @RequestBody CloudEvent<VdmsPropertyUpdateEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(VdmsPropertyUpdateEvent data) {
        if (data.id() == null || data.address() == null) {
            throw new IllegalArgumentException("id and address are required");
        }
        repo.updateAddress(data.id(), data.address());
        log.info("Updated address for vdms {}", data.id());
    }
}
