package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.TagDeviceOrLocationDTO;
import io.sclera.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LocationController {

    @Autowired
    LocationService locationService;

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/upsertlocations")
    public Set<LocationDTO> upsertLocationsByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        return locationService.upsertLocationsByFloorId(username, vdms_id, floor_id, locations, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{email}/vdms/{vdms_id}/building/floor/deletelocations")
    public void deleteLocationsByIds(@PathVariable String email, @PathVariable String vdms_id, @RequestBody Set<String> location_ids) {
        locationService.deleteLocationsByIds(email, vdms_id, location_ids, false);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/getlocations")
    public Set<LocationDTO> getLocationsByVdmsId(@PathVariable String username, @PathVariable String vdms_id) {
        return locationService.getLocationsByVdmsId(username, vdms_id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getlocationsbyfloorid")
    public Set<LocationDTO> getLocationsByFloor(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id) {
        return locationService.getLocationsByFloor(username, vdms_id, floor_id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/location/{location_id}/updatelocationdetails")
    public void updateLocationsDetailsByLocationId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @PathVariable String location_id, @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        locationService.updateLocationsDetailsByLocationId(username, vdms_id, floor_id, location_id, locations, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getlocationsbyflooridpagination")
    public Set<LocationDTO> getLocationsByFloorByPagination(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id,
                                                            @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                            @RequestParam(defaultValue = "null") String searchKey, @RequestParam(required = false) String field, @RequestParam(required = false) String field_id,
                                                            @RequestBody JSONObject filterObject) {
        return locationService.getLocationsByFloorByPagination(username, vdms_id, floor_id, pageno, pagesize, searchKey, filterObject, field, field_id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getlocationscountbyfloorid")
    public String getLocationsCountByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id,
                                             @RequestParam(defaultValue = "null") String searchkey) {
        return locationService.getLocationsCountByFloorId(username, vdms_id, floor_id, searchkey);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/location/{location_id}/getlocationdetailsbylocationid")
    public LocationDTO getLocationDetailsByLocationId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String location_id) {
        return locationService.getLocationDetailsByLocationId(username, vdms_id, location_id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/group/{group}/getalllocationspagination")
    public Set<LocationDTO> getAllLocationsPagination(@PathVariable String username, @PathVariable String vdmsid,
                                                      @PathVariable String group, @RequestParam(defaultValue = "null") String searchkey,
                                                      @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                      @RequestBody JSONObject filterObject) {
        return locationService.getAllLocationsPagination(username, vdmsid, group, searchkey, pageno, pagesize, filterObject);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/searchSortFilterLocationsCount")
    public int searchSortFilterLocationsCount(@PathVariable String username, @PathVariable String vdms_id, @RequestParam(defaultValue = "null") String searchKey,
                                              @RequestBody JSONObject filterObject) {
        return locationService.searchSortFilterLocationsCount(username, vdms_id, searchKey, filterObject);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/measuring_instrument_id/{measuring_instrument_id}/gettaggedmeasuringinstrumentlocations")
    public Set<LocationDTO> getTaggedMeasuringInstrumentLocations(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String measuring_instrument_id) {
        return locationService.getTaggedMeasuringInstrumentLocations(username, vdmsid, measuring_instrument_id);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/getuniquelocationtypes")
    public List<String> getUniqueLocationTypes(@PathVariable String username, @PathVariable String vdms_id) {
        return locationService.getUniqueLocationTypes(username, vdms_id);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/multiupdatelocations")
    public void multiUpateLocations(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @RequestBody TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest) {
        locationService.multiUpdateLocations(username, vdms_id, floor_id, tagDeviceOrLocationDTO, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/upsertlocationsdetails")
    public Set<LocationDTO> upsertlocationsdetails(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        return locationService.upsertlocationsdetails(username, vdms_id, floor_id, locations, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/getalllocationsbyfilterbypagination")
    public Set<LocationDTO> getAllLocationsByFilterByPagination(@PathVariable String username, @PathVariable String vdms_id,
                                                                @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                                @RequestParam(defaultValue = "null") String searchKey, @RequestParam(required = false) String field, @RequestParam(required = false) String field_id,
                                                                @RequestBody JSONObject filterObject) {
        return locationService.getAllLocationsByFilterByPagination(username, vdms_id, pageno, pagesize, searchKey, filterObject, field, field_id);
    }
}
