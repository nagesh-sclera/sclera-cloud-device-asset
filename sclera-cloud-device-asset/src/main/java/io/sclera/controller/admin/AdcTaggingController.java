package io.sclera.controller.admin;

import io.sclera.dto.QrCodeDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.service.QrCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

/**
 * REST controller for ADC tagging operations on QR codes.
 * Ported from sclera-cloud-vdms AdcTaggingTouchscreenController.
 *
 * Only the QR code tag-by-vdmsId endpoint is wired here because:
 *   - tagClientQrCodeByVdmsId — clientQrCodeService.tagClientQrCodeByVdmsId does NOT exist locally (OMITTED)
 *   - tagClientBarCodeByVdmsId — ClientBarCodeService is not in scope for this module (OMITTED)
 *   - tagClientNfcByVdmsId     — ClientNfcService is not in scope for this module (OMITTED)
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class AdcTaggingController {

    private static final Logger log = LoggerFactory.getLogger(AdcTaggingController.class);

    @Autowired
    private QrCodeService qrCodeService;

    /**
     * Tags a batch of QR codes to the given vdmsId/orgId context.
     * Cloud WebSocket/multi-tenant sync is omitted in this self-contained deployment.
     *
     * @param orgId          customer organisation identifier
     * @param vdmsId         VDMS identifier
     * @param qrCodeDTOList  list of QR code DTOs carrying deviceId/locationId assignments
     */
    @PutMapping(value = "/touchscreen/qrCode/organisation/{orgId}/vdms/{vdmsId}/tagQrCodeByVdmsId")
    public ResponseEntity<ResponseDTO> tagQrCodeByVdmsId(
            @PathVariable String orgId,
            @PathVariable String vdmsId,
            @RequestBody List<QrCodeDTO> qrCodeDTOList) {

        log.info("tagQrCodeByVdmsId orgId={} vdmsId={} count={}", orgId, vdmsId,
                qrCodeDTOList != null ? qrCodeDTOList.size() : 0);
        qrCodeService.tagQrCodeByVdmsId(orgId, vdmsId, qrCodeDTOList);
        ResponseDTO resp = new ResponseDTO("QR codes tagged successfully", 200, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
