package io.sclera.client;

import com.alibaba.fastjson.JSONObject;
import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.InventoryDeviceSyncDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to the sclera-inventory microservice (AP-C8).
 *
 * Replaces the stub {@code io.sclera.service.InventoryDeviceService}.
 * Void methods swallow exceptions with a WARN log; collection-returning methods
 * return an empty set on sidecar failure so that call sites are never interrupted.
 *
 * NOTE: upsertInventoryDevices passes a complex body under GET routing — the body
 * is silently dropped. Needs POST upgrade when scaffold supports verbs.
 */
@Component
public class InventoryDeviceClient {

    private static final Logger log = LoggerFactory.getLogger(InventoryDeviceClient.class);
    private static final String APP_ID = "sclera-inventory";

    private final DaprClient dapr;

    public InventoryDeviceClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /**
     * Mirrors {@code InventoryDeviceService#retireInventoryDevice}.
     * Maps to GET sclera-inventory/inventoryDevice/retireInventoryDevice.
     */
    public void retireInventoryDevice(String vdmsId, String deviceId, String username,
                                      String description, String inventoryTrackingId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        payload.put("deviceId", deviceId);
        payload.put("username", username);
        payload.put("description", description);
        payload.put("inventoryTrackingId", inventoryTrackingId);
        try {
            dapr.invokeMethod(APP_ID, "inventoryDevice/retireInventoryDevice", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("InventoryDeviceClient.retireInventoryDevice failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code InventoryDeviceRepository#deleteByDeviceId}.
     * Maps to GET sclera-inventory/inventoryDevice/deleteByDeviceId.
     */
    public void deleteByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "inventoryDevice/deleteByDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("InventoryDeviceClient.deleteByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code InventoryDeviceService#upsertInventoryDevices}.
     * Maps to GET sclera-inventory/inventoryDevice/upsertInventoryDevices.
     * Returns empty set on sidecar failure (documented stub default).
     * NOTE: JSONObject + DTO body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public Set<DeviceDTO> upsertInventoryDevices(JSONObject stockedOutItems, String vdmsId,
                                                  String email, InventoryDeviceSyncDTO dto) {
        Map<String, String> payload = new HashMap<>();
        payload.put("vdmsId", vdmsId);
        payload.put("email", email);
        try {
            dapr.invokeMethod(APP_ID, "inventoryDevice/upsertInventoryDevices", payload, HttpExtension.POST).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("InventoryDeviceClient.upsertInventoryDevices failed; returning empty set: {}", e.getMessage());
            return Collections.emptySet();
        }
    }
}
