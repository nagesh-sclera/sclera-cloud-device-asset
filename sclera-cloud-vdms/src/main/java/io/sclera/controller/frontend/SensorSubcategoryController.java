package io.sclera.controller.frontend;

import io.sclera.dto.CategoryDTO;
import io.sclera.service.SensorSubcategoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/sensorCategories")
public class SensorSubcategoryController {

    @Autowired
    private SensorSubcategoryService sensorSubcategoryService;

    @GetMapping(value = "/{sensorCategoryId}/sensorSubcategories")
    public ResponseEntity<?> getSensorSubCategoryBySensorCategoryId(@PathVariable String sensorCategoryId,
                                                                    @RequestParam(required = false, defaultValue = "all") String key,
                                                                    @RequestParam(required = false) String sort,
                                                                    @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                    HttpServletRequest httpServletRequest) {
        return sensorSubcategoryService.getSensorSubCategoryBySensorCategoryId(sensorCategoryId, key, sort, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/{sensorCategoryName}/getSensorSubCategoryBySensorCategoryName")
    public ResponseEntity<?> getSensorSubCategoryBySensorCategoryName(@PathVariable String sensorCategoryName,
                                                                      @RequestParam(required = false, defaultValue = "all") String key,
                                                                      @RequestParam(required = false) String sort,
                                                                      @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                      HttpServletRequest httpServletRequest) {
        return sensorSubcategoryService.getSensorSubCategoryBySensorCategoryName(sensorCategoryName, key, sort, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/{sensorCategoryId}/sensorSubcategories/addSensorSubCategoryBySensorCategoryId")
    public ResponseEntity<?> addSensorSubCategoryBySensorCategoryId(@PathVariable String sensorCategoryId,
                                                                    @RequestParam(value = "body") String body,
                                                                    @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                                    @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                    HttpServletRequest httpServletRequest) throws IOException {
        return sensorSubcategoryService.addSensorSubCategoryBySensorCategoryId(sensorCategoryId, body, icon, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/{sensorCategoryId}/sensorSubcategories/{sensorSubCategoryId}")
    public ResponseEntity<?> updateSubCategoryByCategoryAndSubcategoryId(@PathVariable String sensorCategoryId, @PathVariable String sensorSubCategoryId,
                                                                         @RequestParam(value = "body") String body,
                                                                         @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                                         @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                         @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                                         HttpServletRequest httpServletRequest) throws IOException {
        return sensorSubcategoryService.updateSensorSubCategoryBySensorCategoryAndSensorSubcategoryId(sensorCategoryId, sensorSubCategoryId, body, icon, loggedInUser, iconUrl, httpServletRequest);
    }

    @PutMapping(value = "/{sensorCategoryId}/sensorSubcategories/{sensorSubCategoryId}/icon")
    public ResponseEntity<?> updateSubCategoryIconByCategoryAndSubcategoryId(@PathVariable String sensorCategoryId, @PathVariable String sensorSubCategoryId,
                                                                             @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                                             @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                                             @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                             HttpServletRequest httpServletRequest) throws IOException {
        return sensorSubcategoryService.updateSensorSubCategoryIconBySensorCategoryAndSensorSubcategoryId(sensorCategoryId, sensorSubCategoryId, iconUrl, icon, loggedInUser, httpServletRequest);
    }

    @DeleteMapping(value = "/{sensorCategoryId}/sensorSubcategories")
    public ResponseEntity<?> deleteSubcategoryByCategoryAndSubcategoryId(@PathVariable String sensorCategoryId, @RequestBody List<String> sensorSubCategoryIds,
                                                                         @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                         HttpServletRequest httpServletRequest) {
        return sensorSubcategoryService.deleteSensorSubcategoryBySensorCategoryAndSensorSubcategoryId(sensorCategoryId, sensorSubCategoryIds, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/{sensorCategoryId}/sensorSubcategories/addBulkSensorSubCategoryBySensorCategoryId")
    public ResponseEntity<?> addBulkSensorSubCategoryBySensorCategoryId(@RequestBody List<CategoryDTO> categoryDTOS,
                                                                        @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                        HttpServletRequest httpServletRequest) throws IOException {
        return sensorSubcategoryService.addBulkSensorSubCategoryBySensorCategoryId(categoryDTOS, loggedInUser, httpServletRequest);
    }
}
