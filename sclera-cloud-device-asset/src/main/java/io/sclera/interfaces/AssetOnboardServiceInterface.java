package io.sclera.interfaces;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.CorrigoConfigurationDTO;
import io.sclera.dto.DeviceDTO;
import io.sclera.dto.DeviceOnboardStatusDTO;
import io.sclera.dto.SpecificationsDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Service contract for {@link io.sclera.service.AssetOnboardService}. */
public interface AssetOnboardServiceInterface {
    void addAssetOnboardedData(String username, String vdmsid, JSONObject asset_data);

    List<SpecificationsDTO> updateDeviceSpecificationDetails(JSONArray specifications);

    void assetUpsert(DeviceDTO device, String vdmsid, String assetImportType, String username);

    void deleteAllRecords();

    void updateCorrigoAssets(String username, String vdmsid, Integer pageNo, Integer pageSize, String searchKey, CorrigoConfigurationDTO corrigo_configuration);

    void upsertOnboardAssets(String username, String vdmsid, JSONObject onboard_asset_data, HttpServletRequest httpServletRequest);

    void updateAssetOnboardStatus(String username, String vdmsid, JSONObject onboard_asset_data, HttpServletRequest httpServletRequest);

    void updateAssetOnboardData(String username, String vdmsid, String device_id, DeviceOnboardStatusDTO deviceOnboardStatusDTO, Integer onboard_status, HttpServletRequest httpServletRequest);

    Map<String, Integer> getAssetOnboardCount(String username, String vdmsid, String dockername, JSONObject search_sort_filter_details);

    Set<String> getAssetOnboardAssignees(String username, String vdms_id);

    VdmsDTO getPropertyDetails(String username, String vdmsId);
}
