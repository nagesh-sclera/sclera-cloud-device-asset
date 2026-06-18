# Scheduler VDMS Management UI — design

- **Date:** 2026-06-15
- **Service:** `sclera-scheduler` (backend endpoints + dashboard UI only)
- **Status:** Approved design — ready for implementation plan
- **Builds on:** [[2026-06-09-vdms-aware-scheduler-design]] (the per-VDMS backend) and
  [[2026-06-09-scheduler-ui-vdms-design]] (the read/control dashboard). Both are already implemented
  on `feature/scheduler-service`; this is the **write/admin layer** on top of them.

## Problem

The VDMS-aware scheduler already schedules jobs per VDMS, fires them in each VDMS's timezone
(`CRON_TZ`), and surfaces per-VDMS instances in the dashboard. But the scheduler's `vdms_registry`
is **read-only from the operator's perspective** — it is populated only by `VdmsLifecycleEvent`
pub/sub + a startup-sync backstop. An operator cannot, from the UI:

1. **Add a VDMS** to the scheduler so its per-VDMS jobs start being scheduled.
2. **Edit a VDMS's timezone** (which must re-register that VDMS's jobs with the new `CRON_TZ`).
3. **Remove a VDMS** (tear down its scheduled jobs).

This spec adds those three write operations as HTTP endpoints + a "VDMS" panel in `scheduler.html`.

## Decisions (locked during brainstorming, 2026-06-15)

| Decision | Choice |
|---|---|
| System of record for VDMS identity + timezone | **The scheduler** — UI writes land directly in `vdms_registry`; no cross-service write path |
| How writes take effect | **Reuse `PerVdmsRegistrar`** — the same idempotent fan-out/teardown the lifecycle subscriber already uses |
| Operations in scope | **Add VDMS, Edit timezone, Remove VDMS** |
| Timezone input | **Validated IANA dropdown** (server-validated via `ZoneId.of`), not free text |
| Unverified VDMS id | **Allowed with a soft UI warning** — the scheduler owns identity; it does not verify the id against the fleet |
| Remove semantics | **Deactivate (soft)** — reuse `onVdmsDeactivated`: tear down Dapr jobs, DISABLE instances, set `active=false` (registry row retained for audit) |

## Non-goals (YAGNI)

- **No cross-service VDMS mutation.** The UI does not call device-asset / vdms-service to create or
  rename a real fleet VDMS. The scheduler's registry is authoritative for *scheduling*; fleet truth
  still arrives via lifecycle events (see Coexistence).
- **No per-VDMS schedule override** — every VDMS uses the catalog cron; only the timezone differs
  (unchanged from the per-VDMS backend's non-goals).
- **No VDMS rename** — `vdms_id` is the primary key; a wrong id is removed + re-added.
- **No new auth model** — the endpoints inherit the dashboard's existing exposure (dev/docker).
- **No job-creation UI** — that is the separate sub-project B (`scheduler-ui-job-creation`).

## Architecture / data flow

```
operator → scheduler.html (VDMS panel)
   │  POST /api/vdms {vdmsId, timezone}
   │  PUT  /api/vdms/{vdmsId}/timezone {timezone}
   │  DELETE /api/vdms/{vdmsId}
   ▼
VdmsAdminController  (validate timezone via ZoneId.of; 400/404/409)
   │  add / edit → registrar.onVdmsActivated(vdmsId, timezone)   ── upsert vdms_registry + (re)register PER_VDMS jobs
   │  remove     → registrar.onVdmsDeactivated(vdmsId)           ── delete Dapr jobs + DISABLE instances + active=false
   ▼
PerVdmsRegistrar (existing, idempotent)
   │  SchedulerClient.schedule("{jobName}::{vdmsId}", CRON_TZ={tz}+cron)  /  delete(...)
   ▼
Dapr Scheduler control plane
```

The same `PerVdmsRegistrar` entry points the existing `VdmsLifecycleSubscriber` calls from pub/sub
are now also reachable via HTTP. No second scheduling path is introduced.

## Backend — `VdmsAdminController` (new)

New controller in `io.sclera.scheduler.web`, mirroring `SchedulerApiController`'s style. Depends on
`VdmsRegistryRepository`, `JobInstanceRepository`, and `PerVdmsRegistrar`.

| Op | Endpoint | Behavior |
|---|---|---|
| list | `GET /api/vdms` | `vdms_registry` rows → `VdmsRegistryView(vdmsId, timezone, active, jobCount)`. `jobCount` = number of that VDMS's `job_instance` rows (add `JobInstanceRepository.countByVdmsId`). |
| add | `POST /api/vdms` | Body `{vdmsId, timezone}`. Validate (below). **409** if an *active* row already exists. Else `registrar.onVdmsActivated(vdmsId, timezone)` → **201** + view. |
| edit tz | `PUT /api/vdms/{vdmsId}/timezone` | Body `{timezone}`. **404** if no registry row. Validate, then re-register with the new zone (below) → **200** + view. |
| remove | `DELETE /api/vdms/{vdmsId}` | **404** if no row. `registrar.onVdmsDeactivated(vdmsId)` → **204**. |
| zones | `GET /api/timezones` | Sorted `ZoneId.getAvailableZoneIds()` for the dropdown (served once, cached client-side). |

**Validation** (one helper): `timezone` non-blank and accepted by `ZoneId.of(tz)` (catch
`DateTimeException`/`ZoneRulesException` → **400** `{error}`); `vdmsId` non-blank/trimmed → **400**.
`@every` jobs ignore timezone — unaffected.

**Edit-timezone re-registration:** update `vdms_registry.timezone`, then re-register that VDMS's
PER_VDMS jobs so the Dapr jobs carry the new `CRON_TZ`. Implementation preference: reuse
`onVdmsActivated(vdmsId, newTz)` (it upserts the timezone and re-registers; `SchedulerClient.schedule`
is idempotent-replace). **Verify during implementation** that `onVdmsActivated` re-registers cleanly
for an already-active VDMS without resetting per-instance runtime state (PAUSED/SNOOZED). If it does
reset state, add a thin `PerVdmsRegistrar.reregister(vdmsId)` that only replaces the Dapr schedules.

## Frontend — VDMS panel in `static/scheduler.html`

A new collapsible "VDMS" section in the existing single-file dashboard (vanilla JS, matching the
current style; new logic in dedicated functions — `loadVdms`, `renderVdmsTable`, `submitAddVdms`,
`submitEditTz`, `removeVdms` — kept out of the existing `render()`/feed path):

- **Table:** one row per registry VDMS — `vdmsId`, timezone, active badge, job count, and per-row
  **Edit timezone** (inline dropdown → `PUT`) and **Remove** (confirm → `DELETE`).
- **Add form:** `vdmsId` text input + timezone dropdown (populated from `GET /api/timezones`) +
  Add button (`POST`). On the id field, a persistent soft hint: *"Not verified against the fleet —
  must match the VDMS id the owning services use, or jobs will fire with no effect."*
- **Feedback:** inline success/error from the endpoints' status + `{error}` body; refresh the table
  and the existing job list (instance counts change) after each write.

## Coexistence with lifecycle events

UI writes and `VdmsLifecycleEvent` both funnel through `PerVdmsRegistrar` (idempotent upsert /
teardown), so they compose safely:

- A UI-added VDMS later receiving a real `ACTIVATED` event re-upserts (no-op-ish; may refresh tz).
- A real `DEACTIVATED` event tears down even a UI-added VDMS — **intended**: lifecycle events reflect
  fleet reality and win.
- A UI timezone edit followed by an `ACTIVATED` event carrying a different timezone is overwritten by
  the event (fleet truth). Documented; acceptable for v1.

## Error handling

- Invalid/blank timezone or id → **400** with `{error}` (no partial write — validate before calling the registrar).
- Add on an already-active id → **409**; edit/remove on a missing id → **404**.
- Registrar/Dapr failure mid-operation surfaces as **500**; the operation is idempotent, so a retry
  (or the next startup `reconcileAll`) converges. The `scheduler.max-instances` guardrail still caps fan-out.

## Testing

- **`VdmsAdminControllerTest`** (MockMvc + Testcontainers Postgres, like `SchedulerApiControllerTest`):
  add (201 + registry row + instances registered), add-duplicate (409), invalid timezone (400),
  blank id (400), edit-timezone (200 + row updated + re-registration invoked), edit-missing (404),
  remove (204 + `active=false` + instances DISABLED), remove-missing (404), `GET /api/vdms` shape,
  `GET /api/timezones` non-empty.
- **Registrar reuse:** assert the controller delegates to `PerVdmsRegistrar` (verify interaction) so
  the per-VDMS fan-out itself stays covered by its existing tests.
- **Frontend:** manual verification (Task in the plan) at `http://localhost:8098/scheduler.html` — no
  JS test harness on this machine. Add a VDMS, confirm its per-VDMS instances appear; edit its tz,
  confirm jobs re-register (`CRON_TZ` in the Dapr job / next-fire shifts); remove, confirm teardown.

## File touch-list (decomposition)

**Backend (`sclera-scheduler`):**
- Create `web/VdmsAdminController.java`, `web/dto/VdmsRegistryView.java`.
- Modify `domain/JobInstanceRepository.java` — add `countByVdmsId`.
- Possibly add `service/PerVdmsRegistrar.reregister(vdmsId)` (only if `onVdmsActivated` resets state).
- Test `test/.../web/VdmsAdminControllerTest.java`.

**Frontend:**
- Modify `src/main/resources/static/scheduler.html` — the VDMS panel + its JS functions.
