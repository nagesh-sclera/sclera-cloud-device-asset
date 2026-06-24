package io.sclera.service;

import io.sclera.dto.ResponseDTO;
import io.sclera.dto.TouchscreenDTO;
import io.sclera.exception.ClientException;
import io.sclera.exception.ServerException;
import io.sclera.util.ScleraRoleCheckUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class VdmsPasswordTokenService {

    @Autowired
    private WebClientService webClientService;

    @Autowired
    private ScleraRoleCheckUtils scleraRoleCheckUtils;

    public ResponseEntity<?> getVdmsAccessTokenByRefreshToken(String vdmsId, TouchscreenDTO touchscreenDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload vdmsId: {} ,TouchscreenDTO: {}", vdmsId, touchscreenDTO);
        ResponseDTO responseDTO = webClientService.getVdmsAccessTokenByRefreshToken(vdmsId, touchscreenDTO, httpServletRequest);
        log.info("Response from Login:{}",responseDTO);
        if (responseDTO.getStatus() == 754) {
            log.error("Invalid Vdms refresh token. EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid Vdms refresh token", 754, httpServletRequest.getRequestURI());
        } else if (responseDTO.getStatus() == 755) {
            log.error("Vdms password not found. EndPoint {}", httpServletRequest.getRequestURI());
            throw new ClientException("Vdms password not found", 755, httpServletRequest.getRequestURI());
        } else if (responseDTO.getStatus() == 200) {
            log.info("Fetching Vdms Access Token By Refresh Token. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Unable to fetch Vdms access token by refresh token, Endpoint {}", httpServletRequest.getRequestURI());
            throw new ServerException("Unable to fetch Vdms access token by refresh token", 816, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getVdmsAccessTokenByVdmsIdAndPassword(String vdmsId, String password, HttpServletRequest httpServletRequest) {
        log.info("Payload vdmsId: {}", vdmsId);
        ResponseDTO responseDTO = webClientService.getVdmsAccessTokenByVdmsIdAndPassword(vdmsId, password, httpServletRequest);
        log.info("Response from Login:{}",responseDTO);
        if (responseDTO.getStatus() == 752) {
            log.error("Invalid Token Credentials. EndPoint {}", httpServletRequest.getRequestURI());
            throw new ServerException("Invalid Token Credentials", 752, httpServletRequest.getRequestURI());
        } else if (responseDTO.getStatus() == 200) {
            log.info("Fetching Vdms Access Token By VdmsId and Password. Endpoint: {}", httpServletRequest.getRequestURI());
            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } else {
            log.error("Unable to fetch Vdms access token EndPoint {}", httpServletRequest.getRequestURI());
            throw new ServerException("Unable to fetch Vdms access token", 817, httpServletRequest.getRequestURI());
        }
    }

    public ResponseEntity<?> getVdmsTokenPasswordByVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {
        log.info("Payload vdmsId: {}", vdmsId);
        ResponseDTO responseDTO = webClientService.getVdmsTokenPasswordByVdmsId(vdmsId, httpServletRequest);
        log.info("Response from Login:{}",responseDTO);
        log.info("Fetching Vdms Token Password By VdmsId. Endpoint: {}", httpServletRequest.getRequestURI());
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
