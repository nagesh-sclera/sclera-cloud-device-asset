package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Dapr client that fetches a device's sensor count from sclera-integrations.
 *
 * <p>Unlike the stub clients in this package, this one returns the REAL value from the
 * remote service. It demonstrates genuine inter-service Dapr invocation:
 * cloud-device-asset -> its Dapr sidecar -> sclera-integrations, which owns the sensors
 * in its own database schema ({@code integrations_svc.device_sensor}). The count therefore
 * comes from a different service AND a different DB than the device itself.</p>
 */
@Component
public class DeviceSensorClient {

    private static final Logger log = LoggerFactory.getLogger(DeviceSensorClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public DeviceSensorClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Invokes sclera-integrations over Dapr to count this device's sensors (read from the
     * integrations_svc DB). Returns a map {device_id, count, source, via}. On failure returns
     * count 0 so the UI degrades gracefully instead of erroring.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getSensorCount(String deviceId) {
        try {
            Map<String, Object> result = dapr.invokeMethod(
                    APP_ID,
                    "sensors/device/" + deviceId + "/count",
                    null,
                    HttpExtension.GET,
                    Map.class).block();
            if (result == null) {
                result = new HashMap<>();
            }
            result.putIfAbsent("device_id", deviceId);
            result.putIfAbsent("count", 0);
            result.put("via", "dapr-invoke:sclera-integrations");
            log.info("sensor count for device_id={} via Dapr -> {}", deviceId, result.get("count"));
            return result;
        } catch (Exception e) {
            log.warn("DeviceSensorClient.getSensorCount via Dapr failed for device_id={}: {}", deviceId, e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("device_id", deviceId);
            fallback.put("count", 0);
            fallback.put("via", "dapr-invoke-failed");
            return fallback;
        }
    }
}
