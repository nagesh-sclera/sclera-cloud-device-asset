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

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class AiCallLogController {
    @Autowired
    AiCallService aiCallService;

   @PostMapping("/user/{username}/vdms/{vdmsid}/deviceId/{deviceId}/createcalllog")
    public String createCallLog(@PathVariable String deviceId, @RequestParam String issueType) {
        return aiCallService.createCallLog(deviceId, issueType);
    }

    @GetMapping("/user/{username}/vdms/{vdmsid}/getallcallstatus")
    public List<AiCallLogDTO>  getallcallstatus(@PathVariable String username, @PathVariable String vdmsid,@RequestParam(defaultValue = "1") Integer pageno, @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "null") String searchkey, @RequestParam(defaultValue = "false") boolean isCompleted) {
        return aiCallService.getallcallstatus(username, vdmsid, pageno, pagesize, searchkey, isCompleted);
    }

    @GetMapping("/user/{username}/vdms/{vdmsid}/getcallstatuscount")
    public Map<String, Integer> getCallStatusCount(@PathVariable String username, @PathVariable String vdmsid) {
        return aiCallService.getCallStatusCount(username, vdmsid);
    }

    @GetMapping("/getdeviceinfo/{deviceId}")
    public DeviceDTO getDeviceInfo(@PathVariable String deviceId) {
        return aiCallService.getDeviceInfoFromDb(deviceId);
    }

    @GetMapping("/assign")
    public String triggerGetAssignee() {
        return aiCallService.getAssignee("");
   }

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



    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertcallflow")
    public ResponseEntity<ResponseDTO> upsertCallFlow(@PathVariable String username, @PathVariable String vdmsid, @RequestBody CallFlowRuleDTO callFlowRuleDTO) {
        System.out.println("Received DTO: " + callFlowRuleDTO);
        return aiCallService.upsertCallFlow(callFlowRuleDTO, username, vdmsid);
    }

    @GetMapping("/user/{username}/vdms/{vdmsid}/browsedockers")
    public Set<String> browseDockers(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(defaultValue = "null") String searchkey) {
        return aiCallService.browseDockers(username,vdmsid,searchkey);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/browsedevices")
    public List<CallFlowRuleDTO> browseCallFlowDevicesWithSearch(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername,
                                                                    @RequestParam(defaultValue = "1") Integer pageno,
                                                                    @RequestParam(defaultValue = "10") Integer pagesize,
                                                                    @RequestParam(defaultValue = "null") String searchkey) {
        return aiCallService.browseCallFlowDevicesWithSearch(username, vdmsid, dockername, pageno, pagesize, searchkey);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/getcallflow")
    public List<CallFlowRuleDTO> getCallFlow(@PathVariable String username, @PathVariable String vdmsid, @RequestParam(defaultValue = "1") Integer pageno,
                                             @RequestParam(defaultValue = "10") Integer pagesize, @RequestParam(defaultValue = "null") String searchkey) {
        return aiCallService.getCallFlow(username, vdmsid, pageno, pagesize, searchkey);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/user/{username}/vdms/{vdmsid}/configuration/deletecallflowbyid")
    public void deleteCallFlowById(@PathVariable String username, @PathVariable String vdmsid, @RequestBody Set<String> callFlowRuleId) {
        aiCallService.deleteCallFlowById(username, vdmsid, callFlowRuleId);
    }

    @RequestMapping(method= RequestMethod.GET , value = "/user/{username}/vdms/{vdmsid}/{deviceid}/{criteria}/{calllogid}/triggercallflow")
    public void triggerCallFlow(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String deviceid, @PathVariable String criteria, @PathVariable String calllogid) {
        aiCallService.triggerCallFlow(deviceid, criteria, calllogid);
    }

}
