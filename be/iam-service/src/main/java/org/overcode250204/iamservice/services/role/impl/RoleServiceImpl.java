package org.overcode250204.iamservice.services.role.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.overcode250204.iamservice.dto.role.RoleDTO;
import org.overcode250204.iamservice.dto.role.RoleListDTO;
import org.overcode250204.iamservice.dto.role.RoleUpdateDTO;
import org.overcode250204.iamservice.entities.Privilege;
import org.overcode250204.iamservice.entities.Role;
import org.overcode250204.iamservice.entities.RolePrivilege;
import org.overcode250204.iamservice.entities.UserRole;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.overcode250204.iamservice.mappers.RoleMapper;
import org.overcode250204.iamservice.repositories.PrivilegeRepository;
import org.overcode250204.iamservice.repositories.RolePrivilegeRepository;
import org.overcode250204.iamservice.repositories.RoleRepository;
import org.overcode250204.iamservice.repositories.UserRoleRepository;
import org.overcode250204.iamservice.services.role.RoleService;
import org.overcode250204.iamservice.utils.AuditLogUtils;
import org.overcode250204.iamservice.utils.MaskData;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PrivilegeRepository privilegeRepository;
    private final RolePrivilegeRepository rolePrivilegeRepository;
    private final RoleMapper roleMapper;
    private final UserRoleRepository userRoleRepository;
    private final AuditLogUtils auditLogUtils;

    @Override
    @Transactional
    public String createRole(RoleDTO dto, String createdBy) {
        if (roleRepository.existsByCode(dto.getCode())) {
            throw new IamServiceException(ErrorCode.ROLE_CODE_EXISTED);
        }
        List<Privilege> privileges;
        if (dto.getPrivilegeIds() == null || dto.getPrivilegeIds().isEmpty()) {
            privileges = List.of(
                    privilegeRepository.findByCode("READ_ONLY")
                            .orElseThrow(() -> new IamServiceException(ErrorCode.PRIVILEGE_DEFAULT_NOT_FOUND))
            );
        } else {
            privileges = privilegeRepository.findAllById(dto.getPrivilegeIds());
            if (privileges.isEmpty())
                throw new IamServiceException(ErrorCode.INVALID_PRIVILEGE_ID);
        }
        Role role = Role.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
        roleRepository.save(role);
        List<RolePrivilege> rolePrivileges = privileges.stream()
                .map(p -> RolePrivilege.builder()
                        .role(role)
                        .privilege(p)
                        .build())
                .collect(Collectors.toList());
        rolePrivilegeRepository.saveAll(rolePrivileges);

        auditLogUtils.createAuditOutboxEvent(
                "ROLE_CREATE",
                role.getId().toString(),
                "IAM_ROLE_CREATED",
                createdBy.toString(),
                Map.of(
                        "roleCode", dto.getCode(),
                        "roleName", dto.getName(),
                        "privileges", privileges.stream().map(Privilege::getCode).collect(Collectors.toList())
                )
        );
        return "Create role successful!";
    }

    @Override
    public List<RoleListDTO> getAllRoles(String keyword, String sortBy, String order) {
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        List<Role> roles;
        if (keyword != null && !keyword.trim().isEmpty()) {
            roles = roleRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(keyword, keyword, sort);
        } else {
            roles = roleRepository.findAll(sort);
        }
        return roles.stream()
                .map(roleMapper::toRoleListResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String updateRole(UUID id, RoleUpdateDTO dto, String updatedBy) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IamServiceException(ErrorCode.ROLE_NOT_FOUND));
        List<Long> privilegeIds = Optional.ofNullable(dto.getPrivilegeIds())
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<Privilege> privileges = privilegeIds.isEmpty()
                ? List.of(privilegeRepository.findByCode("READ_ONLY")
                .orElseThrow(() -> new IamServiceException(ErrorCode.PRIVILEGE_DEFAULT_NOT_FOUND)))
                : privilegeRepository.findAllById(privilegeIds);
        if (privileges.isEmpty()) {
            throw new IamServiceException(ErrorCode.INVALID_PRIVILEGE_ID);
        }
        List<String> changes = new ArrayList<>();
        String newName = dto.getName();
        if (newName != null) {
            changes.add(String.format("Field 'name' was updated (from: '%s' to: '%s')",
                    MaskData.maskData(role.getName(), false), MaskData.maskData(newName, false)));
            role.setName(newName);
        }
//        role.setName(dto.getName());
//        role.setDescription(dto.getDescription());
        String newDesc = dto.getDescription();
        if (newDesc != null) {
            changes.add(String.format("Field 'description' was updated (from: '%s' to: '%s')",
                    MaskData.maskData(role.getDescription(), false), MaskData.maskData(newDesc, false)));
            role.setDescription(newDesc);
        }
        rolePrivilegeRepository.deleteByRoleId(role.getId());
        List<RolePrivilege> newPrivileges = privileges.stream()
                .map(priv -> RolePrivilege.builder()
                        .role(role)
                        .privilege(priv)
                        .build())
                .toList();
        rolePrivilegeRepository.saveAll(newPrivileges);
        roleRepository.save(role);

        if (!changes.isEmpty()) {
            auditLogUtils.createAuditOutboxEvent(
                    "ROLE_UPDATE",
                    role.getId().toString(),
                    "IAM_ROLE_UPDATED",
                    updatedBy,
                    Map.of("changes", changes)
            );
        }
        return "Update role successfully!";
    }

    @Override
    public String deleteRole(UUID id, String deletedBy) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new IamServiceException(ErrorCode.ROLE_NOT_FOUND));
        List<UserRole> assignedUsers = userRoleRepository.findAllByRoleId(role.getId());
        if (!assignedUsers.isEmpty()) {
            userRoleRepository.deleteAll(assignedUsers);
        }
        rolePrivilegeRepository.deleteAll(role.getRolePrivileges());
        roleRepository.delete(role);

        auditLogUtils.createAuditOutboxEvent(
                "ROLE_DELETE",
                role.getId().toString(),
                "IAM_ROLE_DELETED",
                deletedBy,
                Map.of("roleCode", role.getCode())
        );

        return "Delete role successfully!";
    }
}
