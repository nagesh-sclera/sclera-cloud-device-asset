package io.sclera.controller.admin;

import io.sclera.dto.touchscreen.assetmapper.AssetDTO;
import io.sclera.service.touchscreen.assetmapper.AssetMapperService;
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
public class AssetMapperController {

    private static final Logger log = LoggerFactory.getLogger(AssetMapperController.class);

    @Autowired
    private AssetMapperService assetMapperService;

    /** Uploads and stages a spreadsheet using the supplied field mapping. */
    @PostMapping(value = "/upload")
    public ResponseEntity<Void> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam("fieldMapping") String fieldMapping,
                                       @RequestParam("vdms_id") String vdmsId) {
        log.info("upload vdms_id={} filename={}", vdmsId, file != null ? file.getOriginalFilename() : null);
        return assetMapperService.upload(file, fieldMapping, vdmsId);
    }

    /** Returns a page of staged top-level assets for the preview screen. */
    @GetMapping(value = "/getSubSystemParentAssets")
    public ResponseEntity<List<AssetDTO>> getSubSystemParentAssets(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "50") Integer pageSize,
            @RequestParam(defaultValue = "spreadsheet") String importType) {
        return assetMapperService.getSubSystemParentAssets(pageNo, pageSize, importType);
    }

    /** Returns the Sclera asset fields available as mapping targets. */
    @GetMapping(value = "/getAssetFields")
    public ResponseEntity<List<Map<String, String>>> getAssetFields() {
        return assetMapperService.getAssetFields();
    }

    /** Commits the selected staged assets into real devices. */
    @PostMapping(value = "/user/{username}/vdms/{vdms_id}/docker/{docker_name}/saveAssets")
    public ResponseEntity<?> saveAssets(@PathVariable String username,
                                        @PathVariable String vdms_id,
                                        @PathVariable String docker_name,
                                        @RequestBody(required = false) List<String> asset_ids,
                                        @RequestParam(defaultValue = "spreadsheet") String importType,
                                        @RequestParam(defaultValue = "all") String assignee) {
        return assetMapperService.saveAssets(username, vdms_id, docker_name, asset_ids, importType, assignee);
    }
}
