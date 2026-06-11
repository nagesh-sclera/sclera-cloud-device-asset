# AssetRepository JPA Query Conversion — Pilot Design

> Date: 2026-06-11
> Branch: `feature/Query-JPA-update`
> Scope: `sclera-cloud-device-asset` only
> Status: approved design, pre-implementation

## Goal

Convert native SQL queries to portable JPA (Spring Data derived methods + JPQL),
serving two equally-weighted drivers:

1. **Database portability** — queries become engine-agnostic and survive the
   MySQL → PostgreSQL migration without per-query SQL translation.
2. **Maintainability / type-safety** — string SQL gives way to compiler- and
   Spring-Data-validated query methods.

This is a **pilot**: convert two repositories end-to-end, prove the patterns with
the existing PostgreSQL integration-test harness, and produce a repeatable
**conversion playbook** for the remaining ~40 files catalogued in
`migration-notes/phase3-native-sql.md`.

## Pilot targets

- `io.sclera.Repository.AssetRepository` (~25 queries — central; exercises every
  pattern: derived methods, JPQL projections, `@Modifying` updates/deletes, an
  upsert, and `@SqlResultSetMapping`/`@NamedNativeQuery` DTO projections).
- `io.sclera.Repository.DeviceIPAddressRepository` (3 queries — proves the simple
  path including an insert).

## Conversion taxonomy — three buckets

Every query in the two repositories is classified into exactly one bucket.

### Bucket 1 — Derived query methods (no SQL)

Simple finders, counters, and deleters become Spring Data method names. The
`@Query` annotation is removed entirely.

| Current | Becomes |
|---------|---------|
| `getTotalAssetCount()` | `count()` (built-in) |
| `getParentAssetSubsystemCount(parentId)` | `countBySubsystemParentId(String)` |
| `getSubAssetIdByParentId(parentId)` | `findIdBySubsystemParentId(String)` returning `Set<String>` |
| `getSubsystemParentIdByAssetId(id)` | `findSubsystemParentIdById(String)` |
| `deleteAllById(ids)` | built-in `deleteAllById(Iterable)` |
| `checkImportExists()` | `existsBy…` / `count() > 0` |

### Bucket 2 — JPQL `@Query` (constructor expressions for DTOs)

`AssetDTO` projections move off `@NamedNativeQuery` + `@SqlResultSetMapping` onto
JPQL constructor expressions:

```java
@Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
       "a.id, a.displayName, a.description, a.macAddress, a.model, a.vendor, " +
       "a.type, a.ipAddress, a.networkLayer, a.serialNumber, a.warranty, " +
       "a.originalKeys, a.customFields, a.matchedProducts, a.subsystemParentId, " +
       "a.importType) " +
       "FROM Asset a WHERE a.importType = :importType " +
       "ORDER BY a.displayName")
List<AssetDTO> getPaginatedAssets(...);
```

This **deletes** the corresponding `@SqlResultSetMapping(name="filteredAssetMapping")`
and the per-method `@NamedNativeQuery` blocks from `Asset.java` once all consumers
are migrated. `@Modifying` JPQL covers portable writes: `saveMatchedProductsById`,
`updateProductId`, `setMatched`, `updateSubsystemParentId`,
`updateSetOfSubsystemParentId`, `updateParentAssetSubsystemCount`,
`setAllAssetsToUnMatched`, `deleteAllRecords`, `deleteAllMatchedRecords`,
`updateTypeByType`, `setTypeGeneric`, `updateDeviceType`, `getUniqueDeviceTypes`
(DISTINCT projection).

Projections that join `asset_device_mapping` — `getLinkedAssets`,
`getUnmappedAssets`, `getUnmappedAssetsByIds`, `getUnmappedMatchedAssets` — become
JPQL joins/sub-selects against the `AssetDeviceMapping` entity. Still portable.

### Bucket 3 — Write via `save()` / `saveAll()` (service layer)

Non-portable upserts/inserts move out of the repository into the service as entity
persistence.

- `AssetRepository.assetUpsert(...)` → find-or-create in the service.
- `DeviceIPAddressRepository.insertIPAddressByDeviceId(...)` → entity `save()`.

**⚠ Semantic preservation (critical).** The current `assetUpsert` is:

```sql
INSERT INTO asset(... 19 columns ...) VALUES(...)
ON CONFLICT (id) DO UPDATE SET display_name=EXCLUDED.display_name,
                               description=EXCLUDED.description,
                               type=EXCLUDED.type
```

On conflict it updates **only** `display_name`, `description`, `type`. A naive
`save()` would overwrite all 19 columns on an existing row. The faithful rewrite
is **find-or-create**:

- If the row exists: set only `displayName`, `description`, `type`, then `save()`.
- Otherwise: persist a new `Asset` with all supplied fields.

This semantic is locked in by dedicated insert-path and conflict-path tests.

## Prerequisite — entity field coverage

Constructor-expression JPQL can only reference **mapped** entity fields.
**Implementation step 1** is auditing `Asset.java` to confirm every projected
column maps to a field. Columns sourced from joins (`match_score` from
`asset_device_mapping`) or computed (`NULL AS match_score`) are handled in the
JPQL itself (join, sub-select, or literal). Any column with no entity field is
**flagged as a carve-out before conversion** — never silently dropped.

## What stays native

No native query is expected to remain in these two repositories after the
upsert/insert moves to `save()`. The hairy queries (JSON, timezone, dynamic SQL)
live in other files and remain owned by the PG-translation track. If the
entity-coverage audit surfaces a column-level blocker, that single query is
documented as a carve-out rather than worked around.

## Verification

Each converted query gets a result-asserting integration test in `io.sclera.it.*`
against **real PostgreSQL**, following the existing `TechnicianAvailabilityTest`
pattern (`@SpringBootTest(classes = JpaTestConfig.class)`, `@ServiceConnection`,
`schema-pg.sql` + seed SQL, AssertJ assertions). Unlike Mockito unit tests — which
mock the repository and cannot validate query semantics — this harness executes
the **new repository method** and asserts the returned `AssetDTO`/entity matches
pre-conversion behavior.

- Each Bucket-1/Bucket-2 query: one seed-and-assert test.
- The upsert → `save()` rewrite: two tests — insert path, and conflict path
  asserting only the three columns change.

## Out of scope

- All other repositories.
- Hairy queries: `JSON_MERGE_PATCH`, timezone (`CONVERT_TZ`/`UNIX_TIMESTAMP`),
  and `DeviceSearchService` dynamic SQL.
- Any schema change.

## Deliverable

A converted `AssetRepository` + `DeviceIPAddressRepository`, their service-layer
`save()` rewrites, a green IT suite, and a short conversion playbook (the three
buckets + the entity-coverage check + the verification recipe) for the later repos.
