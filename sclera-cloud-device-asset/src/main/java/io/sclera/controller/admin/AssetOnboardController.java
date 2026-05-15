package io.sclera.controller.admin;


import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.CorrigoConfigurationDTO;
import io.sclera.dto.DeviceOnboardStatusDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.service.AssetOnboardService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AssetOnboardController {

    @Autowired
    AssetOnboardService assetOnboardService;

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/addaionboardassets")
    public void addAssetOnboardedData(@PathVariable String username, @PathVariable String vdmsid, @RequestBody JSONObject asset_data) {
        assetOnboardService.addAssetOnboardedData(username, vdmsid, asset_data);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/updatecorrigoassets")
    public void updateCorrigoAssets(@PathVariable String username, @PathVariable String vdmsid,
                                    @RequestParam(defaultValue = "1") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize,
                                    @RequestParam(defaultValue = "null") String searchKey, @RequestBody CorrigoConfigurationDTO corrigo_configuration) throws JSONException {
        assetOnboardService.updateCorrigoAssets(username, vdmsid, pageNo, pageSize, searchKey, corrigo_configuration);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/upsertonboardassets")
    public void upsertOnboardAssets(@PathVariable String username, @PathVariable String vdmsid,
                                    @RequestBody JSONObject onboard_asset_data, HttpServletRequest httpServletRequest) {
        assetOnboardService.upsertOnboardAssets(username, vdmsid, onboard_asset_data, httpServletRequest);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/updateassetonboardstatus")
    public void updateAssetOnboardStatus(@PathVariable String username, @PathVariable String vdmsid,
                                         @RequestBody JSONObject onboard_asset_data, HttpServletRequest httpServletRequest) {
        assetOnboardService.updateAssetOnboardStatus(username, vdmsid, onboard_asset_data,httpServletRequest);
    }

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

    @RequestMapping(method = RequestMethod.POST, value = "/user/{username}/vdms/{vdmsid}/docker/{dockername}/getassetonboardcount")
    public Map<String, Integer> getAssetOnboardCount(@PathVariable String username, @PathVariable String vdmsid,
                                                     @PathVariable String dockername,
                                                     @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        return assetOnboardService.getAssetOnboardCount(username, vdmsid, dockername, search_sort_filter_details);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/getassetonboardassignees")
    public Set<String> getAssetOnboardAssignees(@PathVariable String username, @PathVariable String vdms_id) {
        return assetOnboardService.getAssetOnboardAssignees(username, vdms_id);
    }
    //------------------------------get call to fetch adc property details --------------------------------

    @RequestMapping(method = RequestMethod.GET, value = "/user/{username}/vdms/{vdms_id}/getpropertydetails")
    public VdmsDTO getPropertyDetails(@PathVariable String username, @PathVariable String vdms_id) {
        return assetOnboardService.getPropertyDetails(username, vdms_id);
    }

}
