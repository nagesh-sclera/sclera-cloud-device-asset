package io.sclera.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.sclera.service.UserActionLogService;
import io.sclera.service.UserService;
import io.sclera.utils.UserRoleUtils;
import io.sclera.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


/**
 * Servlet filter (active outside the {@code docker} profile) that runs after the request is processed
 * and inspects the bearer token to record VDMS access activity for non-restricted users.
 */
@Component
@org.springframework.context.annotation.Profile("!docker")
public class JwtRequestFilter extends OncePerRequestFilter {


    @Autowired
    Utils utils;

    @Autowired
    UserActionLogService userActionLogService;

    @Autowired
    UserRoleUtils userRoleUtils;

    @Autowired
    UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        filterChain.doFilter(request, response);
        this.checkForTokenEmailForVDMSAccess(request);
    }

    /**
     * Determines whether the given role is permitted, returning {@code false} for roles
     * configured as restricted.
     *
     * @param role the user role to check
     * @return {@code true} if the role is not restricted, {@code false} otherwise
     */
    public Boolean checkRoleOfUser(String role) {
        List<String> roles = utils.getRestrictedRoles();

        if (roles.contains(role)) {
            return false;
        }
        return true;
    }


    /**
     * Records VDMS login/access activity for the given user, swallowing any error that occurs.
     *
     * @param email_id the email identifying the user
     */
    public void updateLogForVDMSActivity(String email_id) {
        try {
//            userActionLogService.updateLoginActivity(email_id, "access");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Extracts the email from the request's bearer token, resolves and caches the user's roles, and
     * records VDMS access activity when the user's role is not restricted.
     *
     * @param httpServletRequest the incoming HTTP request carrying the Authorization header
     */
    public void checkForTokenEmailForVDMSAccess(HttpServletRequest httpServletRequest) {
        String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring("Bearer ".length());

                String email_id = this.extractEmailFromToken(token);
                String roles = userRoleUtils.getRoles(email_id);

                System.out.println("---------- User roles ----------" + email_id);
                if (roles == null) {
                    String roles_db = userService.getAllUserRoles(email_id);
                    userRoleUtils.setRoles(email_id, roles_db);
                    System.out.println(userRoleUtils.getRoles(email_id) + "user rolesssss");
                    roles = roles_db;
                }
                if (email_id != null) {
                    if (checkRoleOfUser(roles)) {
                        this.updateLogForVDMSActivity(email_id);
                    }
                }

            }
        } catch (IllegalArgumentException | JWTVerificationException e) {
            System.out.println(e);
        }
    }

    /**
     * Decodes the given JWT and returns the value of its {@code email} claim.
     *
     * @param token the encoded JWT
     * @return the email claim value, or {@code null} if the claim is absent
     */
    public String extractEmailFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        if (jwt.getClaim("email").isNull()) {
            return null;
        }
        return jwt.getClaim("email").asString();
    }


}
