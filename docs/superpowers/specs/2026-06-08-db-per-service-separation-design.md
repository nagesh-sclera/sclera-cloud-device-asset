# DB-per-Service Separation — Design Spec

**Date:** 2026-06-08
**Status:** Approved
**Scope:** Remove tables this service does not own from its PostgreSQL schema, keep only the
scalar FK id, and replace the cross-module JOIN reads with Dapr-invocation enrichment clients
(stubbed now, real Dapr calls when each owner exposes its endpoint). Completes the
loose-coupling boundary started in the PostgreSQL migration.

Continues the rules in `migration-progress` / `postgres-modularization-rules` and the
`inter-service-dapr-rule` (all inter-service communication goes through Dapr).

---

## Background

During the MySQL→PostgreSQL migration, tables owned by other services were **restored as
minimal `@Entity` stubs inside this service's schema** so the native queries that JOIN them
would parse. That kept the tables physically in `sclera-cloud-device-asset`'s database — the
opposite of DB-per-service. This effort removes them: the table leaves this schema, the scalar
FK id stays on the owning entity, and any read that needed the foreign columns is served by a
**fetch-from-service client**.

The PostgreSQL dialect port is already complete; only intentional `// PG-gap` oddities remain
(preserved verbatim) — they are **out of scope** here.

### Decision (chosen): Pattern + stubbed fetch now (Option 1)

The owning services (inventory, alerts, identity, …) are skeletons without endpoints today, so
this effort does the **database-side separation now** and puts the seam in place as **Dapr-shaped
stub clients** that return safe defaults. Turning each stub into a real Dapr call when the owner
ships is a later, per-domain step (no change to this service's structure). Rejected: full
real cutover (multi-repo, blocked on skeletons); single-relation pilot (user chose full set).

---

## The foreign-table list (canonical — also written to `migration-notes/db-per-service-foreign-tables.md`)

| # | Foreign table | Owner service | Scalar FK kept (here) | Access pattern | Write here? |
|---|---|---|---|---|---|
| 1 | `product_details` | inventory | `Device.product_id` (+ Bacnet_Object/Lorawan_Sensor/MeasuringInstrument joins) | LEFT JOIN → `image_url_1/2/3`, `global_image_url_1` | `addProductImages` already a **no-op** stub; no live writes |
| 2 | `alert_profile` | alerts | `Conditions.alert_profile_id`, `device_conditions.alert_profile_id` | JOIN + FK column in DML | FK column only |
| 3 | `customer_organisation` | identity | `User.customer_org_id` | LEFT JOIN in `User.java` | read |
| 4 | `report_attributes` | reports | scalar `report_template_id` | LEFT JOIN in `Bacnet_Object.java`, `MeasuringInstrument.java` | read |
| 5 | `location_global_checklist` | inspection | scalar checklist id in `Location` | JOIN in `Location.java` | read |
| 6 | `vendor` | integrations | `Device.vendor_org_id` | JOIN in `Docker.java` | read |
| 7 | sensor-attributes family (10): `lorawan_sensor_attributes`, `my_devices_sensor_attributes`, `pelican_sensor_attributes`, `ecobee_sensor_attributes`, `snmp_object`, `disruptive_sensor`, `monnit_sensor`, `knx_group`, `daintree_point`, `modbus_register` | integrations (sensor) | `Conditions.*_id` scalar fields | already `// PG-gap` JOINs (not stubbed locally) | read |

Owners confirmed by the user. Tables 1–6 currently have local `@Entity` stubs to delete; the
table-7 family was never stubbed (only `// PG-gap` JOINs) and just needs its enrichment routed
through a client.

---

## Architecture — the pattern (applied per foreign relation)

For each relation:

1. **Rewrite the native query**: remove `LEFT JOIN <foreign>`; to preserve DTO/API payload
   shape, project literal placeholders for the foreign columns (`NULL AS image_url_1`, …) and
   keep the scalar FK id in the projection. (Chosen over dropping columns — keeps payloads
   byte-identical.)
2. **Enrich in the service layer**: after the query returns, the service calls a per-owner
   client to populate the foreign fields on the DTO from the scalar id(s).
3. **The client is Dapr-shaped and stubbed now**: it lives in `io.sclera.stubs`, returns safe
   defaults (`null`/empty map) and logs **one WARN per invocation** (`CLAUDE.md` stub rule).
   When the owner exposes its endpoint, the stub body is replaced by a **Dapr service-invocation**
   call (`DaprClient.invokeMethod(appId, …)`) — the interface and all call sites are unchanged.
4. **Batch-capable interface**: each client exposes `Map<Id, Dto> fetch(Set<Id> ids)` (plus a
   single-id convenience) so list endpoints enrich in one call and do not become N+1 when the
   real Dapr call lands.

### Hard cases (classified during planning, not skipped)

- **Foreign column used in `WHERE`/`ORDER BY`** (not just `SELECT`): cannot be enriched after
  the query. Each JOIN is classified as **projection-only** (safe: remove + enrich) or
  **filter/sort** (needs a decision). For filter/sort cases the resolution is one of: (a) drop
  the filter/sort if non-critical, (b) keep the query disabled/`// PG-gap` until the owner
  offers a query API, or (c) move the predicate to an id-set obtained from the owner. The plan
  enumerates each and picks per query; none are silently dropped.
- **Writes**: audited — no live writes to these tables (`addProductImages` is already a no-op;
  no `@Modifying` on the stub repos). Any future write becomes a **Dapr command/event to the
  owner**, never a cross-DB write. If the audit during implementation surfaces a real write, it
  is recorded and stubbed as a no-op + WARN (not silently dropped).

---

## Components

One client per owner domain — interface + stub impl + a small enrichment DTO:

| Client (`io.sclera.stubs`) | Owner app-id (Dapr, when wired) | Enriches |
|---|---|---|
| `InventoryClient` | `sclera-inventory` | product images (`image_url_1/2/3`, `global_image_url_1`) |
| `AlertsClient` | `sclera-alerts` | alert profile (name, ioc) |
| `IdentityClient` | `sclera-identity` | customer organisation |
| `ReportsClient` | (reports owner) | report attributes (by `report_template_id`) |
| `InspectionClient` | `sclera-inspection` | location global checklist |
| `VendorClient` | `sclera-integrations` | vendor (by `vendor_org_id`) |
| `SensorClient` | `sclera-integrations` | sensor-attributes family fields |

- Each client: `interface` (batch + single), a `@Component` stub impl returning defaults + WARN,
  and a record DTO for the enrichment fields only.
- The owning entities keep their scalar FK columns. The six stub `@Entity` classes
  (`Product_Details`, `AlertProfile`, `Vendor`, `ReportAttributes`, `LocationGlobalChecklist`,
  `CustomerOrganisation`) and their repositories are **deleted** (and any now-dead service code
  that only existed to serve the stub).

## Data flow (example: device list → product images)

```
GET /devices  ->  DeviceService.getDevices()
   ->  native query (no product_details JOIN; returns ..., product_id, NULL AS image_url_1 ...)
   ->  collect productIds
   ->  InventoryClient.getProductImages(Set<productId>)   // STUB: returns {} + WARN today
                                                           // later: Dapr invoke sclera-inventory
   ->  merge image urls into each DeviceDTO by product_id
   ->  response payload shape unchanged (urls null until inventory is wired)
```

---

## Schema removal

- Deleting the six stub `@Entity` classes stops Hibernate from generating those tables.
- `ddl-auto=update` does **not** drop existing tables, so the already-created stub tables are
  cleared by a **`docker compose down -v` regen** (greenfield — no data to lose). Documented in
  the runbook/migration-note.
- Scalar FK columns (`product_id`, `alert_profile_id`, `customer_org_id`, `vendor_org_id`,
  `report_template_id`, `Conditions.*_id`) remain on their owning entities.

## API / DTO compatibility

Preserved. DTO shapes and endpoint payloads are unchanged; foreign-sourced fields return
`null`/empty until each owner is wired. No DTO field is added or removed.

---

## Decomposition & order

One spec (this) + one implementation plan with **independent per-domain tasks**. Each domain =
{client + DTO + stub test} → {query rewrites} → {entity/repo deletion} → {EXPLAIN + DTO-shape
check}, committed on its own. Suggested order (largest/clearest first as the reference):

1. `product_details` → inventory (the reference implementation)
2. `customer_organisation` → identity
3. `alert_profile` → alerts
4. `report_attributes` → reports
5. `location_global_checklist` → inspection
6. `vendor` → integrations
7. sensor-attributes family → integrations
8. Schema regen + final full-suite + live boot smoke

## Testing

- **Per client (L1 unit, pure Mockito):** stub returns empty/defaults and logs WARN; batch
  method returns a `Map` keyed by id; single-id convenience delegates to batch.
- **Per query rewrite:** `EXPLAIN` on live `sclera-postgres` parses clean (no missing
  relation/column); DTO projection still yields every field (placeholders for foreign cols).
  Respect the Hibernate `::`-cast rule (use `CAST(?N AS type)`, never `?N::type`).
- **Per domain:** existing related unit tests stay green; the app boots on a fresh
  `down -v` stack with the stub tables gone (no Hibernate validation error, health UP).
- **Final:** full device-asset suite green; full Docker stack boots; spot-check a list endpoint
  returns the unchanged DTO shape with null foreign fields.

## Out of scope

- Implementing the owning-service endpoints / real Dapr calls (later, per-domain, when owners
  ship). The stubs are the seam.
- The remaining intentional `// PG-gap` query oddities (preserved verbatim).
- Any DTO/API payload change.
- Moving non-foreign (owned) tables.

## Files touched (anticipated)

| Area | Change |
|---|---|
| `io.sclera.stubs/*Client.java` (+ interfaces, DTOs) | new per-domain Dapr-shaped enrichment clients (stubs) |
| `models/*.java`, `Repository/*.java` (native queries) | drop foreign JOINs; project NULL placeholders; keep scalar id |
| `service/*.java` | enrich DTOs via the clients after query |
| `models/{Product_Details,AlertProfile,Vendor,ReportAttributes,LocationGlobalChecklist,CustomerOrganisation}.java` + repos | **delete** stub entities/repos (+ dead code) |
| `migration-notes/db-per-service-foreign-tables.md` | the canonical list + per-query classification + regen note |
| tests | client stub unit tests; EXPLAIN/DTO-shape checks |
