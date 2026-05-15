package io.sclera.service;

import io.sclera.dto.AlertProfileDTO;
import io.sclera.dto.DeviceAlertDTO;
import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.util.Set;

/** STUB: replace with remote call to edge-D */
@Service
public class IOCService {
    public void sendDeviceAlertDataIOC(DeviceConditionsDTO deviceConditionsDTO, DeviceAlertDTO deviceAlert, Integer status, AlertProfileDTO alertProfile, BigInteger timestamp) {}
    public void sendDigitalTwinData(Set<String> deviceIds) {}

    public void sendSensorValueDataToIOC(String deviceId, BigInteger sensorValue) {
        StubLog.warn("IOCService", "sendSensorValueDataToIOC", "edge-D");
    }
}
