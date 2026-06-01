package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.VdmsCustomerOrgEvent;
import io.sclera.vdms.repository.VdmsJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsCustomerOrgSubscriber extends DaprEventSubscriber<VdmsCustomerOrgEvent> {

    private static final Logger log = LoggerFactory.getLogger(VdmsCustomerOrgSubscriber.class);
    private final VdmsJpaRepository repo;

    public VdmsCustomerOrgSubscriber(DaprClient dapr, VdmsJpaRepository repo) {
        super(dapr, "vdms.update-customer-org-id");
        this.repo = repo;
    }

    @Topic(name = "vdms.update-customer-org-id", pubsubName = "pubsub",
           deadLetterTopic = "vdms.update-customer-org-id.dlq")
    @PostMapping("/vdms/update-customer-org-id")
    public ResponseEntity<Map<String, String>> onCustomerOrgUpdate(
            @RequestBody CloudEvent<VdmsCustomerOrgEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(VdmsCustomerOrgEvent data) {
        if (data.vdmsId() == null || data.customerOrgId() == null) {
            throw new IllegalArgumentException("vdmsId and customerOrgId are required");
        }
        repo.updateCustomerOrgId(data.vdmsId(), data.customerOrgId());
        log.info("Updated customerOrgId for vdms {}", data.vdmsId());
    }
}
