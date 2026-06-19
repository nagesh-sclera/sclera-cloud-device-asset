# Workorder Service Integration & Stub Retirement — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the hardcoded `sclera-workorders` stub with the received full workorder service and prove its Dapr inter-service communication (audit pub/sub, device-asset & vdms service-invocation) end-to-end.

**Architecture:** Drop the received `Sclera-2.0-sclera-cloud-workorder-main` source into the existing `sclera-workorders/` module (keeping its `sclera-workorders` app-id @ 8094 and Maven wrapper), wire it into docker-compose + Postgres, then close the two reverse-side gaps its clients depend on: a new internal `ticket-sync` endpoint on `sclera-cloud-device-asset` and a new `user-action-log-events` pub/sub subscriber on `sclera-vdms-service`. Verify by running the service slice and smoking a few methods.

**Tech Stack:** Java 21, Spring Boot 4.0.x, PostgreSQL 16, Redis (cache), Dapr 1.12 sidecars (service-invocation + pub/sub), docker-compose, Maven (`./mvnw`).

## Global Constraints

- Service identity is fixed: **app-id `sclera-workorders`, HTTP port `8094`** (registry + compose unchanged).
- Module directory stays **`sclera-workorders/`**; keep its existing `mvnw`, `mvnw.cmd`, `.mvn/`.
- Java package for the workorder service is **`io.sclera.workorder`** (singular); the stub package `io.sclera.workorders` (plural) is removed entirely.
- Maven artifactId/version stays **`sclera-cloud-workorder` / `1.0.0`** (the Dockerfile copies `target/sclera-cloud-workorder-1.0.0.jar`).
- DB schema management stays **Hibernate `ddl-auto=update`** (no Flyway); database name **`workorder_db`** on the shared `postgres:16` instance (user `root` / pw `mypass123`).
- Topic names stay as shipped: **`user-action-log-events`** (publish), **`scheduler.trigger`** (subscribe). No renaming.
- Pub/sub component name is **`pubsub`**; Dapr sidecar HTTP port inside compose is **`3500`** (`DAPR_HTTP_PORT`).
- Build context for the workorder image must be **`./sclera-workorders`** (the received Dockerfile does `COPY pom.xml`/`COPY src` relative to context).
- Do not run `git push` or merge; verification is local `docker compose` build + run.
- `dapr-commons` is the platform's shared event/Dapr-subscriber library (`io.sclera.dapr.*`); reuse it for new subscriber code.

---

### Task 1: Swap stub → received workorder service (module builds & tests pass)

**Files:**
- Replace: `sclera-workorders/pom.xml` ← `Sclera-2.0-sclera-cloud-workorder-main/pom.xml`
- Replace: `sclera-workorders/Dockerfile` ← `Sclera-2.0-sclera-cloud-workorder-main/Dockerfile`
- Replace: `sclera-workorders/.dockerignore`, `sclera-workorders/README.md` ← received equivalents
- Replace: `sclera-workorders/src/` ← `Sclera-2.0-sclera-cloud-workorder-main/src/` (entire tree)
- Delete: stub package tree `sclera-workorders/src/main/java/io/sclera/workorders/`, stub `sclera-workorders/src/main/resources/topics.yaml`, stub `sclera-workorders/src/test/java/io/sclera/workorders/SkeletonContractTest.java`, stale `sclera-workorders/target/`
- Keep: `sclera-workorders/mvnw`, `sclera-workorders/mvnw.cmd`, `sclera-workorders/.mvn/`
- Delete (after copy): `Sclera-2.0-sclera-cloud-workorder-main/` (the delivery drop) — do this in Task 6 cleanup, not here.

**Interfaces:**
- Produces: a buildable `sclera-workorders` module whose code is `io.sclera.workorder.*`, artifact `sclera-cloud-workorder:1.0.0`, packaging a jar at `target/sclera-cloud-workorder-1.0.0.jar`.

- [ ] **Step 1: Remove stub sources, copy received sources in**

```bash
cd "sclera-workorders"
# wipe stub src + stale build, keep mvnw/.mvn
rm -rf src target
# bring received module content in (paths relative to repo root)
cp -r ../Sclera-2.0-sclera-cloud-workorder-main/src ./src
cp ../Sclera-2.0-sclera-cloud-workorder-main/pom.xml ./pom.xml
cp ../Sclera-2.0-sclera-cloud-workorder-main/Dockerfile ./Dockerfile
cp ../Sclera-2.0-sclera-cloud-workorder-main/.dockerignore ./.dockerignore
cp ../Sclera-2.0-sclera-cloud-workorder-main/README.md ./README.md
```

- [ ] **Step 2: Confirm artifact coordinates & Boot version**

Run: `grep -nE "<artifactId>sclera-cloud-workorder|<version>1.0.0|spring-boot-starter-parent|<version>4\\." pom.xml`
Expected: artifactId `sclera-cloud-workorder`, project `<version>1.0.0`, parent `spring-boot-starter-parent`.
If the parent `<version>` differs from the platform line (`4.0.6`), set it to `4.0.6` to match the rest of the monorepo. If the project version is not `1.0.0`, set it to `1.0.0` (the Dockerfile copies `sclera-cloud-workorder-1.0.0.jar`).

- [ ] **Step 3: Build & run the module's shipped test suite**

Run: `./mvnw -q clean test`
Expected: BUILD SUCCESS. The received suite (controllers, services, clients with H2, repositories) passes standalone — it does not need Dapr or Postgres.
If failures are version-API related (e.g. `@WebMvcTest`/`@DataJpaTest` moved packages in Boot 4), confirm the pom already declares `spring-boot-starter-webmvc-test` and `spring-boot-data-jpa-test` (it does) and that the parent version matches Step 2.

- [ ] **Step 4: Package the jar (matches the Dockerfile's expected name)**

Run: `./mvnw -q -DskipTests package && ls target/sclera-cloud-workorder-1.0.0.jar`
Expected: the jar exists at `target/sclera-cloud-workorder-1.0.0.jar`.

- [ ] **Step 5: Commit**

```bash
cd ..
git add sclera-workorders
git commit -m "feat(workorders): replace stub with received sclera-cloud-workorder service"
```

---

### Task 2: Wire the service into docker-compose & Postgres (boots in the stack)

**Files:**
- Modify: `docker-compose.yml` — `sclera-workorders` service block (build context + env)
- Modify: `docker-compose.override.yml` — dev override for `sclera-workorders` (if it sets profile/build)
- Create: `infra/postgres-init/20-workorder-db.sql`

**Interfaces:**
- Consumes: Task 1's image build (Dockerfile at `sclera-workorders/Dockerfile`, jar `sclera-cloud-workorder-1.0.0.jar`).
- Produces: a running `sclera-workorders` container listening on 8094 against database `workorder_db`, with its Dapr sidecar (`sclera-workorders-dapr`) already defined.

- [ ] **Step 1: Create the Postgres database for the service**

The shared `postgres:16` runs init scripts from `./infra/postgres-init`. Add a script creating `workorder_db`:

Create `infra/postgres-init/20-workorder-db.sql`:

```sql
-- Database for sclera-workorders (sclera-cloud-workorder service).
-- The shared postgres instance already owns role "root"; create the DB if absent.
SELECT 'CREATE DATABASE workorder_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'workorder_db')\gexec
```

Run: `ls infra/postgres-init/` to confirm the file naming sorts after the base init (use the existing files' numeric prefixes as the guide; pick a prefix that runs after the role/db that owns `root`).

- [ ] **Step 2: Fix the build context and add DB + app-port env to the compose service**

In `docker-compose.yml`, the `sclera-workorders:` service `build:` currently uses `context: .` + `dockerfile: sclera-workorders/Dockerfile`. The received Dockerfile copies `pom.xml`/`src` **relative to the context**, so the context must be the module dir. Change to:

```yaml
  sclera-workorders:
    build:
      context: ./sclera-workorders
      dockerfile: Dockerfile
    container_name: sclera-workorders
    environment:
      SPRING_PROFILES_ACTIVE: docker
      APP_PORT: "8094"
      DB_URL: jdbc:postgresql://postgres:5432/workorder_db
      DB_USER: root
      DB_PASSWORD: mypass123
      REDIS_HOST: redis
      REDIS_PORT: "6379"
      DAPR_HTTP_PORT: "3500"
      DAPR_GRPC_PORT: "50001"
      MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOWEDORIGINS: "http://localhost:3000,http://127.0.0.1:3000"
      MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOWEDMETHODS: "GET"
      JAVA_TOOL_OPTIONS: "-javaagent:/otel/agent.jar"
      OTEL_SERVICE_NAME: "sclera-workorders"
      OTEL_EXPORTER_OTLP_ENDPOINT: "http://otel-collector:4318"
      OTEL_TRACES_EXPORTER: "otlp"
      OTEL_METRICS_EXPORTER: "none"
      OTEL_LOGS_EXPORTER: "none"
      OTEL_PROPAGATORS: "tracecontext,baggage,b3"
    volumes:
      - ./infra/otel/opentelemetry-javaagent.jar:/otel/agent.jar:ro
    ports: ["8094:8094"]
    networks: [sclera-net]
    restart: on-failure
    depends_on: [postgres, redis]
```

Leave the `sclera-workorders-dapr` sidecar block unchanged (`--app-id sclera-workorders --app-port 8094 --dapr-http-port 3500 --resources-path /dapr/components`).

- [ ] **Step 3: Reconcile the dev override**

Run: `grep -nA15 "sclera-workorders" docker-compose.override.yml`
If the override sets `build.context`/`dockerfile` for `sclera-workorders`, update it the same way (`context: ./sclera-workorders`, `dockerfile: Dockerfile`) and set `SPRING_PROFILES_ACTIVE: development` if that's the override's purpose. If it only sets the profile, leave context to the base file. Keep it consistent with the base service.

- [ ] **Step 4: Build the image in isolation**

Run: `docker compose build sclera-workorders`
Expected: image builds; the final stage copies `sclera-cloud-workorder-1.0.0.jar` without error.

- [ ] **Step 5: Boot the service + its DB and check health**

Run:
```bash
docker compose up -d postgres redis sclera-workorders sclera-workorders-dapr
docker compose logs --tail=40 sclera-workorders
```
Expected: Hibernate creates the ticket/maximo tables in `workorder_db` (ddl-auto=update); app logs "Started" / listens on 8094. Verify: `curl -s http://localhost:8094/api/v1/workorder-service/actuator/health` → `{"status":"UP"}`.

- [ ] **Step 6: Commit**

```bash
git add docker-compose.yml docker-compose.override.yml infra/postgres-init/20-workorder-db.sql
git commit -m "chore(compose): wire sclera-workorders to postgres workorder_db and fix build context"
```

---

### Task 3: device-asset internal `ticket-sync` endpoint (the DeviceAssetClient target)

**Files:**
- Create: `sclera-cloud-device-asset/src/main/java/io/sclera/controller/internal/DeviceTicketSyncController.java`
- Test: `sclera-cloud-device-asset/src/test/java/io/sclera/controller/internal/DeviceTicketSyncControllerTest.java`
- Reference (do not modify): `sclera-cloud-device-asset/src/main/java/io/sclera/service/DeviceService.java:2174` (`updateDeviceTicketCount(String)`), `:2188` (`updateDeviceTicketStatus(String)`)

**Interfaces:**
- Consumes: `DeviceService.updateDeviceTicketCount(String device_id)` → `void`, `DeviceService.updateDeviceTicketStatus(String device_id)` → `void`.
- Produces: HTTP `PUT /api/v1/device-asset-service/internal/device/{deviceId}/ticket-sync` returning `204 No Content`. This is exactly the path `io.sclera.workorder.client.DeviceAssetClient.syncTicketStats` invokes via Dapr (`/invoke/sclera-cloud-device-asset/method/api/v1/device-asset-service/internal/device/{deviceId}/ticket-sync`).

- [ ] **Step 1: Confirm the base path convention (no global context-path)**

Run: `grep -rnE "RequestMapping|context-path" sclera-cloud-device-asset/src/main/java/io/sclera/controller sclera-cloud-device-asset/src/main/resources/application.yml | grep -i "device-asset-service\|context-path" | head`
Expected: device controllers carry `/api/v1/device-asset-service` in their own mappings and there is **no** `server.servlet.context-path`. (This is why the scheduler subscriber is at bare `/internal/scheduler-trigger`.) Use the full path in the new controller so the Dapr-invoked URL matches exactly.

- [ ] **Step 2: Write the failing test**

Create `sclera-cloud-device-asset/src/test/java/io/sclera/controller/internal/DeviceTicketSyncControllerTest.java`:

```java
package io.sclera.controller.internal;

import io.sclera.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class DeviceTicketSyncControllerTest {

    private final DeviceService deviceService = Mockito.mock(DeviceService.class);
    private final DeviceTicketSyncController controller = new DeviceTicketSyncController(deviceService);

    @Test
    void syncTicketStats_delegatesToServiceAndReturns204() {
        ResponseEntity<Void> response = controller.syncTicketStats("device-123");

        verify(deviceService).updateDeviceTicketCount("device-123");
        verify(deviceService).updateDeviceTicketStatus("device-123");
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `cd sclera-cloud-device-asset && ./mvnw -q -Dtest=DeviceTicketSyncControllerTest test`
Expected: compilation failure — `DeviceTicketSyncController` does not exist yet.

- [ ] **Step 4: Write the minimal controller**

Create `sclera-cloud-device-asset/src/main/java/io/sclera/controller/internal/DeviceTicketSyncController.java`:

```java
package io.sclera.controller.internal;

import io.sclera.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal endpoint invoked by sclera-workorders (via Dapr service-invocation) to
 * recompute a device's ticket count and ticket status after a ticket change.
 * Mirrors the monolith's in-process call; idempotent and side-effect only.
 *
 * Full path (no global context-path on this service):
 *   PUT /api/v1/device-asset-service/internal/device/{deviceId}/ticket-sync
 */
@RestController
public class DeviceTicketSyncController {

    private static final Logger log = LoggerFactory.getLogger(DeviceTicketSyncController.class);

    private final DeviceService deviceService;

    public DeviceTicketSyncController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PutMapping("/api/v1/device-asset-service/internal/device/{deviceId}/ticket-sync")
    public ResponseEntity<Void> syncTicketStats(@PathVariable String deviceId) {
        log.info("[ticket-sync] recomputing ticket count/status for device={}", deviceId);
        deviceService.updateDeviceTicketCount(deviceId);
        deviceService.updateDeviceTicketStatus(deviceId);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `cd sclera-cloud-device-asset && ./mvnw -q -Dtest=DeviceTicketSyncControllerTest test`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
cd ..
git add sclera-cloud-device-asset/src/main/java/io/sclera/controller/internal/DeviceTicketSyncController.java \
        sclera-cloud-device-asset/src/test/java/io/sclera/controller/internal/DeviceTicketSyncControllerTest.java
git commit -m "feat(device-asset): add internal ticket-sync endpoint for workorder service"
```

---

### Task 4: vdms-service subscriber for `user-action-log-events` (the UserActionLogClient target)

**Files:**
- Create: `dapr-commons/src/main/java/io/sclera/dapr/events/UserActionLogEvent.java`
- Create: `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/UserActionLogVdmsSubscriber.java`
- Test: `sclera-vdms-service/src/test/java/io/sclera/vdms/subscriber/UserActionLogVdmsSubscriberTest.java`
- Reference (do not modify): `sclera-vdms-service/.../subscriber/DeviceAuditVdmsSubscriber.java` (pattern), `.../model/UserActionLog.java` (entity), `.../repository/UserActionLogRepository.java`, received `.../workorder/dto/UserActionLogDTO.java` (wire format)

**Interfaces:**
- Consumes: the JSON published by `UserActionLogClient.addUserAction` — fields `email, type, action, message, status, subType, primaryId, vdmsId, requestId` to topic `user-action-log-events` on pubsub `pubsub`.
- Consumes: `DaprEventSubscriber<T>` base (`super(DaprClient, String topic)`, `ResponseEntity<Map<String,String>> onEvent(CloudEvent<T>)`, abstract `void handleEvent(T data)`) and `UserActionLogRepository extends JpaRepository<UserActionLog,String>`.
- Produces: a persisted `UserActionLog` row per event (`userEmail=email`, `type=type`, `action=action`, `status=status`, `message=message`, `affectedRecordId=primaryId`, `vdmsId=vdmsId`, generated `id`, `createdAt=now`).

- [ ] **Step 1: Add the typed event record to dapr-commons**

Create `dapr-commons/src/main/java/io/sclera/dapr/events/UserActionLogEvent.java`:

```java
package io.sclera.dapr.events;

/**
 * Audit entry published by sclera-workorders to the {@code user-action-log-events} topic;
 * consumed by vdms-service, which owns the user_action_log table.
 * Field names match io.sclera.workorder.dto.UserActionLogDTO so Jackson binds the CloudEvent data.
 */
public record UserActionLogEvent(
    String email,
    String type,
    String action,
    String message,
    String status,
    String subType,
    String primaryId,
    String vdmsId,
    String requestId) {
}
```

- [ ] **Step 2: Write the failing subscriber test**

Create `sclera-vdms-service/src/test/java/io/sclera/vdms/subscriber/UserActionLogVdmsSubscriberTest.java`:

```java
package io.sclera.vdms.subscriber;

import io.dapr.client.DaprClient;
import io.sclera.dapr.events.UserActionLogEvent;
import io.sclera.vdms.model.UserActionLog;
import io.sclera.vdms.repository.UserActionLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class UserActionLogVdmsSubscriberTest {

    private final DaprClient dapr = Mockito.mock(DaprClient.class);
    private final UserActionLogRepository repo = Mockito.mock(UserActionLogRepository.class);
    private final UserActionLogVdmsSubscriber subscriber = new UserActionLogVdmsSubscriber(dapr, repo);

    @Test
    void handleEvent_mapsDtoFieldsAndPersists() {
        UserActionLogEvent event = new UserActionLogEvent(
            "ops@sclera.com", "maximo", "ADD", "Created config",
            "success", "maximo_configuration", "cfg-1", "demo-vdms-001", "req-abc");

        subscriber.handleEvent(event);

        ArgumentCaptor<UserActionLog> captor = ArgumentCaptor.forClass(UserActionLog.class);
        verify(repo).save(captor.capture());
        UserActionLog saved = captor.getValue();
        assertThat(saved.getUserEmail()).isEqualTo("ops@sclera.com");
        assertThat(saved.getType()).isEqualTo("maximo");
        assertThat(saved.getAction()).isEqualTo("ADD");
        assertThat(saved.getStatus()).isEqualTo("success");
        assertThat(saved.getMessage()).isEqualTo("Created config");
        assertThat(saved.getAffectedRecordId()).isEqualTo("cfg-1");
        assertThat(saved.getVdmsId()).isEqualTo("demo-vdms-001");
        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getCreatedAt()).isNotNull();
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `cd sclera-vdms-service && ./mvnw -q -Dtest=UserActionLogVdmsSubscriberTest test`
Expected: compilation failure — `UserActionLogVdmsSubscriber` does not exist yet.

- [ ] **Step 4: Write the subscriber (mirror DeviceAuditVdmsSubscriber)**

Create `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/UserActionLogVdmsSubscriber.java`:

```java
package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.UserActionLogEvent;
import io.sclera.vdms.model.UserActionLog;
import io.sclera.vdms.repository.UserActionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Consumes audit entries published by sclera-workorders to {@code user-action-log-events}
 * and persists them into the user_action_log table (vdms-service owns audit storage).
 * Mirrors {@link DeviceAuditVdmsSubscriber}.
 */
@RestController
public class UserActionLogVdmsSubscriber extends DaprEventSubscriber<UserActionLogEvent> {

    private static final Logger log = LoggerFactory.getLogger(UserActionLogVdmsSubscriber.class);
    private final UserActionLogRepository repo;

    public UserActionLogVdmsSubscriber(DaprClient dapr, UserActionLogRepository repo) {
        super(dapr, "user-action-log-events");
        this.repo = repo;
    }

    @Topic(name = "user-action-log-events", pubsubName = "pubsub",
           deadLetterTopic = "user-action-log-events.dlq")
    @PostMapping("/vdms/user-action-log")
    public ResponseEntity<Map<String, String>> onUserActionLog(
            @RequestBody CloudEvent<UserActionLogEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(UserActionLogEvent data) {
        UserActionLog entry = new UserActionLog();
        entry.setId(UUID.randomUUID().toString());
        entry.setVdmsId(data.vdmsId() != null ? data.vdmsId() : "");
        entry.setUserEmail(data.email() != null ? data.email() : "system");
        entry.setType(data.type() != null ? data.type() : "workorder");
        entry.setAction(data.action() != null ? data.action() : "UNKNOWN");
        entry.setStatus(data.status() != null ? data.status() : "success");
        entry.setMessage(data.message() != null ? data.message() : "");
        entry.setAffectedRecordId(data.primaryId() != null ? data.primaryId() : "");
        entry.setCreatedAt(LocalDateTime.now());
        repo.save(entry);
        log.info("[Audit] Logged {}.{} for vdms={} primaryId={}",
            entry.getType(), entry.getAction(), entry.getVdmsId(), entry.getAffectedRecordId());
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `cd sclera-vdms-service && ./mvnw -q -Dtest=UserActionLogVdmsSubscriberTest test`
Expected: PASS. (If `dapr-commons` is consumed as a built dependency rather than a reactor module, run `cd dapr-commons && ./mvnw -q install` first so the new `UserActionLogEvent` is on vdms-service's classpath.)

- [ ] **Step 6: Commit**

```bash
cd ..
git add dapr-commons/src/main/java/io/sclera/dapr/events/UserActionLogEvent.java \
        sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/UserActionLogVdmsSubscriber.java \
        sclera-vdms-service/src/test/java/io/sclera/vdms/subscriber/UserActionLogVdmsSubscriberTest.java
git commit -m "feat(vdms): subscribe to user-action-log-events and persist audit entries"
```

---

### Task 5: Scheduler-trigger subscription + Dapr registry update

**Files:**
- Create: `sclera-cloud-device-asset/dapr/components/subscription-scheduler-trigger-workorder.yaml` (this is the LIVE resources dir mounted into every sidecar via `./sclera-cloud-device-asset/dapr:/dapr` + `--resources-path /dapr/components`; the root `dapr/components/` tree is NOT mounted)
- Modify: `dapr/APP_IDS.md` (Subscribes/Publishes for `sclera-workorders` — this is a doc, root path is correct)

**Interfaces:**
- Consumes: the received `io.sclera.workorder.scheduler.SchedulerTriggerSubscriber` route `POST /api/v1/workorder-service/internal/scheduler-demo` (filters events to `owner == "workorder"`).
- Produces: a declarative Dapr subscription scoped to app-id `sclera-workorders` so the shared sidecar delivers `scheduler.trigger` to that route. (The workorder service does not depend on the Dapr SDK, so it uses a declarative subscription rather than the `@Topic` programmatic style the Java services use.)

- [ ] **Step 1: Confirm the route's full path**

Run: `grep -rn "scheduler-demo\|PostMapping" sclera-workorders/src/main/java/io/sclera/workorder/scheduler/SchedulerTriggerSubscriber.java`
Expected: `@PostMapping("/internal/scheduler-demo")`. With the service context-path `/api/v1/workorder-service`, the full route is `/api/v1/workorder-service/internal/scheduler-demo`.

- [ ] **Step 2: Create the declarative subscription**

Create `sclera-cloud-device-asset/dapr/components/subscription-scheduler-trigger-workorder.yaml` (the live, sidecar-mounted components dir):

```yaml
apiVersion: dapr.io/v2alpha1
kind: Subscription
metadata:
  name: scheduler-trigger-workorder
spec:
  topic: scheduler.trigger
  pubsubname: pubsub
  routes:
    default: /api/v1/workorder-service/internal/scheduler-demo
  deadLetterTopic: scheduler.trigger.dlq
scopes:
  - sclera-workorders
```

Note: `scopes` restricts this subscription to the `sclera-workorders` sidecar, so it does not affect other apps that share `/dapr/components`.

- [ ] **Step 3: Update the app-id registry**

In `dapr/APP_IDS.md`, change the `sclera-workorders` row so **Subscribes** = `scheduler.trigger` and **Publishes** = `user-action-log-events`, and drop the "(skeleton)" label. Add a `user-action-log-events` line to the `## Topics` section: "published by sclera-workorders; consumed by vdms-service (persists user_action_log)."

- [ ] **Step 4: Commit**

```bash
git add dapr/components/subscription-scheduler-trigger-workorder.yaml dapr/APP_IDS.md
git commit -m "feat(dapr): subscribe sclera-workorders to scheduler.trigger; update app-id registry"
```

---

### Task 6: End-to-end verification of the Dapr flow & cleanup

**Files:**
- Delete: `Sclera-2.0-sclera-cloud-workorder-main/` (delivery drop, now integrated)
- No source changes — this task builds images, runs the slice, and smokes the flows.

**Interfaces:**
- Consumes: Tasks 1–5. Rebuilds images for the three changed services (`sclera-workorders`, `sclera-cloud-device-asset`, `sclera-vdms-service`).

- [ ] **Step 1: Rebuild the three changed images & bring up the slice**

Compose service names (verified): device-asset = `app` (sidecar `app-dapr`), vdms = `vdms-service` (`vdms-dapr`), scheduler = `sclera-scheduler` (`sclera-scheduler-dapr`), workorder = `sclera-workorders` (`sclera-workorders-dapr`). Sidecars use empty placement (`--placement-host-address ""`), so there is no `placement` service to start.

Run:
```bash
docker compose build sclera-workorders app vdms-service
docker compose up -d postgres redis \
  app app-dapr \
  vdms-service vdms-dapr \
  sclera-scheduler sclera-scheduler-dapr \
  sclera-workorders sclera-workorders-dapr
```
Expected: all containers reach healthy/running. **Sidecar gotcha:** if you recreate an app container, immediately `docker compose up -d --force-recreate <app>-dapr` (its sidecar binds to the app's network namespace; a recreated app orphans the old sidecar). After a Docker restart, `postgres`/`redis` have no restart policy — bring them up first and wait for the postgres healthcheck.

- [ ] **Step 2: Smoke — upsert a ticket (proves publish→subscribe AND invoke→remote app)**

Run a ticket upsert through the workorder service (use the request body/headers documented in the workorder README; tickets require `loggedInUser` and `vdms_id` query params). Example shape:
```bash
curl -i -X POST "http://localhost:8094/api/v1/workorder-service/ticket/upsertticket?loggedInUser=ops@sclera.com&vdms_id=demo-vdms-001" \
  -H "Content-Type: application/json" \
  -d '{ "deviceId": "device-123", "title": "Smoke test ticket", "type": "INCIDENT" }'
```
Expected: HTTP 200/201 with the persisted ticket.
Then assert each Dapr hop:
- Ticket row: `docker compose exec postgres psql -U root -d workorder_db -c "select id, device_id from ticket order by created_at desc limit 3;"` → the new ticket.
- **Publish→subscribe (workorder→vdms):** `docker compose logs --tail=50 vdms-service | grep "\[Audit\] Logged"` → an audit line; confirm the row lands wherever vdms persists `user_action_log`.
- **Invoke→remote app (workorder→device-asset):** `docker compose logs --tail=50 app | grep "\[ticket-sync\]"` → "recomputing ticket count/status for device=device-123".

- [ ] **Step 3: Smoke — VDMS service-invocation (workorder→vdms-service)**

Run: `curl -s "http://localhost:8094/api/v1/workorder-service/maximo/getvdmsdetails?loggedInUser=ops@sclera.com&vdms_id=demo-vdms-001"`
Expected: VDMS details returned (proves `VdmsClient` service-invocation succeeds). If the path/DTO mismatches vdms-service's actual controller, fix `VdmsClient`'s URI/DTO to match the real endpoint and rebuild `sclera-workorders` (see spec §8 — outbound contract verification).

- [ ] **Step 4: Smoke — scheduler trigger (optional, proves declarative subscription)**

Emit a `scheduler.trigger` event with `data.owner = "workorder"` (publish to the `pubsub` component, e.g. via the scheduler's normal flow or `dapr publish`).
Expected: `docker compose logs --tail=30 sclera-workorders | grep "scheduler-demo"` → "workorder job fired … hello from sclera-cloud-workorder".

- [ ] **Step 5: Confirm tracing routes through sidecars**

Open Jaeger at `http://localhost:16686`, select service `sclera-workorders`, and confirm the upsert trace shows spans crossing into `vdms-service` and `sclera-cloud-device-asset` via the Dapr sidecars (not direct app-to-app).

- [ ] **Step 6: Regression — changed modules still green**

Run:
```bash
( cd sclera-cloud-device-asset && ./mvnw -q test )   # baseline 622/0/4 (4 known Testcontainers IT failures w/o Docker)
( cd sclera-vdms-service && ./mvnw -q test )
( cd sclera-workorders && ./mvnw -q test )
```
Expected: device-asset matches the known baseline; vdms-service and workorders pass.

- [ ] **Step 7: Remove the delivery drop & commit**

```bash
rm -rf Sclera-2.0-sclera-cloud-workorder-main
git add -A
git commit -m "chore(workorders): remove integrated delivery drop; verified Dapr inter-service flow"
```

---

## Self-Review Notes

- **Spec coverage:** §3 swap → Task 1; §4 identity/config/DB → Tasks 1–2; §5a outbound verify → Task 6 Step 3; §5c device-asset endpoint → Task 3; §5b vdms subscriber → Task 4; §5d scheduler subscription → Task 5; §5e registry → Task 5; §6 build/compose/db-init → Task 2; §7 verification → Task 6.
- **Deferred-by-design (spec §1):** Flyway, topic renaming, dapr-commons reuse in the workorder service, fastjson removal — intentionally not tasks.
- **Type consistency:** `UserActionLogEvent` record component names (`email,type,action,message,status,subType,primaryId,vdmsId,requestId`) match `UserActionLogDTO`'s JSON; the subscriber maps `email→userEmail` and `primaryId→affectedRecordId` to the `UserActionLog` entity setters verified in the model. `DeviceService.updateDeviceTicketCount/Status(String)` signatures match the controller call sites.
- **Known verify-at-runtime items (carried into Task 6):** outbound `VdmsClient`/`SchedulerClient` exact paths vs the live controllers; the received `spring-boot-starter-parent` version vs platform 4.0.6; whether `dapr-commons` is a reactor module or an installed dependency (affects whether `install` is needed in Task 4 Step 5).
