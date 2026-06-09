package io.sclera.controller.admin;

import io.sclera.dto.BuildingDTO;
import io.sclera.dto.FloorDTO;
import io.sclera.service.BuildingService;
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
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    /**
     * Creates or updates the given buildings for the tenant.
     *
     * @param username            owning user
     * @param vdms_id             owning VDMS id
     * @param buildings           set of buildings to upsert
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     * @return set of upserted buildings
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upsertbuildings")
    public Set<BuildingDTO> upsertBuildingsByVdmsId(@RequestParam String username, @RequestParam String vdms_id, @RequestBody Set<BuildingDTO> buildings, HttpServletRequest httpServletRequest) {
        return buildingService.upsertBuildingsByVdmsId(username, vdms_id, buildings, httpServletRequest);
    }

    /**
     * Returns the building that contains the given location.
     *
     * @param username     owning user
     * @param vdms_id      owning VDMS id
     * @param location_id  location whose building is requested
     * @return building containing the location
     */
    @RequestMapping(method = RequestMethod.GET, value = "/building/floor/location/{location_id}/getbuildingbylocation")
    public BuildingDTO getBuildingByLocationId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String location_id) {
        return buildingService.getBuildingByLocationId(username, vdms_id, location_id);
    }


    /**
     * Returns the buildings for the tenant, optionally filtered by a field and value.
     *
     * @param vdms_id   owning VDMS id
     * @param field     optional field name to filter on
     * @param field_id  optional field value to match
     * @return set of matching buildings
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getbuildingsbyvdmsid")
    public Set<BuildingDTO> getBuildingsByVdmsId(@RequestParam String vdms_id,
                                                 @RequestParam(required = false) String field,@RequestParam(required = false) String field_id) {
        return buildingService.getBuildingsByVdmsId(vdms_id, field, field_id);
    }


    /**
     * Deletes the buildings identified by the given ids.
     *
     * @param username            owning user
     * @param vdmsid              owning VDMS id
     * @param building_ids        set of building ids to delete
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/deletebuildings")
    public void deleteBuildingsByIds(@RequestParam String username, @RequestParam String vdmsid, @RequestBody Set<String> building_ids, HttpServletRequest httpServletRequest) {
        buildingService.deleteBuildingsByIds(username, vdmsid, building_ids, httpServletRequest);
    }

    //  syncLocationsFromBackend to be deleted after sync
    //  syncLocationsFromBackend to be deleted after sync
    /**
     * Synchronizes building locations from the backend (temporary sync endpoint).
     *
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     * @return map describing the sync result
     */
    @RequestMapping(method = RequestMethod.GET, value = "/syncbuildings")
    public Map<String, Object> syncLocationsFromBackend(HttpServletRequest httpServletRequest) {
        return buildingService.syncLocationsFromBackend(httpServletRequest);
    }

    //  syncLocationsFromBackend to be deleted after sync
    /**
     * Synchronizes floor maps for the given VDMS from the backend (temporary sync endpoint).
     *
     * @param vdms_id  owning VDMS id
     * @return set of synchronized floors
     */
    @RequestMapping(method = RequestMethod.GET, value = "/syncfloormaps")
    public Set<FloorDTO> syncFloorMaps(@RequestParam String vdms_id) {
        return buildingService.syncFloorMaps(vdms_id);
    }

    //  syncLocationsFromBackend to be deleted after sync
    /**
     * Updates floor map images for the given VDMS (temporary sync endpoint).
     *
     * @param vdms_id      owning VDMS id
     * @param floorImages  floor map images to apply
     * @return list of updated floors
     */
    @RequestMapping(method = RequestMethod.POST, value = "/updatefloormaps")
    public List<FloorDTO> updateFloorMaps(@RequestParam String vdms_id, @RequestBody List<FloorDTO> floorImages) {
        System.out.println("******************Floor Images********************* " + floorImages);
        return buildingService.updateFloorMaps(vdms_id, floorImages);
    }

    //  syncFloorMapsTiles to be deleted after sync
    /**
     * Synchronizes floor map tiles from the backend (temporary sync endpoint).
     *
     * @return list of floors whose map tiles were synchronized
     */
    @RequestMapping(method = RequestMethod.GET, value = "/syncfloormapstiles")
    public List<FloorDTO> syncFloorMapsTiles() {

        return buildingService.syncFloorMapsTiles();
    }
}

