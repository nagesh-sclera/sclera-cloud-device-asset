package io.sclera.controller.internal;

import io.sclera.dto.DeviceDTO;
import io.sclera.service.DeviceService;
import io.sclera.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Internal (Dapr service-invocation) routes that let the measuring-instrument microservice
 * read/update device-asset device state it no longer owns under DB-per-service. Mirrors the
 * existing {@code controller.internal.DeviceTicketSyncController} convention: plain
 * {@code @RestController}, no class-level base path, full path baked into each method,
 * reachable un-authenticated by the Dapr sidecar under the {@code docker} profile
 * ({@code DockerSecurityConfig} permits all).
 */
@RestController
public class MiDeviceBridgeController {

    private static final Logger log = LoggerFactory.getLogger(MiDeviceBridgeController.class);

    private final DeviceService deviceService;
    private final LocationService locationService;

    public MiDeviceBridgeController(DeviceService deviceService, LocationService locationService) {
        this.deviceService = deviceService;
        this.locationService = locationService;
    }

    /** Returns a device by its id (no tenant scoping — device id is globally unique). */
    @GetMapping("/api/v1/device-asset-service/internal/device/{deviceId}/getbyid")
    public DeviceDTO getDeviceById(@PathVariable String deviceId) {
        log.info("internal getDeviceById deviceId={}", deviceId);
        return deviceService.getDeviceById(deviceId);
    }

    /** Recomputes and stores each device's measuring-instrument count. Null-safe, loop-safe. */
    @PostMapping("/api/v1/device-asset-service/internal/devices/measurecount")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMeasureCount(@RequestBody Set<String> deviceIds) {
        log.info("internal updateMeasureCount count={}", deviceIds == null ? 0 : deviceIds.size());
        if (deviceIds == null) return;
        for (String deviceId : deviceIds) {
            deviceService.updateDeviceMeasureCountByDeviceId(deviceId);
        }
    }

    /** Recomputes the device's measuring-instrument alert status ("alert"/"no-alert"). */
    @PostMapping("/api/v1/device-asset-service/internal/device/{deviceId}/measuringinstrumentstatus")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMeasuringInstrumentStatus(@PathVariable String deviceId) {
        log.info("internal updateMeasuringInstrumentStatus deviceId={}", deviceId);
        deviceService.updateDeviceMeasuringInstrumentStatusByDeviceId(deviceId);
    }

    /**
     * "Match-all" sentinel for the native getDeviceIds / getLocationIdsByFilter queries: their
     * collection filters test {@code 'all' IN ?n}, so a list containing "all" bypasses that
     * dimension. Passing {@code null} instead throws a Postgres "syntax error at or near $1"
     * (a native {@code IN ?} cannot bind null). Reused read-only across params.
     */
    private static final List<String> ALL = Collections.singletonList("all");

    /** Device ids located under a floor (floor -> locations -> devices), tenant-free. */
    @GetMapping("/api/v1/device-asset-service/internal/devices/ids/byfloor/{floorId}")
    public List<String> deviceIdsByFloor(@PathVariable String floorId) {
        log.info("internal deviceIdsByFloor floorId={}", floorId);
        List<String> locationIds = locationService.getLocationIdsByFilter(null, null, null,
                ALL, Collections.singletonList(floorId), ALL);
        if (locationIds == null || locationIds.isEmpty()) return Collections.emptyList();
        return deviceService.getDeviceIdsByFilter(null, ALL, ALL, null, ALL, null, null, locationIds);
    }

    /** Device ids tagged to a location, tenant-free. */
    @GetMapping("/api/v1/device-asset-service/internal/devices/ids/bylocation/{locationId}")
    public List<String> deviceIdsByLocation(@PathVariable String locationId) {
        log.info("internal deviceIdsByLocation locationId={}", locationId);
        return deviceService.getDeviceIdsByFilter(null, ALL, ALL, null, ALL, null, null,
                Collections.singletonList(locationId));
    }

    /** Device ids whose device-side fields match the search key, tenant-free. */
    @GetMapping("/api/v1/device-asset-service/internal/devices/ids/bysearch")
    public List<String> deviceIdsBySearch(@RequestParam String searchKey) {
        log.info("internal deviceIdsBySearch searchKey={}", searchKey);
        return deviceService.getDeviceIdsByFilter(null, ALL, ALL, searchKey, ALL, null, null, ALL);
    }

    /** Subset of the given device ids whose device.monitor flag is set. Tenant-free. */
    @PostMapping("/api/v1/device-asset-service/internal/devices/monitored")
    public Set<String> monitoredDeviceIds(@RequestBody Set<String> deviceIds) {
        log.info("internal monitoredDeviceIds count={}", deviceIds == null ? 0 : deviceIds.size());
        return deviceService.getMonitoredDeviceIds(deviceIds);
    }
}
