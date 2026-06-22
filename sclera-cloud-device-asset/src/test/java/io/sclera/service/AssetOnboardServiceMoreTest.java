package io.sclera.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.client.APICallClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Coverage for AssetOnboardService.addAssetOnboardedData - the temporary-product to device build
 * loop the main test deferred. The async onboarding tail (executor) is left unverified; this
 * exercises the synchronous device assembly including the location-resolution branch.
 */
@ExtendWith(MockitoExtension.class)
class AssetOnboardServiceMoreTest {

    @Mock APICallClient apiCallService;
    @Mock DeviceService deviceService;
    @Mock LocationService locationService;

    @InjectMocks AssetOnboardService service;

    private JSONArray oneTempProduct(String locationId) {
        JSONObject o = new JSONObject();
        o.put("specifications", "[{\"key\":\"ram\",\"value\":\"16GB\"}]");
        o.put("model", "ModelX");
        o.put("vendor", "Acme");
        o.put("description", "desc");
        o.put("image_url", "http://img");
        o.put("name", "Asset 1");
        if (locationId != null) o.put("location_id", locationId);
        JSONArray arr = new JSONArray();
        arr.add(o);
        return arr;
    }

    @Test
    void addAssetOnboardedData_buildsDevicesFromTemporaryProducts() {
        JSONObject assetData = new JSONObject();
        assetData.put("docker_name", "dock");
        assetData.put("location_id", "loc1"); // provided -> location branch skipped
        JSONArray tempIds = new JSONArray();
        tempIds.add("t1");
        assetData.put("temp_asset_ids", tempIds);
        when(apiCallService.getTemporaryProductByIds(any())).thenReturn(oneTempProduct("loc1"));

        service.addAssetOnboardedData("u", "v1", assetData);

        verify(apiCallService).getTemporaryProductByIds(any());
    }

    @Test
    void addAssetOnboardedData_nullLocation_resolvesViaLocationService() {
        JSONObject assetData = new JSONObject();
        assetData.put("docker_name", "dock");
        // no top-level location_id -> resolved from the product's own location_id
        JSONArray tempIds = new JSONArray();
        tempIds.add("t1");
        assetData.put("temp_asset_ids", tempIds);
        when(apiCallService.getTemporaryProductByIds(any())).thenReturn(oneTempProduct("loc2"));
        when(locationService.getLocationId("loc2")).thenReturn(1); // non-zero -> kept

        service.addAssetOnboardedData("u", "v1", assetData);

        verify(locationService).getLocationId("loc2");
    }
}
