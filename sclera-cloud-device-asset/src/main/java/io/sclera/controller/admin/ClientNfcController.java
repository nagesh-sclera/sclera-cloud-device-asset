package io.sclera.controller.admin;

import io.sclera.dto.ClientNfcDTO;
import io.sclera.integration.dto.ResponseDTO;
import io.sclera.service.impl.ClientNfcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for client NFC tag lookup, tagging and ADC check.
 * Mirrors {@link ClientQrCodeController}; DB-only paths, no cloud machinery.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Validated
@RequestMapping("/api/v1/sclera-cloud-device-asset-service")
public class ClientNfcController {

    private static final Logger log = LoggerFactory.getLogger(ClientNfcController.class);

    @Autowired
    private ClientNfcService clientNfcService;

    /**
     * Returns client NFC tags for the given vdmsId and deviceId.
     */
    @GetMapping(value = "/vdms/{vdmsId}/deviceId/{deviceId}/getClientNfcDetailsByVdmsIdAndDeviceId")
    public ResponseEntity<ResponseDTO> getClientNfcDetailsByVdmsIdAndDeviceId(
            @PathVariable String vdmsId,
            @PathVariable String deviceId) {

        log.info("getClientNfcDetailsByVdmsIdAndDeviceId vdmsId={} deviceId={}", vdmsId, deviceId);
        return clientNfcService.getClientNfcDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
    }

    /**
     * Tags (or re-tags) a client NFC to a device or location.
     */
    @PostMapping("/clientNfc")
    public ResponseEntity<ResponseDTO> tagClientNfc(
            @RequestParam String orgId,
            @RequestParam String email,
            @RequestBody ClientNfcDTO clientNfcDTO,
            @RequestParam String loggedInUser) {

        log.info("tagClientNfc orgId={} email={} loggedInUser={}", orgId, email, loggedInUser);
        return clientNfcService.tagClientNfc(orgId, email, clientNfcDTO, loggedInUser);
    }

    /**
     * Returns the ADC tagging status for the given clientNfcId.
     */
    @GetMapping(value = "/clientNfc/getClientNfcCheckById")
    public ResponseEntity<ResponseDTO> getClientNfcCheckById(
            @RequestParam String clientNfcId) {

        log.info("getClientNfcCheckById clientNfcId={}", clientNfcId);
        return clientNfcService.getAdcCheckByClientNfcId(clientNfcId);
    }
}
