# CLAUDE.md — sclera-scheduler

## Project identity
Central scheduling service. Spring Boot 4.0.6, Java 21. Registers cron jobs with the
Dapr Scheduler control plane (Jobs API) and fires them as `scheduler.trigger` pub/sub
events. Owns the `sclera_scheduler` Postgres schema and the monitoring/control UI.

## Rules
- All alpha Dapr Jobs-API calls go through `client/SchedulerClient` only.
- `jobs.yaml` is the source of truth for the job catalog. DB holds runtime state
  (ENABLED/PAUSED/DISABLED) and run history.
- No business logic lives here — jobs fire events; owning services do the work.

## See also
- Spec: `docs/superpowers/specs/2026-06-02-dapr-scheduler-service-design.md`
- Plan: `docs/superpowers/plans/2026-06-02-dapr-scheduler-service.md`
