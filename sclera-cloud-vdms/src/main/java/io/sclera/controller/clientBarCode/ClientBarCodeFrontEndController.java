package io.sclera.controller.clientBarCode;


import com.alibaba.fastjson2.JSONObject;
import io.sclera.dto.ClientBarCodeDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.service.ClientBarCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;

@RequestMapping("/api")
@RestController
public class ClientBarCodeFrontEndController {

    @Autowired
    private ClientBarCodeService clientBarCodeService;


    @PostMapping("/org/{orgId}/email/{email}/clientBarCode/tagClientBarCode")
    public ResponseEntity<?> tagClientBarCode(@PathVariable String orgId, @PathVariable String email, @RequestBody ClientBarCodeDTO clientBarCodeDTO,
                                              @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return clientBarCodeService.tagClientBarCode(orgId, email, clientBarCodeDTO, loggedInUser, httpServletRequest);

    }

    @PostMapping(value = "/getClientBarCodeProxyProfileByVdmsId")
    public ResponseEntity<?> getClientBarCodeProxyProfileByVdmsId(@RequestParam(required = false) String loggedInUser, @RequestBody JSONObject body,
                                                                  HttpServletRequest httpServletRequest) {
        return clientBarCodeService.getClientBarCodeProxyProfileByVdmsId(loggedInUser, body, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/deviceId/{deviceId}/getClientBarCodeDetailsByVdmsIdAndDeviceId")
    public ResponseEntity<?> getBarCodeDetailsByVdmsIdAndDeviceId(@PathVariable String vdmsId, @PathVariable String deviceId, HttpServletRequest httpServletRequest) {
        return clientBarCodeService.getClientBarCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/locationId/{locationId}/getClientBarCodeDetailsByVdmsIdAndLocationId")
    public ResponseEntity<?> getBarCodeDetailsByVdmsIdAndLocationId(@PathVariable String vdmsId, @PathVariable String locationId, HttpServletRequest httpServletRequest) {
        return clientBarCodeService.getClientBarCodeDetailsByVdmsIdAndLocationId(vdmsId, locationId, httpServletRequest);
    }

    @GetMapping(value = "/clientBarCode/{clientBarCodeId}/getClientBarCodeDetailsByClientBarCodeId")
    public ResponseEntity<?> getClientBarCodeDetailsByClientBarCodeId(@PathVariable String clientBarCodeId, HttpServletRequest httpServletRequest) {
        return clientBarCodeService.getClientBarCodeDetailsByClientBarCodeId(clientBarCodeId, httpServletRequest);
    }


    @PostMapping("/org/{orgId}/email/{email}/clientBarCode/importClientBarCode")
    public ResponseEntity<?> importClientBarCode(@PathVariable String orgId, @PathVariable String email,
                                                @RequestParam(name = "file") MultipartFile file,
                                                @RequestParam(name = "loggedInUser") String loggedInUser,HttpServletRequest httpServletRequest) {
        return clientBarCodeService.importClientBarCode(orgId, email, file, loggedInUser,httpServletRequest);

    }

    @GetMapping("/org/{orgId}/email/{email}/vdms/{vdmsId}/clientBarCode")
    public ResponseEntity<ResponseDTO> getClientBarCodeRecordsByVdmsIdAndLastSyncTime(
            @PathVariable String orgId,
            @PathVariable String email,
            @PathVariable String vdmsId,
            @RequestParam(name = "lastSyncTime") BigInteger lastSyncTime,
            @RequestParam(name = "loggedInUser") String loggedInUser,
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
            HttpServletRequest httpServletRequest) {

        return clientBarCodeService.getClientBarCodeRecordsByVdmsIdAndLastSyncTime(
                orgId, email, vdmsId, lastSyncTime, loggedInUser, pageNo, pageSize, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getBarCodeCounts")
    public ResponseEntity<ResponseDTO> getBarCodeCountsByVdmsId(@PathVariable String vdmsId,@RequestParam(name = "lastSyncTime") BigInteger lastSyncTime,HttpServletRequest httpServletRequest){
        return clientBarCodeService.getBarCodeCountsByVdmsId(vdmsId,lastSyncTime,httpServletRequest);
    }

    @GetMapping(value = "/clientBarCode/{clientBarCodeId}/getClientBarCodeCheckById")
    public ResponseEntity<ResponseDTO>getClientBarCodeCheckById(@PathVariable String clientBarCodeId){
        return clientBarCodeService.getAdcCheckByClientBarCodeId(clientBarCodeId);
    }


}
