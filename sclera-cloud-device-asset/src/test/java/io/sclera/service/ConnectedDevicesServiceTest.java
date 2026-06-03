package io.sclera.service;

import io.sclera.client.ConnectedDevicesClient;
import io.sclera.dto.ConnectedDevicesDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.LoadCalculationDTO;
import io.sclera.dto.PowerSourceConnectionsDTO;
import io.sclera.dto.SpecificationsDTO;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean main methods of ConnectedDevicesService. The heavy
 * orchestration methods that chain many collaborators per element
 * (getConnectedDevicesSpecifications, getTaggedPowerSourcesByDeviceId,
 * getPowerSourceTopologyByPagination) are deferred to a focused round.
 */
@ExtendWith(MockitoExtension.class)
class ConnectedDevicesServiceTest {

    @Mock ConnectedDevicesClient connectedDevicesRepository;
    @Mock DeviceService deviceService;
    @Mock SpecificationsService specificationsService;

    @InjectMocks ConnectedDevicesService service;

    // ---- delegates --------------------------------------------------------

    @Test
    void addConnectedDevices_delegatesWithIds() {
        ConnectedDevicesDTO dto = mock(ConnectedDevicesDTO.class);
        when(dto.getId()).thenReturn("id1");
        when(dto.getConnected_specifications_id()).thenReturn("cs1");
        when(dto.getSpecifications_id()).thenReturn("s1");

        service.addConnectedDevices(dto);

        verify(connectedDevicesRepository).addConnectedDevices("id1", "cs1", "s1");
    }

    @Test
    void getConnectedSpecifications_delegates() {
        List<ConnectedDevicesDTO> list = List.of(mock(ConnectedDevicesDTO.class));
        when(connectedDevicesRepository.getConnectedSpecificationsByDeviceId("d1")).thenReturn(list);
        assertThat(service.getConnectedSpecifications("d1")).isSameAs(list);
    }

    @Test
    void getAllInputConnectedSpecifications_delegates() {
        List<ConnectedDevicesDTO> list = List.of(mock(ConnectedDevicesDTO.class));
        when(connectedDevicesRepository.getAllInputConnectedSpecifications("d1")).thenReturn(list);
        assertThat(service.getAllInputConnectedSpecifications("d1")).isSameAs(list);
    }

    @Test
    void getAllOutputConnectedSpecifications_delegates() {
        List<ConnectedDevicesDTO> list = List.of(mock(ConnectedDevicesDTO.class));
        when(connectedDevicesRepository.getAllOutputConnectedSpecifications("d1")).thenReturn(list);
        assertThat(service.getAllOutputConnectedSpecifications("d1")).isSameAs(list);
    }

    @Test
    void getPowerSourceTopologyForDevice_delegates() {
        List<PowerSourceConnectionsDTO> list = List.of(mock(PowerSourceConnectionsDTO.class));
        when(connectedDevicesRepository.getPowerSourceTopologyForDevice(Set.of("d1"))).thenReturn(list);
        assertThat(service.getPowerSourceTopologyForDevice(Set.of("d1"))).isSameAs(list);
    }

    @Test
    void getAllConnectedDevicesForLoadCalculation_delegates() {
        List<ConnectedDevicesDTO> list = List.of(mock(ConnectedDevicesDTO.class));
        when(connectedDevicesRepository.getAllConnectedDevicesForLoadCalculation("s1")).thenReturn(list);
        assertThat(service.getAllConnectedDevicesForLoadCalculation("s1")).isSameAs(list);
    }

    @Test
    void getPowerSourceTopologyConnectionsCount_delegates() {
        when(connectedDevicesRepository.getPowerSourceTopologyConnectionsCount()).thenReturn(9);
        assertThat(service.getPowerSourceTopologyConnectionsCount()).isEqualTo(9);
    }

    @Test
    void untagPowerSource_delegates() {
        service.untagPowerSource("s1", "cs1");
        verify(connectedDevicesRepository).untagPowerSource("s1", "cs1");
    }

    @Test
    void untagDevice_delegates() {
        service.untagDevice("s1", "cs1");
        verify(connectedDevicesRepository).untagDevice("s1", "cs1");
    }

    @Test
    void untagPowerSourceByDeviceId_delegates() {
        service.untagPowerSourceByDeviceId("d1");
        verify(connectedDevicesRepository).untagPowerSourceByDeviceId("d1");
    }

    @Test
    void deleteConnectedDevicesBySpecificationId_delegates() {
        service.deleteConnectedDevicesBySpecificationId("s1");
        verify(connectedDevicesRepository).deleteConnectedDevicesBySpecificationId("s1");
    }

    // ---- getPowerUnit branch ---------------------------------------------

    @Test
    void getPowerUnit_returnsKeyUnitWhenSpecPresent() {
        SpecificationsDTO spec = mock(SpecificationsDTO.class);
        when(spec.getKey_unit()).thenReturn("kW");
        when(specificationsService.getPowerDetails("d1", "Power")).thenReturn(spec);
        assertThat(service.getPowerUnit("d1", "Power")).isEqualTo("kW");
    }

    @Test
    void getPowerUnit_defaultsToWWhenSpecNull() {
        when(specificationsService.getPowerDetails("d1", "Power")).thenReturn(null);
        assertThat(service.getPowerUnit("d1", "Power")).isEqualTo("W");
    }

    // ---- getPortPattern (pure regex) -------------------------------------

    @Test
    void getPortPattern_returnsMatchedGroup() {
        assertThat(service.getPortPattern("Port 1")).isEqualTo("Port 1");
    }

    @Test
    void getPortPattern_returnsNullWhenNoMatch() {
        assertThat(service.getPortPattern("abc")).isNull();
    }

    // ---- getSpaceNameByDeviceId ------------------------------------------

    @Test
    void getSpaceNameByDeviceId_buildsSpaceNameAndDelegates() {
        ConnectedDevicesDTO dto = mock(ConnectedDevicesDTO.class);
        when(dto.getKey_name()).thenReturn("Port 1");
        when(dto.getDevice_id()).thenReturn("d1");
        SpecificationsDTO expected = mock(SpecificationsDTO.class);
        when(specificationsService.getDeviceSpecificationsBasedOnDeviceIdAndKeyName("d1", "Port 1 Name"))
                .thenReturn(expected);

        assertThat(service.getSpaceNameByDeviceId(dto)).isSameAs(expected);
    }

    // ---- mappingConnectedDevicesToSpecifications (pure mapping) -----------

    @Test
    void mappingConnectedDevicesToSpecifications_mapsFields() {
        ConnectedDevicesDTO dto = mock(ConnectedDevicesDTO.class);
        when(dto.getId()).thenReturn("id1");
        when(dto.getConnected_specifications_id()).thenReturn("cs1");
        when(dto.getConnected_device()).thenReturn("cd1");
        when(dto.getDevice_id()).thenReturn("d1");
        when(dto.getKey_name()).thenReturn("Power");

        List<SpecificationsDTO> result = service.mappingConnectedDevicesToSpecifications(List.of(dto));

        assertThat(result).hasSize(1);
        SpecificationsDTO s = result.get(0);
        assertThat(s.getId()).isEqualTo("id1");
        assertThat(s.getConnected_specifications_id()).isEqualTo("cs1");
        assertThat(s.getConnected_device()).isEqualTo("cd1");
        assertThat(s.getDevice_id()).isEqualTo("d1");
        assertThat(s.getKey_name()).isEqualTo("Power");
    }

    // ---- getDeviceSpecificationsByDevices ---------------------------------

    @Test
    void getDeviceSpecificationsByDevices_setsSpecsOnEachDevice() {
        DeviceDTO device = mock(DeviceDTO.class);
        when(device.getId()).thenReturn("d1");
        List<SpecificationsDTO> specs = List.of(mock(SpecificationsDTO.class));
        when(specificationsService.getDeviceSpecificationsByDeviceId(isNull(), isNull(), eq("d1")))
                .thenReturn(specs);

        List<DeviceDTO> result = service.getDeviceSpecificationsByDevices(List.of(device));

        verify(device).setSpecifications(specs);
        assertThat(result).containsExactly(device);
    }

    // ---- getPowerSourceTopologyPagination (offset math) ------------------

    @Test
    void getPowerSourceTopologyPagination_computesOffsetAndDelegates() {
        List<PowerSourceConnectionsDTO> list = List.of(mock(PowerSourceConnectionsDTO.class));
        when(connectedDevicesRepository.getPowerSourceTopologyByPagination(20, 40)).thenReturn(list);

        // pageno=3, pagesize=20 -> offset = 20 * (3 - 1) = 40
        assertThat(service.getPowerSourceTopologyPagination(3, 20)).isSameAs(list);
        verify(connectedDevicesRepository).getPowerSourceTopologyByPagination(20, 40);
    }

    // ---- calculateLoadForTopology ----------------------------------------

    @Test
    void calculateLoadForTopology_buildsSpecsAndDelegates() {
        PowerSourceConnectionsDTO conn = mock(PowerSourceConnectionsDTO.class);
        when(conn.getSource_specifications_id()).thenReturn("ss1");
        when(conn.getSource_specifications_name()).thenReturn("Source 1");
        when(conn.getSource_device_id()).thenReturn("d1");
        List<LoadCalculationDTO> expected = List.of(mock(LoadCalculationDTO.class));
        when(specificationsService.getPowerBasedLoadCalculation(isNull(), isNull(), any()))
                .thenReturn(expected);

        assertThat(service.calculateLoadForTopology(List.of(conn))).isSameAs(expected);
        verify(specificationsService).getPowerBasedLoadCalculation(isNull(), isNull(), any());
    }
}
