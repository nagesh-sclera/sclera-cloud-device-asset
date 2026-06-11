# JPA Query Conversion Playbook

> Distilled from the AssetRepository + DeviceIPAddressRepository pilot (branch `feature/Query-JPA-update`, 2026-06-11).
> Use this when converting the remaining ~40 native-SQL files catalogued in `phase3-native-sql.md`.
> Spec: `docs/superpowers/specs/2026-06-11-assetrepository-jpa-query-conversion-design.md`
> Plan: `docs/superpowers/plans/2026-06-11-assetrepository-jpa-query-conversion.md`

## Goal
Replace native `@Query(nativeQuery=true)` / `@NamedNativeQuery` with portable JPA so queries survive the
MySQL→PostgreSQL move without per-query SQL translation, and gain compiler/Spring-Data validation.

## The three buckets — classify every query first

1. **Derived / scalar JPQL** — simple finders, counts, `EXISTS`. Convert the `@Query` body to JPQL keeping the
   existing method signature (existing names like `getTotalAssetCount` are NOT derived-method compatible, so use
   `@Query("...")` rather than renaming and touching callers). Examples:
   `@Query("SELECT COUNT(a) FROM Asset a")`, `@Query("SELECT CASE WHEN COUNT(a)>0 THEN true ELSE false END FROM Asset a")`.

2. **DTO projection JPQL (constructor expression)** — `@NamedNativeQuery` + `@SqlResultSetMapping` projections become
   `SELECT new fully.qualified.Dto(a.field1, a.field2, ...) FROM Entity a WHERE ...`. Pick the DTO constructor by the
   named query's `resultSetMapping` — **read each one; do not assume** (e.g. `getAllAssets` used `subsystemAssetMapping`,
   a different constructor than its siblings).

3. **Write via `save()` / `saveAll()`** — non-portable upserts (`ON CONFLICT` / `ON DUPLICATE KEY`) and inserts move out
   of the repository into the service as entity persistence. Preserve the EXACT conflict semantics (see below).

## Step-by-step recipe (per repository)

1. **Build the entity field→column map first.** JPQL references the **entity field name**, which is often NOT the column
   name. In this codebase fields are mostly snake_case (`display_name`, `subsystem_parent_id`, `import_type`) but a few
   are camelCase with explicit `@Column` (`originalKeys`→`original_keys`, `customFields`→`custom_fields`,
   `matchedProductIds`→`matched_products`, `isMatched`→`is_matched`). Get these exact or JPQL fails to parse.
2. For each native method, open its source (inline `value=` OR the `@NamedNativeQuery` in the entity) and translate the
   predicate **semantically**, not textually.
3. Convert, run the IT, iterate.
4. After all consumers are converted, delete the now-dead `@NamedNativeQuery` / `@SqlResultSetMapping` from the entity —
   but **grep first** (`<mappingName>`, `createNamedQuery("Entity.`, `@Query(name=...)`) to prove nothing else uses them.

## Translation cheatsheet (MySQL native → JPQL)

| Native | JPQL |
|---|---|
| `is_matched = 0` / `= 1` | `a.isMatched = false` / `true` |
| `IFNULL(x, y)` | `COALESCE(x, y)` |
| `IF(c, a, b)` | `CASE WHEN c THEN a ELSE b END` |
| `CONCAT_WS('', col1, col2)` | `CONCAT(COALESCE(col1,''), COALESCE(col2,''))` — **must COALESCE** (see gotcha) |
| `... NOT IN (SELECT asset_id FROM asset_device_mapping)` | `... NOT IN (SELECT m.asset.id FROM AssetDeviceMapping m)` (navigate relations, not columns) |
| `LIMIT ?x OFFSET ?y` | drop the params, add a trailing `Pageable` arg (JPQL has no LIMIT) |
| `NULL as match_score` in a ctor expr | `CAST(NULL AS integer)` (typed NULL so Hibernate resolves the constructor) |
| `RIGHT JOIN ... WHERE a.id IN (...)` | an INNER `JOIN` is equivalent when the WHERE already requires the row to exist |

## Gotchas this pilot actually hit

- **CONCAT_WS null-skip:** `CONCAT_WS('', a, b)` silently skips NULLs; plain JPQL `CONCAT(a, b)` can propagate NULL and
  drop the row. Wrap each arg in `COALESCE(col, '')` to preserve behavior. (A NULL-column row that matched before would
  otherwise vanish — and a happy-path test with non-null seed data will NOT catch it; add a NULL-column regression test.)
- **`LIMIT/OFFSET` → `Pageable`:** changes the method signature. Safe here because the projection methods had **zero
  production callers** (verify with `git grep '\.methodName('`); if callers exist, update them to `PageRequest.of(...)`.
- **Constructor-expression resolution:** Hibernate matches the `new Dto(...)` by arg count + types. A bare `NULL` is
  ambiguous; use `CAST(NULL AS integer)`. Mismatched arg count fails at context load.
- **Upsert → find-or-create semantics:** the original `ON CONFLICT (id) DO UPDATE SET a, b, c` updates ONLY those columns
  on an existing row. A naive `save()` overwrites everything. Faithful rewrite: `findById`; if present set ONLY a,b,c;
  else build a new entity with all the insert defaults; then `save()`. Cover BOTH paths with tests (the conflict path must
  assert untouched fields stay untouched).
- **Setting a `@ManyToOne` FK by id without a JpaRepository:** `Asset.vdms` / `Device_IP_Address.device` are entity
  relations, not scalar ids. Set them with `entityManager.getReference(Target.class, id)` (a lazy proxy that writes the FK
  on flush). `VdmsRepository` here is a NON-JPA stub — don't expect `getReferenceById` on it; `DeviceRepository` IS a
  `JpaRepository<Device,String>` so `deviceRepository.getReferenceById(id)` works.
- **Service read-modify-write must be `@Transactional`:** find-or-create (`findById` + `getReference` + `save`) needs a
  single transaction so the loaded entity stays managed and `getReference` has a valid persistence context (else
  `TransactionRequiredException` / detached-proxy surprises in production). Annotate the service method.
- **Bulk `@Modifying` + stale reads:** bulk JPQL UPDATE/DELETE bypass the persistence context. In a `@Transactional` test,
  a later `findById` can read stale L1 cache. Add `@Modifying(clearAutomatically = true)` to those methods.
- **AND/OR precedence:** `... = ?1 AND ?2 = 'null' OR CONCAT(...) LIKE ...` (unparenthesized) is a latent bug — the JPQL
  rewrite is the moment to parenthesize: `... = ?1 AND (?2 = 'null' OR ... LIKE ...)`.

## Verification harness (the proof, not Mockito)

- Result-asserting IT under `io.sclera.it.*`, extending `PostgresJpaIT` (real PostgreSQL 16 via Testcontainers,
  `@ServiceConnection`/`@DynamicPropertySource`). `@Sql` loads `/schema-pg.sql` (class), a `/seed/*.sql` (method),
  `/cleanup-*.sql` (method). Assert the **new repository method's** returned DTO/entity against seeded data.
  Mockito mocks the repository and CANNOT validate query semantics — use it only for service-layer wiring (e.g. that
  find-or-create calls `save()` with the right entity).
- **Enable only the repos under test:** `JpaTestConfig` omits `@EnableJpaRepositories` globally (other entities carry
  MySQL `ON DUPLICATE KEY` `@NamedNativeQuery` that break bootstrap). Add a SCOPED
  `@EnableJpaRepositories(... includeFilters = ASSIGNABLE_TYPE {RepoA.class, RepoB.class})` for just the repos you convert.
- **Full-entity loads need their eager-association tables in `schema-pg.sql`.** Loading/saving a full `Asset` eagerly joins
  `vdms` → `building`; loading a `Device_IP_Address` eagerly joins `device`. Constructor-expression PROJECTIONS do NOT load
  associations (so they don't need those tables), but any `save()`/`findById` of the full entity does — add the missing
  tables (this pilot added `device`, `building`, `device_ip_address`).
- **Build/run on this machine:** from the nested module dir
  `sclera-cloud-device-asset/sclera-cloud-device-asset`, set `$env:JAVA_HOME='C:\Users\DhanushVasanth\.jdks\corretto-21.0.8'`
  (PATH java is 17 — wrong) before each `.\mvnw.cmd` call; `dapr-commons` must be `install`ed to `.m2` first. Run one IT
  class: `.\mvnw.cmd -q -Dtest=<Class>IT test`. Logback `NumberFormatException`/appender WARN/ERROR at startup is
  pre-existing noise — judge by `Tests run: N, Failures: 0, Errors: 0` and exit 0.

## Lessons from the LocationRepository pass (2nd repo)
- **The phase3 audit undercounts.** LocationRepository was listed as "~8 queries" but is ~60 methods. Re-scope by reading
  the actual file before estimating: a large fraction (JSONArray-`IN` params, multi-join `'all' IN ?n` dynamic filters,
  `CASE WHEN` tagging conditions) have no portable JPQL form — leave them native and add a
  `// NOT CONVERTED — stays native (PG-translation track): <reason>` comment so the next maintainer knows it was a
  deliberate skip, not an oversight.
- **JPQL conversion surfaces pre-existing broken native SQL.** `getLocationsByFloorId`'s original query selected
  `f.name, b.*` but its FROM clause had no join defining `f`/`b` — invalid SQL that throws at runtime on MySQL and PG
  alike. You literally cannot write the JPQL without deciding the join, which forces the latent bug into the open. When
  this happens, restore the obvious intent (here: the joins its working paginated sibling already had), and document it
  in the method Javadoc as a deliberate bugfix — don't reproduce a query that never worked.
- **Entity relations vs scalar columns in JPQL:** `Location.floor` is a `@ManyToOne` (column `floor_id`). JPQL must
  navigate `l.floor.id` (NOT `l.floor_id`); a bulk `UPDATE ... WHERE l.floor.id = ?1` works. Reaching a grandparent is a
  path chain: `l.floor.building.vdms.id`. Verify each hop's field name in the entity before writing it.
- **Don't `findById` a heavily-associated entity in an IT.** Loading a full `Location` drags in a deep eager chain
  (floor→building→vdms, GlobalQrcode, Device…) whose tables aren't in the minimal test schema. Assert converted writes
  via scalar JPQL reads (`SELECT l.field FROM Location l WHERE l.id=...`) or the converted projection methods instead.
- **String-returning count/LIMIT-1 methods:** preserve the original return type with a `default` wrapper over a
  `Long`/`List` backing query rather than silently changing the signature (`getLocationsCountByFloorId`,
  `getLocationIdbyLocationName`).

## Lessons from the AssetDeviceMappingRepository pass (3rd repo)
- **Not every upsert should become `save()`. An `ON CONFLICT` that's already valid PostgreSQL may be best left
  native.** `saveNewAssetMapping`'s `INSERT ... ON CONFLICT (id) DO UPDATE SET match_score=EXCLUDED.match_score` is
  already PG-portable; the only gain from converting is type-safety, and it isn't worth the cost below. Leave it native
  with a `// NOT CONVERTED — stays native` comment explaining why.
- **Assigned `@Id` + `save()` = `merge()` = SELECT-before-insert.** Spring Data treats an entity whose `@Id` is already
  set as non-new, so `save()` routes to `merge()`, which issues a SELECT to load the row first. If the entity has eager
  `@ManyToOne`s (here `asset` and `device`, and `Device` drags in a huge eager graph: `device_onboard_status`, `docker`,
  `global_qrcode`, …), that SELECT materialises the whole graph just to write one column — a real perf regression and it
  explodes in the minimal test schema. `findById`-then-`save` is even worse (two loads). For a genuine find-or-create on
  an assigned-id entity, prefer `existsById` + a targeted bulk `@Modifying UPDATE` over `save()`/`merge`.
- **Hibernate 7 will not bind a JPA entity to a native-query parameter** ("Could not resolve NativeQuery parameter type").
  Native `@Query` methods that took `Asset`/`Device` entity params (to land in FK columns) must take scalar id params
  instead — the columns are plain FKs anyway. Safe to change the signature when the method has no callers.
- **`m.asset.id` path reads are cheap; `findById(entity)` is not.** Selecting an association's id (`SELECT m.asset.id`)
  resolves to the FK column with no join/materialisation. Use that in ITs instead of loading the owning entity.

## Lessons from the 10-repo batch (small/medium CRUD repos)
- **`@Lob String` fields break full-entity loads on Hibernate 7 / PostgreSQL** (`ClobJdbcType` → "Bad value for type long"). Any method/derived-finder that materialises such an entity, or a JPQL scalar projection of the Lob column, fails. Keep those specific reads native (read the raw TEXT column) and in ITs null-seed the Lob columns / use scalar reads. (Seen in DeviceNetworkSpecification, DeviceSpecification.)
- **Entity field type ≠ column type ⇒ keep native.** Several entities map a `String` field over a `BOOLEAN`/`INTEGER` column (e.g. `AiCallLog.isCompleted` is `String` over a `BOOLEAN` column). A JPQL `SET`/predicate with the mismatched Java type fails type resolution — leave those native and flag the entity mapping as the real bug to fix later (fixing the field type then unlocks the JPQL conversion).
- **Scalar `@Column` FK vs relation FK.** `UPDATE ... SET device_id = ?` only stays native when `device` is a `@ManyToOne`/`@OneToOne` relation (JPQL can't set a relation from a bare id). If `device_id` is a plain scalar `@Column` field, it converts normally (`SET x.deviceId = ?`). Check the entity before assuming.
- **Empty / derived-only repositories need nothing.** Some repos (e.g. AddressRepository) are pure `JpaRepository` with no `@Query` — already portable, skip them. Derived methods (`findByDeviceId`, `deleteByDeviceId`, `existsBy...`) are likewise left as-is everywhere.
- **Conversion keeps surfacing pre-existing latent bugs** (queries referencing non-existent columns like `device_lifecycle_history.assigned_user_email`, stub DTOs with too-few ctor args like `CallStatusDTO`). When a method can't convert because the underlying query/DTO is already broken, leave it native with a `// NOT CONVERTED` note explaining the defect rather than papering over it.
- **Shared test files are the serialization point.** Every repo adds itself to `JpaTestConfig`'s scoped `@EnableJpaRepositories` and appends its table(s) to `schema-pg.sql`. Run repo conversions that touch these **sequentially** (not parallel) to avoid edit races.

## Pilot residuals / known follow-ups (non-blocking)
- **`AssetRepository.getFilteredAssets`** keeps an unused `filter` param and applies no `ORDER BY`. The original was
  `ORDER BY ?1` — a *bind parameter*, i.e. SQL ordering by a constant literal (no-op, since a column name can't be a bind
  param), so dropping it is behavior-preserving. If a real dynamic sort is ever wanted, pass a `Sort` via `Pageable` and
  remove the dead `filter` param. No production caller today.
- **IP-address insert** (`DeviceService.persistDeviceIpAddress`) is covered transitively, not by a direct `save()` IT.
  A 1-line `repo.save(...)` + read-back IT would make it symmetric with the asset-upsert coverage.
- **Uncalled converted methods:** most `AssetRepository` projection methods have no production call site in this
  extracted seed module — their correctness rests entirely on the IT suite. Keep the ITs green; they are the only guard.

## What stays native (hand to the PG-translation track, not JPQL)
JSON functions (`JSON_MERGE_PATCH`, `JSON_EXTRACT`/`JSON_SET`, `JSON_CONTAINS`), timezone math
(`CONVERT_TZ`/`UNIX_TIMESTAMP`/`FROM_UNIXTIME`/`DATE_FORMAT`), `HAVING`-on-alias, and dynamically-assembled SQL
(`DeviceSearchService`). These have no portable JPQL form — convert them to PG-syntax native SQL instead (see
`phase3-native-sql.md`).
