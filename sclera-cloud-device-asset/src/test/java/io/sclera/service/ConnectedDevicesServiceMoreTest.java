package io.sclera.service;

import io.sclera.client.ConnectedDevicesClient;
import io.sclera.dto.ConnectedDevicesDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.PowerSourceConnectionsDTO;
import io.sclera.dto.PowerSourceTopologyDTO;
import io.sclera.dto.SpecificationsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for the heavy multi-collaborator orchestration methods of ConnectedDevicesService that
 * the main test deferred: tagged-power-source enrichment, connected-device specification enrichment
 * with per-port power, and the paginated power-source topology assembly with load calculation.
 */
@ExtendWith(MockitoExtension.class)
class ConnectedDevicesServiceMoreTest {

    @Mock ConnectedDevicesClient connectedDevicesRepository;
    @Mock DeviceService deviceService;
    @Mock SpecificationsService specificationsService;

    @InjectMocks ConnectedDevicesService service;

    private ConnectedDevicesDTO connected(String deviceId, String keyName) {
        ConnectedDevicesDTO dto = mock(ConnectedDevicesDTO.class);
        when(dto.getDevice_id()).thenReturn(deviceId);
        when(dto.getKey_name()).thenReturn(keyName);
        return dto;
    }

    @Test
    void getTaggedPowerSourcesByDeviceId_enrichesEachDeviceWithSpecs() {
        ConnectedDevicesDTO spec = connected("d2", "Port 1");
        when(connectedDevicesRepository.getConnectedSpecificationsByDeviceId("d1"))
                .thenReturn(List.of(spec));
        DeviceDTO device = mock(DeviceDTO.class);
        when(deviceService.getDeviceDetails("d2")).thenReturn(device);
        // space name lookup returns null -> only the mapped spec is attached
        when(specificationsService.getDeviceSpecificationsBasedOnDeviceIdAndKeyName("d2", "Port 1 Name"))
                .thenReturn(null);

        List<DeviceDTO> result = service.getTaggedPowerSourcesByDeviceId("u", "v1", "d1");

        assertThat(result).hasSize(1);
        verify(device).setSpecifications(any());
    }

    @Test
    void getConnectedDevicesSpecifications_attachesPowerPerSpecification() {
        ConnectedDevicesDTO spec = connected("d2", "Port 1");
        when(connectedDevicesRepository.getConnectedDevicesSpecifications("s1", 10, 0))
                .thenReturn(List.of(spec));
        DeviceDTO device = mock(DeviceDTO.class);
        when(deviceService.getDeviceDetails("d2")).thenReturn(device);
        when(specificationsService.getDeviceSpecificationsBasedOnDeviceIdAndKeyName("d2", "Port 1 Name"))
                .thenReturn(null);
        when(specificationsService.calculateConsumedPower("Port 1", "d2")).thenReturn(12.5);
        when(specificationsService.getPowerDetails("d2", "Port 1")).thenReturn(null); // -> unit "W"

        List<DeviceDTO> result = service.getConnectedDevicesSpecifications("s1", 10, 0);

        assertThat(result).hasSize(1);
        verify(device).setSpecifications(any());
    }

    @Test
    void getPowerSourceTopologyByPagination_assemblesConnectionsDevicesAndLoad() {
        PowerSourceConnectionsDTO conn = mock(PowerSourceConnectionsDTO.class);
        when(conn.getSource_device_id()).thenReturn("s1");
        when(conn.getTarget_device_id()).thenReturn("t1");
        // pageno=1, pagesize=10 -> offset 0
        when(connectedDevicesRepository.getPowerSourceTopologyByPagination(10, 0))
                .thenReturn(List.of(conn));
        when(connectedDevicesRepository.getPowerSourceTopologyForDevice(any()))
                .thenReturn(List.of()); // no further connections -> empty load

        DeviceDTO device = mock(DeviceDTO.class);
        when(device.getId()).thenReturn("s1");
        when(deviceService.getDeviceDetailsByDeviceIdList(any())).thenReturn(List.of(device));
        when(specificationsService.getDeviceSpecificationsByDeviceId(isNull(), isNull(), anyString()))
                .thenReturn(List.of());
        when(specificationsService.getPowerBasedLoadCalculation(isNull(), isNull(), any()))
                .thenReturn(List.<io.sclera.dto.LoadCalculationDTO>of());

        PowerSourceTopologyDTO result = service.getPowerSourceTopologyByPagination(1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getConnections()).hasSize(1);
        verify(device).setLoad_calculation(any());
    }
}
