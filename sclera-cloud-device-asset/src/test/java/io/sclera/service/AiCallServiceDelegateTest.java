package io.sclera.service;

import io.sclera.Repository.AiCallLogHistoryRepository;
import io.sclera.client.SocketClient;
import io.sclera.dto.DeviceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for the simple AiCallService methods: device-info delegate and the call-flow response
 * history insert + socket notification.
 */
@ExtendWith(MockitoExtension.class)
class AiCallServiceDelegateTest {

    @Mock DeviceService deviceService;
    @Mock AiCallLogHistoryRepository aiCallLogHistoryRepository;
    @Mock SocketClient socketService;

    @InjectMocks AiCallService service;

    @Test
    void getDeviceInfoFromDb_delegates() {
        DeviceDTO dto = mock(DeviceDTO.class);
        when(deviceService.getDeviceInfoFromDb("d1")).thenReturn(dto);
        assertThat(service.getDeviceInfoFromDb("d1")).isSameAs(dto);
    }

    @Test
    void insertCallFlowResponse_recordsHistoryAndNotifiesSocket() {
        service.insertCallFlowResponse("Technician accepted", "accepted", "log1");

        verify(aiCallLogHistoryRepository).insertAiCallLogHistoryState(anyString(), any(),
                eq("Technician accepted"), isNull(), eq("log1"), eq("accepted"));
        verify(socketService).socketAiCallLogHistoryUpdate(anyString());
    }
}
