package io.sclera.service;

import io.sclera.Repository.SpecificationsRepository;
import io.sclera.dto.ConnectedDevicesDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.SpecificationsDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for the clean main methods of SpecificationsService, including the pure
 * power-calculation logic. The heavy orchestration methods (tagPowerSources and the full
 * getPowerBasedLoadCalculation chain) are deferred to a focused round.
 */
@ExtendWith(MockitoExtension.class)
class SpecificationsServiceTest {

    @Mock SpecificationsRepository specificationsRepository;
    @Mock ConnectedDevicesService connectedDevicesService;

    @InjectMocks SpecificationsService service;

    private SpecificationsDTO spec(String id, String deviceId, String keyName, String keyValue) {
        SpecificationsDTO s = new SpecificationsDTO();
        s.setId(id);
        s.setDevice_id(deviceId);
        s.setKey_name(keyName);
        s.setKey_value(keyValue);
        return s;
    }

    // ---- delegates / loops -----------------------------------------------

    @Test
    void editDeviceSpecifications_delegatesPerItem() {
        SpecificationsDTO s = spec("s1", "d1", "Power", "10");
        s.setKey_unit("W");
        service.editDeviceSpecifications("u", "v", List.of(s));
        verify(specificationsRepository).editDeviceSpecifications("s1", "10", "W", "Power");
    }

    @Test
    void checkSpecificationByDeviceId_delegates() {
        when(specificationsRepository.checkSpecificationByDeviceId("vd", "K")).thenReturn(3);
        assertThat(service.checkSpecificationByDeviceId("vd", "K")).isEqualTo(3);
    }

    @Test
    void getDeviceSpecificationsByDeviceId_delegates() {
        List<SpecificationsDTO> list = List.of(spec("s1", "d1", "K", "v"));
        when(specificationsRepository.getDeviceSpecificationsBasedOnDeviceId("d1")).thenReturn(list);
        assertThat(service.getDeviceSpecificationsByDeviceId("u", "v", "d1")).isSameAs(list);
    }

    @Test
    void getDeviceSpecificationsBasedOnDeviceIdAndKeyName_delegates() {
        SpecificationsDTO s = spec("s1", "d1", "K", "v");
        when(specificationsRepository.getDeviceSpecificationsBasedOnDeviceIdAndKeyName("d1", "K")).thenReturn(s);
        assertThat(service.getDeviceSpecificationsBasedOnDeviceIdAndKeyName("d1", "K")).isSameAs(s);
    }

    @Test
    void checkSpecificationsAdded_delegatesWithSwappedArgs() {
        when(specificationsRepository.checkSpecificationByDeviceId("d1", "K")).thenReturn(2);
        assertThat(service.checkSpecificationsAdded("K", "d1")).isEqualTo(2);
    }

    @Test
    void untagPowerSource_delegatesPerItem() {
        SpecificationsDTO s = spec("s1", "d1", "K", "v");
        s.setConnected_specifications_id("cs1");
        service.untagPowerSource("u", "v", List.of(s));
        verify(connectedDevicesService).untagPowerSource("s1", "cs1");
    }

    @Test
    void untagDevice_delegatesPerItemWithSwappedArgs() {
        SpecificationsDTO s = spec("s1", "d1", "K", "v");
        s.setConnected_specifications_id("cs1");
        service.untagDevice("u", "v", List.of(s));
        verify(connectedDevicesService).untagDevice("cs1", "s1");
    }

    @Test
    void deleteSpecifications_deletesConnectedThenById() {
        SpecificationsDTO s = spec("s1", "d1", "K", "v");
        service.deleteSpecifications("u", "v", List.of(s));
        verify(connectedDevicesService).deleteConnectedDevicesBySpecificationId("s1");
        verify(specificationsRepository).deleteById("s1");
    }

    @Test
    void getTaggedPowerSourcesByDeviceId_delegates() {
        List<DeviceDTO> devices = List.of(mock(DeviceDTO.class));
        when(connectedDevicesService.getTaggedPowerSourcesByDeviceId("u", "v", "d1")).thenReturn(devices);
        assertThat(service.getTaggedPowerSourcesByDeviceId("u", "v", "d1")).isSameAs(devices);
    }

    // ---- upsertDeviceSpecification (id-generation branch) -----------------

    @Test
    void upsertDeviceSpecification_generatesIdWhenNull() {
        SpecificationsDTO s = spec(null, "d1", "K", "v");
        s.setKey_unit("u");

        service.upsertDeviceSpecification(s);

        assertThat(s.getId()).isNotNull();
        verify(specificationsRepository).upsertDeviceSpecification(anyString(), eq("K"), eq("v"), eq("u"), eq("d1"));
    }

    @Test
    void upsertDeviceSpecification_usesProvidedId() {
        SpecificationsDTO s = spec("s1", "d1", "K", "v");
        s.setKey_unit("u");

        service.upsertDeviceSpecification(s);

        verify(specificationsRepository).upsertDeviceSpecification(eq("s1"), eq("K"), eq("v"), eq("u"), eq("d1"));
    }

    // ---- getTaggedDevices branch -----------------------------------------

    @Test
    void getTaggedDevices_nullSpec_returnsNull() {
        assertThat(service.getTaggedDevices("u", "v", null, 1, 10)).isNull();
    }

    @Test
    void getTaggedDevices_withSpec_computesOffsetAndDelegates() {
        SpecificationsDTO s = spec("s1", "d1", "K", "v");
        List<DeviceDTO> devices = List.of(mock(DeviceDTO.class));
        // pageno=2, pagesize=10 -> offset = 10 * (2 - 1) = 10
        when(connectedDevicesService.getConnectedDevicesSpecifications("s1", 10, 10)).thenReturn(devices);

        assertThat(service.getTaggedDevices("u", "v", s, 2, 10)).isSameAs(devices);
        verify(connectedDevicesService).getConnectedDevicesSpecifications("s1", 10, 10);
    }

    // ---- addConnectedDevices ---------------------------------------------

    @Test
    void addConnectedDevices_buildsDtoAndDelegates() {
        SpecificationsDTO s = spec("s1", "d1", "K", "v");
        s.setConnected_specifications_id("cs1");
        service.addConnectedDevices(s);
        verify(connectedDevicesService).addConnectedDevices(any(ConnectedDevicesDTO.class));
    }

    // ---- power calculation (pure-ish) ------------------------------------

    @Test
    void getTotalPowerWithHeadRoom_removes20Percent() {
        assertThat(service.getTotalPowerWithHeadRoom(100.0)).isCloseTo(80.0, within(1e-6));
    }

    @Test
    void calculatePower_validPowerValue_parsed() {
        SpecificationsDTO s = spec("s1", "d1", "A Power", "100");
        assertThat(service.calculatePower(s)).isEqualTo(100.0);
    }

    @Test
    void calculatePower_nullSpec_returnsZero() {
        assertThat(service.calculatePower(null)).isEqualTo(0.0);
    }

    @Test
    void calculatePower_nonPowerKey_returnsZero() {
        SpecificationsDTO s = spec("s1", "d1", "A Voltage", "100");
        assertThat(service.calculatePower(s)).isEqualTo(0.0);
    }

    @Test
    void calculatePower_emptyValue_returnsZero() {
        SpecificationsDTO s = spec("s1", "d1", "A Power", "");
        assertThat(service.calculatePower(s)).isEqualTo(0.0);
    }

    @Test
    void getPowerDetails_usesPortPrefixAndDelegates() {
        SpecificationsDTO s = spec("s1", "d1", "Port 1 Power", "100");
        when(connectedDevicesService.getPortPattern(anyString())).thenReturn("Port 1");
        when(specificationsRepository.getPower("d1", "Port 1 Power")).thenReturn(s);

        assertThat(service.getPowerDetails("d1", "Port 1 Voltage")).isSameAs(s);
        verify(specificationsRepository).getPower("d1", "Port 1 Power");
    }

    @Test
    void calculateConsumedPower_returnsParsedPowerFromDetails() {
        SpecificationsDTO s = spec("s1", "d1", "Port 1 Power", "100");
        when(connectedDevicesService.getPortPattern(anyString())).thenReturn("Port 1");
        when(specificationsRepository.getPower("d1", "Port 1 Power")).thenReturn(s);

        assertThat(service.calculateConsumedPower("Port 1 Power", "d1")).isEqualTo(100.0);
    }

    @Test
    void getAllConnectedDevices_emptyList_returnsZero() {
        when(connectedDevicesService.getAllConnectedDevicesForLoadCalculation("s1")).thenReturn(List.of());
        assertThat(service.getAllConnectedDevices("s1")).isEqualTo(0.0);
    }

    // ---- upsertDeviceSpecifications branches ------------------------------

    @Test
    void upsertDeviceSpecifications_newSpec_upsertsAndReturnsDeviceSpecs() {
        SpecificationsDTO incoming = spec(null, "d1", "K", "v");
        List<SpecificationsDTO> deviceSpecs = List.of(spec("s9", "d1", "K", "v"));
        when(specificationsRepository.checkSpecificationByDeviceId("d1", "K")).thenReturn(0);
        when(specificationsRepository.getDeviceSpecificationsBasedOnDeviceId("d1")).thenReturn(deviceSpecs);

        List<SpecificationsDTO> result = service.upsertDeviceSpecifications("u", "v", List.of(incoming));

        assertThat(result).isSameAs(deviceSpecs);
        verify(specificationsRepository).upsertDeviceSpecification(anyString(), eq("K"), eq("v"), any(), eq("d1"));
    }

    @Test
    void upsertDeviceSpecifications_existingWithNullId_returnsNull() {
        SpecificationsDTO incoming = spec(null, "d1", "K", "v"); // id null + already exists -> skipped
        when(specificationsRepository.checkSpecificationByDeviceId("d1", "K")).thenReturn(1);

        assertThat(service.upsertDeviceSpecifications("u", "v", List.of(incoming))).isNull();
    }
}
