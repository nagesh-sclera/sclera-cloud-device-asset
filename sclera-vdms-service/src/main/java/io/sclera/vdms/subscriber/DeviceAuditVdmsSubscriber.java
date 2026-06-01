package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.DeviceAuditEvent;
import io.sclera.vdms.model.UserActionLog;
import io.sclera.vdms.repository.UserActionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
public class DeviceAuditVdmsSubscriber extends DaprEventSubscriber<DeviceAuditEvent> {

    private static final Logger log = LoggerFactory.getLogger(DeviceAuditVdmsSubscriber.class);
    private final UserActionLogRepository repo;

    public DeviceAuditVdmsSubscriber(DaprClient dapr, UserActionLogRepository repo) {
        super(dapr, "device.audit");
        this.repo = repo;
    }

    @Topic(name = "device.audit", pubsubName = "pubsub",
           deadLetterTopic = "device.audit.dlq")
    @PostMapping("/vdms/device-audit")
    public ResponseEntity<Map<String, String>> onDeviceAudit(
            @RequestBody CloudEvent<DeviceAuditEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(DeviceAuditEvent data) {
        if (data.vdmsId() == null || data.userEmail() == null || data.action() == null || data.deviceId() == null) {
            log.warn("DeviceAuditEvent has null fields vdmsId={} deviceId={} action={} userEmail={}",
                data.vdmsId(), data.deviceId(), data.action(), data.userEmail());
        }
        UserActionLog entry = new UserActionLog();
        entry.setId(UUID.randomUUID().toString());
        entry.setVdmsId(data.vdmsId() != null ? data.vdmsId() : "");
        entry.setUserEmail(data.userEmail() != null ? data.userEmail() : "system");
        entry.setType("device");
        entry.setAction(data.action() != null ? data.action() : "UNKNOWN");
        entry.setStatus(data.status() != null ? data.status() : "success");
        entry.setMessage(data.message() != null ? data.message() : "Device event");
        entry.setAffectedRecordId(data.deviceId() != null ? data.deviceId() : "");
        entry.setCreatedAt(LocalDateTime.now());
        repo.save(entry);
        log.info("[Audit] Logged device.{} for vdms={} deviceId={}",
            entry.getAction(), entry.getVdmsId(), entry.getAffectedRecordId());
    }
}
