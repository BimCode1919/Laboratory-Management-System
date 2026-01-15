package org.overcode250204.iamservice.services.auth.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.overcode250204.clients.IamClient;
import org.overcode250204.iamservice.entities.Role;
import org.overcode250204.iamservice.repositories.PrivilegeRepository;
import org.overcode250204.iamservice.repositories.RolePrivilegeRepository;
import org.overcode250204.iamservice.repositories.UserRepository;
import org.overcode250204.iamservice.services.auth.AuthorizationService;
import org.overcode250204.iamservice.services.privilegeservice.PrivilegeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationServiceImp implements AuthorizationService {


    private final UserRepository userRepository;

    private final PrivilegeRepository  privilegeRepository;

    private final RolePrivilegeRepository rolePrivilegeRepository;

    private final PrivilegeService privilegeService;

    public final IamClient iamClient;

    @Override
    public Role getRole() {
        return null;
    }

    @Override
    public Map<String, Object> getPrivilegesByRole(String role) throws Exception {
        List<String> privileges = rolePrivilegeRepository.findPrivilegeCodesByRoleCodes(List.of(role));

        if (privileges.isEmpty()) {
            log.info("Privileges of role {} is empty", role);
            return Map.of("role", role, "privileges", List.of());
        }

        return Map.of(
                "role", role,
                "privileges", privileges
        );
    }


}
