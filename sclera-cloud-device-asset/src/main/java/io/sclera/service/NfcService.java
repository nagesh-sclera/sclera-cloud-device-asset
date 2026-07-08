package io.sclera.service;

import io.sclera.Repository.NfcRepository;
import io.sclera.dto.NfcDTO;
import io.sclera.integration.dto.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Local read/tag/untag surface for generated NFC tags. Mirrors {@code QrCodeService};
 * cloud-sync, WebSocket and multi-tenant machinery are omitted.
 */
@Service("nfcService")
public class NfcService {

    private static final Logger log = LoggerFactory.getLogger(NfcService.class);

    @Autowired
    NfcRepository nfcRepository;

    /**
     * Returns generated NFC tags for the given VDMS filtered by device identifier.
     */
    public ResponseEntity<ResponseDTO> getNfcDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId) {
        log.info("getNfcDetailsByVdmsIdAndDeviceId: vdmsId={}, deviceId={}", vdmsId, deviceId);
        List<NfcDTO> results = nfcRepository.getNfcDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
        ResponseDTO resp = new ResponseDTO(null, 200, results, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Tags (or untags) a generated NFC tag by setting its device/location assignment.
     * Setting deviceId (and locationId) to null untags the NFC.
     */
    public ResponseEntity<ResponseDTO> updateNfcDetails(NfcDTO nfcDTO) {
        log.info("updateNfcDetails: id={}", nfcDTO != null ? nfcDTO.getId() : null);
        if (nfcDTO == null || nfcDTO.getId() == null) {
            log.error("updateNfcDetails: invalid payload");
            ResponseDTO err = new ResponseDTO("Invalid client params", 700, false,
                    BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        }
        nfcRepository.updateNfcDetailsById(nfcDTO.getDeviceId(), nfcDTO.getLocationId(), nfcDTO.getId());
        ResponseDTO resp = new ResponseDTO("NFC Details Updated Successfully", 200, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Returns a page of untagged generated NFC identifiers (no vdms/device/location assigned).
     */
    public ResponseEntity<ResponseDTO> getUnTaggedNfc(String vdmsId, String loggedInUser, int pageNo, int pageSize) {
        log.info("getUnTaggedNfc: vdmsId={} pageNo={} pageSize={}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<String> ids = nfcRepository.getUnTaggedNfc(pageSize, offset);
        ResponseDTO resp = new ResponseDTO(null, 200, ids, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Returns the tagging status for the given generated NFC id.
     * isTagged: 2 = not in DB, otherwise 1 (present).
     */
    public ResponseEntity<ResponseDTO> getNfcCheckById(String id) {
        log.info("getNfcCheckById: id={}", id);
        Map<String, Object> result = new HashMap<>();
        int isPresentInDb = nfcRepository.checkNfcId(id);
        result.put("isTagged", isPresentInDb == 0 ? 2 : 1);
        ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
