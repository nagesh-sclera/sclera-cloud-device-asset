package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.AlertProfileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-alerts microservice (AP-C5).
 *
 * Replaces the null-returning {@code io.sclera.service.AlertProfileService} stub.
 * Methods return null on exception (documented stub default) so that call
 * sites are never interrupted by sidecar-down or network errors.
 */
@Component
public class AlertProfileClient {

    private static final Logger log = LoggerFactory.getLogger(AlertProfileClient.class);
    private static final String APP_ID = "sclera-alerts";

    private final DaprClient dapr;

    public AlertProfileClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code AlertProfileService#getAlertProfileById}.
     * Maps to GET sclera-alerts/alertProfile/getAlertProfileById.
     * Returns null on sidecar failure (documented stub default).
     */
    public AlertProfileDTO getAlertProfileById(String alertProfileId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("alertProfileId", alertProfileId);
        try {
            return dapr.invokeMethod(APP_ID, "alertProfile/getAlertProfileById", payload, HttpExtension.GET, AlertProfileDTO.class).block();
        } catch (Exception e) {
            log.warn("AlertProfileClient.getAlertProfileById failed; returning null: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Mirrors {@code AlertProfileService#getAlertProfileDetailsById}.
     * Maps to GET sclera-alerts/alertProfile/getAlertProfileDetailsById.
     * Returns null on sidecar failure (documented stub default).
     */
    public AlertProfileDTO getAlertProfileDetailsById(String a, String b, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            return dapr.invokeMethod(APP_ID, "alertProfile/getAlertProfileDetailsById", payload, HttpExtension.GET, AlertProfileDTO.class).block();
        } catch (Exception e) {
            log.warn("AlertProfileClient.getAlertProfileDetailsById failed; returning null: {}", e.getMessage());
            return null;
        }
    }
}
