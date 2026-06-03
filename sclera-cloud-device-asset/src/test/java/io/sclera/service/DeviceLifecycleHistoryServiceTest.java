package io.sclera.service;

import io.sclera.Repository.DeviceLifeCycleHistoryRepository;
import io.sclera.Repository.DeviceRepository;
import io.sclera.client.InventoryDeviceClient;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DeviceLifecycleHistoryDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for DeviceLifecycleHistoryService: updateOperationalStatus branches (status change,
 * retire email reset, inventory retire), getDeviceHistory offset, and addDeviceHistory unassigned path.
 */
@ExtendWith(MockitoExtension.class)
class DeviceLifecycleHistoryServiceTest {

    @Mock DeviceLifeCycleHistoryRepository deviceLifeCycleHistoryRepository;
    @Mock DeviceRepository deviceRepository;
    @Mock DeviceService deviceService;
    @Mock InventoryDeviceClient inventoryDeviceClient;

    @InjectMocks DeviceLifecycleHistoryService service;

    // ---- getDeviceHistory ------------------------------------------------

    @Test
    void getDeviceHistory_computesOffset() {
        Set<DeviceLifecycleHistoryDTO> hist = Set.of(mock(DeviceLifecycleHistoryDTO.class));
        // page 3, size 10 -> offset 20
        when(deviceLifeCycleHistoryRepository.getDeviceLifeCycleHistory("d1", 10, 20)).thenReturn(hist);
        assertThat(service.getDeviceHistory("u", "v", "d1", 3, 10)).isSameAs(hist);
    }

    // ---- updateOperationalStatus -----------------------------------------

    @Test
    void updateOperationalStatus_statusChanged_updatesDevice() {
        when(deviceLifeCycleHistoryRepository.getLatestOperationalStatusFromHistory("d1")).thenReturn("Working");

        service.updateOperationalStatus("d1", "Not Working", null, "u", "v", "desc");

        verify(deviceRepository).updateDeviceOperationalStatus("d1", "Not Working");
        verify(deviceRepository, never()).updateAssignedUserEmail(any(), any());
    }

    @Test
    void updateOperationalStatus_statusSame_skipsUpdate() {
        when(deviceLifeCycleHistoryRepository.getLatestOperationalStatusFromHistory("d1")).thenReturn("Working");

        service.updateOperationalStatus("d1", "working", null, "u", "v", "desc");

        verify(deviceRepository, never()).updateDeviceOperationalStatus(any(), any());
    }

    @Test
    void updateOperationalStatus_retireWithInventoryId_archivesAndRetires() {
        when(deviceLifeCycleHistoryRepository.getLatestOperationalStatusFromHistory("d1")).thenReturn(null);
        DeviceDTO device = mock(DeviceDTO.class);
        when(device.getInventory_tracking_id()).thenReturn("inv1");
        when(deviceService.getDeviceDetailsByDeviceId("d1")).thenReturn(device);

        service.updateOperationalStatus("d1", null, "true", "u", "v", "desc");

        verify(deviceRepository).updateAssignedUserEmail("d1", null);
        verify(deviceService).archiveDevicesForInventoryDevice(eq("u"), eq("v"), eq(1), anySet());
        verify(inventoryDeviceClient).retireInventoryDevice("v", "d1", "u", "desc", "inv1");
    }

    @Test
    void updateOperationalStatus_retireWithoutInventoryId_noArchive() {
        when(deviceLifeCycleHistoryRepository.getLatestOperationalStatusFromHistory("d1")).thenReturn(null);
        DeviceDTO device = mock(DeviceDTO.class);
        when(device.getInventory_tracking_id()).thenReturn(null);
        when(deviceService.getDeviceDetailsByDeviceId("d1")).thenReturn(device);

        service.updateOperationalStatus("d1", null, "true", "u", "v", "desc");

        verify(deviceService, never()).archiveDevicesForInventoryDevice(any(), any(), any(), anySet());
    }

    // ---- addDeviceHistory ------------------------------------------------

    @Test
    void addDeviceHistory_unassigned_marksNewAndPersists() {
        DeviceLifecycleHistoryDTO dto = mock(DeviceLifecycleHistoryDTO.class);
        lenient().when(dto.getId()).thenReturn(null);
        lenient().when(dto.getDevice_id()).thenReturn("d1");
        lenient().when(dto.getAssigned_user_id()).thenReturn(null);
        lenient().when(dto.getOperational_status()).thenReturn("Working");
        when(deviceLifeCycleHistoryRepository.getLatestAssignedCount("d1")).thenReturn(null);
        lenient().when(deviceLifeCycleHistoryRepository.getLatestOperationalStatusFromHistory("d1")).thenReturn("Working");

        service.addDeviceHistory("u", "v", dto, null);

        verify(dto).setUsage_status("new");
        verify(deviceLifeCycleHistoryRepository).addDeviceLifeCycleHistory(
                any(), eq("Working"), any(), isNull(), any(), any(), isNull(), eq("d1"), any(), any());
    }
}
