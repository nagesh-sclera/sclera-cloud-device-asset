# Centralised Per-VDMS Fan-out — Design

**Date:** 2026-06-10
**Service:** `sclera-scheduler`
**Status:** Proposed

## Problem

Some jobs must run for **every active VDMS** on a **single shared schedule** — fire
once on the interval, then update the related data for all VDMS ids in the database.

The existing `PER_VDMS` scope does not fit this: it registers a **separate, timezone-aware
Dapr job per VDMS** (`{jobName}::{vdmsId}`), so the same logical job fires at *different*
wall-clock times per VDMS. We want one centralised schedule that fans out to all active VDMS.

## Goal

Add a **`CENTRALISED`** job scope: one Dapr job on one schedule; when it fires, the scheduler
publishes **one `scheduler.trigger` event per active VDMS**, each carrying its own `runId` and
`vdmsId`. Owning services consume each event through the **existing** per-VDMS path. The result
is **N run rows per fire**, with per-VDMS success/failure visible in the dashboard.

Non-goals: changing `GLOBAL` or `PER_VDMS` behavior; changing any owning-service consumer;
per-VDMS timezone handling for centralised jobs (centralised jobs fire on one shared schedule).

## Decisions (from brainstorming)

- **Fan-out point:** the scheduler publishes one trigger per VDMS at fire time (not a single
  event that the owning service loops over). Reuses the existing `vdmsId` pipeline and keeps
  per-VDMS run history, retries, and DLQ.
- **Scope model:** a **new** `CENTRALISED` scope alongside `GLOBAL` and `PER_VDMS`. The existing
  `PER_VDMS` timezone-aware behavior is kept; jobs opt into the new model in `jobs.yaml`.
- **Target VDMS:** **active only** — `vdms_registry.active = true` (same set the per-VDMS
  reconcile uses). Deactivated VDMS are skipped.
- **Error handling (partial publish failure):** **best-effort** — see below.
- **Job to flip:** `vdmsSystemHealth` (currently the only `PER_VDMS` job) becomes `CENTRALISED`.
  *(Override on review if other jobs should be centralised, or if vdmsSystemHealth should stay PER_VDMS.)*

## Architecture & flow

A centralised job is registered as a **single Dapr job** (one schedule, like `GLOBAL`). On fire,
the callback detects the scope, loads active VDMS ids, and fans out:

```
Dapr job "vdmsSystemHealth" fires on its schedule
   │  POST /job/vdmsSystemHealth        (bare name, no ::vdms suffix)
   ▼
JobCallbackController → TriggerDispatcher.dispatch("vdmsSystemHealth")
   scope = CENTRALISED
   → SELECT vdms_id FROM vdms_registry WHERE active = true
   → for each vdms:
        runId = UUID
        RunRecorder.recordFired(job, runId, oneShot=false, vdmsId)
        publish scheduler.trigger { job, runId, vdmsId, firedAt }
   ▼  N events  (durable pub/sub, DLQ-backed)
Owning service (existing per-VDMS consumer) updates each VDMS, publishes scheduler.result per runId
   ▼
ResultSubscriber updates each run by runId  →  N run rows, per-VDMS status
```

`GLOBAL` is unchanged: `TriggerDispatcher.dispatch(name)` publishes a single trigger with
`vdmsId = null`. `PER_VDMS` is unchanged: `PerVdmsRegistrar` still registers
`{jobName}::{vdmsId}` jobs, and those fire individually through the existing
`{jobName}::{vdmsId}` callback path.

## Components & changes

| # | Change | File(s) |
|---|--------|---------|
| 1 | Add `CENTRALISED` to the scope enum | `domain/JobScope.java` |
| 2 | `TriggerDispatcher` (new): `dispatch(jobName)` looks up scope. `GLOBAL` → publish 1 trigger (`vdmsId=null`). `CENTRALISED` → load active VDMS, publish 1 trigger per VDMS (each own `runId`, each `recordFired(...,vdmsId)`). Best-effort per VDMS. Returns whether all publishes succeeded. | new `service/TriggerDispatcher.java` |
| 3 | `JobCallbackController.onJobFired` delegates to `TriggerDispatcher` instead of inline single-publish. For a centralised job the incoming name is bare `{jobName}`. The existing `{jobName}::{vdmsId}` (PER_VDMS) parsing path is preserved. | `web/JobCallbackController.java` |
| 4 | `JobService.runNow(name)` delegates to `TriggerDispatcher` → run-now on a centralised job also fans out to all active VDMS | `service/JobService.java` |
| 5 | **Fix:** `JobService.registerAll()` registers only `GLOBAL` + `CENTRALISED` jobs as single Dapr jobs; **skips `PER_VDMS`** (those are owned by `PerVdmsRegistrar`). Removes the current latent double-registration where a bare `vdmsSystemHealth` Dapr job was registered alongside its per-VDMS instances. | `service/JobService.java` |
| 6 | **Orphan teardown:** when a job is no longer `PER_VDMS` but has `job_instance` rows, delete its `{jobName}::{vdmsId}` Dapr jobs and mark those instances `DISABLED`. Idempotent, runs in the startup reconcile path. Prevents the old per-VDMS jobs from firing alongside the new centralised job. | `service/PerVdmsRegistrar.java` (or reconcile path) |
| 7 | Flip `vdmsSystemHealth` to `scope: CENTRALISED` | `resources/jobs.yaml` |

Unchanged: `PerVdmsRegistrar`, control ops (`pause`/`resume`/`disable` operate on the single job
by name, same as `GLOBAL`), `ResultSubscriber`, owning-service consumers, and the DB schema
(`job.scope` is already a string-valued enum column — **no migration required**; `CENTRALISED`
is a new permitted value).

## Error handling — partial publish failure

With fan-out, returning **500** from the callback would make Dapr refire the **whole** job →
every VDMS re-triggered → duplicate runs for VDMS that already succeeded. A refire mints new
`runId`s, so the dispatcher's `runId` idempotency cannot dedupe them.

**Chosen: best-effort fan-out.** Record + publish per VDMS; on a publish failure, **log and
continue**, return **200**. Stuck `FIRED` rows from failed publishes are swept to `FAILED` by the
existing `HousekeepingService` reaper. This matches how `JobService.runNow` already behaves.

Edge cases:
- **No active VDMS:** publish nothing, log at info, return 200.
- **VDMS registry read fails:** log error, return 200 (next fire / restart reconciles); no
  partial state is persisted beyond any rows already recorded.

## Testing

- **`TriggerDispatcher`**
  - `GLOBAL` → exactly one trigger published with `vdmsId = null`.
  - `CENTRALISED` → one trigger per **active** VDMS; inactive VDMS skipped; each trigger has a
    distinct `runId`; `recordFired` called once per VDMS with the right `vdmsId`.
  - `CENTRALISED` with empty active set → no publish, no run rows.
  - One publish failing does not prevent the remaining VDMS from being triggered.
- **`JobService.registerAll`** → registers `GLOBAL` + `CENTRALISED`; does **not** register
  `PER_VDMS` jobs as bare Dapr jobs.
- **`JobCallbackController`** → bare centralised name delegates to the dispatcher; partial
  failure still returns 200; `{jobName}::{vdmsId}` PER_VDMS path still records a single fired run.
- **`JobService.runNow`** on a centralised job fans out to all active VDMS.
- **Orphan teardown** → a job that flipped away from `PER_VDMS` with existing `job_instance` rows
  has its `{jobName}::{vdmsId}` Dapr jobs deleted and those instances marked `DISABLED`;
  re-running teardown is a no-op.

## Rollout / compatibility

- Pure additive enum value + behavior gated on scope. Existing `GLOBAL` and `PER_VDMS` jobs are
  unaffected.
- On deploy, startup reconcile updates `vdmsSystemHealth.scope` to `CENTRALISED`; `registerAll`
  registers the single `vdmsSystemHealth` Dapr job and (with the fix) stops registering the stray
  bare job for any remaining `PER_VDMS` jobs.
- The orphan teardown (change #6) deletes the existing `vdmsSystemHealth::{vdmsId}` Dapr jobs and
  marks their `job_instance` rows `DISABLED` on the next reconcile, so the old per-VDMS jobs do
  **not** fire alongside the new centralised job. Teardown is idempotent and self-healing across
  restarts.
