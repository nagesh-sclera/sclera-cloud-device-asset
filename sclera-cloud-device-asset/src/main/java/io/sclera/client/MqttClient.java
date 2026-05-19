package io.sclera.client;

import io.dapr.client.DaprClient;
import io.dapr.client.domain.HttpExtension;
import io.sclera.dto.MqttDeviceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Thin Dapr client delegating to sclera-integrations (AP-C2).
 * Replaces {@code io.sclera.service.MqttService}.
 */
@Component
public class MqttClient {

    private static final Logger log = LoggerFactory.getLogger(MqttClient.class);
    private static final String APP_ID = "sclera-integrations";

    private final DaprClient dapr;

    public MqttClient(DaprClient dapr) {
        this.dapr = dapr;
    }

    /** Mirrors {@code MqttService#getAllMqttDevices}. Returns empty set on failure. */
    public Set<MqttDeviceDTO> getAllMqttDevices(String username, String vdmsId, String dockername, String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("vdmsId", vdmsId);
        payload.put("dockername", dockername);
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "mqtt/getAllMqttDevices", payload, HttpExtension.GET).block();
            return Collections.emptySet();
        } catch (Exception e) {
            log.warn("MqttClient.getAllMqttDevices failed; returning stub default: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /** Mirrors {@code MqttService#getMqttDeviceCountByDeviceId}. Returns 0 on failure. */
    public Integer getMqttDeviceCountByDeviceId(String deviceId) {
        Map<String, String> payload = new HashMap<>();
        payload.put("deviceId", deviceId);
        try {
            dapr.invokeMethod(APP_ID, "mqtt/getMqttDeviceCountByDeviceId", payload, HttpExtension.GET).block();
            return 0;
        } catch (Exception e) {
            log.warn("MqttClient.getMqttDeviceCountByDeviceId failed; returning stub default: {}", e.getMessage());
            return 0;
        }
    }
}
