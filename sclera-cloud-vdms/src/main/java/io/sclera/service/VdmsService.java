package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.uuid.Generators;
import io.sclera.component.TokenVerifier;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.exception.ServerException;
import io.sclera.repository.UserRepository;
import io.sclera.repository.VdmsRepository;
import io.sclera.repository.VdmsVisibilityRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import io.sclera.util.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class VdmsService {

    @Autowired
    private VdmsRepository vdmsRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private UserService userService;

    @Autowired
    private TokenVerifier tokenVerifier;

    @Autowired
    private AddressService addressService;

    @Autowired
    private DockerService dockerService;

    @Autowired
    private VdmsVisibilityService vdmsvisibilityService;

    @Autowired
    private VdmsIntegrationService vdmsIntegrationService;

    @Autowired
    private VdmsProfileService vdmsprofileService;
    @Autowired
    private NfcService nfcService;
    @Autowired
    private P2PRemoteSessionService p2premotesessionService;

    @Autowired
    private ScleraFXService scleraFXService;

    @Autowired
    private ProxyProfileService proxyProfileService;

    @Autowired
    private SocketUtils socketUtils;

    @Autowired
    private AwsService awsService;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;
    @Autowired
    private DigitalTwinTemplateService digitalTwinTemplateService;


    @Autowired
    private WebClientService webClientService;

    @Autowired
    private CustomerOrganisationService customerOrganisationService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QrCodeService qrCodeService;

    @Autowired
    private VdmsVisibilityRepository vdmsVisibilityRepository;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private VdmsFeatureService vdmsFeatureService;

    @Autowired
    private ClientQrCodeService clientQrCodeService;

    @Autowired
    private ClientNfcService clientNfcService;
    @Autowired
    private VdmsOnboardingSummaryService vdmsOnboardingSummaryService;

    @Autowired
    private VdmsCoordinatesService vdmsCoordinatesService;

    @Autowired
    private ClientBarCodeService clientBarCodeService;


    public ResponseEntity<?> getVdmsInfoByVdmsId(String email, String vdms_id, String loggedInUser, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        log.info("Payload:Email:{},Vdms_Id:{},loggedInUser:{}", email, vdms_id, loggedInUser);
        if (email != null && vdms_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                String isVdms = vdmsRepository.checkVdmsId(vdms_id);
                if (isVdms != null) {
                    UserDTO loginResponse = webClientService.getRoleAndOrganisationIdByVendorEmail(email, loggedInUser, httpServletRequest);
                    log.info("loginResponse:{}", loginResponse);
                    String dbEmail;
                    if (loginResponse.getRole().equals("master-user") || loginResponse.getRole().equals("org-admin")) {
                        dbEmail = vdmsRepository.checkVdmsIdByEmail(email, vdms_id);
                        if (dbEmail != null) {
                            VdmsDTO vdmsdto = this.getVdmsInfoByVdmsId(vdms_id, httpServletRequest);
                            if (Boolean.FALSE.equals(vdmsdto.getTrialStatus())) {
                                setNetworkCountAndStatusForUsers(vdmsdto, httpServletRequest);
                            } else {
                                vdmsdto.setTrialDaysRemaining(calculateRemainingTrialDays(vdmsdto.getTrialEndDate()));
                                this.checkAndSetupTrial(vdmsdto, httpServletRequest);
                            }
                            String permission = webClientService.getAgentPermissionsByVdmsId(vdms_id, httpServletRequest);

                            if (permission != null) {
                                vdmsdto.setPermissions(permission);
                            }
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsdto, 200, true);
                            log.info("Fetching Vdms Info By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            log.error("Error! Permission denied for accessing VDMS.EndPoint:{}", httpServletRequest.getRequestURI());
                            throw new ClientException("Error! Permission denied for accessing VDMS", 748, httpServletRequest.getRequestURI());
                        }
                    } else if (loginResponse.getRole().equals("master-vendor")) {
                        String inviteeOrgId = dockerService.getInviteeOrgId(vdms_id, loginResponse.getOrganisation_id(), httpServletRequest);
                        if (inviteeOrgId != null) {
                            Integer inviteeNetworkCount = dockerService.getNetworkCountByVdmsIdAndInviteeOrgId(vdms_id, loginResponse.getOrganisation_id(), httpServletRequest);
                            if (inviteeNetworkCount > 0) {
                                VdmsDTO vdmsdto = this.getVdmsInfoByVdmsId(vdms_id, httpServletRequest);
                                if (Boolean.FALSE.equals(vdmsdto.getTrialStatus())) {
                                    setNetworkCountAndStatusForUsers(vdmsdto, httpServletRequest);
                                } else {
                                    vdmsdto.setTrialDaysRemaining(calculateRemainingTrialDays(vdmsdto.getTrialEndDate()));
                                    this.checkAndSetupTrial(vdmsdto, httpServletRequest);
                                }
                                String permission = webClientService.getAgentPermissionsByVdmsId(vdms_id, httpServletRequest);

                                if (permission != null) {
                                    vdmsdto.setPermissions(permission);
                                }
                                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsdto, 200, true);
                                log.info("Fetching Vdms Info By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
                                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                            } else {
                                log.error("Error! Permission denied for accessing VDMS.EndPoint:{}", httpServletRequest.getRequestURI());
                                throw new ClientException("Error! Permission denied for accessing VDMS", 748, httpServletRequest.getRequestURI());
                            }
                        } else {
                            Integer networkCount = dockerService.getNetworkCountByVdmsIdAndVendorOrganisationId(vdms_id, loginResponse.getOrganisation_id(), httpServletRequest);
                            if (networkCount > 0) {
                                VdmsDTO vdmsdto = this.getVdmsInfoByVdmsId(vdms_id, httpServletRequest);
                                if (Boolean.FALSE.equals(vdmsdto.getTrialStatus())) {
                                    setNetworkCountAndStatusForUsers(vdmsdto, httpServletRequest);
                                } else {
                                    vdmsdto.setTrialDaysRemaining(calculateRemainingTrialDays(vdmsdto.getTrialEndDate()));
                                    this.checkAndSetupTrial(vdmsdto, httpServletRequest);
                                }
                                String permission = webClientService.getAgentPermissionsByVdmsId(vdms_id, httpServletRequest);

                                if (permission != null) {
                                    vdmsdto.setPermissions(permission);
                                }
                                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsdto, 200, true);
                                log.info("Fetching Vdms Info By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
                                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                            } else {
                                log.error("Error! Permission denied for accessing VDMS.EndPoint:{}", httpServletRequest.getRequestURI());
                                throw new ClientException("Error! Permission denied for accessing VDMS", 748, httpServletRequest.getRequestURI());
                            }
                        }
                    } else if (loginResponse.getRole().equals("user") || loginResponse.getRole().equals("vendor") || loginResponse.getRole().equals("property-admin")) {
                        dbEmail = vdmsvisibilityService.checkVisibleVdmsByEmailAndVdmsId(email, vdms_id, httpServletRequest);
                        if (dbEmail != null) {
                            VdmsDTO vdmsdto = this.getVdmsInfoByVdmsId(vdms_id, httpServletRequest);
                            if (Boolean.FALSE.equals(vdmsdto.getTrialStatus())) {
                                setNetworkCountAndStatusForUsers(vdmsdto, httpServletRequest);
                            } else {
                                vdmsdto.setTrialDaysRemaining(calculateRemainingTrialDays(vdmsdto.getTrialEndDate()));
                                this.checkAndSetupTrial(vdmsdto, httpServletRequest);
                            }
                            String permission = webClientService.getAgentPermissionsByVdmsId(vdms_id, httpServletRequest);

                            if (permission != null) {
                                vdmsdto.setPermissions(permission);
                            }
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsdto, 200, true);
                            log.info("Fetching Vdms Info By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            log.error("Error! Permission denied for accessing VDMS.EndPoint:{}", httpServletRequest.getRequestURI());
                            throw new ClientException("Error! Permission denied for accessing VDMS", 748, httpServletRequest.getRequestURI());
                        }
                    } else {
                        log.error("Error! Permission denied for accessing VDMS.EndPoint:{}", httpServletRequest.getRequestURI());
                        throw new ClientException("Error! Permission denied for accessing VDMS", 748, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("VdmsId: {} does not exist.EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
                    throw new ClientException("VdmsId does not exist", 728, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public VdmsDTO getVdmsInfoByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:Vdms_Id:{}", vdms_id);
        VdmsDTO vdmsdto = vdmsRepository.getVdmsInfoByVdmsId(vdms_id);
        setNetworkCountAndStatusForUsers(vdmsdto, httpServletRequest);
        if (vdmsdto.getTrialStatus()) {
            vdmsdto.setTrialDaysRemaining(calculateRemainingTrialDays(vdmsdto.getTrialEndDate()));
        }
        log.info("Fetching Vdms Info By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return setProxyProfileForUsers(vdmsdto, httpServletRequest);
    }

    public ResponseEntity<?> getAllVdmsInfoByUserEmail(String user_email, String key, String sort, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:User_Email:{},Key:{}, Sort: {},Page_No:{},Page_Size:{},loggedInUser:{}", user_email, key, sort, pageNo, pageSize, loggedInUser);
        if (user_email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "property-admin");
            if (access) {
                String organisation_id = userService.getOrganisationIdByEmail(user_email, httpServletRequest);
                String role = userService.getRoleNameByUserEmail(user_email, httpServletRequest);
                int offset = ScleraUtils.calculateOffset(pageNo, pageSize);
                if (role.equalsIgnoreCase("master-user") || role.equalsIgnoreCase("org-admin")) {
                    log.info("Fetching All Vdms Info By User_Email:{},EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                    Set<VdmsDTO> vdmsDTOS = vdmsRepository.getAllVdmsInfoByMasterUserOrganisationId(organisation_id, key, sort, pageSize, offset);
                    if (vdmsDTOS != null) {
                        for (VdmsDTO vdms : vdmsDTOS) {
                            log.info("VDMSID:" + vdms.getVdms_id());
                            if (Boolean.FALSE.equals(vdms.getTrialStatus())) {
                                setNetworkCountAndStatusForUsers(vdms, httpServletRequest);
                                setProxyProfileForUsers(vdms, httpServletRequest);
                            } else {
                                vdms.setTrialDaysRemaining(calculateRemainingTrialDays(vdms.getTrialEndDate()));
                                this.checkAndSetupTrial(vdms, httpServletRequest);
                            }
                        }
                    }
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else if (role.equalsIgnoreCase("user") || role.equalsIgnoreCase("property-admin")) {
                    log.info("Fetching All Vdms Info By User_Email:{},EndPoint:{}", user_email, httpServletRequest.getRequestURI());
                    Integer fullAccess = vdmsvisibilityService.getVdmsFullAccessByEmail(user_email, httpServletRequest);
                    if (fullAccess != null && fullAccess.equals(1)) {
                        Set<VdmsDTO> vdmsDTOS = vdmsRepository.getAllVdmsInfoByMasterUserOrganisationId(organisation_id, key, sort, pageSize, offset);
                        if (vdmsDTOS != null) {
                            for (VdmsDTO vdms : vdmsDTOS) {
                                log.info("VDMSID:" + vdms.getVdms_id());
                                if (Boolean.FALSE.equals(vdms.getTrialStatus())) {
                                    setNetworkCountAndStatusForUsers(vdms, httpServletRequest);
                                    setProxyProfileForUsers(vdms, httpServletRequest);
                                } else {
                                    vdms.setTrialDaysRemaining(calculateRemainingTrialDays(vdms.getTrialEndDate()));
                                    this.checkAndSetupTrial(vdms, httpServletRequest);
                                }
                            }
                        }
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        Set<VdmsDTO> vdmsDTOS = vdmsRepository.getAllVdmsInfoByUserOrganisationIdAndUserEmail(organisation_id, user_email, key, sort, pageSize, offset);
                        if (vdmsDTOS != null) {
                            for (VdmsDTO vdms : vdmsDTOS) {
                                log.info("VDMSID:" + vdms.getVdms_id());
                                if (Boolean.FALSE.equals(vdms.getTrialStatus())) {
                                    setNetworkCountAndStatusForUsers(vdms, httpServletRequest);
                                    setProxyProfileForUsers(vdms, httpServletRequest);
                                } else {
                                    vdms.setTrialDaysRemaining(calculateRemainingTrialDays(vdms.getTrialEndDate()));
                                    this.checkAndSetupTrial(vdms, httpServletRequest);
                                }
                            }
                        }
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } else {
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }


        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getAllVdmsInfoByVendorEmail(String email, String key, String sort, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:User_Email:{},Key:{}, Sort: {},Page_No:{},Page_Size:{},loggedInUser:{}", email, key, sort, pageNo, pageSize, loggedInUser);
        if (email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-vendor", "vendor");
            if (access) {
                UserDTO user = webClientService.getRoleAndOrganisationIdByVendorEmail(email, loggedInUser, httpServletRequest);
                log.info("user:{}", user);
                if (user != null) {
                    int offset = ScleraUtils.calculateOffset(pageNo, pageSize);
                    if (user.getRole().equalsIgnoreCase("master-vendor")) {
                        Set<VdmsDTO> vdmsDTOS = vdmsRepository.getAllVdmsInfoByMasterVendorOrganisationId(user.getOrganisation_id(), key, sort, pageSize, offset);
                        if (vdmsDTOS != null) {
                            for (VdmsDTO vdms : vdmsDTOS) {
                                setNetworkCountAndStatusForVendors(vdms, user.getOrganisation_id(), httpServletRequest);
                                setInviteeNetworksByVdmsIdAndOrganisationId(vdms, user.getOrganisation_id(), httpServletRequest);
                                setProxyProfileForVendors(vdms, httpServletRequest);
                            }
                        }
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else if (user.getRole().equalsIgnoreCase("vendor")) {
                        Integer fullAccess = vdmsvisibilityService.getVdmsFullAccessByEmail(user.getEmail(), httpServletRequest);
                        if (fullAccess != null && fullAccess.equals(1)) {
                            Set<VdmsDTO> vdmsDTOS = vdmsRepository.getAllVdmsInfoByMasterVendorOrganisationId(user.getOrganisation_id(), key, sort, pageSize, offset);
                            if (vdmsDTOS != null) {
                                for (VdmsDTO vdms : vdmsDTOS) {
                                    setNetworkCountAndStatusForVendors(vdms, user.getOrganisation_id(), httpServletRequest);
                                    setInviteeNetworksByVdmsIdAndOrganisationId(vdms, user.getOrganisation_id(), httpServletRequest);
                                    setProxyProfileForVendors(vdms, httpServletRequest);
                                }
                            }
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            Set<VdmsDTO> vdmsDTOS = vdmsRepository.getAllVdmsInfoByVendorOrganisationIdAndVendorEmail(user.getOrganisation_id(), user.getEmail(), key, sort, pageSize, offset);
                            if (vdmsDTOS != null) {
                                for (VdmsDTO vdms : vdmsDTOS) {
                                    setNetworkCountAndStatusForVendors(vdms, user.getOrganisation_id(), httpServletRequest);
                                    setProxyProfileForVendors(vdms, httpServletRequest);
                                }
                            }
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
                            log.info("Fetching All Vdms Info By Vendor Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    } else {
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } else {
                    log.error("User Not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("User Not Found", 701, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }


    public ResponseEntity<?> getAllVdmsInfoByAdminEmail(String email, String key, String sort, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:User_Email:{},Key:{},Sort:{},Page_No:{},Page_Size:{},loggedInUser:{}", email, key, sort, pageNo, pageSize, loggedInUser);
        if (email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
                if (role != null) {
                    int offset = ScleraUtils.calculateOffset(pageNo, pageSize);
                    if (role.equalsIgnoreCase("super-admin") || role.equalsIgnoreCase("admin")) {
                        Set<VdmsDTO> vdmsDTOS = vdmsRepository.getAllVdmsInfo(key, sort, pageSize, offset);
                        if (vdmsDTOS != null) {
                            for (VdmsDTO vdms : vdmsDTOS) {
                                log.info("VDMSID:" + vdms.getVdms_id());
                                if (Boolean.FALSE.equals(vdms.getTrialStatus())) {
                                    setNetworkCountAndStatusForUsers(vdms, httpServletRequest);
                                    setProxyProfileForUsers(vdms, httpServletRequest);
                                } else {
                                    vdms.setTrialDaysRemaining(calculateRemainingTrialDays(vdms.getTrialEndDate()));
                                    this.checkAndSetupTrial(vdms, httpServletRequest);
                                }
                            }
                        }
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
                        log.info("Fetching All vdms Info By Admin_Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                        log.info("Fetching All vdms Info By Admin_Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } else {
                    log.error("Role Not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }


    public ResponseEntity<?> addVdmsByUserOrganisationId(VdmsDTO vdmsdto, String email, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload:Email:{},VdmsDTO:{},loggedInUser:{}", email, vdmsdto, loggedInUser);
        if (vdmsdto != null && email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
                String organisation_id = userService.getOrganisationIdByEmail(email, httpServletRequest);
                if (role.equalsIgnoreCase("master-user") || role.equalsIgnoreCase("org-admin")) {
                    String vdms_id = getRandomVdmsId(httpServletRequest);
                    List<String> db_ids = vdmsRepository.getVdmsIds();
                    String id = checkVdmsId(vdms_id, db_ids, httpServletRequest);
                    BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
                    if (id != null) {

                        String vdmsOneTimePassword = this.generateTimeBasedPasswordForVdms();
                        log.info("Payload:Id:{},vdmsOneTimePassword:{},Email:{}", id, vdmsOneTimePassword, email);
                        webClientService.addVdmsOneTimePassword(id, vdmsOneTimePassword, email, vdmsdto.getDeployment_type(), loggedInUser, httpServletRequest);
                        log.info("Adding Vdms One Time Password in Login");
                        log.info("Payload:{}", vdms_id);
                        webClientService.addVdmsInHealth(vdms_id, loggedInUser, httpServletRequest);
                        log.info("Adding Vdms in Health");
                        if (vdmsdto.getBase64image() != null) {
                            byte[] image = Base64.getDecoder().decode(vdmsdto.getBase64image());
                            String image_url = awsService.addFileToAWSS3(image, resourceUrlConfig.getVdmsImageDirectory(), resourceUrlConfig.getVdmsImageUrl(), vdmsdto.getExtension(), id, httpServletRequest);
                            vdmsdto.setImage_url(image_url);
                        }

                        addressService.addAddress(id, vdmsdto.getAddress(), vdmsdto.getCity(), vdmsdto.getCountry(), vdmsdto.getState(), vdmsdto.getZip(), httpServletRequest);
                        if (vdmsdto.getPermissions() != null) {
                            webClientService.addAgentPermissions(vdms_id, vdmsdto.getPermissions().toString(), httpServletRequest);
                            VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                                    .sclera_agent_permission_sync(1)
                                    .build();
                            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdms_id + "/sync/data", vdmsSyncDTO);
                        }
                        BigInteger trialEndDate = BigInteger.valueOf(calculateTrialEndTime(vdmsdto.getTrialEndDate()));
                        vdmsRepository.addVdmsByUserOrganisationId(id, vdmsdto.getProperty_name(), vdmsdto.getImage_url(), vdmsdto.getLongitude(), vdmsdto.getLatitude()
                                , creation_time, id, organisation_id, vdmsdto.getDeployment_type(),
                                vdmsdto.getTrialStatus(), vdmsdto.getTrialStartDate(), trialEndDate, vdmsdto.getRegion(), vdmsdto.getAwsRegion());
                        vdmsprofileService.tagPrimaryProfileToVdms(id, role, organisation_id, httpServletRequest);
                        if (vdmsdto.getDeployment_type().equals("cloud") && vdmsdto.getIsMultiTenant().equals(1)) {
                            log.info("Cloud Vdms(Multitenancy)");
                            userActionLogService.addUserActionLog(loggedInUser, "VDMS",
                                    "ADD", "A VDMS With Id:" + vdms_id + " is Added(cloud)", "success");
                            sendMultiTenantData(vdms_id, vdmsOneTimePassword, httpServletRequest);
                        } else {
                            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "ADD",
                                    "A VDMS With Id:" + vdms_id + " Is Added(on-premies)", "success");
                        }
                        log.info("Adding Vdms By User_Org_Id:{},EndPoint:{}", id, httpServletRequest.getRequestURI());
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(id, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                        userActionLogService.addUserActionLog(loggedInUser, "VDMS", "ADD", "A VDMS With Id:" + vdms_id + " Already Exist", "failed");
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } else {
                    log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "VDMS", "ADD", "Role Not Authorised", "failed");
                    throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public String generateTimeBasedPasswordForVdms() {

        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 8) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    public Integer deleteVdmsDataByVdmsIdAndSuperAdminEmail(String username, String vdms_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserName:{},vdms_Id:{}, loggedInUser: {}", username, vdms_id, loggedInUser);
        boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
        if (access) {
            String org_id = vdmsRepository.getCustomerOrgIdByVdmsId(vdms_id);
            Integer is_enterprise = customerOrganisationService.getEnterpriseInfoById(org_id);
            if (is_enterprise == 0) {
//                ResponseEntity<String> response = webClientService.deleteSubscriptionByEmailAndVdmsId(username, vdms_id, httpServletRequest);
//                log.info("PAYMENT Server response : {}", response.getBody());
//                if (response.getStatusCodeValue() < 300) {
                deleteVdms(vdms_id, loggedInUser, httpServletRequest);
                return 0;
//                } else {
//                    return 1;
//                }
            } else if (is_enterprise == 1) {
                deleteVdms(vdms_id, loggedInUser, httpServletRequest);
                return 0;
            } else {
                return 1;
            }
        } else {
            log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
        }
    }


    public Integer deleteVdmsByVdmsId(String username, String vdms_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserName:{},vdms_Id:{}", username, vdms_id);
        String org_id = vdmsRepository.getCustomerOrgIdByVdmsId(vdms_id);
        Integer is_enterprise = customerOrganisationService.getEnterpriseInfoById(org_id);
        log.info("is_enterprise:" + is_enterprise);
        if (is_enterprise == 0) {
//            ResponseEntity<String> paymentResponse = webClientService.deleteSubscriptionByEmailAndVdmsId(username, vdms_id, httpServletRequest);
//            log.info("Payment response : {}", paymentResponse.getBody());
//            if (paymentResponse.getStatusCodeValue() < 300) {
            String is_activated = getVdmsActivationStatusByVdmsId(vdms_id, httpServletRequest);
            log.info("is_activated:" + is_activated);
            if (!is_activated.equalsIgnoreCase("Activated")) {
                log.info("Delete Vdms By vdms_Id:{},Endpoint:{}", vdms_id, httpServletRequest.getRequestURI());
                deleteVdms(vdms_id, loggedInUser, httpServletRequest);
                return 0;
            } else {
                log.error("Can Not Delete,Vdms Activated.EndPoint:{}", httpServletRequest.getRequestURI());
                return 1;
            }
//            } else {
//                return 1;
//            }
        } else if (is_enterprise == 1) {
            String is_activated = getVdmsActivationStatusByVdmsId(vdms_id, httpServletRequest);
            if (!is_activated.equalsIgnoreCase("Activated")) {
                log.info("Delete Vdms By vdms_Id:{},Endpoint:{}", vdms_id, httpServletRequest.getRequestURI());
                deleteVdms(vdms_id, loggedInUser, httpServletRequest);
                return 0;
            } else {
                log.error("Can Not Delete,Vdms Activated.EndPoint:{}", httpServletRequest.getRequestURI());
                return 1;
            }
        } else {
            return 1;
        }
    }


    public ResponseEntity<?> deleteNewVdmsByVdmsId(String username, String vdms_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserName:{},vdms_Id:{},loggedInUser:{}", username, vdms_id, loggedInUser);
        if (username != null && vdms_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                deleteVdmsByVdmsId(username, vdms_id, loggedInUser, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "A VDMS With Id:" + vdms_id + " Is Deleted", "success");
                log.info("Delete New Vdms By vdms_Id:{},Endpoint:{}", vdms_id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> editVdmsByVdmsId(VdmsDTO vdmsdto, String username, String vdms_id, String loggedInUser, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload:UserName:{},vdms_Id:{},VdmsDTO:{},loggedInUser:{}", username, vdms_id, vdmsdto, loggedInUser);
        if (vdmsdto != null && username != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                if (vdmsdto.getImage_url() == null) {
                    deleteVdmsImageByVdmsId(vdms_id, httpServletRequest);
                    vdmsdto.setImage_url(null);
                }

                if (vdmsdto.getBase64image() != null) {
                    byte[] image = Base64.getDecoder().decode(vdmsdto.getBase64image());
                    String image_url = awsService.addFileToAWSS3(image, resourceUrlConfig.getVdmsImageDirectory(), resourceUrlConfig.getVdmsImageUrl(), vdmsdto.getExtension(), vdms_id, httpServletRequest);
                    vdmsdto.setImage_url(image_url);
                }
                if (vdmsdto.getPermissions() != null) {
                    VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder()
                            .sclera_agent_permission_sync(1)
                            .build();
                    if (vdmsdto.getIsMultiTenant().equals(1)) {
                        webClientService.multiTenantSyncApiCall(vdms_id, vdmsSyncDTO, vdmsdto.getAwsRegion(), httpServletRequest);
                    } else {
                        socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdms_id + "/sync/data", vdmsSyncDTO);
                    }
                    webClientService.addAgentPermissions(vdms_id, vdmsdto.getPermissions().toString(), httpServletRequest);
                }
                BigInteger trialEndDate = BigInteger.valueOf(calculateTrialEndTime(vdmsdto.getTrialEndDate()));

                addressService.editAddress(vdmsdto.getAddress(), vdmsdto.getCity(), vdmsdto.getState(), vdmsdto.getZip(), vdmsdto.getCountry(), vdms_id, httpServletRequest);
                vdmsRepository.editVdmsByVdmsId(vdmsdto.getProperty_name(), vdmsdto.getImage_url(), vdmsdto.getLongitude(), vdmsdto.getLatitude(), vdmsdto.getDeployment_type(),
                        vdmsdto.getTrialStatus(), vdmsdto.getTrialStartDate(), trialEndDate, vdmsdto.getRegion(), vdmsdto.getAwsRegion(),vdmsdto.getIsMultiTenant(), vdms_id);
                vdmsvisibilityService.editPropertyNameByVdmsId(vdmsdto.getProperty_name(), vdms_id, httpServletRequest);
                if (vdmsdto.getDeployment_type().equals("cloud") &&
                        vdmsdto.getActivation_status().equals("Not activated") && vdmsdto.getIsMultiTenant().equals(1)) {
                    log.info("Cloud Vdms(Multitenancy)");
                    sendUpdatedVdmsMultiTenantData(vdms_id, httpServletRequest);
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "A VDMS WIth Id:" + vdms_id + " Is Updated.", "success");
                log.info("Edit Vdms By vdms_Id:{},Endpoint:{}", vdms_id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    private void deleteVdmsImageByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        String image_url = vdmsRepository.getImageUrlByVdmsId(vdms_id);
        if (image_url != null) {
            String extension = image_url.substring(image_url.lastIndexOf(".") + 1);
            String fileName = getFileNameByImageUrl(image_url, httpServletRequest);
            log.info("Delete Vdms Image  By vdms_Id:{},Endpoint:{}", vdms_id, httpServletRequest.getRequestURI());
            awsService.removeFileFromAWSS3(resourceUrlConfig.getVdmsImageDirectory(), fileName, httpServletRequest);
        }
    }

    public void deleteVdms(String vdms_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId:{},LoggedInUser:{}", vdms_id, loggedInUser);
        webClientService.deleteVdmsTokenInLoginDB(vdms_id, loggedInUser, httpServletRequest);
        log.info("Deleting Vdms Token in Login_DB");
        ResponseDTO vdmsHealth = webClientService.deleteVdmsHealthByVdmsId(vdms_id, loggedInUser, httpServletRequest);
        log.info("Response from Health:{}", vdmsHealth);
        log.info("Deleting Vdms Health By Vdms_Id:{}", vdms_id);
        if (vdmsHealth.getStatus() == 200) {
            dockerService.deleteDockerByVdmsId(vdms_id, httpServletRequest);
            vdmsIntegrationService.deleteVdmsIntegrationByVdmsId(vdms_id, httpServletRequest);
            vdmsvisibilityService.deleteVdmsVisibilityByVdmsId(vdms_id, httpServletRequest);
            vdmsprofileService.deleteVdmsProfileByVdmsId(vdms_id, httpServletRequest);
            nfcService.deleteNfcByVdmsId(vdms_id, httpServletRequest);
            qrCodeService.deleteQrCodeByVdmsId(vdms_id, httpServletRequest);
            clientBarCodeService.deleteClientBarCodeByVdmsId(vdms_id, httpServletRequest);
            clientQrCodeService.deleteClientQrCodeByVdmsId(vdms_id);
            clientNfcService.deleteClientNfcByVdmsId(vdms_id, httpServletRequest);
            p2premotesessionService.deleteP2PRemoteSessionByVdmsId(vdms_id, httpServletRequest);
            vdmsFeatureService.deleteVdmsFeatureByVdmsId(vdms_id, loggedInUser, httpServletRequest);
            deleteVdmsImageByVdmsId(vdms_id, httpServletRequest);
            vdmsRepository.deleteVdmsByVdmsId(vdms_id, httpServletRequest);
            addressService.deleteAddressById(vdms_id, httpServletRequest);
        } else {
            throw new ServerException("Unable to delete VDMS Health", 750, httpServletRequest.getRequestURI());
        }

    }

    public void deleteVdmsByEmail(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        Set<String> vdms_ids = vdmsRepository.getVdmsIdsByEmail(email);
        if (vdms_ids != null) {
            for (String vdms_id : vdms_ids) {
                log.info("Delete Vdms By Email:{},Endpoint:{}", email, httpServletRequest.getRequestURI());
                deleteVdmsByVdmsId(email, vdms_id, loggedInUser, httpServletRequest);
            }
        }
    }

    public Set<QuickSearchDTO> getQuickSearchListByOrganisationId(String email, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        UserDTO user = webClientService.getRoleAndOrganisationIdByVendorEmail(email, loggedInUser, httpServletRequest);
        log.info("user:{}", user);
        if (user.getRole().equalsIgnoreCase("super-admin") || user.getRole().equalsIgnoreCase("admin")) {
            log.info("Fetching Quick Search List By Admin.EndPoint:{}", httpServletRequest.getRequestURI());
            return vdmsRepository.getQuickSearchListByAdmin();
        } else if (user.getRole().equalsIgnoreCase("master-user") || user.getRole().equalsIgnoreCase("org-admin")) {
            log.info("Fetching Quick Search List By Master User Organisation_Id.EndPoint:{}", httpServletRequest.getRequestURI());
            return vdmsRepository.getQuickSearchListByMasterUserOrganisationId(user.getOrganisation_id());
        } else if (user.getRole().equalsIgnoreCase("user") || user.getRole().equals("property-admin")) {
            log.info("Fetching Quick Search List By User_Organisation_Id And Email.EndPoint:{}", httpServletRequest.getRequestURI());
            return vdmsRepository.getQuickSearchListByUserOrganisationIdAndEmail(user.getOrganisation_id(), email);
        } else if (user.getRole().equalsIgnoreCase("master-vendor")) {
            log.info("Fetching Quick Search List By Master_Vendor_Organisation_Id.EndPoint:{}", httpServletRequest.getRequestURI());
            return vdmsRepository.getQuickSearchListByMasterVendorOrganisationId(user.getOrganisation_id());
        } else if (user.getRole().equalsIgnoreCase("vendor")) {
            log.info("Fetching Quick Search List By Vendor_Organisation_Id And Email.EndPoint:{}", httpServletRequest.getRequestURI());
            return vdmsRepository.getQuickSearchListByVendorOrganisationIdAndEmail(user.getOrganisation_id(), email);
        } else if (user.getRole().equalsIgnoreCase("property-admin")) {
            log.info("Fetching Quick Search List By Vendor_Organisation_Id And Email.EndPoint:{}", httpServletRequest.getRequestURI());
            return vdmsRepository.getQuickSearchListByPropertyAdminOrganisationIdAndEmail(user.getOrganisation_id(), email);
        } else {
            return null;
        }
    }

    public ResponseEntity<?> getNewQuickSearchListByOrganisationId(String email, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        if (email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                Set<QuickSearchDTO> data = getQuickSearchListByOrganisationId(email, loggedInUser, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Fetching New Quick Search List By Organisation_Id.Email:{}.EndPoint:{}", email, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> blockVdmsByVdmsId(String username, String vdms_id, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserName:{},vdms_Id:{},loggedInUser:{}", username, vdms_id, loggedInUser);
        if (username != null && vdms_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                BigInteger block_timestamp = BigInteger.valueOf(System.currentTimeMillis());
                vdmsRepository.blockVdmsByVdmsId(vdms_id, block_timestamp);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "A VDMS:" + vdms_id + " Is Blocked", "success");
                log.info("Block Vdms By Vdms_Id:{},UserName:{},EndPoint:{}", vdms_id, username, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> unBlockVdmsByVdmsId(String username, String vdms_id, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserName:{},vdms_Id:{},loggedInUser:{}", username, vdms_id, loggedInUser);
        if (username != null && vdms_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                vdmsRepository.unBlockVdmsByVdmsId(vdms_id);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "A VDMS:" + vdms_id + " Is UnBlocked", "success");
                log.info("UnBlock Vdms By Vdms_Id:{},UserName:{},EndPoint:{}", vdms_id, username, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public VdmsSyncDTO getVdmsSyncByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        VdmsSyncDTO vdmssyncdto = vdmsRepository.getVdmsSyncByVdmsId(vdms_id);
        log.info("########################################");
        log.info("VDMS SYNC DATA");
        log.info(vdms_id);
        log.info("VdmsSyncDTO:{}", vdmssyncdto);
        log.info("########################################");
        Set<DockerSyncDTO> dockers = dockerService.getDockerSyncByVdmsId(vdms_id, httpServletRequest);
        vdmssyncdto.setDockers(dockers);
        log.info("Fetching Vdms Sync By Vdms_id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return vdmssyncdto;
    }

    public String getVdmsActivationStatusByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Fetching Vdms Activation Status By Vdms_id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return vdmsRepository.getVdmsActivationStatusByVdmsId(vdms_id);
    }

    public void updateVdmsSyncByVdmsId(VdmsSyncDTO vdmssyncdto, String vdms_id, HttpServletRequest
            httpServletRequest) {
        log.info("Payload:VdmsSyncDTO:{}", vdmssyncdto);
        log.info("Update Vdms Sync By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        vdmsRepository.updateVdmsSyncByVdmsId(vdmssyncdto.getImage_sync(), vdmssyncdto.getUser_sync(), vdms_id);
        if (vdmssyncdto.getUser_sync() == 1) {
            int isMultiTenant = vdmsRepository.getMultiTenantCheck(vdms_id);
            if (isMultiTenant == 1) {
                String awsRegion = vdmsRepository.getAwsRegionByVdmsId(vdms_id);
                webClientService.multiTenantSyncApiCall(vdms_id, vdmssyncdto, awsRegion, httpServletRequest);
            } else {
                socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdms_id + "/sync/data", vdmssyncdto);
            }
        }
    }

    public VdmsSyncDTO updateVdmsData(String vdms_id, HttpServletRequest httpServletRequest) {
        BigInteger last_seen = BigInteger.valueOf(System.currentTimeMillis());
        String isActivated = vdmsRepository.getVdmsActivationStatusByVdmsId(vdms_id);
        if (isActivated.equals("Activated")) {
            log.info("Update Vdms Data Done Successfully.Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
            vdmsRepository.updateVdmsLastSeenTimeStampByVdmsId(last_seen, vdms_id);
            updaateVdmsEmailAlertByVdmsId(vdms_id, 0);
        }
        return getVdmsSyncByVdmsId(vdms_id, httpServletRequest);
    }

    public String getRandomVdmsId(HttpServletRequest httpServletRequest) {
        log.info("Fetching Random vdms_Id,EndPoint:{}", httpServletRequest.getRequestURI());
        Random r = new Random();
        int low = 100;
        int high = 999;
        int result;
        do {
            result = r.nextInt(high - low) + low;
        } while ((high - low) == 0);
        return "VDMS" + result;
    }

    public String checkVdmsId(String vdms_id, List<String> db_ids, HttpServletRequest httpServletRequest) {
        log.info("Checking Vdms_Id:{},DB_Ids:{},Endpoint:{}", vdms_id, db_ids, httpServletRequest.getRequestURI());
        try {
            if (db_ids.contains(vdms_id)) {
                log.info("VDMS already exists");
                String new_id = getRandomVdmsId(httpServletRequest);
                checkVdmsId(new_id, db_ids, httpServletRequest);
            } else {
                log.info("VDMS not found");
                return vdms_id;
            }
        } catch (StackOverflowError e) {
            log.info("Error");
            log.error("StackOverflowError:{},EndPoint:{}", e.getMessage(), httpServletRequest.getRequestURI());
        }
        return null;
    }

    public void setNetworkCountAndStatusForUsers(VdmsDTO vdmsdto, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO:{}", vdmsdto);
        Integer network_count = dockerService.getNetworkCountByVdmsId(vdmsdto.getVdms_id(), httpServletRequest);
        if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(vdmsdto.getLast_seen().longValueExact()) > 10) {
            log.info("Vdms Is Offline,EndPoint:{}", httpServletRequest.getRequestURI());
            vdmsdto.setTimestamp(vdmsdto.getLast_seen());
            vdmsdto.setVdms_status("0");    // VDMS offline
        } else {
            log.info("Vdms Is Online,EndPoint:{}", httpServletRequest.getRequestURI());
            vdmsdto.setTimestamp(vdmsdto.getFirst_seen());
            vdmsdto.setVdms_status("1");        // VDMS online
        }
        log.info("Set Network Count And Status For Users.EndPoint:{}", httpServletRequest.getRequestURI());
        vdmsdto.setNetwork_count(network_count);
    }

    public VdmsDTO setProxyProfileForUsers(VdmsDTO vdmsdto, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO:{}", vdmsdto);
        if (vdmsdto.getPrimary_proxy_profile_id() != null) {
            ProxyProfileDTO proxyProfileDTO = proxyProfileService.getProxyProfileByProxyProfileId(vdmsdto.getPrimary_proxy_profile_id(), httpServletRequest);
            log.info("Set Proxy Profile For Users.EndPoint:{}", httpServletRequest.getRequestURI());
            vdmsdto.setProxy_profile(proxyProfileDTO);
        } else {
            log.info("No Proxy profile present for vdms.EndPoint:{}", httpServletRequest.getRequestURI());
            log.info("No Proxy profile present for vdms");
        }
        return vdmsdto;
    }

    public void setProxyProfileForVendors(VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO:{}", vdmsDTO);
        String primary_proxy_profile_id = dockerService.getDistinctProxyProfileByVdmsId(vdmsDTO.getVdms_id(), httpServletRequest);
        if (primary_proxy_profile_id != null) {
            ProxyProfileDTO proxyProfileDTO = proxyProfileService.getProxyProfileByProxyProfileId(primary_proxy_profile_id, httpServletRequest);
            log.info("Set Proxy Profile For Vendors.EndPoint:{}", httpServletRequest.getRequestURI());
            vdmsDTO.setProxy_profile(proxyProfileDTO);
        }
    }


    public void setNetworkCountAndStatusForVendors(VdmsDTO vdmsdto, String vendor_org_id, HttpServletRequest
            httpServletRequest) {
        log.info("Payload:vendor_org_id:{},VdmsDTO:{}", vendor_org_id, vdmsdto);
        Integer network_count = dockerService.getNetworkCountByVdmsIdAndVendorOrganisationId(vdmsdto.getVdms_id(), vendor_org_id, httpServletRequest);
        if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(vdmsdto.getLast_seen().longValueExact()) > 10) {
            log.info("Vdms Is Offline,EndPoint:{}", httpServletRequest.getRequestURI());
            vdmsdto.setTimestamp(vdmsdto.getLast_seen());
            vdmsdto.setVdms_status("0");    // VDMS offline
        } else {
            log.info("Vdms Is Online,EndPoint:{}", httpServletRequest.getRequestURI());
            vdmsdto.setTimestamp(vdmsdto.getFirst_seen());
            vdmsdto.setVdms_status("1");        // VDMS online
        }
        log.info("Set Network Count And Status For Vendors.EndPoint:{}", httpServletRequest.getRequestURI());
        vdmsdto.setNetwork_count(network_count);
    }

    public void setInviteeNetworksByVdmsIdAndOrganisationId(VdmsDTO vdmsdto, String
            vendor_org_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:vendor_org_id:{},VdmsDTO:{}", vendor_org_id, vdmsdto);
        Set<DockerDTO> dockers = dockerService.getAllDockersByInviteeOrganisationIdAndVdmsId(vendor_org_id, vdmsdto.getVdms_id(), httpServletRequest);
        log.info("Set Invite Network By vdms Id:{}, And Org_Id:{},EndPoint:{}", vdmsdto.getVdms_id(), vendor_org_id, httpServletRequest.getRequestURI());
        vdmsdto.setDockers(dockers);
    }

    public ResponseEntity<?> updateSubscriptionByVdmsId(VdmsDTO vdmsdto, String loggedInUser, HttpServletRequest
            httpServletRequest) {
        log.info("Payload:VdmsDTO:{},loggedInUser:{}", vdmsdto, loggedInUser);
        if (vdmsdto != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                vdmsRepository.updateSubscriptionByVdmsId(vdmsdto.getEnd_date(), vdmsdto.getStatus(), vdmsdto.getPlan(), vdmsdto.getVdms_id());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "A VDMS:" + vdmsdto.getVdms_id() + " Subscription Is Updated", "success");
                log.info("Update Subscription By Vdms_Id:{},EndPoint:{}", vdmsdto.getVdms_id(), httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public Set<QuickSearchDTO> getVisibleVdmsByVendorOrganisationIdAndEmail(String organisation_id, String
            email, HttpServletRequest httpServletRequest) {
        log.info("Fetching Visible Vdms By Vendor_Organisation_Id:{} And Email:{},EndPoint:{}", organisation_id, email, httpServletRequest.getRequestURI());
        return vdmsRepository.getQuickSearchListByMasterVendorOrganisationId(organisation_id);
    }

    public Set<String> getVisibleVdmsIdsByVendorOrganisationId(String organisation_id, HttpServletRequest
            httpServletRequest) {
        log.info("Fetching Visible Vdms Id By Vendor_Organisation_Id:{} EndPoint:{}", organisation_id, httpServletRequest.getRequestURI());
        return vdmsRepository.getVisibleVdmsIdsByVendorOrganisationId(organisation_id);
    }

    public Set<String> getVdmsIdsByCustomerOrganisationId(String email, HttpServletRequest httpServletRequest) {
        log.info("Fetching vdms_id By Customer_Organisation_Id,Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        return vdmsRepository.getVdmsIdsByCustomerOrganisationId(email);
    }

    public String getPropertyNameByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Fetching Property Name By Vdms-Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return vdmsRepository.getPropertyNameByVdmsId(vdms_id);
    }
    public String getPropertyNameByVdmsId(String vdms_id ) {
        return vdmsRepository.getPropertyNameByVdmsId(vdms_id);
    }

    public ResponseEntity<?> saveBackupFile(MultipartFile file, String vdms_id, HttpServletRequest
            httpServletRequest) {
        if (vdms_id != null) {
            if (file != null && !file.isEmpty()) {
                log.info(file.getOriginalFilename());

                String file_name = Objects.requireNonNull(file.getOriginalFilename()).substring(0, file.getOriginalFilename().lastIndexOf("."));

                awsService.removeFileFromAWSS3(resourceUrlConfig.getVdmsBackupDirectory(), file_name + ".zip", httpServletRequest);
                try {
                    awsService.addFileWithoutTimestampToAWSS3(file.getBytes(), resourceUrlConfig.getVdmsBackupDirectory(), resourceUrlConfig.getVdmsBackupUrl(), "zip", file_name, httpServletRequest);
                    BigInteger time = BigInteger.valueOf(System.currentTimeMillis());
                    vdmsRepository.changeLastUpdatedTimeByVdmsId(time, vdms_id);
                } catch (IOException e) {
                    log.error("StackTrace:{},EndPoint:{}", e.getMessage(), httpServletRequest.getRequestURI());
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(vdms_id, "VDMS", "ADD", "A VDMS:" + vdms_id + " BackUp Is Added", "success");
                log.info("Save BackUp File,Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("File Not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdms_id, "VDMS", "ADD", "A VDMS:" + vdms_id + " BackUp File not Found", "failed");
                throw new ClientException(" File Not Found  ", 722, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdms_id, "VDMS", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getBackupFileByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        if (vdms_id != null) {
            String fileName = "sclera-" + vdms_id + "-backup.zip";
            boolean fileExists = awsService.checkFileExist(fileName, httpServletRequest);
            ResponseDTO responseDTO;
            if (fileExists) {
                responseDTO = ScleraUtils.generatePayload(resourceUrlConfig.getVdmsBackupUrl() + fileName, 200, true);
            } else {
                responseDTO = ScleraUtils.generatePayload(null, 200, true);
            }
            log.info("Fetching BackUp File By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> tagPrimaryProxyProfileToVdmsByProxyProfileId(String email, String vdms_id, String
            proxy_profile_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Email:{},vdms_Id:{},loggedInUser:{}", email, vdms_id, loggedInUser);
        if (email != null && vdms_id != null && proxy_profile_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                vdmsRepository.tagPrimaryProxyProfileToVdmsByProxyProfileId(proxy_profile_id, vdms_id);
                updateProxyClientSyncByVdmsId(1, vdms_id, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "A Primary Proxy Profile Id:" + proxy_profile_id + " Is Tagged to VDMS:" + vdms_id, "success");
                log.info("Tag Primary Proxy Profile To Vdms By Proxy_Profile_Id:{},EndPoint:{}", proxy_profile_id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> untagPrimaryProxyProfileToVdmsByVdmsId(String email, String vdms_id, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Email:{},vdms_Id:{},loggedInUser:{}", email, vdms_id, loggedInUser);
        if (email != null && vdms_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                vdmsRepository.untagPrimaryProxyProfileToVdmsByVdmsId(vdms_id);
                updateProxyClientSyncByVdmsId(1, vdms_id, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "A Primary Proxy Profile Is UnTagged to VDMS:" + vdms_id, "success");
                log.info("UnTag Primary Proxy Profile To Vdms By vdms_id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public void untagAllProxyProfilesFromVdmsByProxyProfileId(String id, HttpServletRequest httpServletRequest) {
        updateProxyClientSyncByProxyProfileId(id, httpServletRequest);
        log.info("UnTag All Proxy Profile From Vdms By Proxy_Profile_Id:{},EndPoint:{}", id, httpServletRequest.getRequestURI());
        vdmsRepository.untagAllProxyProfilesFromVdmsByProxyProfileId(id);
    }


    public ResponseEntity<?> getProxyProfilesTaggedToVdmsIdByOrganisationId(String customer_org_id, String
            email, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (customer_org_id != null && email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                Set<VdmsDTO> vdmsDTOS = vdmsRepository.getProxyProfilesTaggedToVdmsIdByOrganisationId(customer_org_id);
                if (vdmsDTOS != null && vdmsDTOS.size() > 0) {
                    for (VdmsDTO vdmsDTO : vdmsDTOS) {
                        if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(vdmsDTO.getLast_seen().longValueExact()) > 10) {
                            log.info("Vdms Is Offline,EndPoint:{}", httpServletRequest.getRequestURI());
                            vdmsDTO.setTimestamp(vdmsDTO.getLast_seen());
                            vdmsDTO.setVdms_status("0");    // VDMS offline
                        } else {
                            log.info("Vdms Is Online,EndPoint:{}", httpServletRequest.getRequestURI());
                            vdmsDTO.setTimestamp(vdmsDTO.getFirst_seen());
                            vdmsDTO.setVdms_status("1");        // VDMS online
                        }

                        Set<String> network_names = dockerService.getProxyProfileTaggedDockersByVdmsId(vdmsDTO.getVdms_id(), httpServletRequest);
                        vdmsDTO.setNetwork_names(network_names);
                    }
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
                log.info("Fetching Proxy Profiles Tagged To Vdms_Id By Organisation_Id:{},EndPoint:{}", customer_org_id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public void updateProxyServerHostSyncByVdmsId(Integer value, String host_machine, HttpServletRequest
            httpServletRequest) {
        log.info("Payload:Value:{},Host_Machine:{}", value, host_machine);
        vdmsRepository.updateProxyServerHostSyncByVdmsId(value, host_machine);
        log.info("Update Proxy Server Host Sync By Vdms_Id.EndPoint:{}", httpServletRequest.getRequestURI());
        syncDataByVdmsId(host_machine, httpServletRequest);
    }

    public void updateProxyClientSyncByVdmsId(Integer value, String host_machine, HttpServletRequest
            httpServletRequest) {
        log.info("Payload:Value:{},Host_Machine:{}", value, host_machine);
        vdmsRepository.updateProxyClientSyncByVdmsId(value, host_machine);
        log.info("Update Proxy Client Sync By Vdms_Id.EndPoint:{}", httpServletRequest.getRequestURI());
        syncDataByVdmsId(host_machine, httpServletRequest);
    }

    public void updateProxyClientSyncByProxyProfileId(String id, HttpServletRequest httpServletRequest) {
        vdmsRepository.updateProxyClientSyncByProxyProfileId(id);
        Set<String> vdms_ids = vdmsRepository.getVdmsIdsByProxyProfileId(id);
        if (vdms_ids != null && vdms_ids.size() > 0) {
            for (String vdms_id : vdms_ids) {
                log.info("Update Proxy Client Sync By Proxy_Profile_Id.EndPoint:{}", httpServletRequest.getRequestURI());
                syncDataByVdmsId(vdms_id, httpServletRequest);
            }
        }
    }

    public String getPrimaryProxyProfileByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        updateProxyClientSyncByVdmsId(0, vdms_id, httpServletRequest);
        log.info("Fetching Primary Proxy Profile By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return vdmsRepository.getPrimaryProxyProfileByVdmsId(vdms_id);
    }

    public String getDefaultProxyProfileByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Fetching Default Proxy Profile By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return vdmsRepository.getPrimaryProxyProfileByVdmsId(vdms_id);
    }

    public void syncDataByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        VdmsSyncDTO vdmsSyncDTO = updateVdmsData(vdms_id, httpServletRequest);
        log.info("Sync data By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        int isMultiTenant = vdmsRepository.getMultiTenantCheck(vdms_id);
        if (isMultiTenant == 1) {
            String awsRegion = vdmsRepository.getAwsRegionByVdmsId(vdms_id);
            webClientService.multiTenantSyncApiCall(vdms_id, vdmsSyncDTO, awsRegion, httpServletRequest);
        } else {
            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdms_id + "/sync/data", vdmsSyncDTO);
        }
    }

    public ResponseEntity<?> updateVdmsLocationByVdmsId(Set<VdmsDTO> vdmsDTOS, String email, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO:{},loggedInUser:{}", vdmsDTOS, loggedInUser);
        if (email != null && loggedInUser != null) {

            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                if (vdmsDTOS != null && vdmsDTOS.size() > 0) {
                    for (VdmsDTO vdmsDTO : vdmsDTOS) {
                        String coordinateId = vdmsRepository.getCoordinatesVdmsId(vdmsDTO.getVdms_id());
                        if (coordinateId != null) {
                            if (vdmsDTO.getCoordinates() == null) {
                                vdmsRepository.updateVdmsLocationByVdmsId(vdmsDTO.getLatitude(), vdmsDTO.getLongitude(), null, vdmsDTO.getVdms_id());
                                vdmsCoordinatesService.deleteVdmsCoordinatesById(coordinateId);
                            } else {
                                vdmsCoordinatesService.updateVdmsCoordinates(coordinateId, vdmsDTO.getCoordinates());
                                vdmsRepository.updateVdmsLocationByVdmsId(vdmsDTO.getLatitude(), vdmsDTO.getLongitude(), coordinateId, vdmsDTO.getVdms_id());
                            }

                        } else {
                            coordinateId = Generators.timeBasedGenerator().generate().toString();
                            vdmsCoordinatesService.addVdmsCoordinates(coordinateId, vdmsDTO.getCoordinates());
                            vdmsRepository.updateVdmsLocationByVdmsId(vdmsDTO.getLatitude(), vdmsDTO.getLongitude(), coordinateId, vdmsDTO.getVdms_id());
                        }

                    }
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);

                    userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "A VDMS Location Is Updated", "success");
                    log.info("Update Vdms Location By Vdms_Id,EndPoint:{}", httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Invalid Client Parameters", "failed");
                    throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    private String getFileNameByImageUrl(String image_url, HttpServletRequest httpServletRequest) {
        log.info("Fetching File Name By Image_Url:{},EndPoint:{}", image_url, httpServletRequest.getRequestURI());
        return image_url.substring(image_url.lastIndexOf("/") + 1);
    }

    public ResponseEntity<?> deleteVdmsByVdmsIdAndSuperAdminEmail(String username, String vdms_id, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        if (username != null && vdms_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin");
            if (access) {
                String role = userService.getRoleNameByUserEmail(username, httpServletRequest);
                if (role.equals("super-admin")) {
                    deleteVdmsDataByVdmsIdAndSuperAdminEmail(username, vdms_id, loggedInUser, httpServletRequest);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "A VDMS With Id:" + vdms_id + " Is Deleted By Super Admin", "success");
                    log.info("Delete Vdms By Vdms_Id:{},And Super_Admin_Email.username:{}.EndPoint:{}", username, vdms_id, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Invalid Role.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "Invalid Role", "failed");
                    throw new ClientException("Invalid role", 702, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void transferVdmsToRegisteredUserByVdmsId(String email, String vdms_id, VdmsDTO
            vdmsDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO:{}", vdmsDTO);
        UserDTO oldUserDetails = userService.getMasterUserInfoByVdmsId(vdms_id, httpServletRequest);
        VdmsDTO vdmsDetails = getVdmsInfoByVdmsId(vdms_id, httpServletRequest);
        UserDTO newUserDetails = userService.getMasterUserInfoByOrganisationId(vdmsDTO.getCustomer_org_id(), httpServletRequest);

        if (vdmsDetails.vdms_id != null) {
            if (oldUserDetails.getEmail().equals(email)) {
                if (!oldUserDetails.getOrganisation_id().equalsIgnoreCase(vdmsDTO.getCustomer_org_id())) {
                    vdmsprofileService.deleteVdmsProfileByVdmsIdAndCustomerOrganisationId(vdms_id, oldUserDetails.getOrganisation_id(), httpServletRequest);
                    vdmsvisibilityService.deleteVisibleVdmsByCustomerOrganisationIdAndVdmsId(oldUserDetails.getOrganisation_id(), vdms_id, httpServletRequest);

                    // send email to the old master-user of that vdms
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("to", oldUserDetails.getEmail());
                    jsonObject.put("name", oldUserDetails.getName());
                    jsonObject.put("masterUserEmail", newUserDetails.getEmail());
                    jsonObject.put("vdmsId", vdms_id);
                    jsonObject.put("propertyName", vdmsDetails.getProperty_name());
                    jsonObject.put("vdmsAddress", vdmsDetails.getAddress());

                    log.info("Payload:JSONObject:{}", jsonObject);
                    webClientService.vdmsRemovedEmail(jsonObject, httpServletRequest);
                    log.info("Successfully Vdms removed Email.EndPoint:{}", httpServletRequest.getRequestURI());
                    vdmsRepository.transferVdmsToRegisteredUserByVdmsId(vdmsDTO.getCustomer_org_id(), 1, vdms_id);

                    //send email to the new master-user
                    jsonObject.put("to", newUserDetails.getEmail());
                    jsonObject.put("name", newUserDetails.getName());
                    jsonObject.put("propertyOwnerName", oldUserDetails.getName());
                    jsonObject.put("phone", oldUserDetails.getPhone());
                    jsonObject.put("email", oldUserDetails.getEmail());
                    jsonObject.put("vdmsId", vdms_id);
                    jsonObject.put("propertyName", vdmsDetails.getProperty_name());
                    jsonObject.put("vdmsAddress", vdmsDetails.getAddress());

                    log.info("Payload:JSONObject:{}", jsonObject);
                    webClientService.vdmsTranferEmail(jsonObject, httpServletRequest);
                    log.info("Transfer Vdms To Registered User By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
                    VdmsSyncDTO vdmsSyncDTO = this.getVdmsSyncByVdmsId(vdms_id, httpServletRequest);
                    int isMultiTenant = vdmsRepository.getMultiTenantCheck(vdms_id);
                    if (isMultiTenant == 1) {
                        webClientService.multiTenantSyncApiCall(vdms_id, vdmsSyncDTO, vdmsDetails.getAwsRegion(), httpServletRequest);
                    } else {
                        socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdms_id + "/sync/data", vdmsSyncDTO);
                    }
                } else {
                    log.info("VDMS already assigned.EndPoint:{}", httpServletRequest.getRequestURI());
                }
            } else {
                log.info("Master user does not match.EndPoint:{}", httpServletRequest.getRequestURI());
            }
        } else {
            log.info("Vdms does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> transferBulkVdmsToRegisteredUserByVdmsId(String
                                                                              email, List<VdmsDTO> vdmsDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Email:{},VdmsDTO:{},loggedInUser:{}", email, vdmsDTO, loggedInUser);
        if (email != null && vdmsDTO != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                for (VdmsDTO vdmsDetails : vdmsDTO) {
                    transferVdmsToRegisteredUserByVdmsId(email, vdmsDetails.getVdms_id(), vdmsDetails, httpServletRequest);
                    userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Transferred Bulk VDMS Data:" + vdmsDetails.getVdms_id() + " TO Registered User", "success");
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Transfer Bulk Vdms To Registered User By Vdms_Id,EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getVdmsStatus(String key, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                List<VdmsDTO> vdmsDTOList = vdmsRepository.getVdmsByKey(key);

                for (VdmsDTO vdmsDTO : vdmsDTOList) {
                    if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(vdmsDTO.getLast_seen().longValueExact()) > 10) {
                        vdmsDTO.setVdms_status("0");
                    } else {
                        vdmsDTO.setVdms_status("1");
                    }
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOList, 200, true);
                log.info("Fetching Vdms Status. Key:{}.EndPoint:{}", key, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getProxyProfileByVdmsId(String loggedInUser, JSONObject body, HttpServletRequest httpServletRequest) {
        log.info("Payload:body:{}", body);
        if (body.get("vdmsId") != null || body.get("qrcodeId") != null) {
            JSONObject data = new JSONObject();
            if (body.get("vdmsId") != null && body.get("qrcodeId") == null) {
                //nfc
                ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(body.get("vdmsId").toString(), httpServletRequest);
                boolean isNotExpired = true;
                if (body.get("token") != null) {
                    isNotExpired = tokenVerifier.isTokenValid(body.get("token").toString());
                    System.out.println("is Expires ==>" + isNotExpired);

                    if (!isNotExpired) {
                        // if token is expred
                        String username = loggedInUser;
                        if (username == null) {
                            username = this.getUsername(body.get("token").toString());
                        }
                        String orgId = vdmsRepository.getOrgIdByVdmsId(body.get("vdmsId").toString());// getorgId by vdmsId
                        log.info("orgId:{}", orgId);
                        if (orgId != null) {
                            ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                            JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                            String role = privilege.get("role").toString();
                            String checkAccess = null;
                            if (role.contains("master-user") || role.contains("org-admin")) {
                                checkAccess = userRepository.checkVdmsVisbilityByEmail(body.get("vdmsId").toString(), privilege.get("organisation_id").toString(), username);
                            } else if (role.contains("master-vendor")) {
                                String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(body.get("vdmsId").toString(), privilege.get("organisation_id").toString());
                                if (vdmsId != null && vdmsId.equals(body.get("vdmsId").toString())) {
                                    checkAccess = username;
                                }
                            } else {
                                checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, body.get("vdmsId").toString());
                            }
                            if (checkAccess != null && checkAccess.equals(username)) {
                                data.put("vdms_id", body.get("vdmsId").toString());
//                            data.put("proxy_profile", null);
                                data.put("errorCode", 0);   // has vdms access
                            } else {
                                data.put("vdms_id", body.get("vdmsId").toString());
//                            data.put("proxy_profile", null);
                                data.put("errorCode", 1);  // vdms access denied
                            }
                            data.put("isAuthenticated", 0);
                        } else {
                            log.info("NFC is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                            data.put("email", username);
                            data.put("organisation_id", null);
                            data.put("privileges", null);
                            data.put("errorCode", 2);  // qr code not tagged with vdms
                            data.put("isAuthenticated", 0);
                        }
//                        data.put("email", null);
//                        data.put("organisation_id", null);
//                        data.put("privileges", null);
//                        data.put("role", null);


                        log.info("Response:{}", data);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        // if token is not expred
                        String username = loggedInUser;
                        if (username == null) {
                            username = this.getUsername(body.get("token").toString());
                        }
                        String orgId = vdmsRepository.getOrgIdByVdmsId(body.get("vdmsId").toString());// getorgId by vdmsId
                        log.info("orgId:{}", orgId);
                        if (orgId != null) {
                            ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                            JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                            String role = privilege.get("role").toString();
                            String checkAccess = null;
                            if (role.contains("master-user") || role.contains("org-admin")) {
                                checkAccess = userRepository.checkVdmsVisbilityByEmail(body.get("vdmsId").toString(), privilege.get("organisation_id").toString(), username);

                            } else if (role.contains("master-vendor")) {
                                String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(body.get("vdmsId").toString(), privilege.get("organisation_id").toString());
                                if (vdmsId != null && vdmsId.equals(body.get("vdmsId").toString())) {
                                    checkAccess = username;
                                }
                            } else {
                                checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, body.get("vdmsId").toString());
                            }


                            if (checkAccess != null && checkAccess.equals(username)) {
                                data.put("vdms_id", body.get("vdmsId").toString());
                                data.put("proxy_profile", proxyProfileData);
//                         String coordinates = vdmsRepository.getCoordinatesByVdmsId(body.get("vdmsId").toString());
//                            data.put("coordinates",coordinates);

                                String property_name = vdmsRepository.getPropertyNameByVdmsId(body.get("vdmsId").toString());
                                data.put("property_name", property_name);


                                if (body.containsKey("currentPosition")) {
                                    if (body.getString("currentPosition") != null) {
                                        String vdmsId = body.get("vdmsId").toString();
                                        String coordinatesId = vdmsRepository.getCoordinatesVdmsId(vdmsId);
                                        if (coordinatesId != null) {
                                            String coordinates = vdmsCoordinatesService.getCoordinatesById(coordinatesId);
                                            float lat = body.getJSONArray("currentPosition").getFloat(0);
                                            float lng = body.getJSONArray("currentPosition").getFloat(1);
                                            Long result = vdmsCoordinatesService.checkIsPresent(lat, lng, coordinates);


                                            log.info("RESULT : {}", result);
                                            if (result == null) {
                                                data.put("isWithinPremise", 1);
                                            } else {
                                                if (result == 0) {
                                                    data.put("isWithinPremise", 0);
                                                } else {
                                                    data.put("isWithinPremise", 1);
                                                }
                                            }
                                        } else {
                                            data.put("isWithinPremise", 1);
                                        }
                                    } else {
                                        data.put("isWithinPremise", 1);
                                    }

                                } else {
                                    data.put("isWithinPremise", 1);
                                }


                                data.put("errorCode", 0);   // has vdms access
                            } else {
                                data.put("vdms_id", body.get("vdmsId").toString());
                                data.put("proxy_profile", null);
                                data.put("errorCode", 1);  // vdms access denied
                            }
                            data.put("email", username);
                            data.put("organisation_id", privilege.get("organisation_id"));
//                        data.put("privileges", privilege.get("privileges"));
                            data.put("role", privilege.get("role"));
                            data.put("sclerafx_windows_version", privilege.get("sclerafx_windows_version"));
                            data.put("sclerafx_ubuntu_version", privilege.get("sclerafx_ubuntu_version"));
                            data.put("sclerafx_mac_version", privilege.get("sclerafx_mac_version"));
                            data.put("is_enterprise", privilege.get("is_enterprise"));
                            data.put("terms_and_conditions", privilege.get("terms_and_conditions"));
                            data.put("isAuthenticated", 1);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            log.info("QR code is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                            data.put("email", username);
                            data.put("organisation_id", null);
                            data.put("privileges", null);
                            data.put("errorCode", 2);  // qr code not tagged with vdms
                            data.put("isAuthenticated", 1);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    }
                } else {
                    //if token not given
                    data.put("vdms_id", body.get("vdmsId").toString());
//                    data.put("proxy_profile", null);
//                    data.put("email", null);
//                    data.put("organisation_id", null);
//                    data.put("privileges", null);
//                    data.put("role", null);
                    data.put("isAuthenticated", 0);
                    data.put("errorCode", 0);
                    log.info("Response:{}", data);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }
            } else {
                // qr code


                boolean isExpired = true;
                if (body.get("token") != null) {
                    isExpired = tokenVerifier.isTokenValid(body.get("token").toString());
                    System.out.println("is Expires ==>" + isExpired);
                    if (!isExpired) {
                        // if token is expred
                        String username = loggedInUser;
                        if (username == null) {
                            username = this.getUsername(body.get("token").toString());
                        }
                        String orgId = getOrgIdByQrCodeId(body.get("qrcodeId").toString());
                        log.info("orgId:{}", orgId);
                        if (orgId != null) {
                            ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                            log.info("response from bff server" + response);
                            JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                            QrCodeDTO qrData = qrCodeService.getQrCodeDataByQrCodeId(body.get("qrcodeId").toString());
                            if (qrData != null && qrData.getVdmsId() != null) {
                                ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(qrData.getVdmsId(), httpServletRequest);

                                String role = privilege.get("role").toString();
                                String checkAccess = null;
                                if (role.contains("master-user") || role.contains("org-admin")) {
                                    checkAccess = userRepository.checkVdmsVisbilityByEmail(qrData.getVdmsId(), privilege.get("organisation_id").toString(), username);

                                } else if (role.contains("master-vendor")) {
                                    String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(qrData.getVdmsId(), privilege.get("organisation_id").toString());
                                    if (vdmsId != null && vdmsId.equals(qrData.getVdmsId())) {
                                        checkAccess = username;
                                    }
                                } else if (role.contains("guest")) {
                                    checkAccess = username;
                                } else {
                                    checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, qrData.getVdmsId());
                                }
                                if (checkAccess != null && checkAccess.equals(username)) {
                                    data.put("vdms_id", qrData.getVdmsId());
//                                data.put("proxy_profile", null);
                                    data.put("location_id", qrData.getLocationId());
                                    data.put("device_id", qrData.getDeviceId());
                                    data.put("errorCode", 0);   // has vdms access // qr code linked to vdms


                                } else {
//                                data.put("vdms_id", null);
//                                data.put("location_id", null);
//                                data.put("device_id", null);
//                                data.put("proxy_profile", null);
                                    data.put("errorCode", 1);  // vdms access denied
                                }

                                if (qrData.getLocationId() != null && (privilege.get("organisation_id").toString().equals("99950026"))) { //removed || privilege.get("organisation_id").toString().equals("85161001") for testing
                                    data.put("authPageType", 1);
                                }
                            } else {
//                            data.put("location_id", null);
//                            data.put("device_id", null);
//                            data.put("proxy_profile", null); // qr code not linked to vdms
                                data.put("errorCode", 2);
                            }
//                        data.put("email", null);
//                        data.put("organisation_id", null);
//                        data.put("privileges", null);
//                        data.put("role", null);

                            data.put("isAuthenticated", 0);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            log.info("QR code is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                            data.put("email", username);
                            data.put("organisation_id", null);
                            data.put("privileges", null);
                            data.put("errorCode", 2);  // qr code not tagged with vdms
                            data.put("isAuthenticated", 0);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    } else {
                        // if token is not expred
                        String username = loggedInUser;
                        if (username == null) {
                            username = this.getUsername(body.get("token").toString());
                        }
                        log.info("UserName:{}", username);
                        String orgId = getOrgIdByQrCodeId(body.get("qrcodeId").toString());
                        log.info("orgId:{}", orgId);

                        if (orgId != null) {
                            ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                            System.out.println("response===>" + response);

                            JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                            QrCodeDTO qrData = qrCodeService.getQrCodeDataByQrCodeId(body.get("qrcodeId").toString());
                            if (qrData != null && qrData.getVdmsId() != null) {
                                ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(qrData.getVdmsId(), httpServletRequest);
                                String role = privilege.get("role").toString();
                                String checkAccess = null;
                                if (role.contains("master-user") || role.contains("org-admin")) {
                                    checkAccess = userRepository.checkVdmsVisbilityByEmail(qrData.getVdmsId(), privilege.get("organisation_id").toString(), username);

                                } else if (role.contains("master-vendor")) {
                                    String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(qrData.getVdmsId(), privilege.get("organisation_id").toString());
                                    if (vdmsId != null && vdmsId.equals(qrData.getVdmsId())) {
                                        checkAccess = username;
                                    }
                                } else if (role.contains("guest")) {
                                    checkAccess = username;
                                } else {
                                    checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, qrData.getVdmsId());
                                }
                                if (checkAccess != null && checkAccess.equals(username)) {
                                    data.put("vdms_id", qrData.getVdmsId());

                                    String property_name = vdmsRepository.getPropertyNameByVdmsId(qrData.getVdmsId());
                                    data.put("property_name", property_name);

                                    data.put("location_id", qrData.getLocationId());
                                    data.put("device_id", qrData.getDeviceId());
                                    data.put("proxy_profile", proxyProfileData);


                                    if (body.containsKey("currentPosition")) {
                                        if (body.getString("currentPosition") != null) {
                                            String vdmsId = qrData.getVdmsId();
                                            String coordinatesId = vdmsRepository.getCoordinatesVdmsId(vdmsId);
                                            if (coordinatesId != null) {
                                                String coordinates = vdmsCoordinatesService.getCoordinatesById(coordinatesId);
                                                log.info("COORDINATES : {}", coordinates);
                                                float lat = body.getJSONArray("currentPosition").getFloat(0);
                                                float lng = body.getJSONArray("currentPosition").getFloat(1);
                                                log.info("LATITUDE : {}", lat);
                                                log.info("LONGITUDE : {}", lng);


                                                Long result = vdmsCoordinatesService.checkIsPresent(lat, lng, coordinates);

                                                log.info("RESULT : {}", result);

                                                if (result == null) {
                                                    data.put("isWithinPremise", 1);
                                                } else {
                                                    if (result == 0) {
                                                        data.put("isWithinPremise", 0);
                                                    } else {
                                                        data.put("isWithinPremise", 1);
                                                    }
                                                }
                                            } else {
                                                data.put("isWithinPremise", 1);
                                            }
                                        } else {
                                            data.put("isWithinPremise", 1);
                                        }

                                    } else {
                                        data.put("isWithinPremise", 1);
                                    }


                                    data.put("errorCode", 0);   // has vdms access // qr code linked to vdms
                                } else {
//                                data.put("vdms_id", null);
//                                data.put("location_id", null);
//                                data.put("device_id", null);
//                                data.put("proxy_profile", null);
                                    data.put("errorCode", 1);  // vdms access denied
                                }
                            } else {
//                            data.put("location_id", null);
//                            data.put("device_id", null);
//                            data.put("proxy_profile", null); // qr code not linked to vdms
                                data.put("errorCode", 2);
                            }
                            data.put("email", username);
                            data.put("organisation_id", privilege.get("organisation_id"));
//                        data.put("privileges", privilege.get("privileges"));
                            data.put("role", privilege.get("role"));
                            data.put("sclerafx_windows_version", privilege.get("sclerafx_windows_version"));
                            data.put("sclerafx_ubuntu_version", privilege.get("sclerafx_ubuntu_version"));
                            data.put("sclerafx_mac_version", privilege.get("sclerafx_mac_version"));
                            data.put("is_enterprise", privilege.get("is_enterprise"));
                            data.put("terms_and_conditions", privilege.get("terms_and_conditions"));
                            data.put("isAuthenticated", 1);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            log.info("QR code is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                            data.put("email", username);
                            data.put("organisation_id", null);
                            data.put("privileges", null);
                            data.put("errorCode", 2);  // qr code not tagged with vdms
                            data.put("isAuthenticated", 1);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    }
                } else {
                    //if token not given

                    QrCodeDTO qrData = qrCodeService.getQrCodeDataByQrCodeId(body.get("qrcodeId").toString());
                    if (qrData != null && qrData.getVdmsId() != null) {
                        ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(qrData.getVdmsId(), httpServletRequest);
                        data.put("vdms_id", qrData.getVdmsId());
                        data.put("location_id", qrData.getLocationId());
                        data.put("device_id", qrData.getDeviceId());
//                        data.put("proxy_profile", null); // qr code linked to vdms
                        data.put("errorCode", 0);
                        String orgId = vdmsRepository.getOrgIdByVdmsId(qrData.getVdmsId());
                        if (qrData.getLocationId() != null && (orgId.equals("99950026"))) { //removed || orgId.equals("85161001") for testing
                            data.put("authPageType", 1);
                        }
                    } else {
//                        data.put("vdms_id", null);
//                        data.put("location_id", null);
//                        data.put("device_id", null);
//                        data.put("proxy_profile", null); // qr code not linked to vdms
                        data.put("errorCode", 2);
                    }
//                    data.put("email", null);
//                    data.put("organisation_id", null);
//                    data.put("privileges", null);
//                    data.put("role", null);


                    data.put("isAuthenticated", 0);
                    log.info("Response:{}", data);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }
            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public String getUsername(String token) {
        return JWT.decode(token).getClaim("email").asString() != null
                ? JWT.decode(token).getClaim("email").asString()
                : JWT.decode(token).getClaim("sub").asString();
    }


    public Boolean checkToken(String token, HttpServletRequest httpServletRequest) {
        log.info("Payload: token: {}", token);

        if (token != null) {
            try {
                Date expiration = JWT.decode(token).getExpiresAt();
                Instant expirationInstant = expiration.toInstant();
                Instant currentTime = Instant.now();
                boolean isExpired = expirationInstant.isBefore(currentTime);
                return isExpired;
            } catch (JWTDecodeException | SignatureVerificationException exception) {
                return true;
            }

        } else {
            return true;
        }
    }

    public ResponseEntity<?> updateVdmsMasterSlaveStatusByVdmsId(String orgId, String email, String vdmsId, String
            loggedInUser, VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload:OrgId:{},Email:{},VdmsId:{},loggedInUser:{},vdmsDTO:{}", orgId, email, vdmsId, loggedInUser, vdmsDTO);
        boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "master-vendor", "org-admin", "user", "vendor", "property-admin");
        if (access) {
            String dbData = vdmsRepository.checkVdmsIdByEmail(email, vdmsId);
            log.info("Vdms Info: {}", dbData);
            if (dbData != null && Objects.equals(vdmsId, vdmsDTO.getVdms_id())) {
                vdmsRepository.updateVdmsMasterStatusByVdmsId(vdmsDTO.getIs_master(), vdmsDTO.getSecondary_device_id(), vdmsId);
                vdmsRepository.updateVdmsSlaveStatusByVdmsId(0, null, vdmsDTO.getSecondary_device_id());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Update Master-Slave status successfully for VDMS: " + vdmsId, "success");
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Update Master-Slave status successfully for VDMS " + vdmsId, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("VdmsId: {} does not exist.EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "VDMS ID:" + vdmsId + " Does Not Exist", "failed");
                throw new ClientException("VdmsId does not exist", 728, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Role Not Authorised", "failed");
            throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
        }
    }

    public Integer getActivationStatus(String orgId, HttpServletRequest httpServletRequest) {
        return vdmsRepository.getVdmsActivationStatusByOrgId(orgId);
    }

    public Set<String> getVdmsIdByOrgId(String orgId) {
        return vdmsRepository.getVdmsIdsByCustomerOrganisationId(orgId);
    }

    public void deleteVdmsByListOfVdmsId(String username, Set<String> vdms_ids, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserName:{},vdms_Id:{},loggedInUser:{}", username, vdms_ids, loggedInUser);
        if (username != null && vdms_ids != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                for (String vdms_id : vdms_ids) {
                    deleteVdmsByMasterUser(username, vdms_id, loggedInUser, httpServletRequest);
                    log.info("Delete New Vdms By vdms_Id:{},Endpoint:{}", vdms_id, httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "A VDMS With Id:" + vdms_id + " Is Deleted", "success");
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMS", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }


    public Integer deleteVdmsByMasterUser(String username, String vdms_id, String loggedInUser, HttpServletRequest
            httpServletRequest) {
        log.info("Payload:UserName:{},vdms_Id:{}", username, vdms_id);
        String org_id = vdmsRepository.getCustomerOrgIdByVdmsId(vdms_id);
        Integer is_enterprise = customerOrganisationService.getEnterpriseInfoById(org_id);
        log.info("is_enterprise:" + is_enterprise);
        if (is_enterprise == 0) {
            String is_activated = getVdmsActivationStatusByVdmsId(vdms_id, httpServletRequest);
            log.info("is_activated:" + is_activated);
            if (!is_activated.equalsIgnoreCase("Activated")) {
                log.info("Delete Vdms By vdms_Id:{},Endpoint:{}", vdms_id, httpServletRequest.getRequestURI());
                deleteVdmsById(vdms_id, loggedInUser, httpServletRequest);
                return 0;
            } else {
                log.error("Can Not Delete,Vdms Activated.EndPoint:{}", httpServletRequest.getRequestURI());
                return 1;
            }
//            } else {
//                return 1;
//            }
        } else if (is_enterprise == 1) {
            String is_activated = getVdmsActivationStatusByVdmsId(vdms_id, httpServletRequest);
            if (!is_activated.equalsIgnoreCase("Activated")) {
                log.info("Delete Vdms By vdms_Id:{},Endpoint:{}", vdms_id, httpServletRequest.getRequestURI());
                deleteVdmsById(vdms_id, loggedInUser, httpServletRequest);
                return 0;
            } else {
                log.error("Can Not Delete,Vdms Activated.EndPoint:{}", httpServletRequest.getRequestURI());
                return 1;
            }
        } else {
            return 1;
        }
    }


    public void deleteVdmsById(String vdms_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId:{},LoggedInUser:{}", vdms_id, loggedInUser);
        webClientService.deleteVdmsTokenInLoginDB(vdms_id, loggedInUser, httpServletRequest);

        log.info("Deleting Vdms Token in Login_DB");

        ResponseDTO vdmsHealth = webClientService.deleteVdmsHealthByVdmsId(vdms_id, loggedInUser, httpServletRequest);
        log.info("Response from Health:{}", vdmsHealth);
        log.info("Deleting Vdms Health By Vdms_Id:{}", vdms_id);
        if (vdmsHealth.getStatus() == 200) {
            dockerService.deleteDockerByVdmsId(vdms_id, httpServletRequest);
            vdmsIntegrationService.deleteVdmsIntegrationByVdmsId(vdms_id, httpServletRequest);
            vdmsvisibilityService.deleteVdmsVisibilityByVdmsId(vdms_id, httpServletRequest);
            vdmsprofileService.deleteVdmsProfileByVdmsId(vdms_id, httpServletRequest);
            p2premotesessionService.deleteP2PRemoteSessionByVdmsId(vdms_id, httpServletRequest);
            nfcService.deleteNfcByVdmsId(vdms_id, httpServletRequest);
            qrCodeService.deleteQrCodeByVdmsId(vdms_id, httpServletRequest);
            vdmsFeatureService.deleteVdmsFeatureByVdmsId(vdms_id, loggedInUser, httpServletRequest);
            deleteVdmsImageByVdmsId(vdms_id, httpServletRequest);
            clientQrCodeService.deleteClientQrCodeByVdmsId(vdms_id);
            digitalTwinTemplateService.deleteDigitalTwinTemplatesByVdmsId(vdms_id);
            clientNfcService.deleteClientNfcByVdmsId(vdms_id, httpServletRequest);
            vdmsOnboardingSummaryService.deleteVdmsOnboardingSummaryByVdmsId(vdms_id);
            vdmsRepository.deleteVdmsByVdmsId(vdms_id, httpServletRequest);
            addressService.deleteAddressById(vdms_id, httpServletRequest);
        } else {
            throw new ServerException("Unable to delete VDMS Health", 750, httpServletRequest.getRequestURI());
        }

    }


    public ResponseEntity<?> getStatus(String loggedInUser, HttpServletRequest httpServletRequest) {
        if (loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                List<VdmsDTO> vdmsDTOList = vdmsRepository.getStatus();

                for (VdmsDTO vdmsDTO : vdmsDTOList) {
                    if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(vdmsDTO.getLast_seen().longValueExact()) > 10) {
                        vdmsDTO.setVdms_status("0");
                    } else {
                        vdmsDTO.setVdms_status("1");
                    }
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOList, 200, true);
                log.info("Fetching Vdms Status EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getVdms(String status, String key, String loggedInUser, HttpServletRequest
            httpServletRequest) {
        log.info("Payload: Status: {}, Key:{}, LoggedInUser: {}", status, key, loggedInUser);
        if (status != null && key != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                List<VdmsDTO> vdmsDTOList = vdmsRepository.getVdmsByKey(key);

                for (VdmsDTO vdmsDTO : vdmsDTOList) {
                    if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(vdmsDTO.getLast_seen().longValueExact()) > 10) {
                        vdmsDTO.setVdms_status("0");
                    } else {
                        vdmsDTO.setVdms_status("1");
                    }
                }

                if (status.equals("0")) {
                    vdmsDTOList.removeIf(dtolist -> !dtolist.getVdms_status().equals("0"));
                }

                if (status.equals("1")) {
                    vdmsDTOList.removeIf(dtolist -> !dtolist.getVdms_status().equals("1"));
                }

                log.info("Response size:{}", vdmsDTOList.size());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOList, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> getVdmsFeatureByVdmsId(String vdmsId, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, LoggedInUser: {}", vdmsId, loggedInUser);
        List<FeatureDTO> featureDTOs = featureService.getVdmsFeatureByVdmsId(vdmsId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(featureDTOs, 200, true);
        log.info("Fetching Vdms Feature By VdmsId: {}. Endpoint: {}", vdmsId, httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> updateVdmsFeatureByVdmsId(String vdmsId, List<String> featureIds, String
            loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, FeatureIds: {}, LoggedInUser: {}", vdmsId, featureIds, loggedInUser);
        vdmsFeatureService.deleteVdmsFeatureByVdmsId(vdmsId, loggedInUser, httpServletRequest);
        vdmsFeatureService.addVdmsFeatureByVdmsAndFeatureIds(vdmsId, featureIds, loggedInUser, httpServletRequest);
        userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Update Vdms Feature By VdmsId:" + vdmsId, "success");
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Vdms Feature Updated Successfully", 200, true);
        log.info("Vdms Feature Updated Successfully For VdmsId: {}. Endpoint: {}", vdmsId, httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getVdmsPropertyInfoByVdmsId(List<String> vdmsId, String orgId, HttpServletRequest
            httpServletRequest) {
        log.info("Payload:vdmsId:{}", vdmsId);
        List<QuickSearchDTO> vdmsDTOs = vdmsRepository.getVdmsPropertyInfoByVdmsId(vdmsId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOs, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public List<String> getVdmsIdsByOrgIdAndVdmsIds(String orgId, List<String> vdmsIds) {
        List<String> vdms = vdmsRepository.getVdmsIdsByOrgIdAndVdmsIds(orgId, vdmsIds);
        log.info("vdms related to org:{}", vdms);
        return vdms;
    }

    public ResponseEntity<?> getClientQRCodeProxyProfileByVdmsId(String loggedInUser, JSONObject body, HttpServletRequest httpServletRequest) {
        log.info("JSONObject: {}", body);
        if (body.get("vdmsId") != null || body.get("clientQrCodeId") != null) {
            JSONObject data = new JSONObject();
            // qr code
            boolean isExpired = true;
            if (body.get("token") != null) {
                isExpired = this.checkToken(body.get("token").toString(), httpServletRequest);
                if (isExpired) {
                    // if token is expred
                    String username = loggedInUser;
                    if (username == null) {
                        username = this.getUsername(body.get("token").toString());
                    }
                    log.info("UserName:{}", username);
                    String orgId = getOrgIdByQrCodeId(body.get("clientQrCodeId").toString());
                    log.info("orgId:{}", orgId);
                    if (orgId != null) {
                        ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                        log.info("Response from Login:{}", response);
                        JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                        ClientQrCodeDTO qrData = clientQrCodeService.getQrCodeDataByClientQrCodeId(body.get("clientQrCodeId").toString());
                        if (qrData != null && qrData.getVdmsId() != null) {
                            ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(qrData.getVdmsId(), httpServletRequest);

                            String role = privilege.get("role").toString();
                            String checkAccess = null;
                            if (role.contains("master-user") || role.contains("org-admin")) {
                                checkAccess = userRepository.checkVdmsVisbilityByEmail(qrData.getVdmsId(), privilege.get("organisation_id").toString(), username);

                            } else if (role.contains("master-vendor")) {
                                String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(qrData.getVdmsId(), privilege.get("organisation_id").toString());
                                if (vdmsId != null && vdmsId.equals(qrData.getVdmsId())) {
                                    checkAccess = username;
                                }
                            } else if (role.contains("guest")) {
                                checkAccess = username;
                            } else {
                                checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, qrData.getVdmsId());
                            }
                            if (checkAccess != null && checkAccess.equals(username)) {
                                data.put("vdms_id", qrData.getVdmsId());
                                data.put("proxy_profile", proxyProfileData);
                                data.put("location_id", qrData.getLocationId());
                                data.put("device_id", qrData.getDeviceId());
                                data.put("errorCode", 0);   // has vdms access // qr code linked to vdms
                            } else {
                                data.put("errorCode", 1);  // vdms access denied
                            }
                            if (qrData.getLocationId() != null && (privilege.get("organisation_id").toString().equals("99950026"))) { //removed || privilege.get("organisation_id").toString().equals("85161001") for testing
                                data.put("authPageType", 1);
                            }
                        } else {
                            // qr code not linked to vdms
                            data.put("errorCode", 2);
                        }


                        data.put("isAuthenticated", 0);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        log.info("QR code is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                        data.put("email", username);
                        data.put("organisation_id", null);
                        data.put("privileges", null);
                        data.put("errorCode", 2);  // qr code not tagged with vdms
                        data.put("isAuthenticated", 0);
                        log.info("Response:{}", data);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } else {
                    // if token is not expred
                    String username = loggedInUser;
                    if (username == null) {
                        username = this.getUsername(body.get("token").toString());
                    }
                    log.info("UserName:{}", username);
                    String orgId = getOrgIdByQrCodeId(body.get("clientQrCodeId").toString());
                    log.info("orgId:{}", orgId);
                    if (orgId != null) {
                        ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                        log.info("Response from Login:{}", response);
                        JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                        ClientQrCodeDTO qrData = clientQrCodeService.getQrCodeDataByClientQrCodeId(body.get("clientQrCodeId").toString());
                        if (qrData != null && qrData.getVdmsId() != null) {
                            ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(qrData.getVdmsId(), httpServletRequest);
                            String role = privilege.get("role").toString();
                            String checkAccess = null;
                            if (role.contains("master-user") || role.contains("org-admin")) {
                                checkAccess = userRepository.checkVdmsVisbilityByEmail(qrData.getVdmsId(), privilege.get("organisation_id").toString(), username);

                            } else if (role.contains("master-vendor")) {
                                String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(qrData.getVdmsId(), privilege.get("organisation_id").toString());
                                if (vdmsId != null && vdmsId.equals(qrData.getVdmsId())) {
                                    checkAccess = username;
                                }
                            } else if (role.contains("guest")) {
                                checkAccess = username;
                            } else {
                                checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, qrData.getVdmsId());
                            }
                            if (checkAccess != null && checkAccess.equals(username)) {
                                data.put("vdms_id", qrData.getVdmsId());
                                String property_name = vdmsRepository.getPropertyNameByVdmsId(qrData.getVdmsId());
                                data.put("property_name", property_name);
                                data.put("location_id", qrData.getLocationId());
                                data.put("device_id", qrData.getDeviceId());
                                data.put("proxy_profile", proxyProfileData);


                                if (body.containsKey("currentPosition")) {
                                    if (body.getString("currentPosition") != null) {
                                        String vdmsId = qrData.getVdmsId();
                                        String coordinatesId = vdmsRepository.getCoordinatesVdmsId(vdmsId);
                                        if (coordinatesId != null) {
                                            String coordinates = vdmsCoordinatesService.getCoordinatesById(coordinatesId);
                                            float lat = body.getJSONArray("currentPosition").getFloat(0);
                                            float lng = body.getJSONArray("currentPosition").getFloat(1);
                                            Long result = vdmsCoordinatesService.checkIsPresent(lat, lng, coordinates);

                                            log.info("RESULT : {}", result);

                                            if (result == null) {
                                                data.put("isWithinPremise", 1);
                                            } else {
                                                if (result == 0) {
                                                    data.put("isWithinPremise", 0);
                                                } else {
                                                    data.put("isWithinPremise", 1);
                                                }
                                            }
                                        } else {
                                            data.put("isWithinPremise", 1);
                                        }
                                    } else {
                                        data.put("isWithinPremise", 1);
                                    }

                                } else {
                                    data.put("isWithinPremise", 1);
                                }


                                data.put("errorCode", 0);   // has vdms access // qr code linked to vdms
                            } else {
                                data.put("errorCode", 1);  // vdms access denied
                            }
                        } else {
                            // qr code not linked to vdms
                            data.put("errorCode", 2);
                        }
                        data.put("email", username);
                        data.put("organisation_id", privilege.get("organisation_id"));
//                    data.put("privileges", privilege.get("privileges"));
                        data.put("role", privilege.get("role"));
                        data.put("sclerafx_windows_version", privilege.get("sclerafx_windows_version"));
                        data.put("sclerafx_ubuntu_version", privilege.get("sclerafx_ubuntu_version"));
                        data.put("sclerafx_mac_version", privilege.get("sclerafx_mac_version"));
                        data.put("is_enterprise", privilege.get("is_enterprise"));
                        data.put("terms_and_conditions", privilege.get("terms_and_conditions"));
                        data.put("isAuthenticated", 1);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        log.info("QR code is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                        data.put("email", username);
                        data.put("organisation_id", null);
                        data.put("privileges", null);
                        data.put("errorCode", 2);  // qr code not tagged with vdms
                        data.put("isAuthenticated", 1);
                        log.info("Response:{}", data);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                }
            } else {
                //if token not given

                ClientQrCodeDTO qrData = clientQrCodeService.getQrCodeDataByClientQrCodeId(body.get("clientQrCodeId").toString());
                if (qrData != null && qrData.getVdmsId() != null) {
                    ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(qrData.getVdmsId(), httpServletRequest);
                    data.put("vdms_id", qrData.getVdmsId());
                    data.put("location_id", qrData.getLocationId());
                    data.put("device_id", qrData.getDeviceId());
                    data.put("proxy_profile", proxyProfileData); // qr code linked to vdms
                    data.put("errorCode", 0);

                    String orgId = vdmsRepository.getOrgIdByVdmsId(qrData.getVdmsId());

                    if (qrData.getLocationId() != null && (orgId.equals("99950026"))) { //removed || orgId.equals("85161001") for testing
                        data.put("authPageType", 1);
                    }
                } else {
                    // qr code not linked to vdms
                    data.put("errorCode", 2);
                }

                data.put("isAuthenticated", 0);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);

            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(null, "VDMS", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> migrateVdms(String email, VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        log.info("PayLoad:Email:{},vdmsDTO:{}", email, vdmsDTO);
        String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
        if (role.equals("super-admin") || role.equals("admin")) {
            String isVdms = vdmsRepository.checkVdmsId(vdmsDTO.getVdms_id());
            if (isVdms == null) {
                String userEmail = userService.getMasterUserEmailsByUserOrganisationId(vdmsDTO.getCustomer_org_id());
                addressService.addAddress(vdmsDTO.getVdms_id(), null, null, null, null, null, httpServletRequest);
                if (vdmsDTO.getBase64image() != null) {
                    byte[] image = Base64.getDecoder().decode(vdmsDTO.getBase64image());
                    String image_url = awsService.addFileToAWSS3(image, resourceUrlConfig.getVdmsImageDirectory(), resourceUrlConfig.getVdmsImageUrl(),
                            vdmsDTO.getExtension(), vdmsDTO.getVdms_id(), httpServletRequest);
                    vdmsDTO.setImage_url(image_url);
                }
                vdmsRepository.addVdms(vdmsDTO.getVdms_id(), vdmsDTO.getProperty_name(), vdmsDTO.getVdms_id(), vdmsDTO.getDevuid(),
                        vdmsDTO.getCustomer_org_id(), "Activated", vdmsDTO.getImage_url());
                webClientService.addVdmsOneTimePassword(vdmsDTO.getVdms_id(), vdmsDTO.getPassword(), userEmail, vdmsDTO.getDeployment_type(), userEmail, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Vdms Successfully Migrated", 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("VdmsId: {} Already Exists.EndPoint:{}", vdmsDTO.getVdms_id(), httpServletRequest.getRequestURI());
                throw new ClientException("Vdms Already Exist", 772, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getVdmsPropertyInfoByOrganisationId(String orgId, HttpServletRequest
            httpServletRequest) {
        log.info("Payload:orId:{}", orgId);
        List<ExternalClientUserDTO> vdmsDTO = vdmsRepository.getExternalVdmsPropertyInfoByOrgId(orgId);

        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTO, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getClientNfcProxyProfileByVdmsId(String loggedInUser, JSONObject body, HttpServletRequest httpServletRequest) {
        log.info("Payload:body:{}", body);
        if (body.get("nfc_id") != null) {
            ClientNfcDTO nfcData = clientNfcService.getClientNfcDetailsByNfcId(body.get("nfc_id").toString());
            if (nfcData != null && nfcData.getVdmsId() != null) {
                body.put("vdmsId", nfcData.getVdmsId());
                JSONObject data = new JSONObject();
                //nfc
                boolean isNotExpired = true;
                if (body.get("token") != null) {
                    isNotExpired = tokenVerifier.isTokenValid(body.get("token").toString());

                    if (!isNotExpired) {
                        // if token is expred
                        String username = loggedInUser;
                        if (username == null) {
                            username = this.getUsername(body.get("token").toString());
                        }
                        log.info("UserName:{}", username);
                        String orgId = getOrgIdByClientNfcId(body.get("nfc_id").toString());
                        if (orgId != null) {
                            ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                            JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                            String role = privilege.get("role").toString();
                            String checkAccess = null;
                            if (role.contains("master-user") || role.contains("org-admin")) {
                                checkAccess = userRepository.checkVdmsVisbilityByEmail(body.get("vdmsId").toString(), privilege.get("organisation_id").toString(), username);
                            } else if (role.contains("master-vendor")) {
                                String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(body.get("vdmsId").toString(), privilege.get("organisation_id").toString());
                                if (vdmsId != null && vdmsId.equals(body.get("vdmsId").toString())) {
                                    checkAccess = username;
                                }
                            } else if (role.contains("guest")) {
                                checkAccess = username;
                            } else {
                                checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, body.get("vdmsId").toString());
                            }
                            if (checkAccess != null && checkAccess.equals(username)) {
                                data.put("vdms_id", body.get("vdmsId").toString());
                                data.put("errorCode", 0);   // has vdms access
                            } else {
                                data.put("vdms_id", body.get("vdmsId").toString());
                                data.put("errorCode", 1);  // vdms access denied
                            }

                            data.put("isAuthenticated", 0);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            log.info("NFC is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                            data.put("errorCode", 2);  // nfc not tagged with vdms
                            data.put("isAuthenticated", 0);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    } else {
                        // if token is not expred

                        ProxyProfileDTO proxyProfileData = proxyProfileService.getVdmsProxyProfileDataByVdmsId(body.get("vdmsId").toString(), httpServletRequest);

                        String username = loggedInUser;
                        if (username == null) {
                            username = this.getUsername(body.get("token").toString());
                        }
                        log.info("UserName:{}", username);
                        String orgId = getOrgIdByClientNfcId(body.get("nfc_id").toString());
                        log.info("orgId:{}", orgId);
                        if (orgId != null) {
                            ResponseDTO response = webClientService.getUserDetailsAndProxyProfileDetailsByEmail(username, orgId);
                            JSONObject privilege = JSONObject.parseObject(JSON.toJSONString(response.getData()));
                            String role = privilege.get("role").toString();
                            String checkAccess = null;
                            if (role.contains("master-user") || role.contains("org-admin")) {
                                checkAccess = userRepository.checkVdmsVisbilityByEmail(body.get("vdmsId").toString(), privilege.get("organisation_id").toString(), username);

                            } else if (role.contains("master-vendor")) {
                                String vdmsId = dockerService.getVdmsAccessByOrganisationIdAndVdmsId(body.get("vdmsId").toString(), privilege.get("organisation_id").toString());
                                if (vdmsId != null && vdmsId.equals(body.get("vdmsId").toString())) {
                                    checkAccess = username;
                                }
                            } else if (role.contains("guest")) {
                                checkAccess = username;
                            } else {
                                checkAccess = vdmsVisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(username, body.get("vdmsId").toString());
                            }


                            if (checkAccess != null && checkAccess.equals(username) && nfcData != null && nfcData.getNfc_id() != null) {
                                data.put("vdms_id", body.get("vdmsId").toString());
                                String property_name = vdmsRepository.getPropertyNameByVdmsId(body.get("vdmsId").toString());
                                data.put("property_name", property_name);
                                data.put("proxy_profile", proxyProfileData);
                                data.put("location_id", nfcData.getLocationId());
                                data.put("device_id", nfcData.getDeviceId());


                                if (body.containsKey("currentPosition")) {
                                    if (body.getString("currentPosition") != null) {
                                        String vdmsId = body.get("vdmsId").toString();
                                        String coordinatesId = vdmsRepository.getCoordinatesVdmsId(vdmsId);
                                        if (coordinatesId != null) {
                                            String coordinates = vdmsCoordinatesService.getCoordinatesById(coordinatesId);
                                            float lat = body.getJSONArray("currentPosition").getFloat(0);
                                            float lng = body.getJSONArray("currentPosition").getFloat(1);
                                            Long result = vdmsCoordinatesService.checkIsPresent(lat, lng, coordinates);
                                            log.info("RESULT : {}", result);

                                            if (result == null) {
                                                data.put("isWithinPremise", 1);
                                            } else {
                                                if (result == 0) {
                                                    data.put("isWithinPremise", 0);
                                                } else {
                                                    data.put("isWithinPremise", 1);
                                                }
                                            }
                                        } else {
                                            data.put("isWithinPremise", 1);
                                        }
                                    } else {
                                        data.put("isWithinPremise", 1);
                                    }


                                } else {
                                    data.put("isWithinPremise", 1);
                                }


                                data.put("errorCode", 0);   // has vdms access
                            } else {
                                data.put("vdms_id", body.get("vdmsId").toString());
                                data.put("proxy_profile", null);
                                data.put("errorCode", 1);  // vdms access denied
                            }
                            data.put("email", username);
                            data.put("organisation_id", privilege.get("organisation_id"));
                            data.put("role", privilege.get("role"));
                            data.put("sclerafx_windows_version", privilege.get("sclerafx_windows_version"));
                            data.put("sclerafx_ubuntu_version", privilege.get("sclerafx_ubuntu_version"));
                            data.put("sclerafx_mac_version", privilege.get("sclerafx_mac_version"));
                            data.put("is_enterprise", privilege.get("is_enterprise"));
                            data.put("terms_and_conditions", privilege.get("terms_and_conditions"));
                            data.put("isAuthenticated", 1);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            log.info("NFC is not tagged with any organisation. EndPoint:{}", httpServletRequest.getRequestURI());
                            data.put("email", username);
                            data.put("organisation_id", null);
                            data.put("privileges", null);
                            data.put("errorCode", 2);  // nfc not tagged with vdms
                            data.put("isAuthenticated", 1);
                            log.info("Response:{}", data);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    }
                } else {
                    //if token not given
                    data.put("vdms_id", body.get("vdmsId").toString());
                    data.put("isAuthenticated", 0);
                    data.put("errorCode", 0);
                    log.info("Response:{}", data);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }

            } else {
                JSONObject data = new JSONObject();
                data.put("errorCode", 2);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }

        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(null, "VDMS", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> getVdmsCoordinates(String orgId, String email, String vdmsId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Received request to get VDMS coordinates with orgId: {}, email: {}, vdmsId: {}, loggedInUser: {}", orgId, email, vdmsId, loggedInUser);
        if (vdmsId != null) {
            String data = vdmsCoordinatesService.getVdmsCoordinatesByVdmsId(vdmsId);
            log.info("Successfully fetched VDMS coordinates for vdmsId: {}", vdmsId);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, false);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public Long checkerUserLocationWithinBoundaryByCoordinatesAndVdmsId(JSONObject payload) {
        String coordinatesId = vdmsRepository.getCoordinatesVdmsId(payload.get("vdms_id").toString());

        Long result = 1L;
        if (coordinatesId != null) {
            String coordinates = vdmsCoordinatesService.getCoordinatesById(coordinatesId);
            if (coordinates != null) {
                float lat = payload.getFloat("latitude");
                float lng = payload.getFloat("longitude");
                result = vdmsCoordinatesService.checkIsPresent(lat, lng, coordinates);
            }
        }

        return result;

    }

    public List<VdmsDTO> getVdmsAlertData() {
        return vdmsRepository.getVdmsAlertData();
    }

    public void updaateVdmsEmailAlertByVdmsId(String vdmsId, int alertStatus) {
        vdmsRepository.updaateVdmsEmailAlertByVdmsId(vdmsId, alertStatus);
    }

    public void updateQrCodeSyncByVdmsId(Integer qrSync, String vdmsId, HttpServletRequest httpServletRequest) {
        vdmsRepository.updateQrCodeSyncByVdmId(qrSync, vdmsId);
        log.info("SuccessFully Updated Qr-Code Sync By VdmsID:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
    }

    public void updateNfcSyncByVdmsId(Integer nfcSync, String vdmsId, HttpServletRequest httpServletRequest) {
        vdmsRepository.updateNfcSyncByVdmId(nfcSync, vdmsId);
        log.info("SuccessFully Updated Nfc Sync By VdmsID:{}", vdmsId);
    }


    public void updateNfcSyncByVdmsIds(Integer nfcSync, List<String> vdmsId) {
        if (!vdmsId.isEmpty()) {
            vdmsRepository.updateNfcSyncByVdmIds(nfcSync, vdmsId);
        }
        log.info("SuccessFully Updated Nfc Sync By VdmsID:{}", vdmsId);
    }

    public void updateQrCodeSyncByVdmsIds(Integer qrSync, List<String> vdmsId) {
        if (!vdmsId.isEmpty()) {
            vdmsRepository.updateQrCodeSyncByVdmIds(qrSync, vdmsId);
        }
        log.info("SuccessFully Updated Qr-Code Sync By VdmsID:{}", vdmsId);
    }

    public ResponseEntity<?> getVisibleVdmsByEmailList(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        JSONArray masterUserAndOrgAdminEmailsArray = jsonObject.getJSONArray("masterUserAndOrgAdminEmails");
        JSONArray userAndPropertyAdminEmailsArray = jsonObject.getJSONArray("userAndPropertyAdminEmails");

        List<String> masterUserAndOrgAdminEmailsList = masterUserAndOrgAdminEmailsArray != null
                ? IntStream.range(0, masterUserAndOrgAdminEmailsArray.size())
                .mapToObj(masterUserAndOrgAdminEmailsArray::getString)
                .collect(Collectors.toList())
                : Collections.emptyList();

        List<String> userAndPropertyAdminEmailsList = userAndPropertyAdminEmailsArray != null
                ? IntStream.range(0, userAndPropertyAdminEmailsArray.size())
                .mapToObj(userAndPropertyAdminEmailsArray::getString)
                .collect(Collectors.toList())
                : Collections.emptyList();


        List<ExternalClientUserDTO> masterUserOrgAdminVdmsIds = vdmsRepository.getVdmsIdsByEmailList(masterUserAndOrgAdminEmailsList);

        List<String> fullAccessEmails = new ArrayList<>();
        List<String> limitedAccessEmails = new ArrayList<>();

        Map<String, String> fullAccessMap = vdmsvisibilityService.getVdmsFullAccessByEmails(userAndPropertyAdminEmailsList);

        for (String email : userAndPropertyAdminEmailsList) {
            String fullAccess = fullAccessMap.get(email);
            if (fullAccess != null && fullAccess.equals("1")) {
                fullAccessEmails.add(email);
            } else {
                limitedAccessEmails.add(email);
            }
        }

        List<ExternalClientUserDTO> fullAccessVdmsIds = vdmsRepository.getVdmsIdsByEmailList(fullAccessEmails);
        List<ExternalClientUserDTO> limitedAccessVdmsIds = vdmsVisibilityRepository.getVisibleVdmsByEmailList(limitedAccessEmails);

        List<ExternalClientUserDTO> combinedList = new ArrayList<>();
        combinedList.addAll(masterUserOrgAdminVdmsIds);
        combinedList.addAll(fullAccessVdmsIds);
        combinedList.addAll(limitedAccessVdmsIds);

        ResponseDTO responseDTO = ScleraUtils.generatePayload(combinedList, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public void updateBarCodeSyncByVdmsId(Integer barCodeSync, String vdmsId, HttpServletRequest httpServletRequest) {
        vdmsRepository.updateBarCodeSyncByVdmsId(barCodeSync, vdmsId);
        log.info("SuccessFully Updated BarCode Sync By VdmsID:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
    }

    public void updateBarCodeSyncByVdmsIds(Integer barCodeSync, List<String> vdmsId) {
        vdmsRepository.updateBarCodeSyncByVdmsIds(barCodeSync, vdmsId);
        log.info("SuccessFully Updated BarCode Sync By VdmsID:{}", vdmsId);
    }

    public ResponseEntity<?> deactivateVdmsByVdmsId(String loggedInUser, String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:vdmsId:{},loggedInUser:{}", vdmsId, loggedInUser);
        BigInteger deActivationTime = BigInteger.valueOf(System.currentTimeMillis());
        vdmsRepository.activateVdms("Not activated", deActivationTime, vdmsId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(String.format("Successfully deactivated VDMS: %s", vdmsId), 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }


    public void updateVdmsAssetCountByVdmsId(String vdmsId, Integer assetCount, HttpServletRequest httpServletRequest) {
        vdmsRepository.updateVdmsAssetCountByVdmsId(vdmsId, assetCount);
        log.info("Successfully updated vdms asset count Vdms ID {},Endpoint {}", vdmsId, httpServletRequest.getRequestURI());
    }

    public List<PropertySummaryDTO> getBillingInfoPropertySummaryByOrgId(String orgId, List<String> selectedVdmsList, int pagesize, int offset) {
        return vdmsRepository.getBillingInfoPropertySummaryByOrgId(orgId, selectedVdmsList, pagesize, offset);
    }

    public TierCountSummaryDTO getPropertyTierSummaryByOrgId(String orgId, List<String> selectedVdmsList, String billingId) {
        return vdmsRepository.getPropertyTierSummaryByOrgId(orgId, selectedVdmsList, billingId);
    }

    public Integer getTotalOnboardedAssets(List<String> selectedVdmsList, String orgId) {
        return vdmsRepository.getTotalOnboardedAssets(orgId, selectedVdmsList);
    }

    public List<String> getVdmsIdListByOrgId(String orgId) {
        return vdmsRepository.getVdmsIdListByOrgId(orgId);
    }

    public void updateBarCodeSyncByVdmsIds(Integer barCodeSync, List<String> vdmsId, HttpServletRequest httpServletRequest) {
        vdmsRepository.updateBarCodeSyncByVdmsIds(barCodeSync, vdmsId);
        log.info("SuccessFully Updated BarCode Sync By VdmsID:{}", vdmsId);
    }

    public int getBarCodeSyncByVdmsId(String vdmsId) {
        return vdmsRepository.getBarCodeSyncByVdmsId(vdmsId);
    }

    public int getNfcSyncStateByVdmsId(String vdmsId) {
        return vdmsRepository.getNfcSyncStateByVdmsId(vdmsId);
    }

    public int getQrCodeSyncByVdmsId(String vdmsId) {
        return vdmsRepository.getQrSyncByVdmsId(vdmsId);
    }

    public ResponseEntity<?> getExternalClientVdmsPropertyInfoByOrganisationId(String orgId, int pageNo, int pageSize, HttpServletRequest
            httpServletRequest) {
        log.info("Payload:orId:{}, pageNo:{}, pageSize:{}", orgId, pageNo, pageSize);
        int offset = ScleraUtils.calculateOffset(pageNo, pageSize);
        List<ExternalClientUserDTO> vdmsDTO = vdmsRepository.getExternalClientVdmsPropertyInfoByOrganisationId(orgId, pageSize, offset);

        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTO, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public List<String> getAllVdmsIdByMasterUserOrganisationId(String organisationId) {
        return vdmsRepository.getVdmsIdListByOrgId(organisationId);
    }

    public List<String> getAllVdmsIdByUserOrganisationIdAndUserEmail(String organisationId, String email) {
        return vdmsRepository.getAllVdmsIdByUserOrganisationIdAndUserEmail(organisationId, email);
    }

    public long calculateRemainingTrialDays(BigInteger trialEndDateMillis) {
        if (trialEndDateMillis == null || BigInteger.ZERO.equals(trialEndDateMillis)) {
            return 0;
        }
        long nowMillis = System.currentTimeMillis();
        Instant trialInstant = Instant.ofEpochMilli(trialEndDateMillis.longValue());
        ZonedDateTime trialDateTime = trialInstant.atZone(ZoneId.systemDefault());
        ZonedDateTime trialEndOfDay = trialDateTime.toLocalDate()
                .atTime(LocalTime.of(23, 59))
                .atZone(ZoneId.systemDefault());
        long trialEndOfDayMillis = trialEndOfDay.toInstant().toEpochMilli();
        if (trialEndOfDayMillis <= nowMillis) {
            return 0;
        }
        long diffMillis = trialEndOfDayMillis - nowMillis;
        long remainingDays = (long) Math.ceil((double) diffMillis / TimeUnit.DAYS.toMillis(1));
        return remainingDays;
    }

    public long calculateTrialEndTime(BigInteger trialEndDateMillis) {
        if (trialEndDateMillis == null || BigInteger.ZERO.equals(trialEndDateMillis)) {
            return 0;
        }
        Instant trialInstant = Instant.ofEpochMilli(trialEndDateMillis.longValue());
        ZonedDateTime trialDateTime = trialInstant.atZone(ZoneId.systemDefault());
        ZonedDateTime trialEndOfDay = trialDateTime.toLocalDate()
                .atTime(LocalTime.of(23, 59))
                .atZone(ZoneId.systemDefault());

        long trialEndOfDayMillis = trialEndOfDay.toInstant().toEpochMilli();
        return trialEndOfDayMillis;
    }

    public void checkAndSetupTrial(VdmsDTO vdms, HttpServletRequest httpServletRequest) {
        // Current UTC time in millis
        BigInteger nowMillis = BigInteger.valueOf(System.currentTimeMillis());

        // Trial timestamps from VDMS
        BigInteger trialStartDate = vdms.getTrialStartDate();
        BigInteger trialEndDate = vdms.getTrialEndDate();

        // Check if trial is active now or starts now
        if (trialStartDate != null && trialEndDate != null &&
                trialStartDate.compareTo(nowMillis) <= 0 &&
                nowMillis.compareTo(trialEndDate) < 0) {

            log.info("Trial is active. Setting up proxy.");
            setNetworkCountAndStatusForUsers(vdms, httpServletRequest);
            setProxyProfileForUsers(vdms, httpServletRequest);
        } else {
            vdms.setProxy_profile(null);
            vdms.setPrimary_proxy_profile_id(null);
            vdms.setTrialActivationStatus(0);
            vdms.setActivation_status("Not activated");
            log.info("Trial proxy setup skipped. Trial not active.");
        }
    }

    public ResponseEntity<?> getScleraAgentVdmsInfo(String orgId, String email, int pageNo, int pageSize, String key, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId:{},PageNo:{},PageSize:{},Key:{}", orgId, pageNo, pageSize, key);
        int offset = ScleraUtils.calculateOffset(pageNo, pageSize);
        String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
        List<ExternalClientUserDTO> vdmsDTOS = null;
        if (role.equals("master-user") || role.equals("org-admin")) {
            vdmsDTOS = vdmsRepository.getScleraAgentVdmsInfoByMasterUser(orgId, key, pageSize, offset);
        } else {
            List<String> vdmsIds = vdmsvisibilityService.getVisibleVdmsByEmail(email, httpServletRequest);
            vdmsDTOS = vdmsRepository.getScleraAgentVdmsInfo(vdmsIds, key, pageSize, offset);
        }
        Integer isGenerated = webClientService.checkAppCredentialsExistOrNot(orgId, httpServletRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("vdmsList", vdmsDTOS);
        jsonObject.put("isGenerated", isGenerated);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonObject, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getResellerPortalVdmsInfoByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:Vdms_Id:{}", vdms_id);
        VdmsDTO vdmsdto = vdmsRepository.getVdmsInfoByVdmsId(vdms_id);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsdto, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public void updateVDMSCorrigoConfigIds(List<String> configIds, HttpServletRequest httpServletRequest) {
        log.info("Fetching VDMS Id's for configIds:{}", configIds);
        List<String> vdmsIds = vdmsRepository.getCorrigoTaggedVdmsIds(configIds);
        if (vdmsIds != null || !vdmsIds.isEmpty()) {
            log.info("Updating Corrigo Sync to 1 for :{} vdmsIds due to Url Change", vdmsIds.size());
            vdmsRepository.updateCorrigoSyncByVdmsIds(1, vdmsIds);
            VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder().corrigo_sync(1).build();
            for (String vdmsId : vdmsIds) {
                int isMultiTenant = vdmsRepository.getMultiTenantCheck(vdmsId);
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsRepository.getAwsRegionByVdmsId(vdmsId);
                    webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO, awsRegion, httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
                }
            }
            userActionLogService.addUserActionLog(null, "Corrigo Configuration", "UPDATE", "Fetched the Corrigo Tagged VDMS Id's and Sent them to Touchscreen for URL update", "success");
        } else {
            log.info("No VDMS found for the Corrigo Config Ids");
            userActionLogService.addUserActionLog(null, "Corrigo Configuration", "UPDATE", "No VDMS found for the Corrigo Config Ids", "failed");
        }
    }

    public void updateVDMSCorrigoConfigId(String configId, HttpServletRequest httpServletRequest) {
        log.info("Fetching VDMS Id for configId:{}", configId);
        List<String> vdmsIds = vdmsRepository.getCorrigoTaggedVdmsIdByConfigId(configId);
        if (vdmsIds != null || !vdmsIds.isEmpty()) {
            log.info("Updating Corrigo Sync to 1 for :{} vdmsIds due to Corrigo Config Update", vdmsIds.size());
            vdmsRepository.updateCorrigoSyncByVdmsIds(1, vdmsIds);
            VdmsSyncDTO vdmsSyncDTO = VdmsSyncDTO.builder().user_sync(1).corrigo_sync(1).build();
            for (String vdmsId : vdmsIds) {
                int isMultiTenant = vdmsRepository.getMultiTenantCheck(vdmsId);
                if (isMultiTenant == 1) {
                    String awsRegion = vdmsRepository.getAwsRegionByVdmsId(vdmsId);
                    webClientService.multiTenantSyncApiCall(vdmsId, vdmsSyncDTO, awsRegion, httpServletRequest);
                } else {
                    socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdmsId + "/sync/data", vdmsSyncDTO);
                }
            }
            userActionLogService.addUserActionLog(null, "Corrigo Configuration", "UPDATE", "Fetched the Corrigo Tagged VDMS Id and Sent them to Touchscreen for Config update", "success");
        } else {
            log.info("No VDMS found for the Corrigo Config Id");
            userActionLogService.addUserActionLog(null, "Corrigo Configuration", "UPDATE", "No VDMS found for the Corrigo Config Id", "failed");
        }
    }

    public boolean checkCorrigoConfigIdForDelete(String configId, HttpServletRequest httpServletRequest) {
        log.info("Checking if config Id : {} is tagged to a VDMS", configId);
        List<String> vdmsId = vdmsRepository.getCorrigoTaggedVdmsIdByConfigId(configId);
        if (!vdmsId.isEmpty()) {
            userActionLogService.addUserActionLog(null, "Corrigo Configuration", "DELETE", "Cannot delete Corrigo Config Id " + configId + " since it is tagged to a VDMS", "failed");
            return true;
        } else {
            userActionLogService.addUserActionLog(null, "Corrigo Configuration", "DELETE", "Successful attempt to delete Corrigo Config Id " + configId, "success");
            return false;
        }
    }

    public ResponseEntity<?> getOrgIdByQrCodeId(String qrCodeId, Boolean isClientQrCode, HttpServletRequest httpServletRequest) {
        log.info("Payload:qrCodeId:{}", qrCodeId);
        String orgId = null;
        if (Boolean.TRUE.equals(isClientQrCode)) {
            orgId = vdmsRepository.getOrgIdByClientQrCodeId(qrCodeId);
        } else {
            orgId = vdmsRepository.getOrgIdByQrCodeId(qrCodeId);
        }
        log.info("orgId from DB: {}", orgId);  // confirm DB value reaches here
        ResponseDTO responseDTO = ScleraUtils.generatePayload(orgId, 200, true);
        log.info("responseDTO: {}", responseDTO);  // confirm payload is built
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getOrgIdByBarCodeId(String barCodeId, HttpServletRequest httpServletRequest) {
        log.info("Payload:barCodeId:{}", barCodeId);
        String orgId = vdmsRepository.getOrgIdByBarCodeId(barCodeId);
        log.info("orgId from DB: {}", orgId);  // confirm DB value reaches here
        ResponseDTO responseDTO = ScleraUtils.generatePayload(orgId, 200, true);
        log.info("responseDTO: {}", responseDTO);  // confirm payload is built
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public String getOrgIdByBarCodeId(String barCodeId) {
        log.info("Payload:barCodeId:{}", barCodeId);
        return vdmsRepository.getOrgIdByBarCodeId(barCodeId);
    }

    public String getOrgIdByQrCodeId(String qrCodeId) {
        log.info("Payload:qrCodeId:{}", qrCodeId);
        String orgId = vdmsRepository.getOrgIdByClientQrCodeId(qrCodeId);
        if (orgId != null) {
            log.info("Client QR code. orgId from DB: {}", orgId);  // confirm DB value reaches here
            return orgId;
        }
        log.info("Not a client QR code. Fetching orgId using regular QR code. qrCodeId: {}", qrCodeId);
        return vdmsRepository.getOrgIdByQrCodeId(qrCodeId);
    }

    private String getOrgIdByClientNfcId(String nfcId) {
        log.info("Payload:nfcId:{}", nfcId);
        String orgId = vdmsRepository.getOrgIdByClientNfcId(nfcId);
        log.info("orgId from DB: {}", orgId);  // confirm DB value reaches here
        return orgId;
    }

    public void sendMultiTenantData(String vdmsId, String password, HttpServletRequest httpServletRequest) {
        log.info("payload: VdmsId:{}", vdmsId);
        VdmsDTO vdmsDTO = vdmsRepository.getVdmsInfoByVdmsId(vdmsId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("vdmsId", vdmsId);
        jsonObject.put("password", password);
        jsonObject.put("customerOrgId", vdmsDTO.getCustomer_org_id());
        webClientService.sendMultiTenantData(jsonObject, vdmsId, vdmsDTO.getAwsRegion(), httpServletRequest);
    }

    public void sendUpdatedVdmsMultiTenantData(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("payload: VdmsId:{}", vdmsId);
        ResponseDTO data = webClientService.getVdmsTokenPasswordByVdmsId(vdmsId, httpServletRequest);
        log.info("DATA:" + data);
        String password = data.getData().toString();
        VdmsDTO vdmsDTO = vdmsRepository.getVdmsInfoByVdmsId(vdmsId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("vdmsId", vdmsId);
        jsonObject.put("password", password);
        jsonObject.put("customerOrgId", vdmsDTO.getCustomer_org_id());
        webClientService.sendMultiTenantData(jsonObject, vdmsId, vdmsDTO.getAwsRegion(), httpServletRequest);
    }

    public void sendActivationEmail(String email, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:Email:{},Vdms_Id:{}", email, vdms_id);
        UserDTO userdto = userService.getUserDetailsByEmail(email, httpServletRequest);
        VdmsDTO vdmsdto = vdmsRepository.getVdmsInfoByVdmsId(vdms_id);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("to", userdto.getEmail());
        jsonObject.put("propertyOwnerName", userdto.getName());
        jsonObject.put("vdmsId", vdms_id);
        jsonObject.put("propertyName", vdmsdto.getProperty_name());
        jsonObject.put("vdmsAddress", dockerService.getVdmsAddress(vdmsdto, httpServletRequest));
        log.info("Payload:JSONObject:{}", jsonObject);
        ResponseEntity<ResponseDTO> alertServerResponse = webClientService.sendActivationEmail(jsonObject, httpServletRequest);
        log.info("Response from Alert:{}", alertServerResponse);
        log.info("Successfully Sent Activation Email.EndPoint:{}", httpServletRequest.getRequestURI());
        if (alertServerResponse.getStatusCode() == HttpStatus.OK) {
            log.info("Activation email has been sent successfully to {} via email.EndPoint:{}", email, httpServletRequest.getRequestURI());
        } else {
            ResponseDTO responseDTO = alertServerResponse.getBody();
            throw new ServerException(responseDTO.getData().toString(), 812, httpServletRequest.getRequestURI());
        }
    }

    public void updateVdmsActivationStatus(String vdmsId) {
        BigInteger activationTime = BigInteger.valueOf(System.currentTimeMillis());
        vdmsRepository.activateMultiTenantVdms("Activated", activationTime, vdmsId);
    }

    public int getMultiTenantCheck(String vdmsId) {
        return vdmsRepository.getMultiTenantCheck(vdmsId);
    }

    public ResponseEntity<?> reactivateVdmsByVdmsId(String loggedInUser, String vdmsId, HttpServletRequest httpServletRequest) {
        String role = userService.getRoleNameByUserEmail(loggedInUser, httpServletRequest);
        if (role != null) {
            if (role.equalsIgnoreCase("super-admin") || role.equalsIgnoreCase("admin")) {
                VdmsDTO vdmsDTO = vdmsRepository.getVdmsInfoByVdmsId(vdmsId);
                if (vdmsDTO.getActivation_status().equals("Not activated") && vdmsDTO.getDeployment_type().equals("cloud")) {
                    log.info("Cloud Vdms(Multitenancy)");
                    userActionLogService.addUserActionLog(loggedInUser, "VDMS", "UPDATE", "Reactivation Intialized for vdms:" + vdmsId, "success");
                    sendUpdatedVdmsMultiTenantData(vdmsId, httpServletRequest);
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Successfully reactivated Vdms", 200, true);
                log.info("Successfully reactivated Vdms:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Role Not Found.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Role not found", 720, httpServletRequest.getRequestURI());
        }
    }

    public void updateVdmsDeploymentType(String vdmsId, HttpServletRequest httpServletRequest) {
        vdmsRepository.updateVdmsDeploymentType(vdmsId);
    }

    public ResponseEntity<?> updateAwsRegionById(List<VdmsDTO> vdmsDTOS, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO:{}", vdmsDTOS);
        for (VdmsDTO vdmsDTO : vdmsDTOS) {
            vdmsRepository.updateAwsRegionById(vdmsDTO.getAwsRegion(), vdmsDTO.getVdms_id());
            userActionLogService.addUserActionLog(loggedInUser, "Vdms", "UPDATE",
                    "Vdms Aws Region Added:" + vdmsDTO.getAwsRegion() + "for VDMS:" + vdmsDTO.getVdms_id(), "success");
            log.info("Successfully Updated AWS Region for vdms:{},EndPoint:{}", vdmsDTO.getVdms_id(), httpServletRequest.getRequestURI());
        }
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Successfully Updated AWS Region", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);

    }

    public String getAwsRegionByVdmsId(String vdmsId) {
        return vdmsRepository.getAwsRegionByVdmsId(vdmsId);
    }

    public List<VdmsDTO> getAllVdmsInfoByOrganisationId(String orgId) {
        return vdmsRepository.getAllVdmsInfoByOrganisationId(orgId);
    }

    public void updateVdmsSyncByVdmsIdV2(VdmsSyncDTO vdmssyncdto, VdmsDTO vdmsDTO, String token) {
        log.info("Payload:VdmsSyncDTO:{}", vdmssyncdto);
        log.info("Update Vdms Sync By Vdms_Id:{}", vdmsDTO.getVdms_id());
        vdmsRepository.updateVdmsSyncByVdmsId(vdmssyncdto.getImage_sync(), vdmssyncdto.getUser_sync(),  vdmsDTO.getVdms_id());
        if (vdmssyncdto.getUser_sync() == 1) {
            if (vdmsDTO.getIsMultiTenant() == 1) {
                webClientService.multiTenantSyncApiCall( vdmsDTO.getVdms_id(), vdmssyncdto, vdmsDTO.getAwsRegion(), token);
            } else {
                socketUtils.invokeWebSocketEndpoint("/topic/vdms/" +  vdmsDTO.getVdms_id() + "/sync/data", vdmssyncdto);
            }
        }
    }
}

