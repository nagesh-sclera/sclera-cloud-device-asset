package io.sclera.controller.admin;

import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.service.impl.touchscreen.assetmapper.AssetMapperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints for the spreadsheet import wizard: upload a file (staged against a field mapping),
 * list the staged assets for preview, expose the Sclera asset fields for matching, and commit the
 * selected staged assets into real devices. Delegates to {@link AssetMapperService}.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
@Tag(name = "Asset Mapper", description = "Spreadsheet import wizard: upload, preview staged assets, list asset fields and commit assets into devices.")
public class AssetMapperController {

    private static final Logger log = LoggerFactory.getLogger(AssetMapperController.class);

    @Autowired
    private AssetMapperService assetMapperService;

    /**
     * Uploads and stages a spreadsheet using the supplied field mapping.
     *
     * @param file         spreadsheet file to import
     * @param fieldMapping serialized mapping of spreadsheet columns to asset fields
     * @param vdmsId       owning VDMS id
     * @return empty response indicating the upload was staged
     */
    @Operation(summary = "Upload and stage a spreadsheet",
            description = "Uploads a spreadsheet and stages its rows against the supplied field mapping for later preview and commit.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Spreadsheet staged"),
            @ApiResponse(responseCode = "400", description = "Invalid file or field mapping"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping(value = "/upload")
    public ResponseEntity<Void> upload(@RequestParam("file") MultipartFile file,
                                       @Parameter(description = "Serialized mapping of spreadsheet columns to asset fields") @RequestParam("fieldMapping") String fieldMapping,
                                       @Parameter(description = "Owning VDMS id") @RequestParam("vdms_id") String vdmsId) {
        log.info("upload vdms_id={} filename={}", vdmsId, file != null ? file.getOriginalFilename() : null);
        return assetMapperService.upload(file, fieldMapping, vdmsId);
    }

    /**
     * Returns a page of staged top-level assets for the preview screen.
     *
     * @param pageNo     page number to return (default 1)
     * @param pageSize   number of assets per page (default 50)
     * @param importType import source the staged assets belong to (default "spreadsheet")
     * @return page of staged top-level assets
     */
    @Operation(summary = "Get staged parent assets",
            description = "Returns a page of staged top-level assets for the import preview screen.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Staged assets returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/getSubSystemParentAssets")
    public ResponseEntity<List<AssetDTO>> getSubSystemParentAssets(
            @Parameter(description = "Page number to return") @RequestParam(defaultValue = "1") Integer pageNo,
            @Parameter(description = "Number of assets per page") @RequestParam(defaultValue = "50") Integer pageSize,
            @Parameter(description = "Import source the staged assets belong to") @RequestParam(defaultValue = "spreadsheet") String importType) {
        log.info("getSubSystemParentAssets pageNo={} pageSize={} importType={}", pageNo, pageSize, importType);
        return assetMapperService.getSubSystemParentAssets(pageNo, pageSize, importType);
    }

    /**
     * Returns the Sclera asset fields available as mapping targets.
     *
     * @return list of asset field name/label pairs
     */
    @Operation(summary = "List asset fields",
            description = "Returns the Sclera asset fields available as mapping targets.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Asset fields returned"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @GetMapping(value = "/getAssetFields")
    public ResponseEntity<List<Map<String, String>>> getAssetFields() {
        log.info("getAssetFields called");
        return assetMapperService.getAssetFields();
    }

    /**
     * Commits the selected staged assets into real devices.
     *
     * @param username    owning user
     * @param vdms_id     owning VDMS id
     * @param docker_name docker the devices are created under
     * @param asset_ids   staged asset ids to commit (all staged assets if omitted)
     * @param importType  import source the staged assets belong to (default "spreadsheet")
     * @param assignee    assignee to associate with the created devices (default "all")
     * @return result of the commit operation
     */
    @Operation(summary = "Commit staged assets into devices",
            description = "Commits the selected staged assets into real devices under the given docker.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assets committed"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "500", description = "Unexpected server error")
    })
    @PostMapping(value = "/user/{username}/vdms/{vdms_id}/docker/{docker_name}/saveAssets")
    public ResponseEntity<?> saveAssets(
            @Parameter(description = "Owning user") @PathVariable String username,
            @Parameter(description = "Owning VDMS id") @PathVariable String vdms_id,
            @Parameter(description = "Docker the devices are created under") @PathVariable String docker_name,
            @RequestBody(required = false) List<String> asset_ids,
            @Parameter(description = "Import source the staged assets belong to") @RequestParam(defaultValue = "spreadsheet") String importType,
            @Parameter(description = "Assignee to associate with the created devices") @RequestParam(defaultValue = "all") String assignee) {
        log.info("saveAssets username={} vdms_id={} docker_name={} importType={}", username, vdms_id, docker_name, importType);
        return assetMapperService.saveAssets(username, vdms_id, docker_name, asset_ids, importType, assignee);
    }
}
