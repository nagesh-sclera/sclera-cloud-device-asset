package io.sclera.controller.admin;

import io.sclera.integration.dto.ResponseDTO;
import io.sclera.service.impl.QrCodeTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST endpoints for QR code PDF template management (per-org CRUD, in-use selection,
 * default-template lookup). Mirrors the endpoint paths of the sclera-cloud-vdms
 * QrCodeTemplateController; cloud-sync and WebSocket calls are omitted.
 */
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class QrCodeTemplateController {

    private final QrCodeTemplateService qrCodeTemplateService;

    public QrCodeTemplateController(QrCodeTemplateService qrCodeTemplateService) {
        this.qrCodeTemplateService = qrCodeTemplateService;
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    @GetMapping("/organisation/{orgId}/QrCodeTemplate/getAllQrCodeTemplateByOrgId")
    public ResponseEntity<ResponseDTO> getAllQrCodeTemplateByOrgId(
            @PathVariable String orgId,
            @RequestParam(required = false, name = "loggedInUser") String loggedInUser,
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
            @RequestParam(name = "key", defaultValue = "all") String key,
            HttpServletRequest request) {
        return qrCodeTemplateService.getAllQrCodeTemplateByOrgId(orgId, pageNo, pageSize, key, loggedInUser, request);
    }

    @GetMapping("/organisation/{orgId}/QrCodeTemplate/getInUseUrlByOrgId")
    public ResponseEntity<ResponseDTO> getInUseUrlByOrgId(
            @PathVariable String orgId,
            HttpServletRequest request) {
        return qrCodeTemplateService.getInUseUrlByOrgId(orgId, request);
    }

    @GetMapping("/QrCodeTemplate/getDefaultTemplate")
    public ResponseEntity<ResponseDTO> getDefaultTemplate(HttpServletRequest request) {
        return qrCodeTemplateService.getDefaultTemplate(request);
    }

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    @PostMapping("/organisation/{orgId}/QrCodeTemplate/addQrCodeTemplate")
    public ResponseEntity<ResponseDTO> addQrCodeTemplate(
            @PathVariable String orgId,
            @RequestParam(required = false, name = "file") MultipartFile template,
            @RequestParam(required = false, name = "companyLogo") MultipartFile logo,
            @RequestParam(name = "body") String body,
            @RequestParam(required = false, name = "loggedInUser") String loggedInUser,
            HttpServletRequest request) throws IOException {
        return qrCodeTemplateService.addQrCodeTemplate(orgId, body, template, logo, loggedInUser, request);
    }

    @PutMapping("/organisation/{orgId}/QrCodeTemplate/updateQrCodeTemplate")
    public ResponseEntity<ResponseDTO> updateQrCodeTemplate(
            @PathVariable String orgId,
            @RequestParam(required = false, name = "file") MultipartFile template,
            @RequestParam(required = false, name = "companyLogo") MultipartFile logo,
            @RequestParam(name = "body") String body,
            @RequestParam(required = false, name = "templateUrl") String templateUrl,
            @RequestParam(required = false, name = "logoUrl") String logoUrl,
            @RequestParam(required = false, name = "loggedInUser") String loggedInUser,
            HttpServletRequest request) throws IOException {
        return qrCodeTemplateService.updateQrCodeTemplate(orgId, body, template, logo, templateUrl, logoUrl, loggedInUser, request);
    }

    @DeleteMapping("/organisation/{orgId}/QrCodeTemplate/deleteQrCodeTemplate")
    public ResponseEntity<ResponseDTO> deleteQrCodeTemplate(
            @PathVariable String orgId,
            @RequestBody List<String> ids,
            HttpServletRequest request) {
        return qrCodeTemplateService.deleteQrCodeTemplate(orgId, ids, request);
    }

    // -------------------------------------------------------------------------
    // In-use toggle
    // -------------------------------------------------------------------------

    @PutMapping("/organisation/{orgId}/QrCodeTemplate/updateInUseByUrl")
    public ResponseEntity<ResponseDTO> updateInUseByUrl(
            @PathVariable String orgId,
            @RequestParam(required = false, name = "templateUrl") String templateUrl,
            @RequestParam(required = false, defaultValue = "false") boolean isDefault,
            HttpServletRequest request) {
        return qrCodeTemplateService.updateInUseByUrl(orgId, templateUrl, isDefault, request);
    }
}
