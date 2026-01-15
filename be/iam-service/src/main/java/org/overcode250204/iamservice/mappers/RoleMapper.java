package org.overcode250204.iamservice.mappers;

import org.overcode250204.iamservice.dto.role.PrivilegeDTO;
import org.overcode250204.iamservice.dto.role.RoleListDTO;
import org.overcode250204.iamservice.entities.Privilege;
import org.overcode250204.iamservice.entities.Role;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public RoleListDTO toRoleListResponse(Role role) {
        List<PrivilegeDTO> privileges = Optional.ofNullable(role.getRolePrivileges())
                .orElse(Collections.emptyList())
                .stream()
                .map(rp -> toPrivilegeResponse(rp.getPrivilege()))
                .collect(Collectors.toList());

        return RoleListDTO.builder()
                .id(role.getId())
                .code(role.getCode())
                .name(role.getName())
                .description(role.getDescription())
                .privileges(privileges)
                .build();
    }

    public PrivilegeDTO toPrivilegeResponse(Privilege privilege) {
        return PrivilegeDTO.builder()
                .id(privilege.getId())
                .code(privilege.getCode())
                .name(privilege.getName())
                .build();
    }
}
