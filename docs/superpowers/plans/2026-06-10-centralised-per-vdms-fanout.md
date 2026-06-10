# Centralised Per-VDMS Fan-out Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `CENTRALISED` job scope so one Dapr job, on a single schedule, fans out one `scheduler.trigger` event per active VDMS — updating every active VDMS's data per fire.

**Architecture:** A new `TriggerDispatcher` owns the publish/fan-out logic, shared by the fire callback (`JobCallbackController`) and run-now (`JobService`). `CENTRALISED` jobs register as a single Dapr job (like `GLOBAL`); on fire the dispatcher loads `vdms_registry` active rows and publishes one trigger per VDMS, each with its own `runId` + `vdmsId`. Existing consumers and result-handling are unchanged. `registerAll()` is fixed to skip `PER_VDMS`, and a startup teardown removes orphaned `{job}::{vdms}` Dapr jobs when a job flips away from `PER_VDMS`.

**Tech Stack:** Spring Boot 4 / Java 21, JUnit 5 + Mockito + AssertJ, Dapr Jobs API (alpha) + pub/sub, Postgres (`sclera_scheduler` schema, Flyway).

**Spec:** `docs/superpowers/specs/2026-06-10-centralised-per-vdms-fanout-design.md`

**Conventions for every task:**
- Work inside the `sclera-scheduler` module.
- Run tests from PowerShell with `JAVA_HOME` pointing at a JDK 21 (Amazon Corretto 21):
  ```powershell
  Set-Location "C:\Users\KNageshNayak\OneDrive - Sclera\Desktop\Work\sclera-cloud-device-asset\sclera-scheduler"
  $env:JAVA_HOME="<path-to-corretto-21>"
  .\mvnw.cmd -Dtest=<TestClass> test
  ```
- **Do NOT run `git commit` / `git push`.** This project commits manually (user rule). Each task ends at "tests green"; the user commits. A "Checkpoint" step replaces the usual commit step.

---

### Task 1: Add `CENTRALISED` to the scope enum

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobScope.java`

- [ ] **Step 1: Add the enum value**

```java
package io.sclera.scheduler.domain;

public enum JobScope { GLOBAL, PER_VDMS, CENTRALISED }
```

- [ ] **Step 2: Compile to verify it builds**

Run:
```powershell
Set-Location "C:\Users\KNageshNayak\OneDrive - Sclera\Desktop\Work\sclera-cloud-device-asset\sclera-scheduler"
.\mvnw.cmd -q -DskipTests compile
```
Expected: BUILD SUCCESS.

- [ ] **Step 3: Checkpoint** — enum value added; nothing references it yet. Hand to user to commit.

---

### Task 2: `TriggerDispatcher` — the publish/fan-out component (TDD)

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/TriggerDispatcher.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/TriggerDispatcherTest.java`

- [ ] **Step 1: Write the failing test**

Create `TriggerDispatcherTest.java`:

```java
package io.sclera.scheduler.service;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerDispatcherTest {

    @Mock JobRepository jobs;
    @Mock VdmsRegistryRepository registry;
    @Mock RunRecorder recorder;
    @Mock DaprEventPublisher publisher;

    TriggerDispatcher dispatcher() {
        TriggerDispatcher d = new TriggerDispatcher(jobs, registry, recorder, publisher);
        d.setPubsubName("pubsub");
        d.setTriggerTopic("scheduler.trigger");
        return d;
    }

    @Test
    void publishSingleRecordsFiredAndPublishesWithSameRunId() {
        when(publisher.publish(anyString(), anyString(), any()))
            .thenReturn(new PublishResult(true, "evt", null));

        boolean ok = dispatcher().publishSingle("snmpSync", null, false);

        assertThat(ok).isTrue();
        ArgumentCaptor<UUID> runId = ArgumentCaptor.forClass(UUID.class);
        verify(recorder).recordFired(eq("snmpSync"), runId.capture(), eq(false), isNull());
        verify(publisher).publish(eq("pubsub"), eq("scheduler.trigger"), argThat(p ->
            p instanceof SchedulerTriggerEvent e
                && e.jobName().equals("snmpSync")
                && e.runId().equals(runId.getValue().toString())
                && e.vdmsId() == null));
    }

    @Test
    void publishSingleReturnsFalseOnPublishFailure() {
        when(publisher.publish(anyString(), anyString(), any()))
            .thenReturn(new PublishResult(false, null, "broker-down"));

        boolean ok = dispatcher().publishSingle("snmpSync", null, false);

        assertThat(ok).isFalse();
        verify(recorder).recordFired(eq("snmpSync"), any(), eq(false), isNull());
    }

    @Test
    void fanOutPublishesOneTriggerPerActiveVdms() {
        when(registry.findByActiveTrue()).thenReturn(List.of(
            new VdmsRegistryEntity("vdms-1", "UTC", true),
            new VdmsRegistryEntity("vdms-2", "Asia/Kolkata", true)));
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e", null));

        dispatcher().fanOutCentralised("vdmsSystemHealth", false);

        verify(recorder).recordFired(eq("vdmsSystemHealth"), any(), eq(false), eq("vdms-1"));
        verify(recorder).recordFired(eq("vdmsSystemHealth"), any(), eq(false), eq("vdms-2"));
        verify(publisher, times(2)).publish(eq("pubsub"), eq("scheduler.trigger"), any());
    }

    @Test
    void fanOutWithNoActiveVdmsPublishesNothing() {
        when(registry.findByActiveTrue()).thenReturn(List.of());

        dispatcher().fanOutCentralised("vdmsSystemHealth", false);

        verifyNoInteractions(publisher);
        verifyNoInteractions(recorder);
    }

    @Test
    void fanOutContinuesAfterAPublishFailure() {
        when(registry.findByActiveTrue()).thenReturn(List.of(
            new VdmsRegistryEntity("vdms-1", "UTC", true),
            new VdmsRegistryEntity("vdms-2", "UTC", true)));
        when(publisher.publish(any(), any(), any()))
            .thenReturn(new PublishResult(false, null, "x"))
            .thenReturn(new PublishResult(true, "e", null));

        dispatcher().fanOutCentralised("vdmsSystemHealth", false);

        verify(publisher, times(2)).publish(any(), any(), any());
        verify(recorder, times(2)).recordFired(eq("vdmsSystemHealth"), any(), eq(false), any());
    }

    @Test
    void scopeOfDefaultsToGlobalForUnknownJob() {
        when(jobs.findById("nope")).thenReturn(Optional.empty());
        assertThat(dispatcher().scopeOf("nope")).isEqualTo(JobScope.GLOBAL);
    }

    @Test
    void scopeOfReturnsConfiguredScope() {
        JobEntity j = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
            "scheduler.trigger", JobState.ENABLED);
        j.setScope(JobScope.CENTRALISED);
        when(jobs.findById("vdmsSystemHealth")).thenReturn(Optional.of(j));
        assertThat(dispatcher().scopeOf("vdmsSystemHealth")).isEqualTo(JobScope.CENTRALISED);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:
```powershell
.\mvnw.cmd -Dtest=TriggerDispatcherTest test
```
Expected: COMPILATION FAILURE — `TriggerDispatcher` does not exist.

- [ ] **Step 3: Write the implementation**

Create `TriggerDispatcher.java`:

```java
package io.sclera.scheduler.service;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.domain.JobEntity;
import io.sclera.scheduler.domain.JobRepository;
import io.sclera.scheduler.domain.JobScope;
import io.sclera.scheduler.domain.VdmsRegistryEntity;
import io.sclera.scheduler.domain.VdmsRegistryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a fired job into one or more scheduler.trigger events. GLOBAL and explicit
 * per-VDMS fires publish a single event; CENTRALISED fires fan out one event per ACTIVE
 * VDMS. Shared by the fire callback and run-now so fan-out lives in exactly one place.
 */
@Service
public class TriggerDispatcher {

    private static final Logger log = LoggerFactory.getLogger(TriggerDispatcher.class);

    private final JobRepository jobs;
    private final VdmsRegistryRepository registry;
    private final RunRecorder recorder;
    private final DaprEventPublisher publisher;

    @Value("${scheduler.pubsub-name}") private String pubsubName;
    @Value("${scheduler.trigger-topic}") private String triggerTopic;

    public TriggerDispatcher(JobRepository jobs, VdmsRegistryRepository registry,
                             RunRecorder recorder, DaprEventPublisher publisher) {
        this.jobs = jobs;
        this.registry = registry;
        this.recorder = recorder;
        this.publisher = publisher;
    }

    void setPubsubName(String v) { this.pubsubName = v; }
    void setTriggerTopic(String v) { this.triggerTopic = v; }

    /** Scope of a job; unknown names default to GLOBAL (single fire). */
    public JobScope scopeOf(String jobName) {
        return jobs.findById(jobName).map(JobEntity::getScope).orElse(JobScope.GLOBAL);
    }

    /** Record a FIRED run and publish one trigger. Returns true on publish success. */
    public boolean publishSingle(String jobName, String vdmsId, boolean manual) {
        UUID runId = UUID.randomUUID();
        recorder.recordFired(jobName, runId, manual, vdmsId);
        PublishResult r = publisher.publish(pubsubName, triggerTopic,
            new SchedulerTriggerEvent(jobName, runId.toString(), vdmsId, System.currentTimeMillis()));
        if (!r.success()) {
            log.error("Trigger publish failed job={} vdmsId={} runId={} error={}",
                jobName, vdmsId, runId, r.error());
        }
        return r.success();
    }

    /**
     * Fan out one trigger per ACTIVE VDMS. Best-effort: a failed publish is logged and the
     * remaining VDMS are still triggered; stuck FIRED rows are reclaimed by housekeeping.
     */
    public void fanOutCentralised(String jobName, boolean manual) {
        List<VdmsRegistryEntity> active = registry.findByActiveTrue();
        int ok = 0, failed = 0;
        for (VdmsRegistryEntity v : active) {
            if (publishSingle(jobName, v.getVdmsId(), manual)) ok++; else failed++;
        }
        log.info("Centralised fan-out job={} activeVdms={} published={} failed={}",
            jobName, active.size(), ok, failed);
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run:
```powershell
.\mvnw.cmd -Dtest=TriggerDispatcherTest test
```
Expected: PASS (7 tests).

- [ ] **Step 5: Checkpoint** — dispatcher exists and is tested; not yet wired into callers. Hand to user to commit.

---

### Task 3: Refactor `JobCallbackController` to delegate to the dispatcher

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/JobCallbackController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/JobCallbackControllerTest.java` (rewrite)

- [ ] **Step 1: Rewrite the test**

Replace the entire body of `JobCallbackControllerTest.java` with:

```java
package io.sclera.scheduler.web;

import io.sclera.scheduler.domain.JobScope;
import io.sclera.scheduler.service.TriggerDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobCallbackControllerTest {

    @Mock TriggerDispatcher dispatcher;

    JobCallbackController controller() {
        return new JobCallbackController(dispatcher);
    }

    @Test
    void globalFirePublishesSingleAndReturns200() {
        when(dispatcher.scopeOf("snmpSync")).thenReturn(JobScope.GLOBAL);
        when(dispatcher.publishSingle("snmpSync", null, false)).thenReturn(true);

        ResponseEntity<Void> response = controller().onJobFired("snmpSync");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(dispatcher).publishSingle("snmpSync", null, false);
    }

    @Test
    void globalFireReturns500WhenPublishFails() {
        when(dispatcher.scopeOf("snmpSync")).thenReturn(JobScope.GLOBAL);
        when(dispatcher.publishSingle("snmpSync", null, false)).thenReturn(false);

        ResponseEntity<Void> response = controller().onJobFired("snmpSync");

        // 500 signals the Dapr Scheduler to retry the fire.
        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void centralisedFireFansOutAndReturns200() {
        when(dispatcher.scopeOf("vdmsSystemHealth")).thenReturn(JobScope.CENTRALISED);

        ResponseEntity<Void> response = controller().onJobFired("vdmsSystemHealth");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(dispatcher).fanOutCentralised("vdmsSystemHealth", false);
        verify(dispatcher, never()).publishSingle(any(), any(), anyBoolean());
    }

    @Test
    void barePerVdmsFireIsANoOpAndReturns200() {
        when(dispatcher.scopeOf("vdmsSystemHealth")).thenReturn(JobScope.PER_VDMS);

        ResponseEntity<Void> response = controller().onJobFired("vdmsSystemHealth");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(dispatcher, never()).publishSingle(any(), any(), anyBoolean());
        verify(dispatcher, never()).fanOutCentralised(any(), anyBoolean());
    }

    @Test
    void explicitPerVdmsInstanceFirePublishesSingleWithVdmsId() {
        when(dispatcher.publishSingle("vdmsSystemHealth", "vdms-7", false)).thenReturn(true);

        ResponseEntity<Void> response = controller().onJobFired("vdmsSystemHealth::vdms-7");

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(dispatcher).publishSingle("vdmsSystemHealth", "vdms-7", false);
        verify(dispatcher, never()).scopeOf(any());
    }

    @Test
    void explicitInstanceFireReturns500WhenPublishFails() {
        when(dispatcher.publishSingle("vdmsSystemHealth", "vdms-7", false)).thenReturn(false);

        ResponseEntity<Void> response = controller().onJobFired("vdmsSystemHealth::vdms-7");

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void oneShotInstanceFireIsRecordedAsManual() {
        when(dispatcher.publishSingle("vdmsSystemHealth", "vdms-7", true)).thenReturn(true);

        controller().onJobFired("vdmsSystemHealth::vdms-7::once-abc");

        verify(dispatcher).publishSingle("vdmsSystemHealth", "vdms-7", true);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:
```powershell
.\mvnw.cmd -Dtest=JobCallbackControllerTest test
```
Expected: COMPILATION FAILURE — `JobCallbackController(TriggerDispatcher)` constructor does not exist.

- [ ] **Step 3: Rewrite the controller**

Replace the entire body of `JobCallbackController.java` with:

```java
package io.sclera.scheduler.web;

import io.sclera.scheduler.service.TriggerDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dapr Scheduler invokes POST /job/{name} when a registered job fires. This controller is a
 * thin adapter over {@link TriggerDispatcher}: it parses the Dapr job-name convention and maps
 * the dispatch outcome to an HTTP status.
 *
 * <p>Job-name forms:
 * <ul>
 *   <li>{@code {jobName}} — a bare job. Scope decides the action: GLOBAL → single trigger;
 *       CENTRALISED → fan out one trigger per active VDMS; PER_VDMS → no-op (its real fires
 *       arrive as {@code {jobName}::{vdmsId}}).</li>
 *   <li>{@code {jobName}::{vdmsId}} — a per-VDMS instance fire → single trigger for that VDMS.</li>
 *   <li>{@code {jobName}::{vdmsId}::once-{id}} — a one-shot delayed run → single manual trigger.</li>
 * </ul>
 *
 * <p>For single triggers a publish failure returns 500 so Dapr retries the fire. Centralised
 * fan-out is best-effort (always 200); failed publishes leave FIRED rows that the housekeeping
 * reaper reclaims, avoiding a whole-job refire that would duplicate the VDMS that succeeded.
 */
@RestController
public class JobCallbackController {

    private static final Logger log = LoggerFactory.getLogger(JobCallbackController.class);

    private final TriggerDispatcher dispatcher;

    public JobCallbackController(TriggerDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @PostMapping("/job/{name}")
    public ResponseEntity<Void> onJobFired(@PathVariable("name") String name) {
        String[] parts = name.split("::");
        String jobName = parts[0];

        if (parts.length >= 2) {
            String vdmsId = parts[1];
            boolean oneShot = parts.length >= 3 && parts[2].startsWith("once-");
            return status(dispatcher.publishSingle(jobName, vdmsId, oneShot));
        }

        return switch (dispatcher.scopeOf(jobName)) {
            case CENTRALISED -> {
                dispatcher.fanOutCentralised(jobName, false);
                yield ResponseEntity.ok().build();
            }
            case PER_VDMS -> {
                // A bare PER_VDMS job should never be registered; its instances fire via
                // {jobName}::{vdmsId}. Ignore defensively so a stray fire is not double-counted.
                log.warn("Bare PER_VDMS fire ignored job={}", jobName);
                yield ResponseEntity.ok().build();
            }
            case GLOBAL -> status(dispatcher.publishSingle(jobName, null, false));
        };
    }

    private static ResponseEntity<Void> status(boolean published) {
        return published ? ResponseEntity.ok().build() : ResponseEntity.internalServerError().build();
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run:
```powershell
.\mvnw.cmd -Dtest=JobCallbackControllerTest test
```
Expected: PASS (7 tests).

- [ ] **Step 5: Checkpoint** — callback now delegates to the dispatcher. Hand to user to commit.

---

### Task 4: Refactor `JobService` — delegate run-now + skip `PER_VDMS` in `registerAll`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/JobService.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/JobServiceTest.java`

- [ ] **Step 1: Update the test (mocks, factory, registerAll + runNow tests)**

In `JobServiceTest.java`:

(a) Replace the publisher/recorder mocks with a dispatcher mock — change:
```java
    @Mock SchedulerClient scheduler;
    @Mock DaprEventPublisher publisher;
    @Mock RunRecorder recorder;
```
to:
```java
    @Mock SchedulerClient scheduler;
    @Mock TriggerDispatcher dispatcher;
```

(b) Replace the `service()` factory — change:
```java
    JobService service() {
        JobService s = new JobService(jobs, instances, registry, scheduler, publisher, recorder);
        s.setPubsubName("pubsub");
        s.setTriggerTopic("scheduler.trigger");
        return s;
    }
```
to:
```java
    JobService service() {
        return new JobService(jobs, instances, registry, scheduler, dispatcher);
    }
```

(c) Add a `registerAll` skip test next to `registerAllSchedulesOnlyEnabledJobs`:
```java
    @Test
    void registerAllSkipsPerVdmsJobs() {
        JobEntity global = new JobEntity("g", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED);
        JobEntity perVdms = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
            "scheduler.trigger", JobState.ENABLED);
        perVdms.setScope(JobScope.PER_VDMS);
        when(jobs.findByState(JobState.ENABLED)).thenReturn(List.of(global, perVdms));

        service().registerAll();

        verify(scheduler).schedule(new JobSchedule("g", "@every 1m"));
        verify(scheduler, never()).schedule(new JobSchedule("vdmsSystemHealth", "0 0 0 * * *"));
    }
```

(d) Replace the test `runNowPublishesTriggerAndRecordsManualRunWithSameRunId` (lines 84–101) with:
```java
    @Test
    void runNowDelegatesToDispatcherForGlobalJob() {
        JobEntity job = new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED);
        when(jobs.findById("a")).thenReturn(Optional.of(job));

        service().runNow("a");

        verify(dispatcher).publishSingle("a", null, true);
    }

    @Test
    void runNowFansOutForCentralisedJob() {
        JobEntity job = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
            "scheduler.trigger", JobState.ENABLED);
        job.setScope(JobScope.CENTRALISED);
        when(jobs.findById("vdmsSystemHealth")).thenReturn(Optional.of(job));

        service().runNow("vdmsSystemHealth");

        verify(dispatcher).fanOutCentralised("vdmsSystemHealth", true);
    }
```

(e) Remove the now-unused imports `io.sclera.dapr.DaprEventPublisher`, `io.sclera.dapr.PublishResult`, and `io.sclera.dapr.events.SchedulerTriggerEvent`. Add `import io.sclera.scheduler.service.TriggerDispatcher;` is unnecessary (same package). `ArgumentCaptor` / `UUID` imports may now be unused — remove them if the compiler flags them as unused is not enforced; leaving an unused import will not fail the build, so this is optional cleanup.

- [ ] **Step 2: Run the test to verify it fails**

Run:
```powershell
.\mvnw.cmd -Dtest=JobServiceTest test
```
Expected: COMPILATION FAILURE — `JobService` still has the old 6-arg constructor and `setPubsubName`/`setTriggerTopic`.

- [ ] **Step 3: Update `JobService`**

In `JobService.java`:

(a) Replace the fields and constructor. Change:
```java
    private final JobRepository jobs;
    private final JobInstanceRepository instances;
    private final VdmsRegistryRepository registry;
    private final SchedulerClient scheduler;
    private final DaprEventPublisher publisher;
    private final RunRecorder recorder;

    @Value("${scheduler.pubsub-name}") private String pubsubName;
    @Value("${scheduler.trigger-topic}") private String triggerTopic;

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

    void setPubsubName(String v) { this.pubsubName = v; }
    void setTriggerTopic(String v) { this.triggerTopic = v; }
```
to:
```java
    private final JobRepository jobs;
    private final JobInstanceRepository instances;
    private final VdmsRegistryRepository registry;
    private final SchedulerClient scheduler;
    private final TriggerDispatcher dispatcher;

    public JobService(JobRepository jobs, JobInstanceRepository instances,
                      VdmsRegistryRepository registry, SchedulerClient scheduler,
                      TriggerDispatcher dispatcher) {
        this.jobs = jobs;
        this.instances = instances;
        this.registry = registry;
        this.scheduler = scheduler;
        this.dispatcher = dispatcher;
    }
```

(b) Replace `registerAll()` to skip `PER_VDMS`:
```java
    /** Called once at startup. Registers all ENABLED GLOBAL/CENTRALISED jobs as single Dapr
     *  jobs. PER_VDMS jobs are owned by {@link PerVdmsRegistrar} and skipped here. */
    public void registerAll() {
        int ok = 0, failed = 0;
        for (JobEntity job : jobs.findByState(JobState.ENABLED)) {
            if (job.getScope() == JobScope.PER_VDMS) continue;
            try {
                scheduler.schedule(new JobSchedule(job.getName(), job.getSchedule()));
                ok++;
            } catch (Exception e) {
                failed++;
                log.error("Failed to register job={} schedule={} error={}",
                    job.getName(), job.getSchedule(), e.getMessage());
            }
        }
        log.info("Registered enabled jobs with Dapr Scheduler: ok={} failed={}", ok, failed);
    }
```

(c) Replace `runNow(...)` to delegate:
```java
    /**
     * Fire immediately, bypassing the schedule. Records manual run(s) via the dispatcher.
     * CENTRALISED jobs fan out to all active VDMS; others fire a single trigger. Best-effort:
     * a publish failure is logged by the dispatcher and the FIRED row is reaped by housekeeping.
     */
    public void runNow(String name) {
        JobEntity job = require(name);
        if (job.getScope() == JobScope.CENTRALISED) {
            dispatcher.fanOutCentralised(name, true);
        } else {
            dispatcher.publishSingle(name, null, true);
        }
    }
```

(d) Remove the now-unused imports: `io.sclera.dapr.DaprEventPublisher`, `io.sclera.dapr.events.SchedulerTriggerEvent`, and `org.springframework.beans.factory.annotation.Value`. Keep `java.time.Instant` and `java.util.UUID` (still used by the instance methods).

- [ ] **Step 4: Run the test to verify it passes**

Run:
```powershell
.\mvnw.cmd -Dtest=JobServiceTest test
```
Expected: PASS (all JobService tests, including the two new ones).

- [ ] **Step 5: Checkpoint** — run-now delegates; registerAll skips PER_VDMS. Hand to user to commit.

---

### Task 5: Orphan teardown in `PerVdmsRegistrar`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/PerVdmsRegistrar.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/PerVdmsRegistrarTest.java`

- [ ] **Step 1: Write the failing tests**

Add to `PerVdmsRegistrarTest.java`. First add this import near the other static imports:
```java
import static org.assertj.core.api.Assertions.assertThat;
```
Then add these tests:
```java
    @Test
    void tearDownStaleInstancesDeletesDaprJobsAndDisablesInstances() {
        JobEntity flipped = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        flipped.setScope(JobScope.CENTRALISED);
        when(jobs.findAll()).thenReturn(List.of(flipped));
        JobInstanceEntity inst = new JobInstanceEntity(
                "vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        when(instances.findByJobName("vdmsSystemHealth")).thenReturn(List.of(inst));

        registrar().tearDownStaleInstances();

        verify(scheduler).delete("vdmsSystemHealth::vdms-1");
        assertThat(inst.getState()).isEqualTo(JobInstanceState.DISABLED);
    }

    @Test
    void tearDownStaleInstancesSkipsPerVdmsJobs() {
        when(jobs.findAll()).thenReturn(List.of(perVdmsJob()));

        registrar().tearDownStaleInstances();

        verify(instances, never()).findByJobName(any());
        verifyNoInteractions(scheduler);
    }

    @Test
    void tearDownStaleInstancesSkipsAlreadyDisabledInstances() {
        JobEntity flipped = new JobEntity("vdmsSystemHealth", "0 0 0 * * *", "device-asset",
                "scheduler.trigger", JobState.ENABLED);
        flipped.setScope(JobScope.CENTRALISED);
        when(jobs.findAll()).thenReturn(List.of(flipped));
        JobInstanceEntity inst = new JobInstanceEntity(
                "vdmsSystemHealth", "vdms-1", "vdmsSystemHealth::vdms-1");
        inst.setState(JobInstanceState.DISABLED);
        when(instances.findByJobName("vdmsSystemHealth")).thenReturn(List.of(inst));

        registrar().tearDownStaleInstances();

        verifyNoInteractions(scheduler);
    }
```

- [ ] **Step 2: Run the tests to verify they fail**

Run:
```powershell
.\mvnw.cmd -Dtest=PerVdmsRegistrarTest test
```
Expected: COMPILATION FAILURE — `tearDownStaleInstances()` does not exist.

- [ ] **Step 3: Implement the method**

Add to `PerVdmsRegistrar.java` (after `reconcileAll()`):
```java
    /**
     * Tear down instances of jobs that are no longer PER_VDMS (e.g. flipped to CENTRALISED or
     * GLOBAL). Deletes each orphaned {jobName}::{vdmsId} Dapr job and DISABLEs its instance row
     * so the old per-VDMS jobs stop firing alongside the job's new scope. Idempotent and
     * self-healing across restarts; per-instance failures are isolated.
     */
    @Transactional
    public void tearDownStaleInstances() {
        for (JobEntity job : jobs.findAll()) {
            if (job.getScope() == JobScope.PER_VDMS) continue;
            for (JobInstanceEntity inst : instances.findByJobName(job.getName())) {
                if (inst.getState() == JobInstanceState.DISABLED) continue;
                try {
                    scheduler.delete(inst.getDaprJobName());
                } catch (Exception e) {
                    log.error("Failed to delete stale Dapr job {}: {}",
                            inst.getDaprJobName(), e.getMessage());
                }
                inst.setState(JobInstanceState.DISABLED);
            }
        }
    }
```

- [ ] **Step 4: Run the tests to verify they pass**

Run:
```powershell
.\mvnw.cmd -Dtest=PerVdmsRegistrarTest test
```
Expected: PASS (existing tests + 3 new).

- [ ] **Step 5: Checkpoint** — teardown implemented; not yet called at startup. Hand to user to commit.

---

### Task 6: Wire teardown into `CatalogStartupRunner`

**Files:**
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/CatalogStartupRunner.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/CatalogStartupRunnerTest.java`

- [ ] **Step 1: Update the tests to expect teardown after reconcile, before registerAll**

In `CatalogStartupRunnerTest.java`, replace the `reconcilesThenRegistersInOrder` body's InOrder block:
```java
        InOrder order = inOrder(reconciler, jobService);
        order.verify(reconciler).reconcile(props.jobs());
        order.verify(jobService).registerAll();
```
with:
```java
        InOrder order = inOrder(reconciler, registrar, jobService);
        order.verify(reconciler).reconcile(props.jobs());
        order.verify(registrar).tearDownStaleInstances();
        order.verify(jobService).registerAll();
```

And in `runReconcilesThenRegistersGlobalThenSyncsVdmsThenReconcilesPerVdms`, replace:
```java
        InOrder order = inOrder(reconciler, jobService, vdmsDirectory, registry, registrar);
        order.verify(reconciler).reconcile(any());
        order.verify(jobService).registerAll();
```
with:
```java
        InOrder order = inOrder(reconciler, registrar, jobService, vdmsDirectory, registry);
        order.verify(reconciler).reconcile(any());
        order.verify(registrar).tearDownStaleInstances();
        order.verify(jobService).registerAll();
```
(Leave the remaining `order.verify(...)` lines for `vdmsDirectory`, `registry`, and `registrar.reconcileAll()` as they are.)

- [ ] **Step 2: Run the test to verify it fails**

Run:
```powershell
.\mvnw.cmd -Dtest=CatalogStartupRunnerTest test
```
Expected: FAIL — `tearDownStaleInstances()` is never called (verification fails).

- [ ] **Step 3: Add the call in the runner**

In `CatalogStartupRunner.run(...)`, change:
```java
        reconciler.reconcile(catalog.jobs());
        jobService.registerAll();
```
to:
```java
        reconciler.reconcile(catalog.jobs());
        registrar.tearDownStaleInstances();
        jobService.registerAll();
```

- [ ] **Step 4: Run the test to verify it passes**

Run:
```powershell
.\mvnw.cmd -Dtest=CatalogStartupRunnerTest test
```
Expected: PASS.

- [ ] **Step 5: Checkpoint** — teardown runs at startup before registration. Hand to user to commit.

---

### Task 7: Flip `vdmsSystemHealth` to `CENTRALISED`

**Files:**
- Modify: `sclera-scheduler/src/main/resources/jobs.yaml`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/JobCatalogReconcilerTest.java`

- [ ] **Step 1: Add a reconciler test for the CENTRALISED scope**

Open `JobCatalogReconcilerTest.java` and add a test that a catalog entry with `scope = CENTRALISED` is persisted on the saved `JobEntity`. Match the existing test style in that file; the assertion is that after `reconcile(...)` of an entry whose `scope()` is `"CENTRALISED"`, the saved `JobEntity.getScope()` equals `JobScope.CENTRALISED`. Use the existing entry constructor `new JobCatalogProperties.Entry(name, schedule, owner, triggerTopic, scope)` and the file's existing mock of `JobRepository` (capture the `save` argument):
```java
    @Test
    void reconcileSetsCentralisedScope() {
        var entry = new JobCatalogProperties.Entry(
            "vdmsSystemHealth", "0 0 0 * * *", "device-asset", "scheduler.trigger", "CENTRALISED");
        when(jobs.findById("vdmsSystemHealth")).thenReturn(java.util.Optional.empty());

        reconciler.reconcile(java.util.List.of(entry));

        org.mockito.ArgumentCaptor<JobEntity> saved =
            org.mockito.ArgumentCaptor.forClass(JobEntity.class);
        verify(jobs).save(saved.capture());
        org.assertj.core.api.Assertions.assertThat(saved.getValue().getScope())
            .isEqualTo(JobScope.CENTRALISED);
    }
```
> If `JobCatalogReconcilerTest` constructs the reconciler differently (e.g. a `reconciler()` factory or `@Mock JobRepository jobs`), adapt the `jobs`/`reconciler` references to the names already used in that file. Read the file first and follow its existing pattern.

- [ ] **Step 2: Run the test to verify it passes (enum already supports CENTRALISED from Task 1)**

Run:
```powershell
.\mvnw.cmd -Dtest=JobCatalogReconcilerTest test
```
Expected: PASS. (This guards that `JobScope.valueOf("CENTRALISED")` works end-to-end through the reconciler.)

- [ ] **Step 3: Flip the job in `jobs.yaml`**

Change the `vdmsSystemHealth` entry:
```yaml
      - name: vdmsSystemHealth
        schedule: "0 0 0 * * *"
        owner: device-asset
        scope: PER_VDMS
```
to:
```yaml
      - name: vdmsSystemHealth
        schedule: "0 0 0 * * *"
        owner: device-asset
        scope: CENTRALISED
```

- [ ] **Step 4: Checkpoint** — config flipped. Hand to user to commit.

---

### Task 8: Full module verification

**Files:** none (verification only).

- [ ] **Step 1: Run the whole scheduler test suite**

Run:
```powershell
Set-Location "C:\Users\KNageshNayak\OneDrive - Sclera\Desktop\Work\sclera-cloud-device-asset\sclera-scheduler"
.\mvnw.cmd test
```
Expected: BUILD SUCCESS, all tests green. Pay attention to `SchedulerFlowComponentTest` — it wires the real Spring context and exercises the fire → trigger → result flow; it must still pass with the new `TriggerDispatcher` bean and refactored controller/service.

- [ ] **Step 2: If `SchedulerFlowComponentTest` (or any context test) fails**

Read the failure. The most likely cause is a Spring wiring expectation that referenced the old `JobCallbackController(RunRecorder, DaprEventPublisher)` or `JobService` 6-arg constructor, or an assertion on a single-publish path for a job that is now `CENTRALISED`. Update the test's expectations to match the new flow (delegation through `TriggerDispatcher`; `vdmsSystemHealth` now fans out per active VDMS). Do not weaken assertions beyond what the new design requires.

- [ ] **Step 3: Final checkpoint** — full suite green. Hand to user to commit the full change set.

---

## Notes for the implementer

- **Why best-effort for fan-out (not 500-on-failure):** returning 500 makes Dapr refire the whole job, re-triggering every VDMS — including ones that already succeeded — because a refire mints fresh `runId`s that `runId` idempotency can't dedupe. Single (GLOBAL / explicit per-VDMS) fires keep 500-on-failure because a refire there re-triggers only that one unit.
- **No DB migration:** `job.scope` is already a string-valued enum column; `CENTRALISED` is a new permitted value.
- **`PER_VDMS` is untouched:** `PerVdmsRegistrar.reconcileAll`, instance control ops, and timezone-aware `{job}::{vdms}` registration all still work for any job that stays `PER_VDMS`.
- **Manual commits:** per project rule, never run `git commit`/`git push`; stop at green tests and let the user commit.
