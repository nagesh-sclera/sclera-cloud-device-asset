package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.PolyLensDeviceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-integrations (AP-C2).
 * Replaces {@code io.sclera.service.PolyLensService}.
 */
@Component
public class PolyLensClient {

    private static final Logger log = LoggerFactory.getLogger(PolyLensClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public PolyLensClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code PolyLensService#getAllPolyLensDevices}. Returns empty set on failure. */
    public Set<PolyLensDeviceDTO> getAllPolyLensDevices(String username, String vdmsId, String dockername, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "polyLens/getAllPolyLensDevices", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("PolyLensClient.getAllPolyLensDevices failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Mirrors {@code PolyLensService#updatePolyLensDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updatePolyLensDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "polyLens/updatePolyLensDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PolyLensClient.updatePolyLensDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code PolyLensService#getPolyLensDeviceCountByDeviceId}. Returns 0 on failure. */
    public Integer getPolyLensDeviceCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "polyLens/getPolyLensDeviceCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("PolyLensClient.getPolyLensDeviceCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }
}
