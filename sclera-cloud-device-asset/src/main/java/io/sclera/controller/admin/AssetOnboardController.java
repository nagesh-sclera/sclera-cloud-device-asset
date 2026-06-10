package io.sclera.controller.admin;


import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.CorrigoConfigurationDTO;
import io.sclera.dto.DeviceOnboardStatusDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.service.AssetOnboardService;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class AssetOnboardController {

    private static final Logger log = LoggerFactory.getLogger(AssetOnboardController.class);

    @Autowired
    AssetOnboardService assetOnboardService;

    /**
     * Adds AI-onboarded asset data for the tenant.
     *
     * @param username    owning user
     * @param vdmsid      owning VDMS id
     * @param asset_data  asset data payload to add
     */
    @RequestMapping(method = RequestMethod.POST, value = "/addaionboardassets")
    public void addAssetOnboardedData(@RequestParam String username, @RequestParam String vdmsid, @RequestBody JSONObject asset_data) {
        log.info("addAssetOnboardedData username={} vdmsid={}", username, vdmsid);
        try {
            assetOnboardService.addAssetOnboardedData(username, vdmsid, asset_data);
        } catch (Exception e) {
            log.error("addAssetOnboardedData failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/updatecorrigoassets")
    public void updateCorrigoAssets(@RequestParam String username, @RequestParam String vdmsid,
                                    @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(defaultValue = "null") String searchKey, @RequestBody CorrigoConfigurationDTO corrigo_configuration) throws JSONException {
        log.info("updateCorrigoAssets username={} vdmsid={} searchKey={}", username, vdmsid, searchKey);
        try {
            assetOnboardService.updateCorrigoAssets(username, vdmsid, pageNo, pageSize, searchKey, corrigo_configuration);
        } catch (Exception e) {
            log.error("updateCorrigoAssets failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Creates or updates onboard asset data for the tenant.
     *
     * @param username            owning user
     * @param vdmsid              owning VDMS id
     * @param onboard_asset_data  onboard asset data payload to upsert
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/upsertonboardassets")
    public void upsertOnboardAssets(@RequestParam String username, @RequestParam String vdmsid,
                                    @RequestBody JSONObject onboard_asset_data, HttpServletRequest httpServletRequest) {
        log.info("upsertOnboardAssets username={} vdmsid={}", username, vdmsid);
        try {
            assetOnboardService.upsertOnboardAssets(username, vdmsid, onboard_asset_data, httpServletRequest);
        } catch (Exception e) {
            log.error("upsertOnboardAssets failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Updates the onboard status of assets for the tenant.
     *
     * @param username            owning user
     * @param vdmsid              owning VDMS id
     * @param onboard_asset_data  onboard asset data payload carrying the new status
     * @param httpServletRequest  current request, used to resolve tenant/VDMS context
     */
    @RequestMapping(method = RequestMethod.POST, value = "/updateassetonboardstatus")
    public void updateAssetOnboardStatus(@RequestParam String username, @RequestParam String vdmsid,
                                         @RequestBody JSONObject onboard_asset_data, HttpServletRequest httpServletRequest) {
        log.info("updateAssetOnboardStatus username={} vdmsid={}", username, vdmsid);
        try {
            assetOnboardService.updateAssetOnboardStatus(username, vdmsid, onboard_asset_data,httpServletRequest);
        } catch (Exception e) {
            log.error("updateAssetOnboardStatus failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/device/{device_id}/updateassetonboarddata")
    public void updateAssetOnboardData(@RequestParam String username, @RequestParam String vdmsid, @PathVariable String device_id,
                                       @RequestBody DeviceOnboardStatusDTO deviceOnboardStatusDTO, HttpServletRequest httpServletRequest) {
        log.info("updateAssetOnboardData username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        try {
            assetOnboardService.updateAssetOnboardData(username, vdmsid, device_id, deviceOnboardStatusDTO, null,httpServletRequest);
        } catch (Exception e) {
            log.error("updateAssetOnboardData failed device_id={}: {}", device_id, e.getMessage(), e);
            throw e;
        }
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
    @RequestMapping(method = RequestMethod.POST, value = "/docker/{dockername}/getassetonboardcount")
    public Map<String, Integer> getAssetOnboardCount(@RequestParam String username, @RequestParam String vdmsid,
                                                     @PathVariable String dockername,
                                                     @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        log.info("getAssetOnboardCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        try {
            return assetOnboardService.getAssetOnboardCount(username, vdmsid, dockername, search_sort_filter_details);
        } catch (Exception e) {
            log.error("getAssetOnboardCount failed dockername={}: {}", dockername, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Returns the set of assignees available for asset onboarding.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @return set of assignee identifiers
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getassetonboardassignees")
    public Set<String> getAssetOnboardAssignees(@RequestParam String username, @RequestParam String vdms_id) {
        log.info("getAssetOnboardAssignees username={} vdms_id={}", username, vdms_id);
        try {
            return assetOnboardService.getAssetOnboardAssignees(username, vdms_id);
        } catch (Exception e) {
            log.error("getAssetOnboardAssignees failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }
    //------------------------------get call to fetch adc property details --------------------------------

    /**
     * Returns ADC property details for the tenant.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @return property details for the VDMS
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getpropertydetails")
    public VdmsDTO getPropertyDetails(@RequestParam String username, @RequestParam String vdms_id) {
        log.info("getPropertyDetails username={} vdms_id={}", username, vdms_id);
        try {
            return assetOnboardService.getPropertyDetails(username, vdms_id);
        } catch (Exception e) {
            log.error("getPropertyDetails failed username={}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

}
