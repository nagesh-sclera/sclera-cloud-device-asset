package io.sclera.service;

import io.sclera.dto.P2PRemoteSessionDTO;
import io.sclera.dto.ResponseDTO;
import io.sclera.exception.ClientException;
import io.sclera.repository.P2PRemoteSessionRepository;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Slf4j
public class P2PRemoteSessionService {

    @Autowired
    private P2PRemoteSessionRepository p2premotesessionRepository;
    @Autowired
    private Environment environment;

    @Autowired
    private SocketUtils socketUtils;

    public ResponseEntity<?> getPortAndSessionIdByVdmsIdAndVendorEmail(String vendor_email, String vdms_id, String loggedInUser, HttpServletRequest httpServletRequest) {
        log.info("Payload:Vendor_Email:{},Vdms_Id:{},loggedInUser:{}", vendor_email, vdms_id, loggedInUser);
        if (vdms_id != null && vendor_email != null && loggedInUser != null) {
            P2PRemoteSessionDTO p2premotesessiondto = p2premotesessionRepository.getP2PRemoteSessionByVdmsIdAndVendorEmail(vdms_id, vendor_email);
            log.info("P2PRemoteSessionDTO :" + p2premotesessiondto);

            if (p2premotesessiondto != null) {
                Map<String, Object> p2pRemoteSessionDTO = new HashMap<>();

                p2pRemoteSessionDTO.put("session_id", p2premotesessiondto.getSession_id());
                p2pRemoteSessionDTO.put("port", p2premotesessiondto.getPort());
                p2pRemoteSessionDTO.put("stun_ip", environment.getProperty("stun.ip"));
                p2pRemoteSessionDTO.put("stun_port", environment.getProperty("stun.port"));
                p2pRemoteSessionDTO.put("turn_ip", environment.getProperty("turn.ip"));
                p2pRemoteSessionDTO.put("turn_port", environment.getProperty("turn.port"));

                log.info("p2pRemoteSessionDTO: {}", p2pRemoteSessionDTO);
                log.info("Invoking socket for VDMS : {}", vdms_id);
                socketUtils.invokeWebSocketEndpoint("/topic/response-" + vdms_id, p2pRemoteSessionDTO);
                ResponseDTO responseDTO = ScleraUtils.generatePayload(p2pRemoteSessionDTO, 200, true);
                log.info("Fetching port and session Id by vdmsId: {} and vendor email: {}. Endpoint: {}", vdms_id, vendor_email, httpServletRequest.getRequestURI());
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            } else {
                Integer port = p2premotesessionRepository.getMaxRemotePort();
                log.info("Port: {}", port);

                if (port != null) {
                    p2premotesessionRepository.addP2PRemoteSessionByVdmsIdAndVendorEmail(vdms_id, vendor_email, getRandomSessionId(httpServletRequest), port + 10);

                    Integer session_id = getP2PRemoteSessionIdByVdmsIdAndVendorEmail(vdms_id, vendor_email, httpServletRequest);

                    Map<String, Object> p2pRemoteSessionDTO = new HashMap<>();

                    p2pRemoteSessionDTO.put("session_id", session_id);
                    p2pRemoteSessionDTO.put("port", port + 10);
                    p2pRemoteSessionDTO.put("stun_ip", environment.getProperty("stun.ip"));
                    p2pRemoteSessionDTO.put("stun_port", environment.getProperty("stun.port"));
                    p2pRemoteSessionDTO.put("turn_ip", environment.getProperty("turn.ip"));
                    p2pRemoteSessionDTO.put("turn_port", environment.getProperty("turn.port"));

                    log.info("p2pRemoteSessionDTO: {}", p2pRemoteSessionDTO);
                    log.info("Invoking socket for VDMS : {}", vdms_id);
                    socketUtils.invokeWebSocketEndpoint("/topic/response-" + vdms_id, p2pRemoteSessionDTO);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(p2pRemoteSessionDTO, 200, true);
                    log.info("Fetching port and session Id by vdmsId: {} and vendor email: {}. Endpoint: {}", vdms_id, vendor_email, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                } else {
                    port = 30010;

                    p2premotesessionRepository.addP2PRemoteSessionByVdmsIdAndVendorEmail(vdms_id, vendor_email, getRandomSessionId(httpServletRequest), port + 10);

                    Integer session_id = getP2PRemoteSessionIdByVdmsIdAndVendorEmail(vdms_id, vendor_email, httpServletRequest);

                    Map<String, Object> p2pRemoteSessionDTO = new HashMap<>();

                    p2pRemoteSessionDTO.put("session_id", session_id);
                    p2pRemoteSessionDTO.put("port", port);
                    p2pRemoteSessionDTO.put("stun_ip", environment.getProperty("stun.ip"));
                    p2pRemoteSessionDTO.put("stun_port", environment.getProperty("stun.port"));
                    p2pRemoteSessionDTO.put("turn_ip", environment.getProperty("turn.ip"));
                    p2pRemoteSessionDTO.put("turn_port", environment.getProperty("turn.port"));

                    log.info("p2pRemoteSessionDTO: {}", p2pRemoteSessionDTO);
                    log.info("Invoking socket for VDMS : {}", vdms_id);
                    socketUtils.invokeWebSocketEndpoint("/topic/response-" + vdms_id, p2pRemoteSessionDTO);
                    ResponseDTO responseDTO = ScleraUtils.generatePayload(p2pRemoteSessionDTO, 200, true);
                    log.info("Fetching port and session Id by vdmsId: {} and vendor email: {}. Endpoint: {}", vdms_id, vendor_email, httpServletRequest.getRequestURI());
                    return new ResponseEntity<>(responseDTO, HttpStatus.OK);
                }
            }
        } else {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Client param exception", 700, httpServletRequest.getRequestURI());
        }
    }

    public void deleteP2PRemoteSessionByVdmsId(String vdms_id, HttpServletRequest httpServletRequest) {
        log.info("VdmsId: {}", vdms_id);
        p2premotesessionRepository.deleteP2PRemoteSessionByVdmsId(vdms_id);
        log.info("Delete P2P Remote Session By Vdms_Id:{}.EndPoint:{}", vdms_id, httpServletRequest.getRequestURI());
    }

    public Integer getP2PRemoteSessionIdByVdmsIdAndVendorEmail(String vdms_id, String vendor_email, HttpServletRequest httpServletRequest) {
        log.info("Payload: VdmsId: {}, VendorEmail: {}", vdms_id, vendor_email);
        log.info("Fetching P2P Remote Session By Vdms_Id:{} And Vendor_Email:{},EndPoint:{}", vdms_id, vendor_email, httpServletRequest.getRequestURI());
        return p2premotesessionRepository.getP2PRemoteSessionIdByVdmsIdAndVendorEmail(vdms_id, vendor_email);
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

}
