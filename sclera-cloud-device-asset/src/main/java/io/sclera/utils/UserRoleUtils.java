package io.sclera.utils;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserRoleUtils {

    public final Map<String, String> roles = new HashMap<>();

    public String getRoles(String email) {
        String role = roles.get(email);
        return role;
    }

    public void setRoles(String email, String role) {
        roles.put(email, role);
    }

    public void deleteRole(String email) {
        roles.remove(email);
    }
}

