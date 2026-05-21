package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.DeviceAlertDTO;
import io.sclera.dto.DeviceConditionsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-edge (AP-C1edge).
 * Replaces {@code io.sclera.service.IOCService}.
 */
@Component
public class IOCClient {

    private static final Logger log = LoggerFactory.getLogger(IOCClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public IOCClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    public void sendDeviceAlertDataIOC(DeviceConditionsDTO deviceConditionsDTO, DeviceAlertDTO deviceAlert,
                                       Integer status, AlertProfileDTO alertProfile, BigInteger timestamp) {
        try {
            dapr.invokeMethod(APP_ID, "ioc/sendDeviceAlertDataIOC", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("IOCClient.sendDeviceAlertDataIOC failed; swallowing", e);
        }
    }

    public void sendDigitalTwinData(Set<String> deviceIds) {
        try {
            dapr.invokeMethod(APP_ID, "ioc/sendDigitalTwinData", null, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("IOCClient.sendDigitalTwinData failed; swallowing", e);
        }
    }

    public void sendSensorValueDataToIOC(String deviceId, BigInteger sensorValue) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("deviceId", deviceId);
            dapr.invokeMethod(APP_ID, "ioc/sendSensorValueDataToIOC", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("IOCClient.sendSensorValueDataToIOC failed; swallowing", e);
        }
    }
}
