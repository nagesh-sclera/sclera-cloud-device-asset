package io.sclera.controller.frontend;

import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.UserDTO;
import io.sclera.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RequestMapping(value = "/api")
@Validated
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping(value = "/user/{user_email}/info")
    public ResponseEntity<?> getUserList(@RequestParam(required = false) String loggedInUser, @PathVariable String user_email,
                                         @RequestParam(required = false, defaultValue = "all") String key,
                                         @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                         @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int pagesize,
                                         @RequestParam(defaultValue = "all") List<String> userProfiles,
                                         @RequestParam(defaultValue = "false") boolean withoutProfile,
                                         @RequestParam(defaultValue = "all") String active, HttpServletRequest httpServletRequest) {
        return userService.getUserList(user_email, key, pageno, pagesize, loggedInUser, userProfiles, withoutProfile, active, httpServletRequest);
    }

    @GetMapping(value = "/organisation/{organisation_id}/users")
    public ResponseEntity<?> getAllUsersByOrganisationId(@PathVariable String organisation_id,
                                                         @RequestParam(required = false, defaultValue = "all") String key,
                                                         @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                         @RequestParam(defaultValue = "2000") @Min(1) @Max(2001) int pagesize,
                                                         @RequestParam(name = "loggedInUser") String loggedInUser,
                                                         HttpServletRequest httpServletRequest) {
        return userService.getAllUsersByOrganisationId(organisation_id, key, pageno, pagesize, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/user/{email}/details")
    public ResponseEntity<?> getUserDetailsByUserEmail(@RequestParam(required = false) String loggedInUser, @PathVariable String email, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return userService.getUserDetailsByUserEmail(email, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/admin/{email}/details")
    public ResponseEntity<?> getAdminDetailsByAdminEmail(@RequestParam(required = false) String loggedInUser, @PathVariable String email, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return userService.getAdminDetailsByAdminEmail(email, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/user/{email}/vdms/{vdms_id}/details")
    public ResponseEntity<?> getMasterUserDetailsByVdmsId(@RequestParam(required = false) String loggedInUser, @PathVariable String vdms_id, HttpServletRequest httpServletRequest) {
        return userService.getMasterUserDetailsByVdmsId(vdms_id, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/user/{email}/image")
    public ResponseEntity<?> getUserImageByEmail(@RequestParam(required = false) String loggedInUser, @PathVariable String email, HttpServletRequest httpServletRequest) {
        return userService.getUserImageByEmail(email, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/admin/{email}/getAllUserInfoByAdminEmail")
    public ResponseEntity<?> getAllUserInfoByAdminEmail(@RequestParam(required = false) String loggedInUser, @PathVariable String email,
                                                        @RequestParam(required = false, defaultValue = "all") String key,
                                                        @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int pageno,
                                                        @RequestParam(defaultValue = "1000") @Min(1) @Max(1000) int pagesize,
                                                        @RequestParam(defaultValue = "all") List<String> userProfiles,
                                                        @RequestParam(defaultValue = "false") boolean withoutProfile,
                                                        @RequestParam(defaultValue = "all") String orgId,
                                                        @RequestParam(defaultValue = "all") String active,
                                                        HttpServletRequest httpServletRequest) {
        return userService.getAllUserInfoByAdminEmail(email, key, pageno, pagesize, loggedInUser, userProfiles, withoutProfile, orgId, active, httpServletRequest);
    }

    @GetMapping(value = "/email/{email}/getUserDetailsByEmail")
    public ResponseEntity<?> getUserActiveStatusByEmail(@RequestParam String loggedInUser, @PathVariable String email, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return userService.getUserActiveStatusByEmail(email, loggedInUser, httpServletRequest);
    }

    @DeleteMapping(value = "/email/{email}/deleteEnterPriseMasterUserAccount")
    public ResponseEntity<?> deleteEnterPriseMasterUserAccount(@RequestParam(required = false) String loggedInUser, @PathVariable String email, @RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return userService.deleteEnterPriseMasterUserAccount(loggedInUser, email, userDTO, httpServletRequest);
    }


    @PostMapping(value = "/bff/organisation/masterUser/createMasterUser")
    public ResponseEntity<?> createMasterUser(@RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) throws IOException {
        return userService.createMasterUser(userDTO, httpServletRequest);
    }

    @PostMapping(value = "/organisation/{orgId}/user/{email}/createUserByPrivilegedUser")
    public ResponseEntity<?> createUserByPrivilegedUser(@RequestParam(required = false) String loggedInUser, @RequestBody UserDTO userDTO, @PathVariable String orgId,
                                                        @PathVariable String email, @RequestParam(required = false) boolean ipAclEnabled,
                                                        @RequestParam(required = false) boolean geoAclEnabled,
                                                        HttpServletRequest httpServletRequest) throws IOException {
        return userService.createUserByPrivilegedUser(userDTO, orgId, email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/organisation/{orgId}/masterUser/{email}/createUserByMasterUser")
    public ResponseEntity<?> createUserByMasterUser(@RequestParam(required = false) String loggedInUser, @RequestBody UserDTO userDTO,
                                                    @PathVariable String orgId, @PathVariable String email, @RequestParam(required = false) boolean ipAclEnabled, @RequestParam(required = false) boolean geoAclEnabled, HttpServletRequest httpServletRequest) throws IOException {
        return userService.createUserByMasterUser(userDTO, orgId, email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/organisation/{orgId}/masterUser/{email}/createOrgAdminByMasterUser")
    public ResponseEntity<?> createOrgAdminByMasterUser(@RequestParam(required = false) String loggedInUser, @RequestBody UserDTO userDTO,
                                                        @PathVariable String orgId, @PathVariable String email, @RequestParam(required = false) boolean ipAclEnabled, @RequestParam(required = false) boolean geoAclEnabled, HttpServletRequest httpServletRequest) throws IOException {
        return userService.createOrgAdminByMasterUser(userDTO, orgId, email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/organisation/admin/{email}/createUserByAdmin")
    public ResponseEntity<?> createUserByAdmin(@RequestParam(required = false) String loggedInUser, @RequestBody UserDTO userDTO, @PathVariable String email,
                                               @RequestParam(required = false) boolean ipAclEnabled, @RequestParam(required = false) boolean geoAclEnabled, HttpServletRequest httpServletRequest) throws IOException {
        return userService.createUserByAdmin(userDTO, email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/organisation/{orgId}/masterUser/{email}/createPropertyAdminByMasterUser")
    public ResponseEntity<?> createPropertyAdminByMasterUser(@RequestParam(required = false) String loggedInUser, @RequestBody UserDTO userDTO,
                                                             @PathVariable String orgId, @PathVariable String email, @RequestParam(required = false) boolean ipAclEnabled, @RequestParam(required = false) boolean geoAclEnabled, HttpServletRequest httpServletRequest) throws IOException {
        return userService.createPropertyAdminByMasterUser(userDTO, orgId, email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/organisation/superAdmin/{email}/createAdminBySuperAdmin")
    public ResponseEntity<?> createAdminBySuperAdmin(@RequestParam(required = false) String loggedInUser, @RequestBody UserDTO userDTO, @PathVariable String email, @RequestParam(required = false) boolean ipAclEnabled, @RequestParam(required = false) boolean geoAclEnabled, HttpServletRequest httpServletRequest) throws IOException {
        return userService.createAdminBySuperAdmin(userDTO, email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
    }

    @PostMapping(value = "/organisation/superAdmin/{email}/createSuperAdminByEmail")
    public ResponseEntity<?> createSuperAdminByEmail(@RequestParam(required = false) String loggedInUser, @RequestBody UserDTO userDTO, @PathVariable String email, HttpServletRequest httpServletRequest) throws IOException {
        return userService.createSuperAdminByEmail(userDTO, email, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/organisation/{orgId}/masterUser/{email}/updateMasterUserByEmail")
    public ResponseEntity<?> updateMasterUserByEmail(@RequestParam(required = false) String loggedInUser, @PathVariable String orgId, @PathVariable String email, @RequestBody UserDTO userdto, HttpServletRequest httpServletRequest) throws IOException {
        return userService.updateMasterUserByEmail(orgId, email, userdto, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/organisation/admin/{email}/updateAdminBySuperAdmin")
    public ResponseEntity<?> updateAdminBySuperAdmin(@RequestParam(required = false) String loggedInUser, @PathVariable String email, @RequestBody UserDTO userdto, HttpServletRequest httpServletRequest) throws IOException {
        return userService.updateAdminBySuperAdmin(email, userdto, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/organisation/superAdmin/{email}/updateSuperAdminByEmail")
    public ResponseEntity<?> updateSuperAdminByEmail(@RequestParam(required = false) String loggedInUser, @RequestBody UserDTO userDTO, @PathVariable String email, HttpServletRequest httpServletRequest) throws IOException {
        return userService.updateSuperAdminByEmail(userDTO, email, loggedInUser, httpServletRequest);
    }

    @DeleteMapping(value = "/organisation/{orgId}/user/{email}/deleteUserByEmail")
    public ResponseEntity<?> deleteUserByMasterUser(@RequestParam(required = false) String loggedInUser, @PathVariable String orgId, @PathVariable String email, @RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) {
        return userService.deleteUserByMasterUser(orgId, email, userDTO, loggedInUser, httpServletRequest);
    }

    @DeleteMapping(value = "/organisation/{orgId}/user/{email}/deletePropertyAdminByEmail")
    public ResponseEntity<?> deletePropertyAdminByEmail(@RequestParam(required = false) String loggedInUser, @PathVariable String orgId, @PathVariable String email, @RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) {
        return userService.deletePropertyAdminByEmail(orgId, email, userDTO, loggedInUser, httpServletRequest);
    }

    @DeleteMapping(value = "/organisation/admin/{email}/deleteUserByAdmin")
    public ResponseEntity<?> deleteUserByAdmin(@RequestParam(required = false) String loggedInUser, @PathVariable String email, @RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        return userService.deleteUserByAdmin(email, userDTO, loggedInUser, httpServletRequest);
    }

    @DeleteMapping(value = "/organisation/superAdmin/{email}/deleteAdminBySuperAdmin")
    public ResponseEntity<?> deleteAdminBySuperAdmin(@RequestParam(required = false) String loggedInUser, @PathVariable String email, @RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) {
        return userService.deleteAdminBySuperAdmin(email, userDTO, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/organisation/dataEntry/{email}/updateDataEntryByEmail")
    public ResponseEntity<?> updateDataEntryByEmail(@RequestParam(required = false) String loggedInUser, @PathVariable String email, @RequestBody UserDTO userdto, HttpServletRequest httpServletRequest) throws IOException {
        return userService.updateDataEntry(email, userdto, loggedInUser, httpServletRequest);
    }

    @DeleteMapping(value = "/organisation/admin/{email}/deleteDataEntryByAdmin")
    public ResponseEntity<?> deleteDataEntryByAdmin(@RequestParam(required = false) String loggedInUser, @PathVariable String email, @RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) {
        return userService.deleteDataEntryByAdmin(email, userDTO, loggedInUser, httpServletRequest);
    }

    @DeleteMapping(value = "/organisation/{orgId}/masterUser/{email}/deleteOrgAdminByMasterUser")
    public ResponseEntity<?> deleteOrgAdminByMasterUser(@RequestParam(required = false) String loggedInUser, @PathVariable String orgId, @PathVariable String email, @RequestBody UserDTO userDTO, HttpServletRequest httpServletRequest) {
        return userService.deleteOrgAdminByMasterUser(orgId, email, userDTO, loggedInUser, httpServletRequest);
    }


    @PutMapping(value = "/organisation/{orgId}/masterUser/{email}/updateOrgAdminByMasterUser")
    public ResponseEntity<?> updateOrgAdminByMasterUser(@RequestParam(required = false) String loggedInUser, @PathVariable String orgId, @PathVariable String email, @RequestBody UserDTO userdto, HttpServletRequest httpServletRequest) throws IOException {
        return userService.updateOrgAdminByMasterUser(orgId, email, userdto, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/organisation/admin/{email}/updateUserByAdmin")
    public ResponseEntity<?> updateUserByAdmin(@RequestParam(required = false) String loggedInUser, @RequestBody UserDTO userDTO, @PathVariable String email, HttpServletRequest httpServletRequest) throws IOException {
        return userService.updateUserByAdmin(userDTO, email, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/organisation/{orgId}/masterUser/{email}/updateUserByMasterUser")
    public ResponseEntity<?> updateUserByMasterUser(@RequestParam(required = false) String loggedInUser, @PathVariable String orgId, @PathVariable String email, @RequestBody UserDTO userdto, HttpServletRequest httpServletRequest) throws IOException {
        return userService.updateUserByMasterUser(orgId, email, userdto, loggedInUser, httpServletRequest);
    }

    @PutMapping(value = "/organisation/{orgId}/masterUser/{email}/updatePropertyAdminByMasterUser")
    public ResponseEntity<?> updatePropertyAdminByMasterUser(@RequestParam(required = false) String loggedInUser, @PathVariable String orgId, @PathVariable String email, @RequestBody UserDTO userdto, HttpServletRequest httpServletRequest) throws IOException {
        return userService.updatePropertyAdminByMasterUser(orgId, email, userdto, loggedInUser, httpServletRequest);
    }

    @GetMapping(value = "/user/{email}/getAllUserCount")
    public ResponseEntity<?> getAllUserCount(@PathVariable String email,
                                             @RequestParam(required = false) String loggedInUser,
                                             @RequestParam(required = false, defaultValue = "all") String key,
                                             @RequestParam(defaultValue = "all") List<String> userProfiles,
                                             @RequestParam(defaultValue = "false") boolean withoutProfile,
                                             @RequestParam(defaultValue = "all") String orgId, @RequestParam(defaultValue = "all") String active,
                                             HttpServletRequest httpServletRequest) {
        return userService.getAllUserCount(email, key, loggedInUser, userProfiles, withoutProfile, orgId, active, httpServletRequest);
    }


    @PostMapping(value = "/organisation/{orgId}/user/{email}/location/withinBoundary")
    public ResponseEntity<?> checkerUserLocationWithinBoundaryByCoordinatesAndVdmsId(@PathVariable String orgId, @PathVariable String email, @RequestBody JSONObject payload, @RequestParam String loggedInUser, HttpServletRequest httpServletRequest) {
        return userService.checkerUserLocationWithinBoundaryByCoordinatesAndVdmsId(orgId, email, payload, loggedInUser, httpServletRequest);

    }


}
