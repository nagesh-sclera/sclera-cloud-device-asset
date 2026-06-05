package io.sclera.service;

import io.sclera.client.VdmsClient;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.DeviceAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Records user and device audit actions by publishing {@link DeviceAuditEvent} messages
 * to the {@code device.audit} topic via {@link VdmsClient}.
 *
 * <p>The originating VDMS identifier is resolved lazily through {@link VdmsClient} and cached
 * for the lifetime of the service. Publishing is best-effort: failures are logged and swallowed
 * rather than propagated to callers.</p>
 */
@Service
public class UserActionLogService {

    private static final Logger log = LoggerFactory.getLogger(UserActionLogService.class);

    @Autowired
    private VdmsClient vdmsClient;

    private volatile String cachedVdmsId;

    /**
     * Builds and publishes a single device audit event to the {@code device.audit} topic.
     *
     * @param username the acting user; defaults to {@code "system"} when {@code null}
     * @param type the action type, used for logging context
     * @param action the action performed, carried on the published event
     * @param message a human-readable description of the action
     * @param status the outcome status of the action
     * @param subType the action sub-type, used for logging context
     * @param recordId the identifier of the affected record; defaults to an empty string when {@code null}
     */
    public void addUserAction(String username, String type, String action,
                              String message, String status, String subType, String recordId) {
        try {
            DeviceAuditEvent event = new DeviceAuditEvent(
                resolveVdmsId(),
                recordId  != null ? recordId  : "",
                action,
                status,
                message,
                username != null ? username : "system"
            );
            log.info("[AuditLog] publishing device.audit | type={} action={} device={} user={} vdmsId={}",
                     type, action, recordId, username, event.vdmsId());
            PublishResult result = vdmsClient.publishEvent("device.audit", event);
            if (!result.success()) {
                log.error("[AuditLog] Publish failed action={} eventId={} error={}",
                    action, result.eventId(), result.error());
            }
        } catch (Exception e) {
            log.warn("[UserActionLogService] Failed to publish audit event action={}: {}", action, e.getMessage());
        }
    }

    /**
     * Publishes an audit event for each entry in the supplied batch via {@link #addUserAction}.
     *
     * @param logs the user action log entries to publish; a {@code null} list is ignored
     */
    public void batchUpdateUserActionLogs(List<UserActionLogDTO> logs) {
        if (logs == null) return;
        for (UserActionLogDTO entry : logs) {
            addUserAction(entry.getEmail(), entry.getType(), entry.getAction(),
                          entry.getMessage(), entry.getStatus(),
                          entry.getSub_type(), entry.getPrimary_id());
        }
    }

    private String resolveVdmsId() {
        if (cachedVdmsId == null) {
            try {
                Map<String, Object> result = vdmsClient.getVdmsId();
                if (result != null && result.get("vdmsId") != null) {
                    cachedVdmsId = (String) result.get("vdmsId");
                }
            } catch (Exception e) {
                log.warn("[UserActionLogService] Could not fetch vdmsId from vdms-service: {}", e.getMessage());
            }
        }
        return cachedVdmsId != null ? cachedVdmsId : "";
    }
}
