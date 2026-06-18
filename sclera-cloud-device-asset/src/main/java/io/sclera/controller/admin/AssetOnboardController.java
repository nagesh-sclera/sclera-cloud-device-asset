package io.sclera.controller.admin;


import com.alibaba.fastjson.JSONObject;
import io.sclera.dto.CorrigoConfigurationDTO;
import io.sclera.dto.DeviceOnboardStatusDTO;
import io.sclera.dto.touchscreen.settings.VdmsDTO;
import io.sclera.service.AssetOnboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Asset Onboarding", description = "Onboard assets, sync Corrigo assets and track asset onboard status for a VDMS.")
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
    @Operation(summary = "Add AI-onboarded asset data",
            description = "Adds AI-onboarded asset data for the tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset data added"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/addaionboardassets")
    public void addAssetOnboardedData(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody JSONObject asset_data) {
        log.info("addAssetOnboardedData username={} vdmsid={}", username, vdmsid);
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
    @Operation(summary = "Update Corrigo assets",
            description = "Updates Corrigo assets for the tenant using the supplied configuration. Results are scoped by the supplied paging and search criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Corrigo assets updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/updatecorrigoassets")
    public void updateCorrigoAssets(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "Optional search filter") @RequestParam(defaultValue = "null") String searchKey,
            @RequestBody CorrigoConfigurationDTO corrigo_configuration) throws JSONException {
        log.info("updateCorrigoAssets username={} vdmsid={} searchKey={}", username, vdmsid, searchKey);
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
    @Operation(summary = "Upsert onboard assets",
            description = "Creates or updates onboard asset data for the tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onboard assets upserted"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/upsertonboardassets")
    public void upsertOnboardAssets(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody JSONObject onboard_asset_data, HttpServletRequest httpServletRequest) {
        log.info("upsertOnboardAssets username={} vdmsid={}", username, vdmsid);
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
    @Operation(summary = "Update asset onboard status",
            description = "Updates the onboard status of assets for the tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onboard status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/updateassetonboardstatus")
    public void updateAssetOnboardStatus(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @RequestBody JSONObject onboard_asset_data, HttpServletRequest httpServletRequest) {
        log.info("updateAssetOnboardStatus username={} vdmsid={}", username, vdmsid);
        assetOnboardService.updateAssetOnboardStatus(username, vdmsid, onboard_asset_data, httpServletRequest);
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
    @Operation(summary = "Update asset onboard data for a device",
            description = "Updates onboard data for a specific device.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onboard data updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/device/{device_id}/updateassetonboarddata")
    public void updateAssetOnboardData(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Device whose onboard data is updated") @PathVariable String device_id,
            @RequestBody DeviceOnboardStatusDTO deviceOnboardStatusDTO, HttpServletRequest httpServletRequest) {
        log.info("updateAssetOnboardData username={} vdmsid={} device_id={}", username, vdmsid, device_id);
        assetOnboardService.updateAssetOnboardData(username, vdmsid, device_id, deviceOnboardStatusDTO, null, httpServletRequest);
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
    @Operation(summary = "Get asset onboard counts for a docker",
            description = "Returns onboard counts for a docker. Counts are filtered by the supplied search, sort and filter criteria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Onboard counts returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping("/docker/{dockername}/getassetonboardcount")
    public Map<String, Integer> getAssetOnboardCount(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdmsid,
            @Parameter(description = "Docker whose onboard counts are requested") @PathVariable String dockername,
            @RequestBody com.alibaba.fastjson.JSONObject search_sort_filter_details) {
        log.info("getAssetOnboardCount username={} vdmsid={} dockername={}", username, vdmsid, dockername);
        return assetOnboardService.getAssetOnboardCount(username, vdmsid, dockername, search_sort_filter_details);
    }

    /**
     * Returns the set of assignees available for asset onboarding.
     *
     * @param username  owning user
     * @param vdms_id   owning VDMS id
     * @return set of assignee identifiers
     */
    @Operation(summary = "Get asset onboard assignees",
            description = "Returns the set of assignees available for asset onboarding.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assignees returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getassetonboardassignees")
    public Set<String> getAssetOnboardAssignees(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id) {
        log.info("getAssetOnboardAssignees username={} vdms_id={}", username, vdms_id);
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
    @Operation(summary = "Get ADC property details",
            description = "Returns ADC property details for the tenant.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Property details returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getpropertydetails")
    public VdmsDTO getPropertyDetails(
            @Parameter(description = "Owning user") @RequestParam String username,
            @Parameter(description = "Owning VDMS id") @RequestParam String vdms_id) {
        log.info("getPropertyDetails username={} vdms_id={}", username, vdms_id);
        return assetOnboardService.getPropertyDetails(username, vdms_id);
    }

}
