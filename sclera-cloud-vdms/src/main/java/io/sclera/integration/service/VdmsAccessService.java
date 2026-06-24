package io.sclera.integration.service;


import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.sclera.dto.ProxyProfileDTO;
import io.sclera.exception.ClientException;
import io.sclera.integration.dto.IntegrationResponseDTO;
import io.sclera.integration.dto.VdmsAccessDTO;
import io.sclera.integration.repository.VdmsAccessRepository;

import io.sclera.repository.VdmsRepository;
import io.sclera.service.ProxyProfileService;
import io.sclera.service.WebClientService;
import io.sclera.util.ScleraUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Service
@Slf4j
public class VdmsAccessService {

    @Autowired
    private VdmsAccessRepository vdmsAccessRepository;

    @Autowired
    private VdmsRepository vdmsRepository;

    @Autowired
    private ProxyProfileService proxyProfileService;

    @Autowired
    private IntegrationUserService integrationUserService;




    @Autowired
    private WebClientService webClientService;

    public ResponseEntity<?> getVdmsAccessByUsername(String username, String key, int pageNo, int pageSize, HttpServletRequest httpServletRequest) {
        log.info("Payload:Username:{},Key:{},Page_No:{},Page_Size:{}", username, key, pageNo, pageSize);
        String orgId = integrationUserService.getOrgIdByUsername(username);
        if (orgId != null) {
            int offset = ScleraUtils.calculateOffset(pageNo, pageSize);
            log.info("Fetching All Vdms Info By Username:{},EndPoint:{}", username, httpServletRequest.getRequestURI());
            Set<VdmsAccessDTO> vdmsAccessDTOS = vdmsAccessRepository.getVdmsAccessByUsername(username, orgId, key, pageSize, offset);
            if (vdmsAccessDTOS != null) {
                for (VdmsAccessDTO vdmsAccessDTO : vdmsAccessDTOS) {
                    String primaryProxyProfileId = vdmsRepository.getPrimaryProxyProfileByVdmsId(vdmsAccessDTO.getVdmsId());
                    setProxyProfileForUsers(primaryProxyProfileId, vdmsAccessDTO, httpServletRequest);
                }
            }
            IntegrationResponseDTO integrationResponseDTO = ScleraUtils.generateIntegrationPayload(vdmsAccessDTOS, 0, true);
            return new ResponseEntity<>(integrationResponseDTO, HttpStatus.OK);
        } else {
            log.error("User Not Found.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("User Not Found", 701, httpServletRequest.getRequestURI());
        }
    }


    public void setProxyProfileForUsers(String primaryProxyProfileId, VdmsAccessDTO vdmsAccessDTO, HttpServletRequest httpServletRequest) {
        log.info("Payload:primaryProxyProfileId:{},VdmsDTO:{}", primaryProxyProfileId, vdmsAccessDTO);
        if (primaryProxyProfileId != null) {
            ProxyProfileDTO proxyProfileDTO = proxyProfileService.getProxyProfileByProxyProfileId(primaryProxyProfileId, httpServletRequest);
            log.info("Set Proxy Profile For Users.EndPoint:{}", httpServletRequest.getRequestURI());
            if (proxyProfileDTO.getSsl_enabled() == 0) {
                vdmsAccessDTO.setUrl("http://" + proxyProfileDTO.getPublic_ip() + ":" + proxyProfileDTO.getTcp_port());
            } else if (proxyProfileDTO.getSsl_enabled() == 1) {
                vdmsAccessDTO.setUrl("https://" + proxyProfileDTO.getPublic_ip() + ":" + proxyProfileDTO.getTcp_port());
            }
        } else {
            log.info("No Proxy profile present for vdms.EndPoint:{}", httpServletRequest.getRequestURI());
        }
    }




}

