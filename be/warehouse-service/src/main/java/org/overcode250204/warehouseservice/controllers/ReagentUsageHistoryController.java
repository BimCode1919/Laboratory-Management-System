package org.overcode250204.warehouseservice.controllers;

import org.overcode250204.base.BaseResponse;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentUsageResponse;
import org.overcode250204.warehouseservice.services.interfaces.ReagentUsageHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usage")
public class ReagentUsageHistoryController {
    @Value("${spring.application.name}")
    private String serviceName;
    @Autowired
    private ReagentUsageHistoryService usageService;

    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER', 'LAB_MANAGER','SERVICE_USER')")
    @GetMapping
    public ResponseEntity<BaseResponse<List<ReagentUsageResponse>>> getAllHistory() {
        List<ReagentUsageResponse> history = usageService.getAllHistory();
        return ResponseEntity.ok(BaseResponse.success(serviceName, history));
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER', 'LAB_MANAGER','SERVICE_USER')")
    @GetMapping("/{reagentId}")
    public ResponseEntity<BaseResponse<List<ReagentUsageResponse>>> getHistoryByReagent(@PathVariable UUID reagentId) {
        List<ReagentUsageResponse> history = usageService.getHistoryByReagent(reagentId);
        if (history.isEmpty()) {
            return ResponseEntity.ok(BaseResponse.success(serviceName, List.of()));
        }
        return ResponseEntity.ok(BaseResponse.success(serviceName, history));
    }

    @PreAuthorize("hasAuthority('REAGENT_DELETE')")

    @DeleteMapping("/{usageId}")
    public ResponseEntity<BaseResponse<Void>> deleteUsageHistory(@PathVariable UUID usageId) {
        usageService.deleteUsageHistory(usageId);
        return ResponseEntity.ok(BaseResponse.success(serviceName, null));
    }
}
