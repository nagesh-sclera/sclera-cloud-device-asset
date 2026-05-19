package io.sclera.audit.subscriber;

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
public class DeviceAuditSubscriber {
    private static final Logger log = LoggerFactory.getLogger(DeviceAuditSubscriber.class);

    @Topic(name = "device.audit-recorded", pubsubName = "pubsub")
    @PostMapping("/internal/device-audit")
    public ResponseEntity<Void> onDeviceAudit(@RequestBody CloudEvent<Map<String, Object>> evt) {
        log.info("[skeleton] received {}: id={}", evt.getType(), evt.getId());
        return ResponseEntity.ok().build();
    }
}
