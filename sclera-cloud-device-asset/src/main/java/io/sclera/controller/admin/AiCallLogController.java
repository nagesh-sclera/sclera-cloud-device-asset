package io.sclera.controller.admin;

import io.sclera.dto.AiCallLogDTO;
import io.sclera.dto.CallFlowRuleDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.service.AiCallService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST endpoints for AI-assisted call logging, call-flow configuration and call triggering.
 * Delegates all persistence and business logic to {@link AiCallService}.
 */
@RestController
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class AiCallLogController {
    @Autowired
    AiCallService aiCallService;

   /**
    * Creates a call log for the given device and issue type.
    *
    * @param deviceId   device the call log belongs to
    * @param issueType  type of issue the call concerns
    * @return identifier or status of the created call log
    */
   @PostMapping("/deviceId/{deviceId}/createcalllog")
    public String createCallLog(@PathVariable String deviceId, @RequestParam String issueType) {
        return aiCallService.createCallLog(deviceId, issueType);
    }

    /**
     * Returns a paged list of call statuses for the tenant.
     *
     * @param username     owning user
     * @param vdmsid       owning VDMS id
     * @param pageno       page number (default 1)
     * @param pagesize     page size (default 10)
     * @param searchkey    optional search filter (default "null")
     * @param isCompleted  whether to return only completed calls (default false)
     * @return list of call log entries for the requested page
     */
    @GetMapping("/getallcallstatus")
    public List<AiCallLogDTO>  getallcallstatus(@RequestParam String username, @RequestParam String vdmsid,@RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "null") String searchkey, @RequestParam(defaultValue = "false") boolean isCompleted) {
        return aiCallService.getallcallstatus(username, vdmsid, pageno, pagesize, searchkey, isCompleted);
    }

    /**
     * Returns a count of call statuses grouped by status for the tenant.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @return map of status name to count
     */
    @GetMapping("/getcallstatuscount")
    public Map<String, Integer> getCallStatusCount(@RequestParam String username, @RequestParam String vdmsid) {
        return aiCallService.getCallStatusCount(username, vdmsid);
    }

    /**
     * Returns device information loaded from the database.
     *
     * @param deviceId  device to look up
     * @return device details for the given id
     */
    @GetMapping("/getdeviceinfo/{deviceId}")
    public DeviceDTO getDeviceInfo(@PathVariable String deviceId) {
        return aiCallService.getDeviceInfoFromDb(deviceId);
    }

    /**
     * Triggers resolution of an assignee and returns the result.
     *
     * @return identifier or status of the resolved assignee
     */
    @GetMapping("/assign")
    public String triggerGetAssignee() {
        return aiCallService.getAssignee("");
   }

    /**
     * Parses an uploaded JSON file and inserts the contained call response.
     *
     * @param file  multipart file containing the call response JSON payload
     * @return identifier or status of the inserted call response
     */
    @PostMapping("/insertcallresponse")
    public String insertCallResponse(@RequestParam("callinfo") MultipartFile file) {
        try {
            String jsonString = new String(file.getBytes(), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(jsonString); // Use org.json
            return aiCallService.insertCallResponse(json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded JSON file", e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }



    /**
     * Creates or updates a call-flow rule for the tenant.
     *
     * @param username         owning user
     * @param vdmsid           owning VDMS id
     * @param callFlowRuleDTO  call-flow rule payload to upsert
     * @return response entity wrapping the upsert result
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upsertcallflow")
    public ResponseEntity<ResponseDTO> upsertCallFlow(@RequestParam String username, @RequestParam String vdmsid, @RequestBody CallFlowRuleDTO callFlowRuleDTO) {
        System.out.println("Received DTO: " + callFlowRuleDTO);
        return aiCallService.upsertCallFlow(callFlowRuleDTO, username, vdmsid);
    }

    /**
     * Returns the set of docker names available to the tenant, optionally filtered.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param searchkey  optional search filter (default "null")
     * @return set of matching docker names
     */
    @GetMapping("/browsedockers")
    public Set<String> browseDockers(@RequestParam String username, @RequestParam String vdmsid, @RequestParam(defaultValue = "null") String searchkey) {
        return aiCallService.browseDockers(username,vdmsid,searchkey);
    }

    /**
     * Returns a paged list of call-flow devices for a docker, with optional search.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param dockername  docker whose devices are browsed
     * @param pageno      page number (default 1)
     * @param pagesize    page size (default 10)
     * @param searchkey   optional search filter (default "null")
     * @return list of call-flow rules for the matching devices
     */
    @RequestMapping(method = RequestMethod.GET, value = "/docker/{dockername}/browsedevices")
    public List<CallFlowRuleDTO> browseCallFlowDevicesWithSearch(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String dockername,
                                                                    @RequestParam(defaultValue = "1") Integer pageno,
                                                                    @RequestParam(defaultValue = "10") Integer pagesize,
                                                                    @RequestParam(defaultValue = "null") String searchkey) {
        return aiCallService.browseCallFlowDevicesWithSearch(username, vdmsid, dockername, pageno, pagesize, searchkey);
    }

    /**
     * Returns a paged list of call-flow rules for the tenant.
     *
     * @param username   owning user
     * @param vdmsid     owning VDMS id
     * @param pageno     page number (default 1)
     * @param pagesize   page size (default 10)
     * @param searchkey  optional search filter (default "null")
     * @return list of call-flow rules for the requested page
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getcallflow")
    public List<CallFlowRuleDTO> getCallFlow(@RequestParam String username, @RequestParam String vdmsid, @RequestParam(defaultValue = "1") Integer pageno,
                                             @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "null") String searchkey) {
        return aiCallService.getCallFlow(username, vdmsid, pageno, pagesize, searchkey);
    }

    /**
     * Deletes the call-flow rules identified by the given ids.
     *
     * @param username        owning user
     * @param vdmsid          owning VDMS id
     * @param callFlowRuleId  set of call-flow rule ids to delete
     */
    @RequestMapping(method = RequestMethod.DELETE, value = "/configuration/deletecallflowbyid")
    public void deleteCallFlowById(@RequestParam String username, @RequestParam String vdmsid, @RequestBody Set<String> callFlowRuleId) {
        aiCallService.deleteCallFlowById(username, vdmsid, callFlowRuleId);
    }

    /**
     * Triggers the call flow for a device matching the given criteria and call log.
     *
     * @param deviceid   device whose call flow is triggered
     * @param criteria   criteria selecting the call-flow rule
     * @param calllogid  call log associated with the trigger
     */
    @RequestMapping(method= RequestMethod.GET , value = "/{deviceid}/{criteria}/{calllogid}/triggercallflow")
    public void triggerCallFlow(@PathVariable String deviceid, @PathVariable String criteria, @PathVariable String calllogid) {
        aiCallService.triggerCallFlow(deviceid, criteria, calllogid);
    }

}
