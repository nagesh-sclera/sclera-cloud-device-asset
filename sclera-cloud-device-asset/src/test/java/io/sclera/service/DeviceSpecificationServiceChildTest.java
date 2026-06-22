package io.sclera.service;

import io.sclera.Repository.DeviceRepository;
import io.sclera.Repository.DeviceSpecificationRepository;
import io.sclera.dto.DeviceDTO;
import io.sclera.utils.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for DeviceSpecificationService.updateChildDevices - the child/subsystem onboarding
 * path deferred by the main test: parses stored child-device JSON and creates virtual device
 * records for new entries, updating the parent subsystem count and onboard status.
 */
@ExtendWith(MockitoExtension.class)
class DeviceSpecificationServiceChildTest {

    @Mock DeviceSpecificationRepository deviceSpecificationRepository;
    @Mock DeviceRepository deviceRepository;
    @Mock DeviceService deviceService;
    @Mock Utils utils;

    @InjectMocks DeviceSpecificationService service;

    @Test
    void updateChildDevices_nullChildData_returnsEarly() {
        when(deviceSpecificationRepository.getChildDeviceByDeviceId("d1")).thenReturn(null);

        service.updateChildDevices("d1", "v1", "u");

        verify(deviceService, never()).updateParentSubsystemCount(any());
    }

    @Test
    void updateChildDevices_newChild_createsVirtualDeviceAndUpdatesStatus() {
        when(deviceSpecificationRepository.getChildDeviceByDeviceId("d1"))
                .thenReturn("{\"cameras\":[{\"displayName\":\"Cam1\"}]}");
        when(deviceRepository.findByDisplayNameAndSubsystemParentId("Cam1", "d1"))
                .thenReturn(Optional.empty());
        when(utils.replaceSpecialCharactersWithUnderscore(anyString())).thenReturn("v1_child_cam1");
        when(deviceRepository.getDevicesByIdList(any())).thenReturn(Set.of(mock(DeviceDTO.class)));

        service.updateChildDevices("d1", "v1", "u");

        verify(deviceService).updateParentSubsystemCount("d1");
        verify(deviceService).updateVirtualDeviceOnboardStatus(any(), eq("u"));
    }

    @Test
    void updateChildDevices_existingChild_skipsCreation() {
        when(deviceSpecificationRepository.getChildDeviceByDeviceId("d1"))
                .thenReturn("{\"cameras\":[{\"displayName\":\"Cam1\"}]}");
        when(deviceRepository.findByDisplayNameAndSubsystemParentId("Cam1", "d1"))
                .thenReturn(Optional.of(mock(io.sclera.models.Device.class)));

        service.updateChildDevices("d1", "v1", "u");

        verify(deviceService, never()).updateParentSubsystemCount(any());
        verify(deviceService, never()).updateVirtualDeviceOnboardStatus(any(), any());
    }
}
