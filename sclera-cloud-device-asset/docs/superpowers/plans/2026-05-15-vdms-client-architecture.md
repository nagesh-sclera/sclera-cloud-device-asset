# VdmsClient Architecture Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the externally-exposed Dapr bridge endpoints from `sclera-cloud-device-asset` and replace them with an internal `VdmsClient` service that calls `vdms-service` via Dapr sidecar from inside the service layer.

**Architecture:** External HTTP calls hit device-asset endpoints as before. When device-asset needs to reach `vdms-service`, it uses a `VdmsClient` Spring bean that issues plain HTTP requests to the local Dapr sidecar (`http://localhost:${DAPR_HTTP_PORT:3500}/v1.0/invoke/vdms-service/method/...`). The sidecar routes the call to `vdms-service`. No Dapr SDK needed in service code — just RestTemplate. The vdms-service subscriber handlers and `/dapr/subscribe` registration remain untouched.

**Tech Stack:** Spring Boot 2.6.5, Java 17, RestTemplate, Dapr HTTP sidecar API (service invocation + pub/sub)

---

## File Map

| Action | Path | Responsibility |
|--------|------|----------------|
| DELETE | `src/main/java/io/sclera/controller/dapr/DaprBridgeController.java` | Remove externally-exposed Dapr bridge endpoints |
| CREATE | `src/main/java/io/sclera/client/VdmsClient.java` | Internal service that calls vdms-service via Dapr sidecar |

No pom.xml or application.yml changes required — `DAPR_HTTP_PORT` env var already wired in docker-compose.

---

## Task 1: Delete DaprBridgeController

**Files:**
- Delete: `src/main/java/io/sclera/controller/dapr/DaprBridgeController.java`

- [ ] **Step 1: Confirm nothing in device-asset calls DaprBridgeController methods directly**

Run from `sclera-cloud-device-asset/`:
```bash
grep -r "DaprBridgeController" src/main/java --include="*.java"
```
Expected: no output (the controller is only referenced by Spring's component scan, not injected anywhere).

- [ ] **Step 2: Delete the file**

```bash
rm src/main/java/io/sclera/controller/dapr/DaprBridgeController.java
```

Also remove the now-empty package directory if empty:
```bash
rmdir src/main/java/io/sclera/controller/dapr 2>/dev/null || true
```

- [ ] **Step 3: Verify the project compiles**

Run from `sclera-cloud-device-asset/`:
```bash
mvn compile -q
```
Expected: `BUILD SUCCESS` with no errors.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "remove DaprBridgeController — no external Dapr bridge endpoints"
```

---

## Task 2: Create VdmsClient

**Files:**
- Create: `src/main/java/io/sclera/client/VdmsClient.java`

This service wraps all calls to `vdms-service` via the Dapr sidecar HTTP API.  
- Service invocation URL pattern: `http://localhost:{DAPR_HTTP_PORT}/v1.0/invoke/vdms-service/method/{vdmsPath}`  
- Pub/Sub publish URL pattern: `http://localhost:{DAPR_HTTP_PORT}/v1.0/publish/pubsub/{topic}`

- [ ] **Step 1: Create the client package directory (if it doesn't exist)**

```bash
mkdir -p src/main/java/io/sclera/client
```

- [ ] **Step 2: Write `VdmsClient.java`**

Create `src/main/java/io/sclera/client/VdmsClient.java` with the following content:

```java
package io.sclera.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Internal client for vdms-service.
 *
 * Calls are routed via the local Dapr sidecar:
 *   GET  http://localhost:{DAPR_HTTP_PORT}/v1.0/invoke/vdms-service/method/{path}
 *   POST http://localhost:{DAPR_HTTP_PORT}/v1.0/publish/pubsub/{topic}
 *
 * Inject this bean wherever device-asset needs data from vdms-service.
 */
@Service
public class VdmsClient {

    private static final Logger log = LoggerFactory.getLogger(VdmsClient.class);
    private static final String VDMS_APP_ID = "vdms-service";
    private static final String PUBSUB_NAME = "pubsub";

    private final RestTemplate rest = new RestTemplate();

    // ── Service invocation ────────────────────────────────────────────────────

    /** GET /vdms/id → {"vdmsId": "..."} */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getVdmsId() {
        return invoke("vdms/id", Map.class);
    }

    /** GET /vdms/details → full VdmsDTO as map */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getVdmsDetails() {
        return invoke("vdms/details", Map.class);
    }

    /** GET /vdms/master → {"isMaster": 0|1} */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMaster() {
        return invoke("vdms/master", Map.class);
    }

    /** GET /vdms/has-secondary-device → {"hasSecondaryDevice": 0|1} */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getHasSecondaryDevice() {
        return invoke("vdms/has-secondary-device", Map.class);
    }

    /** GET /vdms/secondary-device-id → {"secondaryDeviceId": "..."} */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSecondaryDeviceId() {
        return invoke("vdms/secondary-device-id", Map.class);
    }

    /** GET /vdms/customer-org-id/{vdmsId} → {"customerOrgId": "..."} */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getCustomerOrgId(String vdmsId) {
        return invoke("vdms/customer-org-id/" + vdmsId, Map.class);
    }

    /** GET /vdms/sync-details-for-adc → subset VdmsDTO as map */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSyncDetailsForAdc() {
        return invoke("vdms/sync-details-for-adc", Map.class);
    }

    // ── Pub/Sub ───────────────────────────────────────────────────────────────

    /**
     * Publish an event to a Dapr topic.
     *
     * Topics consumed by vdms-service:
     *   vdms.update-property-details, vdms.update-customer-org-id,
     *   vdms.set-agent-permission, device.audit
     */
    public void publishEvent(String topic, Object payload) {
        String url = daprBaseUrl() + "/v1.0/publish/" + PUBSUB_NAME + "/" + topic;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(payload, headers);
        try {
            rest.postForEntity(url, request, Void.class);
            log.info("[VdmsClient] Published topic={}", topic);
        } catch (Exception e) {
            log.error("[VdmsClient] Failed to publish topic={}: {}", topic, e.getMessage());
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private <T> T invoke(String vdmsPath, Class<T> responseType) {
        String url = daprBaseUrl() + "/v1.0/invoke/" + VDMS_APP_ID + "/method/" + vdmsPath;
        try {
            log.debug("[VdmsClient] GET {}", url);
            ResponseEntity<T> response = rest.getForEntity(url, responseType);
            return response.getBody();
        } catch (Exception e) {
            log.error("[VdmsClient] Failed to invoke {}: {}", vdmsPath, e.getMessage());
            return null;
        }
    }

    private String daprBaseUrl() {
        String port = System.getenv("DAPR_HTTP_PORT");
        return "http://localhost:" + (port != null ? port : "3500");
    }
}
```

- [ ] **Step 3: Verify the project compiles**

Run from `sclera-cloud-device-asset/`:
```bash
mvn compile -q
```
Expected: `BUILD SUCCESS` with no errors.

- [ ] **Step 4: Confirm the bean is discovered**

Grep to verify it's in a package that Spring component-scans (Spring Boot scans `io.sclera.*` from the main application class):
```bash
grep -r "@SpringBootApplication\|@ComponentScan" src/main/java --include="*.java" | head -5
```
Expected: main application class is in `io.sclera` package, which means `io.sclera.client.VdmsClient` is automatically discovered.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/io/sclera/client/VdmsClient.java
git commit -m "add VdmsClient — internal Dapr sidecar HTTP client for vdms-service"
```

---

## Post-Implementation Verification

- [ ] Start the stack with `docker compose up --build`
- [ ] Confirm device-asset starts without errors (no missing DaprBridgeController bean)
- [ ] Confirm `/dapr/publish/...` and `/dapr/invoke/vdms-*` routes no longer respond (404)
- [ ] Confirm regular device endpoints (e.g. `GET /user/.../devices`) still work
- [ ] Confirm vdms-service subscribes and receives events when published via `VdmsClient.publishEvent()`
