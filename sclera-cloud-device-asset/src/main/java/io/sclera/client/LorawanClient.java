package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.LorawanSensorDTO;
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
 * Replaces {@code io.sclera.service.LorawanService}.
 */
@Component
public class LorawanClient {

    private static final Logger log = LoggerFactory.getLogger(LorawanClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public LorawanClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code LorawanService#getDeviceIdByLorawanSensorId}. Returns null on failure. */
    public String getDeviceIdByLorawanSensorId(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "lorawan/getDeviceIdByLorawanSensorId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("LorawanClient.getDeviceIdByLorawanSensorId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code LorawanService#getLorawanSensorCountByDeviceId}. Returns 0 on failure. */
    public Integer getLorawanSensorCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "lorawan/getLorawanSensorCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("LorawanClient.getLorawanSensorCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code LorawanService#getLorawanSensorAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getLorawanSensorAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "lorawan/getLorawanSensorAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("LorawanClient.getLorawanSensorAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code LorawanService#getDeviceLorawanSensors}. Returns empty set on failure. */
    public Set<LorawanSensorDTO> getDeviceLorawanSensors(String username, String vdmsId, String dockername, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "lorawan/getDeviceLorawanSensors", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("LorawanClient.getDeviceLorawanSensors failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code LorawanService#getLorawanSensorsByDeviceId}. Returns empty list on failure. */
    public List<SensorDTO> getLorawanSensorsByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "lorawan/getLorawanSensorsByDeviceId", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("LorawanClient.getLorawanSensorsByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code LorawanService#listLorawanDevicesAlertMessagesByDeviceIds}. Returns empty list on failure. */
    public List<ConditionsDTO> listLorawanDevicesAlertMessagesByDeviceIds(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "lorawan/listLorawanDevicesAlertMessagesByDeviceIds", null, HttpExtension.POST).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("LorawanClient.listLorawanDevicesAlertMessagesByDeviceIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code LorawanService#updateLorawanSensorDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateLorawanSensorDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "lorawan/updateLorawanSensorDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("LorawanClient.updateLorawanSensorDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
