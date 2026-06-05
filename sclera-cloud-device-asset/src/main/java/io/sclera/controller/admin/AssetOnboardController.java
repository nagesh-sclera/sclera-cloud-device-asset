package io.sclera.controller.admin;


import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.CorrigoConfigurationDTO;
import io.sclera.dto.DeviceOnboardStatusDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.service.AssetOnboardService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * REST endpoints for onboarding assets, syncing Corrigo assets and tracking onboard status.
 * Delegates all persistence and business logic to {@link AssetOnboardService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AssetOnboardController {

    @Autowired
    AssetOnboardService assetOnboardService;

    /**
     * Adds AI-onboarded asset data for the tenant.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param asset_data  asset data payload to add
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/addaionboardassets")
    public void addAssetOnboardedData(@PathVariable String username, @PathVariable String vdmsid, @RequestBody JSONObject asset_data) {
        assetOnboardService.addAssetOnboardedData(username, vdmsid, asset_data);
    }

    /**
     * Updates Corrigo assets for the tenant using the supplied configuration.
     *
     * @param username               owning user
     * @param vdmsid                 owning VDMS id
     * @param pageNo                 page number (default 1)
     * @param pageSize               page size (default 10)
     * @param searchKey              optional search filter (default "null")
     * @param corrigo_configuration  Corrigo configuration payload driving the update
     * @throws JSONException if the configuration payload cannot be parsed
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/updatecorrigoassets")
    public void updateCorrigoAssets(@PathVariable String username, @PathVariable String vdmsid,
                                    @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(defaultValue = "null") String searchKey, @RequestBody CorrigoConfigurationDTO corrigo_configuration) throws JSONException {
        assetOnboardService.updateCorrigoAssets(username, vdmsid, pageNo, pageSize, searchKey, corrigo_configuration);
    }

    /**
     * Creates or updates onboard asset data for the tenant.
     *
     * @param username            owning user
     * @param vdmsid              owning VDMS id
     * @param onboard_asset_data  onboard asset data payload to upsert
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertonboardassets")
    public void upsertOnboardAssets(@PathVariable String username, @PathVariable String vdmsid,
                                    @RequestBody JSONObject onboard_asset_data, HttpServletRequest httpServletRequest) {
        assetOnboardService.upsertOnboardAssets(username, vdmsid, onboard_asset_data, httpServletRequest);
    }

    /**
     * Updates the onboard status of assets for the tenant.
     *
     * @param username            owning user
     * @param vdmsid              owning VDMS id
     * @param onboard_asset_data  onboard asset data payload carrying the new status
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/updateassetonboardstatus")
    public void updateAssetOnboardStatus(@PathVariable String username, @PathVariable String vdmsid,
                                         @RequestBody JSONObject onboard_asset_data, HttpServletRequest httpServletRequest) {
        assetOnboardService.updateAssetOnboardStatus(username, vdmsid, onboard_asset_data,httpServletRequest);
    }

    /**
     * Updates onboard data for a specific device.
     *
     * @param username                owning user
     * @param vdmsid                  owning VDMS id
     * @param device_id               device whose onboard data is updated
     * @param deviceOnboardStatusDTO  onboard status payload to apply
     * @param httpServletRequest      current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/device/{device_id}/updateassetonboarddata")
    public void updateAssetOnboardData(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String device_id,
                                       @RequestBody DeviceOnboardStatusDTO deviceOnboardStatusDTO, HttpServletRequest httpServletRequest) {
        assetOnboardService.updateAssetOnboardData(username, vdmsid, device_id, deviceOnboardStatusDTO, null,httpServletRequest);
    }

//    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getassetonboardcount")
//    public Map<String,Integer> getAssetOnboardCount(@PathVariable String username, @PathVariable String vdmsid, @PathVariable String dockername){
//        return assetOnboardService.getAssetOnboardCount(username, vdmsid, dockername);
//
//    }

    /**
     * Returns onboard counts for a docker, filtered by the supplied search/sort criteria.
     *
     * @param username                    owning user
     * @param vdmsid                      owning VDMS id
     * @param dockername                  docker whose onboard counts are requested
     * @param search_sort_filter_details  search, sort and filter criteria payload
     * @return map of onboard category to count
     */
    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getassetonboardcount")
    public Map<String, Integer> getAssetOnboardCount(@PathVariable String username, @PathVariable String vdmsid,
                                                     @PathVariable String dockername,
                                                     @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        return assetOnboardService.getAssetOnboardCount(username, vdmsid, dockername, search_sort_filter_details);
    }

    /**
     * Returns the set of assignees available for asset onboarding.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @return set of assignee identifiers
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/getassetonboardassignees")
    public Set<String> getAssetOnboardAssignees(@PathVariable String username, @PathVariable String vdms_id) {
        return assetOnboardService.getAssetOnboardAssignees(username, vdms_id);
    }
    //------------------------------get call to fetch adc property details --------------------------------

    /**
     * Returns ADC property details for the tenant.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @return property details for the VDMS
     */
    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/getpropertydetails")
    public VdmsDTO getPropertyDetails(@PathVariable String username, @PathVariable String vdms_id) {
        return assetOnboardService.getPropertyDetails(username, vdms_id);
    }

}
