package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.TagDeviceOrLocationDTO;
import io.sclera.service.LocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class LocationController {

    private static final Logger log = LoggerFactory.getLogger(LocationController.class);

    @Autowired
    LocationService locationService;

    /**
     * Upserts the supplied locations under the given floor.
     *
     * @param floor_id           floor the locations belong to
     * @param locations          locations to create or update
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return the persisted set of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/floor/{floor_id}/upsertlocations")
    public Set<LocationDTO> upsertLocationsByFloorId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id, @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        log.info("upsertLocationsByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        try {
            return locationService.upsertLocationsByFloorId(username, vdms_id, floor_id, locations, httpServletRequest);
        } catch (Exception e) {
            log.error("upsertLocationsByFloorId failed username={} vdms_id={} floor_id={}: {}", username, vdms_id, floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes the locations identified by the supplied ids.
     *
     * @param location_ids ids of the locations to delete
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/building/floor/deletelocations")
    public void deleteLocationsByIds(@RequestParam String email, @RequestParam String vdms_id, @RequestBody Set<String> location_ids) {
        log.info("deleteLocationsByIds email={} vdms_id={}", email, vdms_id);
        try {
            locationService.deleteLocationsByIds(email, vdms_id, location_ids, false);
        } catch (Exception e) {
            log.error("deleteLocationsByIds failed email={} vdms_id={}: {}", email, vdms_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns all locations for the given VDMS.
     *
     * @return the set of locations under the VDMS
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getlocations")
    public Set<LocationDTO> getLocationsByVdmsId(@RequestParam String username, @RequestParam String vdms_id) {
        log.info("getLocationsByVdmsId username={} vdms_id={}", username, vdms_id);
        try {
            return locationService.getLocationsByVdmsId(username, vdms_id);
        } catch (Exception e) {
            log.error("getLocationsByVdmsId failed username={} vdms_id={}: {}", username, vdms_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns all locations under the given floor.
     *
     * @param floor_id floor whose locations are requested
     * @return the set of locations on the floor
     */
    @RequestMapping(method = RequestMethod.GET, value = "/floor/{floor_id}/getlocationsbyfloorid")
    public Set<LocationDTO> getLocationsByFloor(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id) {
        log.info("getLocationsByFloor username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        try {
            return locationService.getLocationsByFloor(username, vdms_id, floor_id);
        } catch (Exception e) {
            log.error("getLocationsByFloor failed username={} vdms_id={} floor_id={}: {}", username, vdms_id, floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates the details of the specified location.
     *
     * @param floor_id           floor the location belongs to
     * @param location_id        location to update
     * @param locations          updated location details
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/floor/{floor_id}/location/{location_id}/updatelocationdetails")
    public void updateLocationsDetailsByLocationId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id, @PathVariable String location_id, @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        log.info("updateLocationsDetailsByLocationId username={} vdms_id={} floor_id={} location_id={}", username, vdms_id, floor_id, location_id);
        try {
            locationService.updateLocationsDetailsByLocationId(username, vdms_id, floor_id, location_id, locations, httpServletRequest);
        } catch (Exception e) {
            log.error("updateLocationsDetailsByLocationId failed username={} vdms_id={} floor_id={} location_id={}: {}", username, vdms_id, floor_id, location_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns a paginated set of locations for the given floor matching the search and filter criteria.
     *
     * @param floor_id     floor whose locations are requested
     * @param pageno       page number to retrieve (default 1)
     * @param pagesize     number of records per page (default 10)
     * @param searchKey    search term to match (default "null")
     * @param field        optional field name to scope the query
     * @param field_id     optional field id to scope the query
     * @param filterObject filter criteria payload
     * @return the matching page of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/floor/{floor_id}/getlocationsbyflooridpagination")
    public Set<LocationDTO> getLocationsByFloorByPagination(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id,
                                                            @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                            @RequestParam(defaultValue = "null") String searchKey, @RequestParam(required = false) String field, @RequestParam(required = false) String field_id,
                                                            @RequestBody JSONObject filterObject) {
        log.info("getLocationsByFloorByPagination username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        try {
            return locationService.getLocationsByFloorByPagination(username, vdms_id, floor_id, pageno, pagesize, searchKey, filterObject, field, field_id);
        } catch (Exception e) {
            log.error("getLocationsByFloorByPagination failed username={} vdms_id={} floor_id={}: {}", username, vdms_id, floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the count of locations on the given floor matching the search term.
     *
     * @param floor_id  floor whose locations are counted
     * @param searchkey search term to match (default "null")
     * @return the location count as a string
     */
    @RequestMapping(method = RequestMethod.GET, value = "/floor/{floor_id}/getlocationscountbyfloorid")
    public String getLocationsCountByFloorId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id,
                                             @RequestParam(defaultValue = "null") String searchkey) {
        log.info("getLocationsCountByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        try {
            return locationService.getLocationsCountByFloorId(username, vdms_id, floor_id, searchkey);
        } catch (Exception e) {
            log.error("getLocationsCountByFloorId failed username={} vdms_id={} floor_id={}: {}", username, vdms_id, floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the details of the specified location.
     *
     * @param location_id location whose details are requested
     * @return the location details
     */
    @RequestMapping(method = RequestMethod.GET, value = "/location/{location_id}/getlocationdetailsbylocationid")
    public LocationDTO getLocationDetailsByLocationId(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String location_id) {
        log.info("getLocationDetailsByLocationId username={} vdms_id={} location_id={}", username, vdms_id, location_id);
        try {
            return locationService.getLocationDetailsByLocationId(username, vdms_id, location_id);
        } catch (Exception e) {
            log.error("getLocationDetailsByLocationId failed username={} vdms_id={} location_id={}: {}", username, vdms_id, location_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns a paginated set of all locations for the given group matching the search criteria.
     *
     * @param group        group to scope the query
     * @param searchkey    search term to match (default "null")
     * @param pageno       page number to retrieve (default 1)
     * @param pagesize     number of records per page (default 10)
     * @param filterObject filter criteria payload
     * @return the matching page of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/group/{group}/getalllocationspagination")
    public Set<LocationDTO> getAllLocationsPagination(@RequestParam String username, @RequestParam String vdmsid,
                                                      @PathVariable String group, @RequestParam(defaultValue = "null") String searchkey,
                                                      @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                      @RequestBody JSONObject filterObject) {
        log.info("getAllLocationsPagination username={} vdmsid={} group={}", username, vdmsid, group);
        try {
            return locationService.getAllLocationsPagination(username, vdmsid, group, searchkey, pageno, pagesize, filterObject);
        } catch (Exception e) {
            log.error("getAllLocationsPagination failed username={} vdmsid={} group={}: {}", username, vdmsid, group, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the count of locations matching the search, sort, and filter criteria.
     *
     * @param searchKey    search term to match (default "null")
     * @param filterObject filter criteria payload
     * @return the matching location count
     */
    @RequestMapping(method = RequestMethod.POST, value = "/searchSortFilterLocationsCount")
    public int searchSortFilterLocationsCount(@RequestParam String username, @RequestParam String vdms_id, @RequestParam(defaultValue = "null") String searchKey,
                                              @RequestBody JSONObject filterObject) {
        log.info("searchSortFilterLocationsCount username={} vdms_id={}", username, vdms_id);
        try {
            return locationService.searchSortFilterLocationsCount(username, vdms_id, searchKey, filterObject);
        } catch (Exception e) {
            log.error("searchSortFilterLocationsCount failed username={} vdms_id={}: {}", username, vdms_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the locations tagged to the given measuring instrument.
     *
     * @param measuring_instrument_id measuring instrument whose tagged locations are requested
     * @return the set of tagged locations
     */
    @RequestMapping(method = RequestMethod.GET, value = "/measuring_instrument_id/{measuring_instrument_id}/gettaggedmeasuringinstrumentlocations")
    public Set<LocationDTO> getTaggedMeasuringInstrumentLocations(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String measuring_instrument_id) {
        log.info("getTaggedMeasuringInstrumentLocations username={} vdmsid={} measuring_instrument_id={}", username, vdmsid, measuring_instrument_id);
        try {
            return locationService.getTaggedMeasuringInstrumentLocations(username, vdmsid, measuring_instrument_id);
        } catch (Exception e) {
            log.error("getTaggedMeasuringInstrumentLocations failed username={} vdmsid={} measuring_instrument_id={}: {}", username, vdmsid, measuring_instrument_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the distinct location types defined for the given VDMS.
     *
     * @return the list of unique location types
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getuniquelocationtypes")
    public List<String> getUniqueLocationTypes(@RequestParam String username, @RequestParam String vdms_id) {
        log.info("getUniqueLocationTypes username={} vdms_id={}", username, vdms_id);
        try {
            return locationService.getUniqueLocationTypes(username, vdms_id);
        } catch (Exception e) {
            log.error("getUniqueLocationTypes failed username={} vdms_id={}: {}", username, vdms_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Applies a bulk tag/update operation to multiple locations on the given floor.
     *
     * @param floor_id                floor the locations belong to
     * @param tagDeviceOrLocationDTO  bulk tag/update payload
     * @param httpServletRequest      current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/floor/{floor_id}/multiupdatelocations")
    public void multiUpateLocations(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id, @RequestBody TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest) {
        log.info("multiUpateLocations username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        try {
            locationService.multiUpdateLocations(username, vdms_id, floor_id, tagDeviceOrLocationDTO, httpServletRequest);
        } catch (Exception e) {
            log.error("multiUpateLocations failed username={} vdms_id={} floor_id={}: {}", username, vdms_id, floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Upserts the detailed information of the supplied locations on the given floor.
     *
     * @param floor_id           floor the locations belong to
     * @param locations          location details to create or update
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return the persisted set of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/floor/{floor_id}/upsertlocationsdetails")
    public Set<LocationDTO> upsertlocationsdetails(@RequestParam String username, @RequestParam String vdms_id, @PathVariable String floor_id, @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        log.info("upsertlocationsdetails username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        try {
            return locationService.upsertlocationsdetails(username, vdms_id, floor_id, locations, httpServletRequest);
        } catch (Exception e) {
            log.error("upsertlocationsdetails failed username={} vdms_id={} floor_id={}: {}", username, vdms_id, floor_id, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns a paginated set of all locations for the VDMS matching the search and filter criteria.
     *
     * @param pageno       page number to retrieve (default 1)
     * @param pagesize     number of records per page (default 10)
     * @param searchKey    search term to match (default "null")
     * @param field        optional field name to scope the query
     * @param field_id     optional field id to scope the query
     * @param filterObject filter criteria payload
     * @return the matching page of locations
     */
    @RequestMapping(method = RequestMethod.POST, value = "/getalllocationsbyfilterbypagination")
    public Set<LocationDTO> getAllLocationsByFilterByPagination(@RequestParam String username, @RequestParam String vdms_id,
                                                                @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize,
                                                                @RequestParam(defaultValue = "null") String searchKey, @RequestParam(required = false) String field, @RequestParam(required = false) String field_id,
                                                                @RequestBody JSONObject filterObject) {
        log.info("getAllLocationsByFilterByPagination username={} vdms_id={}", username, vdms_id);
        try {
            return locationService.getAllLocationsByFilterByPagination(username, vdms_id, pageno, pagesize, searchKey, filterObject, field, field_id);
        } catch (Exception e) {
            log.error("getAllLocationsByFilterByPagination failed username={} vdms_id={}: {}", username, vdms_id, e.getMessage(), e);
            throw e;
        }
    }
}
