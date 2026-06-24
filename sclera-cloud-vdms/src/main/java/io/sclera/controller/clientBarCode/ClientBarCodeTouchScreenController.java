package io.sclera.controller.clientBarCode;


import io.sclera.dto.ResponseDTO;
import io.sclera.service.ClientBarCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/touchscreen/clientBarCode")
@RestController
public class ClientBarCodeTouchScreenController {

    @Autowired
    private ClientBarCodeService clientBarCodeService;

    @GetMapping(value = "/vdms/{vdmsId}/getClientBarCodeCountByVdmsId")
    public ResponseEntity<ResponseDTO> getClientBarCodeCountByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return clientBarCodeService.getClientBarCodeCountByVdmsId(vdmsId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getAllClientBarCodeByVdmsId")
    public ResponseEntity<ResponseDTO> getAllClientBarCodeByVdmsId(@PathVariable String vdmsId,
                                                               @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                               @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                               HttpServletRequest httpServletRequest) {
        return clientBarCodeService.getAllClientBarCodeByVdmsId(vdmsId, pageNo, pageSize, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateBarCodeSyncByVdmsId")
    public ResponseEntity<ResponseDTO> updateBarCodeSyncByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return clientBarCodeService.updateBarCodeSyncByVdmsId(vdmsId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getSyncedClientBarCodeByVdmsId")
    public ResponseEntity<ResponseDTO> getSyncedClientBarCodeByVdmsId(@PathVariable String vdmsId,
                                                                      @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                                      @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                                      HttpServletRequest httpServletRequest){
        return clientBarCodeService.getSyncedClientBarCodeByVdmsId(vdmsId, pageNo, pageSize, httpServletRequest);
    }




}
