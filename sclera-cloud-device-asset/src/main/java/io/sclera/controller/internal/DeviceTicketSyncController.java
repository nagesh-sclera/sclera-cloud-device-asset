package io.sclera.controller.internal;

import io.sclera.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal endpoint invoked by sclera-workorders (via Dapr service-invocation) to
 * recompute a device's ticket count and ticket status after a ticket change.
 * Mirrors the monolith's in-process call; idempotent and side-effect only.
 *
 * Full path (no global context-path on this service):
 *   PUT /api/v1/device-asset-service/internal/device/{deviceId}/ticket-sync
 */
@RestController
public class DeviceTicketSyncController {

    private static final Logger log = LoggerFactory.getLogger(DeviceTicketSyncController.class);

    private final DeviceService deviceService;

    public DeviceTicketSyncController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PutMapping("/api/v1/device-asset-service/internal/device/{deviceId}/ticket-sync")
    public ResponseEntity<Void> syncTicketStats(@PathVariable String deviceId) {
        log.info("[ticket-sync] recomputing ticket count/status for device={}", deviceId);
        deviceService.updateDeviceTicketCount(deviceId);
        deviceService.updateDeviceTicketStatus(deviceId);
        return ResponseEntity.noContent().build();
    }
}
