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

## What stays native (hand to the PG-translation track, not JPQL)
JSON functions (`JSON_MERGE_PATCH`, `JSON_EXTRACT`/`JSON_SET`, `JSON_CONTAINS`), timezone math
(`CONVERT_TZ`/`UNIX_TIMESTAMP`/`FROM_UNIXTIME`/`DATE_FORMAT`), `HAVING`-on-alias, and dynamically-assembled SQL
(`DeviceSearchService`). These have no portable JPQL form — convert them to PG-syntax native SQL instead (see
`phase3-native-sql.md`).
