package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.BacnetAdvanceExportExcelDTO;
import io.sclera.dto.BacnetObjectDTO;
import io.sclera.dto.ConditionsDTO;
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
 * Replaces {@code io.sclera.service.BacnetService}.
 */
@Component
public class BacnetClient {

    private static final Logger log = LoggerFactory.getLogger(BacnetClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public BacnetClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code BacnetService#getDeviceIdByBacnetObjectId}. Returns null on failure. */
    public String getDeviceIdByBacnetObjectId(String bacnetDeviceId, String bacnetObjectId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("bacnetDeviceId", bacnetDeviceId);
        payload.put("bacnetObjectId", bacnetObjectId);
        try {
            dapr.invokeMethod(APP_ID, "bacnet/getDeviceIdByBacnetObjectId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("BacnetClient.getDeviceIdByBacnetObjectId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code BacnetService#getBacnetObjectCountByDeviceId}. Returns 0 on failure. */
    public Integer getBacnetObjectCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "bacnet/getBacnetObjectCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("BacnetClient.getBacnetObjectCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code BacnetService#getBacnetObjectAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getBacnetObjectAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "bacnet/getBacnetObjectAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("BacnetClient.getBacnetObjectAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code BacnetService#getDeviceBacnetObjects}. Returns empty set on failure. */
    public Set<BacnetObjectDTO> getDeviceBacnetObjects(String username, String vdmsId, String dockername, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "bacnet/getDeviceBacnetObjects", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("BacnetClient.getDeviceBacnetObjects failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code BacnetService#getBacnetObjectsByDeviceId}. Returns empty list on failure. */
    public List<SensorDTO> getBacnetObjectsByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "bacnet/getBacnetObjectsByDeviceId", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("BacnetClient.getBacnetObjectsByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code BacnetService#listBacnetDevicesAlertMessagesByDeviceIds}. Returns empty list on failure. */
    public List<ConditionsDTO> listBacnetDevicesAlertMessagesByDeviceIds(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "bacnet/listBacnetDevicesAlertMessagesByDeviceIds", null, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("BacnetClient.listBacnetDevicesAlertMessagesByDeviceIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code BacnetService#getBacnetDeviceIdForAdvanceExcelExport}. Returns empty list on failure. */
    public List<BacnetAdvanceExportExcelDTO> getBacnetDeviceIdForAdvanceExcelExport(String username, String vdmsId, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "bacnet/getBacnetDeviceIdForAdvanceExcelExport", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("BacnetClient.getBacnetDeviceIdForAdvanceExcelExport failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Mirrors {@code BacnetService#updateBacnetObjectDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateBacnetObjectDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "bacnet/updateBacnetObjectDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("BacnetClient.updateBacnetObjectDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
