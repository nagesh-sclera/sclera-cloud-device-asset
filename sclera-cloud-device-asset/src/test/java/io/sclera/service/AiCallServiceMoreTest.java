package io.sclera.service;

import io.sclera.Repository.AiCallLogHistoryRepository;
import io.sclera.Repository.AiCallLogRepository;
import io.sclera.client.CallFlowRuleClient;
import io.sclera.client.CallFlowRuleConditionClient;
import io.sclera.client.SocketClient;
import io.sclera.dto.AiCallLogHistoryDTO;
import io.sclera.dto.CallFlowRuleConditionDTO;
import io.sclera.dto.CallFlowRuleDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for AiCallService methods the main test deferred: call-log-history lookup (with and
 * without technician-name enrichment) and updateDeviceOnlineStatus across its
 * scheduler-terminate (deviceConditionId present) and device-online (absent) branches.
 */
@ExtendWith(MockitoExtension.class)
class AiCallServiceMoreTest {

    @Mock AiCallLogRepository aiCallLogRepository;
    @Mock AiCallLogHistoryRepository aiCallLogHistoryRepository;
    @Mock DeviceService deviceService;
    @Mock CallFlowRuleClient callFlowRuleRepository;
    @Mock CallFlowRuleConditionClient callFlowRuleConditionRepository;
    @Mock TechnicianService technicianService;
    @Mock DeviceConditionsService deviceConditionsService;
    @Mock SocketClient socketService;

    @InjectMocks AiCallService service;

    @Test
    void getAiCallLogHistoryById_enrichesTechnicianName() {
        AiCallLogHistoryDTO dto = mock(AiCallLogHistoryDTO.class);
        when(dto.getTechnicianId()).thenReturn("t1");
        when(aiCallLogHistoryRepository.getAiCallLogHistoryById("h1")).thenReturn(dto);
        when(technicianService.getTechnicianNameById("t1")).thenReturn("Bob");

        assertThat(service.getAiCallLogHistoryById("h1")).isSameAs(dto);
        verify(dto).setTechnicianName("Bob");
    }

    @Test
    void fetchAiCallLogHistoryById_present_returnsDto() {
        AiCallLogHistoryDTO dto = mock(AiCallLogHistoryDTO.class);
        when(aiCallLogHistoryRepository.getAiCallLogHistoryById("h1")).thenReturn(dto);
        assertThat(service.fetchAiCallLogHistoryById("h1")).isSameAs(dto);
    }

    @Test
    void fetchAiCallLogHistoryById_absent_returnsNull() {
        when(aiCallLogHistoryRepository.getAiCallLogHistoryById("h1")).thenReturn(null);
        assertThat(service.fetchAiCallLogHistoryById("h1")).isNull();
    }

    @Test
    void updateDeviceOnlineStatus_noConditionId_marksDeviceOnline() {
        when(aiCallLogRepository.getAiCallLogIdByDeviceId("d1")).thenReturn("log1");
        when(deviceConditionsService.getAlertCount("d1")).thenReturn(0);
        when(aiCallLogHistoryRepository.countByAiCallLogId("log1")).thenReturn(0);

        service.updateDeviceOnlineStatus("d1", 1, "");

        verify(aiCallLogRepository).upsertStatus(eq("log1"), isNull(), eq(Boolean.TRUE), isNull(), eq("Device Online"));
        verify(socketService).socketAiCallLogOngoingHistoryUpdate(anyString());
    }

    @Test
    void updateDeviceOnlineStatus_withConditionId_terminatesCallAndClearsConditions() {
        when(aiCallLogRepository.getAiCallLogIdByDeviceId("d1")).thenReturn("log1");
        when(deviceConditionsService.getAlertCount("d1")).thenReturn(0);
        when(callFlowRuleRepository.checkCallFlowByDeviceid("d1")).thenReturn(""); // empty -> upsert path
        when(deviceConditionsService.getDeviceConditionsForAiCall("", "", "", "d1")).thenReturn(Set.of());

        service.updateDeviceOnlineStatus("d1", 1, "dc1");

        verify(aiCallLogHistoryRepository).insertAiCallLogHistoryState(anyString(), any(),
                eq("Device online"), isNull(), eq("log1"), eq("Scheduler terminated"));
        verify(deviceConditionsService).deleteDeviceConditions(eq(""), eq(""), any());
    }

    @Test
    void getCallFlowByDeviceId_populatesConditions() {
        CallFlowRuleDTO rule = mock(CallFlowRuleDTO.class);
        when(rule.getId()).thenReturn("r1");
        when(callFlowRuleRepository.getCallFlowByDeviceId("d1")).thenReturn(List.of(rule));
        when(callFlowRuleConditionRepository.getCallFlowRuleConditionsByCallFlowRuleId("r1"))
                .thenReturn(List.of(mock(CallFlowRuleConditionDTO.class)));

        assertThat(service.getCallFlowByDeviceId("d1")).hasSize(1);
        verify(rule).setCallFlowRuleConditions(any());
    }

    @Test
    void getCallFlowByDeviceId_blankDeviceId_returnsEmpty() {
        assertThat(service.getCallFlowByDeviceId("  ")).isEmpty();
    }

    @Test
    void getCallFlowByDeviceIdAndCriteria_populatesMatchingConditions() {
        CallFlowRuleDTO rule = mock(CallFlowRuleDTO.class);
        when(rule.getId()).thenReturn("r1");
        when(callFlowRuleRepository.getCallFlowByDeviceId("d1")).thenReturn(List.of(rule));
        when(callFlowRuleConditionRepository.getCallFlowRuleConditionByRuleIdAndCriteria("r1", "offline"))
                .thenReturn(List.of(mock(CallFlowRuleConditionDTO.class)));

        assertThat(service.getCallFlowByDeviceIdAndCriteria("d1", "offline")).hasSize(1);
        verify(rule).setCallFlowRuleConditions(any());
    }

    @Test
    void getCallFlowByDeviceIdAndCriteria_blankDeviceId_returnsEmpty() {
        assertThat(service.getCallFlowByDeviceIdAndCriteria("", "offline")).isEmpty();
    }
}
