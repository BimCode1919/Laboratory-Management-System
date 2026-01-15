package org.overcode250204.iamservice.controllers;


import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.iamservice.services.dashboard.DashboardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardStatController {

    private final DashboardService dashboardService;

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping("/role-count")
    public ResponseEntity<?> getRoleUserCounts() {
        return ResponseEntity.ok(BaseResponse.success(serviceName, dashboardService.getUserCountByRole()));
    }

    @GetMapping("/user-status")
    public ResponseEntity<?> getUserStatusCounts() {
        return ResponseEntity.ok(
                BaseResponse.success(serviceName, dashboardService.getUserStatusCounts())
        );
    }
}
