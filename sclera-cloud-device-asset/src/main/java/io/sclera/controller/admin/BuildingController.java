package io.sclera.controller.admin;

import io.sclera.dto.BuildingDTO;
import io.sclera.dto.FloorDTO;
import io.sclera.service.BuildingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST endpoints for managing buildings, floors and floor maps for a VDMS.
 * Delegates all persistence and business logic to {@link BuildingService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Buildings", description = "Create, read, delete and sync buildings, floors and floor maps for a VDMS.")
public class BuildingController {

    private static final Logger log = LoggerFactory.getLogger(BuildingController.class);

    @Autowired
    private BuildingService buildingService;

    /**
     * Creates or updates the given buildings for the tenant.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param buildings          set of buildings to upsert
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return set of upserted buildings
     */
    @Operation(summary = "Upsert buildings for a VDMS",
            description = "Creates or updates the given set of buildings for the tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buildings upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertbuildings")
    public Set<BuildingDTO> upsertBuildingsByVdmsId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @RequestBody Set<BuildingDTO> buildings, HttpServletRequest httpServletRequest) {
        log.info("upsertBuildingsByVdmsId username={} vdms_id={}", username, vdms_id);
        return buildingService.upsertBuildingsByVdmsId(username, vdms_id, buildings, httpServletRequest);
    }

    /**
     * Returns the building that contains the given location.
     *
     * @param username    owning user
     * @param vdms_id     owning VDMS id
     * @param location_id location whose building is requested
     * @return building containing the location
     */
    @Operation(summary = "Get building by location",
            description = "Returns the building that contains the given location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Building found"),
            @ApiResponse(responseCode = "404", description = "No building for the location"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/building/floor/location/{location_id}/getbuildingbylocation")
    public BuildingDTO getBuildingByLocationId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Location whose building is requested") @PathVariable String location_id) {
        log.info("getBuildingByLocationId username={} vdms_id={} location_id={}", username, vdms_id, location_id);
        return buildingService.getBuildingByLocationId(username, vdms_id, location_id);
    }

    /**
     * Returns the buildings for the tenant, optionally filtered by a field and value.
     *
     * @param vdms_id  owning VDMS id
     * @param field    optional field name to filter on
     * @param field_id optional field value to match
     * @return set of matching buildings
     */
    @Operation(summary = "Get buildings for a VDMS",
            description = "Returns the buildings for the tenant, optionally filtered by a field name and value.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buildings returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getbuildingsbyvdmsid")
    public Set<BuildingDTO> getBuildingsByVdmsId(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Optional field name to filter on") @RequestParam(required = false) String field,
            @Parameter(description = "Optional field value to match") @RequestParam(required = false) String field_id) {
        log.info("getBuildingsByVdmsId vdms_id={}", vdms_id);
        return buildingService.getBuildingsByVdmsId(vdms_id, field, field_id);
    }

    /**
     * Deletes the buildings identified by the given ids.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param building_ids       set of building ids to delete
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Delete buildings by ids",
            description = "Deletes the buildings identified by the given ids.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Buildings deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/deletebuildings")
    public void deleteBuildingsByIds(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<String> building_ids, HttpServletRequest httpServletRequest) {
        log.info("deleteBuildingsByIds username={} vdmsid={}", username, vdmsid);
        buildingService.deleteBuildingsByIds(username, vdmsid, building_ids, httpServletRequest);
    }

    /**
     * Synchronizes building locations from the backend (temporary sync endpoint).
     *
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return map describing the sync result
     */
    @Operation(summary = "Sync buildings from backend",
            description = "Synchronizes building locations from the backend. Temporary sync endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync completed"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/syncbuildings")
    public Map<String, Object> syncLocationsFromBackend(HttpServletRequest httpServletRequest) {
        log.info("syncLocationsFromBackend called");
        return buildingService.syncLocationsFromBackend(httpServletRequest);
    }

    /**
     * Synchronizes floor maps for the given VDMS from the backend (temporary sync endpoint).
     *
     * @param vdms_id owning VDMS id
     * @return set of synchronized floors
     */
    @Operation(summary = "Sync floor maps from backend",
            description = "Synchronizes floor maps for the given VDMS from the backend. Temporary sync endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor maps synced"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/syncfloormaps")
    public Set<FloorDTO> syncFloorMaps(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id) {
        log.info("syncFloorMaps vdms_id={}", vdms_id);
        return buildingService.syncFloorMaps(vdms_id);
    }

    /**
     * Updates floor map images for the given VDMS (temporary sync endpoint).
     *
     * @param vdms_id     owning VDMS id
     * @param floorImages floor map images to apply
     * @return list of updated floors
     */
    @Operation(summary = "Update floor maps",
            description = "Updates floor map images for the given VDMS. Temporary sync endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor maps updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/updatefloormaps")
    public List<FloorDTO> updateFloorMaps(
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @RequestBody List<FloorDTO> floorImages) {
        log.info("updateFloorMaps vdms_id={} count={}", vdms_id, floorImages == null ? 0 : floorImages.size());
        return buildingService.updateFloorMaps(vdms_id, floorImages);
    }

    /**
     * Synchronizes floor map tiles from the backend (temporary sync endpoint).
     *
     * @return list of floors whose map tiles were synchronized
     */
    @Operation(summary = "Sync floor map tiles",
            description = "Synchronizes floor map tiles from the backend. Temporary sync endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Floor map tiles synced"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/syncfloormapstiles")
    public List<FloorDTO> syncFloorMapsTiles() {
        log.info("syncFloorMapsTiles called");
        return buildingService.syncFloorMapsTiles();
    }
}
