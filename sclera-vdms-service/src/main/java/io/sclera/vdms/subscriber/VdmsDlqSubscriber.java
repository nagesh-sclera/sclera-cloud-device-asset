package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsDlqSubscriber {

    private static final Logger log = LoggerFactory.getLogger(VdmsDlqSubscriber.class);

    @Topic(name = "vdms.update-property-details.dlq", pubsubName = "pubsub")
    @PostMapping("/vdms/dlq/update-property-details")
    public ResponseEntity<Void> onPropertyUpdateDlq(
            @RequestBody CloudEvent<Map<String, Object>> event) {
        log.error("DLQ: vdms.update-property-details permanently failed eventId={} data={}",
            event.getId(), event.getData());
        return ResponseEntity.ok().build();
    }

    @Topic(name = "vdms.update-customer-org-id.dlq", pubsubName = "pubsub")
    @PostMapping("/vdms/dlq/update-customer-org-id")
    public ResponseEntity<Void> onCustomerOrgDlq(
            @RequestBody CloudEvent<Map<String, Object>> event) {
        log.error("DLQ: vdms.update-customer-org-id permanently failed eventId={} data={}",
            event.getId(), event.getData());
        return ResponseEntity.ok().build();
    }

    @Topic(name = "vdms.set-agent-permission.dlq", pubsubName = "pubsub")
    @PostMapping("/vdms/dlq/set-agent-permission")
    public ResponseEntity<Void> onAgentPermissionDlq(
            @RequestBody CloudEvent<Map<String, Object>> event) {
        log.error("DLQ: vdms.set-agent-permission permanently failed eventId={} data={}",
            event.getId(), event.getData());
        return ResponseEntity.ok().build();
    }

    @Topic(name = "device.audit.dlq", pubsubName = "pubsub")
    @PostMapping("/vdms/dlq/device-audit")
    public ResponseEntity<Void> onDeviceAuditDlq(
            @RequestBody CloudEvent<Map<String, Object>> event) {
        log.error("DLQ: device.audit permanently failed eventId={} data={}",
            event.getId(), event.getData());
        return ResponseEntity.ok().build();
    }
}
