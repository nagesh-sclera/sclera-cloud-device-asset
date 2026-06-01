package io.sclera.vdms.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Internal client for sclera-cloud-device-asset — the reverse of VdmsClient.
 *
 * Calls are routed via the local Dapr sidecar:
 *   GET  http://localhost:{DAPR_HTTP_PORT}/v1.0/invoke/sclera-cloud-device-asset/method/{path}
 *   POST http://localhost:{DAPR_HTTP_PORT}/v1.0/publish/pubsub/{topic}
 *
 * Inject this bean wherever vdms-service needs data from device-asset, or needs
 * to push VDMS lifecycle events for device-asset to react to.
 */
@Service
public class ScleraCloudDeviceClient {

    private static final Logger log = LoggerFactory.getLogger(ScleraCloudDeviceClient.class);
    private static final String DEVICE_ASSET_APP_ID = "sclera-cloud-device-asset";

    private final RestTemplate rest = new RestTemplate();
    private final String daprBaseUrl = "http://localhost:" +
        (System.getenv("DAPR_HTTP_PORT") != null ? System.getenv("DAPR_HTTP_PORT") : "3500");

    // ── Service invocation ────────────────────────────────────────────────────

    /**
     * GET /user/{username}/vdms/{vdmsId}/docker/{dockerName}/getdevicecount
     * → {"deviceCount": N}
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getDeviceCount(String username, String vdmsId, String dockerName) {
        String path = "user/" + username + "/vdms/" + vdmsId + "/docker/" + dockerName + "/getdevicecount";
        return invoke(path, Map.class);
    }

    /**
     * GET /user/{username}/vdms/{vdmsId}/docker/{dockerName}/devices
     * → device-asset returns a JSON array; use Object.class so Jackson deserializes it as List<Map>.
     */
    public Object getDevices(String username, String vdmsId, String dockerName) {
        String path = "user/" + username + "/vdms/" + vdmsId + "/docker/" + dockerName + "/devices";
        return invoke(path, Object.class);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private <T> T invoke(String path, Class<T> responseType) {
        String url = daprBaseUrl + "/v1.0/invoke/" + DEVICE_ASSET_APP_ID + "/method/" + path;
        log.info("[Dapr sidecar →] invoke  path={} | url={}", path, url);
        ResponseEntity<T> response = rest.getForEntity(url, responseType);
        log.info("[Dapr sidecar ←] respond path={} | status={}", path, response.getStatusCode());
        return response.getBody();
    }
}
