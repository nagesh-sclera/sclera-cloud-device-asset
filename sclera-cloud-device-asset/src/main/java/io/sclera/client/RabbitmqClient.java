package io.sclera.client;

import io.dapr.client.DaprClient;
import io.sclera.dto.touchscreen.DeviceHistoryDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Publishes RabbitMQ-style device events via Dapr pub/sub.
 *
 * In dev the {@code pubsub} component is backed by Redis; in prod it swaps to
 * RabbitMQ (see {@code dapr/components/k8s/pubsub.yaml}) without any code change.
 *
 * Replaces {@code io.sclera.rabbitmq.RabbitmqService}.
 */
@Component
public class RabbitmqClient {

    private static final Logger log = LoggerFactory.getLogger(RabbitmqClient.class);
    private static final String PUBSUB_NAME = "pubsub";
    private static final String TOPIC_DEVICE_EVENT = "device.event-recorded";
    private static final String TOPIC_SENSOR_READING = "device.sensor-reading";

    private final DaprClient dapr;

    public RabbitmqClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    public void rabbitmqDeviceEvent(String eventType, DeviceHistoryDTO dto) {
        try {
            Map<String, Object> evt = new HashMap<>();
            evt.put("eventType", eventType);
            evt.put("payload", dto);
            dapr.publishEvent(PUBSUB_NAME, TOPIC_DEVICE_EVENT, evt).block();
        } catch (Exception e) {
            log.warn("RabbitmqClient.rabbitmqDeviceEvent publish failed; swallowing", e);
        }
    }

    public void rabbitmqMeasuringInstrumentData(String deviceId, String sensorType,
                                                BigInteger sensorValue, String unit) {
        try {
            Map<String, Object> evt = new HashMap<>();
            evt.put("deviceId", deviceId);
            evt.put("sensorType", sensorType);
            evt.put("sensorValue", sensorValue);
            evt.put("unit", unit);
            dapr.publishEvent(PUBSUB_NAME, TOPIC_SENSOR_READING, evt).block();
        } catch (Exception e) {
            log.warn("RabbitmqClient.rabbitmqMeasuringInstrumentData publish failed; swallowing", e);
        }
    }
}
