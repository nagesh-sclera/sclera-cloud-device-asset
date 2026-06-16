package io.sclera.controller.admin;

import io.sclera.queryrepository.DeviceMetadataQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Distinct device-type / asset-group / category lookups to populate the asset
 * filter dropdowns (img_10). Returns ALL distinct values for the VDMS regardless
 * of monitor state, so the filter mirrors what the asset list actually shows
 * (the original getUniqueDeviceTypes query is restricted to monitor=1, which
 * hid types like Gateway / Power Source on unmonitored devices).
 */
@RestController
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class DeviceMetadataController {

    private static final Logger log = LoggerFactory.getLogger(DeviceMetadataController.class);

    @Autowired
    private DeviceMetadataQueryBuilder metadataQueryBuilder;

    @GetMapping("/getuniquedevicetypes")
    public List<String> getUniqueDeviceTypes(@RequestParam(required = false) String vdms_id,
                                             @RequestParam(required = false) String username,
                                             @RequestParam(required = false) String network_name,
                                             @RequestParam(required = false) String floor_id) {
        return distinct("type", vdms_id);
    }

    @GetMapping("/getuniqueassetgroups")
    public List<String> getUniqueAssetGroups(@RequestParam(required = false) String vdms_id,
                                             @RequestParam(required = false) String username,
                                             @RequestParam(required = false) String network_name) {
        return distinct("asset_group", vdms_id);
    }

    @GetMapping("/getuniquecategory")
    public List<String> getUniqueCategory(@RequestParam(required = false) String vdms_id,
                                          @RequestParam(required = false) String username,
                                          @RequestParam(required = false) String network_name) {
        return distinct("category", vdms_id);
    }

    // PG-port/Criteria: dynamic native SQL replaced by the type-safe DeviceMetadataQueryBuilder
    // (whitelisted column, bound VDMS scope). The error-swallow + empty fallback is preserved.
    @Transactional(readOnly = true)
    private List<String> distinct(String col, String vdmsId) {
        try {
            return metadataQueryBuilder.distinctColumnValues(col, vdmsId);
        } catch (Exception e) {
            log.warn("distinct({}) failed: {}", col, e.getMessage());
            return List.of();
        }
    }
}
