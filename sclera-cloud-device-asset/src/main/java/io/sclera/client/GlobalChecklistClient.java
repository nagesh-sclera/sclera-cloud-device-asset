package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-inspection microservice (AP-C4).
 *
 * Replaces the stub {@code io.sclera.service.GlobalChecklistService}.
 * Void methods swallow exceptions with a WARN log.
 */
@Component
public class GlobalChecklistClient {

    private static final Logger log = LoggerFactory.getLogger(GlobalChecklistClient.class);
    private static final String APP_ID = "sclera-inspection";

    private final DaprClient dapr;

    public GlobalChecklistClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code GlobalChecklistService#updateDeviceGlobalChecklistDeviceId}. */
    public void updateDeviceGlobalChecklistDeviceId(String oldId, String newId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "globalChecklist/updateDeviceGlobalChecklistDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("GlobalChecklistClient.updateDeviceGlobalChecklistDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code GlobalChecklistService#deleteGlobalChecklistByDeviceId}. */
    public void deleteGlobalChecklistByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "globalChecklist/deleteGlobalChecklistByDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("GlobalChecklistClient.deleteGlobalChecklistByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
