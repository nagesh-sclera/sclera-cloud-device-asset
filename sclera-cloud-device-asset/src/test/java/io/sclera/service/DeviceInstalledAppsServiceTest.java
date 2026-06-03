package io.sclera.service;

import io.sclera.Repository.DeviceInstalledAppsRepository;
import io.sclera.dto.DeviceInstalledAppsDTO;
import io.sclera.models.DeviceInstalledApps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for DeviceInstalledAppsService: findByDeviceId delegate, the entity-to-DTO
 * mapping in getInstalledAppDTOs (publisher -> vendor), and updateDeviceIdBySerialNumber delegate.
 */
@ExtendWith(MockitoExtension.class)
class DeviceInstalledAppsServiceTest {

    @Mock DeviceInstalledAppsRepository deviceInstalledAppsRepository;

    @InjectMocks DeviceInstalledAppsService service;

    @Test
    void findByDeviceId_delegates() {
        List<DeviceInstalledApps> apps = List.of(mock(DeviceInstalledApps.class));
        when(deviceInstalledAppsRepository.findByDeviceId("d1")).thenReturn(apps);
        assertThat(service.findByDeviceId("d1")).isSameAs(apps);
    }

    @Test
    void getInstalledAppDTOs_mapsEntityFields() {
        DeviceInstalledApps app = mock(DeviceInstalledApps.class);
        when(app.getName()).thenReturn("Chrome");
        when(app.getVersion()).thenReturn("120");
        when(app.getPublisher()).thenReturn("Google");
        when(deviceInstalledAppsRepository.findByDeviceId("d1")).thenReturn(List.of(app));

        List<DeviceInstalledAppsDTO> dtos = service.getInstalledAppDTOs("d1");

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getName()).isEqualTo("Chrome");
        assertThat(dtos.get(0).getVersion()).isEqualTo("120");
        assertThat(dtos.get(0).getVendor()).isEqualTo("Google");
    }

    @Test
    void updateDeviceIdBySerialNumber_delegates() {
        service.updateDeviceIdBySerialNumber("sn1", "d1");
        verify(deviceInstalledAppsRepository).updateDeviceIdBySerialNumber("sn1", "d1");
    }
}
