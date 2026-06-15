# VDMS Management UI — demo runbook

Demonstrates the new scheduler VDMS-management feature: add a VDMS, edit its timezone,
and remove it from the dashboard — with per-VDMS jobs that fire in each VDMS's local timezone.

## What the demo shows
`jobs.yaml`'s `vdmsSystemHealth` is `scope: PER_VDMS`. When you register a VDMS, the scheduler
fans that job out into a per-VDMS instance `vdmsSystemHealth::<vdmsId>` and registers it with the
Dapr Scheduler control plane using `CRON_TZ=<that VDMS's timezone>`. So one catalog job becomes
N independently-controllable, timezone-correct scheduled units — one per site.

## Prerequisites
- Docker Desktop running.
- Run the scripts from **git-bash / Bash**, not PowerShell (PS 5.1 mangles JSON quotes for curl).

## 1. Build + start the scheduler stack
From the repo root:
```bash
docker compose build sclera-scheduler
docker compose up -d postgres redis dapr-scheduler sclera-scheduler sclera-scheduler-dapr
```
Wait until healthy / the dashboard responds:
```bash
curl -s -o /dev/null -w "scheduler=%{http_code}\n" http://localhost:8098/scheduler.html   # expect 200
```

## 2. Seed the demo VDMS
```bash
cd sclera-scheduler/demo
./seed-demo-vdms.sh
```
Registers `demo-nyc` (America/New_York), `demo-london` (Europe/London), `demo-mumbai` (Asia/Kolkata)
and prints the registry.

## 3. Show it on the dashboard
Open **http://localhost:8098/scheduler.html**:
- The **VDMS** panel (top of the main column) lists the 3 demo sites with their timezones and job counts.
- Expand the **`vdmsSystemHealth`** job in the list below — it now has one instance per demo VDMS
  (`vdmsSystemHealth::demo-nyc`, `…::demo-london`, `…::demo-mumbai`), each with a next-fire time
  reflecting its own timezone.

## 4. Live edits (the point of the feature)
- **Edit timezone:** in a VDMS row, change the dropdown (e.g. `demo-nyc` → `Asia/Tokyo`) → **Save tz**.
  The instance re-registers with the new `CRON_TZ`; its next-fire shifts. Paused/snoozed instances
  (if any) keep their state — only enabled ones re-arm.
- **Add ad-hoc:** type a new id + pick a timezone → **Add VDMS**. A new per-VDMS instance appears.
- **Remove:** click **Remove** on a row → confirm. The row flips to *removed* and its instances are torn
  down (soft delete — the row stays for audit; re-adding the same id reactivates it).

## 5. Reset
```bash
./teardown-demo-vdms.sh     # soft-removes the 3 demo VDMS
```
To wipe completely (fresh registry), recreate the scheduler DB:
```bash
docker compose stop sclera-scheduler sclera-scheduler-dapr
# (optionally drop the sclera_scheduler schema / down -v if a full reset is wanted)
docker compose up -d sclera-scheduler sclera-scheduler-dapr
```

## Notes
- VDMS identity here is scheduler-owned (per the design): the id you enter is **not** verified against
  the real fleet — it must match the id the owning services use, or the fired triggers have no effect.
  The panel shows a soft hint to this effect.
- All three operations are also covered by the backend integration tests
  (`VdmsAdminControllerTest`, real PostgreSQL via Testcontainers).
