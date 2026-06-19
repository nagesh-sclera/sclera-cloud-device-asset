package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.UserActionLogEvent;
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

/**
 * Consumes audit entries published by sclera-workorders to {@code user-action-log-events}
 * and persists them into the user_action_log table (vdms-service owns audit storage).
 * Mirrors {@link DeviceAuditVdmsSubscriber}.
 */
@RestController
public class UserActionLogVdmsSubscriber extends DaprEventSubscriber<UserActionLogEvent> {

    private static final Logger log = LoggerFactory.getLogger(UserActionLogVdmsSubscriber.class);
    private final UserActionLogRepository repo;

    public UserActionLogVdmsSubscriber(DaprClient dapr, UserActionLogRepository repo) {
        super(dapr, "user-action-log-events");
        this.repo = repo;
    }

    @Topic(name = "user-action-log-events", pubsubName = "pubsub",
           deadLetterTopic = "user-action-log-events.dlq")
    @PostMapping("/vdms/user-action-log")
    public ResponseEntity<Map<String, String>> onUserActionLog(
            @RequestBody CloudEvent<UserActionLogEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(UserActionLogEvent data) {
        UserActionLog entry = new UserActionLog();
        entry.setId(UUID.randomUUID().toString());
        entry.setVdmsId(data.vdmsId());
        entry.setUserEmail(data.email());
        entry.setType(data.type());
        entry.setAction(data.action());
        entry.setStatus(data.status());
        entry.setMessage(data.message());
        entry.setAffectedRecordId(data.primaryId());
        entry.setCreatedAt(LocalDateTime.now());
        repo.save(entry);
        log.info("[Audit] Logged {}.{} for vdms={} primaryId={}",
            entry.getType(), entry.getAction(), entry.getVdmsId(), entry.getAffectedRecordId());
    }
}
