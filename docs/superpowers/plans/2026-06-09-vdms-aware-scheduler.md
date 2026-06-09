# VDMS-aware Scheduler Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the scheduler register one Dapr job per (job × VDMS) for jobs flagged `PER_VDMS`, discover VDMS via a lifecycle event + startup-sync, fire each on its VDMS-local timezone, and support time-based snooze and one-shot delayed runs at the (job × VDMS) granularity.

**Architecture:** The `job` table stays the catalog/template (gains a `scope` column). A new `vdms_registry` holds the scheduler's own copy of the active-VDMS list (fed by `VdmsLifecycleEvent` + a startup Dapr call to vdms-service). A new `job_instance` table is the per-VDMS runtime unit that pause/resume/snooze/disable/one-shot act on. Every real fire still goes through the Dapr Scheduler control plane via `SchedulerClient`; the snooze re-arm is an in-process Spring `@Scheduled` that re-registers through Dapr when a snooze expires.

**Tech Stack:** Java 21 (Corretto 21.0.8), Spring Boot 4.0.6, Spring Data JPA, PostgreSQL + Flyway, Dapr Jobs API (alpha) + pub/sub, JUnit 5, Mockito, AssertJ, Testcontainers.

---

## Build & test conventions (read once)

This workspace has **no global `mvn`** and **no parent POM**. Each module builds via its own `.\mvnw.cmd` wrapper from inside the module directory, with `JAVA_HOME` set explicitly. `dapr-commons` is a shared library resolved from the **local Maven repo**, so after editing it you MUST re-`install` it before dependent modules see the change.

**Pure-Mockito unit tests** (no `@SpringBootTest`) need no Docker. **`@SpringBootTest` repo/component tests** start a Testcontainers Postgres (Docker Desktop must be running).

PowerShell command templates used throughout this plan:

```powershell
# Set JDK (env does NOT persist across tool calls — set it each time)
$env:JAVA_HOME = "C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"

# Run one test class in a module
cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=PerVdmsRegistrarTest"

# After editing dapr-commons, reinstall it so scheduler/vdms-service/device-asset pick it up
cd dapr-commons; .\mvnw.cmd install -DskipTests
```

Paths in this plan are relative to the repo root:
`C:\Users\DhanushVasanth\Desktop\AssetManagement POD\Microservice123\sclera-cloud-device-asset`

---

## File Structure (decomposition locked here)

**`dapr-commons` (shared contract — reinstall after editing):**
- Modify `dapr-commons/src/main/java/io/sclera/dapr/events/SchedulerTriggerEvent.java` — add nullable `vdmsId`.
- Create `dapr-commons/src/main/java/io/sclera/dapr/events/VdmsLifecycleEvent.java`.

**`sclera-scheduler` (the bulk):**
- Create `.../resources/db/migration/V2__vdms_aware_scheduler.sql`.
- Create domain: `JobScope.java`, `JobInstanceState.java`, `JobInstanceEntity.java`, `JobInstanceId.java`, `JobInstanceRepository.java`, `VdmsRegistryEntity.java`, `VdmsRegistryRepository.java`.
- Modify domain: `JobEntity.java` (+scope), `JobRepository.java` (+findByScope), `JobRunEntity.java` (+vdmsId), `RunRecorder.java` (+vdmsId overload).
- Modify catalog: `JobCatalogProperties.java` (+scope), `JobCatalogReconciler.java` (write scope), `CatalogStartupRunner.java` (startup-sync + reconcileAll).
- Modify client: `JobSchedule.java` (+timezone), `SchedulerClient.java` (CRON_TZ + scheduleOnce).
- Create service: `VdmsDirectoryClient.java`, `VdmsActiveDto.java`, `PerVdmsRegistrar.java`, `SnoozeReconciler.java`.
- Create subscriber: `VdmsLifecycleSubscriber.java`.
- Modify service/web: `JobService.java` (per-instance ops), `JobCallbackController.java` (parse name), `SchedulerApiController.java` (instance endpoints), `web/dto/JobInstanceView.java` (create).
- Modify `resources/application.yml` (+scheduler.max-instances, vdms-app-id, snooze-scan-ms) and `resources/jobs.yaml` (+scope on vdmsSystemHealth).

**`sclera-vdms-service`:**
- Modify `VdmsJpaRepository.java` (+findAllActive), `VdmsController.java` (+GET /vdms/active).

**`sclera-cloud-device-asset` (consumer):**
- Modify `scheduler/TriggerDispatchSubscriber.java` (thread vdmsId), `scheduler/DeviceAssetJobHandlers.java` (`vdmsSystemHealth(String vdmsId)`).

---

## Phase 0 — Shared contract & migration

### Task 1: Add `vdmsId` to `SchedulerTriggerEvent`

**Files:**
- Modify: `dapr-commons/src/main/java/io/sclera/dapr/events/SchedulerTriggerEvent.java`
- Test: `dapr-commons/src/test/java/io/sclera/dapr/events/SchedulerEventsTest.java`

- [ ] **Step 1: Update the failing test** — add a case asserting the new field and that the legacy 3-arg constructor still works (backward compatibility).

In `SchedulerEventsTest.java`, replace the `triggerEventCarriesJobNameAndRunId` test with:

```java
    @Test
    void triggerEventCarriesJobNameRunIdAndVdmsId() {
        SchedulerTriggerEvent e = new SchedulerTriggerEvent("vdmsSystemHealth", "run-1", "vdms-7", 123L);
        assertEquals("vdmsSystemHealth", e.jobName());
        assertEquals("run-1", e.runId());
        assertEquals("vdms-7", e.vdmsId());
        assertEquals(123L, e.firedAtEpochMs());
    }

    @Test
    void legacyThreeArgConstructorLeavesVdmsIdNull() {
        SchedulerTriggerEvent e = new SchedulerTriggerEvent("snmpSync", "run-1", 123L);
        assertNull(e.vdmsId());
        assertEquals(123L, e.firedAtEpochMs());
    }
```

Add the import: `import static org.junit.jupiter.api.Assertions.assertNull;`

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd dapr-commons; .\mvnw.cmd -q test "-Dtest=SchedulerEventsTest"`
Expected: COMPILE FAILURE — `vdmsId()` and the 4-arg constructor do not exist yet.

- [ ] **Step 3: Add the field + backward-compatible constructor**

Replace `SchedulerTriggerEvent.java` with:

```java
package io.sclera.dapr.events;

/**
 * Published when the Dapr Scheduler fires a job. runId is the idempotency key.
 * vdmsId is the VDMS this fire targets; null for GLOBAL-scope jobs.
 */
public record SchedulerTriggerEvent(
    String jobName,
    String runId,
    String vdmsId,
    long firedAtEpochMs) {

    /** Backward-compatible constructor for GLOBAL jobs (no VDMS dimension). */
    public SchedulerTriggerEvent(String jobName, String runId, long firedAtEpochMs) {
        this(jobName, runId, null, firedAtEpochMs);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd dapr-commons; .\mvnw.cmd -q test "-Dtest=SchedulerEventsTest"`
Expected: PASS (2 trigger tests + the unchanged result test).

- [ ] **Step 5: Reinstall dapr-commons and commit**

```powershell
$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd dapr-commons; .\mvnw.cmd install -DskipTests
cd ..
git add dapr-commons/src/main/java/io/sclera/dapr/events/SchedulerTriggerEvent.java dapr-commons/src/test/java/io/sclera/dapr/events/SchedulerEventsTest.java
git commit -m "feat(dapr-commons): add nullable vdmsId to SchedulerTriggerEvent"
```

### Task 2: Add `VdmsLifecycleEvent`

**Files:**
- Create: `dapr-commons/src/main/java/io/sclera/dapr/events/VdmsLifecycleEvent.java`
- Test: `dapr-commons/src/test/java/io/sclera/dapr/events/VdmsLifecycleEventTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.dapr.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VdmsLifecycleEventTest {
    @Test
    void carriesVdmsIdTimezoneAndStatus() {
        VdmsLifecycleEvent e = new VdmsLifecycleEvent("vdms-7", "America/New_York", "ACTIVATED");
        assertEquals("vdms-7", e.vdmsId());
        assertEquals("America/New_York", e.timezone());
        assertEquals("ACTIVATED", e.status());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd dapr-commons; .\mvnw.cmd -q test "-Dtest=VdmsLifecycleEventTest"`
Expected: COMPILE FAILURE — `VdmsLifecycleEvent` does not exist.

- [ ] **Step 3: Create the record**

```java
package io.sclera.dapr.events;

/**
 * Published by the VDMS-owning activation flow when a VDMS becomes active or is
 * deactivated. The scheduler subscribes to register/tear down that VDMS's per-VDMS jobs.
 * status is "ACTIVATED" or "DEACTIVATED". timezone is an IANA zone (may be null → UTC).
 */
public record VdmsLifecycleEvent(String vdmsId, String timezone, String status) {}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd dapr-commons; .\mvnw.cmd -q test "-Dtest=VdmsLifecycleEventTest"`
Expected: PASS.

- [ ] **Step 5: Reinstall dapr-commons and commit**

```powershell
$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd dapr-commons; .\mvnw.cmd install -DskipTests
cd ..
git add dapr-commons/src/main/java/io/sclera/dapr/events/VdmsLifecycleEvent.java dapr-commons/src/test/java/io/sclera/dapr/events/VdmsLifecycleEventTest.java
git commit -m "feat(dapr-commons): add VdmsLifecycleEvent"
```

### Task 3: Flyway migration `V2__vdms_aware_scheduler.sql`

**Files:**
- Create: `sclera-scheduler/src/main/resources/db/migration/V2__vdms_aware_scheduler.sql`
- Modify (test schema, if present): none — Testcontainers runs Flyway against the real migrations.

- [ ] **Step 1: Write the migration**

```sql
-- job becomes catalog/template: add scope (GLOBAL | PER_VDMS)
ALTER TABLE job ADD COLUMN scope TEXT NOT NULL DEFAULT 'GLOBAL';

-- run history attributable per VDMS (null for GLOBAL jobs)
ALTER TABLE job_run ADD COLUMN vdms_id TEXT;

-- scheduler's own copy of the active-VDMS list (no cross-schema reads)
CREATE TABLE vdms_registry (
    vdms_id     TEXT PRIMARY KEY,
    timezone    TEXT,
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- the per-VDMS runtime unit (pause/resume/snooze/disable/one-shot act on this)
CREATE TABLE job_instance (
    job_name      TEXT NOT NULL REFERENCES job(name),
    vdms_id       TEXT NOT NULL REFERENCES vdms_registry(vdms_id),
    state         TEXT NOT NULL DEFAULT 'ENABLED',
    snooze_until  TIMESTAMPTZ,
    dapr_job_name TEXT NOT NULL,
    next_fire_at  TIMESTAMPTZ,
    last_run_id   UUID,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (job_name, vdms_id)
);

CREATE INDEX idx_job_instance_snooze ON job_instance (state, snooze_until);
```

- [ ] **Step 2: Verify the migration applies** (deferred to Task 6's repo test, which boots Flyway). For now, sanity-check syntax by running the existing scheduler test suite which boots a container:

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobRepositoryTest"`
Expected: PASS — the existing repo test boots the container and runs V1 + V2 migrations clean.

- [ ] **Step 3: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/resources/db/migration/V2__vdms_aware_scheduler.sql
git commit -m "feat(scheduler): V2 migration for scope, vdms_registry, job_instance"
```

---

## Phase 1 — Domain & catalog scope

### Task 4: `JobScope` enum + `scope` on `JobEntity` + `findByScope`

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobScope.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobEntity.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobRepository.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobRepositoryTest.java`

- [ ] **Step 1: Add a failing repo test** — append to `JobRepositoryTest`:

```java
    @Test
    void findsJobsByScope() {
        JobEntity perVdms = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        perVdms.setScope(JobScope.PER_VDMS);
        jobs.save(perVdms);
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED)); // defaults to GLOBAL

        assertThat(jobs.findByScope(JobScope.PER_VDMS))
                .extracting(JobEntity::getName).containsExactly("vdmsSystemHealth");
        assertThat(jobs.findByScope(JobScope.GLOBAL))
                .extracting(JobEntity::getName).containsExactly("snmpSync");
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobRepositoryTest"`
Expected: COMPILE FAILURE — `JobScope`, `setScope`, `findByScope` do not exist.

- [ ] **Step 3a: Create `JobScope`**

```java
package io.sclera.scheduler.domain;

public enum JobScope { GLOBAL, PER_VDMS }
```

- [ ] **Step 3b: Add the `scope` column to `JobEntity`** — insert after the `state` field block (after line 26) and add the accessors near the other getters:

Field (after the `state` field):
```java
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false)
    private JobScope scope = JobScope.GLOBAL;
```

Accessors (with the other getters/setters):
```java
    public JobScope getScope() { return scope; }
    public void setScope(JobScope s) { this.scope = s; }
```

- [ ] **Step 3c: Add `findByScope` to `JobRepository`**

```java
package io.sclera.scheduler.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, String> {
    List<JobEntity> findByState(JobState state);
    List<JobEntity> findByScope(JobScope scope);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobRepositoryTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobScope.java sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobEntity.java sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobRepository.java sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobRepositoryTest.java
git commit -m "feat(scheduler): add JobScope and findByScope"
```

### Task 5: Carry `scope` through the catalog

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/JobCatalogProperties.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/JobCatalogReconciler.java`
- Modify: `sclera-scheduler/src/main/resources/jobs.yaml`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/JobCatalogReconcilerTest.java`

- [ ] **Step 1: Add a failing test** — append to `JobCatalogReconcilerTest` (this is a `@SpringBootTest`-style or Mockito test; check the existing file's style and match it). Add:

```java
    @Test
    void reconcileWritesScopeFromCatalog() {
        var entry = new JobCatalogProperties.Entry(
                "vdmsSystemHealth", "0 0 0 * * *", "device-asset", "scheduler.trigger", "PER_VDMS");
        reconciler.reconcile(java.util.List.of(entry));

        JobEntity saved = jobs.findById("vdmsSystemHealth").orElseThrow();
        assertThat(saved.getScope()).isEqualTo(JobScope.PER_VDMS);
    }
```

> If `JobCatalogReconcilerTest` uses Mockito for `jobs`, assert via `verify`/captor instead. Match the existing file's pattern exactly — read it before writing this step.

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobCatalogReconcilerTest"`
Expected: COMPILE FAILURE — `Entry` has no 5th `scope` arg; `getScope` referenced.

- [ ] **Step 3a: Add `scope` to `JobCatalogProperties.Entry`** (default `GLOBAL`, tolerate missing in yaml):

```java
package io.sclera.scheduler.catalog;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "scheduler.catalog")
public record JobCatalogProperties(List<Entry> jobs) {

    public record Entry(String name, String schedule, String owner, String triggerTopic, String scope) {
        public Entry {
            if (triggerTopic == null) triggerTopic = "scheduler.trigger";
            if (scope == null) scope = "GLOBAL";
        }
    }
}
```

- [ ] **Step 3b: Write `scope` in `JobCatalogReconciler.reconcile`** — set it on both the update and insert branches:

```java
package io.sclera.scheduler.catalog;

import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reconciles the declarative catalog into the job table. Never clobbers runtime state. */
@Service
public class JobCatalogReconciler {

    private static final Logger log = LoggerFactory.getLogger(JobCatalogReconciler.class);
    private final JobRepository jobs;

    public JobCatalogReconciler(JobRepository jobs) {
        this.jobs = jobs;
    }

    @Transactional
    public void reconcile(List<JobCatalogProperties.Entry> entries) {
        for (JobCatalogProperties.Entry e : entries) {
            JobScope scope = JobScope.valueOf(e.scope());
            jobs.findById(e.name()).ifPresentOrElse(existing -> {
                existing.setSchedule(e.schedule());
                existing.setOwner(e.owner());
                existing.setTriggerTopic(e.triggerTopic());
                existing.setScope(scope);
                // state, lastRunId, nextFireAt are runtime-owned — left untouched
                log.info("Catalog: updated job name={} schedule={} scope={}", e.name(), e.schedule(), scope);
            }, () -> {
                JobEntity job = new JobEntity(e.name(), e.schedule(), e.owner(),
                        e.triggerTopic(), JobState.ENABLED);
                job.setScope(scope);
                jobs.save(job);
                log.info("Catalog: inserted new job name={} scope={}", e.name(), scope);
            });
        }
    }
}
```

- [ ] **Step 3c: Flag `vdmsSystemHealth` as `PER_VDMS` in `jobs.yaml`** — edit that one entry:

```yaml
      - name: vdmsSystemHealth
        schedule: "0 0 0 * * *"
        owner: device-asset
        scope: PER_VDMS
```

> Only `vdmsSystemHealth` is per-VDMS for v1. Other entries omit `scope` and default to `GLOBAL`.

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobCatalogReconcilerTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/JobCatalogProperties.java sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/JobCatalogReconciler.java sclera-scheduler/src/main/resources/jobs.yaml sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/JobCatalogReconcilerTest.java
git commit -m "feat(scheduler): carry job scope through catalog; mark vdmsSystemHealth PER_VDMS"
```

### Task 6: `vdms_registry` entity + repository

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/VdmsRegistryEntity.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/VdmsRegistryRepository.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/domain/VdmsRegistryRepositoryTest.java`

- [ ] **Step 1: Write the failing repo test**

```java
package io.sclera.scheduler.domain;

import io.sclera.scheduler.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class VdmsRegistryRepositoryTest extends AbstractPostgresTest {

    @Autowired VdmsRegistryRepository registry;

    @Test
    void findsOnlyActiveVdms() {
        registry.save(new VdmsRegistryEntity("vdms-1", "America/New_York", true));
        registry.save(new VdmsRegistryEntity("vdms-2", "Europe/London", false));

        assertThat(registry.findByActiveTrue())
                .extracting(VdmsRegistryEntity::getVdmsId).containsExactly("vdms-1");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=VdmsRegistryRepositoryTest"`
Expected: COMPILE FAILURE — entity/repository absent.

- [ ] **Step 3a: Create `VdmsRegistryEntity`**

```java
package io.sclera.scheduler.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "vdms_registry")
public class VdmsRegistryEntity {

    @Id
    @Column(name = "vdms_id")
    private String vdmsId;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected VdmsRegistryEntity() {}

    public VdmsRegistryEntity(String vdmsId, String timezone, boolean active) {
        this.vdmsId = vdmsId;
        this.timezone = timezone;
        this.active = active;
    }

    @PreUpdate void touch() { this.updatedAt = Instant.now(); }

    public String getVdmsId() { return vdmsId; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String t) { this.timezone = t; }
    public boolean isActive() { return active; }
    public void setActive(boolean a) { this.active = a; }
}
```

- [ ] **Step 3b: Create `VdmsRegistryRepository`**

```java
package io.sclera.scheduler.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VdmsRegistryRepository extends JpaRepository<VdmsRegistryEntity, String> {
    List<VdmsRegistryEntity> findByActiveTrue();
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=VdmsRegistryRepositoryTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/domain/VdmsRegistryEntity.java sclera-scheduler/src/main/java/io/sclera/scheduler/domain/VdmsRegistryRepository.java sclera-scheduler/src/test/java/io/sclera/scheduler/domain/VdmsRegistryRepositoryTest.java
git commit -m "feat(scheduler): add vdms_registry entity + repository"
```

### Task 7: `job_instance` entity + repository (+ `JobInstanceState`, `JobInstanceId`)

**Files:**
- Create: `JobInstanceState.java`, `JobInstanceId.java`, `JobInstanceEntity.java`, `JobInstanceRepository.java` (all in `.../scheduler/domain/`)
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobInstanceRepositoryTest.java`

- [ ] **Step 1: Write the failing repo test**

```java
package io.sclera.scheduler.domain;

import io.sclera.scheduler.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class JobInstanceRepositoryTest extends AbstractPostgresTest {

    @Autowired JobRepository jobs;
    @Autowired VdmsRegistryRepository registry;
    @Autowired JobInstanceRepository instances;

    private void seed() {
        JobEntity j = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        j.setScope(JobScope.PER_VDMS);
        jobs.save(j);
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
    }

    @Test
    void savesByCompositeKeyAndFindsByJobName() {
        seed();
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1",
                "vdmsSystemHealth::vdms-1"));

        assertThat(instances.findByJobName("vdmsSystemHealth"))
                .extracting(JobInstanceEntity::getVdmsId).containsExactly("vdms-1");
        assertThat(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1"))).isPresent();
    }

    @Test
    void findsSnoozedDueForRearm() {
        seed();
        JobInstanceEntity inst = new JobInstanceEntity("vdmsSystemHealth", "vdms-1",
                "vdmsSystemHealth::vdms-1");
        inst.setState(JobInstanceState.SNOOZED);
        inst.setSnoozeUntil(Instant.parse("2020-01-01T00:00:00Z")); // in the past → due
        instances.save(inst);

        assertThat(instances.findByStateAndSnoozeUntilLessThanEqual(
                JobInstanceState.SNOOZED, Instant.now()))
                .hasSize(1);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobInstanceRepositoryTest"`
Expected: COMPILE FAILURE — instance types absent.

- [ ] **Step 3a: Create `JobInstanceState`**

```java
package io.sclera.scheduler.domain;

public enum JobInstanceState { ENABLED, PAUSED, SNOOZED, DISABLED }
```

- [ ] **Step 3b: Create `JobInstanceId`** (composite-key class for `@IdClass`)

```java
package io.sclera.scheduler.domain;

import java.io.Serializable;
import java.util.Objects;

/** Composite primary key for JobInstanceEntity: (jobName, vdmsId). */
public class JobInstanceId implements Serializable {

    private String jobName;
    private String vdmsId;

    public JobInstanceId() {}

    public JobInstanceId(String jobName, String vdmsId) {
        this.jobName = jobName;
        this.vdmsId = vdmsId;
    }

    public String getJobName() { return jobName; }
    public String getVdmsId() { return vdmsId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JobInstanceId that)) return false;
        return Objects.equals(jobName, that.jobName) && Objects.equals(vdmsId, that.vdmsId);
    }

    @Override
    public int hashCode() { return Objects.hash(jobName, vdmsId); }
}
```

- [ ] **Step 3c: Create `JobInstanceEntity`**

```java
package io.sclera.scheduler.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_instance")
@IdClass(JobInstanceId.class)
public class JobInstanceEntity {

    @Id
    @Column(name = "job_name")
    private String jobName;

    @Id
    @Column(name = "vdms_id")
    private String vdmsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private JobInstanceState state = JobInstanceState.ENABLED;

    @Column(name = "snooze_until")
    private Instant snoozeUntil;

    @Column(name = "dapr_job_name", nullable = false)
    private String daprJobName;

    @Column(name = "next_fire_at")
    private Instant nextFireAt;

    @Column(name = "last_run_id")
    private UUID lastRunId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected JobInstanceEntity() {}

    public JobInstanceEntity(String jobName, String vdmsId, String daprJobName) {
        this.jobName = jobName;
        this.vdmsId = vdmsId;
        this.daprJobName = daprJobName;
    }

    @PreUpdate void touch() { this.updatedAt = Instant.now(); }

    public String getJobName() { return jobName; }
    public String getVdmsId() { return vdmsId; }
    public JobInstanceState getState() { return state; }
    public void setState(JobInstanceState s) { this.state = s; }
    public Instant getSnoozeUntil() { return snoozeUntil; }
    public void setSnoozeUntil(Instant t) { this.snoozeUntil = t; }
    public String getDaprJobName() { return daprJobName; }
    public void setDaprJobName(String n) { this.daprJobName = n; }
    public Instant getNextFireAt() { return nextFireAt; }
    public void setNextFireAt(Instant t) { this.nextFireAt = t; }
    public UUID getLastRunId() { return lastRunId; }
    public void setLastRunId(UUID id) { this.lastRunId = id; }
}
```

- [ ] **Step 3d: Create `JobInstanceRepository`**

```java
package io.sclera.scheduler.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface JobInstanceRepository
        extends JpaRepository<JobInstanceEntity, JobInstanceId> {

    List<JobInstanceEntity> findByJobName(String jobName);
    List<JobInstanceEntity> findByVdmsId(String vdmsId);
    List<JobInstanceEntity> findByStateAndSnoozeUntilLessThanEqual(
            JobInstanceState state, Instant cutoff);
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobInstanceRepositoryTest"`
Expected: PASS (both tests).

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceState.java sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceId.java sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceEntity.java sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceRepository.java sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobInstanceRepositoryTest.java
git commit -m "feat(scheduler): add job_instance entity, composite key, repository"
```

### Task 8: `vdms_id` on `JobRunEntity` + `RunRecorder` overload

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobRunEntity.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/RunRecorder.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/RunRecorderTest.java`

- [ ] **Step 1: Add a failing test** — append to `RunRecorderTest` (match its existing Mockito style; it mocks `JobRepository jobs` and `JobRunRepository runs`):

```java
    @Test
    void recordFiredWithVdmsIdPersistsIt() {
        java.util.UUID runId = java.util.UUID.randomUUID();
        new RunRecorder(jobs, runs).recordFired("vdmsSystemHealth", runId, false, "vdms-9");

        org.mockito.ArgumentCaptor<JobRunEntity> cap =
                org.mockito.ArgumentCaptor.forClass(JobRunEntity.class);
        org.mockito.Mockito.verify(runs).save(cap.capture());
        org.assertj.core.api.Assertions.assertThat(cap.getValue().getVdmsId()).isEqualTo("vdms-9");
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=RunRecorderTest"`
Expected: COMPILE FAILURE — 4-arg `recordFired` and `getVdmsId` absent.

- [ ] **Step 3a: Add `vdmsId` to `JobRunEntity`** — add the field (after `error`), a 6-arg constructor, and the accessor; keep the existing 5-arg constructor delegating null:

Field:
```java
    @Column(name = "vdms_id")
    private String vdmsId;
```

Replace the existing constructor with both:
```java
    public JobRunEntity(UUID runId, String jobName, RunStatus status,
                        boolean manual, Instant firedAt) {
        this(runId, jobName, status, manual, firedAt, null);
    }

    public JobRunEntity(UUID runId, String jobName, RunStatus status,
                        boolean manual, Instant firedAt, String vdmsId) {
        this.runId = runId;
        this.jobName = jobName;
        this.status = status;
        this.manual = manual;
        this.firedAt = firedAt;
        this.vdmsId = vdmsId;
    }
```

Accessor:
```java
    public String getVdmsId() { return vdmsId; }
```

- [ ] **Step 3b: Add the `recordFired` overload to `RunRecorder`** — keep the old 3-arg delegating null, add the 4-arg that persists vdmsId:

```java
    @Transactional
    public void recordFired(String jobName, UUID runId, boolean manual) {
        recordFired(jobName, runId, manual, null);
    }

    @Transactional
    public void recordFired(String jobName, UUID runId, boolean manual, String vdmsId) {
        runs.save(new JobRunEntity(runId, jobName, RunStatus.FIRED, manual, Instant.now(), vdmsId));
        jobs.findById(jobName).ifPresent(j -> j.setLastRunId(runId));
        log.info("Run fired job={} runId={} manual={} vdmsId={}", jobName, runId, manual, vdmsId);
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=RunRecorderTest"`
Expected: PASS (existing + new test).

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobRunEntity.java sclera-scheduler/src/main/java/io/sclera/scheduler/service/RunRecorder.java sclera-scheduler/src/test/java/io/sclera/scheduler/service/RunRecorderTest.java
git commit -m "feat(scheduler): attribute job runs to vdms_id"
```

---

## Phase 2 — Client: timezone + one-shot

### Task 9: `JobSchedule` timezone + `SchedulerClient` CRON_TZ + `scheduleOnce`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/client/JobSchedule.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/client/SchedulerClient.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/client/SchedulerClientTest.java`

- [ ] **Step 1: Add failing tests** — append to `SchedulerClientTest`:

```java
    @Test
    void scheduleWithTimezonePrefixesCronTz() {
        client().schedule(new JobSchedule("vdmsSystemHealth::vdms-1", "0 0 0 * * *", "America/New_York"));

        assertThat(requests).containsExactly("POST /v1.0-alpha1/jobs/vdmsSystemHealth::vdms-1");
        assertThat(bodies.get(0)).contains("\"schedule\":\"CRON_TZ=America/New_York 0 0 0 * * *\"");
    }

    @Test
    void scheduleWithTimezoneLeavesEveryScheduleUnchanged() {
        client().schedule(new JobSchedule("offlineDeviceCheck::vdms-1", "@every 90s", "America/New_York"));
        assertThat(bodies.get(0)).contains("\"schedule\":\"@every 90s\"");
    }

    @Test
    void scheduleOncePostsDueTimeAndSingleRepeat() {
        client().scheduleOnce("vdmsSystemHealth::vdms-1::once-abc",
                java.time.Instant.parse("2026-06-09T15:00:00Z"));

        assertThat(requests).containsExactly("POST /v1.0-alpha1/jobs/vdmsSystemHealth::vdms-1::once-abc");
        assertThat(bodies.get(0)).contains("\"dueTime\":\"2026-06-09T15:00:00Z\"");
        assertThat(bodies.get(0)).contains("\"repeats\":1");
    }
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=SchedulerClientTest"`
Expected: COMPILE FAILURE — 3-arg `JobSchedule` and `scheduleOnce` absent.

- [ ] **Step 3a: Extend `JobSchedule`** with timezone (canonical 3-arg + 2-arg convenience so existing callers/tests keep compiling):

```java
package io.sclera.scheduler.client;

/**
 * A job registration request. schedule is a cron ("0 0 */3 * * *") or "@every 90s".
 * timezone is an IANA zone applied to cron schedules (null → UTC; ignored for "@every").
 */
public record JobSchedule(String name, String schedule, String timezone) {
    public JobSchedule(String name, String schedule) {
        this(name, schedule, null);
    }
}
```

- [ ] **Step 3b: Build CRON_TZ + add `scheduleOnce` in `SchedulerClient`** — replace the `schedule` method and add `scheduleOnce` and a private helper. Add `import java.time.Instant;`:

```java
    /** Register or replace a job. Idempotent: re-posting the same name replaces it. */
    public void schedule(JobSchedule job) {
        Map<String, Object> body = Map.of(
            "schedule", effectiveSchedule(job.schedule(), job.timezone()),
            "data", Map.of("jobName", job.name())
        );
        http.post()
            .uri("/v1.0-alpha1/jobs/{name}", job.name())
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();
        log.info("Scheduled job name={} schedule={} tz={}", job.name(), job.schedule(), job.timezone());
    }

    /** Register a one-shot job that fires once at dueTime, then Dapr auto-removes it. */
    public void scheduleOnce(String name, Instant dueTime) {
        Map<String, Object> body = Map.of(
            "dueTime", dueTime.toString(),
            "repeats", 1,
            "data", Map.of("jobName", name)
        );
        http.post()
            .uri("/v1.0-alpha1/jobs/{name}", name)
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();
        log.info("Scheduled one-shot job name={} dueTime={}", name, dueTime);
    }

    // CRON_TZ applies only to cron expressions; "@every <dur>" interval schedules ignore tz.
    private static String effectiveSchedule(String schedule, String timezone) {
        if (timezone == null || timezone.isBlank() || schedule.startsWith("@")) {
            return schedule;
        }
        return "CRON_TZ=" + timezone + " " + schedule;
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=SchedulerClientTest"`
Expected: PASS (all old + 3 new tests).

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/client/JobSchedule.java sclera-scheduler/src/main/java/io/sclera/scheduler/client/SchedulerClient.java sclera-scheduler/src/test/java/io/sclera/scheduler/client/SchedulerClientTest.java
git commit -m "feat(scheduler): timezone-aware (CRON_TZ) scheduling + one-shot scheduleOnce"
```

---

## Phase 3 — Registrar & discovery

### Task 10: `VdmsDirectoryClient` (startup-sync Dapr call to vdms-service)

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/VdmsActiveDto.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/VdmsDirectoryClient.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/VdmsDirectoryClientTest.java`

- [ ] **Step 1: Write the failing test** (uses the same `HttpServer` stub style as `SchedulerClientTest`):

```java
package io.sclera.scheduler.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VdmsDirectoryClientTest {

    static HttpServer server;
    static int port;

    @BeforeAll
    static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", ex -> {
            String json = "[{\"vdmsId\":\"vdms-1\",\"timezone\":\"UTC\"},"
                        + "{\"vdmsId\":\"vdms-2\",\"timezone\":\"America/New_York\"}]";
            byte[] out = json.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, out.length);
            ex.getResponseBody().write(out);
            ex.close();
        });
        server.start();
    }

    @AfterAll
    static void stop() { server.stop(0); }

    @Test
    void fetchesActiveVdmsList() {
        VdmsDirectoryClient client =
                new VdmsDirectoryClient("http://localhost:" + port, "sclera-vdms-service");

        List<VdmsActiveDto> active = client.fetchActiveVdms();

        assertThat(active).extracting(VdmsActiveDto::vdmsId).containsExactly("vdms-1", "vdms-2");
        assertThat(active).extracting(VdmsActiveDto::timezone).containsExactly("UTC", "America/New_York");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=VdmsDirectoryClientTest"`
Expected: COMPILE FAILURE — client/dto absent.

- [ ] **Step 3a: Create `VdmsActiveDto`**

```java
package io.sclera.scheduler.service;

/** One active VDMS as returned by vdms-service GET /vdms/active. */
public record VdmsActiveDto(String vdmsId, String timezone) {}
```

- [ ] **Step 3b: Create `VdmsDirectoryClient`** (Dapr service-invocation via the local sidecar; mirrors `SchedulerClient`'s two-constructor prod/test pattern):

```java
package io.sclera.scheduler.service;

import io.sclera.scheduler.config.DaprProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Reads the active-VDMS list from vdms-service via the local Dapr sidecar
 * (GET /v1.0/invoke/{appId}/method/vdms/active). Used only by startup-sync — the
 * scheduler never reads the vdms table directly (DB-per-service).
 */
@Component
public class VdmsDirectoryClient {

    private static final Logger log = LoggerFactory.getLogger(VdmsDirectoryClient.class);
    private final RestClient http;
    private final String vdmsAppId;

    @Autowired
    public VdmsDirectoryClient(DaprProperties props,
                               @Value("${scheduler.vdms-app-id}") String vdmsAppId) {
        this("http://localhost:" + props.httpPort(), vdmsAppId);
    }

    // Test constructor — explicit base URL.
    public VdmsDirectoryClient(String daprBaseUrl, String vdmsAppId) {
        this.http = RestClient.builder().baseUrl(daprBaseUrl).build();
        this.vdmsAppId = vdmsAppId;
    }

    public List<VdmsActiveDto> fetchActiveVdms() {
        List<VdmsActiveDto> result = http.get()
            .uri("/v1.0/invoke/{appId}/method/vdms/active", vdmsAppId)
            .retrieve()
            .body(new ParameterizedTypeReference<List<VdmsActiveDto>>() {});
        int size = result == null ? 0 : result.size();
        log.info("Fetched {} active VDMS from {}", size, vdmsAppId);
        return result == null ? List.of() : result;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=VdmsDirectoryClientTest"`
Expected: PASS.

- [ ] **Step 5: Add config + commit** — add to `application.yml` under `scheduler:`:

```yaml
  vdms-app-id: sclera-vdms-service
  max-instances: 5000
  snooze-scan-ms: 60000
```

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/service/VdmsActiveDto.java sclera-scheduler/src/main/java/io/sclera/scheduler/service/VdmsDirectoryClient.java sclera-scheduler/src/test/java/io/sclera/scheduler/service/VdmsDirectoryClientTest.java sclera-scheduler/src/main/resources/application.yml
git commit -m "feat(scheduler): VdmsDirectoryClient for active-VDMS startup sync + config"
```

### Task 11: `PerVdmsRegistrar` (fan-out, activate, deactivate, guardrail)

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/PerVdmsRegistrar.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/PerVdmsRegistrarTest.java`

- [ ] **Step 1: Write the failing unit test** (pure Mockito):

```java
package io.sclera.scheduler.service;

import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerVdmsRegistrarTest {

    @Mock JobRepository jobs;
    @Mock VdmsRegistryRepository registry;
    @Mock JobInstanceRepository instances;
    @Mock SchedulerClient scheduler;

    PerVdmsRegistrar registrar() {
        PerVdmsRegistrar r = new PerVdmsRegistrar(jobs, registry, instances, scheduler);
        r.setMaxInstances(5000);
        return r;
    }

    private JobEntity perVdmsJob() {
        JobEntity j = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        j.setScope(JobScope.PER_VDMS);
        return j;
    }

    @Test
    void reconcileAllRegistersEachPerVdmsJobForEachActiveVdms() {
        when(jobs.findByScope(JobScope.PER_VDMS)).thenReturn(List.of(perVdmsJob()));
        when(registry.findByActiveTrue()).thenReturn(List.of(
                new VdmsRegistryEntity("vdms-1", "UTC", true),
                new VdmsRegistryEntity("vdms-2", "America/New_York", true)));
        when(instances.findById(any())).thenReturn(Optional.empty());

        registrar().reconcileAll();

        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-1", "0 0 0 * * *", "UTC"));
        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-2", "0 0 0 * * *", "America/New_York"));
        verify(instances, times(2)).save(any(JobInstanceEntity.class));
    }

    @Test
    void onVdmsActivatedUpsertsRegistryAndRegistersAllPerVdmsJobs() {
        when(registry.findById("vdms-3")).thenReturn(Optional.empty());
        when(jobs.findByScope(JobScope.PER_VDMS)).thenReturn(List.of(perVdmsJob()));
        when(instances.findById(any())).thenReturn(Optional.empty());

        registrar().onVdmsActivated("vdms-3", "Europe/London");

        verify(registry).save(argThat(v -> v.getVdmsId().equals("vdms-3") && v.isActive()));
        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-3", "0 0 0 * * *", "Europe/London"));
    }

    @Test
    void onVdmsDeactivatedDeletesDaprJobsAndDisablesInstances() {
        JobInstanceEntity inst = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        when(instances.findByVdmsId("vdms-1")).thenReturn(List.of(inst));
        when(registry.findById("vdms-1")).thenReturn(
                Optional.of(new VdmsRegistryEntity("vdms-1", "UTC", true)));

        registrar().onVdmsDeactivated("vdms-1");

        verify(scheduler).delete("vdmsSystemHealth::vdms-1");
        org.assertj.core.api.Assertions.assertThat(inst.getState()).isEqualTo(JobInstanceState.DISABLED);
        verify(registry).save(argThat(v -> !v.isActive()));
    }

    @Test
    void reconcileAllStopsAtMaxInstancesGuardrail() {
        PerVdmsRegistrar r = new PerVdmsRegistrar(jobs, registry, instances, scheduler);
        r.setMaxInstances(1);
        when(jobs.findByScope(JobScope.PER_VDMS)).thenReturn(List.of(perVdmsJob()));
        when(registry.findByActiveTrue()).thenReturn(List.of(
                new VdmsRegistryEntity("vdms-1", "UTC", true),
                new VdmsRegistryEntity("vdms-2", "UTC", true)));
        when(instances.findById(any())).thenReturn(Optional.empty());

        r.reconcileAll();

        // Only the first instance registered; guardrail skipped the rest.
        verify(scheduler, times(1)).schedule(any(JobSchedule.class));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=PerVdmsRegistrarTest"`
Expected: COMPILE FAILURE — `PerVdmsRegistrar` absent.

- [ ] **Step 3: Implement `PerVdmsRegistrar`**

```java
package io.sclera.scheduler.service;

import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Owns per-VDMS job registration. For each PER_VDMS job × each active VDMS it ensures a
 * job_instance row and registers a timezone-aware Dapr job named "{jobName}::{vdmsId}".
 * All operations are idempotent. Per-registration failures are isolated (one bad VDMS must
 * not abort the batch), mirroring JobService.registerAll().
 */
@Service
public class PerVdmsRegistrar {

    private static final Logger log = LoggerFactory.getLogger(PerVdmsRegistrar.class);

    private final JobRepository jobs;
    private final VdmsRegistryRepository registry;
    private final JobInstanceRepository instances;
    private final SchedulerClient scheduler;

    @Value("${scheduler.max-instances}") private int maxInstances;

    public PerVdmsRegistrar(JobRepository jobs, VdmsRegistryRepository registry,
                            JobInstanceRepository instances, SchedulerClient scheduler) {
        this.jobs = jobs;
        this.registry = registry;
        this.instances = instances;
        this.scheduler = scheduler;
    }

    void setMaxInstances(int v) { this.maxInstances = v; }

    static String daprName(String jobName, String vdmsId) {
        return jobName + "::" + vdmsId;
    }

    /** Reconcile every PER_VDMS job against every active VDMS. Startup + self-heal path. */
    @Transactional
    public void reconcileAll() {
        List<JobEntity> perVdmsJobs = jobs.findByScope(JobScope.PER_VDMS);
        List<VdmsRegistryEntity> active = registry.findByActiveTrue();
        int registered = 0;
        for (JobEntity job : perVdmsJobs) {
            for (VdmsRegistryEntity vdms : active) {
                if (registered >= maxInstances) {
                    log.warn("max-instances={} reached — skipping remaining per-VDMS registrations", maxInstances);
                    return;
                }
                if (registerInstance(job, vdms.getVdmsId(), vdms.getTimezone())) registered++;
            }
        }
        log.info("Per-VDMS reconcile complete: registered={} (jobs={} activeVdms={})",
                registered, perVdmsJobs.size(), active.size());
    }

    /** A VDMS became active: upsert the registry row, then register all PER_VDMS jobs for it. */
    @Transactional
    public void onVdmsActivated(String vdmsId, String timezone) {
        VdmsRegistryEntity v = registry.findById(vdmsId).orElseGet(
                () -> new VdmsRegistryEntity(vdmsId, timezone, true));
        v.setTimezone(timezone);
        v.setActive(true);
        registry.save(v);
        for (JobEntity job : jobs.findByScope(JobScope.PER_VDMS)) {
            registerInstance(job, vdmsId, timezone);
        }
        log.info("Activated VDMS {} — per-VDMS jobs registered", vdmsId);
    }

    /** A VDMS was deactivated: delete its Dapr jobs, DISABLE its instances, flag registry. */
    @Transactional
    public void onVdmsDeactivated(String vdmsId) {
        for (JobInstanceEntity inst : instances.findByVdmsId(vdmsId)) {
            try {
                scheduler.delete(inst.getDaprJobName());
            } catch (Exception e) {
                log.error("Failed to delete Dapr job {} on deactivate: {}", inst.getDaprJobName(), e.getMessage());
            }
            inst.setState(JobInstanceState.DISABLED);
        }
        registry.findById(vdmsId).ifPresent(v -> { v.setActive(false); registry.save(v); });
        log.info("Deactivated VDMS {} — per-VDMS jobs torn down", vdmsId);
    }

    // Idempotent: upsert the instance row, then (re-)register with Dapr. Isolated per call.
    private boolean registerInstance(JobEntity job, String vdmsId, String timezone) {
        String daprName = daprName(job.getName(), vdmsId);
        try {
            instances.findById(new JobInstanceId(job.getName(), vdmsId)).orElseGet(
                    () -> instances.save(new JobInstanceEntity(job.getName(), vdmsId, daprName)));
            scheduler.schedule(new JobSchedule(daprName, job.getSchedule(), timezone));
            return true;
        } catch (Exception e) {
            log.error("Failed to register per-VDMS job {} vdms={} error={}",
                    job.getName(), vdmsId, e.getMessage());
            return false;
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=PerVdmsRegistrarTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/service/PerVdmsRegistrar.java sclera-scheduler/src/test/java/io/sclera/scheduler/service/PerVdmsRegistrarTest.java
git commit -m "feat(scheduler): PerVdmsRegistrar fan-out, activate/deactivate, max-instances guardrail"
```

### Task 12: `VdmsLifecycleSubscriber`

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/subscriber/VdmsLifecycleSubscriber.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/subscriber/VdmsLifecycleSubscriberTest.java`

- [ ] **Step 1: Write the failing unit test**

```java
package io.sclera.scheduler.subscriber;

import io.dapr.client.DaprClient;
import io.sclera.dapr.events.VdmsLifecycleEvent;
import io.sclera.scheduler.service.PerVdmsRegistrar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VdmsLifecycleSubscriberTest {

    @Mock DaprClient dapr;
    @Mock PerVdmsRegistrar registrar;

    VdmsLifecycleSubscriber subscriber() {
        return new VdmsLifecycleSubscriber(dapr, registrar);
    }

    @Test
    void activatedEventRegistersVdms() {
        subscriber().handleEvent(new VdmsLifecycleEvent("vdms-1", "UTC", "ACTIVATED"));
        verify(registrar).onVdmsActivated("vdms-1", "UTC");
    }

    @Test
    void deactivatedEventTearsDownVdms() {
        subscriber().handleEvent(new VdmsLifecycleEvent("vdms-1", null, "DEACTIVATED"));
        verify(registrar).onVdmsDeactivated("vdms-1");
    }

    @Test
    void unknownStatusThrows() {
        assertThatThrownBy(() ->
                subscriber().handleEvent(new VdmsLifecycleEvent("vdms-1", "UTC", "WAT")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingVdmsIdThrows() {
        assertThatThrownBy(() ->
                subscriber().handleEvent(new VdmsLifecycleEvent(null, "UTC", "ACTIVATED")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=VdmsLifecycleSubscriberTest"`
Expected: COMPILE FAILURE — subscriber absent.

- [ ] **Step 3: Implement `VdmsLifecycleSubscriber`** (extends the shared `DaprEventSubscriber`, mirrors `VdmsCustomerOrgSubscriber`; `IllegalArgumentException` is treated as a permanent error by the base class → DLQ, not infinite retry):

```java
package io.sclera.scheduler.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.VdmsLifecycleEvent;
import io.sclera.scheduler.service.PerVdmsRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Registers/tears down a VDMS's per-VDMS jobs when the VDMS-owner publishes a lifecycle event. */
@RestController
public class VdmsLifecycleSubscriber extends DaprEventSubscriber<VdmsLifecycleEvent> {

    private static final Logger log = LoggerFactory.getLogger(VdmsLifecycleSubscriber.class);
    private final PerVdmsRegistrar registrar;

    public VdmsLifecycleSubscriber(DaprClient dapr, PerVdmsRegistrar registrar) {
        super(dapr, "vdms.lifecycle");
        this.registrar = registrar;
    }

    @Topic(name = "vdms.lifecycle", pubsubName = "pubsub",
           deadLetterTopic = "vdms.lifecycle.dlq")
    @PostMapping("/internal/vdms-lifecycle")
    public ResponseEntity<Map<String, String>> onLifecycle(
            @RequestBody CloudEvent<VdmsLifecycleEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(VdmsLifecycleEvent data) {
        if (data.vdmsId() == null || data.vdmsId().isBlank()) {
            throw new IllegalArgumentException("vdmsId is required");
        }
        switch (data.status()) {
            case "ACTIVATED" -> registrar.onVdmsActivated(data.vdmsId(), data.timezone());
            case "DEACTIVATED" -> registrar.onVdmsDeactivated(data.vdmsId());
            default -> throw new IllegalArgumentException("Unknown VDMS lifecycle status: " + data.status());
        }
        log.info("Handled VDMS lifecycle vdmsId={} status={}", data.vdmsId(), data.status());
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=VdmsLifecycleSubscriberTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/subscriber/VdmsLifecycleSubscriber.java sclera-scheduler/src/test/java/io/sclera/scheduler/subscriber/VdmsLifecycleSubscriberTest.java
git commit -m "feat(scheduler): VdmsLifecycleSubscriber drives per-VDMS register/teardown"
```

### Task 13: Wire startup-sync + per-VDMS reconcile into `CatalogStartupRunner`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/CatalogStartupRunner.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/CatalogStartupRunnerTest.java`

- [ ] **Step 1: Update the test** — `CatalogStartupRunnerTest` currently verifies reconcile + registerAll order. Add the new collaborators and assert startup-sync runs (fetch active VDMS → upsert registry → registrar.reconcileAll). Replace the test body to construct the runner with the new deps and verify:

```java
    @Test
    void runReconcilesThenRegistersGlobalThenSyncsVdmsThenReconcilesPerVdms() {
        when(vdmsDirectory.fetchActiveVdms()).thenReturn(java.util.List.of(
                new io.sclera.scheduler.service.VdmsActiveDto("vdms-1", "UTC")));

        runner.run(null);

        org.mockito.InOrder order = inOrder(reconciler, jobService, vdmsDirectory, registry, registrar);
        order.verify(reconciler).reconcile(any());
        order.verify(jobService).registerAll();
        order.verify(vdmsDirectory).fetchActiveVdms();
        order.verify(registry).save(argThat(v -> v.getVdmsId().equals("vdms-1") && v.isActive()));
        order.verify(registrar).reconcileAll();
    }
```

Add `@Mock` fields for `VdmsDirectoryClient vdmsDirectory`, `VdmsRegistryRepository registry`, `PerVdmsRegistrar registrar`, and build `runner` with all deps (see Step 3 constructor). Match the existing file's mock-setup style.

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=CatalogStartupRunnerTest"`
Expected: COMPILE FAILURE — runner constructor has the old 3-arg shape.

- [ ] **Step 3: Implement the wider runner**

```java
package io.sclera.scheduler.catalog;

import io.sclera.scheduler.domain.VdmsRegistryEntity;
import io.sclera.scheduler.domain.VdmsRegistryRepository;
import io.sclera.scheduler.service.JobService;
import io.sclera.scheduler.service.PerVdmsRegistrar;
import io.sclera.scheduler.service.VdmsActiveDto;
import io.sclera.scheduler.service.VdmsDirectoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * On boot: reconcile the catalog, register GLOBAL jobs, then sync the active-VDMS list from
 * vdms-service into vdms_registry and reconcile per-VDMS jobs. The VDMS sync is the self-heal
 * backstop for any lifecycle event missed while the scheduler was down. A vdms-service that is
 * unreachable at boot must not crash the scheduler — sync failures are logged, not fatal
 * (the next lifecycle event or restart reconciles).
 */
@Component
public class CatalogStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogStartupRunner.class);

    private final JobCatalogProperties catalog;
    private final JobCatalogReconciler reconciler;
    private final JobService jobService;
    private final VdmsDirectoryClient vdmsDirectory;
    private final VdmsRegistryRepository registry;
    private final PerVdmsRegistrar registrar;

    public CatalogStartupRunner(JobCatalogProperties catalog,
                                JobCatalogReconciler reconciler,
                                JobService jobService,
                                VdmsDirectoryClient vdmsDirectory,
                                VdmsRegistryRepository registry,
                                PerVdmsRegistrar registrar) {
        this.catalog = catalog;
        this.reconciler = reconciler;
        this.jobService = jobService;
        this.vdmsDirectory = vdmsDirectory;
        this.registry = registry;
        this.registrar = registrar;
    }

    @Override
    public void run(ApplicationArguments args) {
        reconciler.reconcile(catalog.jobs());
        jobService.registerAll();
        try {
            for (VdmsActiveDto v : vdmsDirectory.fetchActiveVdms()) {
                VdmsRegistryEntity row = registry.findById(v.vdmsId())
                        .orElseGet(() -> new VdmsRegistryEntity(v.vdmsId(), v.timezone(), true));
                row.setTimezone(v.timezone());
                row.setActive(true);
                registry.save(row);
            }
            registrar.reconcileAll();
        } catch (Exception e) {
            log.error("VDMS startup-sync failed (will reconcile on next event/restart): {}", e.getMessage());
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=CatalogStartupRunnerTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/CatalogStartupRunner.java sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/CatalogStartupRunnerTest.java
git commit -m "feat(scheduler): startup-sync active VDMS + per-VDMS reconcile on boot"
```

---

## Phase 4 — Firing path & delayed jobs

### Task 14: `JobCallbackController` parses `{jobName}::{vdmsId}`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/JobCallbackController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/JobCallbackControllerTest.java`

- [ ] **Step 1: Add failing tests** — append to `JobCallbackControllerTest` (matches its existing Mockito style with `recorder` + `publisher` mocks):

```java
    @Test
    void perVdmsFireRecordsAndPublishesVdmsId() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e", null));

        controller().onJobFired("vdmsSystemHealth::vdms-7");

        verify(recorder).recordFired(eq("vdmsSystemHealth"), any(), eq(false), eq("vdms-7"));
        verify(publisher).publish(eq("pubsub"), eq("scheduler.trigger"), argThat(p ->
                p instanceof SchedulerTriggerEvent e
                        && e.jobName().equals("vdmsSystemHealth")
                        && "vdms-7".equals(e.vdmsId())));
    }

    @Test
    void oneShotFireIsRecordedAsManual() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e", null));

        controller().onJobFired("vdmsSystemHealth::vdms-7::once-abc");

        verify(recorder).recordFired(eq("vdmsSystemHealth"), any(), eq(true), eq("vdms-7"));
    }

    @Test
    void globalFireHasNullVdmsId() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e", null));

        controller().onJobFired("snmpSync");

        verify(recorder).recordFired(eq("snmpSync"), any(), eq(false), isNull());
    }
```

Add imports as needed (`SchedulerTriggerEvent`, `static org.mockito.Mockito.*`).

- [ ] **Step 2: Run tests to verify they fail**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobCallbackControllerTest"`
Expected: FAIL — current code records with the 3-arg `recordFired` and a null vdmsId in the event.

- [ ] **Step 3: Parse the fired name in `JobCallbackController.onJobFired`** — replace the method body:

```java
    @PostMapping("/job/{name}")
    public ResponseEntity<Void> onJobFired(@PathVariable("name") String name) {
        // Dapr job names: "{jobName}" (global), "{jobName}::{vdmsId}" (per-VDMS),
        // or "{jobName}::{vdmsId}::once-{id}" (one-shot delayed run).
        String[] parts = name.split("::");
        String jobName = parts[0];
        String vdmsId = parts.length >= 2 ? parts[1] : null;
        boolean oneShot = parts.length >= 3 && parts[2].startsWith("once-");

        UUID runId = UUID.randomUUID();
        recorder.recordFired(jobName, runId, oneShot, vdmsId);
        PublishResult result = publisher.publish(pubsubName, triggerTopic,
            new SchedulerTriggerEvent(jobName, runId.toString(), vdmsId, System.currentTimeMillis()));
        if (!result.success()) {
            log.error("Trigger publish failed job={} vdmsId={} runId={} error={}",
                jobName, vdmsId, runId, result.error());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobCallbackControllerTest"`
Expected: PASS (existing + 3 new).

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/JobCallbackController.java sclera-scheduler/src/test/java/io/sclera/scheduler/web/JobCallbackControllerTest.java
git commit -m "feat(scheduler): parse vdmsId/one-shot from fired Dapr job name"
```

### Task 15: Per-instance ops + snooze + run-at in `JobService`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/JobService.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/JobServiceTest.java`

- [ ] **Step 1: Add failing unit tests** — append to `JobServiceTest`. The new ops need `JobRepository`, `JobInstanceRepository`, `VdmsRegistryRepository`. Add mocks and update the `service()` factory:

```java
    @Mock JobInstanceRepository instances;
    @Mock VdmsRegistryRepository registry;

    // Update the existing service() factory to pass the new repos:
    // JobService s = new JobService(jobs, instances, registry, scheduler, publisher, recorder);

    private JobInstanceEntity instance() {
        return new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
    }
    private void stubInstanceLookup(JobInstanceEntity inst) {
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(inst));
        when(jobs.findById("vdmsSystemHealth")).thenReturn(Optional.of(
                new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                        "scheduler.trigger", JobState.ENABLED)));
        when(registry.findById("vdms-1")).thenReturn(Optional.of(
                new VdmsRegistryEntity("vdms-1", "UTC", true)));
    }

    @Test
    void pauseInstanceDeletesDaprAndSetsPaused() {
        JobInstanceEntity inst = instance();
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(inst));
        service().pauseInstance("vdmsSystemHealth", "vdms-1");
        verify(scheduler).delete("vdmsSystemHealth::vdms-1");
        assertThat(inst.getState()).isEqualTo(JobInstanceState.PAUSED);
    }

    @Test
    void snoozeInstanceDeletesDaprSetsSnoozedAndStoresUntil() {
        JobInstanceEntity inst = instance();
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(inst));
        Instant until = Instant.parse("2026-06-10T00:00:00Z");
        service().snoozeInstance("vdmsSystemHealth", "vdms-1", until);
        verify(scheduler).delete("vdmsSystemHealth::vdms-1");
        assertThat(inst.getState()).isEqualTo(JobInstanceState.SNOOZED);
        assertThat(inst.getSnoozeUntil()).isEqualTo(until);
    }

    @Test
    void resumeInstanceReregistersTimezoneAwareAndEnables() {
        JobInstanceEntity inst = instance();
        inst.setState(JobInstanceState.SNOOZED);
        inst.setSnoozeUntil(Instant.parse("2026-06-10T00:00:00Z"));
        stubInstanceLookup(inst);
        service().resumeInstance("vdmsSystemHealth", "vdms-1");
        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-1", "0 0 0 * * *", "UTC"));
        assertThat(inst.getState()).isEqualTo(JobInstanceState.ENABLED);
        assertThat(inst.getSnoozeUntil()).isNull();
    }

    @Test
    void runAtInstanceRegistersOneShot() {
        JobInstanceEntity inst = instance();
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(inst));
        Instant at = Instant.now().plusSeconds(3600);
        service().runAtInstance("vdmsSystemHealth", "vdms-1", at);
        verify(scheduler).scheduleOnce(startsWith("vdmsSystemHealth::vdms-1::once-"), eq(at));
    }

    @Test
    void runAtInPastIsRejected() {
        JobInstanceEntity inst = instance();
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "vdms-1")))
                .thenReturn(Optional.of(inst));
        assertThatThrownBy(() -> service().runAtInstance(
                "vdmsSystemHealth", "vdms-1", Instant.parse("2000-01-01T00:00:00Z")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unknownInstanceThrows() {
        when(instances.findById(new JobInstanceId("vdmsSystemHealth", "nope")))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> service().pauseInstance("vdmsSystemHealth", "nope"))
                .isInstanceOf(IllegalArgumentException.class);
    }
```

Add imports: `java.time.Instant`, `JobInstanceEntity`, `JobInstanceId`, `JobInstanceState`, `VdmsRegistryEntity`, `JobSchedule`, and `static org.mockito.Mockito.startsWith`.

- [ ] **Step 2: Run tests to verify they fail**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobServiceTest"`
Expected: COMPILE FAILURE — new constructor + methods absent.

- [ ] **Step 3: Add the per-instance ops to `JobService`** — extend the constructor and add the methods. Replace the constructor + fields and add the new methods (keep all existing global methods unchanged):

Add fields:
```java
    private final JobInstanceRepository instances;
    private final VdmsRegistryRepository registry;
```

Replace the constructor:
```java
    public JobService(JobRepository jobs, JobInstanceRepository instances,
                      VdmsRegistryRepository registry, SchedulerClient scheduler,
                      DaprEventPublisher publisher, RunRecorder recorder) {
        this.jobs = jobs;
        this.instances = instances;
        this.registry = registry;
        this.scheduler = scheduler;
        this.publisher = publisher;
        this.recorder = recorder;
    }
```

Add the new methods (and helpers) at the end of the class:
```java
    @Transactional
    public void pauseInstance(String name, String vdmsId) {
        JobInstanceEntity inst = requireInstance(name, vdmsId);
        scheduler.delete(inst.getDaprJobName());
        inst.setState(JobInstanceState.PAUSED);
    }

    @Transactional
    public void disableInstance(String name, String vdmsId) {
        JobInstanceEntity inst = requireInstance(name, vdmsId);
        scheduler.delete(inst.getDaprJobName());
        inst.setState(JobInstanceState.DISABLED);
    }

    @Transactional
    public void resumeInstance(String name, String vdmsId) {
        JobInstanceEntity inst = requireInstance(name, vdmsId);
        scheduler.schedule(new JobSchedule(inst.getDaprJobName(), scheduleOf(name), timezoneOf(vdmsId)));
        inst.setState(JobInstanceState.ENABLED);
        inst.setSnoozeUntil(null);
    }

    /** Time-bounded pause: delete the Dapr job now; the SnoozeReconciler re-arms it at `until`. */
    @Transactional
    public void snoozeInstance(String name, String vdmsId, Instant until) {
        if (until == null || until.isBefore(Instant.now())) {
            throw new IllegalArgumentException("snooze 'until' must be in the future");
        }
        JobInstanceEntity inst = requireInstance(name, vdmsId);
        scheduler.delete(inst.getDaprJobName());
        inst.setState(JobInstanceState.SNOOZED);
        inst.setSnoozeUntil(until);
    }

    /** Schedule a single future run; does not touch the recurring schedule. */
    @Transactional(readOnly = true)
    public void runAtInstance(String name, String vdmsId, Instant at) {
        if (at == null || at.isBefore(Instant.now())) {
            throw new IllegalArgumentException("run-at 'at' must be in the future");
        }
        JobInstanceEntity inst = requireInstance(name, vdmsId);
        String oneShotName = inst.getDaprJobName() + "::once-" + UUID.randomUUID();
        scheduler.scheduleOnce(oneShotName, at);
    }

    private JobInstanceEntity requireInstance(String name, String vdmsId) {
        return instances.findById(new JobInstanceId(name, vdmsId))
            .orElseThrow(() -> new IllegalArgumentException(
                "Unknown job instance: " + name + "::" + vdmsId));
    }

    private String scheduleOf(String name) {
        return jobs.findById(name)
            .orElseThrow(() -> new IllegalArgumentException("Unknown job: " + name))
            .getSchedule();
    }

    private String timezoneOf(String vdmsId) {
        return registry.findById(vdmsId).map(VdmsRegistryEntity::getTimezone).orElse(null);
    }
```

Add imports: `java.time.Instant`, `io.sclera.scheduler.domain.VdmsRegistryEntity` (the `domain.*` import already covers entities — verify), `io.sclera.scheduler.client.JobSchedule` (already imported).

- [ ] **Step 4: Run tests to verify they pass**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=JobServiceTest"`
Expected: PASS (existing global tests + new instance tests).

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/service/JobService.java sclera-scheduler/src/test/java/io/sclera/scheduler/service/JobServiceTest.java
git commit -m "feat(scheduler): per-instance pause/resume/disable + snooze + run-at"
```

### Task 16: `SnoozeReconciler` (in-process re-arm)

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/SnoozeReconciler.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/SnoozeReconcilerTest.java`

- [ ] **Step 1: Write the failing unit test**

```java
package io.sclera.scheduler.service;

import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SnoozeReconcilerTest {

    @Mock JobInstanceRepository instances;
    @Mock JobRepository jobs;
    @Mock VdmsRegistryRepository registry;
    @Mock SchedulerClient scheduler;

    @Test
    void rearmsDueSnoozesTimezoneAwareAndEnables() {
        JobInstanceEntity inst = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        inst.setState(JobInstanceState.SNOOZED);
        inst.setSnoozeUntil(Instant.parse("2020-01-01T00:00:00Z"));
        when(instances.findByStateAndSnoozeUntilLessThanEqual(eq(JobInstanceState.SNOOZED), any()))
                .thenReturn(List.of(inst));
        when(jobs.findById("vdmsSystemHealth")).thenReturn(Optional.of(
                new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                        "scheduler.trigger", JobState.ENABLED)));
        when(registry.findById("vdms-1")).thenReturn(Optional.of(
                new VdmsRegistryEntity("vdms-1", "America/New_York", true)));

        new SnoozeReconciler(instances, jobs, registry, scheduler).rearmExpiredSnoozes();

        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-1", "0 0 0 * * *", "America/New_York"));
        assertThat(inst.getState()).isEqualTo(JobInstanceState.ENABLED);
        assertThat(inst.getSnoozeUntil()).isNull();
    }

    @Test
    void oneBadInstanceDoesNotAbortTheBatch() {
        JobInstanceEntity bad = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        bad.setState(JobInstanceState.SNOOZED);
        JobInstanceEntity good = new JobInstanceEntity("vdmsSystemHealth", "vdms-2", "vdmsSystemHealth::vdms-2");
        good.setState(JobInstanceState.SNOOZED);
        when(instances.findByStateAndSnoozeUntilLessThanEqual(eq(JobInstanceState.SNOOZED), any()))
                .thenReturn(List.of(bad, good));
        when(jobs.findById("vdmsSystemHealth")).thenReturn(Optional.of(
                new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                        "scheduler.trigger", JobState.ENABLED)));
        when(registry.findById("vdms-1")).thenReturn(Optional.empty()); // bad: no tz lookup → still works (null tz)
        when(registry.findById("vdms-2")).thenReturn(Optional.of(
                new VdmsRegistryEntity("vdms-2", "UTC", true)));
        doThrow(new RuntimeException("dapr down")).when(scheduler)
                .schedule(new JobSchedule("vdmsSystemHealth::vdms-1", "0 0 0 * * *", null));

        new SnoozeReconciler(instances, jobs, registry, scheduler).rearmExpiredSnoozes();

        // good still re-armed despite bad throwing
        verify(scheduler).schedule(new JobSchedule("vdmsSystemHealth::vdms-2", "0 0 0 * * *", "UTC"));
        assertThat(good.getState()).isEqualTo(JobInstanceState.ENABLED);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=SnoozeReconcilerTest"`
Expected: COMPILE FAILURE — `SnoozeReconciler` absent.

- [ ] **Step 3: Implement `SnoozeReconciler`**

```java
package io.sclera.scheduler.service;

import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Re-arms snoozed per-VDMS instances whose snooze window has elapsed. This is an in-process
 * Spring {@code @Scheduled} (NOT a Dapr job) — same pattern as HousekeepingService — so snooze
 * state lives only in the DB. On expiry it re-registers the real job through the Dapr control
 * plane. Idempotent and per-item isolated: one failing re-arm must not block the rest.
 */
@Service
public class SnoozeReconciler {

    private static final Logger log = LoggerFactory.getLogger(SnoozeReconciler.class);

    private final JobInstanceRepository instances;
    private final JobRepository jobs;
    private final VdmsRegistryRepository registry;
    private final SchedulerClient scheduler;

    public SnoozeReconciler(JobInstanceRepository instances, JobRepository jobs,
                            VdmsRegistryRepository registry, SchedulerClient scheduler) {
        this.instances = instances;
        this.jobs = jobs;
        this.registry = registry;
        this.scheduler = scheduler;
    }

    @Scheduled(fixedDelayString = "${scheduler.snooze-scan-ms}")
    @Transactional
    public void rearmExpiredSnoozes() {
        List<JobInstanceEntity> due = instances.findByStateAndSnoozeUntilLessThanEqual(
                JobInstanceState.SNOOZED, Instant.now());
        int rearmed = 0;
        for (JobInstanceEntity inst : due) {
            try {
                String schedule = jobs.findById(inst.getJobName())
                        .map(JobEntity::getSchedule).orElse(null);
                if (schedule == null) {
                    log.warn("Snoozed instance {} references unknown job — skipping", inst.getDaprJobName());
                    continue;
                }
                String tz = registry.findById(inst.getVdmsId())
                        .map(VdmsRegistryEntity::getTimezone).orElse(null);
                scheduler.schedule(new JobSchedule(inst.getDaprJobName(), schedule, tz));
                inst.setState(JobInstanceState.ENABLED);
                inst.setSnoozeUntil(null);
                rearmed++;
            } catch (Exception e) {
                log.error("Failed to re-arm snoozed instance {}: {}", inst.getDaprJobName(), e.getMessage());
            }
        }
        if (rearmed > 0) log.info("Re-armed {} snoozed instances", rearmed);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=SnoozeReconcilerTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/service/SnoozeReconciler.java sclera-scheduler/src/test/java/io/sclera/scheduler/service/SnoozeReconcilerTest.java
git commit -m "feat(scheduler): SnoozeReconciler re-arms expired snoozes via Dapr"
```

### Task 17: Per-instance REST endpoints

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/JobInstanceView.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/SchedulerApiController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/SchedulerApiControllerTest.java`

- [ ] **Step 1: Add failing tests** — append to `SchedulerApiControllerTest` (match its existing style — it constructs the controller with mocked repos + `JobService`). The controller now also needs `JobInstanceRepository`. Add tests:

```java
    @Test
    void snoozeEndpointDelegatesToService() {
        controller().snooze("vdmsSystemHealth", "vdms-1", "2026-06-10T00:00:00Z");
        verify(jobService).snoozeInstance("vdmsSystemHealth", "vdms-1",
                java.time.Instant.parse("2026-06-10T00:00:00Z"));
    }

    @Test
    void runAtEndpointDelegatesToService() {
        controller().runAt("vdmsSystemHealth", "vdms-1", "2026-06-10T00:00:00Z");
        verify(jobService).runAtInstance("vdmsSystemHealth", "vdms-1",
                java.time.Instant.parse("2026-06-10T00:00:00Z"));
    }

    @Test
    void pauseInstanceEndpointDelegatesToService() {
        controller().pauseInstance("vdmsSystemHealth", "vdms-1");
        verify(jobService).pauseInstance("vdmsSystemHealth", "vdms-1");
    }
```

Update the `controller()` factory to pass a mocked `JobInstanceRepository instances` (add the `@Mock`).

- [ ] **Step 2: Run tests to verify they fail**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=SchedulerApiControllerTest"`
Expected: COMPILE FAILURE — endpoints + constructor arg absent.

- [ ] **Step 3a: Create `JobInstanceView`**

```java
package io.sclera.scheduler.web.dto;

public record JobInstanceView(
    String jobName,
    String vdmsId,
    String state,
    String snoozeUntil,
    String nextFireAt) {}
```

- [ ] **Step 3b: Add the endpoints to `SchedulerApiController`** — add the `JobInstanceRepository` dependency and the new mappings. Insert the field + constructor arg, then add these methods (alongside the existing global ones):

```java
    @GetMapping("/{name}/instances")
    public List<JobInstanceView> instances(@PathVariable String name) {
        return jobInstances.findByJobName(name).stream().map(i -> new JobInstanceView(
            i.getJobName(), i.getVdmsId(), i.getState().name(),
            iso(i.getSnoozeUntil()), iso(i.getNextFireAt()))).toList();
    }

    @PostMapping("/{name}/instances/{vdmsId}/pause")
    public void pauseInstance(@PathVariable String name, @PathVariable String vdmsId) {
        jobService.pauseInstance(name, vdmsId);
    }

    @PostMapping("/{name}/instances/{vdmsId}/resume")
    public void resumeInstance(@PathVariable String name, @PathVariable String vdmsId) {
        jobService.resumeInstance(name, vdmsId);
    }

    @PostMapping("/{name}/instances/{vdmsId}/disable")
    public void disableInstance(@PathVariable String name, @PathVariable String vdmsId) {
        jobService.disableInstance(name, vdmsId);
    }

    @PostMapping("/{name}/instances/{vdmsId}/snooze")
    public void snooze(@PathVariable String name, @PathVariable String vdmsId,
                       @RequestParam("until") String until) {
        jobService.snoozeInstance(name, vdmsId, Instant.parse(until));
    }

    @PostMapping("/{name}/instances/{vdmsId}/run-at")
    public void runAt(@PathVariable String name, @PathVariable String vdmsId,
                      @RequestParam("at") String at) {
        jobService.runAtInstance(name, vdmsId, Instant.parse(at));
    }
```

Add the field `private final JobInstanceRepository jobInstances;`, add it to the constructor params/assignment, and add imports for `JobInstanceView` and `JobInstanceRepository`. `Instant` is already imported.

> Note: `IllegalArgumentException` from the service (unknown instance OR bad snooze/at time) maps to 404 via the existing `@ExceptionHandler`. That conflates "not found" with "bad time". Keep v1 as-is for parity with the existing handler; a follow-up can split 400 vs 404 like the `history` limit case already does.

- [ ] **Step 4: Run tests to verify they pass**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=SchedulerApiControllerTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/JobInstanceView.java sclera-scheduler/src/main/java/io/sclera/scheduler/web/SchedulerApiController.java sclera-scheduler/src/test/java/io/sclera/scheduler/web/SchedulerApiControllerTest.java
git commit -m "feat(scheduler): per-instance REST endpoints (pause/resume/disable/snooze/run-at/list)"
```

### Task 18: Full scheduler module build (integration gate)

- [ ] **Step 1: Run the whole scheduler suite** (Docker Desktop must be running for the `@SpringBootTest` repo/component tests):

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-scheduler; .\mvnw.cmd test`
Expected: BUILD SUCCESS — all unit + Testcontainers tests pass. In particular `SchedulerFlowComponentTest` (the end-to-end flow test) still passes; if it constructs `JobService`/`CatalogStartupRunner` directly, update it to the new constructors.

- [ ] **Step 2: Fix any wiring fallout** — the most likely breakages are constructor changes (`JobService`, `CatalogStartupRunner`) in component tests or test config. Fix call sites to the new signatures. Re-run until green.

- [ ] **Step 3: Commit any fixes**

```powershell
cd ..
git add sclera-scheduler
git commit -m "test(scheduler): align component tests with VDMS-aware wiring"
```

---

## Phase 5 — vdms-service endpoint & device-asset consumer

### Task 19: `GET /vdms/active` on vdms-service

**Files:**
- Modify: `sclera-vdms-service/src/main/java/io/sclera/vdms/repository/VdmsJpaRepository.java`
- Modify: `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/VdmsController.java`
- Test: `sclera-vdms-service/src/test/java/io/sclera/vdms/controller/VdmsControllerActiveTest.java` (create; if vdms-service has no controller test yet, this is a new pure-unit test that mocks the repo)

- [ ] **Step 1: Write the failing test** (pure Mockito — no Spring context):

```java
package io.sclera.vdms.controller;

import io.sclera.vdms.model.Vdms;
import io.sclera.vdms.repository.UserActionLogRepository;
import io.sclera.vdms.repository.VdmsJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VdmsControllerActiveTest {

    @Mock VdmsJpaRepository repo;
    @Mock UserActionLogRepository auditRepo;

    @Test
    void activeReturnsIdAndTimezonePerActiveVdms() {
        Vdms v = new Vdms();
        v.setId("vdms-1");
        v.setTimezone("America/New_York");
        when(repo.findAllActive()).thenReturn(List.of(v));

        List<Map<String, String>> out = new VdmsController(repo, auditRepo).getActive();

        assertThat(out).hasSize(1);
        assertThat(out.get(0)).containsEntry("vdmsId", "vdms-1")
                              .containsEntry("timezone", "America/New_York");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-vdms-service; .\mvnw.cmd -q test "-Dtest=VdmsControllerActiveTest"`
Expected: COMPILE FAILURE — `findAllActive` and `getActive` absent.

- [ ] **Step 3a: Add `findAllActive` to `VdmsJpaRepository`**

```java
    @Query(value = "SELECT * FROM vdms WHERE activation_status = 'ACTIVE'", nativeQuery = true)
    java.util.List<Vdms> findAllActive();
```

> Confirm the active sentinel: this codebase stores `activation_status` as the lifecycle flag. If active rows use a different value (e.g. `'active'` lowercase or a numeric `status`), adjust the WHERE clause to match the real data before merging. Grep existing writes/seed data to verify.

- [ ] **Step 3b: Add the endpoint to `VdmsController`** — add the import `java.util.stream.Collectors` is not needed (use `.toList()`); add:

```java
    /** GET /vdms/active → [{ "vdmsId": ..., "timezone": ... }] for all active VDMS.
     *  Consumed by the scheduler's startup-sync (Dapr service-invocation). */
    @GetMapping("/vdms/active")
    public List<Map<String, String>> getActive() {
        return repo.findAllActive().stream()
            .map(v -> Map.of(
                "vdmsId", v.getId() != null ? v.getId() : "",
                "timezone", v.getTimezone() != null ? v.getTimezone() : ""))
            .toList();
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-vdms-service; .\mvnw.cmd -q test "-Dtest=VdmsControllerActiveTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-vdms-service/src/main/java/io/sclera/vdms/repository/VdmsJpaRepository.java sclera-vdms-service/src/main/java/io/sclera/vdms/controller/VdmsController.java sclera-vdms-service/src/test/java/io/sclera/vdms/controller/VdmsControllerActiveTest.java
git commit -m "feat(vdms-service): GET /vdms/active for scheduler startup-sync"
```

### Task 20: Thread `vdmsId` through the device-asset consumer

**Files:**
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/scheduler/TriggerDispatchSubscriber.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/scheduler/DeviceAssetJobHandlers.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/scheduler/TriggerDispatchSubscriberTest.java`

- [ ] **Step 1: Update the test** — `vdmsSystemHealth` is now per-VDMS and the handler takes a `vdmsId`. Add to `TriggerDispatchSubscriberTest`:

```java
    @Test
    void perVdmsJob_passesVdmsIdToHandler() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e1", null));
        subscriber().handleEvent(new SchedulerTriggerEvent("vdmsSystemHealth", "r9", "vdms-7", 0L));
        verify(handlers).vdmsSystemHealth("vdms-7");
        ArgumentCaptor<SchedulerResultEvent> cap = ArgumentCaptor.forClass(SchedulerResultEvent.class);
        verify(publisher).publish(eq("pubsub"), eq("scheduler.result"), cap.capture());
        assertEquals("SUCCESS", cap.getValue().status());
    }
```

> The existing `vdmsSystemHealth` mock interactions elsewhere in the test (if any) must switch to the `vdmsSystemHealth(String)` overload.

- [ ] **Step 2: Run test to verify it fails**

First reinstall dapr-commons is already done (Tasks 1–2). Run:
`$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-cloud-device-asset; .\mvnw.cmd -q test "-Dtest=TriggerDispatchSubscriberTest"`
Expected: COMPILE FAILURE — `vdmsSystemHealth(String)` absent.

- [ ] **Step 3a: Change `DeviceAssetJobHandlers.vdmsSystemHealth` to accept `vdmsId`**

```java
    /** Mirrors {@code Schedular#scheduleVdmsSystemHealth}, now per-VDMS. The scheduler fires
     *  this once per VDMS; vdmsId identifies which site to process.
     *  STUB: LorawanService, vdmsService.AddSystemHealthAsResponse(), and
     *  vdmsService.sendVDMSData() were not extracted into this service. */
    public void vdmsSystemHealth(String vdmsId) { stub("vdmsSystemHealth[" + vdmsId + "]"); }
```

- [ ] **Step 3b: Thread `vdmsId` through `TriggerDispatchSubscriber`** — pass the event's `vdmsId` into `run`, and route the per-VDMS job:

Change `handleEvent` to call `run(data.jobName(), data.vdmsId())`:
```java
    @Override
    protected void handleEvent(SchedulerTriggerEvent data) {
        if (!OWNED.contains(data.jobName())) {
            return; // another service owns this job
        }
        long t0 = System.currentTimeMillis();
        try {
            run(data.jobName(), data.vdmsId());
            publish(data, "SUCCESS", t0, null);
        } catch (Exception e) {
            publish(data, "FAILED", t0, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
```

Change `run` to accept and use `vdmsId` for the per-VDMS case:
```java
    private void run(String job, String vdmsId) {
        switch (job) {
            case "historyRecord" -> handlers.historyRecord();
            case "unlinkVendorOrganisation" -> handlers.unlinkVendorOrganisation();
            case "internetBandwidthCheck" -> handlers.internetBandwidthCheck();
            case "vdmsSystemHealth" -> handlers.vdmsSystemHealth(vdmsId);
            case "connectedStatusForIOC" -> handlers.connectedStatusForIOC();
            case "qrcodeNfcBarcodeSync" -> handlers.qrcodeNfcBarcodeSync();
            case "syncAssetCountToCloud" -> handlers.syncAssetCountToCloud();
            case "userActionLog" -> handlers.userActionLog();
            case "deviceDndEnable" -> handlers.deviceDndEnable();
            case "offlineDeviceCheck" -> handlers.offlineDeviceCheck();
            default -> { /* unreachable: guarded by OWNED */ }
        }
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"; cd sclera-cloud-device-asset; .\mvnw.cmd -q test "-Dtest=TriggerDispatchSubscriberTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
cd ..
git add sclera-cloud-device-asset/src/main/java/io/sclera/scheduler/TriggerDispatchSubscriber.java sclera-cloud-device-asset/src/main/java/io/sclera/scheduler/DeviceAssetJobHandlers.java sclera-cloud-device-asset/src/test/java/io/sclera/scheduler/TriggerDispatchSubscriberTest.java
git commit -m "feat(device-asset): thread vdmsId into per-VDMS scheduler handler"
```

### Task 21: Cross-module build verification

- [ ] **Step 1: Build the three touched modules in dependency order**

```powershell
$env:JAVA_HOME="C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"
cd dapr-commons; .\mvnw.cmd install -DskipTests; cd ..
cd sclera-scheduler; .\mvnw.cmd test; cd ..
cd sclera-vdms-service; .\mvnw.cmd test; cd ..
cd sclera-cloud-device-asset; .\mvnw.cmd test; cd ..
```
Expected: BUILD SUCCESS for each. Docker Desktop must be running for scheduler's Testcontainers tests.

- [ ] **Step 2: Fix any remaining compile/test fallout** from the shared-event change in modules not yet touched (search for other `new SchedulerTriggerEvent(` call sites). The 3-arg convenience constructor keeps GLOBAL callers compiling, so fallout should be limited to anything that pattern-matched the record's component count.

- [ ] **Step 3: Final commit**

```powershell
git add -A
git commit -m "test: cross-module verification for VDMS-aware scheduler"
```

---

## Integration requirement (out of scope for this plan — documented for the VDMS-owner)

The activation/deactivation write path is **not in this codebase**. For live add/remove to work end-to-end, the VDMS-owning service must publish `VdmsLifecycleEvent` to topic `vdms.lifecycle` (pubsub `pubsub`) on activation (`status=ACTIVATED`, with `timezone`) and deactivation (`status=DEACTIVATED`). Until then, **startup-sync** (Task 13) registers all currently-active VDMS on each scheduler boot, so scheduling is correct on restart even without the event — only *live* mid-run add/remove waits on this emit.

---

## Self-Review

**Spec coverage:**
- Per-VDMS jobs (`{jobName}::{vdmsId}`, timezone-aware) → Tasks 7, 9, 11. ✓
- Configurable scope per job → Tasks 4, 5. ✓
- VDMS discovery: lifecycle event + startup-sync → Tasks 2, 10, 12, 13. ✓
- Timezone (CRON_TZ) → Task 9; applied in 11, 15, 16. ✓
- Snooze (auto-resume) → Tasks 15 (snooze op) + 16 (re-arm reconciler). ✓
- One-shot run-at → Tasks 9 (scheduleOnce) + 15 (runAtInstance) + 14 (one-shot fire parse). ✓
- Per (job × VDMS) granularity → Task 7 composite key; all ops keyed on (name, vdmsId). ✓
- Trigger event vdmsId ripple → Tasks 1, 14, 20. ✓
- job_run.vdms_id attribution → Tasks 3, 8. ✓
- Scale guardrail (max-instances) → Tasks 10 (config) + 11. ✓
- vdms-service GET /vdms/active → Task 19. ✓
- Migration → Task 3. ✓
- Execution-engine note (Dapr control plane; snooze re-arm in-process) → honored in Tasks 9, 16. ✓

**Type consistency check:** `daprName` format `{jobName}::{vdmsId}` is identical in `PerVdmsRegistrar.daprName`, `JobCallbackController` parse, and `JobService` (`inst.getDaprJobName()`); one-shot suffix `::once-` is consistent across `JobService.runAtInstance`, `SchedulerClient` test, and `JobCallbackController` parse. `recordFired(name, runId, manual, vdmsId)` 4-arg signature consistent across `RunRecorder`, `JobCallbackController`, and tests. `JobInstanceState` values (ENABLED/PAUSED/SNOOZED/DISABLED) consistent across entity, JobService, SnoozeReconciler. Constructor changes (`JobService` 6-arg, `CatalogStartupRunner` 6-arg) flagged for component-test fixups in Tasks 15/13/18.

**Placeholder scan:** No TBD/TODO. Two explicit "verify against real data" callouts (active-status sentinel in Task 19; component-test constructor fixups in Task 18) are real verification steps, not placeholders.
