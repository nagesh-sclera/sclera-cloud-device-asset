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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;


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

    public Boolean checkRoleOfUser(String role) {
        List<String> roles = utils.getRestrictedRoles();

        if (roles.contains(role)) {
            return false;
        }
        return true;
    }


    public void updateLogForVDMSActivity(String email_id) {
        try {
//            userActionLogService.updateLoginActivity(email_id, "access");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

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

    public String extractEmailFromToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        if (jwt.getClaim("email").isNull()) {
            return null;
        }
        return jwt.getClaim("email").asString();
    }


}
