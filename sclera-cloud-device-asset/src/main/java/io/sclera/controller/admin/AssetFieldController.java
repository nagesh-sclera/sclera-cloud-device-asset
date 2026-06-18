package io.sclera.controller.admin;

import io.sclera.dto.AssetFieldDTO;
import io.sclera.service.touchscreen.AssetFieldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * REST endpoints exposing the configurable asset (custom) fields for a VDMS.
 * Delegates to {@link AssetFieldService}.
 */
@RestController
@RequestMapping("/api/v1/sclera-cloud-device-asset-service/asset-fields")
public class AssetFieldController {

    private static final Logger log = LoggerFactory.getLogger(AssetFieldController.class);

    private final AssetFieldService assetFieldService;

    public AssetFieldController(AssetFieldService assetFieldService) {
        this.assetFieldService = assetFieldService;
    }

    /**
     * Returns all asset fields configured for the VDMS resolved from the request.
     *
     * @param httpServletRequest current request, used to resolve tenant/VDMS context
     * @return list of asset field definitions
     */
    @GetMapping
    public List<AssetFieldDTO> getAllAssetFields(HttpServletRequest httpServletRequest) {
        log.info("getAllAssetFields called");
        try {
            return assetFieldService.getAssetFields(httpServletRequest);
        } catch (Exception e) {
            log.error("getAllAssetFields failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
