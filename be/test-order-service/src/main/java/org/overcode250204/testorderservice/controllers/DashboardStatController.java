package org.overcode250204.testorderservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.dtos.dashboard.OrdersOverTimeDTO;
import org.overcode250204.testorderservice.dtos.dashboard.OrdersTypeCountDTO;
import org.overcode250204.testorderservice.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardStatController {
    private final DashboardService dashboardService;

    @GetMapping("/orders-over-time")
    public ResponseEntity<BaseResponse<List<OrdersOverTimeDTO>>> getOrdersOverTime(
            @RequestParam(defaultValue = "7") int days) {
        List<OrdersOverTimeDTO> data = dashboardService.getOrdersOverTime(days);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", data));
    }

    @GetMapping("/orders-by-type")
    public ResponseEntity<BaseResponse<List<OrdersTypeCountDTO>>> getOrdersByType() {
        List<OrdersTypeCountDTO> data = dashboardService.getOrdersCountByType();
        return ResponseEntity.ok(BaseResponse.success("test-order-service", data));
    }
}
