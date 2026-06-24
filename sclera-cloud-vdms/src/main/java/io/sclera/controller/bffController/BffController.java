package io.sclera.controller.bffController;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.UserDTO;
import io.sclera.service.AlertScheduleService;
import io.sclera.service.UserService;
import io.sclera.service.VdmsService;
import io.sclera.service.VdmsVisibilityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/bff")
public class BffController {

    @Autowired
    private VdmsService vdmsService;
    @Autowired
    private UserService userService;
    @Autowired
    private AlertScheduleService alertScheduleService;

    @Autowired
    private VdmsVisibilityService vdmsVisibilityService;
//    @GetMapping(value = "/hello")
//    public String hello(){
//        return "hello";
//    }

    @GetMapping(value = "/org/{orgId}/vdms/vdmsInfo")
    public ResponseEntity<?> getVdmsPropertyInfoByVdmsId(@RequestBody List<String> vdmsId, @PathVariable String orgId, HttpServletRequest httpServletRequest) {
        return vdmsService.getVdmsPropertyInfoByVdmsId(vdmsId, orgId, httpServletRequest);
    }

//    @PostMapping(value = "/organisation/masterUser/createOrgAdmin")
//    public ResponseEntity<?> createOrganisationAdmin(@RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) throws IOException {
//        System.out.println("came here ");
//        return userService.createOrganisationAdmin(userDTO, httpServletRequest);
//    }

    @GetMapping(value = "/user/{email}/getVisibleVdmsByEmail")
    public ResponseEntity<?> getVisibleVdmsByEmail(@PathVariable String email, HttpServletRequest httpServletRequest) {
        return vdmsVisibilityService.getVisibleVdmsByEmail(email, email, httpServletRequest);
    }

    @DeleteMapping(value = "/email/{email}/deleteEnterPriseMasterUserAccount")
    public ResponseEntity<?> deleteEnterPriseMasterUserAccount(@PathVariable String email, @RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return userService.deleteEnterPriseMasterUserAccount(email, email, userDTO, httpServletRequest);
    }

    @GetMapping(value = "/migrateTimeStamp")
    public ResponseEntity<?> migrateCreationTimeStamp(HttpServletRequest httpServletRequest){
        return userService.migrateCreationTimeStamp(httpServletRequest);
    }

    @DeleteMapping(value = "/organisation/{orgId}/deleteEnterPriseMasterUserAccountByOrgId")
    public ResponseEntity<?> deleteEnterpriseAccountByOrgId(@PathVariable String orgId, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return userService.deleteEnterpriseAccountByOrgId(orgId,loggedInUser, httpServletRequest);
    }

//    @GetMapping(value = "/migrateUserDetails")
//    public ResponseEntity<?> getAllUserDetails(HttpServletRequest httpServletRequest) {
//        return userService.getAllUserDetails(httpServletRequest);
//    }
//
//    @GetMapping(value = "/migrateAlertScheduler")
//    public ResponseEntity<?> getAllAlertScheduler(HttpServletRequest httpServletRequest) {
//        return alertScheduleService.getAllAlertScheduler(httpServletRequest);
//    }



    @GetMapping(value = "/getVisibleVdmsByEmailList")
    public ResponseEntity<?> getVisibleVdmsByEmailList(@RequestBody JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        return vdmsService.getVisibleVdmsByEmailList(jsonObject, httpServletRequest);
    }

    @PutMapping(value = "/vdms/{vdmsId}/deactivateVdmsByVdmsId")
    public ResponseEntity<?> deactivateVdmsByVdmsId(@RequestParam String loggedInUser,@PathVariable String vdmsId,HttpServletRequest httpServletRequest){
        return vdmsService.deactivateVdmsByVdmsId(loggedInUser,vdmsId,httpServletRequest);
    }

    @GetMapping(value = "/getOrgIdByQrCodeId")
    public ResponseEntity<?> getOrgIdByQrCodeId(@RequestParam(name = "qrCodeId") String qrCodeId, @RequestParam(name = "isClientQrCode", defaultValue = "false") Boolean isClientQrCode,HttpServletRequest httpServletRequest) {
        return vdmsService.getOrgIdByQrCodeId(qrCodeId, isClientQrCode, httpServletRequest);
    }

    @GetMapping(value = "/getOrgIdByBarCodeId")
    public ResponseEntity<?> getOrgIdByBarCodeId(@RequestParam(name = "barCodeId") String barCodeId, HttpServletRequest httpServletRequest) {
        return vdmsService.getOrgIdByBarCodeId(barCodeId, httpServletRequest);
    }

    /////////////////////////////////////////////////// CORRIGO AUTO URL ////////////////////////////////////////////////////////////////

    @PostMapping(value = "/corrigo/configIds")
    public void updateVDMSCorrigoConfigIds(@RequestBody List<String> configIds, HttpServletRequest httpServletRequest) {
        vdmsService.updateVDMSCorrigoConfigIds(configIds, httpServletRequest);
    }

    @PostMapping(value = "/corrigo/configId")
    public void updateVDMSCorrigoConfigId(@RequestBody String configId, HttpServletRequest httpServletRequest) {
        vdmsService.updateVDMSCorrigoConfigId(configId, httpServletRequest);
    }

    @PostMapping(value = "/corrigo/configIdForDelete")
    public boolean checkCorrigoConfigIdForDelete(@RequestBody String configId, HttpServletRequest httpServletRequest) {
        return vdmsService.checkCorrigoConfigIdForDelete(configId, httpServletRequest);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


}
