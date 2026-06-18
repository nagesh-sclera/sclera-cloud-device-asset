
package io.sclera.controller.admin;

import io.sclera.dto.DeviceDTO;
import io.sclera.dto.LoadCalculationDTO;
import io.sclera.dto.SpecificationsDTO;
import io.sclera.service.SpecificationsService;
import io.sclera.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


/**
 * REST endpoints for managing device specifications and power-source tagging,
 * including load calculations.
 * Delegates all persistence and business logic to {@link SpecificationsService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Specifications", description = "Edit, upsert, fetch and delete device specifications and power-source tagging, including load calculations.")
public class SpecificationsController {
    private static final Logger log = LoggerFactory.getLogger(SpecificationsController.class);

    @Autowired
    SpecificationsService specificationsService;

    /**
     * Edits the supplied device specifications.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications to edit
     */
    @Operation(summary = "Edit device specifications",
            description = "Edits the supplied device specifications.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Specifications edited"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/editdevicespecifications")
    public void editDeviceSpecifications(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody List<SpecificationsDTO> specifications) {
        log.info("editDeviceSpecifications username={} vdmsid={}", username, vdmsid);
        specificationsService.editDeviceSpecifications(username, vdmsid, specifications);
    }

    /**
     * Upserts the supplied device specifications.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications to create or update
     * @return the persisted specifications
     */
    @Operation(summary = "Upsert device specifications",
            description = "Creates or updates the supplied device specifications.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Specifications upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/adddevicespecifications")
    public List<SpecificationsDTO> upsertDeviceSpecifications(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody List<SpecificationsDTO> specifications) {
        log.info("upsertDeviceSpecifications username={} vdmsid={}", username, vdmsid);
        return specificationsService.upsertDeviceSpecifications(username, vdmsid, specifications);
    }

    /**
     * Returns the specifications for the given device.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param device_id device whose specifications are requested
     * @return the device's specifications
     */
    @Operation(summary = "Get device specifications by device id",
            description = "Returns the specifications for the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Specifications returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{device_id}/getdevicespecificationsbydeviceid")
    public List<SpecificationsDTO> getDeviceSpecificationsByDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose specifications are requested") @PathVariable String device_id) {
        log.info("getDeviceSpecificationsByDeviceId username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        return specificationsService.getDeviceSpecificationsByDeviceId(username, vdmsid, device_id);
    }

    /**
     * Tags multiple devices to a given power source.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications describing the devices and power source to tag
     */
    @Operation(summary = "Tag devices to a power source",
            description = "Tags multiple devices to a given power source.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devices tagged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/tagpowersources")
    public void tagPowerSources(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody List<SpecificationsDTO> specifications) {
        log.info("tagPowerSources username={} vdmsid={}", username, vdmsid);
        specificationsService.tagPowerSources(username, vdmsid, specifications);
    }

    /**
     * Untags a power source from a device.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications describing the power source to untag
     */
    @Operation(summary = "Untag a power source",
            description = "Untags a power source from a device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Power source untagged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/untagpowersource")
    public void untagPowerSource(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody List<SpecificationsDTO> specifications) {
        log.info("untagPowerSource username={} vdmsid={}", username, vdmsid);
        specificationsService.untagPowerSource(username, vdmsid, specifications);
    }

    /**
     * Untags a device from its respective power source.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications describing the device to untag
     */
    @Operation(summary = "Untag a device",
            description = "Untags a device from its respective power source.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device untagged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/untagdevice")
    public void untagDevice(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody List<SpecificationsDTO> specifications) {
        log.info("untagDevice username={} vdmsid={}", username, vdmsid);
        specificationsService.untagDevice(username, vdmsid, specifications);
    }

    /**
     * Returns the devices tagged to a given output port, paginated.
     *
     * @param username          owning user
     * @param vdmsid            owning VDMS id
     * @param specificationsDTO specification describing the output port to match
     * @param pageno            page number to retrieve (default 1)
     * @param pagesize          number of records per page (default 10)
     * @return the matching page of tagged devices
     */
    @Operation(summary = "Get tagged devices by output port",
            description = "Returns the devices tagged to a given output port. Results are paginated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tagged devices returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/gettaggeddevices")
    public Page<DeviceDTO> getTaggedDevices(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody SpecificationsDTO specificationsDTO,
            @Parameter(description = "Page number to retrieve") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getTaggedDevices username={} vdmsid={}", username, vdmsid);
        return PageUtils.toPage(specificationsService.getTaggedDevices(username, vdmsid, specificationsDTO, pageno, pagesize), pageno, pagesize);
    }


    /**
     * Returns the power sources tagged to the given device.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @param device_id device whose tagged power sources are requested
     * @return the list of tagged power sources
     */
    @Operation(summary = "Get tagged power sources by device id",
            description = "Returns the power sources tagged to the given device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tagged power sources returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/device/{device_id}/gettaggedpowersourcesbydeviceid")
    public List<DeviceDTO> getTaggedPowerSourcesByDeviceId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose tagged power sources are requested") @PathVariable String device_id) {
        log.info("getTaggedPowerSourcesByDeviceId username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        return specificationsService.getTaggedPowerSourcesByDeviceId(username, vdmsid, device_id);
    }

    /**
     * Deletes the supplied device specifications.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications to delete
     */
    @Operation(summary = "Delete device specifications",
            description = "Deletes the supplied device specifications.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Specifications deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/deletespecifications")
    public void deleteSpecifications(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody List<SpecificationsDTO> specifications) {
        log.info("deleteSpecifications username={} vdmsid={}", username, vdmsid);
        specificationsService.deleteSpecifications(username, vdmsid, specifications);
    }

    /**
     * Returns the power-based load calculation for the supplied specifications.
     *
     * @param username       owning user
     * @param vdmsid         owning VDMS id
     * @param specifications specifications to base the load calculation on
     * @return the computed load calculations
     */
    @Operation(summary = "Get power-based load calculation",
            description = "Returns the power-based load calculation for the supplied specifications.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Load calculation returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/getpowerbasedloadcalculation")
    public List<LoadCalculationDTO> getPowerBasedLoadCalculation(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody List<SpecificationsDTO> specifications) {
        log.info("getPowerBasedLoadCalculation username={} vdmsid={}", username, vdmsid);
        return specificationsService.getPowerBasedLoadCalculation(username, vdmsid, specifications);
    }


}
