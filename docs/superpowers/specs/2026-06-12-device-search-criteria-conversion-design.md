# Device Search Criteria Conversion — Design

**Date:** 2026-06-12
**Branch:** `feature/Query-JPA-update`
**Status:** Approved design, pending implementation plan

## Problem

`DeviceSearchService.multipleKeywordSearchSortFilterDevices` (and its two siblings
`multipleKeywordSearchSortFilterDevicesForAssetExport` and `multipleKeywordSearchSortFilterDevicesCount`)
build a large dynamic SQL string by concatenation and run it through `JdbcTemplate`. A skeleton SELECT over
`device` with 11 LEFT JOINs is combined at runtime with fragments from ~8 generator methods driven by the
request JSON (column filters, feature filters, multi-keyword search, custom-field jsonb predicates, sort,
device-id filter). Problems:

- **No type safety:** field/column references are strings; renames and entity changes break silently at runtime.
- **SQL injection:** all values (search terms, filter values, custom-field keys, device ids) are inlined into
  the SQL, not bound.
- **Latent bugs:** `does_not_contain` and `not_equal_to` for custom-field search generate the same SQL as
  their positive counterparts; the device-ids filter emits double-quoted literals (`d.id IN ("abc")`), which
  is identifier syntax in PostgreSQL and errors at runtime.
- **Maintainability:** ~500 lines of `StringBuilder` logic; the migration playbook had classified this as
  "stays native" because static JPQL cannot express it — but dynamic JPA (Criteria API) can.

Goal (user-confirmed): modernize to type-safe, maintainable JPA. Scope: **all three methods**. Policy:
**fix obvious bugs, document each fix**, preserve the existing workflow (id-projection query →
`getDevicesByIdList` → fuzzy re-ranking in Java).

## Approach (chosen: A)

JPA Criteria API via an `EntityManager`-based query builder, no new dependencies. Rejected: QueryDSL
(new annotation-processor dependency, shaky upstream maintenance) and jOOQ (whole new query stack,
diverges from the JPQL track established on this branch).

## Components

### 1. `DeviceSearchCriteria` (new, `io.sclera.dto`)

Typed request object, parsed once from the incoming fastjson `JSONObject` + `condition` string + paging via a
static factory `from(String condition, JSONObject searchSortFilterDetails, Integer onboardStatus)`. All
fastjson handling lives here; query code never touches JSON.

Fields:
- Nullable typed status fields resolved from the `condition` switch: `monitor`, `status`,
  `virtualDeviceType`, `assignedStatus`, `assetMatchStatus`, `onboardStatus`. The current `123`/`210`
  sentinel values become parse-time logic (null = no predicate; `notonboarded` becomes an explicit
  "onboard_status != 3" flag) and never reach SQL.
- `vdmsId`, `dockerName` scope values (with their existing `'null'` / `'all'` wildcard semantics resolved at
  parse time into nullable fields).
- `List<ColumnFilter>` (column, custom flag, condition `is_present`/`is_not_present`, optional value).
- `List<FeatureFilter>` (name, condition).
- `List<KeywordSearch>` (column nullable = search-all, custom flag, value, condition enum
  `CONTAINS`/`DOES_NOT_CONTAIN`/`EQUAL_TO`/`NOT_EQUAL_TO`/`STARTS_WITH`/`ENDS_WITH`, defaulting to contains).
- Optional `SortSpec` (column, custom flag, direction handling as today).
- Optional `List<String> deviceIds`.

### 2. `DeviceSearchQueryBuilder` (new, `io.sclera.queryrepository`)

`EntityManager`-based Criteria builder. Public API:
- `List<String> findIds(DeviceSearchCriteria c, int pageNo, int pageSize)`
- `List<String> findAllIds(DeviceSearchCriteria c)` (asset-export variant, no pagination)
- `long count(DeviceSearchCriteria c)`

All three share one private predicate-assembly method; they differ only in projection and pagination.

**Query shape** mirrors the original to preserve semantics: outer `CriteriaQuery<String>` on `Device`
selecting `d.id`, joining `location → floor → building` (LEFT) only when the sort needs them, with
`d.id IN (subquery)`; the subquery carries all filter/search predicates and joins. This preserves the
original dedupe-then-sort behavior (multi-row LEFT JOINs in the inner query cannot duplicate outer rows,
and ORDER BY needs no DISTINCT interaction).

**Predicate mapping:**
- Condition switch → typed predicates added only when the criteria field is non-null
  (`cb.equal(d.get("monitor"), 1)` etc.). The SQL `(123 = 123 OR CASE ...)` constant tricks disappear.
- `assigned`/`unassigned` keeps its current semantics on `assigned_user_email` (NULL-or-'null' vs not).
- Column filters: standard columns via metamodel paths. The `display_name`/`vendor`/`model`
  user-data-fallback (`CASE WHEN user_data_x IS NULL OR '' THEN x ELSE user_data_x END`) becomes one shared
  `cb.selectCase` helper used by filters, search, and sort alike (one definition instead of five copies).
  Custom-field filters via `custom_field_text(...)` preserving the `IS NOT NULL AND <> '' AND <> 'null'`
  triple (and its negation).
- Feature filters: qrcode/barcode/nfc presence → `EXISTS` subqueries on `QrCode`/`ClientQrCode`/
  `ClientBarCode`/`Nfc`/`ClientNfc` (correlated on device id); `geolocation/image/field/tag_status` → join
  through the `device_onboard_status` association; `adc`, count-based and sensor-alert filters → scalar
  predicates on `d`.
- Keyword search: search-all rebuilds the haystack faithfully — `LOWER(CONCAT_WS('±', ...))` over the same
  column list (including `dos.assignee_email`, `dosa.email` via association join from `dos`,
  `ds.username`/`ds.email` via association or Hibernate ad-hoc entity join, `l.name`/`f.name`/`b.name`),
  wrapped in `strip_specials`, compared with the same `±`-sentinel LIKE patterns per condition. Per-column
  and custom-field search reuse the same condition-to-pattern mapping. Search values are bound, with LIKE
  wildcards (`%`, `_`, escape char) escaped.
- Device-ids filter → `d.id IN :ids` (bound).
- Sort: custom-field sort via `custom_field_text` with the null-last/empty-last triple; `ip_address` via
  `inet_val`; timestamp columns keep `DESC, d.id` tiebreak; default `ORDER BY (updated_timestamp IS NULL),
  updated_timestamp DESC, id`.

### 3. `ScleraPgFunctionContributor` (new)

Hibernate 7 `FunctionContributor` registered via `META-INF/services/org.hibernate.boot.model.FunctionContributor`,
defining pattern-based SQL functions usable from Criteria (`cb.function`) and HQL:

| Function | SQL pattern | Used for |
|---|---|---|
| `custom_field_text(col, path)` | `jsonb_path_query_first(?1::jsonb, ?2::jsonpath) #>> '{}'` | custom-field filter/search/sort |
| `custom_field_array_text(col, path)` | `jsonb_path_query_array(?1::jsonb, ?2::jsonpath)::text` | search-all custom-fields haystack |
| `strip_specials(x)` | `REGEXP_REPLACE(?1, '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}\|\\]', '', 'g')` | search normalization (char class copied verbatim from the existing Java `replaceAll` so SQL-side and Java-side stripping stay identical) |
| `inet_val(x)` | `?1::inet` | numeric IP sort |

The jsonpath string (`$[*]."key"`) is built in Java with quote-escaping of the key and **bound as a
parameter** (PG casts the bound text via `::jsonpath`), never concatenated into SQL.

### 4. `DeviceSearchService` rewiring

The three public methods keep their exact signatures and surrounding workflow:
parse → builder call → `deviceService.getDevicesByIdList(vdmsid, ids)` → existing fuzzy re-ranking when a
search exists without an explicit sort → same return types (`Set<DeviceDTO>` / `String` count).
The family-private generators (`generateMultipleKeywordSearchAndFilterCustomQuery`,
`generateFilterCustomQuery`, `generateColumnFilterQuery`, `generateSearchQuery`,
`generateConditionedQueryForAll`, `generateConditionedQueryForCustomFields`, `generateConditionedQuery`,
`generateSortQuery`, `generateDeviceIdsFilterCustomQuery`, `generateFeatureFilterQuery`) are deleted after a
grep proves the three converted methods were their only callers. `updateDeviceSearchColumnName` **stays**
(used by other, out-of-scope methods at lines ~340/587/727).

## Documented bug fixes (fix + javadoc note + migration-notes entry)

> CORRECTION (2026-06-12, plan-research): the originally-suspected "custom-field `does_not_contain` ==
> `contains`" bug is NOT a bug. The inner custom-fields CASE deliberately uses the POSITIVE pattern — it
> injects the search term into the search-all haystack when the custom fields match, and the OUTER
> condition pattern applies the polarity (NOT LIKE for does_not_contain etc.). The conversion must
> reproduce this inner-positive/outer-polarity structure faithfully.

1. **Device-ids filter** emitted double-quoted literals (`d.id IN ("abc")`) — PostgreSQL identifier syntax,
   runtime error today. Fixed by bound `IN` parameters.
2. **SQL injection** throughout — fixed inherently by bind parameters.
3. **`REGEXP_REPLACE` missing `'g'` flag** (PG port artifact): PostgreSQL strips only the FIRST special
   character, while MySQL (and the paired Java `replaceAll`) strips ALL — so any search value/column text
   with 2+ special characters mismatches today. The new `strip_specials` uses `'g'`.
4. **Timestamp sort crash**: sorting by `created_timestamp`/`updated_timestamp` generates `bigint = ''`,
   a PostgreSQL type error at runtime. The rewrite omits the `= ''` sort key for numeric columns.
5. **Sort by `assignee_email`/`username`/`email` crash**: the generated outer query references `dos.`/`ds.`
   aliases that the outer SELECT never joins — SQL error today. The rewrite joins what the sort needs.
6. **Condition-set inconsistency**: the count method supports `onboardpending`/`onboardcompleted` but the
   paged/export methods silently ignore them (count filtered, page unfiltered). Unified: all three accept
   the full condition set via the shared `DeviceSearchCriteria.from`.

Anything else found mid-implementation follows the same policy: obvious self-contradicting behavior is
fixed and documented; questionable semantics are preserved and flagged.

## Error handling

Contract unchanged: the three service methods keep their catch-all (`return null` on failure — callers
depend on it; the count method returns its current error value). The builder throws normally; parse errors
in `DeviceSearchCriteria.from` surface as today (caught by the service wrapper).

## Testing

- **`DeviceSearchQueryBuilderIT`** extends the existing `PostgresJpaIT` Testcontainers harness (real
  PostgreSQL 16). Seeded devices cover:
  - each `condition` value (all/unmonitored/online/offline/other/matched/unmatched/verified/archived/
    onboarded/notonboarded/assigned/unassigned),
  - custom-field column filter present/absent (including the `'null'`-string arm),
  - the inner-positive/outer-polarity semantics of search-all over custom fields (`does_not_contain` /
    `not_equal_to` exclude devices whose custom fields match — preserved behavior),
  - regression tests for the documented fixes (multi-special-char search values, timestamp sort,
    assignee_email/username sort),
  - search-all with special characters and `±` boundary semantics (`equal_to` vs `contains` vs
    `starts_with`/`ends_with`),
  - per-column and custom-field keyword search,
  - feature filters (EXISTS paths: qrcode/barcode/nfc; dos status paths; scalar paths),
  - custom-field sort order (non-null → empty → null last), `ip_address` `::inet` sort, default sort,
  - pagination + count consistency (same criteria: count == total ids; page slicing correct),
  - device-ids filter.
- `schema-pg.sql` gains any missing tables: `qr_code`, `client_qr_code`, `client_bar_code`, `nfc`,
  `client_nfc`, `device_onboard_status`, `device_onboard_status_assignee`, `device_specification`
  (several already exist from `DeviceRepositoryIT`).
- **`DeviceSearchCriteriaTest`** — pure unit test of JSON parsing (no DB): condition resolution, sentinel
  elimination, filter/search/sort extraction, defaults.
- Existing service-level Mockito tests remain green (method signatures unchanged).

## Out of scope

- Other `DeviceSearchService` methods (single-keyword search, sort-only, filter-only families) and
  `updateDeviceSearchColumnName` — same modernization can follow later using the same builder/functions.
- The fuzzy-ranking post-processing (unchanged Java).
- `getDeviceInfoByCustomFields` (separate method, separate decision).
- Behavior of preserved oddities (e.g. `±` sentinel matching semantics) — kept as-is.
