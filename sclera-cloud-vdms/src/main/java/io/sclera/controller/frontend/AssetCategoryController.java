package io.sclera.controller.frontend;


import io.sclera.service.AssetCategoryService;
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
@RequestMapping("/api/categories")
public class AssetCategoryController {

    @Autowired
    private AssetCategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getAllCategory(@RequestParam(required = false, defaultValue = "all") String key,
                                            @RequestParam(required = false, defaultValue = "creation_timestamp") String sort,
                                            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                            @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int pagesize,
                                            @RequestParam(name = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) {
        return categoryService.getAllCategory(key, sort, pageno, pagesize, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/addCategory")
    public ResponseEntity<?> addCategory(@RequestParam(value = "body") String body,
                                         @RequestParam(value = "icon", required = false) MultipartFile icon,
                                         @RequestParam(name = "loggedInUser") String loggedInUser,
                                         HttpServletRequest httpServletRequest) throws IOException {
        return categoryService.addCategory(body, icon, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/{categoryId}")
    public ResponseEntity<?> updateCategoryById(@PathVariable String categoryId, @RequestParam(value = "body") String body,
                                                @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                @RequestParam(name = "loggedInUser") String loggedInUser,
                                                @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                HttpServletRequest httpServletRequest) throws IOException {
        return categoryService.updateCategoryById(categoryId, body, icon, loggedInUser,iconUrl, httpServletRequest);
    }

    @PutMapping(value = "/{categoryId}/icon")
    public ResponseEntity<?> updateCategoryIconById(@PathVariable String categoryId, @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                    @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                    @RequestParam(name = "loggedInUser") String loggedInUser,
                                                    HttpServletRequest httpServletRequest) throws IOException {
        return categoryService.updateCategoryIconById(categoryId, iconUrl, icon, loggedInUser, httpServletRequest);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteCategory(@RequestBody List<String> categoryIds, @RequestParam(name = "loggedInUser") String loggedInUser,
                                            HttpServletRequest httpServletRequest) {
        return categoryService.deleteCategory(categoryIds, loggedInUser, httpServletRequest);
    }

    @PutMapping
    public ResponseEntity<?>updateCategoryDisplayName(HttpServletRequest httpServletRequest){
        return categoryService.updateCategoryDisplayName(httpServletRequest);
    }
}
