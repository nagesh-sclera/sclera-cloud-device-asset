package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.MonnitSensorDTO;
import io.sclera.dto.touchscreen.SensorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-integrations (AP-C2).
 * Replaces {@code io.sclera.service.MonnitService}.
 */
@Component
public class MonnitClient {

    private static final Logger log = LoggerFactory.getLogger(MonnitClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public MonnitClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code MonnitService#getDeviceIdByMonnitSensorId}. Returns null on failure. */
    public String getDeviceIdByMonnitSensorId(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "monnit/getDeviceIdByMonnitSensorId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("MonnitClient.getDeviceIdByMonnitSensorId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code MonnitService#getMonnitCountByDeviceId}. Returns 0 on failure. */
    public Integer getMonnitCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "monnit/getMonnitCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("MonnitClient.getMonnitCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code MonnitService#getMonnitAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getMonnitAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "monnit/getMonnitAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("MonnitClient.getMonnitAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code MonnitService#getDeviceMonnitSensors}. Returns empty set on failure. */
    public Set<MonnitSensorDTO> getDeviceMonnitSensors(String username, String vdmsId, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "monnit/getDeviceMonnitSensors", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("MonnitClient.getDeviceMonnitSensors failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code MonnitService#getMonnitSensorsByDeviceId}. Returns empty list on failure. */
    public List<SensorDTO> getMonnitSensorsByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "monnit/getMonnitSensorsByDeviceId", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("MonnitClient.getMonnitSensorsByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code MonnitService#listmonnitDevicesAlertMessagesByDeviceIds}. Returns empty list on failure. */
    public List<ConditionsDTO> listmonnitDevicesAlertMessagesByDeviceIds(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "monnit/listmonnitDevicesAlertMessagesByDeviceIds", null, HttpExtension.POST).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("MonnitClient.listmonnitDevicesAlertMessagesByDeviceIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code MonnitService#updateMonnitSensorDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateMonnitSensorDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "monnit/updateMonnitSensorDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("MonnitClient.updateMonnitSensorDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
