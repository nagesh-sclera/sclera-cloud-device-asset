package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.InventoryApplicationDTO;
import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import io.sclera.service.ManagedSoftwareSearchService;
import io.sclera.service.ManagedSoftwareService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ManagedSoftwareController {

    @Autowired
    ManagedSoftwareService managedSoftwareService;

    @Autowired
    ManagedSoftwareSearchService managedSoftwareSearchService;

    /**
     * Returns a paginated list of managed softwares for the given docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker scope of the managed softwares
     * @param condition  filter condition (default "all")
     * @param searchKey  search term to match (default "null")
     * @param pageno     page number to retrieve (default 1)
     * @param pagesize   number of records per page (default 10)
     * @return the matching page of managed softwares
     */
    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getallmanagedsoftwares")
    public List<ManagedSoftwareDTO> getAllManagedSoftwares(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "null") String searchKey,
                                                           @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return managedSoftwareService.getAllManagedSoftwares(username, vdmsid, dockername, condition, searchKey, pageno, pagesize);
    }

    /**
     * Upserts a managed-software record.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param managedSoftwareDTO managed-software payload to create or update
     * @return the persisted managed-software record
     */
    @PutMapping(value = "/user/{username}/vdms/{vdmsid}/docker/all/upsertmanagedsoftware")
    public ManagedSoftwareDTO updateManagedSoftware(@PathVariable String username, @PathVariable String vdmsid, @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        return managedSoftwareService.updateManagedSoftware(username, vdmsid, managedSoftwareDTO);
    }

    /**
     * Tags inventory details to the given managed software.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker scope of the managed software
     * @param managedSoftwareDTO managed-software payload carrying the inventory details to tag
     * @return the updated managed-software record
     */
    @PutMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/taginventorydetails")
    public ManagedSoftwareDTO tagInventoryDetails(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        return managedSoftwareService.tagInventoryDetails(username, vdmsid, managedSoftwareDTO);
    }

    /**
     * Untags inventory details from the given managed software.
     *
     * @param username           owning user
     * @param vdmsid             owning VDMS id
     * @param dockername         docker scope of the managed software
     * @param managedSoftwareDTO managed-software payload carrying the inventory details to untag
     * @return the updated managed-software record
     */
    @PutMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/untaginventorydetails")
    public ManagedSoftwareDTO unTagInventoryDetails(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        return managedSoftwareService.unTagInventoryDetails(username, vdmsid, managedSoftwareDTO);
    }

    /**
     * Returns the users associated with the given managed software.
     *
     * @param username          owning user
     * @param vdmsid            owning VDMS id
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software whose users are requested
     * @return the list of managed-software users
     */
    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/users")
    public List<ManagedSoftwareUsersDTO> getManagedSoftwareUsers(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid) {
        return managedSoftwareService.getManagedSoftwareUsers(username, vdmsid, dockername, managedsoftwareid);
    }

    /**
     * Returns the license information for the given managed software and application.
     *
     * @param username          owning user
     * @param vdmsid            owning VDMS id
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software whose license is requested
     * @param applicationId     application to scope the license lookup
     * @return a map of license metrics to their counts
     */
    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/license")
    public Map<String, Integer> getManagedSoftwareLicense(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid, @RequestParam String applicationId) {
        return managedSoftwareService.getManagedSoftwareLicense(username, vdmsid, dockername, managedsoftwareid, applicationId);
    }

    /**
     * Returns count metrics for managed softwares under the given docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker scope of the managed softwares
     * @return a map of count categories to their values
     */
    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getmanagedsoftwarecount")
    public Map<String, Integer> getManagedSoftwareCount(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
        return managedSoftwareService.getManagedSoftwareCount(username, vdmsid, dockername);
    }

    /**
     * Returns the risk and compliance records for the given managed software.
     *
     * @param username          owning user
     * @param vdmsid            owning VDMS id
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software whose risk/compliance records are requested
     * @return the list of risk and compliance records
     */
    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance")
    public List<Map<String, String>> getManagedSoftwareRiskAndCompliances(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid) {
        return managedSoftwareService.getAllRiskAndCompliances(username, vdmsid, dockername, managedsoftwareid);
    }

    /**
     * Applies a risk/compliance action to the given managed software.
     *
     * @param username          owning user
     * @param vdmsid            owning VDMS id
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software the action applies to
     * @param data              action payload describing the risk/compliance change
     */
    @PutMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance/action")
    public void riskAndComplianceAction(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid, @RequestBody JSONObject data) {
        managedSoftwareService.riskAndComplianceAction(username, vdmsid, dockername, managedsoftwareid, data);
    }

    /**
     * Returns a paginated set of managed softwares matching the search, sort, and filter criteria.
     *
     * @param username                    owning user
     * @param vdmsid                      owning VDMS id
     * @param dockername                  docker scope of the managed softwares
     * @param condition                   filter condition (default "all")
     * @param pageno                      page number to retrieve (default 1)
     * @param pagesize                    number of records per page (default 10)
     * @param search_sort_filter_details  search, sort, and filter criteria payload
     * @return the matching page of managed softwares
     */
    @PostMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/searchsortfiltermanagedsoftware")
    public Set<ManagedSoftwareDTO> searchSortFilterManagedSoftware(@PathVariable String username,
                                                                   @PathVariable String vdmsid,
                                                                   @PathVariable String dockername,
                                                                   @RequestParam(defaultValue = "all") String condition,
                                                                   @RequestParam(defaultValue = "1") Integer pageno,
                                                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                                                   @RequestBody JSONObject search_sort_filter_details) {
        return managedSoftwareSearchService.searchSortFilterManagedSoftware(username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details);
    }

    /**
     * Returns the count of managed softwares matching the search, sort, and filter criteria.
     *
     * @param username                    owning user
     * @param vdmsid                      owning VDMS id
     * @param dockername                  docker scope of the managed softwares
     * @param condition                   filter condition (default "all")
     * @param search_sort_filter_details  search, sort, and filter criteria payload
     * @return the matching managed-software count as a string
     */
    @PostMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/searchsortfiltermanagedsoftwarecount")
    public String searchSortFilterManagedSoftwareCount(@PathVariable String username,
                                                       @PathVariable String vdmsid,
                                                       @PathVariable String dockername,
                                                       @RequestParam(defaultValue = "all") String condition,
                                                       @RequestBody JSONObject search_sort_filter_details) {
        return managedSoftwareSearchService.searchSortFilterManagedSoftwareCount(username, vdmsid, dockername, condition, search_sort_filter_details);
    }

    /**
     * Returns the list of available managed-software fields for the given VDMS.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @return the managed-software fields list as a string
     */
    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/getmanagedsoftwarefieldslist")
    public String getManagedSoftwareFieldsList(@PathVariable String username, @PathVariable String vdmsid) {
        return managedSoftwareService.getManagedSoftwareFieldsList(username, vdmsid);
    }

    /**
     * Returns the distinct managed-software users for the given VDMS.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @return the list of managed-software users
     */
    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/getmanagedsoftwareuserslist")
    public List<String> getManagedSoftwareUsersList(@PathVariable String username, @PathVariable String vdmsid) {
        return managedSoftwareService.getManagedSoftwareUsersList(username, vdmsid);
    }

    /**
     * Returns the distinct managed-software OS types for the given VDMS.
     *
     * @param username owning user
     * @param vdmsid   owning VDMS id
     * @return the list of managed-software OS types
     */
    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/getmanagedsoftwareostypeslist")
    public List<String> getManagedSoftwareOSTypesList(@PathVariable String username, @PathVariable String vdmsid) {
        return managedSoftwareService.getManagedSoftwareOSTypesList(username, vdmsid);
    }

    /**
     * Deletes the specified managed software.
     *
     * @param username          owning user
     * @param vdmsid            owning VDMS id
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software to delete
     */
    @DeleteMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/deletemanagedsoftware")
    public void deleteManagedSoftware(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid) {
        managedSoftwareService.deleteManagedSoftware(username, vdmsid, dockername, managedsoftwareid);
    }

    /**
     * Returns the inventory applications available under the given docker.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param dockername docker scope of the inventory applications
     * @return the list of inventory applications
     */
    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getinventoryapplications")
    public List<InventoryApplicationDTO> getInventoryApplications(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
        return managedSoftwareService.getInventoryApplications(username, vdmsid, dockername);
    }
}
