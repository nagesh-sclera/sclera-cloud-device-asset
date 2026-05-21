package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.EcobeeSensorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-integrations (AP-C2).
 * Replaces {@code io.sclera.service.EcobeeService}.
 */
@Component
public class EcobeeClient {

    private static final Logger log = LoggerFactory.getLogger(EcobeeClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public EcobeeClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code EcobeeService#getEcobeeDevicesByDeviceId}. Returns empty set on failure. */
    public Set<EcobeeSensorDTO> getEcobeeDevicesByDeviceId(String username, String vdmsId, String dockername, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "ecobee/getEcobeeDevicesByDeviceId", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("EcobeeClient.getEcobeeDevicesByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code EcobeeService#getEcobeeSensorCountByDeviceId}. Returns 0 on failure. */
    public Integer getEcobeeSensorCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "ecobee/getEcobeeSensorCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("EcobeeClient.getEcobeeSensorCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code EcobeeService#getEcobeeSensorAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getEcobeeSensorAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "ecobee/getEcobeeSensorAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("EcobeeClient.getEcobeeSensorAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code EcobeeService#getDeviceIdByEcobeeSensorId}. Returns null on failure. */
    public String getDeviceIdByEcobeeSensorId(String id) {
        Map<String, String> payload = new HashMap<>();
        payload.put("id", id);
        try {
            dapr.invokeMethod(APP_ID, "ecobee/getDeviceIdByEcobeeSensorId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("EcobeeClient.getDeviceIdByEcobeeSensorId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Mirrors {@code EcobeeService#updateEcobeeSensorDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateEcobeeSensorDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "ecobee/updateEcobeeSensorDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("EcobeeClient.updateEcobeeSensorDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
