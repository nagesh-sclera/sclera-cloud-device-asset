# DB-per-Service Separation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the 6 foreign-owned tables (+ sensor-attributes family) from this service's PostgreSQL schema, keep only the scalar FK id, and serve the cross-module reads through Dapr-shaped enrichment clients that are stubbed today.

**Architecture:** Per foreign relation: drop the `LEFT JOIN <foreign>` from the native query, project `NULL AS <col>` placeholders so DTO payloads stay identical, then enrich the returned DTOs in the service layer via a per-owner client. The client lives in `io.sclera.stubs`, returns safe defaults + one WARN now, and becomes a real `DaprClient.invokeMethod(appId,...)` call when the owner ships. Enrichment maps by an id the DTO already exposes (e.g. device id) so no DTO field changes.

**Tech Stack:** Java 21, Spring Boot 4, Hibernate 7 native/`@NamedNativeQuery`, Dapr (service invocation later), PostgreSQL 16, JUnit 5 + Mockito.

**Spec:** `docs/superpowers/specs/2026-06-08-db-per-service-separation-design.md`

**Branch:** `feature/db-per-service-separation` (off main).

**Build/test (inner module dir):** `C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset\sclera-cloud-device-asset`. PowerShell, `JAVA_HOME` = `C:\Users\DhanushVasanth\.jdks\corretto-21.0.8` (does not persist between calls): `$env:JAVA_HOME="..."; & .\mvnw.cmd -q -o test -Dtest=...`. EXPLAIN validation runs against the live `sclera-postgres` container.

---

## Global rules (apply to every task)

- **DTO/API shape is frozen.** Never add/remove a serialized DTO field. Enrich existing fields only.
- **Enrichment key = an id already in the DTO/result** (e.g. `id` = device id). If the result has no usable key, add the foreign **scalar FK** to the SELECT *only if* that column is already a DTO field; otherwise enrich via a secondary local lookup `id -> fkId` (a small `@Query` returning `(id, fkId)` pairs). Do NOT add new serialized fields.
- **Query transform:** delete `LEFT JOIN <foreign> x ON ...`; replace each `x.<col>` in the SELECT with `CAST(NULL AS <type>) AS <col>` (use `varchar`/`text` for strings, matching the `@ColumnResult`/`@SqlResultSetMapping` type). Keep the result-set mapping unchanged. If `x.<col>` appears in `WHERE`/`ORDER BY`/`GROUP BY` (not just SELECT) → **STOP, this is a hard case** (see "Hard-case handling").
- **Hibernate `::` rule:** never `?N::type`; use `CAST(?N AS type)`. `CAST(NULL AS varchar)` is safe.
- **Stubs:** `@Component`, return safe defaults (empty map / null fields), log exactly one WARN per call: `log.warn("STUB <Client>.<method> ... returning defaults (Dapr owner '<appId>' not wired)")`.
- **EXPLAIN every rewritten query** against `sclera-postgres` (via `docker exec` or `docker run --network`), expecting no `relation/column does not exist`.
- **Commit per logical step.** Each domain is independent.

### Hard-case handling
If a foreign column is used in `WHERE`/`ORDER BY`/`GROUP BY`: do not rewrite blindly. Record the query in `migration-notes/db-per-service-foreign-tables.md` under "Filter/sort dependencies" with the file, query name, and the predicate. Default resolution: **drop the foreign predicate** if it is a display-only filter (and note it); otherwise leave the query unchanged and mark it `// PG-gap: needs <owner> query API` (it already fails today if the table is absent — removing the stub just keeps it non-functional, which is acceptable and documented). Flag any such query in the task report so the controller decides.

---

## Task 1: Canonical foreign-table note + stub-entity discovery

**Files:**
- Create: `sclera-cloud-device-asset/migration-notes/db-per-service-foreign-tables.md`

- [ ] **Step 1: Discover which stub entities actually exist**

Run (repo root, Bash):
```bash
for n in Product_Details AlertProfile Vendor ReportAttributes LocationGlobalChecklist CustomerOrganisation; do
  echo "== $n =="; find sclera-cloud-device-asset/sclera-cloud-device-asset/src/main/java -iname "$n.java" -o -iname "${n}Repository.java" 2>/dev/null;
done
```
Expected: confirms each entity/repo file path (or absence). `Product_Details` + `Product_DetailsRepository` are known to exist.

- [ ] **Step 2: Write the canonical note**

Create `db-per-service-foreign-tables.md` containing the spec's foreign-table table (the 7 rows), the discovered entity/repo file paths per domain (or "no local entity — JOIN-only"), and two empty sections to fill as work proceeds: "Filter/sort dependencies (hard cases)" and "Writes converted to Dapr/no-op". Also note the schema-regen requirement (`docker compose down -v` to drop the now-ungenerated tables).

- [ ] **Step 3: Commit**

```bash
git add sclera-cloud-device-asset/migration-notes/db-per-service-foreign-tables.md
git commit -m "docs(arch): canonical foreign-table list + stub-entity discovery"
```

---

## Task 2: REFERENCE domain — product_details → inventory (full pattern)

This task establishes the exact pattern. Later domains mirror it.

**Files:**
- Create: `.../io/sclera/stubs/InventoryClient.java` (interface)
- Create: `.../io/sclera/stubs/InventoryClientStub.java` (`@Component` stub)
- Create: `.../io/sclera/dto/ProductImagesDTO.java` (enrichment DTO)
- Create test: `.../src/test/java/io/sclera/stubs/InventoryClientStubTest.java`
- Modify: `.../io/sclera/models/{Device,Bacnet_Object,Lorawan_Sensor,MeasuringInstrument}.java` (native queries joining `product_details`)
- Modify: `.../io/sclera/service/DeviceService.java` (+ any service building the affected DTOs) — enrich
- Modify: `.../io/sclera/service/Product_DetailsService.java` — refactor `deleteProductDetailsById`
- Delete: `.../io/sclera/models/Product_Details.java`, `.../io/sclera/Repository/Product_DetailsRepository.java`

- [ ] **Step 1: Write the failing stub test**

```java
package io.sclera.stubs;

import io.sclera.dto.ProductImagesDTO;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class InventoryClientStubTest {
    private final InventoryClient client = new InventoryClientStub();

    @Test
    void getProductImages_emptyInput_returnsEmptyMap() {
        assertTrue(client.getProductImages(Set.of()).isEmpty());
    }

    @Test
    void getProductImages_returnsDefaultsForEachId_neverNullMap() {
        Map<String, ProductImagesDTO> r = client.getProductImages(Set.of("p1", "p2"));
        assertNotNull(r);                       // stub never returns null map
        // stub has no data source yet: either empty map or null-valued DTOs; both mean "no images"
        assertNull(client.getProductImages(Set.of("p1")).getOrDefault("p1", new ProductImagesDTO(null,null,null,null)).image_url_1());
    }
}
```

- [ ] **Step 2: Run the test → expect FAIL (types missing)**

Run: `& .\mvnw.cmd -q -o test -Dtest=InventoryClientStubTest`
Expected: compile failure — `InventoryClient` / `ProductImagesDTO` not found.

- [ ] **Step 3: Create the enrichment DTO**

```java
package io.sclera.dto;

/** Subset of product_details fields this service displays; owned by sclera-inventory. */
public record ProductImagesDTO(String image_url_1, String image_url_2,
                               String image_url_3, String global_image_url_1) {}
```

- [ ] **Step 4: Create the client interface**

```java
package io.sclera.stubs;

import io.sclera.dto.ProductImagesDTO;
import java.util.Map;
import java.util.Set;

/** Fetches product image data owned by sclera-inventory. Today: stub. Later: Dapr invoke. */
public interface InventoryClient {
    /** @return map productId -> images; ids with no data are absent or map to a null-valued DTO. */
    Map<String, ProductImagesDTO> getProductImages(Set<String> productIds);
}
```

- [ ] **Step 5: Create the stub impl**

```java
package io.sclera.stubs;

import io.sclera.dto.ProductImagesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Set;

/**
 * STUB for sclera-inventory product images. Returns no data (DB-per-service: product_details
 * left this schema). Replace the body with DaprClient.invokeMethod("sclera-inventory", ...)
 * when inventory exposes the endpoint. Per CLAUDE.md stubs never throw.
 */
@Component
public class InventoryClientStub implements InventoryClient {
    private static final Logger log = LoggerFactory.getLogger(InventoryClientStub.class);

    @Override
    public Map<String, ProductImagesDTO> getProductImages(Set<String> productIds) {
        if (productIds == null || productIds.isEmpty()) return Map.of();
        log.warn("STUB InventoryClient.getProductImages({} ids) returning no data (Dapr owner 'sclera-inventory' not wired)", productIds.size());
        return Map.of();
    }
}
```

- [ ] **Step 6: Run the test → expect PASS**

Run: `& .\mvnw.cmd -q -o test -Dtest=InventoryClientStubTest`
Expected: PASS.

- [ ] **Step 7: Rewrite the native queries that JOIN `product_details`**

Find them: `grep -rn "product_details\|image_url_1\|global_image_url_1" models/{Device,Bacnet_Object,Lorawan_Sensor,MeasuringInstrument}.java`.
For EACH occurrence apply the **query transform** (global rules): remove `LEFT JOIN product_details p ON ...`; replace `p.image_url_1/2/3`, `p.global_image_url_1` in the SELECT with `CAST(NULL AS varchar) AS image_url_1` (etc.). Leave the `@SqlResultSetMapping`/`@ColumnResult` entries unchanged.

Concrete example — `Device.listDevicesTs` (`Device.java`), the `, p.image_url_1,` in the SELECT and the `LEFT JOIN product_details p ...` clause:
```
// before:  ... d.last_seen_on , p.image_url_1, d.virtual_device_type ...  From device d ... LEFT JOIN product_details p ON d.product_id = p.id ...
// after:   ... d.last_seen_on , CAST(NULL AS varchar) AS image_url_1, d.virtual_device_type ...  From device d ...   (JOIN line removed)
```
If any `p.<col>` is used in WHERE/ORDER BY → apply Hard-case handling (do not blind-rewrite; record + report).

- [ ] **Step 8: EXPLAIN-validate each rewritten query on live PG**

For each query string, run it through `EXPLAIN` against `sclera-postgres` (parameters as `NULL`/dummy):
```bash
docker exec -i sclera-postgres psql -U <user> -d <db> -c "EXPLAIN <query>"
```
Expected: a plan, no `relation "product_details" does not exist` / `column ... does not exist`.

- [ ] **Step 9: Enrich the DTOs in the service**

In the service method(s) that run these queries and build the DTO list (start with `DeviceService` list/detail methods that populate `image_url_1`): after the query returns, enrich without changing DTO shape:
```java
// inject: private final InventoryClient inventoryClient;  (constructor or @Autowired field, matching the class's style)
// after building List<DeviceListDTO> rows that each have a device id + product mapping:
java.util.Map<String,String> deviceToProduct = deviceRepository.findDeviceProductIds(
        rows.stream().map(DeviceListDTO::getId).collect(java.util.stream.Collectors.toSet())); // id -> product_id
var images = inventoryClient.getProductImages(new java.util.HashSet<>(deviceToProduct.values()));
for (DeviceListDTO row : rows) {
    var img = images.get(deviceToProduct.get(row.getId()));
    if (img != null) { row.setImage_url_1(img.image_url_1()); /* _2,_3, global as the DTO exposes */ }
}
```
Add the helper query to `DeviceRepository` (native, returns `(id, product_id)`):
```java
@Query(value = "SELECT id, product_id FROM device WHERE id IN (:ids)", nativeQuery = true)
List<Object[]> findDeviceProductIdRows(@Param("ids") Set<String> ids);
```
(Map the `Object[]` rows to `id->product_id` in a small private helper. Use the projection that matches the DTO actually built by each method; mirror this for Bacnet_Object/Lorawan_Sensor/MeasuringInstrument DTOs where they expose image fields.)

- [ ] **Step 10: Refactor `Product_DetailsService` (remove repo dependency)**

`deleteProductDetailsById` currently reads `product_detailsRepository.getProductsImageUrlById` + `deleteById`. With the table gone, the owner handles deletion. Replace its body with a Dapr-owner no-op + WARN (file cleanup for foreign-owned images is the owner's job):
```java
void deleteProductDetailsById(String productId) {
    log.warn("deleteProductDetailsById({}) is a no-op: product_details is owned by sclera-inventory (DB-per-service)", productId);
}
```
Remove the `Product_DetailsRepository product_detailsRepository` field and its import. Confirm no other caller uses `product_detailsRepository`/`Product_Details` (grep); the already-no-op `checkProductId`/`addProductImages`/`upsertProductDetail` stay no-ops.

- [ ] **Step 11: Delete the stub entity + repo**

```bash
git rm sclera-cloud-device-asset/sclera-cloud-device-asset/src/main/java/io/sclera/models/Product_Details.java
git rm sclera-cloud-device-asset/sclera-cloud-device-asset/src/main/java/io/sclera/Repository/Product_DetailsRepository.java
```
Then `grep -rn "Product_Details\b\|Product_DetailsRepository\|import .*Product_Details" src/main` and fix any remaining references (ProductDTO is a DTO and STAYS; only the `@Entity`/repo go).

- [ ] **Step 12: Compile + run affected tests**

Run: `& .\mvnw.cmd -q -o compile` then `& .\mvnw.cmd -q -o test -Dtest=InventoryClientStubTest,DeviceServiceTest`
Expected: BUILD SUCCESS; tests green (DeviceServiceTest still passes — DTO shape unchanged).

- [ ] **Step 13: Record in the note + commit**

Append to `db-per-service-foreign-tables.md`: product_details DONE (client `InventoryClient`, queries rewritten, entity/repo deleted, any hard cases). Then:
```bash
git add -A
git commit -m "feat(arch): separate product_details -> inventory (Dapr-stub enrichment)"
```

---

## Tasks 3–8: remaining domains (mirror Task 2's pattern exactly)

For each domain below, repeat Task 2's steps with these parameters: **(a)** create `<Client>`/`<Client>Stub` in `io.sclera.stubs` + a record DTO with the listed fields (batch `Map<id, DTO>` signature, WARN stub) + a stub unit test mirroring `InventoryClientStubTest`; **(b)** rewrite every native query JOINing the foreign table per the global query-transform rule (NULL-placeholder projection; hard-case handling for WHERE/ORDER BY); **(c)** EXPLAIN each on live PG; **(d)** enrich the affected DTOs in the owning service by an id already in the DTO; **(e)** if a local `@Entity`/repo exists for the table (verify via Task 1 discovery), refactor dead readers then `git rm` it; **(f)** compile + affected tests green; **(g)** append status to the note; commit `feat(arch): separate <table> -> <owner> (Dapr-stub enrichment)`.

### Task 3: `customer_organisation` → identity
- Client `IdentityClient` (app-id `sclera-identity`), method `Map<String,CustomerOrgDTO> getCustomerOrgs(Set<String> orgIds)`; DTO fields = the columns `User.java` LEFT JOIN selects from `customer_organisation` (grep `customer_organisation` in `models/User.java` + `Repository/UserRepository.java` to enumerate). Scalar FK already present: `User.customer_org_id`. Enrich `UserDTO` by user id. Stub entity: discover (Task 1) — likely none (JOIN-only) → no `git rm`.

### Task 4: `alert_profile` → alerts
- Client `AlertsClient` (app-id `sclera-alerts`), `Map<String,AlertProfileDTO> getAlertProfiles(Set<String> ids)`; DTO = columns selected from `alert_profile` in `Conditions.java`/`ConditionsRepository.java`/`DeviceConditions*` (grep `alert_profile`). Scalar FK: `Conditions.alert_profile_id`, `device_conditions.alert_profile_id` — KEEP. Remove only the JOIN/SELECT of `alert_profile.*`; DML that writes `alert_profile_id` (a scalar column) stays. Enrich Conditions/DeviceConditions DTOs by their id. Delete `AlertProfile` entity/repo if present.

### Task 5: `report_attributes` → reports
- Client `ReportsClient` (app-id: confirm reports owner — grep compose `docker-compose.yml` services for a reports/report service; if none exists, use placeholder app-id `sclera-reports` and note it). `Map<String,ReportAttributesDTO> getReportAttributes(Set<String> templateIds)`; DTO = columns selected from `report_attributes` in `Bacnet_Object.java`/`MeasuringInstrument.java` (grep). Scalar FK: `report_template_id` — KEEP. Delete `ReportAttributes` entity/repo if present.

### Task 6: `location_global_checklist` → inspection
- Client `InspectionClient` (app-id `sclera-inspection`), `Map<String,GlobalChecklistDTO> getGlobalChecklists(Set<String> ids)`; DTO = columns selected in `Location.java` (grep `location_global_checklist`; note its `@IdClass` if the entity exists). Watch the existing `// PG-gap` lines in `Location.java` (the `?8`-as-condition + duplicate-JOIN oddities) — leave those preserved; only remove the `location_global_checklist` JOIN. Delete entity/repo (+ `LocationGlobalChecklistId`) if present.

### Task 7: `vendor` → integrations
- Client `VendorClient` (app-id `sclera-integrations`), `Map<String,VendorDTO> getVendors(Set<String> vendorOrgIds)`; DTO = columns selected from `vendor` in `Docker.java` (grep `vendor ` JOINs — NOTE: `vendor_organisation`/`Vendor_Organisation` is a DIFFERENT, this-service table; do NOT touch it). Scalar FK: `Device.vendor_org_id` — KEEP. Delete `Vendor` entity/repo if present (keep `Vendor_Organisation`).

### Task 8: sensor-attributes family → integrations
- Client `SensorClient` (app-id `sclera-integrations`), one method per attribute table actually JOINed in active (non-`// PG-gap`) queries, or a single `Map<String,SensorAttributesDTO> getSensorAttributes(...)` if they share a shape. These JOINs are **already `// PG-gap`** (tables never stubbed), so most are inert. Scope: only wire enrichment for queries that are actually executed and need the data; for the rest, leave the `// PG-gap` marker and record in the note that they await the integrations sensor service. No entity deletion (none exist). This task is mostly documentation + a thin client for any live path; report what was live vs left gapped.

---

## Task 9: Schema regen + full verification

**Files:** none (verification) + possible note update.

- [ ] **Step 1: Rebuild the device-asset jar + image**

PowerShell (inner dir): `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; & .\mvnw.cmd -DskipTests clean package`
Then repo root: `docker compose build app`.

- [ ] **Step 2: Regen the schema (drop the now-ungenerated foreign tables)**

```bash
docker compose down -v
docker compose up -d postgres redis app app-dapr
```
`ddl-auto=update` regenerates the schema WITHOUT the deleted stub tables (greenfield, no data lost).

- [ ] **Step 3: Confirm the foreign tables are gone and app is healthy**

```bash
docker exec -i sclera-postgres psql -U <user> -d <db> -c "\dt" | grep -iE "product_details|alert_profile|report_attributes|location_global_checklist|customer_organisation|^.*vendor\b" || echo "foreign tables absent (expected)"
curl -s -o /dev/null -w "app health=%{http_code}\n" http://localhost:8085/actuator/health
```
Expected: the 6 foreign tables absent; `vendor_organisation` still present; health 200 (no Hibernate validation error — entities gone, queries don't reference the tables).

- [ ] **Step 4: Full suite**

Run (inner dir): `& .\mvnw.cmd -o test`
Expected: full device-asset suite green (DTO shapes unchanged → existing tests pass; new stub tests pass).

- [ ] **Step 5: DTO-shape spot check**

Hit a list endpoint that previously showed product images (e.g. the touchscreen device list) and confirm the JSON has the same fields, with image urls `null`:
```bash
curl -s "http://localhost:8085/<device-list-endpoint>" | head -c 400
```
Expected: identical field set; foreign-sourced fields null.

- [ ] **Step 6: Commit any note updates**

```bash
git add -A && git commit -m "test(arch): schema regen + full verification of DB-per-service separation"
```

---

## Self-Review

- **Spec coverage:** foreign-table list → Task 1; pattern + clients + query rewrites + enrichment + entity deletion → Task 2 (reference) + Tasks 3–8 (per domain); schema removal/regen → Task 9; testing strategy → per-task stub tests + EXPLAIN + Task 9 suite/boot. Hard cases + writes → global rules + per-task reporting. ✅
- **Placeholders:** Task 2 is fully concrete (real code). Tasks 3–8 are parameterized recipes that name the exact table, owner, app-id, files-to-grep, FK to keep, and DTO source — the per-query rewrite is the deterministic global transform shown with a real example in Task 2 (not "implement later"). The one genuine unknown — the `reports` owner app-id — has an explicit resolve-or-placeholder step in Task 5. ✅
- **Type consistency:** client method shape `Map<Id, DTO> getX(Set<Id>)` and stub WARN form are uniform across tasks; DTOs are records with only the displayed fields; `InventoryClient`/`ProductImagesDTO` names match between test, interface, stub, and enrichment. ✅
- **Risk note for the executor:** enrichment must map by an id already on the DTO; if a method's DTO has no usable key, use the `id -> fkId` helper-query approach (Task 2 Step 9), never add a serialized field.
