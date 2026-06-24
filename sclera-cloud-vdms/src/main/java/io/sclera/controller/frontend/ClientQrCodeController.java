package io.sclera.controller.frontend;


import io.sclera.dto.ClientQrCodeDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.service.ClientQrCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.util.List;

@RequestMapping("/api/org/{orgId}/email/{email}")
@RestController
public class ClientQrCodeController {

    @Autowired
    private ClientQrCodeService clientQrCodeService;

    @PostMapping("/clientQrCode")
    public ResponseEntity<?> tagClientQrCode(@PathVariable String orgId, @PathVariable String email, @RequestBody ClientQrCodeDTO clientQrCodeDTO,
                                                @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return clientQrCodeService.tagClientQrCode(orgId, email, clientQrCodeDTO, loggedInUser, httpServletRequest);

    }

    @GetMapping(value = "/clientQrCode/getClientQrCodeDetailsByClientQrCodeId")
    public ResponseEntity<?> getClientQrCodeDetailsByClientQrCodeId(@RequestParam String clientQrCodeId, HttpServletRequest httpServletRequest) {
        return clientQrCodeService.getClientQrCodeDetailsByClientQrCodeId(clientQrCodeId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/deviceId/{deviceId}/getClientQrCodeDetailsByVdmsIdAndDeviceId")
    public ResponseEntity<?> getQrCodeDetailsByVdmsIdAndDeviceId(@PathVariable String vdmsId, @PathVariable String deviceId, HttpServletRequest httpServletRequest) {
        return clientQrCodeService.getClientQrCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/locationId/{locationId}/getClientQrCodeDetailsByVdmsIdAndLocationId")
    public ResponseEntity<?> getQrCodeDetailsByVdmsIdAndLocationId(@PathVariable String vdmsId, @PathVariable String locationId, HttpServletRequest httpServletRequest) {
        return clientQrCodeService.getClientQrCodeDetailsByVdmsIdAndLocationId(vdmsId, locationId, httpServletRequest);
    }

    @PostMapping("/importClientQrCode")
    public ResponseEntity<?> importClientQrCode(@PathVariable String orgId, @PathVariable String email,
                                                @RequestParam(name = "file") MultipartFile file,
                                                @RequestParam(name = "loggedInUser") String loggedInUser,HttpServletRequest httpServletRequest) {
        return clientQrCodeService.importClientQrCode(orgId, email, file, loggedInUser,httpServletRequest);

    }

    @GetMapping("/vdms/{vdmsId}/clientQrCodeRecords")
    public ResponseEntity<ResponseDTO> getClientQrCodeRecordsByVdmsIdAndLastSyncTime(@PathVariable String orgId, @PathVariable String email, @PathVariable String vdmsId,
                                                                                     @RequestParam(name = "lastSyncTime") BigInteger lastSyncTime,
                                                                                     @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                                     @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                                                     @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
                                                                                     HttpServletRequest httpServletRequest) {
        return clientQrCodeService.getClientQrCodeRecordsByVdmsIdAndLastSyncTime(orgId, email, vdmsId, lastSyncTime, loggedInUser, pageNo, pageSize, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/clientQrCode")
    public ResponseEntity<?> updateClientQrCodeDetails(@PathVariable String vdmsId, @RequestBody List<ClientQrCodeDTO> clientQrCodeDTOS, HttpServletRequest httpServletRequest) {
        return clientQrCodeService.updateClientQrCodeDetails(vdmsId,clientQrCodeDTOS, httpServletRequest);
    }

    @GetMapping("/vdms/{vdmsId}/getUnTaggedClientQrCode")
    public ResponseEntity<ResponseDTO> getUnTaggedClientQrCode(@PathVariable String orgId, @PathVariable String email, @PathVariable String vdmsId,
                                                               @RequestParam(name = "loggedInUser") String loggedInUser,
                                                               @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                               @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
                                                               HttpServletRequest httpServletRequest) {
        return clientQrCodeService.getUnTaggedClientQrCode(orgId, email, vdmsId, loggedInUser, pageNo, pageSize, httpServletRequest);
    }
    @PostMapping(value = "/vdms/{vdmsId}/clientQrCode/preview")
    public ResponseEntity<ResponseDTO>previewExcelSheet(@PathVariable String vdmsId,
                                                        @RequestParam(name = "file") MultipartFile file,
                                                        @RequestParam(name = "loggedInUser") String loggedInUser){
        return clientQrCodeService.previewExcelSheet(vdmsId,file,loggedInUser);
    }

    @GetMapping(value = "/clientQrCode/getClientQrCodeCheckById")
    public ResponseEntity<ResponseDTO>getClientQrCodeCheckById(@RequestParam String clientQrCodeId){
        return clientQrCodeService.getAdcCheckByClientQrCodeId(clientQrCodeId);
    }
}
