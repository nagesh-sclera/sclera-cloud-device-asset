# Multi-VDMS Support Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `sclera-cloud-device-asset` serve many VDMS rows selected explicitly per request, replacing the process-global single-VDMS assumption, while keeping the same Docker Compose deployment and staying backward compatible with the existing single-row install.

**Architecture:** Add a `@RequestScope VdmsContext` populated by a `VdmsContextInterceptor` (header `X-Vdms-Id` → param `vdms_id` → single-row fallback). Back the stubbed `VdmsRepository`/`VdmsconfigurationRepository` reads with the local `vdms`/`vdms_configuration` tables keyed by id. Add a `VdmsController` + `VdmsCrudService` for CRUD. Move single-VDMS env values into nullable `vdms` columns backfilled by migration. In the UI, make the selected `vdmsId` stateful/persisted and inject it as `X-Vdms-Id`.

**Tech Stack:** Java 21 + Spring Boot 3 (Jakarta), Spring Data JPA, PostgreSQL, JUnit 5 + Mockito + AssertJ + Spring MockMvc; React (Vite) for `sclera-ui`.

## Global Constraints

- Package root is `io.sclera.*` (not `com.sclera.*`). Copy this verbatim into every new file.
- No new servers/containers/Compose instances; same app on port `8085`; no topology change.
- Changes limited to `sclera-cloud-device-asset` and `sclera-ui`. Do NOT edit sibling services.
- Flyway is disabled (`spring.flyway.enabled: false`). Schema changes come from JPA `ddl-auto: update` (Hibernate adds new entity columns automatically) + idempotent SQL in `src/main/resources/data.sql` (runs after ddl via `spring.sql.init.mode=always` + `defer-datasource-initialization=true`). Every SQL statement MUST be idempotent.
- Java tests must run WITHOUT Docker: use Mockito/MockMvc unit tests as the gate. `*IT.java` Testcontainers tests are additive only (broken under Docker 29 in this env) — write them but do not rely on them passing locally.
- Build/test with the JBR 21 `JAVA_HOME` and the module wrapper: `cd sclera-cloud-device-asset && ./mvnw -o ...`. Do NOT commit (user commits manually).
- Preserve existing snake_case field names on the `Vdms` entity; do not rename.
- New controllers use base path `/api/v1/sclera-cloud-device-asset-service` with `@RestController @Validated @CrossOrigin(origins = "*", allowedHeaders = "*")`, matching `QrCodeController`.

---

### Task 1: `VdmsContext` request-scoped holder

Replaces the process-global VDMS identity in `AuthenticationUtils` with per-request state.

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/utils/VdmsContext.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/utils/VdmsContextTest.java`

**Interfaces:**
- Produces: `VdmsContext` bean with `String getVdmsId()`, `void setVdmsId(String)`, `boolean hasVdms()`.

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.utils;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class VdmsContextTest {

    @Test
    void empty_hasVdms_false() {
        VdmsContext ctx = new VdmsContext();
        assertThat(ctx.hasVdms()).isFalse();
        assertThat(ctx.getVdmsId()).isNull();
    }

    @Test
    void setVdmsId_thenHasVdms_true() {
        VdmsContext ctx = new VdmsContext();
        ctx.setVdmsId("VDMS760");
        assertThat(ctx.hasVdms()).isTrue();
        assertThat(ctx.getVdmsId()).isEqualTo("VDMS760");
    }

    @Test
    void blankVdmsId_hasVdms_false() {
        VdmsContext ctx = new VdmsContext();
        ctx.setVdmsId("  ");
        assertThat(ctx.hasVdms()).isFalse();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsContextTest`
Expected: FAIL — `VdmsContext` does not exist (compilation error).

- [ ] **Step 3: Write minimal implementation**

```java
package io.sclera.utils;

import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

/**
 * Per-request holder for the VDMS this request targets. Replaces the process-global
 * vdms id that formerly lived in {@link AuthenticationUtils}. Populated by
 * {@code VdmsContextInterceptor}.
 */
@Component
@RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class VdmsContext {

    private String vdmsId;

    public String getVdmsId() {
        return vdmsId;
    }

    public void setVdmsId(String vdmsId) {
        this.vdmsId = vdmsId;
    }

    public boolean hasVdms() {
        return vdmsId != null && !vdmsId.trim().isEmpty();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsContextTest`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/utils/VdmsContext.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/utils/VdmsContextTest.java
git commit -m "feat(vdms): add request-scoped VdmsContext holder"
```

---

### Task 2: `Vdms` entity columns + per-id named queries + migration backfill

Adds the per-VDMS config columns (from single-VDMS env) and fixes the single-row named queries.

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/models/Vdms.java` (add fields near line 224 after `adc_configuration_id`; fix named queries at lines 73-77, 95-99, 118-122)
- Modify: `sclera-cloud-device-asset/src/main/resources/data.sql` (append backfill)
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/models/VdmsColumnsTest.java`

**Interfaces:**
- Produces: `Vdms.getServer_url()/setServer_url(String)`, `Vdms.getCredential_ref()/setCredential_ref(String)`.
- Produces: named query `Vdms.getVdmsDetails` and `Vdms.getVdmsMasterSlaveDetails` now require a `:vdms_id` parameter; `Vdms.getSyncDetailsForADC` now takes `:vdms_id` (no `LIMIT 1`).

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.models;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class VdmsColumnsTest {

    @Test
    void serverUrl_roundTrips() {
        Vdms v = new Vdms();
        v.setServer_url("http://vdms-a:8089/vdms");
        assertThat(v.getServer_url()).isEqualTo("http://vdms-a:8089/vdms");
    }

    @Test
    void credentialRef_roundTrips() {
        Vdms v = new Vdms();
        v.setCredential_ref("sclera/dev/edge/vdms-a/credential/");
        assertThat(v.getCredential_ref()).isEqualTo("sclera/dev/edge/vdms-a/credential/");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsColumnsTest`
Expected: FAIL — `setServer_url`/`getServer_url` not defined.

- [ ] **Step 3a: Add the entity fields**

In `models/Vdms.java`, after the `adc_configuration_id` field (around line 224), add:

```java
    @Column(length = 255)
    private String server_url;

    @Column(length = 255)
    private String credential_ref;
```

And add getters/setters (place alongside the other accessors, e.g. after `getAdc_configuration_id`/`setAdc_configuration_id`):

```java
    public String getServer_url() {
        return server_url;
    }

    public void setServer_url(String server_url) {
        this.server_url = server_url;
    }

    public String getCredential_ref() {
        return credential_ref;
    }

    public void setCredential_ref(String credential_ref) {
        this.credential_ref = credential_ref;
    }
```

- [ ] **Step 3b: Fix the single-row named queries**

In `models/Vdms.java`, replace the three `@NamedNativeQuery` query strings:

`Vdms.getVdmsDetails` (was `... FROM vdms`):
```java
        query = "SELECT vdms.id as id, vdms.property_name, vdms.address, vdms.city, vdms.country, vdms.state, vdms.zip, vdms.timezone, vdms.image_url, vdms.latitude, vdms.longitude, vdms.activation_timestamp, vdms.deployment_type, vdms.region FROM vdms WHERE vdms.id = :vdms_id",
```

`Vdms.getSyncDetailsForADC` (was `... FROM vdms LIMIT 1`):
```java
        query = "SELECT id, customer_org_id, adc_configuration_id, zip FROM vdms WHERE id = :vdms_id",
```

`Vdms.getVdmsMasterSlaveDetails` (was `... FROM vdms`):
```java
        query = "SELECT vdms.id as id, vdms.is_master, vdms.has_secondary_device, vdms.secondary_device_id, vdms.master_ip, vdms.slave_ip FROM vdms WHERE vdms.id = :vdms_id",
```

Leave `Vdms.getVdmsInfo` unchanged (it is a legitimate list query).

- [ ] **Step 3c: Add the migration backfill**

Append to `src/main/resources/data.sql` (idempotent — only fills NULLs on existing rows from the historical single-VDMS env defaults; Hibernate `ddl-auto: update` will have already added the columns):

```sql
-- Multi-VDMS migration: backfill per-VDMS config that previously lived in single-VDMS
-- env/config (sclera.vdms-server-url, secret_url). Idempotent: only sets rows still NULL.
UPDATE vdms
   SET server_url = COALESCE(server_url, 'http://localhost:8089/vdms')
 WHERE server_url IS NULL;

UPDATE vdms
   SET credential_ref = COALESCE(credential_ref, 'sclera/dev/edge/sclera-vdms-server/credential/')
 WHERE credential_ref IS NULL;
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsColumnsTest`
Expected: PASS (2 tests).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/models/Vdms.java \
        sclera-cloud-device-asset/src/main/resources/data.sql \
        sclera-cloud-device-asset/src/test/java/io/sclera/models/VdmsColumnsTest.java
git commit -m "feat(vdms): add per-VDMS server_url/credential_ref columns + id-scoped named queries + backfill"
```

---

### Task 3: `VdmsJpaRepository` (local DB access + child counts)

Spring Data repository over the existing `Vdms` entity, used by the de-stub impl and CRUD service.

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsJpaRepository.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/it/VdmsJpaRepositoryIT.java` (additive IT; Docker-gated)

**Interfaces:**
- Produces: `VdmsJpaRepository extends JpaRepository<Vdms, String>` with:
  - `List<String> findAllIds()`
  - `long countBuildingsByVdmsId(String vdmsId)`
  - `long countAssetsByVdmsId(String vdmsId)`

- [ ] **Step 1: Write the additive IT (Docker-gated; not the local gate)**

```java
package io.sclera.it;

import io.sclera.Repository.VdmsJpaRepository;
import io.sclera.models.Vdms;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VdmsJpaRepositoryIT {

    @Autowired VdmsJpaRepository repo;

    @Test
    void findAllIds_returnsSavedIds() {
        Vdms v = new Vdms();
        v.setId("VDMS-A");
        v.setProperty_name("A");
        repo.save(v);
        assertThat(repo.findAllIds()).contains("VDMS-A");
    }

    @Test
    void countBuildingsByVdmsId_zeroWhenNone() {
        Vdms v = new Vdms();
        v.setId("VDMS-B");
        repo.save(v);
        assertThat(repo.countBuildingsByVdmsId("VDMS-B")).isZero();
    }
}
```

- [ ] **Step 2: Run to verify it fails (compile)**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q -Dtest=VdmsJpaRepositoryIT test -DfailIfNoTests=false`
Expected: FAIL — `VdmsJpaRepository` does not exist (compilation error). (If Docker is unavailable the IT will not run to green; compilation failure is the signal here.)

- [ ] **Step 3: Write the repository**

```java
package io.sclera.Repository;

import io.sclera.models.Vdms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data access to the local {@code vdms} table for multi-VDMS reads, CRUD,
 * and referential-integrity checks used when deleting a VDMS.
 */
@Repository
public interface VdmsJpaRepository extends JpaRepository<Vdms, String> {

    @Query("SELECT v.id FROM Vdms v")
    List<String> findAllIds();

    @Query("SELECT COUNT(b) FROM Building b WHERE b.vdms.id = :vdmsId")
    long countBuildingsByVdmsId(@Param("vdmsId") String vdmsId);

    @Query("SELECT COUNT(a) FROM Asset a WHERE a.vdms.id = :vdmsId")
    long countAssetsByVdmsId(@Param("vdmsId") String vdmsId);
}
```

> Note: `Building` and `Asset` both have a `@ManyToOne Vdms vdms` (see `Vdms.building`/`Vdms.asset` mappedBy="vdms"). If the field name differs, adjust the JPQL path accordingly during implementation.

- [ ] **Step 4: Verify compilation**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsJpaRepository.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/it/VdmsJpaRepositoryIT.java
git commit -m "feat(vdms): add VdmsJpaRepository for local multi-VDMS reads and child counts"
```

---

### Task 4: De-stub `VdmsRepository` with a local JPA-backed impl

Adds per-id read methods + single-row fallback; backs reads with the local DB instead of the remote Dapr `vdms-service`.

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsRepository.java` (add default per-id methods)
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsRepositoryImpl.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/client/VdmsRepoClient.java` (remove `@Primary` so the local impl wins)
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/Repository/VdmsRepositoryImplTest.java`

**Interfaces:**
- Consumes: `VdmsJpaRepository` (Task 3).
- Produces: `VdmsRepository.findSingleVdmsId()` (returns the id iff exactly one row exists, else `null`); `VdmsRepositoryImpl` is the `@Primary` bean.

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.Repository;

import io.sclera.models.Vdms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VdmsRepositoryImplTest {

    @Mock VdmsJpaRepository jpa;
    @InjectMocks VdmsRepositoryImpl repo;

    @Test
    void findSingleVdmsId_returnsId_whenExactlyOne() {
        when(jpa.findAllIds()).thenReturn(List.of("VDMS760"));
        assertThat(repo.findSingleVdmsId()).isEqualTo("VDMS760");
    }

    @Test
    void findSingleVdmsId_null_whenMany() {
        when(jpa.findAllIds()).thenReturn(List.of("A", "B"));
        assertThat(repo.findSingleVdmsId()).isNull();
    }

    @Test
    void findSingleVdmsId_null_whenNone() {
        when(jpa.findAllIds()).thenReturn(List.of());
        assertThat(repo.findSingleVdmsId()).isNull();
    }

    @Test
    void getVDMSPasswordById_returnsRowPassword() {
        Vdms v = new Vdms();
        v.setId("A");
        v.setPassword("secret");
        when(jpa.findById("A")).thenReturn(Optional.of(v));
        assertThat(repo.getVDMSPassword("A")).isEqualTo("secret");
    }

    @Test
    void getIsMasterById_returnsRowFlag() {
        Vdms v = new Vdms();
        v.setId("A");
        v.setIs_master(1);
        when(jpa.findById("A")).thenReturn(Optional.of(v));
        assertThat(repo.getIsMaster("A")).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsRepositoryImplTest`
Expected: FAIL — `VdmsRepositoryImpl` / new methods do not exist.

- [ ] **Step 3a: Add default per-id methods to the interface**

In `Repository/VdmsRepository.java`, add these methods to the interface (default so the existing `VdmsRepoClient` implementor still compiles):

```java
    /**
     * Returns the single VDMS id iff exactly one VDMS row exists (backward-compat
     * fallback), otherwise {@code null}.
     */
    default String findSingleVdmsId() {
        return getVDMSId();
    }

    /** Returns the VDMS details for the given id. */
    default io.sclera.dto.touchscreen.settings.VdmsDTO getVdmsDetails(String vdmsId) {
        return getVdmsDetails();
    }

    /** Returns the VDMS password for the given id. */
    default String getVDMSPassword(String vdmsId) {
        return getVDMSPassword();
    }

    /** Returns the master flag for the given id. */
    default Integer getIsMaster(String vdmsId) {
        return getIsMaster();
    }
```

- [ ] **Step 3b: Write the local impl**

```java
package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.models.Vdms;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Local, database-backed implementation of {@link VdmsRepository}. Reads the
 * {@code vdms} table by id via {@link VdmsJpaRepository}, replacing the remote
 * single-VDMS Dapr delegation in {@code VdmsRepoClient}. Marked {@link Primary}
 * so it is the injected {@code VdmsRepository}.
 */
@Repository
@Primary
public class VdmsRepositoryImpl implements VdmsRepository {

    private final VdmsJpaRepository jpa;

    public VdmsRepositoryImpl(VdmsJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public String findSingleVdmsId() {
        List<String> ids = jpa.findAllIds();
        return ids.size() == 1 ? ids.get(0) : null;
    }

    @Override
    public String getVDMSId() {
        return findSingleVdmsId();
    }

    @Override
    public VdmsDTO getVdmsDetails() {
        String id = findSingleVdmsId();
        return id == null ? null : getVdmsDetails(id);
    }

    @Override
    public VdmsDTO getVdmsDetails(String vdmsId) {
        return jpa.findById(vdmsId).map(this::toDto).orElse(null);
    }

    @Override
    public String getVDMSPassword() {
        String id = findSingleVdmsId();
        return id == null ? null : getVDMSPassword(id);
    }

    @Override
    public String getVDMSPassword(String vdmsId) {
        return jpa.findById(vdmsId).map(Vdms::getPassword).orElse(null);
    }

    @Override
    public Integer getIsMaster() {
        String id = findSingleVdmsId();
        return id == null ? 0 : getIsMaster(id);
    }

    @Override
    public Integer getIsMaster(String vdmsId) {
        return jpa.findById(vdmsId).map(Vdms::getIs_master).orElse(0);
    }

    @Override
    public String getCustomerOrgIdByVdmsId(String vdms_id) {
        return jpa.findById(vdms_id).map(Vdms::getCustomer_org_id).orElse(null);
    }

    @Override
    public void updateCustomerOrgIdByVdmsId(String vdmsId, String customerOrgId) {
        jpa.findById(vdmsId).ifPresent(v -> {
            v.setCustomer_org_id(customerOrgId);
            jpa.save(v);
        });
    }

    @Override
    public VdmsDTO getSyncDetailsForADC() {
        String id = findSingleVdmsId();
        if (id == null) return null;
        return jpa.findById(id).map(v -> {
            VdmsDTO dto = new VdmsDTO();
            dto.setId(v.getId());
            dto.setCustomer_org_id(v.getCustomer_org_id());
            dto.setAdc_configuration_id(v.getAdc_configuration_id());
            dto.setZip(v.getZip());
            return dto;
        }).orElse(null);
    }

    private VdmsDTO toDto(Vdms v) {
        VdmsDTO dto = new VdmsDTO();
        dto.setId(v.getId());
        dto.setProperty_name(v.getProperty_name());
        dto.setAddress(v.getAddress());
        dto.setCity(v.getCity());
        dto.setCountry(v.getCountry());
        dto.setState(v.getState());
        dto.setZip(v.getZip());
        dto.setTimezone(v.getTimezone());
        dto.setImage_url(v.getImage_url());
        dto.setLatitude(v.getLatitude());
        dto.setLongitude(v.getLongitude());
        dto.setActivation_timestamp(v.getActivation_timestamp());
        dto.setDeployment_type(v.getDeployment_type());
        dto.setRegion(v.getRegion());
        return dto;
    }
}
```

> During implementation, verify the `VdmsDTO` setter names/types match (open `dto/touchscreen/settings/VdmsDTO.java`). If a setter is missing for a field, drop that line rather than inventing a setter — the DTO shape is authoritative.

- [ ] **Step 3c: Drop `@Primary` from the Dapr client**

In `client/VdmsRepoClient.java`, remove the `@Primary` annotation (line 20) and its import (line 9) so `VdmsRepositoryImpl` is the primary bean. Leave the class otherwise intact.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsRepositoryImplTest`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsRepositoryImpl.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/client/VdmsRepoClient.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/Repository/VdmsRepositoryImplTest.java
git commit -m "feat(vdms): back VdmsRepository with local DB by id + single-row fallback"
```

---

### Task 5: De-stub `VdmsconfigurationRepository` by id

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsconfigurationRepository.java` (add id-scoped method)
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsconfigurationRepositoryImpl.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/Repository/VdmsconfigurationRepositoryImplTest.java`

**Interfaces:**
- Consumes: `EntityManager` (JPA) to read `vdms_configuration` joined via `vdms`.
- Produces: `VdmsconfigurationRepository.getConfiguration(String vdmsId)`.

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VdmsconfigurationRepositoryImplTest {

    @Mock EntityManager em;
    @Mock Query query;
    @InjectMocks VdmsconfigurationRepositoryImpl repo;

    @Test
    void getConfigurationById_returnsNull_whenNoRow() {
        when(em.createNativeQuery(anyString(), eq("vdmsconfigmapping"))).thenReturn(query);
        when(query.setParameter(eq("vdms_id"), anyString())).thenReturn(query);
        when(query.getResultList()).thenReturn(java.util.List.of());
        VdmsConfigurationDTO cfg = repo.getConfiguration("VDMS760");
        assertThat(cfg).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsconfigurationRepositoryImplTest`
Expected: FAIL — `getConfiguration(String)` not defined; no `EntityManager` field.

- [ ] **Step 3a: Add the id-scoped method to the interface**

In `Repository/VdmsconfigurationRepository.java` add:

```java
    /**
     * Returns the VDMS network configuration for the given VDMS id.
     *
     * @param vdmsId the VDMS id
     * @return the configuration, or {@code null} if none
     */
    VdmsConfigurationDTO getConfiguration(String vdmsId);
```

- [ ] **Step 3b: Implement both methods with a `@SqlResultSetMapping`-free native query**

Replace `Repository/VdmsconfigurationRepositoryImpl.java` body:

```java
package io.sclera.Repository;

import io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Database-backed implementation of {@link VdmsconfigurationRepository}. Reads the
 * {@code vdms_configuration} row linked to a VDMS via {@code vdms.vdms_configuration_id}.
 */
@Repository
public class VdmsconfigurationRepositoryImpl implements VdmsconfigurationRepository {

    private static final Logger log = LoggerFactory.getLogger(VdmsconfigurationRepositoryImpl.class);

    private static final String SQL =
        "SELECT vc.id, vc.interface_id, vc.cidr, vc.gateway, vc.is_configured, vc.is_tagged, " +
        "vc.is_static, vc.primary_dns, vc.secondary_dns, vc.vlan_id, vc.private_ip, vc.mac_address, " +
        "vc.sclera_agent_permission " +
        "FROM vdms_configuration vc JOIN vdms v ON v.vdms_configuration_id = vc.id " +
        "WHERE v.id = :vdms_id";

    @PersistenceContext
    private EntityManager em;

    @Override
    public VdmsConfigurationDTO getConfiguration() {
        log.warn("VdmsconfigurationRepositoryImpl.getConfiguration() called without id; returning null");
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public VdmsConfigurationDTO getConfiguration(String vdmsId) {
        Query q = em.createNativeQuery(SQL, "vdmsconfigmapping");
        q.setParameter("vdms_id", vdmsId);
        List<VdmsConfigurationDTO> rows = q.getResultList();
        return rows.isEmpty() ? null : rows.get(0);
    }
}
```

- [ ] **Step 3c: Register the result-set mapping**

The native query above references a `@SqlResultSetMapping` named `vdmsconfigmapping`. Add it to `models/Vdms.java` (alongside the existing mappings), mapping to the existing `VdmsConfigurationDTO(id, interface_id, cidr, gateway, isConfigured, isTagged, isStatic, primary_dns, secondary_dns, vlan_id, private_ip, mac_address, sclera_agent_permission)` 13-arg constructor:

```java
@SqlResultSetMapping(
        name = "vdmsconfigmapping",
        classes = {
                @ConstructorResult(
                        targetClass = io.sclera.dto.touchscreen.settings.VdmsConfigurationDTO.class,
                        columns = {
                                @ColumnResult(name = "id", type = String.class),
                                @ColumnResult(name = "interface_id", type = String.class),
                                @ColumnResult(name = "cidr", type = Integer.class),
                                @ColumnResult(name = "gateway", type = String.class),
                                @ColumnResult(name = "is_configured", type = Boolean.class),
                                @ColumnResult(name = "is_tagged", type = Boolean.class),
                                @ColumnResult(name = "is_static", type = Boolean.class),
                                @ColumnResult(name = "primary_dns", type = String.class),
                                @ColumnResult(name = "secondary_dns", type = String.class),
                                @ColumnResult(name = "vlan_id", type = Integer.class),
                                @ColumnResult(name = "private_ip", type = String.class),
                                @ColumnResult(name = "mac_address", type = String.class),
                                @ColumnResult(name = "sclera_agent_permission", type = String.class)
                        }
                )
        }
)
```

> The mapping must sit on an `@Entity` class. `Vdms` already hosts several `@SqlResultSetMapping`s, so add it there. Verify the actual `vdms_configuration` column names during implementation (open the entity if one exists, else the DB) and adjust `SQL`/`@ColumnResult` names to match.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsconfigurationRepositoryImplTest`
Expected: PASS (1 test).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsconfigurationRepository.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/Repository/VdmsconfigurationRepositoryImpl.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/models/Vdms.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/Repository/VdmsconfigurationRepositoryImplTest.java
git commit -m "feat(vdms): read vdms_configuration by vdms id"
```

---

### Task 6: `VdmsContextInterceptor` + registration

Resolves the request's VDMS with precedence header → param → single-row fallback.

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/web/VdmsContextInterceptor.java`
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/config/VdmsWebConfig.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/web/VdmsContextInterceptorTest.java`

**Interfaces:**
- Consumes: `VdmsContext` (Task 1), `VdmsRepository.findSingleVdmsId()` (Task 4).
- Produces: `VdmsContextInterceptor` populating `VdmsContext`; header constant `X-Vdms-Id`; param name `vdms_id`.

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.web;

import io.sclera.Repository.VdmsRepository;
import io.sclera.utils.VdmsContext;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VdmsContextInterceptorTest {

    @Mock HttpServletRequest request;
    @Mock VdmsRepository vdmsRepository;

    private VdmsContextInterceptor newInterceptor(VdmsContext ctx) {
        return new VdmsContextInterceptor(ctx, vdmsRepository);
    }

    @Test
    void header_takesPrecedence() {
        VdmsContext ctx = new VdmsContext();
        when(request.getHeader("X-Vdms-Id")).thenReturn("H1");
        lenient().when(request.getParameter("vdms_id")).thenReturn("P1");
        newInterceptor(ctx).preHandle(request, null, null);
        assertThat(ctx.getVdmsId()).isEqualTo("H1");
    }

    @Test
    void param_usedWhenNoHeader() {
        VdmsContext ctx = new VdmsContext();
        when(request.getHeader("X-Vdms-Id")).thenReturn(null);
        when(request.getParameter("vdms_id")).thenReturn("P1");
        newInterceptor(ctx).preHandle(request, null, null);
        assertThat(ctx.getVdmsId()).isEqualTo("P1");
    }

    @Test
    void singleRowFallback_whenNoHeaderNoParam() {
        VdmsContext ctx = new VdmsContext();
        when(request.getHeader("X-Vdms-Id")).thenReturn(null);
        when(request.getParameter("vdms_id")).thenReturn(null);
        when(vdmsRepository.findSingleVdmsId()).thenReturn("ONLY");
        newInterceptor(ctx).preHandle(request, null, null);
        assertThat(ctx.getVdmsId()).isEqualTo("ONLY");
    }

    @Test
    void unset_whenNothingResolves() {
        VdmsContext ctx = new VdmsContext();
        when(request.getHeader("X-Vdms-Id")).thenReturn(null);
        when(request.getParameter("vdms_id")).thenReturn(null);
        when(vdmsRepository.findSingleVdmsId()).thenReturn(null);
        newInterceptor(ctx).preHandle(request, null, null);
        assertThat(ctx.hasVdms()).isFalse();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsContextInterceptorTest`
Expected: FAIL — `VdmsContextInterceptor` does not exist.

- [ ] **Step 3a: Write the interceptor**

```java
package io.sclera.web;

import io.sclera.Repository.VdmsRepository;
import io.sclera.utils.VdmsContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Resolves the target VDMS for each request and stores it in the request-scoped
 * {@link VdmsContext}. Precedence: {@code X-Vdms-Id} header, then {@code vdms_id}
 * request parameter, then the single existing VDMS (backward compatibility).
 */
@Component
public class VdmsContextInterceptor implements HandlerInterceptor {

    public static final String HEADER = "X-Vdms-Id";
    public static final String PARAM = "vdms_id";

    private final VdmsContext vdmsContext;
    private final VdmsRepository vdmsRepository;

    public VdmsContextInterceptor(VdmsContext vdmsContext, VdmsRepository vdmsRepository) {
        this.vdmsContext = vdmsContext;
        this.vdmsRepository = vdmsRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String id = request.getHeader(HEADER);
        if (isBlank(id)) {
            id = request.getParameter(PARAM);
        }
        if (isBlank(id)) {
            id = vdmsRepository.findSingleVdmsId();
        }
        if (!isBlank(id)) {
            vdmsContext.setVdmsId(id);
        }
        return true;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
```

- [ ] **Step 3b: Register the interceptor**

```java
package io.sclera.config;

import io.sclera.web.VdmsContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers {@link VdmsContextInterceptor} so every API request resolves its VDMS
 * into the request-scoped {@code VdmsContext}. Separate from {@code ResourceConfigs}
 * (which handles static resources) to keep one responsibility per configurer.
 */
@Configuration
public class VdmsWebConfig implements WebMvcConfigurer {

    private final VdmsContextInterceptor vdmsContextInterceptor;

    public VdmsWebConfig(VdmsContextInterceptor vdmsContextInterceptor) {
        this.vdmsContextInterceptor = vdmsContextInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(vdmsContextInterceptor)
                .addPathPatterns("/api/v1/sclera-cloud-device-asset-service/**");
    }
}
```

> `ResourceConfigs` already carries `@EnableWebMvc`; multiple `WebMvcConfigurer` beans compose, so this registration is additive. Confirm the app starts and the interceptor is invoked once.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsContextInterceptorTest`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/web/VdmsContextInterceptor.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/config/VdmsWebConfig.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/web/VdmsContextInterceptorTest.java
git commit -m "feat(vdms): resolve per-request VDMS via interceptor (header/param/single-row)"
```

---

### Task 7: Remove the singleton reads in `DeviceService`

Replaces `authenticationUtils.getVdms_id()` in the two filter methods with an explicit `vdms_id` parameter.

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java:6435-6462` and `6468-6493`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/interfaces/DeviceServiceInterface.java:235-236`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/service/DeviceServiceFilterVdmsTest.java`

**Interfaces:**
- Produces (changed signatures):
  - `List<String> getDeviceIdsByFilter(String vdms_id, List<String> dockerNames, List<String> types, String searchKey, List<String> virtual_device_types, Boolean isTaggedToQrCode, Boolean isTaggedToNfc, List<String> locationIds)`
  - `List<DeviceDTO> getDevicesByFilter(String vdms_id, List<String> dockerNames, List<String> types, String searchKey, List<String> virtual_device_types, Boolean isTaggedToQrCode, Boolean isTaggedToNfc, List<String> locationIds, List<String> deviceIds)`

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.service;

import io.sclera.Repository.DeviceRepository;
import io.sclera.service.touchscreen.ClientQrCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceFilterVdmsTest {

    @Mock DeviceRepository deviceRepository;
    @Mock QrCodeService qrCodeService;
    @Mock ClientQrCodeService clientQrCodeService;
    @Mock NfcService nfcService;
    @Mock ClientNfcService clientNfcService;

    @InjectMocks DeviceService deviceService;

    @Test
    void getDeviceIdsByFilter_usesPassedVdmsId_forQrLookup() {
        when(qrCodeService.getDeviceIdsTaggedToQrCode("V-ARG")).thenReturn(List.of("d1"));
        when(clientQrCodeService.getDeviceIdsTaggedToClientQrCode("V-ARG")).thenReturn(List.of());

        deviceService.getDeviceIdsByFilter("V-ARG", List.of(), List.of(), null, List.of(),
                Boolean.TRUE, null, List.of());

        verify(qrCodeService).getDeviceIdsTaggedToQrCode(eq("V-ARG"));
        verify(deviceRepository).getDeviceIds(any(), any(), any(), any(), eq(Boolean.TRUE), any(), any(), any(), any());
    }
}
```

> During implementation, adjust the `@Mock` types/names to the actual field types in `DeviceService` (e.g. the concrete `QrCodeService`, `NfcService`, `ClientQrCodeService`, `ClientNfcService` beans). Only the collaborators touched by these two methods need mocking; Mockito leaves the rest null.

- [ ] **Step 2: Run test to verify it fails**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=DeviceServiceFilterVdmsTest`
Expected: FAIL — method signature takes no `vdms_id`.

- [ ] **Step 3a: Change `DeviceService.getDeviceIdsByFilter`**

At line 6435, change the signature and remove the singleton read at line 6438:

```java
    public List<String> getDeviceIdsByFilter(String vdms_id, List<String> dockerNames, List<String> types, String searchKey,
                                             List<String> virtual_device_types, Boolean isTaggedToQrCode,
                                             Boolean isTaggedToNfc, List<String> locationIds) {
        String vdmsid = vdms_id;
```

(Leave the rest of the method body unchanged — it already uses the local `vdmsid` variable.)

- [ ] **Step 3b: Change `DeviceService.getDevicesByFilter`**

At line 6468, change the signature and remove the singleton read at line 6471:

```java
    public List<DeviceDTO> getDevicesByFilter(String vdms_id, List<String> dockerNames, List<String> types, String
            searchKey, List<String> virtual_device_types,
                                              Boolean isTaggedToQrCode, Boolean isTaggedToNfc, List<String> locationIds, List<String> deviceIds) {
        String vdmsid = vdms_id;
```

- [ ] **Step 3c: Update the interface**

In `interfaces/DeviceServiceInterface.java` lines 235-236, prepend `String vdms_id,` to both method signatures to match.

- [ ] **Step 3d: Update callers**

Run: `cd sclera-cloud-device-asset && grep -rn "getDeviceIdsByFilter\|getDevicesByFilter" src/main src/test --include=*.java | grep -v "DeviceService.java\|DeviceServiceInterface.java\|DeviceRepository.java\|Device.java"`
For each caller found, pass the request VDMS as the new first argument: inject `VdmsContext` and pass `vdmsContext.getVdmsId()` (controllers already have a `vdms_id`/`vdmsid` param — pass that instead). If no callers are found, note it and continue.

- [ ] **Step 4: Run test to verify it passes**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=DeviceServiceFilterVdmsTest`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/interfaces/DeviceServiceInterface.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/service/DeviceServiceFilterVdmsTest.java
git commit -m "refactor(vdms): pass explicit vdms_id into device filter methods (drop singleton read)"
```

---

### Task 8: Retire the `AuthenticationUtils` VDMS singleton fields

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/utils/AuthenticationUtils.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/service/touchscreen/VdmsService.java:92-105` (`startVdmsService`)
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/service/touchscreen/VdmsServiceTest.java` (existing — keep green)

**Interfaces:**
- Produces: `AuthenticationUtils` no longer exposes `getVdms_id/setVdms_id/getAccess_token/setAccess_token/getRefresh_token/setRefresh_token`. `startVdmsService()` becomes a no-op (the boot-time singleton resolution is removed).

- [ ] **Step 1: Confirm remaining consumers**

Run: `cd sclera-cloud-device-asset && grep -rn "getVdms_id\|setVdms_id\|getAccess_token\|setAccess_token\|getRefresh_token\|setRefresh_token" src/main --include=*.java`
Expected after Task 7: only `AuthenticationUtils.java` (definitions) and `VdmsService.startVdmsService`. If any OTHER consumer appears, convert it to read `VdmsContext.getVdmsId()` (inject `VdmsContext`) before proceeding — list each and handle it.

- [ ] **Step 2: Make `startVdmsService` a no-op and drop singleton use**

Replace the body of `VdmsService.startVdmsService()` (lines 92-105) with:

```java
    public void startVdmsService() {
        // Multi-VDMS: no process-global VDMS identity is resolved at boot. The target
        // VDMS is resolved per request by VdmsContextInterceptor. Access tokens (if/when
        // implemented) are fetched per VDMS on demand, not once at startup.
        log.info("startVdmsService: no-op under multi-VDMS (per-request resolution)");
    }
```

Remove the now-unused `authenticationUtils` field/usages in `VdmsService` ONLY if nothing else in the class references it (grep within the file first); otherwise leave the field.

- [ ] **Step 3: Delete the singleton fields from `AuthenticationUtils`**

Edit `utils/AuthenticationUtils.java` to remove `vdms_id`, `access_token`, `refresh_token` and their getters/setters. Keep `devuid` and `public_key` (verify they still have consumers via grep; if not, remove them too). Resulting class keeps only fields with live consumers.

- [ ] **Step 4: Run the affected tests**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsServiceTest`
Then compile everything: `./mvnw -o -q -DskipTests compile`
Expected: VdmsServiceTest PASS; compile SUCCESS (no lingering references to removed accessors).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/utils/AuthenticationUtils.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/service/touchscreen/VdmsService.java
git commit -m "refactor(vdms): retire AuthenticationUtils VDMS singleton + boot resolution"
```

---

### Task 9: `VdmsCrudDTO` + `VdmsCrudService` (list/get/create/update/delete)

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/dto/VdmsCrudDTO.java`
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/service/VdmsCrudService.java`
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/exception/VdmsInUseException.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/service/VdmsCrudServiceTest.java`

**Interfaces:**
- Consumes: `VdmsJpaRepository` (Task 3).
- Produces: `VdmsCrudService` with `List<Vdms> list()`, `Vdms get(String id)`, `Vdms create(VdmsCrudDTO)`, `Vdms update(String id, VdmsCrudDTO)`, `void delete(String id)`. `delete` throws `VdmsInUseException` when child buildings/assets exist. `get`/`update`/`delete` throw `java.util.NoSuchElementException` when the id is unknown.

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.service;

import io.sclera.Repository.VdmsJpaRepository;
import io.sclera.dto.VdmsCrudDTO;
import io.sclera.exception.VdmsInUseException;
import io.sclera.models.Vdms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VdmsCrudServiceTest {

    @Mock VdmsJpaRepository jpa;
    @InjectMocks VdmsCrudService service;

    @Test
    void create_persistsWithGivenId() {
        VdmsCrudDTO dto = new VdmsCrudDTO();
        dto.setId("V1");
        dto.setProperty_name("Site 1");
        dto.setServer_url("http://vdms-1:8089/vdms");
        when(jpa.existsById("V1")).thenReturn(false);
        when(jpa.save(any(Vdms.class))).thenAnswer(i -> i.getArgument(0));

        Vdms saved = service.create(dto);

        assertThat(saved.getId()).isEqualTo("V1");
        assertThat(saved.getProperty_name()).isEqualTo("Site 1");
        assertThat(saved.getServer_url()).isEqualTo("http://vdms-1:8089/vdms");
    }

    @Test
    void create_rejectsDuplicateId() {
        VdmsCrudDTO dto = new VdmsCrudDTO();
        dto.setId("V1");
        when(jpa.existsById("V1")).thenReturn(true);
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(IllegalArgumentException.class);
        verify(jpa, never()).save(any());
    }

    @Test
    void delete_blocksWhenBuildingsExist() {
        when(jpa.existsById("V1")).thenReturn(true);
        when(jpa.countBuildingsByVdmsId("V1")).thenReturn(2L);
        assertThatThrownBy(() -> service.delete("V1")).isInstanceOf(VdmsInUseException.class);
        verify(jpa, never()).deleteById(any());
    }

    @Test
    void delete_succeedsWhenNoChildren() {
        when(jpa.existsById("V1")).thenReturn(true);
        when(jpa.countBuildingsByVdmsId("V1")).thenReturn(0L);
        when(jpa.countAssetsByVdmsId("V1")).thenReturn(0L);
        service.delete("V1");
        verify(jpa).deleteById("V1");
    }

    @Test
    void update_mutatesEditableFields() {
        Vdms existing = new Vdms();
        existing.setId("V1");
        existing.setProperty_name("Old");
        when(jpa.findById("V1")).thenReturn(Optional.of(existing));
        when(jpa.save(any(Vdms.class))).thenAnswer(i -> i.getArgument(0));

        VdmsCrudDTO dto = new VdmsCrudDTO();
        dto.setProperty_name("New");
        Vdms updated = service.update("V1", dto);

        assertThat(updated.getProperty_name()).isEqualTo("New");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsCrudServiceTest`
Expected: FAIL — DTO/service/exception missing.

- [ ] **Step 3a: Write the exception**

```java
package io.sclera.exception;

/** Thrown when a VDMS cannot be deleted because dependent records still reference it. */
public class VdmsInUseException extends RuntimeException {
    public VdmsInUseException(String message) {
        super(message);
    }
}
```

- [ ] **Step 3b: Write the DTO with validation**

```java
package io.sclera.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create/update payload for a VDMS record. */
public class VdmsCrudDTO {

    @NotBlank
    @Size(max = 64)
    private String id;

    @NotBlank
    @Size(max = 128)
    private String property_name;

    @Size(max = 255)
    private String server_url;

    @Size(max = 255)
    private String credential_ref;

    @Size(max = 128)
    private String timezone;

    @Size(max = 16)
    private String deployment_type;

    @Size(max = 32)
    private String region;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProperty_name() { return property_name; }
    public void setProperty_name(String property_name) { this.property_name = property_name; }
    public String getServer_url() { return server_url; }
    public void setServer_url(String server_url) { this.server_url = server_url; }
    public String getCredential_ref() { return credential_ref; }
    public void setCredential_ref(String credential_ref) { this.credential_ref = credential_ref; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public String getDeployment_type() { return deployment_type; }
    public void setDeployment_type(String deployment_type) { this.deployment_type = deployment_type; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
}
```

- [ ] **Step 3c: Write the service**

```java
package io.sclera.service;

import io.sclera.Repository.VdmsJpaRepository;
import io.sclera.dto.VdmsCrudDTO;
import io.sclera.exception.VdmsInUseException;
import io.sclera.models.Vdms;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * CRUD lifecycle for VDMS records. Create rejects duplicate ids; delete is blocked
 * while dependent buildings/assets reference the VDMS.
 */
@Service
public class VdmsCrudService {

    private final VdmsJpaRepository jpa;

    public VdmsCrudService(VdmsJpaRepository jpa) {
        this.jpa = jpa;
    }

    public List<Vdms> list() {
        return jpa.findAll();
    }

    public Vdms get(String id) {
        return jpa.findById(id)
                .orElseThrow(() -> new NoSuchElementException("VDMS not found: " + id));
    }

    public Vdms create(VdmsCrudDTO dto) {
        if (jpa.existsById(dto.getId())) {
            throw new IllegalArgumentException("VDMS already exists: " + dto.getId());
        }
        Vdms v = new Vdms();
        v.setId(dto.getId());
        apply(v, dto);
        return jpa.save(v);
    }

    public Vdms update(String id, VdmsCrudDTO dto) {
        Vdms v = get(id);
        apply(v, dto);
        return jpa.save(v);
    }

    public void delete(String id) {
        if (!jpa.existsById(id)) {
            throw new NoSuchElementException("VDMS not found: " + id);
        }
        if (jpa.countBuildingsByVdmsId(id) > 0 || jpa.countAssetsByVdmsId(id) > 0) {
            throw new VdmsInUseException("VDMS " + id + " has dependent buildings/assets");
        }
        jpa.deleteById(id);
    }

    /** Copies non-null editable fields from the DTO onto the entity. */
    private void apply(Vdms v, VdmsCrudDTO dto) {
        if (dto.getProperty_name() != null) v.setProperty_name(dto.getProperty_name());
        if (dto.getServer_url() != null) v.setServer_url(dto.getServer_url());
        if (dto.getCredential_ref() != null) v.setCredential_ref(dto.getCredential_ref());
        if (dto.getTimezone() != null) v.setTimezone(dto.getTimezone());
        if (dto.getDeployment_type() != null) v.setDeployment_type(dto.getDeployment_type());
        if (dto.getRegion() != null) v.setRegion(dto.getRegion());
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsCrudServiceTest`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/dto/VdmsCrudDTO.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/service/VdmsCrudService.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/exception/VdmsInUseException.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/service/VdmsCrudServiceTest.java
git commit -m "feat(vdms): CRUD service with validation + delete-in-use guard"
```

---

### Task 10: `VdmsController` REST endpoints

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/controller/admin/VdmsController.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/controller/admin/VdmsControllerTest.java`

**Interfaces:**
- Consumes: `VdmsCrudService` (Task 9).
- Produces: `GET /vdms`, `GET /vdms/{id}`, `POST /vdms`, `PUT /vdms/{id}`, `DELETE /vdms/{id}` under `/api/v1/sclera-cloud-device-asset-service`. `VdmsInUseException` → `409`; `NoSuchElementException` → `404`; validation failure → `400`.

- [ ] **Step 1: Write the failing test (MockMvc standalone)**

```java
package io.sclera.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.sclera.dto.VdmsCrudDTO;
import io.sclera.exception.VdmsInUseException;
import io.sclera.models.Vdms;
import io.sclera.service.VdmsCrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VdmsControllerTest {

    MockMvc mvc;
    VdmsCrudService service;
    final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setup() {
        service = Mockito.mock(VdmsCrudService.class);
        mvc = MockMvcBuilders.standaloneSetup(new VdmsController(service))
                .setControllerAdvice(new io.sclera.controller.advice.VdmsExceptionAdvice())
                .build();
    }

    @Test
    void list_returnsOk() throws Exception {
        Vdms v = new Vdms(); v.setId("V1");
        when(service.list()).thenReturn(List.of(v));
        mvc.perform(get("/api/v1/sclera-cloud-device-asset-service/vdms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("V1"));
    }

    @Test
    void create_returnsCreated() throws Exception {
        Vdms v = new Vdms(); v.setId("V1"); v.setProperty_name("Site 1");
        when(service.create(any(VdmsCrudDTO.class))).thenReturn(v);
        VdmsCrudDTO dto = new VdmsCrudDTO(); dto.setId("V1"); dto.setProperty_name("Site 1");
        mvc.perform(post("/api/v1/sclera-cloud-device-asset-service/vdms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("V1"));
    }

    @Test
    void delete_inUse_returns409() throws Exception {
        doThrow(new VdmsInUseException("in use")).when(service).delete(eq("V1"));
        mvc.perform(delete("/api/v1/sclera-cloud-device-asset-service/vdms/V1"))
                .andExpect(status().isConflict());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsControllerTest`
Expected: FAIL — `VdmsController` and `VdmsExceptionAdvice` do not exist.

- [ ] **Step 3a: Write the exception advice**

```java
package io.sclera.controller.advice;

import io.sclera.exception.VdmsInUseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/** Maps VDMS lifecycle exceptions to HTTP status codes. */
@RestControllerAdvice
public class VdmsExceptionAdvice {

    @ExceptionHandler(VdmsInUseException.class)
    public ResponseEntity<String> inUse(VdmsInUseException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> notFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
```

- [ ] **Step 3b: Write the controller**

```java
package io.sclera.controller.admin;

import io.sclera.dto.VdmsCrudDTO;
import io.sclera.models.Vdms;
import io.sclera.service.VdmsCrudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CRUD REST API for VDMS records. Enables managing multiple VDMS configurations
 * from a single running service.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Validated
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class VdmsController {

    private final VdmsCrudService service;

    public VdmsController(VdmsCrudService service) {
        this.service = service;
    }

    @GetMapping("/vdms")
    public List<Vdms> list() {
        return service.list();
    }

    @GetMapping("/vdms/{id}")
    public Vdms get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping("/vdms")
    public ResponseEntity<Vdms> create(@Valid @RequestBody VdmsCrudDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/vdms/{id}")
    public Vdms update(@PathVariable String id, @Valid @RequestBody VdmsCrudDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/vdms/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest=VdmsControllerTest`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/controller/admin/VdmsController.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/controller/advice/VdmsExceptionAdvice.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/controller/admin/VdmsControllerTest.java
git commit -m "feat(vdms): REST CRUD controller + exception-to-status mapping"
```

---

### Task 11: Full backend build + config cleanup

Validates the whole module compiles/tests, and documents the env→DB move.

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/resources/application-docker.yml` (comment the now-DB-backed keys)
- Modify: `.env.example` (add note)

- [ ] **Step 1: Run the full unit-test suite**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q test -Dtest='!*IT'`
Expected: BUILD SUCCESS (all unit tests green; ITs excluded per the Docker-free constraint).

- [ ] **Step 2: Annotate the migrated config**

In `application-docker.yml`, above `vdms-server-url` (line 70) and `secret_url` (line 117), add a comment noting these are now per-VDMS DB columns (`vdms.server_url`, `vdms.credential_ref`) and remain only as migration/backfill defaults:

```yaml
  # MIGRATED to per-VDMS DB columns (vdms.server_url / vdms.credential_ref). Kept only as the
  # default used to backfill the existing single row (see data.sql). New VDMS get these via CRUD.
  vdms-server-url: http://localhost:8089/vdms
```

(Repeat the comment above `secret_url`.)

- [ ] **Step 3: Note in `.env.example`**

Append a comment line to `.env.example`:

```bash
# VDMS server URL and credentials are now per-VDMS DB columns (vdms.server_url / vdms.credential_ref),
# managed via the /vdms CRUD API. No single-VDMS env var is required.
```

- [ ] **Step 4: Verify build once more**

Run: `cd sclera-cloud-device-asset && ./mvnw -o -q -DskipTests package`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/resources/application-docker.yml .env.example
git commit -m "docs(vdms): mark single-VDMS config as migrated to per-VDMS DB columns"
```

---

### Task 12: UI — selectable, persisted VDMS + `X-Vdms-Id` header

**Files:**
- Modify: `sclera-ui/src/context/AppContext.jsx`
- Modify: `sclera-ui/src/services/api.js`

**Interfaces:**
- Produces: `useApp()` context gains `vdmsId` (string) and `setVdmsId(fn)`. `api.js` `request()` sends `X-Vdms-Id` from `localStorage['sclera.vdmsId']`. New `api.vdmsAdmin` functions: `list()`, `get(id)`, `create(body)`, `update(id, body)`, `remove(id)`.

- [ ] **Step 1: Make `vdmsId` stateful + persisted in `AppContext.jsx`**

Replace the `const [ctx] = useState(...)` line (line 20) and add persistence:

```jsx
  const [vdmsId, setVdmsIdState] = useState(() => localStorage.getItem('sclera.vdmsId') || DEMO.vdmsId)
  const setVdmsId = (id) => { setVdmsIdState(id); localStorage.setItem('sclera.vdmsId', id) }
  const ctx = { user: DEMO.user, vdmsId, docker: DEMO.docker }

  useEffect(() => { localStorage.setItem('sclera.vdmsId', vdmsId) }, [vdmsId])
```

Update the provider value (line 29) to expose them:

```jsx
    <AppCtx.Provider value={{ nav, setNav, view, setView, property, setProperty, ctx, vdmsId, setVdmsId }}>
```

- [ ] **Step 2: Send `X-Vdms-Id` from `api.js` and add admin functions**

In `services/api.js`, inside `request()` where headers are built (line 23), merge the stored VDMS id:

```js
      headers: {
        'Content-Type': 'application/json',
        ...(typeof localStorage !== 'undefined' && localStorage.getItem('sclera.vdmsId')
            ? { 'X-Vdms-Id': localStorage.getItem('sclera.vdmsId') } : {}),
        ...(headers || {}),
      },
```

At the end of the file, add and export the admin API (using the existing `asset()` helper + `request`):

```js
export const vdmsAdmin = {
  list: () => request(asset('/vdms')),
  get: (id) => request(asset(`/vdms/${encodeURIComponent(id)}`)),
  create: (body) => request(asset('/vdms'), { method: 'POST', body }),
  update: (id, body) => request(asset(`/vdms/${encodeURIComponent(id)}`), { method: 'PUT', body }),
  remove: (id) => request(asset(`/vdms/${encodeURIComponent(id)}`), { method: 'DELETE' }),
}
```

> If `api.js` uses a single default export object, add `vdmsAdmin` as a property of it instead of a named export — match the file's existing export style.

- [ ] **Step 3: Build the UI**

Run: `cd sclera-ui && npm run build`
Expected: build succeeds with no errors.

- [ ] **Step 4: Manual verification**

Run the UI container per the Dockerized-UI deploy notes (rebuild image; `npm run dev` won't bind :3000). In the browser devtools Network tab, confirm device API calls now carry an `X-Vdms-Id` request header equal to the persisted value.

- [ ] **Step 5: Commit**

```bash
git add sclera-ui/src/context/AppContext.jsx sclera-ui/src/services/api.js
git commit -m "feat(ui): selectable persisted VDMS id + X-Vdms-Id header"
```

---

### Task 13: UI — VDMS management page + selector

**Files:**
- Create: `sclera-ui/src/pages/VdmsPage.jsx`
- Create: `sclera-ui/src/components/VdmsSelector.jsx`
- Modify: `sclera-ui/src/config.js` (add nav item), and the app shell that renders pages by `view` (e.g. the component switching on `view`; find via `grep -rn "view ===" sclera-ui/src`).

**Interfaces:**
- Consumes: `vdmsAdmin` + `useApp()` (Task 12).
- Produces: a `vdms` view listing/creating/editing/deleting VDMS; a header selector that calls `setVdmsId`.

- [ ] **Step 1: Add the nav item**

In `config.js` `NAV_ITEMS` (line 23), add:

```js
  { key: 'vdms', label: 'VDMS', icon: 'globe' },
```

And add `'vdms'` to `AppContext.jsx` `KNOWN_VIEWS` (line 9).

- [ ] **Step 2: Write the selector component**

```jsx
import { useEffect, useState } from 'react'
import { useApp } from '../context/AppContext.jsx'
import { vdmsAdmin } from '../services/api.js'

// Header dropdown to pick the active VDMS. Persists via setVdmsId (writes localStorage,
// which api.js reads into the X-Vdms-Id header).
export default function VdmsSelector() {
  const { vdmsId, setVdmsId } = useApp()
  const [options, setOptions] = useState([])

  useEffect(() => {
    vdmsAdmin.list().then((rows) => setOptions(Array.isArray(rows) ? rows : [])).catch(() => setOptions([]))
  }, [])

  return (
    <select value={vdmsId} onChange={(e) => setVdmsId(e.target.value)} aria-label="Select VDMS">
      {options.length === 0 && <option value={vdmsId}>{vdmsId}</option>}
      {options.map((v) => (
        <option key={v.id} value={v.id}>{v.property_name || v.id}</option>
      ))}
    </select>
  )
}
```

- [ ] **Step 3: Write the management page**

```jsx
import { useEffect, useState } from 'react'
import { vdmsAdmin } from '../services/api.js'

const EMPTY = { id: '', property_name: '', server_url: '', credential_ref: '', timezone: '', deployment_type: '', region: '' }

// List + create/edit/delete VDMS records. Mirrors the simple admin CRUD used elsewhere in the POC.
export default function VdmsPage() {
  const [rows, setRows] = useState([])
  const [form, setForm] = useState(EMPTY)
  const [editing, setEditing] = useState(false)
  const [error, setError] = useState(null)

  const load = () => vdmsAdmin.list().then((r) => setRows(Array.isArray(r) ? r : [])).catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  const submit = async (e) => {
    e.preventDefault()
    setError(null)
    try {
      if (editing) await vdmsAdmin.update(form.id, form)
      else await vdmsAdmin.create(form)
      setForm(EMPTY); setEditing(false); load()
    } catch (err) { setError(err.message) }
  }

  const edit = (v) => { setForm({ ...EMPTY, ...v }); setEditing(true) }
  const remove = async (id) => {
    setError(null)
    try { await vdmsAdmin.remove(id); load() } catch (err) { setError(err.message) }
  }

  return (
    <div>
      <h2>VDMS</h2>
      {error && <p role="alert">{error}</p>}
      <table>
        <thead><tr><th>ID</th><th>Property</th><th>Server URL</th><th></th></tr></thead>
        <tbody>
          {rows.map((v) => (
            <tr key={v.id}>
              <td>{v.id}</td><td>{v.property_name}</td><td>{v.server_url}</td>
              <td>
                <button onClick={() => edit(v)}>Edit</button>
                <button onClick={() => remove(v.id)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <form onSubmit={submit}>
        <input placeholder="id" value={form.id} disabled={editing}
               onChange={(e) => setForm({ ...form, id: e.target.value })} required />
        <input placeholder="property name" value={form.property_name}
               onChange={(e) => setForm({ ...form, property_name: e.target.value })} required />
        <input placeholder="server url" value={form.server_url}
               onChange={(e) => setForm({ ...form, server_url: e.target.value })} />
        <input placeholder="credential ref" value={form.credential_ref}
               onChange={(e) => setForm({ ...form, credential_ref: e.target.value })} />
        <button type="submit">{editing ? 'Update' : 'Create'}</button>
        {editing && <button type="button" onClick={() => { setForm(EMPTY); setEditing(false) }}>Cancel</button>}
      </form>
    </div>
  )
}
```

- [ ] **Step 4: Wire the page + selector into the shell**

Find the view switch: `grep -rn "view === 'qrcodes'" sclera-ui/src`. In that component, add a branch rendering `<VdmsPage />` when `view === 'vdms'`, and place `<VdmsSelector />` in the header/topbar (same file or the layout component that renders the header). Import both. Then build:

Run: `cd sclera-ui && npm run build`
Expected: build succeeds.

- [ ] **Step 5: Manual verification + commit**

Rebuild the UI image, open the app, select the `VDMS` nav item, create a second VDMS, switch the header selector to it, and confirm subsequent list calls send the new `X-Vdms-Id`. Then:

```bash
git add sclera-ui/src/pages/VdmsPage.jsx sclera-ui/src/components/VdmsSelector.jsx \
        sclera-ui/src/config.js sclera-ui/src/context/AppContext.jsx
git commit -m "feat(ui): VDMS management page + header selector"
```

---

## Self-Review

**1. Spec coverage:**
- Multiple VDMS records in DB → Tasks 2, 3, 9. ✅
- Per-VDMS config/credentials/settings → Tasks 2 (columns), 5 (config by id). ✅
- CRUD (create/update/delete/list) → Tasks 9, 10. ✅
- Load correct VDMS per request → Tasks 1, 6. ✅
- Remove singleton assumptions → Tasks 4 (repo LIMIT-1/no-WHERE + Dapr single-VDMS), 6, 7 (DeviceService), 8 (AuthenticationUtils). ✅
- Env → DB, keep global in env → Tasks 2 (backfill), 11 (config annotate). ✅
- API/services/repositories/models updated → Tasks 2–10. ✅
- Backward compatible → single-row fallback (Task 6), nullable+backfill (Task 2). ✅
- DB migrations → Task 2 (data.sql). ✅
- Validation + tests → every task is TDD; validation in Tasks 9, 10. ✅
- UI list/select/manage → Tasks 12, 13. ✅

**2. Placeholder scan:** No "TBD/TODO/implement later". Steps that require reading the authoritative source before finalizing (VdmsDTO setters in Task 4; vdms_configuration column names in Task 5; DeviceService collaborator field names in Task 7; api.js export style in Task 12; view-switch location in Task 13) are flagged with an explicit verification command/instruction, not left vague.

**3. Type consistency:** `VdmsContext` (getVdmsId/setVdmsId/hasVdms) consistent across Tasks 1, 6, 7. `VdmsRepository.findSingleVdmsId()` defined Task 4, used Task 6. `VdmsJpaRepository` (findAllIds/countBuildingsByVdmsId/countAssetsByVdmsId) defined Task 3, used Tasks 4, 9. `VdmsCrudService` method set consistent Tasks 9, 10. `vdmsAdmin` (list/get/create/update/remove) consistent Tasks 12, 13. Endpoint base path identical across Tasks 6, 10, 12. Header `X-Vdms-Id` / param `vdms_id` consistent Tasks 6, 12.

## Notes for the executor
- Tasks 1–11 are backend and strictly ordered (each builds on the prior). Tasks 12–13 are UI and depend only on the endpoints from Task 10.
- The local gate is unit/MockMvc tests (`-Dtest='!*IT'`). `*IT.java` files are written for CI/working-Docker environments.
- Do not commit automatically if the user prefers manual commits — the commit steps are provided for completeness; confirm the user's workflow.
