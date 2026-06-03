package io.sclera.service;

import io.sclera.Repository.AiCallLogHistoryRepository;
import io.sclera.Repository.AiCallLogRepository;
import io.sclera.client.CallFlowRuleClient;
import io.sclera.client.CallFlowRuleConditionClient;
import io.sclera.dto.AiCallLogDTO;
import io.sclera.dto.AiCallLogHistoryDTO;
import io.sclera.dto.CallFlowRuleConditionDTO;
import io.sclera.dto.CallFlowRuleDTO;
import io.sclera.dto.CallStatusDTO;
import io.sclera.dto.DeviceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean main methods of AiCallService: call-status counts, delegates,
 * call-flow CRUD with branches (upsertCallFlow null/duplicate/valid), and enrichment loops.
 * The async/WebClient/scheduler-heavy methods (createCallLog, getAssignee, triggerCallFlow,
 * makeManagerCall, insertCallResponse) are deferred.
 */
@ExtendWith(MockitoExtension.class)
class AiCallServiceTest {

    @Mock AiCallLogRepository aiCallLogRepository;
    @Mock AiCallLogHistoryRepository aiCallLogHistoryRepository;
    @Mock DeviceService deviceService;
    @Mock CallFlowRuleClient callFlowRuleRepository;
    @Mock CallFlowRuleConditionClient callFlowRuleConditionRepository;

    @InjectMocks AiCallService service;

    // ---- counts / delegates ----------------------------------------------

    @Test
    void getCallStatusCount_buildsMapFromCompletedAndOngoing() {
        when(aiCallLogRepository.getCallStatusCount(Boolean.TRUE)).thenReturn(8);
        when(aiCallLogRepository.getCallStatusCount(Boolean.FALSE)).thenReturn(3);

        assertThat(service.getCallStatusCount("u", "v"))
                .containsEntry("completed", 8).containsEntry("ongoing", 3);
    }

    @Test
    void getDeviceInfoFromDb_delegates() {
        DeviceDTO d = mock(DeviceDTO.class);
        when(deviceService.getDeviceInfoFromDb("d1")).thenReturn(d);
        assertThat(service.getDeviceInfoFromDb("d1")).isSameAs(d);
    }

    @Test
    void browseDockers_delegates() {
        Set<String> dockers = Set.of("docker1");
        when(deviceService.listAiEnabledDockers("e@x.com", "v", "key")).thenReturn(dockers);
        assertThat(service.browseDockers("e@x.com", "v", "key")).isSameAs(dockers);
    }

    @Test
    void getCallFlowRuleConditionsByCallFlowRuleId_delegates() {
        List<CallFlowRuleConditionDTO> conds = List.of(mock(CallFlowRuleConditionDTO.class));
        when(callFlowRuleConditionRepository.getCallFlowRuleConditionsByCallFlowRuleId("r1")).thenReturn(conds);
        assertThat(service.getCallFlowRuleConditionsByCallFlowRuleId("r1")).isSameAs(conds);
    }

    @Test
    void getCallFlowRuleConditionByRuleIdAndCriteria_delegates() {
        List<CallFlowRuleConditionDTO> conds = List.of(mock(CallFlowRuleConditionDTO.class));
        when(callFlowRuleConditionRepository.getCallFlowRuleConditionByRuleIdAndCriteria("r1", "offline"))
                .thenReturn(conds);
        assertThat(service.getCallFlowRuleConditionByRuleIdAndCriteria("r1", "offline")).isSameAs(conds);
    }

    @Test
    void deleteCallFlowById_deletesEach() {
        service.deleteCallFlowById("u", "v", new java.util.LinkedHashSet<>(List.of("r1", "r2")));
        verify(callFlowRuleRepository).deleteById("r1");
        verify(callFlowRuleRepository).deleteById("r2");
    }

    // ---- condition upsert / delete branches ------------------------------

    @Test
    void deleteCallFlowConditions_nonEmpty_delegates() {
        service.deleteCallFlowConditions(mock(CallFlowRuleDTO.class), List.of("c1"));
        verify(callFlowRuleConditionRepository).deleteCallFlowRuleConditionById(List.of("c1"));
    }

    @Test
    void deleteCallFlowConditions_emptyList_doesNothing() {
        service.deleteCallFlowConditions(mock(CallFlowRuleDTO.class), List.of());
        verify(callFlowRuleConditionRepository, never()).deleteCallFlowRuleConditionById(any());
    }

    @Test
    void upsertCallFlowConditions_upsertsEachCondition() {
        CallFlowRuleDTO rule = mock(CallFlowRuleDTO.class);
        when(rule.getId()).thenReturn("r1");
        CallFlowRuleConditionDTO cond = new CallFlowRuleConditionDTO();
        cond.setId("c1");
        cond.setCriteria("offline");

        service.upsertCallFlowConditions(rule, List.of(cond));

        verify(callFlowRuleConditionRepository).upsertCallFlowRuleCondition(
                eq("c1"), eq("offline"), any(), any(), any(), eq("r1"));
    }

    // ---- getCallFlow enrichment ------------------------------------------

    @Test
    void getCallFlow_enrichesRulesWithConditions() {
        CallFlowRuleDTO rule = mock(CallFlowRuleDTO.class);
        when(rule.getId()).thenReturn("r1");
        // pageno=2, pagesize=10 -> offset 10
        when(callFlowRuleRepository.getAllCallFlowRules(10, 10, "key")).thenReturn(List.of(rule));
        List<CallFlowRuleConditionDTO> conds = List.of(mock(CallFlowRuleConditionDTO.class));
        when(callFlowRuleConditionRepository.getCallFlowRuleConditionsByCallFlowRuleId("r1")).thenReturn(conds);

        assertThat(service.getCallFlow("u", "v", 2, 10, "key")).containsExactly(rule);
        verify(rule).setCallFlowRuleConditions(conds);
    }

    // ---- browseCallFlowDevicesWithSearch mapping -------------------------

    @Test
    void browseCallFlowDevicesWithSearch_mapsDevicesToRules() {
        DeviceDTO device = mock(DeviceDTO.class);
        when(device.getId()).thenReturn("d1");
        when(device.getName()).thenReturn("Printer");
        when(deviceService.browseAiCallFlowDevicesWithSearch("u", "v", "dock", 1, 10, "key"))
                .thenReturn(List.of(device));

        List<CallFlowRuleDTO> result = service.browseCallFlowDevicesWithSearch("u", "v", "dock", 1, 10, "key");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeviceId()).isEqualTo("d1");
        assertThat(result.get(0).getDeviceName()).isEqualTo("Printer");
    }

    // ---- upsertCallFlow branches -----------------------------------------

    @Test
    void upsertCallFlow_nullDto_returnsBadRequest() {
        assertThat(service.upsertCallFlow(null, "u", "v").getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void upsertCallFlow_duplicateCriteria_returnsConflict() {
        CallFlowRuleDTO dto = new CallFlowRuleDTO();
        dto.setId("r1");
        CallFlowRuleConditionDTO c1 = new CallFlowRuleConditionDTO();
        c1.setCriteria("offline");
        CallFlowRuleConditionDTO c2 = new CallFlowRuleConditionDTO();
        c2.setCriteria("offline"); // duplicate
        dto.setCallFlowRuleConditions(List.of(c1, c2));
        when(callFlowRuleConditionRepository.getCallFlowRuleConditionsByCallFlowRuleId("r1")).thenReturn(List.of());

        assertThat(service.upsertCallFlow(dto, "u", "v").getStatusCode().value()).isEqualTo(409);
        verify(callFlowRuleRepository, never()).upsertAiCallFlow(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void upsertCallFlow_valid_upsertsAndReturnsOk() {
        CallFlowRuleDTO dto = new CallFlowRuleDTO();
        dto.setId("r1");
        CallFlowRuleConditionDTO cond = new CallFlowRuleConditionDTO();
        cond.setId("c1");
        cond.setCriteria("offline");
        dto.setCallFlowRuleConditions(List.of(cond));
        when(callFlowRuleConditionRepository.getCallFlowRuleConditionsByCallFlowRuleId("r1")).thenReturn(List.of());

        assertThat(service.upsertCallFlow(dto, "u", "v").getStatusCode().value()).isEqualTo(200);
        verify(callFlowRuleRepository).upsertAiCallFlow(eq("r1"), any(), any(), any(), any(), any(), any());
    }

    // ---- getallcallstatus enrichment -------------------------------------

    @Test
    void getallcallstatus_enrichesEachLogWithHistoryAndStatus() {
        AiCallLogDTO callLog = mock(AiCallLogDTO.class);
        when(callLog.getId()).thenReturn("cl1");
        when(callLog.getDeviceId()).thenReturn("d1");
        // pageNo=1, pageSize=10 -> offset 0, isCompleted true -> "1"
        when(aiCallLogRepository.getAllAiCallLog(10, 0, "key", "1")).thenReturn(List.of(callLog));
        List<AiCallLogHistoryDTO> hist = List.of(mock(AiCallLogHistoryDTO.class));
        when(aiCallLogHistoryRepository.getAiCallLogHistoryByAiCallLogId("cl1")).thenReturn(hist);
        CallStatusDTO status = mock(CallStatusDTO.class);
        when(aiCallLogRepository.getStatusInformation("d1")).thenReturn(status);

        assertThat(service.getallcallstatus("u", "v", 1, 10, "key", true)).containsExactly(callLog);
        verify(callLog).setAiCallLogHistory(hist);
        verify(callLog).setCallStatus(status);
    }
}
