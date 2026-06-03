package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.Repository.AssetRepository;
import io.sclera.Repository.VdmsRepository;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.SpecificationsDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
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
    void assetUpsert_delegatesWithDeviceFields() {
        DeviceDTO device = mock(DeviceDTO.class);
        when(device.getId()).thenReturn("d1");

        service.assetUpsert(device, "v1", "import", "user");

        verify(assetRepository).assetUpsert(eq("d1"), any(), any(), any(), any(), any(), any(), any(),
                eq(7), any(), any(), eq(""), any(), any(), eq(false), any(), eq("v1"), eq(0), eq("import"));
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
