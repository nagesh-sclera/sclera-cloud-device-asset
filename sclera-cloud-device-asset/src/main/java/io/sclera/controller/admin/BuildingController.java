package io.sclera.controller.admin;

import io.sclera.dto.BuildingDTO;
import io.sclera.dto.FloorDTO;
import io.sclera.service.BuildingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class BuildingController {

    @Autowired
    private BuildingService buildingService;

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/upsertbuildings")
    public Set<BuildingDTO> upsertBuildingsByVdmsId(@PathVariable String username, @PathVariable String vdms_id, @RequestBody Set<BuildingDTO> buildings, HttpServletRequest httpServletRequest) {
        return buildingService.upsertBuildingsByVdmsId(username, vdms_id, buildings, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/building/floor/location/{location_id}/getbuildingbylocation")
    public BuildingDTO getBuildingByLocationId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String location_id) {
        return buildingService.getBuildingByLocationId(username, vdms_id, location_id);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/getbuildingsbyvdmsid")
    public Set<BuildingDTO> getBuildingsByVdmsId(@PathVariable String username, @PathVariable String vdms_id,
                                                 @RequestParam(required = false) String field,@RequestParam(required = false) String field_id) {
        return buildingService.getBuildingsByVdmsId(vdms_id, field, field_id);
    }


    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/deletebuildings")
    public void deleteBuildingsByIds(@PathVariable String username, @PathVariable String vdmsid, @RequestBody Set<String> building_ids, HttpServletRequest httpServletRequest) {
        buildingService.deleteBuildingsByIds(username, vdmsid, building_ids, httpServletRequest);
    }

    //  syncLocationsFromBackend to be deleted after sync
    //  syncLocationsFromBackend to be deleted after sync
    @RequestMapping(method = RequestMethod.GET, value = "/syncbuildings")
    public Map<String, Object> syncLocationsFromBackend(HttpServletRequest httpServletRequest) {
        return buildingService.syncLocationsFromBackend(httpServletRequest);
    }

    //  syncLocationsFromBackend to be deleted after sync
    @RequestMapping(method = RequestMethod.GET, value = "/vdms/{vdms_id}/syncfloormaps")
    public Set<FloorDTO> syncFloorMaps(@PathVariable String vdms_id) {
        return buildingService.syncFloorMaps(vdms_id);
    }

    //  syncLocationsFromBackend to be deleted after sync
    @RequestMapping(method = RequestMethod.POST, value = "/vdms/{vdms_id}/updatefloormaps")
    public List<FloorDTO> updateFloorMaps(@PathVariable String vdms_id, @RequestBody List<FloorDTO> floorImages) {
        System.out.println("******************Floor Images********************* " + floorImages);
        return buildingService.updateFloorMaps(vdms_id, floorImages);
    }

    //  syncFloorMapsTiles to be deleted after sync
    @RequestMapping(method = RequestMethod.GET, value = "/syncfloormapstiles")
    public List<FloorDTO> syncFloorMapsTiles() {

        return buildingService.syncFloorMapsTiles();
    }
}

