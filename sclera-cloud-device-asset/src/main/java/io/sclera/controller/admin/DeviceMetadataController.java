package io.sclera.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Tag(name = "Device Metadata", description = "Distinct device-type, asset-group and category lookups for the asset filter dropdowns.")
public class DeviceMetadataController {

    private static final Logger log = LoggerFactory.getLogger(DeviceMetadataController.class);

    @PersistenceContext
    private EntityManager em;

    /**
     * Returns the distinct device types for the VDMS.
     *
     * @param vdms_id      owning VDMS id
     * @param username     acting user (unused filter context)
     * @param network_name network filter context (unused)
     * @param floor_id     floor filter context (unused)
     * @return distinct device type values
     */
    @Operation(summary = "Get unique device types",
            description = "Returns the distinct device types for the VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Device types returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getuniquedevicetypes")
    public List<String> getUniqueDeviceTypes(
            @Parameter(description = "Owning VDMS id") @RequestParam(required = false) String vdms_id,
            @Parameter(description = "Acting user") @RequestParam(required = false) String username,
            @Parameter(description = "Network filter context") @RequestParam(required = false) String network_name,
            @Parameter(description = "Floor filter context") @RequestParam(required = false) String floor_id) {
        log.info("getUniqueDeviceTypes vdms_id={}", vdms_id);
        return distinct("type", vdms_id);
    }

    /**
     * Returns the distinct asset groups for the VDMS.
     *
     * @param vdms_id      owning VDMS id
     * @param username     acting user (unused filter context)
     * @param network_name network filter context (unused)
     * @return distinct asset group values
     */
    @Operation(summary = "Get unique asset groups",
            description = "Returns the distinct asset groups for the VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset groups returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getuniqueassetgroups")
    public List<String> getUniqueAssetGroups(
            @Parameter(description = "Owning VDMS id") @RequestParam(required = false) String vdms_id,
            @Parameter(description = "Acting user") @RequestParam(required = false) String username,
            @Parameter(description = "Network filter context") @RequestParam(required = false) String network_name) {
        log.info("getUniqueAssetGroups vdms_id={}", vdms_id);
        return distinct("asset_group", vdms_id);
    }

    /**
     * Returns the distinct categories for the VDMS.
     *
     * @param vdms_id      owning VDMS id
     * @param username     acting user (unused filter context)
     * @param network_name network filter context (unused)
     * @return distinct category values
     */
    @Operation(summary = "Get unique categories",
            description = "Returns the distinct categories for the VDMS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping("/getuniquecategory")
    public List<String> getUniqueCategory(
            @Parameter(description = "Owning VDMS id") @RequestParam(required = false) String vdms_id,
            @Parameter(description = "Acting user") @RequestParam(required = false) String username,
            @Parameter(description = "Network filter context") @RequestParam(required = false) String network_name) {
        log.info("getUniqueCategory vdms_id={}", vdms_id);
        return distinct("category", vdms_id);
    }

    // col is from a fixed whitelist below — never user input — so the inlined
    // column name is safe.
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    private List<String> distinct(String col, String vdmsId) {
        if (!col.equals("type") && !col.equals("asset_group") && !col.equals("category")) {
            return List.of();
        }
        try {
            StringBuilder sql = new StringBuilder("SELECT DISTINCT d.").append(col)
                    .append(" FROM device d WHERE d.").append(col)
                    .append(" IS NOT NULL AND d.").append(col).append(" <> '' ")
                    .append("AND (d.asset_match_status IS NULL OR d.asset_match_status <> 3)");
            boolean scoped = vdmsId != null && !vdmsId.isBlank();
            if (scoped) sql.append(" AND d.docker_vdms_id = :v");
            sql.append(" ORDER BY 1");
            var q = em.createNativeQuery(sql.toString());
            if (scoped) q.setParameter("v", vdmsId);
            return q.getResultList();
        } catch (Exception e) {
            log.warn("distinct({}) failed: {}", col, e.getMessage());
            return List.of();
        }
    }
}
