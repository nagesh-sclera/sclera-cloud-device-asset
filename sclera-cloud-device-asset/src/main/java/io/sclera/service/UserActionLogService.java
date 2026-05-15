package io.sclera.service;

import io.sclera.client.VdmsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserActionLogService {

    private static final Logger log = LoggerFactory.getLogger(UserActionLogService.class);

    @Autowired
    private VdmsClient vdmsClient;

    private volatile String cachedVdmsId;

    public void addUserAction(String username, String type, String action,
                              String message, String status, String subType, String recordId) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("vdmsId",    resolveVdmsId());
            payload.put("userEmail", username  != null ? username  : "system");
            payload.put("action",    action);
            payload.put("status",    status);
            payload.put("message",   message);
            payload.put("deviceId",  recordId  != null ? recordId  : "");
            log.info("[AuditLog] publishing device.audit | action={} device={} user={} vdmsId={}",
                     action, recordId, username, payload.get("vdmsId"));
            vdmsClient.publishEvent("device.audit", payload);
        } catch (Exception e) {
            log.warn("[UserActionLogService] Failed to publish audit event action={}: {}", action, e.getMessage());
        }
    }

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
