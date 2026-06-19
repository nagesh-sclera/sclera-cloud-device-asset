package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.LocationDTO;
import io.sclera.dto.TagDeviceOrLocationDTO;
import io.sclera.service.LocationService;
import io.sclera.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
@Tag(name = "Locations", description = "Create, read, update, delete, tag and search locations within floors and VDMS scopes.")
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
    @Operation(summary = "Upsert locations for a floor",
            description = "Creates or updates the supplied locations under the given floor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/floor/{floor_id}/upsertlocations")
    public Set<LocationDTO> upsertLocationsByFloorId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor the locations belong to") @PathVariable String floor_id,
            @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        log.info("upsertLocationsByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        return locationService.upsertLocationsByFloorId(username, vdms_id, floor_id, locations, httpServletRequest);
    }

    /**
     * Deletes the locations identified by the supplied ids.
     *
     * @param location_ids ids of the locations to delete
     */
    @Operation(summary = "Delete locations by ids",
            description = "Deletes the locations identified by the supplied ids.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/building/floor/deletelocations")
    public void deleteLocationsByIds(
            @Parameter(description = "Owning user email") @RequestParam String email,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @RequestBody Set<String> location_ids) {
        log.info("deleteLocationsByIds email={} vdms_id={}", email, vdms_id);
        locationService.deleteLocationsByIds(email, vdms_id, location_ids, false);
    }

    /**
     * Returns all locations for the given VDMS.
     *
     * @return the set of locations under the VDMS
     */
    @Operation(summary = "Get locations for a VDMS",
            description = "Returns all locations for the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getlocations")
    public Set<LocationDTO> getLocationsByVdmsId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id) {
        log.info("getLocationsByVdmsId username={} vdms_id={}", username, vdms_id);
        return locationService.getLocationsByVdmsId(username, vdms_id);
    }

    /**
     * Returns all locations under the given floor.
     *
     * @param floor_id floor whose locations are requested
     * @return the set of locations on the floor
     */
    @Operation(summary = "Get locations for a floor",
            description = "Returns all locations under the given floor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/floor/{floor_id}/getlocationsbyfloorid")
    public Set<LocationDTO> getLocationsByFloor(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor whose locations are requested") @PathVariable String floor_id) {
        log.info("getLocationsByFloor username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        return locationService.getLocationsByFloor(username, vdms_id, floor_id);
    }

    /**
     * Updates the details of the specified location.
     *
     * @param floor_id           floor the location belongs to
     * @param location_id        location to update
     * @param locations          updated location details
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Update location details",
            description = "Updates the details of the specified location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location details updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/floor/{floor_id}/location/{location_id}/updatelocationdetails")
    public void updateLocationsDetailsByLocationId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor the location belongs to") @PathVariable String floor_id,
            @Parameter(description = "Location to update") @PathVariable String location_id,
            @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        log.info("updateLocationsDetailsByLocationId username={} vdms_id={} floor_id={} location_id={}", username, vdms_id, floor_id, location_id);
        locationService.updateLocationsDetailsByLocationId(username, vdms_id, floor_id, location_id, locations, httpServletRequest);
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
    @Operation(summary = "Get paginated locations for a floor",
            description = "Returns a paginated set of locations for the given floor matching the supplied search and filter criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/floor/{floor_id}/getlocationsbyflooridpagination")
    public Page<LocationDTO> getLocationsByFloorByPagination(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor whose locations are requested") @PathVariable String floor_id,
            @Parameter(description = "Page number to retrieve") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Search term to match") @RequestParam(defaultValue = "null") String searchKey,
            @Parameter(description = "Optional field name to scope the query") @RequestParam(required = false) String field,
            @Parameter(description = "Optional field id to scope the query") @RequestParam(required = false) String field_id,
            @RequestBody JSONObject filterObject) {
        log.info("getLocationsByFloorByPagination username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        return PageUtils.toPage(locationService.getLocationsByFloorByPagination(username, vdms_id, floor_id, pageno, pagesize, searchKey, filterObject, field, field_id), pageno, pagesize);
    }

    /**
     * Returns the count of locations on the given floor matching the search term.
     *
     * @param floor_id  floor whose locations are counted
     * @param searchkey search term to match (default "null")
     * @return the location count as a string
     */
    @Operation(summary = "Count locations for a floor",
            description = "Returns the count of locations on the given floor matching the supplied search term.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/floor/{floor_id}/getlocationscountbyfloorid")
    public String getLocationsCountByFloorId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor whose locations are counted") @PathVariable String floor_id,
            @Parameter(description = "Search term to match") @RequestParam(defaultValue = "null") String searchkey) {
        log.info("getLocationsCountByFloorId username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        return locationService.getLocationsCountByFloorId(username, vdms_id, floor_id, searchkey);
    }

    /**
     * Returns the details of the specified location.
     *
     * @param location_id location whose details are requested
     * @return the location details
     */
    @Operation(summary = "Get location details by id",
            description = "Returns the details of the specified location.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location details returned"),
            @ApiResponse(responseCode = "404", description = "Location not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/location/{location_id}/getlocationdetailsbylocationid")
    public LocationDTO getLocationDetailsByLocationId(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Location whose details are requested") @PathVariable String location_id) {
        log.info("getLocationDetailsByLocationId username={} vdms_id={} location_id={}", username, vdms_id, location_id);
        return locationService.getLocationDetailsByLocationId(username, vdms_id, location_id);
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
    @Operation(summary = "Get paginated locations for a group",
            description = "Returns a paginated set of all locations for the given group matching the supplied search criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/group/{group}/getalllocationspagination")
    public Page<LocationDTO> getAllLocationsPagination(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Group to scope the query") @PathVariable String group,
            @Parameter(description = "Search term to match") @RequestParam(defaultValue = "null") String searchkey,
            @Parameter(description = "Page number to retrieve") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @RequestBody JSONObject filterObject) {
        log.info("getAllLocationsPagination username={} vdmsid={} group={}", username, vdmsid, group);
        return PageUtils.toPage(locationService.getAllLocationsPagination(username, vdmsid, group, searchkey, pageno, pagesize, filterObject), pageno, pagesize);
    }

    /**
     * Returns the count of locations matching the search, sort, and filter criteria.
     *
     * @param searchKey    search term to match (default "null")
     * @param filterObject filter criteria payload
     * @return the matching location count
     */
    @Operation(summary = "Count search/sort/filter locations",
            description = "Returns the count of locations matching the supplied search, sort and filter criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/searchSortFilterLocationsCount")
    public int searchSortFilterLocationsCount(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Search term to match") @RequestParam(defaultValue = "null") String searchKey,
            @RequestBody JSONObject filterObject) {
        log.info("searchSortFilterLocationsCount username={} vdms_id={}", username, vdms_id);
        return locationService.searchSortFilterLocationsCount(username, vdms_id, searchKey, filterObject);
    }

    /**
     * Returns the locations tagged to the given measuring instrument.
     *
     * @param measuring_instrument_id measuring instrument whose tagged locations are requested
     * @return the set of tagged locations
     */
    @Operation(summary = "Get locations tagged to a measuring instrument",
            description = "Returns the locations tagged to the given measuring instrument.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tagged locations returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/measuring_instrument_id/{measuring_instrument_id}/gettaggedmeasuringinstrumentlocations")
    public Set<LocationDTO> getTaggedMeasuringInstrumentLocations(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Measuring instrument whose tagged locations are requested") @PathVariable String measuring_instrument_id) {
        log.info("getTaggedMeasuringInstrumentLocations username={} vdmsid={} measuring_instrument_id={}", username, vdmsid, measuring_instrument_id);
        return locationService.getTaggedMeasuringInstrumentLocations(username, vdmsid, measuring_instrument_id);
    }

    /**
     * Returns the distinct location types defined for the given VDMS.
     *
     * @return the list of unique location types
     */
    @Operation(summary = "Get unique location types",
            description = "Returns the distinct location types defined for the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location types returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getuniquelocationtypes")
    public List<String> getUniqueLocationTypes(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id) {
        log.info("getUniqueLocationTypes username={} vdms_id={}", username, vdms_id);
        return locationService.getUniqueLocationTypes(username, vdms_id);
    }

    /**
     * Applies a bulk tag/update operation to multiple locations on the given floor.
     *
     * @param floor_id                floor the locations belong to
     * @param tagDeviceOrLocationDTO  bulk tag/update payload
     * @param httpServletRequest      current request, used to resolve tenant/VDMS context
     */
    @Operation(summary = "Bulk tag/update locations on a floor",
            description = "Applies a bulk tag/update operation to multiple locations on the given floor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/floor/{floor_id}/multiupdatelocations")
    public void multiUpateLocations(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor the locations belong to") @PathVariable String floor_id,
            @RequestBody TagDeviceOrLocationDTO tagDeviceOrLocationDTO, HttpServletRequest httpServletRequest) {
        log.info("multiUpateLocations username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        locationService.multiUpdateLocations(username, vdms_id, floor_id, tagDeviceOrLocationDTO, httpServletRequest);
    }

    /**
     * Upserts the detailed information of the supplied locations on the given floor.
     *
     * @param floor_id           floor the locations belong to
     * @param locations          location details to create or update
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return the persisted set of locations
     */
    @Operation(summary = "Upsert location details for a floor",
            description = "Creates or updates the detailed information of the supplied locations on the given floor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Location details upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/floor/{floor_id}/upsertlocationsdetails")
    public Set<LocationDTO> upsertlocationsdetails(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Floor the locations belong to") @PathVariable String floor_id,
            @RequestBody Set<LocationDTO> locations, HttpServletRequest httpServletRequest) {
        log.info("upsertlocationsdetails username={} vdms_id={} floor_id={}", username, vdms_id, floor_id);
        return locationService.upsertlocationsdetails(username, vdms_id, floor_id, locations, httpServletRequest);
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
    @Operation(summary = "Get paginated locations for a VDMS by filter",
            description = "Returns a paginated set of all locations for the VDMS matching the supplied search and filter criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Locations returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/getalllocationsbyfilterbypagination")
    public Page<LocationDTO> getAllLocationsByFilterByPagination(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id,
            @Parameter(description = "Page number to retrieve") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Search term to match") @RequestParam(defaultValue = "null") String searchKey,
            @Parameter(description = "Optional field name to scope the query") @RequestParam(required = false) String field,
            @Parameter(description = "Optional field id to scope the query") @RequestParam(required = false) String field_id,
            @RequestBody JSONObject filterObject) {
        log.info("getAllLocationsByFilterByPagination username={} vdms_id={}", username, vdms_id);
        return PageUtils.toPage(locationService.getAllLocationsByFilterByPagination(username, vdms_id, pageno, pagesize, searchKey, filterObject, field, field_id), pageno, pagesize);
    }
}
