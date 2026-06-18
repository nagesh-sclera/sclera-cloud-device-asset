package io.sclera.controller.admin;

import io.sclera.dto.AssetFieldDTO;
import io.sclera.service.touchscreen.AssetFieldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Asset Fields", description = "Expose the configurable asset (custom) fields for a VDMS.")
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
    @Operation(summary = "List asset fields",
            description = "Returns all asset fields configured for the VDMS resolved from the request.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset fields returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping
    public List<AssetFieldDTO> getAllAssetFields(HttpServletRequest httpServletRequest) {
        log.info("getAllAssetFields called");
        return assetFieldService.getAssetFields(httpServletRequest);
    }
}
