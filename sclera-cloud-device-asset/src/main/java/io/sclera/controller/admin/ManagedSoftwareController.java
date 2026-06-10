package io.sclera.controller.admin;

import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.InventoryApplicationDTO;
import io.sclera.dto.ManagedSoftwareDTO;
import io.sclera.dto.ManagedSoftwareUsersDTO;
import io.sclera.service.ManagedSoftwareSearchService;
import io.sclera.service.ManagedSoftwareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
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
    @GetMapping(value = "/docker/{dockername}/getallmanagedsoftwares")
    public List<ManagedSoftwareDTO> getAllManagedSoftwares(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "null") String searchKey,
                                                           @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        log.info("getAllManagedSoftwares username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return managedSoftwareService.getAllManagedSoftwares(username, vdmsid, dockername, condition, searchKey, pageno, pagesize);
        } catch (Exception e) {
            log.error("getAllManagedSoftwares failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Upserts a managed-software record.
     *
     * @param managedSoftwareDTO managed-software payload to create or update
     * @return the persisted managed-software record
     */
    @PutMapping(value = "/docker/all/upsertmanagedsoftware")
    public ManagedSoftwareDTO updateManagedSoftware(@RequestParam String username, @RequestParam String vdmsid, @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        log.info("updateManagedSoftware username={} vdmsid={}", username, vdmsid);
        try {
            return managedSoftwareService.updateManagedSoftware(username, vdmsid, managedSoftwareDTO);
        } catch (Exception e) {
            log.error("updateManagedSoftware failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Tags inventory details to the given managed software.
     *
     * @param dockername         docker scope of the managed software
     * @param managedSoftwareDTO managed-software payload carrying the inventory details to tag
     * @return the updated managed-software record
     */
    @PutMapping(value = "/docker/{dockername}/taginventorydetails")
    public ManagedSoftwareDTO tagInventoryDetails(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        log.info("tagInventoryDetails username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return managedSoftwareService.tagInventoryDetails(username, vdmsid, managedSoftwareDTO);
        } catch (Exception e) {
            log.error("tagInventoryDetails failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Untags inventory details from the given managed software.
     *
     * @param dockername         docker scope of the managed software
     * @param managedSoftwareDTO managed-software payload carrying the inventory details to untag
     * @return the updated managed-software record
     */
    @PutMapping(value = "/docker/{dockername}/untaginventorydetails")
    public ManagedSoftwareDTO unTagInventoryDetails(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        log.info("unTagInventoryDetails username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return managedSoftwareService.unTagInventoryDetails(username, vdmsid, managedSoftwareDTO);
        } catch (Exception e) {
            log.error("unTagInventoryDetails failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the users associated with the given managed software.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software whose users are requested
     * @return the list of managed-software users
     */
    @GetMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/users")
    public List<ManagedSoftwareUsersDTO> getManagedSoftwareUsers(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid) {
        log.info("getManagedSoftwareUsers username={} vdmsid={} dockername={} managedsoftwareid={}", username, vdmsid, dockername, managedsoftwareid);
        try {
            return managedSoftwareService.getManagedSoftwareUsers(username, vdmsid, dockername, managedsoftwareid);
        } catch (Exception e) {
            log.error("getManagedSoftwareUsers failed username={} vdmsid={} dockername={} managedsoftwareid={}: {}", username, vdmsid, dockername, managedsoftwareid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the license information for the given managed software and application.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software whose license is requested
     * @param applicationId     application to scope the license lookup
     * @return a map of license metrics to their counts
     */
    @GetMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/license")
    public Map<String, Integer> getManagedSoftwareLicense(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid, @RequestParam String applicationId) {
        log.info("getManagedSoftwareLicense username={} vdmsid={} dockername={} managedsoftwareid={} applicationId={}", username, vdmsid, dockername, managedsoftwareid, applicationId);
        try {
            return managedSoftwareService.getManagedSoftwareLicense(username, vdmsid, dockername, managedsoftwareid, applicationId);
        } catch (Exception e) {
            log.error("getManagedSoftwareLicense failed username={} vdmsid={} dockername={} managedsoftwareid={} applicationId={}: {}", username, vdmsid, dockername, managedsoftwareid, applicationId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns count metrics for managed softwares under the given docker.
     *
     * @param dockername docker scope of the managed softwares
     * @return a map of count categories to their values
     */
    @GetMapping(value = "/docker/{dockername}/getmanagedsoftwarecount")
    public Map<String, Integer> getManagedSoftwareCount(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername) {
        log.info("getManagedSoftwareCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return managedSoftwareService.getManagedSoftwareCount(username, vdmsid, dockername);
        } catch (Exception e) {
            log.error("getManagedSoftwareCount failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the risk and compliance records for the given managed software.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software whose risk/compliance records are requested
     * @return the list of risk and compliance records
     */
    @GetMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance")
    public List<Map<String, String>> getManagedSoftwareRiskAndCompliances(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid) {
        log.info("getManagedSoftwareRiskAndCompliances username={} vdmsid={} dockername={} managedsoftwareid={}", username, vdmsid, dockername, managedsoftwareid);
        try {
            return managedSoftwareService.getAllRiskAndCompliances(username, vdmsid, dockername, managedsoftwareid);
        } catch (Exception e) {
            log.error("getManagedSoftwareRiskAndCompliances failed username={} vdmsid={} dockername={} managedsoftwareid={}: {}", username, vdmsid, dockername, managedsoftwareid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Applies a risk/compliance action to the given managed software.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software the action applies to
     * @param data              action payload describing the risk/compliance change
     */
    @PutMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance/action")
    public void riskAndComplianceAction(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid, @RequestBody JSONObject data) {
        log.info("riskAndComplianceAction username={} vdmsid={} dockername={} managedsoftwareid={}", username, vdmsid, dockername, managedsoftwareid);
        try {
            managedSoftwareService.riskAndComplianceAction(username, vdmsid, dockername, managedsoftwareid, data);
        } catch (Exception e) {
            log.error("riskAndComplianceAction failed username={} vdmsid={} dockername={} managedsoftwareid={}: {}", username, vdmsid, dockername, managedsoftwareid, e.getMessage(), e);
            throw e;
        }
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
    @PostMapping(value = "/docker/{dockername}/searchsortfiltermanagedsoftware")
    public Set<ManagedSoftwareDTO> searchSortFilterManagedSoftware(@RequestParam String username,
                                                                   @RequestParam String vdmsid,
                                                                   @PathVariable String dockername,
                                                                   @RequestParam(defaultValue = "all") String condition,
                                                                   @RequestParam(defaultValue = "1") Integer pageno,
                                                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                                                   @RequestBody JSONObject search_sort_filter_details) {
        log.info("searchSortFilterManagedSoftware username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return managedSoftwareSearchService.searchSortFilterManagedSoftware(username, vdmsid, dockername, condition, pageno, pagesize, search_sort_filter_details);
        } catch (Exception e) {
            log.error("searchSortFilterManagedSoftware failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the count of managed softwares matching the search, sort, and filter criteria.
     *
     * @param dockername                  docker scope of the managed softwares
     * @param condition                   filter condition (default "all")
     * @param search_sort_filter_details  search, sort, and filter criteria payload
     * @return the matching managed-software count as a string
     */
    @PostMapping(value = "/docker/{dockername}/searchsortfiltermanagedsoftwarecount")
    public String searchSortFilterManagedSoftwareCount(@RequestParam String username,
                                                       @RequestParam String vdmsid,
                                                       @PathVariable String dockername,
                                                       @RequestParam(defaultValue = "all") String condition,
                                                       @RequestBody JSONObject search_sort_filter_details) {
        log.info("searchSortFilterManagedSoftwareCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return managedSoftwareSearchService.searchSortFilterManagedSoftwareCount(username, vdmsid, dockername, condition, search_sort_filter_details);
        } catch (Exception e) {
            log.error("searchSortFilterManagedSoftwareCount failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the list of available managed-software fields for the given VDMS.
     *
     * @return the managed-software fields list as a string
     */
    @GetMapping(value = "/getmanagedsoftwarefieldslist")
    public String getManagedSoftwareFieldsList(@RequestParam String username, @RequestParam String vdmsid) {
        log.info("getManagedSoftwareFieldsList username={} vdmsid={}", username, vdmsid);
        try {
            return managedSoftwareService.getManagedSoftwareFieldsList(username, vdmsid);
        } catch (Exception e) {
            log.error("getManagedSoftwareFieldsList failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the distinct managed-software users for the given VDMS.
     *
     * @return the list of managed-software users
     */
    @GetMapping(value = "/getmanagedsoftwareuserslist")
    public List<String> getManagedSoftwareUsersList(@RequestParam String username, @RequestParam String vdmsid) {
        log.info("getManagedSoftwareUsersList username={} vdmsid={}", username, vdmsid);
        try {
            return managedSoftwareService.getManagedSoftwareUsersList(username, vdmsid);
        } catch (Exception e) {
            log.error("getManagedSoftwareUsersList failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the distinct managed-software OS types for the given VDMS.
     *
     * @return the list of managed-software OS types
     */
    @GetMapping(value = "/getmanagedsoftwareostypeslist")
    public List<String> getManagedSoftwareOSTypesList(@RequestParam String username, @RequestParam String vdmsid) {
        log.info("getManagedSoftwareOSTypesList username={} vdmsid={}", username, vdmsid);
        try {
            return managedSoftwareService.getManagedSoftwareOSTypesList(username, vdmsid);
        } catch (Exception e) {
            log.error("getManagedSoftwareOSTypesList failed username={} vdmsid={}: {}", username, vdmsid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Deletes the specified managed software.
     *
     * @param dockername        docker scope of the managed software
     * @param managedsoftwareid managed software to delete
     */
    @DeleteMapping(value = "/docker/{dockername}/managedsoftware/{managedsoftwareid}/deletemanagedsoftware")
    public void deleteManagedSoftware(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid) {
        log.info("deleteManagedSoftware username={} vdmsid={} dockername={} managedsoftwareid={}", username, vdmsid, dockername, managedsoftwareid);
        try {
            managedSoftwareService.deleteManagedSoftware(username, vdmsid, dockername, managedsoftwareid);
        } catch (Exception e) {
            log.error("deleteManagedSoftware failed username={} vdmsid={} dockername={} managedsoftwareid={}: {}", username, vdmsid, dockername, managedsoftwareid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the inventory applications available under the given docker.
     *
     * @param dockername docker scope of the inventory applications
     * @return the list of inventory applications
     */
    @GetMapping(value = "/docker/{dockername}/getinventoryapplications")
    public List<InventoryApplicationDTO> getInventoryApplications(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername) {
        log.info("getInventoryApplications username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return managedSoftwareService.getInventoryApplications(username, vdmsid, dockername);
        } catch (Exception e) {
            log.error("getInventoryApplications failed username={} vdmsid={} dockername={}: {}", username, vdmsid, dockername, e.getMessage(), e);
            throw e;
        }
    }
}
