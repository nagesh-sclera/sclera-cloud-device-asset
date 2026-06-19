# Sclera 2.0 — Architecture

> **Purpose of this document:** a single reference you can walk your team through to explain what
> Sclera 2.0 is, how the pieces fit together, and how a request flows end-to-end. It reflects the
> current state of the repo after the platform migration (Java 21 / Spring Boot 4 / PostgreSQL /
> Dapr / full observability stack).

---

## 1. The Big Picture

Sclera 2.0 is a **microservices platform** built as one Maven reactor. A React frontend talks to a
single API gateway; the gateway fans traffic out to a set of Spring Boot services; services
communicate with each other through **Dapr** (synchronous service invocation + asynchronous
pub/sub over Redis); everything is observable through an OpenTelemetry → Jaeger / Prometheus /
Grafana / Loki stack.

```
                          ┌─────────────────────────────┐
   Browser (React/Vite)   │   sclera-ui   :3000         │
   ───────────────────────│   (asset mgmt dashboard)    │
                          └──────────────┬──────────────┘
                                         │ HTTP (VITE_API_BASE)
                                         ▼
                        ┌────────────────────────────────────┐
                        │   sclera-api-gateway   :8080        │ ← only port exposed externally
                        │   (Spring Cloud Gateway, WebFlux)   │
                        └───────┬────────────────────────────┘
                                │ path-prefix routing (see route table)
   ┌───────────┬───────────┬────┴──────┬───────────┬───────────┬─────────── … 12 routes
   ▼           ▼           ▼           ▼           ▼           ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌──────────┐ ┌───────────┐
│ device │ │ vdms   │ │ audit  │ │integr. │ │inspection│ │ scheduler │  …+ identity,
│ asset  │ │service │ │        │ │        │ │          │ │           │     alerts, inventory,
│ :8085  │ │ :8089  │ │ :8090  │ │ :8096  │ │  :8095   │ │  :8098    │     workorders, edge
└───┬────┘ └───┬────┘ └────────┘ └───┬────┘ └────┬─────┘ └─────┬─────┘
    │          │                     │           │             │
    │ each service has a paired Dapr sidecar (daprd) in the same network namespace
    ▼          ▼                     ▼           ▼             ▼
 ┌─────────────────────────────────────────────────────────────────┐
 │                         Dapr sidecars (daprd)                     │
 │   service-invocation (sync)  •  pub/sub (async)  •  state  •  jobs│
 └───────────────┬──────────────────────────────┬──────────────────┘
                 │ pub/sub + state (Redis)       │ jobs (etcd)
                 ▼                               ▼
          ┌─────────────┐               ┌────────────────────┐
          │ redis :6379 │               │ dapr-scheduler     │
          │ (broker +   │               │ control plane      │
          │  state)     │               │ (etcd-backed jobs) │
          └─────────────┘               └────────────────────┘

   All stateful services share ──► PostgreSQL 16  (sclera_assets DB, schema-per-service)
   All services emit traces/metrics ──► OTel Collector ──► Jaeger / Prometheus / Grafana / Loki
```

---

## 2. Services at a Glance

| Service | Port | Status | Owns DB? | Responsibility |
|---|---|---|---|---|
| `sclera-api-gateway` | 8080 | Active | — | Single entry point; routes `/asset/**`, `/vdms/**`, etc. to services |
| `sclera-cloud-device-asset` | 8085 | **Core** | Postgres (default schema) | Device & asset management — the heart of the platform |
| `sclera-vdms-service` | 8089 | Active | Postgres (`vdms_svc`) | VDMS (Virtual Device Management System) identity/config data + audit log store |
| `sclera-integrations` | 8096 | Active | Postgres (`integrations_svc`) | Vendor integrations: Bacnet, Daintree, Ecobee, Monnit, Modbus, Pelican, Lorawan, … |
| `sclera-scheduler` | 8098 | Active | Postgres (`sclera_scheduler`) | Central cron/job control plane via Dapr Jobs API |
| `sclera-inspection` | 8095 | Active | Postgres | Inspection domain (Postgres-backed) |
| `sclera-audit` | 8090 | Skeleton | — | Future audit service; subscribes to `device.audit-recorded` |
| `sclera-identity` | 8091 | Skeleton | — | Future identity/org service; publishes `identity.*` events |
| `sclera-alerts` | 8092 | Skeleton | — | Future alerting (`device.alert-condition-fired` → notifications) |
| `sclera-inventory` | 8093 | Skeleton | — | Future inventory service |
| `sclera-workorders` | 8094 | Skeleton | — | Future work-orders service |
| `sclera-edge` | 8097 | Skeleton | — | Future edge service |

**Plus two non-service modules:**

| Module | Type | Role |
|---|---|---|
| `dapr-commons` | Shared library | Reusable Dapr pub/sub publisher/subscriber + domain event objects (idempotency, DLQ routing) |
| `sclera-ui` | Frontend | React 18 + Vite 5 asset-management dashboard (port 3000) |

> **"Skeleton" / walking-skeleton** services are deliberate placeholders: they boot, register their
> Dapr sidecar and pub/sub surface, and return safe defaults. They exist so the platform topology,
> routing, and event contracts are in place *before* the real domain logic is written.

---

## 3. The Maven Reactor

The root `pom.xml` is a **pure aggregator (reactor)** — it does *not* act as a parent. Each module
keeps `spring-boot-starter-parent` as its own parent and builds independently via its own Maven
wrapper. The aggregator exists so the whole repo builds as one reactor, which lets dependent
modules resolve the in-repo `io.sclera:dapr-commons` library without `mvn install`-ing it first.

```
sclera-platform (aggregator)
├── dapr-commons            ← shared library, built first
├── sclera-cloud-device-asset
├── sclera-vdms-service
├── sclera-audit
├── sclera-identity
├── sclera-alerts
├── sclera-inventory
├── sclera-workorders
├── sclera-inspection
├── sclera-integrations
├── sclera-edge
├── sclera-scheduler
└── sclera-api-gateway
```

```
Build everything:          ./mvnw install
Build one module + deps:   ./mvnw -pl sclera-scheduler -am install
```

---

## 4. Technology Stack

| Layer | Technology |
|---|---|
| Frontend | React 18.3 + Vite 5.4 (`sclera-ui`, port 3000) |
| API Gateway | Spring Cloud Gateway (WebFlux, reactive) |
| Services | **Spring Boot 4.0.6, Java 21** (migrated from the original Spring Boot 2.6.5 / Java 17 / MySQL stack) |
| Service mesh / runtime | **Dapr** (sidecar pattern) — daprd `1.12.0` platform-wide; `1.15.5` for the scheduler only |
| Pub/Sub broker | Redis 7 (Dapr `pubsub.redis` component) |
| State stores | Redis (Dapr `state.redis` — idempotency keys + VDMS read-through cache) |
| Job scheduling | Dapr Jobs API + standalone Dapr Scheduler control plane (etcd) |
| Database | **PostgreSQL 16** — single `sclera_assets` DB, **schema-per-service** isolation |
| Auth | JWT (multi-tenant, per-issuer JWK validation) inside device-asset |
| Tracing | OpenTelemetry Java agent → OTel Collector → Jaeger |
| Metrics | Micrometer / OTel → Prometheus → Grafana |
| Logs | Promtail → Loki → Grafana |
| Containerization | Docker Compose |

---

## 5. Dapr — How Services Talk to Each Other

Every service runs a **paired Dapr sidecar** (`daprd`) in the same network namespace
(`network_mode: "service:<name>"`), reachable at `localhost:3500` (HTTP) / `localhost:50001`
(gRPC). All sidecars mount the same component set from `sclera-cloud-device-asset/dapr/`.

Sclera uses **four Dapr building blocks**:

### 5.1 Service Invocation (synchronous)
One service calls another by name through its sidecar — no hard-coded host/port, Dapr handles
discovery and retries.

```
caller → GET localhost:3500/v1.0/invoke/<app-id>/method/<path>  → Dapr routes to <app-id>:<port>
```

Example: `VdmsClient` in device-asset calls `vdms-service` for VDMS identity/config data.

### 5.2 Pub/Sub (asynchronous, Redis-backed)
Component `pubsub` (`pubsub.redis`, `redis:6379`). Publishers POST to the sidecar; Dapr writes to a
Redis stream; the subscriber's sidecar delivers the CloudEvent to the registered handler.

```
publisher → POST localhost:3500/v1.0/publish/pubsub/<topic>  → Redis  → subscriber handler
```

| Topic | Published by | Consumed by | Meaning |
|---|---|---|---|
| `device.audit-recorded` | device-asset | sclera-audit (idempotent) | Audit event recorded |
| `device.audit` | device-asset | vdms-service `AuditSubscriber` | Persist audit-log row |
| `vdms.update-property-details` | device-asset | vdms-service `VdmsSubscriber` | Property address change |
| `vdms.update-customer-org-id` | device-asset | vdms-service `VdmsSubscriber` | Org-ID change |
| `vdms.set-agent-permission` | device-asset | vdms-service `VdmsSubscriber` | Agent permission change |
| `device.event-recorded` | device-asset | (RabbitMQ replacement) | Generic device event |
| `device.sensor-reading` | device-asset | (RabbitMQ replacement) | Sensor measurement |
| `scheduler.trigger` (+ `.dlq`) | scheduler | owning service | A job fired — go do the work |
| `scheduler.result` (+ `.dlq`) | owning service | scheduler `ResultSubscriber` | Job outcome (SUCCESS/FAILED) |
| `identity.org-renamed`, `identity.user-deactivated` | sclera-identity | (future) | Identity lifecycle (skeleton) |

The shared `dapr-commons` library standardizes this: `DaprEventPublisher` (auto event IDs,
structured errors) and `DaprEventSubscriber<T>` (idempotency via state store, permanent-vs-transient
error classification, automatic dead-letter routing).

### 5.3 State Stores (Redis)
| Component | Backing | Key prefix | TTL | Used for |
|---|---|---|---|---|
| `statestore-idempotency` | Redis | `idem` | 24 h | Subscriber dedupe — guards exactly-once event processing |
| `statestore-vdmscache` | Redis | `vdms` | 5 min | `VdmsClient` read-through cache for VDMS lookups |

### 5.4 Jobs (scheduler only)
`sclera-scheduler` uses the **Dapr Jobs API** (`v1.0-alpha1/jobs`), which requires the standalone
**Dapr Scheduler control plane** (`dapr-scheduler`, etcd-backed, daprd ≥ 1.14). Because the rest of
the platform pins daprd `1.12.0`, only the scheduler's sidecar + control plane run `1.15.5`.

---

## 6. API Gateway Routing

`sclera-api-gateway` (Spring Cloud Gateway, WebFlux) is the **only externally exposed port (8080)**.
It path-prefix-routes to each backend. All routes strip their prefix (`StripPrefix=1`) **except VDMS**,
which is proxied as-is.

| Path prefix | → Target service | Port | Strips prefix |
|---|---|---|---|
| `/asset/**` | sclera-cloud-device-asset | 8085 | ✔ |
| `/vdms/**` | vdms-service | 8089 | ✘ (pass-through) |
| `/audit/**` | sclera-audit | 8090 | ✔ |
| `/identity/**` | sclera-identity | 8091 | ✔ |
| `/alerts/**` | sclera-alerts | 8092 | ✔ |
| `/inventory/**` | sclera-inventory | 8093 | ✔ |
| `/workorders/**` | sclera-workorders | 8094 | ✔ |
| `/inspection/**` | sclera-inspection | 8095 | ✔ |
| `/integrations/**` | sclera-integrations | 8096 | ✔ |
| `/edge/**` | sclera-edge | 8097 | ✔ |
| `/scheduler/**` | sclera-scheduler | 8098 | ✔ |
| `/dapr/**` | device-asset Dapr sidecar | 3500 | ✔ |

CORS is open to `http://localhost:3000` (the React UI) and `http://localhost:8080`. No auth is
enforced at the gateway — JWT validation happens inside `sclera-cloud-device-asset`.

Example: `GET /asset/api/v1/devices` → `http://sclera-cloud-device-asset:8085/api/v1/devices`.

---

## 7. The Core Service — `sclera-cloud-device-asset`

The heart of the platform: all device and asset CRUD, counts, dropdowns, documents, AI suggestions,
and audit publishing. Spring Boot 4.0.6 / Java 21 / PostgreSQL.

| Package | Responsibility |
|---|---|
| `controller/admin/` | REST endpoints — devices, buildings, locations, floors, sensors, documents, AI suggestions |
| `client/` | `VdmsClient` — calls vdms-service via Dapr (service invocation + pub/sub), with read-through cache |
| `service/` | Business logic — `DeviceService`, `VdmsService`, `BuildingService`, `UserActionLogService` (publishes audit events on add/edit/delete) |
| `Repository/` | JPA repositories (PostgreSQL) |
| `models/` / `dto/` | JPA entities and request/response DTOs |
| `config/` | Security (`WebSecurityConfig`), CORS, JWT filter, cache, OTel `PayloadTracingFilter` |
| `auth/` | Multi-tenant JWT validation (`TenantJWSKeySelector`, `TenantJwtIssuerValidator`) |
| `integration/` | External system integrations (Corrigo, Daintree, …) |
| `sockets/` + `websocket/` | WebSocket support |
| `rabbitmq/` | Legacy messaging (being replaced by Dapr pub/sub) |

---

## 8. The Scheduler Flow (worth highlighting to the team)

`sclera-scheduler` is a **control plane, not a worker** — it fires events; the owning services do the
work. This keeps scheduling concerns out of every domain service.

```
jobs.yaml (catalog)                                  Dapr Scheduler control plane (etcd)
   │ registered at startup via SchedulerClient            │ persists job, fires on cron/interval
   └──────────────────────────────────────────────────────┘
                                  │ POST /job/{name}  (job fires)
                                  ▼
                       ┌─────────────────────┐
                       │  sclera-scheduler   │
                       └─────────┬───────────┘
                                 │ publish  scheduler.trigger  (optional vdmsId for PER_VDMS jobs)
                                 ▼
                       ┌─────────────────────┐
                       │   owning service    │  (device-asset / integrations / inspection / …)
                       │   does the actual   │
                       │   work              │
                       └─────────┬───────────┘
                                 │ publish  scheduler.result  (SUCCESS | FAILED, durationMs, error)
                                 ▼
                       ┌─────────────────────┐
                       │  sclera-scheduler   │  ResultSubscriber records run in job_instance table
                       └─────────────────────┘
```

- **Job catalog:** declared in `jobs.yaml` (cron `0 0 0 */1 * *` or interval `@every 90s`).
- **Scope:** each job is `GLOBAL` (fires once) or `PER_VDMS` (fires once per registered VDMS property —
  `SchedulerTriggerEvent.vdmsId` routes the fire to the right tenant).
- **State:** owns the `sclera_scheduler` schema (`job` catalog, `job_instance` run history),
  Flyway-migrated, with history retention.

---

## 9. Database — PostgreSQL with Schema-per-Service

A single PostgreSQL 16 instance (`sclera_assets`, port 5432) hosts **isolated schemas per service**,
giving logical DB-per-service separation without operating many database servers.

| Schema | Owner |
|---|---|
| default/public | sclera-cloud-device-asset |
| `vdms_svc` | sclera-vdms-service |
| `integrations_svc` | sclera-integrations |
| `sclera_scheduler` | sclera-scheduler |
| (inspection schema) | sclera-inspection |

Cross-service references use **scalar FK columns** (IDs, often UUID strings) rather than DB-level
foreign keys across schemas — this keeps services loosely coupled and independently deployable.

---

## 10. Observability

Every service runs the OpenTelemetry Java agent and ships telemetry to a shared collector. Dapr
sidecars also export traces (100% sampling — tune for prod in `dapr/config.yaml`).

```
services + Dapr sidecars
   ├─ traces  ──► OTel Collector :4317/:4318 ──► Jaeger        (UI :16686)
   ├─ metrics ──► OTel Collector :9464        ──► Prometheus    (:9090) ──► Grafana (:3002)
   └─ logs    ──► Promtail ──► Loki (:3100)                     ──► Grafana (:3002)
```

| Tool | Port | Purpose |
|---|---|---|
| OTel Collector | 4317 / 4318 / 9464 | Receives OTLP traces & metrics, re-exposes Prometheus scrape |
| Jaeger | 16686 | Distributed trace UI |
| Prometheus | 9090 | Metrics store (7-day retention) |
| Grafana | 3002 | Dashboards (anonymous Viewer enabled) |
| Loki + Promtail | 3100 | Log aggregation from container stdout |

device-asset also captures request/response **headers and bodies** (via `PayloadTracingFilter`) and
un-sanitized SQL on spans, for full request visibility in dev.

---

## 11. Local Infrastructure (`docker-compose.yml`)

| Container | Image | Port(s) | Purpose |
|---|---|---|---|
| `sclera-postgres` | postgres:16 | 5432 | Shared DB (`sclera_assets`, schema-per-service) |
| `sclera-redis` | redis:7-alpine | internal | Dapr pub/sub broker + state stores |
| `dapr-scheduler` | daprio/scheduler:1.15.5 | 50006 | Dapr Jobs control plane (etcd-backed) |
| `*-dapr` (one per service) | daprio/daprd:1.12.0 (scheduler: 1.15.5) | 3500/50001 | Per-service Dapr sidecar |
| `sclera-otel-collector` | otel-collector-contrib | 4317/4318/9464 | Telemetry pipeline |
| `sclera-jaeger` | jaeger all-in-one | 16686 | Traces UI |
| `sclera-prometheus` | prometheus | 9090 | Metrics |
| `sclera-grafana` | grafana | 3002 | Dashboards |
| `sclera-loki` / `sclera-promtail` | loki / promtail | 3100 | Logs |
| `sclera-httpbin` | httpbin | internal | Test double for outbound integration bindings |
| `sclera-mailhog` | mailhog | 8025 | Test SMTP/email capture |

All on the `sclera-net` bridge network.

---

## 12. End-to-End Request Flows

**Device list query (UI → gateway → service → Dapr → vdms):**
```
Browser → GET http://localhost:8080/asset/api/v1/.../devices
  → api-gateway strips /asset → sclera-cloud-device-asset:8085/api/v1/.../devices
  → DeviceController → DeviceService
  → (if VDMS data needed) VdmsClient → Dapr invoke → vdms-service:8089 → Postgres
  → returns device list
```

**Device add (with async audit):**
```
Browser → POST /asset/.../adddevice
  → DeviceController → DeviceService.addDevice()  → persists to Postgres
       → UserActionLogService → DaprEventPublisher.publish("device.audit", payload)
             → Dapr → Redis → vdms-service AuditSubscriber → user_action_log table
       → (also) "device.audit-recorded" → sclera-audit (idempotent subscriber)
```

**Scheduled job (scheduler → owning service → back):**
```
Dapr Scheduler fires → POST /job/{name} on sclera-scheduler
  → publish scheduler.trigger (vdmsId if PER_VDMS)
  → owning service subscribes, does the work
  → publish scheduler.result (SUCCESS/FAILED, durationMs)
  → sclera-scheduler ResultSubscriber records run in job_instance
```

---

## 13. What Changed from Sclera 1.x (talking points)

- **Monolith → microservices reactor:** one app became 12 services + a shared library, fronted by a gateway.
- **MySQL → PostgreSQL 16** with schema-per-service isolation.
- **Java 17 / Spring Boot 2.6.5 → Java 21 / Spring Boot 4.0.6.**
- **Direct HTTP / RabbitMQ → Dapr** for service-to-service calls, pub/sub, state, and jobs (RabbitMQ being phased out).
- **Centralized scheduling** via the Dapr Jobs control plane instead of in-process schedulers.
- **Full observability stack** (OTel + Jaeger + Prometheus + Grafana + Loki) wired into every service and sidecar.
- **Walking-skeleton services** (audit, identity, alerts, inventory, workorders, edge) stake out the
  topology and event contracts ahead of building their domain logic.
