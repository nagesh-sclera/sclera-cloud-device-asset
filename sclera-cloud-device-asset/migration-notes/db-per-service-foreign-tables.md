# DB-per-Service Separation — Foreign-Table Register

Canonical list for `docs/superpowers/specs/2026-06-08-db-per-service-separation-design.md` /
plan `docs/superpowers/plans/2026-06-08-db-per-service-separation.md`.

**Base branch:** `feature/db-per-service-separation` off `feature/scheduler-service` (NOT main —
main is ~238 commits behind and lacks the PostgreSQL migration entities).

**Goal:** remove these foreign-owned tables from this service's schema; keep the scalar FK id;
serve cross-module reads via Dapr-shaped stub clients (`io.sclera.stubs`).

## Foreign tables (confirmed present in live PG `vdms` as of 2026-06-08)

| # | Table | Owner | Local `@Entity` (delete) | Scalar FK kept | Status |
|---|-------|-------|--------------------------|----------------|--------|
| 1 | `product_details` | inventory | `models/Product_Details.java` (+ `Repository/Product_DetailsRepository.java`) | `Device.product_id` | pending |
| 2 | `customer_organisation` | identity | `models/CustomerOrganisation.java` (deleted) | `User.customer_org_id` | DONE (Task 3) |
| 3 | `alert_profile` | alerts | `models/AlertProfile.java` (deleted) | `Conditions.alert_profile_id`, `device_conditions.alert_profile_id` | DONE (Task 4) |
| 4 | `report_attributes` | reports | `models/ReportAttributes.java` (deleted) | none on MI (link is `report_attributes.primary_id`) | DONE (Task 5) |
| 5 | `location_global_checklist` | inspection | `models/LocationGlobalChecklist.java` (+ `LocationGlobalChecklistId.java`) (deleted) | none (queries gapped) | DONE (Task 6) |
| 6 | `vendor` | integrations | `models/Vendor.java` (deleted) | `docker.vendor_org_id` (queries gapped) | DONE (Task 7) |
| 7 | sensor-attributes family (`lorawan_sensor_attributes`, `my_devices_sensor_attributes`, `pelican_sensor_attributes`, `ecobee_sensor_attributes`, `snmp_object`, `disruptive_sensor`, `monnit_sensor`, `knx_group`, `daintree_point`, `modbus_register`) | integrations (sensor) | mixed — see note | `Conditions.*_id` scalars | DONE (Task 8, doc-only — already decoupled) |

**Keep (this service's own — do NOT touch):** `vendor_organisation` (`models/Vendor_Organisation.java`).

### Sensor-family caveat (table 7)
Live PG currently has only `snmp_object` of the family; `monnit_sensor`, `modbus_register`,
`knx_group`, `disruptive_sensor`, `lorawan_sensor_attributes`, `daintree_point` are NOT in the
DB, yet entity classes named `SnmpObject`, `Monnit_Sensor`, `ModbusRegister`, `KNXGroup`,
`DisruptiveSensor` (etc.) exist in `models/`. Ownership here is contested vs the old
"sensor tables are out" rule. **Task 8 is documentation + a thin client only — do NOT delete
sensor entities or guess ownership; report findings and escalate.**

## Schema regen requirement
`ddl-auto=update` does NOT drop tables. After deleting the entities, run
`docker compose down -v` + `up` to regenerate the schema without the removed tables
(greenfield — no data loss).

## Task 5 — `report_attributes` → reports: DONE (2026-06-15, approach: project defaults + // PG-gap)

Owner `sclera-reports` does NOT exist (no client/DTO/compose service/app-id) — so NO stub client created
(user decision 2026-06-15). Dropped the JOINs and projected defaults; `// PG-gap` notes mark where a future
sclera-reports query API is needed.

- **Only in `models/MeasuringInstrument.java`** (plan also guessed Bacnet_Object — WRONG, MI-only): two
  `@NamedNativeQuery` JOINed `report_attributes`:
  - `getAnalyticsMeasuringInstruments`: `LEFT JOIN report_attributes r ON r.primary_id = mi.id AND r.report_template_id = ?5 AND protocol='measuring_instrument'`. Selected `CASE WHEN r.primary_id=mi.id THEN 1 ELSE 0 END as is_added` and `r.id as report_attribute_id`. Rewrite: drop JOIN; `is_added` → constant `0`; `report_attribute_id` → `CAST(NULL AS varchar)`.
  - `getMeasuringInstrumentsByTemplateId`: `LEFT JOIN report_attributes r ON r.primary_id = mi.id AND r.id = ?3`. Selected `r.id as report_attribute_id` (is_added already hardcoded 0). Rewrite: drop JOIN; `report_attribute_id` → `CAST(NULL AS varchar)`.
- **Dead params dropped from the REPO methods only** (each was the LAST positional, used solely in the removed
  JOIN — no renumbering): `getAnalyticsMeasuringInstruments(..., report_template_id)` → drop `?5`;
  `getMeasuringInstrumentsByTemplateId(..., report_attribute_id)` → drop `?3`. **Service method signatures
  KEPT** (`MeasuringInstrumentService` L761/L872 still accept the param — API frozen — but no longer pass it to the repo).
- **No scalar FK on MI** — the relationship lives on `report_attributes.primary_id → mi.id` (reports' side); nothing to retain.
- **Entity deleted:** `models/ReportAttributes.java` (no repo, no other usage).
- **Tests:** updated `MeasuringInstrumentServiceTest.getAnalyticsMeasuringInstruments_computesOffsetAndDelegates`
  stub to the 4-arg repo signature; class green. Both queries EXPLAIN-clean on live PG; module compiles.
- **// PG-gap (deferred):** computing `is_added` (whether an MI is in a given report template) needs a sclera-reports query API; currently always 0 / report_attribute_id null.

## Task 6 — `location_global_checklist` → inspection: DONE (2026-06-15, approach: gap all + delete entity)

User decision 2026-06-15: **gap all three queries** rather than project defaults, because this whole
feature cluster (checklist / reactive-service location listings) is inspection-domain and depends on
tables that are ALREADY non-local here.

- **Owner has no local seam:** there is an `InspectionRecordClient` but no checklist-DTO client; **no client created**.
- **No local entities** for `global_checklist` / `global_inspection_relation` (only `GlobalQrcode` is local) — so the
  reactive-service query was already runtime-gapped before this task.
- **Three `@NamedNativeQuery` in `models/Location.java` marked `// PG-gap`, SQL left VERBATIM** (no edits to query text):
  - `getAllChecklistLocationsPagination` (L~443) — `lgc` used only for `CASE WHEN lgc.location_id=l.id THEN 1 ELSE 0 END as is_added`.
  - `getAllChecklistLocations` (L~494) — same is_added pattern.
  - `getAllReactiveServiceLocationsPagination` (L~544) — `location_global_checklist` is a structural INNER-JOIN bridge from inspection-owned `global_checklist` to `location`; is_added already hardcoded 0.
  - These will fail at runtime once the table is dropped (schema regen) until sclera-inspection exposes a query API. Acceptable & documented per the plan's hard-case rule.
- **Why not project defaults (is_added=0):** the dead `global_checklist_id(s)` param sits MID positional-list (`?4`/`?2` with `?5–?11` after), so a clean drop needs a risky ~7-param renumber in the query carrying the pre-existing `?8`-as-condition / duplicate-JOIN oddities — not result-validatable on greenfield. Gapping avoids that.
- **Entity deleted:** `models/LocationGlobalChecklist.java` + `models/LocationGlobalChecklistId.java` (`@IdClass`) — no repo, no Java references outside themselves.
- **Pre-existing `// PG-gap` oddities in Location.java PRESERVED** (the `?8`-as-condition in `getAllInspectionLocations`, the duplicate `LEFT JOIN global_qrcode` in `getAllQrcodeLocations`) — untouched. Module compiles (exit 0).
- **No EXPLAIN / no test changes:** SQL unchanged; no repo/service signatures changed (the 3 query methods are byte-identical), only comments added + unreferenced entities deleted.

## Task 7 — `vendor` → integrations: DONE (2026-06-15, approach: gap all + delete entity)

User decision 2026-06-15: **gap all** — all four active `vendor` queries are HARD CASES (vendor drives
WHERE/FROM, including reverse lookups). sclera-integrations exists but has NO vendor endpoints, so a stub
client would return empty → the queries yield nothing either way. NO client created.

- **Only in `models/Docker.java`** (NOT touching the LOCAL `vendor_organisation` / `models/Vendor_Organisation.java` — KEPT). All four marked `// PG-gap`, SQL left VERBATIM:
  - `Docker.listdocker` — `WHERE ve.role='master-vendor'` (filter docker by vendor role). [sibling `listdockerTS` was ALREADY de-vendored — vendor JOIN commented out.]
  - `Docker.getNetworksByVendorEmail` — `WHERE v.email=?1` (filter docker by vendor email; reverse lookup).
  - `Docker.getVendorInfoByDockerName` — `FROM vendor v` + 15 vendor cols (output IS vendor data).
  - `Docker.getHostDockerObj` — `WHERE v.role LIKE 'master-vendor' AND d.host=1`.
  - All break at runtime once `vendor` leaves the schema, until sclera-integrations exposes vendor query APIs (getVendorByOrgId, email/role -> vendor_org_id). Documented per the hard-case rule.
- **Kept:** `dto/VendorDTO.java` (projection target for `vendorinfomapping` — a DTO, not the table) and `models/Vendor_Organisation.java` (this service's own table). Scalar `docker.vendor_org_id` stays.
- **Entity deleted:** `models/Vendor.java` (no repo, no Java refs — only its own declaration).
- **No EXPLAIN / no test changes:** SQL & all signatures unchanged; only comments added + the unreferenced entity deleted. Module compiles (exit 0).

## Task 8 — sensor-attributes family → integrations: DONE (2026-06-15, doc-only — NO code change)

Audited the 10 family tables (`lorawan_sensor_attributes`, `my_devices_sensor_attributes`,
`pelican_sensor_attributes`, `ecobee_sensor_attributes`, `snmp_object`, `disruptive_sensor`,
`monnit_sensor`, `knx_group`, `daintree_point`, `modbus_register`) across `io.sclera` main source.

**Finding: the family is ALREADY fully decoupled in this service — zero JOIN/FROM to any of them.**
`grep '(JOIN|FROM)\s+<table>'` → no matches. They appear only as:
1. **Local scalar FK columns on `conditions`** (e.g. `lorawan_sensor_attributes_lorawan_sensor_id`,
   `monnit_sensor_id`, `snmp_object_oid`, `modbus_register_id`, …) — the loose-coupling scalars restored
   during the PG migration. The `Conditions.*` named queries are pure `FROM/INSERT INTO/UPDATE conditions`
   (the local table); the sensor names are its own columns, NOT joins. Nothing to remove.
2. **Local `device` columns** `snmp_object_count` / `snmp_object_status` (count/status on the device row).
3. **`models/SnmpObject.java`** — wired as `Device`'s `@OneToMany Set<SnmpObject> snmp_object` and used in
   `DeviceService` (1757/1880). It is a device-owned child collection HERE, not a foreign integrations read.
   Per the Task-8 caveat (contested ownership, no guessing) it is LEFT IN PLACE — not deleted.

The other family entities (`Lorawan_Sensor`, `Monnit_Sensor`, `ModbusRegister`, `KNXGroup`,
`DisruptiveSensor`, …) live in the **`sclera-integrations` module**, not in cloud-device-asset.

**Conclusion: no foreign table to remove, no entity to delete, no SensorClient needed.** The plan's
"thin client for any live path" is moot — there is no live cross-service sensor read path in this service.

## Task 9 — schema regen + verification: DONE (2026-06-15)

Ran the destructive regen on the live Docker stack and verified. **App DB on this branch is `vdms`**
(POSTGRES_DB=vdms), user `root` — NOT `sclera_assets` (that was a stale pre-`down` container).

- **Build:** this branch's app `Dockerfile` is RUNTIME-ONLY (`COPY target/sclera.jar app.jar`) — NOT the
  self-contained multi-stage build (that's on the JPA/scheduler line). So: host `mvnw -DskipTests clean package`
  FIRST (the existing jar was stale, Jun 8), THEN `docker compose build app`. Don't trust a cached COPY layer.
- **Regen:** `docker compose down -v` (wiped pgdata) + `up -d postgres redis app app-dapr`.
- **App health = 200** — boots clean with all 5 foreign entities deleted (Hibernate doesn't validate native
  queries at boot, so the gapped `@NamedNativeQuery` don't block startup).
- **Schema correct:** the 5 removed tables (`customer_organisation`, `alert_profile`, `report_attributes`,
  `location_global_checklist`, `vendor`) are ABSENT; `vendor_organisation` + core tables present; 66 public tables.
- **Live (modified) queries validated** against the regenerated schema via EXPLAIN: Task 4 (alert_profile JOIN
  removed) and Task 5 (report_attributes JOIN removed) both produce clean plans.
- **Gapped query confirmed gapped:** `SELECT ... FROM vendor v ...` → `relation "vendor" does not exist` (fails at
  call-time as documented, not silently wrong).
- **Test suite: 615/619 pass.** The 4 failures are **pre-existing and unrelated** — all in
  `io.sclera.client.MyDevicesClientTest`, a Mockito app-id assertion mismatch (`MyDevicesClient.APP_ID =
  "sclera-integrations"` but the test asserts `"sclera-workorders"`). My commits touch NONE of the
  MyDevices/sensor/client code (`git diff --name-only base..HEAD` → no MyDevices), so those files are byte-identical
  to the base branch → the failures exist on base too. **Flagged as a separate pre-existing bug, NOT fixed here**
  (out of db-per-service scope). All tests in the areas I changed (ConditionsServiceTest, MeasuringInstrumentServiceTest,
  InventoryClientStubTest, …) pass.

**Tasks 3–9 COMPLETE.** The DB-per-service separation of cloud-device-asset is done: 6 foreign tables removed from
this service's schema (product_details in Task 2 + the 5 here), sensor family confirmed already-decoupled, cross-module
reads served by Dapr clients (existing) or `// PG-gap` seams (where no owner/endpoint exists yet).

## Filter/sort dependencies (hard cases — fill as found)
_(queries where a foreign column is used in WHERE/ORDER BY/GROUP BY, not just SELECT)_

- _none recorded yet_

## Writes converted to Dapr / no-op (fill as found)
- `Product_DetailsService.addProductImages` — already a no-op (pre-existing).
- _others as discovered_

## Task 3 — `customer_organisation` → identity: DONE (2026-06-15)

**Key finding (plan premise corrected):** the plan assumed `UserDTO` is enriched from
`customer_organisation`. The actual query text in `models/User.java` shows the two queries that
`LEFT JOIN customer_organisation` (`User.getAllOrganisationUsersByPagination`,
`User.getAllUsersByOrganisationId`) **select NO `co.*` column** and never reference `co` in
WHERE/ORDER BY. The DTO's `organisation_id` comes from `u.customer_org_id as organisation_id`
(the scalar FK we keep). The LEFT JOIN was on the PK (`co.id`) so it could neither multiply nor
drop rows — pure dead weight.

**Therefore: no read enrichment, no new client/DTO, no service/test changes.** Write path was
already covered by the pre-existing `io.sclera.client.CustomerOrganisationClient`
(upsert/delete → `sclera-identity`), which stays.

- Removed the two dead `LEFT JOIN customer_organisation co on co.id = u.customer_org_id` lines
  from `models/User.java` (replaced with a `// db-per-service:` note); both queries now reference
  only `"user"`.
- Deleted `models/CustomerOrganisation.java` (entity had no repo, no imports, no usages — only its
  own declaration; grep-verified).
- EXPLAIN-validated both rewritten queries on live `sclera-postgres` (`root`/`sclera_assets`):
  clean `Seq Scan on "user"` plans, no `customer_organisation` reference. Module compiles (exit 0).
- **Hard cases:** none.

## Task 4 — `alert_profile` → alerts: DONE (2026-06-15)

Mostly already Dapr-wired (unlike the plan's assumption of raw JOINs everywhere):

- **DeviceConditions named queries** (4×, `models/DeviceConditions.java`): select only `dc.*` incl. the
  scalar `dc.alert_profile_id` — **no `alert_profile` JOIN**. Enrichment of `DeviceConditionsDTO.alert_profile`
  was ALREADY done in the service via `alertProfileClient.getAlertProfileDetailsById(...)`
  (`DeviceConditionsService:152,184`). No change needed.
- **ConditionsService** read paths (`:1535`, `DeviceService:4862`) already enrich via `AlertProfileClient`. No change.
- **One real JOIN rewritten:** `ConditionsRepository.getConditionsForAdvanceExcelExport` (advanced Excel export)
  `LEFT JOIN alert_profile ap` selected `ap.id`, `ap.name`, `ap.ioc` (SELECT-only — `ap` not in WHERE/ORDER BY,
  so NOT a hard case). Rewrite: drop the JOIN; `ap.id AS alert_profile_id` → `c.alert_profile_id AS alert_profile_id`
  (local scalar FK); `ap.name` → `CAST(NULL AS varchar)`; `ap.ioc` → `CAST(NULL AS integer)`.
  Enriched `name`/`ioc` in `ConditionsService.getConditionsForAdvanceExcelExport` via `alertProfileClient.getAlertProfileById(id)`
  (stub returns null → null until sclera-alerts is wired; DTO shape unchanged). EXPLAIN-clean.
- **Scalar-FK DML untouched** (stays): `UPDATE conditions/device_conditions SET alert_profile_id = NULL WHERE ... = ?1`
  (ConditionsRepository:308, DeviceConditionsRepository:138) and the insert/update DML carrying `alert_profile_id` —
  these write the local scalar column, not the foreign table.
- **Entity deleted:** `models/AlertProfile.java` (no repo, no JPA usage — only declaration + 2 comments).
- **Existing clients reused (NOT created):** `io.sclera.client.AlertProfileClient` (→ `sclera-alerts`) + `io.sclera.dto.AlertProfileDTO` already existed from an earlier stub→client wave.
- **Tests:** `ConditionsServiceTest` — added `@Mock AlertProfileClient` + `..._enrichesAlertProfileFromClient` happy-path test; full class green. Module compiles (exit 0).
- **Hard cases:** none.
