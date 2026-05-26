# Phase 3 — Schema Gap Survey: Missing Columns/Tables vs Dialect Issues

**Date:** 2026-05-26  
**Branch:** feature/postgres-migration  
**Scope:** `sclera-cloud-device-asset/src` + `sclera-vdms-service/src`  
**Method:** information_schema baseline (PG live container `sclera-postgres`) + grep-based query enumeration + targeted EXPLAIN sweeps

---

## Summary Totals

| Category | Approx # queries / method annotations | Notes |
|---|---|---|
| **Total native query method annotations** | ~1,057 | 653 `nativeQuery=true` in @Query; 404 `@NamedNativeQuery` |
| **DIALECT-only** (portable; MySQL->PG mechanical port) | ~480 | Queries using IFNULL, IF(), ON DUPLICATE KEY, JSON_*, FROM_UNIXTIME, CONVERT_TZ, CAST AS JSON — but referencing columns/tables that DO exist in PG |
| **MISSING_COLUMN** (extraction gap) | ~200 | Queries referencing `product_details.image_url_*`, `conditions.*` (table has only 3 of ~43 expected columns), `user.customer_org_id` |
| **MISSING_TABLE** (extraction gap) | ~16 | Queries JOINing `alert_profile`, `vendor`, `report_attributes`, `location_global_checklist`, `customer_organisation` — none exist in PG |
| **Clean / already-portable (no issues)** | ~361 | Pure SELECT/INSERT/UPDATE on tables that have their columns; no MySQL dialect constructs. Includes all 3 sclera-vdms-service queries. |

> **Coverage caveat:** The 653+404 annotation count may include both the @Query/@NamedNativeQuery annotation line AND its `resultSetMapping` companion on adjacent lines. True distinct query strings are approximately 600–700. EXPLAIN was run on ~25 representative queries; the remainder classified by grep-pattern frequency against the confirmed PG schema.

---

## Table 1 — Missing COLUMNS (column referenced in query, column absent from PG table)

| schema.table.column(s) | # query lines referencing | Example files |
|---|---|---|
| `public.product_details.image_url_1` | 35 | `Bacnet_Object.java`, `Device.java`, `Lorawan_Sensor.java`, `MeasuringInstrument.java` |
| `public.product_details.image_url_2` | ~5 | `Device.java` (getDeviceByDeviceId query) |
| `public.product_details.image_url_3` | ~5 | `Device.java` |
| `public.product_details.global_image_url_1` | ~4 | `Bacnet_Object.java`, `Device.java`, `Lorawan_Sensor.java` |
| `public.conditions.*` (all except id, bacnet_object_bacnet_device_id, bacnet_object_id) | 22 | `Conditions.java`, `ConditionsRepository.java`, `Bacnet_Device.java`, `MeasuringInstrument.java` |
| `public.user.customer_org_id` | 15 (query lines in User.java + UserRepository) | `User.java`, `UserRepository.java` |

**Detail on `conditions` table:**  
The PG table `public.conditions` has only 3 columns (`id`, `bacnet_object_bacnet_device_id`, `bacnet_object_id`). The Java queries INSERT/SELECT/UPDATE ~43 columns including: `name`, `value`, `second_value`, `alert_message`, `start_time`, `end_time`, `schedule`, `schedule_conditions`, `alert_count_enabled`, `max_alert_count`, `alert_count`, `alert_condition`, `alert`, `show_alert`, `show_alert_message_as_value`, `lorawan_sensor_attributes_lorawan_sensor_id`, `lorawan_sensor_attributes_name`, `snmp_device_id`, `disruptive_sensor_id`, `measuring_instrument_id`, `alert_time`, `daintree_device_id`, `daintree_point_id`, `alert_profile_id`, `ecobee_sensor_attributes_*`, `modbus_register_id`, `priority`, `last_alerted`, `alert_count_time`, `enable_threshold_line_onchart`, `color_of_threshold_line_onchart`, etc. This is a severely trimmed table.

**Detail on `product_details` table:**  
The PG table `public.product_details` has only 1 column (`id bigint PK`). All 32+ JOIN references query columns `image_url_1`, `image_url_2`, `image_url_3`, `global_image_url_1` that do not exist. The FK `device.product_id → product_details.id` is intact, but the product data columns were not migrated.

---

## Table 2 — Missing TABLES (relation does not exist in PG)

| Table referenced | # query occurrences (active, non-commented) | Files | Likely status |
|---|---|---|---|
| `alert_profile` | 2 (JOIN in Conditions.java + ConditionsRepository.java); ~16 FK column `alert_profile_id` used in DML | `Conditions.java`, `ConditionsRepository.java`, `DeviceConditions.java`, `DeviceConditionsRepository.java` | **Removed entirely** — no @Entity or @Table stub found in src |
| `vendor` | 4 (active JOINs in Docker.java) | `Docker.java` | **Removed entirely** — no entity stub (Vendor_Organisation.java exists but maps to `vendor_organisation`, not `vendor`) |
| `report_attributes` | 4 (LEFT JOIN in Bacnet_Object.java + MeasuringInstrument.java) | `Bacnet_Object.java`, `MeasuringInstrument.java` | **Removed entirely** — no entity stub |
| `location_global_checklist` | 3 (JOIN in Location.java) | `Location.java` | **Removed entirely** — no entity stub |
| `customer_organisation` | 2 (LEFT JOIN in User.java) | `User.java` | **Removed entirely** — no entity stub |

All five were confirmed via `EXPLAIN` → `ERROR: relation "X" does not exist`. None have a corresponding @Entity class in the codebase.

---

## Table 3 — MySQL Dialect Constructs Present (sizing the mechanical port)

| Construct | Approx # occurrences (grep, non-commented) | # files |
|---|---|---|
| `IF(cond, a, b)` | 356 | ~30+ |
| `IFNULL(a, b)` | 80 | ~20+ |
| `ON DUPLICATE KEY UPDATE` | 41 | 33 |
| `JSON_EXTRACT(col, '$.path')` or `col->'$.path'` | 74 + arrow-operator usage | 5 |
| `JSON_SET(...)` | 19 | 3 |
| `JSON_MERGE_PATCH(...)` | 2 | 1 |
| `JSON_CONTAINS(...)` | 8 | 1 |
| `JSON_QUOTE(...)` | 4 | 1 |
| `FROM_UNIXTIME(...)` | 32 | 3 |
| `CONVERT_TZ(...)` | 32 | 3 |
| `UNIX_TIMESTAMP(...)` | 9 | 3 |
| `DATE_FORMAT(...)` | 4 | 3 |
| `CAST(? AS JSON)` | 10 | 2 |
| `CONCAT_WS(...)` | 110 (exists in PG, compatible) | many |

**PG mapping notes:**
- `IFNULL(a,b)` → `COALESCE(a,b)` (direct 1-for-1)
- `IF(cond,a,b)` → `CASE WHEN cond THEN a ELSE b END`
- `ON DUPLICATE KEY UPDATE` → `INSERT ... ON CONFLICT (pk) DO UPDATE SET ...`
- `JSON_EXTRACT(col,'$.k')` → `col::jsonb -> 'k'` or `col::jsonb ->> 'k'`; MySQL `->` path operator → PG `->` / `->>`
- `JSON_SET(...)` → `jsonb_set(...)` (argument order differs)
- `JSON_MERGE_PATCH(...)` → `jsonb_merge(...) ` / manual `|| operator` for jsonb
- `JSON_CONTAINS(col, val)` → `col::jsonb @> val::jsonb`
- `FROM_UNIXTIME(ms/1000)` → `to_timestamp(ms/1000.0)`
- `UNIX_TIMESTAMP(ts)` → `EXTRACT(EPOCH FROM ts)`
- `CONVERT_TZ(ts, 'UTC', tz)` → `ts AT TIME ZONE tz`
- `DATE_FORMAT(dt,'%a')` → `TO_CHAR(dt,'Dy')`
- `CAST(? AS JSON)` → `CAST(? AS jsonb)` or `?::jsonb`
- `CONCAT_WS(sep, ...)` → compatible in PG, no change needed

---

## Interpretation

**The missing-column/table problem is concentrated in a small number of entities, not widespread:**

1. **`product_details` is the single largest missing-column cluster.** The table exists with only `id bigint`, but 35 query lines across 4 model files (`Bacnet_Object.java`, `Device.java`, `Lorawan_Sensor.java`, `MeasuringInstrument.java`) select image URL columns that were stripped during extraction. Every query that joins `product_details` and selects `p.image_url_1` or `p.global_image_url_1` will fail. Fix: either add the missing columns to `product_details` (requires source data) or replace with `NULL AS image_url_1` placeholders where image display is non-critical.

2. **`conditions` is the most severely trimmed existing table.** It retains only 3 of ~43 columns. Every INSERT and SELECT on this table (22 query lines, 4 files) will fail with column-not-found errors. The `conditions` table in the original MySQL schema stores sensor alert rules across all protocols (BACnet, LoRaWAN, SNMP, etc.) — the extraction schema migration needs to add all missing columns before any alert/monitoring workflow can run.

3. **`user.customer_org_id` is missing** — this column links users to customer organisations. 15 query lines in `User.java` and `UserRepository.java` reference it directly; additional service-layer code in `EssentialService.java`, `UserService.java`, `VdmsService.java` propagates the value. The column needs to be added to the `user` table.

4. **5 tables are fully absent** — `alert_profile`, `vendor`, `report_attributes`, `location_global_checklist`, `customer_organisation`. These affect only 16 query occurrences across 7 files (small blast radius). None have entity stubs; they were removed entirely. Queries referencing `alert_profile` via a FK column (`alert_profile_id` in `conditions` and `device_conditions`) will fail on INSERT/UPDATE until either the table is added or the FK column is removed.

5. **The dialect problem is broad** — 43 files contain MySQL-specific functions. The constructs are mechanical to translate (IFNULL→COALESCE, IF→CASE, ON DUPLICATE KEY→ON CONFLICT, JSON_* → jsonb equivalents) but the sheer volume (~556 individual occurrences) makes manual translation error-prone. A scripted find-and-replace approach per construct type is recommended.

6. **sclera-vdms-service is clean** — all 3 of its native queries (SELECT *, UPDATE vdms SET customer_org_id, UPDATE vdms SET address) pass EXPLAIN without errors. It has no dialect issues or missing schema references.

---

## EXPLAIN Results Summary (representative sample)

| Query | Result | Primary Error |
|---|---|---|
| `SELECT COUNT(id) FROM device WHERE mac_address=... AND docker_vdms_id=...` | PASS | — |
| `INSERT INTO device(id, docker_vdms_id, ..., asset_group) VALUES(...)` | PASS | — |
| `UPDATE device SET monitor=..., IFNULL(?22, custom_fields)...` | FAIL | `function ifnull(...) does not exist` → **DIALECT** |
| `SELECT d.id, p.image_url_1 FROM device d LEFT JOIN product_details p ON d.product_id=p.id` | FAIL | `column p.image_url_1 does not exist` → **MISSING_COLUMN** |
| `SELECT IFNULL(status,0) FROM device` | FAIL | `function ifnull(integer, integer) does not exist` → **DIALECT** |
| `SELECT IF(status=1,'active','inactive') FROM device` | FAIL | `function if(boolean, unknown, unknown) does not exist` → **DIALECT** |
| `SELECT JSON_MERGE_PATCH(adc_json::text,'{}') FROM device` | FAIL | `function json_merge_patch(text, unknown) does not exist` → **DIALECT** |
| `SELECT id, name FROM conditions WHERE id=1` | FAIL | `column "name" does not exist` → **MISSING_COLUMN** |
| `SELECT u.email, u.customer_org_id FROM "user" u` | FAIL | `column u.customer_org_id does not exist` → **MISSING_COLUMN** |
| `... LEFT JOIN alert_profile ap ON ap.id=...` | FAIL | `relation "alert_profile" does not exist` → **MISSING_TABLE** |
| `... LEFT JOIN vendor v ON d.vendor_org_id=v.vendor_org_id` | FAIL | `relation "vendor" does not exist` → **MISSING_TABLE** |
| `... LEFT JOIN location_global_checklist lgc ON ...` | FAIL | `relation "location_global_checklist" does not exist` → **MISSING_TABLE** |
| `... LEFT JOIN report_attributes r ON ...` | FAIL | `relation "report_attributes" does not exist` → **MISSING_TABLE** |
| `... LEFT JOIN customer_organisation co ON ...` | FAIL | `relation "customer_organisation" does not exist` → **MISSING_TABLE** |
| `SELECT * FROM vdms LIMIT 1` | PASS | — |
| `UPDATE vdms SET customer_org_id=... WHERE id=...` | PASS | — |
| `UPDATE device_conditions SET alert_condition=..., alert_profile_id=...` | PASS | — |
| `INSERT INTO building(id,name,code)... ON DUPLICATE KEY UPDATE name='x'` | FAIL | `syntax error at or near "DUPLICATE"` → **DIALECT** |
| `SELECT ta.condition->'$.exceptions' FROM technician_availability` | FAIL | `operator does not exist: character varying -> unknown` → **DIALECT** (JSON path arrow operator) |
| `UPDATE vdms SET address=... WHERE id=...` | PASS | — |

---

## Recommended Remediation Priority

1. **[BLOCKING — Data]** Add missing columns to `product_details` (image_url_1, image_url_2, image_url_3, global_image_url_1, etc.) or null-stub them with `NULL AS x` in the SELECT list.
2. **[BLOCKING — Data]** Restore the full `conditions` table DDL with all ~43 columns from the MySQL schema.
3. **[BLOCKING — Schema]** Add `customer_org_id varchar(255)` column to the `user` table.
4. **[BLOCKING — Schema]** Create `alert_profile` table (or stub it); update FK references in `conditions` and `device_conditions`.
5. **[BLOCKING — Schema]** Create `vendor`, `report_attributes`, `location_global_checklist`, `customer_organisation` tables or redirect the JOINs.
6. **[MECHANICAL — Dialect]** Script-translate all 43 files: IFNULL→COALESCE, IF()→CASE, ON DUPLICATE KEY→ON CONFLICT, JSON_*→jsonb equivalents, date functions→PG equivalents.
