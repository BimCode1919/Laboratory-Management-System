package org.overcode250204.iamservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.iamservice.dto.role.RoleDTO;
import org.overcode250204.iamservice.dto.role.RoleListDTO;
import org.overcode250204.iamservice.dto.role.RoleUpdateDTO;
import org.overcode250204.iamservice.services.role.RoleService;
import org.overcode250204.iamservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Value("${spring.application.name}")
    private String serviceName;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleDTO dto) {
        String createdBy = AuthUtils.getCurrentUser().getPrincipal().toString();
        return ResponseEntity.ok(BaseResponse.success(serviceName, roleService.createRole(dto, createdBy)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public ResponseEntity<?> getAllRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "name", required = false) String sortBy,
            @RequestParam(defaultValue = "asc", required = false) String order) {
        List<RoleListDTO> roles = roleService.getAllRoles(keyword, sortBy, order);
        return ResponseEntity.ok(BaseResponse.success(serviceName, roles));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<?> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleUpdateDTO dto) {
        String updatedBy = AuthUtils.getCurrentUser().getPrincipal().toString();
        return ResponseEntity.ok(BaseResponse.success(serviceName, roleService.updateRole(id, dto, updatedBy)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_DELETE')")
    public ResponseEntity<?> deleteRole(@PathVariable UUID id) {
        String deletedBy = AuthUtils.getCurrentUser().getPrincipal().toString();
        return ResponseEntity.ok(BaseResponse.success(serviceName, roleService.deleteRole(id, deletedBy)));
    }
}
