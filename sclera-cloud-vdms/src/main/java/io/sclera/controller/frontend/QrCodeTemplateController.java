package io.sclera.controller.frontend;

import io.sclera.dto.ResponseDTO;
import io.sclera.service.QrCodeTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api")
public class QrCodeTemplateController {


    private final QrCodeTemplateService qrCodeTemplateService;

    public QrCodeTemplateController(QrCodeTemplateService qrCodeTemplateService) {
        this.qrCodeTemplateService = qrCodeTemplateService;
    }

    @GetMapping("/organisation/{orgId}/QrCodeTemplate/getAllQrCodeTemplateByOrgId")
    public ResponseEntity<ResponseDTO> getAllQrCodeTemplateByOrgId(@PathVariable String orgId,
                                                                   @RequestParam(required = false, name = "loggedInUser") String loggedInUser,
                                                                   @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                                   @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
                                                                   @RequestParam(name = "key", defaultValue = "all") String key,
                                                                   HttpServletRequest httpServletRequest) {
        return qrCodeTemplateService.getAllQrCodeTemplateByOrgId(orgId, pageNo, pageSize, key, loggedInUser, httpServletRequest);
    }

    @GetMapping("/QrCodeTemplate/getAllQrCodeTemplate")
    public ResponseEntity<ResponseDTO> getAllQrCodeTemplate(@RequestParam(required = false, name = "loggedInUser") String loggedInUser,
                                                            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
                                                            @RequestParam(name = "key", defaultValue = "all") String key,
                                                            HttpServletRequest httpServletRequest) {
        return qrCodeTemplateService.getAllQrCodeTemplate(pageNo, pageSize, key, loggedInUser, httpServletRequest);
    }

    @PostMapping("/organisation/{orgId}/QrCodeTemplate/addQrCodeTemplate")
    public ResponseEntity<ResponseDTO> addQrCodeTemplate(@PathVariable String orgId,
                                                         @RequestParam(required = false, name = "file") MultipartFile template,
                                                         @RequestParam(required = false, name = "companyLogo") MultipartFile logo,
                                                         @RequestParam(name = "body") String body,
                                                         @RequestParam(required = false, name = "loggedInUser") String loggedInUser,
                                                         HttpServletRequest httpServletRequest) throws IOException {
        return qrCodeTemplateService.addQrCodeTemplate(orgId, body, template, logo, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/organisation/{orgId}/QrCodeTemplate/deleteQrCodeTemplate")
    public ResponseEntity<ResponseDTO> deleteQrCodeTemplate(@PathVariable String orgId,@RequestBody List<String> ids, HttpServletRequest httpServletRequest) {
        return qrCodeTemplateService.deleteQrCodeTemplate(orgId,ids, httpServletRequest);
    }

    @PutMapping("/organisation/{orgId}/QrCodeTemplate/updateQrCodeTemplate")
    public ResponseEntity<ResponseDTO> updateQrCodeTemplate(@PathVariable String orgId,
                                                            @RequestParam(required = false, name = "file") MultipartFile template,
                                                            @RequestParam(required = false, name = "companyLogo") MultipartFile logo,
                                                            @RequestParam(name = "body") String body,
                                                            @RequestParam(required = false,name = "templateUrl") String templateUrl,
                                                            @RequestParam(required = false,name = "logoUrl") String logoUrl,
                                                            @RequestParam(required = false, name = "loggedInUser") String loggedInUser,
                                                            HttpServletRequest httpServletRequest) throws IOException {
        return qrCodeTemplateService.updateQrCodeTemplate(orgId, body, template, logo, templateUrl, logoUrl, loggedInUser, httpServletRequest);
    }

    @GetMapping("/organisation/{orgId}/QrCodeTemplate/getInUseUrlByOrgId")
    public ResponseEntity<ResponseDTO> getInUseUrlByOrgId(@PathVariable String orgId,
                                                       HttpServletRequest httpServletRequest) {
        return qrCodeTemplateService.getInUseUrlByOrgId(orgId, httpServletRequest);
    }

    @PutMapping("/organisation/{orgId}/QrCodeTemplate/updateInUseByUrl")
    public ResponseEntity<ResponseDTO> updateInUseByUrl(@PathVariable String orgId,
                                                        @RequestParam(required = false,name = "templateUrl") String templateUrl,
                                                        @RequestParam(required = false, defaultValue = "0") boolean isDefault,
                                                        HttpServletRequest httpServletRequest) {
        return qrCodeTemplateService.updateInUseByUrl(orgId,templateUrl,isDefault, httpServletRequest);
    }

}
