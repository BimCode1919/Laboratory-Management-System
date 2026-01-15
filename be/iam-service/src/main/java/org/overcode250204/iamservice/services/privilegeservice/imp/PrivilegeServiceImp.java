package org.overcode250204.iamservice.services.privilegeservice.imp;

import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.repositories.RolePrivilegeRepository;
import org.overcode250204.iamservice.repositories.UserRepository;
import org.overcode250204.iamservice.services.privilegeservice.PrivilegeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrivilegeServiceImp implements PrivilegeService {

    private final UserRepository userRepository;

    private final RolePrivilegeRepository rolePrivilegeRepository;

    @Override
    public List<String> getPrivilegesByUserName(String username) {
        List<String> roleCodes = userRepository.findRoleCodesByUsername(username);
        if (roleCodes.isEmpty()) {
            return List.of();
        }
        return rolePrivilegeRepository.findPrivilegeCodesByRoleCodes(roleCodes);
    }
}
