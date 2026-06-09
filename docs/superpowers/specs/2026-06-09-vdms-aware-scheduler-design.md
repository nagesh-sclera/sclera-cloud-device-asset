# VDMS-aware scheduler — design

- **Date:** 2026-06-09
- **Service:** `sclera-scheduler` (with a contract change in `dapr-commons` and an integration requirement on the VDMS-owning service)
- **Status:** Approved design — ready for implementation plan

## Problem

The scheduler registers one Dapr job per job *name* (global). Three capabilities are missing:

1. **VDMS-aware scheduling** — jobs that are conceptually per-site (e.g. `vdmsSystemHealth`) fire once globally and fan out inside the handler. We want the scheduler itself to schedule a job *per VDMS*, so each site is an independently controllable, timezone-correct scheduled unit.
2. **Add/remove a VDMS's schedule** — when a VDMS is activated, its per-VDMS jobs should be registered automatically; when deactivated, torn down. No restart, no manual step.
3. **Delayed jobs** — alongside the existing pause/resume, support time-based **snooze** (pause now, auto-resume at time T) and **one-shot delayed run** (run once at a future time T), at the granularity of a single VDMS's job.

## Decisions (locked during brainstorming)

| Decision | Choice |
|---|---|
| Per-VDMS model | **Per-VDMS jobs** — one Dapr job per (job × VDMS), named `{jobName}::{vdmsId}` |
| Which jobs are VDMS-aware | **Configurable per job** — each `jobs.yaml` entry declares `scope: GLOBAL \| PER_VDMS` |
| Delayed jobs | **Both** snooze (auto-resume at T) **and** one-shot delayed run |
| VDMS discovery | **Pub/sub event** (`VdmsLifecycleEvent`) for live add/remove, **plus startup-sync** backstop |
| Timezone | **Honor VDMS timezone** via Dapr `CRON_TZ`, fall back to UTC |
| Scale | **Unknown / varies** — design defensively: idempotent, batched, guardrailed |
| Delay granularity | **Per (job × VDMS)** |

## Non-goals (YAGNI)

- No lazy/grouped scheduling in v1. The `job_instance` table makes adding it later non-breaking.
- No direct Dapr service-invocation add/remove path — pub/sub event only (weighed and rejected: broker buffering through scheduler downtime is the deciding advantage; a synchronous call gives the caller confirmation but no buffering).
- No per-instance *schedule override* (every VDMS uses the catalog cron, only the timezone differs). Can be added to `job_instance` later.
- The scheduler does **not** read the `vdms` table directly (DB-per-service rule). It keeps its own `vdms_registry`.

---

## Execution engine

Every actual scheduled fire goes through **the same Dapr Scheduler control plane** the existing scheduler already uses — no second scheduling engine is introduced. `SchedulerClient` registers jobs via the Dapr Jobs API (`POST /v1.0-alpha1/jobs/{name}`); the control plane persists them (etcd) and calls back `POST /job/{name}` on fire. All additions stay inside that API: per-VDMS jobs are ordinary named jobs (`{jobName}::{vdmsId}`), timezone uses the native `CRON_TZ=` cron prefix, and one-shot runs use native `dueTime`+`repeats=1`. The **only** non-Dapr timer is the snooze re-arm reconciler — an in-process Spring `@Scheduled` (like `HousekeepingService`) that holds snooze state in the DB and, on expiry, *re-registers the real job through the Dapr control plane*. It never fires jobs itself.

## Architecture

```
jobs.yaml (catalog: name, schedule, owner, scope)
   │ reconcile (startup)
   ▼
 job ───────────────────────────────┐  GLOBAL  → 1 Dapr job per name (unchanged path)
 (template: schedule, owner, scope)  │
   │ PER_VDMS                        │
   ▼                                 │
 PerVdmsRegistrar  ◄── vdms_registry (vdms_id, timezone, active)
   │   for each PER_VDMS job × each active VDMS
   ▼
 job_instance (job_name, vdms_id, state, snooze_until, dapr_job_name, …)
   │ SchedulerClient.schedule("{jobName}::{vdmsId}", CRON_TZ={tz} + cron)
   ▼
 Dapr Scheduler control plane
   │ fires POST /job/{name}
   ▼
 JobCallbackController  → parse {jobName}::{vdmsId} → record run (vdms_id)
   │ publish SchedulerTriggerEvent{ jobName, runId, vdmsId, firedAt }
   ▼
 owning service TriggerDispatchSubscriber → per-VDMS handler(vdmsId)
```

The net mental model: **`job` = "what & how often", `vdms_registry` = "for whom", `job_instance` = "the live, individually-controllable scheduled unit."**

---

## Data model

### `job` (existing — one new column)

| column | type | note |
|---|---|---|
| `name` | TEXT PK | unchanged |
| `schedule` | TEXT | unchanged (cron or `@every`) |
| `owner` | TEXT | unchanged |
| `trigger_topic` | TEXT | unchanged |
| `state` | TEXT | meaningful only for `GLOBAL` jobs |
| `scope` | TEXT NOT NULL DEFAULT `'GLOBAL'` | **new** — `GLOBAL` \| `PER_VDMS`, from `jobs.yaml` |
| `last_run_id`, `next_fire_at`, timestamps | | unchanged |

`scope` is catalog-owned: `JobCatalogReconciler` writes it from `jobs.yaml` (like `schedule`/`owner`). Runtime fields stay untouched on reconcile.

### `vdms_registry` (new — scheduler's own copy of the VDMS list)

| column | type | note |
|---|---|---|
| `vdms_id` | TEXT PK | |
| `timezone` | TEXT | IANA zone (e.g. `America/New_York`); nullable → UTC fallback |
| `active` | BOOLEAN NOT NULL DEFAULT TRUE | |
| `updated_at` | TIMESTAMPTZ NOT NULL DEFAULT now() | |

Populated by **startup-sync** (`GET /vdms/active` on vdms-service) and kept live by the **`VdmsLifecycleSubscriber`**. Never cross-joins to the `vdms` table.

### `job_instance` (new — the per-VDMS runtime unit)

| column | type | note |
|---|---|---|
| `job_name` | TEXT | FK → `job(name)` |
| `vdms_id` | TEXT | FK → `vdms_registry(vdms_id)` |
| `state` | TEXT NOT NULL DEFAULT `'ENABLED'` | `ENABLED` \| `PAUSED` \| `SNOOZED` \| `DISABLED` |
| `snooze_until` | TIMESTAMPTZ | null unless `SNOOZED` |
| `dapr_job_name` | TEXT NOT NULL | `{jobName}::{vdmsId}` |
| `next_fire_at` | TIMESTAMPTZ | |
| `last_run_id` | UUID | |
| `created_at`, `updated_at` | TIMESTAMPTZ | |

Primary key `(job_name, vdms_id)`. Rows exist only for `PER_VDMS` jobs. `GLOBAL` jobs are untouched and keep using `job.state` + a single Dapr registration.

Index: `idx_job_instance_snooze ON job_instance (state, snooze_until)` for the re-arm reconciler scan.

### `job_run` (existing — one new column)

Add nullable `vdms_id TEXT` so run history is attributable per VDMS. `GLOBAL` job runs leave it null.

---

## Components

### 1. Catalog: `scope` (requirement: configurable per job)

- `jobs.yaml` entries gain an optional `scope` (default `GLOBAL`). Example:
  ```yaml
  - name: vdmsSystemHealth
    schedule: "0 0 0 * * *"
    owner: device-asset
    scope: PER_VDMS
  ```
- `JobCatalogProperties.Entry` and `JobCatalogReconciler` carry `scope` through to `job.scope`.

### 2. VDMS discovery (requirement 2: add/remove)

**`VdmsLifecycleEvent`** (new, in `dapr-commons/events`):
```java
public record VdmsLifecycleEvent(String vdmsId, String timezone, String status) {} // status: ACTIVATED | DEACTIVATED
```

**Integration requirement on the VDMS-owner** (the activation flow — *not present in this extracted codebase*; documented as an upstream contract): publish `VdmsLifecycleEvent` to topic `vdms.lifecycle` when a VDMS becomes active (`ACTIVATED`) and when deactivated/deleted (`DEACTIVATED`). Until that emit is wired, **startup-sync** covers correctness.

**`VdmsLifecycleSubscriber`** (new, in scheduler — modeled on `VdmsCustomerOrgSubscriber`):
- `ACTIVATED` → upsert `vdms_registry(active=true, timezone)`, then `PerVdmsRegistrar.onVdmsActivated(vdmsId)`.
- `DEACTIVATED` → `PerVdmsRegistrar.onVdmsDeactivated(vdmsId)` (delete that VDMS's Dapr jobs, mark its `job_instance` rows `DISABLED`, set `vdms_registry.active=false`).

**`GET /vdms/active`** (new endpoint on **vdms-service**): returns `[{ vdmsId, timezone }]` for all active VDMS. Used by startup-sync only.

### 3. `PerVdmsRegistrar` (requirement 1: VDMS-aware scheduling)

Owns the fan-out and reconciliation. Idempotent throughout.

- `reconcileAll()` — for each `PER_VDMS` job × each active VDMS in `vdms_registry`: ensure a `job_instance` row and register `{jobName}::{vdmsId}` with Dapr. Run at startup (extends `CatalogStartupRunner`) and is the self-heal path on a fresh DB / dropped event.
- `onVdmsActivated(vdmsId)` — register every `PER_VDMS` job for one VDMS.
- `onVdmsDeactivated(vdmsId)` — tear down one VDMS's jobs.
- A new `PER_VDMS` catalog entry fans out to all active VDMS on next `reconcileAll()`; removing one tears its instances down.

**Timezone:** registration prefixes the catalog cron with `CRON_TZ={vdms.timezone}` (UTC fallback). `@every` schedules pass through unchanged (timezone is meaningless for intervals).

**Scale guardrail:** registration is batched; a configurable `scheduler.max-instances` ceiling logs + skips beyond the limit so a surprise VDMS count cannot flood the Dapr control plane.

### 4. `SchedulerClient` (timezone + one-shot support)

- `JobSchedule` extended to carry an optional timezone; the `CRON_TZ=` prefix is built in one place.
- New one-shot registration: `dueTime={ISO-8601}` + `repeats=1` (Dapr Jobs API native one-shot). Dapr auto-removes after it fires.
- `delete(name)` already tolerates 404 — reused for teardown.

### 5. Trigger event & consumers (the ripple)

- **`SchedulerTriggerEvent`** gains a **nullable `vdmsId`**. Global jobs send `null` — backward compatible.
- **`JobCallbackController`** parses the fired Dapr job name: `{jobName}::{vdmsId}` → records run with `vdms_id`, publishes event with `vdmsId`. Plain `{jobName}` → `vdmsId=null`.
- **`TriggerDispatchSubscriber`** (each owning service) receives `vdmsId` and passes it to the per-VDMS handler. v1 in device-asset: thread `vdmsId` into the handler signature; handlers currently stubbed remain stubs (no behavior regression).

### 6. Delayed jobs (requirement 3)

Both act on a single `job_instance`, alongside `pause`/`resume`.

**Snooze** — `POST /api/jobs/{name}/instances/{vdmsId}/snooze?until={ISO-8601}`:
- `state=SNOOZED`, `snooze_until=T`, delete the Dapr job (won't fire while snoozed).
- **Re-arm reconciler** — an in-process Spring `@Scheduled(fixedDelay)` (~60s), **not** a Dapr job, alongside `HousekeepingService`'s existing service-local timers (it fires on every replica; the re-arm is idempotent so that is safe). Scans `job_instance WHERE state=SNOOZED AND snooze_until <= now()`, re-registers (timezone-aware), flips to `ENABLED`. DB is the single source of truth → crash-safe across restarts.
- `resume` before T cancels the snooze early (clear `snooze_until`, re-arm now).
- *Rejected alternative:* a Dapr one-shot job to fire the re-arm — spreads snooze state across Dapr + DB and complicates callback routing. The polling reconciler keeps one source of truth.

**One-shot delayed run** — `POST /api/jobs/{name}/instances/{vdmsId}/run-at?at={ISO-8601}`:
- Register a Dapr job with `dueTime=T`, `repeats=1`, named `{jobName}::{vdmsId}::once-{runId}` (never collides with the recurring instance).
- On fire: publish the trigger event (with `vdmsId`), record a run flagged one-shot, Dapr auto-removes. Does **not** touch the recurring schedule.

### 7. API surface (consolidated)

Existing global ops unchanged (for `GLOBAL` jobs). Per-instance ops mirror them at `(name, vdmsId)`:

| op | endpoint |
|---|---|
| pause | `POST /api/jobs/{name}/instances/{vdmsId}/pause` |
| resume | `POST /api/jobs/{name}/instances/{vdmsId}/resume` |
| disable | `POST /api/jobs/{name}/instances/{vdmsId}/disable` |
| run now | `POST /api/jobs/{name}/instances/{vdmsId}/run` |
| snooze | `POST /api/jobs/{name}/instances/{vdmsId}/snooze?until=…` |
| run-at | `POST /api/jobs/{name}/instances/{vdmsId}/run-at?at=…` |
| list instances | `GET /api/jobs/{name}/instances` |

`JobService`'s existing ops are extended to operate on a `job_instance` key; the global-name ops remain for `GLOBAL` jobs.

---

## Error handling & edge cases

- **Idempotency:** registering `{jobName}::{vdmsId}` twice is a no-op (Dapr replaces by name; `job_instance` upsert keyed on the pair). Overlap between startup-sync and a live event cannot duplicate.
- **Dropped lifecycle event / scheduler down at activation:** startup-sync reconciles on next boot. The event is the fast path; sync is the backstop.
- **Deactivate then reactivate:** `DEACTIVATED` marks instances `DISABLED`; `ACTIVATED` re-registers and flips back to `ENABLED`.
- **Snooze boundary:** reconciler scans `<= now()`; tests cover just-before / just-after `snooze_until`.
- **Per-registration isolation:** one VDMS's Dapr failure must not abort the batch (mirror `registerAll()`'s try/per-item logging).
- **Missing timezone:** UTC fallback, logged once.
- **One-shot in the past:** reject with 400 (`at` must be in the future).

---

## Testing

Follows the module's existing pure-Mockito + `AbstractPostgresTest` split.

- **Unit:** `PerVdmsRegistrar` fan-out & teardown; `CRON_TZ` prefixing; snooze re-arm boundary; one-shot naming; idempotent re-registration; `max-instances` guardrail; trigger-name parse (`{jobName}::{vdmsId}` vs plain).
- **Component/repo:** `job_instance` persistence & state transitions; `job_run.vdms_id` attribution; `VdmsLifecycleSubscriber` upsert + register/teardown; startup-sync reconcile; `scope` reconciliation from catalog.
- **Client:** `SchedulerClient` CRON_TZ and `dueTime`/`repeats` against a mock RestClient (as existing client tests do).

---

## Migration

Single Flyway migration `V2__vdms_aware_scheduler.sql`:
- `ALTER TABLE job ADD COLUMN scope TEXT NOT NULL DEFAULT 'GLOBAL';`
- `ALTER TABLE job_run ADD COLUMN vdms_id TEXT;`
- `CREATE TABLE vdms_registry (...);`
- `CREATE TABLE job_instance (...);` + `idx_job_instance_snooze`.

Backward compatible: existing global jobs default to `scope=GLOBAL` and behave exactly as before.

## Rollout order

1. Migration + `dapr-commons` `SchedulerTriggerEvent.vdmsId` + `VdmsLifecycleEvent`.
2. Scheduler: `scope` catalog, `vdms_registry`, `job_instance`, `SchedulerClient` (CRON_TZ + one-shot), `JobCallbackController` parse.
3. Scheduler: `PerVdmsRegistrar`, startup-sync, `VdmsLifecycleSubscriber`, re-arm reconciler, per-instance API.
4. vdms-service: `GET /vdms/active`.
5. Consumers: `TriggerDispatchSubscriber` threads `vdmsId`.
6. Integration requirement (upstream owner): emit `VdmsLifecycleEvent` from the activation flow.
