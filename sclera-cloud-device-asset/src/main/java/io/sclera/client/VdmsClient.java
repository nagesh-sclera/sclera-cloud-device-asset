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
    private final String daprBaseUrl = "http://localhost:" +
        (System.getenv("DAPR_HTTP_PORT") != null ? System.getenv("DAPR_HTTP_PORT") : "3500");

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
     *
     * <p>Best-effort: exceptions are logged and swallowed. Dapr/Redis guarantees at-least-once
     * delivery once the sidecar accepts the event. Callers that need delivery confirmation
     * should use synchronous service invocation instead.
     */
    public void publishEvent(String topic, Object payload) {
        String url = daprBaseUrl + "/v1.0/publish/" + PUBSUB_NAME + "/" + topic;
        log.info("[Dapr sidecar →] publish topic={} | url={}", topic, url);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(payload, headers);
        try {
            rest.postForEntity(url, request, Void.class);
            log.info("[Dapr sidecar ←] publish accepted topic={}", topic);
        } catch (Exception e) {
            log.error("[Dapr sidecar ✗] publish failed topic={}: {}", topic, e.getMessage());
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private <T> T invoke(String vdmsPath, Class<T> responseType) {
        String url = daprBaseUrl + "/v1.0/invoke/" + VDMS_APP_ID + "/method/" + vdmsPath;
        log.info("[Dapr sidecar →] invoke  path={} | url={}", vdmsPath, url);
        ResponseEntity<T> response = rest.getForEntity(url, responseType);
        log.info("[Dapr sidecar ←] respond path={} | status={}", vdmsPath, response.getStatusCode());
        return response.getBody();
    }

}
