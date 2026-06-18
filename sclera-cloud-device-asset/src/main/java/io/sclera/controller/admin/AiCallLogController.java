package io.sclera.controller.admin;

import io.sclera.dto.AiCallLogDTO;
import io.sclera.dto.CallFlowRuleDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.service.AiCallService;
import io.sclera.utils.PageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "AI Call Log", description = "AI-assisted call logging, call-flow configuration and call triggering.")
public class AiCallLogController {
    private static final Logger log = LoggerFactory.getLogger(AiCallLogController.class);
    @Autowired
    AiCallService aiCallService;

    /**
     * Creates a call log for the given device and issue type.
     *
     * @param deviceId   device the call log belongs to
     * @param issueType  type of issue the call concerns
     * @return identifier or status of the created call log
     */
    @Operation(summary = "Create a call log",
            description = "Creates a call log for the given device and issue type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call log created"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/deviceId/{deviceId}/createcalllog")
    public String createCallLog(
            @Parameter(description = "Device the call log belongs to") @PathVariable String deviceId,
            @Parameter(description = "Type of issue the call concerns") @RequestParam String issueType) {
        log.info("createCallLog deviceId={} issueType={}", deviceId, issueType);
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
    @Operation(summary = "Get call statuses",
            description = "Returns a paged list of call statuses for the tenant, optionally restricted to completed calls.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call statuses returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getallcallstatus")
    public Page<AiCallLogDTO> getallcallstatus(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Optional search filter") @RequestParam(defaultValue = "null") String searchkey,
            @Parameter(description = "Whether to return only completed calls") @RequestParam(defaultValue = "false") boolean isCompleted) {
        log.info("getallcallstatus username={} vdmsid={}", username, vdmsid);
        return PageUtils.toPage(aiCallService.getallcallstatus(username, vdmsid, pageno, pagesize, searchkey, isCompleted), pageno, pagesize);
    }

    /**
     * Returns a count of call statuses grouped by status for the tenant.
     *
     * @param username  owning user
     * @param vdmsid    owning VDMS id
     * @return map of status name to count
     */
    @Operation(summary = "Get call status counts",
            description = "Returns a count of call statuses grouped by status for the tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call status counts returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getcallstatuscount")
    public Map<String, Integer> getCallStatusCount(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid) {
        log.info("getCallStatusCount username={} vdmsid={}", username, vdmsid);
        return aiCallService.getCallStatusCount(username, vdmsid);
    }

    /**
     * Returns device information loaded from the database.
     *
     * @param deviceId  device to look up
     * @return device details for the given id
     */
    @Operation(summary = "Get device info",
            description = "Returns device information loaded from the database for the given id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device info returned"),
            @ApiResponse(responseCode = "404", description = "Device not found"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getdeviceinfo/{deviceId}")
    public DeviceDTO getDeviceInfo(
            @Parameter(description = "Device to look up") @PathVariable String deviceId) {
        log.info("getDeviceInfo deviceId={}", deviceId);
        return aiCallService.getDeviceInfoFromDb(deviceId);
    }

    /**
     * Triggers resolution of an assignee and returns the result.
     *
     * @return identifier or status of the resolved assignee
     */
    @Operation(summary = "Trigger assignee resolution",
            description = "Triggers resolution of an assignee and returns the result.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assignee resolved"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/assign")
    public String triggerGetAssignee() {
        log.info("triggerGetAssignee called");
        return aiCallService.getAssignee("");
    }

    /**
     * Parses an uploaded JSON file and inserts the contained call response.
     *
     * @param file  multipart file containing the call response JSON payload
     * @return identifier or status of the inserted call response
     */
    @Operation(summary = "Insert a call response",
            description = "Parses an uploaded JSON file and inserts the contained call response.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call response inserted"),
            @ApiResponse(responseCode = "400", description = "Invalid or unreadable JSON file"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/insertcallresponse")
    public String insertCallResponse(@RequestParam("callinfo") MultipartFile file) {
        log.info("insertCallResponse called");
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
    @Operation(summary = "Upsert a call-flow rule",
            description = "Creates or updates a call-flow rule for the tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call-flow rule upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertcallflow")
    public ResponseEntity<ResponseDTO> upsertCallFlow(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody CallFlowRuleDTO callFlowRuleDTO) {
        log.info("upsertCallFlow username={} vdmsid={}", username, vdmsid);
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
    @Operation(summary = "Browse docker names",
            description = "Returns the set of docker names available to the tenant, optionally filtered by a search term.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Docker names returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/browsedockers")
    public Set<String> browseDockers(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Optional search filter") @RequestParam(defaultValue = "null") String searchkey) {
        log.info("browseDockers username={} vdmsid={}", username, vdmsid);
        return aiCallService.browseDockers(username, vdmsid, searchkey);
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
    @Operation(summary = "Browse call-flow devices",
            description = "Returns a paged list of call-flow devices for a docker, with optional search.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call-flow devices returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/docker/{dockername}/browsedevices")
    public Page<CallFlowRuleDTO> browseCallFlowDevicesWithSearch(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker whose devices are browsed") @PathVariable String dockername,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Optional search filter") @RequestParam(defaultValue = "null") String searchkey) {
        log.info("browseCallFlowDevicesWithSearch username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return PageUtils.toPage(aiCallService.browseCallFlowDevicesWithSearch(username, vdmsid, dockername, pageno, pagesize, searchkey), pageno, pagesize);
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
    @Operation(summary = "Get call-flow rules",
            description = "Returns a paged list of call-flow rules for the tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call-flow rules returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getcallflow")
    public Page<CallFlowRuleDTO> getCallFlow(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageno,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer pagesize,
            @Parameter(description = "Optional search filter") @RequestParam(defaultValue = "null") String searchkey) {
        log.info("getCallFlow username={} vdmsid={}", username, vdmsid);
        return PageUtils.toPage(aiCallService.getCallFlow(username, vdmsid, pageno, pagesize, searchkey), pageno, pagesize);
    }

    /**
     * Deletes the call-flow rules identified by the given ids.
     *
     * @param username        owning user
     * @param vdmsid          owning VDMS id
     * @param callFlowRuleId  set of call-flow rule ids to delete
     */
    @Operation(summary = "Delete call-flow rules by ids",
            description = "Deletes the call-flow rules identified by the given ids.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call-flow rules deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @DeleteMapping("/configuration/deletecallflowbyid")
    public void deleteCallFlowById(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody Set<String> callFlowRuleId) {
        log.info("deleteCallFlowById username={} vdmsid={}", username, vdmsid);
        aiCallService.deleteCallFlowById(username, vdmsid, callFlowRuleId);
    }

    /**
     * Triggers the call flow for a device matching the given criteria and call log.
     *
     * @param deviceid   device whose call flow is triggered
     * @param criteria   criteria selecting the call-flow rule
     * @param calllogid  call log associated with the trigger
     */
    @Operation(summary = "Trigger a call flow",
            description = "Triggers the call flow for a device matching the given criteria and call log.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Call flow triggered"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/{deviceid}/{criteria}/{calllogid}/triggercallflow")
    public void triggerCallFlow(
            @Parameter(description = "Device whose call flow is triggered") @PathVariable String deviceid,
            @Parameter(description = "Criteria selecting the call-flow rule") @PathVariable String criteria,
            @Parameter(description = "Call log associated with the trigger") @PathVariable String calllogid) {
        log.info("triggerCallFlow deviceid={} criteria={} calllogid={}", deviceid, criteria, calllogid);
        aiCallService.triggerCallFlow(deviceid, criteria, calllogid);
    }

}
