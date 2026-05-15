package io.sclera.rabbitmq;

import io.sclera.dto.touchscreen.DeviceHistoryDTO;
import io.sclera.utils.StubLog;
import org.springframework.stereotype.Service;
import java.math.BigInteger;

/** STUB: edge-only AMQP publisher */
@Service
public class RabbitmqService {
    public void rabbitmqDeviceEvent(String eventType, DeviceHistoryDTO dto) {
        StubLog.warn("RabbitmqService", "rabbitmqDeviceEvent", "edge-local");
    }
    public void rabbitmqMeasuringInstrumentData(String deviceId, String sensorType,
                                                BigInteger sensorValue, String unit) {
        StubLog.warn("RabbitmqService", "rabbitmqMeasuringInstrumentData", "edge-local");
    }
}
