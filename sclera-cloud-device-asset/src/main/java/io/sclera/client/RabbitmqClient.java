package io.sclera.client;

import io.sclera.dapr.DaprEventPublisher;
import io.sclera.dapr.PublishResult;
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

    private final DaprEventPublisher publisher;

    public RabbitmqClient(DaprEventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Publishes a device event to the {@code device.event-recorded} topic via Dapr pub/sub.
     * Logs an error if the publish fails; does not throw.
     */
    public void rabbitmqDeviceEvent(String eventType, DeviceHistoryDTO dto) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventType", eventType);
        evt.put("payload", dto);
        PublishResult result = publisher.publish(PUBSUB_NAME, TOPIC_DEVICE_EVENT, evt);
        if (!result.success()) {
            log.error("RabbitmqClient publish failed topic={} eventId={} error={}",
                TOPIC_DEVICE_EVENT, result.eventId(), result.error());
        }
    }

    /**
     * Publishes a measuring-instrument sensor reading to the {@code device.sensor-reading}
     * topic via Dapr pub/sub. Logs an error if the publish fails; does not throw.
     */
    public void rabbitmqMeasuringInstrumentData(String deviceId, String sensorType,
                                                BigInteger sensorValue, String unit) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("deviceId", deviceId);
        evt.put("sensorType", sensorType);
        evt.put("sensorValue", sensorValue);
        evt.put("unit", unit);
        PublishResult result = publisher.publish(PUBSUB_NAME, TOPIC_SENSOR_READING, evt);
        if (!result.success()) {
            log.error("RabbitmqClient publish failed topic={} eventId={} error={}",
                TOPIC_SENSOR_READING, result.eventId(), result.error());
        }
    }
}
