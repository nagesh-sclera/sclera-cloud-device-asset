package io.sclera.controller.admin;

import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.service.ClientBarCodeService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for client bar code tagging, lookup and ADC check.
 * Mirrors {@link ClientQrCodeController}; DB-only paths, no cloud machinery.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Validated
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class ClientBarCodeController {

    private static final Logger log = LoggerFactory.getLogger(ClientBarCodeController.class);

    @Autowired
    private ClientBarCodeService clientBarCodeService;

    /**
     * Tags (or re-tags) a client bar code to a device or location.
     */
    @PostMapping("/clientBarCode")
    public ResponseEntity<ResponseDTO> tagClientBarCode(
            @RequestParam String orgId,
            @RequestParam String email,
            @RequestBody ClientBarCodeDTO clientBarCodeDTO,
            @RequestParam String loggedInUser) {

        log.info("tagClientBarCode orgId={} email={} loggedInUser={}", orgId, email, loggedInUser);
        return clientBarCodeService.tagClientBarCode(orgId, email, clientBarCodeDTO, loggedInUser);
    }

    /**
     * Returns client bar codes for the given VDMS filtered by device identifier.
     */
    @GetMapping(value = "/vdms/{vdmsId}/deviceId/{deviceId}/getClientBarCodeDetailsByVdmsIdAndDeviceId")
    public ResponseEntity<ResponseDTO> getClientBarCodeDetailsByVdmsIdAndDeviceId(
            @PathVariable String vdmsId,
            @PathVariable String deviceId) {

        log.info("getClientBarCodeDetailsByVdmsIdAndDeviceId vdmsId={} deviceId={}", vdmsId, deviceId);
        return clientBarCodeService.getClientBarCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
    }

    /**
     * Returns a page of untagged client bar codes.
     */
    @GetMapping("/vdms/{vdmsId}/getUnTaggedClientBarCode")
    public ResponseEntity<ResponseDTO> getUnTaggedClientBarCode(
            @RequestParam String orgId,
            @RequestParam String email,
            @PathVariable String vdmsId,
            @RequestParam(name = "loggedInUser") String loggedInUser,
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize) {

        log.info("getUnTaggedClientBarCode vdmsId={} pageNo={} pageSize={}", vdmsId, pageNo, pageSize);
        return clientBarCodeService.getUnTaggedClientBarCode(orgId, email, vdmsId, loggedInUser, pageNo, pageSize);
    }

    /**
     * Returns the ADC tagging status for the given clientBarCodeId.
     */
    @GetMapping(value = "/clientBarCode/getClientBarCodeCheckById")
    public ResponseEntity<ResponseDTO> getClientBarCodeCheckById(
            @RequestParam String clientBarCodeId) {

        log.info("getClientBarCodeCheckById clientBarCodeId={}", clientBarCodeId);
        return clientBarCodeService.getAdcCheckByClientBarCodeId(clientBarCodeId);
    }

    /**
     * Returns the client bar code record for the given clientBarCodeId.
     */
    @GetMapping(value = "/clientBarCode/getClientBarCodeDetailsByClientBarCodeId")
    public ResponseEntity<ResponseDTO> getClientBarCodeDetailsByClientBarCodeId(
            @RequestParam String clientBarCodeId) {

        log.info("getClientBarCodeDetailsByClientBarCodeId clientBarCodeId={}", clientBarCodeId);
        return clientBarCodeService.getClientBarCodeDetailsByClientBarCodeId(clientBarCodeId);
    }
}
