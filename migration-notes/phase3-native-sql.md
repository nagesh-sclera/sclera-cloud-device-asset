# Phase 3 — MySQL-Specific Native SQL Audit

> Task 3.1 — READ-ONLY discovery. No source files were modified.
> Scope: `sclera-cloud-device-asset/src` and `sclera-vdms-service/src`.
> Branch: `feature/microservice-sclera2.0`

---

## Summary Table

| Module | Repository / Model file | Native query count (approx.) | MySQL constructs? |
|--------|------------------------|------------------------------|-------------------|
| sclera-cloud-device-asset | AiCallLogHistoryRepository | 6 | No |
| sclera-cloud-device-asset | AiCallLogRepository | 12 | No |
| sclera-cloud-device-asset | ApplicationUserRepository | 9 + NamedNativeQuery | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | AssetDeviceMappingRepository | 5 | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | AssetFieldRepository | 3 + QueryRepository | **Yes** — ON DUPLICATE KEY, VALUES() |
| sclera-cloud-device-asset | AssetRepository | ~25 | **Yes** — ON DUPLICATE KEY, CONCAT_WS, is_matched=1/0 |
| sclera-cloud-device-asset | BuildingRepository | 6 | **Yes** — ON DUPLICATE KEY, VALUE() |
| sclera-cloud-device-asset | ClientBarCodeRepository | 7 + QueryRepository | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | ClientNfcQueryRepository | 1 | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | ClientQrCodeQueryRepository | 1 | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | ConditionsRepository | ~12 | No |
| sclera-cloud-device-asset | DeviceConditionsRepository | ~15 | No |
| sclera-cloud-device-asset | DeviceInstalledAppsRepository | ~8 | No |
| sclera-cloud-device-asset | DeviceIPAddressRepository | 3 | No |
| sclera-cloud-device-asset | DeviceLifeCycleHistoryRepository | 5 | No |
| sclera-cloud-device-asset | DeviceNetworkSpecificationRepository | 1 | No |
| sclera-cloud-device-asset | DeviceOnboardStatusAssigneeRepository | 4 | No |
| sclera-cloud-device-asset | DeviceOnboardStatusRepository | 5 | **Yes** — IFNULL x6 |
| sclera-cloud-device-asset | DeviceRepository | ~90 | **Yes** — IFNULL x25+, IF x6, ON DUPLICATE KEY x1, REGEXP_REPLACE x1, JSON_MERGE_PATCH x2, JSON_EXTRACT/JSON_SET/JSON_CONTAINS/JSON_QUOTE/JSON_UNQUOTE (many), CONCAT_WS x5, VALUE() (INSERT) |
| sclera-cloud-device-asset | DeviceTechnicianAISuggestionRepository | 1 | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | DeviceTypesRepository | 1 | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | DocumentRepository | 2 + QueryRepository | **Yes** — ON DUPLICATE KEY, VALUES() |
| sclera-cloud-device-asset | FloorRepository | 2 | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | LocationRepository | 8 | **Yes** — ON DUPLICATE KEY, CONCAT_WS, IFNULL |
| sclera-cloud-device-asset | ManagedSoftwareRepository | 1 + QueryRepository | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | MeasuringInstrumentRepository | ~10 | **Yes** — ON DUPLICATE KEY, JSON_EXTRACT, JSON_SET |
| sclera-cloud-device-asset | MediaRepository | 2 + QueryRepository | **Yes** — ON DUPLICATE KEY, VALUES() |
| sclera-cloud-device-asset | SystemInterfaceRepository | 1 | **Yes** — ON DUPLICATE KEY, VALUE() |
| sclera-cloud-device-asset | TechnicianAvailabilityRepository | 4 | **Yes** — ON DUPLICATE KEY, backtick \`condition\` |
| sclera-cloud-device-asset | TechnicianCertificateRepository | 1 | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | TechnicianRepository | 1 | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | TechnicianSkillRepository | 1 | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | VdmsDetailsRepository | 4 | **Yes** — ON DUPLICATE KEY, VALUE() |
| sclera-cloud-device-asset | models/AiCallLog.java | 1 NamedNativeQuery | **Yes** — CONCAT_WS |
| sclera-cloud-device-asset | models/ApplicationUser.java | 1 NamedNativeQuery | **Yes** — ON DUPLICATE KEY |
| sclera-cloud-device-asset | models/Asset.java | 1 NamedNativeQuery | **Yes** — CONCAT_WS |
| sclera-cloud-device-asset | models/Bacnet_Device.java | 1 NamedNativeQuery | **Yes** — CONCAT_WS |
| sclera-cloud-device-asset | models/Bacnet_Object.java | 8+ NamedNativeQueries | **Yes** — IF, IFNULL, CONCAT_WS |
| sclera-cloud-device-asset | models/Device.java | 25+ NamedNativeQueries | **Yes** — CONCAT_WS, REGEXP_REPLACE, IF, IFNULL, UNIX_TIMESTAMP |
| sclera-cloud-device-asset | models/Document.java | 1 | **Yes** — CONCAT_WS |
| sclera-cloud-device-asset | models/GlobalQrcode.java | 4 NamedNativeQueries | **Yes** — CONCAT_WS |
| sclera-cloud-device-asset | models/Location.java | 15+ NamedNativeQueries | **Yes** — CONCAT_WS, REGEXP_REPLACE |
| sclera-cloud-device-asset | models/Lorawan_Sensor.java | 4 NamedNativeQueries | **Yes** — CONCAT_WS, UNIX_TIMESTAMP |
| sclera-cloud-device-asset | models/ManagedSoftware.java | 1 NamedNativeQuery | **Yes** — CONCAT_WS, LIMIT n OFFSET m |
| sclera-cloud-device-asset | models/MeasuringInstrument.java | 8+ NamedNativeQueries | **Yes** — IF, IFNULL, CONCAT_WS |
| sclera-cloud-device-asset | models/Media.java | 1 | **Yes** — CONCAT_WS |
| sclera-cloud-device-asset | models/System_interface.java | 1 NamedNativeQuery | **Yes** — ON DUPLICATE KEY, VALUE() |
| sclera-cloud-device-asset | models/Technician.java | 5 NamedNativeQueries | **Yes** — UNIX_TIMESTAMP, FROM_UNIXTIME, DATE_FORMAT, CONVERT_TZ, JSON_CONTAINS, JSON_QUOTE, CONCAT_WS |
| sclera-cloud-device-asset | models/TechnicianAvailability.java | 4 NamedNativeQueries | **Yes** — ON DUPLICATE KEY, backtick \`condition\` |
| sclera-cloud-device-asset | models/User.java | 3 NamedNativeQueries | **Yes** — CONCAT_WS, LIMIT/OFFSET |
| sclera-cloud-device-asset | service/DeviceSearchService.java | dynamic SQL | **Yes** — CONCAT_WS, IF, REGEXP_REPLACE, JSON_EXTRACT, JSON_UNQUOTE |
| sclera-cloud-device-asset | service/ManagedSoftwareSearchService.java | dynamic SQL | **Yes** — CONCAT_WS, REGEXP_REPLACE |
| sclera-cloud-device-asset | queryrepository/*.java | 9 query builders | **Yes** — ON DUPLICATE KEY, VALUES() |
| sclera-vdms-service | VdmsJpaRepository | 3 | No (simple UPDATE/SELECT) |

**Total native queries (annotated `nativeQuery=true` or `@NamedNativeQuery`):** ~300+  
**Queries with at least one MySQL-specific construct requiring translation:** ~200+ (across ~40 files)  
**Queries that are already portable (simple SELECT/UPDATE/DELETE/INSERT with no MySQL functions):** ~100

---

## Findings by File

### 1. `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/AssetDeviceMappingRepository.java`

**Line 19 — ON DUPLICATE KEY UPDATE**
```sql
INSERT INTO asset_device_mapping(id,match_score,asset_id,device_id)
VALUES(?1,?2,?3,?4)
ON DUPLICATE KEY UPDATE match_score=?2
```
PostgreSQL:
```sql
INSERT INTO asset_device_mapping(id,match_score,asset_id,device_id)
VALUES(?1,?2,?3,?4)
ON CONFLICT (id) DO UPDATE SET match_score = EXCLUDED.match_score
```
> Note: confirm the conflict target column — likely `id` (PK).

---

### 2. `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/AssetRepository.java`

**Line 66 — tinyint/integer boolean comparison**
```sql
DELETE FROM asset WHERE is_matched=1
```
```sql
DELETE FROM asset WHERE is_matched = TRUE
-- or: WHERE is_matched IS TRUE
```

**Line 114 — integer 0 used as boolean**
```sql
UPDATE asset SET is_matched=0
```
```sql
UPDATE asset SET is_matched = FALSE
```
> Depends on whether `is_matched` is mapped to `BOOLEAN` or `TINYINT`. If schema stays `SMALLINT`, keep as `0`/`1` and no change is needed.

**Line 137 — ON DUPLICATE KEY UPDATE**
```sql
INSERT INTO asset(...) VALUES(...)
ON DUPLICATE KEY UPDATE display_name=?2, description=?3, type=?4
```
PostgreSQL:
```sql
INSERT INTO asset(...) VALUES(...)
ON CONFLICT (id) DO UPDATE SET display_name=EXCLUDED.display_name, description=EXCLUDED.description, type=EXCLUDED.type
```

**Line 141 — CONCAT_WS**
```sql
SELECT COUNT(*) FROM asset
WHERE import_type = ?1
AND ?2 = 'null'
OR CONCAT_WS('',display_name,description) LIKE CONCAT('%',?2,'%')
```
PostgreSQL: `CONCAT_WS` is available in PostgreSQL 9.1+ so this is **safe** — no change needed.
> Caution: The `OR` operator precedence here means the `AND` binds tighter than `OR`, which may already be a logic bug. Flag for review.

---

### 3. `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/BuildingRepository.java`

**Line 25 — VALUE() (MySQL-only singular form)**
```sql
INSERT INTO building(id,name,vdms_id,updated_timestamp) VALUE(?1,?2,?3,?4)
```
PostgreSQL uses `VALUES` (plural) only:
```sql
INSERT INTO building(id,name,vdms_id,updated_timestamp) VALUES(?1,?2,?3,?4)
```

**Line 41 — ON DUPLICATE KEY UPDATE**
```sql
INSERT INTO building(...) VALUES(...) ON DUPLICATE KEY UPDATE name = ?2, code = ?4, updated_timestamp = ?5
```
PostgreSQL:
```sql
INSERT INTO building(...) VALUES(...) ON CONFLICT (id) DO UPDATE SET name=EXCLUDED.name, code=EXCLUDED.code, updated_timestamp=EXCLUDED.updated_timestamp
```

---

### 4. `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/DeviceOnboardStatusRepository.java`

**Lines 21-22, 32-33 — IFNULL (x6)**
```sql
UPDATE device_onboard_status
SET image_status = IFNULL(?4, image_status),
    geolocation_status = IFNULL(?5, geolocation_status),
    tag_status = IFNULL(?6, tag_status),
    field_status = IFNULL(?7, field_status)
WHERE id = ?1
```
PostgreSQL:
```sql
UPDATE device_onboard_status
SET image_status = COALESCE(?4, image_status),
    geolocation_status = COALESCE(?5, geolocation_status),
    tag_status = COALESCE(?6, tag_status),
    field_status = COALESCE(?7, field_status)
WHERE id = ?1
```

---

### 5. `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/DeviceRepository.java`

**Lines 55-59 — IFNULL (x1) + JSON_MERGE_PATCH**
```sql
UPDATE device SET
  ...
  custom_fields = IFNULL(?22, custom_fields),
  ...
  adc_json = JSON_MERGE_PATCH(adc_json, CAST(?35 AS JSON))
WHERE ...
```
- `IFNULL` → `COALESCE`
- `JSON_MERGE_PATCH(a, b)` → PostgreSQL uses `jsonb_merge_patch(a, b)` if using the `pg_jsonb_merge_patch` extension (available in PG 17+), **or** use: `a::jsonb || b::jsonb` for simple merging, **or** `jsonb_strip_nulls(a::jsonb || b::jsonb)`. This is a concern — see Hairy Queries section.
- `CAST(?35 AS JSON)` → `?35::jsonb`

**Lines 72-74 — IFNULL (x5)**
```sql
UPDATE device SET
  global_vendor_id = IFNULL(?1, global_vendor_id),
  local_vendor_id  = IFNULL(?2, local_vendor_id),
  ...
  other_vendor_3_id = IFNULL(?5, other_vendor_3_id)
WHERE id = ?6
```
Replace all `IFNULL` with `COALESCE`.

**Lines 116-125 — IFNULL (x23) + IF (x2)**
```sql
location_id = IF(?8 = 'null' or ?8 = '', NULL, IFNULL(?8, location_id)),
...
location_status = IF(?8 IS NULL, location_status, NULL)
```
- `IF(cond, a, b)` → `CASE WHEN cond THEN a ELSE b END`
- All `IFNULL(x, y)` → `COALESCE(x, y)`

**Line 154 — VALUE() (MySQL-only singular form)**
```sql
INSERT INTO device(...) VALUE(?1, ?2, ...)
```
→ `VALUES(?1, ?2, ...)`

**Line 389 — IF**
```sql
SELECT IF((d.user_data_name IS NULL or d.user_data_name = ''), d.display_name, d.user_data_name)
FROM device d WHERE d.id = ?1
```
→
```sql
SELECT CASE WHEN (d.user_data_name IS NULL OR d.user_data_name = '') THEN d.display_name ELSE d.user_data_name END
FROM device d WHERE d.id = ?1
```

**Lines 536-574 — IF(condition, true, false) — multiple count queries**
```sql
SELECT COUNT(id) FROM device WHERE ...
AND IF('all' = ?2, true, assigned_user_email = ?2)
```
→
```sql
AND (CASE WHEN 'all' = ?2 THEN true ELSE assigned_user_email = ?2 END)
-- or more idiomatically:
AND ('all' = ?2 OR assigned_user_email = ?2)
```
> The second form is semantically equivalent for this boolean use case and is preferred.

**Lines 58, 171 — JSON_MERGE_PATCH**
```sql
adc_json = JSON_MERGE_PATCH(adc_json, CAST(?35 AS JSON))
```
See Hairy Queries section for full treatment.

**Line 690-693 — ON DUPLICATE KEY UPDATE + IFNULL (x10)**
```sql
INSERT INTO device (...) VALUES (..., ?3, ?4, ...)
ON DUPLICATE KEY UPDATE
  user_data_name = IFNULL(?4, user_data_name),
  ...
  custom_fields = IFNULL(?13, custom_fields)
```
→
```sql
ON CONFLICT (id) DO UPDATE SET
  user_data_name = COALESCE(EXCLUDED.user_data_name, device.user_data_name),
  ...
```

**Line 1101 — REGEXP_REPLACE + CONCAT_WS**
```sql
LOWER(REGEXP_REPLACE(CONCAT_WS('', d.docker_name), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\ ]', ''))
LIKE CONCAT('%',?2,'%')
```
PostgreSQL:
- `REGEXP_REPLACE(str, pattern, replacement)` exists in PostgreSQL with same 3-arg signature — syntax is compatible.
- `CONCAT_WS` is available in PostgreSQL — no change needed.
- The escape sequence `'\\ '` in Java becomes the regex `\\ ` which is MySQL-specific; in PostgreSQL use `E'[ -.!\\t_+#~...]'` or use a dollar-quoted string.
- Pattern flag: MySQL `REGEXP_REPLACE` is case-sensitive by default; PostgreSQL same. No flag mismatch.

---

### 6. `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/MeasuringInstrumentRepository.java`

**Lines 26-30 — JSON_EXTRACT + JSON_SET**
```sql
UPDATE measuring_instrument SET attribute =
  CASE
    WHEN JSON_EXTRACT(attribute, '$.parameter_1_protocol') = ?1 ...
    THEN JSON_SET(attribute, '$.parameter_1_value', ?4)
    ...
  END WHERE ...
```
PostgreSQL JSON equivalents:
- `JSON_EXTRACT(col, '$.key')` → `col->>'key'` or `col->'key'` (for object) or `jsonb_extract_path_text(col, 'key')`
- `JSON_SET(col, '$.key', val)` → `jsonb_set(col, '{key}', to_jsonb(val))`
- The `$.parameter_N_protocol` path syntax maps to `'{parameter_N_protocol}'` in PostgreSQL.

**Lines 88-92 — JSON_EXTRACT (read)**
Same as above — `JSON_EXTRACT(attribute, '$.parameter_N_xxx')` → `attribute->>'parameter_N_xxx'`.

---

### 7. `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianAvailabilityRepository.java` / `models/TechnicianAvailability.java`

**Line 18 — Backtick-quoted reserved word + ON DUPLICATE KEY**
```sql
INSERT INTO technician_availability (..., `condition`, ...)
VALUES (...)
ON DUPLICATE KEY UPDATE ..., `condition` = ?8
```
PostgreSQL:
- Remove backticks; `condition` is NOT a reserved keyword in PostgreSQL, so bare `condition` works.
- If you want to be safe, use double-quotes: `"condition"`.
- `ON DUPLICATE KEY UPDATE` → `ON CONFLICT (id) DO UPDATE SET ...`

---

### 8. `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsDetailsRepository.java`

**Lines 19, 35, 49, 56 — VALUE() + ON DUPLICATE KEY (x4)**
```sql
INSERT INTO vdms_details (id, ...) VALUE (?1, ?2, ?3) ON DUPLICATE KEY UPDATE ...
```
→ `VALUES(...)` and `ON CONFLICT (id) DO UPDATE SET ...`

---

### 9. `sclera-cloud-device-asset/src/main/java/io/sclera/models/Bacnet_Object.java`

**Lines 420, 450, 576, 588, 601, 757, 768 — IF + IFNULL (repeated pattern)**
```sql
IF(bo.user_data_value IS NULL or bo.user_data_value = '',
   CONCAT(bo.present_value, ' ', IFNULL(bo.unit, '')),
   bo.user_data_value) as value
```
→
```sql
CASE WHEN bo.user_data_value IS NULL OR bo.user_data_value = ''
     THEN CONCAT(bo.present_value, ' ', COALESCE(bo.unit, ''))
     ELSE bo.user_data_value
END as value
```
`CONCAT` is available in PostgreSQL — no change there.

---

### 10. `sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java`

**Lines 2394-2399 — UNIX_TIMESTAMP()**
```sql
AND d.dnd_timestamp < ((UNIX_TIMESTAMP() * 1000) - 86400000)
```
`dnd_timestamp` appears to be stored as epoch-milliseconds (bigint). PostgreSQL:
```sql
AND d.dnd_timestamp < (EXTRACT(EPOCH FROM NOW())::bigint * 1000 - 86400000)
```

**Lines 1344, 1722, 1783, 1804, 2304 — REGEXP_REPLACE + CONCAT_WS (search normalize pattern)**
```sql
LOWER(REGEXP_REPLACE(CONCAT_WS('', d.display_name, ...), '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\ ]', ''))
LIKE CONCAT('%', ?N, '%')
```
PostgreSQL: `REGEXP_REPLACE` exists but uses POSIX-style (not MySQL-style) regex. The character class `[ -.!...]` with a literal hyphen not at start/end may need escaping. Recommended approach:
```sql
LOWER(REGEXP_REPLACE(CONCAT_WS('', d.display_name, ...), '[ \-\.!...]', '', 'g'))
LIKE '%' || ?N || '%'
```
- Add the `'g'` flag (global replace) — MySQL `REGEXP_REPLACE` replaces all by default; PostgreSQL requires the `'g'` flag.
- `CONCAT('%', ?N, '%')` → `'%' || ?N || '%'`

---

### 11. `sclera-cloud-device-asset/src/main/java/io/sclera/models/Lorawan_Sensor.java`

**Lines 475, 491, 522, 539 — UNIX_TIMESTAMP() (x4)**
```sql
WHERE ls.last_seen <= ((UNIX_TIMESTAMP() * 1000) - 86400000)
WHERE ls.last_seen > ((UNIX_TIMESTAMP() * 1000) - 86400000)
```
`last_seen` is stored as epoch-ms bigint. PostgreSQL:
```sql
WHERE ls.last_seen <= (EXTRACT(EPOCH FROM NOW())::bigint * 1000 - 86400000)
WHERE ls.last_seen > (EXTRACT(EPOCH FROM NOW())::bigint * 1000 - 86400000)
```

---

### 12. `sclera-cloud-device-asset/src/main/java/io/sclera/models/Technician.java`

**Lines 37-41 — Complex availability queries (x4 NamedNativeQueries)**
Each query contains the following MySQL-specific functions:
- `UNIX_TIMESTAMP(expr)` — converts datetime to Unix seconds
- `FROM_UNIXTIME(expr)` — converts epoch-ms/1000 to datetime
- `DATE_FORMAT(expr, '%a')` — returns abbreviated weekday name
- `CONVERT_TZ(expr, 'UTC', tz)` — timezone conversion
- `JSON_CONTAINS(json, val)` — JSON containment check
- `JSON_QUOTE(str)` — wraps a string as JSON
- `CAST(x AS JSON)` — cast to JSON type

Representative snippet:
```sql
CAST((UNIX_TIMESTAMP(CONVERT_TZ(DATE(FROM_UNIXTIME(?1 / 1000)), 'UTC', t.time_zone)) * 1000) AS JSON)
```
PostgreSQL translation:
```sql
TO_JSON(
  (EXTRACT(EPOCH FROM
    (DATE(TO_TIMESTAMP(?1 / 1000.0) AT TIME ZONE 'UTC' AT TIME ZONE t.time_zone))
  )::bigint * 1000)
)
```
Full breakdown:
| MySQL | PostgreSQL |
|-------|-----------|
| `FROM_UNIXTIME(?1 / 1000)` | `TO_TIMESTAMP(?1 / 1000.0)` |
| `DATE(expr)` | `expr::date` |
| `CONVERT_TZ(expr, 'UTC', tz)` | `expr AT TIME ZONE 'UTC' AT TIME ZONE tz` (note: `tz` must be a named zone, not a Java offset string) |
| `UNIX_TIMESTAMP(expr)` | `EXTRACT(EPOCH FROM expr)::bigint` |
| `DATE_FORMAT(expr, '%a')` | `TO_CHAR(expr, 'Dy')` — returns 3-char abbreviated weekday, e.g. `Mon` |
| `JSON_CONTAINS(json, val)` | `json::jsonb @> val::jsonb` |
| `JSON_QUOTE(str)` | `to_jsonb(str)::text` or `'"' || str || '"'` |
| `CAST(x AS JSON)` | `to_jsonb(x)` or `x::jsonb` |

> **FLAG FOR HUMAN REVIEW**: The `HAVING (?6 = 'all' OR availability = ?6)` pattern in `getAllTechniciansByFilterByPagination` refers to the computed alias `availability` in a HAVING clause. MySQL allows referencing a SELECT-level alias in HAVING; PostgreSQL does NOT allow referencing aliases in HAVING. The CASE expression will need to be repeated in HAVING, or the query wrapped in a subquery/CTE. This is a **hairy query** — see Hairy Queries section.

---

### 13. `sclera-cloud-device-asset/src/main/java/io/sclera/models/ManagedSoftware.java`

**Line 10 — CONCAT_WS + LIMIT n OFFSET m**
```sql
WHERE ... AND (?2 = 'null' OR CONCAT_WS('', ms.name, ...) LIKE CONCAT('%', ?2, '%'))
LIMIT ?4 OFFSET ?3
```
- `CONCAT_WS` — available in PostgreSQL, no change.
- `LIMIT ?4 OFFSET ?3` — already in PostgreSQL style (LIMIT/OFFSET). **Safe** — no change needed.

---

### 14. `sclera-cloud-device-asset/src/main/java/io/sclera/models/User.java`

**Lines 39, 48, 56 — CONCAT_WS + LIMIT/OFFSET**
```sql
WHERE (?3 = 'null' or CONCAT_WS('' , u.name, u.email) LIKE CONCAT('%', ?3, '%'))
LIMIT ?1 OFFSET ?2
```
- `CONCAT_WS` — safe.
- `LIMIT/OFFSET` — already PostgreSQL style. **Safe.**

---

### 15. `sclera-cloud-device-asset/src/main/java/io/sclera/models/System_interface.java`

**Line 45 — VALUE() + ON DUPLICATE KEY**
```sql
INSERT INTO system_interface(interface_name, status) VALUE (?1, ?2)
ON DUPLICATE KEY UPDATE status = ?2
```
→
```sql
INSERT INTO system_interface(interface_name, status) VALUES(?1, ?2)
ON CONFLICT (interface_name) DO UPDATE SET status = EXCLUDED.status
```
> Conflict target: `interface_name` (appears to be the unique key based on context).

---

### 16. `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/AssetFieldQueryRepository.java` + similar

**ON DUPLICATE KEY + VALUES() reference shorthand (x9 files)**
Files: `AssetFieldQueryRepository`, `BuildingQueryRepository`, `ClientBarCodeQueryRepository`, `ClientNfcQueryRepository`, `ClientQrCodeQueryRepository`, `DeviceQueryRepository`, `DeviceTypeQueryRepository`, `DocumentQueryRepository`, `FloorQueryRepository`, `LocationQueryRepository`, `MediaQueryRepository`.

Pattern:
```sql
ON DUPLICATE KEY UPDATE name = VALUES(name), type = VALUES(type), ...
```
PostgreSQL:
```sql
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, type = EXCLUDED.type, ...
```
`VALUES(col)` is a MySQL-only shorthand that refers to the value that was proposed for that column in the INSERT. PostgreSQL uses `EXCLUDED.col` instead.

---

### 17. `sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceSearchService.java`

**Lines 1419-1438 — dynamically built SQL with IF + REGEXP_REPLACE + JSON_EXTRACT + JSON_UNQUOTE + CONCAT_WS**
```java
searchColumnValue.append("LOWER(CONCAT_WS('±','',d.id, IF(d.user_data_name IS NULL or ..., d.display_name, d.user_data_name),")
...
"COALESCE(IF(LOWER(REGEXP_REPLACE(JSON_EXTRACT(d.custom_fields, '$[*].*'), '[-.!...]', '')) ..."
...
".append("LOWER(CONCAT_WS('',JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(custom_fields,CONCAT(\"$[*].\",\"...")))
...
".append("REGEXP_REPLACE(")
```
This is a dynamically constructed native SQL string assembled in Java. All MySQL-specific constructs in the static parts need the same translations as above. Additionally:
- `JSON_UNQUOTE(JSON_EXTRACT(col, path))` → `col #>> path_array` or `jsonb_extract_path_text(col::jsonb, 'key')`
- `JSON_EXTRACT(col, '$[*].*')` — wildcard JSON path extraction. PostgreSQL jsonb does not have a direct equivalent for `$[*].*` wildcard; use `jsonb_array_elements(col) -> 'key'` or cast via `jsonb_path_query_array`.

> **FLAG FOR HUMAN REVIEW**: The dynamic SQL builder in `DeviceSearchService.java` (lines ~1419–1445) and `ManagedSoftwareSearchService.java` (lines ~237–254) is complex. After translating individual functions, the `$[*].*` wildcard JSON path and the `CONCAT_WS` with a `'±'` separator delimiter used for search normalization will need careful testing. These may be the most labor-intensive queries to port.

---

### 18. `sclera-cloud-device-asset/src/main/java/io/sclera/models/ApplicationUser.java` (NamedNativeQuery)

**Line 7 — ON DUPLICATE KEY**
```sql
INSERT INTO application_user (...) VALUES (...)
ON DUPLICATE KEY UPDATE technician_id = ?2, email = ?3, type = ?4
```
→
```sql
ON CONFLICT (id) DO UPDATE SET technician_id = EXCLUDED.technician_id, email = EXCLUDED.email, type = EXCLUDED.type
```

---

### 19. `sclera-vdms-service/src/main/java/io/sclera/vdms/repository/VdmsJpaRepository.java`

**Lines 16, 21, 26 — Simple portable queries**
```sql
SELECT * FROM vdms LIMIT 1
UPDATE vdms SET customer_org_id = :customerOrgId WHERE id = :vdmsId
UPDATE vdms SET address = :address WHERE id = :vdmsId
```
**No MySQL-specific constructs. Safe.**

---

## Hairy Queries (Flag for Human Review)

### H-1: `JSON_MERGE_PATCH` in DeviceRepository (lines 58, 171)
```sql
adc_json = JSON_MERGE_PATCH(adc_json, CAST(?35 AS JSON))
```
- `JSON_MERGE_PATCH` follows RFC 7396 (null values delete keys).
- PostgreSQL 17+ has `jsonb_merge_patch(a, b)` via the `jsonb_merge_patch` extension.
- For PG < 17: use the `jsonb` merge operator `a::jsonb || b::jsonb` — **but this is NOT RFC 7396 compliant** (it does not delete keys when the new value is null).
- Decision required: If RFC 7396 null-deletion semantics are needed, install the extension or write a PL/pgSQL helper. If not needed, `||` may suffice.

### H-2: `HAVING` alias reference in `Technician.getAllTechniciansByFilterByPagination` (Technician.java line 39)
```sql
HAVING (?6 = 'all' OR availability = ?6)
```
`availability` is a computed CASE alias in the SELECT list. MySQL allows HAVING to reference SELECT aliases; PostgreSQL does not. Fix: wrap in a CTE or subquery:
```sql
WITH base AS (
  SELECT ..., CASE WHEN ... END AS availability FROM technician t ...
  GROUP BY t.id, t.name, t.department
)
SELECT * FROM base
WHERE (?6 = 'all' OR availability = ?6)
ORDER BY name LIMIT ?2 OFFSET ?3
```

### H-3: `CONVERT_TZ` with runtime timezone string in Technician queries (Technician.java lines 37-41)
```sql
CONVERT_TZ(FROM_UNIXTIME(?1 / 1000), 'UTC', t.time_zone)
```
`t.time_zone` is a runtime column value (e.g. `'America/New_York'`). In PostgreSQL:
```sql
TO_TIMESTAMP(?1 / 1000.0) AT TIME ZONE 'UTC' AT TIME ZONE t.time_zone
```
This requires that `t.time_zone` stores IANA timezone identifiers (not MySQL-style offsets like `+05:30`). If the data currently contains non-IANA strings, a mapping table is needed.

### H-4: `$[*].*` JSON wildcard in DeviceSearchService dynamic queries
MySQL `JSON_EXTRACT(col, '$[*].*')` extracts all values at all paths in an array of objects. PostgreSQL has no single equivalent operator. Recommended rewrite using `jsonb_path_query_array`:
```sql
jsonb_path_query_array(col::jsonb, '$[*].*')
```
This is available in PostgreSQL 12+. Verify the PG target version.

### H-5: `condition` column backtick-quoting in TechnicianAvailability
The column is named `condition`. In PostgreSQL, `condition` is NOT a reserved word. However, to be safe, double-quote it: `"condition"`. Verify no other columns collide with PostgreSQL reserved words across the schema (not in scope of this task but note it).

---

## CONCAT_WS — Confirmed Safe

`CONCAT_WS(sep, col1, col2, ...)` is available in PostgreSQL since 9.1 with identical semantics (NULLs are skipped). No change required for these occurrences (found in 20+ query strings across Device, Location, Bacnet_Object, MeasuringInstrument, AiCallLog, Asset, Lorawan_Sensor, User, ManagedSoftware, GlobalQrcode, etc.).

## LIMIT / OFFSET — Confirmed Safe

All occurrences use `LIMIT n OFFSET m` form (not MySQL's two-arg `LIMIT m, n` form). These are already PostgreSQL-compatible.

## NOW() — Confirmed Safe

No occurrences of `NOW()` were found in the native query strings. MySQL-style `CURDATE()` was not found either.

## RAND() — Not Found

No occurrences found.

---

## Complete MySQL Construct Inventory

| Construct | Files affected | Count (approx.) | PostgreSQL equivalent |
|-----------|---------------|-----------------|----------------------|
| `ON DUPLICATE KEY UPDATE` | AssetDeviceMappingRepository, AssetRepository, ApplicationUserRepository, BuildingRepository, DeviceRepository, DeviceTechnicianAISuggestionRepository, DeviceTypesRepository, DocumentRepository, FloorRepository, LocationRepository, ManagedSoftwareRepository, MeasuringInstrumentRepository, MediaRepository, SystemInterfaceRepository, TechnicianAvailabilityRepository, TechnicianCertificateRepository, TechnicianRepository, TechnicianSkillRepository, VdmsDetailsRepository, models/ApplicationUser, models/System_interface, models/TechnicianAvailability, queryrepository/* | ~35 sites | `ON CONFLICT (...) DO UPDATE SET col=EXCLUDED.col` |
| `VALUES(col)` in ON DUPLICATE KEY | AssetFieldQueryRepository, BuildingQueryRepository, ClientBarCodeQueryRepository, ClientNfcQueryRepository, ClientQrCodeQueryRepository, DeviceQueryRepository, DocumentQueryRepository, FloorQueryRepository, LocationQueryRepository, MediaQueryRepository | ~10 files | `EXCLUDED.col` |
| `IFNULL(a, b)` | DeviceOnboardStatusRepository, DeviceRepository, LocationRepository, models/Bacnet_Object, models/MeasuringInstrument | ~35 occurrences | `COALESCE(a, b)` |
| `IF(cond, a, b)` | DeviceRepository, DeviceSearchService, models/Bacnet_Object, models/MeasuringInstrument | ~15 occurrences | `CASE WHEN cond THEN a ELSE b END` |
| `CONCAT_WS(sep, ...)` | 20+ files | ~50 occurrences | Safe — same in PostgreSQL |
| `REGEXP_REPLACE(str, pat, repl)` | models/Device, models/Location, DeviceRepository, DeviceSearchService, ManagedSoftwareSearchService | ~12 occurrences | `REGEXP_REPLACE(str, pat, repl, 'g')` — add `'g'` flag |
| `UNIX_TIMESTAMP()` | models/Device, models/Lorawan_Sensor, models/Technician | ~6 occurrences | `EXTRACT(EPOCH FROM NOW())::bigint` |
| `FROM_UNIXTIME(x)` | models/Technician | ~8 occurrences | `TO_TIMESTAMP(x / 1000.0)` |
| `DATE_FORMAT(d, fmt)` | models/Technician | ~4 occurrences | `TO_CHAR(d, 'Dy')` for `'%a'` format |
| `CONVERT_TZ(d, from, to)` | models/Technician | ~8 occurrences | `d AT TIME ZONE from AT TIME ZONE to` |
| `JSON_MERGE_PATCH(a, b)` | DeviceRepository | 2 occurrences | `jsonb_merge_patch(a,b)` (PG17+) or custom helper |
| `JSON_EXTRACT(col, path)` | MeasuringInstrumentRepository, DeviceSearchService | ~15 occurrences | `col->>'key'` or `jsonb_extract_path_text(col, 'key')` |
| `JSON_SET(col, path, val)` | MeasuringInstrumentRepository | ~5 occurrences | `jsonb_set(col, '{key}', to_jsonb(val))` |
| `JSON_CONTAINS(a, b)` | models/Technician | ~8 occurrences | `a::jsonb @> b::jsonb` |
| `JSON_QUOTE(str)` | models/Technician | ~4 occurrences | `to_jsonb(str)::text` |
| `JSON_UNQUOTE(JSON_EXTRACT(...))` | DeviceSearchService | ~4 occurrences | `col #>> path_array` |
| `CAST(x AS JSON)` | models/Technician, DeviceRepository | ~5 occurrences | `to_jsonb(x)` or `x::jsonb` |
| `VALUE(...)` (singular INSERT) | BuildingRepository, DeviceRepository, SystemInterfaceRepository, VdmsDetailsRepository, models/System_interface | ~8 occurrences | `VALUES(...)` |
| Backtick identifiers `` `condition` `` | models/TechnicianAvailability | 2 occurrences | Remove backticks; optionally use `"condition"` |
| `is_matched = 1` / `= 0` | AssetRepository | 2 occurrences | `= TRUE` / `= FALSE` (or keep integer if column is not boolean) |
| `HAVING alias` (computed alias in HAVING) | models/Technician | 1 occurrence | Wrap in CTE/subquery |
| `$[*].*` JSON wildcard path | DeviceSearchService | 1 occurrence | `jsonb_path_query_array(col, '$[*].*')` |
