package org.overcode250204.iamservice.services.auth;

import org.overcode250204.iamservice.entities.Role;


import java.util.Map;

public interface AuthorizationService {
    Role getRole();
    Map<String, Object> getPrivilegesByRole(String role) throws Exception;
}
