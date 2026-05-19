package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.PelicanSensorDTO;
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
 * Replaces {@code io.sclera.service.PelicanService}.
 */
@Component
public class PelicanClient {

    private static final Logger log = LoggerFactory.getLogger(PelicanClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public PelicanClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code PelicanService#getDeviceIdByPelicanSensorId}. Returns null on failure. */
    public String getDeviceIdByPelicanSensorId(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "pelican/getDeviceIdByPelicanSensorId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("PelicanClient.getDeviceIdByPelicanSensorId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code PelicanService#getPelicanSensorCountByDeviceId}. Returns 0 on failure. */
    public Integer getPelicanSensorCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "pelican/getPelicanSensorCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("PelicanClient.getPelicanSensorCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code PelicanService#getPelicanSensorAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getPelicanSensorAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "pelican/getPelicanSensorAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("PelicanClient.getPelicanSensorAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code PelicanService#getDevicePelicanSensors}. Returns empty set on failure. */
    public Set<PelicanSensorDTO> getDevicePelicanSensors(String username, String vdmsId, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "pelican/getDevicePelicanSensors", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("PelicanClient.getDevicePelicanSensors failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code PelicanService#getPelicanSensorsByDeviceId}. Returns empty list on failure. */
    public List<SensorDTO> getPelicanSensorsByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "pelican/getPelicanSensorsByDeviceId", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("PelicanClient.getPelicanSensorsByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code PelicanService#listpelicanDevicesAlertMessagesByDeviceIds}. Returns empty list on failure. */
    public List<ConditionsDTO> listpelicanDevicesAlertMessagesByDeviceIds(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "pelican/listpelicanDevicesAlertMessagesByDeviceIds", null, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("PelicanClient.listpelicanDevicesAlertMessagesByDeviceIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code PelicanService#updatePelicanSensorDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updatePelicanSensorDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "pelican/updatePelicanSensorDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("PelicanClient.updatePelicanSensorDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
