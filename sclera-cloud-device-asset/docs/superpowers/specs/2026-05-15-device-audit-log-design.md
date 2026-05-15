# Device Audit Log — Design Spec

## Goal

When a device is added, edited, or deleted in `sclera-cloud-device-asset`, a user action log entry is published via Dapr pub/sub to `sclera-vdms-service`, which persists it and exposes it via a paginated, filtered GET endpoint.

## Architecture

```
DeviceService (add/edit/delete)
  └─► UserActionLogService.addUserAction()   [device-asset]
        └─► VdmsClient.publishEvent("device.audit", payload)
              └─► Dapr sidecar (localhost:3500)
                    └─► Redis pub/sub
                          └─► AuditSubscriber.onDeviceAudit()  [vdms-service]
                                └─► UserActionLogRepository.save()
                                      └─► user_action_log table (MySQL)

Client
  └─► GET /vdms/audit-log?vdmsId=xxx&page=0&size=20  [via api-gateway → vdms-service]
        └─► AuditSubscriber.getAuditLog()
              └─► UserActionLogRepository.findByVdmsId...()
```

## What Already Exists (no changes)

- `DeviceService` already calls `userActionLogService.addUserAction(...)` at the correct points for add, edit, and delete
- `AuditSubscriber.onDeviceAudit()` already subscribes to `device.audit` and persists to `user_action_log`
- `UserActionLog` model already has: `vdmsId`, `userEmail`, `action`, `status`, `message`, `affectedRecordId`, `createdAt`
- API gateway already routes `/vdms/**` → vdms-service (no gateway changes needed)

---

## Changes

### 1. `sclera-cloud-device-asset` — `UserActionLogService.java`

**File:** `src/main/java/io/sclera/service/UserActionLogService.java`

Replace the empty stub with a real implementation.

**Behaviour:**
- Inject `VdmsClient`
- Lazy-cache the vdmsId: on first call, invoke `VdmsClient.getVdmsId()` once and cache the result in a `volatile String` field. Subsequent calls reuse the cached value. If the call fails, use empty string and retry next time (`cachedVdmsId` stays null)
- `addUserAction()` builds the audit payload and calls `VdmsClient.publishEvent("device.audit", payload)`
- `batchUpdateUserActionLogs()` iterates the list and calls `addUserAction()` per entry
- All exceptions are caught and logged — audit publishing must never break device CRUD

**Payload structure:**
```json
{
  "vdmsId":    "<cached from VdmsClient>",
  "userEmail": "<username param>",
  "action":    "<action param — ADD | UPDATE | DELETE>",
  "status":    "<status param — success | failed>",
  "message":   "<message param>",
  "deviceId":  "<recordId param>"
}
```

**No call site changes** — `DeviceService` already calls `addUserAction` with the right arguments.

---

### 2. `sclera-vdms-service` — `UserActionLogRepository.java`

**File:** `src/main/java/io/sclera/vdms/repository/UserActionLogRepository.java`

Add one Spring Data method:

```java
Page<UserActionLog> findByVdmsIdOrderByCreatedAtDesc(String vdmsId, Pageable pageable);
```

The existing `findTop20ByOrderByCreatedAtDesc()` can be removed — it is replaced by the new paginated method.

---

### 3. `sclera-vdms-service` — `AuditSubscriber.java`

**File:** `src/main/java/io/sclera/vdms/controller/AuditSubscriber.java`

Replace `getAuditLog()` with a paginated, filtered version:

**Endpoint:**
```
GET /vdms/audit-log?vdmsId={vdmsId}&page=0&size=20
```

| Param | Type | Required | Default | Description |
|---|---|---|---|---|
| `vdmsId` | String | yes | — | Scope logs to this VDMS instance |
| `page` | int | no | `0` | Zero-based page number |
| `size` | int | no | `20` | Page size (max 100) |

**Response:** `200 OK` with JSON array of log entries, newest first.

```json
[
  {
    "id": 1,
    "vdmsId": "vdms-abc",
    "userEmail": "user@example.com",
    "type": "device",
    "action": "ADD",
    "status": "success",
    "message": "A Device name: Sensor-1 and id: vdms-abc_net_Sensor-1_123 is added for network net",
    "affectedRecordId": "vdms-abc_net_Sensor-1_123",
    "createdAt": "2026-05-15T10:30:00"
  }
]
```

The `onDeviceAudit()` subscriber handler is unchanged.

---

## Data Flow Summary

| Step | Where | What happens |
|---|---|---|
| 1 | device-asset `DeviceService` | Calls `userActionLogService.addUserAction(username, "asset", "ADD\|UPDATE\|DELETE", message, status, subType, deviceId)` |
| 2 | device-asset `UserActionLogService` | Builds payload, calls `VdmsClient.publishEvent("device.audit", payload)` |
| 3 | Dapr sidecar (device-asset) | Routes to Redis pub/sub topic `device.audit` |
| 4 | Dapr sidecar (vdms-service) | Delivers CloudEvent to vdms-service |
| 5 | vdms-service `AuditSubscriber` | Persists to `user_action_log` table |
| 6 | Client | `GET /vdms/audit-log?vdmsId=xxx` via api-gateway → vdms-service |

## Out of Scope

- Auth on the audit log endpoint (handled by api-gateway / JWT filter upstream)
- Retry logic for failed publishes (Dapr/Redis guarantees at-least-once delivery once sidecar accepts)
- Date-range filtering (not requested; add in a future iteration)
