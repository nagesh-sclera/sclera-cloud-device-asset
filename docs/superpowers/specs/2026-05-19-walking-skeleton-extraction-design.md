# Walking-skeleton extraction — design

**Date:** 2026-05-19
**Repo:** `Microservice123/sclera-cloud-device-asset` (multi-service mono-repo)
**Status:** Draft for review
**Scope:** Skeleton microservices for every AP-Cx / CP-2 extraction target, scaffolded with full Dapr surface, hosting current stub defaults until real implementations land
**Supersedes:** Phase 4 of `2026-05-19-dapr-expansion-and-extraction-design.md`
**Depends on:** Phases 1–3 of that same spec (Dapr foundation, SDK adoption, OTel, resiliency, secrets, idempotency, RabbitMQ→Dapr)
**Out of scope:** DB decoupling, real service implementations, MySQL → PostgreSQL migration

---

## 1. Concept and relationship to the existing Dapr spec

### The walking-skeleton pattern

Instead of extracting microservices one-at-a-time (the original Phase 4), scaffold all 7 target services up front as **skeleton microservices** that exist solely to host the Dapr surface. Each skeleton:

- Boots, registers with its Dapr sidecar, and exposes the full set of HTTP endpoints its eventual real version will own.
- Subscribes to the pub/sub topics it will eventually consume — as no-op handlers that log + return 200.
- Has **no database, no Flyway, no JPA** — endpoint bodies return the same null / default / empty-collection values the current stub methods return inside `sclera-cloud-device-asset`.

System behavior stays identical. What changes is *where* the null/default comes from — produced by a remote service over Dapr, not by an in-process stub.

### What this supersedes in `2026-05-19-dapr-expansion-and-extraction-design.md`

- **Phase 4.2–4.9** (extract one service at a time) → replaced by **Phase A** (scaffold all 7 skeletons in one PR) followed by **Phase B** (per-stub-class client migration in priority order, ~39 PRs).
- **AP-C9 source-missing blocker** → dissolved. Skeleton is scaffolded fresh; no source needed.
- **Top-level `CustomerOrganisationService` missing source** → dissolved for the skeleton phase, same reason.
- **The "extraction kit"** → reused verbatim as the skeleton-service template (same Maven archetype, same Dapr wiring, same naming).
- **Section 5 DB decoupling** → deferred. Skeletons have no DB. The shared `vdms` schema stays put. Decoupling work happens per service when real implementations land — separate future spec.

### What stays from the existing Dapr spec

- Phases 1–3 (Dapr foundation, SDK adoption, OTel, resiliency, secrets, idempotency, RabbitMQ→Dapr) are prerequisites and unchanged. The skeleton work consumes that foundation.
- App-id / port / topic naming conventions, `APP_IDS.md`, `resiliency.yaml`, and the OTel pipeline all carry forward.
- The 32 edge-only Bucket-D stubs stay in `cloud-device-asset` for now — disposition (binding / no-op / delete) handled per call site, not by extraction.

### Why this is better than the original Phase 4

1. **Verification surface is wide from day 1.** Once the scaffolding PR lands, every cross-module call site can be migrated incrementally with confidence — no "does this Dapr wire work?" uncertainty.
2. **Per-stub-class PRs have a tiny blast radius.** One client class + N small skeleton endpoints + one integration test. Reviewable in minutes; bisectable if red.
3. **Source-missing services (AP-C9, top-level CP-2) become non-issues.** Skeleton returns nulls — exactly what the missing source would have produced.

---

## 2. Skeleton service anatomy

### Module layout (same shape across all 7)

```
sclera-<servicekey>/
  pom.xml
  Dockerfile
  src/main/java/io/sclera/<servicekey>/
    Application.java                       # @SpringBootApplication
    controller/                            # one @RestController per stub class
      BacnetController.java
      DaintreeController.java
      ...
    subscriber/                            # @Topic handlers, no-op
      DeviceAuditSubscriber.java
      ...
    defaults/
      Defaults.java                        # central registry of return values
  src/main/resources/
    application.yml                        # port, app-name, no DB
    openapi.yaml                           # contract for sync invoke
    topics.yaml                            # publishes: []   subscribes: [...]
  src/test/java/io/sclera/<servicekey>/
    SkeletonContractTest.java              # one test per endpoint asserts default
  CLAUDE.md
  README.md
```

### Endpoint shape

Each stub method becomes one endpoint. Body returns the matching `Defaults.*` constant. Example for `BacnetService.getDeviceIdByBacnetObjectId(...)`:

```java
@RestController
@RequestMapping("/bacnet")
class BacnetController {
  @GetMapping("/device-id-by-object-id")
  String getDeviceIdByBacnetObjectId(@RequestParam String bacnetDeviceId,
                                      @RequestParam String bacnetObjectId) {
    return Defaults.NULL_STRING;
  }
}
```

### Defaults registry

A central `Defaults` class per skeleton holds every default value used. Single place to find what behavior the eventual real implementation must preserve:

```java
final class Defaults {
  static final String NULL_STRING = null;
  static final Integer ZERO = 0;
  static final Boolean FALSE = Boolean.FALSE;
  static <T> List<T> emptyList() { return List.of(); }
  static <T> Set<T> emptySet() { return Set.of(); }
  // …per stub-inventory.md
}
```

### Subscribers (no-op)

```java
@PostMapping("/internal/device-audit")
@Topic(name = "device.audit-recorded", pubsubName = "pubsub")
ResponseEntity<Void> onDeviceAudit(@RequestBody CloudEvent<Map<String, Object>> evt) {
  log.info("[skeleton] received {}: id={}", evt.getType(), evt.getId());
  return ResponseEntity.ok().build();
}
```

Subscription registers via the Dapr SDK at startup. Delivery proves end-to-end: producer → Dapr → broker → Dapr → subscriber → 200. Every event is INFO-logged with id and type so anyone evolving the skeleton can see what's been delivered.

### Compose entry per skeleton

```yaml
sclera-audit:
  build: ./sclera-audit
  container_name: sclera-audit
  environment:
    SPRING_PROFILES_ACTIVE: docker
    DAPR_HTTP_PORT: "3500"
    DAPR_GRPC_PORT: "50001"
  ports: ["8090:8090"]
  networks: [sclera-net]

sclera-audit-dapr:
  image: daprio/daprd:1.12.0
  command: [
    "./daprd", "--app-id", "sclera-audit", "--app-port", "8090",
    "--dapr-http-port", "3500", "--dapr-grpc-port", "50001",
    "--config", "/dapr/config.yaml",
    "--components-path", "/dapr/components/local",
    "--resiliency-path", "/dapr",
    "--log-level", "info"
  ]
  volumes: ["./dapr:/dapr"]
  network_mode: "service:sclera-audit"
  depends_on: [sclera-audit, redis]
```

### The 7 skeletons

Port assignments allocated in `APP_IDS.md`.

| Skeleton | App-id | Port | Hosts (from `stub-inventory.md`) | Stubs | Topics |
|---|---|---|---|---|---|
| `sclera-audit` | `sclera-audit` | 8090 | HistoryService, ArchivedRecordService, SyslogService (UserActionLogService already real — skip) | 3 | subscribes: `device.audit-recorded` |
| `sclera-identity` | `sclera-identity` | 8091 | UserService, CustomerOrganisationService (source missing — skeleton only), VendorAdminService, PhonebookService | 4 | publishes: `identity.org-renamed`, `identity.user-deactivated` (declared, not yet emitted) |
| `sclera-alerts` | `sclera-alerts` | 8092 | AlertService, AlertProfileService, AlertDowntimeScheduleService, CallFlowRule* | 5 | subscribes: `device.alert-condition-fired` (future); publishes: `alerts.notification-dispatched` (future) |
| `sclera-inventory` | `sclera-inventory` | 8093 | InventoryDeviceService (Product_DetailsService already real — skip) | 1 | – |
| `sclera-workorders` | `sclera-workorders` | 8094 | TicketService, CorrigoService, PmsService, WorkorderTemplateService | 4 | – |
| `sclera-inspection` | `sclera-inspection` | 8095 | CheckListTemplateService, GlobalChecklistService, InspectionRecordService, RecordChecklistService, GlobalInspectionRecordService, GlobalChecklistConditionsService | 6 | – |
| `sclera-integrations` | `sclera-integrations` | 8096 | Bacnet, Daintree, Disruptive, Ecobee, KNX, Lorawan, Modbus, Monnit, Mqtt, Pelican, PolyLens, Siemens, Snmp + IntegrationService, PropertyQrcodeService, AssetMapperService | 16 | – |

The AP-C2 3-way split is deferred. For the skeleton phase, one `sclera-integrations` skeleton hosts all 16. Split happens at real-implementation time when teams differentiate poll-based / vendor-cloud / push paths.

---

## 3. Phased rollout

### Phase A — Scaffolding (one PR, ~3–5 days)

Lands all 7 skeleton services and wiring. Adds the surface; routes no traffic.

| Step | Output |
|---|---|
| A.1 | Skeleton template at `tools/skeleton-template/` — Maven archetype + `scaffold-skeleton.sh` parameterized on `<servicekey>`, `<port>`, list of stubs |
| A.2 | Generate the 7 modules |
| A.3 | Per module: populate `controller/` from `stub-inventory.md` — one `@RestController` per stub class, every endpoint returning `Defaults.*` |
| A.4 | Per module: populate `subscriber/` with no-op `@Topic` handlers for any topic the eventual real service consumes |
| A.5 | Compose: 7 new app entries + 7 new `*-dapr` sidecar entries, shared `dapr/` directory |
| A.6 | `dapr/APP_IDS.md`: register all 7 app-ids, port assignments, exposed methods, topics |
| A.7 | `dapr/resiliency.yaml`: add each new app-id to `scopes:` and `targets.apps:` |
| A.8 | Build, boot, smoke-test all 7 services + sidecars healthy + Jaeger sees a self-test trace for each |

**Phase A exit criteria**
- `docker compose up` brings 10 app services + 9 sidecars healthy (existing 2 + new 7).
- Each skeleton responds to a self-test invoke through its sidecar.
- Each subscribed topic visible in Dapr's subscription registration.
- No call site in `sclera-cloud-device-asset` has changed yet.
- Behavior unchanged externally — stubs still serve all in-process calls.

### Phase B — Per-stub-class client migration (~39 small PRs)

Each PR follows the same shape:

| Step | Action |
|---|---|
| B.1 | Pick next stub class in priority order |
| B.2 | Create matching `<Stub>Client` in `sclera-cloud-device-asset` wrapping `DaprClient.invokeMethod(...)` |
| B.3 | Replace every `@Autowired <Stub>Service` with `@Autowired <Stub>Client`; update call sites |
| B.4 | Delete the stub class from `cloud-device-asset` |
| B.5 | Integration test (`SkeletonContractTest` on the skeleton + `<Stub>ClientTest` in `cloud-device-asset`) verifies every method returns its documented default through Dapr |
| B.6 | Commit: `migrate(stub→client): <ClassName> via Dapr to sclera-<servicekey>` |

### Wave order

| Wave | Target | Stub classes (order) | PRs |
|---|---|---|---|
| 1 | AP-C6 `sclera-audit` | HistoryService → ArchivedRecordService → SyslogService | 3 |
| 2 | CP-2 `sclera-identity` | UserService → CustomerOrganisationService → VendorAdminService → PhonebookService | 4 |
| 3 | AP-C5 `sclera-alerts` | AlertService → AlertProfileService → AlertDowntimeScheduleService → CallFlowRule* (2 classes) | 5 |
| 4 | AP-C8 `sclera-inventory` | InventoryDeviceService | 1 |
| 5 | AP-C3 `sclera-workorders` | TicketService → CorrigoService → PmsService → WorkorderTemplateService | 4 |
| 6 | AP-C4 `sclera-inspection` | CheckListTemplateService → GlobalChecklistService → InspectionRecordService → RecordChecklistService → GlobalInspectionRecordService → GlobalChecklistConditionsService | 6 |
| 7 | AP-C2 `sclera-integrations` | poll-based (Bacnet, Modbus, Snmp, Siemens), then vendor-cloud (Daintree, Ecobee, Monnit, Pelican, PolyLens, Disruptive), then push (KNX, Lorawan, Mqtt), then core (IntegrationService, PropertyQrcodeService, AssetMapperService) | 16 |

Total ≈ **39 client migration PRs**. PRs within a wave are independent — any order works.

**Phase B exit criteria**
- Every stub class in `stub-inventory.md` (excluding edge-only Bucket-D) is replaced with a Dapr client.
- Corresponding `@Service` stub files in `cloud-device-asset` are deleted.
- External behavior unchanged — every documented null/0/empty default still returned.

### What happens after Phase B

- Every cross-module call in the codebase goes over Dapr.
- Each skeleton can be evolved into a real service independently by its owning team.
- DB decoupling (deferred) gets picked up per service when real implementations land — skeletons acquire their own schemas at that point.
- AP-C2 3-way split happens when the integrations team forks the skeleton into AP-C2a/b/c/core.

---

## 4. Risks, testing, PoC scope

### Top risks

| Risk | Mitigation |
|---|---|
| Phase A PR is large (7 services + Compose + sidecars + APP_IDS + resiliency) | Mechanical, template-driven. Split commit-by-commit within the PR (one commit per skeleton). Reviewers walk commit-by-commit. The template is the high-value review surface. |
| Resource overhead of 7 new services + 7 sidecars locally | Skeletons run `-Xmx128m` (no DB, no JPA). Sidecars ~30 MB each. Total added ≈ 1.1 GB. Acceptable for dev laptops; tune per-service heap if tight. |
| Skeleton endpoints drift from real stub signatures | Generate skeleton controllers FROM `stub-inventory.md` via the scaffold script. CI re-runs inventory + diff to catch drift. |
| `Defaults.*` values diverge from original stub returns | Per-migration PR asserts the value matches before/after via test. PR description includes the before/after snippet. |
| Topic subscriptions registered without producers | Phase A subscribes only to topics that already have producers. New subscriptions land with the producer PR, not speculatively. |
| Migration PR changes call-site behavior accidentally | PR template: "before: stub returned X; after: client returns X via Dapr; verified by test Y". Use `block()` to preserve synchronous semantics. |
| Network failure between cloud-device-asset and skeleton turns a previously-impossible NPE into a Dapr exception | `<Stub>Client` wraps each method in try/catch mapping Dapr exceptions to the original return type. Logged + traced. Same observable behavior. |
| No-op subscribers swallow events real implementations would have used | INFO-log every event with id + type. Future implementer greps logs. |
| 39 migration PRs — review fatigue or merge ordering | Wave-internal PRs are independent. Reviewers rotate per wave. CI catches conflicts. |
| Scaffold script becomes load-bearing | Treat `tools/skeleton-template/` as production code: PR review, tests for the script (assert generated output for a sample servicekey), tagged version. |
| AP-C9 / top-level CustomerOrganisationService source still missing for real impls | Skeleton phase unblocks migration. Real-implementation work still needs source confirmation — tracked in `APP_IDS.md`. |

### Testing strategy

| Layer | Tool | What's tested |
|---|---|---|
| Skeleton contract | JUnit + MockMvc per skeleton | Each endpoint returns documented default. Skeleton's own CI. |
| Client contract | JUnit + WireMock / in-process Dapr SDK mock | `<Stub>Client.method()` produces right Dapr invoke shape (verb, path, params, body); unwraps response correctly. |
| End-to-end Dapr wiring (per skeleton) | Testcontainers (Dapr placement + Redis + skeleton) | Skeleton + sidecar up; calls via Dapr invoke and direct HTTP both return the same default. |
| Cross-service (per migration PR) | Compose + RestAssured | `cloud-device-asset` → `<Stub>Client` → Dapr → skeleton → default. Same value as pre-migration. |
| Subscription delivery (per subscribed topic) | Compose + producer test harness | Publish CloudEvent on each subscribed topic; assert skeleton subscriber logs receipt with right id. |
| Trace assertions in CI | Jaeger HTTP API | Per migration PR: span tree `cloud-device-asset → sclera-<servicekey>` with traceparent continuity. Catches silent SDK regression. |
| Default-value regression | Inventory-diff in CI | Re-run `stub-inventory.md` generator on every PR; fail CI if any stub class's documented default would change without explicit acknowledgement. |
| Chaos | `docker pause sclera-<servicekey>` | After migration: pausing skeleton produces graceful default-return (client try/catch), not 500. |

### PoC scope

**PoC = Phase A in full + Wave 1 of Phase B** (three sclera-audit migrations).

Phase A's whole point is that nothing routes to the skeletons until B starts. Wave 1 is the smallest possible exercise of the full flow.

**PoC exit criteria**
1. 7 skeleton services up via `docker compose up`; each healthy; each registered in `APP_IDS.md`.
2. Each subscription visible in Dapr's subscription registration endpoint.
3. Three sclera-audit stub classes migrated to clients; stubs deleted from `cloud-device-asset`; behavior unchanged.
4. End-to-end trace for one history-call path visible in Jaeger spanning `cloud-device-asset → sclera-audit`.
5. Pausing `sclera-audit` 30s during a call returns the documented default (client try/catch) and surfaces a circuit-breaker-open log; no user-visible 500.

### Out of scope

- DB decoupling — skeletons have no DB. Shared `vdms` schema stays put.
- Real implementations of any skeleton — Phase B completion hands off to per-service implementation teams, but that work is not in this spec.
- AP-C2 3-way split — happens at real-implementation time.
- AP-C9 — no source class to scaffold against; tracked separately.
- MySQL → PostgreSQL migration — separate sub-spec.

---

## 5. Glossary

| Term | Meaning |
|---|---|
| Skeleton service | A scaffolded microservice that exists to host a future real service's Dapr surface. Returns hardcoded defaults; has no DB. |
| Walking skeleton | Pattern: stand up the full surface area of a system end-to-end with minimal behavior, then fill in real logic incrementally. |
| Thin Dapr client | Java class in `sclera-cloud-device-asset` that replaces a stub `@Service`. Wraps `DaprClient.invokeMethod(...)` per stub method. |
| Stub-class migration | A PR that replaces one stub class with a thin Dapr client and ensures the matching skeleton endpoints are in place. |
| Defaults registry | Per-skeleton class collecting every documented default value, so eventual real implementations can see exactly what behavior they must preserve. |
| Migration wave | A contiguous group of stub-class migration PRs targeting one AP-Cx skeleton. PRs within a wave are independent. |
