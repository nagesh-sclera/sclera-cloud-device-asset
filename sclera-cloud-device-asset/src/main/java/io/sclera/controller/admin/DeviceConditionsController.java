package io.sclera.controller.admin;

import io.sclera.dto.DeviceConditionsDTO;
import io.sclera.dto.ShareConditionsDTO;
import io.sclera.service.DeviceConditionsService;
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
 * REST endpoints for managing device conditions, including sharing and resetting them.
 * Delegates all persistence and business logic to {@link DeviceConditionsService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Device Conditions", description = "Upsert, read, share, reset and delete device conditions for a VDMS.")
public class DeviceConditionsController {

    private static final Logger log = LoggerFactory.getLogger(DeviceConditionsController.class);

    @Autowired
    DeviceConditionsService deviceConditionsService;

    /**
     * Creates or updates the given device conditions for a docker.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker the conditions belong to
     * @param device_conditions  set of device conditions to upsert
     */
    @Operation(summary = "Upsert device conditions",
            description = "Creates or updates the given device conditions for a docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device conditions upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/upsertdeviceconditions")
    public void upsertDeviceConditions(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker the conditions belong to") @PathVariable String dockername,
            @RequestBody Set<DeviceConditionsDTO> device_conditions) {
        log.info("upsertDeviceConditions username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceConditionsService.upsertDeviceConditions(username, vdmsid, dockername, device_conditions);
    }

    /**
     * Returns the device conditions for a specific device under a docker.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param dockername  docker the conditions belong to
     * @param device_id   device whose conditions are requested
     * @return set of device conditions for the device
     */
    @Operation(summary = "Get device conditions for a device",
            description = "Returns the device conditions for a specific device under a docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device conditions returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/device/{device_id}/getdeviceconditions")
    public Set<DeviceConditionsDTO> getDeviceConditions(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker the conditions belong to") @PathVariable String dockername,
            @Parameter(description = "Device whose conditions are requested") @PathVariable String device_id) {
        log.info("getDeviceConditions username={} vdmsid={} dockername={} device_id={}", username, vdmsid, dockername, device_id);
        return deviceConditionsService.getDeviceConditions(username, vdmsid, dockername, device_id);
    }

    /**
     * Deletes all conditions for the given device.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param device_id  device whose conditions are deleted
     */
    @Operation(summary = "Delete all device conditions",
            description = "Deletes all conditions for the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device conditions deleted"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/device/{device_id}/deletealldeviceconditions")
    public void deleteAllDeviceConditions(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose conditions are deleted") @PathVariable String device_id) {
        log.info("deleteAllDeviceConditions username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        deviceConditionsService.deleteAllDeviceConditions(username, vdmsid, device_id);
    }

    /**
     * Returns a single device condition by its id.
     *
     * @param username      owning user
     * @param vdmsid        owning VDMS id
     * @param condition_id  id of the condition to retrieve
     * @return the matching device condition
     */
    @Operation(summary = "Get a device condition by id",
            description = "Returns a single device condition by its id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device condition found"),
            @ApiResponse(responseCode = "404", description = "Device condition not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/condition/{condition_id}/getdeviceconditionsbyid")
    public DeviceConditionsDTO getDeviceConditionsById(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Id of the condition to retrieve") @PathVariable String condition_id) {
        log.info("getDeviceConditionsById username={} vdmsid={} condition_id={}", username, vdmsid, condition_id);
        return deviceConditionsService.getDeviceConditionsById(username, vdmsid, condition_id);
    }

    /**
     * Deletes the given device conditions.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param device_conditions  set of device conditions to delete
     */
    @Operation(summary = "Delete device conditions",
            description = "Deletes the given device conditions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device conditions deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/deletedeviceconditions")
    public void deleteDeviceConditions(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<DeviceConditionsDTO> device_conditions) {
        log.info("deleteDeviceConditions username={} vdmsid={}", username, vdmsid);
        deviceConditionsService.deleteDeviceConditions(username, vdmsid, device_conditions);
    }

    /**
     * Shares device conditions with the targets described in the payload.
     *
     * @param username         owning user
     * @param vdmsid           owning VDMS id
     * @param dockername       docker the conditions belong to
     * @param shareConditions  payload describing the conditions and share targets
     */
    @Operation(summary = "Share device conditions",
            description = "Shares device conditions with the targets described in the payload.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device conditions shared"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/sharedeviceconditions")
    public void shareDeviceConditions(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker the conditions belong to") @PathVariable String dockername,
            @RequestBody ShareConditionsDTO shareConditions) {
        log.info("shareDeviceConditions username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceConditionsService.shareDeviceConditions(username, vdmsid, dockername, shareConditions);
    }

    /**
     * Resets the given device conditions to their default state for a docker.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker the conditions belong to
     * @param device_conditions  set of device conditions to reset
     */
    @Operation(summary = "Reset device conditions",
            description = "Resets the given device conditions to their default state for a docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device conditions reset"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/resetdeviceconditions")
    public void resetDeviceConditions(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker the conditions belong to") @PathVariable String dockername,
            @RequestBody Set<DeviceConditionsDTO> device_conditions) {
        log.info("resetDeviceConditions username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        deviceConditionsService.resetDeviceConditions(username, vdmsid, dockername, device_conditions);
    }
}
