package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.touchscreen.DeviceHistoryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Thin Dapr client delegating to sclera-edge.
 * Replaces {@code io.sclera.rabbitmq.RabbitmqService}.
 */
@Component
public class RabbitmqClient {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqClient.class);
    private static final String APP_ID = "sclera-edge";

    private final DaprClient dapr;

    public RabbitmqClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    public void rabbitmqDeviceEvent(String eventType, DeviceHistoryDTO dto) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("eventType", eventType);
            dapr.invokeMethod(APP_ID, "rabbitmq/rabbitmqDeviceEvent", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("RabbitmqClient.rabbitmqDeviceEvent failed; swallowing", e);
        }
    }

    public void rabbitmqMeasuringInstrumentData(String deviceId, String sensorType,
                                                BigInteger sensorValue, String unit) {
        try {
            Map<String, String> p = new HashMap<>();
            p.put("deviceId", deviceId);
            p.put("sensorType", sensorType);
            p.put("unit", unit);
            dapr.invokeMethod(APP_ID, "rabbitmq/rabbitmqMeasuringInstrumentData", p, HttpExtension.GET).block();
        } catch (Exception e) {
            log.warn("RabbitmqClient.rabbitmqMeasuringInstrumentData failed; swallowing", e);
        }
    }
}
