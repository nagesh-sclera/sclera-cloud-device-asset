# Multi-VDMS Support — Design Spec

- **Date:** 2026-07-06
- **Service:** `sclera-cloud-device-asset` (+ `sclera-ui`)
- **Status:** Approved for planning
- **Approach:** Hybrid — keep existing explicit `vdms_id` params, add a request-scoped `VdmsContext` to remove the singleton, de-stub the VDMS repositories, add CRUD, migrate single-VDMS env into DB.

## 1. Goal & Non-Goals

**Goal:** Convert the service from "one implicit VDMS per process" into "many VDMS rows, one selected explicitly per request," using the **same Docker Compose** and the **same running application**. The change is database-driven and backward compatible with the existing single-VDMS install.

**Non-goals:**
- No new servers, containers, or Compose instances; no topology change.
- No changes to sibling services (`sclera-scheduler`, `dapr-commons`, `sclera-cloud-vdms`, gateway, etc.).
- No live upstream token-exchange implementation. `APICallClient.getVdmsAccessToken(...)` is a **no-op stub today** and stays a stub — but is made per-VDMS-ready (called with a row's data, not a global).
- No rename of the existing snake_case entity fields on `Vdms`.

## 2. Background

This service was extracted from `sclera-vdms-edge-server` — software that ran *on a single VDMS edge gateway*. Consequently the entire access path assumes **"this process = exactly one VDMS."** The data model, however, is already multi-row capable: the `vdms` table has a PK `id`, and there are `vdms_configuration` and `vdms_details` tables. The singleton lives in the **access layer** and in **single-VDMS config/env**, not the schema.

## 3. Current Single-VDMS Assumptions (inventory to remove)

| # | Location | Assumption |
|---|----------|-----------|
| 1 | `utils/AuthenticationUtils.java` | Process-global `@Component` holds one `vdms_id` + one `access_token`/`refresh_token`. **Core singleton.** |
| 2 | `service/touchscreen/VdmsService.startVdmsService()` | Boot resolves *the* VDMS id → stashes in `AuthenticationUtils` → fetches one token. |
| 3 | `Repository/VdmsRepository` (Phase-2 stub) | `getVDMSId()`, `getVDMSPassword()`, `getVdmsDetails()`, `getIsMaster()` take **no id**. |
| 4 | `Repository/VdmsconfigurationRepository` (Phase-2 stub) | `getConfiguration()` takes **no id**. |
| 5 | `models/Vdms.java` named queries | `Vdms.getSyncDetailsForADC` → `... FROM vdms LIMIT 1`; `Vdms.getVdmsDetails` / `Vdms.getVdmsMasterSlaveDetails` → no `WHERE`. |
| 6 | `service/DeviceService.java:6438,6471` | `getDeviceIdsByFilter` / `getDevicesByFilter` read `authenticationUtils.getVdms_id()` instead of taking a parameter. |
| 7 | `application-*.yml`, `.env.example` | Single `sclera.vdms-server-url` + single credential path `secret_url: .../sclera-vdms-server/credential/`. |

**Already multi-VDMS-ready (no change):** ~263 `vdms_id` uses across 14 controllers and 15 in `DeviceService` already thread `vdms_id` explicitly.

## 4. Target Architecture (Approach 3)

- **`VdmsContext`** — a `@RequestScope` bean carrying the request's `vdmsId` plus lazily-resolved per-VDMS credentials/token.
- **`VdmsContextInterceptor`** — a Spring `HandlerInterceptor` that resolves the VDMS for each request with this precedence:
  1. `X-Vdms-Id` request header (primary),
  2. `vdms_id` request parameter (fallback),
  3. the single existing row when **exactly one** VDMS exists (backward-compat fallback),
  4. otherwise leave unset; endpoints that require a VDMS return `400`.
- **`AuthenticationUtils`** — remove the `vdms_id` / `access_token` / `refresh_token` fields (verify `devuid`/`public_key` consumers before removing those). Its ~2 singleton reads in `DeviceService` are replaced by an added `vdms_id` method parameter sourced from the existing controller-level value.
- **De-stub** `VdmsRepository` and `VdmsconfigurationRepository` into real Spring Data JPA repositories keyed by `vdms_id`.
- **`VdmsController` + `VdmsCrudService`** — new CRUD surface (Section 7).

### Unit boundaries
- `VdmsContext` (state) — what VDMS this request targets; depends on nothing.
- `VdmsContextInterceptor` (resolution) — maps request → `vdmsId`; depends on `VdmsRepository` (single-row fallback) + `VdmsContext`.
- `VdmsCrudService` (lifecycle) — create/update/delete/list; depends on `VdmsRepository`, validation.
- `VdmsRepository` / `VdmsconfigurationRepository` (data) — per-id reads/writes on `vdms` / `vdms_configuration`.

## 5. Data Model & Migration

The `vdms` table already holds many rows and has a `password` column.

**Schema additions to `vdms` (all nullable):**
- `server_url` — per-VDMS upstream URL (was global `sclera.vdms-server-url`).
- `credential_ref` — pointer / secret name for credentials (was global `secret_url`). Stores a **reference**, not a raw secret.
- `settings` (`text`, JSON) — per-VDMS settings not already covered by `vdms_details` / `vdms_configuration`. Add only if needed during planning; otherwise omit (YAGNI).

**Query fixes in `models/Vdms.java`:** add `WHERE vdms.id = :vdms_id` to `getVdmsDetails` and `getVdmsMasterSlaveDetails`; parameterize `getSyncDetailsForADC` (drop `LIMIT 1`).

**Migration mechanics:** Flyway is disabled (`spring.flyway.enabled: false`); the project uses `spring.sql.init` + `ddl-auto: update`, with SQL under `infra/postgres-init/*.sql` (precedent: `30-qr-schema.sql`) and `src/main/resources/data.sql`. Add an **idempotent** migration that:
1. `ALTER TABLE vdms ADD COLUMN IF NOT EXISTS ...` for the new columns.
2. Backfills `server_url` / `credential_ref` on the single existing row from the old env defaults, so current installs keep working unchanged.

Confirm the exact file/ordering convention during planning.

## 6. Credentials & Config: env → DB

- **Per-VDMS** values move to the `vdms` row: `server_url`, `credential_ref` (+ existing `password`). `getVdmsAccessToken(vdmsId, password)` is invoked with the row's data.
- **Global** values stay in env: DB connection, Redis, S3 bucket/region, image paths, translator URL, etc.
- Old `sclera.vdms-server-url` / `secret_url` remain readable as a **global default/fallback**, used only to backfill during migration → backward compatible.

## 7. VDMS CRUD API (new `VdmsController`)

```
GET    /vdms            list   (wrap in Page<T> only when pageno/pagesize are supplied — matches repo pagination convention)
GET    /vdms/{id}       get one
POST   /vdms            create (Bean Validation)
PUT    /vdms/{id}       update (Bean Validation)
DELETE /vdms/{id}       delete
```

**Delete semantics:** **block** deletion with `409 Conflict` when the VDMS has child `Building`/`Asset`/`DeviceTechnicianAISuggestion` rows (safer than the entity's current `CascadeType.ALL` silently removing device data). Cascade only on an explicit `?force=true` if we decide we need it during planning.

## 8. Validation

- Bean Validation on the create/update DTO: `@NotBlank` `id` + `property_name`; `@Size` limits matching column lengths (e.g. `property_name` ≤ 128, `mac_address` ≤ 32); constrained `deployment_type` / `status`.
- Request-level: when an endpoint needs a VDMS and none resolves (no header, no param, not exactly-one-row), return `400 Bad Request` with a clear message.
- Create: reject duplicate `id`.

## 9. Testing

- **Unit:** `VdmsCrudService` create/update/delete/list + validation paths; `VdmsContextInterceptor` resolution precedence (header > param > single-row > 400).
- **Repository/IT:** per-id queries return the correct row; extend `schema-pg.sql` seed + add a **multi-row** VDMS seed. (Existing precedent: `VdmsDetailsRepositoryIT`, `src/test/resources/seed/vdms-details-pilot.sql`.)
- **Regression:** the two `DeviceService` filter methods (`getDeviceIdsByFilter`, `getDevicesByFilter`) return correct QR/NFC tag lists for a supplied `vdms_id`.
- **Env:** follow the repo's Docker-free unit-test approach (Testcontainers ITs are broken under Docker 29; JBR 21 `JAVA_HOME` + `mvnw`).

## 10. UI Changes (`sclera-ui`)

- **VDMS management page** — list/create/edit/delete against the new endpoints, mirroring existing admin pages (e.g. `QrCodesPage.jsx`).
- **VDMS selector** in the app shell (`context/AppContext.jsx`) — stores the selected `vdms_id`, persists it, and injects it as the `X-Vdms-Id` header for all requests in `services/api.js`, replacing today's implicit single VDMS.

## 11. Backward Compatibility & Rollout

- One existing VDMS row → interceptor's single-row fallback keeps current UI/clients (which send no `vdms_id`) working.
- New columns nullable + backfilled → non-breaking schema change.
- No Compose/topology change; same app, same container, same port `8085`.

## 12. Risks / Open Items for Planning

- Exact migration file location & load ordering (`infra/postgres-init` vs `data.sql`).
- Whether any `AuthenticationUtils` field other than vdms/token is still consumed (verify before deleting).
- Whether the `settings` JSON column is actually needed (default: omit).
- Confirm no other call site reads the singleton beyond `DeviceService:6438,6471`.
