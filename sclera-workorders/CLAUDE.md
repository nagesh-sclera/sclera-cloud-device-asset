# CLAUDE.md — sclera-workorders

## Project identity
The real workorder microservice (tickets + Maximo integration + user-action audit),
integrated from the `sclera-cloud-workorder` delivery. Java 21, Spring Boot 4.0.6,
PostgreSQL (`workorder_db`), package root `io.sclera.workorder.*`. Runs under Dapr
app-id `sclera-workorders` on HTTP port 8094; context-path `/api/v1/workorder-service`.

Replaced the original walking-skeleton stub on 2026-06-19 (see the integration spec/plan
below).

## Rules
- Schema is managed by Hibernate `ddl-auto=update` (no Flyway yet) — tables are created on
  first boot against `workorder_db`.
- Cross-service calls go through the local Dapr sidecar: `DeviceAssetClient`,
  `VdmsClient`, `SchedulerClient` (service-invocation) and `UserActionLogClient`
  (publishes audit entries to the `user-action-log-events` topic, consumed by vdms-service).
- `MaximoApiClient` talks directly to the external Maximo server (NOT through Dapr).

## See also
- Service README: `README.md`
- Integration spec: `docs/superpowers/specs/2026-06-19-workorder-integration-design.md`
- Integration plan: `docs/superpowers/plans/2026-06-19-workorder-integration.md`
