package io.sclera.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TokenValidationEventListener {

    @EventListener
    public void handleAuthenticationFailureEvent(AbstractAuthenticationFailureEvent event) {
        Throwable exception = event.getException();
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException authException = (OAuth2AuthenticationException) exception;
            if (authException.getCause() instanceof JwtException) {
                JwtException jwtException = (JwtException) authException.getCause();
                String token  = event.getAuthentication().getPrincipal().toString();

                if (token != null) {
                    String email = extractEmailFromToken(token);
                    String vdmsId = extractVdmsIdFromToken(token);
                    log.error("######################################################################################");
                    log.error("Token : {}",token);
                    log.error("Email in token: {}", email);
                    log.error("VDMS Id in token: {}", vdmsId);
                    log.error("Token exception: {}", jwtException.getMessage());
                    log.error("######################################################################################");
                }
            }
        }
    }

    private String extractVdmsIdFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        if (jwt.getClaim("vdmsId").isNull()) {
            return null;
        }
        return jwt.getClaim("vdmsId").asString();
    }

    public String extractEmailFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        if (jwt.getClaim("email").isNull()) {
            return null;
        }
        return jwt.getClaim("email").asString();
    }
}