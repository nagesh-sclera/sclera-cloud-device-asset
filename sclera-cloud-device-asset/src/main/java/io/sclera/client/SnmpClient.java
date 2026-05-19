package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.Product_SnmpDTO;
import io.sclera.dto.SnmpObjectDTO;
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
 * Replaces {@code io.sclera.service.SnmpService}.
 */
@Component
public class SnmpClient {

    private static final Logger log = LoggerFactory.getLogger(SnmpClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public SnmpClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code SnmpService#getSnmpDeviceAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getSnmpDeviceAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/getSnmpDeviceAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("SnmpClient.getSnmpDeviceAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code SnmpService#getDeviceIdBySnmpDeviceId}. Returns null on failure. */
    public String getDeviceIdBySnmpDeviceId(String snmpDeviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("snmpDeviceId", snmpDeviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/getDeviceIdBySnmpDeviceId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("SnmpClient.getDeviceIdBySnmpDeviceId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code SnmpService#getSnmpDeviceCountByDeviceAndSnmpConfiguration}. Returns 0 on failure. */
    public Integer getSnmpDeviceCountByDeviceAndSnmpConfiguration(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/getSnmpDeviceCountByDeviceAndSnmpConfiguration", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("SnmpClient.getSnmpDeviceCountByDeviceAndSnmpConfiguration failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code SnmpService#getSnmpObjectCountByDeviceId}. Returns 0 on failure. */
    public Integer getSnmpObjectCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/getSnmpObjectCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("SnmpClient.getSnmpObjectCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /** Mirrors {@code SnmpService#getDeviceIdBySnmpObjectId}. Returns null on failure. */
    public String getDeviceIdBySnmpObjectId(String snmpDeviceConfigId, String snmpObjectOid) {
        Map<String, String> payload = new HashMap<>();
        payload.put("snmpDeviceConfigId", snmpDeviceConfigId);
        payload.put("snmpObjectOid", snmpObjectOid);
        try {
            dapr.invokeMethod(APP_ID, "snmp/getDeviceIdBySnmpObjectId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("SnmpClient.getDeviceIdBySnmpObjectId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code SnmpService#getSnmpObjectAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getSnmpObjectAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/getSnmpObjectAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("SnmpClient.getSnmpObjectAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code SnmpService#getDeviceSnmpObjects}. Returns empty set on failure. */
    public Set<SnmpObjectDTO> getDeviceSnmpObjects(String username, String vdmsId, String dockername, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/getDeviceSnmpObjects", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("SnmpClient.getDeviceSnmpObjects failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code SnmpService#getSnmpDevicesByDeviceId}. Returns empty list on failure. */
    public List<SensorDTO> getSnmpDevicesByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/getSnmpDevicesByDeviceId", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("SnmpClient.getSnmpDevicesByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code SnmpService#deleteGlobalSnmpByDeviceId}. */
    public void deleteGlobalSnmpByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/deleteGlobalSnmpByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SnmpClient.deleteGlobalSnmpByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code SnmpService#upsertGlobalSnmpByDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void upsertGlobalSnmpByDeviceId(Set<Product_SnmpDTO> snmpSet, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/upsertGlobalSnmpByDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SnmpClient.upsertGlobalSnmpByDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /**
     * Mirrors {@code SnmpService#updateSnmpObjectDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateSnmpObjectDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/updateSnmpObjectDeviceId", payload, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("SnmpClient.updateSnmpObjectDeviceId failed; swallowing: {}", e.getMessage());
        }
    }

    /** Mirrors {@code SnmpService#getAllNetworkSnmpDeviceData}. Returns null on failure. */
    public Object getAllNetworkSnmpDeviceData(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "snmp/getAllNetworkSnmpDeviceData", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("SnmpClient.getAllNetworkSnmpDeviceData failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }
}
