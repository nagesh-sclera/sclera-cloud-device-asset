# Dapr Scheduler Service Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a central `sclera-scheduler` microservice that registers all of the monolith's cron jobs with the Dapr Scheduler control plane (Jobs API), fires them exactly-once cluster-wide as pub/sub `scheduler.trigger` events, records execution results, and exposes a monitoring/control UI.

**Architecture:** A new Spring Boot 4 / Java 21 service owns a `sclera_scheduler` Postgres schema (Flyway) holding a job catalog and run history. On startup it reconciles a version-controlled `jobs.yaml` into the DB and registers each enabled job with its Dapr sidecar's Jobs API. When the Scheduler fires a job, Dapr calls `POST /job/{name}`; the service records a run and publishes a `scheduler.trigger` event. The owning service (initially the monolith dispatcher) runs the work and publishes `scheduler.result`, which the service records. A REST API + static page provide the dashboard and controls (pause/resume/disable/run-now). All alpha Jobs-API calls are isolated in one `SchedulerClient` class.

**Tech Stack:** Spring Boot 4.0.6, Java 21, `dapr-sdk` 1.12.0, `dapr-commons` (shared pub/sub harness), Postgres + Flyway, Spring `RestClient`, Testcontainers (Postgres), JUnit 5.

---

## Context for the implementer

You have zero context for this codebase. Read these before starting:

- **Spec:** `docs/superpowers/specs/2026-06-02-dapr-scheduler-service-design.md` — the approved design.
- **Shared harness:** `dapr-commons/src/main/java/io/sclera/dapr/` contains:
  - `DaprEventSubscriber<T>` — abstract subscriber base. You extend it; it handles idempotency (`statestore-idempotency`, keyed on CloudEvent id), and returns `ResponseEntity<Map<String,String>>` with status `SUCCESS`/`RETRY`/`DROP`. Override `handleEvent(T)` for business logic.
  - `DaprEventPublisher` — `publish(pubsubName, topic, payload)` returns `PublishResult(boolean success, String eventId, String error)`.
  - `events/` — typed event record DTOs.
- **Template service:** `sclera-audit/` is a working skeleton service. Mirror its `pom.xml`, `Application.java`, `config/DaprClientConfig.java`, `application.yml`, and subscriber pattern. **Differences for us:** we add Postgres + Flyway + JPA (audit has none).
- **App-id registry:** `dapr/APP_IDS.md`. Ports 8090–8097 are taken; **use 8098** for `sclera-scheduler`.
- **Dapr components:** `dapr/components/local/pubsub.yaml` (Redis dev pub/sub, component name `pubsub`). Idempotency store component name is `statestore-idempotency`.

### Build & run on this machine (from memory: `build-environment`)
- No global `mvn`. Use the module's `./mvnw` wrapper.
- Java is Corretto 21. Each service builds independently (there is **no** root aggregator pom).
- `dapr-commons` must be installed to the local Maven repo before dependent services build:
  `cd dapr-commons && ./mvnw -q install -DskipTests`

### Conventions (from `dapr/APP_IDS.md`)
- App-id = `spring.application.name` = kebab-case, `sclera-` prefix.
- Topics = `<domain>.<event-past-tense>`. Our topics: `scheduler.trigger`, `scheduler.result`, plus `.dlq` companions.

### Important constraint — the monolith dispatcher is cross-repo
The `@Scheduled` jobs live in `io.sclera.startup.Schedular` inside the **read-only `sclera-vdms-edge-server` monolith repo**, which is **not in this working tree**. Tasks 1–12 build and fully test the `sclera-scheduler` service *standalone* in this repo (Task 12 uses a test-only stub subscriber to prove the end-to-end flow). **Task 13 is the cross-repo dispatcher** — it lands in the monolith repo and is marked accordingly. Do not look for `Schedular.java` in this tree.

---

## File Structure

New module `sclera-scheduler/` (sibling of `sclera-audit/`):

```
sclera-scheduler/
  pom.xml
  mvnw, mvnw.cmd, .mvn/                      (copied from sclera-audit)
  Dockerfile                                 (copied from sclera-audit, port 8098)
  CLAUDE.md
  src/main/java/io/sclera/scheduler/
    Application.java
    config/
      DaprClientConfig.java                  DaprClient bean
      DaprProperties.java                    dapr.http-port binding
    catalog/
      JobCatalogProperties.java              @ConfigurationProperties("scheduler.catalog")
      JobCatalogReconciler.java              yaml -> DB on startup
    client/
      SchedulerClient.java                   ONLY alpha Jobs-API touch-point (RestClient)
      JobSchedule.java                        record(name, schedule, data)
    domain/
      JobEntity.java                          @Entity job
      JobRunEntity.java                       @Entity job_run
      JobState.java                           enum ENABLED|PAUSED|DISABLED
      RunStatus.java                          enum FIRED|SUCCESS|FAILED
      JobRepository.java
      JobRunRepository.java
    service/
      JobService.java                         register-on-startup + pause/resume/disable/run-now
      RunRecorder.java                        insert/update job_run rows
      HousekeepingService.java                orphan reaper + history retention
    web/
      JobCallbackController.java              POST /job/{name}  (Dapr Scheduler -> us)
      SchedulerApiController.java             /api/jobs ... (UI)
      dto/JobView.java, RunView.java
    subscriber/
      ResultSubscriber.java                  scheduler.result -> update run
  src/main/resources/
    application.yml
    jobs.yaml                                 the job catalog (source of truth)
    db/migration/V1__scheduler_schema.sql
    static/scheduler.html                     dashboard page
  src/test/java/io/sclera/scheduler/
    AbstractPostgresTest.java                 Testcontainers base
    domain/JobRepositoryTest.java
    catalog/JobCatalogReconcilerTest.java
    client/SchedulerClientTest.java
    service/JobServiceTest.java
    service/HousekeepingServiceTest.java
    web/JobCallbackControllerTest.java
    web/SchedulerApiControllerTest.java
    subscriber/ResultSubscriberTest.java
    component/SchedulerFlowComponentTest.java (L3, @Disabled unless dapr present)
```

Also touched:
- `dapr-commons/src/main/java/io/sclera/dapr/events/SchedulerTriggerEvent.java` (new)
- `dapr-commons/src/main/java/io/sclera/dapr/events/SchedulerResultEvent.java` (new)
- `dapr/components/local/pubsub.yaml` and `dapr/components/k8s/pubsub.yaml` (DLQ note)
- `dapr/APP_IDS.md` (register app-id + topics)
- `docker-compose.yml` (scheduler app + sidecar)

---

## Task 1: Add shared event DTOs to dapr-commons

**Files:**
- Create: `dapr-commons/src/main/java/io/sclera/dapr/events/SchedulerTriggerEvent.java`
- Create: `dapr-commons/src/main/java/io/sclera/dapr/events/SchedulerResultEvent.java`
- Test: `dapr-commons/src/test/java/io/sclera/dapr/events/SchedulerEventsTest.java`

- [ ] **Step 1: Write the failing test**

```java
package io.sclera.dapr.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SchedulerEventsTest {

    @Test
    void triggerEventCarriesJobNameAndRunId() {
        SchedulerTriggerEvent e = new SchedulerTriggerEvent("snmpSync", "run-1", 123L);
        assertEquals("snmpSync", e.jobName());
        assertEquals("run-1", e.runId());
        assertEquals(123L, e.firedAtEpochMs());
    }

    @Test
    void resultEventCarriesOutcome() {
        SchedulerResultEvent e =
            new SchedulerResultEvent("snmpSync", "run-1", "SUCCESS", 250L, null);
        assertEquals("run-1", e.runId());
        assertEquals("SUCCESS", e.status());
        assertEquals(250L, e.durationMs());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd dapr-commons && ./mvnw -q -Dtest=SchedulerEventsTest test`
Expected: FAIL — `SchedulerTriggerEvent` / `SchedulerResultEvent` cannot be resolved (compilation error).

- [ ] **Step 3: Write the records**

`SchedulerTriggerEvent.java`:
```java
package io.sclera.dapr.events;

/** Published when the Dapr Scheduler fires a job. runId is the idempotency key. */
public record SchedulerTriggerEvent(
    String jobName,
    String runId,
    long firedAtEpochMs) {}
```

`SchedulerResultEvent.java`:
```java
package io.sclera.dapr.events;

/** Published by the job's owning service after running the work. */
public record SchedulerResultEvent(
    String jobName,
    String runId,
    String status,      // "SUCCESS" | "FAILED"
    long durationMs,
    String error) {}    // null when SUCCESS
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd dapr-commons && ./mvnw -q -Dtest=SchedulerEventsTest test`
Expected: PASS (2 tests).

- [ ] **Step 5: Install dapr-commons to local repo (dependents need it)**

Run: `cd dapr-commons && ./mvnw -q install -DskipTests`
Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add dapr-commons/src/main/java/io/sclera/dapr/events/SchedulerTriggerEvent.java \
        dapr-commons/src/main/java/io/sclera/dapr/events/SchedulerResultEvent.java \
        dapr-commons/src/test/java/io/sclera/dapr/events/SchedulerEventsTest.java
git commit -m "feat(dapr-commons): add scheduler trigger/result event DTOs"
```

---

## Task 2: Scaffold the sclera-scheduler module

**Files:**
- Create: `sclera-scheduler/pom.xml`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/Application.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/config/DaprClientConfig.java`
- Create: `sclera-scheduler/src/main/resources/application.yml`
- Create: `sclera-scheduler/CLAUDE.md`
- Copy: `sclera-scheduler/mvnw`, `mvnw.cmd`, `.mvn/`, `Dockerfile` from `sclera-audit/`

- [ ] **Step 1: Copy the Maven wrapper and Dockerfile from the template service**

```bash
cp -r sclera-audit/.mvn sclera-scheduler/.mvn
cp sclera-audit/mvnw sclera-audit/mvnw.cmd sclera-scheduler/
cp sclera-audit/Dockerfile sclera-scheduler/Dockerfile
```
Then edit `sclera-scheduler/Dockerfile`: change any `EXPOSE 8090` to `EXPOSE 8098` and any `sclera-audit` artifact references to `sclera-scheduler`.

- [ ] **Step 2: Write `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.6</version>
    <relativePath/>
  </parent>
  <groupId>io.sclera</groupId>
  <artifactId>sclera-scheduler</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <properties>
    <java.version>21</java.version>
    <dapr.sdk.version>1.12.0</dapr.sdk.version>
  </properties>
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>testcontainers-bom</artifactId>
        <version>1.20.4</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>io.dapr</groupId>
      <artifactId>dapr-sdk</artifactId>
      <version>${dapr.sdk.version}</version>
    </dependency>
    <dependency>
      <groupId>io.dapr</groupId>
      <artifactId>dapr-sdk-springboot</artifactId>
      <version>${dapr.sdk.version}</version>
    </dependency>
    <dependency>
      <groupId>io.sclera</groupId>
      <artifactId>dapr-commons</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-webmvc-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-testcontainers</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>postgresql</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 3: Write `Application.java`**

```java
package io.sclera.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- [ ] **Step 4: Write `config/DaprClientConfig.java`**

```java
package io.sclera.scheduler.config;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaprClientConfig {
    @Bean(destroyMethod = "close")
    public DaprClient daprClient() {
        return new DaprClientBuilder().build();
    }
}
```

- [ ] **Step 5: Write `application.yml`**

```yaml
server:
  port: 8098

spring:
  application:
    name: sclera-scheduler
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/sclera}
    username: ${DB_USER:sclera}
    password: ${DB_PASS:sclera}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: sclera_scheduler
    open-in-view: false
  flyway:
    enabled: true
    schemas: sclera_scheduler
    default-schema: sclera_scheduler
    create-schemas: true
    locations: classpath:db/migration

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

dapr:
  http-port: ${DAPR_HTTP_PORT:3500}

scheduler:
  pubsub-name: pubsub
  trigger-topic: scheduler.trigger
  result-topic: scheduler.result
  # orphan FIRED runs older than this are swept to FAILED
  orphan-timeout-seconds: 900
  # job_run history older than this is pruned
  history-retention-days: 30

logging:
  level:
    io.sclera.scheduler: INFO
    io.dapr: INFO
```

- [ ] **Step 6: Write `CLAUDE.md`**

```markdown
# CLAUDE.md — sclera-scheduler

## Project identity
Central scheduling service. Spring Boot 4.0.6, Java 21. Registers cron jobs with the
Dapr Scheduler control plane (Jobs API) and fires them as `scheduler.trigger` pub/sub
events. Owns the `sclera_scheduler` Postgres schema and the monitoring/control UI.

## Rules
- All alpha Dapr Jobs-API calls go through `client/SchedulerClient` only.
- `jobs.yaml` is the source of truth for the job catalog. DB holds runtime state
  (ENABLED/PAUSED/DISABLED) and run history.
- No business logic lives here — jobs fire events; owning services do the work.

## See also
- Spec: `docs/superpowers/specs/2026-06-02-dapr-scheduler-service-design.md`
- Plan: `docs/superpowers/plans/2026-06-02-dapr-scheduler-service.md`
```

- [ ] **Step 7: Verify the module compiles**

Run: `cd sclera-scheduler && ./mvnw -q compile`
Expected: `BUILD SUCCESS` (dapr-commons must already be installed — Task 1 Step 5).

- [ ] **Step 8: Commit**

```bash
git add sclera-scheduler/pom.xml sclera-scheduler/Dockerfile sclera-scheduler/.mvn \
        sclera-scheduler/mvnw sclera-scheduler/mvnw.cmd sclera-scheduler/CLAUDE.md \
        sclera-scheduler/src/main/java/io/sclera/scheduler/Application.java \
        sclera-scheduler/src/main/java/io/sclera/scheduler/config/DaprClientConfig.java \
        sclera-scheduler/src/main/resources/application.yml
git commit -m "feat(scheduler): scaffold sclera-scheduler module"
```

---

## Task 3: Postgres schema + JPA entities + repositories

**Files:**
- Create: `sclera-scheduler/src/main/resources/db/migration/V1__scheduler_schema.sql`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobState.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/RunStatus.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobEntity.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobRunEntity.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobRepository.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/domain/JobRunRepository.java`
- Create: `sclera-scheduler/src/test/java/io/sclera/scheduler/AbstractPostgresTest.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobRepositoryTest.java`

- [ ] **Step 1: Write the Flyway migration**

`V1__scheduler_schema.sql`:
```sql
CREATE TABLE job (
    name           TEXT PRIMARY KEY,
    schedule       TEXT NOT NULL,
    owner          TEXT NOT NULL,
    trigger_topic  TEXT NOT NULL DEFAULT 'scheduler.trigger',
    state          TEXT NOT NULL DEFAULT 'ENABLED',
    last_run_id    UUID,
    next_fire_at   TIMESTAMPTZ,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE job_run (
    run_id       UUID PRIMARY KEY,
    job_name     TEXT NOT NULL REFERENCES job(name),
    status       TEXT NOT NULL,
    manual       BOOLEAN NOT NULL DEFAULT FALSE,
    fired_at     TIMESTAMPTZ NOT NULL,
    finished_at  TIMESTAMPTZ,
    duration_ms  BIGINT,
    error        TEXT
);

CREATE INDEX idx_job_run_job_fired ON job_run (job_name, fired_at DESC);
CREATE INDEX idx_job_run_status ON job_run (status);
```

- [ ] **Step 2: Write the enums**

`JobState.java`:
```java
package io.sclera.scheduler.domain;

public enum JobState { ENABLED, PAUSED, DISABLED }
```

`RunStatus.java`:
```java
package io.sclera.scheduler.domain;

public enum RunStatus { FIRED, SUCCESS, FAILED }
```

- [ ] **Step 3: Write `JobEntity.java`**

```java
package io.sclera.scheduler.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job")
public class JobEntity {

    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "schedule", nullable = false)
    private String schedule;

    @Column(name = "owner", nullable = false)
    private String owner;

    @Column(name = "trigger_topic", nullable = false)
    private String triggerTopic = "scheduler.trigger";

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private JobState state = JobState.ENABLED;

    @Column(name = "last_run_id")
    private UUID lastRunId;

    @Column(name = "next_fire_at")
    private Instant nextFireAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected JobEntity() {}

    public JobEntity(String name, String schedule, String owner,
                     String triggerTopic, JobState state) {
        this.name = name;
        this.schedule = schedule;
        this.owner = owner;
        this.triggerTopic = triggerTopic;
        this.state = state;
    }

    @PreUpdate void touch() { this.updatedAt = Instant.now(); }

    public String getName() { return name; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String s) { this.schedule = s; }
    public String getOwner() { return owner; }
    public void setOwner(String o) { this.owner = o; }
    public String getTriggerTopic() { return triggerTopic; }
    public void setTriggerTopic(String t) { this.triggerTopic = t; }
    public JobState getState() { return state; }
    public void setState(JobState s) { this.state = s; }
    public UUID getLastRunId() { return lastRunId; }
    public void setLastRunId(UUID id) { this.lastRunId = id; }
    public Instant getNextFireAt() { return nextFireAt; }
    public void setNextFireAt(Instant t) { this.nextFireAt = t; }
}
```

- [ ] **Step 4: Write `JobRunEntity.java`**

```java
package io.sclera.scheduler.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "job_run")
public class JobRunEntity {

    @Id
    @Column(name = "run_id")
    private UUID runId;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RunStatus status;

    @Column(name = "manual", nullable = false)
    private boolean manual;

    @Column(name = "fired_at", nullable = false)
    private Instant firedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error")
    private String error;

    protected JobRunEntity() {}

    public JobRunEntity(UUID runId, String jobName, RunStatus status,
                        boolean manual, Instant firedAt) {
        this.runId = runId;
        this.jobName = jobName;
        this.status = status;
        this.manual = manual;
        this.firedAt = firedAt;
    }

    public UUID getRunId() { return runId; }
    public String getJobName() { return jobName; }
    public RunStatus getStatus() { return status; }
    public void setStatus(RunStatus s) { this.status = s; }
    public boolean isManual() { return manual; }
    public Instant getFiredAt() { return firedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant t) { this.finishedAt = t; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long d) { this.durationMs = d; }
    public String getError() { return error; }
    public void setError(String e) { this.error = e; }
}
```

- [ ] **Step 5: Write the repositories**

`JobRepository.java`:
```java
package io.sclera.scheduler.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, String> {
    List<JobEntity> findByState(JobState state);
}
```

`JobRunRepository.java`:
```java
package io.sclera.scheduler.domain;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface JobRunRepository extends JpaRepository<JobRunEntity, java.util.UUID> {

    List<JobRunEntity> findByJobNameOrderByFiredAtDesc(String jobName, Limit limit);

    List<JobRunEntity> findByStatusAndFiredAtBefore(RunStatus status, Instant before);

    long deleteByFiredAtBefore(Instant before);
}
```

- [ ] **Step 6: Write the Testcontainers base class**

`AbstractPostgresTest.java`:
```java
package io.sclera.scheduler;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractPostgresTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine");

    // Flyway runs against the container; schema sclera_scheduler is auto-created.
    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.jpa.properties.hibernate.default_schema", () -> "sclera_scheduler");
    }
}
```

- [ ] **Step 7: Write the failing repository test**

`JobRepositoryTest.java`:
```java
package io.sclera.scheduler.domain;

import io.sclera.scheduler.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Limit;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JobRepositoryTest extends AbstractPostgresTest {

    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @Test
    void savesJobAndFindsByState() {
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED));
        jobs.save(new JobEntity("paused", "@every 1m", "device-asset",
                "scheduler.trigger", JobState.PAUSED));

        List<JobEntity> enabled = jobs.findByState(JobState.ENABLED);
        assertThat(enabled).extracting(JobEntity::getName).containsExactly("snmpSync");
    }

    @Test
    void runHistoryReturnsNewestFirst() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        runs.save(new JobRunEntity(UUID.randomUUID(), "j", RunStatus.SUCCESS, false,
                Instant.parse("2026-06-01T00:00:00Z")));
        runs.save(new JobRunEntity(UUID.randomUUID(), "j", RunStatus.FIRED, false,
                Instant.parse("2026-06-02T00:00:00Z")));

        List<JobRunEntity> history = runs.findByJobNameOrderByFiredAtDesc("j", Limit.of(50));
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getStatus()).isEqualTo(RunStatus.FIRED);
    }
}
```

- [ ] **Step 8: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=JobRepositoryTest test`
Expected: PASS (2 tests). Flyway applies `V1`, Testcontainers starts Postgres. (Requires Docker running.)

- [ ] **Step 9: Commit**

```bash
git add sclera-scheduler/src/main/resources/db/migration/V1__scheduler_schema.sql \
        sclera-scheduler/src/main/java/io/sclera/scheduler/domain/ \
        sclera-scheduler/src/test/java/io/sclera/scheduler/AbstractPostgresTest.java \
        sclera-scheduler/src/test/java/io/sclera/scheduler/domain/JobRepositoryTest.java
git commit -m "feat(scheduler): add postgres schema, jpa entities, repositories"
```

---

## Task 4: SchedulerClient — the Dapr Jobs API wrapper

> This is the **only** class that touches the alpha Jobs API. It calls the local Dapr
> sidecar's HTTP Jobs endpoints. **Verify the exact contract against the pinned Dapr
> runtime** (see Step 6) — the API is `v1.0-alpha1` and may shift between versions.

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/client/JobSchedule.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/client/SchedulerClient.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/client/SchedulerClientTest.java`

- [ ] **Step 1: Write `JobSchedule.java`**

```java
package io.sclera.scheduler.client;

/** A job registration request. schedule is a cron ("0 0 */3 * * *") or "@every 90s". */
public record JobSchedule(String name, String schedule) {}
```

- [ ] **Step 2: Write the failing test**

Uses an embedded MockWebServer-style stub via Spring's `MockRestServiceServer` is awkward
with `RestClient`; instead we inject the base URL and assert the HTTP calls against a tiny
local stub server (`com.sun.net.httpserver.HttpServer`, JDK built-in, no new dependency).

`SchedulerClientTest.java`:
```java
package io.sclera.scheduler.client;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerClientTest {

    static HttpServer server;
    static int port;
    static final List<String> requests = new ArrayList<>();
    static final List<String> bodies = new ArrayList<>();

    @BeforeAll
    static void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", ex -> {
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            requests.add(ex.getRequestMethod() + " " + ex.getRequestURI().getPath());
            bodies.add(body);
            ex.sendResponseHeaders(204, -1);
            ex.close();
        });
        server.start();
    }

    @AfterAll
    static void stop() { server.stop(0); }

    @BeforeEach
    void clear() { requests.clear(); bodies.clear(); }

    SchedulerClient client() {
        return new SchedulerClient("http://localhost:" + port);
    }

    @Test
    void scheduleJobPostsToJobsApiWithSchedule() {
        client().schedule(new JobSchedule("snmpSync", "0 0 */3 * * *"));

        assertThat(requests).containsExactly("POST /v1.0-alpha1/jobs/snmpSync");
        assertThat(bodies.get(0)).contains("\"schedule\":\"0 0 */3 * * *\"");
    }

    @Test
    void deleteJobCallsDelete() {
        client().delete("snmpSync");
        assertThat(requests).containsExactly("DELETE /v1.0-alpha1/jobs/snmpSync");
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=SchedulerClientTest test`
Expected: FAIL — `SchedulerClient` cannot be resolved.

- [ ] **Step 4: Write `SchedulerClient.java`**

```java
package io.sclera.scheduler.client;

import io.sclera.scheduler.config.DaprProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * The ONLY touch-point for the Dapr Jobs API (alpha). Registers/deletes jobs on the
 * local Dapr sidecar. The Scheduler control plane persists them in etcd and calls back
 * the app at POST /job/{name} when each fires.
 */
@Component
public class SchedulerClient {

    private static final Logger log = LoggerFactory.getLogger(SchedulerClient.class);
    private final RestClient http;

    // Spring injects this constructor in production.
    public SchedulerClient(DaprProperties props) {
        this("http://localhost:" + props.httpPort());
    }

    // Test constructor — explicit base URL.
    public SchedulerClient(String daprBaseUrl) {
        this.http = RestClient.builder().baseUrl(daprBaseUrl).build();
    }

    /** Register or replace a job. Idempotent: re-posting the same name replaces it. */
    public void schedule(JobSchedule job) {
        // Jobs API body: { "schedule": "<cron|@every>", "data": { "jobName": "<name>" } }
        // data is echoed back to POST /job/{name}; we key everything on the path name.
        Map<String, Object> body = Map.of(
            "schedule", job.schedule(),
            "data", Map.of("jobName", job.name())
        );
        http.post()
            .uri("/v1.0-alpha1/jobs/{name}", job.name())
            .body(body)
            .retrieve()
            .toBodilessEntity();
        log.info("Scheduled job name={} schedule={}", job.name(), job.schedule());
    }

    /** Remove a job from the Scheduler. Safe to call if it does not exist. */
    public void delete(String name) {
        http.delete()
            .uri("/v1.0-alpha1/jobs/{name}", name)
            .retrieve()
            .toBodilessEntity();
        log.info("Deleted job name={}", name);
    }
}
```

- [ ] **Step 5: Write `config/DaprProperties.java`** (needed by the production constructor)

```java
package io.sclera.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "dapr")
public record DaprProperties(int httpPort) {
    public DaprProperties {
        if (httpPort == 0) httpPort = 3500;
    }
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=SchedulerClientTest test`
Expected: PASS (2 tests).

> **Manual verification step (do this once before Task 12 component test):** with a Dapr
> sidecar running (`dapr --version` ≥ 1.15), confirm the Jobs API shape:
> `curl -X POST localhost:3500/v1.0-alpha1/jobs/ping -H 'Content-Type: application/json' -d '{"schedule":"@every 10s","data":{"jobName":"ping"}}'`
> then `curl localhost:3500/v1.0-alpha1/jobs/ping`. If the field names differ in your Dapr
> version, adjust `SchedulerClient.schedule()` body keys — and ONLY that method.

- [ ] **Step 7: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/client/ \
        sclera-scheduler/src/main/java/io/sclera/scheduler/config/DaprProperties.java \
        sclera-scheduler/src/test/java/io/sclera/scheduler/client/SchedulerClientTest.java
git commit -m "feat(scheduler): add SchedulerClient Dapr Jobs API wrapper"
```

---

## Task 5: Job catalog (jobs.yaml) + startup reconcile

**Files:**
- Create: `sclera-scheduler/src/main/resources/jobs.yaml`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/JobCatalogProperties.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/JobCatalogReconciler.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/JobCatalogReconcilerTest.java`

- [ ] **Step 1: Write `jobs.yaml`** (the full catalog — ~30 active jobs mapped from the monolith)

```yaml
scheduler:
  catalog:
    jobs:
      - name: historyRecord
        schedule: "0 0 0 */1 * *"
        owner: device-asset
      - name: snmpTrapRecord
        schedule: "0 0 0 */3 * *"
        owner: integrations
      - name: unlinkVendorOrganisation
        schedule: "0 0 0 */3 * *"
        owner: device-asset
      - name: unlinkedProductDetails
        schedule: "0 0 0 */3 * *"
        owner: inventory
      - name: productDetailResync
        schedule: "0 0 0 */15 * *"
        owner: inventory
      - name: internetBandwidthCheck
        schedule: "0 0 1 * * *"
        owner: device-asset
      - name: monnitValueUpdate
        schedule: "0 0 * * * *"
        owner: integrations
      - name: globalInspectionRecords
        schedule: "0 0 * * * *"
        owner: inspection
      - name: daintreeValueUpdate
        schedule: "0 0 */3 * * *"
        owner: integrations
      - name: vdmsSystemHealth
        schedule: "0 0 0 * * *"
        owner: device-asset
      - name: recordChecklistStatusSync
        schedule: "0 0 0 * * *"
        owner: inspection
      - name: siemensEventsSync
        schedule: "0 */15 * * * *"
        owner: integrations
      - name: recordChecklistStatusUpdate
        schedule: "0 5 */1 * * *"
        owner: inspection
      - name: connectedStatusForIOC
        schedule: "0 0 * * * *"
        owner: device-asset
      - name: siemensMeasuringInstrumentUpdate
        schedule: "0 */30 * * * *"
        owner: integrations
      - name: snmpSync
        schedule: "0 0 */3 * * *"
        owner: integrations
      - name: polyLensSync
        schedule: "0 0 */1 * * *"
        owner: integrations
      - name: hourlyIntegrationsSync
        schedule: "0 0 */1 * * *"
        owner: integrations
      - name: awairSync
        schedule: "0 */30 * * * *"
        owner: integrations
      - name: qrcodeNfcBarcodeSync
        schedule: "0 0 3 * * *"
        owner: device-asset
      - name: syncAssetCountToCloud
        schedule: "0 0 0,12 * * *"
        owner: device-asset
      - name: airthingSync
        schedule: "0 */15 * * * *"
        owner: integrations
      - name: modbusSync
        schedule: "0 */5 * * * *"
        owner: integrations
      - name: userActionLog
        schedule: "0 0 * * * *"
        owner: device-asset
      - name: deviceDndEnable
        schedule: "0 0/30 * * * *"
        owner: device-asset
      - name: offlineDeviceCheck
        schedule: "@every 90s"
        owner: device-asset
      - name: gaiameshSync
        schedule: "0 */5 * * * *"
        owner: integrations
      - name: createSqliteFile
        schedule: "0 30 3 * * ?"
        owner: edge
```

> Note: `?` in the last cron is Quartz syntax. If the Dapr verification (Task 4 Step 6)
> shows Dapr rejects `?`, replace it with `*` → `"0 30 3 * * *"`. Composite monolith
> methods (`vdmsSystemHealth`, `hourlyIntegrationsSync`, `connectedStatusForIOC`,
> `globalInspectionRecords`) are ONE job each — the dispatcher (Task 13) runs the full
> sequence. Commented-out monolith methods are intentionally absent.

- [ ] **Step 2: Write `JobCatalogProperties.java`**

```java
package io.sclera.scheduler.catalog;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "scheduler.catalog")
public record JobCatalogProperties(List<Entry> jobs) {

    public record Entry(String name, String schedule, String owner, String triggerTopic) {
        public Entry {
            if (triggerTopic == null) triggerTopic = "scheduler.trigger";
        }
    }
}
```

Make Spring load `jobs.yaml` in addition to `application.yml`. Add to `application.yml`
under `spring`:
```yaml
  config:
    import: "classpath:jobs.yaml"
```

- [ ] **Step 3: Write the failing test**

`JobCatalogReconcilerTest.java`:
```java
package io.sclera.scheduler.catalog;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JobCatalogReconcilerTest extends AbstractPostgresTest {

    @Autowired JobCatalogReconciler reconciler;
    @Autowired JobRepository jobs;

    @Test
    void insertsNewCatalogEntriesButPreservesRuntimeState() {
        // Pre-seed one job that an operator has PAUSED.
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.PAUSED));

        reconciler.reconcile(List.of(
            new JobCatalogProperties.Entry("snmpSync", "0 0 */3 * * *", "integrations", "scheduler.trigger"),
            new JobCatalogProperties.Entry("modbusSync", "0 */5 * * * *", "integrations", "scheduler.trigger")
        ));

        // New entry inserted as ENABLED; existing PAUSED state untouched.
        assertThat(jobs.findById("modbusSync")).get()
            .extracting(JobEntity::getState).isEqualTo(JobState.ENABLED);
        assertThat(jobs.findById("snmpSync")).get()
            .extracting(JobEntity::getState).isEqualTo(JobState.PAUSED);
    }

    @Test
    void updatesScheduleWhenCatalogChanges() {
        jobs.save(new JobEntity("modbusSync", "0 */5 * * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED));

        reconciler.reconcile(List.of(
            new JobCatalogProperties.Entry("modbusSync", "0 */10 * * * *", "integrations", "scheduler.trigger")
        ));

        assertThat(jobs.findById("modbusSync")).get()
            .extracting(JobEntity::getSchedule).isEqualTo("0 */10 * * * *");
    }
}
```

- [ ] **Step 4: Run the test to verify it fails**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=JobCatalogReconcilerTest test`
Expected: FAIL — `JobCatalogReconciler` cannot be resolved.

- [ ] **Step 5: Write `JobCatalogReconciler.java`**

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
            jobs.findById(e.name()).ifPresentOrElse(existing -> {
                existing.setSchedule(e.schedule());   // schedule + owner are catalog-owned
                existing.setOwner(e.owner());
                existing.setTriggerTopic(e.triggerTopic());
                // state, lastRunId, nextFireAt are runtime-owned — left untouched
            }, () -> {
                jobs.save(new JobEntity(e.name(), e.schedule(), e.owner(),
                        e.triggerTopic(), JobState.ENABLED));
                log.info("Catalog: inserted new job name={}", e.name());
            });
        }
    }
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=JobCatalogReconcilerTest test`
Expected: PASS (2 tests).

- [ ] **Step 7: Commit**

```bash
git add sclera-scheduler/src/main/resources/jobs.yaml \
        sclera-scheduler/src/main/resources/application.yml \
        sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/ \
        sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/JobCatalogReconcilerTest.java
git commit -m "feat(scheduler): add job catalog and startup reconcile"
```

---

## Task 6: RunRecorder — create/update job_run rows

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/RunRecorder.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/RunRecorderTest.java`

- [ ] **Step 1: Write the failing test**

`RunRecorderTest.java`:
```java
package io.sclera.scheduler.service;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RunRecorderTest extends AbstractPostgresTest {

    @Autowired RunRecorder recorder;
    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @Test
    void recordFiredInsertsFiredRunAndStampsLastRunId() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        UUID runId = UUID.randomUUID();

        recorder.recordFired("j", runId, false);

        assertThat(runs.findById(runId)).get().extracting(JobRunEntity::getStatus)
            .isEqualTo(RunStatus.FIRED);
        assertThat(jobs.findById("j")).get().extracting(JobEntity::getLastRunId)
            .isEqualTo(runId);
    }

    @Test
    void recordResultUpdatesStatusDurationError() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        UUID runId = UUID.randomUUID();
        recorder.recordFired("j", runId, false);

        recorder.recordResult(runId, RunStatus.FAILED, 500L, "boom");

        JobRunEntity run = runs.findById(runId).orElseThrow();
        assertThat(run.getStatus()).isEqualTo(RunStatus.FAILED);
        assertThat(run.getDurationMs()).isEqualTo(500L);
        assertThat(run.getError()).isEqualTo("boom");
        assertThat(run.getFinishedAt()).isNotNull();
    }

    @Test
    void recordResultForUnknownRunIsIgnored() {
        recorder.recordResult(UUID.randomUUID(), RunStatus.SUCCESS, 1L, null); // no throw
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=RunRecorderTest test`
Expected: FAIL — `RunRecorder` cannot be resolved.

- [ ] **Step 3: Write `RunRecorder.java`**

```java
package io.sclera.scheduler.service;

import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RunRecorder {

    private static final Logger log = LoggerFactory.getLogger(RunRecorder.class);
    private final JobRepository jobs;
    private final JobRunRepository runs;

    public RunRecorder(JobRepository jobs, JobRunRepository runs) {
        this.jobs = jobs;
        this.runs = runs;
    }

    @Transactional
    public void recordFired(String jobName, UUID runId, boolean manual) {
        runs.save(new JobRunEntity(runId, jobName, RunStatus.FIRED, manual, Instant.now()));
        jobs.findById(jobName).ifPresent(j -> j.setLastRunId(runId));
        log.info("Run fired job={} runId={} manual={}", jobName, runId, manual);
    }

    @Transactional
    public void recordResult(UUID runId, RunStatus status, long durationMs, String error) {
        runs.findById(runId).ifPresentOrElse(run -> {
            run.setStatus(status);
            run.setFinishedAt(Instant.now());
            run.setDurationMs(durationMs);
            run.setError(error);
            log.info("Run result runId={} status={} durationMs={}", runId, status, durationMs);
        }, () -> log.warn("Result for unknown runId={} ignored", runId));
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=RunRecorderTest test`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/service/RunRecorder.java \
        sclera-scheduler/src/test/java/io/sclera/scheduler/service/RunRecorderTest.java
git commit -m "feat(scheduler): add RunRecorder for job_run lifecycle"
```

---

## Task 7: JobService — register on startup + control operations

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/JobService.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/JobServiceTest.java`

- [ ] **Step 1: Write the failing test** (uses Mockito for `SchedulerClient` + `DaprEventPublisher`)

`JobServiceTest.java`:
```java
package io.sclera.scheduler.service;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
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
class JobServiceTest {

    @Mock JobRepository jobs;
    @Mock SchedulerClient scheduler;
    @Mock DaprEventPublisher publisher;
    @Mock RunRecorder recorder;

    JobService service() {
        JobService s = new JobService(jobs, scheduler, publisher, recorder);
        s.setPubsubName("pubsub");
        s.setTriggerTopic("scheduler.trigger");
        return s;
    }

    @Test
    void registerAllSchedulesOnlyEnabledJobs() {
        when(jobs.findByState(JobState.ENABLED)).thenReturn(List.of(
            new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED)));

        service().registerAll();

        verify(scheduler).schedule(new JobSchedule("a", "@every 1m"));
        verifyNoMoreInteractions(scheduler);
    }

    @Test
    void pauseDeletesFromSchedulerAndSetsState() {
        JobEntity job = new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED);
        when(jobs.findById("a")).thenReturn(Optional.of(job));

        service().pause("a");

        verify(scheduler).delete("a");
        org.assertj.core.api.Assertions.assertThat(job.getState()).isEqualTo(JobState.PAUSED);
    }

    @Test
    void resumeReregistersAndEnables() {
        JobEntity job = new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.PAUSED);
        when(jobs.findById("a")).thenReturn(Optional.of(job));

        service().resume("a");

        verify(scheduler).schedule(new JobSchedule("a", "@every 1m"));
        org.assertj.core.api.Assertions.assertThat(job.getState()).isEqualTo(JobState.ENABLED);
    }

    @Test
    void runNowPublishesTriggerAndRecordsManualRun() {
        JobEntity job = new JobEntity("a", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED);
        when(jobs.findById("a")).thenReturn(Optional.of(job));
        when(publisher.publish(anyString(), anyString(), any()))
            .thenReturn(new PublishResult(true, "evt", null));

        service().runNow("a");

        verify(recorder).recordFired(eq("a"), any(), eq(true));
        verify(publisher).publish(eq("pubsub"), eq("scheduler.trigger"), any());
    }

    @Test
    void unknownJobThrows() {
        when(jobs.findById("nope")).thenReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service().pause("nope"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=JobServiceTest test`
Expected: FAIL — `JobService` cannot be resolved.

- [ ] **Step 3: Write `JobService.java`**

```java
package io.sclera.scheduler.service;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.client.JobSchedule;
import io.sclera.scheduler.client.SchedulerClient;
import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class JobService {

    private static final Logger log = LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobs;
    private final SchedulerClient scheduler;
    private final DaprEventPublisher publisher;
    private final RunRecorder recorder;

    @Value("${scheduler.pubsub-name}") private String pubsubName;
    @Value("${scheduler.trigger-topic}") private String triggerTopic;

    public JobService(JobRepository jobs, SchedulerClient scheduler,
                      DaprEventPublisher publisher, RunRecorder recorder) {
        this.jobs = jobs;
        this.scheduler = scheduler;
        this.publisher = publisher;
        this.recorder = recorder;
    }

    void setPubsubName(String v) { this.pubsubName = v; }
    void setTriggerTopic(String v) { this.triggerTopic = v; }

    /** Called once at startup (Task 8 wires the runner). Registers all ENABLED jobs. */
    public void registerAll() {
        for (JobEntity job : jobs.findByState(JobState.ENABLED)) {
            scheduler.schedule(new JobSchedule(job.getName(), job.getSchedule()));
        }
        log.info("Registered all enabled jobs with Dapr Scheduler");
    }

    @Transactional
    public void pause(String name) {
        JobEntity job = require(name);
        scheduler.delete(name);
        job.setState(JobState.PAUSED);
    }

    @Transactional
    public void resume(String name) {
        JobEntity job = require(name);
        scheduler.schedule(new JobSchedule(name, job.getSchedule()));
        job.setState(JobState.ENABLED);
    }

    @Transactional
    public void disable(String name) {
        JobEntity job = require(name);
        scheduler.delete(name);
        job.setState(JobState.DISABLED);
    }

    /** Fire immediately, bypassing the schedule. Records a manual run. */
    public void runNow(String name) {
        require(name);
        UUID runId = UUID.randomUUID();
        recorder.recordFired(name, runId, true);
        var result = publisher.publish(pubsubName, triggerTopic,
            new SchedulerTriggerEvent(name, runId.toString(), System.currentTimeMillis()));
        if (!result.success()) {
            log.error("run-now publish failed job={} error={}", name, result.error());
        }
    }

    private JobEntity require(String name) {
        return jobs.findById(name)
            .orElseThrow(() -> new IllegalArgumentException("Unknown job: " + name));
    }
}
```

> Note: the test sets `pubsubName`/`triggerTopic` via the package-private setters; in
> production they are injected by `@Value`. Keep both.

- [ ] **Step 4: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=JobServiceTest test`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/service/JobService.java \
        sclera-scheduler/src/test/java/io/sclera/scheduler/service/JobServiceTest.java
git commit -m "feat(scheduler): add JobService register + pause/resume/disable/run-now"
```

---

## Task 8: Startup runner — reconcile catalog then register jobs

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/CatalogStartupRunner.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/CatalogStartupRunnerTest.java`

- [ ] **Step 1: Write the failing test**

`CatalogStartupRunnerTest.java`:
```java
package io.sclera.scheduler.catalog;

import io.sclera.scheduler.service.JobService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogStartupRunnerTest {

    @Mock JobCatalogReconciler reconciler;
    @Mock JobService jobService;

    @Test
    void reconcilesThenRegistersInOrder() throws Exception {
        var props = new JobCatalogProperties(List.of(
            new JobCatalogProperties.Entry("a", "@every 1m", "x", "scheduler.trigger")));
        var runner = new CatalogStartupRunner(props, reconciler, jobService);

        runner.run(null);

        InOrder order = inOrder(reconciler, jobService);
        order.verify(reconciler).reconcile(props.jobs());
        order.verify(jobService).registerAll();
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=CatalogStartupRunnerTest test`
Expected: FAIL — `CatalogStartupRunner` cannot be resolved.

- [ ] **Step 3: Write `CatalogStartupRunner.java`**

```java
package io.sclera.scheduler.catalog;

import io.sclera.scheduler.service.JobService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** On boot: reconcile the catalog into the DB, then register enabled jobs with Dapr. */
@Component
public class CatalogStartupRunner implements ApplicationRunner {

    private final JobCatalogProperties catalog;
    private final JobCatalogReconciler reconciler;
    private final JobService jobService;

    public CatalogStartupRunner(JobCatalogProperties catalog,
                                JobCatalogReconciler reconciler,
                                JobService jobService) {
        this.catalog = catalog;
        this.reconciler = reconciler;
        this.jobService = jobService;
    }

    @Override
    public void run(ApplicationArguments args) {
        reconciler.reconcile(catalog.jobs());
        jobService.registerAll();
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=CatalogStartupRunnerTest test`
Expected: PASS (1 test).

- [ ] **Step 5: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/catalog/CatalogStartupRunner.java \
        sclera-scheduler/src/test/java/io/sclera/scheduler/catalog/CatalogStartupRunnerTest.java
git commit -m "feat(scheduler): reconcile + register jobs on startup"
```

---

## Task 9: JobCallbackController — Dapr Scheduler fires here

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/JobCallbackController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/JobCallbackControllerTest.java`

- [ ] **Step 1: Write the failing test** (slice test with mocked collaborators)

`JobCallbackControllerTest.java`:
```java
package io.sclera.scheduler.web;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.scheduler.service.RunRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobCallbackControllerTest {

    @Mock RunRecorder recorder;
    @Mock DaprEventPublisher publisher;

    JobCallbackController controller() {
        JobCallbackController c = new JobCallbackController(recorder, publisher);
        c.setPubsubName("pubsub");
        c.setTriggerTopic("scheduler.trigger");
        return c;
    }

    @Test
    void onFireRecordsRunAndPublishesTrigger() {
        when(publisher.publish(anyString(), anyString(), any()))
            .thenReturn(new PublishResult(true, "evt", null));

        controller().onJobFired("snmpSync");

        verify(recorder).recordFired(eq("snmpSync"), any(), eq(false));
        verify(publisher).publish(eq("pubsub"), eq("scheduler.trigger"), any());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=JobCallbackControllerTest test`
Expected: FAIL — `JobCallbackController` cannot be resolved.

- [ ] **Step 3: Write `JobCallbackController.java`**

```java
package io.sclera.scheduler.web;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import io.sclera.scheduler.service.RunRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Dapr Scheduler invokes POST /job/{name} when a registered job fires. We record the run
 * and publish a scheduler.trigger event for the owning service to execute.
 */
@RestController
public class JobCallbackController {

    private static final Logger log = LoggerFactory.getLogger(JobCallbackController.class);

    private final RunRecorder recorder;
    private final DaprEventPublisher publisher;

    @Value("${scheduler.pubsub-name}") private String pubsubName;
    @Value("${scheduler.trigger-topic}") private String triggerTopic;

    public JobCallbackController(RunRecorder recorder, DaprEventPublisher publisher) {
        this.recorder = recorder;
        this.publisher = publisher;
    }

    void setPubsubName(String v) { this.pubsubName = v; }
    void setTriggerTopic(String v) { this.triggerTopic = v; }

    @PostMapping("/job/{name}")
    public ResponseEntity<Void> onJobFired(@PathVariable("name") String name) {
        UUID runId = UUID.randomUUID();
        recorder.recordFired(name, runId, false);
        PublishResult result = publisher.publish(pubsubName, triggerTopic,
            new SchedulerTriggerEvent(name, runId.toString(), System.currentTimeMillis()));
        if (!result.success()) {
            log.error("Trigger publish failed job={} runId={} error={}",
                name, runId, result.error());
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=JobCallbackControllerTest test`
Expected: PASS (1 test).

- [ ] **Step 5: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/JobCallbackController.java \
        sclera-scheduler/src/test/java/io/sclera/scheduler/web/JobCallbackControllerTest.java
git commit -m "feat(scheduler): add /job/{name} callback that publishes trigger events"
```

---

## Task 10: ResultSubscriber — record downstream outcomes

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/subscriber/ResultSubscriber.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/subscriber/ResultSubscriberTest.java`

- [ ] **Step 1: Write the failing test**

`ResultSubscriberTest.java`:
```java
package io.sclera.scheduler.subscriber;

import io.dapr.client.DaprClient;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.scheduler.domain.RunStatus;
import io.sclera.scheduler.service.RunRecorder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResultSubscriberTest {

    @Mock DaprClient dapr;
    @Mock RunRecorder recorder;

    @Test
    void handleEventRecordsResultByRunId() {
        ResultSubscriber sub = new ResultSubscriber(dapr, recorder);
        UUID runId = UUID.randomUUID();

        sub.handleEvent(new SchedulerResultEvent("snmpSync", runId.toString(), "FAILED", 42L, "x"));

        verify(recorder).recordResult(runId, RunStatus.FAILED, 42L, "x");
    }
}
```

> Note: `handleEvent` is `protected` in the base class. Since the test is in the same
> package as `ResultSubscriber`, it can call it directly.

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=ResultSubscriberTest test`
Expected: FAIL — `ResultSubscriber` cannot be resolved.

- [ ] **Step 3: Write `ResultSubscriber.java`**

```java
package io.sclera.scheduler.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.scheduler.domain.RunStatus;
import io.sclera.scheduler.service.RunRecorder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class ResultSubscriber extends DaprEventSubscriber<SchedulerResultEvent> {

    private final RunRecorder recorder;

    public ResultSubscriber(DaprClient dapr, RunRecorder recorder) {
        super(dapr, "scheduler.result");
        this.recorder = recorder;
    }

    @Topic(name = "scheduler.result", pubsubName = "pubsub",
           deadLetterTopic = "scheduler.result.dlq")
    @PostMapping("/internal/scheduler-result")
    public ResponseEntity<Map<String, String>> onResult(
            @RequestBody CloudEvent<SchedulerResultEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(SchedulerResultEvent data) {
        RunStatus status = RunStatus.valueOf(data.status());
        recorder.recordResult(UUID.fromString(data.runId()), status, data.durationMs(), data.error());
    }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=ResultSubscriberTest test`
Expected: PASS (1 test).

- [ ] **Step 5: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/subscriber/ResultSubscriber.java \
        sclera-scheduler/src/test/java/io/sclera/scheduler/subscriber/ResultSubscriberTest.java
git commit -m "feat(scheduler): subscribe to scheduler.result and record outcomes"
```

---

## Task 11: SchedulerApiController — REST API for the UI

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/JobView.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/RunView.java`
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/web/SchedulerApiController.java`
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/web/SchedulerApiControllerTest.java`

- [ ] **Step 1: Write the DTOs**

`JobView.java`:
```java
package io.sclera.scheduler.web.dto;

public record JobView(
    String name,
    String schedule,
    String owner,
    String state,
    String lastStatus,     // null if never run
    Long lastDurationMs,
    String lastFiredAt,     // ISO-8601, null if never run
    String nextFireAt) {}   // ISO-8601, null if unknown
```

`RunView.java`:
```java
package io.sclera.scheduler.web.dto;

public record RunView(
    String runId,
    String status,
    boolean manual,
    String firedAt,
    String finishedAt,
    Long durationMs,
    String error) {}
```

- [ ] **Step 2: Write the failing test**

`SchedulerApiControllerTest.java`:
```java
package io.sclera.scheduler.web;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SchedulerApiControllerTest extends AbstractPostgresTest {

    @Autowired WebApplicationContext ctx;
    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    // JobService bean is real but its SchedulerClient calls hit Dapr; we only test
    // read endpoints + that pause delegates. Replace SchedulerClient with a mock.
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    io.sclera.scheduler.client.SchedulerClient schedulerClient;

    MockMvc mvc() { return MockMvcBuilders.webAppContextSetup(ctx).build(); }

    @Test
    void listJobsReturnsCatalogWithLatestRun() throws Exception {
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED));
        UUID runId = UUID.randomUUID();
        JobRunEntity run = new JobRunEntity(runId, "snmpSync", RunStatus.SUCCESS, false,
                Instant.parse("2026-06-02T00:00:00Z"));
        run.setStatus(RunStatus.SUCCESS);
        run.setDurationMs(123L);
        runs.save(run);

        mvc().perform(get("/api/jobs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("snmpSync"))
            .andExpect(jsonPath("$[0].lastStatus").value("SUCCESS"))
            .andExpect(jsonPath("$[0].lastDurationMs").value(123));
    }

    @Test
    void pauseEndpointDelegatesToScheduler() throws Exception {
        jobs.save(new JobEntity("snmpSync", "0 0 */3 * * *", "integrations",
                "scheduler.trigger", JobState.ENABLED));

        mvc().perform(post("/api/jobs/snmpSync/pause")).andExpect(status().isOk());

        verify(schedulerClient).delete("snmpSync");
    }

    @Test
    void unknownJobReturns404() throws Exception {
        mvc().perform(post("/api/jobs/nope/pause")).andExpect(status().isNotFound());
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=SchedulerApiControllerTest test`
Expected: FAIL — `SchedulerApiController` not found / no mapping.

- [ ] **Step 4: Write `SchedulerApiController.java`**

```java
package io.sclera.scheduler.web;

import io.sclera.scheduler.domain.*;
import io.sclera.scheduler.service.JobService;
import io.sclera.scheduler.web.dto.JobView;
import io.sclera.scheduler.web.dto.RunView;
import org.springframework.data.domain.Limit;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/jobs")
public class SchedulerApiController {

    private final JobRepository jobs;
    private final JobRunRepository runs;
    private final JobService jobService;

    public SchedulerApiController(JobRepository jobs, JobRunRepository runs, JobService jobService) {
        this.jobs = jobs;
        this.runs = runs;
        this.jobService = jobService;
    }

    @GetMapping
    public List<JobView> list() {
        return jobs.findAll().stream().map(this::toView).toList();
    }

    @GetMapping("/{name}")
    public ResponseEntity<JobView> get(@PathVariable String name) {
        return jobs.findById(name).map(j -> ResponseEntity.ok(toView(j)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{name}/runs")
    public List<RunView> history(@PathVariable String name,
                                 @RequestParam(defaultValue = "50") int limit) {
        return runs.findByJobNameOrderByFiredAtDesc(name, Limit.of(limit))
            .stream().map(this::toRunView).toList();
    }

    @PostMapping("/{name}/pause")
    public void pause(@PathVariable String name) { jobService.pause(name); }

    @PostMapping("/{name}/resume")
    public void resume(@PathVariable String name) { jobService.resume(name); }

    @PostMapping("/{name}/disable")
    public void disable(@PathVariable String name) { jobService.disable(name); }

    @PostMapping("/{name}/run")
    public void run(@PathVariable String name) { jobService.runNow(name); }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public void notFound() {}

    private JobView toView(JobEntity j) {
        Optional<JobRunEntity> last = j.getLastRunId() == null
            ? Optional.empty() : runs.findById(j.getLastRunId());
        return new JobView(
            j.getName(), j.getSchedule(), j.getOwner(), j.getState().name(),
            last.map(r -> r.getStatus().name()).orElse(null),
            last.map(JobRunEntity::getDurationMs).orElse(null),
            last.map(r -> iso(r.getFiredAt())).orElse(null),
            iso(j.getNextFireAt()));
    }

    private RunView toRunView(JobRunEntity r) {
        return new RunView(r.getRunId().toString(), r.getStatus().name(), r.isManual(),
            iso(r.getFiredAt()), iso(r.getFinishedAt()), r.getDurationMs(), r.getError());
    }

    private static String iso(Instant t) { return t == null ? null : t.toString(); }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=SchedulerApiControllerTest test`
Expected: PASS (3 tests).

- [ ] **Step 6: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/web/SchedulerApiController.java \
        sclera-scheduler/src/main/java/io/sclera/scheduler/web/dto/ \
        sclera-scheduler/src/test/java/io/sclera/scheduler/web/SchedulerApiControllerTest.java
git commit -m "feat(scheduler): add REST API for job list/history/control"
```

---

## Task 12: Housekeeping — orphan reaper + history retention

**Files:**
- Create: `sclera-scheduler/src/main/java/io/sclera/scheduler/service/HousekeepingService.java`
- Modify: `sclera-scheduler/src/main/java/io/sclera/scheduler/Application.java` (add `@EnableScheduling`)
- Test: `sclera-scheduler/src/test/java/io/sclera/scheduler/service/HousekeepingServiceTest.java`

> This uses Spring `@Scheduled` for *internal* housekeeping only (not domain jobs). This
> service is single-instance in deployment, so internal `@Scheduled` is acceptable here.

- [ ] **Step 1: Write the failing test**

`HousekeepingServiceTest.java`:
```java
package io.sclera.scheduler.service;

import io.sclera.scheduler.AbstractPostgresTest;
import io.sclera.scheduler.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "scheduler.orphan-timeout-seconds=900",
    "scheduler.history-retention-days=30"
})
class HousekeepingServiceTest extends AbstractPostgresTest {

    @Autowired HousekeepingService housekeeping;
    @Autowired JobRepository jobs;
    @Autowired JobRunRepository runs;

    @Test
    void sweepsStuckFiredRunsToFailed() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        UUID old = UUID.randomUUID();
        JobRunEntity stuck = new JobRunEntity(old, "j", RunStatus.FIRED, false,
                Instant.now().minus(2, ChronoUnit.HOURS));
        runs.save(stuck);

        housekeeping.reapOrphans();

        assertThat(runs.findById(old)).get().extracting(JobRunEntity::getStatus)
            .isEqualTo(RunStatus.FAILED);
        assertThat(runs.findById(old)).get().extracting(JobRunEntity::getError)
            .isEqualTo("timed out: no result received");
    }

    @Test
    void prunesOldHistory() {
        jobs.save(new JobEntity("j", "@every 1m", "x", "scheduler.trigger", JobState.ENABLED));
        runs.save(new JobRunEntity(UUID.randomUUID(), "j", RunStatus.SUCCESS, false,
                Instant.now().minus(40, ChronoUnit.DAYS)));
        runs.save(new JobRunEntity(UUID.randomUUID(), "j", RunStatus.SUCCESS, false,
                Instant.now().minus(1, ChronoUnit.DAYS)));

        housekeeping.pruneHistory();

        assertThat(runs.count()).isEqualTo(1);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=HousekeepingServiceTest test`
Expected: FAIL — `HousekeepingService` cannot be resolved.

- [ ] **Step 3: Write `HousekeepingService.java`**

```java
package io.sclera.scheduler.service;

import io.sclera.scheduler.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class HousekeepingService {

    private static final Logger log = LoggerFactory.getLogger(HousekeepingService.class);

    private final JobRunRepository runs;

    @Value("${scheduler.orphan-timeout-seconds}") private long orphanTimeoutSeconds;
    @Value("${scheduler.history-retention-days}") private long retentionDays;

    public HousekeepingService(JobRunRepository runs) {
        this.runs = runs;
    }

    /** Every 5 minutes: FIRED runs with no result past the timeout become FAILED. */
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void reapOrphans() {
        Instant cutoff = Instant.now().minusSeconds(orphanTimeoutSeconds);
        List<JobRunEntity> orphans = runs.findByStatusAndFiredAtBefore(RunStatus.FIRED, cutoff);
        for (JobRunEntity r : orphans) {
            r.setStatus(RunStatus.FAILED);
            r.setFinishedAt(Instant.now());
            r.setError("timed out: no result received");
        }
        if (!orphans.isEmpty()) log.warn("Reaped {} orphan runs", orphans.size());
    }

    /** Daily: delete run history older than the retention window. */
    @Scheduled(fixedDelay = 86_400_000)
    @Transactional
    public void pruneHistory() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deleted = runs.deleteByFiredAtBefore(cutoff);
        if (deleted > 0) log.info("Pruned {} old run rows", deleted);
    }
}
```

- [ ] **Step 4: Add `@EnableScheduling` to `Application.java`**

```java
package io.sclera.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `cd sclera-scheduler && ./mvnw -q -Dtest=HousekeepingServiceTest test`
Expected: PASS (2 tests).

- [ ] **Step 6: Commit**

```bash
git add sclera-scheduler/src/main/java/io/sclera/scheduler/service/HousekeepingService.java \
        sclera-scheduler/src/main/java/io/sclera/scheduler/Application.java \
        sclera-scheduler/src/test/java/io/sclera/scheduler/service/HousekeepingServiceTest.java
git commit -m "feat(scheduler): add orphan reaper and history retention"
```

---

## Task 13: Dashboard UI page

**Files:**
- Create: `sclera-scheduler/src/main/resources/static/scheduler.html`

> No test (static asset). Verified manually in Task 15.

- [ ] **Step 1: Write `scheduler.html`**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8"/>
  <title>Sclera Scheduler</title>
  <style>
    body { font-family: system-ui, sans-serif; margin: 1.5rem; color: #222; }
    h1 { font-size: 1.3rem; }
    table { border-collapse: collapse; width: 100%; font-size: 0.9rem; }
    th, td { text-align: left; padding: 6px 10px; border-bottom: 1px solid #eee; }
    th { background: #fafafa; }
    .SUCCESS { color: #137333; font-weight: 600; }
    .FAILED  { color: #c5221f; font-weight: 600; }
    .FIRED   { color: #888; }
    .state-PAUSED   { color: #b06000; }
    .state-DISABLED { color: #999; text-decoration: line-through; }
    button { margin-right: 4px; cursor: pointer; }
    #runs { margin-top: 1rem; }
  </style>
</head>
<body>
  <h1>Sclera Scheduler</h1>
  <table id="jobs">
    <thead><tr>
      <th>Job</th><th>Owner</th><th>Schedule</th><th>State</th>
      <th>Last run</th><th>Duration</th><th>Actions</th>
    </tr></thead>
    <tbody></tbody>
  </table>
  <div id="runs"></div>

  <script>
    async function api(path, method) {
      const r = await fetch(path, { method: method || 'GET' });
      if (method) return;            // control actions return no body
      return r.json();
    }
    function fmt(t) { return t ? new Date(t).toLocaleString() : '—'; }

    async function load() {
      const jobs = await api('/api/jobs');
      const tb = document.querySelector('#jobs tbody');
      tb.innerHTML = '';
      for (const j of jobs) {
        const tr = document.createElement('tr');
        tr.innerHTML = `
          <td><a href="#" data-job="${j.name}">${j.name}</a></td>
          <td>${j.owner}</td>
          <td><code>${j.schedule}</code></td>
          <td class="state-${j.state}">${j.state}</td>
          <td class="${j.lastStatus||''}">${j.lastStatus||'—'} <small>${fmt(j.lastFiredAt)}</small></td>
          <td>${j.lastDurationMs!=null ? j.lastDurationMs+' ms' : '—'}</td>
          <td>
            <button data-act="run">Run now</button>
            <button data-act="pause">Pause</button>
            <button data-act="resume">Resume</button>
            <button data-act="disable">Disable</button>
          </td>`;
        tr.querySelectorAll('button').forEach(b =>
          b.onclick = async () => {
            await api(`/api/jobs/${j.name}/${b.dataset.act}`, 'POST');
            load();
          });
        tr.querySelector('a').onclick = (e) => { e.preventDefault(); showRuns(j.name); };
        tb.appendChild(tr);
      }
    }

    async function showRuns(name) {
      const runs = await api(`/api/jobs/${name}/runs?limit=50`);
      const div = document.getElementById('runs');
      div.innerHTML = `<h2>${name} — last ${runs.length} runs</h2>` +
        '<table><thead><tr><th>Fired</th><th>Status</th><th>Duration</th><th>Error</th></tr></thead><tbody>' +
        runs.map(r => `<tr><td>${fmt(r.firedAt)}</td>
          <td class="${r.status}">${r.status}${r.manual?' (manual)':''}</td>
          <td>${r.durationMs!=null?r.durationMs+' ms':'—'}</td>
          <td>${r.error||''}</td></tr>`).join('') +
        '</tbody></table>';
    }

    load();
    setInterval(load, 5000);
  </script>
</body>
</html>
```

- [ ] **Step 2: Commit**

```bash
git add sclera-scheduler/src/main/resources/static/scheduler.html
git commit -m "feat(scheduler): add monitoring/control dashboard page"
```

---

## Task 14: Dapr components, app-id registry, docker-compose

**Files:**
- Modify: `dapr/components/local/pubsub.yaml` (add DLQ documentation note)
- Modify: `dapr/APP_IDS.md` (register sclera-scheduler)
- Modify: `docker-compose.yml` (add scheduler app + sidecar)

> The Redis pub/sub component needs no per-topic config; DLQ routing is declared on the
> subscriber via `@Topic(deadLetterTopic=...)` (already done in Task 10). The Scheduler
> control plane is a sidecar/runtime feature — ensure the Dapr version supports it.

- [ ] **Step 1: Register the app-id in `dapr/APP_IDS.md`**

Add this row to the app-id table (after `sclera-edge`):
```markdown
| `sclera-scheduler` | sclera-scheduler | 8098 | scheduler.result | scheduler.trigger | platform |
```
And add to the Topics section:
```markdown
- `scheduler.trigger` — published by sclera-scheduler when a job fires; consumed by the job's owning service (initially the monolith dispatcher)
- `scheduler.result` — published by the owning service after running the job; consumed by sclera-scheduler to record outcome
- `scheduler.trigger.dlq`, `scheduler.result.dlq` — dead-letter companions
```

- [ ] **Step 2: Add a DLQ note to `dapr/components/local/pubsub.yaml`**

Append as a YAML comment (Redis pub/sub auto-creates topics; this documents intent):
```yaml
# Scheduler topics: scheduler.trigger, scheduler.result (+ .dlq companions).
# DLQ routing is declared per-subscriber via @Topic(deadLetterTopic=...).
```

- [ ] **Step 3: Add the scheduler service + sidecar to `docker-compose.yml`**

Mirror the existing `sclera-audit` + `sclera-audit-dapr` pair. Add:
```yaml
  sclera-scheduler:
    build: ./sclera-scheduler
    ports:
      - "8098:8098"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/sclera
      DB_USER: sclera
      DB_PASS: sclera
      DAPR_HTTP_PORT: "3500"
    depends_on:
      - postgres
      - redis
    networks: [ sclera ]

  sclera-scheduler-dapr:
    image: daprio/daprd:1.15.0
    command: [
      "./daprd",
      "--app-id", "sclera-scheduler",
      "--app-port", "8098",
      "--dapr-http-port", "3500",
      "--config", "/dapr/config.yaml",
      "--resources-path", "/dapr/components/local"
    ]
    volumes:
      - ./dapr:/dapr
    network_mode: "service:sclera-scheduler"
    depends_on:
      - sclera-scheduler
```

> Match the exact Redis/Postgres service names, network name, and daprd image tag used by
> the other services in `docker-compose.yml`. **The daprd image must be ≥ 1.15** so the
> Scheduler control plane is present and enabled by default. If the existing compose pins
> an older daprd (e.g. 1.12), bump the scheduler's sidecar tag and confirm the standalone
> Scheduler service is running (`dapr/daprd` placement+scheduler containers).

- [ ] **Step 4: Verify YAML is well-formed**

Run: `cd "$(git rev-parse --show-toplevel)" && docker compose config -q`
Expected: no output (valid). If `docker` is unavailable, skip and rely on Task 15.

- [ ] **Step 5: Commit**

```bash
git add dapr/APP_IDS.md dapr/components/local/pubsub.yaml docker-compose.yml
git commit -m "chore(scheduler): register app-id, topics, and compose sidecar"
```

---

## Task 15: End-to-end component test (L3)

**Files:**
- Create: `sclera-scheduler/src/test/java/io/sclera/scheduler/component/SchedulerFlowComponentTest.java`

> This test needs a live Dapr sidecar + Scheduler + Redis + Postgres. It is `@Disabled` by
> default so the normal build stays hermetic; run it manually when the stack is up. It is
> the canonical proof of exactly-once firing and the full trigger→result loop.

- [ ] **Step 1: Write the component test (disabled by default)**

```java
package io.sclera.scheduler.component;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Run manually against a live stack:
 *   1) docker compose up -d postgres redis
 *   2) dapr run --app-id sclera-scheduler --app-port 8098 --dapr-http-port 3500 \
 *        --resources-path dapr/components/local -- ./mvnw -q spring-boot:run
 *   3) Remove @Disabled and run this test, OR exercise manually with curl (steps below).
 *
 * Manual verification (no test infra needed):
 *   - Register a fast job:
 *       curl -X POST localhost:3500/v1.0-alpha1/jobs/pingTest \
 *         -H 'Content-Type: application/json' \
 *         -d '{"schedule":"@every 5s","data":{"jobName":"pingTest"}}'
 *   - Watch the service log: POST /job/pingTest every 5s, scheduler.trigger published.
 *   - GET localhost:8098/api/jobs → pingTest shows recent FIRED runs.
 *   - Publish a result to close the loop:
 *       curl -X POST localhost:3500/v1.0/publish/pubsub/scheduler.result \
 *         -H 'Content-Type: application/json' \
 *         -d '{"jobName":"pingTest","runId":"<runId-from-/api/jobs>","status":"SUCCESS","durationMs":10,"error":null}'
 *   - GET /api/jobs/pingTest/runs → that run flips to SUCCESS with duration.
 *
 * Exactly-once check: start a SECOND scheduler replica (app-port 8099, same app-id) and
 * confirm each tick produces exactly ONE job_run row, not two.
 */
@Disabled("Requires live Dapr Scheduler + Redis + Postgres; run manually")
class SchedulerFlowComponentTest {

    @Test
    void triggerToResultLoop() {
        // Placeholder for an automated harness once a Dapr Testcontainer is wired.
        // The manual steps in the class javadoc are the current acceptance procedure.
    }
}
```

- [ ] **Step 2: Run the full module test suite (component test stays skipped)**

Run: `cd sclera-scheduler && ./mvnw -q test`
Expected: all prior tests PASS; `SchedulerFlowComponentTest` reported as skipped.

- [ ] **Step 3: Manual smoke (if a Dapr ≥1.15 stack is available)**

Follow the javadoc steps. Confirm: `/job/{name}` fires on schedule, `/api/jobs` shows
runs, a published `scheduler.result` flips the run to SUCCESS, and two replicas produce
one run per tick. Record the outcome in the PR description.

- [ ] **Step 4: Commit**

```bash
git add sclera-scheduler/src/test/java/io/sclera/scheduler/component/SchedulerFlowComponentTest.java
git commit -m "test(scheduler): add component-test harness and manual acceptance steps"
```

---

## Task 16 (CROSS-REPO): Monolith trigger dispatcher

> **This task lands in the read-only-by-default `sclera-vdms-edge-server` monolith repo,
> NOT in this tree.** Coordinate access before starting. It replaces the `@Scheduled`
> methods in `io.sclera.startup.Schedular`. Do this in a PILOT first: migrate only
> `offlineDeviceCheck` and `syncAssetCountToCloud`, leave all other `@Scheduled` methods
> running, validate in staging, then migrate the rest in batches by owner.

**Files (in the monolith repo):**
- Add `dapr-commons` as a dependency (it publishes `SchedulerTriggerEvent`/`SchedulerResultEvent`).
- Create: `io/sclera/startup/TriggerDispatchSubscriber.java`
- Modify: `io/sclera/startup/Schedular.java` — comment out the two pilot `@Scheduled` methods.

- [ ] **Step 1: Write the dispatcher**

```java
package io.sclera.startup;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.SchedulerResultEvent;
import io.sclera.dapr.events.SchedulerTriggerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Receives scheduler.trigger events and runs the existing SchedularService logic by job
 * name. Publishes scheduler.result so the scheduler service records the outcome.
 * Idempotent on the CloudEvent id (= runId) via the base class.
 */
@RestController
public class TriggerDispatchSubscriber extends DaprEventSubscriber<SchedulerTriggerEvent> {

    private static final Logger log = LoggerFactory.getLogger(TriggerDispatchSubscriber.class);

    private final SchedularService schedularService;
    private final DaprEventPublisher publisher;

    public TriggerDispatchSubscriber(DaprClient dapr,
                                     SchedularService schedularService,
                                     DaprEventPublisher publisher) {
        super(dapr, "scheduler.trigger");
        this.schedularService = schedularService;
        this.publisher = publisher;
    }

    @Topic(name = "scheduler.trigger", pubsubName = "pubsub",
           deadLetterTopic = "scheduler.trigger.dlq")
    @PostMapping("/internal/scheduler-trigger")
    public ResponseEntity<Map<String, String>> onTrigger(
            @RequestBody CloudEvent<SchedulerTriggerEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(SchedulerTriggerEvent data) {
        long t0 = System.currentTimeMillis();
        String error = null;
        try {
            dispatch(data.jobName());
        } catch (RuntimeException e) {
            error = e.getMessage();
            log.error("Job dispatch failed job={} runId={}", data.jobName(), data.runId(), e);
            throw e; // let base class classify for RETRY/DROP
        } finally {
            long durationMs = System.currentTimeMillis() - t0;
            publisher.publish("pubsub", "scheduler.result",
                new SchedulerResultEvent(data.jobName(), data.runId(),
                    error == null ? "SUCCESS" : "FAILED", durationMs, error));
        }
    }

    /** Maps job name -> existing SchedularService method. Unknown name = permanent error. */
    private void dispatch(String jobName) {
        switch (jobName) {
            case "offlineDeviceCheck" -> schedularService.updateDeviceStatusToOffline();
            case "syncAssetCountToCloud" -> schedularService.syncAssetCountToCloud();
            // ── add the remaining cases as each job is migrated (batch by owner) ──
            // case "snmpSync" -> schedularService.scheduleSnmpSync();
            // case "vdmsSystemHealth" -> { /* lorawan + health + IOC sync sequence */ }
            default -> throw new IllegalArgumentException("No dispatch for job: " + jobName);
        }
    }
}
```

> `IllegalArgumentException` is classified as a permanent error by the base class
> (`isPermanentError`), so an unknown/not-yet-migrated job name → DROP → DLQ, not an
> infinite retry. The `finally` block always publishes a result so the dashboard never
> shows a permanently-FIRED run.

- [ ] **Step 2: Comment out the two pilot `@Scheduled` methods in `Schedular.java`**

In `io.sclera.startup.Schedular`, comment out `scheduleOfflineDeviceCheck()` and
`syncAssetCountToCloud()` (their work now arrives via `scheduler.trigger`). Leave every
other `@Scheduled` method as-is for the pilot.

- [ ] **Step 3: Build the monolith**

Run the monolith repo's build (its own wrapper). Expected: compiles with the new
`dapr-commons` dependency.

- [ ] **Step 4: Pilot validation (staging)**

With the scheduler service running and the two pilot jobs in `jobs.yaml`:
- Confirm `offlineDeviceCheck` fires (~90s) → trigger → dispatcher runs
  `updateDeviceStatusToOffline()` → result recorded SUCCESS in the dashboard.
- Confirm `syncAssetCountToCloud` fires at its cron and records a run.
- Confirm no duplicate execution when the monolith runs >1 replica.

- [ ] **Step 5: Commit (in the monolith repo)**

```bash
git add io/sclera/startup/TriggerDispatchSubscriber.java io/sclera/startup/Schedular.java
git commit -m "feat(scheduler): dispatch scheduler.trigger events; migrate 2 pilot jobs"
```

- [ ] **Step 6: Roll out the remaining jobs in batches**

For each subsequent batch (by `owner`): add the `case` to `dispatch()`, comment out the
matching `@Scheduled` method, validate. When all are migrated, remove `@EnableScheduling`
and the `service-host=box` gate and delete the `Schedular` class. `SchedularService` stays.

---

## Self-Review

**Spec coverage** (each spec section → task):
- Central scheduler service, app-id, schema → Tasks 2, 3 ✓
- Dapr Jobs API (alpha, isolated client) → Task 4 ✓
- Job catalog `jobs.yaml` + reconcile (preserve runtime state) → Tasks 5, 8 ✓
- Composite jobs kept whole; commented-out excluded → Task 5 jobs.yaml + Task 16 dispatch ✓
- `/job/{name}` callback → trigger publish → Task 9 ✓
- Result subscriber → record outcome → Task 10 ✓
- Persistence schema (job, job_run) → Task 3 ✓
- REST API (list/detail/history/pause/resume/disable/run) → Task 11 ✓
- Control op semantics (pause=delete, resume=register, run-now=publish) → Task 7 ✓
- Self-contained dashboard page → Task 13 ✓
- DLQs on both topics → Tasks 10, 16 (`@Topic deadLetterTopic`) ✓
- Orphan reaper + retention → Task 12 ✓
- Exactly-once via runId idempotency → Tasks 9/16 (runId as CloudEvent id key) ✓
- Monolith dispatcher bridge, phased rollout → Task 16 ✓
- Components/compose/APP_IDS → Task 14 ✓
- L3 component test → Task 15 ✓
- Shared event DTOs → Task 1 ✓

**Placeholder scan:** The only intentional "fill-in-later" is Task 16's `dispatch()` switch,
which is the explicit batch-migration mechanism (pilot 2 jobs are fully written). The Task
15 component test is `@Disabled` with a complete manual procedure — not a stub of unwritten
logic. No `TODO`/`TBD` in shipped service code.

**Type consistency check:**
- `SchedulerTriggerEvent(jobName, runId, firedAtEpochMs)` and
  `SchedulerResultEvent(jobName, runId, status, durationMs, error)` used identically in
  Tasks 1, 7, 9, 10, 16 ✓
- `RunRecorder.recordFired(String, UUID, boolean)` / `recordResult(UUID, RunStatus, long, String)`
  consistent across Tasks 6, 7, 9, 10 ✓
- `SchedulerClient.schedule(JobSchedule)` / `delete(String)` consistent across Tasks 4, 7 ✓
- `JobService` methods `registerAll/pause/resume/disable/runNow` consistent across Tasks 7,
  8, 11 ✓
- `JobRunRepository.findByJobNameOrderByFiredAtDesc(String, Limit)` used in Tasks 3, 11 ✓

**Build-order note:** Task 1 must install `dapr-commons` (Step 5) before Task 2 compiles.
Each task's tests are independently runnable with `-Dtest=`; the full suite runs in Task 15.
```
