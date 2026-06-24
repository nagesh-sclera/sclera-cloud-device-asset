package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.amazonaws.HttpMethod;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.config.ResourceUrlConfig;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.exception.ServerException;
import io.sclera.model.Vdms;
import io.sclera.model.compositeclass.DockerIds;
import io.sclera.repository.*;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
public class TouchscreenService {

    @Autowired
    private DockerRepository dockerRepository;

    @Autowired
    private VdmsService vdmsService;

    @Autowired
    private VdmsVisibilityService vdmsvisibilityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VdmsRepository vdmsRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CustomerOrganisationService customerOrganisationService;

    @Autowired
    private DockerService dockerService;

    @Autowired
    private WebClientService webClientService;

    @Autowired
    private ProxyProfileRepository proxyProfileRepository;

    @Autowired
    private ResourceUrlConfig resourceUrlConfig;

    @Autowired
    private AwsService awsService;

    @Autowired
    private CommandLineService commandLineService;

    @Autowired
    private NfcRepository nfcRepository;

    @Autowired
    private QrCodeRepository qrCodeRepository;
    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;

    @Autowired
    private UserActionLogRepository userActionLogRepository;
    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private ClientQrCodeService clientQrCodeService;

    @Autowired
    private ClientNfcService clientNfcService;

    @Autowired
    private AssetTypeService assetTypeService;

    @Autowired
    private BillingInfoService billingInfoService;

    @Autowired
    private BillingAdminEmailService billingAdminEmailService;

    @Autowired
    private WebClientAlertService webClientAlertService;

    private final SecretsManagerClient secretsManagerClient;

    @Autowired
    public TouchscreenService(SecretsManagerClient secretsManagerClient) {
        this.secretsManagerClient = secretsManagerClient;
    }

    public ResponseEntity<?> syncVendorTransferByVdmsIdAndDockerName(String vdmsId, String name, HttpServletRequest httpServletRequest) {
        log.info("Payload:vdmsId:{},Docker_Name:{}", vdmsId, name);
        if (vdmsId != null && name != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                String vendor_org_id = dockerRepository.getVendorOrganisationIdByVdmsIdAndDockerName(vdmsId, name);
                if (vendor_org_id != null) {
                    List<UserDTO> vendors = webClientService.getAllVendorDetailsByOrganisationId(vendor_org_id, vdmsId, name, httpServletRequest);
                    log.info("Fetching All Vendor Details By Organisation_Id:{} from Vendor Server", vendor_org_id);
                    if (vendors != null && vendors.size() > 0) {
                        dockerRepository.updateVendorTransferByVdmsIdAndDockerName(vdmsId, name);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(new VendorTransferDTO(vendor_org_id, vendors), 200, true);
                        log.info("Sync Vendor Transfer By Vdms_Id:{},And Docker_Name:{},EndPoint:{}", vdmsId, name, httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        log.error("Vendor details does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                        throw new ClientException("Vendor details does not exist", 735, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("Vendor organisation id does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("Vendor organisation id does not exist", 736, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> upsertDockerByVdmsId(String vdmsId, String docker, HttpServletRequest httpServletRequest) {
        log.info("Payload:Docker:{}, vdmsId: {}", docker, vdmsId);
        if (docker != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                try {
                    DockerDTO dockerdto = JSON.parseObject(docker, DockerDTO.class);
                    Set<String> docker_names = dockerRepository.getDockerNamesByVdmsId(dockerdto.getVdms_id());
                    String proxy_profile_id = vdmsService.getDefaultProxyProfileByVdmsId(dockerdto.getVdms_id(), httpServletRequest);
                    dockerdto.setPrimary_proxy_profile_id(proxy_profile_id);
                    if (docker_names != null && docker_names.size() > 0) {
                        if (docker_names.contains(dockerdto.getName())) {
                            dockerRepository.updateDockerByVdmsIdAndDockerName(dockerdto.getBlock_timestamp(), dockerdto.getCidr(), dockerdto.getExternal_ip_address(),
                                    dockerdto.getGateway(), dockerdto.getHost(), dockerdto.getInterface_in(), dockerdto.getInterface_out(), dockerdto.getInternal_ip_address(),
                                    dockerdto.getInternet_required(), dockerdto.getInternet_status(), dockerdto.getInternet_timestamp(), dockerdto.getIs_static(),
                                    dockerdto.getIs_tagged(), dockerdto.getIs_block(), dockerdto.getMac_address(), dockerdto.getMacvlan_name(), dockerdto.getPrimary_dns(),
                                    dockerdto.getPublic_ip_address(), dockerdto.getSecondary_dns(), dockerdto.getSystem_type(), dockerdto.getVlan_id(), dockerdto.getApproval_status(),
                                    dockerdto.getVendor_org_id(), dockerdto.getNetwork_name(), dockerdto.getName(), dockerdto.getVdms_id());
                            ResponseDTO responseDTO = ScleraUtils.generatePayload("Docker details updated successfully", 200, true);
                            log.info("Docker details updated successfully:EndPoint:{}", httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Docker details updated successfully For VDMS:" + vdmsId, "success");
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            dockerRepository.addDockerByVdmsIdAndDockerName(dockerdto.getName(), dockerdto.getVdms_id(), dockerdto.getNetwork_name(), dockerdto.getBlock_timestamp(), dockerdto.getCidr(), dockerdto.getExternal_ip_address(),
                                    dockerdto.getGateway(), dockerdto.getHost(), dockerdto.getInterface_in(), dockerdto.getInterface_out(), dockerdto.getInternal_ip_address(),
                                    dockerdto.getInternet_required(), dockerdto.getInternet_status(), dockerdto.getInternet_timestamp(), dockerdto.getIs_static(),
                                    dockerdto.getIs_tagged(), dockerdto.getIs_block(), dockerdto.getMac_address(), dockerdto.getMacvlan_name(), dockerdto.getPrimary_dns(),
                                    dockerdto.getPublic_ip_address(), dockerdto.getSecondary_dns(), dockerdto.getSystem_type(), dockerdto.getVlan_id(), dockerdto.getApproval_status(),
                                    dockerdto.getVendor_org_id(), dockerdto.getPrimary_proxy_profile_id());

                            log.info("Docker details Added successfully:EndPoint:{}", httpServletRequest.getRequestURI());
                            vdmsvisibilityService.addVdmsByVendorOrganisationId(dockerdto.getVendor_org_id(), dockerdto.getVdms_id(), httpServletRequest);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload("Docker added successfully", 200, true);
                            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Docker details Added successfully For VDMS:" + vdmsId, "success");
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    } else {
                        dockerRepository.addDockerByVdmsIdAndDockerName(dockerdto.getName(), dockerdto.getVdms_id(), dockerdto.getNetwork_name(), dockerdto.getBlock_timestamp(), dockerdto.getCidr(), dockerdto.getExternal_ip_address(),
                                dockerdto.getGateway(), dockerdto.getHost(), dockerdto.getInterface_in(), dockerdto.getInterface_out(), dockerdto.getInternal_ip_address(),
                                dockerdto.getInternet_required(), dockerdto.getInternet_status(), dockerdto.getInternet_timestamp(), dockerdto.getIs_static(),
                                dockerdto.getIs_tagged(), dockerdto.getIs_block(), dockerdto.getMac_address(), dockerdto.getMacvlan_name(), dockerdto.getPrimary_dns(),
                                dockerdto.getPublic_ip_address(), dockerdto.getSecondary_dns(), dockerdto.getSystem_type(), dockerdto.getVlan_id(), dockerdto.getApproval_status(),
                                dockerdto.getVendor_org_id(), dockerdto.getPrimary_proxy_profile_id());

                        log.info("Docker details Added successfully:EndPoint:{}", httpServletRequest.getRequestURI());
                        vdmsvisibilityService.addVdmsByVendorOrganisationId(dockerdto.getVendor_org_id(), dockerdto.getVdms_id(), httpServletRequest);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload("Docker added successfully", 200, true);
                        userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Docker details Added successfully For VDMS:" + vdmsId, "success");
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } catch (Exception e) {
                    log.error("Docker does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    log.error("Stack Trace: {}", e.getMessage());
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Docker does Not Exist For VDMS:" + vdmsId, "failed");
                    throw new ClientException("Docker does not exist", 732, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Unauthorised Access For VDMS:" + vdmsId, "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> authenticateUser(String vdmsId, TouchscreenDTO userdto, HttpServletRequest httpServletRequest) throws
            JsonProcessingException {
        log.info("Payload: vdmsId: {}, UserDTO:{}", vdmsId, userdto);
        if (userdto != null) {
            String response = webClientService.authenticateUser(userdto, httpServletRequest);
            log.info("Response from Login Server:{}", response);
            log.info("Successfully Authenticating User");
            if (response != null) {
                if (response.equalsIgnoreCase("Invalid Credentials")) {
                    userdto.setStatus_code(1);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(userdto, 200, true);
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Invalid Client Parameters", "failed");
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else if (response.equalsIgnoreCase("Invalid User")) {
                    userdto.setStatus_code(2);
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "User With Email:" + userdto.getVdms_id() + " Not Found", "failed");
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(userdto, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    String user_email = userRepository.getCustomerEmailByVdmsId(userdto.getEmail(), userdto.getVdms_id());

                    if (user_email == null || !user_email.equals(userdto.getEmail())) {
                        userdto.setOrganisation_id(response);
                        userdto.setStatus_code(3);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(userdto, 200, true);
                        userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Successfully Authenticating User", "success");
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        UserDTO user = userRepository.getUserDetailsByOrganisationIdAndUserEmailAndVdmsId(response, userdto.getEmail(), userdto.getVdms_id());
                        UserDTO userDTO = webClientService.getUserDetailsByEmailFromLoginDB(user.getEmail(), httpServletRequest);
                        userDTO.setActivation_status(user.getActivation_status());
                        userDTO.setLast_updated_time(user.getLast_updated_time());
                        userDTO.setStatus_code(4);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(userDTO, 200, true);
                        log.info("Successfully Authenticating User.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Successfully Authenticating User", "success");

                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                }
            } else {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "User With Email:" + userdto.getVdms_id() + " Not Found", "failed");
                throw new ServerException("User not found", 807, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> activateVdms(String vdmsId, TouchscreenDTO userdto, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        log.info("Payload: vdmsID: {}, UserDTO:{}", vdmsId, userdto);
        if (userdto != null) {
            String isVdms = vdmsRepository.checkVdmsId(userdto.getVdms_id());
            if (isVdms != null) {


                String role = userService.getRoleNameByUserEmail(userdto.getEmail(), httpServletRequest);
                log.info("ROLE : {}", role);

                ResponseDTO vdmsPassword = webClientService.checkVdmsTokenCredentials(userdto.getVdms_id(), userdto, httpServletRequest);


                if (vdmsPassword.getStatus() == 751) {
                    throw new ClientException("Invalid User Credentials", 751, httpServletRequest.getRequestURI());
                } else if (vdmsPassword.getStatus() == 752) {
                    throw new ClientException("Invalid Token Credentials", 752, httpServletRequest.getRequestURI());
                } else if (vdmsPassword.getStatus() == 753) {
                    throw new ClientException("Invalid Token Params", 752, httpServletRequest.getRequestURI());
                } else if (vdmsPassword.getStatus() == 200) {
                    String is_activated = getVdmsActivationStatusByVdmsId(userdto.getVdms_id(), httpServletRequest);

                    if (!is_activated.equals("Activated")) {

                        UserDTO masterUserDetails = userService.getMasterUserInfoByVdmsId(userdto.getVdms_id(), httpServletRequest);
                        userdto.setEmail(masterUserDetails.getEmail());


                        String org_id = vdmsRepository.getCustomerOrgIdByVdmsId(userdto.getVdms_id());
                        Integer is_enterprise = customerOrganisationService.getEnterpriseInfoById(org_id);

                        if (is_enterprise == 1) {
                            BigInteger activationTime = BigInteger.valueOf(System.currentTimeMillis());
                            log.info("Activate vdms is_enterprice == 1");
                            vdmsRepository.activateVdms(userdto.getActivation_status(), activationTime, userdto.getVdms_id());
                            sendActivationEmail(userdto.getEmail(), userdto.getVdms_id(), httpServletRequest);
                            VdmsDTO dbData = vdmsRepository.getVdmsInfoByVdmsId(userdto.getVdms_id());
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(dbData, 200, true);
                            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "VDMS Activated Successfully(Is Enterprise == 1)", "success");
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

                        } else if (is_enterprise == 0) {
                            log.info("Activate vdms is_enterprice == 0");
//                                        ResponseEntity<String> paymentResponse = webClientService.activateSubscriptionByVdmsId(userdto.getEmail(), userdto.getVdms_id(), httpServletRequest);
//                                        log.info("Payment server Response :{}", paymentResponse.getBody());
//                                        if (paymentResponse.getStatusCodeValue() < 300) {
                            BigInteger activationTime = BigInteger.valueOf(System.currentTimeMillis());
                            vdmsRepository.activateVdms(userdto.getActivation_status(), activationTime, userdto.getVdms_id());
//                                            VdmsDTO vdmsDTO = JSON.parseObject(paymentResponse.getBody(), VdmsDTO.class);
//                                            vdmsRepository.updateSubscriptionByVdmsId(vdmsDTO.getEnd_date(), vdmsDTO.getStatus(), vdmsDTO.getPlan(), userdto.getVdms_id());
                            sendActivationEmail(userdto.getEmail(), userdto.getVdms_id(), httpServletRequest);
                            VdmsDTO dbData = vdmsRepository.getVdmsInfoByVdmsId(userdto.getVdms_id());
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(dbData, 200, true);
                            log.info("vdms Activated Successfully.vdms_Id:{},EndPoint:{}", isVdms, httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "VDMS Activated Successfully(Is Enterprise == 0)", "success");
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
                        }
                    } else {
                        log.info("VDMS Already Activated.vdms_Id:{},EndPoint:{}", isVdms, httpServletRequest.getRequestURI());
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                        userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "VDMS Already Activated", "failed");
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } else {
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Failed To Verify VdmsOneTimePassword For VDMS:" + vdmsId, "failed");
                    throw new ServerException("Failed To Verify VdmsOneTimePassword", 814, httpServletRequest.getRequestURI());
                }

            } else {
                log.error("User not found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "VDMS Already Activated", "failed");
                throw new ServerException("User not found", 807, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid client params.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public String getVdmsActivationStatusByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Fetching Vdms Activation Status By Vdms_id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return vdmsRepository.getVdmsActivationStatusByVdmsId(vdms_id);
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

    public ResponseEntity<?> authenticateVendor(String vdmsId, TouchscreenDTO userdto, HttpServletRequest httpServletRequest) throws
            JsonProcessingException {
        log.info("Payload: vdmsId: {}, UserDTO:{}", vdmsId, userdto);
        if (userdto != null) {
//            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
//            if (authorized) {
            String response = webClientService.authenticateUser(userdto, httpServletRequest);
            log.info("Response from Login Server:{}", response);
            log.info("Successfully Authenticating User");
            log.info(response);
            if (response != null) {
                if (response.equalsIgnoreCase("Invalid Credentials")) {
                    log.info("Incorrect Password");
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Invalid user Credentials", "failed");
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else if (response.equalsIgnoreCase("Invalid User")) {
                    log.info("Invalid User");
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Invalid user", "failed");
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    ResponseDTO vendorResponse = webClientService.validateRoleByEmail(userdto.getEmail(), httpServletRequest);
                    log.info("Response from Vendor:{}", vendorResponse);
                    String jsonString = JSON.toJSONString(vendorResponse.getData());
                    log.info("jsonString:{}", jsonString);
                    UserDTO role = JSON.parseObject(jsonString, UserDTO.class);
                    log.info("role:{}", role);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(role, 200, true);
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Successfully Authenticating Vendor", "success");
                    log.info("Successfully Authenticating Vendor.EndPoint:{}", httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }
            } else {
                log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "User not Found", "failed");
                throw new ServerException("User not found", 807, httpServletRequest.getRequestURI());
            }
//            } else {
//                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
//                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Unauthorised Access", "failed");
//                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
//            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getSystemTypes(String vdmsId, HttpServletRequest httpServletRequest) {
        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
        if (authorized) {
            Set<String> system_types = new HashSet<>();
            system_types.add("audio_video");
            system_types.add("back_office");
            system_types.add("business_center");
            system_types.add("digital_signage");
            system_types.add("door_locks_and_key_cards");
            system_types.add("energy_management_system");
            system_types.add("generic");
            system_types.add("gpon");
            system_types.add("guest_internet");
            system_types.add("guest_iot");
            system_types.add("guest_tv");
            system_types.add("isp");
            system_types.add("lighting_solution");
            system_types.add("Other-Devices");
            system_types.add("pbx");
            system_types.add("pms_system");
            system_types.add("restaurant_pos");
            system_types.add("security_system");
            log.info("Fetching System Types,EndPoint:{}", httpServletRequest.getRequestURI());
            ResponseDTO responseDTO = ScleraUtils.generatePayload(system_types, 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteDockerByDockerNameAndVdmsId(String vdmsId, String name, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId:{},Docker_Name:{}", vdmsId, name);
        if (vdmsId != null && name != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                dockerRepository.deleteById(new DockerIds(name, new Vdms(vdmsId)));
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Delete Docker By Docker_Name:{} And Vdms_Id:{},EndPoint:{}", name, vdmsId, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "DELETE", "Successfully Deleted Docker By Docker Name:" + name, "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "DELETE", "Unauthorised Access", "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getAllUserInfoByOrganisationId(String orgId, String
            vdmsId, HttpServletRequest httpServletRequest) {
        if (orgId != null && vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                List<UserDTO> users = webClientService.getAllUserDetailsByOrganisationId(orgId, httpServletRequest);
                if (users != null) {
                    VdmsSyncDTO vdmssyncdto = new VdmsSyncDTO();
                    vdmssyncdto.setUser_sync(0);
                    vdmsService.updateVdmsSyncByVdmsId(vdmssyncdto, vdmsId, httpServletRequest);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(users, 200, true);
                    log.info("Fetching All User Info By Org_Id:{},EndPoint:{}", orgId, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("User not Found.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ServerException("User not found", 807, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getVdmsInfoByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId: {}", vdmsId);
        if (vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                VdmsDTO vdmsdto = vdmsRepository.getVdmsInfoByVdmsId(vdmsId);
                setNetworkCountAndStatusForUsers(vdmsdto, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsdto, 200, true);
                log.info("Fetching Vdms Info By Vdms_Id:{}, Without Email,EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public void setNetworkCountAndStatusForUsers(VdmsDTO vdmsdto, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO:{}", vdmsdto);
        Integer network_count = dockerService.getNetworkCountByVdmsId(vdmsdto.getVdms_id(), httpServletRequest);
        if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(vdmsdto.getLast_seen().longValueExact()) > 10) {
            log.info("Vdms Is Offline,EndPoint:{}", httpServletRequest.getRequestURI());
            vdmsdto.setVdms_status("0");    // VDMS offline
        } else {
            log.info("Vdms Is Online,EndPoint:{}", httpServletRequest.getRequestURI());
            vdmsdto.setVdms_status("1");        // VDMS online
        }
        log.info("Set Network Count And Status For Users.EndPoint:{}", httpServletRequest.getRequestURI());
        vdmsdto.setNetwork_count(network_count);
//		return vdmsdto;
    }

    public ResponseEntity<?> updateVdmsByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId: {}", vdmsId);
        if (vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                VdmsSyncDTO vdmsSyncDTO = this.updateVdmsData(vdmsId, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsSyncDTO, 200, true);
                log.info("Update Vdms Data By Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
//                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "VDMS " + vdmsId + " Updated Successfully", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Unauthorised Access", "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public VdmsSyncDTO updateVdmsData(String vdms_id, HttpServletRequest httpServletRequest) {
        BigInteger last_seen = BigInteger.valueOf(System.currentTimeMillis());
        String isActivated = vdmsRepository.getVdmsActivationStatusByVdmsId(vdms_id);

        BigInteger lastSeenByVdmsId = vdmsRepository.getVdmsLastSeenByVdmsId(vdms_id);
        if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(lastSeenByVdmsId.longValueExact()) > 10) {
            vdmsRepository.updateVdmsFirstSeenByVdmsId(last_seen, vdms_id);
        }
        if (isActivated.equals("Activated")) {
            log.info("Update Vdms Data Done Successfully.Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
            vdmsRepository.updateVdmsLastSeenTimeStampByVdmsId(last_seen, vdms_id);
            vdmsService.updaateVdmsEmailAlertByVdmsId(vdms_id, 0);
        }
        return getVdmsSyncByVdmsId(vdms_id, httpServletRequest);
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

    public ResponseEntity<?> getServerProxyProfileByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId: {}", vdmsId);
        if (vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                vdmsService.updateProxyServerHostSyncByVdmsId(0, vdmsId, httpServletRequest);
                ProxyProfileDTO proxyProfileDTO = proxyProfileRepository.getServerProxyProfileByVdmsId(vdmsId);
                if (proxyProfileDTO == null) {
                    proxyProfileDTO = new ProxyProfileDTO();
                    proxyProfileDTO.setIs_deleted(1);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(proxyProfileDTO, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }
                proxyProfileDTO.setIs_deleted(0);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(proxyProfileDTO, 200, true);
                log.info("Fetching Server Proxy Profile By VDMS_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getClientProxyProfileByProxyProfileId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId: {}", vdmsId);
        if (vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                String proxy_profile_id = vdmsService.getPrimaryProxyProfileByVdmsId(vdmsId, httpServletRequest);
                ProxyProfileDTO proxyProfileDTO = getProxyProfileByProxyProfileId(proxy_profile_id, httpServletRequest);
                if (proxyProfileDTO == null) {
                    proxyProfileDTO = new ProxyProfileDTO();
                    proxyProfileDTO.setIs_deleted(1);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(proxyProfileDTO, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }
                proxyProfileDTO.setIs_deleted(0);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(proxyProfileDTO, 200, true);
                log.info("Fetching Client Proxy Profile By Proxy Profile Id,VDMS_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ProxyProfileDTO getProxyProfileByProxyProfileId(String id, HttpServletRequest httpServletRequest) {
        log.info("Fetching Proxy Profiles,by Proxy_Profile_Id:{},EndPoint:{}", id, httpServletRequest.getRequestURI());
        return proxyProfileRepository.getProxyProfileByProxyProfileId(id);
    }

    public ResponseEntity<?> updateVdmsDetailsByVdmsId(String vdmsId, VdmsDTO vdmsDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsID: {}, VdmsDTO:{}", vdmsId, vdmsDTO);
        if (vdmsDTO != null && vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                vdmsRepository.updateVdmsDetailsByVdmsId(vdmsDTO.getProperty_name(), vdmsDTO.getDevuid(), vdmsId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Update Vdms Details By Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
//                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "VDMS " + vdmsId + " Updated Successfully", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Unauthorised Access", "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> inviteUnregisteredVendorByEmail(String email, String vdmsId, DockerDTO dockerdto, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, Email:{},DockerDTO:{}", vdmsId, email, dockerdto);
        if (vdmsId != null && email != null && dockerdto != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                UserDTO userdto = userService.getMasterUserInfoByVdmsId(dockerdto.getVdms_id(), httpServletRequest);
                log.info("User DTO:{}", userdto);
                if (userdto != null) {
                    VdmsDTO vdmsdto = vdmsService.getVdmsInfoByVdmsId(dockerdto.getVdms_id(), httpServletRequest);
                    log.info("Vdms DTO:{}", vdmsdto);
                    if (vdmsdto != null) {

                        JSONObject jsonObject = new JSONObject();

                        if (dockerdto.getNetwork_name() != null) {
                            jsonObject.put("networkName", dockerdto.getNetwork_name());
                        } else {
                            jsonObject.put("networkName", dockerdto.getName());
                        }

                        jsonObject.put("to", dockerdto.getVendor_email());
                        jsonObject.put("vendorName", dockerdto.getVendor_email());
                        jsonObject.put("propertyOwnerName", userdto.getName());
                        jsonObject.put("vdmsId", vdmsdto.getVdms_id());
                        jsonObject.put("systemType", dockerdto.getSystem_type());
                        jsonObject.put("propertyName", vdmsdto.getProperty_name());
                        jsonObject.put("vdmsAddress", getVdmsAddress(vdmsdto, httpServletRequest));
                        jsonObject.put("userEmail", userdto.getEmail());
                        jsonObject.put("userPhone", userdto.getPhone());
                        ResponseEntity<ResponseDTO> alertServerResponse = webClientService.inviteUnregisteredVendorByEmail(jsonObject, httpServletRequest);
                        log.info("Response from Alert:{}", alertServerResponse);
                        log.info("Successfully Invite UnRegistered Vendor By Email.EndPoint:{}", httpServletRequest.getRequestURI());
                        if (alertServerResponse.getStatusCode() == HttpStatus.OK) {
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Successfully Invited Unregistered Vendor By E-Mail:" + email, "success");
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            ResponseDTO responseDTO = alertServerResponse.getBody();
                            log.error("Server Error!!!,EndPoint:{}", httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Server Error", "failed");
                            throw new ServerException(responseDTO.getData().toString(), 812, httpServletRequest.getRequestURI());
                        }
                    } else {
                        log.error("VDMS does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "VDMS Does Not Exist", "failed");
                        throw new ClientException("VDMS does not exist", 728, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("User not found.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "User Not Found", "failed");
                    throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Unauthorised Access", "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public String getVdmsAddress(VdmsDTO vdmsdto, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsDTO:{}", vdmsdto);
        String address = "";
        if (vdmsdto.getAddress() != null) {
            address = address + vdmsdto.getAddress();
        }
        if (vdmsdto.getCity() != null) {
            address = address + ", " + vdmsdto.getCity();
        }
        if (vdmsdto.getState() != null) {
            address = address + ", " + vdmsdto.getState();
        }
        if (vdmsdto.getCountry() != null) {
            address = address + ", " + vdmsdto.getCountry();
        }
        if (vdmsdto.getZip() != null) {
            address = address + ", " + vdmsdto.getZip();
        }
        log.info("Fetching VDMS Address,EndPoint:{}", httpServletRequest.getRequestURI());
        return address;
    }

    public ResponseEntity<?> inviteRegisteredVendor(String email, String vdmsId, DockerDTO dockerdto, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, Email:{},DockerDTO:{}", vdmsId, email, dockerdto);
        if (vdmsId != null && email != null && dockerdto != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                ResponseDTO response = webClientService.getVendorDetailsByEmailForTouchScreen(dockerdto.getVendor_email(), dockerdto.getVendor_org_id(), vdmsId, httpServletRequest);
                log.info("Response from Vendor : {}", response);
                String jsonString = JSON.toJSONString(response.getData());
                log.info("jsonString:{}", jsonString);
                UserDTO vendorDTO = JSON.parseObject(jsonString, UserDTO.class);
                log.info("vendorDTO:{}", vendorDTO);
                if (vendorDTO != null) {
                    UserDTO userdto = userService.getMasterUserInfoByVdmsId(dockerdto.getVdms_id(), httpServletRequest);
                    log.info("userDTO : {}", userdto);
                    if (userdto != null) {
                        VdmsDTO vdmsdto = vdmsService.getVdmsInfoByVdmsId(dockerdto.getVdms_id(), httpServletRequest);
                        log.info("vdmsDTO : {}", vdmsdto);
                        if (vdmsdto != null) {

                            JSONObject jsonObject = new JSONObject();

                            if (dockerdto.getNetwork_name() != null) {
                                jsonObject.put("networkName", dockerdto.getNetwork_name());
                            } else {
                                jsonObject.put("networkName", dockerdto.getName());
                            }

                            jsonObject.put("to", dockerdto.getVendor_email());
                            jsonObject.put("vendorName", vendorDTO.getName());
                            jsonObject.put("vendorExtension", vendorDTO.getValue());
                            jsonObject.put("vendorPhone", vendorDTO.getPhone());
                            jsonObject.put("propertyOwnerName", userdto.getName());
                            jsonObject.put("vdmsId", vdmsdto.getVdms_id());
                            jsonObject.put("systemType", dockerdto.getSystem_type());
                            jsonObject.put("propertyName", vdmsdto.getProperty_name());
                            jsonObject.put("vdmsAddress", getVdmsAddress(vdmsdto, httpServletRequest));
                            jsonObject.put("userEmail", userdto.getEmail());
                            jsonObject.put("userExtension", userdto.getValue());
                            jsonObject.put("userPhone", userdto.getPhone());
                            ResponseEntity<ResponseDTO> alertServerResponse = webClientService.inviteRegisteredVendor(jsonObject, httpServletRequest);
                            log.info("Response from Alert:{}", alertServerResponse);
                            log.info("Successfully Invite Registered Vendor.EndPoint:{}", httpServletRequest.getRequestURI());
                            if (alertServerResponse.getStatusCode() == HttpStatus.OK) {
                                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Successfully Invited Registered Vendor By E-Mail:" + email, "success");
                                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                            } else {
                                ResponseDTO responseDTO = alertServerResponse.getBody();
                                log.error("Server Error Occurred:EndPoint:{}", httpServletRequest.getRequestURI());
                                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Server Error", "failed");
                                throw new ServerException(responseDTO.getData().toString(), 812, httpServletRequest.getRequestURI());
                            }
                        } else {
                            log.error("VDMS does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "VDMS Does Not Exist", "failed");
                            throw new ClientException("VDMS does not exist", 728, httpServletRequest.getRequestURI());
                        }
                    } else {
                        log.error("Master User not found.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Master User Not Found", "failed");
                        throw new ClientException("Master User not found", 701, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("Vendor not found.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Vendor Not Found", "failed");
                    throw new ClientException("Vendor not found", 701, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Unauthorised Access", "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> unTagVendorByMasterUser(String vdmsId, String name, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId: {}, dockerName: {}", vdmsId, name);
        if (vdmsId != null && name != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                dockerRepository.unTagVendorByMasterUserEmail(vdmsId, name);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("UnTag Vendor By Master User Email.Vdms_Id:{},Name:{},EndPoint:{}", vdmsId, name, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Successfully Untagged Vendor By Master-User E-Mail:" + name, "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Unauthorised Access", "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

//    public ResponseEntity<?> syncFloorMapImageByFloorId(String vdmsId, List<JSONObject> body, HttpServletRequest httpServletRequest) throws IOException {
//        log.info("Payload: VdmsId: {}, FloorObject: {}", vdmsId, body);
//        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
//        if (authorized) {
//            final String floorImageDirectory = String.format(resourceUrlConfig.getFloorImageDirectory(), vdmsId);
//            final String floorImageUrl = String.format(resourceUrlConfig.getFloorImageUrl(), vdmsId);
//
//            if (body != null && body.size() > 0) {
//                int count = 0;
//                for (JSONObject item : body) {
//                    URL documentUrl = new URL(item.getString("image_url"));
//                    HttpURLConnection huc = (HttpURLConnection) documentUrl.openConnection();
//                    int responseCode = huc.getResponseCode();
//                    if (responseCode == 200) {
//                        InputStream inputstream = documentUrl.openStream();
//                        ByteArrayResource byteArrayResource = new ByteArrayResource(inputstream.readAllBytes());
//                        if (awsService.checkFileExist(floorImageDirectory + awsService.getFileNameByImageUrl(item.getString("image_url"), httpServletRequest), httpServletRequest)) {
//                            String fileName = awsService.getFileNameByImageUrl(item.getString("local_image_url"), httpServletRequest);
//                            awsService.removeFileFromAWSS3(floorImageDirectory, fileName, httpServletRequest);
//                        }
//                        String imageUrl = awsService.floorSync(byteArrayResource.getByteArray(), floorImageDirectory,
//                                floorImageUrl, awsService.getFileNameByImageUrl(item.getString("local_image_url"), httpServletRequest), httpServletRequest);
//                        body.get(count).put("image_url", imageUrl);
//                    } else {
//                        body.get(count).put("image_url", null);
//                    }
//                    count++;
//                }
//                ResponseDTO responseDTO = ScleraUtils.generatePayload(body, 200, true);
//                log.info("Sync Floor Map Image By Floor_Id,Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
//                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            } else {
//                log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
//                throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
//            }
//        } else {
//            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
//            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
//        }
//    }

    public ResponseEntity<?> addFloorMapsByFloorId(String vdmsId, List<MultipartFile> floorMap, String body, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: VdmsId: {}, FloorObject: {}", vdmsId, body);
        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
        if (authorized) {
            final String floorImageDirectory = String.format(resourceUrlConfig.getFloorImageDirectory(), vdmsId);

            final String floorImageUrl = String.format(resourceUrlConfig.getFloorImageUrl(), vdmsId);

            body = UriUtils.decode(body, StandardCharsets.UTF_8);
            List<JSONObject> floorObjects = JSON.parseArray(body, JSONObject.class);

            if (floorMap != null && !floorMap.isEmpty()) {
                for (JSONObject floorObject : floorObjects) {
                    MultipartFile floorMapImage = floorMap.get(0);

                    String imageExtension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(floorMapImage.getOriginalFilename()), httpServletRequest);
                    String imageUrl = awsService.addFileToAWSS3(floorMapImage.getBytes(), floorImageDirectory, floorImageUrl, imageExtension, floorObject.getString("floor_id"), httpServletRequest);
                    log.info("Floor image added to s3 bucket...");
                    floorObject.put("image_url", imageUrl);

                    String floorId = floorObject.getString("floor_id");
                    String floorTilesDirectory = String.format(resourceUrlConfig.getFloorTilesDirectory(), vdmsId, floorId);
                    String s3Url = awsService.constructS3URL(vdmsId, imageUrl);


                    String ulx = floorObject.getString("topleft_longitude");
                    String uly = floorObject.getString("topleft_latitude");
                    String llx = floorObject.getString("bottomright_longitude");
                    String lly = floorObject.getString("bottomright_latitude");

                    log.info("ulx:" + ulx);
                    log.info("uly:" + uly);
                    log.info("llx:" + llx);
                    log.info("lly:" + lly);

                    String sanitizedUlx = ulx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
                    String sanitizedUly = uly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
                    String sanitizedLlx = llx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
                    String sanitizedLly = lly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
                    log.info("Generating tile started...");

                    log.info("sanitizedUlx:" + sanitizedUlx);
                    log.info("sanitizedUly:" + sanitizedUly);
                    log.info("sanitizedLlx:" + sanitizedLlx);
                    log.info("sanitizedLly:" + sanitizedLly);


                    JSONObject lambdaResponse = awsService.invokeScleraTiles(s3Url, floorTilesDirectory, floorId, ulx, uly, llx, lly);

                    if (lambdaResponse != null && !"Runtime.ExitError".equals(lambdaResponse.getString("errorType"))) {
                        log.info("LAMBDA-RESPONSE : {}", lambdaResponse);
                        if (lambdaResponse.getInteger("statusCode") == 200) {
                            JSONObject responseBody = lambdaResponse.getJSONObject("body");
                            log.info("Floor Map tiled Successfully ...");
                            log.info("RES-BODY : {}", responseBody);

                            floorObject.put("min_zoom", responseBody.getIntValue("min_zoom"));
                            floorObject.put("max_zoom", responseBody.getIntValue("max_zoom"));
                            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "ADD", "Successfully Added Floor Id: " + floorId + " For VDMS:" + vdmsId, "success");
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(floorObjects, 200, true);
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "ADD", "Unable to tile Image", "failed");
                        }
                    } else {
                        userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "ADD", "Unable to tile Image", "failed");
                    }

                }

            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "ADD", "Unauthorised Access", "failed");
            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> uploadFloorMapsByFloorId(String vdmsId, List<JSONObject> body, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: VdmsId: {}, FloorObject: {}", vdmsId, body);
        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
        if (authorized) {
            final String floorImageDirectory = String.format(resourceUrlConfig.getFloorImageDirectory(), vdmsId);

            List<JSONObject> responses = new ArrayList<>();

            for (JSONObject jsonObject : body) {
                String imageUrl = jsonObject.getString("image_url");


                awsService.removeFolderFromAWSS3(floorImageDirectory + jsonObject.getString("floor_id") + "/", httpServletRequest);

                String floorId = jsonObject.getString("floor_id");
                String floorTilesDirectory = String.format(resourceUrlConfig.getFloorTilesDirectory(), vdmsId, floorId);
                String s3Url = awsService.constructS3URL(vdmsId, imageUrl);


                String ulx = jsonObject.getString("topleft_longitude");
                String uly = jsonObject.getString("topleft_latitude");
                String llx = jsonObject.getString("bottomright_longitude");
                String lly = jsonObject.getString("bottomright_latitude");

                log.info("ulx:" + ulx);
                log.info("uly:" + uly);
                log.info("llx:" + llx);
                log.info("lly:" + lly);

                String sanitizedUlx = ulx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
                String sanitizedUly = uly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
                String sanitizedLlx = llx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
                String sanitizedLly = lly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
                log.info("Generating tile started...");

                log.info("sanitizedUlx:" + sanitizedUlx);
                log.info("sanitizedUly:" + sanitizedUly);
                log.info("sanitizedLlx:" + sanitizedLlx);
                log.info("sanitizedLly:" + sanitizedLly);


                JSONObject lambdaResponse = awsService.invokeScleraTiles(s3Url, floorTilesDirectory, floorId, ulx, uly, llx, lly);

                if (lambdaResponse != null && !"Runtime.ExitError".equals(lambdaResponse.getString("errorType"))) {
                    log.info("LAMBDA-RESPONSE : {}", lambdaResponse);
                    if (lambdaResponse.getInteger("statusCode") == 200) {
                        JSONObject responseBody = lambdaResponse.getJSONObject("body");
                        log.info("Floor Map tiled Successfully ...");
                        log.info("RES-BODY : {}", responseBody);

                        jsonObject.put("min_zoom", responseBody.getIntValue("min_zoom"));
                        jsonObject.put("max_zoom", responseBody.getIntValue("max_zoom"));

                        responses.add(jsonObject);

                        userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Successfully Updated Floor Id: " + floorId + " For VDMS:" + vdmsId, "success");
                    } else {
                        userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Failed Updated Floor Id: " + floorId + " For VDMS:" + vdmsId, "failed");
                    }
                } else {
                    userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Failed Updated Floor Id: " + floorId + " For VDMS:" + vdmsId, "failed");
                }

            }
            log.info("upload Floor Maps By Floor_Id,Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
            ResponseDTO responseDTO = ScleraUtils.generatePayload(responses, 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Unauthorised Access", "failed");
            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateFloorMapTilesByFloorId(String vdmsId, String floorId, JSONObject floor, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: VdmsId: {}, floorId: {}, FloorObject: {}", vdmsId, floorId, floor);
        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
        if (authorized) {
            final String floorImageDirectory = String.format(resourceUrlConfig.getFloorImageDirectory(), vdmsId);


            String imageUrl = floor.getString("image_url");


            awsService.removeFolderFromAWSS3(floorImageDirectory + floor.getString("floor_id") + "/", httpServletRequest);


            String floorTilesDirectory = String.format(resourceUrlConfig.getFloorTilesDirectory(), vdmsId, floorId);
            String s3Url = awsService.constructS3URL(vdmsId, imageUrl);


            String ulx = floor.getString("topleft_longitude");
            String uly = floor.getString("topleft_latitude");
            String llx = floor.getString("bottomright_longitude");
            String lly = floor.getString("bottomright_latitude");

            log.info("ulx:" + ulx);
            log.info("uly:" + uly);
            log.info("llx:" + llx);
            log.info("lly:" + lly);

            String sanitizedUlx = ulx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
            String sanitizedUly = uly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
            String sanitizedLlx = llx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
            String sanitizedLly = lly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
            log.info("Generating tile started...");

            log.info("sanitizedUlx:" + sanitizedUlx);
            log.info("sanitizedUly:" + sanitizedUly);
            log.info("sanitizedLlx:" + sanitizedLlx);
            log.info("sanitizedLly:" + sanitizedLly);


            JSONObject lambdaResponse = awsService.invokeScleraTiles(s3Url, floorTilesDirectory, floorId, ulx, uly, llx, lly);

            if (lambdaResponse != null && !"Runtime.ExitError".equals(lambdaResponse.getString("errorType"))) {
                log.info("LAMBDA-RESPONSE : {}", lambdaResponse);
                if (lambdaResponse.getInteger("statusCode") == 200) {
                    JSONObject responseBody = lambdaResponse.getJSONObject("body");
                    log.info("Floor Map tiled Successfully ...");
                    log.info("RES-BODY : {}", responseBody);

                    floor.put("min_zoom", responseBody.getIntValue("min_zoom"));
                    floor.put("max_zoom", responseBody.getIntValue("max_zoom"));
                    userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Successfully Updated Floor Id: " + floorId + " For VDMS:" + vdmsId, "success");
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(floor, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Failed Updated Floor Id: " + floorId + " For VDMS:" + vdmsId, "failed");
                }
            } else {
                userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Failed Updated Floor Id: " + floorId + " For VDMS:" + vdmsId, "failed");
            }


            ResponseDTO responseDTO = ScleraUtils.generatePayload(floor, 200, true);
            log.info("Update Floor Map Tiles By Floor_Id:{},Vdms_Id:{},EndPoint:{}", floorId, vdmsId, httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Successfully Updated Floor Id: " + floorId + " For VDMS:" + vdmsId, "success");
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Unauthorised Access", "failed");
            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
        }
    }


//    public ResponseEntity<?> addFloorMapsByFloorId(String vdmsId, List<MultipartFile> floorMap, String body, HttpServletRequest httpServletRequest) throws IOException {
//        log.info("Payload: VdmsId: {}, FloorObject: {}", vdmsId, body);
//        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
//        if (authorized) {
//            final String floorImageDirectory = String.format(resourceUrlConfig.getFloorImageDirectory(), vdmsId);
//
//            final String floorImageUrl = String.format(resourceUrlConfig.getFloorImageUrl(), vdmsId);
//
//            body = UriUtils.decode(body, StandardCharsets.UTF_8);
//            List<JSONObject> floorObjects = JSON.parseArray(body, JSONObject.class);
//
//            if (floorMap != null && !floorMap.isEmpty()) {
//                for (JSONObject floorObject : floorObjects) {
//                    MultipartFile floorMapImage = floorMap.get(0);
//
//                    String imageExtension = awsService.getFileExtensionByImageUrl(Objects.requireNonNull(floorMapImage.getOriginalFilename()), httpServletRequest);
//                    String imageUrl = awsService.addFileToAWSS3(floorMapImage.getBytes(), floorImageDirectory, floorImageUrl, imageExtension, floorObject.getString("floor_id"), httpServletRequest);
//                    log.info("Floor image added to s3 bucket...");
//                    floorObject.put("image_url", imageUrl);
//
//                    final String UPLOAD_DIRECTORY = "/tmp/sclera/";
//                    String floorId = floorObject.getString("floor_id");
//                    String imageName = floorId + "." + imageExtension;
//                    String sanitizedFileName = imageName.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//                    Path imageDirectory = Paths.get(UPLOAD_DIRECTORY, floorId, sanitizedFileName);
//                    log.info("imageDirectory:" + imageDirectory);
//
//                    if (!imageDirectory.normalize().startsWith(UPLOAD_DIRECTORY)) {
//                        throw new IOException("Could not upload file: " + floorId);
//                    }
//
//                    Path folderPathName = Paths.get(UPLOAD_DIRECTORY, floorId);
//                    log.info("folderPathName:" + folderPathName);
//
//                    if (!folderPathName.normalize().startsWith(UPLOAD_DIRECTORY)) {
//                        throw new IOException("Could not upload file: " + floorId);
//                    }
//
//
//                    File folder = new File(folderPathName.toString());
//                    FileSystemUtils.deleteRecursively(folder);
//                    folder.mkdir();
//                    log.info("Floor Folder Created in local path...");
//
//
//                    Files.write(imageDirectory, floorMapImage.getBytes());
//                    log.info("Floor image added to local path...");
//
//
//                    String ulx = floorObject.getString("topleft_longitude");
//                    String uly = floorObject.getString("topleft_latitude");
//                    String llx = floorObject.getString("bottomright_longitude");
//                    String lly = floorObject.getString("bottomright_latitude");
//
//                    log.info("ulx:" + ulx);
//                    log.info("uly:" + uly);
//                    log.info("llx:" + llx);
//                    log.info("lly:" + lly);
//
//                    String sanitizedUlx = ulx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//                    String sanitizedUly = uly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//                    String sanitizedLlx = llx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//                    String sanitizedLly = lly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//                    log.info("Generating tile started...");
//
//                    log.info("sanitizedUlx:" + sanitizedUlx);
//                    log.info("sanitizedUly:" + sanitizedUly);
//                    log.info("sanitizedLlx:" + sanitizedLlx);
//                    log.info("sanitizedLly:" + sanitizedLly);
//
//                    String[] tileCmd = {"./createImageTiles.sh", "-file", imageDirectory.toString(), "-ulx", sanitizedUlx, "-uly", sanitizedUly, "-llx", sanitizedLlx, "-lly", sanitizedLly};
//                    commandLineService.execCommand(floorObject.getString("floor_id"), tileCmd, httpServletRequest);
//
//                    log.info("Generating tile Completed...");
//
//                    File[] directories = new File(folderPathName.toString()).listFiles(File::isDirectory);
//                    List<String> list = new ArrayList<>();
//                    if (directories != null)
//                        for (File content : directories) {
//                            list.add(content.getName());
//                        }
//
//                    floorObject.put("min_zoom", Collections.min(list));
//                    floorObject.put("max_zoom", Collections.max(list));
//                    log.info("min and max is calculated and added to object");
//
//                    awsService.addFolderToAWSS3(folderPathName.toString(), floorImageDirectory, floorObject.getString("floor_id"), httpServletRequest);
//                    log.info("Add Floor Map By Floor_Id,Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
//                    FileSystemUtils.deleteRecursively(folder);
//                    log.info("Image and Tiled imaged Removed from local system...");
//                    userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "ADD", "Successfully Added Floor Id: " + floorId + " For VDMS:" + vdmsId, "success");
//                }
//                ResponseDTO responseDTO = ScleraUtils.generatePayload(floorObjects, 200, true);
//                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//            }
//            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
//            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//        } else {
//            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
//            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "ADD", "Unauthorised Access", "failed");
//            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
//        }
//    }
//
//    public ResponseEntity<?> uploadFloorMapsByFloorId(String vdmsId, List<JSONObject> body, HttpServletRequest httpServletRequest) throws IOException {
//        log.info("Payload: VdmsId: {}, FloorObject: {}", vdmsId, body);
//        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
//        if (authorized) {
//            final String floorImageDirectory = String.format(resourceUrlConfig.getFloorImageDirectory(), vdmsId);
//
//            for (JSONObject jsonObject : body) {
//                String imageUrl = jsonObject.getString("image_url");
//                S3Object obj = awsService.getObjectFromAwsS3(floorImageDirectory, awsService.getFileNameByImageUrl(imageUrl, httpServletRequest), httpServletRequest);
//                byte[] byteArrayResource = IOUtils.toByteArray(obj.getObjectContent());
//                String imageExtension = awsService.getFileExtensionByImageUrl(imageUrl, httpServletRequest);
//
//
//                awsService.removeFolderFromAWSS3(floorImageDirectory + jsonObject.getString("floor_id") + "/", httpServletRequest);
//
//                final String UPLOAD_DIRECTORY = "/tmp/sclera/";
//                String floorId = jsonObject.getString("floor_id");
//                String imageName = floorId + "." + imageExtension;
//                String sanitizedFileName = imageName.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//                Path imageDirectory = Paths.get(UPLOAD_DIRECTORY, floorId, sanitizedFileName);
//                log.info("imageDirectory:" + imageDirectory);
//
//                if (!imageDirectory.normalize().startsWith(UPLOAD_DIRECTORY)) {
//                    throw new IOException("Could not upload file: " + floorId);
//                }
//
//                Path folderPathName = Paths.get(UPLOAD_DIRECTORY, floorId);
//                log.info("folderPathName:" + folderPathName);
//
//                if (!folderPathName.normalize().startsWith(UPLOAD_DIRECTORY)) {
//                    throw new IOException("Could not upload file: " + floorId);
//                }
//
//
//                File folder = new File(folderPathName.toString());
//                FileSystemUtils.deleteRecursively(folder);
//                folder.mkdir();
//                log.info("Floor Folder Created in local path...");
//
//
//                Files.write(imageDirectory, byteArrayResource);
//                log.info("Floor image added to local path...");
//
//                String ulx = jsonObject.getString("topleft_longitude");
//                String uly = jsonObject.getString("topleft_latitude");
//                String llx = jsonObject.getString("bottomright_longitude");
//                String lly = jsonObject.getString("bottomright_latitude");
//
//                String sanitizedUlx = ulx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//                String sanitizedUly = uly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//                String sanitizedLlx = llx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//                String sanitizedLly = lly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//
//                log.info("Generating tile started...");
//                String[] tileCmd = {"./createImageTiles.sh", "-file", imageDirectory.toString(), "-ulx", sanitizedUlx, "-uly", sanitizedUly, "-llx", sanitizedLlx, "-lly", sanitizedLly};
//                commandLineService.execCommand(jsonObject.getString("floor_id"), tileCmd, httpServletRequest);
//                log.info("Generating tile Completed...");
//
//                File[] directories = new File(folderPathName.toString()).listFiles(File::isDirectory);
//                List<String> list = new ArrayList<>();
//                if (directories != null)
//                    for (File content : directories) {
//                        list.add(content.getName());
//                    }
//
//                jsonObject.put("min_zoom", Collections.min(list));
//                jsonObject.put("max_zoom", Collections.max(list));
//                log.info("min and max is calculated and added to object");
//
//                awsService.addFolderToAWSS3(folderPathName.toString(), floorImageDirectory, jsonObject.getString("floor_id"), httpServletRequest);
//
//                FileSystemUtils.deleteRecursively(folder);
//                log.info("Image and Tiled imaged Removed from local system...");
//                userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Successfully Updated Floor Id: " + floorId + " For VDMS:" + vdmsId, "success");
//            }
//            log.info("upload Floor Maps By Floor_Id,Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
//            ResponseDTO responseDTO = ScleraUtils.generatePayload(body, 200, true);
//            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//        } else {
//            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
//            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Unauthorised Access", "failed");
//            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
//        }
//    }
//
//
//    public ResponseEntity<?> updateFloorMapTilesByFloorId(String vdmsId, String floorId, JSONObject floor, HttpServletRequest httpServletRequest) throws IOException {
//        log.info("Payload: VdmsId: {}, floorId: {}, FloorObject: {}", vdmsId, floorId, floor);
//        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
//        if (authorized) {
//            final String floorImageDirectory = String.format(resourceUrlConfig.getFloorImageDirectory(), vdmsId);
//
//            S3Object obj = awsService.getObjectFromAwsS3(floorImageDirectory, awsService.getFileNameByImageUrl(floor.getString("floorImageUrl"), httpServletRequest), httpServletRequest);
//            byte[] byteArrayResource = IOUtils.toByteArray(obj.getObjectContent());
//            String imageExtension = awsService.getFileExtensionByImageUrl(floor.getString("floorImageUrl"), httpServletRequest);
//
//            awsService.removeFolderFromAWSS3(floorImageDirectory + floorId + "/", httpServletRequest);
//
//            final String UPLOAD_DIRECTORY = "/tmp/sclera/";
//            String imageName = floorId + "." + imageExtension;
//            String sanitizedFileName = imageName.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//            Path imageDirectory = Paths.get(UPLOAD_DIRECTORY, floorId, sanitizedFileName);
//            log.info("imageDirectory:" + imageDirectory);
//
//            if (!imageDirectory.normalize().startsWith(UPLOAD_DIRECTORY)) {
//                throw new IOException("Could not upload file: " + floorId);
//            }
//
//            Path folderPathName = Paths.get(UPLOAD_DIRECTORY, floorId);
//            log.info("folderPathName:" + folderPathName);
//
//            if (!folderPathName.normalize().startsWith(UPLOAD_DIRECTORY)) {
//                throw new IOException("Could not upload file: " + floorId);
//            }
//
//
//            File folder = new File(folderPathName.toString());
//            FileSystemUtils.deleteRecursively(folder);
//            folder.mkdir();
//            log.info("Floor Folder Created in local path...");
//
//
//            Files.write(imageDirectory, byteArrayResource);
//            log.info("Floor image added to local path...");
//
//            String ulx = floor.getString("topleft_longitude");
//            String uly = floor.getString("topleft_latitude");
//            String llx = floor.getString("bottomright_longitude");
//            String lly = floor.getString("bottomright_latitude");
//
//            String sanitizedUlx = ulx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//            String sanitizedUly = uly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//            String sanitizedLlx = llx.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//            String sanitizedLly = lly.replaceAll("[^a-zA-Z0-9.%_:-]", "");
//
//            log.info("Generating tile started...");
//            String[] tileCmd = {"./createImageTiles.sh", "-file", imageDirectory.toString(), "-ulx", sanitizedUlx, "-uly", sanitizedUly, "-llx", sanitizedLlx, "-lly", sanitizedLly};
//            commandLineService.execCommand(floorId, tileCmd, httpServletRequest);
//            log.info("Generating tile Completed...");
//
//            File[] directories = new File(folderPathName.toString()).listFiles(File::isDirectory);
//            List<String> list = new ArrayList<>();
//            if (directories != null)
//                for (File content : directories) {
//                    list.add(content.getName());
//                }
//
//            floor.put("min_zoom", Collections.min(list));
//            floor.put("max_zoom", Collections.max(list));
//            log.info("min and max is calculated and added to object");
//
//            awsService.addFolderToAWSS3(folderPathName.toString(), floorImageDirectory, floorId, httpServletRequest);
//
//            FileSystemUtils.deleteRecursively(folder);
//            log.info("Image and Tiled imaged Removed from local system...");
//            ResponseDTO responseDTO = ScleraUtils.generatePayload(floor, 200, true);
//            log.info("Update Floor Map Tiles By Floor_Id:{},Vdms_Id:{},EndPoint:{}", floorId, vdmsId, httpServletRequest.getRequestURI());
//            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Successfully Updated Floor Id: " + floorId + " For VDMS:" + vdmsId, "success");
//            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//        } else {
//            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
//            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "UPDATE", "Unauthorised Access", "failed");
//            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
//        }
//    }


//    public ResponseEntity<?> syncFloorMapsByFloorId(String vdmsId, List<JSONObject> body, HttpServletRequest httpServletRequest) throws IOException {
//        log.info("Payload: VdmsId: {}, FloorObject: {}", vdmsId, body);
//        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
//        if (authorized) {
//            final String floorImageDirectory = String.format(resourceUrlConfig.getFloorImageDirectory(), vdmsId);
//
//            for (JSONObject jsonObject : body) {
//                String fileName = awsService.getFileNameByImageUrl(jsonObject.getString("image_url"), httpServletRequest);
//                Boolean isFileExist = awsService.checkFileExist(floorImageDirectory + fileName, httpServletRequest);
//
//                if (isFileExist) {
//
//                    S3Object obj = awsService.getObjectFromAwsS3(floorImageDirectory, fileName, httpServletRequest);
//                    byte[] byteArrayResource = IOUtils.toByteArray(obj.getObjectContent());
//                    String imageExtension = awsService.getFileExtensionByImageUrl(jsonObject.getString("image_url"), httpServletRequest);
//                    String imageUrl = jsonObject.getString("image_url");
//
//                    awsService.removeFolderFromAWSS3(floorImageDirectory + jsonObject.getString("floor_id") + "/", httpServletRequest);
//
//                    log.info(jsonObject.getString("floor_id"));
//                    String imagePath = "/tmp/sclera/" + jsonObject.getString("floor_id") + "." + imageExtension;
//                    Path path = Paths.get(imagePath);
//                    Files.write(path, byteArrayResource);
//                    log.info("Floor image added to local path...");
//
//                    jsonObject.put("image_url", imageUrl);
//
//                    String folderPath = "/tmp/sclera/" + jsonObject.getString("floor_id");
//                    File folder = new File(folderPath);
//
//                    String ulx = jsonObject.getString("topleft_longitude");
//                    String uly = jsonObject.getString("topleft_latitude");
//                    String llx = jsonObject.getString("bottomright_longitude");
//                    String lly = jsonObject.getString("bottomright_latitude");
//
//                    log.info("Generating tile started...");
//                    String[] tileCmd = {"./createImageTiles.sh", "-file", imagePath, "-ulx", ulx, "-uly", uly, "-llx", llx, "-lly", lly};
//                    commandLineService.execCommand(jsonObject.getString("floor_id"), tileCmd, httpServletRequest);
//                    log.info("Generating tile Completed...");
//
//                    File[] directories = new File(folderPath).listFiles(File::isDirectory);
//                    List<String> list = new ArrayList<>();
//                    if (directories != null)
//                        for (File content : directories) {
//                            list.add(content.getName());
//                        }
//
//                    jsonObject.put("min_zoom", Collections.min(list));
//                    jsonObject.put("max_zoom", Collections.max(list));
//                    log.info("min and max is calculated and added to object");
//
//                    awsService.addFolderToAWSS3(folderPath, floorImageDirectory, jsonObject.getString("floor_id"), httpServletRequest);
//                    FileSystemUtils.deleteRecursively(folder);
//                    log.info("Image and Tiled imaged Removed from local system...");
//                } else {
//                    jsonObject.put("image_url", null);
//                    jsonObject.put("min_zoom", null);
//                    jsonObject.put("max_zoom", null);
//                }
//            }
//            log.info("Sync Floor Map By Floor_Id,Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
//            ResponseDTO responseDTO = ScleraUtils.generatePayload(body, 200, true);
//            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
//        } else {
//            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
//            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
//        }
//    }

    public ResponseEntity<?> deleteFloorMapsByFloorId(String vdmsId, List<JSONObject> floorObjects, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, FloorObject: {}", vdmsId, floorObjects);
        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
        if (authorized) {
            final String floorImageDirectory = String.format(resourceUrlConfig.getFloorImageDirectory(), vdmsId);
            for (JSONObject floorObject : floorObjects) {
                if (floorObject.containsKey("image_url")) {
                    awsService.removeFolderFromAWSS3(floorImageDirectory + floorObject.getString("floor_id"), httpServletRequest);
                    userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "DELETE", "Successfully Deleted Floor Id: " + floorObject.getString("floor_id") + " For VDMS:" + vdmsId, "success");
                }
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Images deleted successfully", 200, true);
            log.info("Delete Floor Maps By Floor_Id,Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "Floor-Map", "DELETE", "Unauthorised Access", "failed");
            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> updateVdmsTranfer(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}", vdmsId);
        if (vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                vdmsRepository.updateVdmsTranfer(vdmsId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Update Vdms transfer.Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Successfully Updated VDMS Transfer", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Unauthorised Access", "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getCustomerOrgIdByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}", vdmsId);
        if (vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                String customerOrgId = vdmsRepository.getCustomerOrgIdByVdmsId(vdmsId);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(customerOrgId, 200, true);
                log.info("Fetching Customer org Id By Vdms_Id:{},EndPoint:{}", vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getAccessUrlForFileUpload(String vdmsId, String fileName, HttpServletRequest httpServletRequest) throws Exception {
        log.info("Payload: VdmsId: {}", vdmsId);
        if (vdmsId != null) {
            boolean authorized = checkVdmsIdAndClinetId(vdmsId, httpServletRequest);
            if (authorized) {
                String url;
                int expirationMinutes = 30; //valid for 5minutes
                if (fileName != null) {
                    url = awsService.getPreSignedUrlForFileUpload(resourceUrlConfig.getVdmsBackupDirectory() + vdmsId + "/backup/" + fileName, expirationMinutes, HttpMethod.PUT);
                } else {
                    BigInteger timeStamp = BigInteger.valueOf(System.currentTimeMillis());
                    url = awsService.getPreSignedUrlForFileUpload(resourceUrlConfig.getVdmsBackupDirectory() + vdmsId + "/backup/" + timeStamp + ".tar.gz", expirationMinutes, HttpMethod.PUT);
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(url, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public boolean checkVdmsIdAndClinetId(String vdmsId, HttpServletRequest httpServletRequest) {
        String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);
        String token = authorizationHeader.substring("Bearer ".length());
        DecodedJWT decodedJWT = JWT.decode(token);
        String vdmsIdInToken = decodedJWT.getClaim("vdmsId").asString();
        String vdmsClientIdInToken = decodedJWT.getClaim("client_id").asString();
        String vdmsClientIdFromBffServer = webClientService.getVdmsClientIdByVdmsId(vdmsIdInToken, httpServletRequest);
        log.info("VDMS ID:{} , Client ID from bff Server :{}, VDMS ID in TOKEN :{} , Client ID in Token:{}", vdmsId, vdmsClientIdFromBffServer, vdmsIdInToken, vdmsClientIdInToken);
        return vdmsId.equals(vdmsIdInToken) && vdmsClientIdInToken.equals(vdmsClientIdFromBffServer);
    }

    public ResponseEntity<?> getNfcDetailsByVdmsId(String vdmsId, String type, HttpServletRequest httpServletRequest) {
        log.info("Payload : vdmsId {}, type:{} ", vdmsId, type);
        if (vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                List<NfcDTO> nfcDTOS = null;
                if (type.equals("device")) {
                    nfcDTOS = nfcRepository.getNfcDetailsByVdmsIdTaggedByDevice(vdmsId);


                    List<ClientNfcDTO> clientNfcDetails = clientNfcService.getClientNfcDetailsByVdmsIdTaggedByDevice(vdmsId);
                    if (!clientNfcDetails.isEmpty()) {
                        for (int i = 0; i < clientNfcDetails.size(); i++) {

                            NfcDTO clientNfcDTO = new NfcDTO();
                            clientNfcDTO.setId(clientNfcDetails.get(i).getNfc_id());
                            clientNfcDTO.setLocationId(clientNfcDetails.get(i).getLocationId());
                            clientNfcDTO.setDeviceId(clientNfcDetails.get(i).getDeviceId());
                            clientNfcDTO.setVdmsId(clientNfcDetails.get(i).getVdmsId());
                            clientNfcDTO.setCreatedBy(clientNfcDetails.get(i).getCreatedBy());
                            clientNfcDTO.setCreationTime(clientNfcDetails.get(i).getCreationTime());
                            nfcDTOS.add(clientNfcDTO);

                        }
                    }


                } else if (type.equals("location")) {
                    nfcDTOS = nfcRepository.getNfcDetailsByVdmsIdTaggedByLocation(vdmsId);


                    List<ClientNfcDTO> clientNfcDetails = clientNfcService.getClientNfcDetailsByVdmsIdTaggedByLocation(vdmsId);
                    if (!clientNfcDetails.isEmpty()) {
                        for (int i = 0; i < clientNfcDetails.size(); i++) {

                            NfcDTO clientNfcDTO = new NfcDTO();
                            clientNfcDTO.setId(clientNfcDetails.get(i).getNfc_id());
                            clientNfcDTO.setLocationId(clientNfcDetails.get(i).getLocationId());
                            clientNfcDTO.setDeviceId(clientNfcDetails.get(i).getDeviceId());
                            clientNfcDTO.setVdmsId(clientNfcDetails.get(i).getVdmsId());
                            clientNfcDTO.setCreatedBy(clientNfcDetails.get(i).getCreatedBy());
                            clientNfcDTO.setCreationTime(clientNfcDetails.get(i).getCreationTime());
                            nfcDTOS.add(clientNfcDTO);

                        }
                    }
                } else {
                    log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
                    throw new ClientException("Invalid client param : EndPoint ;{}", 700, httpServletRequest.getRequestURI());
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOS, 200, true);
                log.info("Fetching NFC info from db by vdmsId {} ,EndPoint: {}", vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param : EndPoint ;{}", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getNfcDetailsByDeviceIds(String vdmsId, List<String> taggedIds, String type, HttpServletRequest httpServletRequest) {
        log.info("Payload : taggedIds {} , vdmsId {}, type:{}", taggedIds, vdmsId, type);
        if (vdmsId != null && taggedIds != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                List<NfcDTO> nfcDTOS = null;
                if (type.equals("device")) {
                    nfcDTOS = nfcRepository.getNfcDetailsByDeviceIds(vdmsId, taggedIds);


                    List<ClientNfcDTO> clientNfcDetails = clientNfcService.getClientNfcDetailsByVdmsIdAndDeviceIds(vdmsId, taggedIds);
                    if (!clientNfcDetails.isEmpty()) {
                        for (int i = 0; i < clientNfcDetails.size(); i++) {

                            NfcDTO clientNfcDTO = new NfcDTO();
                            clientNfcDTO.setId(clientNfcDetails.get(i).getNfc_id());
                            clientNfcDTO.setLocationId(clientNfcDetails.get(i).getLocationId());
                            clientNfcDTO.setDeviceId(clientNfcDetails.get(i).getDeviceId());
                            clientNfcDTO.setVdmsId(clientNfcDetails.get(i).getVdmsId());
                            clientNfcDTO.setCreatedBy(clientNfcDetails.get(i).getCreatedBy());
                            clientNfcDTO.setCreationTime(clientNfcDetails.get(i).getCreationTime());
                            nfcDTOS.add(clientNfcDTO);

                        }
                    }


                } else if (type.equals("location")) {
                    nfcDTOS = nfcRepository.getNfcDetailsByLocationIds(vdmsId, taggedIds);


                    List<ClientNfcDTO> clientNfcDetails = clientNfcService.getClientNfcDetailsByVdmsIdAndLocationIds(vdmsId, taggedIds);
                    if (!clientNfcDetails.isEmpty()) {
                        for (int i = 0; i < clientNfcDetails.size(); i++) {

                            NfcDTO clientNfcDTO = new NfcDTO();
                            clientNfcDTO.setId(clientNfcDetails.get(i).getNfc_id());
                            clientNfcDTO.setLocationId(clientNfcDetails.get(i).getLocationId());
                            clientNfcDTO.setDeviceId(clientNfcDetails.get(i).getDeviceId());
                            clientNfcDTO.setVdmsId(clientNfcDetails.get(i).getVdmsId());
                            clientNfcDTO.setCreatedBy(clientNfcDetails.get(i).getCreatedBy());
                            clientNfcDTO.setCreationTime(clientNfcDetails.get(i).getCreationTime());
                            nfcDTOS.add(clientNfcDTO);

                        }
                    }


                } else {
                    log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
                    throw new ClientException("Invalid client param : EndPoint ;{}", 700, httpServletRequest.getRequestURI());
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOS, 200, true);
                log.info("Fetching NFC info from db ,EndPoint: {}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param : EndPoint ;{}", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getNfcIdsByVdmsIdAndType(String vdmsId, String type, HttpServletRequest httpServletRequest) {
        log.info("Payload : vdmsId {}, type:{}", vdmsId, type);
        if (vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                JSONObject jsonObject = new JSONObject();
                if (type.equals("device")) {
                    List<String> deviceIds = nfcRepository.getNfcIdsByVdmsIdAndDevice(vdmsId);

                    List<String> clientNfcDeviceIds = clientNfcService.getClientNfcIdsByVdmsIdAndDevice(vdmsId);
                    if (!clientNfcDeviceIds.isEmpty()) {
                        deviceIds.addAll(clientNfcDeviceIds);
                    }
                    jsonObject.put("deviceIds", deviceIds);
                } else if (type.equals("location")) {
                    List<String> locationIds = nfcRepository.getNfcIdsByVdmsIdAndlocation(vdmsId);

                    List<String> clientQrCodeLocationIds = clientNfcService.getClientNfcIdsByVdmsIdAndlocation(vdmsId);
                    if (!clientQrCodeLocationIds.isEmpty()) {
                        locationIds.addAll(clientQrCodeLocationIds);
                    }


                    jsonObject.put("locationIds", locationIds);
                } else {
                    List<String> deviceIds = nfcRepository.getNfcIdsByVdmsIdAndDevice(vdmsId);
                    List<String> locationIds = nfcRepository.getNfcIdsByVdmsIdAndlocation(vdmsId);

                    List<String> clientNfcDeviceIds = clientNfcService.getClientNfcIdsByVdmsIdAndDevice(vdmsId);
                    if (!clientNfcDeviceIds.isEmpty()) {
                        deviceIds.addAll(clientNfcDeviceIds);
                    }


                    List<String> clientQrCodeLocationIds = clientNfcService.getClientNfcIdsByVdmsIdAndlocation(vdmsId);
                    if (!clientQrCodeLocationIds.isEmpty()) {
                        locationIds.addAll(clientQrCodeLocationIds);
                    }

                    jsonObject.put("deviceIds", deviceIds);
                    jsonObject.put("locationIds", locationIds);
                }
                log.info("Fetching NFC info from db by vdmsId {} and Type {},EndPoint: {}", vdmsId, type, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(jsonObject, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param : EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param : EndPoint ;{}", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteNfcInfoByVdmsIdAndTaggedIds(String vdmsId, String type, List<String> taggedIds, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId: {} ,type: {}, taggedIds: {}", vdmsId, type, taggedIds);
        if (vdmsId != null && type != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                if (type.equals("device")) {
                    nfcRepository.deleteNfcInfoByDeviceIdsAndVdmsId(taggedIds, vdmsId);
                    clientNfcService.deleteClientNfcInfoByDeviceIdsAndVdmsId(taggedIds, vdmsId);
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "DELETE", "Successfully Deleted NFC Info(device)", "success");
                }
                if (type.equals("location")) {
                    nfcRepository.deleteNfcInfoByLocationIdsAndVdmsId(taggedIds, vdmsId);
                    clientNfcService.deleteClientNfcInfoByLocationIdsAndVdmsId(taggedIds, vdmsId);
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "DELETE", "Successfully Deleted NFC Info(location)", "success");
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Deleting NFC Info By VDMSId: {} And TaggedIDs: {},EndPoint: {}", vdmsId, taggedIds, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "DELETE", "Unauthorised Access", "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param,EndPoint: {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getQrCodeDetailsByTaggedType(String vdmsId, String taggedType, List<String> taggedIds, HttpServletRequest httpServletRequest) {
        log.info("Payload: TaggedType: {}, VdmsId:{}, TaggedIds: {}", taggedType, vdmsId, taggedIds);
        if (taggedType != null && taggedIds != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                ResponseDTO responseDTO = null;
                if (taggedType.equals("device")) {
                    List<QrCodeDTO> qrDetails = qrCodeRepository.getQrCodeDetailsByDeviceIds(vdmsId, taggedIds);
//                    List<String> clientQrCodeDeviceIds = new ArrayList<>();


                    List<ClientQrCodeDTO> clientQrCodeDetails = clientQrCodeService.getClientQrCodeDetailsByVdmsIdAndDeviceIds(vdmsId, taggedIds);
                    if (!clientQrCodeDetails.isEmpty()) {
                        for (int i = 0; i < clientQrCodeDetails.size(); i++) {

                            QrCodeDTO newQrCodeDTO = new QrCodeDTO();
                            newQrCodeDTO.setId(clientQrCodeDetails.get(i).getId());
                            newQrCodeDTO.setLocationId(clientQrCodeDetails.get(i).getLocationId());
                            newQrCodeDTO.setDeviceId(clientQrCodeDetails.get(i).getDeviceId());
                            newQrCodeDTO.setVdmsId(clientQrCodeDetails.get(i).getVdmsId());
                            newQrCodeDTO.setCreatedBy(clientQrCodeDetails.get(i).getCreatedBy());
                            newQrCodeDTO.setUpdated_by(clientQrCodeDetails.get(i).getUpdated_by());
                            newQrCodeDTO.setCreationTime(clientQrCodeDetails.get(i).getCreationTime().toString());
                            qrDetails.add(newQrCodeDTO);

                        }
                    }

                    responseDTO = ScleraUtils.generatePayload(qrDetails, 200, true);
                }
                if (taggedType.equals("location")) {
                    List<QrCodeDTO> qrDetails = qrCodeRepository.getQrCodeDetailsByLocationIds(vdmsId, taggedIds);


                    List<ClientQrCodeDTO> clientQrCodeDetails = clientQrCodeService.getClientQrCodeDetailsByVdmsIdAndLocationIds(vdmsId, taggedIds);
                    if (!clientQrCodeDetails.isEmpty()) {
                        for (int i = 0; i < clientQrCodeDetails.size(); i++) {

                            QrCodeDTO newQrCodeDTO = new QrCodeDTO();
                            newQrCodeDTO.setId(clientQrCodeDetails.get(i).getId());
                            newQrCodeDTO.setLocationId(clientQrCodeDetails.get(i).getLocationId());
                            newQrCodeDTO.setDeviceId(clientQrCodeDetails.get(i).getDeviceId());
                            newQrCodeDTO.setVdmsId(clientQrCodeDetails.get(i).getVdmsId());
                            newQrCodeDTO.setCreatedBy(clientQrCodeDetails.get(i).getCreatedBy());
                            newQrCodeDTO.setUpdated_by(clientQrCodeDetails.get(i).getUpdated_by());
                            newQrCodeDTO.setCreationTime(clientQrCodeDetails.get(i).getCreationTime().toString());
                            qrDetails.add(newQrCodeDTO);

                        }
                    }
                    responseDTO = ScleraUtils.generatePayload(qrDetails, 200, true);
                }
                log.info("Get qrCode details by Tagged type {},EndPoint: {}", taggedType, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client params, EndPoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getTaggedDevicesAndLocationByVdmsId(String vdmsId, String type, HttpServletRequest httpServletRequest) {
        log.info("Payload: TaggedType: {}, VdmsId:{}", type, vdmsId);
        if (vdmsId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                ResponseDTO responseDTO = null;
                if (type.equals("device")) {
                    List<QrCodeDTO> qrDetails = qrCodeRepository.getTaggedDevicesByVdmsId(vdmsId);
                    List<ClientQrCodeDTO> ClientQrCodeDetails = clientQrCodeService.getClientQrCodeTaggedDevicesDetailsByVdmsId(vdmsId);

                    for (int i = 0; i < ClientQrCodeDetails.size(); i++) {


                        QrCodeDTO newQrCodeDTO = new QrCodeDTO();
                        newQrCodeDTO.setId(ClientQrCodeDetails.get(i).getId());
                        newQrCodeDTO.setLocationId(ClientQrCodeDetails.get(i).getLocationId());
                        newQrCodeDTO.setDeviceId(ClientQrCodeDetails.get(i).getDeviceId());
                        newQrCodeDTO.setVdmsId(ClientQrCodeDetails.get(i).getVdmsId());
                        newQrCodeDTO.setCreatedBy(ClientQrCodeDetails.get(i).getCreatedBy());
                        newQrCodeDTO.setUpdated_by(ClientQrCodeDetails.get(i).getUpdated_by());
                        newQrCodeDTO.setCreationTime(ClientQrCodeDetails.get(i).getCreationTime().toString());
                        qrDetails.add(newQrCodeDTO);

                    }
                    responseDTO = ScleraUtils.generatePayload(qrDetails, 200, true);
                }
                if (type.equals("location")) {
                    List<QrCodeDTO> qrDetails = qrCodeRepository.getTaggedLocationsByVdmsId(vdmsId);
                    List<ClientQrCodeDTO> ClientQrCodeDetails = clientQrCodeService.getClientQrCodeTaggedLocationsDetailsByVdmsId(vdmsId);
                    for (int i = 0; i < ClientQrCodeDetails.size(); i++) {


                        QrCodeDTO newQrCodeDTO = new QrCodeDTO();
                        newQrCodeDTO.setId(ClientQrCodeDetails.get(i).getId());
                        newQrCodeDTO.setLocationId(ClientQrCodeDetails.get(i).getLocationId());
                        newQrCodeDTO.setDeviceId(ClientQrCodeDetails.get(i).getDeviceId());
                        newQrCodeDTO.setVdmsId(ClientQrCodeDetails.get(i).getVdmsId());
                        newQrCodeDTO.setCreatedBy(ClientQrCodeDetails.get(i).getCreatedBy());
                        newQrCodeDTO.setUpdated_by(ClientQrCodeDetails.get(i).getUpdated_by());
                        newQrCodeDTO.setCreationTime(ClientQrCodeDetails.get(i).getCreationTime().toString());
                        qrDetails.add(newQrCodeDTO);

                    }
                    responseDTO = ScleraUtils.generatePayload(qrDetails, 200, true);
                }
                log.info("Get tagged devices and location by vdmsId {}, EndPoint: {}", vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param,EndPoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getQrCodeIdsByVdmsIdAndType(String vdmsId, String type, HttpServletRequest httpServletRequest) {
        log.info("Payload : vdmsId {} ,type {}", vdmsId, type);
        if (vdmsId != null && type != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                JSONObject jsonObject = new JSONObject();
                if (type.equals("device")) {
                    List<String> deviceIds = qrCodeRepository.getQrCodeIdByVdmsIdAndDevice(vdmsId);
                    List<String> clientQrCodeDeviceIds = clientQrCodeService.getClientQrCodeDeviceIdsByVdmsId(vdmsId);
                    System.out.println(deviceIds);
                    System.out.println(clientQrCodeDeviceIds);
                    if (!clientQrCodeDeviceIds.isEmpty()) {
                        System.out.println(clientQrCodeDeviceIds);
                        deviceIds.addAll(clientQrCodeDeviceIds);
                    }
                    jsonObject.put("deviceIds", deviceIds);
                } else if (type.equals("location")) {
                    List<String> locationIds = qrCodeRepository.getQrCodeIdByVdmsIdAndLocation(vdmsId);
                    List<String> clientQrCodeLocationIds = clientQrCodeService.getClientQrCodeLocationIdsByVdmsId(vdmsId);
                    if (!clientQrCodeLocationIds.isEmpty()) {
                        locationIds.addAll(clientQrCodeLocationIds);
                    }
                    jsonObject.put("locationIds", locationIds);
                } else {
                    List<String> deviceIds = qrCodeRepository.getQrCodeIdByVdmsIdAndDevice(vdmsId);
                    List<String> locationIds = qrCodeRepository.getQrCodeIdByVdmsIdAndLocation(vdmsId);
                    List<String> clientQrCodeDeviceIds = clientQrCodeService.getClientQrCodeDeviceIdsByVdmsId(vdmsId);
                    if (!clientQrCodeDeviceIds.isEmpty()) {
                        deviceIds.addAll(clientQrCodeDeviceIds);
                    }
                    List<String> clientQrCodeLocationIds = clientQrCodeService.getClientQrCodeLocationIdsByVdmsId(vdmsId);
                    if (!clientQrCodeLocationIds.isEmpty()) {
                        locationIds.addAll(clientQrCodeLocationIds);
                    }
                    jsonObject.put("deviceIds", deviceIds);
                    jsonObject.put("locationIds", locationIds);
                }
                log.info("Fetching QrCode ids from db by vdmsId {} and Type {},EndPoint: {}", vdmsId, type, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(jsonObject, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param,EndPoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateQrCodeInfoByVdmsIdAndTaggedIds(String vdmsId, String type, List<String> taggedIds, HttpServletRequest httpServletRequest) {
        log.info("Payload: vdmsId: {} ,type: {}, taggedIds: {}", vdmsId, type, taggedIds);
        if (vdmsId != null && type != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                if (type.equals("device")) {
                    qrCodeRepository.updateQrCodeInfoBydeviceIdsAndVdmsId(taggedIds, vdmsId);
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Successfully Updated NFC Info(device)", "success");
                }
                if (type.equals("location")) {
                    qrCodeRepository.updateQrCodeInfoByLocationIdsAndVdmsId(taggedIds, vdmsId);
                    userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Successfully Updated NFC Info(location)", "success");
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Updating QRCode Info By VDMSId: {} And TaggedIDs: {},EndPoint: {}", vdmsId, taggedIds, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Unauthorised Access", "failed");
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param,EndPoint: {}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getAllUserInfoByOrganisationIdAndVdmsId(String orgId, String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, OrgId: {}", vdmsId, orgId);
        if (vdmsId != null && orgId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                List<String> emailIds = userRepository.getAllemailIdsByVdmsVisiblity(orgId, vdmsId);

//                List<UserDTO> userLoggedInData = webClientService.getUsersLastLoggedIntimeByEmailIds(emailIds, httpServletRequest);
                List<String> emails = userRepository.getAllUserEmailByOrgIdAndVdmsId(orgId, vdmsId);
                System.out.println("emails ===>" + emails);

                List<UserDTO> userDTOS = webClientService.getAllUserDetailsByOrganisationIdAndUserEmail(orgId, emails, httpServletRequest);


//                for (int i = 0; i < data.size(); i++) {
//                    if (data.get(i).getEmail().equals(userLoggedInData.get(i).getEmail())) {
//                        data.get(i).setLastlogintime(userLoggedInData.get(i).getLastlogintime());
//                    }
//                }
//                data = data.stream()
//                        .sorted(Comparator.comparing(UserDTO::getCreation_timestamp, Comparator.nullsLast(Comparator.naturalOrder())))
//                        .collect(Collectors.toList());

                ResponseDTO responseDTO = ScleraUtils.generatePayload(userDTOS, 200, true);
                log.info("Fetching All User Info By Organisation Id: {} And VdmsId: {}. Endpoint: {}", orgId, vdmsId, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param,EndPoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getAllQrCodeAndNfcData(JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        log.info("PayLoad:Type:{},startDate:{},endDate:{},vdmsId:{}", jsonObject.getJSONArray("type"), jsonObject.getBigInteger("start_date"),
                jsonObject.getBigInteger("end_date"), jsonObject.getString("vdmsid"));

        List<UserActivityDTO> userActionLogDTO = userActivityService.getAllQrCodeAndNfcData(jsonObject, httpServletRequest);

        ResponseDTO responseDTO = ScleraUtils.generatePayload(userActionLogDTO, 200, true);
        log.info("Fetching User Action Logs. Endpoint: {}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getUserAndLoginCount(String vdmsId, String orgId, HttpServletRequest httpServletRequest) {
        log.info("PayLoad:VdmsId:{},OrgId:{}", vdmsId, orgId);
        if (vdmsId != null && orgId != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authorized) {
                List<String> userList = userRepository.getUserList(vdmsId, orgId);
                log.info("userList:" + userList);
                Integer userCount = userList.size() + 1;
                log.info("userCount:" + userCount);
                JSONObject loginCount = webClientService.getLoginCountByEmail(userList, orgId, httpServletRequest);
                log.info("Response from Login:{}", loginCount);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user_count", userCount);
                jsonObject.put("active_user_count", loginCount.getIntValue("activeUserCount"));
                jsonObject.put("global_user_count", loginCount.getIntValue("globalUserCount"));
                jsonObject.put("global_active_user_count", loginCount.getIntValue("globalActiveUserCount"));
                log.info("SuccessFully Fetching Count:{}", jsonObject);
                return new ResponseEntity<>(jsonObject, HttpStatus.OK);
            } else {
                log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid client param,EndPoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> deleteDigitalTwinImagesByVdmsId(String email, String vdmsId, List<String> imageUrls, HttpServletRequest httpServletRequest) {
        log.info("Payload: Email: {}, VdmsId: {}, Size of ImageUrls: {}, ImageUrls: {}", email, vdmsId, imageUrls.size(), imageUrls);
        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
        if (authorized) {
            if (email != null && vdmsId != null) {
                List<String> url = new ArrayList<>();
                if (imageUrls != null && !imageUrls.isEmpty()) {
                    for (String imageUrl : imageUrls) {
                        String imageName = awsService.getFileNameByImageUrl(imageUrl, httpServletRequest);
                        String directory = String.format(resourceUrlConfig.getDeviceImageDirectory(), vdmsId);
                        url.add(directory + imageName);
                    }
                }
                if (!url.isEmpty()) {
                    awsService.removeFilesFromAWSS3(url);
                }

                ResponseDTO responseDTO = ScleraUtils.generatePayload("Digital Twin Image(s) Deleted Successfully", 200, true);
                log.info("Digital Twin Image(s) Deleted Successfully For VdmsId: {},EndPoint: {}", vdmsId, httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "TouchScreen", "DELETE", imageUrls.size() + " Digital Twin Image(s) Deleted Successfully", "success");
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Invalid client param,EndPoint: {}", httpServletRequest.getRequestURI());
                throw new ClientException("Invalid client param", 700, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> tagDigitalTwinToMultipleDevices(String vdmsId, List<MultipartFile> image, String imageUrl, List<String> deviceIds, HttpServletRequest httpServletRequest) throws IOException {
        log.info("Payload: VdmsId: {}, DeviceId: {}, ImageUrl: {}", vdmsId, deviceIds, imageUrl);
        boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
        if (authorized) {
            if (vdmsId != null && deviceIds != null) {
                JSONArray jsonArray = new JSONArray();
                if (image != null && !image.isEmpty()) {
                    String extension = FilenameUtils.getExtension(image.get(0).getOriginalFilename());
                    String url = String.format(resourceUrlConfig.getDeviceImageUrl(), vdmsId);
                    String directory = String.format(resourceUrlConfig.getDeviceImageDirectory(), vdmsId);
                    for (String deviceId : deviceIds) {
                        String data = awsService.addFileToAWSS3(image.get(0).getBytes(), directory, url, extension, deviceId, httpServletRequest);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("device_id", deviceId);
                        jsonObject.put("digital_twin_image_url", data);
                        jsonArray.add(jsonObject);
                        log.info("Tagged Digital Twin To Device By VdmsId: {} And DeviceId: {}, Endpoint: {}", vdmsId, deviceId, httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(vdmsId, "DigitalTwinTemplate", "ADD", "Tagged Digital Twin To Device: " + deviceId + " Successfully", "success");
                    }
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonArray, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }

                if (imageUrl != null) {
                    String extension = getFileExtensionByImageUrl(imageUrl);
                    String sourceKey = awsService.getFilePathByImageUrl(imageUrl, httpServletRequest);
                    String url = String.format(resourceUrlConfig.getDeviceImageUrl(), vdmsId);
                    String directory = String.format(resourceUrlConfig.getDeviceImageDirectory(), vdmsId);
                    for (String deviceId : deviceIds) {
                        String data = awsService.copyFileToAWSS3(sourceKey, directory, url, extension, deviceId, httpServletRequest);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("device_id", deviceId);
                        jsonObject.put("digital_twin_image_url", data);
                        jsonArray.add(jsonObject);
                        log.info("Tagged Digital Twin To Device By VdmsId: {} And DeviceId: {}, Endpoint: {}", vdmsId, deviceId, httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(vdmsId, "DigitalTwinTemplate", "ADD", "Tagged Digital Twin To Device: " + deviceId + " Successfully", "success");
                    }
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonArray, 200, true);
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }
                log.info("Digital Twin Image Is Not Present To Tag Endpoint: {}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "DigitalTwinTemplate", "ADD", "Digital Twin Image Is Not Present To Tag", "failed");
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Unauthorised Access. Endpoint: {}", httpServletRequest.getRequestURI());
            throw new ClientException("Unauthorised Access", 762, httpServletRequest.getRequestURI());
        }
    }

    public String getFileExtensionByImageUrl(String image_url) {
        return image_url.substring(image_url.lastIndexOf(".") + 1);
    }

    public String getDiskKeyByUniqueID(String vdmsId, String uniqueId, HttpServletRequest httpServletRequest) {
        return webClientService.getDiskKeyByUniqueID(vdmsId, uniqueId, httpServletRequest);
    }

    public String getMySQLKeyByUniqueID(String vdmsId, String uniqueId, HttpServletRequest httpServletRequest) {
        return webClientService.getMySQLKeyByUniqueID(vdmsId, uniqueId, httpServletRequest);
    }

    public String getSQLiteKeyByUniqueID(String vdmsId, String uniqueId, HttpServletRequest httpServletRequest) {
        return webClientService.getSQLiteKeyByUniqueID(vdmsId, uniqueId, httpServletRequest);
    }

    public ResponseEntity<ResponseDTO> getQrCodeAndClientQrCodeCount(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{}", vdmsId);
        Integer qrCodeCount = qrCodeRepository.getQrcodeCountByVdmsId(vdmsId);
        Integer clientQrCodeCount = clientQrCodeService.getClientQrCodeCountByVdmsID(vdmsId, httpServletRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("qrCodeCount", qrCodeCount);
        jsonObject.put("clientQrCodeCount", clientQrCodeCount);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonObject, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getAllQrCodeByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<QrCodeDTO> qrCodeDTOS = qrCodeRepository.getAllQrCodeByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Qr-Code Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    // qr code sync change
    public ResponseEntity<ResponseDTO> updateQrCodeSyncByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        vdmsService.updateQrCodeSyncByVdmsId(0, vdmsId, httpServletRequest);
        // qrcode sync changes
        BigInteger updatedTime = BigInteger.valueOf(System.currentTimeMillis());
        qrCodeRepository.updateQrCodeSync(0, updatedTime, vdmsId, httpServletRequest);
        clientQrCodeService.updateClientQrCodeSyncByVdmsId(0, updatedTime, vdmsId, httpServletRequest);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("SuccessFully Updated QrCode Sync", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getAllClientQrCodeByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<ClientQrCodeDTO> clientQrCodeDTOS = clientQrCodeService.getAllClientQrCodeByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Client Qr-Code Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(clientQrCodeDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> updateNfcSyncByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        vdmsService.updateNfcSyncByVdmsId(0, vdmsId, httpServletRequest);
        //nfc sync changes
        BigInteger updatedTime = BigInteger.valueOf(System.currentTimeMillis());
        nfcRepository.updateNfcSyncByVdmsId(0, updatedTime, vdmsId);
        clientNfcService.updateClientNfcSyncByVdmsId(0, updatedTime, vdmsId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("SuccessFully Updated Nfc Sync", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getNfcAndClientNfcCount(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{}", vdmsId);
        Integer nfcCount = nfcRepository.getNfcCountByVdmsId(vdmsId);
        Integer clientNfcCount = clientNfcService.getClientNfcCountByVdmsID(vdmsId, httpServletRequest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("nfcCount", nfcCount);
        jsonObject.put("clientNfcCount", clientNfcCount);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(jsonObject, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getAllNfcByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<NfcDTO> nfcDTOS = nfcRepository.getNfcRecordsByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Nfc Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getAllClientNfcByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<ClientNfcDTO> clientNfcDTOS = clientNfcService.getAllClientNfcByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Client Nfc Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(clientNfcDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }


    public ResponseEntity<ResponseDTO> updateVdmsAssetCountByVdmsId(String vdmsId, JSONObject jsonObject, HttpServletRequest httpServletRequest) {
        log.info("Payload:Data:" + jsonObject);
        if (vdmsId == null || jsonObject == null) {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
        Integer assetCount = jsonObject.getInteger("assetCount");
        vdmsService.updateVdmsAssetCountByVdmsId(vdmsId, assetCount, httpServletRequest);
        String orgId = vdmsRepository.getOrgIdByVdmsId(vdmsId);
        String orgName = customerOrganisationService.getOrgNameByOrgId(orgId, httpServletRequest);
        Integer totalOnboardedAssets = vdmsService.getTotalOnboardedAssets(List.of(vdmsId), orgId);
        log.info("totalOnboardedAssets:" + totalOnboardedAssets);
        BillingInfoDTO billingInfoDTO = billingInfoService.findBillingDataByOrgId(orgId);
        billingInfoDTO.setTotalOnboardedAssets(totalOnboardedAssets);
        log.info("billingInfoDTO:" + billingInfoDTO);

        if (billingInfoDTO.getTotalLicensedAssets() < totalOnboardedAssets) {
            JSONObject alertData = new JSONObject();
            List<String> emails = billingAdminEmailService.getBillingAdminEmails(httpServletRequest);
            alertData.put("emails", emails);
            alertData.put("template_type", "billing_reminder_alert");
            alertData.put("startDate", getCurrentTime(billingInfoDTO.getBillingStartDate(), "Asia/Kolkata", httpServletRequest));
            alertData.put("endDate", getCurrentTime(billingInfoDTO.getBillingEndDate(), "Asia/Kolkata", httpServletRequest));
            alertData.put("licensedAsset", billingInfoDTO.getTotalLicensedAssets());
            alertData.put("totalAssets", billingInfoDTO.getTotalOnboardedAssets());
            alertData.put("orgName",orgName);
            webClientAlertService.sendBillingReminderAlert(alertData);
        }

        ResponseDTO responseDTO = ScleraUtils.generatePayload("Vdms asset count successfully updated", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public String getCurrentTime(BigInteger timestamp, String timezone, HttpServletRequest httpServletRequest) {
        log.info("Payload:TimeStamp:{},TimeZone:{}", timestamp, timezone);
        if (timestamp != null) {
            DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            if (timezone != null) {
                dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
            }
            Timestamp stamp = new Timestamp(timestamp.longValueExact());
            Date date = new Date(stamp.getTime());
            log.info("Fetching Current Time.EndPoint:{}", httpServletRequest.getRequestURI());
            return dateFormat.format(date);
        } else {
            return "-";
        }
    }

    // qr code sync changes
    public ResponseEntity<ResponseDTO> getSyncedQrCodeByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<QrCodeDTO> qrCodeDTOS = qrCodeRepository.getAllSyncQrCodeByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Qr-Code Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(qrCodeDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getAllSyncClientQrCodeByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<ClientQrCodeDTO> clientQrCodeDTOS = clientQrCodeService.getAllSyncClientQrCodeByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Client Qr-Code Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(clientQrCodeDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //nfc sync changes
    public ResponseEntity<ResponseDTO> getAllSyncNfcByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<NfcDTO> nfcDTOS = nfcRepository.getNfcSyncRecordsByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Nfc Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<ResponseDTO> getAllSyncClientNfcByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<ClientNfcDTO> clientNfcDTOS = clientNfcService.getAllSyncClientNfcByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Client Nfc Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(clientNfcDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    //nfc sync changes
    public ResponseEntity<ResponseDTO> geAllSyncNfcByVdmsId(String vdmsId, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{},PageNo:{},PageSize:{}", vdmsId, pageNo, pageSize);
        int offset = pageSize * (pageNo - 1);
        List<NfcDTO> nfcDTOS = nfcRepository.getNfcSyncRecordsByVdmsId(vdmsId, pageSize, offset);
        log.info("Fetching All Nfc Data For Vdms:{},Endpoint:{}", vdmsId, httpServletRequest.getRequestURI());
        ResponseDTO responseDTO = ScleraUtils.generatePayload(nfcDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);

    }

    public ResponseEntity<?> getUpdatedAssetType(String assetTypeGroupName, String key, String sort, int pageNo, int pageSize,
                                                 String updatedTimestamp, HttpServletRequest httpServletRequest) {
        List<CategoryDTO> categoryDTOS = assetTypeService.getUpdatedAssetType(assetTypeGroupName, key, sort, pageNo, pageSize, updatedTimestamp, httpServletRequest);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(categoryDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getAgentPermissionsByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{}", vdmsId);
        String permissions = webClientService.getAgentPermissionsByVdmsId(vdmsId, httpServletRequest);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(permissions, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getUserRemoteDesktopAuth(String email, HttpServletRequest httpServletRequest) {
        log.info("Payload: email:" + email);
        Integer isUserRemoteDesktopAuth = webClientService.getUserRemoteDesktopAuthByUserId(email, httpServletRequest);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(isUserRemoteDesktopAuth, 200, true);
        log.info("Successfully fetching isUserRemoteDesktopAuth:{},By User EMail:{}", isUserRemoteDesktopAuth, email);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> updateConfigIdByVdmsId(String vdmsId, String configId, HttpServletRequest httpServletRequest) {
        log.info("Updating Config id :{} for vdmsId:{}", configId, vdmsId);
        if (vdmsId != null && configId != null) {
            vdmsRepository.updateCorrigoConfigIdByVdmsId(configId, vdmsId);
            userActionLogService.addUserActionLog(null,"Corrigo Configuration", "UPDATE", "Successfully updated the corrigo config id : " + configId + " for vdmsId: " + vdmsId, "success");
            ResponseDTO responseDTO = ScleraUtils.generatePayload("Successfully updated the corrigo config id", 200, true);
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters ,Endpoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateCorrigoSyncByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:VdmsId:{}", vdmsId);
        vdmsRepository.updateCorrigoSyncByVdmsId(0,vdmsId);
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Successfully updated the corrigo sync to 0", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getVdmsGlobalToken(String tokenSecretName, HttpServletRequest httpServletRequest) {
        log.info("Fetching Vdms Global Token from secret name:{}, Endpoint: {}", tokenSecretName, httpServletRequest.getRequestURI());
        try {
            ResponseDTO responseDTO;
            if (tokenSecretName.contains("edge")) {
                GetSecretValueRequest request = GetSecretValueRequest.builder()
                        .secretId(tokenSecretName)
                        .build();
                GetSecretValueResponse response = secretsManagerClient.getSecretValue(request);
                JSONObject data = JSONObject.parseObject(response.secretString());
                log.info("Data fetched from Secret: {}",data);
                responseDTO = ScleraUtils.generatePayload(data, 200, true);
            } else {
                responseDTO = ScleraUtils.generatePayload("Invalid Secret name provided", 401, false);
            }
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to retrieve VDMS global token from Secrets Manager", e);
            return null;
        }
        }

}
