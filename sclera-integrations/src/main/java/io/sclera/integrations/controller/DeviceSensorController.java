package io.sclera.integrations.controller;

import io.sclera.integrations.model.DeviceSensor;
import io.sclera.integrations.repository.DeviceSensorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sensor add/get/delete, owned by the integrations service. The device lives in
 * cloud-device-asset; the caller passes its {@code device_id} here so the sensor
 * can be related to it. Reachable via the gateway at /integrations/sensors/...
 */
@RestController
@RequestMapping("/sensors")
public class DeviceSensorController {

    private static final Logger log = LoggerFactory.getLogger(DeviceSensorController.class);
    private final DeviceSensorRepository repo;

    public DeviceSensorController(DeviceSensorRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/device/{device_id}")
    public List<DeviceSensor> list(@PathVariable("device_id") String deviceId,
                                   @RequestParam(required = false) String username,
                                   @RequestParam(required = false) String vdmsid) {
        return repo.findForDevice(deviceId);
    }

    /**
     * Live count of this device's sensors, read straight from the integrations_svc DB.
     * Called by cloud-device-asset over Dapr to prove cross-service / cross-DB counting.
     */
    @GetMapping("/device/{device_id}/count")
    public Map<String, Object> count(@PathVariable("device_id") String deviceId,
                                     @RequestParam(required = false) String username,
                                     @RequestParam(required = false) String vdmsid) {
        long count = repo.countForDevice(deviceId);
        log.info("sensor count for device_id={} -> {} (integrations_svc.device_sensor)", deviceId, count);
        Map<String, Object> result = new HashMap<>();
        result.put("device_id", deviceId);
        result.put("count", count);
        result.put("source", "integrations_svc.device_sensor");
        return result;
    }

    @PostMapping("/device/{device_id}")
    public DeviceSensor add(@PathVariable("device_id") String deviceId,
                            @RequestParam(required = false) String username,
                            @RequestParam(required = false) String vdmsid,
                            @RequestBody DeviceSensor body) {
        if (body.getId() == null || body.getId().isBlank()) {
            body.setId(UUID.randomUUID().toString());
        }
        body.setDevice_id(deviceId);          // relation to the device in cloud-device-asset
        if (vdmsid != null) body.setVdms_id(vdmsid);
        body.setCreated_timestamp(BigInteger.valueOf(System.currentTimeMillis()));
        DeviceSensor saved = repo.save(body);
        log.info("added sensor id={} name={} for device_id={}", saved.getId(), saved.getName(), deviceId);
        return saved;
    }

    @DeleteMapping("/device/{device_id}/{sensor_id}")
    public void delete(@PathVariable("device_id") String deviceId,
                       @PathVariable("sensor_id") String sensorId,
                       @RequestParam(required = false) String username) {
        repo.deleteById(sensorId);
        log.info("deleted sensor id={} for device_id={}", sensorId, deviceId);
    }
}
