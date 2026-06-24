package io.sclera.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class ScleraRoleCheckUtils {

//    @Autowired
//    private JwtPublicKeyProvider jwtPublicKeyProvider;

    public boolean checkRole(String email, HttpServletRequest httpServletRequest,String ... roles) {
//        String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);
//        String token = authorizationHeader.substring("Bearer ".length());
//        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwtPublicKeyProvider.getPublicKey(), null);
//        JWTVerifier verifier = JWT.require(algorithm).build();
//        DecodedJWT decodedJWT = verifier.verify(token);
//        String tokenEmail = decodedJWT.getSubject();
//        String tokenRole = String.valueOf(decodedJWT.getClaim("role")).substring(1, String.valueOf(decodedJWT.getClaim("role")).length() - 1);
//
//        boolean emailMatch = email.equals(tokenEmail);
//        boolean roleMatch =  Arrays.asList(roles).contains(tokenRole);

//        return emailMatch && roleMatch;
        return true;
    }

    public boolean checkVdmsId(String vdmsId, HttpServletRequest httpServletRequest) {

        String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);
        String token = authorizationHeader.substring("Bearer ".length());

        DecodedJWT decodedJWT = JWT.decode(token);
        String tokenVdmsId = decodedJWT.getClaim("vdmsId").asString();

        log.info("Payload vdmsId:{}",vdmsId);
        log.info("Token Vdms Id:{}",tokenVdmsId);

        return vdmsId.equals(tokenVdmsId);
    }



}
