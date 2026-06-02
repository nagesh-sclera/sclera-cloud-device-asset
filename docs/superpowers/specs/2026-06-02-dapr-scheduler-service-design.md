# Dapr Scheduler Service — Design Spec

**Date:** 2026-06-02
**Status:** Approved
**Scope:** Replace the monolith's `@Scheduled` cron jobs with a central `sclera-scheduler` microservice backed by the Dapr Scheduler control plane (Jobs API), with a monitoring/control UI.

---

## Problem Statement

The monolith's `io.sclera.startup.Schedular` class holds ~35 `@Scheduled` methods (Siemens, SNMP, Modbus, Monnit, Daintree, Awair, Airthings, PolyLens, VergeSense, Gaiamesh, inspection, checklists, IOC, user-activity, offline-device check, SQLite migration, etc.). This has several problems:

| Gap | Risk |
|-----|------|
| `@Scheduled` fires on **every** replica once services scale horizontally | Duplicate execution; the only thing preventing it today is the `sclera.service-host=box` single-host gate |
| Cron expressions are hardcoded in Java annotations | No runtime visibility or control; every schedule change is a redeploy |
| Most exceptions swallowed with `log.error`/`System.out` | Job failures are invisible; no history, no status |
| No way to pause, resume, disable, or manually trigger a job | Ops has no control surface |

### Goals (from brainstorming)
- **HA / exactly-once:** each job fires once cluster-wide even with multiple replicas.
- **Decouple schedule from code:** cron strings move out of annotations into a version-controlled catalog + control plane.
- **Monitoring + control UI:** full execution status (schedule, last/next fire, success/fail, duration, error) plus controls (pause / resume / disable / run-now).

---

## Solution Overview

A new central **`sclera-scheduler`** service registers all jobs with the **Dapr Scheduler control plane** via the **Jobs API**. When a job fires, the Scheduler calls the service back; the service publishes a `scheduler.trigger` pub/sub event. The **owning service runs the work** and publishes a `scheduler.result` event, which the scheduler records for the UI.

During migration, the **monolith becomes a thin dispatcher**: one pub/sub subscriber replaces all `@Scheduled` methods, routing trigger events to the existing `SchedularService` methods by job name. No business logic moves. As real services extract later, each takes over its own job's trigger subscription with no change to the scheduler service.

### Chosen approach: Dapr Jobs API (Scheduler control plane)

Considered and rejected:
- **Cron input bindings (`binding-cron-*`)** — stable but static: no run-now/pause without redeploy, no per-job durability/history. Insufficient for the control-surface and HA-with-dynamic-jobs goals.
- **Clustered Quartz (JDBC store)** — mature HA + control + history, but does not use the Dapr Scheduler at all and adds a second scheduling engine.

**Caveat & mitigation:** the app-facing Jobs API is **alpha** in Dapr (the Scheduler control plane itself is stable — it already backs actor reminders and workflows). Mitigation: the Dapr version is pinned, and all Jobs API calls are isolated behind a single `SchedulerClient` class so an API change touches exactly one file. Quartz remains a fallback if the alpha surface proves unstable.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                      sclera-scheduler (NEW service)                    │
│  app-id: sclera-scheduler   port: 8090-8099 range   schema: sclera_scheduler│
│                                                                        │
│  ┌────────────────┐   ┌─────────────────┐   ┌──────────────────────┐  │
│  │ JobCatalog     │   │ SchedulerClient │   │ JobCallbackController │  │
│  │ - jobs.yaml    │   │ (Dapr Jobs API  │   │ POST /job/{name}      │  │
│  │   seed         │──▶│  wrapper, the   │◀──│  ← Dapr Scheduler     │  │
│  │ - DB overrides │   │  ONLY alpha     │   │  fires here           │  │
│  └────────────────┘   │  touch-point)   │   └─────────┬────────────┘  │
│           │           └─────────────────┘             │ publishes     │
│           ▼                    ▲                       ▼               │
│  ┌────────────────┐            │            ┌──────────────────────┐  │
│  │ JobService     │────────────┘            │ DaprEventPublisher   │  │
│  │ (register/pause│                         │  scheduler.trigger   │  │
│  │  /resume/run)  │                         └──────────────────────┘  │
│  └───────┬────────┘                                                   │
│          │                     ┌──────────────────────┐               │
│          ▼                     │ ResultSubscriber     │ subscribes    │
│  ┌────────────────┐            │  scheduler.result    │◀────────────  │
│  │ Postgres       │◀───────────│  → update job_run    │               │
│  │ job, job_run   │            └──────────────────────┘               │
│  └────────────────┘                                                   │
│          ▲                     ┌──────────────────────┐               │
│          └─────────────────────│ SchedulerApiController│  REST + UI   │
│                                │  /api/jobs ... + page │               │
│                                └──────────────────────┘               │
└──────────────────────────────────────────────────────────────────────┘
        │ scheduler.trigger (pub/sub)              ▲ scheduler.result
        ▼                                          │
┌──────────────────────────────────────────────────────────────────────┐
│   Monolith (box host) — TriggerDispatchSubscriber                      │
│   - extends DaprEventSubscriber<SchedulerTriggerEvent>                 │
│   - switch(jobName) → existing schedularService.xxx() / migrationSvc   │
│   - publishes SchedulerResultEvent (success|fail, duration, error)     │
│   - @Scheduled + @EnableScheduling + service-host=box gate REMOVED     │
└──────────────────────────────────────────────────────────────────────┘
```

---

## Components

### In `sclera-scheduler` (new service)

1. **`JobCatalog`** — loads the declarative `jobs.yaml` (the version-controlled source of truth for the active jobs: name, cron/`@every`, owning-service tag, trigger topic, default-enabled) and reconciles it into the `job` table on startup. New yaml entries are inserted; existing runtime `state` (ENABLED/PAUSED/DISABLED) in the DB is never clobbered.
2. **`SchedulerClient`** — the **only** class that touches the alpha Dapr Jobs API (register/delete a job; cron and `@every` schedules). Isolates the alpha risk. Register is upsert-like (idempotent on restart).
3. **`JobService`** — orchestration: on startup registers all ENABLED jobs via `SchedulerClient`; implements pause (delete job from Scheduler, DB → PAUSED), resume (re-register, DB → ENABLED), disable (delete + DB → DISABLED), run-now (publish trigger immediately with a fresh `runId`, schedule untouched).
4. **`JobCallbackController`** — `POST /job/{name}`, invoked by the Dapr Scheduler when a job fires. Inserts a `job_run` row (status FIRED, fresh `runId`), then publishes `scheduler.trigger`.
5. **`ResultSubscriber`** — subscribes to `scheduler.result`; updates the matching `job_run` by `runId` (SUCCESS/FAILED, `finished_at`, `duration_ms`, `error`). Extends `DaprEventSubscriber<SchedulerResultEvent>` from `dapr-commons`.
6. **`SchedulerApiController`** — REST API for the UI; also serves the static dashboard page.

### In the monolith

- **`TriggerDispatchSubscriber`** — extends `DaprEventSubscriber<SchedulerTriggerEvent>` from `dapr-commons`. A single subscriber replacing all `@Scheduled` methods. `switch(jobName)` calls the existing `schedularService.*` / `databaseMigrationService.*` method, then publishes a `SchedulerResultEvent`. Idempotent on `runId` via `statestore-idempotency`.

---

## Data Flow

### Normal fire path (exactly-once)

```
Dapr Scheduler (etcd, cluster-wide single fire)
   │ on schedule
   ▼
POST /job/snmpSync           ← only ONE scheduler replica gets this
   │
   ├─ insert job_run(jobName=snmpSync, runId=<uuid>, status=FIRED, firedAt=now)
   └─ publish scheduler.trigger  { jobName: "snmpSync", runId: "<uuid>", firedAt }
        │ (pub/sub, durable, DLQ-backed)
        ▼
   Monolith TriggerDispatchSubscriber (idempotent on runId via statestore-idempotency)
        ├─ t0 = now
        ├─ switch("snmpSync") → schedularService.scheduleSnmpSync()
        ├─ publish scheduler.result { jobName, runId, status: SUCCESS|FAILED,
        │                             durationMs: now-t0, error? }
        ▼
   Scheduler ResultSubscriber → update job_run(runId) SUCCESS/FAILED, duration, error
```

**Why exactly-once holds despite pub/sub redelivery:** the Scheduler control plane fires each job exactly once cluster-wide. The `runId` minted at fire time flows through both events, so the dispatcher uses it as the idempotency key — a redelivered trigger event is a no-op. The result event updates the run by `runId`.

### Control operations (from the UI)

| Action | What happens |
|--------|--------------|
| Pause | `SchedulerClient.delete(name)` — Dapr stops firing; DB → PAUSED. Catalog row retained. |
| Resume | `SchedulerClient.register(name, schedule)` — re-armed; DB → ENABLED. |
| Disable | Delete from Scheduler + DB → DISABLED; survives restart (startup reconcile respects DB state, does not re-register DISABLED jobs). |
| Run now | Publish `scheduler.trigger` immediately with a fresh `runId` (records a run, `manual=true`). Schedule untouched. |

---

## Job Catalog

`jobs.yaml` is the version-controlled source of truth. One entry per active job:

```yaml
jobs:
  - name: snmpSync
    schedule: "0 0 */3 * * *"      # 6-field cron
    owner: integrations
    triggerTopic: scheduler.trigger
    enabled: true
  - name: offlineDeviceCheck
    schedule: "@every 90s"          # was @Scheduled(fixedRate=90000)
    owner: device-asset
    enabled: true
```

### Mapping the monolith's methods

- **Active cron jobs (~30)** → one catalog entry each. `cron = "0 0 0 */3 * *"` maps directly to Dapr's 6-field cron; `fixedRate`/`initialDelay` → `@every`.
- **Commented-out methods** (`schedulePelicanValueUpdate`, `checkP2PSocketStatus`, `checkIntegrationSocketStatus`, `scheduleUnlinkedArea`, the standalone `scheduleLorawanDownlink`, `scheduleUpdateBacnetMeasuringInstruments`, `ensureTenantPool`) → **excluded** (not running today; YAGNI).
- **Composite methods** that do several things in one tick (e.g. `scheduleVdmsSystemHealth` = lorawan downlink + health + IOC sync; `scheduleHourlyIntegrationsSync` = polylens + vergesense; `scheduleConnectedStatusForIOC` = IOC status + user-activity; `scheduleGlobalInspectionRecords` = restart-occurrence + inspection) → stay as **one job** firing one dispatcher case that calls the same sequence. Behavior is preserved exactly; they are **not** split in this project.
- **Dynamic args** (`currentTimestamp = System.currentTimeMillis()` in user-activity/action-log) → computed inside the dispatcher at execution time. The event carries only `jobName`, `runId`, `firedAt`.
- **`box`/edge-only jobs** (`createSQLiteFileScheduled`; tenant pool stays excluded as it is commented out) → catalog `owner: edge`, dispatched in the monolith like the rest. The old `service-host=box` gate is replaced by deployment scoping: these jobs are dispatched only in the box runtime where the dispatcher exists.

---

## Persistence

Postgres schema `sclera_scheduler`, Flyway-managed.

```
job
  name            text  PK          -- "snmpSync"
  schedule        text              -- "0 0 */3 * *" or "@every 90s"
  owner           text              -- "integrations"
  trigger_topic   text  default 'scheduler.trigger'
  state           text              -- ENABLED | PAUSED | DISABLED
  last_run_id     uuid  null
  next_fire_at    timestamptz null  -- best-effort, derived from schedule
  created_at / updated_at

job_run
  run_id          uuid  PK
  job_name        text  FK -> job.name
  status          text              -- FIRED | SUCCESS | FAILED
  manual          boolean default false
  fired_at        timestamptz
  finished_at     timestamptz null
  duration_ms     bigint null
  error           text null
  index (job_name, fired_at desc)   -- powers history view
```

`job` is reconciled from `jobs.yaml` at startup (insert new entries, never clobber `state`). `job_run` is append-only history; a retention job (one of the scheduler's own jobs) prunes runs older than N days.

---

## REST API

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/jobs` | List all jobs + latest run summary (status, last fired, next fire) |
| GET | `/api/jobs/{name}` | Job detail |
| GET | `/api/jobs/{name}/runs?limit=50` | Run history |
| POST | `/api/jobs/{name}/pause` | Pause |
| POST | `/api/jobs/{name}/resume` | Resume |
| POST | `/api/jobs/{name}/disable` | Disable |
| POST | `/api/jobs/{name}/run` | Run now |

---

## UI

One static `scheduler.html` (same self-contained style as the existing `test-ui/` pages), served by the service. No build step, no framework — plain HTML/JS hitting the REST API.

- Table: job name · owner · schedule · state · last run (green SUCCESS / red FAILED / grey FIRED-pending) · last duration · next fire.
- Row actions: Pause / Resume / Disable / Run now.
- Click a row → run-history panel (last 50 runs with timestamps, durations, errors).
- Polls `/api/jobs` every few seconds.

---

## Error Handling & Resiliency

- **DLQs** for both topics (`scheduler.trigger.dlq`, `scheduler.result.dlq`) per the pub/sub hardening spec. The dispatcher classifies via the inherited `isPermanentError()`: unknown/bad `jobName` → DROP (can never succeed); transient downstream failure → RETRY.
- **The dispatcher always reports a result.** Success or thrown exception, it publishes a `scheduler.result` — a failed job shows **FAILED + error** in the UI rather than hanging in FIRED. (Fixes the current code's swallowed-exception blindness.)
- **Orphan/stuck runs:** a `job_run` stuck in FIRED past a timeout (result never arrived, e.g. monolith was down) is swept to `FAILED (timed out)` by a reaper so the dashboard reflects reality.
- **Scheduler control plane down:** jobs persist in etcd; on recovery Dapr re-fires missed jobs per its catch-up policy. Startup reconcile is idempotent (register is upsert-like), so restarts never double-register.
- **Single alpha touch-point:** if the Jobs API surface changes between Dapr versions, only `SchedulerClient` changes. Dapr version pinned in compose/k8s.

---

## Testing Strategy

Mirrors the 3-layer `dapr-commons` strategy.

- **L1 unit:** `JobCatalog` reconcile (yaml ↔ DB merge preserves `state`); `JobService` control ops; dispatcher `switch(jobName)` routing. Plain methods, no Dapr.
- **L2 integration:** stub `SchedulerClient` + state store. Verify: callback records a run and publishes trigger; result subscriber updates the correct `run_id`; duplicate trigger (same `runId`) is a no-op.
- **L3 component (Docker Compose):** real Dapr sidecar + Scheduler control plane + Redis + Postgres. Register a `@every 5s` test job; assert it fires once cluster-wide (2 scheduler replicas → still one run per tick); trigger flows to a stub dispatcher; result updates the row; forced permanent failure lands in the DLQ.

---

## Rollout (incremental, reversible)

1. Build `sclera-scheduler` (service + schema + `SchedulerClient` + UI) with a couple of low-risk jobs (`offlineDeviceCheck`, `syncAssetCountToCloud`). Monolith dispatcher handles just those two; their `@Scheduled` methods are commented out, the rest still run. Both mechanisms coexist.
2. Validate exactly-once + UI in staging.
3. Migrate remaining jobs in batches by owner, removing each `@Scheduled` as its catalog entry goes live.
4. When the catalog is complete, remove `@EnableScheduling` and the `service-host=box` gate from the monolith; delete the `Schedular` class. `SchedularService` stays (called by the dispatcher).
5. As real services extract later, each takes over its jobs' trigger subscription from the monolith dispatcher — no scheduler-service change needed.

---

## Services Affected

| Service | Change |
|---------|--------|
| `sclera-scheduler` | **New service** — catalog, `SchedulerClient`, callback, result subscriber, REST API, UI, Postgres schema |
| `dapr-commons` | Add `SchedulerTriggerEvent` and `SchedulerResultEvent` record DTOs |
| Monolith (`io.sclera.startup`) | Add `TriggerDispatchSubscriber`; remove `@Scheduled`/`@EnableScheduling`/`service-host=box` gate; delete `Schedular` (phased); keep `SchedularService` |
| `dapr/components/*/pubsub.yaml` | Add `scheduler.trigger`, `scheduler.result`, and their `.dlq` topics |
| Dapr config / compose / k8s | Ensure Scheduler control plane enabled; pin Dapr version; register `sclera-scheduler` app-id + sidecar |
| `dapr/APP_IDS.md` | Register `sclera-scheduler` app-id, ports, topics |
| Root `pom.xml` | Register `sclera-scheduler` module |

---

## Out of Scope

- Splitting composite jobs into separate jobs.
- Moving business logic out of `SchedularService`.
- Per-tenant / dynamic job authoring via the UI (the Jobs API supports runtime jobs, but the catalog is yaml-driven; no create-job UI).
- Kubernetes manifests beyond component/sidecar wiring.
- Migrating actor reminders or workflows onto the Scheduler (only the Jobs API surface is used here).
