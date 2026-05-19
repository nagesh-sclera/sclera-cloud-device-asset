package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-alerts microservice (AP-C5).
 *
 * Replaces the {@code io.sclera.service.AlertDowntimeScheduleService} stub
 * which returned Boolean.FALSE. On sidecar failure this client also returns
 * Boolean.FALSE (documented stub default) to keep call sites safe.
 */
@Component
public class AlertDowntimeScheduleClient {

    private static final Logger log = LoggerFactory.getLogger(AlertDowntimeScheduleClient.class);
    private static final String APP_ID = "sclera-alerts";

    private final DaprClient dapr;

    public AlertDowntimeScheduleClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code AlertDowntimeScheduleService#checkAlertDowntime}.
     * Maps to GET sclera-alerts/alertDowntimeSchedule/checkAlertDowntime.
     * Returns Boolean.FALSE on sidecar failure (documented stub default).
     */
    public Boolean checkAlertDowntime(String deviceId, String alertProfileId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        payload.put("alertProfileId", alertProfileId);
        try {
            Boolean result = dapr.invokeMethod(APP_ID, "alertDowntimeSchedule/checkAlertDowntime", payload, HttpExtension.GET, Boolean.class).block();
            return result != null ? result : Boolean.FALSE;
        } catch (Exception e) {
            log.warn("AlertDowntimeScheduleClient.checkAlertDowntime failed; returning false: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }
}
