package org.overcode250204.iamservice.services.role;

import org.overcode250204.iamservice.dto.role.RoleDTO;
import org.overcode250204.iamservice.dto.role.RoleListDTO;
import org.overcode250204.iamservice.dto.role.RoleUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    String createRole(RoleDTO dto, String createdBy);
    List<RoleListDTO> getAllRoles(String keyword, String sortBy, String order);
    String updateRole(UUID id, RoleUpdateDTO dto, String updatedBy);
    String deleteRole(UUID id, String deletedBy);
}
