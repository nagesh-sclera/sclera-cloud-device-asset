package io.sclera.controller.frontend;


import io.sclera.dto.QrCodeDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.service.QrCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api")
public class QRCodeController {
  @Autowired private QrCodeService qrCodeService;

  @GetMapping(value = "/touchscreen/org/{orgId}/user/{email}/qrCode/generateQRCode")
  public void generateQRCode(
          @PathVariable String orgId,
          @PathVariable String email,
          @RequestParam(defaultValue = "1") @Min(1) @Max(1000) Integer count,
          @RequestParam(required = false, defaultValue = "2.75") Float width,
          @RequestParam(required = false, defaultValue = "4.0") Float height,
          @RequestParam String type,
          @RequestParam(required = false, defaultValue = "true") boolean brand,
          @RequestParam(required = false, defaultValue = "0") boolean defaultTemplate,
          @RequestParam(required = false, name = "templateUrl") String templateUrl,
          HttpServletResponse httpServletResponse,
          HttpServletRequest httpServletRequest)
          throws IOException {
    qrCodeService.generateQRCode(
            orgId, email, count, width, height, type, brand,defaultTemplate,templateUrl, httpServletResponse, httpServletRequest);
  }
  

  @PostMapping(value = "/touchscreen/org/{orgId}/user/{email}/qrCode/generateBulkQRCode")
  public ResponseEntity<?> generateBulkQRCode(
          @PathVariable String orgId,
          @PathVariable String email,
          @RequestParam(defaultValue = "1") @Min(1001) @Max(50000) Integer count,
          @RequestParam(required = false, defaultValue = "2.75") Float width,
          @RequestParam(required = false, defaultValue = "4.0") Float height,
          @RequestParam String type,
          @RequestBody String emailTo,
          @RequestParam(required = false, defaultValue = "true") boolean brand,
          @RequestParam(required = false, defaultValue = "0") boolean defaultTemplate,
          @RequestParam(required = false, name = "templateUrl") String templateUrl,
          HttpServletRequest httpServletRequest)
          throws IOException {
    return qrCodeService.generateBulkQRCode(
            orgId, email, count, width, height, type, emailTo, brand,defaultTemplate,templateUrl, httpServletRequest);
  }


  @PostMapping(value = "/touchscreen/updateQrCode")
  public ResponseEntity<?> upsertQrcode(
      @RequestBody QrCodeDTO qrCodeDTO,
      @RequestParam String loggedInUser,
      HttpServletRequest httpServletRequest) {
    return qrCodeService.upsertQrcode(qrCodeDTO, loggedInUser, httpServletRequest);
  }

  @GetMapping(value = "/touchscreen/qrCode/{qrCodeId}/getQrCodeDetailsByQrCodeId")
  public ResponseEntity<?> getQrCodeDetailsByQrCodeId(
      @PathVariable String qrCodeId, HttpServletRequest httpServletRequest) {
    return qrCodeService.getQrCodeDetailsByQrCodeId(qrCodeId, httpServletRequest);
  }

  @GetMapping(
      value = "/touchscreen/vdms/{vdmsId}/deviceId/{deviceId}/getQrCodeDetailsByVdmsIdAndDeviceId")
  public ResponseEntity<?> getQrCodeDetailsByVdmsIdAndDeviceId(
      @PathVariable String vdmsId,
      @PathVariable String deviceId,
      HttpServletRequest httpServletRequest) {
    return qrCodeService.getQrCodeDetailsByVdmsIdAndDeviceId(vdmsId, deviceId, httpServletRequest);
  }

  @GetMapping(
      value =
          "/touchscreen/vdms/{vdmsId}/locationId/{locationId}/getQrCodeDetailsByVdmsIdAndLocationId")
  public ResponseEntity<?> getQrCodeDetailsByVdmsIdAndLocationId(
      @PathVariable String vdmsId,
      @PathVariable String locationId,
      HttpServletRequest httpServletRequest) {
    return qrCodeService.getQrCodeDetailsByVdmsIdAndLocationId(
        vdmsId, locationId, httpServletRequest);
  }

  @GetMapping(value = "/vdms/{vdmsId}/tagged/{id}/getQrCodeDetailsByType")
  public ResponseEntity<?> getQrCodeDetailsByType(
      @PathVariable String vdmsId,
      @PathVariable String id,
      @RequestParam String type,
      HttpServletRequest httpServletRequest) {
    return qrCodeService.getQrCodeDetailsByType(vdmsId, id, type, httpServletRequest);
  }

  @GetMapping(value = "/touchscreen/qrCode/email")
  public ResponseEntity<?> getQrCodeRedirectionLink(
      @RequestParam String key, @RequestParam String redirectUrl) {
    HttpHeaders headers = qrCodeService.getQrCodeRedirectionLink(key, redirectUrl);
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }

  @GetMapping("/org/{orgId}/email/{email}/vdms/{vdmsId}/qrCodeRecords")
  public ResponseEntity<ResponseDTO> getQrCodeRecordsByVdmsIdAndLastSyncTime(
          @PathVariable String orgId,
          @PathVariable String email,
          @PathVariable String vdmsId,
          @RequestParam(name = "lastSyncTime") BigInteger lastSyncTime,
          @RequestParam(name = "loggedInUser") String loggedInUser,
          @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
          @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
          HttpServletRequest httpServletRequest) {
    return qrCodeService.getQrCodeRecordsByVdmsIdAndLastSyncTime(
            orgId, email, vdmsId, lastSyncTime, loggedInUser, pageNo, pageSize, httpServletRequest);
  }


  @PutMapping(value = "/vdms/{vdmsId}/updateQrCodeDetails")
  public ResponseEntity<?> updateQrCodeDetails(@PathVariable String vdmsId, @RequestBody List<QrCodeDTO> qrCodeDTOS, HttpServletRequest httpServletRequest) {
    return qrCodeService.updateQrCodeDetails(vdmsId,qrCodeDTOS, httpServletRequest);
  }

  @GetMapping(value = "/vdms/{vdmsId}/getQrCodeCounts")
  public ResponseEntity<?> getQrCodeCounts(@PathVariable String vdmsId, @RequestParam(name = "lastSyncTime") BigInteger lastSyncTime,HttpServletRequest httpServletRequest){
    return qrCodeService.getQrCodeCounts(vdmsId,lastSyncTime,httpServletRequest);
  }
  @GetMapping("/vdms/{vdmsId}/getUnTaggedQrCode")
  public ResponseEntity<ResponseDTO> getUnTaggedQrCode( @PathVariable String vdmsId,
                                                        @RequestParam(name = "loggedInUser") String loggedInUser,
                                                        @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                        @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pageSize,
                                                        HttpServletRequest httpServletRequest) {
    return qrCodeService.getUnTaggedQrCode( vdmsId, loggedInUser, pageNo, pageSize, httpServletRequest);
  }

  @GetMapping(value = "/qrCode/{qrCodeId}/getQrCodeCheckById")
  public ResponseEntity<ResponseDTO>getQrCodeCheckById(@PathVariable String qrCodeId) {
    return qrCodeService.getAdcCheckByQrCodeId(qrCodeId);
  }
}
