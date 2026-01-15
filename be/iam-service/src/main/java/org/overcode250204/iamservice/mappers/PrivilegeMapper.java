package org.overcode250204.iamservice.mappers;

import org.overcode250204.iamservice.dto.privilege.PrivilegeDTO;
import org.overcode250204.iamservice.entities.Privilege;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrivilegeMapper {

    public PrivilegeDTO toPrivilegeDTO(Privilege privilege){
        if (privilege == null) {
            return null;
        }
        return PrivilegeDTO.builder()
                .id(privilege.getId())
                .name(privilege.getName())
                .build();
    }

    public List<PrivilegeDTO> toPrivilegeDTOList(List<Privilege> privileges) {
        if (privileges == null) {
            return Collections.emptyList();
        }
        return privileges.stream()
                .map(this::toPrivilegeDTO)
                .collect(Collectors.toList());
    }
}
