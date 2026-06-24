package io.sclera.component;

import io.sclera.config.TenantJwtIssuerValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class TokenVerifier {

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    public TenantJwtIssuerValidator tenantJwtIssuerValidator;

    public boolean isTokenValid(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);

            // Validate the token using the TenantJwtIssuerValidator
            OAuth2TokenValidatorResult validationResult = tenantJwtIssuerValidator.validate(jwt);

            // Check if the validation result has errors
            if (validationResult.hasErrors()) {
                return false; // Token is invalid if there are errors
            }

            return true; // Token is valid if there are no errors
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
