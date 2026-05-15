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

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ManagedSoftwareController {

    @Autowired
    ManagedSoftwareService managedSoftwareService;

    @Autowired
    ManagedSoftwareSearchService managedSoftwareSearchService;

    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getallmanagedsoftwares")
    public List<ManagedSoftwareDTO> getAllManagedSoftwares(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestParam(defaultValue = "all") String condition, @RequestParam(defaultValue = "null") String searchKey,
                                                           @RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize) {
        return managedSoftwareService.getAllManagedSoftwares(username, vdmsid, dockername, condition, searchKey, pageno, pagesize);
    }

    @PutMapping(value = "/user/{username}/vdms/{vdmsid}/docker/all/upsertmanagedsoftware")
    public ManagedSoftwareDTO updateManagedSoftware(@PathVariable String username, @PathVariable String vdmsid, @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        return managedSoftwareService.updateManagedSoftware(username, vdmsid, managedSoftwareDTO);
    }

    @PutMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/taginventorydetails")
    public ManagedSoftwareDTO tagInventoryDetails(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        return managedSoftwareService.tagInventoryDetails(username, vdmsid, managedSoftwareDTO);
    }

    @PutMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/untaginventorydetails")
    public ManagedSoftwareDTO unTagInventoryDetails(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @RequestBody ManagedSoftwareDTO managedSoftwareDTO) {
        return managedSoftwareService.unTagInventoryDetails(username, vdmsid, managedSoftwareDTO);
    }

    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/users")
    public List<ManagedSoftwareUsersDTO> getManagedSoftwareUsers(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid) {
        return managedSoftwareService.getManagedSoftwareUsers(username, vdmsid, dockername, managedsoftwareid);
    }

    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/license")
    public Map<String, Integer> getManagedSoftwareLicense(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid, @RequestParam String applicationId) {
        return managedSoftwareService.getManagedSoftwareLicense(username, vdmsid, dockername, managedsoftwareid, applicationId);
    }

    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getmanagedsoftwarecount")
    public Map<String, Integer> getManagedSoftwareCount(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
        return managedSoftwareService.getManagedSoftwareCount(username, vdmsid, dockername);
    }

    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance")
    public List<Map<String, String>> getManagedSoftwareRiskAndCompliances(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid) {
        return managedSoftwareService.getAllRiskAndCompliances(username, vdmsid, dockername, managedsoftwareid);
    }

    @PutMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/riskandcompliance/action")
    public void riskAndComplianceAction(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid, @RequestBody JSONObject data) {
        managedSoftwareService.riskAndComplianceAction(username, vdmsid, dockername, managedsoftwareid, data);
    }

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

    @PostMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/searchsortfiltermanagedsoftwarecount")
    public String searchSortFilterManagedSoftwareCount(@PathVariable String username,
                                                       @PathVariable String vdmsid,
                                                       @PathVariable String dockername,
                                                       @RequestParam(defaultValue = "all") String condition,
                                                       @RequestBody JSONObject search_sort_filter_details) {
        return managedSoftwareSearchService.searchSortFilterManagedSoftwareCount(username, vdmsid, dockername, condition, search_sort_filter_details);
    }

    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/getmanagedsoftwarefieldslist")
    public String getManagedSoftwareFieldsList(@PathVariable String username, @PathVariable String vdmsid) {
        return managedSoftwareService.getManagedSoftwareFieldsList(username, vdmsid);
    }

    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/getmanagedsoftwareuserslist")
    public List<String> getManagedSoftwareUsersList(@PathVariable String username, @PathVariable String vdmsid) {
        return managedSoftwareService.getManagedSoftwareUsersList(username, vdmsid);
    }

    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/getmanagedsoftwareostypeslist")
    public List<String> getManagedSoftwareOSTypesList(@PathVariable String username, @PathVariable String vdmsid) {
        return managedSoftwareService.getManagedSoftwareOSTypesList(username, vdmsid);
    }

    @DeleteMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/managedsoftware/{managedsoftwareid}/deletemanagedsoftware")
    public void deleteManagedSoftware(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername, @PathVariable String managedsoftwareid) {
        managedSoftwareService.deleteManagedSoftware(username, vdmsid, dockername, managedsoftwareid);
    }

    @GetMapping(value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getinventoryapplications")
    public List<InventoryApplicationDTO> getInventoryApplications(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername) {
        return managedSoftwareService.getInventoryApplications(username, vdmsid, dockername);
    }
}
