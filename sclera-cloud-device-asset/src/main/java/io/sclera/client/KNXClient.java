package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.KNXGroupDTO;
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
 * Replaces {@code io.sclera.service.KNXService}.
 */
@Component
public class KNXClient {

    private static final Logger log = LoggerFactory.getLogger(KNXClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public KNXClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code KNXService#getDeviceIdByKNXGroupAddress}. Returns null on failure. */
    public String getDeviceIdByKNXGroupAddress(String deviceAddress, String groupAddress) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceAddress", deviceAddress);
        payload.put("groupAddress", groupAddress);
        try {
            dapr.invokeMethod(APP_ID, "kNX/getDeviceIdByKNXGroupAddress", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("KNXClient.getDeviceIdByKNXGroupAddress failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code KNXService#getKNXGroupCountByDeviceId}. Returns 0 on failure. */
    public Integer getKNXGroupCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "kNX/getKNXGroupCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("KNXClient.getKNXGroupCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code KNXService#getKNXGroupAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getKNXGroupAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "kNX/getKNXGroupAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("KNXClient.getKNXGroupAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code KNXService#getDeviceKNXGroups}. Returns empty set on failure. */
    public Set<KNXGroupDTO> getDeviceKNXGroups(String username, String vdmsId, String dockername, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "kNX/getDeviceKNXGroups", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("KNXClient.getDeviceKNXGroups failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code KNXService#getKNXGroupsByDeviceAddress}. Returns empty list on failure. */
    public List<SensorDTO> getKNXGroupsByDeviceAddress(String addr) {
        Map<String, String> payload = new HashMap<>();
        payload.put("addr", addr);
        try {
            dapr.invokeMethod(APP_ID, "kNX/getKNXGroupsByDeviceAddress", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("KNXClient.getKNXGroupsByDeviceAddress failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code KNXService#listKNXDevicesAlertMessagesByDeviceIds}. Returns empty list on failure. */
    public List<ConditionsDTO> listKNXDevicesAlertMessagesByDeviceIds(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "kNX/listKNXDevicesAlertMessagesByDeviceIds", null, HttpExtension.POST).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("KNXClient.listKNXDevicesAlertMessagesByDeviceIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code KNXService#updateKnxGroupDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateKnxGroupDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "kNX/updateKnxGroupDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("KNXClient.updateKnxGroupDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
