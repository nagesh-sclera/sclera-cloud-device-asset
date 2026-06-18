package io.sclera.controller.admin;

import io.sclera.dto.DeviceLifecycleHistoryDTO;
import io.sclera.service.DeviceLifecycleHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST endpoints recording and retrieving the lifecycle history of a device
 * (status changes, retirement, etc.). Delegates to {@link DeviceLifecycleHistoryService}.
 */
@RestController
@CrossOrigin(allowedHeaders = "*", origins = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Device Lifecycle History", description = "Record and retrieve the lifecycle history (status changes, retirement) of a device.")
public class DeviceLifecycleHistoryController {

    private static final Logger log = LoggerFactory.getLogger(DeviceLifecycleHistoryController.class);

    @Autowired
    DeviceLifecycleHistoryService deviceLifeCycleHistoryService;

    /**
     * Appends a lifecycle history entry for a device.
     *
     * @param username     owning user
     * @param vdmsid       owning VDMS id
     * @param dto          lifecycle history entry to record
     * @param retireStatus retirement status applied alongside the entry
     */
    @Operation(summary = "Add a device lifecycle history entry",
            description = "Appends a lifecycle history entry for a device and applies the supplied retirement status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History entry recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/adddevicehistory")
    public void addDeviceHistory(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody DeviceLifecycleHistoryDTO dto,
            @Parameter(description = "Retirement status applied alongside the entry") @RequestParam String retireStatus) {
        log.info("addDeviceHistory username={} vdmsid={} retireStatus={}", username, vdmsid, retireStatus);
        deviceLifeCycleHistoryService.addDeviceHistory(username, vdmsid, dto, retireStatus);
    }

    /**
     * Returns a paginated lifecycle history for the given device.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param device_id device whose history is fetched
     * @param pageno    1-based page number (default 1)
     * @param pagesize  page size (default 5)
     * @return set of lifecycle history entries for the requested page
     */
    @Operation(summary = "Get device lifecycle history",
            description = "Returns a paginated lifecycle history for the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{device_id}/getdevicehistory")
    public Set<DeviceLifecycleHistoryDTO> getDeviceHistory(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose history is fetched") @PathVariable String device_id,
            @Parameter(description = "1-based page number") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "5") Integer pagesize) {
        log.info("getDeviceHistory username={} vdmsid={} device_id={} pageno={} pagesize={}", username, vdmsid, device_id, pageno, pagesize);
        return deviceLifeCycleHistoryService.getDeviceHistory(username, vdmsid, device_id, pageno, pagesize);
    }
}
