package io.sclera.controller.frontend;


import io.sclera.service.AssetSubCategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categories/{categoryId}/subCategories")
public class AssetSubCategoryController {

    @Autowired
    private AssetSubCategoryService assetSubCategoryService;

    @GetMapping
    public ResponseEntity<?> getSubCategoryByCategoryId(@PathVariable String categoryId,
                                                        @RequestParam(required = false, defaultValue = "all") String key,
                                                        @RequestParam(required = false) String sort,
                                                        @RequestParam(name = "loggedInUser") String loggedInUser,
                                                        HttpServletRequest httpServletRequest) {
        return assetSubCategoryService.getSubCategoryByCategoryId(categoryId, key, sort, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/addSubCategoryByCategoryId")
    public ResponseEntity<?> addSubCategoryByCategoryId(@PathVariable String categoryId,
                                                        @RequestParam(value = "body") String body,
                                                        @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                        @RequestParam(name = "loggedInUser") String loggedInUser,
                                                        HttpServletRequest httpServletRequest) throws IOException {
        return assetSubCategoryService.addSubCategoryByCategoryId(categoryId, body, icon, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/{subCategoryId}")
    public ResponseEntity<?> updateSubCategoryByCategoryAndSubcategoryId(@PathVariable String categoryId, @PathVariable String subCategoryId,
                                                                         @RequestParam(value = "body") String body,
                                                                         @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                                         @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                         @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                                         HttpServletRequest httpServletRequest) throws IOException {
        return assetSubCategoryService.updateSubCategoryByCategoryAndSubcategoryId(categoryId, subCategoryId, body, icon, loggedInUser,iconUrl, httpServletRequest);
    }

    @PutMapping(value = "/{subCategoryId}/icon")
    public ResponseEntity<?> updateSubCategoryIconByCategoryAndSubcategoryId(@PathVariable String categoryId, @PathVariable String subCategoryId,
                                                                             @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                                             @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                                             @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                             HttpServletRequest httpServletRequest) throws IOException {
        return assetSubCategoryService.updateSubCategoryIconByCategoryAndSubcategoryId(categoryId, subCategoryId, iconUrl, icon, loggedInUser, httpServletRequest);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteSubcategoryByCategoryAndSubcategoryId(@PathVariable String categoryId, @RequestBody List<String> subCategoryIds,
                                                                         @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                         HttpServletRequest httpServletRequest) {
        return assetSubCategoryService.deleteSubcategoryByCategoryAndSubcategoryId(categoryId, subCategoryIds, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/updateBulkSubCategoryIcon")
    public ResponseEntity<?> updateBulkSubCategoryIcon(@PathVariable String categoryId,
                                                       @RequestParam(value = "icon", required = false) List<MultipartFile> icons,
                                                       @RequestParam(name = "loggedInUser") String loggedInUser,
                                                       HttpServletRequest httpServletRequest) throws IOException {
        return assetSubCategoryService.updateBulkSubCategoryIcon(categoryId, icons,loggedInUser, httpServletRequest);
    }
    @PutMapping
    public ResponseEntity<?>updateSubCategoryDisplayName(HttpServletRequest httpServletRequest){
        return assetSubCategoryService.updateSubCategoryDisplayName(httpServletRequest);
    }

}
