package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.Repository.ClientNfcRepository;
import io.sclera.dto.ClientNfcDTO;
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
 * Local read/tag/untag surface for client NFC tags. Mirrors {@code ClientQrCodeService};
 * cloud-sync, WebSocket and multi-tenant machinery are omitted.
 */
@Service("clientNfcService")
public class ClientNfcService {

    private static final Logger log = LoggerFactory.getLogger(ClientNfcService.class);

    @Autowired
    ClientNfcRepository clientNfcRepository;

    /**
     * Returns client NFC tags for the given VDMS filtered by device identifier.
     */
    public ResponseEntity<ResponseDTO> getClientNfcDetailsByVdmsIdAndDeviceId(String vdmsId, String deviceId) {
        log.info("getClientNfcDetailsByVdmsIdAndDeviceId: vdmsId={}, deviceId={}", vdmsId, deviceId);
        List<ClientNfcDTO> results = clientNfcRepository.getClientNfcDetailsByVdmsIdAndDeviceId(vdmsId, deviceId);
        ResponseDTO resp = new ResponseDTO(null, 200, results, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    /**
     * Tags (or re-tags) a client NFC to a device or location. If the NFC is not found locally
     * it is inserted as a new record. Setting deviceId (and locationId) to null untags the NFC.
     */
    public ResponseEntity<ResponseDTO> tagClientNfc(String orgId, String email,
                                                    ClientNfcDTO clientNfcDTO, String loggedInUser) {
        log.info("tagClientNfc: orgId={}, email={}, loggedInUser={}", orgId, email, loggedInUser);
        if (clientNfcDTO == null) {
            log.error("tagClientNfc: null payload");
            ResponseDTO err = new ResponseDTO("Invalid client params", 700, false,
                    BigInteger.valueOf(System.currentTimeMillis()));
            return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
        }

        BigInteger now = BigInteger.valueOf(System.currentTimeMillis());
        String batchId = Generators.timeBasedGenerator().generate().toString();

        int existsCount = clientNfcRepository.checkClientNfcId(clientNfcDTO.getNfcId());
        if (existsCount > 0) {
            clientNfcRepository.tagClientNfc(
                    clientNfcDTO.getDeviceId(),
                    clientNfcDTO.getLocationId(),
                    clientNfcDTO.getVdmsId(),
                    batchId,
                    clientNfcDTO.getNfcId());
            log.info("tagClientNfc: updated nfcId={}", clientNfcDTO.getNfcId());
        } else {
            String newId = Generators.timeBasedGenerator().generate().toString();
            clientNfcRepository.addClientNFC(
                    newId,
                    clientNfcDTO.getNfcId(),
                    now,
                    email,
                    clientNfcDTO.getDeviceId(),
                    clientNfcDTO.getLocationId(),
                    clientNfcDTO.getVdmsId(),
                    batchId);
            log.info("tagClientNfc: inserted nfcId={}", clientNfcDTO.getNfcId());
        }

        ResponseDTO responseDTO = new ResponseDTO("Client NFC Details Updated Successfully",
                200, true, BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    /**
     * Returns the ADC tagging status for the given client NFC id (nfcId).
     * isTagged: 2 = not in DB, 0 = present but not tagged, 1 = present and tagged.
     */
    public ResponseEntity<ResponseDTO> getAdcCheckByClientNfcId(String nfcId) {
        log.info("getAdcCheckByClientNfcId: nfcId={}", nfcId);
        Map<String, Object> result = new HashMap<>();
        int isPresentInDb = clientNfcRepository.checkClientNfcId(nfcId);
        if (isPresentInDb == 0) {
            result.put("isTagged", 2);
            result.put("isAdc", false);
        } else {
            int adcCheck = clientNfcRepository.getAdcCheckByClientNfcId(nfcId);
            result.put("isTagged", adcCheck > 0 ? 1 : 0);
            result.put("isAdc", adcCheck == 1);
        }
        ResponseDTO resp = new ResponseDTO(null, 200, result, true,
                BigInteger.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }
}
