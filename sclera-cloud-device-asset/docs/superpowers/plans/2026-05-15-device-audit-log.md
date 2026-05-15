# Device Audit Log Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When a device is added, edited, or deleted in `sclera-cloud-device-asset`, publish a `device.audit` Dapr pub/sub event that `sclera-vdms-service` persists and exposes via a paginated GET endpoint.

**Architecture:** `DeviceService` already calls `userActionLogService.addUserAction()` at the right points — the method body is currently an empty stub. We replace the stub with a real implementation that uses `VdmsClient.publishEvent("device.audit", payload)`. On the vdms-service side, `AuditSubscriber` already receives and persists the event; we just upgrade the GET endpoint from a hardcoded top-20 query to a paginated, vdmsId-filtered one.

**Tech Stack:** Spring Boot 2.6.5 / Java 17, Spring Data JPA (`Page`, `PageRequest`, `Pageable`), Dapr pub/sub via `VdmsClient` (RestTemplate → Dapr sidecar HTTP API)

---

## File Map

| Action | Service | File |
|---|---|---|
| Modify | device-asset | `src/main/java/io/sclera/service/UserActionLogService.java` |
| Modify | vdms-service | `src/main/java/io/sclera/vdms/repository/UserActionLogRepository.java` |
| Modify | vdms-service | `src/main/java/io/sclera/vdms/controller/AuditSubscriber.java` |

Tasks 1 and 2+3 are independent (different services). Task 3 depends on Task 2.

---

## Task 1: Replace UserActionLogService stub (device-asset)

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/service/UserActionLogService.java`

**Context:**
- `UserActionLogService` is currently a do-nothing stub (`/** STUB: replace with remote call to AP-C6 */`)
- `DeviceService` already injects and calls `userActionLogService.addUserAction(username, "asset", "ADD|UPDATE|DELETE", message, status, subType, deviceId)` — no changes needed there
- `VdmsClient` is the internal HTTP client for vdms-service (already implemented in `io.sclera.client.VdmsClient`)
- `VdmsRepository` in device-asset is also a stub that returns `null` for `getVDMSId()`, so we fetch vdmsId lazily via `VdmsClient.getVdmsId()` and cache it
- `UserActionLogDTO` fields: `email`, `type`, `action`, `message`, `status`, `sub_type`, `primary_id`

- [ ] **Step 1: Replace the file content**

Overwrite `sclera-cloud-device-asset/src/main/java/io/sclera/service/UserActionLogService.java` with:

```java
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
```

- [ ] **Step 2: Verify device-asset compiles**

Run from `sclera-cloud-device-asset/`:
```bash
mvn compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Confirm DeviceService still injects UserActionLogService without errors**

```bash
grep -n "userActionLogService" src/main/java/io/sclera/service/DeviceService.java | head -10
```
Expected: lines showing `@Autowired UserActionLogService userActionLogService` and several `addUserAction(...)` calls — no compile-time changes needed there.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/io/sclera/service/UserActionLogService.java
git commit -m "implement UserActionLogService — publish device.audit events via VdmsClient"
```

---

## Task 2: Add paginated query to UserActionLogRepository (vdms-service)

**Files:**
- Modify: `sclera-vdms-service/src/main/java/io/sclera/vdms/repository/UserActionLogRepository.java`

**Context:**
- Currently the repo has only `findTop20ByOrderByCreatedAtDesc()` — no filtering, no pagination
- Spring Data JPA derives the query from the method name automatically — no `@Query` annotation needed
- `findTop20ByOrderByCreatedAtDesc()` will be removed because `AuditSubscriber` will stop using it in Task 3

- [ ] **Step 1: Replace the file content**

Overwrite `sclera-vdms-service/src/main/java/io/sclera/vdms/repository/UserActionLogRepository.java` with:

```java
package io.sclera.vdms.repository;

import io.sclera.vdms.model.UserActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActionLogRepository extends JpaRepository<UserActionLog, Long> {
    Page<UserActionLog> findByVdmsIdOrderByCreatedAtDesc(String vdmsId, Pageable pageable);
}
```

- [ ] **Step 2: Verify vdms-service compiles**

Run from `sclera-vdms-service/`:
```bash
mvn compile -q
```
Expected: `BUILD SUCCESS` (AuditSubscriber still references `findTop20ByOrderByCreatedAtDesc` — it will fail here if Task 3 hasn't been done yet. That's OK — complete Task 3 immediately after and re-verify. Alternatively, do both steps before compiling.)

> **Note:** If compile fails because `AuditSubscriber` still calls the removed method, proceed directly to Task 3 Step 1 and then compile once after both edits are done.

- [ ] **Step 3: Commit (after Task 3 compiles successfully)**

```bash
git add src/main/java/io/sclera/vdms/repository/UserActionLogRepository.java
git commit -m "upgrade UserActionLogRepository — paginated findByVdmsId query"
```

---

## Task 3: Upgrade AuditSubscriber GET endpoint (vdms-service)

**Files:**
- Modify: `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/AuditSubscriber.java`

**Context:**
- Current `getAuditLog()` calls `repo.findTop20ByOrderByCreatedAtDesc()` — that method no longer exists after Task 2
- Replace it with a method that accepts `vdmsId` (required), `page` (default 0), `size` (default 20, max 100)
- `onDeviceAudit()` subscriber handler is unchanged — only the GET method changes
- Imports to add: `org.springframework.data.domain.PageRequest`, `org.springframework.data.domain.Pageable`, `org.springframework.web.bind.annotation.RequestParam`

- [ ] **Step 1: Read the current file**

Read `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/AuditSubscriber.java` to confirm the exact current `getAuditLog()` signature before editing.

- [ ] **Step 2: Replace the full file content**

Overwrite `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/AuditSubscriber.java` with:

```java
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
            @RequestParam String vdmsId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);
        return repo.findByVdmsIdOrderByCreatedAtDesc(vdmsId, pageable).getContent();
    }
}
```

- [ ] **Step 3: Verify vdms-service compiles**

Run from `sclera-vdms-service/`:
```bash
mvn compile -q
```
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit both Task 2 and Task 3 changes together**

```bash
git add src/main/java/io/sclera/vdms/repository/UserActionLogRepository.java \
        src/main/java/io/sclera/vdms/controller/AuditSubscriber.java
git commit -m "upgrade audit log — paginated GET /vdms/audit-log?vdmsId=&page=&size="
```

---

## Post-Implementation Verification

- [ ] Start the full stack: `docker compose up --build` from `C:/Users/KNageshNayak/Desktop/Work/Sclara-2.0/`
- [ ] Add a device via: `POST /asset/user/{username}/vdms/{vdmsid}/docker/{dockername}/adddevice`
- [ ] Query audit log: `GET /vdms/audit-log?vdmsId={vdmsId}&page=0&size=20` — expect a JSON array with at least one entry showing `"action": "ADD"`
- [ ] Edit the device via: `POST /asset/user/{username}/vdms/{vdmsid}/docker/{dockername}/device/{device_id}/edit`
- [ ] Query again — expect an `"action": "UPDATE"` entry
- [ ] Delete via: `DELETE /asset/user/{username}/vdms/{vdmsid}/docker/{dockername}/deletedevices`
- [ ] Query again — expect a `"action": "DELETE"` entry
