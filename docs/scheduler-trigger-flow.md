# Scheduler — How Jobs Are Stored & Triggered

How a job goes from "defined in `jobs.yaml`" to "real work runs", and **where the
schedule actually lives** in Dapr.

Companion to `docs/scheduler-execution-runbook.md` (how to build/run the stack) and the
design spec `docs/superpowers/specs/2026-06-02-dapr-scheduler-service-design.md`.

---

## TL;DR

- The **schedule itself** (cron / `@every`) lives in the **Dapr Scheduler control plane**,
  persisted in **embedded etcd** inside the `dapr-scheduler` container — **not** in Postgres
  and **not** in the `sclera-scheduler` app.
- Postgres (`sclera_scheduler` schema) holds only **runtime state** (ENABLED/PAUSED/DISABLED)
  and **run history** — it is *not* what makes a job fire.
- Dapr fires a job by calling back `POST /job/{name}` on the app. That callback **does no
  business logic** — it records the run and publishes a `scheduler.trigger` pub/sub event.
  The **owning service** subscribes and does the real work, then publishes `scheduler.result`.

---

## Where jobs are stored

| What | Where it lives | Survives restart? |
|------|----------------|-------------------|
| The schedule that fires (`0 0 */3 * * *`, `@every 90s`, one-shot `dueTime`) | **Dapr Scheduler control plane → embedded etcd** (`dapr-scheduler` container, `--etcd-data-dir /var/run/dapr/scheduler`, backed by the `scheduler-data` Docker volume) | ✅ yes — etcd remembers it |
| Job catalog source of truth | `sclera-scheduler/src/main/resources/jobs.yaml` (version-controlled) | ✅ in git |
| Runtime state + run history | Postgres `sclera_scheduler` schema (`job`, `job_run`, `job_instance`, `vdms_registry`) | ✅ in Postgres |

Key point: **registering a Dapr job and persisting our DB row are two separate writes.** The
app keeps them in sync via idempotent startup reconcile (`CatalogStartupRunner`), so if the
scheduler app restarts, etcd still has the jobs and the DB still has their state.

```
docker-compose.yml
  dapr-scheduler  (daprio/scheduler:1.15.5)
    ./scheduler --port 50006 --etcd-data-dir /var/run/dapr/scheduler
    volumes: scheduler-data:/var/run/dapr/scheduler   <-- jobs persisted here (etcd)

  sclera-scheduler-dapr  (daprd sidecar)
    --scheduler-host-address dapr-scheduler:50006      <-- sidecar talks to the control plane
```

The **only** code that touches the Dapr Jobs API is
`sclera-scheduler/.../client/SchedulerClient.java`. It POSTs to the *local sidecar*
(`http://localhost:<daprHttpPort>/v1.0-alpha1/jobs/{name}`), and the sidecar forwards to the
control plane, which writes to etcd.

---

## How a job gets registered (so it can fire)

On boot, `CatalogStartupRunner.run()` does, in order:

1. `reconciler.reconcile(catalog.jobs())` — read `jobs.yaml`, upsert `job` rows in Postgres.
2. `jobService.registerAll()` — for every **GLOBAL** ENABLED job, call
   `SchedulerClient.schedule(...)` → registers it in the Dapr Scheduler (etcd).
3. Sync active VDMS list from vdms-service → `vdms_registry`, then
   `registrar.reconcileAll()` — for every **PER_VDMS** job × active VDMS, register a Dapr job
   named `{jobName}::{vdmsId}` (`PerVdmsRegistrar`).

All registration is **idempotent** (re-posting the same job name replaces it), so restarts
never double-register, and DISABLED jobs are intentionally *not* registered.

Dapr job-name conventions (see `JobCallbackController`):

| Form | Meaning |
|------|---------|
| `{jobName}` | a global job |
| `{jobName}::{vdmsId}` | a per-VDMS job instance |
| `{jobName}::{vdmsId}::once-{id}` | a one-shot delayed run (Dapr auto-removes after firing) |

---

## How a job is triggered (the fire path)

```
┌──────────────────────────────────────────────────────────────────────────┐
│ Dapr Scheduler control plane (etcd)                                        │
│   schedule matches → fires the job exactly once, cluster-wide              │
└───────────────┬────────────────────────────────────────────────────────────┘
                │  POST /job/{name}        (only ONE replica receives this)
                ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ sclera-scheduler :  JobCallbackController.onJobFired(name)                 │
│   • parse name → jobName / vdmsId / oneShot                                │
│   • runId = random UUID                                                     │
│   • RunRecorder.recordFired(...)   → job_run row, status = FIRED           │
│   • publish "scheduler.trigger"  { jobName, runId, vdmsId, firedAt }       │
│   • publish fails → return 500 so Dapr RETRIES the fire (at-least-once)    │
└───────────────┬────────────────────────────────────────────────────────────┘
                │  pub/sub (durable, DLQ-backed)
                ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ Owning service  (e.g. device-asset app : TriggerDispatchSubscriber)       │
│   • idempotent on runId (statestore-idempotency) → redelivery is a no-op  │
│   • switch(jobName) → run the real work                                    │
│   • publish "scheduler.result" { jobName, runId, status, durationMs, err? }│
└───────────────┬────────────────────────────────────────────────────────────┘
                │  pub/sub
                ▼
┌──────────────────────────────────────────────────────────────────────────┐
│ sclera-scheduler :  ResultSubscriber.onResult(...)                        │
│   • RunRecorder.recordResult(runId, SUCCESS|FAILED, durationMs, error)    │
│   • updates the job_run row by runId → terminal status                    │
│   • non-terminal / unparseable status → routed to scheduler.result.dlq    │
└──────────────────────────────────────────────────────────────────────────┘
```

### Why this is exactly-once (despite pub/sub redelivery)
The **Scheduler control plane fires each job exactly once cluster-wide.** The `runId` minted
at fire time flows through *both* the trigger and result events, so:
- the dispatcher treats `runId` as the **idempotency key** — a redelivered trigger is a no-op;
- the result updates the run **by `runId`**, so duplicate results are harmless.

### At-least-once on publish failure
If `JobCallbackController` fails to publish the trigger, it returns **500** so Dapr **retries
the fire** — scheduled triggers must never be silently dropped. A retry mints a *new* `runId`;
the orphaned `FIRED` row from the failed attempt is reclaimed by the housekeeping reaper
(`HousekeepingService`). This is expected at-least-once behavior at the publish boundary, while
the dispatcher’s `runId` idempotency keeps *execution* effectively-once.

---

## The events (defined in `dapr-commons`)

- `SchedulerTriggerEvent(jobName, runId, vdmsId, firedAt)` — topic `scheduler.trigger`.
- `SchedulerResultEvent(jobName, runId, status, durationMs, error)` — topic `scheduler.result`,
  DLQ `scheduler.result.dlq`.

---

## Other ways a job fires

- **Run now (manual)** — the UI/API publishes a `scheduler.trigger` immediately with a fresh
  `runId` (`manual=true`); the etcd schedule is untouched. (See the runbook's
  `POST /api/jobs/{name}/run`.)
- **One-shot / delayed** — `SchedulerClient.scheduleOnce(name, dueTime)` registers a job with
  `repeats: 1`; Dapr fires it once at `dueTime` and auto-removes it (used for snooze/delayed runs).

## Control operations and what they do to etcd

| Action | Effect on Dapr (etcd) | Effect on Postgres |
|--------|------------------------|--------------------|
| Pause | `SchedulerClient.delete(name)` — stops firing | state → PAUSED (catalog row kept) |
| Resume | `SchedulerClient.schedule(name, schedule)` — re-armed | state → ENABLED |
| Disable | delete from Scheduler | state → DISABLED; startup reconcile won't re-register it |
| VDMS deactivated | delete all `{job}::{vdmsId}` jobs | instances → DISABLED, registry flagged inactive |

---

## Quick reference — the code

| Concern | File |
|---------|------|
| Only Jobs-API touch-point (register/delete/once) | `sclera-scheduler/.../client/SchedulerClient.java` |
| Receives the fire callback, publishes trigger | `sclera-scheduler/.../web/JobCallbackController.java` |
| Records FIRED / terminal results | `sclera-scheduler/.../service/RunRecorder.java` |
| Updates run by `runId` from result event | `sclera-scheduler/.../subscriber/ResultSubscriber.java` |
| Startup registration / reconcile | `sclera-scheduler/.../catalog/CatalogStartupRunner.java` |
| Global job registration | `sclera-scheduler/.../service/JobService.java` |
| Per-VDMS registration | `sclera-scheduler/.../service/PerVdmsRegistrar.java` |
| Catalog source of truth | `sclera-scheduler/src/main/resources/jobs.yaml` |
| Event types | `dapr-commons/.../events/Scheduler{Trigger,Result}Event.java` |
| Scheduler control-plane container | `docker-compose.yml` service `dapr-scheduler` |
