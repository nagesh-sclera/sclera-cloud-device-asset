# Device Search Criteria Conversion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the string-concatenated dynamic SQL in `DeviceSearchService.multipleKeywordSearchSortFilterDevices` / `...ForAssetExport` / `...Count` with a type-safe JPA Criteria query builder.

**Architecture:** A `DeviceSearchCriteria` DTO parses the request JSON once; an `EntityManager`-based `DeviceSearchQueryBuilder` builds the id-projection / count Criteria queries (outer `Device` root with `d.id IN (subquery)`, mirroring the original shape); PG-specific functions (`jsonb_path_query_*`, `REGEXP_REPLACE`, `::inet`) are registered once via a Hibernate 7 `FunctionContributor`. The three service methods keep their signatures and workflow (ids → `getDevicesByIdList` → fuzzy re-rank).

**Tech Stack:** Java 21, Spring Boot 4.0.6, Hibernate 7.2, PostgreSQL 16, Testcontainers ITs (`PostgresJpaIT`), JUnit 5, AssertJ.

**Spec:** `docs/superpowers/specs/2026-06-12-device-search-criteria-conversion-design.md` (read it first — especially the bug-fix list and the inner-positive/outer-polarity CORRECTION).

---

## Build environment (read first)

- Work from the **inner module dir**: `sclera-cloud-device-asset/sclera-cloud-device-asset`.
- Before every `mvnw` call set `$env:JAVA_HOME = 'C:\Users\DhanushVasanth\.jdks\corretto-21.0.8'` (PATH java is 17 — wrong).
- Run a single test class: `& .\mvnw.cmd -q "-Dtest=ClassName" test` (PowerShell). Exit 0 + `Tests run: N, Failures: 0, Errors: 0` is the gate; logback appender WARN/ERROR noise at startup is pre-existing — ignore it.
- ITs use Docker (Testcontainers, postgres:16). Docker Desktop must be running.
- Commit from the repo root (`sclera-cloud-device-asset`), paths below are repo-root-relative unless stated.

## Verified codebase facts (do NOT re-derive; they're checked)

- `Device` (in `io.sclera.models`) has NO mapped fields for columns `docker_vdms_id`, `docker_name`, `assigned_user_email` — they are join columns of `@ManyToOne Docker docker` (composite PK name+vdms) and `@ManyToOne @JoinColumn(name="assigned_user_email", referencedColumnName="email") User user`. Task 3 adds read-only shadow fields.
- `Device` fields used here (exact names): `id`, `monitor`, `status`, `virtual_device_type`, `asset_match_status`, `onboard_status`, `source_type`, `type`, `description`, `ip_address`, `mac_address`, `latitude`, `longitude`, `serial_number`, `warranty`, `created_timestamp` (BigInteger), `updated_timestamp` (BigInteger), `display_name`, `user_data_name`, `vendor`, `user_data_vendor`, `model`, `user_data_model`, `custom_fields` (public String, TEXT), `asset_group`, `category`, `sub_category`, `record_checklist_count`, `document_count`, `asset_image_url`, `measuring_instrument_count`, `monnit_status`, `pelican_status`, `knx_status`, `snmp_object_status`, `daintree_status`, `ecobee_status`, `bacnet_status`, `lorawan_status`, `my_devices_status`, `measuring_instrument_status`, `disruptive_status`; associations `location` (`@ManyToOne Location`), `device_onboard_status` (`@OneToOne mappedBy="device"`).
- `Location.name`, `Location.floor` (`@ManyToOne Floor`); `Floor.name`, `Floor.building` (`@ManyToOne Building`); `Building.name`.
- `DeviceOnboardStatus`: `assignee_email`, `geolocation_status`, `image_status`, `field_status`, `tag_status` (Integer), `device` (`@OneToOne`), `device_onboard_status_assignees` (`@OneToMany mappedBy="device_onboard_status"`).
- `DeviceOnboardStatusAssignee`: `email`.
- `QrCode`, `ClientQrCode`, `ClientBarCode`, `Nfc`, `ClientNfc`: each has `@ManyToOne Device device`.
- `DeviceSpecification`: `username`, `email`, `osType` (camelCase → column `os_type`), `device` (`@OneToOne @JoinColumn(name="device_id")`). Device has NO inverse association → use a Hibernate ad-hoc entity join (`JpaFrom.join(Class, SqmJoinType)`).
- `updateDeviceSearchColumnName` is ALSO used by out-of-scope methods (DeviceSearchService.java lines ~340/587/727) — it must NOT be deleted.
- Test harness: `io.sclera.it.PostgresJpaIT` (+ `JpaTestConfig`); class-level `@Sql("/schema-pg.sql")`, method-level seed/cleanup `@Sql`. `schema-pg.sql` already has the `device` table with `docker_vdms_id`, `docker_name`, `assigned_user_email`, `custom_fields`, all count/status columns, plus `location`/`floor`/`building`/`device_onboard_status`/`device_onboard_status_assignee`/`device_specification`/`client_bar_code` tables. MISSING: `qr_code`, `client_qr_code`, `nfc`, `client_nfc` tables; `device.source_type` column; `device_specification.username/email/os_type` columns (verify, add guarded).
- The two regex character classes are DIFFERENT and both intentional (copy verbatim):
  - Haystack/value strip (Java + SQL, `[ ` starts a space→`.` RANGE that covers `"#$%&'()*+,-.`):
    Java: `"[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]"` ; SQL: `'[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\]'`
  - Custom-fields-array strip (NO leading-space range; quotes survive — patterns like `%"t"%` depend on that):
    SQL: `'[-.!\t_+#~`@$%^&*()=;:<>?,/{}|\'' ]'`

---

### Task 1: Test schema groundwork (missing tables/columns + seed + cleanup)

**Files:**
- Modify: `sclera-cloud-device-asset/src/test/resources/schema-pg.sql` (append at end)
- Create: `sclera-cloud-device-asset/src/test/resources/seed/device-search-it.sql`
- Create: `sclera-cloud-device-asset/src/test/resources/cleanup-device-search-it.sql`

- [ ] **Step 1: Append missing tables/columns to schema-pg.sql**

Append at the end of `schema-pg.sql`:

```sql
-- ============================================================
-- Device search criteria conversion (DeviceSearchQueryBuilderIT)
-- ============================================================
-- qr/nfc tag tables: only existence is queried (EXISTS on device FK)
CREATE TABLE IF NOT EXISTS qr_code (
    id          VARCHAR(255) PRIMARY KEY,
    device_id   VARCHAR(255) REFERENCES device(id),
    location_id VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS client_qr_code (
    id          VARCHAR(255) PRIMARY KEY,
    device_id   VARCHAR(255) REFERENCES device(id),
    location_id VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS nfc (
    id          VARCHAR(255) PRIMARY KEY,
    device_id   VARCHAR(255) REFERENCES device(id),
    location_id VARCHAR(255)
);
CREATE TABLE IF NOT EXISTS client_nfc (
    id          VARCHAR(255) PRIMARY KEY,
    device_id   VARCHAR(255) REFERENCES device(id),
    location_id VARCHAR(255)
);
ALTER TABLE device ADD COLUMN IF NOT EXISTS source_type VARCHAR(64) DEFAULT 'vdms';
ALTER TABLE device_specification ADD COLUMN IF NOT EXISTS username VARCHAR(255);
ALTER TABLE device_specification ADD COLUMN IF NOT EXISTS email    VARCHAR(255);
ALTER TABLE device_specification ADD COLUMN IF NOT EXISTS os_type  VARCHAR(64);
```

- [ ] **Step 2: Create the seed file**

`seed/device-search-it.sql`:

```sql
-- Scope: all devices live in docker_vdms_id='v1', docker_name='dock1' except dsx9 (other vdms)
INSERT INTO building (id, name) VALUES ('bldg1', 'HeadQuarters');
INSERT INTO floor (id, name, building_id) VALUES ('flr1', 'FirstFloor', 'bldg1');
INSERT INTO location (id, name, floor_id) VALUES ('loc1', 'MainLab', 'flr1');

-- dsx1: online, matched(1), onboarded(3), assigned, custom dept=Engineering, cat Hardware/Server
INSERT INTO device (id, display_name, user_data_name, vendor, user_data_vendor, model, type,
                    ip_address, mac_address, serial_number, monitor, status, onboard_status,
                    asset_match_status, assigned_user_email, docker_vdms_id, docker_name,
                    custom_fields, category, sub_category, asset_group, source_type,
                    record_checklist_count, document_count, asset_image_url, monnit_status,
                    created_timestamp, updated_timestamp, location_id)
VALUES ('dsx1', 'Alpha Device', 'My-Alpha', 'Cisco', NULL, 'CX100', 'router',
        '10.0.0.2', 'AA:BB:01', 'SN-001', 1, 1, 3,
        1, 'alice@sclera.com', 'v1', 'dock1',
        '[{"department":"Engineering","owner":"alice"}]', 'Hardware', 'Server', 'GroupA', 'vdms',
        2, 1, '["img1.png"]', 'alert',
        100, 500, 'loc1');

-- dsx2: offline, unmatched(0), notonboarded(1), unassigned(NULL), custom dept=Finance
INSERT INTO device (id, display_name, vendor, model, type, ip_address, monitor, status,
                    onboard_status, asset_match_status, assigned_user_email, docker_vdms_id,
                    docker_name, custom_fields, category, sub_category, asset_group, source_type,
                    created_timestamp, updated_timestamp)
VALUES ('dsx2', 'Beta Device', 'HP', 'HP-22', 'switch', '10.0.0.10', 1, 0,
        1, 0, NULL, 'v1',
        'dock1', '[{"department":"Finance"}]', 'Hardware', 'Laptop', 'GroupB', 'adc',
        200, 400);

-- dsx3: unmonitored(0), verified(2), assigned_user_email literal 'null' (counts as unassigned)
INSERT INTO device (id, display_name, type, monitor, status, onboard_status, asset_match_status,
                    assigned_user_email, docker_vdms_id, docker_name, created_timestamp, updated_timestamp)
VALUES ('dsx3', 'Gamma!! Device', 'sensor', 0, 0, 2, 2,
        'null', 'v1', 'dock1', 300, 300);

-- dsx4: archived(3) — excluded from 'all' (asset_match_status != 3 default filter)
INSERT INTO device (id, display_name, type, monitor, asset_match_status, docker_vdms_id,
                    docker_name, created_timestamp, updated_timestamp)
VALUES ('dsx4', 'Delta Device', 'router', 1, 3, 'v1', 'dock1', 400, 200);

-- dsx5: virtual 'other' (virtual_device_type=5), NULL monitor, ip for inet sort
INSERT INTO device (id, display_name, type, monitor, virtual_device_type, asset_match_status,
                    docker_vdms_id, docker_name, ip_address, created_timestamp, updated_timestamp)
VALUES ('dsx5', 'Epsilon Device', 'camera', NULL, 5, 0, 'v1', 'dock1', '9.1.1.1', 500, 100);

-- dsx9: different vdms — must NEVER appear in v1-scoped results
INSERT INTO device (id, display_name, type, monitor, asset_match_status, docker_vdms_id,
                    docker_name, created_timestamp, updated_timestamp)
VALUES ('dsx9', 'Foreign Device', 'router', 1, 0, 'v2', 'dock2', 900, 900);

INSERT INTO device_onboard_status (id, assignee_email, geolocation_status, image_status,
                                   field_status, tag_status, device_id)
VALUES ('dosx1', 'tech1@sclera.com', 1, 0, 2, 3, 'dsx1');
INSERT INTO device_onboard_status (id, assignee_email, geolocation_status, device_id)
VALUES ('dosx2', NULL, 0, 'dsx2');
INSERT INTO device_onboard_status_assignee (id, email, device_onboard_status_id)
VALUES ('dosax1', 'tech2@sclera.com', 'dosx1');

INSERT INTO device_specification (id, username, email, os_type, device_id)
VALUES ('dspec1', 'winuser01', 'winuser01@sclera.com', 'windows', 'dsx1');

INSERT INTO qr_code (id, device_id) VALUES ('qrx1', 'dsx1');
INSERT INTO client_bar_code (id, device_id) VALUES ('cbcx1', 'dsx2');
INSERT INTO nfc (id, device_id) VALUES ('nfcx1', 'dsx2');
```

NOTE: `client_bar_code` already exists from `ClientBarCodeRepositoryIT`'s pilot — check its column list (`Select-String -Path src/test/resources/schema-pg.sql -Pattern 'client_bar_code' -Context 0,12`) and adjust the INSERT's column names if `device_id` is named differently there.

- [ ] **Step 3: Create the cleanup file**

`cleanup-device-search-it.sql`:

```sql
DELETE FROM qr_code            WHERE id LIKE 'qrx%';
DELETE FROM client_qr_code     WHERE id LIKE 'cqcx%';
DELETE FROM client_bar_code    WHERE id LIKE 'cbcx%';
DELETE FROM nfc                WHERE id LIKE 'nfcx%';
DELETE FROM client_nfc         WHERE id LIKE 'cnfcx%';
DELETE FROM device_specification WHERE id LIKE 'dspec%';
DELETE FROM device_onboard_status_assignee WHERE id LIKE 'dosax%';
DELETE FROM device_onboard_status WHERE id LIKE 'dosx%';
DELETE FROM device             WHERE id LIKE 'dsx%';
DELETE FROM location           WHERE id = 'loc1';
DELETE FROM floor              WHERE id = 'flr1';
DELETE FROM building           WHERE id = 'bldg1';
```

- [ ] **Step 4: Verify the schema still loads (run an existing IT)**

```powershell
$env:JAVA_HOME = 'C:\Users\DhanushVasanth\.jdks\corretto-21.0.8'
& .\mvnw.cmd -q "-Dtest=DeviceRepositoryIT" test
```
Expected: `Tests run: ..., Failures: 0, Errors: 0`, exit 0.

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/test/resources
git commit -m "test: schema/seed groundwork for device-search Criteria conversion"
```

---

### Task 2: Hibernate FunctionContributor for PG-specific SQL

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/config/ScleraPgFunctionContributor.java`
- Create: `sclera-cloud-device-asset/src/main/resources/META-INF/services/org.hibernate.boot.model.FunctionContributor`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/it/PgFunctionContributorIT.java`

- [ ] **Step 1: Write the failing IT**

```java
package io.sclera.it;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** Proves the FunctionContributor-registered PG functions are callable from HQL/Criteria. */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-search-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-search-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class PgFunctionContributorIT extends PostgresJpaIT {

    @PersistenceContext
    EntityManager em;

    @Test
    void customFieldText_extractsValueByKey() {
        String dept = em.createQuery(
                "SELECT custom_field_text(d.custom_fields, '$[*].\"department\"') "
                + "FROM Device d WHERE d.id = 'dsx1'", String.class).getSingleResult();
        assertThat(dept).isEqualTo("Engineering");
    }

    @Test
    void customFieldText_missingKey_isNull() {
        String v = em.createQuery(
                "SELECT custom_field_text(d.custom_fields, '$[*].\"nope\"') "
                + "FROM Device d WHERE d.id = 'dsx1'", String.class).getSingleResult();
        assertThat(v).isNull();
    }

    @Test
    void customFieldArrayText_returnsJsonArrayText() {
        String arr = em.createQuery(
                "SELECT custom_field_array_text(d.custom_fields, '$[*].*') "
                + "FROM Device d WHERE d.id = 'dsx1'", String.class).getSingleResult();
        assertThat(arr).contains("\"Engineering\"").contains("\"alice\"");
    }

    @Test
    void stripSpecials_haystackClass_stripsAllOccurrencesIncludingQuotes() {
        // 'g' flag fix: ALL specials stripped, not just the first.
        // Haystack class has the space->dot range, so '"' and '-' are stripped too.
        String s = em.createQuery(
                "SELECT strip_specials('a-b.c\"d e_f') FROM Device d WHERE d.id = 'dsx1'",
                String.class).getSingleResult();
        assertThat(s).isEqualTo("abcdef");
    }

    @Test
    void stripCustomSpecials_quotesSurvive() {
        // Custom-fields class has NO range: '"' must SURVIVE (patterns like %"t"% depend on it).
        String s = em.createQuery(
                "SELECT strip_custom_specials('a-b\"c\"_d') FROM Device d WHERE d.id = 'dsx1'",
                String.class).getSingleResult();
        assertThat(s).isEqualTo("ab\"c\"d");
    }

    @Test
    void inetVal_castsForOrdering() {
        java.util.List<String> ips = em.createQuery(
                "SELECT d.ip_address FROM Device d WHERE d.ip_address IS NOT NULL "
                + "AND d.id LIKE 'dsx%' ORDER BY inet_val(d.ip_address)", String.class)
                .getResultList();
        assertThat(ips).containsExactly("9.1.1.1", "10.0.0.2", "10.0.0.10"); // numeric, not lexicographic
    }
}
```

- [ ] **Step 2: Run it to verify it fails**

```powershell
$env:JAVA_HOME = 'C:\Users\DhanushVasanth\.jdks\corretto-21.0.8'
& .\mvnw.cmd -q "-Dtest=PgFunctionContributorIT" test
```
Expected: FAIL — HQL parse errors ("no function registered" / unknown function `custom_field_text`).

- [ ] **Step 3: Implement the contributor**

`src/main/java/io/sclera/config/ScleraPgFunctionContributor.java`:

```java
package io.sclera.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

/**
 * Registers PostgreSQL-specific SQL as named HQL/Criteria functions so dynamic queries
 * (DeviceSearchQueryBuilder) stay type-safe without raw SQL strings.
 *
 * Registered via META-INF/services/org.hibernate.boot.model.FunctionContributor.
 *
 * NOTE both REGEXP_REPLACE patterns carry the 'g' flag — the earlier PG port omitted it,
 * which strips only the FIRST special character (MySQL strips all). Documented port-bug fix.
 *
 * The two character classes are intentionally DIFFERENT (verbatim from DeviceSearchService):
 *  - strip_specials: leading "[ -." opens a space-to-dot RANGE (covers " # $ % & ' ( ) * + , - .)
 *  - strip_custom_specials: no range; double quotes survive so %"term"% patterns can match
 */
public class ScleraPgFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions fc) {
        var stringType = fc.getTypeConfiguration().getBasicTypeRegistry()
                .resolve(StandardBasicTypes.STRING);

        fc.getFunctionRegistry().registerPattern(
                "custom_field_text",
                "jsonb_path_query_first(CAST(?1 AS jsonb), CAST(?2 AS jsonpath)) #>> '{}'",
                stringType);

        fc.getFunctionRegistry().registerPattern(
                "custom_field_array_text",
                "CAST(jsonb_path_query_array(CAST(?1 AS jsonb), CAST(?2 AS jsonpath)) AS text)",
                stringType);

        fc.getFunctionRegistry().registerPattern(
                "strip_specials",
                "REGEXP_REPLACE(?1, '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]', '', 'g')",
                stringType);

        fc.getFunctionRegistry().registerPattern(
                "strip_custom_specials",
                "REGEXP_REPLACE(?1, '[-.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\'' ]', '', 'g')",
                stringType);

        fc.getFunctionRegistry().registerPattern(
                "inet_val",
                "CAST(?1 AS inet)",
                fc.getTypeConfiguration().getBasicTypeRegistry()
                        .resolve(StandardBasicTypes.STRING));
    }
}
```

API note: in Hibernate 7.2 `registerPattern(String, String, BasicTypeReference|BasicType)` lives on
`SqmFunctionRegistry` (returned by `fc.getFunctionRegistry()`). If `resolve(StandardBasicTypes.STRING)`
doesn't compile, use `fc.getTypeConfiguration().getBasicTypeForJavaType(String.class)`.
Escaping note: in the pattern strings above, `\t` must be a REAL tab character reaching the SQL regex —
in the Java source write it as `\t` inside the literal (Java unescapes it). `\\\\` in Java source =
`\\` in the SQL literal = one literal backslash in the regex class. `''` is a quoted single-quote inside
the SQL literal. After implementing, sanity-check the generated SQL in the IT failure output if a test
fails on stripping.

- [ ] **Step 4: Register the service**

`src/main/resources/META-INF/services/org.hibernate.boot.model.FunctionContributor` (one line):

```
io.sclera.config.ScleraPgFunctionContributor
```

- [ ] **Step 5: Run the IT to verify it passes**

```powershell
& .\mvnw.cmd -q "-Dtest=PgFunctionContributorIT" test
```
Expected: `Tests run: 6, Failures: 0, Errors: 0`.

- [ ] **Step 6: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/config/ScleraPgFunctionContributor.java sclera-cloud-device-asset/src/main/resources/META-INF sclera-cloud-device-asset/src/test/java/io/sclera/it/PgFunctionContributorIT.java
git commit -m "feat: register PG jsonb/regexp/inet functions via Hibernate FunctionContributor"
```

---

### Task 3: Read-only shadow columns on Device

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java` (after the `source_type` field, ~line 3216)
- Test: extend `PgFunctionContributorIT` (same harness, cheap)

- [ ] **Step 1: Write the failing test (add to PgFunctionContributorIT)**

```java
    @Test
    void deviceShadowColumns_readableViaJpql() {
        Object[] row = em.createQuery(
                "SELECT d.docker_vdms_id, d.docker_name, d.assigned_user_email "
                + "FROM Device d WHERE d.id = 'dsx1'", Object[].class).getSingleResult();
        assertThat(row[0]).isEqualTo("v1");
        assertThat(row[1]).isEqualTo("dock1");
        assertThat(row[2]).isEqualTo("alice@sclera.com");
    }
```

- [ ] **Step 2: Run to verify it fails**

```powershell
& .\mvnw.cmd -q "-Dtest=PgFunctionContributorIT" test
```
Expected: FAIL — `Could not resolve attribute 'docker_vdms_id'`.

- [ ] **Step 3: Add the shadow fields to Device.java**

Insert after the `source_type` field (keep the entity's field-access style; deliberately NO getters/setters so Jackson serialization of Device is unchanged):

```java
    // Read-only shadow mappings of FK join columns (owned by the 'docker' and 'user'
    // associations above). They give Criteria/JPQL direct typed access to the raw column
    // values — including the legacy literal-'null' sentinel in assigned_user_email —
    // without forcing joins. Never write through these.
    @Column(name = "docker_vdms_id", insertable = false, updatable = false)
    private String docker_vdms_id;

    @Column(name = "docker_name", insertable = false, updatable = false)
    private String docker_name;

    @Column(name = "assigned_user_email", insertable = false, updatable = false)
    private String assigned_user_email;
```

- [ ] **Step 4: Run the IT + the existing Device IT (mapping regression)**

```powershell
& .\mvnw.cmd -q "-Dtest=PgFunctionContributorIT,DeviceRepositoryIT" test
```
Expected: all pass (the duplicate-column mapping is legal because the shadows are read-only).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/models/Device.java sclera-cloud-device-asset/src/test/java/io/sclera/it/PgFunctionContributorIT.java
git commit -m "feat: read-only shadow mappings for device FK columns (docker_vdms_id, docker_name, assigned_user_email)"
```

---

### Task 4: DeviceSearchCriteria DTO + parser

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/dto/DeviceSearchCriteria.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/dto/DeviceSearchCriteriaTest.java`

- [ ] **Step 1: Write the failing unit test**

```java
package io.sclera.dto;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceSearchCriteriaTest {

    @Test
    void scopeWildcards_becomeNull() {
        DeviceSearchCriteria c = DeviceSearchCriteria.from("null", "all", "all", new JSONObject(), 123);
        assertThat(c.getVdmsId()).isNull();
        assertThat(c.getDockerName()).isNull();
        DeviceSearchCriteria c2 = DeviceSearchCriteria.from("v1", "dock1", "all", new JSONObject(), 123);
        assertThat(c2.getVdmsId()).isEqualTo("v1");
        assertThat(c2.getDockerName()).isEqualTo("dock1");
    }

    @Test
    void condition_online_setsMonitorAndStatus() {
        DeviceSearchCriteria c = DeviceSearchCriteria.from("v1", "all", "online", new JSONObject(), 123);
        assertThat(c.getMonitor()).isEqualTo(1);
        assertThat(c.getStatus()).isEqualTo(1);
        assertThat(c.getAssetMatchStatus()).isNull();
    }

    @Test
    void condition_other_setsVirtualOtherFlag() {
        DeviceSearchCriteria c = DeviceSearchCriteria.from("v1", "all", "other", new JSONObject(), 123);
        assertThat(c.isVirtualOther()).isTrue();
    }

    @Test
    void condition_notonboarded_setsNot3Flag_and_onboardParam123_meansNoFilter() {
        DeviceSearchCriteria c = DeviceSearchCriteria.from("v1", "all", "notonboarded", new JSONObject(), 123);
        assertThat(c.isOnboardStatusNot3()).isTrue();
        assertThat(c.getOnboardStatusEquals()).isNull();
        DeviceSearchCriteria all = DeviceSearchCriteria.from("v1", "all", "all", new JSONObject(), 123);
        assertThat(all.isOnboardStatusNot3()).isFalse();
        assertThat(all.getOnboardStatusEquals()).isNull();
    }

    @Test
    void onboardParam_explicitValue_filtersEquals_and_210_meansNot3() {
        DeviceSearchCriteria c = DeviceSearchCriteria.from("v1", "all", "all", new JSONObject(), 3);
        assertThat(c.getOnboardStatusEquals()).isEqualTo(3);
        DeviceSearchCriteria c210 = DeviceSearchCriteria.from("v1", "all", "all", new JSONObject(), 210);
        assertThat(c210.isOnboardStatusNot3()).isTrue();
    }

    @Test
    void countOnlyConditions_nowParsedForAllVariants() {
        // documented consistency fix: onboardpending/onboardcompleted previously worked only in Count
        assertThat(DeviceSearchCriteria.from("v1", "all", "onboardpending", new JSONObject(), 123)
                .getOnboardStatusEquals()).isEqualTo(1);
        assertThat(DeviceSearchCriteria.from("v1", "all", "onboardcompleted", new JSONObject(), 123)
                .getOnboardStatusEquals()).isEqualTo(2);
    }

    @Test
    void deviceIds_searches_filters_sort_parsed() {
        JSONObject json = JSONObject.parseObject("""
            {
              "device_ids": ["d1","d2"],
              "search_details": [{"column":"vendor","custom":false,"value":"Cis-co!","condition":"contains"}],
              "filter_details": {
                "column_details": [{"column":"department","custom":true,"condition":"is_present"}],
                "feature_details": [{"name":"qrcode","condition":"is_present"}]
              },
              "sort_details": {"column":"ip_address","custom":false}
            }""");
        DeviceSearchCriteria c = DeviceSearchCriteria.from("v1", "all", "all", json, 123);
        assertThat(c.getDeviceIds()).containsExactly("d1", "d2");
        assertThat(c.getSearches()).hasSize(1);
        DeviceSearchCriteria.KeywordSearch s = c.getSearches().get(0);
        assertThat(s.column()).isEqualTo("vendor");
        assertThat(s.custom()).isFalse();
        assertThat(s.value()).isEqualTo("Cis-co!");
        assertThat(s.condition()).isEqualTo(DeviceSearchCriteria.Cond.CONTAINS);
        assertThat(c.getColumnFilters()).hasSize(1);
        assertThat(c.getColumnFilters().get(0).custom()).isTrue();
        assertThat(c.getFeatureFilters()).extracting(DeviceSearchCriteria.FeatureFilter::name)
                .containsExactly("qrcode");
        assertThat(c.getSort()).isNotNull();
        assertThat(c.getSort().column()).isEqualTo("ip_address");
    }

    @Test
    void searchAll_nullColumn_unknownCondition_defaultsToContains() {
        JSONObject json = JSONObject.parseObject(
            "{\"search_details\":[{\"column\":null,\"custom\":false,\"value\":\"x\",\"condition\":\"bogus\"}]}");
        DeviceSearchCriteria c = DeviceSearchCriteria.from("v1", "all", "all", json, 123);
        assertThat(c.getSearches().get(0).column()).isNull();
        assertThat(c.getSearches().get(0).condition()).isEqualTo(DeviceSearchCriteria.Cond.CONTAINS);
    }
}
```

- [ ] **Step 2: Run to verify it fails to compile**

```powershell
& .\mvnw.cmd -q "-Dtest=DeviceSearchCriteriaTest" test
```
Expected: compile error — class missing.

- [ ] **Step 3: Implement DeviceSearchCriteria**

```java
package io.sclera.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Typed, parse-once representation of the multipleKeywordSearchSortFilter* request
 * (condition string + search_sort_filter_details JSON + onboard_status param).
 * All fastjson handling lives here; DeviceSearchQueryBuilder never sees JSON.
 *
 * Sentinel translation (was inlined into SQL as constant-folding tricks):
 *   monitor 123 -> null (no predicate); onboard 123 -> null; onboard 210 -> "not 3" flag;
 *   vdmsid 'null' -> null; dockername 'all' -> null.
 *
 * Consistency fix (documented): onboardpending/onboardcompleted were previously handled
 * ONLY by the Count variant (count filtered while the page didn't). All variants now share
 * this parser, so the full condition set applies uniformly.
 */
public class DeviceSearchCriteria {

    public enum Cond { CONTAINS, DOES_NOT_CONTAIN, EQUAL_TO, NOT_EQUAL_TO, STARTS_WITH, ENDS_WITH;
        static Cond parse(String s) {
            if (s == null) return CONTAINS;
            return switch (s) {
                case "does_not_contain" -> DOES_NOT_CONTAIN;
                case "equal_to" -> EQUAL_TO;
                case "not_equal_to" -> NOT_EQUAL_TO;
                case "starts_with" -> STARTS_WITH;
                case "ends_with" -> ENDS_WITH;
                default -> CONTAINS;
            };
        }
    }

    /** column==null means search-all. */
    public record KeywordSearch(String column, boolean custom, String value, Cond condition) {}
    /** condition is the raw string: is_present / is_not_present / retag / not_added_exception. */
    public record ColumnFilter(String column, boolean custom, String condition, Object value) {}
    public record FeatureFilter(String name, String condition) {}
    public record SortSpec(String column, boolean custom) {}

    private String vdmsId;
    private String dockerName;
    private Integer monitor;            // null = no predicate; 1 = strict; other = "is null or ="
    private Integer status;
    private boolean virtualOther;       // condition 'other'
    private Integer assignedStatus;     // null / 0 / 1
    private Integer assetMatchStatus;   // null / 0..3
    private Integer onboardStatusEquals;
    private boolean onboardStatusNot3;  // condition 'notonboarded' or param 210
    private List<String> deviceIds = new ArrayList<>();
    private List<ColumnFilter> columnFilters = new ArrayList<>();
    private List<FeatureFilter> featureFilters = new ArrayList<>();
    private List<KeywordSearch> searches = new ArrayList<>();
    private SortSpec sort;

    private DeviceSearchCriteria() {}

    public static DeviceSearchCriteria from(String vdmsid, String dockername, String condition,
                                            JSONObject details, Integer onboardStatusParam) {
        DeviceSearchCriteria c = new DeviceSearchCriteria();
        c.vdmsId = (vdmsid == null || "null".equals(vdmsid)) ? null : vdmsid;
        c.dockerName = (dockername == null || "all".equals(dockername)) ? null : dockername;

        switch (condition == null ? "all" : condition) {
            case "unmonitored" -> c.monitor = 0;
            case "online" -> { c.monitor = 1; c.status = 1; }
            case "offline" -> { c.monitor = 1; c.status = 0; }
            case "other" -> c.virtualOther = true;
            case "matched" -> c.assetMatchStatus = 1;
            case "unmatched" -> c.assetMatchStatus = 0;
            case "verified" -> c.assetMatchStatus = 2;
            case "archived" -> c.assetMatchStatus = 3;
            case "onboarded" -> c.onboardStatusEquals = 3;
            case "notonboarded" -> c.onboardStatusNot3 = true;
            case "onboardpending" -> c.onboardStatusEquals = 1;
            case "onboardcompleted" -> c.onboardStatusEquals = 2;
            case "assigned" -> c.assignedStatus = 1;
            case "unassigned" -> c.assignedStatus = 0;
            default -> { /* "all": no predicates */ }
        }
        // The onboard_status method param applies only when the condition didn't decide it.
        if (!c.onboardStatusNot3 && c.onboardStatusEquals == null
                && onboardStatusParam != null && onboardStatusParam != 123) {
            if (onboardStatusParam == 210) c.onboardStatusNot3 = true;
            else c.onboardStatusEquals = onboardStatusParam;
        }

        if (details != null) {
            JSONArray ids = details.getJSONArray("device_ids");
            if (ids != null) for (int i = 0; i < ids.size(); i++) c.deviceIds.add(ids.getString(i));

            JSONObject filters = details.getJSONObject("filter_details");
            if (filters != null) {
                JSONArray cols = filters.getJSONArray("column_details");
                if (cols != null) for (int i = 0; i < cols.size(); i++) {
                    JSONObject f = cols.getJSONObject(i);
                    c.columnFilters.add(new ColumnFilter(f.getString("column"),
                            Boolean.TRUE.equals(f.getBoolean("custom")),
                            f.getString("condition"), f.get("value")));
                }
                JSONArray feats = filters.getJSONArray("feature_details");
                if (feats != null) for (int i = 0; i < feats.size(); i++) {
                    JSONObject f = feats.getJSONObject(i);
                    c.featureFilters.add(new FeatureFilter(f.getString("name"), f.getString("condition")));
                }
            }

            JSONArray searches = details.getJSONArray("search_details");
            if (searches != null) for (int i = 0; i < searches.size(); i++) {
                JSONObject s = searches.getJSONObject(i);
                String column = s.get("column") == null ? null
                        : String.valueOf(s.get("column")).replaceAll("\\s", "");
                c.searches.add(new KeywordSearch(column,
                        Boolean.TRUE.equals(s.getBoolean("custom")),
                        s.getString("value"), Cond.parse(s.getString("condition"))));
            }

            JSONObject sort = details.getJSONObject("sort_details");
            if (sort != null) {
                c.sort = new SortSpec(String.valueOf(sort.get("column")).replaceAll("\\s", ""),
                        Boolean.TRUE.equals(sort.getBoolean("custom")));
            }
        }
        return c;
    }

    public String getVdmsId() { return vdmsId; }
    public String getDockerName() { return dockerName; }
    public Integer getMonitor() { return monitor; }
    public Integer getStatus() { return status; }
    public boolean isVirtualOther() { return virtualOther; }
    public Integer getAssignedStatus() { return assignedStatus; }
    public Integer getAssetMatchStatus() { return assetMatchStatus; }
    public Integer getOnboardStatusEquals() { return onboardStatusEquals; }
    public boolean isOnboardStatusNot3() { return onboardStatusNot3; }
    public List<String> getDeviceIds() { return deviceIds; }
    public List<ColumnFilter> getColumnFilters() { return columnFilters; }
    public List<FeatureFilter> getFeatureFilters() { return featureFilters; }
    public List<KeywordSearch> getSearches() { return searches; }
    public SortSpec getSort() { return sort; }
}
```

- [ ] **Step 4: Run the unit test**

```powershell
& .\mvnw.cmd -q "-Dtest=DeviceSearchCriteriaTest" test
```
Expected: `Tests run: 8, Failures: 0, Errors: 0` (pure unit test, no Docker needed).

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/dto/DeviceSearchCriteria.java sclera-cloud-device-asset/src/test/java/io/sclera/dto/DeviceSearchCriteriaTest.java
git commit -m "feat: DeviceSearchCriteria typed request parser for device search family"
```

---

### Task 5: DeviceSearchQueryBuilder core (scope + condition predicates, ids/count, default sort)

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java`

- [ ] **Step 1: Write the failing IT (core slice)**

```java
package io.sclera.it;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.DeviceSearchCriteria;
import io.sclera.queryrepository.DeviceSearchQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Result-asserting coverage for the Criteria rewrite of the device multi-keyword
 * search/sort/filter family. Seed: /seed/device-search-it.sql (dsx1..dsx5 in vdms v1,
 * dsx9 in v2; see the seed file header comments for each device's role).
 */
@Sql(scripts = "/schema-pg.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/seed/device-search-it.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup-device-search-it.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
class DeviceSearchQueryBuilderIT extends PostgresJpaIT {

    @PersistenceContext
    EntityManager em;

    DeviceSearchQueryBuilder builder;

    @BeforeEach
    void setUp() { builder = new DeviceSearchQueryBuilder(em); }

    private static DeviceSearchCriteria crit(String condition, String json) {
        return DeviceSearchCriteria.from("v1", "all", condition, JSONObject.parseObject(json), 123);
    }

    // ---- scope + condition switch ----

    @Test
    void all_scopedToVdms_excludesArchivedAndForeign() {
        List<String> ids = builder.findAllIds(crit("all", "{}"));
        // default sort: updated_timestamp DESC -> dsx1(500), dsx2(400), dsx3(300), dsx5(100)
        // dsx4 excluded (archived, asset_match_status=3); dsx9 excluded (vdms v2)
        assertThat(ids).containsExactly("dsx1", "dsx2", "dsx3", "dsx5");
    }

    @Test
    void nullVdms_seesAllVdms() {
        DeviceSearchCriteria c = DeviceSearchCriteria.from("null", "all", "all", new JSONObject(), 123);
        assertThat(builder.findAllIds(c)).contains("dsx9");
    }

    @Test
    void online_offline_unmonitored() {
        assertThat(builder.findAllIds(crit("online", "{}"))).containsExactly("dsx1");
        assertThat(builder.findAllIds(crit("offline", "{}"))).containsExactly("dsx2");
        assertThat(builder.findAllIds(crit("unmonitored", "{}"))).containsExactly("dsx3");
    }

    @Test
    void other_isVirtualDeviceTypeOutside01() {
        assertThat(builder.findAllIds(crit("other", "{}"))).containsExactly("dsx5");
    }

    @Test
    void matched_unmatched_verified_archived() {
        assertThat(builder.findAllIds(crit("matched", "{}"))).containsExactly("dsx1");
        assertThat(builder.findAllIds(crit("unmatched", "{}"))).containsExactly("dsx2", "dsx5");
        assertThat(builder.findAllIds(crit("verified", "{}"))).containsExactly("dsx3");
        assertThat(builder.findAllIds(crit("archived", "{}"))).containsExactly("dsx4");
    }

    @Test
    void onboarded_notonboarded() {
        assertThat(builder.findAllIds(crit("onboarded", "{}"))).containsExactly("dsx1");
        // not-3: dsx2(1), dsx3(2); dsx5 has NULL onboard_status -> excluded (preserved != semantics)
        assertThat(builder.findAllIds(crit("notonboarded", "{}"))).containsExactly("dsx2", "dsx3");
    }

    @Test
    void assigned_unassigned_nullStringCountsAsUnassigned() {
        // 'assigned' preserves the legacy OR quirk: (IS NOT NULL OR != 'null') — any non-NULL
        // value matches, INCLUDING the literal 'null' string (dsx3). Preserved verbatim.
        assertThat(builder.findAllIds(crit("assigned", "{}"))).containsExactly("dsx1", "dsx3");
        // unassigned: dsx2 (SQL NULL), dsx3 (literal 'null'), dsx5 (SQL NULL — never assigned)
        assertThat(builder.findAllIds(crit("unassigned", "{}"))).containsExactly("dsx2", "dsx3", "dsx5");
    }

    // ---- pagination + count ----

    @Test
    void pagination_slices_inSortOrder() {
        assertThat(builder.findIds(crit("all", "{}"), 1, 2)).containsExactly("dsx1", "dsx2");
        assertThat(builder.findIds(crit("all", "{}"), 2, 2)).containsExactly("dsx3", "dsx5");
    }

    @Test
    void count_matchesFindAllIds() {
        assertThat(builder.count(crit("all", "{}"))).isEqualTo(4);
        assertThat(builder.count(crit("online", "{}"))).isEqualTo(1);
    }

    // ---- device_ids filter (double-quote PG bug fixed by binding) ----

    @Test
    void deviceIdsFilter_restrictsToGivenIds() {
        List<String> ids = builder.findAllIds(crit("all", "{\"device_ids\":[\"dsx2\",\"dsx5\"]}"));
        assertThat(ids).containsExactly("dsx2", "dsx5");
    }
}
```

- [ ] **Step 2: Run to verify it fails to compile**

```powershell
& .\mvnw.cmd -q "-Dtest=DeviceSearchQueryBuilderIT" test
```
Expected: compile error — builder class missing.

- [ ] **Step 3: Implement the builder core**

`src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java`:

```java
package io.sclera.queryrepository;

import io.sclera.dto.DeviceSearchCriteria;
import io.sclera.dto.DeviceSearchCriteria.Cond;
import io.sclera.dto.DeviceSearchCriteria.KeywordSearch;
import io.sclera.models.Device;
import io.sclera.models.DeviceSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.query.criteria.JpaFrom;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Type-safe Criteria replacement for the string-built SQL of
 * DeviceSearchService.multipleKeywordSearchSortFilterDevices / ...ForAssetExport / ...Count.
 *
 * Query shape mirrors the original: outer query on Device selecting d.id with
 * "d.id IN (subquery carrying all filter/search predicates and joins)", preserving the
 * dedupe-then-sort behaviour of the legacy SQL. The Count variant counts the same subquery.
 *
 * Documented behaviour fixes vs the legacy SQL (see spec 2026-06-12):
 *  - values are BOUND (no SQL injection; device_ids double-quote PG bug gone);
 *  - REGEXP_REPLACE carries 'g' (legacy PG port stripped only the first special char);
 *  - sorting by created/updated_timestamp no longer emits bigint = '' (PG type error);
 *  - sorting by assignee_email/username/email now joins what it references (legacy outer
 *    query referenced dos./ds. aliases it never joined -> SQL error).
 */
@Component
public class DeviceSearchQueryBuilder {

    /** Java-side analog of strip_specials — identical class as the legacy replaceAll. */
    private static final String JAVA_SPECIALS = "[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]";

    private final EntityManager em;

    public DeviceSearchQueryBuilder(EntityManager em) { this.em = em; }

    // ------------------------------------------------------------------ public API

    public List<String> findIds(DeviceSearchCriteria c, int pageNo, int pageSize) {
        TypedQuery<String> q = buildIdQuery(c);
        q.setFirstResult(pageSize * (pageNo - 1));
        q.setMaxResults(pageSize);
        return q.getResultList();
    }

    public List<String> findAllIds(DeviceSearchCriteria c) {
        return buildIdQuery(c).getResultList();
    }

    public long count(DeviceSearchCriteria c) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Device> d = cq.from(Device.class);
        cq.select(cb.count(d.get("id")));
        cq.where(d.get("id").in(matchingIds(cb, cq, c)));
        return em.createQuery(cq).getSingleResult();
    }

    // ------------------------------------------------------------------ outer query

    private TypedQuery<String> buildIdQuery(DeviceSearchCriteria c) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<Device> d = cq.from(Device.class);
        cq.select(d.get("id"));
        cq.where(d.get("id").in(matchingIds(cb, cq, c)));
        cq.orderBy(buildOrders(cb, new Ctx(cb, d), c));
        return em.createQuery(cq);
    }

    /** Subquery selecting matching device ids, carrying every filter/search predicate. */
    private Subquery<String> matchingIds(CriteriaBuilder cb, AbstractQuery<?> parent,
                                         DeviceSearchCriteria c) {
        Subquery<String> sub = parent.subquery(String.class);
        Root<Device> d = sub.from(Device.class);
        sub.select(d.get("id"));
        Ctx ctx = new Ctx(cb, d);

        List<Predicate> ps = new ArrayList<>();
        addScopeAndConditionPredicates(cb, ctx, c, ps);
        addColumnFilterPredicates(cb, ctx, c, ps, sub);
        addFeatureFilterPredicates(cb, ctx, c, ps, sub);
        addSearchPredicates(cb, ctx, c, ps);
        sub.where(ps.toArray(new Predicate[0]));
        return sub;
    }

    // ------------------------------------------------------------------ scope + condition

    private void addScopeAndConditionPredicates(CriteriaBuilder cb, Ctx ctx,
                                                DeviceSearchCriteria c, List<Predicate> ps) {
        From<?, Device> d = ctx.d;
        if (c.getVdmsId() != null) ps.add(cb.equal(d.get("docker_vdms_id"), c.getVdmsId()));
        if (c.getDockerName() != null) ps.add(cb.equal(d.get("docker_name"), c.getDockerName()));

        if (c.isOnboardStatusNot3()) {
            // preserved legacy semantics: NULL onboard_status does NOT match "!= 3"
            ps.add(cb.notEqual(d.get("onboard_status"), 3));
        } else if (c.getOnboardStatusEquals() != null) {
            ps.add(cb.equal(d.get("onboard_status"), c.getOnboardStatusEquals()));
        }

        if (c.isVirtualOther()) {
            Expression<Integer> vdt = d.get("virtual_device_type");
            ps.add(cb.and(cb.isNotNull(vdt), cb.notEqual(vdt, 0), cb.notEqual(vdt, 1)));
        }

        if (c.getStatus() != null) ps.add(cb.equal(d.get("status"), c.getStatus()));

        if (c.getMonitor() != null) {
            Expression<Integer> m = d.get("monitor");
            if (c.getMonitor() == 1) ps.add(cb.equal(m, 1));
            else ps.add(cb.or(cb.isNull(m), cb.equal(m, c.getMonitor())));
        }

        if (c.getAssignedStatus() != null) {
            Expression<String> aue = d.get("assigned_user_email");
            if (c.getAssignedStatus() == 0) {
                ps.add(cb.or(cb.isNull(aue), cb.equal(aue, "null")));
            } else {
                // preserved verbatim from legacy SQL (OR of the two arms)
                ps.add(cb.or(cb.isNotNull(aue), cb.notEqual(aue, "null")));
            }
        }

        Expression<Integer> ams = d.get("asset_match_status");
        if (c.getAssetMatchStatus() == null) {
            ps.add(cb.notEqual(ams, 3));
        } else if (c.getAssetMatchStatus() == 3) {
            ps.add(cb.equal(ams, 3));
        } else {
            ps.add(cb.and(cb.equal(ams, c.getAssetMatchStatus()), cb.notEqual(ams, 3)));
        }

        if (!c.getDeviceIds().isEmpty()) {
            ps.add(ctx.d.get("id").in(c.getDeviceIds()));
        }
    }

    // ------------------------------------------------------------------ filters/search: Tasks 6-8

    private void addColumnFilterPredicates(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c,
                                           List<Predicate> ps, AbstractQuery<?> sub) {
        // Task 6
    }

    private void addFeatureFilterPredicates(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c,
                                            List<Predicate> ps, AbstractQuery<?> sub) {
        // Task 7
    }

    private void addSearchPredicates(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c,
                                     List<Predicate> ps) {
        // Task 8
    }

    // ------------------------------------------------------------------ sort (Task 9 extends)

    private List<Order> buildOrders(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c) {
        // default: ORDER BY (updated_timestamp IS NULL), updated_timestamp DESC, id
        List<Order> orders = new ArrayList<>();
        Expression<?> ut = ctx.d.get("updated_timestamp");
        orders.add(cb.asc(cb.selectCase().when(cb.isNull(ut), 1).otherwise(0)));
        orders.add(cb.desc(ut));
        orders.add(cb.asc(ctx.d.get("id")));
        return orders;
    }

    // ------------------------------------------------------------------ shared helpers

    /** Lazily-created LEFT joins off a Device root; used by both inner subquery and outer sort. */
    static final class Ctx {
        final CriteriaBuilder cb;
        final From<?, Device> d;
        private Join<?, ?> dos, dosa, location, floor, building;
        private From<?, ?> ds;

        Ctx(CriteriaBuilder cb, From<?, Device> d) { this.cb = cb; this.d = d; }

        Join<?, ?> dos() {
            if (dos == null) dos = d.join("device_onboard_status", JoinType.LEFT);
            return dos;
        }
        Join<?, ?> dosa() {
            if (dosa == null) dosa = dos().join("device_onboard_status_assignees", JoinType.LEFT);
            return dosa;
        }
        Join<?, ?> location() {
            if (location == null) location = d.join("location", JoinType.LEFT);
            return location;
        }
        Join<?, ?> floor() {
            if (floor == null) floor = location().join("floor", JoinType.LEFT);
            return floor;
        }
        Join<?, ?> building() {
            if (building == null) building = floor().join("building", JoinType.LEFT);
            return building;
        }
        /** Ad-hoc entity LEFT JOIN (Device has no inverse association to DeviceSpecification). */
        From<?, ?> ds() {
            if (ds == null) {
                var j = ((JpaFrom<?, Device>) d).join(DeviceSpecification.class, SqmJoinType.LEFT);
                j.on(cb.equal(j.get("device").get("id"), d.get("id")));
                ds = j;
            }
            return ds;
        }
    }

    /** Java-side special-character strip — must stay identical to the legacy replaceAll. */
    static String stripSpecials(String s) {
        return s == null ? null : s.replaceAll(JAVA_SPECIALS, "");
    }

    /** Builds the bound jsonpath '$[*]."key"' with the key's quotes/backslashes escaped. */
    static String jsonPathFor(String key) {
        return "$[*].\"" + key.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
```

API note (ad-hoc join): if `JpaFrom.join(Class, SqmJoinType)` does not resolve on Hibernate 7.2,
check `org.hibernate.query.criteria.JpaFrom` / `JpaRoot` javadoc in the IDE for the entity-join
overload (7.x renamed generics to `JpaEntityJoin<L,R>`; the method exists on `JpaFrom`). The `var`
declaration absorbs the return-type difference.

- [ ] **Step 4: Run the IT**

```powershell
& .\mvnw.cmd -q "-Dtest=DeviceSearchQueryBuilderIT" test
```
Expected: `Tests run: 10, Failures: 0, Errors: 0`.

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java
git commit -m "feat: DeviceSearchQueryBuilder core (scope/condition predicates, ids/count, default sort)"
```

---

### Task 6: Column filters

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java`
- Modify: `sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java`

- [ ] **Step 1: Add failing IT tests**

```java
    // ---- column filters ----

    private static String filterJson(String columnDetails) {
        return "{\"filter_details\":{\"column_details\":[" + columnDetails + "]}}";
    }

    @Test
    void customFieldFilter_isPresent_and_isNotPresent() {
        assertThat(builder.findAllIds(crit("all",
                filterJson("{\"column\":\"department\",\"custom\":true,\"condition\":\"is_present\"}"))))
                .containsExactly("dsx1", "dsx2");
        assertThat(builder.findAllIds(crit("all",
                filterJson("{\"column\":\"department\",\"custom\":true,\"condition\":\"is_not_present\"}"))))
                .containsExactly("dsx3", "dsx5");
    }

    @Test
    void standardFilter_presentWithValue_equality() {
        assertThat(builder.findAllIds(crit("all",
                filterJson("{\"column\":\"vendor\",\"custom\":false,\"condition\":\"is_present\",\"value\":\"Cisco\"}"))))
                .containsExactly("dsx1");
    }

    @Test
    void assigneeEmailFilter_matchesDosOrDosa() {
        assertThat(builder.findAllIds(crit("all",
                filterJson("{\"column\":\"assignee_email\",\"custom\":false,\"condition\":\"is_present\",\"value\":\"tech2@sclera.com\"}"))))
                .containsExactly("dsx1"); // matches via dosa.email
    }

    @Test
    void typeFilter_inList_and_notInList() {
        assertThat(builder.findAllIds(crit("all",
                filterJson("{\"column\":\"type\",\"custom\":false,\"condition\":\"is_present\",\"value\":[\"router\",\"switch\"]}"))))
                .containsExactly("dsx1", "dsx2");
        assertThat(builder.findAllIds(crit("all",
                filterJson("{\"column\":\"type\",\"custom\":false,\"condition\":\"is_not_present\",\"value\":[\"router\",\"switch\"]}"))))
                .containsExactly("dsx3", "dsx5");
    }

    @Test
    void categoryFilter_categoryWithSubcategories() {
        assertThat(builder.findAllIds(crit("all",
                filterJson("{\"column\":\"category\",\"custom\":false,\"condition\":\"is_present\","
                        + "\"value\":{\"Hardware\":[\"Server\"]}}"))))
                .containsExactly("dsx1");
    }

    @Test
    void osTypeFilter_viaDeviceSpecification() {
        assertThat(builder.findAllIds(crit("all",
                filterJson("{\"column\":\"os_type\",\"custom\":false,\"condition\":\"is_present\",\"value\":\"windows\"}"))))
                .containsExactly("dsx1");
    }

    @Test
    void presentWithoutValue_meansNotNullNotEmpty() {
        assertThat(builder.findAllIds(crit("all",
                filterJson("{\"column\":\"serial_number\",\"custom\":false,\"condition\":\"is_present\"}"))))
                .containsExactly("dsx1");
    }
```

- [ ] **Step 2: Run to verify the new tests fail**

```powershell
& .\mvnw.cmd -q "-Dtest=DeviceSearchQueryBuilderIT" test
```
Expected: the 7 new tests FAIL (filters silently ignored — results unfiltered); the 10 core tests still pass.

- [ ] **Step 3: Implement addColumnFilterPredicates + resolve helpers**

Replace the Task-6 stub and add the helpers:

```java
    private void addColumnFilterPredicates(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c,
                                           List<Predicate> ps, AbstractQuery<?> sub) {
        for (DeviceSearchCriteria.ColumnFilter f : c.getColumnFilters()) {
            if (f.custom()) {
                Expression<String> v = customFieldText(cb, ctx, f.column());
                if ("is_present".equals(f.condition())) {
                    ps.add(cb.and(cb.isNotNull(v), cb.notEqual(v, ""), cb.notEqual(v, "null")));
                } else if ("is_not_present".equals(f.condition())) {
                    ps.add(cb.or(cb.isNull(v), cb.equal(v, ""), cb.equal(v, "null")));
                }
                continue;
            }
            boolean present = "is_present".equals(f.condition());
            if (!present && !"is_not_present".equals(f.condition())) continue; // legacy: other values ignored
            Object value = f.value();
            if (value != null) {
                ps.add(columnValuePredicate(cb, ctx, f.column(), value, present));
            } else if (present) {
                Expression<String> col = resolveString(cb, ctx, f.column());
                ps.add(cb.and(cb.isNotNull(col), cb.notEqual(col, "")));
            } else {
                Expression<String> col = resolveString(cb, ctx, f.column());
                ps.add(cb.or(cb.isNull(col), cb.equal(col, "")));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Predicate columnValuePredicate(CriteriaBuilder cb, Ctx ctx, String column,
                                           Object value, boolean present) {
        switch (column) {
            case "assignee_email" -> {
                // legacy: only the is_present arm special-cases assignee_email
                if (present) {
                    return cb.or(cb.equal(ctx.dos().get("assignee_email"), String.valueOf(value)),
                                 cb.equal(ctx.dosa().get("email"), String.valueOf(value)));
                }
                return cb.notEqual(resolveString(cb, ctx, column), String.valueOf(value));
            }
            case "type", "asset_group" -> {
                List<String> values = new ArrayList<>();
                for (Object o : (com.alibaba.fastjson.JSONArray) value) values.add(String.valueOf(o));
                Predicate in = resolveString(cb, ctx, column).in(values);
                return present ? in : in.not();
            }
            case "category" -> {
                com.alibaba.fastjson.JSONObject categories = (com.alibaba.fastjson.JSONObject) value;
                List<Predicate> clauses = new ArrayList<>();
                for (String cat : categories.keySet()) {
                    com.alibaba.fastjson.JSONArray subs = categories.getJSONArray(cat);
                    Predicate p = cb.equal(ctx.d.get("category"), cat);
                    if (subs != null && !subs.isEmpty()) {
                        List<String> subList = new ArrayList<>();
                        for (Object o : subs) subList.add(String.valueOf(o));
                        Predicate subIn = ctx.d.<String>get("sub_category").in(subList);
                        p = cb.and(p, present ? subIn : subIn.not());
                    }
                    clauses.add(p);
                }
                return cb.or(clauses.toArray(new Predicate[0]));
            }
            case "assigned_user_email" -> {
                return present ? cb.equal(ctx.d.get("assigned_user_email"), String.valueOf(value))
                               : cb.notEqual(ctx.d.get("assigned_user_email"), String.valueOf(value));
            }
            case "os_type" -> {
                // legacy special-cases os_type only in the is_present arm; keep symmetric (= / <>)
                return present ? cb.equal(ctx.ds().get("osType"), String.valueOf(value))
                               : cb.notEqual(ctx.ds().get("osType"), String.valueOf(value));
            }
            default -> {
                Expression<String> col = resolveString(cb, ctx, column);
                return present ? cb.equal(col, String.valueOf(value))
                               : cb.notEqual(col, String.valueOf(value));
            }
        }
    }

    /**
     * Criteria analog of updateDeviceSearchColumnName: maps a UI column name to a STRING
     * expression. Non-string columns (timestamps) are stringified via concat_ws so
     * comparisons/LIKEs behave like the legacy implicit text cast.
     */
    private Expression<String> resolveString(CriteriaBuilder cb, Ctx ctx, String column) {
        From<?, Device> d = ctx.d;
        return switch (column) {
            case "id" -> d.get("id");
            case "display_name" -> userDataFallback(cb, d, "user_data_name", "display_name");
            case "vendor" -> userDataFallback(cb, d, "user_data_vendor", "vendor");
            case "model" -> userDataFallback(cb, d, "user_data_model", "model");
            case "type" -> d.get("type");
            case "ip_address" -> d.get("ip_address");
            case "mac_address" -> d.get("mac_address");
            case "location" -> ctx.location().get("name");
            case "floor" -> ctx.floor().get("name");
            case "building" -> ctx.building().get("name");
            case "warranty" -> d.get("warranty");
            case "latitude" -> d.get("latitude");
            case "longitude" -> d.get("longitude");
            case "serial_number" -> d.get("serial_number");
            case "created_timestamp" -> asText(cb, d.get("created_timestamp"));
            case "updated_timestamp" -> asText(cb, d.get("updated_timestamp"));
            case "assignee_email" -> ctx.dos().get("assignee_email");
            case "description" -> d.get("description");
            case "asset_group" -> d.get("asset_group");
            case "category" -> d.get("category");
            case "sub_category" -> d.get("sub_category");
            case "assigned_user_email" -> d.get("assigned_user_email");
            case "username" -> ctx.ds().get("username");
            case "email" -> ctx.ds().get("email");
            default -> userDataFallback(cb, d, "user_data_name", "display_name");
        };
    }

    /** CASE WHEN user_data_x IS NULL OR '' THEN x ELSE user_data_x END (shared, was copied 5x). */
    private Expression<String> userDataFallback(CriteriaBuilder cb, From<?, Device> d,
                                                String userField, String baseField) {
        return cb.<String>selectCase()
                .when(cb.or(cb.isNull(d.get(userField)), cb.equal(d.get(userField), "")),
                      d.get(baseField))
                .otherwise(d.get(userField));
    }

    /** Stringify any expression via concat_ws (PG variadic any -> text). */
    private Expression<String> asText(CriteriaBuilder cb, Expression<?> e) {
        return cb.function("concat_ws", String.class, cb.literal(""), e, cb.literal(""));
    }

    /** custom_field_text(d.custom_fields, '$[*]."key"') with the key bound, not concatenated. */
    private Expression<String> customFieldText(CriteriaBuilder cb, Ctx ctx, String key) {
        return cb.function("custom_field_text", String.class,
                ctx.d.get("custom_fields"), cb.literal(jsonPathFor(key)));
    }
```

- [ ] **Step 4: Run the IT**

```powershell
& .\mvnw.cmd -q "-Dtest=DeviceSearchQueryBuilderIT" test
```
Expected: `Tests run: 17, Failures: 0, Errors: 0`.

- [ ] **Step 5: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java
git commit -m "feat: column filters (standard/custom/assignee/type/category/os_type) in DeviceSearchQueryBuilder"
```

---

### Task 7: Feature filters

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java`
- Modify: `sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java`

- [ ] **Step 1: Add failing IT tests**

```java
    // ---- feature filters ----

    private static String featureJson(String name, String condition) {
        return "{\"filter_details\":{\"feature_details\":[{\"name\":\"" + name
                + "\",\"condition\":\"" + condition + "\"}]}}";
    }

    @Test
    void qrcodeFeature_existsOnQrOrClientQr() {
        assertThat(builder.findAllIds(crit("all", featureJson("qrcode", "is_present"))))
                .containsExactly("dsx1");
        assertThat(builder.findAllIds(crit("all", featureJson("qrcode", "is_not_present"))))
                .containsExactly("dsx2", "dsx3", "dsx5");
    }

    @Test
    void barcodeAndNfcFeatures() {
        assertThat(builder.findAllIds(crit("all", featureJson("barcode", "is_present"))))
                .containsExactly("dsx2");
        assertThat(builder.findAllIds(crit("all", featureJson("nfc", "is_present"))))
                .containsExactly("dsx2");
    }

    @Test
    void adcFeature_mapsToSourceType() {
        assertThat(builder.findAllIds(crit("all", featureJson("adc", "is_present"))))
                .containsExactly("dsx2");
        // is_not_present means source_type = 'vdms' (legacy semantics, not a negation)
        assertThat(builder.findAllIds(crit("all", featureJson("adc", "is_not_present"))))
                .containsExactly("dsx1", "dsx3", "dsx5");
    }

    @Test
    void countAndImageFeatures() {
        assertThat(builder.findAllIds(crit("all", featureJson("record_checklist", "is_present"))))
                .containsExactly("dsx1");
        assertThat(builder.findAllIds(crit("all", featureJson("document", "is_not_present"))))
                .containsExactly("dsx2", "dsx3", "dsx5");
        assertThat(builder.findAllIds(crit("all", featureJson("asset_image_url", "is_present"))))
                .containsExactly("dsx1");
    }

    @Test
    void sensorAlertFeature() {
        assertThat(builder.findAllIds(crit("all", featureJson("sensor_alert", "is_present"))))
                .containsExactly("dsx1"); // monnit_status='alert'
        assertThat(builder.findAllIds(crit("all", featureJson("sensor_alert", "is_not_present"))))
                .containsExactly("dsx2", "dsx3", "dsx5");
    }

    @Test
    void dosStatusFeatures() {
        assertThat(builder.findAllIds(crit("all", featureJson("geolocation_status", "is_present"))))
                .containsExactly("dsx1"); // geolocation_status=1
        assertThat(builder.findAllIds(crit("all", featureJson("field_status", "retag"))))
                .containsExactly("dsx1"); // field_status=2
        assertThat(builder.findAllIds(crit("all", featureJson("tag_status", "not_added_exception"))))
                .containsExactly("dsx1"); // tag_status=3
    }
```

- [ ] **Step 2: Run to verify the new tests fail, then implement**

Replace the Task-7 stub:

```java
    private static final String[] SENSOR_STATUS_FIELDS = {
            "monnit_status", "pelican_status", "knx_status", "snmp_object_status",
            "daintree_status", "ecobee_status", "bacnet_status", "lorawan_status",
            "my_devices_status", "measuring_instrument_status", "disruptive_status"};

    private void addFeatureFilterPredicates(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c,
                                            List<Predicate> ps, AbstractQuery<?> sub) {
        From<?, Device> d = ctx.d;
        for (DeviceSearchCriteria.FeatureFilter f : c.getFeatureFilters()) {
            String cond = f.condition();
            boolean present = "is_present".equals(cond);
            switch (f.name()) {
                case "qrcode" -> {
                    Predicate qc = existsForDevice(cb, sub, d, io.sclera.models.QrCode.class);
                    Predicate cqc = existsForDevice(cb, sub, d, io.sclera.models.ClientQrCode.class);
                    if (present) ps.add(cb.or(qc, cqc));
                    else if ("is_not_present".equals(cond)) ps.add(cb.and(qc.not(), cqc.not()));
                }
                case "barcode" -> {
                    Predicate cbc = existsForDevice(cb, sub, d, io.sclera.models.ClientBarCode.class);
                    if (present) ps.add(cbc);
                    else if ("is_not_present".equals(cond)) ps.add(cbc.not());
                }
                case "nfc" -> {
                    Predicate n = existsForDevice(cb, sub, d, io.sclera.models.Nfc.class);
                    Predicate cn = existsForDevice(cb, sub, d, io.sclera.models.ClientNfc.class);
                    if (present) ps.add(cb.or(n, cn));
                    else if ("is_not_present".equals(cond)) ps.add(cb.and(n.not(), cn.not()));
                }
                case "adc" -> {
                    if (present) ps.add(cb.equal(d.get("source_type"), "adc"));
                    else if ("is_not_present".equals(cond)) ps.add(cb.equal(d.get("source_type"), "vdms"));
                }
                case "record_checklist" -> addCountFeature(cb, d, "record_checklist_count", cond, ps);
                case "document" -> addCountFeature(cb, d, "document_count", cond, ps);
                case "measuring_instrument" -> addCountFeature(cb, d, "measuring_instrument_count", cond, ps);
                case "asset_image_url" -> {
                    Expression<String> url = d.get("asset_image_url");
                    if (present) ps.add(cb.and(cb.isNotNull(url), cb.notEqual(url, "[]")));
                    else if ("is_not_present".equals(cond)) ps.add(cb.or(cb.isNull(url), cb.equal(url, "[]")));
                }
                case "sensor_alert" -> {
                    List<Predicate> arms = new ArrayList<>();
                    for (String field : SENSOR_STATUS_FIELDS) {
                        Expression<String> s = d.get(field);
                        arms.add(present ? cb.equal(s, "alert")
                                         : cb.or(cb.isNull(s), cb.notEqual(s, "alert")));
                    }
                    ps.add(present ? cb.or(arms.toArray(new Predicate[0]))
                                   : cb.and(arms.toArray(new Predicate[0])));
                }
                case "geolocation_status", "image_status", "field_status", "tag_status" -> {
                    Integer v = switch (cond) {
                        case "is_not_present" -> 0;
                        case "is_present" -> 1;
                        case "retag" -> 2;
                        case "not_added_exception" -> 3;
                        default -> null;
                    };
                    if (v != null) ps.add(cb.equal(ctx.dos().get(f.name()), v));
                }
                default -> { /* unknown feature: legacy appends nothing */ }
            }
        }
    }

    private void addCountFeature(CriteriaBuilder cb, From<?, Device> d, String field,
                                 String cond, List<Predicate> ps) {
        Expression<Integer> count = d.get(field);
        if ("is_present".equals(cond)) ps.add(cb.greaterThan(count, 0));
        else if ("is_not_present".equals(cond)) ps.add(cb.or(cb.isNull(count), cb.equal(count, 0)));
    }

    /** EXISTS (SELECT 1 FROM <entity> e WHERE e.device.id = d.id). */
    private Predicate existsForDevice(CriteriaBuilder cb, AbstractQuery<?> parent,
                                      From<?, Device> d, Class<?> entity) {
        Subquery<Integer> ex = parent.subquery(Integer.class);
        Root<?> r = ex.from(entity);
        ex.select(cb.literal(1));
        ex.where(cb.equal(r.get("device").get("id"), d.get("id")));
        return cb.exists(ex);
    }
```

- [ ] **Step 3: Run the IT**

```powershell
& .\mvnw.cmd -q "-Dtest=DeviceSearchQueryBuilderIT" test
```
Expected: `Tests run: 23, Failures: 0, Errors: 0`.

- [ ] **Step 4: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java
git commit -m "feat: feature filters (qr/bar/nfc EXISTS, adc, counts, sensor alerts, dos statuses)"
```

---

### Task 8: Keyword search (per-column, custom-field, search-all haystack)

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java`
- Modify: `sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java`

- [ ] **Step 1: Add failing IT tests**

```java
    // ---- keyword search ----

    private static String searchJson(String column, boolean custom, String value, String condition) {
        String col = column == null ? "null" : "\"" + column + "\"";
        return "{\"search_details\":[{\"column\":" + col + ",\"custom\":" + custom
                + ",\"value\":\"" + value + "\",\"condition\":\"" + condition + "\"}]}";
    }

    @Test
    void columnSearch_contains_stripsSpecialsBothSides() {
        // value 'Cis-co!' strips to 'cisco'; dsx1 vendor 'Cisco' strips+lowers to 'cisco'
        assertThat(builder.findAllIds(crit("all", searchJson("vendor", false, "Cis-co!", "contains"))))
                .containsExactly("dsx1");
    }

    @Test
    void columnSearch_multiSpecialValue_regressionForGFlag() {
        // display_name 'Gamma!! Device' has TWO specials + a space; without the 'g' flag the
        // SQL side strips only the first '!' and the match fails.
        assertThat(builder.findAllIds(crit("all", searchJson("display_name", false, "Gamma!!Device", "equal_to"))))
                .containsExactly("dsx3");
    }

    @Test
    void columnSearch_displayName_userDataFallback() {
        // dsx1 has user_data_name 'My-Alpha' -> fallback prefers it over display_name
        assertThat(builder.findAllIds(crit("all", searchJson("display_name", false, "MyAlpha", "equal_to"))))
                .containsExactly("dsx1");
        assertThat(builder.findAllIds(crit("all", searchJson("display_name", false, "AlphaDevice", "equal_to"))))
                .isEmpty();
    }

    @Test
    void columnSearch_doesNotContain_and_notEqual() {
        assertThat(builder.findAllIds(crit("all", searchJson("type", false, "router", "does_not_contain"))))
                .containsExactly("dsx2", "dsx3", "dsx5");
        assertThat(builder.findAllIds(crit("all", searchJson("type", false, "router", "not_equal_to"))))
                .containsExactly("dsx2", "dsx3", "dsx5");
    }

    @Test
    void columnSearch_startsWith_endsWith() {
        assertThat(builder.findAllIds(crit("all", searchJson("type", false, "rou", "starts_with"))))
                .containsExactly("dsx1");
        assertThat(builder.findAllIds(crit("all", searchJson("type", false, "ter", "ends_with"))))
                .containsExactly("dsx1");
    }

    @Test
    void customColumnSearch_contains() {
        assertThat(builder.findAllIds(crit("all", searchJson("department", true, "Engineer", "contains"))))
                .containsExactly("dsx1");
        assertThat(builder.findAllIds(crit("all", searchJson("department", true, "Engineering", "equal_to"))))
                .containsExactly("dsx1");
    }

    @Test
    void searchAll_contains_matchesAnyColumnIncludingJoined() {
        // 'MainLab' lives on the joined location row
        assertThat(builder.findAllIds(crit("all", searchJson(null, false, "MainLab", "contains"))))
                .containsExactly("dsx1");
        // 'winuser01' lives on the ad-hoc-joined device_specification row
        assertThat(builder.findAllIds(crit("all", searchJson(null, false, "winuser01", "contains"))))
                .containsExactly("dsx1");
        // 'tech2' lives on the dosa row
        assertThat(builder.findAllIds(crit("all", searchJson(null, false, "tech2", "contains"))))
                .containsExactly("dsx1");
    }

    @Test
    void searchAll_equalTo_usesPlusMinusBoundaries() {
        // equal_to '%±term±%': the whole concat element must equal the term
        assertThat(builder.findAllIds(crit("all", searchJson(null, false, "router", "equal_to"))))
                .containsExactly("dsx1");
        // 'rout' is a substring, not a full element -> no match under equal_to
        assertThat(builder.findAllIds(crit("all", searchJson(null, false, "rout", "equal_to"))))
                .isEmpty();
    }

    @Test
    void searchAll_customFields_innerPositiveOuterPolarity() {
        // custom field value matches -> term injected into haystack -> contains matches
        assertThat(builder.findAllIds(crit("all", searchJson(null, false, "Engineering", "contains"))))
                .containsExactly("dsx1");
        // does_not_contain: dsx1 must be EXCLUDED because its custom fields contain the term
        assertThat(builder.findAllIds(crit("all", searchJson(null, false, "Engineering", "does_not_contain"))))
                .containsExactly("dsx2", "dsx3", "dsx5");
    }
```

- [ ] **Step 2: Run to verify the new tests fail, then implement**

Replace the Task-8 stub:

```java
    private void addSearchPredicates(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c,
                                     List<Predicate> ps) {
        for (KeywordSearch ks : c.getSearches()) {
            String strippedTerm = stripSpecials(ks.value()).toLowerCase();
            if (ks.column() == null) {
                ps.add(searchAllPredicate(cb, ctx, ks, strippedTerm));
            } else {
                Expression<String> raw = ks.custom()
                        ? customFieldText(cb, ctx, ks.column())
                        : resolveString(cb, ctx, ks.column());
                Expression<String> hay = cb.function("strip_specials", String.class,
                        cb.lower(asText(cb, raw)));
                ps.add(singleColumnCondition(cb, hay, ks.condition(), strippedTerm));
            }
        }
    }

    /** generateConditionedQuery analog (single standard/custom column). */
    private Predicate singleColumnCondition(CriteriaBuilder cb, Expression<String> hay,
                                            Cond cond, String term) {
        return switch (cond) {
            case CONTAINS -> cb.like(hay, "%" + term + "%");
            case DOES_NOT_CONTAIN -> cb.notLike(hay, "%" + term + "%");
            case EQUAL_TO -> cb.equal(hay, term);
            case NOT_EQUAL_TO -> cb.notEqual(hay, term);
            case STARTS_WITH -> cb.like(hay, term + "%");
            case ENDS_WITH -> cb.like(hay, "%" + term);
        };
    }

    /**
     * Search-all: one big LOWER(CONCAT_WS('±', ...)) haystack over the same columns as the
     * legacy SQL, including the custom-fields injector:
     *   COALESCE(CASE WHEN <custom-array-text matches POSITIVE pattern> THEN <term> ELSE '' END, '')
     * The injector is always POSITIVE — the outer ±-pattern applies the polarity
     * (NOT LIKE for does_not_contain/not_equal_to). This is intentional legacy design;
     * see the spec CORRECTION note.
     */
    private Predicate searchAllPredicate(CriteriaBuilder cb, Ctx ctx, KeywordSearch ks, String term) {
        From<?, Device> d = ctx.d;

        // custom-fields injector
        Expression<String> customArray = cb.function("strip_custom_specials", String.class,
                cb.lower(cb.function("custom_field_array_text", String.class,
                        d.get("custom_fields"), cb.literal("$[*].*"))));
        String positive = switch (ks.condition()) {
            case CONTAINS, DOES_NOT_CONTAIN -> "%" + term + "%";
            case EQUAL_TO, NOT_EQUAL_TO -> "%\"" + term + "\"%";
            case STARTS_WITH -> "%\"" + term + "%";
            case ENDS_WITH -> "%" + term + "\"%";
        };
        Expression<String> injector = cb.coalesce(
                cb.<String>selectCase()
                        .when(cb.like(customArray, positive), term)
                        .otherwise(""),
                cb.literal(""));

        Expression<String> hay = cb.function("strip_specials", String.class, cb.lower(
                cb.function("concat_ws", String.class,
                        cb.literal("±"), cb.literal(""),
                        d.get("id"),
                        userDataFallback(cb, d, "user_data_name", "display_name"),
                        userDataFallback(cb, d, "user_data_vendor", "vendor"),
                        userDataFallback(cb, d, "user_data_model", "model"),
                        d.get("type"), d.get("description"),
                        d.get("ip_address"), d.get("mac_address"),
                        d.get("latitude"), d.get("longitude"),
                        d.get("serial_number"), d.get("warranty"),
                        d.get("created_timestamp"),
                        ctx.location().get("name"), ctx.floor().get("name"), ctx.building().get("name"),
                        ctx.dos().get("assignee_email"), ctx.dosa().get("email"),
                        ctx.ds().get("username"), ctx.ds().get("email"),
                        injector)));

        return switch (ks.condition()) {
            case CONTAINS -> cb.like(hay, "%" + term + "%");
            case DOES_NOT_CONTAIN -> cb.notLike(hay, "%" + term + "%");
            case EQUAL_TO -> cb.like(hay, "%±" + term + "±%");
            case NOT_EQUAL_TO -> cb.notLike(hay, "%±" + term + "±%");
            case STARTS_WITH -> cb.like(hay, "%±" + term + "%");
            case ENDS_WITH -> cb.like(hay, "%" + term + "±%");
        };
    }
```

Two faithfulness notes (already encoded above — do not "fix" them):
- The strip happens OUTSIDE the lower+concat (legacy order: REGEXP_REPLACE(LOWER(CONCAT_WS(...)))) and
  the `±` separator survives the strip class.
- Escaping: search terms are already stripped of every LIKE metacharacter (`%`/`_` are in the strip
  class), so no LIKE-escape clause is needed; binding handles quote safety.

- [ ] **Step 3: Run the IT**

```powershell
& .\mvnw.cmd -q "-Dtest=DeviceSearchQueryBuilderIT" test
```
Expected: `Tests run: 32, Failures: 0, Errors: 0`.

- [ ] **Step 4: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java
git commit -m "feat: keyword search (per-column, custom-field, search-all haystack with custom-fields injector)"
```

---

### Task 9: Sort variants

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java`
- Modify: `sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java`

- [ ] **Step 1: Add failing IT tests**

```java
    // ---- sort ----

    private static String sortJson(String column, boolean custom) {
        return "{\"sort_details\":{\"column\":\"" + column + "\",\"custom\":" + custom + "}}";
    }

    @Test
    void sortByStandardColumn_nullsAndEmptiesLast() {
        // vendor: dsx1 Cisco, dsx2 HP, dsx3/dsx5 NULL (null-last, then by id tiebreak order n/a)
        List<String> ids = builder.findAllIds(crit("all", sortJson("vendor", false)));
        assertThat(ids.subList(0, 2)).containsExactly("dsx1", "dsx2");
        assertThat(ids.subList(2, 4)).containsExactlyInAnyOrder("dsx3", "dsx5");
    }

    @Test
    void sortByIpAddress_numericNotLexicographic() {
        List<String> ids = builder.findAllIds(crit("all", sortJson("ip_address", false)));
        // 9.1.1.1 (dsx5) < 10.0.0.2 (dsx1) < 10.0.0.10 (dsx2); dsx3 NULL ip last
        assertThat(ids).containsExactly("dsx5", "dsx1", "dsx2", "dsx3");
    }

    @Test
    void sortByCreatedTimestamp_descWithIdTiebreak_noBigintEmptyStringError() {
        // regression: legacy emitted "bigint = ''" -> PG error; must succeed now
        List<String> ids = builder.findAllIds(crit("all", sortJson("created_timestamp", false)));
        assertThat(ids).containsExactly("dsx5", "dsx3", "dsx2", "dsx1"); // 500,300,200,100 DESC
    }

    @Test
    void sortByCustomField_nonEmptyFirst_thenMissingLast() {
        List<String> ids = builder.findAllIds(crit("all", sortJson("department", true)));
        // Engineering (dsx1) < Finance (dsx2) asc; dsx3/dsx5 missing field -> last
        assertThat(ids.subList(0, 2)).containsExactly("dsx1", "dsx2");
        assertThat(ids.subList(2, 4)).containsExactlyInAnyOrder("dsx3", "dsx5");
    }

    @Test
    void sortByAssigneeEmail_joinsDos_regressionForMissingAlias() {
        // regression: legacy outer query referenced dos. without joining it -> SQL error
        List<String> ids = builder.findAllIds(crit("all", sortJson("assignee_email", false)));
        assertThat(ids.get(0)).isEqualTo("dsx1"); // tech1@... first; NULLs last
    }
```

- [ ] **Step 2: Run to verify the new tests fail, then implement**

Replace `buildOrders` from Task 5:

```java
    private List<Order> buildOrders(CriteriaBuilder cb, Ctx ctx, DeviceSearchCriteria c) {
        List<Order> orders = new ArrayList<>();
        DeviceSearchCriteria.SortSpec sort = c.getSort();
        if (sort == null) {
            Expression<?> ut = ctx.d.get("updated_timestamp");
            orders.add(nullsLast(cb, ut));
            orders.add(cb.desc(ut));
            orders.add(cb.asc(ctx.d.get("id")));
            return orders;
        }
        if (sort.custom()) {
            Expression<String> v = customFieldText(cb, ctx, sort.column());
            // ORDER BY (v IS NULL OR v = ''), v  -> non-empty values first, missing/empty last
            orders.add(cb.asc(cb.selectCase()
                    .when(cb.or(cb.isNull(v), cb.equal(v, "")), 1).otherwise(0)));
            orders.add(cb.asc(v));
            return orders;
        }
        if ("ip_address".equals(sort.column())) {
            Expression<String> ip = ctx.d.get("ip_address");
            orders.add(nullsLast(cb, ip));
            orders.add(cb.asc(cb.function("inet_val", String.class, ip)));
            return orders;
        }
        if ("created_timestamp".equals(sort.column()) || "updated_timestamp".equals(sort.column())) {
            // legacy appended "col = ''" here -> bigint = '' PG error; numeric columns skip it (fix)
            Expression<?> ts = ctx.d.get(sort.column());
            orders.add(nullsLast(cb, ts));
            orders.add(cb.desc(ts));
            orders.add(cb.asc(ctx.d.get("id")));
            return orders;
        }
        // standard string column (may live on dos/ds/l/f/b — Ctx joins it on demand;
        // legacy outer query did NOT join dos/ds and errored: documented fix)
        Expression<String> col = resolveString(cb, ctx, sort.column());
        orders.add(nullsLast(cb, col));
        orders.add(cb.asc(cb.selectCase().when(cb.equal(col, ""), 1).otherwise(0)));
        orders.add(cb.asc(col));
        return orders;
    }

    private Order nullsLast(CriteriaBuilder cb, Expression<?> e) {
        return cb.asc(cb.selectCase().when(cb.isNull(e), 1).otherwise(0));
    }
```

- [ ] **Step 3: Run the IT**

```powershell
& .\mvnw.cmd -q "-Dtest=DeviceSearchQueryBuilderIT" test
```
Expected: `Tests run: 37, Failures: 0, Errors: 0`.

- [ ] **Step 4: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/queryrepository/DeviceSearchQueryBuilder.java sclera-cloud-device-asset/src/test/java/io/sclera/it/DeviceSearchQueryBuilderIT.java
git commit -m "feat: sort variants (custom-field, inet ip, timestamp desc, joined-column) with crash fixes"
```

---

### Task 10: Rewire DeviceSearchService, delete dead generators, migration notes

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceSearchService.java`
- Modify: `migration-notes/jpa-conversion-playbook.md` (append section)

- [ ] **Step 1: Wire the builder into the service**

In `DeviceSearchService`, add next to the existing `@Autowired` fields:

```java
    @Autowired
    private io.sclera.queryrepository.DeviceSearchQueryBuilder deviceSearchQueryBuilder;
```

- [ ] **Step 2: Rewrite the three methods**

Replace the body of `multipleKeywordSearchSortFilterDevices` (keep the signature and javadoc; update the javadoc with a `PG-port/Criteria` note listing the documented fixes):

```java
        try {
            DeviceSearchCriteria criteria = DeviceSearchCriteria.from(
                    vdmsid, dockername, condition, search_sort_filter_details, onboard_status);
            Set<String> device_ids = new LinkedHashSet<>(
                    deviceSearchQueryBuilder.findIds(criteria, pageno, pagesize));
            Set<DeviceDTO> searchSortFilteredDevices = deviceService.getDevicesByIdList(vdmsid, device_ids);
            if (search_sort_filter_details.getJSONObject("sort_details") != null) {
                return searchSortFilteredDevices;
            } else {
                if (search_sort_filter_details.getJSONArray("search_details") != null) {
                    com.alibaba.fastjson.JSONArray search_details = search_sort_filter_details.getJSONArray("search_details");
                    Map<String, Object> fuzzySearchDetails = new HashMap<>();
                    fuzzySearchDetails.put("column", search_details.getJSONObject(0).get("column"));
                    fuzzySearchDetails.put("custom", search_details.getJSONObject(0).get("custom"));
                    fuzzySearchDetails.put("value", search_details.getJSONObject(0).get("value"));
                    Set<DeviceDTO> fuzzyScoreUpdatedDevices = this.updateFuzzyMatchScore(searchSortFilteredDevices, fuzzySearchDetails);
                    if (fuzzyScoreUpdatedDevices != null) {
                        List<DeviceDTO> filtered_devices = new ArrayList<DeviceDTO>(fuzzyScoreUpdatedDevices);
                        filtered_devices = this.sortFilteredDevicesByMatchedScore(filtered_devices);
                        return new LinkedHashSet<DeviceDTO>(filtered_devices);
                    }
                } else {
                    return searchSortFilteredDevices;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
```

(import `io.sclera.dto.DeviceSearchCriteria` at the top.)

Replace the body of `multipleKeywordSearchSortFilterDevicesForAssetExport` with the same code minus pagination — the `findIds(...)` line becomes:

```java
            Set<String> device_ids = new LinkedHashSet<>(deviceSearchQueryBuilder.findAllIds(criteria));
```

Replace the body of `multipleKeywordSearchSortFilterDevicesCount` with:

```java
        try {
            DeviceSearchCriteria criteria = DeviceSearchCriteria.from(
                    vdmsid, dockername, condition, search_sort_filter_details, onboard_status);
            return String.valueOf(deviceSearchQueryBuilder.count(criteria));
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
```

- [ ] **Step 3: Delete the dead generators — grep first**

For EACH of these methods, run a grep across `src/` and delete ONLY if the converted methods were the sole callers (expected — but verify):

```powershell
foreach ($m in 'generateMultipleKeywordSearchAndFilterCustomQuery','generateFilterCustomQuery','generateColumnFilterQuery','generateSearchQuery','generateConditionedQueryForAll','generateConditionedQueryForCustomFields','generateConditionedQuery','generateSortQuery','generateDeviceIdsFilterCustomQuery','generateFeatureFilterQuery') { "== $m"; Get-ChildItem -Recurse sclera-cloud-device-asset\src -Filter *.java | Select-String -Pattern $m | Select-Object Path, LineNumber }
```

Do NOT delete `updateDeviceSearchColumnName` (other methods use it). If any generator has another caller, keep it and note that in the commit message.

- [ ] **Step 4: Compile + run the new tests AND the existing service unit tests**

```powershell
$env:JAVA_HOME = 'C:\Users\DhanushVasanth\.jdks\corretto-21.0.8'
& .\mvnw.cmd -q "-Dtest=DeviceSearchQueryBuilderIT,DeviceSearchCriteriaTest,PgFunctionContributorIT,DeviceSearchServiceTest" test
```
Expected: all pass. If `DeviceSearchServiceTest` stubs the deleted generators or jdbcTemplate paths, update those specific tests to stub `deviceSearchQueryBuilder` instead (`@Mock DeviceSearchQueryBuilder` + `when(...findIds/findAllIds/count...)`).

- [ ] **Step 5: Full suite**

```powershell
& .\mvnw.cmd -q test
```
Expected: BUILD SUCCESS, 0 failures/errors (~230+ tests including the 37 new ones).

- [ ] **Step 6: Append migration notes**

Append to `migration-notes/jpa-conversion-playbook.md`:

```markdown
## DeviceSearchService Criteria conversion (2026-06-12)

The multipleKeywordSearchSortFilterDevices family (paged/export/count) moved from string-built
native SQL (jdbcTemplate) to JPA Criteria: DeviceSearchCriteria (parse-once DTO) +
DeviceSearchQueryBuilder (EntityManager Criteria, outer `d.id IN (subquery)` shape) +
ScleraPgFunctionContributor (custom_field_text/custom_field_array_text/strip_specials/
strip_custom_specials/inet_val). Spec: docs/superpowers/specs/2026-06-12-device-search-criteria-conversion-design.md.

Documented behaviour fixes (all bound-parameter SQL now):
- device_ids filter emitted `IN ("id")` — PG identifier quoting, runtime error.
- REGEXP_REPLACE lacked 'g' (PG strips first match only; MySQL strips all) — search values
  with 2+ special characters never matched.
- Sort by created/updated_timestamp emitted `bigint = ''` — PG type error.
- Sort by assignee_email/username/email referenced dos./ds. aliases the outer query never
  joined — SQL error; the builder joins on demand.
- onboardpending/onboardcompleted conditions now work in all three variants (previously
  count-only: count filtered while the page didn't).

NOT bugs (preserved intentionally): the custom-fields search-all injector uses POSITIVE
patterns for does_not_contain/not_equal_to — the outer ±-pattern applies polarity.
The two REGEXP character classes differ (haystack class opens with a space->dot RANGE that
strips quotes; the custom-fields class has no range so quotes survive) — both kept verbatim.

Device gained read-only shadow mappings (insertable=false/updatable=false, no accessors) for
the FK columns docker_vdms_id / docker_name / assigned_user_email.
updateDeviceSearchColumnName stays (other, unconverted methods use it).
```

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: device search/sort/filter family on type-safe Criteria; delete string-SQL generators"
```

---

## Self-review checklist (run after Task 10)

1. Spec coverage: components 1-4 → Tasks 4/5-9/2/10; bug fixes 1-6 → Tasks 5 (binding), 2+8 ('g' flag), 9 (sort crashes), 4 (condition unification); testing section → Tasks 2-9 ITs + Task 4 unit test.
2. `git grep -n "jdbcTemplate" sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceSearchService.java` — the three converted methods must not use it (other methods in the file still may).
3. Full suite green; commit count ≈ 8.
