package io.sclera.service;

import io.sclera.Repository.DeviceRepository;
import io.sclera.dto.AlertDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DeviceTopologyDTO;
import io.sclera.dto.DevicedataDTO;
import io.sclera.dto.ProductImagesDTO;
import io.sclera.dto.touchscreen.DeviceListDTO;
import io.sclera.stubs.InventoryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Representative unit coverage for a slice of the very large DeviceService: the repository-backed
 * "unique" lookups and simple delegates, the getDeviceCount status-map aggregation, and the pure
 * getDeviceObjectbyId list search. The bulk of DeviceService (file IO, topology, multi-collaborator
 * orchestration, async/SNMP/sensor flows) is intentionally out of scope for this slice.
 */
@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock DeviceRepository deviceRepository;
    @Mock InventoryClient inventoryClient;

    @InjectMocks DeviceService service;

    // ---- unique lookups / simple delegates -------------------------------

    @Test
    void getUniqueDeviceTypes_delegates() {
        List<String> types = List.of("Printer", "Sensor");
        when(deviceRepository.getUniqueDeviceTypes("net1", "f1")).thenReturn(types);
        assertThat(service.getUniqueDeviceTypes("net1", "f1")).isSameAs(types);
    }

    @Test
    void getUniqueAssetGroups_delegates() {
        List<String> groups = List.of("generic");
        when(deviceRepository.getUniqueAssetGroups("net1")).thenReturn(groups);
        assertThat(service.getUniqueAssetGroups("net1")).isSameAs(groups);
    }

    @Test
    void getUniqueCategory_delegates() {
        List<String> cats = List.of("HVAC");
        when(deviceRepository.getUniqueCategory("net1")).thenReturn(cats);
        assertThat(service.getUniqueCategory("net1")).isSameAs(cats);
    }

    @Test
    void getUniqueSubCategory_delegates() {
        List<String> subs = List.of("Chiller");
        when(deviceRepository.getUniqueSubCategory("net1", "HVAC")).thenReturn(subs);
        assertThat(service.getUniqueSubCategory("net1", "HVAC")).isSameAs(subs);
    }

    @Test
    void getUniqueAssignedUserEmail_delegates() {
        List<String> emails = List.of("a@x.com");
        when(deviceRepository.getUniqueAssignedUserEmail("v1", "net1")).thenReturn(emails);
        assertThat(service.getUniqueAssignedUserEmail("v1", "net1")).isSameAs(emails);
    }

    @Test
    void getDevicesByTypeCount_delegates() {
        Set<String> types = Set.of("Printer");
        when(deviceRepository.getDevicesByTypeCount("net1", "f1", types)).thenReturn("5");
        assertThat(service.getDevicesByTypeCount("net1", "f1", types)).isEqualTo("5");
    }

    @Test
    void listAlldevices_delegates() {
        List<DeviceDTO> devices = List.of(mock(DeviceDTO.class));
        when(deviceRepository.listAlldevices()).thenReturn(devices);
        assertThat(service.listAlldevices()).isSameAs(devices);
    }

    @Test
    void getAllDevices_delegates() {
        Set<DevicedataDTO> devices = Set.of(mock(DevicedataDTO.class));
        when(deviceRepository.getAllDevices()).thenReturn(devices);
        assertThat(service.getAllDevices()).isSameAs(devices);
    }

    @Test
    void getDeviceAlertInfoByDeviceId_delegates() {
        AlertDTO alert = mock(AlertDTO.class);
        when(deviceRepository.getDeviceAlertInfoByDeviceId("d1")).thenReturn(alert);
        assertThat(service.getDeviceAlertInfoByDeviceId("d1")).isSameAs(alert);
    }

    @Test
    void getDeviceCountForIOC_delegatesToDockerCount() {
        when(deviceRepository.getAllDeviceCountByDocker("dock")).thenReturn(42);
        assertThat(service.getDeviceCountForIOC("u", "v", "dock")).isEqualTo(42);
    }

    @Test
    void updateDevicesDisplayNameById_delegates() {
        service.updateDevicesDisplayNameById("d1", "Lobby Printer");
        verify(deviceRepository).updateDevicesDisplayNameById("d1", "Lobby Printer");
    }

    @Test
    void updateDeviceVendorById_delegates() {
        service.updateDeviceVendorById("d1", "HP");
        verify(deviceRepository).updateDeviceVendorById("d1", "HP");
    }

    @Test
    void unLinkVendorByVendorIdAndVdmsId_delegates() {
        service.unLinkVendorByVendorIdAndVdmsId("pa1", "v1");
        verify(deviceRepository).unLinkVendorByVendorIdAndVdmsId("pa1", "v1");
    }

    // ---- getDeviceCount aggregation --------------------------------------

    @Test
    void getDeviceCount_buildsStatusMap() {
        when(deviceRepository.onlineOfflineCountByDocker("dock", 1, "asg")).thenReturn(10);
        when(deviceRepository.onlineOfflineCountByDocker("dock", 0, "asg")).thenReturn(3);
        when(deviceRepository.unmonitorCountByDocker("dock", "asg")).thenReturn(2);
        when(deviceRepository.otherDeviceCountByDockerAssignee("dock", "asg")).thenReturn(1);
        when(deviceRepository.getAllDeviceCountByDockerAssignee("dock", "asg")).thenReturn(16);
        when(deviceRepository.getMatchedUnmatchedDeviceCountByDocker("dock", 3, "asg")).thenReturn(4);
        when(deviceRepository.getOnboardedDeviceCountByDocker("dock", "asg")).thenReturn(8);
        when(deviceRepository.getNotOnboardedDeviceCountByDocker("dock", "asg")).thenReturn(8);
        when(deviceRepository.monitorCountByDocker("dock", "asg")).thenReturn(13);
        when(deviceRepository.assignedCountByDocker("dock", "asg")).thenReturn(9);
        when(deviceRepository.unAssignedCountByDocker("dock", "asg")).thenReturn(7);

        Map<String, Integer> counts = service.getDeviceCount("u", "v", "dock", "asg");

        assertThat(counts)
                .containsEntry("online_device_count", 10)
                .containsEntry("offline_device_count", 3)
                .containsEntry("all_device_count", 16)
                .containsEntry("archived_device_count", 4)
                .containsEntry("assigned_device_count", 9)
                .containsEntry("unassigned_device_count", 7);
    }

    // ---- getDeviceObjectbyId pure search ---------------------------------

    @Test
    void getDeviceObjectbyId_match_returnsDevice() {
        DeviceTopologyDTO a = mock(DeviceTopologyDTO.class);
        when(a.getId()).thenReturn("d1");
        DeviceTopologyDTO b = mock(DeviceTopologyDTO.class);
        when(b.getId()).thenReturn("d2");

        assertThat(service.getDeviceObjectbyId(List.of(a, b), "d2")).isSameAs(b);
    }

    @Test
    void getDeviceObjectbyId_noMatch_returnsNull() {
        DeviceTopologyDTO a = mock(DeviceTopologyDTO.class);
        when(a.getId()).thenReturn("d1");
        assertThat(service.getDeviceObjectbyId(List.of(a), "zzz")).isNull();
    }

    @Test
    void getDeviceObjectbyId_nullList_returnsNull() {
        assertThat(service.getDeviceObjectbyId(null, "d1")).isNull();
    }

    // ---- image enrichment via InventoryClient ------------------------------

    @Test
    void listDevicesTs_enrichesDeviceListWithProductImages() {
        // Setup: one device "dev1" with product "prod1"
        DeviceListDTO device = new DeviceListDTO();
        device.setId("dev1");
        device.setDisplay_name("Device 1");

        // Mock repository.listDevicesTs to return the device
        when(deviceRepository.listDevicesTs("net1", "b1", "f1", "loc1", 1))
                .thenReturn(Set.of(device));

        // Mock repository.findDeviceProductIdRows to return device->product mapping
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"dev1", "prod1"});
        when(deviceRepository.findDeviceProductIdRows(Set.of("dev1")))
                .thenReturn(rows);

        // Mock inventoryClient.getProductImages to return image URL for prod1
        ProductImagesDTO images = new ProductImagesDTO("http://img1.jpg", null, null, null);
        when(inventoryClient.getProductImages(Set.of("prod1")))
                .thenReturn(Map.of("prod1", images));

        // Call the public method
        Set<DeviceListDTO> result = service.listDevicesTs("net1", "b1", "f1", "loc1", 1);

        // Assert the device was enriched with the image URL
        assertThat(result).hasSize(1);
        DeviceListDTO enriched = result.iterator().next();
        assertThat(enriched.getImage_url_1()).isEqualTo("http://img1.jpg");
    }
}
