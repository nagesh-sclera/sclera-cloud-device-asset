package io.sclera.service;

import io.sclera.dto.P2PRemoteSessionDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.AdminP2PRemoteSessionRepository;
import io.sclera.util.ScleraRoleCheckUtils;
import io.sclera.util.ScleraUtils;
import io.sclera.util.SocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@Service
@Slf4j
public class AdminP2PRemoteSessionService {

    @Autowired
    private AdminP2PRemoteSessionRepository adminP2PRemoteSessionRepository;
    @Autowired
    private UserActionLogService userActionLogService;
    @Autowired
    private SocketUtils socketUtils;

    @Autowired
    private Environment environment;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;


    public ResponseEntity<?> getPortAndSessionIdByAdminEmailAndDevUID(String admin_email, String devuid, String access_port, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:admin_email:{},devuid:{},access_port:{},loggedInUser:{}", admin_email, devuid, access_port, loggedInUser);
        if (admin_email != null && devuid != null && access_port != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                P2PRemoteSessionDTO p2PRemoteSessionDTO = adminP2PRemoteSessionRepository.getPortAndSessionIdByAdminEmailAndDevUID(admin_email, devuid, access_port);
                log.info("P2PRemoteSessionDTO: {}", p2PRemoteSessionDTO);
                if (p2PRemoteSessionDTO != null) {
                    if (p2PRemoteSessionDTO.getPort() + 1 < 65535) {
                        Map<String, Object> adminP2PRemoteSessionDTO = new HashMap<>();

                        Integer session_id = getRandomSessionId(httpServletRequest);

                        adminP2PRemoteSessionDTO.put("session_id", session_id);
                        adminP2PRemoteSessionDTO.put("port", p2PRemoteSessionDTO.getPort() + 1);
                        adminP2PRemoteSessionDTO.put("system_port", Integer.parseInt(access_port));
                        adminP2PRemoteSessionDTO.put("stun_ip", environment.getProperty("stun.ip"));
                        adminP2PRemoteSessionDTO.put("stun_port", environment.getProperty("stun.port"));
                        adminP2PRemoteSessionDTO.put("turn_ip", environment.getProperty("turn.ip"));
                        adminP2PRemoteSessionDTO.put("turn_port", environment.getProperty("turn.port"));

                        log.info("AdminP2PRemoteSessionDTO: {}", adminP2PRemoteSessionDTO);
                        adminP2PRemoteSessionRepository.updatePortByAdminEmailAndDevUID(p2PRemoteSessionDTO.getPort() + 1, admin_email, devuid);
                        socketUtils.invokeWebSocketEndpoint("/topic/response-" + devuid, adminP2PRemoteSessionDTO);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(adminP2PRemoteSessionDTO, 200, true);
                        log.info("Get Port And Session Id By Admin Email:{} And Dev_UID:{}.EndPoint:{}", admin_email, devuid, httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    } else {
                        log.error("Error! Max port reached , Clearing all Sessions.EndPoint:{}", httpServletRequest.getRequestURI());
                        adminP2PRemoteSessionRepository.clearAllSessions();
                        return this.getPortAndSessionIdByAdminEmailAndDevUID(admin_email, devuid, access_port, loggedInUser, httpServletRequest);
                    }
                } else {
                    Integer port = adminP2PRemoteSessionRepository.getMaxRemotePort();
                    log.info("Port: {}", port);
                    if (port != null) {
                        if (port + 10 < 65535) {
                            adminP2PRemoteSessionRepository.addAdminP2PRemoteSessionByAdminEmailAndDevUID(admin_email, devuid, getRandomSessionId(httpServletRequest), port + 10);

                            Integer session_id = getAdminP2PRemoteSessionIdByAdminEmailAndDevUID(admin_email, devuid, httpServletRequest);

                            Map<String, Object> adminP2PRemoteSessionDTO = new HashMap<>();

                            adminP2PRemoteSessionDTO.put("session_id", session_id);
                            adminP2PRemoteSessionDTO.put("port", port + 10);
                            adminP2PRemoteSessionDTO.put("system_port", Integer.parseInt(access_port));
                            adminP2PRemoteSessionDTO.put("stun_ip", environment.getProperty("stun.ip"));
                            adminP2PRemoteSessionDTO.put("stun_port", environment.getProperty("stun.port"));
                            adminP2PRemoteSessionDTO.put("turn_ip", environment.getProperty("turn.ip"));
                            adminP2PRemoteSessionDTO.put("turn_port", environment.getProperty("turn.port"));

                            log.info("AdminP2PRemoteSessionDTO: {}", adminP2PRemoteSessionDTO);
                            socketUtils.invokeWebSocketEndpoint("/topic/response-" + devuid, adminP2PRemoteSessionDTO);
                            ResponseDTO responseDTO = ScleraUtils.generatePayload(adminP2PRemoteSessionDTO, 200, true);
                            log.info("Get Port And Session Id By Admin Email:{} And Dev_UID:{}.EndPoint:{}", admin_email, devuid, httpServletRequest.getRequestURI());
                            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                        } else {
                            log.error("Error! Max port reached , Clearing all Sessions.EndPoint:{}", httpServletRequest.getRequestURI());
                            adminP2PRemoteSessionRepository.clearAllSessions();
                            return this.getPortAndSessionIdByAdminEmailAndDevUID(admin_email, devuid, access_port, loggedInUser, httpServletRequest);
                        }
                    } else {
                        port = 60010;

                        adminP2PRemoteSessionRepository.addAdminP2PRemoteSessionByAdminEmailAndDevUID(admin_email, devuid, getRandomSessionId(httpServletRequest), port + 10);

                        Integer session_id = getAdminP2PRemoteSessionIdByAdminEmailAndDevUID(admin_email, devuid, httpServletRequest);

                        Map<String, Object> adminP2PRemoteSessionDTO = new HashMap<>();

                        adminP2PRemoteSessionDTO.put("session_id", session_id);
                        adminP2PRemoteSessionDTO.put("port", port);
                        adminP2PRemoteSessionDTO.put("system_port", Integer.parseInt(access_port));
                        adminP2PRemoteSessionDTO.put("stun_ip", environment.getProperty("stun.ip"));
                        adminP2PRemoteSessionDTO.put("stun_port", environment.getProperty("stun.port"));
                        adminP2PRemoteSessionDTO.put("turn_ip", environment.getProperty("turn.ip"));
                        adminP2PRemoteSessionDTO.put("turn_port", environment.getProperty("turn.port"));

                        log.info("AdminP2PRemoteSessionDTO: {}", adminP2PRemoteSessionDTO);
                        socketUtils.invokeWebSocketEndpoint("/topic/response-" + devuid, adminP2PRemoteSessionDTO);
                        ResponseDTO responseDTO = ScleraUtils.generatePayload(adminP2PRemoteSessionDTO, 200, true);
                        log.info("Get Port And Session Id By Admin Email:{} And Dev_UID:{}.EndPoint:{}", admin_email, devuid, httpServletRequest.getRequestURI());
                        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                    }
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

    public Integer getAdminP2PRemoteSessionIdByAdminEmailAndDevUID(String admin_email, String udi, HttpServletRequest httpServletRequest) {
        log.info("Fetching Admin P2P Remote Session id By Admin Email:{},Dev_Uid:{},Endpoint:{}", admin_email, udi, httpServletRequest.getRequestURI());
        return adminP2PRemoteSessionRepository.getAdminP2PRemoteSessionIdByAdminEmailAndDevUID(admin_email, udi);
    }


    public Integer getRandomSessionId(HttpServletRequest httpServletRequest) {
        Random r = new Random();
        int low = 1;
        int high = 65535;
        log.info("Fetching Random Session_Id.EndPoint:{}", httpServletRequest.getRequestURI());
        Integer result;
        do {
            result = r.nextInt(high - low) + low;
        } while ((high - low) == 0);
        return result;
    }


    public ResponseEntity<?> clearSessionsByAdminEmail(String admin_email, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload: admin_email: {}, loggedInUser: {}", admin_email, loggedInUser);
        if (admin_email != null && loggedInUser != null) {
            boolean access = scleraRoleCheckUtils.checkRole(loggedInUser, httpServletRequest, "super-admin", "admin");
            if (access) {
                adminP2PRemoteSessionRepository.clearSessionsByAdminEmail(admin_email);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(null, 200, true);
                userActionLogService.addUserActionLog(loggedInUser, "AdminP2PRemoteSession", "DELETE", "A Remote Session Is Removed", "success");
                log.info("Clear Sessions By Admin Email:{}.EndPoint:{}", admin_email, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                log.error("Role not authorized.EndPoint:{}", httpServletRequest.getRequestURI());
                userActionLogService.addUserActionLog(loggedInUser, "AdminP2PRemoteSession", "DELETE", "Role Not Authorized", "failed");
                throw new ClientException("Role not Authorised ", 702, httpServletRequest.getRequestURI());
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            userActionLogService.addUserActionLog(loggedInUser, "AdminP2PRemoteSession", "DELETE", "Invalid Client Parameters", "failed");
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }

    }
}
