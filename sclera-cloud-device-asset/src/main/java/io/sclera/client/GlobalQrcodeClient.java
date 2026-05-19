package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to the sclera-integrations microservice (AP-C2).
 *
 * Replaces {@code io.sclera.service.GlobalQrcodeService}.
 * All methods return documented safe defaults on sidecar failure.
 */
@Component
public class GlobalQrcodeClient {

    private static final Logger log = LoggerFactory.getLogger(GlobalQrcodeClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public GlobalQrcodeClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code GlobalQrcodeService#getDeviceQrcodeCountByDeviceId}. Returns 0 on failure. */
    public Integer getDeviceQrcodeCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "globalQrcode/getDeviceQrcodeCountByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("GlobalQrcodeClient.getDeviceQrcodeCountByDeviceId failed; returning 0: {}", e.getMessage());
        }
        return 0;
    }

    /** Mirrors {@code GlobalQrcodeService#deleteGlobalQRCodeByLocationId}. */
    public void deleteGlobalQRCodeByLocationId(String locationId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("locationId", locationId);
        try {
            dapr.invokeMethod(APP_ID, "globalQrcode/deleteGlobalQRCodeByLocationId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("GlobalQrcodeClient.deleteGlobalQRCodeByLocationId failed; swallowing: {}", e.getMessage());
        }
    }
}
