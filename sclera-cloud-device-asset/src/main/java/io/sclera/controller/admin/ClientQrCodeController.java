package io.sclera.controller.admin;

import io.sclera.dto.ClientQrCodeDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.service.ClientQrCodeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for client QR code tagging, Excel import/preview, lookup and ADC check.
 * Ported from sclera-cloud-vdms ClientQrCodeController; cloud orgId/email path vars
 * moved to @RequestParam; HttpServletRequest dropped.
 *
 * OMITTED endpoints (service method absent):
 *   - getClientQrCodeRecordsByVdmsIdAndLastSyncTime — clientQrCodeService.getClientQrCodeRecordsByVdmsIdAndLastSyncTime absent
 *   - updateClientQrCodeDetails (PUT /vdms/{vdmsId}/clientQrCode) — clientQrCodeService.updateClientQrCodeDetails absent
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Validated
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class ClientQrCodeController {

    private static final Logger log = LoggerFactory.getLogger(ClientQrCodeController.class);

    @Autowired
    private ClientQrCodeService clientQrCodeService;

    /**
     * Tags (or re-tags) a client QR code to a device or location.
     *
     * @param orgId           customer organisation identifier
     * @param email           operator email
     * @param clientQrCodeDTO payload carrying clientQrCodeId, deviceId/locationId and vdmsId
     * @param loggedInUser    audit user string
     */
    @PostMapping("/clientQrCode")
    public ResponseEntity<ResponseDTO> tagClientQrCode(
            @RequestParam String orgId,
            @RequestParam String email,
            @RequestBody ClientQrCodeDTO clientQrCodeDTO,
            @RequestParam String loggedInUser) {

        log.info("tagClientQrCode orgId={} email={} loggedInUser={}", orgId, email, loggedInUser);
        return clientQrCodeService.tagClientQrCode(orgId, email, clientQrCodeDTO, loggedInUser);
    }

    /**
     * Returns the client QR code record for the given (possibly URL-encoded) clientQrCodeId.
     *
     * @param clientQrCodeId client QR code identifier (may be URL-encoded)
     */
    @GetMapping(value = "/clientQrCode/getClientQrCodeDetailsByClientQrCodeId")
    public ResponseEntity<ResponseDTO> getClientQrCodeDetailsByClientQrCodeId(
            @RequestParam String clientQrCodeId) {

        log.info("getClientQrCodeDetailsByClientQrCodeId clientQrCodeId={}", clientQrCodeId);
        return clientQrCodeService.getClientQrCodeDetailsByClientQrCodeId(clientQrCodeId);
    }

    /**
     * Returns client QR codes for the given VDMS filtered by device identifier.
     *
     * @param vdmsId   VDMS identifier
     * @param deviceId device identifier
     */
    @GetMapping(value = "/vdms/{vdmsId}/deviceId/{deviceId}/getClientQrCodeDetailsByVdmsIdAndDeviceId")
    public ResponseEntity<ResponseDTO> getClientQrCodeDetailsByVdmsIdAndDeviceId(
            @PathVariable String vdmsId,
            @PathVariable String deviceId) {

        log.info("getClientQrCodeDetailsByVdmsIdAndDeviceId vdmsId={} deviceId={}", vdmsId, deviceId);
        return clientQrCodeService.getClientQrCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
    }

    /**
     * Returns client QR codes for the given VDMS filtered by location identifier.
     *
     * @param vdmsId     VDMS identifier
     * @param locationId location identifier
     */
    @GetMapping(value = "/vdms/{vdmsId}/locationId/{locationId}/getClientQrCodeDetailsByVdmsIdAndLocationId")
    public ResponseEntity<ResponseDTO> getClientQrCodeDetailsByVdmsIdAndLocationId(
            @PathVariable String vdmsId,
            @PathVariable String locationId) {

        log.info("getClientQrCodeDetailsByVdmsIdAndLocationId vdmsId={} locationId={}", vdmsId, locationId);
        return clientQrCodeService.getClientQrCodeDetailsByVdmsIdAndLocationId(vdmsId, locationId);
    }

    /**
     * Imports client QR code records from the supplied Excel workbook.
     *
     * @param orgId        customer organisation identifier
     * @param email        email of the importing user
     * @param file         the XLSX or XLS workbook (must have headers: client_qr_code_id, device_id, location_id, vdms_id)
     * @param loggedInUser audit user string
     */
    @PostMapping("/importClientQrCode")
    public ResponseEntity<ResponseDTO> importClientQrCode(
            @RequestParam String orgId,
            @RequestParam String email,
            @RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "loggedInUser") String loggedInUser) {

        log.info("importClientQrCode orgId={} email={} loggedInUser={}", orgId, email, loggedInUser);
        return clientQrCodeService.importClientQrCode(orgId, email, file, loggedInUser);
    }

    /**
     * Returns a page of untagged client QR code identifiers.
     *
     * @param orgId        customer organisation identifier
     * @param email        operator email
     * @param vdmsId       VDMS identifier
     * @param loggedInUser audit user string
     * @param pageNo       1-based page number (1-1000)
     * @param pageSize     records per page (1-1000)
     */
    @GetMapping("/vdms/{vdmsId}/getUnTaggedClientQrCode")
    public ResponseEntity<ResponseDTO> getUnTaggedClientQrCode(
            @RequestParam String orgId,
            @RequestParam String email,
            @PathVariable String vdmsId,
            @RequestParam(name = "loggedInUser") String loggedInUser,
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize) {

        log.info("getUnTaggedClientQrCode vdmsId={} pageNo={} pageSize={}", vdmsId, pageNo, pageSize);
        return clientQrCodeService.getUnTaggedClientQrCode(orgId, email, vdmsId, loggedInUser, pageNo, pageSize);
    }

    /**
     * Validates the structure and content of an uploaded Excel file before import.
     *
     * @param vdmsId       the expected VDMS identifier every row must carry
     * @param file         the uploaded Excel workbook
     * @param loggedInUser audit user string
     */
    @PostMapping(value = "/vdms/{vdmsId}/clientQrCode/preview")
    public ResponseEntity<ResponseDTO> previewExcelSheet(
            @PathVariable String vdmsId,
            @RequestParam(name = "file") MultipartFile file,
            @RequestParam(name = "loggedInUser") String loggedInUser) {

        log.info("previewExcelSheet vdmsId={} loggedInUser={}", vdmsId, loggedInUser);
        return clientQrCodeService.previewExcelSheet(vdmsId, file, loggedInUser);
    }

    /**
     * Returns the ADC tagging status for the given (possibly URL-encoded) clientQrCodeId.
     *
     * @param clientQrCodeId client QR code identifier (may be URL-encoded)
     */
    @GetMapping(value = "/clientQrCode/getClientQrCodeCheckById")
    public ResponseEntity<ResponseDTO> getClientQrCodeCheckById(
            @RequestParam String clientQrCodeId) {

        log.info("getClientQrCodeCheckById clientQrCodeId={}", clientQrCodeId);
        return clientQrCodeService.getAdcCheckByClientQrCodeId(clientQrCodeId);
    }
}
