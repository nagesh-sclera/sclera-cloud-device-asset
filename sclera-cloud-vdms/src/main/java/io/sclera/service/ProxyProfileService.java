package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.dto.ProxyProfileDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.ProxyProfileRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Set;

@Service
@Slf4j
public class ProxyProfileService {

    @Autowired
    private ProxyProfileRepository proxyProfileRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private VdmsService vdmsService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;


    public ResponseEntity<?> getProxyProfileByCustomerOrganisationId(String customer_org_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Customer_org_id: {}, loggedInUser: {}", customer_org_id, loggedInUser);
        if (customer_org_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                Set<ProxyProfileDTO> data = proxyProfileRepository.getProxyProfileByCustomerOrganisationId(customer_org_id);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Fetching Proxy Profile By Customer_Organisation_Id:{},EndPoint:{}", customer_org_id, httpServletRequest.getRequestURI());
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

    public ResponseEntity<?> getGlobalProxyProfiles(String admin_email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: admin_email: {}, loggedInUser: {}", admin_email, loggedInUser);
        if (admin_email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                Set<ProxyProfileDTO> data = proxyProfileRepository.getGlobalProxyProfiles();
                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Fetching Global Proxy Profiles,Admin_Email:{},EndPoint:{}", admin_email, httpServletRequest.getRequestURI());
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

    public ProxyProfileDTO getProxyProfileByProxyProfileId(String id, HttpServletRequest httpServletRequest) {
        log.info("Payload: ProxyProfileId: {}", id);
        log.info("Fetching Proxy Profiles,by Proxy_Profile_Id:{},EndPoint:{}", id, httpServletRequest.getRequestURI());
        return proxyProfileRepository.getProxyProfileByProxyProfileId(id);
    }

    public ResponseEntity<?> addProxyProfileByCustomerOrganisationId(String customer_org_id, ProxyProfileDTO proxyProfileDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Customer_Organisation_Id:{},ProxyProfileDTO:{},loggedInUser:{}", customer_org_id, proxyProfileDTO, loggedInUser);
        if (customer_org_id != null && proxyProfileDTO != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                Integer count = proxyProfileRepository.checkProxyProfileByHostMachine(proxyProfileDTO.getHost_machine());
                if (count == 0) {
                    String id = Generators.timeBasedGenerator().generate().toString();
                    proxyProfileRepository.addProxyProfileByCustomerOrganisationId(id, proxyProfileDTO.getName(), proxyProfileDTO.getPublic_ip(), proxyProfileDTO.getHost_machine(), proxyProfileDTO.getTcp_port(), proxyProfileDTO.getUdp_port(), proxyProfileDTO.getSsl_enabled(), customer_org_id);
                    vdmsService.updateProxyServerHostSyncByVdmsId(1, proxyProfileDTO.getHost_machine(), httpServletRequest);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(0, 200, true);
                    userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "ADD", "A proxy Profile Is Added By Name:" + proxyProfileDTO.getName(), "success");
                    log.info("Adding Proxy Profile By Customer_Organisation_Id:{},EndPoint:{}", customer_org_id, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Proxy Profile Already Exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "ADD", "Proxy Profile Already Exist", "failed");
                    throw new ClientException("Proxy Profile Already Exist", 738, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> addGlobalProxyProfileByAdminEmail(String admin_email, ProxyProfileDTO proxyProfileDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Admin_Email:{},ProxyProfileDTO:{},loggedInUser:{}", admin_email, proxyProfileDTO, loggedInUser);
        if (admin_email != null && proxyProfileDTO != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                String id = Generators.timeBasedGenerator().generate().toString();
                proxyProfileRepository.addGlobalProxyProfileByAdminEmail(id, proxyProfileDTO.getName(), proxyProfileDTO.getPublic_ip(), proxyProfileDTO.getTcp_port(), proxyProfileDTO.getUdp_port(), 1, proxyProfileDTO.getSsl_enabled());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "ADD", "A Global Proxy Profile Is Added By Name:" + proxyProfileDTO.getName(), "success");
                log.info("Adding Global Proxy Profile By Admin_Email:{},EndPoint:{}", admin_email, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> updateProxyProfileByProxyProfileId(String customer_org_id, String id, ProxyProfileDTO proxyProfileDTO, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Customer_Organisation_Id:{},ProxyProfileId:{},ProxyProfileDTO:{},loggedInUser:{}", customer_org_id, id, proxyProfileDTO, loggedInUser);
        if (customer_org_id != null && id != null && proxyProfileDTO != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                String previous_host_machine = proxyProfileRepository.getHostMachineByProfileByProxyProfileId(id);
                log.info("previous_host_machine: {}", previous_host_machine);
                proxyProfileRepository.updateProxyProfileByProxyProfileId(proxyProfileDTO.getName(), proxyProfileDTO.getPublic_ip(), proxyProfileDTO.getHost_machine(), proxyProfileDTO.getTcp_port(), proxyProfileDTO.getUdp_port(), proxyProfileDTO.getSsl_enabled(), id);
                vdmsService.updateProxyServerHostSyncByVdmsId(1, proxyProfileDTO.getHost_machine(), httpServletRequest);
                vdmsService.updateProxyClientSyncByProxyProfileId(id, httpServletRequest);
                if (!previous_host_machine.equals(proxyProfileDTO.getHost_machine())) {
                    vdmsService.updateProxyServerHostSyncByVdmsId(1, previous_host_machine, httpServletRequest);
                }
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "UPDATE", "A proxy Profile Is Updated By Name:" + proxyProfileDTO.getName(), "success");
                log.info("Updating Proxy Profile By Proxy Profile Id:{},EndPoint:{}", id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> updateGlobalProxyProfileByProxyProfileId(String admin_email, ProxyProfileDTO proxyProfileDTO, String proxy_profile_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:proxy_profile_id:{},admin_email:{},ProxyProfileDTO:{},loggedInUser:{}", proxy_profile_id, admin_email, proxyProfileDTO, loggedInUser);
        if (admin_email != null && proxyProfileDTO != null && proxy_profile_id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                proxyProfileRepository.updateGlobalProxyProfileByProxyProfileId(proxyProfileDTO.getName(), proxyProfileDTO.getPublic_ip(), proxyProfileDTO.getTcp_port(), proxyProfileDTO.getUdp_port(), proxyProfileDTO.getSsl_enabled(), proxy_profile_id);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "UPDATE", "A Global proxy Profile Is Updated By Name:" + proxyProfileDTO.getName(), "success");
                log.info("Updating Global Proxy Profile By Proxy Profile Id:{},EndPoint:{}", proxy_profile_id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> deleteProxyProfileByProxyProfileId(String customer_org_id, String id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Customer_Organisation_Id:{},ProxyProfileId:{},loggedInUser:{}", customer_org_id, id, loggedInUser);
        if (customer_org_id != null && id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin");
            if (access) {
                String host_machine = proxyProfileRepository.getHostMachineByProfileByProxyProfileId(id);
                log.info("host_machine: {}", host_machine);
                vdmsService.untagAllProxyProfilesFromVdmsByProxyProfileId(id, httpServletRequest);
                vdmsService.updateProxyServerHostSyncByVdmsId(1, host_machine, httpServletRequest);
                proxyProfileRepository.deleteProxyProfileByProxyProfileId(id);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "DELETE", "A proxy Profile Is Deleted", "success");
                log.info("Deleting Proxy Profile By Proxy Profile Id.EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteProxyProfileByCustomerOrganisationId(String customer_org_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: customer_org_id: {}", customer_org_id);
        proxyProfileRepository.deleteProxyProfileByCustomerOrganisationId(customer_org_id);
        log.info("Deleted Proxy Profile By Customer_Organisation_Id:{},EndPoint:{}", customer_org_id, httpServletRequest.getRequestURI());
    }

    public ResponseEntity<?> deleteGlobalProxyProfileByProxyProfileId(String admin_email, String id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:admin_email:{},Id:{},loggedInUser:{}", admin_email, id, loggedInUser);
        if (admin_email != null && id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                proxyProfileRepository.deleteGlobalProxyProfileByProxyProfileId(id);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "DELETE", "A proxy Profile Is Deleted", "success");
                log.info("Deleting Global Proxy Profile By Proxy Profile Id:{},EndPoint:{}", id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "ProxyProfile", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> getVdmsProxyProfileByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}", vdms_id);
        if (vdms_id != null) {
            ProxyProfileDTO proxyProfileDTO = proxyProfileRepository.getVdmsProxyProfileByVdmsId(vdms_id);
            log.info("ProxyProfileDTO: {}", proxyProfileDTO);
            VdmsDTO vdmsDTO = vdmsService.getVdmsInfoByVdmsId(vdms_id, httpServletRequest);
            log.info("VdmsDTO: {}", vdmsDTO);
            if (proxyProfileDTO != null) {
                proxyProfileDTO.setLatitude(vdmsDTO.getLatitude());
                proxyProfileDTO.setLongitude(vdmsDTO.getLongitude());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(proxyProfileDTO, 200, true);
                log.info("Fetching vdms proxy profile by vdms Id: {}. Endpoint: {}", vdms_id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                log.info("Vdms Proxy profile does not exist.EndPoint:{}", httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

    }

    public ProxyProfileDTO getVdmsProxyProfileDataByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}", vdms_id);
        ProxyProfileDTO proxyProfileDTO = proxyProfileRepository.getVdmsProxyProfileByVdmsId(vdms_id);
        log.info("ProxyProfileDTO: {}", proxyProfileDTO);
        VdmsDTO vdmsDTO = vdmsService.getVdmsInfoByVdmsId(vdms_id, httpServletRequest);
        log.info("VdmsDTO: {}", vdmsDTO);
        if (proxyProfileDTO != null) {
            proxyProfileDTO.setLatitude(vdmsDTO.getLatitude());
            proxyProfileDTO.setLongitude(vdmsDTO.getLongitude());
        }
        log.info("Fetching vdms proxy profile data by vdms Id: {}. Endpoint: {}", vdms_id, httpServletRequest.getRequestURI());
        return proxyProfileDTO;
    }

}
