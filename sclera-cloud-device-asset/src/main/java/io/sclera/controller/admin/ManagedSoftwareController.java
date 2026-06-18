package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.InventoryApplicationDTO;
import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import io.sclera.service.ManagedSoftwareSearchService;
import io.sclera.service.ManagedSoftwareService;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST endpoints for managing managed-software records, their inventory tagging,
 * licensing, risk/compliance, and search.
 * Delegates persistence and business logic to {@link ManagedSoftwareService} and
 * search/filter operations to {@link ManagedSoftwareSearchService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Managed Software", description = "Manage managed-software records, inventory tagging, licensing, risk/compliance and search.")
public class ManagedSoftwareController {

    private static final Logger log = LoggerFactory.getLogger(ManagedSoftwareController.class);

    @Autowired
    ManagedSoftwareService managedSoftwareService;

    @Autowired
    ManagedSoftwareSearchService managedSoftwareSearchService;

    /**
     * Returns a paginated list of managed softwares for the given docker.
     *
     * @param dockername docker scope of the managed softwares
     * @param condition  filter condition (default "all")
     * @param searchKey  search term to match (default "null")
     * @param pageno     page number to retrieve (default 1)
     * @param pagesize   number of records per page (default 10)
     * @return the matching page of managed softwares
     */
    @Operation(summary = "Get managed softwares for a docker",
            description = "Returns a paginated list of managed softwares for the given docker, filtered by the supplied condition and search term.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Managed softwares returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/docker/{dockername}/getallmanagedsoftwares")
    public Page<ManagedSoftwareDTO> getAllManagedSoftwares(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed softwares") @PathVariable String dockername,
            @Parameter(description = "Filter condition") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Search term to match") @RequestParam(defaultValue = "null") String searchKey,
            @Parameter(description = "Page number to retrieve") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getAllManagedSoftwares username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return PageUtils.toPage(managedSoftwareService.getAllManagedSoftwares(username, vdmsid, dockername, condition, searchKey, pageno, pagesize), pageno, pagesize);
    }

    /**
     * Upserts a managed-software record.
     *
     * @param managedSoftwareDTO managed-software payload to create or update
     * @return the persisted managed-software record
     */
    @Operation(summary = "Upsert a managed software",
            description = "Creates or updates a managed-software record.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Managed software upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping(value = "/docker/all/upsertmanagedsoftware")
    public ManagedSoftwareDTO updateManagedSoftware(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        log.info("updateManagedSoftware username={} vdmsid={}", username, vdmsid);
        return managedSoftwareService.updateManagedSoftware(username, vdmsid, managedSoftwareDTO);
    }

    /**
     * Tags inventory details to the given managed software.
     *
     * @param dockername         docker scope of the managed software
     * @param managedSoftwareDTO managed-software payload carrying the inventory details to tag
     * @return the updated managed-software record
     */
    @Operation(summary = "Tag inventory details",
            description = "Tags inventory details to the given managed software.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory details tagged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping(value = "/docker/{dockername}/taginventorydetails")
    public ManagedSoftwareDTO tagInventoryDetails(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed software") @PathVariable String dockername,
            @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        log.info("tagInventoryDetails username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return managedSoftwareService.tagInventoryDetails(username, vdmsid, managedSoftwareDTO);
    }

    /**
     * Untags inventory details from the given managed software.
     *
     * @param dockername         docker scope of the managed software
     * @param managedSoftwareDTO managed-software payload carrying the inventory details to untag
     * @return the updated managed-software record
     */
    @Operation(summary = "Untag inventory details",
            description = "Untags inventory details from the given managed software.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory details untagged"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping(value = "/docker/{dockername}/untaginventorydetails")
    public ManagedSoftwareDTO unTagInventoryDetails(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed software") @PathVariable String dockername,
            @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        log.info("unTagInventoryDetails username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return managedSoftwareService.unTagInventoryDetails(username, vdmsid, managedSoftwareDTO);
    }

    /**
     * Returns the users associated with the given managed software.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software whose users are requested
     * @return the list of managed-software users
     */
    @Operation(summary = "Get managed-software users",
            description = "Returns the users associated with the given managed software.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Managed-software users returned"),
            @ApiResponse(responseCode = "404", description = "Managed software not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/users")
    public List<ManagedSoftwareUsersDTO> getManagedSoftwareUsers(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed software") @PathVariable String dockername,
            @Parameter(description = "Managed software whose users are requested") @PathVariable String managedsoftwareid) {
        log.info("getManagedSoftwareUsers username={} vdmsid={} dockername={} managedsoftwareid={}", username, vdmsid, dockername, managedsoftwareid);
        return managedSoftwareService.getManagedSoftwareUsers(username, vdmsid, dockername, managedsoftwareid);
    }

    /**
     * Returns the license information for the given managed software and application.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software whose license is requested
     * @param applicationId     application to scope the license lookup
     * @return a map of license metrics to their counts
     */
    @Operation(summary = "Get managed-software license",
            description = "Returns the license information for the given managed software and application.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "License information returned"),
            @ApiResponse(responseCode = "404", description = "Managed software not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/license")
    public Map<String, Integer> getManagedSoftwareLicense(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed software") @PathVariable String dockername,
            @Parameter(description = "Managed software whose license is requested") @PathVariable String managedsoftwareid,
            @Parameter(description = "Application to scope the license lookup") @RequestParam String applicationId) {
        log.info("getManagedSoftwareLicense username={} vdmsid={} dockername={} managedsoftwareid={} applicationId={}", username, vdmsid, dockername, managedsoftwareid, applicationId);
        return managedSoftwareService.getManagedSoftwareLicense(username, vdmsid, dockername, managedsoftwareid, applicationId);
    }

    /**
     * Returns count metrics for managed softwares under the given docker.
     *
     * @param dockername docker scope of the managed softwares
     * @return a map of count categories to their values
     */
    @Operation(summary = "Get managed-software counts",
            description = "Returns count metrics for managed softwares under the given docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Counts returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/docker/{dockername}/getmanagedsoftwarecount")
    public Map<String, Integer> getManagedSoftwareCount(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed softwares") @PathVariable String dockername) {
        log.info("getManagedSoftwareCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return managedSoftwareService.getManagedSoftwareCount(username, vdmsid, dockername);
    }

    /**
     * Returns the risk and compliance records for the given managed software.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software whose risk/compliance records are requested
     * @return the list of risk and compliance records
     */
    @Operation(summary = "Get risk and compliance records",
            description = "Returns the risk and compliance records for the given managed software.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Risk and compliance records returned"),
            @ApiResponse(responseCode = "404", description = "Managed software not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance")
    public List<Map<String, String>> getManagedSoftwareRiskAndCompliances(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed software") @PathVariable String dockername,
            @Parameter(description = "Managed software whose risk/compliance records are requested") @PathVariable String managedsoftwareid) {
        log.info("getManagedSoftwareRiskAndCompliances username={} vdmsid={} dockername={} managedsoftwareid={}", username, vdmsid, dockername, managedsoftwareid);
        return managedSoftwareService.getAllRiskAndCompliances(username, vdmsid, dockername, managedsoftwareid);
    }

    /**
     * Applies a risk/compliance action to the given managed software.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software the action applies to
     * @param data              action payload describing the risk/compliance change
     */
    @Operation(summary = "Apply a risk/compliance action",
            description = "Applies a risk/compliance action to the given managed software.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Action applied"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PutMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance/action")
    public void riskAndComplianceAction(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed software") @PathVariable String dockername,
            @Parameter(description = "Managed software the action applies to") @PathVariable String managedsoftwareid,
            @RequestBody JSONObject data) {
        log.info("riskAndComplianceAction username={} vdmsid={} dockername={} managedsoftwareid={}", username, vdmsid, dockername, managedsoftwareid);
        managedSoftwareService.riskAndComplianceAction(username, vdmsid, dockername, managedsoftwareid, data);
    }

    /**
     * Returns a paginated set of managed softwares matching the search, sort, and filter criteria.
     *
     * @param dockername                  docker scope of the managed softwares
     * @param condition                   filter condition (default "all")
     * @param pageno                      page number to retrieve (default 1)
     * @param pagesize                    number of records per page (default 10)
     * @param search_sort_filter_details  search, sort, and filter criteria payload
     * @return the matching page of managed softwares
     */
    @Operation(summary = "Search, sort and filter managed softwares",
            description = "Returns a paginated set of managed softwares matching the supplied search, sort and filter criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Managed softwares returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping(value = "/docker/{dockername}/searchsortfiltermanagedsoftware")
    public Page<ManagedSoftwareDTO> searchSortFilterManagedSoftware(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed softwares") @PathVariable String dockername,
            @Parameter(description = "Filter condition") @RequestParam(defaultValue = "all") String condition,
            @Parameter(description = "Page number to retrieve") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Number of records per page") @RequestParam(defaultValue = "10") Integer pagesize,
            @RequestBody JSONObject search_sort_filter_details) {
        log.info("searchSortFilterManagedSoftware username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return PageUtils.toPage(managedSoftwareSearchService.searchSortFilterManagedSoftware(username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details), pageno, pagesize);
    }

    /**
     * Returns the count of managed softwares matching the search, sort, and filter criteria.
     *
     * @param dockername                  docker scope of the managed softwares
     * @param condition                   filter condition (default "all")
     * @param search_sort_filter_details  search, sort, and filter criteria payload
     * @return the matching managed-software count as a string
     */
    @Operation(summary = "Count search/sort/filter managed softwares",
            description = "Returns the count of managed softwares matching the supplied search, sort and filter criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Count returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping(value = "/docker/{dockername}/searchsortfiltermanagedsoftwarecount")
    public String searchSortFilterManagedSoftwareCount(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed softwares") @PathVariable String dockername,
            @Parameter(description = "Filter condition") @RequestParam(defaultValue = "all") String condition,
            @RequestBody JSONObject search_sort_filter_details) {
        log.info("searchSortFilterManagedSoftwareCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return managedSoftwareSearchService.searchSortFilterManagedSoftwareCount(username, vdmsid, dockername, condition, search_sort_filter_details);
    }

    /**
     * Returns the list of available managed-software fields for the given VDMS.
     *
     * @return the managed-software fields list as a string
     */
    @Operation(summary = "Get managed-software fields list",
            description = "Returns the list of available managed-software fields for the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fields list returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/getmanagedsoftwarefieldslist")
    public String getManagedSoftwareFieldsList(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid) {
        log.info("getManagedSoftwareFieldsList username={} vdmsid={}", username, vdmsid);
        return managedSoftwareService.getManagedSoftwareFieldsList(username, vdmsid);
    }

    /**
     * Returns the distinct managed-software users for the given VDMS.
     *
     * @return the list of managed-software users
     */
    @Operation(summary = "Get managed-software users list",
            description = "Returns the distinct managed-software users for the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users list returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/getmanagedsoftwareuserslist")
    public List<String> getManagedSoftwareUsersList(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid) {
        log.info("getManagedSoftwareUsersList username={} vdmsid={}", username, vdmsid);
        return managedSoftwareService.getManagedSoftwareUsersList(username, vdmsid);
    }

    /**
     * Returns the distinct managed-software OS types for the given VDMS.
     *
     * @return the list of managed-software OS types
     */
    @Operation(summary = "Get managed-software OS types list",
            description = "Returns the distinct managed-software OS types for the given VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OS types list returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/getmanagedsoftwareostypeslist")
    public List<String> getManagedSoftwareOSTypesList(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid) {
        log.info("getManagedSoftwareOSTypesList username={} vdmsid={}", username, vdmsid);
        return managedSoftwareService.getManagedSoftwareOSTypesList(username, vdmsid);
    }

    /**
     * Deletes the specified managed software.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software to delete
     */
    @Operation(summary = "Delete a managed software",
            description = "Deletes the specified managed software.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Managed software deleted"),
            @ApiResponse(responseCode = "404", description = "Managed software not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/deletemanagedsoftware")
    public void deleteManagedSoftware(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the managed software") @PathVariable String dockername,
            @Parameter(description = "Managed software to delete") @PathVariable String managedsoftwareid) {
        log.info("deleteManagedSoftware username={} vdmsid={} dockername={} managedsoftwareid={}", username, vdmsid, dockername, managedsoftwareid);
        managedSoftwareService.deleteManagedSoftware(username, vdmsid, dockername, managedsoftwareid);
    }

    /**
     * Returns the inventory applications available under the given docker.
     *
     * @param dockername docker scope of the inventory applications
     * @return the list of inventory applications
     */
    @Operation(summary = "Get inventory applications",
            description = "Returns the inventory applications available under the given docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventory applications returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/docker/{dockername}/getinventoryapplications")
    public List<InventoryApplicationDTO> getInventoryApplications(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker scope of the inventory applications") @PathVariable String dockername) {
        log.info("getInventoryApplications username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return managedSoftwareService.getInventoryApplications(username, vdmsid, dockername);
    }
}
