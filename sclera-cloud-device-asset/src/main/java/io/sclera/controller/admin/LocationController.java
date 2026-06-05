package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.TagDeviceOrLocationDTO;
import io.sclera.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

/**
 * REST endpoints for managing locations within floors and VDMS scopes.
 * Delegates all persistence and business logic to {@link LocationService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LocationController {

    @Autowired
    LocationService locationService;

    /**
     * Upserts the supplied locations under the given floor.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param floor_id           floor the locations belong to
     * @param locations          locations to create or update
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return the persisted set of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/upsertlocations")
    public Set<LocationDTO> upsertLocationsByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        return locationService.upsertLocationsByFloorId(username, vdms_id, floor_id, locations, httpServletRequest);
    }

    /**
     * Deletes the locations identified by the supplied ids.
     *
     * @param email        owning user email
     * @param vdms_id      owning VDMS id
     * @param location_ids ids of the locations to delete
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{email}/vdms/{vdms_id}/building/floor/deletelocations")
    public void deleteLocationsByIds(@PathVariable String email, @PathVariable String vdms_id, @RequestBody Set<String> location_ids) {
        locationService.deleteLocationsByIds(email, vdms_id, location_ids, false);
    }

    /**
     * Returns all locations for the given VDMS.
     *
     * @param username owning user
     * @param vdms_id  owning VDMS id
     * @return the set of locations under the VDMS
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/getlocations")
    public Set<LocationDTO> getLocationsByVdmsId(@PathVariable String username, @PathVariable String vdms_id) {
        return locationService.getLocationsByVdmsId(username, vdms_id);
    }

    /**
     * Returns all locations under the given floor.
     *
     * @param username owning user
     * @param vdms_id  owning VDMS id
     * @param floor_id floor whose locations are requested
     * @return the set of locations on the floor
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getlocationsbyfloorid")
    public Set<LocationDTO> getLocationsByFloor(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id) {
        return locationService.getLocationsByFloor(username, vdms_id, floor_id);
    }

    /**
     * Updates the details of the specified location.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param floor_id           floor the location belongs to
     * @param location_id        location to update
     * @param locations          updated location details
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/location/{location_id}/updatelocationdetails")
    public void updateLocationsDetailsByLocationId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @PathVariable String location_id, @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        locationService.updateLocationsDetailsByLocationId(username, vdms_id, floor_id, location_id, locations, httpServletRequest);
    }

    /**
     * Returns a paginated set of locations for the given floor matching the search and filter criteria.
     *
     * @param username     owning user
     * @param vdms_id      owning VDMS id
     * @param floor_id     floor whose locations are requested
     * @param pageno       page number to retrieve (default 1)
     * @param pagesize     number of records per page (default 10)
     * @param searchKey    search term to match (default "null")
     * @param field        optional field name to scope the query
     * @param field_id     optional field id to scope the query
     * @param filterObject filter criteria payload
     * @return the matching page of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getlocationsbyflooridpagination")
    public Set<LocationDTO> getLocationsByFloorByPagination(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id,
                                                            @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                            @RequestParam(defaultValue = "null") String searchKey, @RequestParam(required = false) String field, @RequestParam(required = false) String field_id,
                                                            @RequestBody JSONObject filterObject) {
        return locationService.getLocationsByFloorByPagination(username, vdms_id, floor_id, pageno, pagesize, searchKey, filterObject, field, field_id);
    }

    /**
     * Returns the count of locations on the given floor matching the search term.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @param floor_id  floor whose locations are counted
     * @param searchkey search term to match (default "null")
     * @return the location count as a string
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/getlocationscountbyfloorid")
    public String getLocationsCountByFloorId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id,
                                             @RequestParam(defaultValue = "null") String searchkey) {
        return locationService.getLocationsCountByFloorId(username, vdms_id, floor_id, searchkey);
    }

    /**
     * Returns the details of the specified location.
     *
     * @param username    owning user
     * @param vdms_id     owning VDMS id
     * @param location_id location whose details are requested
     * @return the location details
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/location/{location_id}/getlocationdetailsbylocationid")
    public LocationDTO getLocationDetailsByLocationId(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String location_id) {
        return locationService.getLocationDetailsByLocationId(username, vdms_id, location_id);
    }

    /**
     * Returns a paginated set of all locations for the given group matching the search criteria.
     *
     * @param username     owning user
     * @param vdmsid       owning VDMS id
     * @param group        group to scope the query
     * @param searchkey    search term to match (default "null")
     * @param pageno       page number to retrieve (default 1)
     * @param pagesize     number of records per page (default 10)
     * @param filterObject filter criteria payload
     * @return the matching page of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/group/{group}/getalllocationspagination")
    public Set<LocationDTO> getAllLocationsPagination(@PathVariable String username, @PathVariable String vdmsid,
                                                      @PathVariable String group, @RequestParam(defaultValue = "null") String searchkey,
                                                      @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                      @RequestBody JSONObject filterObject) {
        return locationService.getAllLocationsPagination(username, vdmsid, group, searchkey, pageno, pagesize, filterObject);
    }

    /**
     * Returns the count of locations matching the search, sort, and filter criteria.
     *
     * @param username     owning user
     * @param vdms_id      owning VDMS id
     * @param searchKey    search term to match (default "null")
     * @param filterObject filter criteria payload
     * @return the matching location count
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/searchSortFilterLocationsCount")
    public int searchSortFilterLocationsCount(@PathVariable String username, @PathVariable String vdms_id, @RequestParam(defaultValue = "null") String searchKey,
                                              @RequestBody JSONObject filterObject) {
        return locationService.searchSortFilterLocationsCount(username, vdms_id, searchKey, filterObject);
    }

    /**
     * Returns the locations tagged to the given measuring instrument.
     *
     * @param username                owning user
     * @param vdmsid                  owning VDMS id
     * @param measuring_instrument_id measuring instrument whose tagged locations are requested
     * @return the set of tagged locations
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/measuring_instrument_id/{measuring_instrument_id}/gettaggedmeasuringinstrumentlocations")
    public Set<LocationDTO> getTaggedMeasuringInstrumentLocations(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String measuring_instrument_id) {
        return locationService.getTaggedMeasuringInstrumentLocations(username, vdmsid, measuring_instrument_id);
    }

    /**
     * Returns the distinct location types defined for the given VDMS.
     *
     * @param username owning user
     * @param vdms_id  owning VDMS id
     * @return the list of unique location types
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/getuniquelocationtypes")
    public List<String> getUniqueLocationTypes(@PathVariable String username, @PathVariable String vdms_id) {
        return locationService.getUniqueLocationTypes(username, vdms_id);
    }

    /**
     * Applies a bulk tag/update operation to multiple locations on the given floor.
     *
     * @param username                owning user
     * @param vdms_id                 owning VDMS id
     * @param floor_id                floor the locations belong to
     * @param tagDeviceOrLocationDTO  bulk tag/update payload
     * @param httpServletRequest      current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/multiupdatelocations")
    public void multiUpateLocations(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @RequestBody TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest) {
        locationService.multiUpdateLocations(username, vdms_id, floor_id, tagDeviceOrLocationDTO, httpServletRequest);
    }

    /**
     * Upserts the detailed information of the supplied locations on the given floor.
     *
     * @param username           owning user
     * @param vdms_id            owning VDMS id
     * @param floor_id           floor the locations belong to
     * @param locations          location details to create or update
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return the persisted set of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/floor/{floor_id}/upsertlocationsdetails")
    public Set<LocationDTO> upsertlocationsdetails(@PathVariable String username, @PathVariable String vdms_id, @PathVariable String floor_id, @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        return locationService.upsertlocationsdetails(username, vdms_id, floor_id, locations, httpServletRequest);
    }

    /**
     * Returns a paginated set of all locations for the VDMS matching the search and filter criteria.
     *
     * @param username     owning user
     * @param vdms_id      owning VDMS id
     * @param pageno       page number to retrieve (default 1)
     * @param pagesize     number of records per page (default 10)
     * @param searchKey    search term to match (default "null")
     * @param field        optional field name to scope the query
     * @param field_id     optional field id to scope the query
     * @param filterObject filter criteria payload
     * @return the matching page of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdms_id}/getalllocationsbyfilterbypagination")
    public Set<LocationDTO> getAllLocationsByFilterByPagination(@PathVariable String username, @PathVariable String vdms_id,
                                                                @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                                @RequestParam(defaultValue = "null") String searchKey, @RequestParam(required = false) String field, @RequestParam(required = false) String field_id,
                                                                @RequestBody JSONObject filterObject) {
        return locationService.getAllLocationsByFilterByPagination(username, vdms_id, pageno, pagesize, searchKey, filterObject, field, field_id);
    }
}
