# Dapr Pub/Sub Production Hardening — Design Spec

**Date:** 2026-06-01  
**Status:** Approved  
**Scope:** Production-grade Dapr pub/sub patterns across all Sclera 2.0 microservices

---

## Problem Statement

The current Dapr pub/sub implementation has correctness and reliability gaps that are unsafe for production:

| Gap | Risk |
|-----|------|
| Subscribers return `void` or always-200 `ResponseEntity<Void>` | Failed events silently acknowledged; no replay |
| Idempotency missing on `VdmsSubscriber` and `AuditSubscriber` | Duplicate processing on Dapr redelivery |
| All exceptions swallowed with `log.warn` | Failures invisible to Dapr; events lost |
| CloudEvents extracted via unsafe `Map.get("data")` casts | NPE and type errors at runtime |
| Publisher `publishEvent()` returns void and silently fails | Callers have no signal that delivery failed |
| No dead-letter queue configured | Permanently failing messages dropped silently |
| Three different subscriber patterns across services | Maintenance burden; no consistent guarantees |

---

## Solution: Shared `dapr-commons` Module + Standardized Patterns

### Approach

Create a `dapr-commons` Maven module shared by all services. It provides:
- `DaprEventSubscriber<T>` — abstract base class centralizing all production concerns
- `DaprEventPublisher` — replaces ad-hoc `publishEvent()` with result feedback
- Typed event DTOs (Java records) replacing `Map<String, Object>` payloads

Each service only implements business logic. All production concerns are inherited.

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    dapr-commons (new module)                 │
│                                                              │
│  DaprEventSubscriber<T>   ← abstract base class             │
│    ├── idempotency check (statestore-idempotency)            │
│    ├── CloudEvent<T> deserialization (typed DTO)             │
│    ├── response semantics (SUCCESS / RETRY / DROP)           │
│    └── structured logging + trace ID propagation            │
│                                                              │
│  DaprEventPublisher       ← replaces ad-hoc publishEvent()  │
│    ├── PublishResult return type (not void)                  │
│    ├── structured logging with event ID                      │
│    └── caller-actionable failure signal                      │
│                                                              │
│  Event DTOs (Java records)                                   │
│    ├── DeviceAuditEvent                                      │
│    ├── VdmsPropertyUpdateEvent                               │
│    ├── VdmsCustomerOrgEvent                                  │
│    └── VdmsAgentPermissionEvent                              │
└─────────────────────────────────────────────────────────────┘
         ↓ Maven dependency
┌──────────────────┐  ┌─────────────────┐  ┌────────────────────┐
│ sclera-vdms-     │  │  sclera-audit   │  │ sclera-cloud-      │
│ service          │  │                 │  │ device-asset       │
│                  │  │                 │  │                    │
│ VdmsSubscriber   │  │ AuditSubscriber │  │ VdmsClient         │
│ extends base     │  │ extends base    │  │ uses publisher     │
│ — business logic │  │ — business logic│  │                    │
└──────────────────┘  └─────────────────┘  └────────────────────┘
```

---

## Components

### 1. `DaprEventSubscriber<T>` (abstract base class)

Location: `dapr-commons/src/main/java/io/sclera/dapr/DaprEventSubscriber.java`

**Responsibilities:**
- Extracts `cloudEvent.getId()` as idempotency key
- Checks `statestore-idempotency` before delegating to `handleEvent()`
- Calls `isPermanentError(e)` to classify exceptions
- Returns `TopicEventResponse.SUCCESS`, `RETRY`, or `DROP` correctly
- Writes idempotency key to state store on successful processing
- Emits structured log on every path (event ID, topic, outcome)

**Contract:**
```java
public abstract class DaprEventSubscriber<T> {

    // Dapr calls the concrete @PostMapping; it delegates here
    public final Mono<TopicEventResponse> onEvent(CloudEvent<T> event);

    // Subclass implements only business logic
    protected abstract void handleEvent(T data);

    // Subclass classifies exceptions: true → DROP, false → RETRY
    protected boolean isPermanentError(Exception e) {
        return e instanceof DataIntegrityViolationException
            || e instanceof IllegalArgumentException;
    }
}
```

**Decision table:**

| Condition | Dapr response | Idempotency written? |
|-----------|--------------|----------------------|
| Duplicate event ID | SUCCESS | No (already written) |
| `handleEvent` succeeds | SUCCESS | Yes |
| `handleEvent` throws, `isPermanentError` = false | RETRY | No |
| `handleEvent` throws, `isPermanentError` = true | DROP | No |

### 2. `DaprEventPublisher`

Location: `dapr-commons/src/main/java/io/sclera/dapr/DaprEventPublisher.java`

Wraps `DaprClient.publishEvent()` and returns `PublishResult` so callers can act on failure.

```java
public record PublishResult(boolean success, String eventId, String error) {}

public class DaprEventPublisher {
    public PublishResult publish(String pubsubName, String topic, Object payload);
}
```

Replaces `VdmsClient.publishEvent()`, `RabbitmqClient` methods, and `ScleraCloudDeviceClient.publishEvent()` (which uses raw HTTP instead of the SDK — that inconsistency is eliminated).

### 3. Typed Event DTOs

Location: `dapr-commons/src/main/java/io/sclera/dapr/events/`

```java
public record DeviceAuditEvent(
    String deviceId, String action, String userEmail, Instant timestamp) {}

public record VdmsPropertyUpdateEvent(String vdmsId, String address) {}

public record VdmsCustomerOrgEvent(String vdmsId, String customerOrgId) {}

public record VdmsAgentPermissionEvent(
    String vdmsId, String agentId, String permission) {}
```

Replace all `Map<String, Object>` payloads on both publisher and subscriber sides.

### 4. Subscription Registration: `@Topic` annotations

Replace the manual `VdmsController.daprSubscribe()` map with `@Topic` on each subscriber method.

```java
// Before (runtime-only, manual, error-prone)
@GetMapping("/dapr/subscribe")
public List<Map<String, String>> daprSubscribe() { ... }

// After (declarative, build-time discoverable)
@Topic(name = "vdms.update-property-details", pubsubName = "pubsub")
@PostMapping("/vdms/update-property-details")
public Mono<TopicEventResponse> onPropertyUpdate(CloudEvent<VdmsPropertyUpdateEvent> event) {
    return super.onEvent(event);
}
```

### 5. Dead-Letter Queue Configuration

Each topic gets a companion DLQ topic in `pubsub.yaml`. Dapr routes DROP responses there automatically.

```yaml
# dapr/components/local/pubsub.yaml additions
# Per-topic subscriptions with deadLetterTopic declared in component metadata
```

DLQ topics: `device.audit.dlq`, `vdms.update-property-details.dlq`, `vdms.update-customer-org-id.dlq`, `vdms.set-agent-permission.dlq`

A lightweight DLQ subscriber in each service logs the failed event with full payload for manual inspection. No business logic.

---

## Error Handling

### Subscriber error flow

```
Dapr delivers event
        ↓
DaprEventSubscriber.onEvent()
        ↓
  idempotency check ──(duplicate)──→ return SUCCESS (no-op, logged)
        ↓ (first time)
  handleEvent(data)
        ↓
  ┌──────────────────────────────────────────────────┐
  │ No exception → write idempotency key             │
  │              → return SUCCESS                    │
  │                                                  │
  │ Exception, isPermanentError=false (transient)    │
  │              → log.warn with event ID + topic    │
  │              → return RETRY                      │
  │              → Dapr redelivers per resiliency    │
  │                                                  │
  │ Exception, isPermanentError=true (permanent)     │
  │              → log.error with full payload       │
  │              → return DROP                       │
  │              → Dapr routes to <topic>.dlq        │
  └──────────────────────────────────────────────────┘
```

### Publisher error flow

```java
PublishResult result = publisher.publish("pubsub", "device.audit", event);
if (!result.success()) {
    log.error("Publish failed topic=device.audit eventId={} error={}",
        result.eventId(), result.error());
    // caller decides: throw, record metric, or retry
}
```

### Permanent vs transient classification (defaults in base class, overridable)

| Exception type | Default classification |
|---------------|----------------------|
| `DataIntegrityViolationException` | Permanent (DROP) |
| `IllegalArgumentException` | Permanent (DROP) |
| `NullPointerException` | Permanent (DROP) |
| `DataAccessResourceFailureException` | Transient (RETRY) |
| `TransientDataAccessException` | Transient (RETRY) |
| Any other `RuntimeException` | Transient (RETRY) |

---

## Services Affected

| Service | Change |
|---------|--------|
| `dapr-commons` | **New module** — base class, publisher, DTOs |
| `sclera-cloud-device-asset` | `VdmsClient.publishEvent()` → `DaprEventPublisher`; DTO payloads |
| `sclera-vdms-service` | `VdmsSubscriber` extends base; `AuditSubscriber` extends base; remove manual `/dapr/subscribe`; `ScleraCloudDeviceClient` uses SDK not raw HTTP |
| `sclera-audit` | `DeviceAuditSubscriber` already closest to target — migrate to base class, remove boilerplate |
| `sclera-cloud-device-asset` | `RabbitmqClient` uses `DaprEventPublisher` |
| `dapr/components/local/pubsub.yaml` | Add DLQ topic configuration |
| `dapr/components/k8s/pubsub.yaml` | Add DLQ topic configuration |
| Root `pom.xml` | Register `dapr-commons` as module |

---

## Testing Strategy

### Layer 1 — Unit tests on business logic

Each `handleEvent()` implementation is a plain method with no Dapr dependency. Test with typed DTOs directly. No mocking of Dapr.

### Layer 2 — Integration tests on `DaprEventSubscriber`

Stub `DaprClient` (in-memory). Verify:
- Duplicate event ID → `SUCCESS` without calling `handleEvent()`
- Transient exception → `RETRY`
- Permanent exception → `DROP`
- First-time success → idempotency key written → `SUCCESS`

### Layer 3 — Component tests (Docker Compose)

Use existing Redis + real Dapr sidecar stack. Publish an event via `DaprEventPublisher`. Assert:
- Subscriber processes it exactly once (publish same event twice → one DB row)
- DLQ subscriber receives event after forced permanent failure

---

## Out of Scope

- Kubernetes manifests and cloud infrastructure
- Service invocation hardening (VdmsClient cache logic)
- Secret store migration
- API Gateway Dapr integration
- Other Dapr building blocks (bindings, actors)

---

## File Locations (New)

```
dapr-commons/
  pom.xml
  src/main/java/io/sclera/dapr/
    DaprEventSubscriber.java
    DaprEventPublisher.java
    PublishResult.java
    events/
      DeviceAuditEvent.java
      VdmsPropertyUpdateEvent.java
      VdmsCustomerOrgEvent.java
      VdmsAgentPermissionEvent.java
  src/test/java/io/sclera/dapr/
    DaprEventSubscriberTest.java
    DaprEventPublisherTest.java
```
