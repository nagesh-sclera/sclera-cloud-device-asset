# AssetRepository JPA Query Conversion — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the native SQL in `AssetRepository` and `DeviceIPAddressRepository` to portable JPA (JPQL `@Query` + entity `save()`), proven against the existing PostgreSQL integration-test harness, producing a reusable conversion playbook.

**Architecture:** Three buckets (per the spec): most reads/writes become JPQL `@Query` keeping the *existing method signature* (minimal caller impact); `AssetDTO`/`DeviceIPAddressDTO` projections use JPQL constructor expressions in place of `@NamedNativeQuery` + `@SqlResultSetMapping`; the two non-portable write paths (`assetUpsert`, `insertIPAddressByDeviceId`) move to the service layer as entity `save()` with FK relations set via `getReferenceById(...)`. Each converted query is verified by a result-asserting test in `io.sclera.it.*` against real PostgreSQL.

**Tech Stack:** Spring Data JPA, Hibernate 7, Spring Boot 4, JUnit 5 + AssertJ, PostgreSQL (Testcontainers via `@ServiceConnection`), Maven (`./mvnw`).

---

## Reference data (verified from source — use throughout)

### Asset entity field → column map (`io.sclera.models.Asset`)

JPQL references the **field name** (left column). Note most are snake_case.

| Entity field | DB column | Type |
|---|---|---|
| `id` | `id` | String |
| `display_name` | `display_name` | String |
| `description` | `description` | String |
| `mac_address` | `mac_address` | String |
| `model` | `model` | String |
| `vendor` | `vendor` | String |
| `type` | `type` | String |
| `ip_address` | `ip_address` | String |
| `network_layer` | `network_layer` | Integer |
| `serial_number` | `serial_number` | String |
| `warranty` | `warranty` | String |
| `import_type` | `import_type` | String |
| `isMatched` | `is_matched` | Boolean |
| `subsystem_parent_id` | `subsystem_parent_id` | String |
| `subsystem_count` | `subsystem_count` | Integer |
| `originalKeys` | `original_keys` | String |
| `customFields` | `custom_fields` | String |
| `matchedProductIds` | `matched_products` | String |
| `vdms` | `vdms_id` (FK) | `@ManyToOne Vdms` |
| `assetDeviceMappings` | (inverse) | `@OneToMany List<AssetDeviceMapping>` (`mappedBy="asset"`) |

`AssetDeviceMapping` fields: `id`, `asset` (`@ManyToOne Asset`), `device` (`@ManyToOne Device`), `matchScore` (Integer, column `match_score`).

### AssetDTO constructors (target of constructor expressions) — `io.sclera.dto.touchscreen.assetmapper.AssetDTO`

- **C17** (paginated/filtered/linked/unmapped): `(String id, String display_name, String description, String mac_address, String model, String vendor, String type, String ip_address, Integer network_layer, String serial_number, String warranty, Integer match_score, String originalKeys, String customFields, String matchedProductsIds, String subsystem_parent_id, String import_type)`
- **C-sub** (subsystem parent/child): `(... 11 fields ..., String originalKeys, String customFields, String matchedProductsIds, String subsystem_parent_id, Integer subsystem_count)` — 16 args.
- **C-prod** (unmatched product info): `(String id, String display_name, String model, String vendor, String type, String matched_product_ids)`

### Constructor-expression SELECT clause (C17) — reused verbatim

```
SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(
  a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type,
  a.ip_address, a.network_layer, a.serial_number, a.warranty, :matchScoreExpr,
  a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type)
```
Where `:matchScoreExpr` is `CAST(NULL AS integer)` for the queries that select `NULL as match_score`, or `adm.matchScore` for the join query (`getLinkedAssets`).

### Source of each query's WHERE/ORDER/LIMIT logic

The valueless `@Query(nativeQuery=true)` methods resolve to `@NamedNativeQuery` blocks in `Asset.java` (lines 39–335). For each converted method, copy that query's filter/order/paging logic **verbatim**, translating column names → entity fields via the map above, and replacing `LIMIT ?n OFFSET ?m` with a `Pageable`/positional `setMaxResults`-equivalent expressed in JPQL as nothing (JPQL has no LIMIT — use the existing positional params with `@Query` + `nativeQuery=false` is not possible for LIMIT). **Therefore: for paginated queries pass a `org.springframework.data.domain.Pageable` and drop the LIMIT/OFFSET params** (see Task 2 note).

---

## Task 0: Establish the AssetRepository IT baseline

**Files:**
- Read: `src/test/java/io/sclera/it/TechnicianAvailabilityTest.java` (pattern reference)
- Read: `src/test/resources/schema-pg.sql` (confirm `asset` + `asset_device_mapping` tables exist)
- Create: `src/test/resources/seed/asset-pilot.sql`
- Create: `src/test/resources/cleanup-asset-pilot.sql`
- Create: `src/test/java/io/sclera/it/AssetRepositoryIT.java`

- [ ] **Step 1: Confirm schema coverage**

Run: `./mvnw -q -DskipTests test-compile` then inspect `src/test/resources/schema-pg.sql`.
Expected: `CREATE TABLE asset (...)` and `CREATE TABLE asset_device_mapping (...)` present. If `asset`/`asset_device_mapping` are missing, add them to `schema-pg.sql` matching the entity columns in the reference map above (do not invent columns).

- [ ] **Step 2: Write the seed script** `src/test/resources/seed/asset-pilot.sql`

```sql
-- Two import types, a matched/unmatched mix, a parent + subsystem, and one device mapping.
INSERT INTO asset (id, display_name, description, type, network_layer, original_keys, import_type, is_matched, subsystem_count)
VALUES ('a1', 'Alpha Pump', 'desc-a', 'pump', 7, '{}', 'corrigo', false, 0);
INSERT INTO asset (id, display_name, description, type, network_layer, original_keys, import_type, is_matched, subsystem_parent_id, subsystem_count)
VALUES ('a2', 'Beta Valve', 'desc-b', 'valve', 7, '{}', 'corrigo', true, 'a1', 0);
INSERT INTO asset (id, display_name, description, type, network_layer, original_keys, import_type, is_matched, subsystem_count)
VALUES ('a3', 'Gamma Meter', 'desc-g', 'meter', 7, '{}', 'bacnet', false, 0);
INSERT INTO asset_device_mapping (id, asset_id, device_id, match_score)
VALUES ('m1', 'a1', 'd1', 88);
```
> If `asset_device_mapping.device_id` has a FK to `device`, insert a minimal `device` row first (id `d1`), or drop the FK in `schema-pg.sql` for the test schema. Choose whichever the existing schema-pg.sql already does for other join tables.

- [ ] **Step 3: Write the cleanup script** `src/test/resources/cleanup-asset-pilot.sql`

```sql
DELETE FROM asset_device_mapping;
DELETE FROM asset;
```

- [ ] **Step 4: Create the IT class skeleton** `src/test/java/io/sclera/it/AssetRepositoryIT.java`

```java
package io.sclera.it;

import io.sclera.Repository.AssetRepository;
import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting IT for the JPA-converted AssetRepository, run against real PostgreSQL.
 * Mirrors the existing TechnicianAvailabilityTest harness (JpaTestConfig + @ServiceConnection).
 */
@SpringBootTest(classes = JpaTestConfig.class)
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/asset-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-asset-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class AssetRepositoryIT {

    @Autowired
    AssetRepository assetRepository;
}
```
> Note: `JpaTestConfig` deliberately omits `@EnableJpaRepositories` (see its javadoc — it was disabled because the *MySQL* named queries failed to bootstrap). After this conversion removes those named queries (Task 4), add `@EnableJpaRepositories(basePackageClasses = AssetRepository.class)` to `JpaTestConfig` **or** a test-only `@Import` so `@Autowired AssetRepository` resolves. Do this in Step 5.

- [ ] **Step 5: Enable repository autowiring for the test context**

Add to `JpaTestConfig.java` (after the conversion this is now safe for these repos):

```java
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(
        basePackageClasses = io.sclera.Repository.AssetRepository.class,
        includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = { io.sclera.Repository.AssetRepository.class,
                            io.sclera.Repository.DeviceIPAddressRepository.class }))
```
> Scoping the filter to just the two pilot repos avoids bootstrapping the other repositories that still carry MySQL named queries.

- [ ] **Step 6: Compile to verify the harness wires up**

Run: `./mvnw -q test-compile`
Expected: BUILD SUCCESS (no test run yet).

- [ ] **Step 7: Commit**

```bash
git add src/test/java/io/sclera/it/AssetRepositoryIT.java src/test/resources/seed/asset-pilot.sql src/test/resources/cleanup-asset-pilot.sql src/test/java/io/sclera/it/JpaTestConfig.java
git commit -m "test: AssetRepository IT baseline harness for JPA conversion"
```

---

## Task 1: Convert the simple scalar reads/counts to JPQL (keep signatures)

**Files:**
- Modify: `src/main/java/io/sclera/Repository/AssetRepository.java`
- Test: `src/test/java/io/sclera/it/AssetRepositoryIT.java`

These already have inline native `value=` SQL. Convert each to JPQL by setting `nativeQuery=false` (default) and rewriting columns → entity fields. Signatures unchanged.

- [ ] **Step 1: Write failing tests** (append to `AssetRepositoryIT`)

```java
@Test
void getTotalAssetCount_countsAllRows() {
    assertThat(assetRepository.getTotalAssetCount()).isEqualTo(3);
}

@Test
void getParentAssetSubsystemCount_countsChildren() {
    assertThat(assetRepository.getParentAssetSubsystemCount("a1")).isEqualTo(1);
}

@Test
void getSubsystemParentIdByAssetId_returnsParent() {
    assertThat(assetRepository.getSubsystemParentIdByAssetId("a2")).isEqualTo("a1");
}

@Test
void getSubAssetIdByParentId_returnsChildIds() {
    assertThat(assetRepository.getSubAssetIdByParentId("a1")).containsExactly("a2");
}

@Test
void getUniqueDeviceTypes_returnsDistinct() {
    assertThat(assetRepository.getUniqueDeviceTypes())
        .containsExactlyInAnyOrder("pump", "valve", "meter");
}

@Test
void checkImportExists_trueWhenRowsPresent() {
    assertThat(assetRepository.checkImportExists()).isTrue();
}
```

- [ ] **Step 2: Run to verify they fail**

Run: `./mvnw -q -Dtest=AssetRepositoryIT test`
Expected: FAIL/ERROR (current methods are native; some still resolve, but `checkImportExists` returns `Boolean` from `SELECT EXISTS` native — keep behavior identical after conversion).

- [ ] **Step 3: Convert the methods to JPQL** (replace each method's annotation/body in `AssetRepository.java`)

```java
@Query("SELECT COUNT(a) FROM Asset a")
Integer getTotalAssetCount();

@Query("SELECT COUNT(a) FROM Asset a WHERE a.subsystem_parent_id = ?1")
Integer getParentAssetSubsystemCount(String parent_asset_id);

@Query("SELECT a.subsystem_parent_id FROM Asset a WHERE a.id = ?1")
String getSubsystemParentIdByAssetId(String asset_id);

@Query("SELECT a.id FROM Asset a WHERE a.subsystem_parent_id = ?1")
Set<String> getSubAssetIdByParentId(String parent_asset_id);

@Query("SELECT DISTINCT a.type FROM Asset a")
List<String> getUniqueDeviceTypes();

@Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Asset a")
Boolean checkImportExists();
```
> `getOriginalKeys()` (native `SELECT original_keys FROM asset LIMIT 1`): convert to `@Query("SELECT a.originalKeys FROM Asset a")` returning `List<String>` and have the (single) caller take `.get(0)`, **or** keep it returning `String` via `@Query(value="SELECT a.originalKeys FROM Asset a", nativeQuery=false)` plus a `Pageable` of size 1. Prefer the `List<String>` form; update the caller. Confirm the caller with `grep getOriginalKeys`.

- [ ] **Step 4: Run tests to verify pass**

Run: `./mvnw -q -Dtest=AssetRepositoryIT test`
Expected: PASS (all 6 tests above green).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/io/sclera/Repository/AssetRepository.java src/test/java/io/sclera/it/AssetRepositoryIT.java
git commit -m "refactor: convert AssetRepository scalar reads/counts to JPQL"
```

---

## Task 2: Convert the AssetDTO projection queries to JPQL constructor expressions

**Files:**
- Modify: `src/main/java/io/sclera/Repository/AssetRepository.java`
- Test: `src/test/java/io/sclera/it/AssetRepositoryIT.java`

Each projection currently resolves to an `@NamedNativeQuery` in `Asset.java`. Replace with an inline JPQL constructor expression using the **C17 SELECT clause** from the reference section, plus the WHERE/ORDER copied from the corresponding named query (translate columns → fields).

**Pagination change:** JPQL has no `LIMIT/OFFSET`. For the paged methods (`getPaginatedAssets`, `getUnmappedAssets`, `getSubSystemParentAssets`, `getSubSystemAssets`, `getUnmappedSubSystemParentAssets`), change the signature to accept `org.springframework.data.domain.Pageable` instead of the `Integer pageSize/limit, Integer offset` params, and drop those params from the query. Update callers to pass `PageRequest.of(offset/pageSize, pageSize)`. Confirm each caller with `grep` before editing.

Per-method mapping (filter logic copied verbatim from the named query, columns → fields):

| Method | matchScore expr | WHERE (fields) | Paged? |
|---|---|---|---|
| `getPaginatedAssets` | `CAST(NULL AS integer)` | `a.import_type = :importType AND (:searchKey = 'null' OR CONCAT(a.display_name, a.description) LIKE CONCAT('%', :searchKey, '%')) ORDER BY a.display_name` | yes |
| `getLinkedAssets` | `adm.matchScore` | join `Asset a JOIN a.assetDeviceMappings adm` where `a.id IN (SELECT m.asset.id FROM AssetDeviceMapping m WHERE m.device.id = :id)` | no |
| `getUnmappedAssets` | `CAST(NULL AS integer)` | `a.id NOT IN (SELECT m.asset.id FROM AssetDeviceMapping m) AND a.isMatched = false` | yes |
| `getUnmappedAssetsByIds` | `CAST(NULL AS integer)` | as above `AND a.id IN :idList` | no |
| `getFilteredAssets` | `CAST(NULL AS integer)` | copy filter from `Asset.getFilteredAssets` named query | yes |
| `getAssetsById` | `CAST(NULL AS integer)` | `a.id IN :idList` | no |
| `getUnmappedMatchedAssets` | `CAST(NULL AS integer)` | `a.id NOT IN (SELECT m.asset.id FROM AssetDeviceMapping m) AND a.isMatched = true` | no |
| `getSubAssetsByParentId` | `CAST(NULL AS integer)` | `a.subsystem_parent_id = :asset_id` | no |
| `getSubSystemParentAssets` | C-sub ctor | `a.import_type = :importType AND a.subsystem_parent_id IS NULL` (copy from named query) | yes |
| `getSubSystemAssets` | C-sub ctor | `a.subsystem_parent_id = :asset_id` | yes |
| `getUnmappedSubSystemParentAssets` | C-sub ctor | copy from named query | yes |
| `getUnmappedSubSystemParentAssetsByAssetIds` | C-sub ctor | copy from named query `AND a.id IN :asset_ids` | no |
| `getAllAssets` | C17 (per named query) | `a.import_type = :importType` | no |

> The C-sub methods use the 16-arg subsystem constructor (`...originalKeys, customFields, matchedProductIds, subsystem_parent_id, subsystem_count`) — SELECT clause ends with `a.subsystem_count` instead of `..., a.subsystem_parent_id, a.import_type`. Match each named query's `resultSetMapping` (`filteredAssetMapping` → C17; `subsystemAssetMapping` → C-sub) to pick the right constructor.

- [ ] **Step 1: Write failing tests for the representative cases**

```java
@Test
void getPaginatedAssets_filtersByImportTypeAndSearch() {
    List<AssetDTO> result = assetRepository.getPaginatedAssets(
            "corrigo", "null", PageRequest.of(0, 10));
    assertThat(result).extracting(AssetDTO::getId).containsExactly("a1", "a2"); // ORDER BY display_name: Alpha, Beta
    assertThat(result).allSatisfy(a -> assertThat(a.getMatch_score()).isNull());
}

@Test
void getLinkedAssets_joinsMappingForMatchScore() {
    List<AssetDTO> result = assetRepository.getLinkedAssets("d1");
    assertThat(result).extracting(AssetDTO::getId).contains("a1");
    assertThat(result).filteredOn(a -> a.getId().equals("a1"))
            .first().extracting(AssetDTO::getMatch_score).isEqualTo(88);
}

@Test
void getUnmappedAssets_excludesMappedAndMatched() {
    List<AssetDTO> result = assetRepository.getUnmappedAssets(PageRequest.of(0, 10));
    assertThat(result).extracting(AssetDTO::getId).contains("a3").doesNotContain("a1", "a2");
}

@Test
void getSubSystemAssets_returnsChildrenWithCount() {
    List<AssetDTO> result = assetRepository.getSubSystemAssets("a1", PageRequest.of(0, 10));
    assertThat(result).extracting(AssetDTO::getId).containsExactly("a2");
}
```

- [ ] **Step 2: Run to verify they fail**

Run: `./mvnw -q -Dtest=AssetRepositoryIT test`
Expected: COMPILE error (signatures not yet changed) — this is the expected red.

- [ ] **Step 3: Rewrite each projection method** in `AssetRepository.java`

Example (`getPaginatedAssets`, full):

```java
@Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
       "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
       "a.ip_address, a.network_layer, a.serial_number, a.warranty, CAST(NULL AS integer), " +
       "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
       "FROM Asset a WHERE a.import_type = :importType " +
       "AND (:searchKey = 'null' OR CONCAT(a.display_name, a.description) LIKE CONCAT('%', :searchKey, '%')) " +
       "ORDER BY a.display_name")
List<AssetDTO> getPaginatedAssets(@Param("importType") String importType,
                                  @Param("searchKey") String searchKey,
                                  Pageable pageable);
```

Example (`getLinkedAssets`, join for match_score):

```java
@Query("SELECT new io.sclera.dto.touchscreen.assetmapper.AssetDTO(" +
       "a.id, a.display_name, a.description, a.mac_address, a.model, a.vendor, a.type, " +
       "a.ip_address, a.network_layer, a.serial_number, a.warranty, adm.matchScore, " +
       "a.originalKeys, a.customFields, a.matchedProductIds, a.subsystem_parent_id, a.import_type) " +
       "FROM Asset a JOIN a.assetDeviceMappings adm " +
       "WHERE a.id IN (SELECT m.asset.id FROM AssetDeviceMapping m WHERE m.device.id = :id)")
List<AssetDTO> getLinkedAssets(@Param("id") String id);
```

Apply the per-method table for the rest. Use `@Param` names matching the JPQL.

- [ ] **Step 4: Update callers of changed (paged) signatures**

Run: `grep -rn "getPaginatedAssets\|getUnmappedAssets\|getSubSystemParentAssets\|getSubSystemAssets\|getUnmappedSubSystemParentAssets\b" src/main/java`
For each call site, replace `(..., pageSize, offset)` with `(..., PageRequest.of(offset / pageSize, pageSize))`. Keep all other args identical.

- [ ] **Step 5: Run tests to verify pass**

Run: `./mvnw -q -Dtest=AssetRepositoryIT test`
Expected: PASS (the 4 representative tests green). Add one assert-test per remaining projection method before considering the task done — each follows the same seed-and-assert shape.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/io/sclera/Repository/AssetRepository.java src/test/java/io/sclera/it/AssetRepositoryIT.java
git commit -m "refactor: convert AssetRepository DTO projections to JPQL constructor expressions"
```

---

## Task 3: Convert the @Modifying writes to JPQL

**Files:**
- Modify: `src/main/java/io/sclera/Repository/AssetRepository.java`
- Test: `src/test/java/io/sclera/it/AssetRepositoryIT.java`

All of these are inline-native `UPDATE/DELETE`. Convert to JPQL (`nativeQuery=false`), columns → fields, keeping `@Modifying @Transactional` and signatures. `is_matched=1/0` → `a.isMatched = true/false`.

Exact conversions:

```java
@Modifying @Transactional
@Query("DELETE FROM Asset a")
void deleteAllRecords();

@Modifying @Transactional
@Query("UPDATE Asset a SET a.matchedProductIds = ?2 WHERE a.id = ?1")
void saveMatchedProductsById(String id, String matchedProducts);

@Modifying @Transactional
@Query("DELETE FROM Asset a WHERE a.id IN ?1")
void deleteAllById(ArrayList<String> ids);

@Modifying @Transactional
@Query("UPDATE Asset a SET a.isMatched = ?1 WHERE a.id = ?2")
void setMatched(Boolean matched, String id);

@Modifying @Transactional
@Query("DELETE FROM Asset a WHERE a.isMatched = true")
void deleteAllMatchedRecords();

@Modifying @Transactional
@Query("UPDATE Asset a SET a.matchedProductIds = ?1 WHERE a.id = ?2")
void updateProductId(String matchedProducts, String asset_id);

@Modifying @Transactional
@Query("UPDATE Asset a SET a.subsystem_count = ?2 WHERE a.id = ?1")
void updateParentAssetSubsystemCount(String parent_asset_id, Integer subsystemCount);

@Modifying @Transactional
@Query("UPDATE Asset a SET a.subsystem_parent_id = ?2 WHERE a.id = ?1")
void updateSubsystemParentId(String asset_id, String subsystem_parent_id);

@Modifying @Transactional
@Query("UPDATE Asset a SET a.isMatched = false")
void setAllAssetsToUnMatched();

@Modifying @Transactional
@Query("UPDATE Asset a SET a.subsystem_parent_id = ?2 WHERE a.id IN ?1")
void updateSetOfSubsystemParentId(Set<String> subsystem_assets, String subsystem_parent_id);

@Modifying @Transactional
@Query("UPDATE Asset a SET a.type = ?1 WHERE a.type LIKE CONCAT(?2, '%')")
void updateTypeByType(String type, String idPrefix);

@Modifying @Transactional
@Query("UPDATE Asset a SET a.type = 'generic' WHERE a.type IS NULL")
void setTypeGeneric();

@Modifying @Transactional
@Query("UPDATE Asset a SET a.type = ?2 WHERE a.type = ?1")
void updateDeviceType(String type, String generic);
```

> `getAssetCount(importType, searchKey)` (native COUNT with CONCAT_WS): convert to
> `@Query("SELECT COUNT(a) FROM Asset a WHERE a.import_type = ?1 AND (?2 = 'null' OR CONCAT(a.display_name, a.description) LIKE CONCAT('%', ?2, '%'))")`.
> Note this also fixes the AND/OR precedence bug flagged in `phase3-native-sql.md` by parenthesizing — call this out in the commit message.

- [ ] **Step 1: Write failing tests**

```java
@Test
void setMatched_thenDeleteAllMatched_removesRow() {
    assetRepository.setMatched(true, "a3");
    assetRepository.deleteAllMatchedRecords();
    assertThat(assetRepository.findById("a3")).isEmpty();
    assertThat(assetRepository.findById("a1")).isPresent(); // a2 was matched in seed -> also removed
}

@Test
void updateSubsystemParentId_reparents() {
    assetRepository.updateSubsystemParentId("a3", "a1");
    assertThat(assetRepository.getSubAssetIdByParentId("a1")).contains("a2", "a3");
}

@Test
void getAssetCount_filtersAndCounts() {
    assertThat(assetRepository.getAssetCount("corrigo", "null")).isEqualTo(2);
    assertThat(assetRepository.getAssetCount("corrigo", "Alpha")).isEqualTo(1);
}
```

- [ ] **Step 2: Run to verify they fail** — `./mvnw -q -Dtest=AssetRepositoryIT test` → FAIL.
- [ ] **Step 3: Apply the conversions above.**
- [ ] **Step 4: Run to verify pass** — `./mvnw -q -Dtest=AssetRepositoryIT test` → PASS.
- [ ] **Step 5: Commit**

```bash
git add src/main/java/io/sclera/Repository/AssetRepository.java src/test/java/io/sclera/it/AssetRepositoryIT.java
git commit -m "refactor: convert AssetRepository @Modifying writes to JPQL (+fix getAssetCount precedence)"
```

---

## Task 4: Replace `assetUpsert` with find-or-create in the service

**Files:**
- Modify: `src/main/java/io/sclera/service/AssetOnboardService.java:145-148`
- Modify: `src/main/java/io/sclera/Repository/AssetRepository.java` (remove `assetUpsert`)
- Modify: `src/main/java/io/sclera/models/Asset.java` (remove the upsert `@NamedNativeQuery` + the now-unused `@SqlResultSetMapping`/`@NamedNativeQuery` blocks once Task 2 removed all native callers)
- Modify: `src/test/java/io/sclera/service/AssetOnboardServiceTest.java:64-74` (update the Mockito expectation)
- Test: `src/test/java/io/sclera/it/AssetRepositoryIT.java`

**Semantic to preserve:** current upsert inserts a full row but on PK conflict updates **only** `display_name`, `description`, `type`. The caller always passes `network_layer=7`, `is_matched=false`, `subsystem_count=0`, `original_keys=""`, `vdms=vdmsid`.

- [ ] **Step 1: Write failing IT tests** (insert path + conflict path)

```java
@Autowired org.springframework.data.jpa.repository.JpaRepository<io.sclera.models.Asset, String> assetJpa; // or use assetRepository

@Test
void upsert_insertPath_persistsFullRow() {
    // simulate service insert for a new id
    io.sclera.models.Asset a = new io.sclera.models.Asset();
    a.setId("a9"); a.setDisplay_name("New"); a.setDescription("d"); a.setType("pump");
    a.setNetwork_layer(7); a.setOriginalKeys(""); a.setImport_type("corrigo");
    a.setIsMatched(false); a.setSubsystem_count(0);
    assetRepository.save(a);
    assertThat(assetRepository.findById("a9")).get()
        .extracting(io.sclera.models.Asset::getDisplay_name).isEqualTo("New");
}

@Test
void upsert_conflictPath_updatesOnlyNameDescType() {
    io.sclera.models.Asset existing = assetRepository.findById("a1").orElseThrow();
    existing.setDisplay_name("Renamed"); existing.setDescription("d2"); existing.setType("t2");
    assetRepository.save(existing);
    io.sclera.models.Asset after = assetRepository.findById("a1").orElseThrow();
    assertThat(after.getDisplay_name()).isEqualTo("Renamed");
    assertThat(after.getNetwork_layer()).isEqualTo(7); // untouched
    assertThat(after.getImport_type()).isEqualTo("corrigo"); // untouched
}
```

- [ ] **Step 2: Run to verify they fail/compile-fail** — `./mvnw -q -Dtest=AssetRepositoryIT test`.

- [ ] **Step 3: Rewrite the service method** `AssetOnboardService.assetUpsert`

```java
public void assetUpsert(DeviceDTO device, String vdmsid, String assetImportType, String username) {
    Asset asset = assetRepository.findById(device.getId()).orElse(null);
    if (asset != null) {
        // CONFLICT path: original ON CONFLICT updated only these three columns
        asset.setDisplay_name(device.getUser_data_name());
        asset.setDescription(device.getDescription());
        asset.setType(device.getType());
    } else {
        // INSERT path: full row as the original VALUES(...) supplied
        asset = new Asset();
        asset.setId(device.getId());
        asset.setDisplay_name(device.getUser_data_name());
        asset.setDescription(device.getDescription());
        asset.setType(device.getType());
        asset.setNetwork_layer(7);
        asset.setOriginalKeys("");
        asset.setCustomFields(device.getCustom_fields());
        asset.setIsMatched(false);
        asset.setSubsystem_count(0);
        asset.setImport_type(assetImportType);
        if (vdmsid != null) {
            asset.setVdms(vdmsRepository.getReferenceById(vdmsid));
        }
    }
    assetRepository.save(asset);
}
```
> Add a `private final VdmsRepository vdmsRepository;` constructor dependency to `AssetOnboardService` (confirm `io.sclera.Repository.VdmsRepository extends JpaRepository<Vdms, String>`; if its id type differs, use `entityManager.getReference(Vdms.class, vdmsid)` instead). Import `io.sclera.models.Asset`.

- [ ] **Step 4: Remove `assetUpsert` from `AssetRepository.java`** (the `@Modifying ... void assetUpsert(...)` method and its javadoc).

- [ ] **Step 5: Update the Mockito unit test** `AssetOnboardServiceTest` (lines 64-74)

Replace the `verify(assetRepository).assetUpsert(...)` expectation with the find-or-create interaction:

```java
@Test
void assetUpsert_insertPath_savesNewAsset() {
    DeviceDTO device = new DeviceDTO();
    device.setId("d1"); device.setType("pump");
    when(assetRepository.findById("d1")).thenReturn(java.util.Optional.empty());
    service.assetUpsert(device, "v1", "import", "user");
    org.mockito.ArgumentCaptor<Asset> cap = org.mockito.ArgumentCaptor.forClass(Asset.class);
    verify(assetRepository).save(cap.capture());
    assertThat(cap.getValue().getId()).isEqualTo("d1");
    assertThat(cap.getValue().getNetwork_layer()).isEqualTo(7);
}
```
> Stub `vdmsRepository.getReferenceById("v1")` to return a mock `Vdms` if the insert path sets it.

- [ ] **Step 6: Run both test sets** — `./mvnw -q -Dtest=AssetRepositoryIT,AssetOnboardServiceTest test` → PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/io/sclera/service/AssetOnboardService.java src/main/java/io/sclera/Repository/AssetRepository.java src/test/java/io/sclera/service/AssetOnboardServiceTest.java src/test/java/io/sclera/it/AssetRepositoryIT.java
git commit -m "refactor: replace assetUpsert native query with find-or-create save()"
```

---

## Task 5: Remove the now-dead native query metadata from `Asset.java`

**Files:**
- Modify: `src/main/java/io/sclera/models/Asset.java`

After Tasks 2–4, no code references the `Asset.*` `@NamedNativeQuery` blocks or the `filteredAssetMapping`/`subsystemAssetMapping` `@SqlResultSetMapping`s.

- [ ] **Step 1: Confirm no references remain**

Run: `grep -rn "filteredAssetMapping\|subsystemAssetMapping\|Asset.getPaginatedAssets\|Asset.getLinkedAssets\|Asset.getFilteredAssets\|Asset.getAllAssets\|Asset.getSubSystem" src`
Expected: no matches in `src/main` (and any test references already migrated).

- [ ] **Step 2: Delete the `@NamedNativeQuery` and `@SqlResultSetMapping` annotation blocks** (lines ~11–335) from `Asset.java`, leaving `@Entity`, `@JsonInclude`, the class, and its fields/getters intact.

- [ ] **Step 3: Compile** — `./mvnw -q test-compile` → BUILD SUCCESS.

- [ ] **Step 4: Run the full AssetRepository IT** — `./mvnw -q -Dtest=AssetRepositoryIT test` → PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/io/sclera/models/Asset.java
git commit -m "chore: drop dead @NamedNativeQuery/@SqlResultSetMapping from Asset entity"
```

---

## Task 6: Convert `DeviceIPAddressRepository` (reads + delete to JPQL, insert to save())

**Files:**
- Modify: `src/main/java/io/sclera/Repository/DeviceIPAddressRepository.java`
- Modify: `src/main/java/io/sclera/service/DeviceService.java:2951,2965,2996` (insert call sites)
- Modify: `src/main/java/io/sclera/models/Device_IP_Address.java` (remove named queries/mapping)
- Create: `src/test/resources/seed/device-ip-pilot.sql`, `src/test/resources/cleanup-device-ip-pilot.sql`
- Create: `src/test/java/io/sclera/it/DeviceIPAddressRepositoryIT.java`

- [ ] **Step 1: Seed + cleanup scripts**

`seed/device-ip-pilot.sql`:
```sql
INSERT INTO device (id) VALUES ('dev1');
INSERT INTO device_ip_address (id, ip_address, ip_conflict_status, device_id)
VALUES ('ip1', '10.0.0.5', 0, 'dev1');
```
`cleanup-device-ip-pilot.sql`:
```sql
DELETE FROM device_ip_address;
DELETE FROM device WHERE id = 'dev1';
```
> If `device` has NOT NULL columns beyond `id`, add minimal values to the insert to satisfy the schema.

- [ ] **Step 2: Write failing IT** `DeviceIPAddressRepositoryIT.java`

```java
package io.sclera.it;

import io.sclera.Repository.DeviceIPAddressRepository;
import io.sclera.dto.touchscreen.DeviceIPAddressDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = JpaTestConfig.class)
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-ip-pilot.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-ip-pilot.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceIPAddressRepositoryIT {

    @Autowired DeviceIPAddressRepository repo;

    @Test
    void getIPAddressByDeviceId_returnsProjection() {
        List<DeviceIPAddressDTO> result = repo.getIPAddressByDeviceId("dev1");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIp_address()).isEqualTo("10.0.0.5");
        assertThat(result.get(0).getIp_conflict_status()).isEqualTo(0);
    }

    @Test
    void deleteIPAddressByDeviceId_removesRows() {
        repo.deleteIPAddressByDeviceId("dev1");
        assertThat(repo.getIPAddressByDeviceId("dev1")).isEmpty();
    }
}
```

- [ ] **Step 3: Convert the repository** `DeviceIPAddressRepository.java`

```java
@Query("SELECT new io.sclera.dto.touchscreen.DeviceIPAddressDTO(d.ip_address, d.ip_conflict_status) " +
       "FROM Device_IP_Address d WHERE d.device.id = ?1")
List<DeviceIPAddressDTO> getIPAddressByDeviceId(String id);

@Modifying @Transactional
@Query("DELETE FROM Device_IP_Address d WHERE d.device.id = ?1")
void deleteIPAddressByDeviceId(String id);
```
Remove `insertIPAddressByDeviceId` from the interface.
> Verify `DeviceIPAddressDTO` has a `(String ip_address, Integer ip_conflict_status)` constructor; if not, add it (it currently backs the `deviceipaddressmapping` ConstructorResult, so the constructor exists). Confirm the `Device_IP_Address.ip_address`/`ip_conflict_status` field names for JPQL.

- [ ] **Step 4: Rewrite the 3 insert call sites** in `DeviceService.java`

Replace each `deviceIPAddressRepository.insertIPAddressByDeviceId(id, ip, status, device.getId())` with:

```java
Device_IP_Address rec = new Device_IP_Address();
rec.setId(id);
rec.setIp_address(deviceIPAddress.getIp_address());
rec.setIp_conflict_status(deviceIPAddress.getIp_conflict_status());
rec.setDevice(deviceRepository.getReferenceById(device.getId()));
deviceIPAddressRepository.save(rec);
```
> Add imports for `io.sclera.models.Device_IP_Address` if missing. `deviceRepository` is already a field of `DeviceService` (used on line 2972). Confirm `DeviceRepository extends JpaRepository<Device, String>` so `getReferenceById` is available.

- [ ] **Step 5: Run to verify pass** — `./mvnw -q -Dtest=DeviceIPAddressRepositoryIT test` → PASS.

- [ ] **Step 6: Remove dead native metadata** from `Device_IP_Address.java` (the `@SqlResultSetMapping` + `@NamedNativeQueries` blocks), keeping `@Entity`, fields, getters.

Run: `grep -rn "deviceipaddressmapping\|Device_IP_Address.getIPAddress\|Device_IP_Address.insertIPAddress\|Device_IP_Address.deleteIPAddress" src` → expect no matches before deleting.

- [ ] **Step 7: Compile + run** — `./mvnw -q test-compile && ./mvnw -q -Dtest=DeviceIPAddressRepositoryIT test` → PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/io/sclera/Repository/DeviceIPAddressRepository.java src/main/java/io/sclera/service/DeviceService.java src/main/java/io/sclera/models/Device_IP_Address.java src/test/java/io/sclera/it/DeviceIPAddressRepositoryIT.java src/test/resources/seed/device-ip-pilot.sql src/test/resources/cleanup-device-ip-pilot.sql
git commit -m "refactor: convert DeviceIPAddressRepository to JPQL + save()"
```

---

## Task 7: Full regression + playbook capture

**Files:**
- Create: `migration-notes/jpa-conversion-playbook.md`

- [ ] **Step 1: Run the whole module build**

Run: `./mvnw -q clean verify`
Expected: BUILD SUCCESS. If other repositories' MySQL named queries break the broader context, scope the run: `./mvnw -q -Dtest='AssetRepositoryIT,DeviceIPAddressRepositoryIT,AssetOnboardServiceTest' test`.

- [ ] **Step 2: Write the playbook** `migration-notes/jpa-conversion-playbook.md`

Document the three buckets, the entity-field-map step, the constructor-expression recipe, the `LIMIT/OFFSET → Pageable` rule, the FK-via-`getReferenceById` rule for `save()` rewrites, and the IT seed/assert recipe — so the next repository conversion follows the same path.

- [ ] **Step 3: Commit**

```bash
git add migration-notes/jpa-conversion-playbook.md
git commit -m "docs: JPA conversion playbook from AssetRepository pilot"
```

---

## Self-review notes

- **Spec coverage:** Bucket 1 (derived/scalar) → Tasks 1; Bucket 2 (JPQL projections + modifying) → Tasks 2–3; Bucket 3 (`save()` upsert/insert) → Tasks 4, 6; verification via `io.sclera.it` PG harness → every task; dead-metadata cleanup → Tasks 5, 6; playbook → Task 7. All spec sections mapped.
- **Open verification points flagged inline** (not placeholders — explicit confirm-before-edit checks): `schema-pg.sql` table coverage, `VdmsRepository`/`DeviceRepository` id types for `getReferenceById`, `DeviceIPAddressDTO`/`AssetDTO` constructor availability, and each paged-method caller. These are deliberate guard rails because the entity-coverage audit can only be fully resolved against the running schema.
- **Type consistency:** JPQL uses entity field names exactly as in the reference map (`a.display_name`, `a.isMatched`, `a.matchedProductIds`, `a.subsystem_parent_id`); constructor expressions target the documented `AssetDTO` C17/C-sub constructors.
