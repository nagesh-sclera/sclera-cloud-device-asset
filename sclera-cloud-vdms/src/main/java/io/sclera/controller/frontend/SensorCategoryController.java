package io.sclera.controller.frontend;

import io.sclera.dto.CategoryDTO;
import io.sclera.service.SensorCategoryService;
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
@RequestMapping("/api/sensorCategories")
public class SensorCategoryController {

    @Autowired
    private SensorCategoryService sensorCategoryService;

    @GetMapping
    public ResponseEntity<?> getAllSensorCategory(@RequestParam(required = false, defaultValue = "all") String key,
                                                  @RequestParam(required = false, defaultValue = "creation_timestamp") String sort,
                                                  @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                  @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int pageSize,
                                                  @RequestParam(name = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) {
        return sensorCategoryService.getAllSensorCategory(key, sort, pageNo, pageSize, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/addSensorCategory")
    public ResponseEntity<?> addSensorCategory(@RequestParam(value = "body") String body,
                                               @RequestParam(value = "icon", required = false) MultipartFile icon,
                                               @RequestParam(name = "loggedInUser") String loggedInUser,
                                               HttpServletRequest httpServletRequest) throws IOException {
        return sensorCategoryService.addSensorCategory(body, icon, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/{sensorCategoryId}")
    public ResponseEntity<?> updateSensorCategoryById(@PathVariable String sensorCategoryId, @RequestParam(value = "body") String body,
                                                      @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                      @RequestParam(name = "loggedInUser") String loggedInUser,
                                                      @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                      HttpServletRequest httpServletRequest) throws IOException {
        return sensorCategoryService.updateSensorCategoryById(sensorCategoryId, body, icon, loggedInUser, iconUrl, httpServletRequest);
    }

    @PutMapping(value = "/{sensorCategoryId}/icon")
    public ResponseEntity<?> updateSensorCategoryIconById(@PathVariable String sensorCategoryId, @RequestParam(name = "iconUrl", required = false) String iconUrl,
                                                          @RequestParam(value = "icon", required = false) MultipartFile icon,
                                                          @RequestParam(name = "loggedInUser") String loggedInUser,
                                                          HttpServletRequest httpServletRequest) throws IOException {
        return sensorCategoryService.updateSensorCategoryIconById(sensorCategoryId, iconUrl, icon, loggedInUser, httpServletRequest);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteSensorCategory(@RequestBody List<String> sensorCategoryIds, @RequestParam(name = "loggedInUser") String loggedInUser,
                                                  HttpServletRequest httpServletRequest) {
        return sensorCategoryService.deleteSensorCategory(sensorCategoryIds, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/addBulkSensorCategory")
    public ResponseEntity<?> addBulkSensorCategory(@RequestBody List<CategoryDTO> categoryDTOS,
                                                   @RequestParam(name = "loggedInUser") String loggedInUser,
                                                   HttpServletRequest httpServletRequest) throws IOException {
        return sensorCategoryService.addBulkSensorCategory(categoryDTOS, loggedInUser, httpServletRequest);
    }


}
