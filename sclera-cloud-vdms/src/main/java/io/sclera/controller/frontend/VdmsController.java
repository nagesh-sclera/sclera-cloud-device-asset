package io.sclera.controller.frontend;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsDTO;
import io.sclera.service.VdmsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;
import java.util.Set;


@RequestMapping("/api")
@Validated
@RestController
public class VdmsController {

    @Autowired
    private VdmsService vdmsService;


    @GetMapping("/user/{email}/vdms/{vdms_id}")
    public ResponseEntity<?> getVdmsInfoByVdmsIdAndEmail(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return vdmsService.getVdmsInfoByVdmsId(email, vdms_id, loggedInUser, httpServletRequest);
    }


    @PostMapping("/user/{email}/vdms")
    public ResponseEntity<?> addVdmsByUserOrganisationId(@RequestParam String loggedInUser, @RequestBody VdmsDTO vdmsdto, @PathVariable String email, HttpServletRequest httpServletRequest) throws IOException {
        return vdmsService.addVdmsByUserOrganisationId(vdmsdto, email, loggedInUser, httpServletRequest);
    }

    @GetMapping("/user/{email}/vdms")
    public ResponseEntity<?> getAllVdmsInfoByUserEmail(@RequestParam String loggedInUser, @PathVariable String email,
                                                       @RequestParam(required = false, defaultValue = "all") String key,
                                                       @RequestParam(required = false) String sort,
                                                       @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                       @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pagesize, HttpServletRequest httpServletRequest) {
        return vdmsService.getAllVdmsInfoByUserEmail(email, key, sort, pageno, pagesize, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/vendor/{email}/vdms")
    public ResponseEntity<?> getAllVdmsInfoByVendorEmail(@RequestParam String loggedInUser, @PathVariable String email,
                                                         @RequestParam(required = false, defaultValue = "all") String key,
                                                         @RequestParam(required = false) String sort,
                                                         @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                         @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pagesize,
                                                         HttpServletRequest httpServletRequest) {
        return vdmsService.getAllVdmsInfoByVendorEmail(email, key, sort, pageno, pagesize, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/admin/{email}/vdms")
    public ResponseEntity<?> getAllVdmsInfoByAdminEmail(@RequestParam String loggedInUser, @PathVariable String email,
                                                        @RequestParam(required = false, defaultValue = "all") String key,
                                                        @RequestParam(required = false) String sort,
                                                        @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                        @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pagesize, HttpServletRequest httpServletRequest) {
        return vdmsService.getAllVdmsInfoByAdminEmail(email, key, sort, pageno, pagesize, loggedInUser, httpServletRequest);
    }

    @PutMapping("/user/{username}/vdms/{vdms_id}")
    public ResponseEntity<?> editVdmsByVdmsId(@RequestParam String loggedInUser, @RequestBody VdmsDTO vdmsdto, @PathVariable String username, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) throws IOException {
        return vdmsService.editVdmsByVdmsId(vdmsdto, username, vdms_id, loggedInUser, httpServletRequest);
    }

    @PutMapping("/user/{email}/vdms/updateVdmsLocation")
    public ResponseEntity<?> updateVdmsLocationByVdmsId(@RequestParam String loggedInUser, @RequestBody Set<VdmsDTO> vdmsDTO, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsService.updateVdmsLocationByVdmsId(vdmsDTO, email, loggedInUser, httpServletRequest);
    }


    @PutMapping("/user/{username}/vdms/{vdms_id}/block")
    public ResponseEntity<?> blockVdmsByVdmsId(@RequestParam String loggedInUser, @PathVariable String username, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return vdmsService.blockVdmsByVdmsId(username, vdms_id, loggedInUser, httpServletRequest);
    }

    @PutMapping("/user/{username}/vdms/{vdms_id}/unblock")
    public ResponseEntity<?> unBlockVdmsByVdmsId(@RequestParam String loggedInUser, @PathVariable String username, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return vdmsService.unBlockVdmsByVdmsId(username, vdms_id, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/user/{username}/vdms/{vdms_id}")
    public ResponseEntity<?> deleteVdmsByVdmsId(@RequestParam String loggedInUser, @PathVariable String username, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return vdmsService.deleteNewVdmsByVdmsId(username, vdms_id, loggedInUser, httpServletRequest);
    }

    @DeleteMapping("/user/{username}/vdms/{vdms_id}/deleteVdmsBySuperAdmin")
    public ResponseEntity<?> deleteVdmsByVdmsIdAndSuperAdminEmail(@RequestParam String loggedInUser, @PathVariable String username, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return vdmsService.deleteVdmsByVdmsIdAndSuperAdminEmail(username, vdms_id, loggedInUser, httpServletRequest);
    }

    @GetMapping("/user/{username}/quicksearch")
    public ResponseEntity<?> getQuickSearchListByOrganisationId(@RequestParam String loggedInUser, @PathVariable String username, HttpServletRequest httpServletRequest) {
        return vdmsService.getNewQuickSearchListByOrganisationId(username, loggedInUser, httpServletRequest);
    }

    @PutMapping("/vdms/subscription")
    public ResponseEntity<?> updateSubscriptionByVdmsId(@RequestParam String loggedInUser, @RequestBody VdmsDTO vdmsdto, HttpServletRequest httpServletRequest) {
        return vdmsService.updateSubscriptionByVdmsId(vdmsdto, loggedInUser, httpServletRequest);
    }

    @PostMapping("/vdms/{vdms_id}/backup")
    public ResponseEntity<?> saveBackupFile(@RequestParam("file") MultipartFile file, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return vdmsService.saveBackupFile(file, vdms_id, httpServletRequest);
    }

    @GetMapping("/vdms/{vdms_id}/backup")
    public ResponseEntity<?> getBackupFileByVdmsId(@PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return vdmsService.getBackupFileByVdmsId(vdms_id, httpServletRequest);
    }

    @PutMapping("/user/{email}/vdms/{vdms_id}/proxy/profile/{proxy_profile_id}")
    public ResponseEntity<?> tagPrimaryProxyProfileToVdmsByProxyProfileId(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String vdms_id, @PathVariable String proxy_profile_id, HttpServletRequest httpServletRequest) {
        return vdmsService.tagPrimaryProxyProfileToVdmsByProxyProfileId(email, vdms_id, proxy_profile_id, loggedInUser, httpServletRequest);
    }

    @PutMapping("/user/{email}/vdms/{vdms_id}/proxy/profile")
    public ResponseEntity<?> untagPrimaryProxyProfileToVdmsByVdmsId(@RequestParam String loggedInUser, @PathVariable String email, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return vdmsService.untagPrimaryProxyProfileToVdmsByVdmsId(email, vdms_id, loggedInUser, httpServletRequest);
    }

    @GetMapping("/organisation/{customer_org_id}/user/{email}/vdms/proxy/profile")
    public ResponseEntity<?> getProxyProfilesTaggedToVdmsIdByOrganisationId(@RequestParam String loggedInUser, @PathVariable String customer_org_id, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsService.getProxyProfilesTaggedToVdmsIdByOrganisationId(customer_org_id, email, loggedInUser, httpServletRequest);
    }

    @PutMapping("/user/{email}/transfer/registered-user")
    public ResponseEntity<?> transferVdmsToRegisteredUserByVdmsId(@RequestParam String loggedInUser, @PathVariable String email, @RequestBody List<VdmsDTO> vdmsDTO, HttpServletRequest httpServletRequest) {
        return vdmsService.transferBulkVdmsToRegisteredUserByVdmsId(email, vdmsDTO, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/getProxyProfileByVdmsId")
    public ResponseEntity<?> getProxyProfileByVdmsId(@RequestParam(required = false) String loggedInUser,@RequestBody JSONObject body, HttpServletRequest httpServletRequest) {
        return vdmsService.getProxyProfileByVdmsId(loggedInUser,body, httpServletRequest);
    }

    @PutMapping(value = "/organisation/{orgId}/user/{email}/vdms/{vdmsId}/updateVdmsMasterSlaveStatus")
    public ResponseEntity<?> updateVdmsMasterSlaveStatusByVdmsId(@PathVariable String orgId, @PathVariable String email, @PathVariable String vdmsId, @RequestParam String loggedInUser,
                                                                 @RequestBody VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        return vdmsService.updateVdmsMasterSlaveStatusByVdmsId(orgId, email, vdmsId, loggedInUser, vdmsDTO, httpServletRequest);
    }

    @GetMapping("/vdms/{vdmsId}/getVdmsFeatureByVdmsId")
    public ResponseEntity<ResponseDTO> getVdmsFeatureByVdmsId(@PathVariable String vdmsId, @RequestParam(name = "loggedInUser") String loggedInUser,
                                                              HttpServletRequest httpServletRequest) {
        return vdmsService.getVdmsFeatureByVdmsId(vdmsId, loggedInUser, httpServletRequest);
    }

    @PutMapping("/vdms/{vdmsId}/updateVdmsFeatureByVdmsId")
    public ResponseEntity<ResponseDTO> updateVdmsFeatureByVdmsId(@PathVariable String vdmsId, @RequestBody List<String> featureIds,
                                                                 @RequestParam(name = "loggedInUser") String loggedInUser,
                                                                 HttpServletRequest httpServletRequest) {
        return vdmsService.updateVdmsFeatureByVdmsId(vdmsId, featureIds, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/getClientQRCodeProxyProfileByVdmsId")
    public ResponseEntity<?> getClientQRCodeProxyProfileByVdmsId(@RequestParam(required = false) String loggedInUser,@RequestBody JSONObject body, HttpServletRequest httpServletRequest) {
        return vdmsService.getClientQRCodeProxyProfileByVdmsId(loggedInUser,body, httpServletRequest);
    }

    @PostMapping(value = "/user/{email}/migrateVdms")
    public ResponseEntity<?> migrateVdms(@PathVariable String email, @RequestBody VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        return vdmsService.migrateVdms(email, vdmsDTO, httpServletRequest);
    }

    @PostMapping(value = "/getClientNfcProxyProfileByVdmsId")
    public ResponseEntity<?> getClientNfcProxyProfileByVdmsId(@RequestParam String loggedInUser,@RequestBody JSONObject body, HttpServletRequest httpServletRequest) {
        return vdmsService.getClientNfcProxyProfileByVdmsId(loggedInUser,body, httpServletRequest);
    }

    @GetMapping("/organisation/{orgId}/email/{email}/vdms/{vdmsId}/getCoordinates")
    public ResponseEntity<?> getVdmsCoordinates(@PathVariable String orgId,@PathVariable String email,@PathVariable String vdmsId,@RequestParam String loggedInUser,HttpServletRequest httpServletRequest) {
        return vdmsService.getVdmsCoordinates(orgId,email,vdmsId,loggedInUser,httpServletRequest);
    }

    @GetMapping(value = "/organisations/{orgId}/getVdmsPropertyInfoByOrganisationId")
    public ResponseEntity<?> getVdmsPropertyInfoByOrganisationId(@PathVariable String orgId,HttpServletRequest httpServletRequest){
        return vdmsService.getVdmsPropertyInfoByOrganisationId(orgId,httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/reactivateVdmsByVdmsId")
    public ResponseEntity<?> reactivateVdmsByVdmsId(@RequestParam String loggedInUser,@PathVariable String vdmsId,HttpServletRequest httpServletRequest){
        return vdmsService.reactivateVdmsByVdmsId(loggedInUser,vdmsId,httpServletRequest);
    }

    @PutMapping(value = "/vdms/updateAwsRegionById")
    public ResponseEntity<?> updateAwsRegionById(@RequestBody List<VdmsDTO> vdmsDTOS, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return vdmsService.updateAwsRegionById(vdmsDTOS, loggedInUser, httpServletRequest);
    }
}
