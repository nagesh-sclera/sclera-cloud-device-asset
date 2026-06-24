package io.sclera.service;

import com.fasterxml.uuid.Generators;
import io.sclera.dto.ResponseDTO;
import io.sclera.dto.VdmsAccessVisibilityDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.VdmsAccessVisibilityRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class VdmsAccessVisibilityService {

    @Autowired
    private VdmsAccessVisibilityRepository vdmsAccessVisibilityRepository;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;

    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private UserService userService;

    public ResponseEntity<ResponseDTO> replaceVdmsAccessVisibilityByEmail(String email, List<String> devUIds, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (email != null && devUIds != null && loggedInUser != null) {
            log.info("Payload: Email: {}, vdmsAccessVisibilityDTOS: {}, LoggedInUser: {}", email, devUIds, loggedInUser);
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin");
            if (access) {
                String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
                if (role.equalsIgnoreCase("admin")) {
                    vdmsAccessVisibilityRepository.deleteVdmsAccessVisibilityByEmail(email);
                    if (!devUIds.isEmpty()) {
                        for (String devUId : devUIds) {
                            String id = Generators.timeBasedGenerator().generate().toString();
                            vdmsAccessVisibilityRepository.addVdmsAccessVisibilityByEmail(id, devUId, email);
                        }
                    }
                    ResponseDTO responseDTO = ScleraUtils.generatePayload("Replaced Vdms Access Visibility", 200, true);
                    userActionLogService.addUserActionLog(loggedInUser, "VdmsAccessVisibility", "ADD", "A VDMS Access Visibility IS Replaced", "success");
                    log.info("Replaced Vdms Access Visibility By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                    userActionLogService.addUserActionLog(loggedInUser, "VdmsAccessVisibility", "ADD", "Role Not Authorised", "failed");
                    throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
                }
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VdmsAccessVisibility", "ADD", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VdmsAccessVisibility", "ADD", "Invalid Client Parameters", "failed");
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<ResponseDTO> deleteVdmsAccessVisibilityByEmail(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: Email: {}, LoggedInUser: {}", email, loggedInUser);
        boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin");
        if (access) {
            String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
            if (role.equalsIgnoreCase("admin")) {
                deleteVdmsAccessVisibilityByEmail(email, httpServletRequest);
                ResponseDTO responseDTO = ScleraUtils.generatePayload("Deleted Vdms Access Visibility", 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "VdmsAccessVisibility", "DELETE", "A VDMS Access Visibility Is Deleted", "success");
                log.info("Deleted Vdms Access Visibility By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "VdmsAccessVisibility", "DELETE", "Role Not Authorised", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "VdmsAccessVisibility", "DELETE", "Role Not Authorised", "failed");
            throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
        }
    }

    public void deleteVdmsAccessVisibilityByEmail(String email, HttpServletRequest httpServletRequest) {
        vdmsAccessVisibilityRepository.deleteVdmsAccessVisibilityByEmail(email);
        log.info("Deleted Vdms Access visibility For Email: {}. Endpoint: {}", email, httpServletRequest.getRequestURI());
    }

    public ResponseEntity<ResponseDTO> getVdmsAccessVisibilityByEmail(String email, String loggedInUser, HttpServletRequest httpServletRequest) {
        if (email != null && loggedInUser != null) {
            log.info("Payload: Email: {}, LoggedInUser: {}", email, loggedInUser);
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin");
            if (access) {
                String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
                if (role.equalsIgnoreCase("admin")) {
                    List<VdmsAccessVisibilityDTO> vdmsAccessVisibilityDTOS = getVdmsAccessVisibilityByEmail(email, httpServletRequest);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(vdmsAccessVisibilityDTOS, 200, true);
                    log.info("Fetching Vdms Access Visibility By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                    throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
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

    public List<VdmsAccessVisibilityDTO> getVdmsAccessVisibilityByEmail(String email, HttpServletRequest httpServletRequest) {
        List<VdmsAccessVisibilityDTO> vdmsAccessVisibilityDTOS = vdmsAccessVisibilityRepository.getVdmsAccessVisibilityByEmail(email);
        log.info("Fetching Vdms Access Visibility By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        return vdmsAccessVisibilityDTOS;
    }

    public Integer getVdmsFullAccessByEmail(String email, HttpServletRequest httpServletRequest) {
        return vdmsAccessVisibilityRepository.getVdmsFullAccessByEmail(email);

    }

    public void updateVdmsAccessVisibilityService(String email, Integer fullAccess, HttpServletRequest httpServletRequest) {
        String role = userService.getRoleNameByUserEmail(email, httpServletRequest);
        if (role.equalsIgnoreCase("super-admin") ||role.equalsIgnoreCase("admin")) {
            vdmsAccessVisibilityRepository.deleteVdmsAccessVisibilityByEmail(email);
            String id = Generators.timeBasedGenerator().generate().toString();
            vdmsAccessVisibilityRepository.addVdmsFullAccessVisibilityByEmail(id, email, fullAccess);
            userActionLogService.addUserActionLog(email, "VdmsAccessVisibility", "ADD", "A VDMS Access Visibility IS Replaced", "success");
            log.info("Replaced Vdms Access Visibility By Email:{},EndPoint:{}", email, httpServletRequest.getRequestURI());
        }
    }

}
