package io.sclera.controller.frontend;


import io.sclera.dto.ClientNfcDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.service.ClientNfcService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/org/{orgId}/email/{email}")
public class ClientNfcController {


    @Autowired
    private ClientNfcService clientNfcService;



    @GetMapping(value = "/device/{deviceId}/vdms/{vdmsId}/getClientNfcDetailsByDeviceIdAndVdmsId")
    public ResponseEntity<?> getNfcNfcDetailsByDeviceIdAndVdmsId(@RequestParam String loggedInUser, @PathVariable String deviceId, @PathVariable String vdmsId, HttpServletRequest httpServletRequest) {

        return clientNfcService.getClientNfcDetailsByDeviceIdAndVdmsId(deviceId, vdmsId, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/location/{locationId}/vdms/{vdmsId}/getClientNfcDetailsByLocationIdAndVdmsId")
    public ResponseEntity<?> getNfcNfcDetailsByLocationIdAndVdmsId(@RequestParam String loggedInUser, @PathVariable String locationId, @PathVariable String vdmsId, HttpServletRequest httpServletRequest) {

        return clientNfcService.getClientNfcDetailsByLocationIdAndVdmsId(locationId, vdmsId, loggedInUser, httpServletRequest);
    }


    @PostMapping("/importClientNfc")
    public ResponseEntity<?> importClientNfc(@PathVariable String orgId, @PathVariable String email,
                                                @RequestParam(name = "file") MultipartFile file,
                                                @RequestParam(name = "loggedInUser") String loggedInUser,HttpServletRequest httpServletRequest) {
        return clientNfcService.importClientNfc(orgId, email, file, loggedInUser,httpServletRequest);

    }
    @GetMapping("/vdms/{vdmsId}/clientNfcRecords")
    public ResponseEntity<ResponseDTO> getClientNfcRecordsByVdmsId(@PathVariable String orgId, @PathVariable String email, @PathVariable String vdmsId,
                                                                   @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                   @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                                   @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
                                                                   HttpServletRequest httpServletRequest) {
        return clientNfcService.getClientNfcRecordsByVdmsId(orgId, email, vdmsId, loggedInUser, pageNo, pageSize, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateClientNfc")
    public ResponseEntity<ResponseDTO> updateClientNfcDetails(@PathVariable String vdmsId, @RequestBody List<ClientNfcDTO> clientNfcDTOS, HttpServletRequest httpServletRequest) {
        return clientNfcService.updateClientNfcDetails(vdmsId, clientNfcDTOS, httpServletRequest);
    }

    @GetMapping("/vdms/{vdmsId}/getUnTaggedClientNfc")
    public ResponseEntity<ResponseDTO> getUnTaggedClientNfc(@PathVariable String orgId, @PathVariable String email, @PathVariable String vdmsId,
                                                            @RequestParam(name = "loggedInUser") String loggedInUser,
                                                            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
                                                            HttpServletRequest httpServletRequest) {
        return clientNfcService.getUnTaggedClientNfc(orgId, email, vdmsId, loggedInUser, pageNo, pageSize, httpServletRequest);
    }

    @PostMapping(value = "/vdms/{vdmsId}/clientNfc/preview")
    public ResponseEntity<ResponseDTO>previewExcelSheet(@PathVariable String vdmsId,
                                                        @RequestParam(name = "file") MultipartFile file,
                                                        @RequestParam(name = "loggedInUser") String loggedInUser){
        return clientNfcService.previewExcelSheet(vdmsId,file,loggedInUser);
    }

    @GetMapping(value = "/clientNfc/{clientNfcId}/getClientNfcDetailsByClientNfcId")
    public ResponseEntity<?> getClientNfcDetailsByClientNfcId(@PathVariable String clientNfcId, HttpServletRequest httpServletRequest) {
        return clientNfcService.getClientNfcDetailsByClientNfcId(clientNfcId, httpServletRequest);
    }
}
