package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.AssetRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.SpecificationsDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit coverage for AssetOnboardService: the pure JSON specification mapper, the asset-upsert and
 * onboard delegates, and property lookup. The executor-backed onboarding and Corrigo sync methods
 * are deferred (async/API-heavy).
 */
@ExtendWith(MockitoExtension.class)
class AssetOnboardServiceTest {

    @Mock DeviceService deviceService;
    @Mock AssetRepository assetRepository;
    @Mock VdmsRepository vdmsRepository;
    @Mock EntityManager entityManager;

    @InjectMocks AssetOnboardService service;

    // ---- updateDeviceSpecificationDetails --------------------------------

    @Test
    void updateDeviceSpecificationDetails_empty_returnsEmpty() {
        assertThat(service.updateDeviceSpecificationDetails(new JSONArray())).isEmpty();
    }

    @Test
    void updateDeviceSpecificationDetails_mapsKeyValue() {
        JSONObject spec = new JSONObject();
        spec.put("key", "RAM");
        spec.put("value", "16GB");
        JSONArray specs = new JSONArray();
        specs.add(spec);

        List<SpecificationsDTO> result = service.updateDeviceSpecificationDetails(specs);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKey_name()).isEqualTo("RAM");
        assertThat(result.get(0).getKey_value()).isEqualTo("16GB");
    }

    // ---- assetUpsert / deleteAllRecords ----------------------------------

    @Test
    void assetUpsert_insertPath_savesNewAssetWithDefaults() {
        DeviceDTO device = mock(DeviceDTO.class);
        when(device.getId()).thenReturn("d1");
        when(device.getType()).thenReturn("pump");
        when(assetRepository.findById("d1")).thenReturn(java.util.Optional.empty());

        service.assetUpsert(device, "v1", "import", "user");

        org.mockito.ArgumentCaptor<io.sclera.models.Asset> cap =
                org.mockito.ArgumentCaptor.forClass(io.sclera.models.Asset.class);
        verify(assetRepository).save(cap.capture());
        io.sclera.models.Asset saved = cap.getValue();
        assertThat(saved.getId()).isEqualTo("d1");
        assertThat(saved.getType()).isEqualTo("pump");
        assertThat(saved.getNetwork_layer()).isEqualTo(7);
        assertThat(saved.getImport_type()).isEqualTo("import");
        assertThat(saved.getIsMatched()).isFalse();
    }

    @Test
    void assetUpsert_conflictPath_updatesOnlyNameDescType() {
        DeviceDTO device = mock(DeviceDTO.class);
        when(device.getId()).thenReturn("d1");
        when(device.getUser_data_name()).thenReturn("Renamed");
        when(device.getDescription()).thenReturn("d2");
        when(device.getType()).thenReturn("t2");

        io.sclera.models.Asset existing = new io.sclera.models.Asset();
        existing.setId("d1");
        existing.setNetwork_layer(7);
        existing.setImport_type("corrigo");
        when(assetRepository.findById("d1")).thenReturn(java.util.Optional.of(existing));

        service.assetUpsert(device, "v1", "import", "user");

        org.mockito.ArgumentCaptor<io.sclera.models.Asset> cap =
                org.mockito.ArgumentCaptor.forClass(io.sclera.models.Asset.class);
        verify(assetRepository).save(cap.capture());
        io.sclera.models.Asset saved = cap.getValue();
        assertThat(saved.getDisplay_name()).isEqualTo("Renamed");
        assertThat(saved.getType()).isEqualTo("t2");
        assertThat(saved.getImport_type()).isEqualTo("corrigo"); // untouched on conflict
        assertThat(saved.getNetwork_layer()).isEqualTo(7);       // untouched on conflict
    }

    @Test
    void deleteAllRecords_delegates() {
        service.deleteAllRecords();
        verify(assetRepository).deleteAllRecords();
    }

    // ---- onboard delegates -----------------------------------------------

    @Test
    void upsertOnboardAssets_delegates() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        JSONObject data = new JSONObject();
        service.upsertOnboardAssets("user", "v1", data, req);
        verify(deviceService).upsertOnboardAssets("user", "v1", data);
    }

    @Test
    void updateAssetOnboardStatus_delegates() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        JSONObject data = new JSONObject();
        service.updateAssetOnboardStatus("user", "v1", data, req);
        verify(deviceService).updateAssetOnboardStatus("user", "v1", data);
    }

    @Test
    void getAssetOnboardCount_delegates() {
        JSONObject details = new JSONObject();
        Map<String, Integer> counts = Map.of("onboarded", 3);
        when(deviceService.getAssetOnboardCount("user", "v1", "dock", details)).thenReturn(counts);
        assertThat(service.getAssetOnboardCount("user", "v1", "dock", details)).isSameAs(counts);
    }

    @Test
    void getAssetOnboardAssignees_delegates() {
        Set<String> assignees = Set.of("a@x.com");
        when(deviceService.getAssetOnboardAssignees("user", "v1")).thenReturn(assignees);
        assertThat(service.getAssetOnboardAssignees("user", "v1")).isSameAs(assignees);
    }

    @Test
    void getPropertyDetails_delegatesToVdmsRepository() {
        VdmsDTO vdms = mock(VdmsDTO.class);
        when(vdmsRepository.getSyncDetailsForADC()).thenReturn(vdms);
        assertThat(service.getPropertyDetails("user", "v1")).isSameAs(vdms);
    }
}
