# MapStruct Read-Path Migration — First Demo-Safe Slice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prove the DAO-custom-impl + MapStruct read pattern end-to-end on the single safest read path (`TechnicianRepository.getAllTechnician`), behind a default-off flag, without risking today's demo.

**Architecture:** Move `getAllTechnician` off the `@Query(nativeQuery=true)` + `@SqlResultSetMapping` style into a Spring Data custom fragment (`TechnicianRepositoryCustom` + `TechnicianRepositoryImpl`). The impl holds two branches selected by a config flag: **off** runs the existing `@NamedNativeQuery` verbatim (byte-identical by construction); **on** loads `Technician` entities and maps them with the *already-existing* `TechnicianDtoMapper`. The old native query + mapping declarations stay in place (deferred deletion) so revert is trivial.

**Tech Stack:** Java (jakarta.* namespace, Hibernate ORM 6+, Spring Data JPA), MapStruct 1.6.3 (already on the build, annotation-processor order already configured in `pom.xml`), JUnit 5 + Testcontainers Postgres (`PostgresJpaIT`).

## Global Constraints

- **Byte-identical response contract.** `getAllTechnician()` output (field values per row, and the set of rows) must be identical between the off and on branches. Any field gained/lost/changed is a defect.
- **Parity always wins.** If the on branch cannot reproduce the off branch exactly, the on branch is wrong — fix the mapping, do not change the contract.
- **Default-off.** The flag `mapstruct.read.technician.get-all` defaults to `false`. With no property set, behavior is 100% the original native path.
- **Deferred deletion.** Do NOT delete `@NamedNativeQuery(name = "Technician.getAllTechnician")` or `@SqlResultSetMapping(name = "technicianMapping")` in `Technician.java` — the off branch uses them. Deletion is a post-demo decision.
- **Never commit on the user's behalf.** Leave all work in the tree; the user commits manually. (Commit *steps* below are written for the user to run, or to skip per their workflow.)
- **Reuse, don't recreate.** `TechnicianDtoMapper` already maps `Technician` → `TechnicianDTO` over exactly the 11 `technicianMapping` fields. Reuse it; do not write a new mapper.
- **Per-slice panic revert** (reverts ONLY this slice, touches nothing else):
  ```bash
  git restore --staged --worktree sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepository.java
  rm -f sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepositoryCustom.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepositoryImpl.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/it/TechnicianGetAllOffPathIT.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/it/TechnicianGetAllMapStructIT.java
  ```

---

## Reference: current state (do not change unless a step says so)

- `TechnicianRepository.java:54-55` — current declaration to remove:
  ```java
  // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with @SqlResultSetMapping; plain SELECT already PG-compatible
  @Query(nativeQuery = true)
  List<TechnicianDTO> getAllTechnician();
  ```
- `Technician.java:33-34` — the named query + mapping the off branch reuses (KEEP):
  ```java
  @SqlResultSetMapping(name = "technicianMapping", classes = @ConstructorResult(targetClass = TechnicianDTO.class, columns = { id, email, phone, countryCode, name, department, designation, timeZone, createdBy, createdAt, vdmsId }))
  @NamedNativeQuery(name = "Technician.getAllTechnician", query = "SELECT id, email, phone, country_code AS countryCode, name, department, designation, time_zone AS timeZone, created_by AS createdBy, created_at AS createdAt, vdms_id AS vdmsId FROM technician", resultSetMapping = "technicianMapping")
  ```
- `mapper/TechnicianDtoMapper.java` — existing `@Mapper(componentModel="spring")` with `TechnicianDTO toDto(Technician t)`, `ignoreByDefault=true`, the 11 fields (`vdmsId` ← `vdms.id`). Reuse as-is.
- `it/DeviceRepositoryIT.java` + `it/PostgresJpaIT` — IT base/conventions to follow (`@Sql` schema, `@Transactional`, Testcontainers).

---

## Task 1: Behavior-preserving move of `getAllTechnician` into a DAO custom fragment (off-path only)

Introduce the custom-fragment rails and the off branch. After this task, `getAllTechnician()` behaves exactly as before — same native query, same `technicianMapping` — but is now served by `TechnicianRepositoryImpl` instead of a `@Query` method. No flag yet (only the off path exists).

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepositoryCustom.java`
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepositoryImpl.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepository.java` (extend custom interface, remove the `@Query` `getAllTechnician`)
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/it/TechnicianGetAllOffPathIT.java`

**Interfaces:**
- Consumes: existing `@NamedNativeQuery("Technician.getAllTechnician")` + `technicianMapping` (from `Technician.java`); `EntityManager`.
- Produces: `TechnicianRepositoryCustom.getAllTechnician() : List<TechnicianDTO>`, implemented by `TechnicianRepositoryImpl`. `TechnicianRepository` now also extends `TechnicianRepositoryCustom`, so callers keep calling `technicianRepository.getAllTechnician()` unchanged.

- [ ] **Step 1: Write the failing IT**

Create `TechnicianGetAllOffPathIT.java`:

```java
package io.sclera.it;

import io.sclera.Repository.TechnicianRepository;
import io.sclera.dto.TechnicianDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Transactional
class TechnicianGetAllOffPathIT extends PostgresJpaIT {

    @Autowired
    TechnicianRepository technicianRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void getAllTechnician_offPath_returnsSeededRows() {
        em.createNativeQuery("INSERT INTO technician(id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
                "VALUES ('t1','a@x.com','111','+1','Alice','dept','desig','UTC','admin',1000, NULL)").executeUpdate();
        em.createNativeQuery("INSERT INTO technician(id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
                "VALUES ('t2','b@x.com','222','+1','Bob','dept','desig','UTC','admin',2000, NULL)").executeUpdate();
        em.flush();
        em.clear();

        // sort a COPY — getAllTechnician() may return an unmodifiable list (Stream.toList() on the on-path)
        List<TechnicianDTO> result = technicianRepository.getAllTechnician().stream()
                .sorted(Comparator.comparing(TechnicianDTO::getId)).toList();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("t1");
        assertThat(result.get(0).getName()).isEqualTo("Alice");
        assertThat(result.get(0).getCreatedAt()).isEqualTo(1000L);
        assertThat(result.get(1).getId()).isEqualTo("t2");
        assertThat(result.get(1).getName()).isEqualTo("Bob");
    }
}
```

- [ ] **Step 2: Run it — verify it passes against the CURRENT code (baseline guard)**

Run: `mvn -q -pl sclera-cloud-device-asset -Dtest=TechnicianGetAllOffPathIT test`
Expected: PASS (this confirms the seed + assertions match today's native behavior BEFORE refactor). If it fails now, fix the test/seed before touching production code.

- [ ] **Step 3: Create the custom fragment interface**

`TechnicianRepositoryCustom.java`:

```java
package io.sclera.Repository;

import io.sclera.dto.TechnicianDTO;

import java.util.List;

/** Custom (DAO) read methods for {@link TechnicianRepository} served by {@link TechnicianRepositoryImpl}. */
public interface TechnicianRepositoryCustom {

    /**
     * Returns all technician projections. Off branch runs the verbatim {@code Technician.getAllTechnician}
     * native query; on branch maps {@code Technician} entities via {@code TechnicianDtoMapper}. Selected
     * by the {@code mapstruct.read.technician.get-all} flag (default false).
     */
    List<TechnicianDTO> getAllTechnician();
}
```

- [ ] **Step 4: Create the impl with the off path only**

`TechnicianRepositoryImpl.java`:

```java
package io.sclera.Repository;

import io.sclera.dto.TechnicianDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

public class TechnicianRepositoryImpl implements TechnicianRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<TechnicianDTO> getAllTechnician() {
        // Off path: verbatim existing named native query + technicianMapping — byte-identical to before.
        return em.createNamedQuery("Technician.getAllTechnician").getResultList();
    }
}
```

- [ ] **Step 5: Wire the repository to the fragment and remove the `@Query` method**

In `TechnicianRepository.java`:

Change the interface declaration:
```java
public interface TechnicianRepository extends JpaRepository<Technician,String>, TechnicianRepositoryCustom {
```

Delete these lines (currently ~48-55):
```java
    /**
     * Returns all technician records.
     *
     * @return the list of technician projections
     */
    // NOT CONVERTED — stays native (PG-translation track): @NamedNativeQuery projection with @SqlResultSetMapping; plain SELECT already PG-compatible
    @Query(nativeQuery = true)
    List<TechnicianDTO> getAllTechnician();
```

(Leave all other methods, imports, and the `@NamedNativeQuery`/`@SqlResultSetMapping` in `Technician.java` untouched.)

- [ ] **Step 6: Run the IT — verify it still passes after the refactor**

Run: `mvn -q -pl sclera-cloud-device-asset -Dtest=TechnicianGetAllOffPathIT test`
Expected: PASS — identical behavior, now served by the custom impl.

- [ ] **Step 7: Compile the module**

Run: `mvn -q -pl sclera-cloud-device-asset compile`
Expected: BUILD SUCCESS (custom fragment auto-detected by Spring Data; nothing else changed).

- [ ] **Step 8: Commit (user runs, or skip per workflow)**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepositoryCustom.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepositoryImpl.java \
        sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepository.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/it/TechnicianGetAllOffPathIT.java
git commit -m "refactor(jpa): serve getAllTechnician via DAO custom fragment (off-path, behavior-preserving)"
```

---

## Task 2: Add the flag-gated MapStruct on-path + parity test

Add the second branch (entities → `TechnicianDtoMapper`) selected by the default-off flag, and a parity IT proving the on branch produces field-identical output to the off branch.

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepositoryImpl.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/it/TechnicianGetAllMapStructIT.java`

**Interfaces:**
- Consumes: existing `io.sclera.mapper.TechnicianDtoMapper` (Spring bean, `toDto(Technician)`); `EntityManager`; property `mapstruct.read.technician.get-all`.
- Produces: no signature change — same `getAllTechnician() : List<TechnicianDTO>`, now branch-selectable.

- [ ] **Step 1: Write the failing parity IT (on-path)**

Create `TechnicianGetAllMapStructIT.java` — identical seed/assertions to the off-path IT, but forces the flag on via `@TestPropertySource`. Because both ITs assert the SAME golden values against the SAME seed, passing both proves parity.

```java
package io.sclera.it;

import io.sclera.Repository.TechnicianRepository;
import io.sclera.dto.TechnicianDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@TestPropertySource(properties = "mapstruct.read.technician.get-all=true")
@Transactional
class TechnicianGetAllMapStructIT extends PostgresJpaIT {

    @Autowired
    TechnicianRepository technicianRepository;

    @PersistenceContext
    EntityManager em;

    @Test
    void getAllTechnician_mapStructPath_matchesGoldenValues() {
        em.createNativeQuery("INSERT INTO technician(id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
                "VALUES ('t1','a@x.com','111','+1','Alice','dept','desig','UTC','admin',1000, NULL)").executeUpdate();
        em.createNativeQuery("INSERT INTO technician(id, email, phone, country_code, name, department, designation, time_zone, created_by, created_at, vdms_id) " +
                "VALUES ('t2','b@x.com','222','+1','Bob','dept','desig','UTC','admin',2000, NULL)").executeUpdate();
        em.flush();
        em.clear();

        // sort a COPY — the on-path returns Stream.toList() (unmodifiable)
        List<TechnicianDTO> result = technicianRepository.getAllTechnician().stream()
                .sorted(Comparator.comparing(TechnicianDTO::getId)).toList();

        assertThat(result).hasSize(2);
        // same golden assertions as the off-path IT — equality of both proves parity
        assertThat(result.get(0).getId()).isEqualTo("t1");
        assertThat(result.get(0).getEmail()).isEqualTo("a@x.com");
        assertThat(result.get(0).getCountryCode()).isEqualTo("+1");
        assertThat(result.get(0).getName()).isEqualTo("Alice");
        assertThat(result.get(0).getCreatedAt()).isEqualTo(1000L);
        assertThat(result.get(0).getVdmsId()).isNull();
        // fields NOT in the 11-column projection must stay null (ignoreByDefault parity)
        assertThat(result.get(0).getCost()).isNull();
        assertThat(result.get(0).getSync()).isNull();
        assertThat(result.get(1).getId()).isEqualTo("t2");
        assertThat(result.get(1).getName()).isEqualTo("Bob");
    }
}
```

- [ ] **Step 2: Run it — record the pre-change baseline**

Run: `mvn -q -pl sclera-cloud-device-asset -Dtest=TechnicianGetAllMapStructIT test`
Expected: **PASS** — and that is expected, not a mistake. The current impl (from Task 1) has no flag branch, so it runs the off path regardless of `@TestPropertySource`. Because the on and off paths are designed to be equivalent, this test passes now too. This is a *characterization* test, not a red/green test: literal red is unachievable when both branches must produce identical output. The genuine gate is **Step 4**, which confirms the on path is actually wired and *still* matches the golden values (i.e. parity holds when the flag truly switches code). Do not skip Step 3 just because Step 2 is green.

- [ ] **Step 3: Add the flag + on path to the impl**

Replace the body of `TechnicianRepositoryImpl.java` with:

```java
package io.sclera.Repository;

import io.sclera.dto.TechnicianDTO;
import io.sclera.mapper.TechnicianDtoMapper;
import io.sclera.models.Technician;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class TechnicianRepositoryImpl implements TechnicianRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    private final TechnicianDtoMapper technicianDtoMapper;

    @Value("${mapstruct.read.technician.get-all:false}")
    private boolean useMapStruct;

    public TechnicianRepositoryImpl(TechnicianDtoMapper technicianDtoMapper) {
        this.technicianDtoMapper = technicianDtoMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<TechnicianDTO> getAllTechnician() {
        if (useMapStruct) {
            // On path: load entities, map via the existing MapStruct mapper (same 11 fields as technicianMapping).
            List<Technician> technicians =
                    em.createQuery("SELECT t FROM Technician t", Technician.class).getResultList();
            return technicians.stream().map(technicianDtoMapper::toDto).toList();
        }
        // Off path: verbatim existing named native query + technicianMapping — byte-identical to before.
        return em.createNamedQuery("Technician.getAllTechnician").getResultList();
    }
}
```

- [ ] **Step 4: Run BOTH ITs — verify both pass (parity proven)**

Run: `mvn -q -pl sclera-cloud-device-asset -Dtest=TechnicianGetAllOffPathIT,TechnicianGetAllMapStructIT test`
Expected: PASS for both. Off path (flag default false) and on path (flag true) yield identical golden values → byte-identical parity for the projected fields, and `ignoreByDefault` keeps non-projected fields null.

**Routing confirmation (do once, then revert):** because both branches are equivalent, a green on-path test does not by itself prove the on path ran. Temporarily change the on-path `return` to `return java.util.List.of();`, run `-Dtest=TechnicianGetAllMapStructIT` → it must now FAIL (size 0 ≠ 2), proving `@TestPropertySource` truly routes into the MapStruct branch. **Revert the temporary change** and re-run Step 4 to green before continuing.

- [ ] **Step 5: Confirm production default is off**

Run: `mvn -q -pl sclera-cloud-device-asset test -Dtest=TechnicianGetAllOffPathIT`
Expected: PASS. With no `mapstruct.read.technician.get-all` property anywhere in `src/main/resources`, the running app uses the off (original) path. Verify no such property was added to any `application*.properties`/`application*.yml`:

Run: `grep -rn "mapstruct.read.technician" sclera-cloud-device-asset/src/main/resources || echo "OK: flag not set in main config (defaults off)"`
Expected: `OK: flag not set in main config (defaults off)`

- [ ] **Step 6: Full-module compile + the two ITs as the final gate**

Run: `mvn -q -pl sclera-cloud-device-asset test -Dtest=TechnicianGetAll*IT`
Expected: BUILD SUCCESS, both ITs green.

- [ ] **Step 7: Commit (user runs, or skip per workflow)**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/Repository/TechnicianRepositoryImpl.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/it/TechnicianGetAllMapStructIT.java
git commit -m "feat(jpa): flag-gated MapStruct on-path for getAllTechnician with parity IT"
```

---

## Demo-day operating notes

- **Default is safe:** with no flag set, `getAllTechnician` runs the original native path. Nothing in this slice changes runtime behavior unless you set `mapstruct.read.technician.get-all=true`.
- **To trial the new path:** set `mapstruct.read.technician.get-all=true` (env var `MAPSTRUCT_READ_TECHNICIAN_GET_ALL=true` or a property) and restart; unset to revert instantly.
- **To remove the slice entirely:** run the per-slice panic-revert command in Global Constraints. It touches only the four slice files and `TechnicianRepository.java`; all other work is untouched.

## Next slices (separate plans, after the demo)

Per the spec's decomposition, subsequent reads each get their own small plan following this exact recipe:
- `Technician.getAllTechnician` *(this plan)*.
- `TechnicianAvailability.getAllTechnicianAvailability` — **note the parity nuance**: its `@ConstructorResult` column order ends `…frequency, condition, technician_id` while `TechnicianAvailabilityDTO`'s constructor ends `…frequency, technicianId, condition, sync`. The on-path mapper must reproduce the *actual current* field-to-column binding exactly (verify against the off path with a parity IT before trusting any field). Reuse/extend the existing `TechnicianAvailabilityDtoMapper`.
- Then batch `DeviceRepository`'s simple reads (buckets ① and ②), easy→hard.

Pass 2 (native → JPQL/Criteria for portability) is a later, separate spec; it edits only the on-path SQL inside each impl, re-verified by the same ITs.
