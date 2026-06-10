package io.sclera.controller.admin;

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
public class DeviceMetadataController {

    private static final Logger log = LoggerFactory.getLogger(DeviceMetadataController.class);

    @PersistenceContext
    private EntityManager em;

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
