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
| 3 | `alert_profile` | alerts | `models/AlertProfile.java` | `Conditions.alert_profile_id`, `device_conditions.alert_profile_id` | pending |
| 4 | `report_attributes` | reports | `models/ReportAttributes.java` | `report_template_id` | pending |
| 5 | `location_global_checklist` | inspection | `models/LocationGlobalChecklist.java` (+ `LocationGlobalChecklistId.java`) | checklist id in `Location` | pending |
| 6 | `vendor` | integrations | `models/Vendor.java` | `Device.vendor_org_id` | pending |
| 7 | sensor-attributes family (`lorawan_sensor_attributes`, `my_devices_sensor_attributes`, `pelican_sensor_attributes`, `ecobee_sensor_attributes`, `snmp_object`, `disruptive_sensor`, `monnit_sensor`, `knx_group`, `daintree_point`, `modbus_register`) | integrations (sensor) | mixed — see note | `Conditions.*_id` scalars | deferred / documentation |

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
