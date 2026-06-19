# Workorder service integration & stub retirement — Design

**Date:** 2026-06-19
**Status:** Approved for planning
**Branch:** feature/scheduler-vdms-management-ui (current)

## 1. Goal & scope

Replace the hardcoded `sclera-workorders` walking-skeleton stub with the received
full workorder service (`Sclera-2.0-sclera-cloud-workorder-main/`) — tickets, Maximo
integration, user-action audit, and scheduler-trigger handling — running inside the
monorepo under the **existing platform identity** (`app-id: sclera-workorders`, HTTP
port **8094**), with all Dapr inter-service communication live and verified end-to-end.

In scope:
- Drop-in replacement of the stub module with the received code.
- The minimal **reverse-side changes** required for the received clients to actually
  reach their targets: a new internal endpoint on `sclera-cloud-device-asset` and a new
  pub/sub subscriber on `sclera-vdms-service`.
- Compose / db-init / Dapr registry wiring.
- End-to-end verification of the Dapr sidecar flow for a few representative methods.

Out of scope (explicitly deferred):
- Flyway conversion (service keeps Hibernate `ddl-auto=update` as shipped).
- Topic renaming to the `<domain>.<event>` convention.
- Reuse of `dapr-commons` shared event types in the workorder service.
- Replacing `fastjson`/`fastjson2` (flagged for a later security review).
- `git push` / branch integration — the verification loop is build + run locally.

## 2. Decisions (locked)

| Decision | Choice |
|---|---|
| Integration style | Full drop-in replacement of the stub |
| Service identity | Keep `sclera-workorders` @ **8094** (registry + compose unchanged) |
| Module directory | Keep `sclera-workorders/` (preserves build context, gateway routes, `network_mode: service:sclera-workorders`) |
| Java package | Standardize on received `io.sclera.workorder` (singular); stub `io.sclera.workorders` removed |
| Maven artifactId | Keep received `sclera-cloud-workorder` (internal only) |
| DB schema mgmt | Hibernate `ddl-auto=update` (as shipped); DB `workorder_db` |
| Verification | Local `docker compose up` of the service slice + smoke a few methods |

## 3. Source → target mapping

- Copy `Sclera-2.0-sclera-cloud-workorder-main/{src, pom.xml, Dockerfile, .dockerignore,
  README.md}` into `sclera-workorders/`, **replacing** the stub `src/` tree.
- Remove stub artifacts: `defaults/Defaults.java`, the 5 stub controllers
  (`CorrigoController`, `MyDevicesController`, `PmsController`, `TicketController`,
  `WorkorderTemplateController`), `Application.java` (stub), `SkeletonContractTest`,
  stub `topics.yaml`, stale `target/`, and the stub `CLAUDE.md` (replace with the
  received README/CLAUDE content).
- After the copy, the received `Sclera-2.0-sclera-cloud-workorder-main/` source folder is
  deleted from the repo root (it is a delivery drop, not a module).

## 4. Identity & config reconciliation

- **App-id / port:** keep `sclera-workorders` @ 8094. Align the received `application.yml`
  so the app listens on 8094 and the Dapr HTTP port matches the compose sidecar; correct
  any `8092` references inherited from the received README/config (8092 belongs to
  `sclera-alerts`).
- **Spring Boot parent:** confirm the received `spring-boot-starter-parent` version matches
  the platform's Boot 4.0.x line; pin to match if it drifts (the rest of the monorepo is on
  Java 21 + Boot 4.0.6).
- **Database:** add `workorder_db` creation to `db-init/init.sql`; supply
  `DB_URL/DB_USER/DB_PASSWORD` env on the compose service. Redis (already running) backs the
  service's cache starter.

## 5. Inter-service wiring

### 5a. Outbound (clients already coded in the received service)

| Client | Target | Endpoint | Action |
|---|---|---|---|
| `DeviceAssetClient` | sclera-cloud-device-asset | PUT `…/internal/device/{id}/ticket-sync` | **ADD** endpoint on device-asset (see 5c) |
| `VdmsClient` | vdms-service | GET `…/vdms/{id}`, `…/vdms/id` | **VERIFY** paths/DTOs vs vdms-service controller; adjust client only if mismatched |
| `SchedulerClient` | sclera-scheduler | one-time job create (Jobs API) | **VERIFY** vs scheduler's API |
| `MaximoApiClient` | external Maximo | direct / binding | external — no platform target |

### 5b. Inbound — vdms-service subscriber (new code)

The workorder's `UserActionLogClient` publishes audit entries to topic
**`user-action-log-events`**, expecting vdms-service to persist them into its existing
`user_action_log` table. vdms-service has the table, model, repo, and a read endpoint, but
**no subscriber** for that topic (it subscribes only to `device.audit` and `vdms.*`).

→ **ADD** a `UserActionLogSubscriber` to `sclera-vdms-service` that consumes
`user-action-log-events` and writes via `UserActionLogRepository`, following the existing
subscriber convention used by `DeviceAuditVdmsSubscriber` (programmatic subscription via
`topics.yaml` / `dapr-commons` `DaprEventSubscriber` — match whatever the existing vdms
subscribers use). Map the `UserActionLogDTO` (incl. `vdmsId`, `requestId`) onto the
`UserActionLog` entity.

### 5c. Inbound — device-asset internal endpoint (new code)

→ **ADD** `PUT /api/v1/device-asset-service/internal/device/{deviceId}/ticket-sync` on
`sclera-cloud-device-asset`: a thin internal controller delegating to the existing
`DeviceService.updateDeviceTicketCount` / `updateDeviceTicketStatus`. No sidecar change —
device-asset's Dapr sidecar already runs at app-id `sclera-cloud-device-asset` (port 8085).

### 5d. Inbound — scheduler trigger to workorder

The received `SchedulerTriggerSubscriber` listens on route `/internal/scheduler-demo` for
`scheduler.trigger`, filtering to owner `workorder`, but ships no subscription YAML.

→ **ADD** the subscription wiring for `sclera-workorders` using this repo's actual
convention (match how `sclera-audit` / `sclera-scheduler` declare subscriptions — the
received code assumes a `components-docker/subscription-*.yaml` layout that does not exist
here, so adapt rather than copy).

### 5e. Registry

Update `dapr/APP_IDS.md`: `sclera-workorders` now **Subscribes** `scheduler.trigger`,
`user-action-log-events` (workorder's own) as needed and **Publishes**
`user-action-log-events`.

## 6. Build, compose & infra

- `docker-compose.yml` `sclera-workorders` service: structure unchanged; add DB env and
  `depends_on: postgres`; confirm Dockerfile path and jar name resolve against the received
  Dockerfile. Sidecar block (`--app-id sclera-workorders --app-port 8094`) unchanged.
- `docker-compose.override.yml`: update the dev override for the new build/profile.
- `db-init/init.sql`: create `workorder_db`.
- Rebuild images/jars for the **three** changed services: `sclera-workorders` (new code),
  `sclera-cloud-device-asset` (new endpoint), `sclera-vdms-service` (new subscriber).

## 7. End-to-end Dapr verification strategy

Every Dapr hop is `app → local sidecar → remote sidecar → remote app`, observable at each
step. Fully testable locally.

1. **Build** jars/images for the three changed services.
2. **Bring up the slice:** `postgres, redis, placement` + `{service, service-dapr}` pairs
   for `sclera-workorders, sclera-cloud-device-asset, vdms-service, sclera-scheduler`.
3. **Smoke representative methods**, asserting each hop:
   - `POST …/workorder-service/ticket/upsertticket` →
     (a) ticket row in `workorder_db`;
     (b) `user_action_log` row appears in **vdms-service** (proves publish → subscribe);
     (c) **device-asset** logs the `ticket-sync` invocation (proves invoke → remote app).
   - `GET …/maximo/getvdmsdetails` → proves workorder → vdms-service service-invocation.
   - Emit a `scheduler.trigger` with owner `workorder` → assert the demo log line.
   - Cross-check Jaeger (`http://localhost:16686`) traces show calls routing through the
     sidecars, not direct app-to-app.
4. **Regression:** device-asset baseline `mvnw test` = 622/0/4 (4 known Testcontainers IT
   failures without Docker) unaffected; the workorder module's shipped test suite passes;
   vdms-service tests pass with the new subscriber.

## 8. Risks / verify-during-implementation

- **Boot version drift** between the received parent and platform Boot 4.0.6.
- **Outbound contract mismatches** — verify each target's actual path/verb/DTO against the
  live controller, not the client's assumed path.
- **Subscription mechanism** — adapt the received `components-docker/subscription-*.yaml`
  assumption to this repo's real convention.
- **`fastjson` / `fastjson2`** present in the received pom — keep for drop-in; flag for a
  follow-up security review.
- **DTO shape** of `UserActionLogDTO` vs the vdms `UserActionLog` entity (field names,
  `vdmsId` partition key) — confirm mapping when writing the subscriber.

## 9. Change-set summary

| Service | Change |
|---|---|
| `sclera-workorders` | Replace stub with received service (the swap) |
| `sclera-cloud-device-asset` | +1 internal `ticket-sync` controller endpoint |
| `sclera-vdms-service` | +1 `UserActionLogSubscriber` + subscription wiring |
| infra | compose env, `db-init/init.sql` (`workorder_db`), `dapr/APP_IDS.md`, subscription YAML(s) |
