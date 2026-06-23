package io.sclera.service;

import io.sclera.Repository.AiCallLogHistoryRepository;
import io.sclera.client.APICallClient;
import io.sclera.client.CallFlowRuleClient;
import io.sclera.client.CallFlowRuleConditionClient;
import io.sclera.client.SocketClient;
import io.sclera.dto.CallFlowRuleConditionDTO;
import io.sclera.dto.CallFlowRuleDTO;
import io.sclera.dto.DeviceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for AiCallService.triggerCallFlow's Email action branch: resolves the device's call-flow
 * conditions for a criteria, sends the call-flow mail via the API client, and records the outcome
 * in the call-log history. (The Re-route branch uses a reactive WebClient call and is excluded.)
 */
@ExtendWith(MockitoExtension.class)
class AiCallServiceTriggerTest {

    @Mock CallFlowRuleClient callFlowRuleRepository;
    @Mock CallFlowRuleConditionClient callFlowRuleConditionRepository;
    @Mock DeviceService deviceService;
    @Mock APICallClient apiCallService;
    @Mock AiCallLogHistoryRepository aiCallLogHistoryRepository;
    @Mock SocketClient socketService;

    @InjectMocks AiCallService service;

    @Test
    void triggerCallFlow_emailAction_sendsMailAndRecordsHistory() {
        CallFlowRuleDTO rule = new CallFlowRuleDTO();
        rule.setId("r1");
        rule.setDeviceName("Printer");
        CallFlowRuleConditionDTO cond = new CallFlowRuleConditionDTO();
        cond.setCriteria("offline");
        cond.setActionType("Email");
        cond.setActionValue("ops@acme.com");
        cond.setActionMessage("Device offline");

        when(callFlowRuleRepository.getCallFlowByDeviceId("d1")).thenReturn(List.of(rule));
        when(callFlowRuleConditionRepository.getCallFlowRuleConditionByRuleIdAndCriteria("r1", "offline"))
                .thenReturn(List.of(cond));
        when(deviceService.getDeviceInfoFromDb("d1")).thenReturn(mock(DeviceDTO.class));
        when(apiCallService.sendCallFlowMail(any())).thenReturn(ResponseEntity.ok("sent"));

        service.triggerCallFlow("d1", "offline", "log1");

        verify(apiCallService).sendCallFlowMail(any());
        verify(aiCallLogHistoryRepository).insertAiCallLogHistoryState(anyString(), any(),
                anyString(), isNull(), eq("log1"), anyString());
        verify(socketService).socketAiCallLogHistoryUpdate(anyString());
    }
}
