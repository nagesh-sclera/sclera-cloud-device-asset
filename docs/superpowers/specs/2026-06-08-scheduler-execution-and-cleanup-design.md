# Scheduler End-to-End Execution + Cleanup Batch — Design Spec

**Date:** 2026-06-08
**Status:** Approved
**Scope:** Close the scheduler loop so device-asset-owned jobs actually execute end-to-end
(FIRED → real work → SUCCESS/FAILED on the dashboard), with a dev-only simulator covering
the jobs owned by services not running in this workspace. Plus a small low-risk cleanup
batch (image-url bug fixes, `Sclara`→`Sclera` rename, scheduler multi-stage Dockerfile).

Builds on the approved `2026-06-02-dapr-scheduler-service-design.md`. This spec only adds the
**owning-service side** of the loop (the dispatcher) plus the dev simulator and cleanups;
the `sclera-scheduler` service, catalog, callback, `ResultSubscriber`, and UI already exist.

---

## Background

The `sclera-scheduler` service is live: the Dapr Scheduler fires a job → `POST /job/{name}`
records a `job_run` (status FIRED) and publishes a `scheduler.trigger` event → the owning
service is meant to run the work and publish a `scheduler.result` → the scheduler's
`ResultSubscriber` flips the run to SUCCESS/FAILED.

Today **step 3 has nobody home.** The real dispatcher (`TriggerDispatchSubscriber`) was
designed to live in the monolith `sclera-vdms-edge-server`, which is **read-only** per
`CLAUDE.md`. So in the running stack, triggers fire but no one executes them or reports a
result — runs hang in FIRED until a result is hand-published. This spec fills that gap for
the jobs this workspace actually owns.

### Contract (already implemented, unchanged)

- `SchedulerTriggerEvent(jobName, runId, firedAtEpochMs)` — published on `scheduler.trigger`.
- `SchedulerResultEvent(jobName, runId, status["SUCCESS"|"FAILED"], durationMs, error)` —
  published on `scheduler.result`; `error` null on success.
- Both DTOs exist in `dapr-commons` (`io.sclera.dapr.events.*`).
- Subscribers extend `DaprEventSubscriber<T>` (`dapr-commons`); permanent errors route to a
  `.dlq` companion topic via `@Topic(deadLetterTopic=...)`.
- `runId` is the idempotency key carried through both events.

### Job ownership (from `sclera-scheduler/src/main/resources/jobs.yaml`)

**device-asset owned (10) — must execute for real:**
`historyRecord`, `unlinkVendorOrganisation`, `internetBandwidthCheck`, `vdmsSystemHealth`,
`connectedStatusForIOC`, `qrcodeNfcBarcodeSync`, `syncAssetCountToCloud`, `userActionLog`,
`deviceDndEnable`, `offlineDeviceCheck`.

**other owners (integrations / inventory / inspection / edge) — simulated:** the remaining
catalog jobs belong to services that are skeletons / not implemented in this workspace.

---

## Part 1 — Cleanup batch (done first; low-risk)

These are independent of the dispatcher feature and ship first.

### 1.1 `DeviceService` image-url copy-paste bugs

Four `getImage_url_1()` calls that should reference `_2`/`_3`, in
`sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java`:

| Method | Line | Current | Fix |
|--------|------|---------|-----|
| `tagProductImages` | 800 | 2nd `addProductImages` arg `getImage_url_1()` | `getImage_url_2()` |
| `retagProductImages` | 819 | `getImageExtensionByImageUrl(getImage_url_1())` (in the `image_url_2` block) | `getImage_url_2()` |
| `retagProductImages` | 824 | `getImageExtensionByImageUrl(getImage_url_1())` (in the `image_url_3` block) | `getImage_url_3()` |
| `retagProductImages` | 833 | 2nd `addProductImages` arg `getImage_url_1()` | `getImage_url_2()` |

Line numbers are indicative; match on the surrounding block, not the number.

### 1.2 `Sclara` → `Sclera` rename

Replace the misspelling `Sclara` with `Sclera` in user-facing assets:
`test-ui/index.html`, `test-ui/traces.html`, `architecture.html`, `ARCHITECTURE.md`,
`start-local.ps1`, `db-init/init.sql`. Leave historical plan/spec docs under
`docs/superpowers/` untouched (audit trail). Verify with a repo grep for `Sclara` after.

### 1.3 `sclera-scheduler` multi-stage Dockerfile

Current `sclera-scheduler/Dockerfile` is runtime-only (`COPY target/*.jar`), so the image
only builds after a host `mvnw package` with `dapr-commons` already installed in `.m2`.
Convert to multi-stage:

- **Build stage** (`maven:3.9-eclipse-temurin-21` or equivalent): build `dapr-commons` then
  `sclera-scheduler` from source so the image is self-contained.
- **Runtime stage** (`eclipse-temurin:21-jre-jammy`): copy the built jar; keep
  `EXPOSE 8098`, `JAVA_OPTS`, entrypoint as-is.

Build context must include both `dapr-commons` and `sclera-scheduler` sources — confirm the
compose `build.context`/`dockerfile` for the scheduler service covers this; adjust if needed.
**Non-goal:** changing how the other services are built.

### 1.4 Non-change: `Product_DetailsRepository` PK

Previously flagged as a String-vs-Long mismatch. It is **already aligned**: the entity is
`@Id private String id` and the repo is `JpaRepository<Product_Details, String>`. No change;
the stale memory note will be corrected.

---

## Part 2 — End-to-end scheduler execution

### 2.A Real dispatcher in `sclera-cloud-device-asset`

A new `TriggerDispatchSubscriber` (package `io.sclera.scheduler` or `io.sclera.subscriber`,
following the module's existing layout) extending `DaprEventSubscriber<SchedulerTriggerEvent>`.

**Subscription:** `@Topic(name="scheduler.trigger", pubsubName="pubsub",
deadLetterTopic="scheduler.trigger.dlq")` on a `POST` mapping; delegates to `onEvent(...)`.

**Routing:** holds the explicit set of the 10 device-asset job names. `handleEvent`:

1. If `jobName` is **not** in the owned set → return without publishing a result (another
   service owns it; this is a no-op for us, NOT a DLQ case).
2. Idempotency: if `runId` already processed (existing state-store idempotency helper used
   elsewhere in the module) → no-op.
3. `t0 = System.currentTimeMillis()`; `switch(jobName)` calls the thin handler.
4. On success → publish `SchedulerResultEvent(jobName, runId, "SUCCESS", now-t0, null)`.
5. On thrown exception → publish `SchedulerResultEvent(jobName, runId, "FAILED", now-t0,
   message)`. The dispatcher **always reports a result** so the dashboard never hangs in
   FIRED. (Genuinely-permanent routing failures, e.g. unknown owned job, still use the base
   class's permanent-error → DLQ path.)

**Thin handlers — mapping rule:** each handler calls the **same `SchedularService`
sequence the monolith's identically-named `@Scheduled` method calls**, reimplemented against
the device-asset services that hold the building blocks. Reference:
`Desktop\AssetManagement POD\New folder\sclera-vdms-edge-server\src\main\java\io\sclera\startup\{Schedular,SchedularService}.java`
(read-only; reference only). Confirmed mappings from the monolith `Schedular`:

| Job | Monolith behavior (reference) |
|-----|-------------------------------|
| `historyRecord` | `scheduleHistoryRecord()` |
| `unlinkVendorOrganisation` | `scheduleUnlinkVendorOrganisation()` |
| `internetBandwidthCheck` | `scheduleInternetBandwidthCheck()` |
| `vdmsSystemHealth` | composite: `scheduleLorawanDownlink()` + `scheduleVdmsSystemHealth()` + `scheduleVDMSDataForIOCSync()` |
| `connectedStatusForIOC` | composite: `scheduleConnectedStatusForIOC()` + `scheduleUserActivityData(now)` |
| `qrcodeNfcBarcodeSync` | `scheduleQrcodeNFCBarcodeSync()` |
| `syncAssetCountToCloud` | `syncAssetCountToCloud()` |
| `userActionLog` | resolve against the monolith's same-purpose method; building block `UserActionLogService` present here |
| `deviceDndEnable` | resolve against the monolith's DND method; building blocks present in `DeviceService` (DND update logic ~L2811–2876) |
| `offlineDeviceCheck` | resolve against the monolith's offline-check method; offline-detection logic present in `DeviceService` |

The plan enumerates the exact device-asset method call(s) per job. Composite jobs stay **one
job firing the same sequence** — not split (consistent with the base scheduler spec).

**Stub-with-WARN fallback:** if a handler's underlying dependency is genuinely absent from
this service (Bucket-C territory per `CLAUDE.md`), the handler logs one WARN and returns
normally (run shows SUCCESS, not stuck). Every such case is listed in the implementation
plan as an explicit follow-up so it is not silently swallowed.

**Publisher / config:** reuse `DaprEventPublisher` from `dapr-commons` (add the bean to the
module if not already wired). Result topic + pubsub name come from config
(`scheduler.pubsub-name`, `scheduler.result-topic`) mirroring the scheduler service's
property style.

### 2.B Dev-only simulator in `sclera-scheduler`

A `TriggerSimulatorSubscriber`, `@Profile({"dev","docker"})` (so it is **off in prod**),
subscribing to `scheduler.trigger` (DLQ `scheduler.trigger.dlq`).

- For each trigger, look up the job's `owner` in the catalog (DB `job` table / loaded
  `jobs.yaml`). If `owner == device-asset` → **skip** (the real device-asset dispatcher owns
  it; prevents double results). Otherwise publish
  `SchedulerResultEvent(jobName, runId, "SUCCESS", <small fixed/simulated durationMs>, null)`.
- Does no real work; existence is logged clearly as a stand-in for absent owning services.

**Why owner-lookup, not a static list:** the trigger event has no `owner` field, and the
scheduler already owns the catalog — looking it up keeps the device-asset/simulated split in
one source of truth (`jobs.yaml`).

### 2.C Wiring

- **DTOs:** reuse existing `SchedulerTriggerEvent` / `SchedulerResultEvent` in `dapr-commons`
  (no change).
- **Topics:** `scheduler.trigger` / `scheduler.result` auto-create on Redis pub/sub; `.dlq`
  companions are declared per-subscriber via `@Topic(deadLetterTopic=...)` (matches the
  existing `ResultSubscriber`). Ensure `sclera-cloud-device-asset` has the `pubsub` Redis
  component (it does: `dapr/components/pubsub.yaml`).
- **App-id / sidecar:** `sclera-cloud-device-asset` already runs with a Dapr sidecar; the new
  subscriber endpoint is auto-registered by the `@Topic` annotation.

---

## Data Flow (device-asset job, end-to-end)

```
Dapr Scheduler (cron) ──► POST /job/offlineDeviceCheck   (sclera-scheduler)
   └─ record job_run(FIRED, runId) ─► publish scheduler.trigger {offlineDeviceCheck, runId}
        │
        ├─► sclera-cloud-device-asset  TriggerDispatchSubscriber
        │      owned? yes ─► t0 ─► offlineDeviceCheck handler (real work)
        │      └─ publish scheduler.result {runId, SUCCESS|FAILED, durationMs, error?}
        │
        └─► sclera-scheduler  TriggerSimulatorSubscriber  (dev/docker only)
               owner == device-asset ─► SKIP   (no result; avoids double-handling)

scheduler.result ─► sclera-scheduler ResultSubscriber ─► job_run(runId) → SUCCESS/FAILED
```

A non-device-asset job (e.g. `snmpSync`) is the mirror image: the device-asset dispatcher
skips it; the simulator publishes a simulated SUCCESS.

---

## Error Handling & Resiliency

- Dispatcher always emits a result (success or FAILED+error) → no stuck-FIRED rows for
  owned jobs.
- Unknown/non-owned jobs are no-ops in each subscriber (return OK, no result) — not DLQ.
- Structurally invalid events (e.g. missing fields) follow the base class permanent-error →
  `.dlq` path already used by `ResultSubscriber`.
- Duplicate trigger redelivery (same `runId`) is idempotent in the dispatcher.
- The scheduler's existing housekeeping reaper still sweeps any run stuck in FIRED past its
  timeout (e.g. dispatcher down), so the dashboard stays truthful.

---

## Testing Strategy

- **Cleanup batch:** existing suite stays green; no new heavy tests. A repo grep confirms no
  remaining `Sclara`; the four image-url fixes are covered by reading the corrected calls.
- **Dispatcher (L1 unit, pure Mockito per the module's test convention):**
  `switch(jobName)` routes each owned job to the expected service call(s); non-owned job →
  no result published; success path publishes SUCCESS with a duration; thrown handler →
  FAILED + error; duplicate `runId` → no-op.
- **Simulator (L1 unit):** device-asset-owned job → skipped; other-owner job → SUCCESS
  published; profile-gated bean only loads under dev/docker.
- **Live (Docker stack):** register/observe `offlineDeviceCheck` (`@every 90s`) firing →
  dispatcher runs → run flips FIRED→SUCCESS on the dashboard with a real duration, **no
  hand-published result**. Observe a non-device-asset job flip to SUCCESS via the simulator.

---

## Out of Scope

- The real monolith dispatcher (`sclera-vdms-edge-server` is read-only).
- Sensor-integration tables (`lorawan_sensor_attributes` + siblings) — stays `// PG-gap` per
  loose-coupling.
- Splitting composite jobs into separate jobs.
- Porting full monolith `SchedularService` logic where the device-asset building blocks are
  absent — those become explicit stub-with-WARN follow-ups, not new ports.
- Any change to how services other than `sclera-scheduler` are containerized.

---

## Files Touched (anticipated)

| Area | Change |
|------|--------|
| `sclera-cloud-device-asset/.../service/DeviceService.java` | 4 image-url getter fixes |
| `test-ui/*.html`, `architecture.html`, `ARCHITECTURE.md`, `start-local.ps1`, `db-init/init.sql` | `Sclara`→`Sclera` |
| `sclera-scheduler/Dockerfile` (+ compose build context) | multi-stage build |
| `sclera-cloud-device-asset/.../TriggerDispatchSubscriber.java` (new) + thin handlers | real dispatcher for 10 jobs |
| `sclera-cloud-device-asset` config + `DaprEventPublisher` bean | result publishing wiring |
| `sclera-scheduler/.../TriggerSimulatorSubscriber.java` (new) | dev/docker-only simulator |
| `*ServiceTest` / subscriber unit tests | L1 coverage for dispatcher + simulator |
