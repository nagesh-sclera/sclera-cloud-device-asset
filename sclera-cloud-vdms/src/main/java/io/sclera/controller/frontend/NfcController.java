package io.sclera.controller.frontend;

import io.sclera.dto.NfcDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.service.NfcService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


@RestController
@RequestMapping("/api")
public class NfcController {
    @Autowired
    private NfcService nfcService;

    @PostMapping(value = "/user/{email}/vdms/{vdmsId}/addNFC")
    public ResponseEntity<?> addNFC(@RequestParam String loggedInUser, @RequestBody NfcDTO nfcDTO, @PathVariable String email, @PathVariable String vdmsId, HttpServletRequest httpServletRequest) {

        return nfcService.addNFC(nfcDTO, email, vdmsId, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/device/{deviceId}/vdms/{vdmsId}/getNfcDetailsByDeviceIdAndVdmsId")
    public ResponseEntity<?> getNfcDetailsByDeviceIdAndVdmsId(@RequestParam String loggedInUser, @PathVariable String deviceId, @PathVariable String vdmsId, HttpServletRequest httpServletRequest) {

        return nfcService.getNfcDetailsByDeviceIdAndVdmsId(deviceId, vdmsId, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/location/{locationId}/vdms/{vdmsId}/getNfcDetailsByLocationIdAndVdmsId")
    public ResponseEntity<?> getNfcDetailsByLocationIdAndVdmsId(@RequestParam String loggedInUser, @PathVariable String locationId, @PathVariable String vdmsId, HttpServletRequest httpServletRequest) {

        return nfcService.getNfcDetailsByLocationIdAndVdmsId(locationId, vdmsId, loggedInUser, httpServletRequest);
    }
    @GetMapping("/org/{orgId}/email/{email}/vdms/{vdmsId}/nfcRecords")
    public ResponseEntity<ResponseDTO> getNfcRecordsByVdmsId(
            @PathVariable String orgId,
            @PathVariable String email,
            @PathVariable String vdmsId,
            @RequestParam(name = "loggedInUser") String loggedInUser,
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
            HttpServletRequest httpServletRequest) {
        return nfcService.getNfcRecordsByVdmsId(
                orgId, email, vdmsId, loggedInUser, pageNo, pageSize, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateNfcDetails")
    public ResponseEntity<ResponseDTO> updateNfcDetails(@PathVariable String vdmsId, @RequestBody List<NfcDTO> nfcDTOS, HttpServletRequest httpServletRequest) {
        return nfcService.updateNfcDetails(vdmsId,nfcDTOS, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getNfcCounts")
    public ResponseEntity<ResponseDTO> getNfcCounts(@PathVariable String vdmsId, HttpServletRequest httpServletRequest){
        return nfcService.getNfcCounts(vdmsId,httpServletRequest);
    }
    @GetMapping("/vdms/{vdmsId}/getUnTaggedNfc")
    public ResponseEntity<ResponseDTO> getUnTaggedNfc( @PathVariable String vdmsId,
                                                       @RequestParam(name = "loggedInUser") String loggedInUser,
                                                       @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                       @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
                                                       HttpServletRequest httpServletRequest) {
        return nfcService.getUnTaggedNfc( vdmsId, loggedInUser, pageNo, pageSize, httpServletRequest);
    }

}
