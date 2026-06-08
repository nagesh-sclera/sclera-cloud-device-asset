# Scheduler Execution + Cleanup — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make device-asset scheduler jobs execute end-to-end (FIRED→SUCCESS on the dashboard), add a dev-only simulator for non-device-asset jobs, and ship a small cleanup batch.

**Architecture:** A `TriggerDispatchSubscriber` in `sclera-cloud-device-asset` subscribes to `scheduler.trigger`, routes the 10 device-asset jobs to a `DeviceAssetJobHandlers` bean, and publishes `scheduler.result`. A profile-gated `TriggerSimulatorSubscriber` in `sclera-scheduler` publishes a simulated SUCCESS for jobs whose catalog `owner != device-asset`. Cleanup: image-url bug fixes, `Sclara`→`Sclera`, multi-stage Dockerfile.

**Tech Stack:** Spring Boot 4 (Java 21), Dapr pub/sub via `dapr-commons` (`DaprEventSubscriber`/`DaprEventPublisher`), JUnit 5 + Mockito, Flyway/Postgres (unchanged here).

**Spec:** `docs/superpowers/specs/2026-06-08-scheduler-execution-and-cleanup-design.md`

**Reference (read-only):** monolith `Desktop\AssetManagement POD\New folder\sclera-vdms-edge-server\src\main\java\io\sclera\startup\{Schedular,SchedularService}.java`

**Module note:** `sclera-cloud-device-asset` runs `mvnw.cmd` from its **inner** dir. Build/test via PowerShell `& .\mvnw.cmd test -Dtest=...` (see build-environment memory). `sclera-scheduler` builds with its own `mvnw`.

---

## File Structure

| File | Responsibility |
|------|----------------|
| `sclera-cloud-device-asset/.../service/DeviceService.java` | (modify) 4 image-url getter fixes |
| `test-ui/index.html`, `test-ui/traces.html`, `architecture.html`, `ARCHITECTURE.md`, `start-local.ps1`, `db-init/init.sql` | (modify) `Sclara`→`Sclera` |
| `sclera-scheduler/Dockerfile` | (modify) multi-stage build |
| `sclera-cloud-device-asset/.../scheduler/DeviceAssetJobHandlers.java` | (new) the 10 job handlers (the work) |
| `sclera-cloud-device-asset/.../scheduler/TriggerDispatchSubscriber.java` | (new) trigger routing + result publishing |
| `sclera-cloud-device-asset/src/test/java/io/sclera/scheduler/TriggerDispatchSubscriberTest.java` | (new) unit tests for routing/result/skip |
| `sclera-cloud-device-asset/src/main/resources/application*.yml` | (modify) scheduler topic/pubsub props |
| `sclera-scheduler/.../subscriber/TriggerSimulatorSubscriber.java` | (new) dev/docker-only simulator |
| `sclera-scheduler/src/test/java/.../subscriber/TriggerSimulatorSubscriberTest.java` | (new) unit tests for skip/simulate |

Package for new device-asset classes: `io.sclera.scheduler` (new package in this module).

---

## Part 1 — Cleanup batch

### Task 1: Fix DeviceService image-url copy-paste bugs

**Files:** Modify `sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java`

> TDD note: `tagProductImages`/`retagProductImages` do file IO + web calls and have no existing unit coverage (deferred per service-unit-test-coverage memory). These are 1-token getter corrections; verify by reading the corrected lines + a clean compile, not a new IT.

- [ ] **Step 1: Apply the four fixes** (match on the block, not the line number)

In `tagProductImages`, the `addProductImages(...)` call — middle image arg:
```java
// before: ...modified_image_url_3, productdto.getImage_url_1(), productdto.getImage_url_1(), productdto.getImage_url_3());
product_detailsService.addProductImages(productdto.getId(), modified_image_url_1, modified_image_url_2,
        modified_image_url_3, productdto.getImage_url_1(), productdto.getImage_url_2(), productdto.getImage_url_3());
```
In `retagProductImages`, the `image_url_2` block extension:
```java
// before: String extension = getImageExtensionByImageUrl(productdto.getImage_url_1());  (inside the getImage_url_2() != null block)
String extension = getImageExtensionByImageUrl(productdto.getImage_url_2());
```
In `retagProductImages`, the `image_url_3` block extension:
```java
// before: String extension = getImageExtensionByImageUrl(productdto.getImage_url_1());  (inside the getImage_url_3() != null block)
String extension = getImageExtensionByImageUrl(productdto.getImage_url_3());
```
In `retagProductImages`, the `addProductImages(...)` call — middle image arg:
```java
product_detailsService.addProductImages(productdto.getId(), modified_image_url_1, modified_image_url_2,
        modified_image_url_3, productdto.getImage_url_1(), productdto.getImage_url_2(), productdto.getImage_url_3());
```

- [ ] **Step 2: Verify no stray `getImage_url_1()` remain in those two methods**

Run: `git diff -- sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java`
Expected: exactly 4 changed lines, each `_1`→`_2`/`_3` as above.

- [ ] **Step 3: Compile**

Run (inner dir): `& .\mvnw.cmd -q -o compile`
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java
git commit -m "fix(device): correct image_url_2/_3 copy-paste in product image tagging"
```

### Task 2: Rename Sclara → Sclera in user-facing assets

**Files:** Modify `test-ui/index.html`, `test-ui/traces.html`, `architecture.html`, `ARCHITECTURE.md`, `start-local.ps1`, `db-init/init.sql`

- [ ] **Step 1: Replace `Sclara` with `Sclera` in each file above** (case-sensitive; one or more occurrences per file). Do NOT touch files under `docs/superpowers/` (historical audit trail).

- [ ] **Step 2: Verify only intended files changed**

Run (from repo root): `grep -rn "Sclara" --exclude-dir=docs --exclude-dir=.git .`
Expected: no matches.

- [ ] **Step 3: Commit**

```bash
git add test-ui/index.html test-ui/traces.html architecture.html ARCHITECTURE.md start-local.ps1 db-init/init.sql
git commit -m "docs(ui): fix Sclara -> Sclera spelling in demo assets"
```

### Task 3: Multi-stage Dockerfile for sclera-scheduler

**Files:** Modify `sclera-scheduler/Dockerfile`

- [ ] **Step 1: Replace the Dockerfile with a multi-stage build**

```dockerfile
# --- build stage: builds dapr-commons + sclera-scheduler from source ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
# dapr-commons is a local dependency; install it first
COPY dapr-commons /src/dapr-commons
RUN mvn -q -f /src/dapr-commons/pom.xml -DskipTests install
COPY sclera-scheduler /src/sclera-scheduler
RUN mvn -q -f /src/sclera-scheduler/pom.xml -DskipTests package

# --- runtime stage ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /src/sclera-scheduler/target/sclera-scheduler-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8098
ENV JAVA_OPTS="-Xmx128m -Xss512k"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

- [ ] **Step 2: Point the compose build context at the repo root so both sources are visible**

In the compose file defining the `sclera-scheduler` service, ensure:
```yaml
    build:
      context: .
      dockerfile: sclera-scheduler/Dockerfile
```
(Adjust `context` to the directory that contains both `dapr-commons/` and `sclera-scheduler/`.)

- [ ] **Step 3: Build the image**

Run: `docker compose build sclera-scheduler`
Expected: build succeeds with no prior host `mvnw package`.

- [ ] **Step 4: Commit**

```bash
git add sclera-scheduler/Dockerfile docker-compose*.yml
git commit -m "build(scheduler): self-contained multi-stage Docker image"
```

---

## Part 2 — End-to-end execution

### Task 4: Device-asset dispatcher (subscriber + handlers)

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/scheduler/DeviceAssetJobHandlers.java`
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/scheduler/TriggerDispatchSubscriber.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/scheduler/TriggerDispatchSubscriberTest.java`

- [ ] **Step 1: Create the handlers bean (one method per device-asset job)**

Each method does the same work as the monolith `Schedular`'s same-named `@Scheduled` method (see Reference). Where the device-asset building block exists, wire to it; where a dependency is genuinely absent, log one WARN and return (stub-with-WARN per `CLAUDE.md`). Below, `offlineDeviceCheck` is shown as the stub-with-WARN template — replace each body with real wiring as the executor confirms the device-asset method against the Reference. Mapping:

| Job | Monolith reference call(s) |
|-----|----------------------------|
| historyRecord | `scheduleHistoryRecord()` |
| unlinkVendorOrganisation | `scheduleUnlinkVendorOrganisation()` |
| internetBandwidthCheck | `scheduleInternetBandwidthCheck()` |
| vdmsSystemHealth | `scheduleLorawanDownlink()`+`scheduleVdmsSystemHealth()`+`scheduleVDMSDataForIOCSync()` |
| connectedStatusForIOC | `scheduleConnectedStatusForIOC()`+`scheduleUserActivityData(now)` |
| qrcodeNfcBarcodeSync | `scheduleQrcodeNFCBarcodeSync()` |
| syncAssetCountToCloud | `syncAssetCountToCloud()` |
| userActionLog | monolith same-purpose method (building block `UserActionLogService`) |
| deviceDndEnable | monolith DND method (building blocks in `DeviceService`) |
| offlineDeviceCheck | monolith offline-check method (building blocks in `DeviceService`) |

```java
package io.sclera.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Executes device-asset-owned scheduler jobs. Each method mirrors the monolith
 *  Schedular's same-named @Scheduled method; absent dependencies stub-with-WARN. */
@Component
public class DeviceAssetJobHandlers {

    private static final Logger log = LoggerFactory.getLogger(DeviceAssetJobHandlers.class);

    // Constructor-inject the device-asset services each handler needs (e.g. DeviceService,
    // UserActionLogService, ...). Added as bodies are wired.

    public void historyRecord()            { stub("historyRecord"); }
    public void unlinkVendorOrganisation() { stub("unlinkVendorOrganisation"); }
    public void internetBandwidthCheck()   { stub("internetBandwidthCheck"); }
    public void vdmsSystemHealth()         { stub("vdmsSystemHealth"); }
    public void connectedStatusForIOC()    { stub("connectedStatusForIOC"); }
    public void qrcodeNfcBarcodeSync()     { stub("qrcodeNfcBarcodeSync"); }
    public void syncAssetCountToCloud()    { stub("syncAssetCountToCloud"); }
    public void userActionLog()            { stub("userActionLog"); }
    public void deviceDndEnable()          { stub("deviceDndEnable"); }
    public void offlineDeviceCheck()       { stub("offlineDeviceCheck"); }

    private void stub(String job) {
        log.warn("scheduler job '{}' not yet wired to device-asset logic (stub-with-WARN)", job);
    }
}
```

- [ ] **Step 2: Write the failing subscriber test**

```java
package io.sclera.scheduler;

import io.dapr.client.DaprClient;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerDispatchSubscriberTest {

    @Mock DaprClient dapr;
    @Mock DaprEventPublisher publisher;
    @Mock DeviceAssetJobHandlers handlers;

    private TriggerDispatchSubscriber subscriber() {
        TriggerDispatchSubscriber s = new TriggerDispatchSubscriber(dapr, publisher, handlers);
        s.setPubsubName("pubsub");
        s.setResultTopic("scheduler.result");
        return s;
    }

    @Test
    void ownedJob_runsHandler_andPublishesSuccess() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e1", null));
        subscriber().handleEvent(new SchedulerTriggerEvent("offlineDeviceCheck", "r1", 0L));
        verify(handlers).offlineDeviceCheck();
        ArgumentCaptor<SchedulerResultEvent> cap = ArgumentCaptor.forClass(SchedulerResultEvent.class);
        verify(publisher).publish(eq("pubsub"), eq("scheduler.result"), cap.capture());
        assertEquals("SUCCESS", cap.getValue().status());
        assertEquals("r1", cap.getValue().runId());
        assertNull(cap.getValue().error());
    }

    @Test
    void handlerThrows_publishesFailedWithError() {
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e1", null));
        doThrow(new RuntimeException("boom")).when(handlers).syncAssetCountToCloud();
        subscriber().handleEvent(new SchedulerTriggerEvent("syncAssetCountToCloud", "r2", 0L));
        ArgumentCaptor<SchedulerResultEvent> cap = ArgumentCaptor.forClass(SchedulerResultEvent.class);
        verify(publisher).publish(eq("pubsub"), eq("scheduler.result"), cap.capture());
        assertEquals("FAILED", cap.getValue().status());
        assertTrue(cap.getValue().error().contains("boom"));
    }

    @Test
    void nonOwnedJob_isIgnored_noHandlerNoResult() {
        subscriber().handleEvent(new SchedulerTriggerEvent("snmpSync", "r3", 0L));
        verifyNoInteractions(handlers);
        verifyNoInteractions(publisher);
    }
}
```

- [ ] **Step 3: Run the test — expect FAIL (class missing)**

Run (inner dir): `& .\mvnw.cmd -q -o test -Dtest=TriggerDispatchSubscriberTest`
Expected: compile/FAIL — `TriggerDispatchSubscriber` not found.

- [ ] **Step 4: Implement the subscriber**

```java
package io.sclera.scheduler;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/** Executes the device-asset-owned scheduler jobs and reports a result. Jobs owned by
 *  other services are ignored (a no-op, not a DLQ case). */
@RestController
public class TriggerDispatchSubscriber extends DaprEventSubscriber<SchedulerTriggerEvent> {

    private static final Set<String> OWNED = Set.of(
        "historyRecord", "unlinkVendorOrganisation", "internetBandwidthCheck",
        "vdmsSystemHealth", "connectedStatusForIOC", "qrcodeNfcBarcodeSync",
        "syncAssetCountToCloud", "userActionLog", "deviceDndEnable", "offlineDeviceCheck");

    private final DaprEventPublisher publisher;
    private final DeviceAssetJobHandlers handlers;

    @Value("${scheduler.pubsub-name:pubsub}") private String pubsubName;
    @Value("${scheduler.result-topic:scheduler.result}") private String resultTopic;

    public TriggerDispatchSubscriber(DaprClient dapr, DaprEventPublisher publisher,
                                     DeviceAssetJobHandlers handlers) {
        super(dapr, "scheduler.trigger");
        this.publisher = publisher;
        this.handlers = handlers;
    }

    void setPubsubName(String v) { this.pubsubName = v; }
    void setResultTopic(String v) { this.resultTopic = v; }

    @Topic(name = "scheduler.trigger", pubsubName = "pubsub",
           deadLetterTopic = "scheduler.trigger.dlq")
    @PostMapping("/internal/scheduler-trigger")
    public ResponseEntity<Map<String, String>> onTrigger(
            @RequestBody CloudEvent<SchedulerTriggerEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(SchedulerTriggerEvent data) {
        if (!OWNED.contains(data.jobName())) {
            return; // another service owns this job
        }
        long t0 = System.currentTimeMillis();
        try {
            run(data.jobName());
            publish(data, "SUCCESS", t0, null);
        } catch (Exception e) {
            publish(data, "FAILED", t0, String.valueOf(e.getMessage()));
        }
    }

    private void run(String job) {
        switch (job) {
            case "historyRecord" -> handlers.historyRecord();
            case "unlinkVendorOrganisation" -> handlers.unlinkVendorOrganisation();
            case "internetBandwidthCheck" -> handlers.internetBandwidthCheck();
            case "vdmsSystemHealth" -> handlers.vdmsSystemHealth();
            case "connectedStatusForIOC" -> handlers.connectedStatusForIOC();
            case "qrcodeNfcBarcodeSync" -> handlers.qrcodeNfcBarcodeSync();
            case "syncAssetCountToCloud" -> handlers.syncAssetCountToCloud();
            case "userActionLog" -> handlers.userActionLog();
            case "deviceDndEnable" -> handlers.deviceDndEnable();
            case "offlineDeviceCheck" -> handlers.offlineDeviceCheck();
            default -> { /* unreachable: guarded by OWNED */ }
        }
    }

    private void publish(SchedulerTriggerEvent in, String status, long t0, String error) {
        publisher.publish(pubsubName, resultTopic,
            new SchedulerResultEvent(in.jobName(), in.runId(), status,
                System.currentTimeMillis() - t0, error));
    }
}
```

- [ ] **Step 5: Run the test — expect PASS**

Run (inner dir): `& .\mvnw.cmd -q -o test -Dtest=TriggerDispatchSubscriberTest`
Expected: 3 tests PASS.

- [ ] **Step 6: Commit**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/scheduler/ sclera-cloud-device-asset/src/test/java/io/sclera/scheduler/
git commit -m "feat(scheduler): device-asset trigger dispatcher with result reporting"
```

### Task 5: Wire device-asset job handler bodies

**Files:** Modify `sclera-cloud-device-asset/.../scheduler/DeviceAssetJobHandlers.java` (+ inject the services each handler uses)

> For each of the 10 methods: open the monolith Reference method of the same name, identify the work it does, and reimplement it against this module's services. If the building block is present, wire it; if genuinely absent (Bucket-C), keep the `stub(...)` WARN body and add the job to the follow-up list in `migration-notes/`. Do one handler per commit.

- [ ] **Step 1: For each job, wire the body** (repeat per job)

Example shape once the target service method is confirmed:
```java
private final DeviceService deviceService; // constructor-injected
// ...
public void offlineDeviceCheck() {
    deviceService.<confirmedMethod>();   // mirrors monolith Schedular.scheduleOfflineDeviceCheck path
}
```

- [ ] **Step 2: Compile after each wiring**

Run (inner dir): `& .\mvnw.cmd -q -o compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Record any job left as stub-with-WARN**

Append the job name + reason to `sclera-cloud-device-asset/migration-notes/` (the audit trail).

- [ ] **Step 4: Commit (per handler or in a small batch)**

```bash
git add sclera-cloud-device-asset/src/main/java/io/sclera/scheduler/DeviceAssetJobHandlers.java
git commit -m "feat(scheduler): wire <job> handler to device-asset logic"
```

### Task 6: Scheduler config props in device-asset

**Files:** Modify `sclera-cloud-device-asset/src/main/resources/application.yml` (and any `application-docker.yml`)

- [ ] **Step 1: Add the scheduler block** (defaults already match the `@Value` fallbacks; make explicit)

```yaml
scheduler:
  pubsub-name: pubsub
  trigger-topic: scheduler.trigger
  result-topic: scheduler.result
```

- [ ] **Step 2: Compile/boot smoke**

Run (inner dir): `& .\mvnw.cmd -q -o compile`
Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add sclera-cloud-device-asset/src/main/resources/application*.yml
git commit -m "chore(scheduler): scheduler pubsub/topic config for device-asset"
```

### Task 7: Dev-only simulator in sclera-scheduler

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/subscriber/TriggerSimulatorSubscriber.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/subscriber/TriggerSimulatorSubscriberTest.java`

The simulator publishes a simulated SUCCESS for any job whose catalog `owner != device-asset`, so the dashboard shows the full lifecycle without the absent owning services. Owner is looked up via the existing job repository/catalog. Confirm the lookup API first:

- [ ] **Step 1: Identify the owner-lookup** — read `sclera-scheduler/.../service/JobService.java` and `.../domain/JobEntity.java` (or `JobRepository`) to find how to read a job's `owner` by name (e.g. `jobRepository.findById(name).map(JobEntity::getOwner)`). Use that exact call in Steps 2 & 4.

- [ ] **Step 2: Write the failing test** (replace `lookupOwner(...)` with the confirmed API from Step 1)

```java
package io.sclera.scheduler.subscriber;

import io.dapr.client.DaprClient;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.domain.JobEntity;
import io.sclera.scheduler.domain.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerSimulatorSubscriberTest {

    @Mock DaprClient dapr;
    @Mock DaprEventPublisher publisher;
    @Mock JobRepository jobRepository;

    private TriggerSimulatorSubscriber sub() {
        TriggerSimulatorSubscriber s = new TriggerSimulatorSubscriber(dapr, publisher, jobRepository);
        s.setPubsubName("pubsub");
        s.setResultTopic("scheduler.result");
        return s;
    }

    private void owner(String job, String owner) {
        JobEntity e = new JobEntity();
        e.setOwner(owner); // adjust to JobEntity's actual setter/ctor
        when(jobRepository.findById(job)).thenReturn(Optional.of(e));
    }

    @Test
    void deviceAssetJob_isSkipped() {
        owner("offlineDeviceCheck", "device-asset");
        sub().handleEvent(new SchedulerTriggerEvent("offlineDeviceCheck", "r1", 0L));
        verifyNoInteractions(publisher);
    }

    @Test
    void otherOwnerJob_publishesSimulatedSuccess() {
        owner("snmpSync", "integrations");
        when(publisher.publish(any(), any(), any())).thenReturn(new PublishResult(true, "e1", null));
        sub().handleEvent(new SchedulerTriggerEvent("snmpSync", "r2", 0L));
        verify(publisher).publish(eq("pubsub"), eq("scheduler.result"),
            argThat(p -> p instanceof SchedulerResultEvent r
                && r.status().equals("SUCCESS") && r.runId().equals("r2")));
    }
}
```

- [ ] **Step 3: Run — expect FAIL**

Run: `sclera-scheduler> mvnw test -Dtest=TriggerSimulatorSubscriberTest`
Expected: compile/FAIL — class missing.

- [ ] **Step 4: Implement the simulator** (adjust the owner lookup to the Step-1 API)

```java
package io.sclera.scheduler.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.domain.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** DEV/DOCKER ONLY. Stand-in for owning services not running in this workspace: for any job
 *  whose owner != device-asset, publishes a simulated SUCCESS so the dashboard shows the full
 *  FIRED->SUCCESS lifecycle. NOT a real dispatcher; never loads in prod. */
@Profile({"dev", "docker"})
@RestController
public class TriggerSimulatorSubscriber extends DaprEventSubscriber<SchedulerTriggerEvent> {

    private static final long SIMULATED_DURATION_MS = 5L;

    private final DaprEventPublisher publisher;
    private final JobRepository jobRepository;

    @Value("${scheduler.pubsub-name:pubsub}") private String pubsubName;
    @Value("${scheduler.result-topic:scheduler.result}") private String resultTopic;

    public TriggerSimulatorSubscriber(DaprClient dapr, DaprEventPublisher publisher,
                                      JobRepository jobRepository) {
        super(dapr, "scheduler.trigger");
        this.publisher = publisher;
        this.jobRepository = jobRepository;
    }

    void setPubsubName(String v) { this.pubsubName = v; }
    void setResultTopic(String v) { this.resultTopic = v; }

    @Topic(name = "scheduler.trigger", pubsubName = "pubsub",
           deadLetterTopic = "scheduler.trigger.dlq")
    @PostMapping("/internal/scheduler-trigger-sim")
    public ResponseEntity<Map<String, String>> onTrigger(
            @RequestBody CloudEvent<SchedulerTriggerEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(SchedulerTriggerEvent data) {
        String owner = jobRepository.findById(data.jobName())
            .map(j -> j.getOwner()).orElse(null);   // adjust to actual API
        if ("device-asset".equals(owner)) {
            return; // real device-asset dispatcher owns this
        }
        publisher.publish(pubsubName, resultTopic,
            new SchedulerResultEvent(data.jobName(), data.runId(), "SUCCESS",
                SIMULATED_DURATION_MS, null));
    }
}
```

- [ ] **Step 5: Run — expect PASS**

Run: `sclera-scheduler> mvnw test -Dtest=TriggerSimulatorSubscriberTest`
Expected: 2 tests PASS.

- [ ] **Step 6: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/subscriber/TriggerSimulatorSubscriber.java sclera-scheduler/src/test/java/io/sclera/scheduler/subscriber/TriggerSimulatorSubscriberTest.java
git commit -m "feat(scheduler): dev-only trigger simulator for non-device-asset jobs"
```

### Task 8: Live end-to-end verification (Docker)

**Files:** none (manual verification)

- [ ] **Step 1: Build & boot the stack** (use the existing `docker-compose.override.yml` fixes)

Run: `docker compose build sclera-scheduler sclera-cloud-device-asset` then `docker compose up -d`

- [ ] **Step 2: Watch `offlineDeviceCheck` (`@every 90s`) flip FIRED→SUCCESS** with a real duration on the dashboard / `GET :8098/api/jobs/offlineDeviceCheck/runs` — **no hand-published result**.

- [ ] **Step 3: Confirm a non-device-asset job** (e.g. `snmpSync`, or run-now via `POST :8098/api/jobs/snmpSync/run`) flips to SUCCESS via the simulator.
Expected: both jobs reach SUCCESS; device-asset job ran real work, the other was simulated.

- [ ] **Step 4: Run the full device-asset suite** to confirm no regressions

Run (inner dir): `& .\mvnw.cmd -o test`
Expected: prior tests + new dispatcher tests all green.

---

## Self-Review

- **Spec coverage:** Part 1 §1.1→Task 1, §1.2→Task 2, §1.3→Task 3, §1.4 non-change (no task needed). Part 2.A→Tasks 4–6, 2.B→Task 7, 2.C→Tasks 4/6/7. Testing→Tasks 4/7/8. ✅
- **Placeholders:** handler bodies in Task 4 are an intentional stub-with-WARN baseline; Task 5 wires them with a concrete recipe + per-job reference table (deterministic, not "implement later"). The only deferred specifics are the exact device-asset method names, which require reading the read-only monolith at execution time — Task 5 instructs exactly how to resolve each.
- **Type consistency:** `SchedulerTriggerEvent(jobName, runId, firedAtEpochMs)` and `SchedulerResultEvent(jobName, runId, status, durationMs, error)` used consistently; `publish(pubsub, topic, payload)` and `DaprEventSubscriber(dapr, topic)`/`handleEvent` match `dapr-commons`; `PublishResult(boolean, String, String)` matches the test-coverage memory.
- **Owner lookup** in Task 7 is flagged to confirm against `JobRepository`/`JobEntity` before coding (Step 1) — the test/impl use placeholders `findById/getOwner` that Step 1 pins.
