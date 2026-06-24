package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.sclera.dto.*;
import io.sclera.exception.ClientException;
import io.sclera.exception.ServerException;
import io.sclera.model.Vdms;
import io.sclera.model.compositeclass.DockerIds;
import io.sclera.repository.DockerRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import io.sclera.util.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class DockerService {

    @Autowired
    private DockerRepository dockerRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private VdmsService vdmsService;

    @Autowired
    private UserService userService;

    @Autowired
    private VdmsVisibilityService vdmsvisibilityService;

    @Autowired
    private WebClientService webClientService;


    @Autowired
    private SocketUtils socketUtils;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;

    public ResponseEntity<?> getDockerInfoByVdmsIdAndDockerName(String username, String vdms_id, String docker_name, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserName:{},VDMS_Id:{},Docker_Name:{}", username, vdms_id, docker_name);

        if (username != null && vdms_id != null && docker_name != null) {
            DockerDTO data = dockerRepository.getDockerInfoByVdmsIdAndDockerName(vdms_id, docker_name);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);

            log.info("Fetching Docker Info By Vdms_Id:{} And Docker_Name:{},EndPoint:{}", vdms_id, docker_name, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getAllDockersByVdmsId(String username, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserName:{},VDMS_Id:{}", username, vdms_id);
        if (username != null && vdms_id != null) {
            Set<DockerDTO> data = dockerRepository.getAllDockersByVdmsId(vdms_id);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
            log.info("Fetching All Docker Info By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> addDockerByVdmsId(DockerDTO dockerdto, String username, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:UserName:{},VDMS_Id:{},DockerDTO:{}", username, vdms_id, dockerdto);
        if (dockerdto != null && username != null && vdms_id != null) {
            dockerRepository.addDockerByVdmsId(dockerdto.getName(), vdms_id, dockerdto.getMac_address(), dockerdto.getPublic_ip_address(),
                    dockerdto.getSystem_type(), dockerdto.getGateway());
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            userActionLogService.addUserActionLog(username, "Docker", "ADD", "A Docker Info Is Added By VDMS_Id:" + vdms_id, "success");
            log.info("Added Docker By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(username, "Docker", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteDockerByDockerNameAndVdmsId(String vdms_id, String docker_name, HttpServletRequest httpServletRequest) {
        log.info("Payload:VDMS_Id:{},Docker_Name:{}", vdms_id, docker_name);
        if (vdms_id != null && docker_name != null) {
            dockerRepository.deleteById(new DockerIds(docker_name, new Vdms(vdms_id)));
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            userActionLogService.addUserActionLog(vdms_id, "Docker", "DELETE", "A Docker Info Is Deleted By VDMS_ID:" + vdms_id, "success");
            log.info("Delete Docker By Docker_Name:{} And Vdms_Id:{},EndPoint:{}", docker_name, vdms_id, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdms_id, "Docker", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteDockerByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}", vdms_id);
        Set<String> docker_names = dockerRepository.getDockerIdsByVdmsId(vdms_id);
        if (docker_names != null && docker_names.size() > 0) {
            for (String docker_name : docker_names) {
                dockerRepository.deleteDockerByVdmsIdAndDockerName(vdms_id, docker_name);
                log.info("Deleted Docker By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
            }
        }
    }

    public ResponseEntity<?> upsertDockerByVdmsId(String docker, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Docker:{},loggedInUser:{}", docker, loggedInUser);
        if (docker != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                try {
                    DockerDTO dockerdto = JSON.parseObject(docker, DockerDTO.class);
                    Set<String> docker_names = dockerRepository.getDockerNamesByVdmsId(dockerdto.getVdms_id());
                    String proxy_profile_id = vdmsService.getDefaultProxyProfileByVdmsId(dockerdto.getVdms_id(), httpServletRequest);
                    dockerdto.setPrimary_proxy_profile_id(proxy_profile_id);
                    if (docker_names != null && !docker_names.isEmpty()) {
                        if (docker_names.contains(dockerdto.getName())) {
                            dockerRepository.updateDockerByVdmsIdAndDockerName(dockerdto.getBlock_timestamp(), dockerdto.getCidr(), dockerdto.getExternal_ip_address(),
                                    dockerdto.getGateway(), dockerdto.getHost(), dockerdto.getInterface_in(), dockerdto.getInterface_out(), dockerdto.getInternal_ip_address(),
                                    dockerdto.getInternet_required(), dockerdto.getInternet_status(), dockerdto.getInternet_timestamp(), dockerdto.getIs_static(),
                                    dockerdto.getIs_tagged(), dockerdto.getIs_block(), dockerdto.getMac_address(), dockerdto.getMacvlan_name(), dockerdto.getPrimary_dns(),
                                    dockerdto.getPublic_ip_address(), dockerdto.getSecondary_dns(), dockerdto.getSystem_type(), dockerdto.getVlan_id(), dockerdto.getApproval_status(),
                                    dockerdto.getVendor_org_id(), dockerdto.getNetwork_name(), dockerdto.getName(), dockerdto.getVdms_id());
                            ResponseDTO responseDTO = ScleraUtils.generatePayload("Docker details updated successfully", 200, true);
                            log.info("Docker details updated successfully:EndPoint:{}", httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "A Docker Info Is Updated For VDMS:" + dockerdto.getVdms_id(), "success");
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            dockerRepository.addDockerByVdmsIdAndDockerName(dockerdto.getName(), dockerdto.getVdms_id(), dockerdto.getNetwork_name(), dockerdto.getBlock_timestamp(), dockerdto.getCidr(), dockerdto.getExternal_ip_address(),
                                    dockerdto.getGateway(), dockerdto.getHost(), dockerdto.getInterface_in(), dockerdto.getInterface_out(), dockerdto.getInternal_ip_address(),
                                    dockerdto.getInternet_required(), dockerdto.getInternet_status(), dockerdto.getInternet_timestamp(), dockerdto.getIs_static(),
                                    dockerdto.getIs_tagged(), dockerdto.getIs_block(), dockerdto.getMac_address(), dockerdto.getMacvlan_name(), dockerdto.getPrimary_dns(),
                                    dockerdto.getPublic_ip_address(), dockerdto.getSecondary_dns(), dockerdto.getSystem_type(), dockerdto.getVlan_id(), dockerdto.getApproval_status(),
                                    dockerdto.getVendor_org_id(), dockerdto.getPrimary_proxy_profile_id());

                            vdmsvisibilityService.addVdmsByVendorOrganisationId(dockerdto.getVendor_org_id(), dockerdto.getVdms_id(), httpServletRequest);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload("Docker added successfully", 200, true);
                            userActionLogService.addUserActionLog(loggedInUser, "Docker", "ADD", "A Docker Info Is Added For VDMS:" + dockerdto.getVdms_id(), "success");
                            log.info("Docker details Added successfully:EndPoint:{}", httpServletRequest.getRequestURI());
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    } else {
                        dockerRepository.addDockerByVdmsIdAndDockerName(dockerdto.getName(), dockerdto.getVdms_id(), dockerdto.getNetwork_name(), dockerdto.getBlock_timestamp(), dockerdto.getCidr(), dockerdto.getExternal_ip_address(),
                                dockerdto.getGateway(), dockerdto.getHost(), dockerdto.getInterface_in(), dockerdto.getInterface_out(), dockerdto.getInternal_ip_address(),
                                dockerdto.getInternet_required(), dockerdto.getInternet_status(), dockerdto.getInternet_timestamp(), dockerdto.getIs_static(),
                                dockerdto.getIs_tagged(), dockerdto.getIs_block(), dockerdto.getMac_address(), dockerdto.getMacvlan_name(), dockerdto.getPrimary_dns(),
                                dockerdto.getPublic_ip_address(), dockerdto.getSecondary_dns(), dockerdto.getSystem_type(), dockerdto.getVlan_id(), dockerdto.getApproval_status(),
                                dockerdto.getVendor_org_id(), dockerdto.getPrimary_proxy_profile_id());

                        vdmsvisibilityService.addVdmsByVendorOrganisationId(dockerdto.getVendor_org_id(), dockerdto.getVdms_id(), httpServletRequest);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload("Docker added successfully", 200, true);
                        userActionLogService.addUserActionLog(loggedInUser, "Docker", "ADD", "A Docker Info Is Added For VDMS:" + dockerdto.getVdms_id(), "success");
                        log.info("Docker details Added successfully:EndPoint:{}", httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } catch (Exception e) {
                    log.error("Docker does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    log.error("Stack Trace: {}", e.getMessage());
                    userActionLogService.addUserActionLog(loggedInUser, "Docker", "ADD", "Docker Data Does Not Exist", "failed");
                    throw new ClientException("Docker does not exist", 732, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "ADD", "Role Not Authorized", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Docker", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> upsertDockerByVdmsIdAndDockerName(String docker, String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload:Docker:{},vdmsId:{}", docker, vdmsId);
        if (docker != null && vdmsId != null) {
            boolean authenticated = scleraRoleCheckUtils.checkVdmsId(vdmsId, httpServletRequest);
            if (authenticated) {
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
                            userActionLogService.addUserActionLog(vdmsId, "Docker", "UPDATE", "Docker Updated successfully for VDMS:" + vdmsId, "success");
                            ResponseDTO responseDTO = ScleraUtils.generatePayload("Docker details updated successfully", 200, true);
                            log.info("Docker details updated successfully:EndPoint:{}", httpServletRequest.getRequestURI());
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            dockerRepository.addDockerByVdmsIdAndDockerName(dockerdto.getName(), dockerdto.getVdms_id(), dockerdto.getNetwork_name(), dockerdto.getBlock_timestamp(), dockerdto.getCidr(), dockerdto.getExternal_ip_address(),
                                    dockerdto.getGateway(), dockerdto.getHost(), dockerdto.getInterface_in(), dockerdto.getInterface_out(), dockerdto.getInternal_ip_address(),
                                    dockerdto.getInternet_required(), dockerdto.getInternet_status(), dockerdto.getInternet_timestamp(), dockerdto.getIs_static(),
                                    dockerdto.getIs_tagged(), dockerdto.getIs_block(), dockerdto.getMac_address(), dockerdto.getMacvlan_name(), dockerdto.getPrimary_dns(),
                                    dockerdto.getPublic_ip_address(), dockerdto.getSecondary_dns(), dockerdto.getSystem_type(), dockerdto.getVlan_id(), dockerdto.getApproval_status(),
                                    dockerdto.getVendor_org_id(), dockerdto.getPrimary_proxy_profile_id());

                            vdmsvisibilityService.addVdmsByVendorOrganisationId(dockerdto.getVendor_org_id(), dockerdto.getVdms_id(), httpServletRequest);
                            userActionLogService.addUserActionLog(vdmsId, "Docker", "ADD", "Docker added successfully for VDMS:" + vdmsId, "success");
                            ResponseDTO responseDTO = ScleraUtils.generatePayload("Docker added successfully", 200, true);
                            log.info("Docker details Added successfully:EndPoint:{}", httpServletRequest.getRequestURI());
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        }
                    } else {
                        dockerRepository.addDockerByVdmsIdAndDockerName(dockerdto.getName(), dockerdto.getVdms_id(), dockerdto.getNetwork_name(), dockerdto.getBlock_timestamp(), dockerdto.getCidr(), dockerdto.getExternal_ip_address(),
                                dockerdto.getGateway(), dockerdto.getHost(), dockerdto.getInterface_in(), dockerdto.getInterface_out(), dockerdto.getInternal_ip_address(),
                                dockerdto.getInternet_required(), dockerdto.getInternet_status(), dockerdto.getInternet_timestamp(), dockerdto.getIs_static(),
                                dockerdto.getIs_tagged(), dockerdto.getIs_block(), dockerdto.getMac_address(), dockerdto.getMacvlan_name(), dockerdto.getPrimary_dns(),
                                dockerdto.getPublic_ip_address(), dockerdto.getSecondary_dns(), dockerdto.getSystem_type(), dockerdto.getVlan_id(), dockerdto.getApproval_status(),
                                dockerdto.getVendor_org_id(), dockerdto.getPrimary_proxy_profile_id());

                        vdmsvisibilityService.addVdmsByVendorOrganisationId(dockerdto.getVendor_org_id(), dockerdto.getVdms_id(), httpServletRequest);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload("Docker added successfully", 200, true);
                        userActionLogService.addUserActionLog(vdmsId, "Docker", "ADD", "Docker added successfully for VDMS:" + vdmsId, "success");
                        log.info("Docker details Added successfully:EndPoint:{}", httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } catch (Exception e) {
                    log.error("Docker does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    log.error("Stack Trace:{}", e.getMessage());
                    userActionLogService.addUserActionLog(vdmsId, "Docker", "ADD", "Docker does not exist for VDMS:" + vdmsId, "failed");
                    throw new ClientException("Docker does not exist", 732, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Unauthorized Access.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(vdmsId, "Docker", "ADD", "Unauthorized access for VDMS:" + vdmsId, "failed");
                throw new ClientException("Unauthorized Access", 762, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdmsId, "Docker", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> inviteVendor(String email, String vendor_email, String loggedInUser, HttpServletRequest httpServletRequest) throws JsonProcessingException {
        log.info("Payload:Email:{},Vendor_Email:{},loggedInUser:{}", email, vendor_email, loggedInUser);
        if (email != null && vendor_email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                UserDTO vendorDTO = webClientService.getRoleAndOrganisationIdByVendorEmail(vendor_email, loggedInUser, httpServletRequest);
                log.info("vendorDTO:{}", vendorDTO);
                if (vendorDTO != null) {

                    if (vendorDTO.getOrganisation_id() != null && vendorDTO.getRole().equalsIgnoreCase("master-vendor")) {
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(vendorDTO.getOrganisation_id(), 200, true);
                        log.info("Fetching vendor organisation Id. EndPoint:{}", httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else if (vendorDTO.getRole().equalsIgnoreCase("vendor")) {
                        ResponseDTO responseDTO = ScleraUtils.generatePayload("Invalid Role", 200, true);
                        log.info("Invalid Role.EndPoint:{}", httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        log.error("Role not Authorised.EndPoint:{}", httpServletRequest.getRequestURI());
                        throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
                    }
                } else {
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    log.error("User not found. Endpoint: {}", httpServletRequest.getRequestURI());
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

    public Integer getNetworkCountByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}", vdms_id);
        log.info("Fetching Network Count By VdmS_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return dockerRepository.getNetworkCountByVdmsId(vdms_id);
    }

    public Integer getNetworkCountByVdmsIdAndVendorOrganisationId(String vdms_id, String vendor_org_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, VendorOrgId: {}", vdms_id, vendor_org_id);
        log.info("Fetching Network Count By VdmS_Id:{} And Vendor_Org_Id:{},EndPoint:{}", vdms_id, vendor_org_id, httpServletRequest.getRequestURI());
        return dockerRepository.getNetworkCountByVdmsIdAndVendorOrganisationId(vdms_id, vendor_org_id);
    }

    public Set<DockerSyncDTO> getDockerSyncByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}", vdms_id);
        log.info("Fetching Docker Sync By VdmS_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return dockerRepository.getDockerSyncByVdmsId(vdms_id);
    }

    public ResponseEntity<?> addVdmsSyncByVendorOrganisationId(String vendor_org_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VendorOrgId: {}", vendor_org_id);
        if (vendor_org_id != null) {
            dockerRepository.addVdmsSyncByVendorOrganisationId(vendor_org_id);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            log.info("Adding VDMS SYNC By Vendor_Org_Id:{},EndPoint:{}", vendor_org_id, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateVdmsSyncByVendorOrgIdVdmsIdAndDockerName(String vendor_org_id, String vdms_id, String name, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, VendorOrgId: {}, DockerName: {}", vdms_id, vendor_org_id, name);
        if (vdms_id != null && vendor_org_id != null && name != null) {
            boolean authorized = scleraRoleCheckUtils.checkVdmsId(vdms_id, httpServletRequest);
            if (authorized) {
                dockerRepository.updateVdmsSyncByVendorOrgIdVdmsIdAndDockerName(vendor_org_id, vdms_id, name);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Updating VDMS SYNC By  Vendor_Org_Id:{} And VdmS_Id:{},Docker_Name:{},EndPoint:{}", vdms_id, vendor_org_id, name, httpServletRequest.getRequestURI());
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

    public Set<DockerDTO> getAllDockersByInviteeOrganisationIdAndVdmsId(String organisation_id, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrganisationId: {}, VdmsId: {}", organisation_id, vdms_id);
        log.info("Fetching All Dockers By Invitee Organisation_Id:{} And Vdms_Id:{},Endpoint:{}", organisation_id, vdms_id, httpServletRequest.getRequestURI());
        return dockerRepository.getAllDockersByInviteeOrganisationIdAndVdmsId(organisation_id, vdms_id);
    }

    public ResponseEntity<?> acceptCustomerRequestByVdmsIdAndDockerName(String vendor_org_id, String email, String vdms_id, String name, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: VendorOrgId: {}, Email: {}, VdmsId: {}, DockerName: {}, LoggedInUser: {}", vendor_org_id, email, vdms_id, name, loggedInUser);
        if (vendor_org_id != null && email != null && vdms_id != null && name != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-vendor");
            if (access) {
                Integer isInvited = dockerRepository.checkInviteStatusByVdmsIdAndDockerName(vendor_org_id, vdms_id, name);
                if (isInvited == 1) {
                    String old_vendor_org_id = dockerRepository.getVendorOrganisationIdByVdmsIdAndDockerName(vdms_id, name);
                    log.info("Payload:{}", old_vendor_org_id);
                    ResponseDTO response = webClientService.getMasterVendorDetailsByOrganisationId(old_vendor_org_id, loggedInUser, httpServletRequest);
                    log.info("Response from Vendor :{}", response);
                    String jsonString = JSON.toJSONString(response.getData());
                    log.info("jsonString:{}", jsonString);
                    UserDTO vendorDTO = JSON.parseObject(jsonString, UserDTO.class);
                    log.info("vendorDTO:{}", vendorDTO);
                    VdmsDTO vdmsDTO = vdmsService.getVdmsInfoByVdmsId(vdms_id, httpServletRequest);
                    DockerDTO dockerDTO = dockerRepository.getDockerInfoByVdmsIdAndDockerName(vdms_id, name);
                    UserDTO userDTO = userService.getMasterUserInfoByVdmsId(vdms_id, httpServletRequest);
                    dockerRepository.acceptCustomerRequestByVdmsIdAndDockerName(vendor_org_id, vdms_id, name);

                    // send email to old master_vendor of that network
                    JSONObject jsonObject = new JSONObject();

                    if (dockerDTO.getNetwork_name() != null) {
                        jsonObject.put("networkName", dockerDTO.getNetwork_name());
                    } else {
                        jsonObject.put("networkName", name);
                    }

                    jsonObject.put("to", vendorDTO.getEmail());
                    jsonObject.put("name", vendorDTO.getName());
                    jsonObject.put("propertyOwnerName", userDTO.getName());
                    jsonObject.put("vdmsId", vdmsDTO.vdms_id);
                    jsonObject.put("systemType", dockerDTO.getSystem_type());
                    jsonObject.put("propertyName", vdmsDTO.getProperty_name());
                    jsonObject.put("vdmsAddress", getVdmsAddress(vdmsDTO, httpServletRequest));
                    jsonObject.put("email", userDTO.getEmail());
                    jsonObject.put("phone", userDTO.getPhone());
                    log.info("Payload:{}", jsonObject);
                    ResponseEntity<ResponseDTO> alertServerResponse = webClientService.networkRemovedEmail(jsonObject, httpServletRequest);
                    log.info("Response from Alert:{}", alertServerResponse);
                    log.info("Successfully Network Removed By Email.EndPoint:{}", httpServletRequest.getRequestURI());
                    if (alertServerResponse.getStatusCode() == HttpStatus.OK) {
                        // remove vdms_visibility to vendors of old vendor_org
                        Set<String> visible_vdms = vdmsService.getVisibleVdmsIdsByVendorOrganisationId(old_vendor_org_id, httpServletRequest);
                        if (!visible_vdms.contains(vdms_id)) {
                            List<String> vendor_emails = webClientService.getVendorEmailsByOrganisationId(old_vendor_org_id, loggedInUser, httpServletRequest);
                            log.info("Response from Vendor:{}", vendor_emails);
                            log.info("Fetching Vendor Emails By Organisation_Id:{} from Vendor Server", old_vendor_org_id);
                            if (vendor_emails != null && vendor_emails.size() > 0) {
                                for (String vendor_email : vendor_emails) {
                                    vdmsvisibilityService.deleteVisibleVdmsByEmailAndVdmsId(vendor_email, vdms_id, httpServletRequest);
                                }
                            }
                        }

                        // add vdms_visibility to vendors under new vendor_org
                        vdmsvisibilityService.addVdmsByInviteeVendorOrganisationId(vendor_org_id, vdms_id, loggedInUser, httpServletRequest);
                        VdmsSyncDTO vdmsSyncDTO = vdmsService.getVdmsSyncByVdmsId(vdms_id, httpServletRequest);
                        log.info("VdmsSyncDTO: {}", vdmsSyncDTO);
                        userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "A Customer Request Is Accepted By VDMS_Id:" + vdms_id + " and Docker_Name:" + name, "success");
                        int isMultiTenant = vdmsService.getMultiTenantCheck(vdms_id);
                        if (isMultiTenant == 1) {
                            webClientService.multiTenantSyncApiCall(vdms_id, vdmsSyncDTO,vdmsDTO.getAwsRegion(),httpServletRequest);
                        } else {
                            socketUtils.invokeWebSocketEndpoint("/topic/vdms/" + vdms_id + "/sync/data", vdmsSyncDTO);
                        }
                        log.info("Accept Customer Request By Vdms_Id:{} And Docker_Name:{},EndPoint:{}", vdms_id, name, httpServletRequest.getRequestURI());
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        ResponseDTO responseDTO = alertServerResponse.getBody();
                        log.error("Unable to send email.EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(email, "Docker", "UPDATE", "Unable To Send the Email", "failed");
                        throw new ServerException(responseDTO.getData().toString(), 812, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("Network invite already cancelled.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(email, "Docker", "UPDATE", "Network invite already cancelled", "failed");
                    throw new ClientException("Network invite already cancelled", 729, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> declineCustomerRequestByVdmsIdAndDockerName(String vendor_org_id, String email, String vdms_id, String name, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Vendor_Org_Id:{},Email:{},VDMS_Id:{},Docker_Name:{},loggedInUser:{}", vendor_org_id, email, vdms_id, name, loggedInUser);
        if (vendor_org_id != null && email != null && vdms_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-vendor");
            if (access) {
                dockerRepository.declineCustomerRequestByVdmsIdAndDockerName(vdms_id, name);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "A customer Request Is Declined By Vdms_Is:" + vdms_id, "success");
                log.info("Decline Customer Request By Vdms_Id:{}, And Docker_Name:{},EndPoint:{}", vdms_id, name, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> transferDockerToRegisteredVendorByVdmsIdAndDockerName(String email, String vdms_id, String name, DockerDTO dockerdto, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:DockerDTO:{},Email:{},VDMS_Id:{},Docker_Name:{},loggedInUser:{}", dockerdto, email, vdms_id, name, loggedInUser);
        if (email != null && vdms_id != null && name != null && dockerdto != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                UserDTO vendorDTO = webClientService.getVendorDetailsByEmail(dockerdto.getVendor_email(), dockerdto.getVendor_org_id(), loggedInUser, httpServletRequest);
                log.info("vendorDTO:{}", vendorDTO);
                UserDTO userDTO = userService.getMasterUserInfoByVdmsId(vdms_id, httpServletRequest);
                VdmsDTO vdmsDTO = vdmsService.getVdmsInfoByVdmsId(vdms_id, httpServletRequest);
                String vendor_org_id = dockerRepository.getVendorOrganisationIdByVdmsIdAndDockerName(vdms_id, name);
                if (vendor_org_id != null) {
                    if (!vendor_org_id.equalsIgnoreCase(vendorDTO.getOrganisation_id())) {
                        dockerRepository.transferDockerToRegisteredVendorByVdmsIdAndDockerName(vendorDTO.getOrganisation_id(), vdms_id, name);
                        JSONObject jsonObject = new JSONObject();

                        if (dockerdto.getNetwork_name() != null) {
                            jsonObject.put("networkName", dockerdto.getNetwork_name());
                        } else {
                            jsonObject.put("networkName", name);
                        }

                        jsonObject.put("to", dockerdto.getVendor_email());
                        jsonObject.put("vendorName", vendorDTO.getName());
                        jsonObject.put("propertyOwnerName", userDTO.getName());
                        jsonObject.put("vdmsId", vdmsDTO.vdms_id);
                        jsonObject.put("systemType", dockerdto.getSystem_type());
                        jsonObject.put("propertyName", vdmsDTO.getProperty_name());
                        jsonObject.put("vdmsAddress", getVdmsAddress(vdmsDTO, httpServletRequest));
                        jsonObject.put("userEmail", userDTO.getEmail());
                        jsonObject.put("userPhone", userDTO.getPhone());
                        log.info("Payload:{}", jsonObject);
                        ResponseEntity<ResponseDTO> alertServerResponse = webClientService.registeredVendorTransfer(jsonObject, httpServletRequest);
                        log.info("Response from Alert:{}", alertServerResponse);
                        log.info("Successfully Registered Vendor transfer.EndPoint:{}", httpServletRequest.getRequestURI());
                        if (alertServerResponse.getStatusCode() == HttpStatus.OK) {
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(0, 200, true);
                            log.info("Transfer Docker To Registered Vendor By Vdms_Id:{} And Docker_Name:{},EndPoint:{}", vdms_id, name, httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Successfully Transferred Docker To Registered Vendor By VDMS_Id:" + vdms_id + " and Docker_Name:" + name, "success");
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            ResponseDTO responseDTO = alertServerResponse.getBody();
                            log.error("Unable to send email:EndPoint:{}", httpServletRequest.getRequestURI());
                            userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Unable To Send The Email", "failed");
                            throw new ServerException(responseDTO.getData().toString(), 812, httpServletRequest.getRequestURI());
                        }
                    } else {
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(1, 200, true);
                        log.error("Network Already assigned to vendor");
                        userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Network Already assigned to vendor!", "failed");
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
                } else {
                    log.error("Network Does Not Exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Network Does Not Exist", "failed");
                    throw new ClientException("Network does not exist", 718, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Role Not Authorized", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }


    public ResponseEntity<?> transferDockerToUnRegisteredVendorByVdmsIdAndDockerName(String email, String vdms_id, String name, DockerDTO dockerdto, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:DockerDTO:{},Email:{},VDMS_Id:{},Docker_Name:{},loggedInUser:{}", dockerdto, email, vdms_id, name, loggedInUser);
        if (email != null && vdms_id != null && name != null && dockerdto != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                UserDTO userdto = userService.getMasterUserInfoByVdmsId(dockerdto.getVdms_id(), httpServletRequest);
                VdmsDTO vdmsdto = vdmsService.getVdmsInfoByVdmsId(dockerdto.getVdms_id(), httpServletRequest);
                if (userdto != null && vdmsdto != null) {
                    JSONObject jsonObject = new JSONObject();
                    if (dockerdto.getNetwork_name() != null) {
                        jsonObject.put("networkName", dockerdto.getNetwork_name());
                    } else {
                        jsonObject.put("networkName", name);
                    }

                    jsonObject.put("to", dockerdto.getVendor_email());
                    jsonObject.put("vendorName", dockerdto.getVendor_email());
                    jsonObject.put("propertyOwnerName", userdto.getName());
                    jsonObject.put("vdmsId", vdmsdto.vdms_id);
                    jsonObject.put("systemType", dockerdto.getSystem_type());
                    jsonObject.put("propertyName", vdmsdto.getProperty_name());
                    jsonObject.put("vdmsAddress", getVdmsAddress(vdmsdto, httpServletRequest));
                    jsonObject.put("userEmail", userdto.getEmail());
                    jsonObject.put("userPhone", userdto.getPhone());
                    log.info("Payload:{}", jsonObject);
                    ResponseEntity<ResponseDTO> alertServerResponse = webClientService.unregisteredVendorTransfer(jsonObject, httpServletRequest);
                    log.info("Response from Alert:{}", alertServerResponse);
                    log.info("Successfully UnRegistered Vendor transfer.EndPoint:{}", httpServletRequest.getRequestURI());
                    if (alertServerResponse.getStatusCode() == HttpStatus.OK) {
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                        userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Successfully Transferred Docker To UnRegistered Vendor By VDMS_Id:" + vdms_id + " and Docker_Name:" + name, "success");
                        log.info("Transfer Docker To UnRegistered Vendor By Vdms_Id:{} And Docker_Name:{},EndPoint:{}", vdms_id, name, httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        ResponseDTO responseDTO = alertServerResponse.getBody();
                        log.error("Unable to send email:EndPoint:{}", httpServletRequest.getRequestURI());
                        userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Unable to send email", "failed");
                        throw new ServerException(responseDTO.getData().toString(), 812, httpServletRequest.getRequestURI());
                    }
                } else {
                    log.error("User not found.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "User Not Found", "failed");
                    throw new ClientException("User not found", 701, httpServletRequest.getRequestURI());

                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> cancelVendorTransferByVdmsIdAndDockerName(String email, String vdms_id, String name, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Email:{},VDMS_Id:{},Docker_Name:{},loggedInUser:{}", email, vdms_id, name, loggedInUser);
        if (email != null && vdms_id != null && name != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                dockerRepository.cancelVendorTransferByVdmsIdAndDockerName(vdms_id, name);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", " A Vendor Transfer Is Canceled By VDMS_Id:" + vdms_id + " and Docker_Name:" + name, "success");
                log.info("Cancel Vendor Transfer By Vdms_Id:{} And Docker_Name:{},EndPoint:{}", vdms_id, name, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> modifyProxyProfileToDockerByVdmsIdAndNetworkName(String email, String vdms_id, String id, Set<String> networks, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:VDMS_Id:{},Email:{},Id:{}, Networks: {},loggedInUser:{}", vdms_id, email, id, networks, loggedInUser);
        if (email != null && vdms_id != null && id != null && networks != null && loggedInUser != null) {

            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                dockerRepository.removeProxyProfileByVdmsId(vdms_id);
                if (networks.size() > 0) {
                    for (String network : networks) {
                        dockerRepository.modifyProxyProfileToDockerByVdmsIdAndNetworkName(id, vdms_id, network);
                    }
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "A Proxy Profile Is Modified By Id:" + vdms_id, "success");
                log.info("Modify Proxy Profile To Docker By Vdms_Id:{} And Network_Name:{},EndPoint:{}", vdms_id, networks, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "Docker", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public Set<String> getProxyProfileTaggedDockersByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}", vdms_id);
        log.info("Fetching Proxy Profile Tagged Dockers By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return dockerRepository.getProxyProfileTaggedDockersByVdmsId(vdms_id);
    }

    public ResponseEntity<?> getNetworkNamesByVdmsId(String email, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: Email: {}, VdmsId: {}", email, vdms_id);


        if (email != null && vdms_id != null) {
            Set<String> data = dockerRepository.getNetworkNamesByVdmsId(vdms_id);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
            log.info("Fetching Network Names By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public String getDistinctProxyProfileByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}", vdms_id);
        log.info("Fetching Distinct Proxy Profile By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        return dockerRepository.getDistinctProxyProfileByVdmsId(vdms_id);
    }

    public ResponseEntity<?> syncDockerByVdmsId(Set<DockerDTO> dockerDTOS, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload:VDMS_Id:{},DockerDTO:{}", vdms_id, dockerDTOS);
        if (dockerDTOS != null && vdms_id != null) {
            dockerRepository.deleteDockerByVdmsId(vdms_id);
            String primary_proxy_profile_id = vdmsService.getDefaultProxyProfileByVdmsId(vdms_id, httpServletRequest);
            if (dockerDTOS.size() > 0) {
                for (DockerDTO dockerDTO : dockerDTOS) {
                    dockerRepository.addDockerByVdmsIdAndDockerName(dockerDTO.getName(), dockerDTO.getVdms_id(), dockerDTO.getNetwork_name(), dockerDTO.getBlock_timestamp(), dockerDTO.getCidr(), dockerDTO.getExternal_ip_address(),
                            dockerDTO.getGateway(), dockerDTO.getHost(), dockerDTO.getInterface_in(), dockerDTO.getInterface_out(), dockerDTO.getInternal_ip_address(),
                            dockerDTO.getInternet_required(), dockerDTO.getInternet_status(), dockerDTO.getInternet_timestamp(), dockerDTO.getIs_static(),
                            dockerDTO.getIs_tagged(), dockerDTO.getIs_block(), dockerDTO.getMac_address(), dockerDTO.getMacvlan_name(), dockerDTO.getPrimary_dns(),
                            dockerDTO.getPublic_ip_address(), dockerDTO.getSecondary_dns(), dockerDTO.getSystem_type(), dockerDTO.getVlan_id(), dockerDTO.getApproval_status(),
                            dockerDTO.getVendor_org_id(), primary_proxy_profile_id);
                }
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
            userActionLogService.addUserActionLog(null, "Docker", "ADD", "Docker Sync Successfully Done By VDMS_Id:" + vdms_id, "success");
            log.info("Sync Docker By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(vdms_id, "Docker", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void tagMasterVendorToNetwork(String vdmsId, String name, String vendorOrgId, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, NetwokName: {}, VendorOrgId: {}, LoggedInUser: {}", vdmsId, name, vendorOrgId, loggedInUser);
        if (vdmsId != null && name != null && vendorOrgId != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                dockerRepository.tagMasterVendorToNetwork(vendorOrgId, vdmsId, name);
                log.info("Tag Master VendorTo Network.Vdms_Id:{},Name:{},loggedInUser:{}.EndPoint:{}", vdmsId, name, loggedInUser, httpServletRequest.getRequestURI());
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> untagVendorByOrganisationId(String orgId, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId: {}", orgId);
        dockerRepository.untagVendorByOrganisationId(orgId);
        this.addVdmsSyncByVendorOrganisationId(orgId, httpServletRequest);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
        log.info("UnTag Vendor By Org_Id:{},EndPoint:{}", orgId, httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public String getVdmsAccessByOrganisationIdAndVdmsId(String vdmsId, String organisationId) {
        return dockerRepository.getVdmsAccessByOrganisationIdAndVdmsId(vdmsId, organisationId);
    }

    public Integer getNetworkCountByVdmsIdAndInviteeOrgId(String vdms_id, String vendor_org_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, VendorOrgId: {}", vdms_id, vendor_org_id);
        log.info("Fetching Network Count By VdmS_Id:{} And Vendor_Org_Id:{},EndPoint:{}", vdms_id, vendor_org_id, httpServletRequest.getRequestURI());
        return dockerRepository.getNetworkCountByVdmsIdAndInviteeOrgId(vdms_id, vendor_org_id);
    }

    public String getInviteeOrgId(String vdmsId, String organisationId, HttpServletRequest httpServletRequest) {
        return dockerRepository.getInviteeOrgId(vdmsId, organisationId);
    }
}