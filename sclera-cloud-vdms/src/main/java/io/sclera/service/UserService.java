package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.uuid.Generators;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.exception.ServerException;
import io.sclera.repository.UserRepository;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    CustomerOrganisationService customerorganisationService;
    @Autowired
    private AlertScheduleService alertScheduleService;
    @Autowired
    private TouchScreenAlertService touchScreenAlertService;
    @Autowired
    private CustomerOrganisationService customerOrganisationService;
    @Autowired
    private IocService iocService;

    @Autowired
    private WebClientService webClientService;
    @Autowired
    private ProfileService profileService;

    @Autowired
    private AddressService addressService;
    @Autowired
    private VdmsAccessVisibilityService vdmsAccessVisibilityService;
    @Autowired
    private VdmsService vdmsService;
    @Autowired
    private VdmsVisibilityService vdmsvisibilityService;

    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private AwsService awsService;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public ResponseEntity<?> createMasterUser(UserDTO userdto, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload:UserDTO:{}", userdto);
        if (userdto != null) {
            if (userdto.getRole().equals("master-user")) {
                if (userdto.getLanguage() == null) {
                    userdto.setLanguage("en");
                }
                int user_db_data = userRepository.checkUser(userdto.getEmail());
                if (user_db_data == 0) {
                    BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
                    userdto.setCreation_timestamp(creation_time);
                    userdto.setOrganisation_id(userdto.getOrganisation_id());
                    userdto.setActive(0);

                    try {
                        customerorganisationService.addCustomerOrganisationById(userdto.getOrganisation_id(), userdto.getCompany_name(), userdto.getIs_enterprise(), httpServletRequest);
                    } catch (DataIntegrityViolationException e) {
                        userActionLogService.addUserActionLog(null, "User", "ADD", "Duplicate entry " + userdto.getCompany_name() + " for customer organisation", "failed");
                        throw new ServerException("Duplicate entry " + userdto.getCompany_name() + " for customer organisation", 727, httpServletRequest.getRequestURI());
                    }
                    userRepository.createNewUserByEmail(userdto.getId(), userdto.getEmail(), userdto.getRole(), userdto.getOrganisation_id(), userdto.getCreation_timestamp());
                    log.info("Master user {} created successfully", userdto.getEmail());
                    userActionLogService.addUserActionLog(null, "User", "ADD", "A Master User With Email:" + userdto.getEmail() + " Is Created", "success");
                    ResponseDTO responseDTO = ScleraUtils.generatePayload("Master user created successfully", 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);

                } else {
                    log.error("User With Email:" + userdto.getEmail() + " Already Exist .Endpoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(null, "User", "ADD", "User With Email:" + userdto.getEmail() + " Already Exist", "failed");
                    throw new ClientException("User already exists", 725, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role Not Authorised. Endpoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(null, "User", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid client params. Endpoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(null, "User", "ADD", "Invalid client params", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> createUserByPrivilegedUser(UserDTO userDTO, String orgId, String email, boolean ipAclEnabled, boolean geoAclEnabled, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Org_Id:{},Email:{},loggedInUser:{},ipAclEnabled:{},geoAclEnabled:{}", userDTO, orgId, email, loggedInUser, ipAclEnabled, geoAclEnabled);
        if (userDTO != null && orgId != null && email != null) {

            BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
            userDTO.setCreation_timestamp(creation_time);
            userDTO.setOrganisation_id(orgId);

            ResponseDTO loginResult = webClientService.createUserByPrivilegedUserInLoginDB(orgId, email, userDTO, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                userRepository.createNewUserByEmail(userDTO.getId(), userDTO.getEmail(), userDTO.getRole(), orgId, userDTO.getCreation_timestamp());
                updateVdmsSyncByOrganisationIdV2(userDTO.getEmail(), httpServletRequest);
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User with Email:" + userDTO.getEmail() + " is created", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("User is created successfully", 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 727) {
                log.error("Duplicate entry:{},for customer organisation.EndPoint:{}", userDTO.getCompany_name(), httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Duplicate entry " + userDTO.getCompany_name() + " for customer organisation", "failed");
                throw new ServerException("Duplicate entry " + userDTO.getCompany_name() + " for customer organisation", 727, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 725) {
                log.error("User already exists in login server.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User With Name:" + userDTO.getEmail() + " Already Exist In Login Server", "failed");
                throw new ClientException("User already exists in login server", 725, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(email, "User", "ADD", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 720) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Found:" + userDTO.getRole(), "failed");
                log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Already Exist", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 781) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Not able to create User in Keycloak", "failed");
                throw new ClientException("Not able to create User", 781, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> createUserByMasterUser(UserDTO userdto, String customerOrgId, String email, boolean ipAclEnabled, boolean geoAclEnabled, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Org_Id:{},Email:{},loggedInUser:{},ipAclEnabled:{},geoAclEnabled:{}", userdto, customerOrgId, email, loggedInUser, ipAclEnabled, geoAclEnabled);
        if (userdto != null && customerOrgId != null && email != null) {

            String role = userRepository.getRoleNameByUserEmail(email);
            if (role != null) {

                BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
                userdto.setCreation_timestamp(creation_time);

                ResponseDTO loginResult = webClientService.createUserInLoginDB(customerOrgId, email, userdto, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
                log.info("login result:{}", loginResult);

                if (loginResult.getStatus() == 200) {
                    log.info("loginResult :{}", loginResult.getData().toString());
                    userRepository.createNewUserByEmail(loginResult.getData().toString(), userdto.getEmail(), userdto.getRole(), customerOrgId, userdto.getCreation_timestamp());
                    updateVdmsSyncByOrganisationIdV2(userdto.getEmail(), httpServletRequest);
                    userActionLogService.addUserActionLog(email, "User", "ADD", "A User With Email:" + userdto.getEmail() + " Is Created By Master user", "success");
                    ResponseDTO responseDTO = ScleraUtils.generatePayload("User is created successfully", 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else if (loginResult.getStatus() == 727) {
                    log.error("Duplicate entry:{},for customer organisation.EndPoint:{}", userdto.getCompany_name(), httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Duplicate entry " + userdto.getCompany_name() + " for customer organisation", "failed");
                    throw new ServerException("Duplicate entry " + userdto.getCompany_name() + " for customer organisation", 727, httpServletRequest.getRequestURI());
                } else if (loginResult.getStatus() == 725) {
                    log.error("User already exists in login server.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("User already exists in login server", 725, httpServletRequest.getRequestURI());
                } else if (loginResult.getStatus() == 702) {
                    log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Found:" + userdto.getRole(), "failed");
                    throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
                } else if (loginResult.getStatus() == 700) {
                    log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(email, "User", "ADD", "Invalid Client Parameters", "failed");
                    throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
                } else if (loginResult.getStatus() == 720) {
                    log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Found:" + userdto.getRole(), "failed");
                    throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
                } else if (loginResult.getStatus() == 701) {
                    log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Not Found", "failed");
                    throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
                } else if (loginResult.getStatus() == 781) {
                    log.error("Not able to create user in keycloak:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Not able to create", "failed");
                    throw new ClientException("Not able to create user", 781, httpServletRequest.getRequestURI());
                } else {
                    log.error("Something went wrong/Thread Interrupted");
                    userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Something went wrong/Thread Interrupted", "failed");
                    throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Not Found", "failed");
                throw new ClientException("User not found ", 701, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> createUserByAdmin(UserDTO userdto, String email, boolean ipAclEnabled, boolean geoAclEnabled, String
            loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Email:{},loggedInUser:{},ipAclEnabled:{},geoAclEnabled:{}", userdto, email, loggedInUser, ipAclEnabled, geoAclEnabled);
        if (userdto != null && email != null) {
            if (userdto.getLanguage() == null) {
                userdto.setLanguage("EN");
            }
            String role = userRepository.getRoleNameByUserEmail(email);
            if (role != null) {
                switch (userdto.getRole()) {
                    case "master-user":
                        log.info("Create Master User By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "A Master User With Email:" + userdto.getEmail() + " Is Created", "success");
                        return this.createMasterUser(userdto, httpServletRequest);
                    case "org-admin":
                        log.info("Create Org Admin By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "A Org Admin With Email:" + userdto.getEmail() + " Is Created", "success");
                        return this.createOrgAdminByMasterUser(userdto, userdto.getOrganisation_id(), email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
                    case "property-admin":
                        log.info("Create Property Admin By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "A Property Admin With Email:" + userdto.getEmail() + " Is Created", "success");
                        return this.createPropertyAdminByMasterUser(userdto, userdto.getOrganisation_id(), email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
                    case "user":
                        log.info("Create User By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "A User With Email:" + userdto.getEmail() + " Is Created", "success");
                        return this.createUserByMasterUser(userdto, userdto.getOrganisation_id(), email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
                    case "data-entry":
                        log.info("Create Data Entry By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "A Data Entry With Email:" + userdto.getEmail() + " Is Created", "success");
                        return this.createDataEntryByAdmin(userdto, email, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
                    default:
                        log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Authorised", "failed");
                        throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Not Found", "failed");
                throw new ClientException("User Not Found", 903, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> createAdminBySuperAdmin(UserDTO userDTO, String email,boolean ipAclEnabled,boolean geoAclEnabled, String
            loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Email:{},loggedInUser:{},ipAclEnabled:{},geoAclEnabled:{}", userDTO, email, loggedInUser,ipAclEnabled,geoAclEnabled);
        if (userDTO != null && email != null) {

            BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
            userDTO.setCreation_timestamp(creation_time);
            userDTO.setCreation_timestamp(creation_time);
            userDTO.setActive(0);

            ResponseDTO loginResult = webClientService.createAdminInLoginDB(userDTO, email,ipAclEnabled,geoAclEnabled, loggedInUser, httpServletRequest);

            if (loginResult.getStatus() == 200) {
                userRepository.createNewUserByEmail(loginResult.getData().toString(), userDTO.getEmail(), userDTO.getRole(), null, userDTO.getCreation_timestamp());

                if (userDTO.getFullAccess().equals(1)) {
                    vdmsAccessVisibilityService.updateVdmsAccessVisibilityService(userDTO.getEmail(), userDTO.getFullAccess(), httpServletRequest);
                } else {
                    vdmsAccessVisibilityService.replaceVdmsAccessVisibilityByEmail(userDTO.getEmail(), userDTO.getDevUIds(), loggedInUser, httpServletRequest);
                }

                userActionLogService.addUserActionLog(email, "User", "ADD", "Admin With Email:" + userDTO.getEmail() + " Is Created", "success");
                log.info("Create Admin By Super Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Create Admin By Super Admin", 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 725) {
                log.error("User already exists in login server.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User With Email:" + userDTO.getEmail() + " already exists in login server", "failed");
                throw new ClientException("User already exists in login server", 725, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(email, "User", "ADD", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 720) {
                log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Found:" + userDTO.getRole(), "failed");
                throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(email, "User", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> createDataEntryByAdmin(UserDTO userDTO, String email, boolean ipAclEnabled, boolean geoAclEnabled, String
            loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Email:{},ipAclEnabled:{},geoAclEnabled:{}", userDTO, email, ipAclEnabled, geoAclEnabled);
        if (userDTO != null && email != null) {


            BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
            userDTO.setCreation_timestamp(creation_time);


            String otp = Generators.timeBasedGenerator().generate().toString();

            userDTO.setEmailVerificationToken(otp);
            userDTO.setCreation_timestamp(creation_time);
            userDTO.setActive(0);
            ResponseDTO loginResult = webClientService.createDataEntryInLoginDB(userDTO, email,ipAclEnabled,geoAclEnabled, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                userRepository.createNewUserByEmail(loginResult.getData().toString(), userDTO.getEmail(), userDTO.getRole(), null, userDTO.getCreation_timestamp());
                log.info("Create Data Entry By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Data entry has been created successfully", 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 725) {
                log.error("User already exists in login server.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User already exists in login server", "failed");
                throw new ClientException("User already exists in login server", 725, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> createPropertyAdminByMasterUser(UserDTO userdto, String customerOrgId, String
            email, boolean ipAclEnabled, boolean geoAclEnabled, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Org_Id:{},Email:{},loggedInUser:{},ipAclEnabled:{},geoAclEnabled:{}", userdto, customerOrgId, email, loggedInUser, ipAclEnabled, geoAclEnabled);
        if (userdto != null && customerOrgId != null && email != null) {


            BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
            userdto.setCreation_timestamp(creation_time);

            ResponseDTO loginResult = webClientService.createPropertyAdminInLoginDB(customerOrgId, email, userdto, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                userRepository.createNewUserByEmail(loginResult.getData().toString(), userdto.getEmail(), userdto.getRole(), customerOrgId, userdto.getCreation_timestamp());
                updateVdmsSyncByOrganisationIdV2(userdto.getEmail(), httpServletRequest);
                log.info("Create User By Master User.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User with Email:" + userdto.getEmail() + " is created", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("User is created successfully", 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
            if (loginResult.getStatus() == 727) {
                log.error("Duplicate entry:{},for customer organisation.EndPoint:{}", userdto.getCompany_name(), httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Duplicate entry " + userdto.getCompany_name() + " for customer organisation", "failed");
                throw new ServerException("Duplicate entry " + userdto.getCompany_name() + " for customer organisation", 727, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 725) {
                log.error("User already exists in login server.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Already Exist", "failed");
                throw new ClientException("User already exists in login server", 725, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Found:" + userdto.getRole(), "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 720) {
                log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Found:" + userdto.getRole(), "failed");
                throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 781) {
                log.error("Not able to create user in keycloak.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Not able to create user", "failed");
                throw new ClientException("Not able to create user", 781, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> createSuperAdminByEmail(UserDTO userDTO, String email, String
            loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Email:{},loggedInUser:{}", userDTO, email, loggedInUser);
        if (userDTO != null && email != null) {


            BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
            userDTO.setCreation_timestamp(creation_time);

            userDTO.setCreation_timestamp(creation_time);

            ResponseDTO loginResult = webClientService.createSuperAdminInLoginDB(userDTO, loggedInUser, userDTO.getEmail());
            if (loginResult.getStatus() == 200) {
                userRepository.createNewUserByEmail(loginResult.getData().toString(), userDTO.getEmail(), userDTO.getRole(), null, userDTO.getCreation_timestamp());

                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "A Super Admin With Email:" + userDTO.getEmail() + " Is Created", "success");
                log.info("Super admin has been created successfully.EndPoint:{}", httpServletRequest.getRequestURI());
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Super admin has been created successfully", 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
            if (loginResult.getStatus() == 725) {
                log.error("User already exists in login server.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User With Email:" + userDTO.getEmail() + " already exists in login server", "failed");
                throw new ClientException("User already exists in login server", 725, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 720) {
                log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Found:" + userDTO.getRole(), "failed");
                throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }


        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateMasterUserByEmail(String customerOrgId, String user_email, UserDTO
            userdto, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        if (user_email != null && userdto != null && customerOrgId != null) {

            try {
                customerorganisationService.updateCompanyNameByOrganisationId(userdto.getCompany_name(), userdto.getOrganisation_id(), httpServletRequest);
            } catch (Exception e) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Duplicate entry " + userdto.getCompany_name() + " for customer organisation", "failed");
                throw new ServerException("Duplicate entry " + userdto.getCompany_name() + " for customer organisation", 727, httpServletRequest.getRequestURI());
            }
            ResponseDTO loginResult = webClientService.updateMasterUserInLoginDB(userdto, customerOrgId, user_email, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                updateVdmsSyncByOrganisationIdV2(userdto.getEmail(), httpServletRequest);
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A Master User With Email:" + userdto.getEmail() + " Is Updated", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Master user details updated successfully", 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 702) {
                customerorganisationService.deleteCustomerOrganisationById(customerOrgId, httpServletRequest);
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                customerorganisationService.deleteCustomerOrganisationById(customerOrgId, httpServletRequest);
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                customerorganisationService.deleteCustomerOrganisationById(customerOrgId, httpServletRequest);
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> updateAdminBySuperAdmin(String user_email, UserDTO userdto, String
            loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Email:{},loggedInUser:{}", userdto, user_email, loggedInUser);
        if (user_email != null && userdto != null) {


            ResponseDTO loginResult = webClientService.updateAdminInLoginDB(userdto, user_email, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                log.info("Update Admin By Super Admin.Email:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                if (userdto.getFullAccess()==1){
                    vdmsAccessVisibilityService.updateVdmsAccessVisibilityService(userdto.getEmail(), userdto.getFullAccess(), httpServletRequest);
                }
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", userdto.getEmail() + " is updated", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Admin details updated successfully", 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 702) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> updateSuperAdminByEmail(UserDTO userdto, String email, String
            loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Email:{},loggedInUser:{}", userdto, email, loggedInUser);
        if (email != null && userdto != null) {

            if (userdto.getRole().equals("super-admin")) {

                ResponseDTO loginResult = webClientService.updateSuperAdminInLoginDB(userdto, userdto.getEmail(), loggedInUser, httpServletRequest);
                if (loginResult.getStatus() == 200) {
                    log.info("Update Super_Admin By Email:{}.EndPoint:{}", email, httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A Super Admin With Email:" + userdto.getEmail() + " Is Updated", "success");
                    ResponseDTO responseDTO = ScleraUtils.generatePayload("Super Admin updated successfully", 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else if (loginResult.getStatus() == 702) {
                    userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                    log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
                } else if (loginResult.getStatus() == 700) {
                    log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
                    throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
                } else if (loginResult.getStatus() == 701) {
                    log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "User Not Found", "failed");
                    throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
                } else {
                    log.error("Something went wrong/Thread Interrupted");
                    userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Something went wrong/Thread Interrupted", "failed");
                    throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
                }
            } else {
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not authorised", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteUserByMasterUser(String orgId, String email, UserDTO userDTO, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: UserDTO:{},Email:{},orgId:{},loggedInUser:{}", userDTO, email, orgId, loggedInUser);
        if (orgId != null && email != null && userDTO != null) {

            ResponseDTO loginResult = webClientService.deleteUserByMasterUser(orgId, email, userDTO, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                userRepository.deleteUserByEmail(userDTO.getEmail());
                addressService.deleteAddressById(userDTO.getEmail(), httpServletRequest);
                vdmsvisibilityService.deleteVisibleVdmsByEmail(userDTO.getEmail());
                updateVdmsSyncByOrganisationIdV2(userDTO.getEmail(), httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(email, "User", "DELETE", "A User With Email:" + userDTO.getEmail() + " Is Deleted By Master User", "success");
                log.info("Delete User By Master_User.EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(email, "User", "DELETE", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deletePropertyAdminByEmail(String orgId, String email, UserDTO userDTO, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: UserDTO:{},Email:{},orgId:{},loggedInUser:{}", userDTO, email, orgId, loggedInUser);
        if (orgId != null && email != null && userDTO != null) {

            ResponseDTO loginResult = webClientService.deletePropertyAdminByMasterUser(orgId, email, userDTO, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                alertScheduleService.deleteAlertScheduleByEmail(userDTO.getEmail(), httpServletRequest);
                userRepository.deleteUserByEmail(userDTO.getEmail());
                addressService.deleteAddressById(userDTO.getEmail(), httpServletRequest);
                vdmsvisibilityService.deleteVisibleVdmsByEmail(userDTO.getEmail());
                updateVdmsSyncByOrganisationIdV2(userDTO.getEmail(), httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "A User With Email:" + userDTO.getEmail() + " Is Deleted By Master User", "success");
                log.info("Delete User By Master_User.EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(email, "User", "DELETE", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }


    public ResponseEntity<?> deleteUserByAdmin(String email, UserDTO userDTO, String
            loggedInUser, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        log.info("Payload: UserDTO:{},Email:{},loggedInUser:{}", userDTO, email, loggedInUser);
        if (email != null && userDTO != null) {

            if (userDTO.getRole().equals("master-user")) {
                ResponseDTO storesResult = webClientService.deleteUserInStoresDB(userDTO.getOrganisation_id(), userDTO.getEmail(), httpServletRequest);
                if (storesResult.getStatus() == 200) {
                    ResponseDTO loginResult = webClientService.deleteUserByAdmin(userDTO.getOrganisation_id(), email, userDTO, loggedInUser, httpServletRequest);
                    if (loginResult.getStatus() == 200) {
                        Set<String> child_users = userRepository.getUserEmailsByOrganisationId(userDTO.getOrganisation_id(), userDTO.getEmail());
                        log.info("Delete User By Admin.EndPoint:{}", httpServletRequest.getRequestURI());

                        alertScheduleService.deleteAlertScheduleByOrgId(userDTO.getOrganisation_id(), httpServletRequest);

                        if (child_users != null && !child_users.isEmpty()) {
                            for (String child : child_users) {
                                userRepository.deleteUserByEmail(child);
                                addressService.deleteAddressById(child, httpServletRequest);
                                vdmsvisibilityService.deleteVisibleVdmsByEmail(child);
                            }
                        }

                        this.deleteUserProfileByEmail(userDTO.getEmail(), httpServletRequest);
                        vdmsService.deleteVdmsByEmail(userDTO.getEmail(), loggedInUser, httpServletRequest);
                        userRepository.deleteUserByEmail(userDTO.getEmail());
                        addressService.deleteAddressById(userDTO.getEmail(), httpServletRequest);
                        profileService.deleteProfileByCustomerOrganisationId(userDTO.getOrganisation_id(), userDTO.getEmail(), loggedInUser, httpServletRequest);
                        customerorganisationService.deleteCustomerOrganisationById(userDTO.getOrganisation_id(), httpServletRequest);
                        log.info("Deleted User" + userDTO.getEmail() + " By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "A  User With Email:" + userDTO.getEmail() + " Is Deleted", "success");
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        log.error("Failed to delete user.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Failed to delete user:" + userDTO.getEmail(), "failed");
                        throw new ServerException("Failed to delete user in Login DB", 809, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("Failed to delete user.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Failed to delete user in Stores DB", "failed");
                    throw new ServerException("Failed to delete user in Stores DB", 809, httpServletRequest.getRequestURI());
                }

            } else if (userDTO.getRole().equals("org-admin") || userDTO.getRole().equals("user") || userDTO.getRole().equals("property-admin")) {
                ResponseDTO loginResult = webClientService.deleteUserByAdmin(userDTO.getOrganisation_id(), email, userDTO, loggedInUser, httpServletRequest);
                if (loginResult.getStatus() == 200) {
                    alertScheduleService.deleteAlertScheduleByEmail(userDTO.getEmail(), httpServletRequest);
                    userRepository.deleteUserByEmail(userDTO.getEmail());
                    addressService.deleteAddressById(userDTO.getEmail(), httpServletRequest);
                    vdmsvisibilityService.deleteVisibleVdmsByEmail(userDTO.getEmail());
                    updateVdmsSyncByOrganisationIdV2(userDTO.getEmail(), httpServletRequest);
                    log.info("Delete User By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "A  User With Email:" + userDTO.getEmail() + " Is Deleted", "success");
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Failed to delete user.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Failed to delete user:" + userDTO.getEmail(), "failed");
                    throw new ServerException("Failed to delete user in Login DB", 809, httpServletRequest.getRequestURI());
                }

            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client params.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteAdminBySuperAdmin(String email, UserDTO userDTO, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:email:{} UserDTO:{},loggedInUser:{}", email, userDTO, loggedInUser);
        if (email != null && userDTO != null) {

            ResponseDTO loginResult = webClientService.deleteAdminBySuperAdmin(email, userDTO, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                vdmsAccessVisibilityService.deleteVdmsAccessVisibilityByEmail(userDTO.getEmail(), httpServletRequest);
                this.deleteUserProfileByEmail(userDTO.getEmail(), httpServletRequest);
                alertScheduleService.deleteAlertScheduleByEmail(userDTO.getEmail(), httpServletRequest);
                userRepository.deleteUserByEmail(userDTO.getEmail());
                addressService.deleteAddressById(userDTO.getEmail(), httpServletRequest);
                log.info("Deleted Admin By Super Admin.Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "A User With Email:" + userDTO.getEmail() + " Is Deleted", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateDataEntry(String user_email, UserDTO userdto, String
            loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Email:{},loggedInUser:{}", userdto, user_email, loggedInUser);
        if (user_email != null && userdto != null) {

            ResponseDTO loginResult = webClientService.updateDataEntryInLoginDB(userdto, user_email, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A Data Entry With Email:" + userdto.getEmail() + " Is Updated", "success");
                log.info("Update Data_Entry. Email:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                ResponseDTO responseDTO = ScleraUtils.generatePayload("User details updated successfully", 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 702) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(user_email, "User", "UPDATE", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteDataEntryByAdmin(String email, UserDTO userDTO, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: UserDTO:{},Email:{},loggedInUser:{}", userDTO, email, loggedInUser);
        if (email != null && userDTO != null) {
            ResponseDTO loginResult = webClientService.deleteDataEntryInLoginDB(email, userDTO, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                alertScheduleService.deleteAlertScheduleByEmail(userDTO.getEmail(), httpServletRequest);
                userRepository.deleteUserByEmail(userDTO.getEmail());
                addressService.deleteAddressById(userDTO.getEmail(), httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Delete data_Entry By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "A User With Email:" + userDTO.getEmail() + " Is Deleted By: " + email, "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(email, "User", "DELETE", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateUserByAdmin(UserDTO userdto, String user_email, String
            loggedInUser, HttpServletRequest httpServletRequest) throws
            IOException {
        log.info("Payload: UserDTO:{},Email:{},loggedInUser:{}", userdto, user_email, loggedInUser);
        if (user_email != null && userdto != null) {
            String role = userRepository.getRoleNameByUserEmail(user_email);
            log.info("ROLE :" + role);
            if (role != null) {
                if (role.equals("super-admin") || role.equals("admin")) {
                    switch (userdto.getRole()) {
                        case "master-user":
                            log.info("Update Master User By Admin:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A Master User With Email:" + userdto.getEmail() + " Is Updated", "success");
                            return this.updateMasterUserByEmail(userdto.getOrganisation_id(), user_email, userdto, loggedInUser, httpServletRequest);
                        case "org-admin":
                            log.info("Update Org_Admin By Admin. Email:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A Org Admin With Email:" + userdto.getEmail() + " Is Updated", "success");
                            return this.updateOrgAdminByMasterUser(userdto.getOrganisation_id(), user_email, userdto, loggedInUser, httpServletRequest);
                        case "property-admin":
                            log.info("Update Property Admin By Admin. Email:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A Property Admin With Email:" + userdto.getEmail() + " Is Updated", "success");
                            return this.updatePropertyAdminByMasterUser(userdto.getOrganisation_id(), user_email, userdto, loggedInUser, httpServletRequest);
                        case "user":
                            log.info("Update User By Admin. Email:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A User With Email:" + userdto.getEmail() + " Is Updated", "success");
                            return this.updateUserByMasterUser(userdto.getOrganisation_id(), user_email, userdto, loggedInUser, httpServletRequest);
                        case "data-entry":
                            log.info("Update Data Entry By Admin.Email:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A Data Entry With Email:" + userdto.getEmail() + " Is Updated", "success");
                            return this.updateDataEntry(user_email, userdto, loggedInUser, httpServletRequest);
                        default:
                            log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                            throw new ClientException("Role not authorised", 702, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                    throw new ClientException("Role not authorised", 702, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateOrgAdminByMasterUser(String customerOrgId, String user_email, UserDTO
            userdto, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},customerOrgId:{},Email:{},loggedInUser:{}", userdto, customerOrgId, user_email, loggedInUser);
        if (user_email != null && userdto != null) {

            ResponseDTO loginResult = webClientService.updateOrgAdminInLoginDB(userdto, customerOrgId, user_email, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                userRepository.updateRoleByEmail(userdto.getRole(), userdto.getEmail());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A Org Admin With Email:" + userdto.getEmail() + " Is Updated", "success");
                updateVdmsSyncByOrganisationIdV2(userdto.getEmail(), httpServletRequest);
                log.info("Update Org_Admin By Master User. Email:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 720) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Found", "failed");
                log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 702) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(user_email, "User", "UPDATE", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());

            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> updatePropertyAdminByMasterUser(String customerOrgId, String user_email, UserDTO
            userdto, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Org_Id:{},Email:{},loggedInUser:{}", userdto, customerOrgId, user_email, loggedInUser);
        if (user_email != null && userdto != null) {


            ResponseDTO loginResult = webClientService.updatePropertyAdminInLoginDB(userdto, customerOrgId, user_email, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                String oldRole = userRepository.getRoleNameByUserEmail(userdto.getEmail());
                if (userdto.getRole().equals("org-admin") && oldRole.equals("property-admin")) {
                    vdmsvisibilityService.deleteVisibleVdmsByEmail(userdto.getEmail());
                }
                userRepository.updateRoleByEmail(userdto.getRole(), userdto.getEmail());
                updateVdmsSyncByOrganisationIdV2(userdto.getEmail(), httpServletRequest);
                log.info("Update Property Admin By Master User. Email:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A User With Email:" + userdto.getEmail() + " Is Updated", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 720) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Found", "failed");
                log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(user_email, "User", "UPDATE", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateUserByMasterUser(String customerOrgId, String user_email, UserDTO
            userdto, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Org_Id:{},Email:{},loggedInUser:{}", userdto, customerOrgId, user_email, loggedInUser);
        if (user_email != null && userdto != null) {


            ResponseDTO loginResult = webClientService.updateUserInLoginDB(userdto, customerOrgId, user_email, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                String oldRole = userRepository.getRoleNameByUserEmail(userdto.getEmail());
                if (userdto.getFullAccess() != null && userdto.getFullAccess().equals(1)) {
                    vdmsvisibilityService.updateVdmsAccessVisibilityService(userdto.getEmail(), userdto.getFullAccess(), httpServletRequest);
                }
                if (userdto.getRole().equals("org-admin") && oldRole.equals("user")) {
                    vdmsvisibilityService.deleteVisibleVdmsByEmail(userdto.getEmail());
                }
                userRepository.updateRoleByEmail(userdto.getRole(), userdto.getEmail());
                updateVdmsSyncByOrganisationIdV2(userdto.getEmail(), httpServletRequest);
                log.info("Update User By Master User. Email:{}.EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "A User with Email:" + userdto.getEmail() + " is updated", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 720) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Found", "failed");
                log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> createOrgAdminByMasterUser(UserDTO userdto, String customerOrgId, String email, boolean ipAclEnabled, boolean geoAclEnabled, String
            loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: UserDTO:{},Org_Id:{},Email:{},loggedInUser:{},ipAclEnabled:{},geoAclEnabled:{}", userdto, customerOrgId, email, loggedInUser, ipAclEnabled, geoAclEnabled);
        if (userdto != null && customerOrgId != null && email != null) {

            BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
            userdto.setCreation_timestamp(creation_time);
            userdto.setOrganisation_id(customerOrgId);

            ResponseDTO loginResult = webClientService.createOrgAdminInLoginDB(customerOrgId, email, userdto, ipAclEnabled, geoAclEnabled, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                userRepository.createNewUserByEmail(loginResult.getData().toString(), userdto.getEmail(), userdto.getRole(), customerOrgId, userdto.getCreation_timestamp());
                UserDTO response = UserDTO.builder()
                        .name(userdto.getName())
                        .email(userdto.getEmail())
                        .language(userdto.getLanguage())
                        .build();
                updateVdmsSyncByOrganisationIdV2(userdto.getEmail(), httpServletRequest);
                log.info("User with email:" + userdto.getEmail() + " is created successfully");
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User with email:" + userdto.getEmail() + " is created", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload(response, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);

            } else if (loginResult.getStatus() == 727) {
                log.error("Duplicate entry:{},for customer organisation.EndPoint:{}", userdto.getCompany_name(), httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Duplicate entry " + userdto.getCompany_name() + " for customer organisation", "failed");
                throw new ServerException("Duplicate entry " + userdto.getCompany_name() + " for customer organisation", 727, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 725) {
                log.error("User already exists in login server.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User already exists in login server", "failed");
                throw new ClientException("User already exists in login server", 725, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 702) {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 720) {
                log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Role Not Found:" + userdto.getRole(), "failed");
                throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteOrgAdminByMasterUser(String orgId, String email, UserDTO userDTO, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: UserDTO:{},OrgId:{},Email:{},loggedInUser:{}", userDTO, orgId, email, loggedInUser);
        if (orgId != null && email != null && userDTO != null) {


            ResponseDTO loginResult = webClientService.deleteOrgAdminByMasterUserFromLogin(orgId, email, userDTO, loggedInUser, httpServletRequest);
            if (loginResult.getStatus() == 200) {
                alertScheduleService.deleteAlertScheduleByEmail(userDTO.getEmail(), httpServletRequest);
                userRepository.deleteUserByEmail(userDTO.getEmail());
                addressService.deleteAddressById(userDTO.getEmail(), httpServletRequest);
                vdmsvisibilityService.deleteVisibleVdmsByEmail(userDTO.getEmail());
                updateVdmsSyncByOrganisationIdV2(userDTO.getEmail(), httpServletRequest);
                userActionLogService.addUserActionLog(email, "User", "DELETE", "A Org-Admin With Email:" + userDTO.getEmail() + " Is Deleted", "success");
                log.info("Delete Org Admin By Master User. Email:{}.EndPoint:{}", email, httpServletRequest.getRequestURI());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else if (loginResult.getStatus() == 702) {
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Role Not Authorised", "failed");
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised", 702, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 700) {
                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
                throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
            } else if (loginResult.getStatus() == 701) {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "User Not Found", "failed");
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            } else {
                log.error("Something went wrong/Thread Interrupted");
                userActionLogService.addUserActionLog(email, "User", "DELETE", "Something went wrong/Thread Interrupted", "failed");
                throw new ClientException("Something went wrong/Thread Interrupted", 800, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getUserList(String user_email, String key, int pageNo, int pageSize, String loggedInUser, List<String> userProfiles,
                                         boolean withoutProfile, String active, HttpServletRequest httpServletRequest) {
        log.info("Payload:User_Email:{},Key:{},PageNo:{},PageSize:{},loggedInUser:{},userProfiles:{},active:{},withoutProfile:{}",
                user_email, key, pageNo, pageSize, loggedInUser, userProfiles, active, withoutProfile);
        if (user_email != null) {
            String role = getRoleNameByUserEmail(user_email, httpServletRequest);
            if (role != null) {
                int offset = ScleraUtils.calculateOffset(pageNo, pageSize);
                int limit = pageSize;
                String customer_org_id = userRepository.getCustomerOrganisationIdByUserEmail(user_email);
                if (customer_org_id != null) {
                    if (role.equalsIgnoreCase("master-user")) {
                        List<UserDTO> data = webClientService.getUserDetailsByAdminEmail(user_email, key, customer_org_id, limit, offset, userProfiles, withoutProfile, active, httpServletRequest);
                        List<String> allEmails = new ArrayList<>();
                        for (UserDTO userDTO : data) {
                            String email = userDTO.getEmail();
                            if (email != null) {
                                allEmails.add(email);
                            }
                        }
                        List<UserDTO> userInfo = webClientService.getAllUserInfo(httpServletRequest);
                        for (UserDTO info : userInfo) {
                            for (UserDTO dataInfo : data) {
                                if (info.getEmail().equals(dataInfo.getEmail()) && info.getImage_url() != null) {
                                    dataInfo.setImage_url(info.getImage_url());
                                }
                            }
                        }
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);

                    } else if (role.equalsIgnoreCase("org-admin")) {
                        List<UserDTO> data = webClientService.getUserDetailsByAdminEmail(user_email, key, customer_org_id, limit, offset, userProfiles, withoutProfile, active, httpServletRequest);
                        List<String> allEmails = new ArrayList<>();
                        for (UserDTO userDTO : data) {
                            String email = userDTO.getEmail();
                            if (email != null) {
                                allEmails.add(email);
                            }
                        }
                        List<UserDTO> userInfo = webClientService.getAllUserInfo(httpServletRequest);
                        for (UserDTO info : userInfo) {
                            for (UserDTO dataInfo : data) {
                                if (info.getEmail().equals(dataInfo.getEmail()) && info.getImage_url() != null) {
                                    dataInfo.setImage_url(info.getImage_url());
                                }
                            }
                        }
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else if (role.equalsIgnoreCase("property-admin")) {

                        List<UserDTO> data = webClientService.getUserDetailsByAdminEmail(user_email, key, customer_org_id, limit, offset, userProfiles, withoutProfile, active, httpServletRequest);

                        List<String> vdmsIds = vdmsvisibilityService.getVisibleVdmsByEmail(user_email, httpServletRequest);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);


                    } else if (role.equalsIgnoreCase("user") || role.equalsIgnoreCase("data-entry")) {
                        List<UserDTO> userdto = new ArrayList<>();
                        UserDTO user = new UserDTO();
                        String image_url = webClientService.getImageUrlByEmail(user_email, httpServletRequest);
                        user.setEmail(user_email);
                        user.setRole(role);
                        user.setImage_url(image_url);
                        userdto.add(user);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(userdto, 200, true);
                        log.info("Fetching User List:EndPoint:{}", httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                        throw new ClientException("Role not authorized", 702, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("Customer organisation Id does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("Customer organisation Id does not exist", 718, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }


    public String getRoleNameByUserEmail(String user_email, HttpServletRequest httpServletRequest) {
        log.info("Fetching Role Name By User Email:{},EndPoint:{}", user_email, httpServletRequest.getRequestURI());
        return userRepository.getRoleNameByUserEmail(user_email);
    }

//    public void updateVdmsSyncByOrganisationId(String email, HttpServletRequest httpServletRequest) {
//        String customer_org_id = userRepository.getCustomerOrganisationIdByUserEmail(email);
//        Set<String> vdms_ids = vdmsService.getVdmsIdsByCustomerOrganisationId(customer_org_id, httpServletRequest);
//        if (vdms_ids != null && vdms_ids.size() > 0) {
//            VdmsSyncDTO vdmssyncdto = new VdmsSyncDTO();
//            vdmssyncdto.setUser_sync(1);
//            log.info("Update Vdms Sync By Org_Id:{},EndPoint:{}", customer_org_id, httpServletRequest.getRequestURI());
//            vdms_ids.forEach((vdms) -> {
//                vdmsService.updateVdmsSyncByVdmsId(vdmssyncdto, vdms, httpServletRequest);
//            });
//        }
//    }

    public ResponseEntity<?> getAllUsersByOrganisationId(String organisation_id, String key, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (organisation_id != null) {
            List<UserDTO> users = webClientService.getAllUserInfoByOrganisationId(organisation_id, key, pageNo, pageSize, httpServletRequest);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(users, 200, true);
            log.info("Fetching All Users By Org_Id:{},EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public String getUserActiveByEmail(String email, String timeZone, HttpServletRequest httpServletRequest) {
        log.info("Email:{}", email);
        BigInteger timestamp = BigInteger.valueOf(System.currentTimeMillis());
        JSONObject dateTimeData = touchScreenAlertService.getCurrentDateAndTimeByTimestampAndTimezone(timestamp, timeZone, httpServletRequest);
        String schedule = webClientService.getAlertSchedule(email, dateTimeData, httpServletRequest);
        log.info("schedule:{}", schedule);
        if (schedule == null) {
            return "1";
        }
        return schedule;

    }


    public ResponseEntity<?> getUserDetailsByUserEmail(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (email != null) {
            UserDTO userdto = this.getUserDetailsByEmail(email, httpServletRequest);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(userdto, 200, true);
            log.info("Fetching User Details By User_Email:{}.EndPoint:{}", email, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public UserDTO getUserDetailsByEmail(String user_email, HttpServletRequest httpServletRequest) {

        UserDTO login_data = webClientService.getUserDetailsByEmailFromLoginDB(user_email, httpServletRequest);
        if (login_data != null) {
            log.info("Payload:UserDTO:" + login_data);
            if (login_data != null) {
                Set<VdmsDTO> visible_vdms = vdmsvisibilityService.getVisibleVdms(user_email, httpServletRequest);
                Integer fullAccess = vdmsvisibilityService.getVdmsFullAccessByEmail(user_email, httpServletRequest);
                if (fullAccess != null && fullAccess.equals(1)) {
                    login_data.setFullAccess(1);
                } else {
                    login_data.setVisible_vdms(visible_vdms);
                }
                log.info("Fetching User Details By Email:{},EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                return login_data;
            } else {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getAdminDetailsByAdminEmail(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:email:{}", email);

        UserDTO login_data = webClientService.getUserDetailsByEmailFromLoginDB(email, httpServletRequest);
        if (login_data != null) {

            List<VdmsAccessVisibilityDTO> vdmsAccessVisibility = vdmsAccessVisibilityService.getVdmsAccessVisibilityByEmail(email, httpServletRequest);
            Integer fullAccess = vdmsAccessVisibilityService.getVdmsFullAccessByEmail(email, httpServletRequest);
            if (fullAccess != null && fullAccess.equals(1)) {
                login_data.setFullAccess(1);
            } else {
                login_data.setVdmsAccessVisibility(vdmsAccessVisibility);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(login_data, 200, true);
            log.info("Fetching Admin Details By Admin_Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Something went wrong.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ServerException("Something went wrong", 800, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getMasterUserDetailsByVdmsId(String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {

        if (vdmsId != null) {
            String email = userRepository.getMasterUserEmailByVdmsId(vdmsId);
            UserDTO data = webClientService.getUserDetailsByEmailFromLoginDB(email, httpServletRequest);
            if (data != null) {
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Fetching Master User Details By Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getUserImageByEmail(String email, String loggedInUser, HttpServletRequest
            httpServletRequest) {
        if (email != null) {
//            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "data-entry", "property-admin");
//            if (access) {
            String data = webClientService.getImageUrlByEmail(email, httpServletRequest);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
            log.info("Fetching user Image BY Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            } else {
//                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
//                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
//            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getAllUserInfoByAdminEmail(String admin_email, String key, int pageNo, int pageSize, String loggedInUser,
                                                        List<String> userProfiles, boolean withoutProfile, String orgId,
                                                        String active, HttpServletRequest httpServletRequest) {
        log.info("Payload:admin_email:{},Key:{},PageNo:{},PageSize:{},loggedInUser:{},orgId:{},active:{},withoutProfile:{}",
                admin_email, key, pageNo, pageSize, loggedInUser, orgId, active, withoutProfile);
        if (admin_email != null) {
//            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
//            if (access) {
            String role = getRoleNameByUserEmail(admin_email, httpServletRequest);
            if (role != null) {
                if (role.equalsIgnoreCase("super-admin")) {
                    List<UserDTO> allUserDTO = this.getAllUserDetailsByAdminEmail(role, admin_email, key, pageNo, pageSize, loggedInUser, userProfiles,
                            withoutProfile, orgId, active, httpServletRequest);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(allUserDTO, 200, true);
                    log.info("Fetching all the user info by {},{}.EndPoint:{}", role, admin_email, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else if (role.equalsIgnoreCase("admin")) {
                    List<UserDTO> allUserDTO = this.getAllUserDetailsByAdminEmail(role, admin_email, key, pageNo, pageSize, loggedInUser, userProfiles,
                            withoutProfile, orgId, active, httpServletRequest);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(allUserDTO, 200, true);
                    log.info("Fetching all the user info by {},{}.EndPoint:{}", role, admin_email, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("Role not authorised", 702, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
            }
//            } else {
//                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
//                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
//            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public List<UserDTO> getAllUserDetailsByAdminEmail(String role, String admin_email, String key, int pageno, int pagesize, String loggedInUser,
                                                       List<String> userProfiles, boolean withoutProfile, String orgId, String active, HttpServletRequest httpServletRequest) {
        log.info("Payload:Role:{},admin_email:{},Key:{},PageNo:{},PageSize:{},loggedInUser:{}", role, admin_email, key, pageno, pagesize, loggedInUser);
        int offset = pagesize * (pageno - 1);
        List<UserDTO> allUsers = webClientService.getUserDetailsByAdminEmail(admin_email, key, orgId, pagesize, offset, userProfiles, withoutProfile, active, httpServletRequest);
        log.info("Fetching all the user details by {} {}.Endpoint:{}", role, admin_email, httpServletRequest.getRequestURI());
        return allUsers;
    }

    public ResponseEntity<?> getUserActiveStatusByEmail(String email, String loggedInUser, HttpServletRequest
            httpServletRequest) throws JsonProcessingException {
        if (email != null) {
//            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
//            if (access) {
            UserDTO userdto;
            ResponseDTO response = webClientService.getRoleByUserEmail(null, email, loggedInUser, httpServletRequest);
            log.info("role:{}", response.getData());
            String role = response.getData().toString();
            if (role.equals("vendor") || role.equals("master-vendor")) {
                userdto = webClientService.getVendorDetailsByEmail(email, null, loggedInUser, httpServletRequest);
                userdto.setUser_status("1");

            } else {
                userdto = this.getUserDetailsByEmail(email, httpServletRequest);
                userdto.setUser_status(getUserActiveByEmail(email, userdto.getTimeZone(), httpServletRequest));
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(userdto, 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            } else {
//                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
//                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
//            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> deleteEnterPriseMasterUserAccount(String loggedInUser, String email, UserDTO
            userDTO, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        log.info("Payload:loggedInUser:{},Email:{},userDTO:{}", loggedInUser, email, userDTO);
        if (email != null) {
            String org_id = userRepository.getCustomerOrganisationIdByUserEmail(userDTO.getEmail());
            log.info("org_id:" + org_id);
            Integer is_activated = vdmsService.getActivationStatus(org_id, httpServletRequest);
            log.info("is_activated:" + is_activated);
//            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
//            if (access) {
            if (is_activated == 0) {
                deleteMasterUserByEmail(email, loggedInUser, userDTO, httpServletRequest);
            } else {
                log.error("Can Not Delete,Vdms Activated.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Can Not Delete,Vdms Activated", 700, httpServletRequest.getRequestURI());
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            userActionLogService.addUserActionLog(loggedInUser, "Vdms", "DELETE", "A  Master User With Email:" + email + " Is Deleted.", "success");
            log.info("A Master User With Email:" + userDTO.getEmail() + "Is Deleted.EndPoint:{}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            } else {
//                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
//                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "Role Not Authorised", "failed");
//                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
//            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Vdms", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    private void deleteMasterUserByEmail(String email, String loggedInUser, UserDTO userDTO, HttpServletRequest
            httpServletRequest) throws JsonProcessingException {
        log.info("Payload:loggedInUser:{},Email:{},userDTO:{}", loggedInUser, email, userDTO);
        String org_id = userRepository.getCustomerOrganisationIdByUserEmail(userDTO.getEmail());
        log.info("org_id:" + org_id);
        Integer is_enterprise = customerOrganisationService.getEnterpriseInfoById(org_id);
        log.info("is_enterprise:" + is_enterprise);
        String role = userRepository.getRoleNameByUserEmail(loggedInUser);
        if (role.equals("super-admin") || role.equals("admin")) {
            ResponseDTO storesResult = webClientService.deleteMasterUserInStoresDB(org_id, userDTO.getEmail(), loggedInUser, httpServletRequest);
            if (storesResult.getStatus() == 200) {
                Set<String> child_users = userRepository.getUserEmailsByOrganisationId(org_id, userDTO.getEmail());
                alertScheduleService.deleteAlertScheduleByOrgId(org_id, httpServletRequest);
                if (child_users != null && !child_users.isEmpty()) {
                    for (String child : child_users) {
                        userRepository.deleteUserByEmail(child);
                        addressService.deleteAddressById(child, httpServletRequest);
                        vdmsvisibilityService.deleteVisibleVdmsByEmail(child);
//                        userActionLogService.deleteUserActionLogsByEmail(child);
                    }
                }

                userRepository.deleteUserByEmail(userDTO.getEmail());
                addressService.deleteAddressById(userDTO.getEmail(), httpServletRequest);
                profileService.deleteProfileByCustomerOrganisationId(org_id, userDTO.getEmail(), loggedInUser, httpServletRequest);
                Set<String> vdmsId = vdmsService.getVdmsIdByOrgId(org_id);
                vdmsService.deleteVdmsByListOfVdmsId(userDTO.getEmail(), vdmsId, loggedInUser, httpServletRequest);
                iocService.deleteIocDataByOrgId(org_id, httpServletRequest);
                customerorganisationService.deleteCustomerOrganisationById(org_id, httpServletRequest);
                log.info("Delete EnterPrise Master User.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "A  User With Email:" + userDTO.getEmail() + " Is Deleted", "success");
            } else {
                userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Failed to delete user in Stores DB", "failed");
                log.error("Failed to delete user in Stores DB.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ServerException("Failed to delete user in Stores DB", 809, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "User", "DELETE", "Role Not Authorised", "failed");
            throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
        }
//        } else {
//            log.error("Can Not DELETE Master User Because Is_EnterPrise Is:{},EndPoint:{}", is_enterprise, httpServletRequest.getRequestURI());
//            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
//        }
    }

    public ResponseEntity<?> getAllUserDetails(HttpServletRequest httpServletRequest) {

        List<CustomerOrganisationDto> vdmsOrganisations = customerOrganisationService.getAllOrganisationDetails(httpServletRequest);
        System.out.println("vdmsOrganisation===>" + vdmsOrganisations.size());
        List<CustomerOrganisationDto> vendorOrganisations = webClientService.getAllOrganisationDetailsFromVendorServer(httpServletRequest);
        System.out.println("vendorOrganisations===>" + vendorOrganisations.size());
        List<CustomerOrganisationDto> organisationDetails = new ArrayList<>();
        List<String> orgIds = new ArrayList<>();

        for (CustomerOrganisationDto vendorOrganisation : vendorOrganisations) {
            orgIds.add(vendorOrganisation.getId());
        }
        for (CustomerOrganisationDto customerOrganisationDto : vdmsOrganisations) {
            orgIds.add(customerOrganisationDto.getId());
        }

        System.out.println("size of orgId==>" + orgIds.size());

        ResponseDTO loginOrgDetails = webClientService.getOrganisationDetailsByOrgId(orgIds);

        String usersOrganisation = JSON.toJSONString(loginOrgDetails.getData());
        List<CustomerOrganisationDto> userOrganisationDetails = JSON.parseArray(usersOrganisation, CustomerOrganisationDto.class);
        organisationDetails.addAll(userOrganisationDetails);


        ResponseDTO usersWithOrgId = webClientService.getAllUserDetailsByOrgId(orgIds);

        List<UserDTO> userDTOS = new ArrayList<>();
        String users = JSON.toJSONString(usersWithOrgId.getData());
        List<UserDTO> usersInLoginServerWithOrgId = JSON.parseArray(users, UserDTO.class);
        System.out.println("usersInLoginServerWithOrgId:=" + usersInLoginServerWithOrgId.size());
        List<UserDTO> usersInVdmsServerWithOrgId = userRepository.getAllUserInfoByOrgIds(orgIds);
        System.out.println("usersInVdmsServerWithOrgId:==" + usersInVdmsServerWithOrgId.size());
        List<UserDTO> vendorUserDetails = webClientService.getAllVendorUserDetailsByOrgId(orgIds);
        System.out.println("size of vendor==>" + vendorUserDetails.size());

        for (UserDTO userInLoginServer : usersInLoginServerWithOrgId) {
            for (UserDTO vendorUser : vendorUserDetails) {
                if (vendorUser.getEmail().equals(userInLoginServer.getEmail())) {

                    vendorUser.setPassword(userInLoginServer.getPassword());
                    vendorUser.setBlock(userInLoginServer.getBlock());
                    vendorUser.setActive(userInLoginServer.getActive());
                    vendorUser.setTerms_and_conditions(userInLoginServer.getTerms_and_conditions());
                    vendorUser.setInventory_visibility(userInLoginServer.getInventory_visibility());

                    log.info("vendor language :{}", vendorUser.getLanguage());
//                    vendorUser.setLanguage(userInLoginServer.getLanguage());
                    vendorUser.setTheme_mode(userInLoginServer.getTheme_mode());
                    vendorUser.setTheme_color(userInLoginServer.getTheme_color());
                    userDTOS.add(vendorUser);
                    break;
                }
            }
        }

        for (UserDTO userInLoginServer : usersInLoginServerWithOrgId) {
            for (UserDTO userInVdmsServer : usersInVdmsServerWithOrgId) {
                if (userInVdmsServer.getEmail().equals(userInLoginServer.getEmail())) {
                    userInVdmsServer.setPassword(userInLoginServer.getPassword());
                    userInVdmsServer.setBlock(userInLoginServer.getBlock());
                    userInVdmsServer.setActive(userInLoginServer.getActive());
                    userInVdmsServer.setTerms_and_conditions(userInLoginServer.getTerms_and_conditions());

                    userInVdmsServer.setApplication_language(userInLoginServer.getLanguage());
//                    userInVdmsServer.setLanguage(userInLoginServer.getLanguage());
                    userInVdmsServer.setUser_profile_id(userInLoginServer.getUser_profile_id());
                    userInVdmsServer.setInventory_visibility(userInLoginServer.getInventory_visibility());
                    userInVdmsServer.setTheme_mode(userInLoginServer.getTheme_mode());
                    userInVdmsServer.setTheme_color(userInLoginServer.getTheme_color());
                    userDTOS.add(userInVdmsServer);
                    break;
                }
            }
        }

        ResponseDTO usersWithoutOrgId = webClientService.getAllUserDetails(httpServletRequest);

        String users2 = JSON.toJSONString(usersWithoutOrgId.getData());

        List<UserDTO> usersInLoginServerWithoutOrgId = JSON.parseArray(users2, UserDTO.class);
        List<UserDTO> usersInVdmsServerWithOutOrgId = userRepository.getAllUserInfoWithoutOrg();

        for (UserDTO userInLoginServer : usersInLoginServerWithoutOrgId) {
            for (UserDTO userInVdmsServer : usersInVdmsServerWithOutOrgId) {
                if (userInVdmsServer.getEmail().equals(userInLoginServer.getEmail())) {
                    userInVdmsServer.setPassword(userInLoginServer.getPassword());
                    userInVdmsServer.setBlock(userInLoginServer.getBlock());
                    userInVdmsServer.setUser_profile_id(userInLoginServer.getUser_profile_id());
                    userInVdmsServer.setActive(userInLoginServer.getActive());
                    userInVdmsServer.setTerms_and_conditions(userInLoginServer.getTerms_and_conditions());
                    userInVdmsServer.setApplication_language(userInLoginServer.getLanguage());
                    userInVdmsServer.setTheme_mode(userInLoginServer.getTheme_mode());
                    userInVdmsServer.setTheme_color(userInLoginServer.getTheme_color());
                    userInVdmsServer.setInventory_visibility(userInLoginServer.getInventory_visibility());
                    userDTOS.add(userInVdmsServer);
                    break;
                }
            }
        }

        List<UserProfileDTO> userProfileDTOS = webClientService.getUserProfileDetailsByOrgId(orgIds);

        JSONObject result = new JSONObject();
        System.out.println("user size ===>" + userDTOS.size());
        result.put("organisations", organisationDetails);
        System.out.println("organisations size ===>" + organisationDetails.size());
        result.put("users", userDTOS);
        result.put("userProfiles", userProfileDTOS);
        System.out.println("userProfiles size ===>" + userProfileDTOS.size());


        ResponseDTO responseDTO = ScleraUtils.generatePayload(result, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);

    }

    public ResponseEntity<?> getAllUserCount(String email, String key, String loggedInUser, List<String> userProfiles, boolean withoutProfile, String orgId, String active, HttpServletRequest httpServletRequest) {
        log.info("Payload:Key:{},loggedInUser:{},orgId:{},active:{},withoutProfile:{}", key, loggedInUser, orgId, active, withoutProfile);
        JSONObject jsonObject = webClientService.getAllUserCount(email, key, loggedInUser, userProfiles, orgId, active, withoutProfile, httpServletRequest);
        JSONObject userCount = new JSONObject();
        userCount.put("count", jsonObject.getString("data"));
        ResponseDTO responseDTO = ScleraUtils.generatePayload(userCount, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public UserDTO getMasterUserInfoByVdmsId(String vdmsid, HttpServletRequest httpServletRequest) {
        log.info("Fetching Master User Info By Vdms_Id:{},EndPoint:{}", vdmsid, httpServletRequest.getRequestURI());
        String email = userRepository.getMasterUserEmailByVdmsId(vdmsid);
        UserDTO loginUserDTO = webClientService.getUserDetailsByEmailFromLoginDB(email, httpServletRequest);
        return loginUserDTO;
    }

    public ResponseEntity<?> deleteExternalClientUser(String email, HttpServletRequest httpServletRequest) {
        log.info("Payload:email:{}", email);
        userRepository.deleteUserByEmail(email);
        addressService.deleteAddressById(email, httpServletRequest);
        vdmsvisibilityService.deleteVisibleVdmsByEmail(email);
        updateVdmsSyncByOrganisationIdV2(email, httpServletRequest);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
        userActionLogService.addUserActionLog(email, "User", "DELETE", "A User With Email:" + email + " Is Deleted ", "success");
        log.info("Delete User By Master_User.EndPoint:{}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> addExternalClientUser(String orgId, ExternalClientUserInfoDTO userDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserDTO:{}", userDTO);
        if (orgId != null) {
            BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
            userDTO.setCreationTimeStamp(creation_time);
            int userCount = userRepository.checkUser(userDTO.getEmail());
            if (userCount == 0) {

                userRepository.createNewUserByEmail(userDTO.getAttributes().getUserId(), userDTO.getEmail(), userDTO.getAttributes().getRoleName(), orgId, userDTO.getCreationTimeStamp());

                String customerOrgId = userRepository.getCustomerOrganisationIdByUserEmail(userDTO.getEmail());
                List<VdmsDTO> vdmsDTOS = vdmsService.getAllVdmsInfoByOrganisationId(customerOrgId);
                updateVdmsSyncByOrganisationIdV2(vdmsDTOS,userDTO.getEmail(), httpServletRequest);

                Set<String> stringSet = new HashSet<>(userDTO.getAttributes().getVdmsIds());
                if (!userDTO.getAttributes().getRoleName().equals("org-admin")) {
                    vdmsvisibilityService.addExternalClientVdmsVisibility(stringSet, userDTO.getEmail(), orgId, httpServletRequest);
                }
                userActionLogService.addUserActionLog(userDTO.getEmail(), "User", "ADD", "A User With Email:" + userDTO.getEmail() + " Is Created ", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("successfully added user with email " + userDTO.getEmail(), 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.info("else part is executed .....");
                userRepository.updateRoleByEmail(userDTO.getAttributes().getRoleName(), userDTO.getEmail());

                String customerOrgId = userRepository.getCustomerOrganisationIdByUserEmail(userDTO.getEmail());
                List<VdmsDTO> vdmsDTOS = vdmsService.getAllVdmsInfoByOrganisationId(customerOrgId);
                updateVdmsSyncByOrganisationIdV2(vdmsDTOS,userDTO.getEmail(), httpServletRequest);

                Set<String> stringSet = new HashSet<>();
                if (userDTO.getAttributes().getVdmsIds() != null) {
                    stringSet = new HashSet<>(userDTO.getAttributes().getVdmsIds());
                }
                if (userDTO.getAttributes().getRoleName().equals("org-admin")) {
                    vdmsvisibilityService.deleteVisibleVdmsByEmail(userDTO.getEmail());
                } else {
                    vdmsvisibilityService.addExternalClientVdmsVisibility(stringSet, userDTO.getEmail(), orgId, httpServletRequest);
                }
                userActionLogService.addUserActionLog(userDTO.getEmail(), "User", "ADD", "A User With Email:" + userDTO.getEmail() + " Is updated ", "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("successfully updated user with email " + userDTO.getEmail(), 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteUserProfileByEmail(String email, HttpServletRequest httpServletRequest) {
        String image_url = userRepository.getImageUrlByEmail(email);
        if (image_url != null) {
            String fileName = getFileNameByImageUrl(image_url, httpServletRequest);
            log.info("Delete User Profile By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
            awsService.removeFileFromAWSS3(resourceUrlConfig.getUserImageDirectory(), fileName, httpServletRequest);
        }
    }

    private String getFileNameByImageUrl(String image_url, HttpServletRequest httpServletRequest) {
        log.info("Fetching File Name By Image URL:{},EndPoint:{}", image_url, httpServletRequest.getRequestURI());
        return image_url.substring(image_url.lastIndexOf("/") + 1);
    }


    public UserDTO getMasterUserInfoByOrganisationId(String customer_org_id, HttpServletRequest httpServletRequest) {
        log.info("Fetching Master User Info By Org_Id:{},EndPoint:{}", customer_org_id, httpServletRequest.getRequestURI());
        String email = userRepository.getMasterUserEmailByOrganisationId(customer_org_id);
        UserDTO response = webClientService.getUserDetailsByEmailFromLoginDB(email, httpServletRequest);
        return response;
    }

    public String getOrganisationIdByEmail(String email, HttpServletRequest httpServletRequest) {
        log.info("Fetching Organisation_Id By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        return userRepository.getCustomerOrganisationIdByUserEmail(email);
    }

    public String getMasterUserEmailsByUserOrganisationId(String customerOrgId) {
        return userRepository.getMasterUserEmailsByUserOrganisationId(customerOrgId);
    }

    public Set<String> getUserEmailsByUserOrganisationId(String organisation_id, String email, HttpServletRequest httpServletRequest) {
        log.info("Fetching User Email By User_Org_Id:{},EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
        return userRepository.getUserEmailsByOrganisationId(organisation_id, email);
    }

    public ResponseEntity<?> migrateCreationTimeStamp(HttpServletRequest httpServletRequest) {
        List<UserDTO> userDTOS = userRepository.getCreationTimeStamps();
        System.out.println("userDTO size " + userDTOS.size());
        ResponseDTO responseDTO = webClientService.migrateCreationTimeStamp(userDTOS, httpServletRequest);
        if (responseDTO.getStatus() == 200) {
            ResponseDTO response = ScleraUtils.generatePayload("successfully migrated creation timestamp", 200, false);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            throw new ServerException("Something went wrong", 800, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteEnterpriseAccountByOrgId(String orgId, String loggedInUser, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        if (orgId != null) {
            String email = userRepository.getMasterUserEmailByOrganisationId(orgId);
            String org_id = userRepository.getCustomerOrganisationIdByUserEmail(email);
            log.info("org_id:" + org_id);
            Integer is_activated = vdmsService.getActivationStatus(org_id, httpServletRequest);
            log.info("is_activated:" + is_activated);

            if (is_activated == 0) {
                UserDTO userDTO = UserDTO.builder()
                        .email(email)
                        .build();
                deleteMasterUserByEmail(email, loggedInUser, userDTO, httpServletRequest);
            } else {
                log.error("Can Not Delete,Vdms Activated.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Can Not Delete,Vdms Activated", 866, httpServletRequest.getRequestURI());
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            userActionLogService.addUserActionLog(null, "Vdms", "DELETE", "A  Master User With Email:" + email + " Is Deleted.", "success");
            log.info("A Master User With Email:" + email + "Is Deleted.EndPoint:{}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(null, "Vdms", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> checkerUserLocationWithinBoundaryByCoordinatesAndVdmsId(String orgId, String email, JSONObject payload, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Received request to check user location within boundary for orgId: {}, email: {}, loggedInUser: {}", orgId, email, loggedInUser);
        if (payload != null &&
                payload.containsKey("vdms_id") && payload.get("vdms_id") != null &&
                payload.containsKey("longitude") && payload.get("longitude") != null &&
                payload.containsKey("latitude") && payload.get("latitude") != null) {

            log.info("Payload received with vdms_id: {}, longitude: {}, latitude: {}",
                    payload.get("vdms_id"), payload.get("longitude"), payload.get("latitude"));

            Long result = vdmsService.checkerUserLocationWithinBoundaryByCoordinatesAndVdmsId(payload);

            log.info("Boundary check result: {}", result);

            log.info("Generated response for URI: {}", httpServletRequest.getRequestURI());
            ResponseDTO responseDTO = ScleraUtils.generatePayload(result, 200, false);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public String getMasterUserEmailByVdmsId(String vdmsid, HttpServletRequest httpServletRequest) {
        log.info("Fetching Master User Info By Vdms_Id:{},EndPoint:{}", vdmsid, httpServletRequest.getRequestURI());
        return userRepository.getMasterUserEmailByVdmsId(vdmsid);
    }

    public void updateVdmsSyncByOrganisationIdV2(String email, HttpServletRequest httpServletRequest) {
        String token =httpServletRequest.getHeader("Authorization");
        String customerOrgId = userRepository.getCustomerOrganisationIdByUserEmail(email);
        List<VdmsDTO> vdmsDTOS = vdmsService.getAllVdmsInfoByOrganisationId(customerOrgId);
        executorService.submit(() -> {
            try {
                if (vdmsDTOS != null && !vdmsDTOS.isEmpty()) {
                    VdmsSyncDTO vdmsSyncDTO = new VdmsSyncDTO();
                    vdmsSyncDTO.setUser_sync(1);
                    log.info("Update Vdms Sync By Org_Id:{}", customerOrgId);
                    vdmsDTOS.forEach(vdms -> vdmsService.updateVdmsSyncByVdmsIdV2(vdmsSyncDTO, vdms, token));
                }
            } catch (Exception e) {
                log.error("Error while updating VDMS sync for email {}", email, e);
            }
        });
    }

    public void updateVdmsSyncByOrganisationIdV2(List<VdmsDTO> vdmsDTOS, String email, HttpServletRequest httpServletRequest) {
        final String token = httpServletRequest.getHeader("Authorization");
        executorService.submit(() -> {
            try {
                VdmsSyncDTO vdmsSyncDTO = new VdmsSyncDTO();
                vdmsSyncDTO.setUser_sync(1);
                log.info("Starting VDMS sync for {} VDMS records", vdmsDTOS.size());
                for (VdmsDTO vdms : vdmsDTOS) {
                    try {
                        vdmsService.updateVdmsSyncByVdmsIdV2(vdmsSyncDTO, vdms, token);
                    } catch (Exception ex) {
                        log.error("Failed to sync VDMS {}", vdms.getVdms_id(), ex);
                    }
                }
                log.info("Completed VDMS sync for {} VDMS records", vdmsDTOS.size());
            } catch (Exception e) {
                log.error("Error while updating VDMS sync for email {}", email, e);
            }
        });
    }
}
