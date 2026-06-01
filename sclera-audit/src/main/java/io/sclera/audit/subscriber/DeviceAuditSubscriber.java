package io.sclera.audit.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.DeviceAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeviceAuditSubscriber extends DaprEventSubscriber<DeviceAuditEvent> {

    private static final Logger log = LoggerFactory.getLogger(DeviceAuditSubscriber.class);

    public DeviceAuditSubscriber(DaprClient dapr) {
        super(dapr, "device.audit-recorded");
    }

    @Topic(name = "device.audit-recorded", pubsubName = "pubsub",
           deadLetterTopic = "device.audit-recorded.dlq")
    @PostMapping("/internal/device-audit")
    public ResponseEntity<Map<String, String>> onDeviceAudit(
            @RequestBody CloudEvent<DeviceAuditEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(DeviceAuditEvent data) {
        log.info("[audit] device.{} vdms={} device={} user={}",
            data.action(), data.vdmsId(), data.deviceId(), data.userEmail());
    }
}
