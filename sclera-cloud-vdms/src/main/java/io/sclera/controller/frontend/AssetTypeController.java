package io.sclera.controller.frontend;


import io.sclera.dto.CategoryDTO;
import io.sclera.service.AssetTypeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AssetTypeController {

    @Autowired
    private AssetTypeService assetTypeService;

    @GetMapping(value = "/getAssetTypes")
    public ResponseEntity<?> getAssetType(@RequestParam(required = false, defaultValue = "all") String assetTypeGroupName,
                                          @RequestParam(required = false, defaultValue = "all") String key,
                                          @RequestParam(required = false) String sort,
                                          @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                          @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int pageSize,
                                          @RequestParam(name = "loggedInUser") String loggedInUser,
                                          HttpServletRequest httpServletRequest) {
        return assetTypeService.getAssetType(assetTypeGroupName, key, sort, pageNo, pageSize, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/assetTypes/addAssetType")
    public ResponseEntity<?> addAssetType(@RequestParam(value = "body") String body,
                                          @RequestParam(value = "icon", required = false) MultipartFile icon,
                                          @RequestParam(name = "loggedInUser") String loggedInUser,
                                          HttpServletRequest httpServletRequest) throws IOException {
        return assetTypeService.addAssetType(body, icon, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/assetTypes/addBulkAssetType")
    public ResponseEntity<?> addBulkAssetType(@RequestBody List<CategoryDTO> categoryDTOS,
                                              @RequestParam(name = "loggedInUser") String loggedInUser,
                                              HttpServletRequest httpServletRequest) throws IOException {
        return assetTypeService.addBulkAssetType(categoryDTOS, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/assetTypes/{assetTypeId}")
    public ResponseEntity<?> updateAssetType(@PathVariable String assetTypeId,
                                             @RequestParam(value = "body") String body,
                                             @RequestParam(value = "icon", required = false) MultipartFile icon,
                                             @RequestParam(name = "loggedInUser") String loggedInUser,
                                             @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                             HttpServletRequest httpServletRequest) throws IOException {
        return assetTypeService.updateAssetType(assetTypeId, body, icon, loggedInUser,iconUrl, httpServletRequest);
    }

    @PutMapping(value = "/assetTypeGroup/{assetTypeGroupName}/assetTypes/{assetTypeId}/icon")
    public ResponseEntity<?> updateAssetTypeIcon(@PathVariable String assetTypeGroupName, @PathVariable String assetTypeId,
                                                 @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                 @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                 @RequestParam(name = "loggedInUser") String loggedInUser,
                                                 HttpServletRequest httpServletRequest) throws IOException {
        return assetTypeService.updateAssetTypeIconByAssetTypeGroupAndAssetTypeId(assetTypeGroupName, assetTypeId, iconUrl, icon, loggedInUser, httpServletRequest);
    }

    @DeleteMapping(value = "/assetTypes/deleteAssetTypes")
    public ResponseEntity<?> deleteAssetType(@RequestBody List<String> assetTypeIds,
                                             @RequestParam(name = "loggedInUser") String loggedInUser,
                                             HttpServletRequest httpServletRequest) {
        return assetTypeService.deleteAssetType(assetTypeIds, loggedInUser, httpServletRequest);
    }
    @GetMapping(value = "/getAllAssetTypeNames")
    public ResponseEntity<?> getAllAssetTypeNames(HttpServletRequest httpServletRequest) {
        return assetTypeService.getAllAssetTypeNames( httpServletRequest);
    }
}
