# Dapr Pub/Sub Production Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all ad-hoc Dapr pub/sub code across `sclera-vdms-service`, `sclera-cloud-device-asset`, and `sclera-audit` with a shared `dapr-commons` library that guarantees idempotency, correct Dapr response semantics (SUCCESS/RETRY/DROP), typed DTOs, and dead-letter queue routing.

**Architecture:** A new `dapr-commons` Maven library provides an abstract `DaprEventSubscriber<T>` base class (idempotency + response codes) and `DaprEventPublisher` (result-returning publisher). Each service extends the base class for its topic, removes manual `/dapr/subscribe` endpoints, and switches to `@Topic` annotations with `deadLetterTopic` configured.

**Tech Stack:** Java 21, Spring Boot 4.0.6, Dapr SDK 1.12.0 (`dapr-sdk` + `dapr-sdk-springboot`), Maven, Mockito 5.x (via `spring-boot-starter-test`), reactor-core (transitive from Dapr SDK)

---

## File Map

### New files (dapr-commons module)
- `dapr-commons/pom.xml` — standalone library POM
- `dapr-commons/src/main/java/io/sclera/dapr/PublishResult.java` — record: `(boolean success, String eventId, String error)`
- `dapr-commons/src/main/java/io/sclera/dapr/DaprEventPublisher.java` — publisher wrapping `DaprClient.publishEvent()`, returns `PublishResult`
- `dapr-commons/src/main/java/io/sclera/dapr/DaprEventSubscriber.java` — abstract base class: idempotency, SUCCESS/RETRY/DROP, structured log
- `dapr-commons/src/main/java/io/sclera/dapr/events/DeviceAuditEvent.java` — typed DTO (record)
- `dapr-commons/src/main/java/io/sclera/dapr/events/VdmsPropertyUpdateEvent.java` — typed DTO (record)
- `dapr-commons/src/main/java/io/sclera/dapr/events/VdmsCustomerOrgEvent.java` — typed DTO (record)
- `dapr-commons/src/main/java/io/sclera/dapr/events/VdmsAgentPermissionEvent.java` — typed DTO (record)
- `dapr-commons/src/test/java/io/sclera/dapr/DaprEventSubscriberTest.java` — tests for idempotency, RETRY, DROP, SUCCESS paths
- `dapr-commons/src/test/java/io/sclera/dapr/DaprEventPublisherTest.java` — tests for success and failure publish paths

### New files (sclera-vdms-service)
- `sclera-vdms-service/src/main/java/io/sclera/vdms/config/DaprClientConfig.java` — `@Bean DaprClient`
- `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/VdmsPropertyUpdateSubscriber.java` — extends base, handles `vdms.update-property-details`
- `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/VdmsCustomerOrgSubscriber.java` — extends base, handles `vdms.update-customer-org-id`
- `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/VdmsAgentPermissionSubscriber.java` — extends base, handles `vdms.set-agent-permission`
- `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/DeviceAuditVdmsSubscriber.java` — extends base, handles `device.audit` (replaces `AuditSubscriber`)
- `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/VdmsDlqSubscriber.java` — logs DLQ events for all 4 topics

### Modified files (sclera-vdms-service)
- `sclera-vdms-service/pom.xml` — add `dapr-sdk`, `dapr-sdk-springboot`, `dapr-commons`, `spring-tx`
- `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/VdmsController.java` — remove `daprSubscribe()` method + unused imports

### Deleted files (sclera-vdms-service)
- `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/VdmsSubscriber.java` — replaced by 3 new subscriber classes
- `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/AuditSubscriber.java` — replaced by `DeviceAuditVdmsSubscriber`

### Modified files (sclera-cloud-device-asset)
- `sclera-cloud-device-asset/pom.xml` — add `dapr-commons` dependency
- `sclera-cloud-device-asset/src/main/java/io/sclera/client/VdmsClient.java` — remove `publishEvent()`, inject `DaprEventPublisher`
- `sclera-cloud-device-asset/src/main/java/io/sclera/service/UserActionLogService.java` — use `DaprEventPublisher` + handle `PublishResult`
- `sclera-cloud-device-asset/src/main/java/io/sclera/client/RabbitmqClient.java` — use `DaprEventPublisher` instead of `dapr.publishEvent()`
- `sclera-vdms-service/src/main/java/io/sclera/vdms/client/ScleraCloudDeviceClient.java` — remove `publishEvent()` method (publish calls replaced by `DaprEventPublisher`; service-invocation `invoke()` stays)

### Modified files (sclera-audit)
- `sclera-audit/pom.xml` — add `dapr-commons`
- `sclera-audit/src/main/java/io/sclera/audit/subscriber/DeviceAuditSubscriber.java` — extend base class, remove manual idempotency, inject `DaprClient` as Spring bean

### Dapr config
- `dapr/components/local/pubsub.yaml` — no changes needed (DLQ routing is declared in `@Topic` annotation)
- `dapr/components/k8s/pubsub.yaml` — no changes needed

---

## Task 1: Create `dapr-commons` module scaffold

**Files:**
- Create: `dapr-commons/pom.xml`
- Create: `dapr-commons/src/main/java/io/sclera/dapr/PublishResult.java`
- Create: `dapr-commons/src/main/java/io/sclera/dapr/events/DeviceAuditEvent.java`
- Create: `dapr-commons/src/main/java/io/sclera/dapr/events/VdmsPropertyUpdateEvent.java`
- Create: `dapr-commons/src/main/java/io/sclera/dapr/events/VdmsCustomerOrgEvent.java`
- Create: `dapr-commons/src/main/java/io/sclera/dapr/events/VdmsAgentPermissionEvent.java`

- [ ] **Step 1: Create `dapr-commons/pom.xml`**

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
    <artifactId>dapr-commons</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>dapr-commons</name>
    <description>Shared Dapr pub/sub building blocks for Sclera microservices</description>
    <properties>
        <java.version>21</java.version>
        <dapr.sdk.version>1.12.0</dapr.sdk.version>
    </properties>
    <dependencies>
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
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-tx</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Create `dapr-commons/src/main/java/io/sclera/dapr/PublishResult.java`**

```java
package io.sclera.dapr;

public record PublishResult(boolean success, String eventId, String error) {}
```

- [ ] **Step 3: Create event DTOs**

`dapr-commons/src/main/java/io/sclera/dapr/events/DeviceAuditEvent.java`:
```java
package io.sclera.dapr.events;

public record DeviceAuditEvent(
    String vdmsId,
    String deviceId,
    String action,
    String status,
    String message,
    String userEmail) {}
```

`dapr-commons/src/main/java/io/sclera/dapr/events/VdmsPropertyUpdateEvent.java`:
```java
package io.sclera.dapr.events;

public record VdmsPropertyUpdateEvent(String id, String address) {}
```

`dapr-commons/src/main/java/io/sclera/dapr/events/VdmsCustomerOrgEvent.java`:
```java
package io.sclera.dapr.events;

public record VdmsCustomerOrgEvent(String vdmsId, String customerOrgId) {}
```

`dapr-commons/src/main/java/io/sclera/dapr/events/VdmsAgentPermissionEvent.java`:
```java
package io.sclera.dapr.events;

public record VdmsAgentPermissionEvent(String vdmsId, String agentId, String permission) {}
```

- [ ] **Step 4: Commit scaffold**

```bash
git add dapr-commons/
git commit -m "feat(dapr-commons): scaffold module with pom and event DTOs"
```

---

## Task 2: Implement `DaprEventPublisher` with tests (TDD)

**Files:**
- Create: `dapr-commons/src/test/java/io/sclera/dapr/DaprEventPublisherTest.java`
- Create: `dapr-commons/src/main/java/io/sclera/dapr/DaprEventPublisher.java`

- [ ] **Step 1: Write the failing tests**

Create `dapr-commons/src/test/java/io/sclera/dapr/DaprEventPublisherTest.java`:

```java
package io.sclera.dapr;

import io.dapr.client.DaprClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DaprEventPublisherTest {

    @Mock
    private DaprClient daprClient;

    private DaprEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new DaprEventPublisher(daprClient);
    }

    @Test
    void successfulPublish_returnsSuccessResultWithEventId() {
        when(daprClient.publishEvent(eq("pubsub"), eq("test-topic"), any()))
            .thenReturn(Mono.empty());

        PublishResult result = publisher.publish("pubsub", "test-topic", Map.of("key", "val"));

        assertThat(result.success()).isTrue();
        assertThat(result.eventId()).isNotBlank();
        assertThat(result.error()).isNull();
    }

    @Test
    void failedPublish_returnsFailureResultWithErrorMessage() {
        when(daprClient.publishEvent(eq("pubsub"), eq("test-topic"), any()))
            .thenReturn(Mono.error(new RuntimeException("sidecar unavailable")));

        PublishResult result = publisher.publish("pubsub", "test-topic", Map.of("key", "val"));

        assertThat(result.success()).isFalse();
        assertThat(result.eventId()).isNotBlank();
        assertThat(result.error()).contains("sidecar unavailable");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd dapr-commons
mvn test -Dtest=DaprEventPublisherTest -q
```

Expected: FAIL — `DaprEventPublisher` does not exist yet.

- [ ] **Step 3: Implement `DaprEventPublisher`**

Create `dapr-commons/src/main/java/io/sclera/dapr/DaprEventPublisher.java`:

```java
package io.sclera.dapr;

import io.dapr.client.DaprClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DaprEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DaprEventPublisher.class);
    private final DaprClient dapr;

    public DaprEventPublisher(DaprClient dapr) {
        this.dapr = dapr;
    }

    public PublishResult publish(String pubsubName, String topic, Object payload) {
        String eventId = UUID.randomUUID().toString();
        try {
            dapr.publishEvent(pubsubName, topic, payload).block();
            log.info("Published event topic={} eventId={}", topic, eventId);
            return new PublishResult(true, eventId, null);
        } catch (Exception e) {
            log.error("Publish failed topic={} eventId={} error={}", topic, eventId, e.getMessage());
            return new PublishResult(false, eventId, e.getMessage());
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
cd dapr-commons
mvn test -Dtest=DaprEventPublisherTest -q
```

Expected: BUILD SUCCESS, 2 tests passed.

- [ ] **Step 5: Commit**

```bash
git add dapr-commons/src/
git commit -m "feat(dapr-commons): implement DaprEventPublisher with result type"
```

---

## Task 3: Implement `DaprEventSubscriber<T>` with tests (TDD)

**Files:**
- Create: `dapr-commons/src/test/java/io/sclera/dapr/DaprEventSubscriberTest.java`
- Create: `dapr-commons/src/main/java/io/sclera/dapr/DaprEventSubscriber.java`

- [ ] **Step 1: Write the failing tests**

Create `dapr-commons/src/test/java/io/sclera/dapr/DaprEventSubscriberTest.java`:

```java
package io.sclera.dapr;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DaprEventSubscriberTest {

    private static final String IDEMPOTENCY_STORE = "statestore-idempotency";
    private static final String EVENT_ID = "test-event-id-123";

    @Mock
    private DaprClient daprClient;

    private TestSubscriber subscriber;

    @BeforeEach
    void setUp() {
        subscriber = new TestSubscriber(daprClient);
    }

    @Test
    void duplicateEvent_returnsSuccess_withoutCallingHandleEvent() {
        State<Boolean> alreadyProcessed = new State<>(EVENT_ID, Boolean.TRUE, "etag", null, null);
        when(daprClient.getState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.class))
            .thenReturn(Mono.just(alreadyProcessed));

        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(EVENT_ID, "payload"));

        assertThat(response.getBody()).containsEntry("status", "SUCCESS");
        assertThat(subscriber.handleCalled).isFalse();
    }

    @Test
    void firstTimeSuccess_writesIdempotencyKey_returnsSuccess() {
        State<Boolean> notProcessed = new State<>(EVENT_ID, null, null, null, null);
        when(daprClient.getState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.class))
            .thenReturn(Mono.just(notProcessed));
        when(daprClient.saveState(eq(IDEMPOTENCY_STORE), eq(EVENT_ID), eq(Boolean.TRUE)))
            .thenReturn(Mono.empty());

        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(EVENT_ID, "payload"));

        assertThat(response.getBody()).containsEntry("status", "SUCCESS");
        assertThat(subscriber.handleCalled).isTrue();
        verify(daprClient).saveState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.TRUE);
    }

    @Test
    void transientError_returnsRetry_doesNotWriteIdempotencyKey() {
        State<Boolean> notProcessed = new State<>(EVENT_ID, null, null, null, null);
        when(daprClient.getState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.class))
            .thenReturn(Mono.just(notProcessed));
        subscriber.throwTransient = true;

        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(EVENT_ID, "payload"));

        assertThat(response.getBody()).containsEntry("status", "RETRY");
        verify(daprClient, never()).saveState(any(), any(), any());
    }

    @Test
    void permanentError_returnsDrop_doesNotWriteIdempotencyKey() {
        State<Boolean> notProcessed = new State<>(EVENT_ID, null, null, null, null);
        when(daprClient.getState(IDEMPOTENCY_STORE, EVENT_ID, Boolean.class))
            .thenReturn(Mono.just(notProcessed));
        subscriber.throwPermanent = true;

        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(EVENT_ID, "payload"));

        assertThat(response.getBody()).containsEntry("status", "DROP");
        verify(daprClient, never()).saveState(any(), any(), any());
    }

    @Test
    void nullEventId_processesWithoutIdempotencyCheck() {
        ResponseEntity<Map<String, String>> response = subscriber.onEvent(makeEvent(null, "payload"));

        assertThat(response.getBody()).containsEntry("status", "SUCCESS");
        assertThat(subscriber.handleCalled).isTrue();
        verify(daprClient, never()).getState(any(), any(), any());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private CloudEvent<String> makeEvent(String id, String data) {
        CloudEvent<String> event = new CloudEvent<>();
        event.setId(id);
        event.setData(data);
        return event;
    }

    static class TestSubscriber extends DaprEventSubscriber<String> {
        boolean handleCalled = false;
        boolean throwTransient = false;
        boolean throwPermanent = false;

        TestSubscriber(DaprClient dapr) {
            super(dapr, "test-topic");
        }

        @Override
        protected void handleEvent(String data) {
            handleCalled = true;
            if (throwPermanent) throw new IllegalArgumentException("invalid data");
            if (throwTransient) throw new RuntimeException("db connection lost");
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd dapr-commons
mvn test -Dtest=DaprEventSubscriberTest -q
```

Expected: FAIL — `DaprEventSubscriber` does not exist yet.

- [ ] **Step 3: Implement `DaprEventSubscriber`**

Create `dapr-commons/src/main/java/io/sclera/dapr/DaprEventSubscriber.java`:

```java
package io.sclera.dapr;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.dapr.client.domain.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public abstract class DaprEventSubscriber<T> {

    private static final Logger log = LoggerFactory.getLogger(DaprEventSubscriber.class);
    private static final String IDEMPOTENCY_STORE = "statestore-idempotency";

    private final DaprClient dapr;
    protected final String topic;

    protected DaprEventSubscriber(DaprClient dapr, String topic) {
        this.dapr = dapr;
        this.topic = topic;
    }

    public final ResponseEntity<Map<String, String>> onEvent(CloudEvent<T> event) {
        String eventId = event.getId();

        if (eventId != null && isAlreadyProcessed(eventId)) {
            log.info("Idempotency hit topic={} eventId={}", topic, eventId);
            return success();
        }

        try {
            handleEvent(event.getData());
            if (eventId != null) markProcessed(eventId);
            log.info("Processed event topic={} eventId={}", topic, eventId);
            return success();
        } catch (Exception e) {
            if (isPermanentError(e)) {
                log.error("Permanent failure topic={} eventId={} error={}", topic, eventId, e.getMessage(), e);
                return drop();
            }
            log.warn("Transient failure topic={} eventId={} error={}", topic, eventId, e.getMessage());
            return retry();
        }
    }

    protected abstract void handleEvent(T data);

    protected boolean isPermanentError(Exception e) {
        return e instanceof DataIntegrityViolationException
            || e instanceof IllegalArgumentException
            || e instanceof NullPointerException;
    }

    private boolean isAlreadyProcessed(String eventId) {
        try {
            State<Boolean> s = dapr.getState(IDEMPOTENCY_STORE, eventId, Boolean.class).block();
            return s != null && Boolean.TRUE.equals(s.getValue());
        } catch (Exception e) {
            log.debug("Idempotency lookup failed eventId={}: {}", eventId, e.getMessage());
            return false;
        }
    }

    private void markProcessed(String eventId) {
        try {
            dapr.saveState(IDEMPOTENCY_STORE, eventId, Boolean.TRUE).block();
        } catch (Exception e) {
            log.debug("Idempotency write failed eventId={}: {}", eventId, e.getMessage());
        }
    }

    private static ResponseEntity<Map<String, String>> success() {
        return ResponseEntity.ok(Map.of("status", "SUCCESS"));
    }

    private static ResponseEntity<Map<String, String>> retry() {
        return ResponseEntity.ok(Map.of("status", "RETRY"));
    }

    private static ResponseEntity<Map<String, String>> drop() {
        return ResponseEntity.ok(Map.of("status", "DROP"));
    }
}
```

- [ ] **Step 4: Run all dapr-commons tests**

```bash
cd dapr-commons
mvn test -q
```

Expected: BUILD SUCCESS, 7 tests passed (2 publisher + 5 subscriber).

- [ ] **Step 5: Commit**

```bash
git add dapr-commons/src/main/java/io/sclera/dapr/DaprEventSubscriber.java
git add dapr-commons/src/test/java/io/sclera/dapr/DaprEventSubscriberTest.java
git commit -m "feat(dapr-commons): implement DaprEventSubscriber base class with idempotency and response semantics"
```

---

## Task 4: Install `dapr-commons` to local Maven repository

**Files:** none (Maven install step)

- [ ] **Step 1: Install to local Maven repo**

```bash
cd dapr-commons
mvn install -DskipTests -q
```

Expected: BUILD SUCCESS. The artifact is now available as `io.sclera:dapr-commons:0.0.1-SNAPSHOT` in `~/.m2/repository/io/sclera/dapr-commons/`.

- [ ] **Step 2: Verify install**

```bash
ls ~/.m2/repository/io/sclera/dapr-commons/0.0.1-SNAPSHOT/
```

Expected: `dapr-commons-0.0.1-SNAPSHOT.jar` and `.pom` file are present.

---

## Task 5: Update `sclera-vdms-service` pom.xml

**Files:**
- Modify: `sclera-vdms-service/pom.xml`

- [ ] **Step 1: Add Dapr SDK and dapr-commons dependencies**

In `sclera-vdms-service/pom.xml`, add inside `<dependencies>`:

```xml
        <dependency>
            <groupId>io.dapr</groupId>
            <artifactId>dapr-sdk</artifactId>
            <version>1.12.0</version>
        </dependency>
        <dependency>
            <groupId>io.dapr</groupId>
            <artifactId>dapr-sdk-springboot</artifactId>
            <version>1.12.0</version>
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
```

- [ ] **Step 2: Verify the module compiles**

```bash
cd sclera-vdms-service
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add sclera-vdms-service/pom.xml
git commit -m "feat(vdms-service): add Dapr SDK and dapr-commons dependencies"
```

---

## Task 6: Add `DaprClientConfig` to `sclera-vdms-service`

**Files:**
- Create: `sclera-vdms-service/src/main/java/io/sclera/vdms/config/DaprClientConfig.java`

- [ ] **Step 1: Create the config bean**

```java
package io.sclera.vdms.config;

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

- [ ] **Step 2: Compile to verify**

```bash
cd sclera-vdms-service
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add sclera-vdms-service/src/main/java/io/sclera/vdms/config/DaprClientConfig.java
git commit -m "feat(vdms-service): add DaprClientConfig Spring bean"
```

---

## Task 7: Create new subscriber classes in `sclera-vdms-service`

**Files:**
- Create: `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/VdmsPropertyUpdateSubscriber.java`
- Create: `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/VdmsCustomerOrgSubscriber.java`
- Create: `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/VdmsAgentPermissionSubscriber.java`
- Create: `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/DeviceAuditVdmsSubscriber.java`
- Create: `sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/VdmsDlqSubscriber.java`

The business logic for each subscriber comes directly from the old `VdmsSubscriber.java` and `AuditSubscriber.java` — just moved into `handleEvent()`.

- [ ] **Step 1: Create `VdmsPropertyUpdateSubscriber`**

```java
package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.VdmsPropertyUpdateEvent;
import io.sclera.vdms.repository.VdmsJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsPropertyUpdateSubscriber extends DaprEventSubscriber<VdmsPropertyUpdateEvent> {

    private static final Logger log = LoggerFactory.getLogger(VdmsPropertyUpdateSubscriber.class);
    private final VdmsJpaRepository repo;

    public VdmsPropertyUpdateSubscriber(DaprClient dapr, VdmsJpaRepository repo) {
        super(dapr, "vdms.update-property-details");
        this.repo = repo;
    }

    @Topic(name = "vdms.update-property-details", pubsubName = "pubsub",
           deadLetterTopic = "vdms.update-property-details.dlq")
    @PostMapping("/vdms/update-property-details")
    public ResponseEntity<Map<String, String>> onPropertyUpdate(
            @RequestBody CloudEvent<VdmsPropertyUpdateEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(VdmsPropertyUpdateEvent data) {
        if (data.id() == null || data.address() == null) {
            throw new IllegalArgumentException("id and address are required");
        }
        repo.updateAddress(data.id(), data.address());
        log.info("Updated address for vdms {}", data.id());
    }
}
```

- [ ] **Step 2: Create `VdmsCustomerOrgSubscriber`**

```java
package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.VdmsCustomerOrgEvent;
import io.sclera.vdms.repository.VdmsJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsCustomerOrgSubscriber extends DaprEventSubscriber<VdmsCustomerOrgEvent> {

    private static final Logger log = LoggerFactory.getLogger(VdmsCustomerOrgSubscriber.class);
    private final VdmsJpaRepository repo;

    public VdmsCustomerOrgSubscriber(DaprClient dapr, VdmsJpaRepository repo) {
        super(dapr, "vdms.update-customer-org-id");
        this.repo = repo;
    }

    @Topic(name = "vdms.update-customer-org-id", pubsubName = "pubsub",
           deadLetterTopic = "vdms.update-customer-org-id.dlq")
    @PostMapping("/vdms/update-customer-org-id")
    public ResponseEntity<Map<String, String>> onCustomerOrgUpdate(
            @RequestBody CloudEvent<VdmsCustomerOrgEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(VdmsCustomerOrgEvent data) {
        if (data.vdmsId() == null || data.customerOrgId() == null) {
            throw new IllegalArgumentException("vdmsId and customerOrgId are required");
        }
        repo.updateCustomerOrgId(data.vdmsId(), data.customerOrgId());
        log.info("Updated customerOrgId for vdms {}", data.vdmsId());
    }
}
```

- [ ] **Step 3: Create `VdmsAgentPermissionSubscriber`**

```java
package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.VdmsAgentPermissionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsAgentPermissionSubscriber extends DaprEventSubscriber<VdmsAgentPermissionEvent> {

    private static final Logger log = LoggerFactory.getLogger(VdmsAgentPermissionSubscriber.class);

    public VdmsAgentPermissionSubscriber(DaprClient dapr) {
        super(dapr, "vdms.set-agent-permission");
    }

    @Topic(name = "vdms.set-agent-permission", pubsubName = "pubsub",
           deadLetterTopic = "vdms.set-agent-permission.dlq")
    @PostMapping("/vdms/set-agent-permission")
    public ResponseEntity<Map<String, String>> onAgentPermission(
            @RequestBody CloudEvent<VdmsAgentPermissionEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(VdmsAgentPermissionEvent data) {
        log.info("Received set-agent-permission vdmsId={} agentId={} permission={}",
            data.vdmsId(), data.agentId(), data.permission());
    }
}
```

- [ ] **Step 4: Create `DeviceAuditVdmsSubscriber`** (replaces `AuditSubscriber`)

The business logic comes from `AuditSubscriber.onDeviceAudit()` — it maps the DTO fields to `UserActionLog` and persists via `UserActionLogRepository`.

```java
package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.DeviceAuditEvent;
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

@RestController
public class DeviceAuditVdmsSubscriber extends DaprEventSubscriber<DeviceAuditEvent> {

    private static final Logger log = LoggerFactory.getLogger(DeviceAuditVdmsSubscriber.class);
    private final UserActionLogRepository repo;

    public DeviceAuditVdmsSubscriber(DaprClient dapr, UserActionLogRepository repo) {
        super(dapr, "device.audit");
        this.repo = repo;
    }

    @Topic(name = "device.audit", pubsubName = "pubsub",
           deadLetterTopic = "device.audit.dlq")
    @PostMapping("/vdms/device-audit")
    public ResponseEntity<Map<String, String>> onDeviceAudit(
            @RequestBody CloudEvent<DeviceAuditEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(DeviceAuditEvent data) {
        UserActionLog entry = new UserActionLog();
        entry.setId(UUID.randomUUID().toString());
        entry.setVdmsId(data.vdmsId() != null ? data.vdmsId() : "");
        entry.setUserEmail(data.userEmail() != null ? data.userEmail() : "system");
        entry.setType("device");
        entry.setAction(data.action() != null ? data.action() : "UNKNOWN");
        entry.setStatus(data.status() != null ? data.status() : "success");
        entry.setMessage(data.message() != null ? data.message() : "Device event");
        entry.setAffectedRecordId(data.deviceId() != null ? data.deviceId() : "");
        entry.setCreatedAt(LocalDateTime.now());
        repo.save(entry);
        log.info("[Audit] Logged device.{} for vdms={} deviceId={}",
            entry.getAction(), entry.getVdmsId(), entry.getAffectedRecordId());
    }
}
```

- [ ] **Step 5: Create `VdmsDlqSubscriber`**

```java
package io.sclera.vdms.subscriber;

import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VdmsDlqSubscriber {

    private static final Logger log = LoggerFactory.getLogger(VdmsDlqSubscriber.class);

    @Topic(name = "vdms.update-property-details.dlq", pubsubName = "pubsub")
    @PostMapping("/vdms/dlq/update-property-details")
    public ResponseEntity<Void> onPropertyUpdateDlq(
            @RequestBody CloudEvent<Map<String, Object>> event) {
        log.error("DLQ: vdms.update-property-details permanently failed eventId={} data={}",
            event.getId(), event.getData());
        return ResponseEntity.ok().build();
    }

    @Topic(name = "vdms.update-customer-org-id.dlq", pubsubName = "pubsub")
    @PostMapping("/vdms/dlq/update-customer-org-id")
    public ResponseEntity<Void> onCustomerOrgDlq(
            @RequestBody CloudEvent<Map<String, Object>> event) {
        log.error("DLQ: vdms.update-customer-org-id permanently failed eventId={} data={}",
            event.getId(), event.getData());
        return ResponseEntity.ok().build();
    }

    @Topic(name = "vdms.set-agent-permission.dlq", pubsubName = "pubsub")
    @PostMapping("/vdms/dlq/set-agent-permission")
    public ResponseEntity<Void> onAgentPermissionDlq(
            @RequestBody CloudEvent<Map<String, Object>> event) {
        log.error("DLQ: vdms.set-agent-permission permanently failed eventId={} data={}",
            event.getId(), event.getData());
        return ResponseEntity.ok().build();
    }

    @Topic(name = "device.audit.dlq", pubsubName = "pubsub")
    @PostMapping("/vdms/dlq/device-audit")
    public ResponseEntity<Void> onDeviceAuditDlq(
            @RequestBody CloudEvent<Map<String, Object>> event) {
        log.error("DLQ: device.audit permanently failed eventId={} data={}",
            event.getId(), event.getData());
        return ResponseEntity.ok().build();
    }
}
```

- [ ] **Step 6: Compile the new classes**

```bash
cd sclera-vdms-service
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add sclera-vdms-service/src/main/java/io/sclera/vdms/subscriber/
git commit -m "feat(vdms-service): add production-grade subscriber classes extending DaprEventSubscriber"
```

---

## Task 8: Delete old subscriber classes and remove manual `/dapr/subscribe` from `sclera-vdms-service`

**Files:**
- Delete: `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/VdmsSubscriber.java`
- Delete: `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/AuditSubscriber.java`
- Modify: `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/VdmsController.java`

- [ ] **Step 1: Delete the old subscriber files**

```bash
git rm sclera-vdms-service/src/main/java/io/sclera/vdms/controller/VdmsSubscriber.java
git rm sclera-vdms-service/src/main/java/io/sclera/vdms/controller/AuditSubscriber.java
```

- [ ] **Step 2: Remove `daprSubscribe()` from `VdmsController`**

In `sclera-vdms-service/src/main/java/io/sclera/vdms/controller/VdmsController.java`, remove lines 83–93 (the `daprSubscribe()` method) and remove the now-unused imports: `java.util.Arrays`, `java.util.List`.

The final `VdmsController.java` should look like this (only the REST query endpoints remain):

```java
package io.sclera.vdms.controller;

import io.sclera.vdms.dto.VdmsDTO;
import io.sclera.vdms.model.Vdms;
import io.sclera.vdms.repository.VdmsJpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
public class VdmsController {

    private final VdmsJpaRepository repo;

    public VdmsController(VdmsJpaRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/vdms/id")
    public ResponseEntity<Map<String, String>> getVdmsId() {
        Optional<Vdms> opt = repo.findFirst();
        return opt.map(v -> ResponseEntity.ok(Map.of("vdmsId", v.getId())))
                  .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vdms/details")
    public ResponseEntity<VdmsDTO> getDetails() {
        return repo.findFirst().map(v -> ResponseEntity.ok(toFullDto(v)))
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vdms/master")
    public ResponseEntity<Map<String, Integer>> getMaster() {
        return repo.findFirst()
                   .map(v -> ResponseEntity.ok(Map.of("isMaster", v.getIs_master() != null ? v.getIs_master() : 0)))
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vdms/has-secondary-device")
    public ResponseEntity<Map<String, Integer>> getHasSecondaryDevice() {
        return repo.findFirst()
                   .map(v -> ResponseEntity.ok(Map.of("hasSecondaryDevice", v.getHas_secondary_device() != null ? v.getHas_secondary_device() : 0)))
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/vdms/secondary-device-id")
    public ResponseEntity<Map<String, String>> getSecondaryDeviceId() {
        Optional<Vdms> opt = repo.findFirst();
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        String secId = opt.get().getSecondary_device_id();
        return ResponseEntity.ok(Map.of("secondaryDeviceId", secId != null ? secId : ""));
    }

    @GetMapping("/vdms/customer-org-id/{vdmsId}")
    public ResponseEntity<Map<String, String>> getCustomerOrgId(@PathVariable String vdmsId) {
        Optional<Vdms> opt = repo.findById(vdmsId);
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        String orgId = opt.get().getCustomer_org_id();
        return ResponseEntity.ok(Map.of("customerOrgId", orgId != null ? orgId : ""));
    }

    @GetMapping("/vdms/sync-details-for-adc")
    public ResponseEntity<VdmsDTO> getSyncDetailsForAdc() {
        Optional<Vdms> opt = repo.findFirst();
        if (!opt.isPresent()) return ResponseEntity.notFound().build();
        Vdms v = opt.get();
        VdmsDTO dto = new VdmsDTO();
        dto.setId(v.getId());
        dto.setCustomer_org_id(v.getCustomer_org_id());
        dto.setAdc_configuration_id(v.getAdc_configuration_id());
        dto.setZip(v.getZip());
        return ResponseEntity.ok(dto);
    }

    private VdmsDTO toFullDto(Vdms v) {
        VdmsDTO dto = new VdmsDTO();
        dto.setId(v.getId()); dto.setProperty_name(v.getProperty_name());
        dto.setActivation_status(v.getActivation_status()); dto.setStatus(v.getStatus());
        dto.setLocation(v.getLocation()); dto.setTimezone(v.getTimezone());
        dto.setActivation_timestamp(v.getActivation_timestamp()); dto.setDeployment_type(v.getDeployment_type());
        dto.setAddress(v.getAddress()); dto.setCity(v.getCity());
        dto.setCountry(v.getCountry()); dto.setState(v.getState());
        dto.setZip(v.getZip()); dto.setImage_url(v.getImage_url());
        dto.setLatitude(v.getLatitude()); dto.setLongitude(v.getLongitude());
        dto.setRegion(v.getRegion()); dto.setCustomer_org_id(v.getCustomer_org_id());
        dto.setAdc_configuration_id(v.getAdc_configuration_id()); dto.setIs_master(v.getIs_master());
        dto.setHas_secondary_device(v.getHas_secondary_device()); dto.setSecondary_device_id(v.getSecondary_device_id());
        dto.setMaster_ip(v.getMaster_ip()); dto.setSlave_ip(v.getSlave_ip());
        return dto;
    }
}
```

- [ ] **Step 3: Compile to verify everything still builds**

```bash
cd sclera-vdms-service
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add sclera-vdms-service/src/main/java/io/sclera/vdms/controller/VdmsController.java
git commit -m "refactor(vdms-service): remove manual daprSubscribe() and old subscriber classes — replaced by @Topic-annotated subscribers"
```

---

## Task 9: Remove `publishEvent()` from `ScleraCloudDeviceClient` in `sclera-vdms-service`

**Files:**
- Modify: `sclera-vdms-service/src/main/java/io/sclera/vdms/client/ScleraCloudDeviceClient.java`

The `invoke()` method (service invocation) stays. Only the `publishEvent()` method is removed since it used raw HTTP and is replaced by callers using `DaprEventPublisher` directly.

- [ ] **Step 1: Remove `publishEvent()` from `ScleraCloudDeviceClient`**

Replace the full file content with the service-invocation-only version:

```java
package io.sclera.vdms.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ScleraCloudDeviceClient {

    private static final Logger log = LoggerFactory.getLogger(ScleraCloudDeviceClient.class);
    private static final String DEVICE_ASSET_APP_ID = "sclera-cloud-device-asset";

    private final RestTemplate rest = new RestTemplate();
    private final String daprBaseUrl = "http://localhost:" +
        (System.getenv("DAPR_HTTP_PORT") != null ? System.getenv("DAPR_HTTP_PORT") : "3500");

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDeviceCount(String username, String vdmsId, String dockerName) {
        String path = "user/" + username + "/vdms/" + vdmsId + "/docker/" + dockerName + "/getdevicecount";
        return invoke(path, Map.class);
    }

    public Object getDevices(String username, String vdmsId, String dockerName) {
        String path = "user/" + username + "/vdms/" + vdmsId + "/docker/" + dockerName + "/devices";
        return invoke(path, Object.class);
    }

    private <T> T invoke(String path, Class<T> responseType) {
        String url = daprBaseUrl + "/v1.0/invoke/" + DEVICE_ASSET_APP_ID + "/method/" + path;
        log.info("[Dapr sidecar →] invoke  path={} | url={}", path, url);
        ResponseEntity<T> response = rest.getForEntity(url, responseType);
        log.info("[Dapr sidecar ←] respond path={} | status={}", path, response.getStatusCode());
        return response.getBody();
    }
}
```

- [ ] **Step 2: Compile to verify**

```bash
cd sclera-vdms-service
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add sclera-vdms-service/src/main/java/io/sclera/vdms/client/ScleraCloudDeviceClient.java
git commit -m "refactor(vdms-service): remove raw HTTP publishEvent from ScleraCloudDeviceClient"
```

---

## Task 10: Migrate `sclera-cloud-device-asset` publisher to `DaprEventPublisher`

**Files:**
- Modify: `sclera-cloud-device-asset/pom.xml`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/client/VdmsClient.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/service/UserActionLogService.java`
- Modify: `sclera-cloud-device-asset/src/main/java/io/sclera/client/RabbitmqClient.java`

- [ ] **Step 1: Add `dapr-commons` to `sclera-cloud-device-asset/pom.xml`**

In `sclera-cloud-device-asset/pom.xml`, inside `<dependencies>`, add after the existing `dapr-sdk-springboot` entry (around line 451):

```xml
        <dependency>
            <groupId>io.sclera</groupId>
            <artifactId>dapr-commons</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```

- [ ] **Step 2: Remove `publishEvent()` from `VdmsClient` and inject `DaprEventPublisher`**

Replace `sclera-cloud-device-asset/src/main/java/io/sclera/client/VdmsClient.java` with this version (the `readThroughCache` logic is unchanged; only `publishEvent` is removed and `DaprEventPublisher` is injected):

```java
package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.dapr.client.domain.State;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class VdmsClient {

    private static final Logger log = LoggerFactory.getLogger(VdmsClient.class);
    private static final String VDMS_APP_ID = "vdms-service";
    private static final String PUBSUB_NAME = "pubsub";
    private static final String CACHE_STORE = "statestore-vdmscache";

    private final DaprClient dapr;
    private final DaprEventPublisher publisher;

    public VdmsClient(DaprClient dapr, DaprEventPublisher publisher) {
        this.dapr = dapr;
        this.publisher = publisher;
    }

    // ── Service invocation (cached) ───────────────────────────────────────────

    public Map<String, Object> getVdmsId() {
        return readThroughCache("vdms-id", "vdms/id");
    }

    public Map<String, Object> getVdmsDetails() {
        return readThroughCache("vdms-details", "vdms/details");
    }

    public Map<String, Object> getMaster() {
        return readThroughCache("vdms-master", "vdms/master");
    }

    public Map<String, Object> getHasSecondaryDevice() {
        return readThroughCache("vdms-has-secondary-device", "vdms/has-secondary-device");
    }

    public Map<String, Object> getSecondaryDeviceId() {
        return readThroughCache("vdms-secondary-device-id", "vdms/secondary-device-id");
    }

    public Map<String, Object> getCustomerOrgId(String vdmsId) {
        return readThroughCache("vdms-customer-org-id:" + vdmsId,
                                "vdms/customer-org-id/" + vdmsId);
    }

    public Map<String, Object> getSyncDetailsForAdc() {
        return readThroughCache("vdms-sync-details-for-adc", "vdms/sync-details-for-adc");
    }

    // ── Pub/Sub ───────────────────────────────────────────────────────────────

    public PublishResult publishEvent(String topic, Object payload) {
        return publisher.publish(PUBSUB_NAME, topic, payload);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Map<String, Object> readThroughCache(String cacheKey, String vdmsPath) {
        try {
            State cached = dapr.getState(CACHE_STORE, cacheKey, Map.class).block();
            if (cached != null && cached.getValue() != null) {
                return (Map<String, Object>) cached.getValue();
            }
        } catch (Exception e) {
            log.debug("VdmsClient cache lookup failed for {}: {}", cacheKey, e.getMessage());
        }
        Map<String, Object> fresh;
        try {
            fresh = (Map<String, Object>) dapr.invokeMethod(
                VDMS_APP_ID, vdmsPath, null, HttpExtension.GET, Map.class
            ).block();
        } catch (Exception e) {
            log.warn("VdmsClient invoke failed for {}: {}", vdmsPath, e.getMessage());
            return null;
        }
        if (fresh != null) {
            try {
                dapr.saveState(CACHE_STORE, cacheKey, fresh).block();
            } catch (Exception e) {
                log.debug("VdmsClient cache write failed for {}: {}", cacheKey, e.getMessage());
            }
        }
        return fresh;
    }
}
```

- [ ] **Step 3: Update `UserActionLogService` to handle `PublishResult`**

Replace `sclera-cloud-device-asset/src/main/java/io/sclera/service/UserActionLogService.java`:

```java
package io.sclera.service;

import io.sclera.client.VdmsClient;
import io.sclera.dapr.PublishResult;
import io.sclera.dapr.events.DeviceAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserActionLogService {

    private static final Logger log = LoggerFactory.getLogger(UserActionLogService.class);

    @Autowired
    private VdmsClient vdmsClient;

    private volatile String cachedVdmsId;

    public void addUserAction(String username, String type, String action,
                              String message, String status, String subType, String recordId) {
        DeviceAuditEvent event = new DeviceAuditEvent(
            resolveVdmsId(),
            recordId != null ? recordId : "",
            action,
            status,
            message,
            username != null ? username : "system"
        );
        log.info("[AuditLog] publishing device.audit | action={} device={} user={} vdmsId={}",
            action, event.deviceId(), event.userEmail(), event.vdmsId());

        PublishResult result = vdmsClient.publishEvent("device.audit", event);
        if (!result.success()) {
            log.error("[AuditLog] Publish failed action={} eventId={} error={}",
                action, result.eventId(), result.error());
        }
    }

    public void batchUpdateUserActionLogs(List<UserActionLogDTO> logs) {
        if (logs == null) return;
        for (UserActionLogDTO entry : logs) {
            addUserAction(entry.getEmail(), entry.getType(), entry.getAction(),
                          entry.getMessage(), entry.getStatus(),
                          entry.getSub_type(), entry.getPrimary_id());
        }
    }

    private String resolveVdmsId() {
        if (cachedVdmsId == null) {
            try {
                var result = vdmsClient.getVdmsId();
                if (result != null && result.get("vdmsId") != null) {
                    cachedVdmsId = (String) result.get("vdmsId");
                }
            } catch (Exception e) {
                log.warn("[UserActionLogService] Could not fetch vdmsId: {}", e.getMessage());
            }
        }
        return cachedVdmsId != null ? cachedVdmsId : "";
    }
}
```

- [ ] **Step 4: Update `RabbitmqClient` to use `DaprEventPublisher`**

Read `sclera-cloud-device-asset/src/main/java/io/sclera/client/RabbitmqClient.java` first to understand what it publishes, then replace its `publishEvent` call with `DaprEventPublisher`. The class uses `dapr.publishEvent()` directly. Inject `DaprEventPublisher` and delegate to it:

```java
package io.sclera.client;

import io.dapr.client.DaprClient;
import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RabbitmqClient {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqClient.class);
    private static final String PUBSUB_NAME = "pubsub";
    private static final String TOPIC_DEVICE_EVENT = "rabbitmq.device-event";
    private static final String TOPIC_MEASURING_INSTRUMENT = "rabbitmq.measuring-instrument-data";

    private final DaprEventPublisher publisher;

    public RabbitmqClient(DaprClient dapr, DaprEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void rabbitmqDeviceEvent(Map<String, Object> evt) {
        PublishResult result = publisher.publish(PUBSUB_NAME, TOPIC_DEVICE_EVENT, evt);
        if (!result.success()) {
            log.error("RabbitmqClient device-event publish failed eventId={} error={}",
                result.eventId(), result.error());
        }
    }

    public void rabbitmqMeasuringInstrumentData(Map<String, Object> evt) {
        PublishResult result = publisher.publish(PUBSUB_NAME, TOPIC_MEASURING_INSTRUMENT, evt);
        if (!result.success()) {
            log.error("RabbitmqClient measuring-instrument publish failed eventId={} error={}",
                result.eventId(), result.error());
        }
    }
}
```

**Note:** The `DaprClient dapr` constructor parameter in `RabbitmqClient` is kept so Spring can inject it — but this class no longer uses `dapr` directly. If there's no other usage of `dapr` in this class, remove the parameter entirely and inject only `DaprEventPublisher`.

- [ ] **Step 5: Compile `sclera-cloud-device-asset`**

```bash
cd sclera-cloud-device-asset
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add sclera-cloud-device-asset/pom.xml
git add sclera-cloud-device-asset/src/main/java/io/sclera/client/VdmsClient.java
git add sclera-cloud-device-asset/src/main/java/io/sclera/service/UserActionLogService.java
git add sclera-cloud-device-asset/src/main/java/io/sclera/client/RabbitmqClient.java
git commit -m "feat(cloud-device-asset): replace fire-and-forget publishEvent with DaprEventPublisher returning PublishResult"
```

---

## Task 11: Migrate `sclera-audit` `DeviceAuditSubscriber` to base class

**Files:**
- Modify: `sclera-audit/pom.xml`
- Modify: `sclera-audit/src/main/java/io/sclera/audit/subscriber/DeviceAuditSubscriber.java`

The `sclera-audit` CLAUDE.md says it is a skeleton with no DB. So `handleEvent()` just logs the event — the base class handles idempotency for us.

- [ ] **Step 1: Add `dapr-commons` to `sclera-audit/pom.xml`**

In `sclera-audit/pom.xml`, inside `<dependencies>`, add after the `dapr-sdk-springboot` entry:

```xml
        <dependency>
            <groupId>io.sclera</groupId>
            <artifactId>dapr-commons</artifactId>
            <version>0.0.1-SNAPSHOT</version>
        </dependency>
```

- [ ] **Step 2: Add `DaprClientConfig` to `sclera-audit`**

Create `sclera-audit/src/main/java/io/sclera/audit/config/DaprClientConfig.java`:

```java
package io.sclera.audit.config;

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

- [ ] **Step 3: Rewrite `DeviceAuditSubscriber` to extend base class**

Replace `sclera-audit/src/main/java/io/sclera/audit/subscriber/DeviceAuditSubscriber.java`:

```java
package io.sclera.audit.subscriber;

import io.dapr.Topic;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.CloudEvent;
import io.sclera.dapr.DaprEventSubscriber;
import io.sclera.dapr.events.DeviceAuditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DeviceAuditSubscriber extends DaprEventSubscriber<DeviceAuditEvent> {

    private static final Logger log = LoggerFactory.getLogger(DeviceAuditSubscriber.class);

    public DeviceAuditSubscriber(DaprClient dapr) {
        super(dapr, "device.audit-recorded");
    }

    @Topic(name = "device.audit-recorded", pubsubName = "pubsub",
           deadLetterTopic = "device.audit-recorded.dlq")
    @PostMapping("/internal/device-audit")
    public ResponseEntity<Map<String, String>> onDeviceAudit(
            @RequestBody CloudEvent<DeviceAuditEvent> event) {
        return onEvent(event);
    }

    @Override
    protected void handleEvent(DeviceAuditEvent data) {
        log.info("[audit] device.{} vdms={} device={} user={}",
            data.action(), data.vdmsId(), data.deviceId(), data.userEmail());
    }
}
```

- [ ] **Step 4: Compile `sclera-audit`**

```bash
cd sclera-audit
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add sclera-audit/pom.xml
git add sclera-audit/src/main/java/io/sclera/audit/config/DaprClientConfig.java
git add sclera-audit/src/main/java/io/sclera/audit/subscriber/DeviceAuditSubscriber.java
git commit -m "feat(audit): migrate DeviceAuditSubscriber to DaprEventSubscriber base class"
```

---

## Task 12: Final build verification

- [ ] **Step 1: Build all three affected services**

```bash
cd dapr-commons && mvn test -q && cd ..
cd sclera-vdms-service && mvn compile -q && cd ..
cd sclera-cloud-device-asset && mvn compile -q && cd ..
cd sclera-audit && mvn compile -q && cd ..
```

Expected: All four: BUILD SUCCESS.

- [ ] **Step 2: Run dapr-commons tests one final time**

```bash
cd dapr-commons
mvn test -q
```

Expected: BUILD SUCCESS, 7 tests passed.

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "chore: production Dapr pub/sub hardening complete across all services"
```

---

## Self-Review Checklist

**Spec coverage:**
- ✅ `DaprEventSubscriber<T>` base class with idempotency, SUCCESS/RETRY/DROP → Tasks 3, 7, 11
- ✅ `DaprEventPublisher` returning `PublishResult` → Task 2, 10
- ✅ Typed event DTOs (4 records) → Task 1
- ✅ `@Topic` annotations with `deadLetterTopic` replacing manual `/dapr/subscribe` → Tasks 7, 8
- ✅ DLQ subscribers logging permanently-failed events → Task 7 (VdmsDlqSubscriber)
- ✅ `sclera-cloud-device-asset` publisher migrated → Task 10
- ✅ `sclera-audit` skeleton migrated → Task 11
- ✅ `ScleraCloudDeviceClient.publishEvent()` removed → Task 9

**Placeholder scan:** No TBDs or unimplemented steps. All code blocks are complete.

**Type consistency:**
- `DaprEventSubscriber<T>` → `onEvent(CloudEvent<T>)` → `ResponseEntity<Map<String, String>>` — used consistently in Tasks 3, 7, 11
- `PublishResult(boolean success, String eventId, String error)` — defined in Task 1, returned in Task 2, consumed in Tasks 10, 11
- Event DTO field names match publisher payloads:
  - `DeviceAuditEvent` fields (`vdmsId`, `deviceId`, `action`, `status`, `message`, `userEmail`) match `UserActionLogService` payload keys ✅
  - `VdmsPropertyUpdateEvent(id, address)` matches `VdmsSubscriber.java` data keys (`id`, `address`) ✅
  - `VdmsCustomerOrgEvent(vdmsId, customerOrgId)` matches `VdmsSubscriber.java` data keys ✅
