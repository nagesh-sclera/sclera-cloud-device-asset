# Scheduler dashboard — VDMS-aware UI — design

- **Date:** 2026-06-09
- **Component:** `sclera-scheduler` monitoring UI (`src/main/resources/static/scheduler.html`) + a small read-side addition to `web/SchedulerApiController` + `web/dto/JobView` + `domain/JobInstanceRepository`.
- **Status:** Approved design — ready for implementation plan
- **Builds on:** `docs/superpowers/specs/2026-06-09-vdms-aware-scheduler-design.md` (the VDMS-aware scheduler backend, already implemented).

## Problem

The scheduler dashboard is entirely job-name-centric (the old GLOBAL model). It does not surface anything the VDMS-aware backend now provides:

- No notion of **`scope`** — a `PER_VDMS` job is indistinguishable from a global one.
- No **per-VDMS instances** — `GET /api/jobs/{name}/instances` (state, `snooze_until`, `next_fire_at` per `vdmsId`) is unused.
- No **per-instance actions** — `…/instances/{vdmsId}/{pause|resume|disable|run|snooze|run-at}`.
- No **SNOOZED** state styling, no snooze/run-at time controls.
- The live feed and run-dot tooltips ignore the new `vdms_id` on runs.

## Goal

Surface the per-VDMS dimension and the delayed-job controls in the existing dashboard, without regressing the global-job view and without introducing a build toolchain (the machine has no Node).

## Decisions (locked during brainstorming)

| Decision | Choice |
|---|---|
| Instance layout | **Inline drill-down** — instances are a third accordion level under owner→job, lazy-loaded on expand |
| Delay time entry | **Presets + custom** — quick presets plus a `datetime-local` field in a popover |
| Large instance lists | **Search + state-filter chips** inside the expanded section; all instances lazy-load on expand |
| Instance row detail | **Status + timing** — no per-instance timeline track |
| `vdms_id` on runs | **Tooltips + feed labels** — no global VDMS filter |

## Non-goals (YAGNI)

- No global VDMS filter across the dashboard.
- No per-instance timeline track.
- No bulk actions (e.g. "snooze all instances").
- No framework/bundler/Node — stays a vanilla single-file `scheduler.html`.
- No backend write-path changes — only `JobView` read additions; instance actions use endpoints that already exist.

---

## Architecture

```
scheduler.html (vanilla JS, single file)
  ├─ poll /api/jobs           → JobView[] (now incl. scope, instanceCount, attentionCount)
  ├─ poll /api/runs?limit=…   → RunView[] (now incl. vdmsId)
  └─ on expand of a PER_VDMS job:
        GET /api/jobs/{name}/instances → JobInstanceView[]  (lazy, cached, refreshed while open)
        actions → POST /api/jobs/{name}/instances/{vdmsId}/{pause|resume|disable|run}
                  POST …/snooze?until=<ISO>   POST …/run-at?at=<ISO>
```

Backend list endpoint stays O(1) queries: one extra grouped instance-count query feeds the per-row summary; the full instance list is fetched only when a job is expanded.

---

## Backend changes (read-side only)

### `JobView` (record) — add three fields
- `scope` (String: `GLOBAL` | `PER_VDMS`)
- `instanceCount` (int) — 0 for GLOBAL jobs
- `attentionCount` (int) — number of this job's instances **not** in `ENABLED` state (paused + snoozed + disabled); 0 for GLOBAL jobs

Existing `JobView` fields are unchanged.

### `JobInstanceRepository` — one aggregate query
A grouped count to avoid N+1 on the list endpoint:

```java
@Query("select i.jobName as jobName, i.state as state, count(i) as cnt "
     + "from JobInstanceEntity i group by i.jobName, i.state")
List<JobInstanceStateCount> countByJobNameAndState();
```

`JobInstanceStateCount` is a Spring Data projection interface (`getJobName()`, `getState()`, `getCnt()`).

### `SchedulerApiController.list()`
- Call `countByJobNameAndState()` **once**, fold into a `Map<String, int[]>` (or `Map<String,{total,attention}>`) keyed by job name.
- In `toView(job)`: set `scope` from `job.getScope().name()`; for PER_VDMS jobs set `instanceCount`/`attentionCount` from the map (default 0,0 when absent); for GLOBAL jobs both are 0.

No change to `GET /api/jobs/{name}/instances` — it already returns `JobInstanceView(jobName, vdmsId, state, snoozeUntil, nextFireAt)`.

---

## UI behavior (`scheduler.html`)

### Row model
- **GLOBAL job** — renders exactly as today: name, schedule, timeline track with run dots, last status, hover actions (Run / Pause / Resume / Disable on `/api/jobs/{name}/{action}`). No regression.
- **PER_VDMS job** — shows a `PER-VDMS` scope badge, a summary `"{instanceCount} inst · {attentionCount} need attention"` (attention figure tooltipped/expanded into the per-state breakdown after expand), and a chevron. **No own timeline track** (its runs belong to instances). Clicking the row toggles its instance sub-section.

### Expanded instance section (lazy-loaded, third level)
- On first expand: `GET /api/jobs/{name}/instances`; show a one-line "loading instances…" until it returns; cache the result on the job.
- While a job is expanded, re-fetch its instances on the existing 5 s poll tick so state transitions (snooze→enabled, etc.) stay live. Collapsed jobs do not fetch.
- Section header: a **vdms-id search box** + **state filter chips** (All / Enabled / Snoozed / Paused / Disabled), both applied client-side over the loaded instances.
- **Instance row:** `vdms-id` · state badge · `next HH:MM` (from `nextFireAt`) · `until HH:MM` (only when SNOOZED, from `snoozeUntil`) · last-run status (derived from the `/api/runs` cache filtered by `jobName` + `vdmsId`). Hover reveals **state-contextual actions**:
  - `ENABLED` → snooze · run-at · pause · disable
  - `PAUSED` → resume · run-at · disable
  - `SNOOZED` → resume (cancels the snooze early) · run-at
  - `DISABLED` → resume
  - Plain actions POST to `/api/jobs/{name}/instances/{vdmsId}/{action}`.

### Delay popover (snooze & run-at)
- Clicking `snooze` or `run-at` opens a small popover anchored to the row containing: quick presets, a `datetime-local` field for an exact time, and Confirm/Cancel.
  - Snooze presets: **+1h**, **+4h**, **until tomorrow 09:00**.
  - Run-at presets: **+1h**, **tonight 20:00**.
- Confirm sends the chosen instant as ISO-8601: `POST …/snooze?until=<ISO>` or `POST …/run-at?at=<ISO>`, then refreshes that job's instances.
- Past-time selections are disabled client-side (backend also rejects them with 4xx).

### New visuals
- Add a `SNOOZED` state badge style (amber) showing the `until` time, alongside the existing `PAUSED`/`DISABLED` badge styles; extend the legend accordingly.
- Run-dot tooltips and live-feed rows append `· {vdmsId}` when the run has a `vdmsId`. No global VDMS filter.

### Code organization
Single `scheduler.html` (matches current; no bundler/Node). New logic lives in clearly separated functions — `renderInstances()`, `openDelayPopover()`, and an instance-action handler — kept distinct from the existing job/feed render path. Reuse existing helpers (`esc`, `hhmm`, `ago`, `cls`).

---

## Error handling & edge cases
- **Instance action fails** (network/4xx): re-enable the button and show a brief inline error on the row; next poll reconciles true state.
- **Instance fetch fails** on expand: show a "couldn't load instances — retry" line with a retry affordance; don't collapse.
- **Empty instance list** (PER_VDMS job with no active VDMS yet): show "no instances yet".
- **Past-time delay**: blocked in the popover; if it still 4xx's, surface the inline error.
- **Large lists**: search + filter are client-side over the already-loaded set; no pagination in v1 (lazy-load fetches the full list once per expand). If list sizes prove very large in practice, server-side paging is a future follow-up (noted, not built).
- **State-contextual actions**: the action set is derived from the instance's current `state`, so an already-snoozed instance never shows "snooze", etc.

---

## Testing
- **Backend (automated)** — in the existing scheduler Testcontainers/Mockito suite:
  - Repo test: `countByJobNameAndState()` returns correct per-(job,state) counts.
  - Controller test: `list()` populates `scope` for both kinds; `instanceCount`/`attentionCount` correct for a PER_VDMS job with mixed states; both 0 for a GLOBAL job.
  - `JobView` carries the three new fields.
- **UI (manual)** — no JS test harness and no Node on this machine, so the dashboard is verified by a manual checklist (stated honestly; not automated):
  - PER_VDMS job shows scope badge + summary; expands and lazy-loads instances.
  - Snooze via a preset and via the custom picker; `until` shows on the row; SNOOZED badge styled.
  - Run-at via preset and custom; resume cancels a snooze early.
  - State filter chips + vdms search narrow the list.
  - GLOBAL jobs render and behave exactly as before.
  - `vdms_id` appears in a run-dot tooltip and a feed row.

## Rollout order
1. Backend: `JobInstanceStateCount` projection + repository query; `JobView` fields; `SchedulerApiController.list()` mapping + tests.
2. UI: scope badge + summary on rows; drill-down expand with lazy instance fetch; instance rows + state-contextual actions; SNOOZED styling; search + filter chips.
3. UI: delay popover (presets + custom) wired to snooze/run-at.
4. UI: `vdms_id` in tooltips + feed.
5. Manual UI verification pass.
