package org.overcode250204.warehouseservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.warehouseservice.model.dto.dashboard.ReagentStockLevelDTO;
import org.overcode250204.warehouseservice.model.dto.dashboard.SupplyUsageTrendDTO;
import org.overcode250204.warehouseservice.services.interfaces.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardStatController {
    private final DashboardService dashboardService;

    @GetMapping("/reagent-stock-levels")
    public ResponseEntity<?> getReagentStockLevels() {
        List<ReagentStockLevelDTO> data = dashboardService.getReagentStockLevels();
        return ResponseEntity.ok(BaseResponse.success("warehouse-service", data));
    }

    @GetMapping("/reagent-supply-usage-trend")
    public ResponseEntity<?> getSupplyUsageTrend(
            @RequestParam(defaultValue = "30") int days) {
        List<SupplyUsageTrendDTO> data = dashboardService.getSupplyUsageTrend(days);
        return ResponseEntity.ok(BaseResponse.success("warehouse-service", data));
    }
}
