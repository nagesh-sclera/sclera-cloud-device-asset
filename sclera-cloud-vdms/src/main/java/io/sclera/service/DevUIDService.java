package io.sclera.service;

import io.sclera.dto.DevUIDDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.DevUIDRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigInteger;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DevUIDService {

    @Autowired
    private DevUIDRepository devUIDRepository;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;
    @Autowired
    private UserActionLogService userActionLogService;

    @Autowired
    private UserService userService;
    @Autowired
    private VdmsAccessVisibilityService vdmsAccessVisibilityService;

    public void addDevUID(String devuid, HttpServletRequest httpServletRequest) {
        log.info("Payload: UID: {}", devuid);
        BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
        devUIDRepository.addDevUID(devuid, creation_time);
        log.info("Added Device UID:{},Endpoint:{}", devuid, httpServletRequest.getRequestURI());
    }

    public void updateDevUID(String devuid, HttpServletRequest httpServletRequest) {
        log.info("Payload: devuid: {}", devuid);
        BigInteger creation_time = BigInteger.valueOf(System.currentTimeMillis());
        devUIDRepository.updateDevUID(creation_time, devuid);
        log.info("Updated Device UID:{},Endpoint:{}", devuid, httpServletRequest.getRequestURI());
    }

    public void upsertDevUID(String devuid, HttpServletRequest httpServletRequest) {
        log.info("Payload: devuid: {}", devuid);
        String db_uid = devUIDRepository.getDevUID(devuid);
        if (db_uid != null) {
            log.info("Updated Device UID:{},Endpoint:{}", db_uid, httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(null, "DevUID", "UPDATE", "A Device UID Is Updated:Id:" + db_uid, "success");
            updateDevUID(db_uid, httpServletRequest);
        } else {
            log.info("Added Device UID:{},Endpoint:{}", devuid, httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(null, "DevUID", "ADD", "A Device UID Is Added:Id:" + devuid, "success");
            addDevUID(devuid, httpServletRequest);
        }
    }

    public ResponseEntity<?> getAllDevUIDs(String key, String sort, int pageNo, int pageSize, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Key:{},Sort:{},Page_No:{},Page_Size:{},loggedInUser:{}", key, sort, pageNo, pageSize, loggedInUser);
            if (loggedInUser != null) {
                boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
                if (access) {
                   String role = userService.getRoleNameByUserEmail(loggedInUser, httpServletRequest);
                   if (role.equalsIgnoreCase("super-admin")){
                       int offset = ScleraUtils.calculateOffset(pageNo, pageSize);
                       Set<DevUIDDTO> devUIDs = devUIDRepository.getAllDevUIDs(key, sort, pageSize, offset);
                       if (devUIDs != null) {
                           for (DevUIDDTO devUID : devUIDs) {
                               if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(devUID.getLast_seen().longValueExact()) > 60) {
                                   devUID.setDevuid_status("0");    // DevUID offline
                               } else {
                                   devUID.setDevuid_status("1");        // DevUID online
                               }
                           }
                       }
                       ResponseDTO responseDTO = ScleraUtils.generatePayload(devUIDs, 200, true);
                       log.info("Get All Device  UIDs:{}.EndPoint:{}", devUIDs, httpServletRequest.getRequestURI());
                       return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                   } else if (role.equalsIgnoreCase("admin")) {
                       int offset = ScleraUtils.calculateOffset(pageNo, pageSize);
                       Integer fullAccess = vdmsAccessVisibilityService.getVdmsFullAccessByEmail(loggedInUser, httpServletRequest);
                       if (fullAccess != null && fullAccess.equals(1)) {
                           Set<DevUIDDTO> devUIDs = devUIDRepository.getAllDevUIDs(key, sort, pageSize, offset);
                           if (devUIDs != null) {
                               for (DevUIDDTO devUID : devUIDs) {
                                   if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(devUID.getLast_seen().longValueExact()) > 60) {
                                       devUID.setDevuid_status("0");    // DevUID offline
                                   } else {
                                       devUID.setDevuid_status("1");        // DevUID online
                                   }
                               }
                           }
                           ResponseDTO responseDTO = ScleraUtils.generatePayload(devUIDs, 200, true);
                           log.info("Get All Device  UIDs:{}.EndPoint:{}", devUIDs, httpServletRequest.getRequestURI());
                           log.info("1.Vdms Full Access:{}", fullAccess);
                           return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                       } else {
                           Set<DevUIDDTO> devUIDs = devUIDRepository.getVisibleDevUIDs(loggedInUser, key, sort, pageSize, offset);
                           if (devUIDs != null) {
                               for (DevUIDDTO devUID : devUIDs) {
                                   if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) - TimeUnit.MILLISECONDS.toMinutes(devUID.getLast_seen().longValueExact()) > 60) {
                                       devUID.setDevuid_status("0");    // DevUID offline
                                   } else {
                                       devUID.setDevuid_status("1");        // DevUID online
                                   }
                               }
                           }
                           ResponseDTO responseDTO = ScleraUtils.generatePayload(devUIDs, 200, true);
                           log.info("Get All Device  UIDs:{}.EndPoint:{}", devUIDs, httpServletRequest.getRequestURI());
                           log.info("0.Vdms Full Access:{}", fullAccess);
                           return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                       }
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
                throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
            }
    }

    public ResponseEntity<?> deleteDevUIDs(Set<String> devUIDs, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: devUIDs: {}, loggedInUser: {}", devUIDs, loggedInUser);
        if (devUIDs != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                devUIDRepository.deleteDevUIDs(devUIDs);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "DevUID", "DELETE", "A Device UID Is Deleted", "success");
                log.info("Successfully Deleted Device UIDs:{}.EndPoint:{}", devUIDs, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "DevUID", "DELETE", "Role Not Authorized", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "DevUID", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }
}
