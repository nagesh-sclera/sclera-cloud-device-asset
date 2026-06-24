package io.sclera.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.uuid.Generators;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsIntegrationDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.VdmsIntegrationRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@Service
@Slf4j
public class VdmsIntegrationService {
    @Autowired
    private VdmsIntegrationRepository vdmsIntegrationRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private WebClientService webClientService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;


    public ResponseEntity<?> addVdmsIntegrationByAndVdmsIdAndId(VdmsIntegrationDTO vdmsIntegrationDTO, String org_id, String email, String vdms, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},Email:{},vdms:{},VdmsIntegrationDTO:{},loggedInUser:{}", org_id, email, vdms, vdmsIntegrationDTO, loggedInUser);
        if (vdmsIntegrationDTO != null && org_id != null && email != null && vdms != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                String id = Generators.timeBasedGenerator().generate().toString();
                vdmsIntegrationRepository.addVdmsIntegrationByAndVdmsIdAndId(id, vdms, vdmsIntegrationDTO.getHelperId());
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMSIntegration", "ADD", "A VDMS Integration Is Added By Name:" + vdmsIntegrationDTO.getName(), "success");
                log.info("Adding Vdms Integration By Vdms_id:{} and Id:{},EndPoint:{}", vdms, id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMSIntegration", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMSIntegration", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getAllVdmsIntegrationsByVdmsId(String org_id, String email, String vdms, Integer type, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},Email:{},vdms:{},loggedInUser:{}", org_id, email, vdms, loggedInUser);
        if (org_id != null && email != null && vdms != null && loggedInUser != null) {

            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                List<String> helperId = vdmsIntegrationRepository.getAllVdmsIntegrationsByVdmsId(vdms);
                log.info("HelperID's:{}",helperId);
                JSONObject helperData = webClientService.getAllHelperDetailsByHelperIds(helperId, type, loggedInUser, httpServletRequest);
                log.info("Response from Stores:{}",helperData);
                log.info("Fetching All Helper Details By type:{}", type);
                JSONArray helperDatas = helperData.getJSONArray("helperDTOS");
                JSONArray helperIds = helperData.getJSONArray("helperIds");
                List<VdmsIntegrationDTO> data = vdmsIntegrationRepository.getAllVdmsIntegrationsByVdmsIdAndHelperId(helperIds.toJavaList(String.class), vdms);
                List<VdmsIntegrationDTO> helperDto = JSON.parseArray(helperDatas.toJSONString(), VdmsIntegrationDTO.class);

                for (int i = 0; i < data.size(); i++) {
                    data.get(i).setDescription(helperDto.get(i).getDescription());
                    data.get(i).setImage_url(helperDto.get(i).getImage_url());
                    data.get(i).setHelper_manual_name(helperDto.get(i).getHelper_manual_name());
                    data.get(i).setType(helperDto.get(i).getType());
                }

                ResponseDTO responseDTO = ScleraUtils.generatePayload(data, 200, true);
                log.info("Fetching All Vdms Integration By Vdms_Id:{},EndPoint:{}", vdms, httpServletRequest.getRequestURI());
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

    public ResponseEntity<?> updateVdmsIntegrationActivityByVdmsIdAndId(String org_id, String email, String
            vdms, String id, Integer activity, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},Email:{},vdms:{},Id:{},loggedInUser:{}", org_id, email, vdms, id, loggedInUser);
        if (org_id != null && email != null && vdms != null && id != null && activity != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                vdmsIntegrationRepository.updateVdmsIntegrationActivityByVdmsIdAndId(activity, vdms, id);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMSIntegration", "UPDATE", "A VDMS Integration Is Updated", "success");
                log.info("Update Vdms Integration Activity By Vdms_Id:{},Id:{},EndPoint:{}", vdms, id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMSIntegration", "UPDATE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMSIntegration", "UPDATE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }

    public ResponseEntity<?> deleteVdmsIntegrationByVdmsIdAndId(String org_id, String email, String vdms, String
            id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Org_Id:{},Email:{},vdms:{},Id:{},loggedInUser:{}", org_id, email, vdms, id, loggedInUser);
        if (org_id != null && email != null && vdms != null && id != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin", "master-user", "org-admin", "user", "master-vendor", "vendor", "property-admin");
            if (access) {
                vdmsIntegrationRepository.deleteVdmsIntegrationByVdmsIdAndId(vdms, id);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VDMSIntegration", "DELETE", "A VDMS Integration Is Deleted", "success");
                log.info("Delete Vdms Integration By Vdms_Id:{},Id:{},EndPoint:{}", vdms, id, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VDMSIntegration", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }

        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VDMSIntegration", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteVdmsIntegrationByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("Delete Vdms Integration By Vdms_Id:{},EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
        vdmsIntegrationRepository.deleteVdmsIntegrationByVdmsId(vdms_id);
    }

}
