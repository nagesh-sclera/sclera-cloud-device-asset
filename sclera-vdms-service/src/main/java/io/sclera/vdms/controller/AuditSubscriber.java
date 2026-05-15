package io.sclera.vdms.controller;

import io.sclera.vdms.model.UserActionLog;
import io.sclera.vdms.repository.UserActionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dapr subscriber for audit events published by sclera-cloud-device-asset.
 *
 * Topic: device.audit
 * Route: /vdms/device-audit
 *
 * Flow: device-asset → VdmsClient.publishEvent("device.audit", payload)
 *         → Redis → Dapr delivers CloudEvent here
 *         → persisted in user_action_log table
 */
@RestController
public class AuditSubscriber {

    private static final Logger log = LoggerFactory.getLogger(AuditSubscriber.class);

    private final UserActionLogRepository repo;

    public AuditSubscriber(UserActionLogRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/vdms/device-audit")
    public ResponseEntity<Void> onDeviceAudit(@RequestBody Map<String, Object> cloudEvent) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) cloudEvent.get("data");
            if (data == null) {
                log.warn("[Audit] Received device.audit event with no data");
                return ResponseEntity.ok().build();
            }

            UserActionLog entry = new UserActionLog();
            entry.setId(UUID.randomUUID().toString());
            entry.setVdmsId((String) data.getOrDefault("vdmsId", ""));
            entry.setUserEmail((String) data.getOrDefault("userEmail", "system"));
            entry.setType("device");
            entry.setAction((String) data.getOrDefault("action", "UNKNOWN"));
            entry.setStatus((String) data.getOrDefault("status", "success"));
            entry.setMessage((String) data.getOrDefault("message", "Device event from sclera-cloud-device-asset"));
            entry.setAffectedRecordId((String) data.getOrDefault("deviceId", ""));
            entry.setCreatedAt(LocalDateTime.now());

            repo.save(entry);
            log.info("[Audit] Logged device.{} for vdms={} deviceId={}",
                entry.getAction(), entry.getVdmsId(), entry.getAffectedRecordId());

        } catch (Exception e) {
            log.error("[Audit] Failed to persist audit event", e);
        }
        return ResponseEntity.ok().build();
    }

    /**
     * GET /vdms/audit-log?vdmsId={vdmsId}&page=0&size=20
     *
     * Returns device audit log entries for a VDMS instance, newest first.
     * size is capped at 100.
     */
    @GetMapping("/vdms/audit-log")
    public List<UserActionLog> getAuditLog(
            @RequestParam(required = false) String vdmsId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);
        if (vdmsId == null || vdmsId.isBlank()) {
            return repo.findAllByOrderByCreatedAtDesc(pageable).getContent();
        }
        return repo.findByVdmsIdOrderByCreatedAtDesc(vdmsId, pageable).getContent();
    }
}
