package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.dto.ExternalClientUserDTO;
import io.sclera.dto.QuickSearchDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.VdmsVisibilityRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class VdmsVisibilityService {

    @Autowired
    private VdmsVisibilityRepository vdmsvisibilityRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private VdmsService vdmsService;
    @Autowired
    private WebClientService webClientService;
    @Autowired
    private UserService userService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public void addVdmsVisibilityByEmail(String vdms_id, String property_name, String email, HttpServletRequest httpServletRequest) {
        log.info("Payload:Vdms_Id:{},Property_Name:{},Email:{}", vdms_id, property_name, email);
        String id = Generators.timeBasedGenerator().generate().toString();
        log.info("Adding Vdms Visibility By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        vdmsvisibilityRepository.addVdmsVisibilityByEmail(id, vdms_id, property_name, email);
    }

    public void deleteVdmsVisibilityByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Delete Vdms Visibility By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        vdmsvisibilityRepository.deleteVdmsVisibilityByVdmsId(vdms_id);
    }

    public void deleteVisibleVdmsByEmail(String email) {
        vdmsvisibilityRepository.deleteVisibleVdmsByEmail(email);
    }

    public ResponseEntity<?> deleteVisibleVdmsByEmail(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "master-vendor");
            if (access) {
                vdmsvisibilityRepository.deleteVisibleVdmsByEmail(email);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "DELETE", "A Visible VDMS Is Deleted", "success");
                log.info("Delete Vdms Visibility By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteVisibleVdmsByEmailAndVdmsId(String email, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Delete Vdms Visibility By Email:{} And Vdms_Id:{},EndPoint:{}", email, vdms_id, httpServletRequest.getRequestURI());
        vdmsvisibilityRepository.deleteVisibleVdmsByEmailAndVdmsId(email, vdms_id);
    }

    public List<String> getVisibleVdmsByEmail(String email, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityRepository.getVisibleVdmsIdsByEmail(email);
    }


    public ResponseEntity<?> getVisibleVdmsByEmail(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "master-vendor");
            if (access) {
                Set<VdmsDTO> vdmsDTOS = this.getVisibleVdms(email, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
                log.info("Fetching Visible Vdms By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
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

    public ResponseEntity<?> getVisibleVdmsByEmailForVendor(String email, HttpServletRequest httpServletRequest) {
        if (email != null) {
            Set<VdmsDTO> vdmsDTOS = this.getVisibleVdms(email, httpServletRequest);
            ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsDTOS, 200, true);
            log.info("Fetching Visible Vdms By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public Set<VdmsDTO> getVisibleVdms(String email, HttpServletRequest httpServletRequest) {
        log.info("Fetching Visible Vdms,Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        Set<VdmsDTO> vdmsDTOS = vdmsvisibilityRepository.getVisibleVdmsByEmail(email);
        return vdmsDTOS;
    }

    public void replaceVisibleVdmsByEmail(Set<QuickSearchDTO> vdmsdtos, String email, HttpServletRequest httpServletRequest) {
        log.info("Payload:QuickSearchDTO:{},Email:{}", vdmsdtos, email);
        log.info("Replace  Visible Vdms By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        vdmsvisibilityRepository.deleteVisibleVdmsByEmail(email);
        if (vdmsdtos != null && vdmsdtos.size() > 0) {
            for (QuickSearchDTO vdmsdto : vdmsdtos) {
                addVdmsVisibilityByEmail(vdmsdto.getVdms_id(), vdmsdto.getProperty_name(), email, httpServletRequest);
            }
        }
    }

//    public void addExternalClientVdmsVisibility(Set<String> vdmsIds, String email, String orgId, HttpServletRequest httpServletRequest) {
//        List<String> taggedVdms = new ArrayList<>();
//        List<String> unTaggedVdms = new ArrayList<>();
//        List<String> existingTaggedVdmsIds = vdmsvisibilityRepository.getVisibleVdmsIdsByEmail(email);
//        if (vdmsIds != null && !vdmsIds.isEmpty()) {
//            vdmsvisibilityRepository.deleteVisibleVdmsByEmail(email);
//            if (!vdmsIds.isEmpty()) {
//                for (String vdmsId : vdmsIds) {
//                    Set<String> vdmsRelatedToOrg = vdmsService.getVdmsIdByOrgId(orgId);
//                    if (vdmsRelatedToOrg.contains(vdmsId)) {
//                        String propertyName = vdmsService.getPropertyNameByVdmsId(vdmsId, httpServletRequest);
//                        addVdmsVisibilityByEmail(vdmsId, propertyName, email, httpServletRequest);
//                        taggedVdms.add(vdmsId);
//                    } else {
//                        unTaggedVdms.add(vdmsId);
//                    }
//                }
//            } else {
//                userActionLogService.addUserActionLog(email, "ExternalClientVdms", "DELETE", "Vdms:" + existingTaggedVdmsIds + " is Untagged.", "success");
//                vdmsvisibilityRepository.deleteVisibleVdmsByEmail(email);
//            }
//        } else {
//            if (!existingTaggedVdmsIds.isEmpty()) {
//                userActionLogService.addUserActionLog(email, "ExternalClientVdms", "DELETE", "Vdms:" + existingTaggedVdmsIds + " is Untagged.", "success");
//            }
//            vdmsvisibilityRepository.deleteVisibleVdmsByEmail(email);
//        }
//        userActionLogService.addUserActionLog(email, "ExternalClientVdms", "ADD", "Vdms:" + taggedVdms + " is tagged, and Vdms:" + unTaggedVdms + " Untagged because it belongs to other organisation.", "success");
//    }

    public ResponseEntity<?> replaceNewVisibleVdmsByEmail(Set<QuickSearchDTO> vdmsdtos, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:QuickSearchDTO:{},Email:{},loggedInUser:{}", vdmsdtos, email, loggedInUser);
        if (vdmsdtos != null && email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "master-vendor");
            if (access) {
                replaceVisibleVdmsByEmail(vdmsdtos, email, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "ADD", "A New Visible VDMS IS Replaced", "success");
                log.info("Replace New Visible Vdms By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> addVisibleVdmsByVendorOrganisationId(String organisation_id, String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (organisation_id != null && email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "master-vendor");
            if (access) {
                Set<QuickSearchDTO> visible_vdms = vdmsService.getVisibleVdmsByVendorOrganisationIdAndEmail(organisation_id, email, httpServletRequest);
                if (visible_vdms != null) {
                    replaceVisibleVdmsByEmail(visible_vdms, email, httpServletRequest);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "ADD", "A New Visible VDMS IS Added", "success");
                    log.info("Add Visible Vdms By Vendor Organisation_Id:{},Endpoint:{}", organisation_id, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Vdms Does Not Exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "ADD", "VDMS Does Not Exist.", "failed");
                    throw new ClientException("VDMS does not exist", 728, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMSVisibility", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> addVisibleVdmsByVendorOrganisationIdAndEmail(String orgId, String email, String body, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (orgId != null && email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-vendor", "vendor");
            if (access) {
                Set<QuickSearchDTO> visibleVdms = vdmsService.getQuickSearchListByOrganisationId(email, loggedInUser, httpServletRequest);
                if (visibleVdms != null) {
                    replaceVisibleVdmsByEmail(visibleVdms, body, httpServletRequest);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                    log.info("Add Visible Vdms By Vendor Organisation_Id:{} And Email:{},Endpoint:{}", orgId, email, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Vdms Does Not Exist.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("VDMS does not exist", 728, httpServletRequest.getRequestURI());
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

    public void addVdmsByVendorOrganisationId(String organisation_id, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId:{}, VdmsId:{}", organisation_id, vdms_id);
        String property_name = vdmsService.getPropertyNameByVdmsId(vdms_id, httpServletRequest);
        List<String> vendors = webClientService.getVendorEmailsByOrganisationId(organisation_id, vdms_id, httpServletRequest);
        log.info("Response from Vendor:{}", vendors);
        log.info("Fetching Vendor Emails By Organisation_Id:{} from Vendor Server", organisation_id);
        if (vendors != null && vendors.size() > 0) {
            for (String vendor : vendors) {
                Integer count = vdmsvisibilityRepository.checkVdmsVisibilityByVdmsIdAndPropertyName(vdms_id, property_name, vendor);
                if (count == 0) {
                    log.info("Add Vdms By Vendor Organisation_Id:{},Vdms_Id:{},EndPoint:{}", organisation_id, vdms_id, httpServletRequest.getRequestURI());
                    addVdmsVisibilityByEmail(vdms_id, property_name, vendor, httpServletRequest);
                }
            }
        }
    }

    public void addVdmsByInviteeVendorOrganisationId(String organisation_id, String vdms_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: OrgId:{}, VdmsId:{}, LoggedInUser:{}", organisation_id, vdms_id, loggedInUser);
        String property_name = vdmsService.getPropertyNameByVdmsId(vdms_id, httpServletRequest);
        List<String> vendors = webClientService.getVendorEmailsByOrganisationId(organisation_id, loggedInUser, httpServletRequest);
        log.info("Response from Vendor:{}", vendors);
        log.info("Fetching Vendor Emails By Organisation_Id:{} from Vendor Server", organisation_id);
        if (vendors != null && vendors.size() > 0) {
            for (String vendor : vendors) {
                Integer count = vdmsvisibilityRepository.checkVdmsVisibilityByVdmsIdAndPropertyName(vdms_id, property_name, vendor);
                if (count == 0) {
                    log.info("Add Vdms By Vendor Organisation_Id:{},Vdms_Id:{},EndPoint:{}", organisation_id, vdms_id, httpServletRequest.getRequestURI());
                    addVdmsVisibilityByEmail(vdms_id, property_name, vendor, httpServletRequest);
                }
            }
        }
    }

    public void addVdmsByUserOrganisationId(String organisation_id, String email, String vdms_id, HttpServletRequest httpServletRequest) {
        String property_name = vdmsService.getPropertyNameByVdmsId(vdms_id, httpServletRequest);
        Set<String> users = userService.getUserEmailsByUserOrganisationId(organisation_id, email, httpServletRequest);

        if (users != null && users.size() > 0) {
            for (String user : users) {
                addVdmsVisibilityByEmail(vdms_id, property_name, user, httpServletRequest);
            }
        }
    }

    public void editPropertyNameByVdmsId(String property_name, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Edit Property_Name:{} By Vdms_Id:{},EndPoint:{}", property_name, vdms_id, httpServletRequest.getRequestURI());
        vdmsvisibilityRepository.editPropertyNameByVdmsId(property_name, vdms_id);
    }

    public String checkVisibleVdmsByEmailAndVdmsId(String email, String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Check Visibility Vdms By Email:{} And Vdms_Id:{},EndPoint:{}", email, vdmsId, httpServletRequest.getRequestURI());
        return vdmsvisibilityRepository.checkVisibleVdmsByEmailAndVdmsId(email, vdmsId);
    }

    public void deleteVisibleVdmsByCustomerOrganisationIdAndVdmsId(String organisation_id, String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Delete Visible Vdms By Customer_Organisation_Id:{} And Vdms_Id:{},EndPoint:{}", organisation_id, vdms_id, httpServletRequest.getRequestURI());
        vdmsvisibilityRepository.deleteVisibleVdmsByCustomerOrganisationIdAndVdmsId(organisation_id, vdms_id);
    }

    public void updateVdmsAccessVisibilityService(String email, Integer fullAccess, HttpServletRequest httpServletRequest) {
        String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
        if (role.equalsIgnoreCase("user") || role.equalsIgnoreCase("property-admin")) {
            vdmsvisibilityRepository.deleteVisibleVdmsByEmail(email);
            String id = Generators.timeBasedGenerator().generate().toString();
            vdmsvisibilityRepository.addVdmsFullAccessVisibilityByEmail(id, email, fullAccess);
            userActionLogService.addUserActionLog(email, "VDMSVisibility", "ADD", "A New Visible VDMS IS Added", "success");
            log.info("Replaced Vdms Access Visibility By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        }
    }

    public Integer getVdmsFullAccessByEmail(String userEmail, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityRepository.getVdmsFullAccessByEmail(userEmail);
    }

    public ResponseEntity<?> tagPropertyByOrganisationIdAndVdmsId(String orgId, String email, List<String> vdmsIds, HttpServletRequest httpServletRequest) {
        log.info("Payload: orgId:{},email:{},vdmsIds:{}", orgId, email, vdmsIds);
        List<String> vdmsRelatedToOrg = vdmsService.getVdmsIdsByOrgIdAndVdmsIds(orgId, vdmsIds);
        vdmsvisibilityRepository.deleteVisibleVdmsByEmail(email);
        for (String vdmsId : vdmsRelatedToOrg) {
            String propertyName = vdmsService.getPropertyNameByVdmsId(vdmsId, httpServletRequest);
            addVdmsVisibilityByEmail(vdmsId, propertyName, email, httpServletRequest);
        }
        userActionLogService.addUserActionLog(email, "VDMS visibility", "UPDATE", vdmsIds + "has been tagged to the user " + email, "SUCCESS");
        ResponseDTO responseDTO = ScleraUtils.generatePayload("Tagged properties " + vdmsIds + " to user " + email, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getVisibleVdmsInfoByEmail(String orgId, String email, HttpServletRequest httpServletRequest) {
        log.info("Payload:orgId:{},email:{}", orgId, email);
        List<ExternalClientUserDTO> externalClientUserDTOS = vdmsvisibilityRepository.getVisibleVdmsInfoByEmail(email);
        System.out.println("external ===>" + externalClientUserDTOS);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(externalClientUserDTOS, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> getVisibleVdmsIdsByEmail(String email, HttpServletRequest httpServletRequest) {
        log.info("Payload:email:{}", email);
        List<String> vdmsIds = vdmsvisibilityRepository.getVisibleVdmsIdsByEmail(email);
        ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsIds, 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteVisibleVdmsByEmailAndVdmsIds(String orgId, String email, List<String> vdmsIds, HttpServletRequest httpServletRequest) {
        List<String> vdmsRelatedToOrg = vdmsService.getVdmsIdsByOrgIdAndVdmsIds(orgId, vdmsIds);

        vdmsvisibilityRepository.deleteVisibleVdmsByEmailAndVdmsIds(email, vdmsRelatedToOrg);

        ResponseDTO responseDTO = ScleraUtils.generatePayload("Profiles and Properties have been revoked for User" + email + "successfully", 200, true);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    public List<String> getEmailsByVdmsIds(List<String> vdmsIds, HttpServletRequest httpServletRequest) {
        return vdmsvisibilityRepository.getEmailsByVdmsIds(vdmsIds);
    }

    public Map<String, String> getVdmsFullAccessByEmails(List<String> emails) {

        List<Object[]> fullAccessByEmails = vdmsvisibilityRepository.getFullAccessByEmails(emails);

        // Convert results to a map
        Map<String, String> fullAccessMap = new HashMap<>();
        for (Object[] result : fullAccessByEmails) {
            String email = result[0].toString();
            String fullAccess = result[1].toString();
            fullAccessMap.put(email, fullAccess);
        }

        return fullAccessMap;
    }

    public ResponseEntity<?> getVisibleVdmsIdByEmail(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "master-vendor");
            if (access) {
                List<String> vdmsIds = vdmsvisibilityRepository.getVisibleVdmsIdsByEmail(email);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsIds, 200, true);
                log.info("Fetching Visible Vdms Id By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
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

    public ResponseEntity<?> getVisibleVdmsByUserEmail(String email, HttpServletRequest httpServletRequest) {
        log.info("Payload:Email:" + email);
        if (email != null) {
            String organisation_id = userService.getOrganisationIdByEmail(email, httpServletRequest);
            String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
            List<String> vdmsId = null;
            if (role.equalsIgnoreCase("master-user") || role.equalsIgnoreCase("org-admin")) {
                vdmsId = vdmsService.getAllVdmsIdByMasterUserOrganisationId(organisation_id);
            } else if (role.equalsIgnoreCase("user") || role.equalsIgnoreCase("property-admin")) {
                vdmsId = vdmsService.getAllVdmsIdByUserOrganisationIdAndUserEmail(organisation_id, email);
            }
            ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsId, 200, true);
            log.info("Fetching Visible Vdms By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }


    public void addExternalClientVdmsVisibility(Set<String> vdmsIds, String email, String orgId, HttpServletRequest request) {
        executorService.submit(() -> {
            try {
                processVdmsVisibility(vdmsIds, email, orgId);
            } catch (Exception e) {
                log.error("Error updating VDMS visibility for {}", email, e);
            }
        });
    }

    private void processVdmsVisibility(Set<String> vdmsIds, String email, String orgId) {
        vdmsvisibilityRepository.deleteVisibleVdmsByEmail(email);
        if (vdmsIds == null || vdmsIds.isEmpty()) {
            return;
        }
        Set<String> vdmsRelatedToOrg = vdmsService.getVdmsIdByOrgId(orgId);
        for (String vdmsId : vdmsIds) {
            if (vdmsRelatedToOrg.contains(vdmsId)) {
                addVdmsVisibilityByEmail(vdmsId, null, email);
            }
        }
    }

    public void addVdmsVisibilityByEmail(String vdms_id, String property_name, String email) {
        log.info("Payload:Vdms_Id:{},Property_Name:{},Email:{}", vdms_id, property_name, email);
        String id = Generators.timeBasedGenerator().generate().toString();
        log.info("Adding Vdms Visibility By Email:{}", email);
        property_name = vdmsService.getPropertyNameByVdmsId(vdms_id);
        vdmsvisibilityRepository.addVdmsVisibilityByEmail(id, vdms_id, property_name, email);
    }
}