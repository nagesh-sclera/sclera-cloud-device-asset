# Dapr expansion, service extraction, and database decoupling — design

**Date:** 2026-05-19
**Repo:** `Microservice123/sclera-cloud-device-asset` (multi-service mono-repo: api-gateway, cloud-device-asset, vdms-service)
**Status:** Draft for review
**Scope:** Dapr expansion across existing services + service-extraction pattern/roadmap + database decoupling principles
**Out of scope:** MySQL → PostgreSQL migration (separate sub-spec)

---

## 1. Current state, Dapr-relevant

### Services on the wire today

- `sclera-api-gateway:8080` — Spring Cloud Gateway, plain HTTP reverse proxy. **No Dapr today.**
- `sclera-cloud-device-asset:8085` — Spring Boot 2.6.5, Java 17. Dapr sidecar `app-dapr` (app-id `sclera-cloud-device-asset`, HTTP 3500, gRPC 50001).
- `sclera-vdms-service:8089` — Spring Boot. Dapr sidecar `vdms-dapr` (app-id `vdms-service`).
- All sidecars on Dapr 1.12, `network_mode: "service:<peer>"`.
- Single `pubsub.redis` component (Redis 7).

### Inter-service traffic, Dapr vs. not

| Channel | Mechanism today | Currently Dapr? |
|---|---|---|
| `api-gateway` → `cloud-device-asset` (`/asset/**`) | Spring Cloud Gateway HTTP proxy | No |
| `api-gateway` → `vdms-service` (`/vdms/**`) | Spring Cloud Gateway HTTP proxy | No |
| `cloud-device-asset` → `vdms-service` sync | `RestTemplate.getForObject("http://localhost:3500/v1.0/invoke/vdms-service/method/...")` — **raw HTTP**, no Dapr SDK | Yes (raw HTTP) |
| `cloud-device-asset` → `vdms-service` events | `RestTemplate.postForObject("http://localhost:3500/v1.0/publish/pubsub/{topic}")` — 4 topics: `vdms.update-property-details`, `vdms.update-customer-org-id`, `vdms.set-agent-permission`, `device.audit` | Yes (raw HTTP) |
| `cloud-device-asset` → RabbitMQ (legacy) | `RabbitmqService` direct AMQP | No (to be migrated) |
| `cloud-device-asset` → external APIs (Corrigo, Daintree, AI, app.sclera.com) | `APICallService` / `RestTemplate` | No |
| Secrets (DB, JWT, broker creds) | Compose env vars + `application*.yml` | No (plaintext) |

### Gaps the spec closes

1. Raw-HTTP sidecar calls bypass Dapr's circuit breaker, retries, and metrics. Java SDK gets these for free.
2. Two near-duplicate `dapr/` directories (`/dapr` and `/sclera-cloud-device-asset/dapr`) drift; no per-environment component layout.
3. Tracing present but `samplingRate=0` (off). No OTel Collector. No correlated traces.
4. No `resiliency.yaml`.
5. Secrets in plaintext.
6. Gateway is a Dapr blind spot.
7. External integrations directly coupled to vendor SDKs.
8. `AuditSubscriber` lives in `vdms-service` for historical reasons — it should be its own service.
9. Single shared `vdms` MySQL schema with cross-module foreign keys.
10. ~30 services exist only as stubs inside `cloud-device-asset` (`io.sclera.stubs/`).

---

## 2. Dapr building-block plan

| Block | Status | Use | Component name | Rationale |
|---|---|---|---|---|
| Service invocation | In use, modernize | All sync inter-service calls; gateway routing | n/a (sidecar feature) | Discoverable app-ids, mTLS, resiliency policies, metrics |
| Pub/Sub | In use, broker portability | Domain events; future RabbitMQ replacement | `pubsub` | Single API, broker per env (`pubsub.redis` dev, `pubsub.rabbitmq` prod) |
| State management | New | VDMS read-through cache; pub/sub idempotency keys; distributed locks | `statestore-cache`, `statestore-idempotency` | Shared across replicas; backend swappable |
| Secrets | New | DB creds, JWT keys, RabbitMQ creds, third-party API keys | `secretstore` | Local file dev; Key Vault / K8s secrets prod |
| Bindings | New | Outbound: SMTP, blob, Corrigo, Daintree. Inbound: cron jobs | `binding-smtp`, `binding-blobstore`, `binding-corrigo`, `binding-daintree`, `binding-cron-*` | Vendor coupling becomes config, not code |
| Configuration API | New, narrow | Feature flags + tenant settings (NOT static config) | `configstore` | Live updates, central store |

### Conventions

- App-id = `kebab-case`, matches `spring.application.name`, prefix `sclera-`.
- Topic naming: `<domain>.<event-past-tense>` (e.g., `device.audit-recorded`, `vdms.property-details-updated`).
- Component names: role-based (`pubsub`, `secretstore`, `statestore-cache`).
- Subscribers declared with SDK `@Topic` annotation, not `GET /dapr/subscribe`.
- Sync Dapr invoke allowed for cross-module **reads only**. Cross-module **writes** go via events.

---

## 3. Phased implementation

### Phase 1 — Foundation (portable, observable, resilient)

Goal: every existing Dapr-touching code path runs on the new foundation. No new building blocks. No external behavior change.

#### 1.1 Single, portable component layout

```
dapr/
  config.yaml                  # Configuration CR (tracing, mTLS)
  resiliency.yaml              # Default policies
  components/
    local/                     # docker-compose
      pubsub.yaml              # pubsub.redis @ redis:6379
      secretstore.yaml         # secretstores.local.file
    k8s/                       # K8s Component CRs
      pubsub.yaml              # pubsub.rabbitmq
      secretstore.yaml         # secretstores.kubernetes
    aca/                       # Azure Container Apps
    vm/                        # bare VM / self-hosted
```

Delete the two duplicated `dapr/` directories. Sidecars mount `dapr/components/<env>` via `--components-path`.

#### 1.2 Gateway sidecar

Add `gateway-dapr` to Compose (app-id `sclera-api-gateway`, app-port 8080, `network_mode: "service:api-gateway"`). Gateway uses a Spring filter calling `DaprClient.invokeMethod(...)` in place of `lb://` / direct HTTP routes.

#### 1.3 Adopt Dapr Java SDK

Add `io.dapr:dapr-sdk:1.12.0` and `io.dapr:dapr-sdk-springboot:1.12.0` to all three services. Refactor `VdmsClient`:

```java
// before
restTemplate.getForObject(
  "http://localhost:" + daprPort + "/v1.0/invoke/vdms-service/method/vdms/id",
  Map.class);

// after
daprClient.invokeMethod(
  "vdms-service", "vdms/id", null, HttpExtension.GET, Map.class
).block();
```

Same shape for `publishEvent`. Subscribers move to `@Topic(name="...", pubsubName="pubsub")` annotations.

#### 1.4 Observability — OTel Collector

Add `otel-collector` to Compose, OTLP receiver, console + Jaeger exporter for dev.

```yaml
# dapr/config.yaml
spec:
  tracing:
    samplingRate: "1"                 # 100% non-prod; tune for prod
    otel:
      endpointAddress: "otel-collector:4317"
      isSecure: false
      protocol: grpc
  metric:
    enabled: true
```

Add `micrometer-tracing-bridge-otel` to each Spring Boot app so app spans nest under sidecar spans.

#### 1.5 Resiliency

```yaml
# dapr/resiliency.yaml
apiVersion: dapr.io/v1alpha1
kind: Resiliency
metadata: { name: sclera-resiliency }
scopes: [sclera-api-gateway, sclera-cloud-device-asset, vdms-service]
spec:
  policies:
    timeouts:
      default: 5s
      pubsub-publish: 3s
    retries:
      default:
        policy: exponential
        duration: 200ms
        maxInterval: 5s
        maxRetries: 3
        retryableHTTPStatusCodes: "408,500,502,503,504"
    circuitBreakers:
      default:
        maxRequests: 1
        timeout: 30s
        trip: consecutiveFailures >= 5
  targets:
    apps:
      vdms-service: { timeout: default, retry: default, circuitBreaker: default }
      sclera-cloud-device-asset: { timeout: default, retry: default, circuitBreaker: default }
    components:
      pubsub:
        outbound: { timeout: pubsub-publish, retry: default }
```

#### 1.6 App-id registry

`dapr/APP_IDS.md` lists every app-id, owner, exposed methods, published/subscribed topics, port assignment. Mandatory to update in any PR adding a service or topic.

**Phase 1 exit:** SDK adoption complete; OTel traces end-to-end visible; resiliency proven by chaos test; single `dapr/` directory at root.

### Phase 2 — Migrate existing usage onto new patterns

- **2.1 Secrets:** DB creds, JWT signing keys, RabbitMQ creds move out of yaml/env. `application.yml` keeps references only. `DaprClient.getSecret(...)` at startup for DB and JWT; `secretRef:` in `pubsub` component for broker creds.
- **2.2 Portable broker:** `pubsub` component swaps from `pubsub.redis` (dev) to `pubsub.rabbitmq` (prod) via component file alone. No app code change.
- **2.3 Declarative subscriptions + dead-letter:** subscribers use SDK `@Topic`. Per-topic `deadLetterTopic` config. Single dead-letter topic `system.dead-letters` with ops handler that logs + persists for replay.

**Phase 2 exit:** no plaintext secrets in repo; `pubsub` broker is config-only; dead-letter topic live.

### Phase 3 — New building blocks

- **3.1 State store — cache:** `VdmsClient` reads through `DaprClient.getState("statestore-cache", "vdms:<vdmsId>:details", ...)`. TTL 5 min. Invalidated on `vdms.property-details-updated`.
- **3.2 State store — idempotency:** every subscriber checks `statestore-idempotency` with the CloudEvent `id` before processing. Specifically fixes `AuditSubscriber` double-write risk. Ship with feature flag, default off in prod for one release, then on.
- **3.3 Bindings:** SMTP, blob, Corrigo, Daintree as output bindings; `binding-cron-*` as input. Replaces `APICallService` direct REST and the stubbed `JobSchedulerService`.
- **3.4 Configuration API:** narrow rollout — feature flags only. `FeatureFlagService` uses `DaprClient.getConfiguration` + `subscribeConfiguration` for live updates. Migrate 2–3 flags (e.g., `audit.publish.enabled`).
- **3.5 RabbitMQ → Dapr pub/sub:** dual-publish window for one release; consumers migrate; v1 retired; `RabbitmqService` deleted.

**Phase 3 exit:** VDMS read latency p95 down (cache); `AuditSubscriber` idempotent under deliberate redelivery; one external binding live; `RabbitmqService` removed.

### Phase 4 — Service extraction & DB decoupling

> **Superseded by `2026-05-19-walking-skeleton-extraction-design.md`.** The walking-skeleton approach replaces this phase's "one service at a time" sequence with: (a) Phase A — scaffold all 7 skeleton services in one PR with full Dapr surface and hardcoded defaults; (b) Phase B — per-stub-class client migration in priority order. DB decoupling deferred to per-service real-implementation work. The original Phase 4 table below is retained for historical context only.

Each iteration extracts one stubbed service and carves out its data ownership. Uses the patterns in Sections 4–5.

Source of truth for what's in scope: `migration-notes/stub-inventory.md` (70 stubs total, generated 2026-05-19). Per-service stub counts inform extraction sizing below.

| Step | Service | Stubs | Notes |
|---|---|---|---|
| 4.1 | Extraction kit | – | Maven archetype, `tools/scaffold-service.sh`, template repo |
| 4.2 | `sclera-audit` (AP-C6) | 4 | Proving ground. Moves `AuditSubscriber` out of `vdms-service`. `UserActionLogService` is already real (no extraction work needed there) — confirm scope before scaffolding. |
| 4.3 | `sclera-identity` (CP-2) | 5 | Cross-cutting; most services depend on user/org resolution. **Blocker:** top-level `CustomerOrganisationService` source file is missing from the repo (only `touchscreen.CustomerOrganisationService` exists). Resolve before scaffolding: either copy from `sclera-vdms-edge-server` or confirm the touchscreen variant is canonical. |
| 4.4 | `sclera-alerts` (AP-C5) | 5 | Event-driven naturally |
| 4.5 | `sclera-inventory` (AP-C8) | 2 | Few cross-module joins. `Product_DetailsService` is already real — scope reduces to `InventoryDeviceService` only. |
| 4.6 | `sclera-workorders` (AP-C3) | 5 | Needs bindings (Corrigo, PMS) from Phase 3.3 |
| 4.7 | `sclera-inspection` (AP-C4) | 6 | Heavier internal surface |
| 4.8 | `sclera-integrations` (AP-C2) | 16 | **Required 3-way split** — AP-C2a poll-based (Bacnet, Modbus, Snmp, Siemens), AP-C2b vendor-cloud (Daintree, Ecobee, Monnit, Pelican, PolyLens, Disruptive), AP-C2c push (KNX, Lorawan, Mqtt) plus AP-C2-core (IntegrationService, PropertyQrcodeService, AssetMapperService). 16 services as one extraction is too large given the kit must be exercised per service. |
| 4.9 | ~~`sclera-adc` (AP-C9)~~ | 0 | **Blocked / out of scope:** no `ADCService` source class exists in the repo. Either AP-C9 was deferred at the original decomposition or the source was never extracted. Confirm with team; if AP-C9 is real, this slot is gated on copying the source class first. Treat as a separate spec item, not part of this rollout. |
| 4.X | Cross-instance DB split for stabilized services | – | Each service to its own MySQL instance. "Stabilized" = no breaking event-contract or schema change for 2+ releases AND no cross-module read-model staleness incidents in the same window |
| 4.Y | Edge-only Bucket-D stubs disposition | 32 | **Separate decision, not extraction.** 32 stubs (`APICallService`, `AsyncService`, `DataHoistService`, `DockerService`, `IOCService`, `JobSchedulerService`, `MasterSlaveAPICallService`, `UtilsService`, `WebClientService`, `RabbitmqService`, `SocketService`, `ProxyService`, touchscreen.*, websocket.client.*) belong to the edge runtime, not extraction targets. Each call site to be evaluated: (a) replaced by a Dapr binding (Phase 3.3), (b) kept as a permanent no-op stub if behavior isn't needed in cloud, or (c) deleted along with the call site. PR per stub, no big-bang. |

---

## 4. Service extraction strategy

### Extraction kit (per service)

| Artifact | Standard |
|---|---|
| Maven module | Java 17, Spring Boot 2.6.5, package `io.sclera.<servicekey>` |
| Dependencies | `dapr-sdk`, `dapr-sdk-springboot`, web, data-jpa, validation, `micrometer-tracing-bridge-otel`, flyway |
| App-id | `sclera-<servicekey>` (registered in `APP_IDS.md`) |
| HTTP port | From port registry (8090–8099 range) |
| Sidecar | Compose service `<servicekey>-dapr`; K8s `dapr.io/enabled: true`; mounts root `dapr/components/<env>` |
| Health | `/actuator/health` + sidecar liveness wired to orchestrator probes |
| Database | Own schema (`sclera_<servicekey>`) in shared instance during migration; own instance later |
| Pub/sub contract | `topics.yaml` at module root listing `publishes:` / `subscribes:`; PR-reviewed |
| Service invocation contract | OpenAPI 3 at `src/main/resources/openapi.yaml`; integration-tested |
| Observability | traceparent in/out, structured JSON logs, Micrometer metrics endpoint |
| Resiliency | Inherits root `resiliency.yaml`; overrides justified per PR |
| CLAUDE.md | Mirrors existing pattern |

### Extraction methodology

1. **Identify boundary** — method inventory, owned entities, cross-module joins (flagged for decoupling).
2. **Define contract** — OpenAPI for sync, `topics.yaml` for async.
3. **Scaffold from template** — Maven module + Compose entry + sidecar.
4. **Move code, not copy** — cut implementations from `sclera-vdms-edge-server` into new service; stubs in `cloud-device-asset` become thin Dapr clients (modeled on `VdmsClient`).
5. **Carve data ownership** — Flyway moves tables into new schema; cross-module FKs become plain ID references.
6. **Backfill via pub/sub** — consumers maintain local read models for previously-joined data.
7. **Cut over** — deploy, smoke test, remove stubs, PR titled `extract(AP-Cx): <servicekey>`.

### Priority order (advisory)

Stub counts cross-referenced with `migration-notes/stub-inventory.md`. Services already partially-real (UserActionLogService, Product_DetailsService, touchscreen.VdmsService, UserService) are noted in their slot.

| Order | Key | Service | Stubs | Rationale |
|---|---|---|---|---|
| 1 | AP-C6 | `sclera-audit` | 4 | Pub/sub side already works; misplaced `AuditSubscriber` corrected. `UserActionLogService` already real — extraction scope is smaller than the count suggests. Smallest, highest-confidence. Proves extraction kit. |
| 2 | CP-2 | `sclera-identity` | 5 | Cross-cutting. **Blocker:** top-level `CustomerOrganisationService` source missing from repo. Resolve before scaffolding. |
| 3 | AP-C5 | `sclera-alerts` | 5 | Event-driven naturally; few inbound deps after identity |
| 4 | AP-C8 | `sclera-inventory` | 2 | `Product_DetailsService` already real — extraction reduces to `InventoryDeviceService` only |
| 5 | AP-C3 | `sclera-workorders` | 5 | Needs bindings (Corrigo, PMS) |
| 6 | AP-C4 | `sclera-inspection` | 6 | Larger surface |
| 7 | AP-C2 | `sclera-integrations` | 16 | **Required 3-way split** AP-C2a/b/c + AP-C2-core. 16 services as one extraction is impractical. |
| – | AP-C9 | ~~`sclera-adc`~~ | 0 | **Blocked / out of scope** — no source class in repo. Confirm scope with team. |

AP-C7 absent from the current decomposition — confirm with team whether it was renamed/merged/dropped.

**Caveats:**
- Stub counts are upper bounds — some classes mix real and stub methods; only stub methods need extraction or stay-as-stub decisions.
- 32 edge-only Bucket-D stubs are NOT in this priority list (see Phase 4.Y). They are evaluated per call site.

---

## 5. Database decoupling

### Today

One MySQL 8 instance, schema `vdms`, shared by both services. Cross-module FKs and joins exist. `Device.java` carries 26 `@Transient`-marked relations to entities that belong to other modules (per `migration-notes/`).

### Target

- Each service owns its schema.
- Cross-module references are **IDs only, no FK constraints** to remote tables.
- Cross-module reads → Dapr service invocation (cached where hot).
- Cross-module writes → Dapr pub/sub events.
- Join-heavy reads → local read models / projections maintained from events.

### Decoupling rules by category

| Category | Today | Rule |
|---|---|---|
| Cross-module FK | `device.customer_org_id` FK → `customer_organisation(id)` | Drop FK, keep column. Validity via Dapr invoke at write or trusted event stream. |
| Cross-module SQL join | `JOIN customer_organisation` for list endpoint | Read model populated by events, OR per-row enrichment via Dapr invoke + cache. Pick by row count. |
| Cross-module write transaction | `addDevice + audit` in one `@Transactional` | Outbox pattern; local write + outbox row commit atomically; publisher emits Dapr event. |
| Shared lookup tables | All services join `unit_of_measure` | Replicate as immutable reference data, owned by primary mutator (or `sclera-reference`); updated via pub/sub. |
| Many-to-many across modules | `device_to_user` | Owned by the side that semantically creates the association; projection on the other side. |

### Eventing patterns

| Pattern | Use | How |
|---|---|---|
| Outbox | Atomic state-change + publish | Local `outbox_event` table written in same tx; background poller publishes via Dapr; row marked sent |
| CDC (Debezium) | Last resort for legacy tables | Debezium → Kafka → Dapr-Kafka pubsub. Adds infra; reserve for 1–2 cases. |
| Saga | Multi-step distributed workflow | Each step publishes; next step subscribes; compensating actions per failure mode |
| Idempotent consumer | Every subscriber | Phase 3.2 idempotency state store |
| Read model / projection | Cross-module query | Subscriber materializes denormalized table locally |

### Per-service migration sequence (during that service's extraction)

1. **Schema rename in place** — Flyway `RENAME TABLE vdms.<table> TO sclera_<service>.<table>;`. Both schemas live in same MySQL instance during the migration window.
2. **App points** — new service's `application.yml` uses `jdbc:mysql://.../sclera_<service>`.
3. **FK drops** — for each FK from a still-owned table to an extracted table, drop constraint, keep column, add index if FK provided one. Record in `migration-notes/dropped-fks.md`.
4. **Projections seeded** — one-time backfill via `<service>.bulk-export` Dapr invoke; then subscribe to ongoing change events.
5. **Dual-read window (optional)** — for one release, consumer can fall back to direct DB read; removed next release.
6. **Cross-instance move** — when stable (criteria above), point service at its own MySQL instance. Other services no longer have network access.

### Risks and tradeoffs

| Risk | Mitigation |
|---|---|
| Eventual consistency in UI ("I created an org but it's not in the dropdown") | Write-through invalidation; explicit "syncing…" UI states; bounded staleness SLO ≤2s p99 per projection |
| Orphaned rows (no FK enforcement) | App-level checks at write time; periodic reconciliation jobs; orphans logged + alerted, not auto-deleted |
| Cross-module analytics joins | Denormalize via projections, or build a separate analytics read store (CDC → warehouse). Not solved here. |
| Outbox publisher lag | Monitor row age; alert at >30s |
| Schema migrations on large tables | `ALGORITHM=INPLACE, LOCK=NONE` where possible; off-hours otherwise; validate row counts |
| Event-contract evolution across producer + consumers | Topic versioning (`...v1` / `...v2`); producer dual-publishes for one release; consumers migrate; v1 retired. Documented in `topics.yaml`. |
| "Distributed monolith" trap (sync everywhere) | Architectural rule: sync Dapr invoke for **reads only**; mutations cross modules via events. Reviewed per PR. |
| Test surface explosion | Contract tests per producer/consumer pair (Pact or similar); CI runs them before any topic schema or OpenAPI change merges. |

---

## 6. Execution order and risks

### Priority order

1. Phase 1.1 — repo layout cleanup (single `dapr/` at root)
2. Phase 1.4 — observability (OTel + Jaeger) — **before refactors so changes are visible**
3. Phase 1.3 — Dapr Java SDK adoption
4. Phase 1.2 — gateway sidecar + Dapr invoke routing
5. Phase 1.5 — `resiliency.yaml`
6. Phase 1.6 — `APP_IDS.md` registry
7. Phase 2.1 — secrets out of yaml/env
8. Phase 2.2/2.3 — pub/sub portability, declarative subscriptions, dead-letter
9. Phase 3.2 — pub/sub idempotency state store (fixes a correctness gap; highest-value new block)
10. Phase 3.1 — VDMS cache state store
11. Phase 3.3 — bindings (start with one: Corrigo or SMTP)
12. Phase 3.5 — RabbitMQ → Dapr pub/sub migration
13. Phase 3.4 — configuration API (narrow scope)
14. Phase 4.1 — extraction kit
15. Phase 4.2 — extract `sclera-audit` (proving ground)
16. Phase 4.3 — extract `sclera-identity` (gated on CP-2 source resolution)
17. Phase 4.4–4.7 — `sclera-alerts`, `sclera-inventory`, `sclera-workorders`, `sclera-inspection`
18. Phase 4.8 — AP-C2 sub-extractions (4.8a poll-based, 4.8b vendor-cloud, 4.8c push, 4.8d core)
19. Phase 4.Y — edge-only Bucket-D stub disposition (per call site, parallel to extractions)
20. Phase 4.X — cross-instance DB split for stabilized services
21. ~~Phase 4.9~~ — AP-C9 `sclera-adc` blocked pending source confirmation; tracked separately

### Top risks

| Risk | Mitigation |
|---|---|
| Dapr Java SDK 1.12 + Spring Boot 2.6.5 + Java 17 compatibility unverified | 30-min spike before Phase 1.3: one `DaprClient.invokeMethod` call against current `vdms-service` |
| `network_mode: "service:..."` shares namespace — `localhost:3500` ambiguity if a sidecar ever targets multiple apps | Keep 1:1 sidecar:app; document in `APP_IDS.md` |
| RabbitMQ broker swap may surface different ordering/delivery semantics | Phase 2.2 topic-by-topic test matrix: 1000-message delivery + ordering assertion per broker |
| OTel + 100% sampling overhead in prod | Non-prod 100%; prod 10% with head-sampling rule |
| Existing pub/sub redeliveries currently re-fire writes (no idempotency) | Phase 3.2 idempotency behind feature flag for one release before on-by-default |
| K8s component delivery is a separate ops project | Components written + validated against an empty cluster; full K8s rollout = follow-up spec |
| Dual-publish during RabbitMQ migration doubles traffic | Acceptable for one-release window; broker sizing pre-confirmed |
| Multi-month roadmap for extractions; priorities will shift | Re-evaluate priority list after every 2 extractions; treat ordering as advisory |
| AP-C7 missing from decomposition list | Confirm with team; document in `APP_IDS.md` |
| **AP-C9 (`sclera-adc`) source class missing from repo** | Confirm whether AP-C9 is a real target; if yes, copy `ADCService` from `sclera-vdms-edge-server` before slot 4.9; otherwise drop from roadmap. Blocking precondition documented in `stub-inventory.md`. |
| **CP-2 top-level `CustomerOrganisationService` source missing** | Resolve before Phase 4.3: copy from source repo or formally adopt `touchscreen.CustomerOrganisationService` as canonical. Failing to resolve means `sclera-identity` extraction is missing a core dependency. |
| **AP-C2 has 16 stub services — one extraction is impractical** | Split AP-C2 into AP-C2a (poll-based), AP-C2b (vendor-cloud), AP-C2c (push), and AP-C2-core. Treat each sub-service as its own slot 4.8a/4.8b/4.8c/4.8d. |
| Stubs may not faithfully represent monolith behavior | Per-extraction fidelity check against `sclera-vdms-edge-server` before scaffolding |
| Several "stubs" per migration notes are now real implementations (`touchscreen.VdmsService`, `UserService`, `UserActionLogService`, `Product_DetailsService`) | Always cross-reference `stub-inventory.md` before scoping an extraction; the inventory is the source of truth, status-2026-05-13.md is now stale on these |
| 32 edge-only Bucket-D stubs have no extraction target | Per-call-site disposition (Phase 4.Y): Dapr binding, permanent no-op, or delete with the call site. No big-bang. |
| Schema renames across active DB | Maintenance window per service; rollback = reverse Flyway script + redeploy stubs |
| Read-model staleness vs UI expectations | Per-projection staleness SLO; explicit UI "syncing" states |

### Proof-of-concept scope

A single time-boxed branch that delivers:

1. Single `dapr/` at repo root with `components/local/` only.
2. Gateway sidecar added.
3. `VdmsClient.getVdmsDetails()` migrated to Dapr Java SDK (one method, not the whole client).
4. OTel Collector + Jaeger in Compose, `samplingRate=1`, end-to-end trace visible for `GET /asset/.../devices`.
5. `resiliency.yaml` with default policies, proven by killing `vdms-service` and observing retries + 503.
6. **Plus Phase 4.2:** extract `sclera-audit`. Owns `user_action_log` in `sclera_audit` schema; subscribes to `device.audit-recorded` directly; `cloud-device-asset` reads it back via `AuditClient` Dapr invoke.

PoC merge → rest of Phase 1 + extraction kit is mechanical. PoC failure → plan changes.

### Testing strategy

| Layer | Tool | Tests |
|---|---|---|
| Unit | JUnit + Dapr SDK in-process mocks | `VdmsClient` invocations, subscriber handlers, idempotency check |
| Component (per service) | Testcontainers (Dapr placement + Redis + service) | Service + its sidecar; publish/subscribe and invoke against a real sidecar |
| Integration (cross-service) | Compose + ephemeral test profile + RestAssured | Golden paths: device-add → audit publish → audit subscriber → DB row; VDMS-detail with cache miss then hit |
| Chaos | Compose + Toxiproxy / `docker pause` | Stop `vdms-service` 30s mid-call; verify retries, circuit breaker open, gateway returns 503 not 500 |
| Trace assertions | Jaeger HTTP API in CI | Per golden path: assert span tree with expected service nodes and `traceparent` continuity |
| Idempotency | Scripted redelivery | `device.audit` redelivered → exactly one `user_action_log` row |
| Contract | Pact (or similar) | Producer/consumer pair tests for each topic schema and OpenAPI endpoint |

### Out of scope

- MySQL → PostgreSQL migration (separate sub-spec, brainstormed next)
- Auth/JWT changes at gateway
- Full Kubernetes / ACA deployment manifests (components written, full rollout is an ops project)
- Spring Boot 3.x upgrade

---

## 7. Glossary

| Term | Meaning |
|---|---|
| App-id | Dapr's identity for a service; clients invoke by app-id, not URL |
| Component | A YAML resource defining a Dapr building-block backend (broker, store, etc.) |
| Resiliency policy | Timeout / retry / circuit-breaker definition applied to a target |
| Outbox pattern | Atomic local commit of state + event row; async publisher emits |
| Projection / read model | Denormalized local table maintained from events, queried locally |
| Extraction kit | Standardized template every new service is scaffolded from |
| Stub (Bucket-C) | Empty `@Service` class in `io.sclera.stubs/` representing a future Dapr call to a not-yet-extracted service |
