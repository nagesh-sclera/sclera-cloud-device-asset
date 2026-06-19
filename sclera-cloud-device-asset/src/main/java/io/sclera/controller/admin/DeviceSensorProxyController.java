package io.sclera.controller.admin;

import io.sclera.client.DeviceSensorClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Demo endpoint proving cross-service / cross-DB communication over Dapr:
 * cloud-device-asset returns a device's sensor count by invoking sclera-integrations
 * (which owns the sensors in its own {@code integrations_svc} DB) through its Dapr sidecar.
 *
 * <p>Kept as a separate controller so the change is fully additive — it touches none of the
 * existing device endpoints.</p>
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class DeviceSensorProxyController {

    private final DeviceSensorClient sensorClient;

    public DeviceSensorProxyController(DeviceSensorClient sensorClient) {
        this.sensorClient = sensorClient;
    }

    /** GET .../device/{id}/sensors/count -> count fetched from sclera-integrations via Dapr. */
    @GetMapping("/device/{device_id}/sensors/count")
    public Map<String, Object> sensorCount(@PathVariable("device_id") String deviceId) {
        return sensorClient.getSensorCount(deviceId);
    }
}
