# Sclara 2.0 — Architecture

## System Overview

```
Internet / Frontend
       │ HTTP
       ▼
┌──────────────────────────┐
│     sclera-api-gateway   │  :8080  ← only port exposed to the outside
│     (Spring Cloud        │
│      Gateway)            │
└────────┬─────────────────┘
         │ Direct HTTP (plain reverse proxy — no Dapr)
    ┌────┴──────────────────────────────┐
    │ /asset/**                         │ /vdms/**
    ▼ (strips prefix)                   ▼ (pass-through)
┌──────────────────────────┐   ┌──────────────────────────┐
│  sclera-cloud-           │   │  sclera-vdms-service     │
│  device-asset  :8085     │   │                  :8089   │
│                          │   │                          │
│  VdmsClient  ────────────┼──►│  VdmsController    [GET] │ ← device-asset queries VDMS data
│  (sync invoke)           │◄──┤  (returns vdmsId,        │   (synchronous, via Dapr invoke)
│                          │   │   details, master, etc.) │
│  VdmsClient  ────────────┼──►│  VdmsSubscriber    [POST]│ ← device-asset pushes state changes
│  (pub/sub publish)       │   │  AuditSubscriber   [POST]│   (async, via Dapr pub/sub / Redis)
│                          │   │                          │
│  JDBC ──► MySQL :3306    │   │  JDBC ──► MySQL :3306    │
└──────────┬───────────────┘   └────────────┬─────────────┘
           │ localhost:3500                 │ localhost:3500
           ▼                                ▼
┌──────────────────────┐       ┌──────────────────────────┐
│  Dapr sidecar        │       │  Dapr sidecar            │
│  app-id:             │◄─────►│  app-id: vdms-service    │
│  sclera-cloud-       │ gRPC  │                          │
│  device-asset        │       │                          │
└──────────┬───────────┘       └────────────┬─────────────┘
           │         Pub/Sub (Redis stream)  │
           └──────────────────┬─────────────┘
                              ▼
                   ┌─────────────────────┐
                   │     Redis :6379     │
                   │  (pub/sub broker)   │
                   └─────────────────────┘
```

---

## Services

### `sclera-api-gateway` — Port 8080

Spring Cloud Gateway. The single entry point for all external HTTP traffic.

| Route | Forwards to | Notes |
|---|---|---|
| `/asset/**` | `sclera-cloud-device-asset:8085` | Strips `/asset` prefix |
| `/vdms/**` | `sclera-vdms-service:8089` | Pass-through |

No authentication is enforced at the gateway layer — JWT validation happens inside `sclera-cloud-device-asset`.

---

### `sclera-cloud-device-asset` — Port 8085

The core device and asset management service. Spring Boot 2.6.5, Java 17.

#### Package Structure

| Package | Responsibility |
|---|---|
| `controller/admin/` | REST endpoints — devices, buildings, locations, floors, sensors, documents, AI suggestions, etc. |
| `client/` | `VdmsClient` — internal HTTP client for vdms-service via Dapr sidecar |
| `service/` | Business logic — `DeviceService`, `VdmsService`, `BuildingService`, `EssentialService`, `UserActionLogService` (publishes `device.audit` events on add/edit/delete), etc. |
| `Repository/` | JPA/JDBC repositories for the shared MySQL `vdms` database |
| `models/` | JPA entity classes |
| `dto/` | Data transfer objects (request/response) |
| `config/` | Security (`WebSecurityConfig`), CORS, JWT filter, cache, server (SSL/HTTP redirect) |
| `auth/` | Multi-tenant JWT validation (`TenantJWSKeySelector`, `TenantJwtIssuerValidator`) |
| `rabbitmq/` | RabbitMQ integration (legacy messaging) |
| `stubs/` | Safe no-op stubs for stubbed-out bucket-C dependencies — never throw, emit one WARN per call |
| `integration/` | External system integrations (Corrigo, Daintree, etc.) |
| `sockets/` + `websocket/` | WebSocket support |
| `utils/` | Shared utilities |

#### Outbound Calls

| Destination | Mechanism | When |
|---|---|---|
| `vdms-service` | `VdmsClient` → Dapr sidecar (`localhost:3500/v1.0/invoke/vdms-service/method/...`) | Querying VDMS data from within the service layer |
| Redis pub/sub | `VdmsClient.publishEvent()` → Dapr sidecar (`localhost:3500/v1.0/publish/pubsub/{topic}`) | Publishing state-change events to vdms-service |
| External cloud APIs | `APICallService` / `RestTemplate` | app.sclera.com, AI services, third-party integrations |
| RabbitMQ | `RabbitmqService` | Legacy async messaging |

---

### `sclera-vdms-service` — Port 8089

Lightweight VDMS (Virtual Device Management System) data service. Spring Boot, Java.

#### Controllers

| Class | Endpoints | Role |
|---|---|---|
| `VdmsController` | `GET /vdms/id`<br>`GET /vdms/details`<br>`GET /vdms/master`<br>`GET /vdms/has-secondary-device`<br>`GET /vdms/secondary-device-id`<br>`GET /vdms/customer-org-id/{vdmsId}`<br>`GET /vdms/sync-details-for-adc`<br>`GET /dapr/subscribe` | Serves VDMS data; registers Dapr pub/sub subscriptions |
| `VdmsSubscriber` | `POST /vdms/update-property-details`<br>`POST /vdms/update-customer-org-id`<br>`POST /vdms/set-agent-permission` | Handles Dapr-delivered CloudEvents that mutate VDMS state |
| `AuditSubscriber` | `POST /vdms/device-audit`<br>`GET /vdms/audit-log?vdmsId=&page=0&size=20` | Dapr subscriber: persists `device.audit` CloudEvents into `user_action_log` table. GET returns paginated audit entries filtered by vdmsId, newest first (size capped at 100) |

---

### Dapr Sidecars

Each service runs a paired Dapr sidecar in the same network namespace (`network_mode: "service:<name>"`).

| Sidecar container | Serves | App-ID | Dapr HTTP port |
|---|---|---|---|
| `app-dapr` | `sclera-cloud-device-asset` | `sclera-cloud-device-asset` | `3500` |
| `vdms-dapr` | `sclera-vdms-service` | `vdms-service` | `3500` |

Both sidecars share a single `pubsub.yaml` component (Redis-backed, component name: `pubsub`).

---

### Infrastructure

| Container | Image | Port | Purpose |
|---|---|---|---|
| `scleravdmsdatabase` | `mysql:8` | `3307→3306` | Shared database (`vdms` schema) for both services |
| `sclera-redis` | `redis:7-alpine` | internal | Dapr pub/sub broker |

---

## Inter-Service Communication

The two services communicate in both directions. `sclera-cloud-device-asset` queries and writes to `sclera-vdms-service`; `sclera-vdms-service` stores results that clients (via api-gateway) then read back.

```
device-asset  ──(sync GET)──►  vdms-service   query VDMS identity/config data
device-asset  ──(async pub)──► vdms-service   push state-change and audit events
client        ──(GET via gw)─► vdms-service   read back persisted data (audit log, VDMS details)
```

### Channel 1 — Synchronous query (device-asset → vdms-service)

`VdmsClient` issues plain HTTP GETs to the local Dapr sidecar, which routes them to vdms-service:

```
VdmsClient.<method>()
  → GET localhost:3500/v1.0/invoke/vdms-service/method/{path}
  → Dapr routes to vdms-service:8089/{path}
  → returns Map<String, Object>
```

| VdmsClient method | vdms-service endpoint | Returns |
|---|---|---|
| `getVdmsId()` | `GET /vdms/id` | `{ "vdmsId": "..." }` |
| `getVdmsDetails()` | `GET /vdms/details` | Full VdmsDTO as map |
| `getMaster()` | `GET /vdms/master` | `{ "isMaster": 0\|1 }` |
| `getHasSecondaryDevice()` | `GET /vdms/has-secondary-device` | `{ "hasSecondaryDevice": 0\|1 }` |
| `getSecondaryDeviceId()` | `GET /vdms/secondary-device-id` | `{ "secondaryDeviceId": "..." }` |
| `getCustomerOrgId(vdmsId)` | `GET /vdms/customer-org-id/{vdmsId}` | `{ "customerOrgId": "..." }` |
| `getSyncDetailsForAdc()` | `GET /vdms/sync-details-for-adc` | Subset VdmsDTO as map |

### Channel 2 — Async event push (device-asset → vdms-service via Redis)

`VdmsClient.publishEvent()` POSTs to the Dapr sidecar pub/sub API. Dapr writes to Redis; the vdms-service Dapr sidecar delivers the CloudEvent to the registered subscriber handler:

```
VdmsClient.publishEvent(topic, payload)
  → POST localhost:3500/v1.0/publish/pubsub/{topic}
  → Dapr writes to Redis stream
  → vdms-service Dapr sidecar delivers CloudEvent to subscriber
```

| Topic | Triggered by | Subscriber handler | Effect |
|---|---|---|---|
| `vdms.update-property-details` | Property address change | `VdmsSubscriber.updatePropertyDetails()` | Updates address in VDMS table |
| `vdms.update-customer-org-id` | Org ID change | `VdmsSubscriber.updateCustomerOrgId()` | Updates customer org ID |
| `vdms.set-agent-permission` | Agent permission change | `VdmsSubscriber.setAgentPermission()` | Agent permission event |
| `device.audit` | Device add / edit / delete | `AuditSubscriber.onDeviceAudit()` | Persists audit log entry to `user_action_log` |

### Channel 3 — Client reads from vdms-service (via api-gateway)

Clients query data that was written by device-asset events. This closes the 2-way loop:

| Endpoint | Handler | Data source |
|---|---|---|
| `GET /vdms/audit-log?vdmsId=&page=&size=` | `AuditSubscriber.getAuditLog()` | `user_action_log` rows written by `device.audit` events |
| `GET /vdms/details` | `VdmsController` | VDMS table (updated by VdmsSubscriber events) |

---

## Request Flow Examples

**Device list query:**
```
Frontend → GET /asset/user/{u}/vdms/{id}/docker/{d}/devices
  → api-gateway strips /asset prefix
  → sclera-cloud-device-asset:8085/user/{u}/vdms/{id}/docker/{d}/devices
  → DeviceController → DeviceService
  → (if VDMS data needed) VdmsClient.getVdmsDetails()
      → Dapr sidecar → vdms-service → VdmsController → MySQL
  → returns device list
```

**Device add (with audit log):**
```
Frontend → POST /asset/user/{u}/vdms/{id}/docker/{d}/adddevice
  → DeviceController → DeviceService.addDevice()
      → persists to MySQL
      → UserActionLogService.addUserAction("ADD", ...)
            → VdmsClient.publishEvent("device.audit", payload)
                  → Dapr sidecar → Redis pub/sub
                        → vdms-service AuditSubscriber.onDeviceAudit()
                              → user_action_log table
```

**Audit log query:**
```
Frontend → GET /vdms/audit-log?vdmsId={id}&page=0&size=20
  → api-gateway passes through to vdms-service:8089
  → AuditSubscriber.getAuditLog()
  → UserActionLogRepository.findByVdmsIdOrderByCreatedAtDesc()
  → returns JSON array of log entries, newest first
```

---

## Technology Stack

| Layer | Technology |
|---|---|
| API Gateway | Spring Cloud Gateway |
| Services | Spring Boot 2.6.5, Java 17 |
| Service mesh | Dapr 1.12 (sidecar pattern) |
| Pub/Sub broker | Redis 7 (via Dapr `pubsub.redis` component) |
| Database | MySQL 8 |
| Auth | JWT (multi-tenant, per-issuer JWK validation) |
| Legacy messaging | RabbitMQ |
| Containerization | Docker Compose |
