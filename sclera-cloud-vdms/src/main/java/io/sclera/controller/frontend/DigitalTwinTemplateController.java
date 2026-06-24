package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.service.DigitalTwinTemplateService;
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
@RequestMapping("/api/organisations/{orgId}/users/{email}/vdms/{vdmsId}")
public class DigitalTwinTemplateController {

    @Autowired
    private DigitalTwinTemplateService digitalTwinTemplateService;

    @PostMapping("/addDigitalTwinTemplates")
    public ResponseEntity<ResponseDTO> addDigitalTwinTemplateByVdmsAndSubCategoryId(@PathVariable String orgId, @PathVariable String email,
                                                                                    @PathVariable String vdmsId, @RequestParam(name = "body") String body,
                                                                                    @RequestParam(name = "image", required = false) MultipartFile image,
                                                                                    @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                                    HttpServletRequest httpServletRequest) throws IOException {
        return digitalTwinTemplateService.addDigitalTwinTemplateByVdmsAndSubCategoryId(orgId, email, vdmsId, body, image, loggedInUser, httpServletRequest);
    }

    @GetMapping("/digitalTwinTemplates")
    public ResponseEntity<ResponseDTO> getDigitalTwinTemplateListByVdmsId(@PathVariable String orgId, @PathVariable String email,
                                                                          @PathVariable String vdmsId,
                                                                          @RequestParam(required = false, defaultValue = "all") String key,
                                                                          @RequestParam(required = false, defaultValue = "all") String categoryId,
                                                                          @RequestParam(required = false, defaultValue = "all") String subCategoryId,
                                                                          @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                                          @RequestParam(defaultValue = "25") @Min(1) @Max(1000) int pagesize,
                                                                          @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                          HttpServletRequest httpServletRequest) {
        return digitalTwinTemplateService.getDigitalTwinTemplateListByVdmsId(orgId, email, vdmsId, key, categoryId, subCategoryId, pageno,
                pagesize, loggedInUser, httpServletRequest);
    }

    @GetMapping("/digitalTwinTemplates/{digitalTwinTemplateId}")
    public ResponseEntity<ResponseDTO> getDigitalTwinTemplateDetailsByVdmsAndDigitalTwinTemplateId(@PathVariable String orgId, @PathVariable String email,
                                                                                                   @PathVariable String vdmsId, @PathVariable String digitalTwinTemplateId,
                                                                                                   @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                                                   HttpServletRequest httpServletRequest) {
        return digitalTwinTemplateService.getDigitalTwinTemplateDetailsByVdmsAndDigitalTwinTemplateId(orgId, email, vdmsId, digitalTwinTemplateId, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/tagDigitalTwinToDevice")
    public ResponseEntity<ResponseDTO> tagDigitalTwinToDevice(@PathVariable String orgId, @PathVariable String email,
                                                              @PathVariable String vdmsId,
                                                              @RequestParam(value = "image", required = false) MultipartFile image,
                                                              @RequestParam(value = "imageUrl", required = false) String imageUrl,
                                                              @RequestParam(name = "deviceId") String deviceId,
                                                              @RequestParam(name = "loggedInUser") String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        return digitalTwinTemplateService.tagDigitalTwinToDevice(orgId, email, vdmsId, image, imageUrl, deviceId, loggedInUser, httpServletRequest);
    }

    @PutMapping("/digitalTwinTemplates/{digitalTwinTemplateId}")
    public ResponseEntity<ResponseDTO> updateDigitalTwinTemplateDetailsByVdmsAndDigitalTwinTemplateId(@PathVariable String orgId, @PathVariable String email,
                                                                                                      @PathVariable String vdmsId, @PathVariable String digitalTwinTemplateId,
                                                                                                      @RequestParam(name = "body") String body,
                                                                                                      @RequestParam(name = "image", required = false) MultipartFile image,
                                                                                                      @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                                                      HttpServletRequest httpServletRequest) throws IOException {
        return digitalTwinTemplateService.updateDigitalTwinTemplateDetailsByVdmsAndDigitalTwinTemplateId(orgId, email, vdmsId, digitalTwinTemplateId, body, image, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/digitalTwinTemplates")
    public ResponseEntity<ResponseDTO> deleteDigitalTwinTemplatesByVdmsAndDigitalTwinTemplateIds(@PathVariable String orgId, @PathVariable String email,
                                                                                                 @PathVariable String vdmsId, @RequestBody List<String> digitalTwinTemplateIds,
                                                                                                 @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                                                 HttpServletRequest httpServletRequest) {
        return digitalTwinTemplateService.deleteDigitalTwinTemplatesByVdmsAndDigitalTwinTemplateIds(orgId, email, vdmsId, digitalTwinTemplateIds, loggedInUser, httpServletRequest);
    }

}
