package org.overcode250204.iamservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.iamservice.services.auth.AuthorizationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationService authorizationService;

    @Value("${spring.application.name}")
    private String serviceName;


    @GetMapping("/users/{role}/privileges")
    public BaseResponse<?> getPrivileges(@PathVariable("role") String role) throws Exception {
        Map<String, Object> response = authorizationService.getPrivilegesByRole(role);
        return BaseResponse.success(serviceName, response);
    }

}
