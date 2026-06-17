# Scheduler VDMS Management UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let an operator add a VDMS, edit its timezone, and remove it from the scheduler dashboard — writing the scheduler-owned `vdms_registry` and (re)registering that VDMS's per-VDMS Dapr jobs.

**Architecture:** A thin new `VdmsAdminController` (`/api/vdms`) validates input and delegates to the existing idempotent `PerVdmsRegistrar`: add → `onVdmsActivated`, remove → `onVdmsDeactivated`, edit-timezone → a new **state-preserving** `reregister` (re-schedules only ENABLED instances with the new `CRON_TZ`, leaving PAUSED/SNOOZED/DISABLED untouched). The single-file `scheduler.html` gains a "VDMS" panel. No second scheduling path; no cross-service writes.

**Tech Stack:** Java 21 (Corretto 21.0.8), Spring Boot 4.0.6, Spring Data JPA, PostgreSQL, JUnit 5 + Mockito + MockMvc + Testcontainers (backend). Frontend is one static HTML file with inline vanilla JS — no Node/bundler, so the UI task is verified manually.

**Spec:** `docs/superpowers/specs/2026-06-15-scheduler-vdms-management-ui-design.md`

**Branch:** `feature/scheduler-vdms-management-ui` (off `feature/scheduler-service`).

---

## Build & test conventions (read once)

No global `mvn`. Build from inside `sclera-scheduler` via its wrapper, with `JAVA_HOME` set in the SAME command (env does not persist between tool calls). `@SpringBootTest` tests start a Testcontainers Postgres — Docker Desktop must be running.

```powershell
$env:JAVA_HOME = "C:\Users\DhanushVasanth\.jdks\corretto-21.0.8"
cd sclera-scheduler; .\mvnw.cmd -q test "-Dtest=VdmsAdminControllerTest"
```

The dashboard is served by the running app at `http://localhost:8098/scheduler.html`.

**Spec deviation (intentional):** the spec wrote the zones endpoint as `GET /api/timezones`; this plan places it under the controller as `GET /api/vdms/timezones` (no separate controller). The frontend calls the latter.

---

## Reference: existing code these tasks build on (verified)

`PerVdmsRegistrar` (`io.sclera.scheduler.service`) — fields `jobs` (JobRepository), `registry` (VdmsRegistryRepository), `instances` (JobInstanceRepository), `scheduler` (SchedulerClient). Existing methods:
- `onVdmsActivated(String vdmsId, String timezone)` — upserts the registry row (`active=true`, `timezone`), then registers **all** PER_VDMS instances (re-creates Dapr jobs even for paused ones — why edit-timezone must NOT reuse it).
- `onVdmsDeactivated(String vdmsId)` — deletes each instance's Dapr job, sets instance `DISABLED`, sets registry `active=false`.
- private `registerInstance(JobEntity, vdmsId, tz)` — `instances.findById(new JobInstanceId(name,vdmsId)).orElseGet(save new)` then `scheduler.schedule(new JobSchedule(daprName, job.getSchedule(), tz))`.

Signatures: `VdmsRegistryEntity(String vdmsId, String timezone, boolean active)` with `getVdmsId/getTimezone/setTimezone/isActive/setActive`. `JobInstanceEntity(String jobName, String vdmsId, String daprJobName)` with `getJobName/getVdmsId/getDaprJobName/getState/setState`. `JobInstanceState { ENABLED, PAUSED, SNOOZED, DISABLED }`. `JobScope { GLOBAL, PER_VDMS }`. `JobEntity(name, schedule, owner, triggerTopic, JobState)` with `getName/getSchedule`. `JobSchedule(String name, String schedule, String timezone)`. `JobRepository.findByScope(JobScope)`, `findById(name)`. `VdmsRegistryRepository extends JpaRepository<…,String>` + `findByActiveTrue()`. `JobInstanceRepository.findByVdmsId(String)`.

Test harness: tests extend `io.sclera.scheduler.AbstractPostgresTest` (Testcontainers Postgres; mocks `CatalogStartupRunner` so the job table stays empty), annotate `@SpringBootTest @Transactional`, `@MockitoBean SchedulerClient schedulerClient`, build MockMvc via `MockMvcBuilders.webAppContextSetup(ctx).build()`.

---

## File Structure (decomposition locked here)

**Backend (`sclera-scheduler`):**
- Modify `domain/JobInstanceRepository.java` — add `countByVdmsId`.
- Modify `service/PerVdmsRegistrar.java` — add `reregister(vdmsId, timezone)`.
- Create `web/dto/VdmsRegistryView.java` — response record.
- Create `web/VdmsAdminController.java` — the 5 endpoints.
- Tests: modify `test/.../domain/JobInstanceRepositoryTest.java`; modify `test/.../service/PerVdmsRegistrarTest.java`; create `test/.../web/VdmsAdminControllerTest.java`.

**Frontend:**
- Modify `src/main/resources/static/scheduler.html` — VDMS panel + its JS.

---

## Task 1: `JobInstanceRepository.countByVdmsId`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceRepository.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobInstanceRepositoryTest.java`

- [ ] **Step 1: Add a failing test** — append to `JobInstanceRepositoryTest` (it already autowires `jobs`, `registry`, `instances` and has a `seed()` creating the `vdmsSystemHealth` PER_VDMS job + `vdms-1`):

```java
    @Test
    void countsInstancesForOneVdms() {
        seed();
        registry.save(new VdmsRegistryEntity("vdms-2", "UTC", true));
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1"));
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-2", "vdmsSystemHealth::vdms-2"));

        assertEquals(1, instances.countByVdmsId("vdms-1"));
        assertEquals(0, instances.countByVdmsId("vdms-404"));
    }
```

- [ ] **Step 2: Run it to verify it fails** — `…\mvnw.cmd -q test "-Dtest=JobInstanceRepositoryTest"` → FAIL/compile error (`countByVdmsId` not defined).

- [ ] **Step 3: Add the derived query** to `JobInstanceRepository`:

```java
    long countByVdmsId(String vdmsId);
```

- [ ] **Step 4: Run it to verify it passes** — `…\mvnw.cmd -q test "-Dtest=JobInstanceRepositoryTest"` → PASS.

- [ ] **Step 5: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobInstanceRepository.java sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobInstanceRepositoryTest.java
git commit -m "feat(scheduler): JobInstanceRepository.countByVdmsId"
```

---

## Task 2: `PerVdmsRegistrar.reregister` (state-preserving timezone re-arm)

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/PerVdmsRegistrar.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/PerVdmsRegistrarTest.java`

Re-register only ENABLED instances with the new timezone; update the registry timezone; leave PAUSED/SNOOZED/DISABLED instances (and their absent Dapr jobs) untouched.

- [ ] **Step 1: Write a failing test** — append to `PerVdmsRegistrarTest` (mirror its existing setup: `@MockitoBean SchedulerClient scheduler`, autowired `jobs`/`registry`/`instances`, a `PerVdmsRegistrar` under test). If the class lacks a reusable seed, inline it as below:

```java
    @Test
    void reregisterUpdatesTimezoneAndOnlyReschedulesEnabledInstances() {
        JobEntity job = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        job.setScope(JobScope.PER_VDMS);   // verified: JobEntity has setScope(JobScope)
        jobs.save(job);
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1")); // ENABLED
        JobInstanceEntity paused = new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1-x");
        paused.setState(JobInstanceState.PAUSED);
        instances.save(paused);

        registrar.reregister("vdms-1", "America/New_York");

        assertEquals("America/New_York", registry.findById("vdms-1").orElseThrow().getTimezone());
        // ENABLED instance re-scheduled with the new tz; PAUSED one is NOT
        org.mockito.ArgumentCaptor<JobSchedule> cap = org.mockito.ArgumentCaptor.forClass(JobSchedule.class);
        verify(scheduler).schedule(cap.capture());
        assertEquals("vdmsSystemHealth::vdms-1", cap.getValue().name());
        assertEquals("America/New_York", cap.getValue().timezone());
    }
```

> Verified signatures: `JobEntity.setScope(JobScope)` exists; `JobSchedule` is a record with accessors `name()`/`schedule()`/`timezone()` (and a 2-arg `JobSchedule(name, schedule)` convenience ctor). `findByScope(PER_VDMS)` returns the seeded job.

- [ ] **Step 2: Run it to verify it fails** — `…\mvnw.cmd -q test "-Dtest=PerVdmsRegistrarTest"` → FAIL (`reregister` not defined).

- [ ] **Step 3: Implement `reregister`** in `PerVdmsRegistrar` (add after `onVdmsDeactivated`):

```java
    /** Edit-timezone path: update the registry tz and re-arm ONLY ENABLED instances with the new
     *  CRON_TZ. Paused/snoozed/disabled instances keep their state (and their torn-down Dapr jobs). */
    @Transactional
    public void reregister(String vdmsId, String timezone) {
        registry.findById(vdmsId).ifPresent(v -> { v.setTimezone(timezone); registry.save(v); });
        for (JobInstanceEntity inst : instances.findByVdmsId(vdmsId)) {
            if (inst.getState() == JobInstanceState.ENABLED) {
                jobs.findById(inst.getJobName()).ifPresent(job ->
                    scheduler.schedule(new JobSchedule(inst.getDaprJobName(), job.getSchedule(), timezone)));
            }
        }
        log.info("Re-registered VDMS {} ENABLED instances with timezone {}", vdmsId, timezone);
    }
```

- [ ] **Step 4: Run it to verify it passes** — `…\mvnw.cmd -q test "-Dtest=PerVdmsRegistrarTest"` → PASS.

- [ ] **Step 5: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/service/PerVdmsRegistrar.java sclera-scheduler/src/test/java/io/sclera/scheduler/service/PerVdmsRegistrarTest.java
git commit -m "feat(scheduler): PerVdmsRegistrar.reregister (state-preserving timezone re-arm)"
```

---

## Task 3: `VdmsRegistryView` + `VdmsAdminController` read endpoints (list + timezones)

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/VdmsRegistryView.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/VdmsAdminController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/VdmsAdminControllerTest.java`

- [ ] **Step 1: Create the view DTO**

```java
package io.sclera.scheduler.web.dto;

public record VdmsRegistryView(String vdmsId, String timezone, boolean active, long jobCount) {}
```

- [ ] **Step 2: Write the failing test** (create the class):

```java
package io.sclera.scheduler.web;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class VdmsAdminControllerTest extends AbstractPostgresTest {

    @Autowired WebApplicationContext ctx;
    @Autowired VdmsRegistryRepository registry;
    @Autowired JobInstanceRepository instances;
    @MockitoBean SchedulerClient schedulerClient;

    MockMvc mvc() { return MockMvcBuilders.webAppContextSetup(ctx).build(); }

    @Test
    void listReturnsRegistryRowsWithJobCount() throws Exception {
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1"));

        mvc().perform(get("/api/vdms"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].vdmsId").value("vdms-1"))
            .andExpect(jsonPath("$[0].timezone").value("UTC"))
            .andExpect(jsonPath("$[0].active").value(true))
            .andExpect(jsonPath("$[0].jobCount").value(1));
    }

    @Test
    void timezonesReturnsNonEmptySortedList() throws Exception {
        mvc().perform(get("/api/vdms/timezones"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").exists())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasItem("UTC")));
    }
}
```

- [ ] **Step 3: Run it to verify it fails** — `…\mvnw.cmd -q test "-Dtest=VdmsAdminControllerTest"` → FAIL (no controller).

- [ ] **Step 4: Create the controller** with the read endpoints + validation/delegation helpers (the write endpoints are added in Tasks 4–6):

```java
package io.sclera.scheduler.web;

import io.sclera.scheduler.domain.JobInstanceRepository;
import io.sclera.scheduler.domain.VdmsRegistryEntity;
import io.sclera.scheduler.domain.VdmsRegistryRepository;
import io.sclera.scheduler.service.PerVdmsRegistrar;
import io.sclera.scheduler.web.dto.VdmsRegistryView;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/vdms")
public class VdmsAdminController {

    private final VdmsRegistryRepository registry;
    private final JobInstanceRepository instances;
    private final PerVdmsRegistrar registrar;

    public VdmsAdminController(VdmsRegistryRepository registry, JobInstanceRepository instances,
                               PerVdmsRegistrar registrar) {
        this.registry = registry;
        this.instances = instances;
        this.registrar = registrar;
    }

    @GetMapping
    public List<VdmsRegistryView> list() {
        return registry.findAll().stream().map(this::toView).toList();
    }

    @GetMapping("/timezones")
    public List<String> timezones() {
        return ZoneId.getAvailableZoneIds().stream().sorted().toList();
    }

    // ---- helpers (used by write endpoints in later tasks) ----

    private VdmsRegistryView toView(VdmsRegistryEntity v) {
        return new VdmsRegistryView(v.getVdmsId(), v.getTimezone(), v.isActive(),
                instances.countByVdmsId(v.getVdmsId()));
    }

    /** Trim + validate an IANA zone; 400 on blank/invalid. Returns the canonical zone id. */
    private String validTimezone(String tz) {
        try {
            return ZoneId.of(tz == null ? "" : tz.trim()).getId();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid timezone: " + tz);
        }
    }

    private String requireVdmsId(String vdmsId) {
        String v = vdmsId == null ? "" : vdmsId.trim();
        if (v.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "vdmsId must not be blank");
        return v;
    }
}
```

- [ ] **Step 5: Run it to verify it passes** — `…\mvnw.cmd -q test "-Dtest=VdmsAdminControllerTest"` → PASS (both tests).

- [ ] **Step 6: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/VdmsRegistryView.java sclera-scheduler/src/main/java/io/sclera/scheduler/web/VdmsAdminController.java sclera-scheduler/src/test/java/io/sclera/scheduler/web/VdmsAdminControllerTest.java
git commit -m "feat(scheduler): VdmsAdminController read endpoints (list + timezones)"
```

---

## Task 4: Add VDMS — `POST /api/vdms`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/VdmsAdminController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/VdmsAdminControllerTest.java`

- [ ] **Step 1: Add failing tests** (append to `VdmsAdminControllerTest`):

```java
    @Autowired io.sclera.scheduler.domain.JobRepository jobs;

    private void seedPerVdmsJob() {
        var j = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        j.setScope(JobScope.PER_VDMS);   // adjust if JobEntity exposes scope differently
        jobs.save(j);
    }

    @Test
    void addCreatesRegistryRowAndRegistersInstances() throws Exception {
        seedPerVdmsJob();
        mvc().perform(post("/api/vdms").contentType("application/json")
                .content("{\"vdmsId\":\"vdms-1\",\"timezone\":\"America/New_York\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.vdmsId").value("vdms-1"))
            .andExpect(jsonPath("$.timezone").value("America/New_York"))
            .andExpect(jsonPath("$.active").value(true));
        assertTrue(registry.findById("vdms-1").isPresent());
        assertEquals(1, instances.countByVdmsId("vdms-1"));  // the PER_VDMS job fanned out
    }

    @Test
    void addRejectsDuplicateActiveVdms() throws Exception {
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        mvc().perform(post("/api/vdms").contentType("application/json")
                .content("{\"vdmsId\":\"vdms-1\",\"timezone\":\"UTC\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void addRejectsInvalidTimezone() throws Exception {
        mvc().perform(post("/api/vdms").contentType("application/json")
                .content("{\"vdmsId\":\"vdms-9\",\"timezone\":\"Not/AZone\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void addRejectsBlankId() throws Exception {
        mvc().perform(post("/api/vdms").contentType("application/json")
                .content("{\"vdmsId\":\"  \",\"timezone\":\"UTC\"}"))
            .andExpect(status().isBadRequest());
    }
```

> Add the imports the new asserts need: `import static org.junit.jupiter.api.Assertions.*;` and `import io.sclera.scheduler.domain.JobEntity; import io.sclera.scheduler.domain.JobScope; import io.sclera.scheduler.domain.JobState;` (most are covered by `domain.*`).

- [ ] **Step 2: Run to verify they fail** — `…\mvnw.cmd -q test "-Dtest=VdmsAdminControllerTest"` → the 4 new tests FAIL (no POST mapping).

- [ ] **Step 3: Add the endpoint + request record** to `VdmsAdminController`:

```java
    public record AddVdmsRequest(String vdmsId, String timezone) {}

    @PostMapping
    public org.springframework.http.ResponseEntity<VdmsRegistryView> add(@RequestBody AddVdmsRequest req) {
        String vdmsId = requireVdmsId(req.vdmsId());
        String tz = validTimezone(req.timezone());
        registry.findById(vdmsId).ifPresent(v -> {
            if (v.isActive())
                throw new ResponseStatusException(HttpStatus.CONFLICT, "VDMS already active: " + vdmsId);
        });
        registrar.onVdmsActivated(vdmsId, tz);   // upserts (reactivates a soft-removed row) + fans out jobs
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED)
                .body(toView(registry.findById(vdmsId).orElseThrow()));
    }
```

- [ ] **Step 4: Run to verify they pass** — `…\mvnw.cmd -q test "-Dtest=VdmsAdminControllerTest"` → PASS.

- [ ] **Step 5: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/VdmsAdminController.java sclera-scheduler/src/test/java/io/sclera/scheduler/web/VdmsAdminControllerTest.java
git commit -m "feat(scheduler): POST /api/vdms (add VDMS, fan out per-VDMS jobs)"
```

---

## Task 5: Edit timezone — `PUT /api/vdms/{vdmsId}/timezone`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/VdmsAdminController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/VdmsAdminControllerTest.java`

- [ ] **Step 1: Add failing tests** (append):

```java
    @Test
    void editTimezoneUpdatesRegistryAndReregisters() throws Exception {
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1"));
        seedPerVdmsJob();

        mvc().perform(put("/api/vdms/vdms-1/timezone").contentType("application/json")
                .content("{\"timezone\":\"Asia/Kolkata\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timezone").value("Asia/Kolkata"));
        assertEquals("Asia/Kolkata", registry.findById("vdms-1").orElseThrow().getTimezone());
        // ENABLED instance re-scheduled with the new tz
        org.mockito.ArgumentCaptor<io.sclera.scheduler.client.JobSchedule> cap =
            org.mockito.ArgumentCaptor.forClass(io.sclera.scheduler.client.JobSchedule.class);
        org.mockito.Mockito.verify(schedulerClient, org.mockito.Mockito.atLeastOnce()).schedule(cap.capture());
        assertTrue(cap.getAllValues().stream().anyMatch(s -> "Asia/Kolkata".equals(s.timezone())));
    }

    @Test
    void editTimezoneRejectsUnknownVdms() throws Exception {
        mvc().perform(put("/api/vdms/nope/timezone").contentType("application/json")
                .content("{\"timezone\":\"UTC\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void editTimezoneRejectsInvalidZone() throws Exception {
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        mvc().perform(put("/api/vdms/vdms-1/timezone").contentType("application/json")
                .content("{\"timezone\":\"Bogus/Zone\"}"))
            .andExpect(status().isBadRequest());
    }
```

> Verified: `JobSchedule` is a record — `s.timezone()` is correct.

- [ ] **Step 2: Run to verify they fail** — FAIL (no PUT mapping).

- [ ] **Step 3: Add the endpoint** to `VdmsAdminController`:

```java
    public record TimezoneRequest(String timezone) {}

    @PutMapping("/{vdmsId}/timezone")
    public VdmsRegistryView editTimezone(@PathVariable String vdmsId, @RequestBody TimezoneRequest req) {
        if (registry.findById(vdmsId).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no such VDMS: " + vdmsId);
        String tz = validTimezone(req.timezone());
        registrar.reregister(vdmsId, tz);   // state-preserving (Task 2)
        return toView(registry.findById(vdmsId).orElseThrow());
    }
```

- [ ] **Step 4: Run to verify they pass** — PASS.

- [ ] **Step 5: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/VdmsAdminController.java sclera-scheduler/src/test/java/io/sclera/scheduler/web/VdmsAdminControllerTest.java
git commit -m "feat(scheduler): PUT /api/vdms/{id}/timezone (state-preserving re-register)"
```

---

## Task 6: Remove VDMS — `DELETE /api/vdms/{vdmsId}`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/VdmsAdminController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/VdmsAdminControllerTest.java`

- [ ] **Step 1: Add failing tests** (append):

```java
    @Test
    void removeDeactivatesVdmsAndDisablesInstances() throws Exception {
        registry.save(new VdmsRegistryEntity("vdms-1", "UTC", true));
        instances.save(new JobInstanceEntity("vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1"));

        mvc().perform(delete("/api/vdms/vdms-1")).andExpect(status().isNoContent());

        assertFalse(registry.findById("vdms-1").orElseThrow().isActive());
        assertEquals(JobInstanceState.DISABLED,
            instances.findByVdmsId("vdms-1").get(0).getState());
    }

    @Test
    void removeRejectsUnknownVdms() throws Exception {
        mvc().perform(delete("/api/vdms/nope")).andExpect(status().isNotFound());
    }
```

- [ ] **Step 2: Run to verify they fail** — FAIL (no DELETE mapping).

- [ ] **Step 3: Add the endpoint** to `VdmsAdminController`:

```java
    @DeleteMapping("/{vdmsId}")
    public org.springframework.http.ResponseEntity<Void> remove(@PathVariable String vdmsId) {
        if (registry.findById(vdmsId).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no such VDMS: " + vdmsId);
        registrar.onVdmsDeactivated(vdmsId);   // delete Dapr jobs, DISABLE instances, active=false
        return org.springframework.http.ResponseEntity.noContent().build();
    }
```

- [ ] **Step 4: Run to verify they pass** — PASS.

- [ ] **Step 5: Run the WHOLE scheduler suite** to confirm no regression — `…\mvnw.cmd -q test` → BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/VdmsAdminController.java sclera-scheduler/src/test/java/io/sclera/scheduler/web/VdmsAdminControllerTest.java
git commit -m "feat(scheduler): DELETE /api/vdms/{id} (deactivate + tear down jobs)"
```

---

## Task 7: VDMS panel in `scheduler.html` (manual verification)

**Files:**
- Modify: `sclera-scheduler/src/main/resources/static/scheduler.html`

No JS test harness on this machine — this task adds concrete code and is verified manually in Task 8. Keep all new JS in dedicated functions; do not touch `render()`/`fetchData()` internals beyond one added call.

- [ ] **Step 1: Add the panel container** — inside `<div class="main" id="groups">`'s parent `.main`, insert a sibling **before** `#groups` (so `render()`, which only rewrites `#groups`, never clobbers it). Change line `<div class="main" id="groups"></div>` (≈line 186) to:

```html
    <div class="main">
      <section id="vdms-panel" style="border:1px solid var(--line);border-radius:10px;margin-bottom:.8rem;padding:.7rem .9rem;background:var(--panel)">
        <div style="display:flex;align-items:center;gap:.6rem;margin-bottom:.5rem">
          <strong style="font-size:.9rem">VDMS</strong>
          <span class="pill" id="vdms-count">0</span>
        </div>
        <div id="vdms-add" style="display:flex;gap:.5rem;flex-wrap:wrap;align-items:center;margin-bottom:.5rem">
          <input id="vdms-id" class="search" style="width:160px" placeholder="vdms id…" autocomplete="off"/>
          <select id="vdms-tz" class="search" style="width:200px"></select>
          <button class="btn" id="vdms-add-btn">Add VDMS</button>
          <span class="d" style="color:var(--muted2)">id is not verified against the fleet — must match the id the owning services use</span>
        </div>
        <div id="vdms-msg" style="font-size:.74rem;min-height:1em;margin-bottom:.4rem"></div>
        <table style="width:100%;border-collapse:collapse;font-size:.8rem"><tbody id="vdms-rows"></tbody></table>
      </section>
      <div id="groups"></div>
    </div>
```

> Remove the original standalone `<div class="main" id="groups"></div>` line — it is now nested inside the new `.main` wrapper above.

- [ ] **Step 2: Add the JS** — just before `</script>` (≈line 537), append:

```javascript
let vdmsZones = null;
async function loadVdms(){
  try{
    if(!vdmsZones){
      vdmsZones = await fetch('/api/vdms/timezones').then(x=>x.json());
      const sel = document.getElementById('vdms-tz');
      sel.innerHTML = vdmsZones.map(z=>`<option>${z}</option>`).join('');
      sel.value = 'UTC';
    }
    const rows = await fetch('/api/vdms').then(x=>x.json());
    document.getElementById('vdms-count').textContent = rows.length;
    document.getElementById('vdms-rows').innerHTML = rows.map(v=>`
      <tr data-vdms="${v.vdmsId}" style="border-top:1px solid var(--line)">
        <td style="padding:.35rem 0">${v.vdmsId}</td>
        <td><select class="search vtz" style="width:180px"></select></td>
        <td>${v.active?'<span class="sw ok"></span> active':'<span class="sw fail"></span> removed'}</td>
        <td>${v.jobCount} jobs</td>
        <td style="text-align:right">
          <button class="btn vsave">Save tz</button>
          <button class="btn vdel">Remove</button>
        </td>
      </tr>`).join('');
    document.querySelectorAll('#vdms-rows tr').forEach(tr=>{
      const sel = tr.querySelector('.vtz');
      sel.innerHTML = vdmsZones.map(z=>`<option>${z}</option>`).join('');
      sel.value = rows.find(r=>r.vdmsId===tr.dataset.vdms).timezone || 'UTC';
    });
  }catch(e){ vmsg('load failed: '+e, true); }
}
function vmsg(t, err){ const m=document.getElementById('vdms-msg'); m.textContent=t; m.style.color= err?'var(--red)':'var(--green)'; }
document.getElementById('vdms-add-btn').addEventListener('click', async ()=>{
  const vdmsId = document.getElementById('vdms-id').value.trim();
  const timezone = document.getElementById('vdms-tz').value;
  const r = await fetch('/api/vdms',{method:'POST',headers:{'Content-Type':'application/json'},
      body:JSON.stringify({vdmsId,timezone})});
  if(r.ok){ vmsg('added '+vdmsId); document.getElementById('vdms-id').value=''; await loadVdms(); await fetchData(); }
  else { vmsg('add failed: '+(await r.text()), true); }
});
document.getElementById('vdms-rows').addEventListener('click', async e=>{
  const tr = e.target.closest('tr'); if(!tr) return; const id = tr.dataset.vdms;
  if(e.target.classList.contains('vsave')){
    const timezone = tr.querySelector('.vtz').value;
    const r = await fetch(`/api/vdms/${encodeURIComponent(id)}/timezone`,{method:'PUT',
        headers:{'Content-Type':'application/json'},body:JSON.stringify({timezone})});
    vmsg(r.ok?`${id} → ${timezone}`:'save failed: '+(await r.text()), !r.ok);
    if(r.ok){ await loadVdms(); await fetchData(); }
  } else if(e.target.classList.contains('vdel')){
    if(!confirm(`Remove VDMS ${id}? Its scheduled jobs will be torn down.`)) return;
    const r = await fetch(`/api/vdms/${encodeURIComponent(id)}`,{method:'DELETE'});
    vmsg(r.ok?`removed ${id}`:'remove failed: '+(await r.text()), !r.ok);
    if(r.ok){ await loadVdms(); await fetchData(); }
  }
});
loadVdms();
```

- [ ] **Step 3: Commit** (verification happens in Task 8):

```bash
git add sclera-scheduler/src/main/resources/static/scheduler.html
git commit -m "feat(scheduler): VDMS management panel in dashboard"
```

---

## Task 8: Build, run, manual end-to-end verification

**Files:** none (verification).

- [ ] **Step 1: Full build + test** — `$env:JAVA_HOME=…; cd sclera-scheduler; .\mvnw.cmd -q test` → BUILD SUCCESS.

- [ ] **Step 2: Start the app** (Docker stack or the module against a Postgres + Dapr sidecar, per the existing run setup). Confirm `http://localhost:8098/scheduler.html` loads and shows the **VDMS** panel.

- [ ] **Step 3: Manual E2E** (a PER_VDMS job must exist in the catalog — `vdmsSystemHealth` has `scope: PER_VDMS`):
  - **Add:** enter `vdms-ui-1`, pick `America/New_York`, Add → row appears `active`, `jobCount ≥ 1`; the PER_VDMS job in the main list now shows an extra instance for `vdms-ui-1`.
  - **Edit tz:** change its dropdown to `Asia/Kolkata`, Save tz → message confirms; the instance's next-fire/`CRON_TZ` reflects the new zone (check the per-VDMS drill-down or the Dapr job).
  - **Invalid:** the dropdown only offers valid zones; (optional) POST a bogus zone via curl → 400.
  - **Remove:** Remove → confirm → row flips to `removed`, instances disappear from the job's drill-down.

- [ ] **Step 4: Update the register note + commit** — append a short "DONE" line to the spec or a migration note if the project keeps one; otherwise no-op. Then:

```bash
git commit --allow-empty -m "test(scheduler): manual E2E verified for VDMS management UI"
```

---

## Self-Review

- **Spec coverage:** add VDMS → Task 4; edit timezone (state-preserving) → Tasks 2 + 5; remove → Task 6; list + timezone dropdown → Task 3; job-count column → Task 1; validated IANA dropdown → Task 3 (`validTimezone`) + Task 7 (`<select>` from `/api/vdms/timezones`); soft unverified-id warning → Task 7 hint text; coexistence/idempotency → reuse of `PerVdmsRegistrar` (Tasks 4–6); testing → per-task MockMvc/Testcontainers + manual Task 8. All spec sections mapped.
- **Placeholder scan:** none — every code step has full code; the two "adjust if `JobEntity`/`JobSchedule` exposes X differently" notes are explicit verify-against-real-signature guards, not placeholders.
- **Type consistency:** `VdmsRegistryView(vdmsId, timezone, active, jobCount)` used identically in Task 3 DTO, `toView`, and all assertions; `reregister(vdmsId, timezone)` defined in Task 2 and called in Task 5; `onVdmsActivated`/`onVdmsDeactivated` used as their verified signatures; `countByVdmsId` defined Task 1, used in `toView`.
- **Signatures verified:** `JobEntity.setScope(JobScope)`, `JobSchedule` record accessors `name()/schedule()/timezone()`, `PerVdmsRegistrar.onVdmsActivated/onVdmsDeactivated`, repo methods — all confirmed against current source on `feature/scheduler-service`. No unverified surface remains.
