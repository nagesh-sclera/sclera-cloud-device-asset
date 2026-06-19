package io.sclera.controller.admin;

import io.sclera.dto.LocationHistoryDTO;
import io.sclera.service.LocationHistoryService;
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
 * REST endpoints recording and retrieving the change history of a location.
 * Delegates to {@link LocationHistoryService}.
 */
@RestController
@CrossOrigin(allowedHeaders = "*", origins = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Location History", description = "Record and retrieve the change history of a location.")
public class LocationHistoryController {

    private static final Logger log = LoggerFactory.getLogger(LocationHistoryController.class);

    @Autowired
    LocationHistoryService locationHistoryService;

    /**
     * Appends a history entry describing a location change.
     *
     * @param username        owning user
     * @param vdmsid          owning VDMS id
     * @param locationHistory history entry to record
     */
    @Operation(summary = "Add a location history entry",
            description = "Appends a history entry describing a location change.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History entry recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/addlocationhistory")
    public void addLocationHistory(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody LocationHistoryDTO locationHistory) {
        log.info("addLocationHistory username={} vdmsid={}", username, vdmsid);
        locationHistoryService.addLocationHistory(username, vdmsid, locationHistory);
    }

    /**
     * Returns the recorded history for the given location.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param location_id location whose history is fetched
     * @return set of history entries for the location
     */
    @Operation(summary = "Get location history",
            description = "Returns the recorded history for the given location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History returned"),
            @ApiResponse(responseCode = "404", description = "Location not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/location/{location_id}/getlocationhistory")
    public Set<LocationHistoryDTO> getLocationHistory(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Location whose history is fetched") @PathVariable String location_id) {
        log.info("getLocationHistory username={} vdmsid={} location_id={}", username, vdmsid, location_id);
        return locationHistoryService.getLocationHistory(username, vdmsid, location_id);
    }
}
