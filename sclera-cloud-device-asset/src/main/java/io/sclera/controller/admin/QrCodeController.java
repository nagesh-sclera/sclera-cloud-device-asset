package io.sclera.controller.admin;

import io.sclera.dto.QrCodeDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.service.impl.QrCodeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * REST controller for QR code generation, tagging, lookup and ADC check.
 * Ported from sclera-cloud-vdms QRCodeController; cloud-only params (HttpServletRequest,
 * multi-tenant orgId/email path vars) replaced with @RequestParam where appropriate.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Validated
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class QrCodeController {

    private static final Logger log = LoggerFactory.getLogger(QrCodeController.class);

    @Autowired
    private QrCodeService qrCodeService;

    /**
     * Generates up to 1000 QR codes and streams the export bundle (zip/pdf/txt)
     * directly to the HTTP response.
     *
     * @param orgId           customer organisation id
     * @param email           creating user email
     * @param count           number of codes to generate (1-1000)
     * @param width           page width in inches (default 2.75)
     * @param height          page height in inches (default 4.0)
     * @param type            export type: zip | pdf | txt
     * @param brand           embed brand template when true
     * @param defaultTemplate use default branded template when true
     * @param templateUrl     optional URL for a custom template image
     * @param response        HTTP response to stream the export bytes into
     */
    @GetMapping(value = "/qrCode/generateQRCode")
    public void generateQRCode(
            @RequestParam String orgId,
            @RequestParam String email,
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) Integer count,
            @RequestParam(required = false, defaultValue = "2.75") Float width,
            @RequestParam(required = false, defaultValue = "4.0") Float height,
            @RequestParam String type,
            @RequestParam(required = false, defaultValue = "true") boolean brand,
            @RequestParam(required = false, defaultValue = "false") boolean defaultTemplate,
            @RequestParam(required = false, name = "templateUrl") String templateUrl,
            HttpServletResponse response) throws IOException {

        log.info("generateQRCode orgId={} email={} count={} type={}", orgId, email, count, type);
        byte[] exportBytes = qrCodeService.generateQRCode(orgId, email, count, width, height, type,
                brand, defaultTemplate, templateUrl);

        if ("pdf".equals(type)) {
            response.setContentType(MediaType.APPLICATION_PDF_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=\"qrcodes.pdf\"");
        } else if ("zip".equals(type)) {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"qrcodes.zip\"");
        } else {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=\"qrcodes.txt\"");
        }

        if (exportBytes != null) {
            response.getOutputStream().write(exportBytes);
            response.flushBuffer();
        }
    }

    /**
     * Asynchronously generates more than 1000 QR codes (bulk) and stores the
     * export bundle via QrImageStorageService; the download link is logged.
     *
     * @param orgId           customer organisation id
     * @param email           creating user email
     * @param count           number of codes to generate (1001-50000)
     * @param width           page width in inches (default 2.75)
     * @param height          page height in inches (default 4.0)
     * @param type            export type: zip | pdf | txt
     * @param emailTo         recipient email for the download link (body)
     * @param brand           embed brand template when true
     * @param defaultTemplate use default branded template when true
     * @param templateUrl     optional URL for a custom template image
     */
    @PostMapping(value = "/qrCode/generateBulkQRCode")
    public ResponseEntity<ResponseDTO> generateBulkQRCode(
            @RequestParam String orgId,
            @RequestParam String email,
            @RequestParam(defaultValue = "1001") @Min(1001) @Max(50000) Integer count,
            @RequestParam(required = false, defaultValue = "2.75") Float width,
            @RequestParam(required = false, defaultValue = "4.0") Float height,
            @RequestParam String type,
            @RequestBody String emailTo,
            @RequestParam(required = false, defaultValue = "true") boolean brand,
            @RequestParam(required = false, defaultValue = "false") boolean defaultTemplate,
            @RequestParam(required = false, name = "templateUrl") String templateUrl) {

        log.info("generateBulkQRCode orgId={} email={} count={} type={}", orgId, email, count, type);
        qrCodeService.generateBulkQRCode(orgId, email, count, width, height, type, emailTo,
                brand, defaultTemplate, templateUrl);

        ResponseDTO resp = new ResponseDTO("Bulk QR code generation started", 200, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Upserts a QR code tag assignment (device/location/vdmsId).
     *
     * @param qrCodeDTO    QR code payload
     * @param loggedInUser audit user string
     */
    @PostMapping(value = "/qrCode/updateQrCode")
    public ResponseEntity<ResponseDTO> upsertQrcode(
            @RequestBody QrCodeDTO qrCodeDTO,
            @RequestParam String loggedInUser) {

        log.info("upsertQrcode loggedInUser={}", loggedInUser);
        qrCodeService.upsertQrcode(qrCodeDTO, loggedInUser);
        ResponseDTO resp = new ResponseDTO("QR code updated successfully", 200, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Returns full QR code details for the given qrCodeId.
     *
     * @param qrCodeId QR code identifier
     */
    @GetMapping(value = "/qrCode/{qrCodeId}/getQrCodeDetailsByQrCodeId")
    public ResponseEntity<ResponseDTO> getQrCodeDetailsByQrCodeId(
            @PathVariable String qrCodeId) {

        log.info("getQrCodeDetailsByQrCodeId qrCodeId={}", qrCodeId);
        QrCodeDTO dto = qrCodeService.getQrCodeDetailsByQrCodeId(qrCodeId);
        ResponseDTO resp = new ResponseDTO(null, 200, dto, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Returns QR codes associated with the given vdmsId and deviceId.
     *
     * @param vdmsId   VDMS identifier
     * @param deviceId device identifier
     */
    @GetMapping(value = "/vdms/{vdmsId}/deviceId/{deviceId}/getQrCodeDetailsByVdmsIdAndDeviceId")
    public ResponseEntity<ResponseDTO> getQrCodeDetailsByVdmsIdAndDeviceId(
            @PathVariable String vdmsId,
            @PathVariable String deviceId) {

        log.info("getQrCodeDetailsByVdmsIdAndDeviceId vdmsId={} deviceId={}", vdmsId, deviceId);
        List<QrCodeDTO> results = qrCodeService.getQrCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
        ResponseDTO resp = new ResponseDTO(null, 200, results, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Returns QR codes associated with the given vdmsId and locationId.
     *
     * @param vdmsId     VDMS identifier
     * @param locationId location identifier
     */
    @GetMapping(value = "/vdms/{vdmsId}/locationId/{locationId}/getQrCodeDetailsByVdmsIdAndLocationId")
    public ResponseEntity<ResponseDTO> getQrCodeDetailsByVdmsIdAndLocationId(
            @PathVariable String vdmsId,
            @PathVariable String locationId) {

        log.info("getQrCodeDetailsByVdmsIdAndLocationId vdmsId={} locationId={}", vdmsId, locationId);
        List<QrCodeDTO> results = qrCodeService.getQrCodeDetailsByVdmsIdAndLocationId(vdmsId, locationId);
        ResponseDTO resp = new ResponseDTO(null, 200, results, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Returns a paginated list of untagged QR code identifiers for the given vdmsId.
     *
     * @param vdmsId       VDMS identifier
     * @param loggedInUser audit user string
     * @param pageNo       1-based page number (1-1000)
     * @param pageSize     records per page (1-1000)
     */
    @GetMapping("/vdms/{vdmsId}/getUnTaggedQrCode")
    public ResponseEntity<ResponseDTO> getUnTaggedQrCode(
            @PathVariable String vdmsId,
            @RequestParam(name = "loggedInUser") String loggedInUser,
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize) {

        log.info("getUnTaggedQrCode vdmsId={} pageNo={} pageSize={}", vdmsId, pageNo, pageSize);
        List<String> ids = qrCodeService.getUnTaggedQrCode(pageNo, pageSize);
        ResponseDTO resp = new ResponseDTO(null, 200, ids, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Returns the QR code count for the given vdmsId updated since lastSyncTime.
     *
     * @param vdmsId       VDMS identifier
     * @param lastSyncTime epoch-millis timestamp
     */
    @GetMapping(value = "/vdms/{vdmsId}/getQrCodeCounts")
    public ResponseEntity<ResponseDTO> getQrCodeCounts(
            @PathVariable String vdmsId,
            @RequestParam(name = "lastSyncTime") BigInteger lastSyncTime) {

        log.info("getQrCodeCounts vdmsId={} lastSyncTime={}", vdmsId, lastSyncTime);
        int count = qrCodeService.getQrCodeCounts(vdmsId, lastSyncTime);
        ResponseDTO resp = new ResponseDTO(null, 200, count, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Checks whether a QR code is ADC-tagged and whether it is tagged to a device/location.
     * Returns a map with keys {@code isTagged} (0=untagged, 1=tagged, 2=not found)
     * and {@code isAdc} (boolean).
     *
     * @param qrCodeId QR code identifier
     */
    @GetMapping(value = "/qrCode/{qrCodeId}/getQrCodeCheckById")
    public ResponseEntity<ResponseDTO> getQrCodeCheckById(@PathVariable String qrCodeId) {
        log.info("getQrCodeCheckById qrCodeId={}", qrCodeId);
        Map<String, Object> check = qrCodeService.getAdcCheckByQrCodeId(qrCodeId);
        ResponseDTO resp = new ResponseDTO(null, 200, check, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Updates device/location detail on a list of QR codes for the given vdmsId.
     *
     * @param vdmsId     VDMS identifier
     * @param qrCodeDTOs list of QR code DTOs carrying updated device/location assignments
     */
    @PutMapping(value = "/vdms/{vdmsId}/updateQrCodeDetails")
    public ResponseEntity<ResponseDTO> updateQrCodeDetails(
            @PathVariable String vdmsId,
            @RequestBody List<QrCodeDTO> qrCodeDTOs) {

        log.info("updateQrCodeDetails vdmsId={} count={}", vdmsId, qrCodeDTOs != null ? qrCodeDTOs.size() : 0);
        qrCodeService.updateQrCodeDetails(vdmsId, qrCodeDTOs);
        ResponseDTO resp = new ResponseDTO("QR code details updated", 200, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
