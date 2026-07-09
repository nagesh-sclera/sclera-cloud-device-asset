package io.sclera.service;

import io.sclera.dto.*;
import io.sclera.integration.dto.ResponseDTO;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Service contract for {@link io.sclera.service.AiCallService}. */
public interface AiCallService {

    String createCallLog(String deviceId, String issueType);

    List<AiCallLogDTO> getallcallstatus(String username, String vdmsid, Integer pageNo, Integer pageSize, String searchKey, Boolean isCompleted);

    Map<String, Integer> getCallStatusCount(String username, String vdmsid);

    DeviceDTO getDeviceInfoFromDb(String deviceId);

    String getAssignee(String deviceId);

    String insertCallResponse(org.json.JSONObject json);

    AiCallLogHistoryDTO getAiCallLogHistoryById(String aiCallLogHistoryId);

    void updateDeviceOnlineStatus(String id, Integer status, String deviceConditionId);

    AiCallLogHistoryDTO fetchAiCallLogHistoryById(String aiCallLogHistoryId);

    List<CallFlowRuleDTO> getCallFlow(String username, String vdmsid, Integer pageno, Integer pagesize, String searchkey);

    void deleteCallFlowById(String username, String vdmsid, Set<String> callFlowRuleIds);

    ResponseEntity<ResponseDTO> upsertCallFlow(CallFlowRuleDTO callFlowRuleDTO, String username, String vdmsid);

    Set<String> browseDockers(String email, String vdmsid, String searchkey);

    List<CallFlowRuleDTO> browseCallFlowDevicesWithSearch(String username, String vdmsid, String dockername, Integer pageno, Integer pagesize, String searchkey);

    void upsertCallFlowConditions(CallFlowRuleDTO callFlowRuleDTO, List<CallFlowRuleConditionDTO> callFlowRuleConditionDTOs);

    List<CallFlowRuleConditionDTO> getCallFlowRuleConditionsByCallFlowRuleId(String callFlowRuleId);

    List<CallFlowRuleConditionDTO> getCallFlowRuleConditionByRuleIdAndCriteria(String callFlowRuleId, String criteria);

    void deleteCallFlowConditions(CallFlowRuleDTO callFlowRuleDTO, List<String> callFlowRuleConditionIds);

    List<CallFlowRuleDTO> getCallFlowByDeviceId(String deviceId);

    List<CallFlowRuleDTO> getCallFlowByDeviceIdAndCriteria(String deviceId, String criteria);

    void triggerCallFlow(String deviceId, String criteria, String callLogId);

    void insertCallFlowResponse(String description, String state, String aiCallLogId);

    Mono<String> makeManagerCall(String deviceId, String phoneNo, String aiCallLogId);
}
