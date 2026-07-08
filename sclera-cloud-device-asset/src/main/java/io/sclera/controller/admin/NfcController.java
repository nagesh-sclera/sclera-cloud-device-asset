package io.sclera.controller.admin;

import io.sclera.dto.NfcDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.service.NfcService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for generated NFC tag lookup, tagging and check.
 * Mirrors {@link QrCodeController}; DB-only paths, no cloud machinery.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Validated
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class NfcController {

    private static final Logger log = LoggerFactory.getLogger(NfcController.class);

    @Autowired
    private NfcService nfcService;

    /**
     * Returns generated NFC tags for the given vdmsId and deviceId.
     */
    @GetMapping(value = "/vdms/{vdmsId}/deviceId/{deviceId}/getNfcDetailsByVdmsIdAndDeviceId")
    public ResponseEntity<ResponseDTO> getNfcDetailsByVdmsIdAndDeviceId(
            @PathVariable String vdmsId,
            @PathVariable String deviceId) {

        log.info("getNfcDetailsByVdmsIdAndDeviceId vdmsId={} deviceId={}", vdmsId, deviceId);
        return nfcService.getNfcDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
    }

    /**
     * Tags/untags a generated NFC tag by device/location assignment.
     */
    @PostMapping(value = "/nfc/updateNfc")
    public ResponseEntity<ResponseDTO> updateNfc(
            @RequestBody NfcDTO nfcDTO,
            @RequestParam String loggedInUser) {

        log.info("updateNfc loggedInUser={}", loggedInUser);
        return nfcService.updateNfcDetails(nfcDTO);
    }

    /**
     * Returns a paginated list of untagged generated NFC identifiers for the given vdmsId.
     */
    @GetMapping("/vdms/{vdmsId}/getUnTaggedNfc")
    public ResponseEntity<ResponseDTO> getUnTaggedNfc(
            @PathVariable String vdmsId,
            @RequestParam(name = "loggedInUser") String loggedInUser,
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize) {

        log.info("getUnTaggedNfc vdmsId={} pageNo={} pageSize={}", vdmsId, pageNo, pageSize);
        return nfcService.getUnTaggedNfc(vdmsId, loggedInUser, pageNo, pageSize);
    }

    /**
     * Checks whether a generated NFC tag exists / is tagged.
     */
    @GetMapping(value = "/nfc/{id}/getNfcCheckById")
    public ResponseEntity<ResponseDTO> getNfcCheckById(@PathVariable String id) {
        log.info("getNfcCheckById id={}", id);
        return nfcService.getNfcCheckById(id);
    }
}
