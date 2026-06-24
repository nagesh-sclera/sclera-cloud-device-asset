package io.sclera.controller.adcTagging;

import io.sclera.dto.*;
import io.sclera.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/touchscreen")
@RestController
public class AdcTaggingTouchscreenController {

    @Autowired
    private ClientBarCodeService clientBarCodeService;

    @Autowired
    private ClientQrCodeService clientQrCodeService;

    @Autowired
    private ClientNfcService clientNfcService;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private NfcService nfcService;

    @PutMapping(value = "/clientBarCode/organisation/{orgId}/vdms/{vdmsId}/tagClientBarCodeByVdmsId")
    public ResponseEntity<ResponseDTO> tagClientBarCodeByVdmsId(@PathVariable String orgId, @PathVariable String vdmsId, @RequestBody List<ClientBarCodeDTO> clientBarCodeDTOList, HttpServletRequest httpServletRequest) {
        return clientBarCodeService.tagClientBarCodeByVdmsId(orgId,vdmsId, clientBarCodeDTOList, httpServletRequest);
    }

    @PutMapping(value = "/clientQrCode/organisation/{orgId}/vdms/{vdmsId}/tagClientQrCodeByVdmsId")
    public ResponseEntity<ResponseDTO> tagClientQrCodeByVdmsId(@PathVariable String orgId, @PathVariable String vdmsId, @RequestBody List<ClientQrCodeDTO> clientQrCodeDTOList, HttpServletRequest httpServletRequest) {
        return clientQrCodeService.tagClientQrCodeByVdmsId(orgId,vdmsId, clientQrCodeDTOList, httpServletRequest);
    }

    @PutMapping(value = "/clientNfc/organisation/{orgId}/vdms/{vdmsId}/tagClientNfcByVdmsId")
    public ResponseEntity<ResponseDTO>tagClientNfcByVdmsId(@PathVariable String orgId, @PathVariable String vdmsId, @RequestBody List<ClientNfcDTO> clientNfcDTOList, HttpServletRequest httpServletRequest) {
        return clientNfcService.tagClientNfcByVdmsId(orgId,vdmsId, clientNfcDTOList, httpServletRequest);
    }

    @PutMapping(value = "/qrCode/organisation/{orgId}/vdms/{vdmsId}/tagQrCodeByVdmsId")
    public ResponseEntity<ResponseDTO> tagQrCodeByVdmsId(@PathVariable String orgId, @PathVariable String vdmsId, @RequestBody List<QrCodeDTO> qrCodeDTOList, HttpServletRequest httpServletRequest) {
        return qrCodeService.tagQrCodeByVdmsId(orgId,vdmsId, qrCodeDTOList, httpServletRequest);
    }
}
