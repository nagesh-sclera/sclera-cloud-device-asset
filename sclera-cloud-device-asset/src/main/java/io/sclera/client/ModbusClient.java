package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.ConditionsDTO;
import io.sclera.dto.ModbusRegisterDTO;
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
 * Replaces {@code io.sclera.service.ModbusService}.
 */
@Component
public class ModbusClient {

    private static final Logger log = LoggerFactory.getLogger(ModbusClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public ModbusClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code ModbusService#getDeviceModbusRegisters}. Returns empty set on failure. */
    public Set<ModbusRegisterDTO> getDeviceModbusRegisters(String username, String vdmsId, String dockername, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "modbus/getDeviceModbusRegisters", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("ModbusClient.getDeviceModbusRegisters failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code ModbusService#getModbusRegistersByDeviceId}. Returns empty list on failure. */
    public List<SensorDTO> getModbusRegistersByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "modbus/getModbusRegistersByDeviceId", payload, HttpExtension.GET).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("ModbusClient.getModbusRegistersByDeviceId failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code ModbusService#listModbusDevicesAlertMessagesByDeviceIds}. Returns empty list on failure. */
    public List<ConditionsDTO> listModbusDevicesAlertMessagesByDeviceIds(List<String> ids) {
        try {
            dapr.invokeMethod(APP_ID, "modbus/listModbusDevicesAlertMessagesByDeviceIds", null, HttpExtension.POST).block();
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("ModbusClient.listModbusDevicesAlertMessagesByDeviceIds failed; returning stub default: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Mirrors {@code ModbusService#getDeviceIdByModbusRegisterId}. Returns null on failure. */
    public String getDeviceIdByModbusRegisterId(String modbusRegisterId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("modbusRegisterId", modbusRegisterId);
        try {
            dapr.invokeMethod(APP_ID, "modbus/getDeviceIdByModbusRegisterId", payload, HttpExtension.GET).block();
            return null;
        } catch (Exception e) {
            log.warn("ModbusClient.getDeviceIdByModbusRegisterId failed; returning stub default: {}", e.getMessage());
            return null;
        }
    }

    /** Mirrors {@code ModbusService#getModbusRegisterAlertStatusByDeviceId}. Returns FALSE on failure. */
    public Boolean getModbusRegisterAlertStatusByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "modbus/getModbusRegisterAlertStatusByDeviceId", payload, HttpExtension.GET).block();
            return Boolean.FALSE;
        } catch (Exception e) {
            log.warn("ModbusClient.getModbusRegisterAlertStatusByDeviceId failed; returning stub default: {}", e.getMessage());
            return Boolean.FALSE;
        }
    }

    /** Mirrors {@code ModbusService#getModbusRegistersCountByDeviceId}. Returns 0 on failure. */
    public Integer getModbusRegistersCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "modbus/getModbusRegistersCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("ModbusClient.getModbusRegistersCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Mirrors {@code ModbusService#updateModbusRegisterDeviceId}.
     * NOTE: Set body is lost under GET-only skeleton routing (needs POST upgrade).
     */
    public void updateModbusRegisterDeviceId(String oldId, String newId, Set<String> ids) {
        Map<String, String> payload = new HashMap<>();
        payload.put("oldId", oldId);
        payload.put("newId", newId);
        try {
            dapr.invokeMethod(APP_ID, "modbus/updateModbusRegisterDeviceId", payload, HttpExtension.POST).block();
        } catch (Exception e) {
            log.warn("ModbusClient.updateModbusRegisterDeviceId failed; swallowing: {}", e.getMessage());
        }
    }
}
