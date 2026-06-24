package io.sclera.controller.touchscreen;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.*;
import io.sclera.service.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;


@RequestMapping("/api/touchscreen")
@RestController
public class TouchScreenController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private TouchscreenService touchscreenService;


    @PostMapping(value = "/vdms/{vdmsId}/alert")
    public ResponseEntity<?> alertUser(@PathVariable String vdmsId, @RequestBody AlertDTO alertDto, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return alertService.alertUser(vdmsId, alertDto, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/docker/{name}/syncVendorTransferByVdmsIdAndDockerName")
    public ResponseEntity<?> syncVendorTransferByVdmsIdAndDockerName(@PathVariable String vdmsId, @PathVariable String name, HttpServletRequest httpServletRequest) {
        return touchscreenService.syncVendorTransferByVdmsIdAndDockerName(vdmsId, name, httpServletRequest);
    }

    @PostMapping(value = "/vdms/{vdmsId}/upsertDockerByVdmsId")
    public ResponseEntity<?> upsertDockerByVdmsId(@PathVariable String vdmsId, @RequestBody String dockerdto, HttpServletRequest httpServletRequest) {
        return touchscreenService.upsertDockerByVdmsId(vdmsId, dockerdto, httpServletRequest);
    }

//    @PostMapping(value = "/vdms/{vdmsId}/authenticateUser")
//    public ResponseEntity<?> authenticateUser(@PathVariable String vdmsId, @RequestBody TouchscreenDTO userdto, HttpServletRequest httpServletRequest) throws JsonProcessingException {
//        return touchscreenService.authenticateUser(vdmsId, userdto, httpServletRequest);
//    }

    @PutMapping(value = "/vdms/{vdmsId}/activateVdms")
    public ResponseEntity<?> activateVdms(@PathVariable String vdmsId, @RequestBody TouchscreenDTO userdto, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return touchscreenService.activateVdms(vdmsId, userdto, httpServletRequest);
    }

    @PostMapping(value = "/vdms/{vdmsId}/authenticateVendor")
    public ResponseEntity<?> authenticateVendor(@PathVariable String vdmsId, @RequestBody TouchscreenDTO userdto, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return touchscreenService.authenticateVendor(vdmsId, userdto, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getSystemTypes")
    public ResponseEntity<?> getSystemTypes(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.getSystemTypes(vdmsId, httpServletRequest);
    }

    @DeleteMapping(value = "/vdms/{vdmsId}/docker/{name}/deleteDockerByVdmsIdAndDockerName")
    public ResponseEntity<?> deleteDockerByDockerNameAndVdmsId(@PathVariable String vdmsId, @PathVariable String name, HttpServletRequest httpServletRequest) {
        return touchscreenService.deleteDockerByDockerNameAndVdmsId(vdmsId, name, httpServletRequest);
    }

    @GetMapping(value = "/organisation/{orgId}/vdms/{vdmsId}/getAllUserInfoByOrganisationId")
    public ResponseEntity<?> getAllUserInfoByOrganisationId(@PathVariable String orgId, @PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.getAllUserInfoByOrganisationId(orgId, vdmsId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getVdmsInfoByVdmsId")
    public ResponseEntity<?> getVdmsInfoByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return touchscreenService.getVdmsInfoByVdmsId(vdmsId, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateVdmsByVdmsId")
    public ResponseEntity<?> updateVdmsByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.updateVdmsByVdmsId(vdmsId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getProxyServerProfileByVdmsId")
    public ResponseEntity<?> getServerProxyProfileByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.getServerProxyProfileByVdmsId(vdmsId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getProxyClientProfileByVdmsId")
    public ResponseEntity<?> getClientProxyProfileByProxyProfileId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.getClientProxyProfileByProxyProfileId(vdmsId, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateVdmsDetailsByVdmsId")
    public ResponseEntity<?> updateVdmsDetailsByVdmsId(@PathVariable String vdmsId, @RequestBody VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        return touchscreenService.updateVdmsDetailsByVdmsId(vdmsId, vdmsDTO, httpServletRequest);
    }

    @PostMapping(value = "/user/{email}/vdms/{vdmsId}/inviteUnregisteredVendor")
    public ResponseEntity<?> inviteUnregisteredVendorByEmail(@PathVariable String email, @PathVariable String vdmsId, @RequestBody DockerDTO dockerdto, HttpServletRequest httpServletRequest) {
        return touchscreenService.inviteUnregisteredVendorByEmail(email, vdmsId, dockerdto, httpServletRequest);
    }

    @PostMapping(value = "/user/{email}/vdms/{vdmsId}/inviteRegisteredVendor")
    public ResponseEntity<?> inviteRegisteredVendor(@PathVariable String email, @PathVariable String vdmsId, @RequestBody DockerDTO dockerdto, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return touchscreenService.inviteRegisteredVendor(email, vdmsId, dockerdto, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/docker/{name}/unTagVendorByMasterUser")
    public ResponseEntity<?> unTagVendorByMasterUser(@PathVariable String vdmsId, @PathVariable String name, HttpServletRequest httpServletRequest) {
        return touchscreenService.unTagVendorByMasterUser(vdmsId, name, httpServletRequest);
    }

//    @PutMapping(value = "/vdms/{vdmsId}/syncFloorMapImageByFloorId")
//    public ResponseEntity<?> syncFloorMapImageByFloorId(@PathVariable String vdmsId, @RequestBody List<JSONObject> body, HttpServletRequest httpServletRequest) throws IOException {
//        return touchscreenService.syncFloorMapImageByFloorId(vdmsId, body, httpServletRequest);
//    }


    @PostMapping(value = "/vdms/{vdmsId}/addFloorMapsByFloorId")
    public ResponseEntity<?> addFloorMapsByFloorId(@PathVariable String vdmsId, @RequestParam(name = "image", required = false) List<MultipartFile> floorMap,
                                                   @RequestParam(name = "floor") String body, HttpServletRequest httpServletRequest) throws IOException {
        return touchscreenService.addFloorMapsByFloorId(vdmsId, floorMap, body, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/uploadFloorMapsByFloorId")
    public ResponseEntity<?> uploadFloorMapsByFloorId(@PathVariable String vdmsId, @RequestBody List<JSONObject> body, HttpServletRequest httpServletRequest) throws IOException {
        return touchscreenService.uploadFloorMapsByFloorId(vdmsId, body, httpServletRequest);
    }

//    @PutMapping(value = "/vdms/{vdmsId}/syncFloorMapsByFloorId")
//    public ResponseEntity<?> syncFloorMapsByFloorId(@PathVariable String vdmsId, @RequestBody List<JSONObject> body, HttpServletRequest httpServletRequest) throws IOException {
//        return touchscreenService.syncFloorMapsByFloorId(vdmsId, body, httpServletRequest);
//    }

    @DeleteMapping(value = "/vdms/{vdmsId}/deleteFloorMapsByFloorId")
    public ResponseEntity<?> deleteFloorMapsByFloorId(@PathVariable String vdmsId, @RequestBody List<JSONObject> floorObjects, HttpServletRequest httpServletRequest) {
        return touchscreenService.deleteFloorMapsByFloorId(vdmsId, floorObjects, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/floor/{floorId}/updateFloorMapTiles")
    public ResponseEntity<?> updateFloorMapTilesByFloorId(@PathVariable String vdmsId, @PathVariable String floorId, @RequestBody JSONObject floor, HttpServletRequest httpServletRequest) throws IOException {
        return touchscreenService.updateFloorMapTilesByFloorId(vdmsId, floorId, floor, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateVdmsTransfer")
    public ResponseEntity<?> updateVdmsTranfer(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) throws IOException {
        return touchscreenService.updateVdmsTranfer(vdmsId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getCustomerOrgIdByVdmsId")
    public ResponseEntity<?> getCustomerOrgIdByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) throws IOException {
        return touchscreenService.getCustomerOrgIdByVdmsId(vdmsId, httpServletRequest);
    }

    @GetMapping("/vdms/{vdmsId}/getAccessUrlForFileUpload")
    public ResponseEntity<?> getAccessUrlForFileUpload(@PathVariable String vdmsId, @RequestParam(required = false) String fileName, HttpServletRequest httpServletRequest) throws Exception {
        return touchscreenService.getAccessUrlForFileUpload(vdmsId, fileName, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/nfc/{type}/getNfcDetailsByVdmsId")
    public ResponseEntity<?> getNfcDetailsByVdmsId(@PathVariable String vdmsId, @PathVariable String type, HttpServletRequest httpServletRequest) {
        return touchscreenService.getNfcDetailsByVdmsId(vdmsId, type, httpServletRequest);
    }

    @PostMapping(value = "/vdms/{vdmsId}/nfc/{type}/getNfcDetailsByVdmsIdAndTaggedIds")
    public ResponseEntity<?> getNfcDetailsByDeviceIds(@PathVariable String vdmsId, @RequestBody List<String> taggedIds, @PathVariable String type, HttpServletRequest httpServletRequest) {
        return touchscreenService.getNfcDetailsByDeviceIds(vdmsId, taggedIds, type, httpServletRequest);
    }

    @GetMapping("/vdms/{vdmsId}/nfc/{type}/getNfcIdsByVdmsId")
    public ResponseEntity<?> getNfcIdsByVdmsAndType(@PathVariable String vdmsId, @PathVariable String type, HttpServletRequest httpServletRequest) {
        return touchscreenService.getNfcIdsByVdmsIdAndType(vdmsId, type, httpServletRequest);
    }

    @DeleteMapping(value = "/vdms/{vdmsId}/nfc/{type}/deleteNfcInfoByVdmsIdAndTaggedIds")
    public ResponseEntity<?> deleteNfcInfoByVdmsIdAndTaggedIds(@PathVariable String vdmsId, @PathVariable String type, @RequestBody List<String> taggedIds, HttpServletRequest httpServletRequest) {
        return touchscreenService.deleteNfcInfoByVdmsIdAndTaggedIds(vdmsId, type, taggedIds, httpServletRequest);
    }

    @PostMapping("/vdms/{vdmsId}/qrCode/{type}/getQrCodeDetailsByVdmsIdAndTaggedIds")
    public ResponseEntity<?> getQrDetailsByTaggedType(@PathVariable String vdmsId, @PathVariable String type, @RequestBody List<String> taggedIds, HttpServletRequest httpServletRequest) {
        return touchscreenService.getQrCodeDetailsByTaggedType(vdmsId, type, taggedIds, httpServletRequest);
    }

    @GetMapping("/vdms/{vdmsId}/qrCode/{type}/getQrCodeDetailsVdmsId")
    public ResponseEntity<?> getTaggedDevicesAndLocationByVdmsId(@PathVariable String vdmsId, @PathVariable String type, HttpServletRequest httpServletRequest) {
        return touchscreenService.getTaggedDevicesAndLocationByVdmsId(vdmsId, type, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/qrCode/{type}/getQrCodeIdsByVdmsId")
    public ResponseEntity<?> getQrCodeIdsByVdmsIdAndType(@PathVariable String vdmsId, @PathVariable String type, HttpServletRequest httpServletRequest) {
        return touchscreenService.getQrCodeIdsByVdmsIdAndType(vdmsId, type, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/qrCode/{type}/updateQrCodeInfoByVdmsIdAndTaggedIds")
    public ResponseEntity<?> updateQrCodeInfoByVdmsIdAndTaggedIds(@PathVariable String vdmsId, @PathVariable String type, @RequestBody List<String> taggedIds, HttpServletRequest httpServletRequest) {
        return touchscreenService.updateQrCodeInfoByVdmsIdAndTaggedIds(vdmsId, type, taggedIds, httpServletRequest);
    }

    @GetMapping("/organisation/{orgId}/vdms/{vdmsId}/getAllUserInfoByOrganisationIdAndVdmsId")
    public ResponseEntity<?> getAllUserInfoByOrganisationIdAndVdmsId(@PathVariable String orgId, @PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.getAllUserInfoByOrganisationIdAndVdmsId(orgId, vdmsId, httpServletRequest);
    }
    @PostMapping("/getAllQrCodeAndNfcData")
    public ResponseEntity<?> getAllQrCodeAndNfcData(@RequestBody JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return touchscreenService.getAllQrCodeAndNfcData(jsonObject, httpServletRequest);
    }

    @GetMapping(value = "/organisation/{orgId}/vdms/{vdmsId}/getUserCount")
    public ResponseEntity<?> getUserAndLoginCount(@PathVariable String vdmsId, @PathVariable String orgId, HttpServletRequest httpServletRequest) {
        return touchscreenService.getUserAndLoginCount(vdmsId, orgId, httpServletRequest);
    }

    @DeleteMapping(value = "/users/{email}/vdms/{vdmsId}/deleteDigitalTwinImages")
    public ResponseEntity<ResponseDTO> deleteDigitalTwinImagesByVdmsId(@PathVariable String email,
                                                                       @PathVariable String vdmsId, @RequestBody List<String> imageUrls, HttpServletRequest httpServletRequest) {
        return touchscreenService.deleteDigitalTwinImagesByVdmsId(email, vdmsId, imageUrls, httpServletRequest);
    }
    @PostMapping(value = "/vdms/{vdmsId}/tagDigitalTwinToMultipleDevices")
    public ResponseEntity<ResponseDTO> tagDigitalTwinToMultipleDevices(@PathVariable String vdmsId,
                                                                       @RequestParam(value = "image", required = false) List<MultipartFile> image,
                                                                       @RequestParam(value = "imageUrl", required = false) String imageUrl,
                                                                       @RequestParam(name = "deviceIds") List<String> deviceIds, HttpServletRequest httpServletRequest) throws IOException {
        return touchscreenService.tagDigitalTwinToMultipleDevices( vdmsId, image, imageUrl, deviceIds, httpServletRequest);
    }

    //qr-code
    @GetMapping(value = "/vdms/{vdmsId}/getQrCodeAndClientQrCodeCount")
    public ResponseEntity<ResponseDTO> getQrCodeAndClientQrCodeCount(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.getQrCodeAndClientQrCodeCount(vdmsId, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getAllQrCodesByVdmsId")
    public ResponseEntity<ResponseDTO> getAllQrCodeByVdmsId(@PathVariable String vdmsId,
                                                            @RequestParam(defaultValue = "1") @Min(1) @Max(100) int pageNo,
                                                            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                            HttpServletRequest httpServletRequest) {
        return touchscreenService.getAllQrCodeByVdmsId(vdmsId, pageNo, pageSize, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getAllClientQrCodesByVdmsId")
    public ResponseEntity<ResponseDTO> getAllClientQrCodeByVdmsId(@PathVariable String vdmsId,
                                                                  @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                                  @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                                  HttpServletRequest httpServletRequest) {
        return touchscreenService.getAllClientQrCodeByVdmsId(vdmsId, pageNo, pageSize, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateQrCodeSyncByVdmsId")
    public ResponseEntity<ResponseDTO> updateQrCodeSyncByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.updateQrCodeSyncByVdmsId(vdmsId, httpServletRequest);
    }


    //nfc
    @GetMapping(value = "/vdms/{vdmsId}/getNfcAndClientNfcCount")
    public ResponseEntity<ResponseDTO> getNfcAndClientNfcCount(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.getNfcAndClientNfcCount(vdmsId, httpServletRequest);
    }


    @GetMapping(value = "/vdms/{vdmsId}/getAllClientNfcByVdmsId")
    public ResponseEntity<ResponseDTO> getAllClientNfcByVdmsId(@PathVariable String vdmsId,
                                                               @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                               @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                               HttpServletRequest httpServletRequest) {
        return touchscreenService.getAllClientNfcByVdmsId(vdmsId, pageNo, pageSize, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getAllNfcByVdmsId")
    public ResponseEntity<ResponseDTO> getAllNfcByVdmsId(@PathVariable String vdmsId,
                                                         @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                         @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                         HttpServletRequest httpServletRequest) {
        return touchscreenService.getAllNfcByVdmsId(vdmsId, pageNo, pageSize, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateNfcSyncByVdmsId")
    public ResponseEntity<ResponseDTO> updateNfcSyncByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.updateNfcSyncByVdmsId(vdmsId, httpServletRequest);
    }


    @PutMapping(value = "/vdms/{vdmsId}/updateVdmsAssetCountByVdmsId")
    public ResponseEntity<ResponseDTO> updateVdmsAssetCountByVdmsId(@PathVariable String vdmsId,@RequestBody JSONObject jsonObject,HttpServletRequest httpServletRequest)
    {
        return touchscreenService.updateVdmsAssetCountByVdmsId(vdmsId,jsonObject,httpServletRequest);
    }

    //nfc sync changes
    @GetMapping(value = "/vdms/{vdmsId}/geAllSyncNfcByVdmsId")
    public ResponseEntity<ResponseDTO> geAllSyncNfcByVdmsId(@PathVariable String vdmsId,
                                                            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                            HttpServletRequest httpServletRequest){
        return touchscreenService.geAllSyncNfcByVdmsId(vdmsId,pageNo,pageSize,httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getAllSyncClientNfcByVdmsId")
    public ResponseEntity<ResponseDTO> getAllSyncClientNfcByVdmsId(@PathVariable String vdmsId,
                                                                   @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                                   @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                                   HttpServletRequest httpServletRequest) {
        return touchscreenService.getAllSyncClientNfcByVdmsId(vdmsId, pageNo, pageSize, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getAllSyncNfcByVdmsId")
    public ResponseEntity<ResponseDTO> getAllSyncNfcByVdmsId(@PathVariable String vdmsId,
                                                             @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                             @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                             HttpServletRequest httpServletRequest){
        return touchscreenService.getAllSyncNfcByVdmsId(vdmsId,pageNo,pageSize,httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getAllSyncClientQrCodesByVdmsId")
    public ResponseEntity<ResponseDTO> getAllSyncClientQrCodeByVdmsId(@PathVariable String vdmsId,
                                                                      @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                                      @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                                      HttpServletRequest httpServletRequest) {
        return touchscreenService.getAllSyncClientQrCodeByVdmsId(vdmsId, pageNo, pageSize, httpServletRequest);
    }

    // qr code sync changes for condition 1
    @GetMapping(value = "/vdms/{vdmsId}/getAllSyncQrCodesByVdmsId")
    public ResponseEntity<ResponseDTO> getSyncedQrCodeByVdmsId(@PathVariable String vdmsId,
                                                               @RequestParam(defaultValue = "1") @Min(1) @Max(100) int pageNo,
                                                               @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pageSize,
                                                               HttpServletRequest httpServletRequest) {
        return touchscreenService.getSyncedQrCodeByVdmsId(vdmsId, pageNo, pageSize, httpServletRequest);

    }

    @GetMapping(value = "/getUpdatedAssetType")
    public ResponseEntity<?> getUpdatedAssetType(@RequestParam(required = false, defaultValue = "all") String assetTypeGroupName,
                                                 @RequestParam(required = false, defaultValue = "all") String key,
                                                 @RequestParam(required = false) String sort,
                                                 @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageNo,
                                                 @RequestParam(defaultValue = "10") @Min(1) @Max(1000) int pageSize,
                                                 @RequestParam(required = false) String updatedTimestamp,
                                                 HttpServletRequest httpServletRequest) {
        return touchscreenService.getUpdatedAssetType(assetTypeGroupName, key, sort, pageNo, pageSize, updatedTimestamp, httpServletRequest);
    }

    @GetMapping(value = "/organisation/user/{email}/getUserRemoteDesktopAuth")
    public ResponseEntity<?> getUserRemoteDesktopAuth(@PathVariable String email, HttpServletRequest httpServletRequest) {
        return touchscreenService.getUserRemoteDesktopAuth(email, httpServletRequest);
    }

    @GetMapping(value = "/vdms/{vdmsId}/getAgentPermissionsByVdmsId")
    public ResponseEntity<?> getAgentPermissionsByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.getAgentPermissionsByVdmsId(vdmsId, httpServletRequest);
    }

    //////////////////////////////// corrigo auto url update////////////////////////////////////////////////

    @PutMapping(value = "/vdms/{vdmsId}/corrigo/{configId}/updateConfigIdByVdmsId")
    public ResponseEntity<?> updateConfigIdByVdmsId(@PathVariable String vdmsId, @PathVariable String configId, HttpServletRequest httpServletRequest) {
        return touchscreenService.updateConfigIdByVdmsId(vdmsId, configId, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/updateCorrigoSyncByVdmsId")
    public ResponseEntity<?>updateCorrigoSyncByVdmsId(@PathVariable String vdmsId, HttpServletRequest httpServletRequest) {
        return touchscreenService.updateCorrigoSyncByVdmsId(vdmsId, httpServletRequest);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping(value = "/vdms/getVdmsGlobalTranslationToken")
    public ResponseEntity<?> getVdmsGlobalToken(@RequestParam String tokenSecretName, HttpServletRequest httpServletRequest) {
        return touchscreenService.getVdmsGlobalToken(tokenSecretName, httpServletRequest);
    }

}
