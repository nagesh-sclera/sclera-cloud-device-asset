package io.sclera.controller.adcTagging;

import io.sclera.dto.*;
import io.sclera.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AdcTaggingMobileController {

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

    @GetMapping(value = "/clientBarCode/{clientBarCodeId}/getAdcCheckByClientBarCodeId")
    public ResponseEntity<ResponseDTO>getAdcCheckByClientBarCodeId(@PathVariable String clientBarCodeId) {
        return clientBarCodeService.getAdcCheckByClientBarCodeId(clientBarCodeId);
    }

    @GetMapping(value = "/clientQrCode/{clientQrCodeId}/getAdcCheckByClientQrCodeId")
    public ResponseEntity<ResponseDTO>getAdcCheckByClientQrCodeId(@PathVariable String clientQrCodeId) {
        return clientQrCodeService.getAdcCheckByClientQrCodeId(clientQrCodeId);
    }

    @GetMapping(value = "/clientNfc/{clientNfcId}/getAdcCheckByClientNfcId")
    public ResponseEntity<ResponseDTO>getAdcCheckByClientNfcId(@PathVariable String clientNfcId) {
        return clientNfcService.getAdcCheckByClientNfcId(clientNfcId);
    }

    @GetMapping(value = "/qrCode/{qrCodeId}/getAdcCheckByQrCodeId")
    public ResponseEntity<ResponseDTO>getAdcCheckByQrCodeId(@PathVariable String qrCodeId) {
        return qrCodeService.getAdcCheckByQrCodeId(qrCodeId);
    }

    @PostMapping(value = "/clientBarCode/addClientBarCode")
    public ResponseEntity<ResponseDTO> addClientBarCode(@RequestBody ClientBarCodeDTO clientBarCodeDTO, HttpServletRequest httpServletRequest) {
        return clientBarCodeService.addClientBarCode(clientBarCodeDTO,httpServletRequest);
    }

    @PostMapping(value = "/clientQrCode/addClientQrCode")
    public ResponseEntity<ResponseDTO> addClientQrCode(@RequestBody ClientQrCodeDTO clientQrCodeDTO,HttpServletRequest httpServletRequest) {
        return clientQrCodeService.addClientQrCode(clientQrCodeDTO,httpServletRequest);
    }

    @PostMapping(value = "/clientNfc/addClientNfc")
    public ResponseEntity<ResponseDTO> addClientNfc(@RequestBody ClientNfcDTO clientNfcDTO,HttpServletRequest httpServletRequest) {
        return clientNfcService.addClientNfc(clientNfcDTO,httpServletRequest);
    }

    @PutMapping(value = "/qrCode/{qrCodeId}/updateQrcodeById")
    public ResponseEntity<ResponseDTO>updateQrcodeById(@PathVariable String qrCodeId, @RequestBody QrCodeDTO qrCodeDTO) {
        return qrCodeService.updateQrcodeById(qrCodeId,qrCodeDTO);
    }

    @PutMapping(value = "/clientQrCode/{clientQrCodeId}/updateClientQrCodeById")
    public ResponseEntity<ResponseDTO>updateClientQrCodeById(@PathVariable String clientQrCodeId, @RequestBody ClientQrCodeDTO clientQrCodeDTO) {
        return clientQrCodeService.updateClientQrCodeById(clientQrCodeId,clientQrCodeDTO);
    }

    @PutMapping(value = "/clientNfc/{clientNfcId}/updateClientNfcById")
    public ResponseEntity<ResponseDTO>updateClientNfcById(@PathVariable String clientNfcId, @RequestBody ClientNfcDTO clientNfcDTO) {
        return clientNfcService.updateClientNfcById(clientNfcId,clientNfcDTO);
    }

    @PutMapping(value = "/clientBarCode/{clientBarCodeId}/updateClientBarCodeById")
    public ResponseEntity<ResponseDTO>updateClientBarCodeById(@PathVariable String clientBarCodeId, @RequestBody ClientBarCodeDTO clientBarCodeDTO) {
        return clientBarCodeService.updateClientBarCodeById(clientBarCodeId,clientBarCodeDTO);
    }

}
