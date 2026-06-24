package io.sclera.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.Repository.QrCodeTemplateRepository;
import io.sclera.dto.QrCodeTemplateDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.utils.QrImageStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages QR code PDF templates for an organisation: CRUD, in-use selection,
 * and default-template lookup. Template and logo images are stored via
 * {@link QrImageStorageService} (S3 in non-local profiles).
 *
 * Ported from sclera-cloud-vdms QrCodeTemplateService; cloud-sync, WebSocket,
 * and multi-tenant logic are omitted.
 */
@Service("qrCodeTemplateService")
@Slf4j
public class QrCodeTemplateService {

    private final QrCodeTemplateRepository qrCodeTemplateRepository;
    private final QrImageStorageService imageStorage;

    public QrCodeTemplateService(QrCodeTemplateRepository qrCodeTemplateRepository,
                                 QrImageStorageService imageStorage) {
        this.qrCodeTemplateRepository = qrCodeTemplateRepository;
        this.imageStorage = imageStorage;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private ResponseDTO ok(Object data) {
        return new ResponseDTO("OK", 200, data, true, BigInteger.valueOf(System.currentTimeMillis()));
    }

    /**
     * Derives a stable storage key from orgId + a UUID suffix so keys are unique per upload.
     * Format: template/{orgId}/{uuid}
     */
    private String templateKey(String orgId, String uuid) {
        return "template/" + orgId + "/" + uuid;
    }

    /**
     * Derives a stable storage key for the company logo.
     * Format: template-logo/{orgId}/{uuid}
     */
    private String logoKey(String orgId, String uuid) {
        return "template-logo/" + orgId + "/" + uuid;
    }

    /**
     * Best-effort fetch of a stored template image's bytes by its storage key.
     * Used by branded PDF generation; returns null (never throws) if the template
     * cannot be retrieved so callers can fall back to the default layout.
     */
    public byte[] fetchTemplateBytes(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            return imageStorage.fetch(key);
        } catch (Exception e) {
            log.warn("fetchTemplateBytes failed for key={}: {}", key, e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    /**
     * Returns all templates for the given org, with the default template prepended on
     * page 1 (marked in-use when no org template is currently selected).
     *
     * NOTE: repository param order is (key, orgId, offset, pageSize) — offset first, then limit.
     */
    public ResponseEntity<ResponseDTO> getAllQrCodeTemplateByOrgId(String orgId,
                                                                   int pageNo,
                                                                   int pageSize,
                                                                   String key,
                                                                   String loggedInUser,
                                                                   HttpServletRequest request) {
        log.info("getAllQrCodeTemplateByOrgId orgId={} pageNo={} pageSize={} key={}", orgId, pageNo, pageSize, key);
        int offset = pageSize * (pageNo - 1);
        List<QrCodeTemplateDTO> templates =
                new ArrayList<>(qrCodeTemplateRepository.getAllQrCodeTemplateByOrgId(key, orgId, offset, pageSize));
        if (pageNo == 1) {
            QrCodeTemplateDTO defaultTemplate = qrCodeTemplateRepository.getDefaultTemplate();
            int count = qrCodeTemplateRepository.getTemplateInUseCount(orgId);
            if (count == 0 && defaultTemplate != null) {
                defaultTemplate.setInUse(1);
            }
            if (defaultTemplate != null) {
                templates.add(0, defaultTemplate);
            }
        }
        log.info("getAllQrCodeTemplateByOrgId success endpoint={}", request.getRequestURI());
        return new ResponseEntity<>(ok(templates), HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getInUseUrlByOrgId(String orgId, HttpServletRequest request) {
        log.info("getInUseUrlByOrgId orgId={}", orgId);
        QrCodeTemplateDTO dto = qrCodeTemplateRepository.getInUseUrlByOrgId(1, orgId);
        if (dto == null) {
            dto = qrCodeTemplateRepository.getDefaultTemplate();
        }
        log.info("getInUseUrlByOrgId success endpoint={}", request.getRequestURI());
        return new ResponseEntity<>(ok(dto), HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getDefaultTemplate(HttpServletRequest request) {
        log.info("getDefaultTemplate endpoint={}", request.getRequestURI());
        QrCodeTemplateDTO dto = qrCodeTemplateRepository.getDefaultTemplate();
        return new ResponseEntity<>(ok(dto), HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // Write
    // -------------------------------------------------------------------------

    public ResponseEntity<ResponseDTO> addQrCodeTemplate(String orgId,
                                                         String body,
                                                         MultipartFile templateFile,
                                                         MultipartFile logo,
                                                         String loggedInUser,
                                                         HttpServletRequest request) throws IOException {
        log.info("addQrCodeTemplate orgId={} body={}", orgId, body);
        QrCodeTemplateDTO dto = JSONObject.parseObject(body, QrCodeTemplateDTO.class);
        if (dto == null) {
            return new ResponseEntity<>(ok(null), HttpStatus.OK);
        }
        BigInteger creationTimestamp = BigInteger.valueOf(System.currentTimeMillis());
        String uuid = Generators.timeBasedGenerator().generate().toString();

        if (templateFile != null && !templateFile.isEmpty()) {
            String key = templateKey(orgId, uuid);
            String url = imageStorage.store(templateFile.getBytes(), key, "png");
            dto.setQrCodeTemplateUrl(url);
        }
        if (logo != null && !logo.isEmpty()) {
            String key = logoKey(orgId, uuid);
            String url = imageStorage.store(logo.getBytes(), key, "png");
            dto.setQrCodeLogoUrl(url);
        }

        qrCodeTemplateRepository.addQrCodeTemplate(
                uuid,
                dto.getTemplateName() != null ? dto.getTemplateName() : dto.getName(),
                dto.getQrCodeTemplateUrl(),
                dto.getQrCodeLogoUrl(),
                orgId,
                body,
                creationTimestamp,
                loggedInUser);

        log.info("addQrCodeTemplate success endpoint={}", request.getRequestURI());
        return new ResponseEntity<>(ok(dto.getQrCodeTemplateUrl()), HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> updateQrCodeTemplate(String orgId,
                                                            String body,
                                                            MultipartFile templateFile,
                                                            MultipartFile logo,
                                                            String templateUrl,
                                                            String logoUrl,
                                                            String loggedInUser,
                                                            HttpServletRequest request) throws IOException {
        log.info("updateQrCodeTemplate orgId={} body={} templateUrl={} logoUrl={}", orgId, body, templateUrl, logoUrl);
        QrCodeTemplateDTO dto = JSONObject.parseObject(body, QrCodeTemplateDTO.class);
        if (dto == null) {
            return new ResponseEntity<>(ok(null), HttpStatus.OK);
        }
        BigInteger updatedTimestamp = BigInteger.valueOf(System.currentTimeMillis());
        String uuid = Generators.timeBasedGenerator().generate().toString();

        // Delete old template image if an existing URL was provided
        if (templateUrl != null && !templateUrl.isBlank()) {
            String oldKey = templateKey(orgId, templateUrl);
            imageStorage.delete(oldKey);
            dto.setQrCodeTemplateUrl(null);
        }

        // Delete old logo if a logoUrl was supplied but no new logo file arrived
        if (logoUrl != null && (logo == null || logo.isEmpty())) {
            String oldKey = logoKey(orgId, logoUrl);
            imageStorage.delete(oldKey);
            dto.setQrCodeLogoUrl(null);
        }

        // Upload new template image
        if (templateFile != null && !templateFile.isEmpty()) {
            String key = templateKey(orgId, uuid);
            String newUrl = imageStorage.store(templateFile.getBytes(), key, "png");
            dto.setQrCodeTemplateUrl(newUrl);
            // Preserve logo URL if no new logo was uploaded
            if (logo == null || logo.isEmpty()) {
                dto.setQrCodeLogoUrl(logoUrl);
            }
        }

        // Upload new logo image
        if (logo != null && !logo.isEmpty()) {
            String key = logoKey(orgId, uuid);
            String newLogoUrl = imageStorage.store(logo.getBytes(), key, "png");
            dto.setQrCodeLogoUrl(newLogoUrl);
        }

        // Fallback: if logoUrl was null all along, read existing one from DB
        if (logoUrl == null) {
            String existingLogoUrl = qrCodeTemplateRepository.getQrCodeLogoUrlById(dto.getId());
            dto.setQrCodeLogoUrl(existingLogoUrl);
        }

        qrCodeTemplateRepository.updateQrCodeTemplateById(
                dto.getTemplateName() != null ? dto.getTemplateName() : dto.getName(),
                dto.getQrCodeTemplateUrl(),
                dto.getQrCodeLogoUrl(),
                orgId,
                body,
                updatedTimestamp,
                loggedInUser,
                dto.getId());

        log.info("updateQrCodeTemplate success endpoint={}", request.getRequestURI());
        return new ResponseEntity<>(ok(dto.getQrCodeTemplateUrl()), HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> deleteQrCodeTemplate(String orgId,
                                                            List<String> ids,
                                                            HttpServletRequest request) {
        log.info("deleteQrCodeTemplate orgId={} ids={}", orgId, ids);
        List<QrCodeTemplateDTO> rows = qrCodeTemplateRepository.getDataByIds(ids);
        if (rows != null) {
            for (QrCodeTemplateDTO row : rows) {
                if (row.getQrCodeTemplateUrl() != null) {
                    imageStorage.delete(templateKey(orgId, row.getQrCodeTemplateUrl()));
                }
                if (row.getQrCodeLogoUrl() != null) {
                    imageStorage.delete(logoKey(orgId, row.getQrCodeLogoUrl()));
                }
            }
        }
        qrCodeTemplateRepository.removeQrCodeTemplateByIds(ids);
        log.info("deleteQrCodeTemplate success endpoint={}", request.getRequestURI());
        return new ResponseEntity<>(ok("Successfully Deleted Qr-Code Template"), HttpStatus.OK);
    }

    // -------------------------------------------------------------------------
    // In-use toggle
    // -------------------------------------------------------------------------

    public ResponseEntity<ResponseDTO> updateInUseByUrl(String orgId,
                                                        String templateUrl,
                                                        boolean isDefault,
                                                        HttpServletRequest request) {
        log.info("updateInUseByUrl orgId={} templateUrl={} isDefault={}", orgId, templateUrl, isDefault);
        // Clear all in-use flags for this org first
        qrCodeTemplateRepository.updateInUseByOrgId(0, orgId);
        if (!isDefault && templateUrl != null && !templateUrl.isBlank()) {
            qrCodeTemplateRepository.updateInUseByUrl(1, templateUrl);
        }
        log.info("updateInUseByUrl success endpoint={}", request.getRequestURI());
        return new ResponseEntity<>(ok("Successfully Updating Qr-Code Template"), HttpStatus.OK);
    }
}
