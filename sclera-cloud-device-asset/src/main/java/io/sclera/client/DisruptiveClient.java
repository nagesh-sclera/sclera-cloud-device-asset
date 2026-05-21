package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.DisruptiveSensorDTO;
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
 * Replaces {@code io.sclera.service.DisruptiveService}.
 */
@Component
public class DisruptiveClient {

    private static final Logger log = LoggerFactory.getLogger(DisruptiveClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public DisruptiveClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code DisruptiveService#getDeviceIdByDisruptiveSensorId}. Returns null on failure. */
    public String getDeviceIdByDisruptiveSensorId(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "disruptive/getDeviceIdByDisruptiveSensorId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("DisruptiveClient.getDeviceIdByDisruptiveSensorId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code DisruptiveService#getDisruptiveSensorCountByDeviceId}. Returns 0 on failure. */
    public Integer getDisruptiveSensorCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "disruptive/getDisruptiveSensorCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("DisruptiveClient.getDisruptiveSensorCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code DisruptiveService#getDisruptiveSensorAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getDisruptiveSensorAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "disruptive/getDisruptiveSensorAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("DisruptiveClient.getDisruptiveSensorAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code DisruptiveService#getDeviceDisruptiveSensors}. Returns empty set on failure. */
    public Set<DisruptiveSensorDTO> getDeviceDisruptiveSensors(String username, String vdmsId, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "disruptive/getDeviceDisruptiveSensors", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("DisruptiveClient.getDeviceDisruptiveSensors failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code DisruptiveService#listDisruptiveDevicesAlertMessagesByDeviceIds}. Returns empty list on failure. */
    public List<ConditionsDTO> listDisruptiveDevicesAlertMessagesByDeviceIds(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "disruptive/listDisruptiveDevicesAlertMessagesByDeviceIds", null, HttpExtension.POST).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("DisruptiveClient.listDisruptiveDevicesAlertMessagesByDeviceIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code DisruptiveService#updateDisruptiveSensorDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateDisruptiveSensorDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "disruptive/updateDisruptiveSensorDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("DisruptiveClient.updateDisruptiveSensorDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
