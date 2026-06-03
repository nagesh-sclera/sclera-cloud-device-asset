package io.sclera.service;

import io.sclera.Repository.DeviceNetworkSpecificationRepository;
import io.sclera.Repository.DeviceRepository;
import io.sclera.Repository.DeviceSpecificationRepository;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DeviceSpecificationDTO;
import io.sclera.models.Device;
import io.sclera.models.DeviceSpecification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean main methods of DeviceSpecificationService: serial-number
 * delegate, online/offline status transitions, id-list lookup, system-updates parsing, and
 * the null-spec branch of getSpecDtoByDeviceId. The heavy JSON ingestion methods
 * (saveFullJson, upsertDeltaJson, updateChildDevices) are deferred.
 */
@ExtendWith(MockitoExtension.class)
class DeviceSpecificationServiceTest {

    @Mock DeviceSpecificationRepository deviceSpecificationRepository;
    @Mock DeviceNetworkSpecificationRepository deviceNetworkSpecificationRepository;
    @Mock DeviceRepository deviceRepository;

    @InjectMocks DeviceSpecificationService service;

    // ---- updateDeviceIdBySerialNumber ------------------------------------

    @Test
    void updateDeviceIdBySerialNumber_delegatesToBothRepos() {
        service.updateDeviceIdBySerialNumber("SN1", "d1");
        verify(deviceSpecificationRepository).updateDeviceIdBySerialNumber("SN1", "d1");
        verify(deviceNetworkSpecificationRepository).updateDeviceIdBySerialNumber("SN1", "d1");
    }

    // ---- updateDeviceStatusToOnline branches -----------------------------

    @Test
    void updateDeviceStatusToOnline_offlineDevice_setsOnlineAndSaves() {
        Device device = mock(Device.class);
        when(device.getStatus()).thenReturn(0);
        when(deviceRepository.findById("d1")).thenReturn(Optional.of(device));

        service.updateDeviceStatusToOnline("d1");

        verify(device).setStatus(1);
        verify(deviceRepository).save(device);
    }

    @Test
    void updateDeviceStatusToOnline_alreadyOnline_doesNotSave() {
        Device device = mock(Device.class);
        when(device.getStatus()).thenReturn(1);
        when(deviceRepository.findById("d1")).thenReturn(Optional.of(device));

        service.updateDeviceStatusToOnline("d1");

        verify(deviceRepository, never()).save(any());
    }

    @Test
    void updateDeviceStatusToOnline_deviceNotFound_doesNothing() {
        when(deviceRepository.findById("d1")).thenReturn(Optional.empty());

        service.updateDeviceStatusToOnline("d1");

        verify(deviceRepository, never()).save(any());
    }

    // ---- updateDeviceStatusToOffline branches ----------------------------

    @Test
    void updateDeviceStatusToOffline_staleDevice_setsOffline() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn("d1");
        when(deviceRepository.findByStatus(1)).thenReturn(List.of(device));
        DeviceSpecification spec = mock(DeviceSpecification.class);
        when(spec.getUpdatedAt()).thenReturn(System.currentTimeMillis() - 100_000L); // >90s ago
        when(deviceSpecificationRepository.findByDeviceId("d1")).thenReturn(spec);

        service.updateDeviceStatusToOffline();

        verify(device).setStatus(0);
        verify(deviceRepository).save(device);
    }

    @Test
    void updateDeviceStatusToOffline_recentDevice_noChange() {
        Device device = mock(Device.class);
        when(device.getId()).thenReturn("d1");
        when(deviceRepository.findByStatus(1)).thenReturn(List.of(device));
        DeviceSpecification spec = mock(DeviceSpecification.class);
        when(spec.getUpdatedAt()).thenReturn(System.currentTimeMillis()); // just now
        when(deviceSpecificationRepository.findByDeviceId("d1")).thenReturn(spec);

        service.updateDeviceStatusToOffline();

        verify(deviceRepository, never()).save(any());
    }

    // ---- getDevicesByIdList branches -------------------------------------

    @Test
    void getDevicesByIdList_nullOrEmpty_returnsEmpty() {
        assertThat(service.getDevicesByIdList(null)).isEmpty();
        assertThat(service.getDevicesByIdList(Set.of())).isEmpty();
    }

    @Test
    void getDevicesByIdList_nonEmpty_delegates() {
        Set<DeviceDTO> result = Set.of(mock(DeviceDTO.class));
        when(deviceRepository.getDevicesByIdList(any())).thenReturn(result);
        assertThat(service.getDevicesByIdList(Set.of("d1"))).isSameAs(result);
    }

    // ---- getSystemUpdatesArrayByDeviceId branches ------------------------

    @Test
    void getSystemUpdatesArrayByDeviceId_present_parsesArray() {
        DeviceSpecification spec = mock(DeviceSpecification.class);
        when(spec.getSystemUpdates()).thenReturn("[{}]");
        when(deviceSpecificationRepository.findByDeviceId("d1")).thenReturn(spec);

        assertThat(service.getSystemUpdatesArrayByDeviceId("d1")).hasSize(1);
    }

    @Test
    void getSystemUpdatesArrayByDeviceId_nullSpec_returnsEmptyArray() {
        when(deviceSpecificationRepository.findByDeviceId("d1")).thenReturn(null);
        assertThat(service.getSystemUpdatesArrayByDeviceId("d1")).isEmpty();
    }

    // ---- getSpecDtoByDeviceId branches -----------------------------------

    @Test
    void getSpecDtoByDeviceId_nullSpec_returnsNull() {
        when(deviceSpecificationRepository.findByDeviceId("d1")).thenReturn(null);
        assertThat(service.getSpecDtoByDeviceId("d1")).isNull();
    }

    @Test
    void getSpecDtoByDeviceId_present_returnsDtoWithDeviceId() {
        DeviceSpecification spec = mock(DeviceSpecification.class);
        when(spec.getOsInfo()).thenReturn("{}"); // dereferenced for username/accountType/emailId
        when(deviceSpecificationRepository.findByDeviceId("d1")).thenReturn(spec);
        when(deviceNetworkSpecificationRepository.findByDeviceId("d1")).thenReturn(null);
        when(deviceRepository.getDeviceNameById("d1")).thenReturn("PC");
        when(deviceRepository.getModelById("d1")).thenReturn("ModelX");

        DeviceSpecificationDTO dto = service.getSpecDtoByDeviceId("d1");

        assertThat(dto).isNotNull();
        assertThat(dto.getDeviceId()).isEqualTo("d1");
    }
}
