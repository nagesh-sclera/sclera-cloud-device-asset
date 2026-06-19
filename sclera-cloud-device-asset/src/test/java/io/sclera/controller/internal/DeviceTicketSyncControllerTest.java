package io.sclera.controller.internal;

import io.sclera.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class DeviceTicketSyncControllerTest {

    private final DeviceService deviceService = Mockito.mock(DeviceService.class);
    private final DeviceTicketSyncController controller = new DeviceTicketSyncController(deviceService);

    @Test
    void syncTicketStats_delegatesToServiceAndReturns204() {
        ResponseEntity<Void> response = controller.syncTicketStats("device-123");

        verify(deviceService).updateDeviceTicketCount("device-123");
        verify(deviceService).updateDeviceTicketStatus("device-123");
        assertThat(response.getStatusCode().value()).isEqualTo(204);
    }
}
