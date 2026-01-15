package org.overcode250204.iamservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.iamservice.services.privilege.PrivilegeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class PrivilegeController {
    private final PrivilegeService privilegeService;

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping("/privilege")
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<?> getAllPrivileges(){
        return ResponseEntity.ok(BaseResponse.success(serviceName, privilegeService.getAllPrivilege()));
    }
}
