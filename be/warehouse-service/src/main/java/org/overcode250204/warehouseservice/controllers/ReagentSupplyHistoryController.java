package org.overcode250204.warehouseservice.controllers;


import org.overcode250204.base.BaseResponse;
import org.overcode250204.warehouseservice.services.interfaces.ReagentSupplyHistoryService;
import org.overcode250204.warehouseservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentSupplyHistoryRequest;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentSupplyHistoryResponse;
import org.overcode250204.warehouseservice.services.implement.ReagentSupplyHistoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reagents/supply-history")
public class ReagentSupplyHistoryController {

    @Value("${spring.application.name}")
    private String serviceName;
    @Autowired
    private ReagentSupplyHistoryService supplyService;

    @PreAuthorize("hasAuthority('REAGENT_CREATE')")
    @PostMapping
    public ResponseEntity<BaseResponse<ReagentSupplyHistoryResponse>> createReagentSupply(
            @RequestBody ReagentSupplyHistoryRequest request) {
        UUID receiveBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        ReagentSupplyHistoryResponse response = supplyService.createReagentSupply(request, receiveBy);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER', 'LAB_MANAGER','SERVICE_USER')")
    @GetMapping("/inventory")
    public ResponseEntity<BaseResponse<List<ReagentSupplyHistoryResponse>>> getReagentInventory() {
        List<ReagentSupplyHistoryResponse> responseList = supplyService.getReagentInventory();
        return ResponseEntity.ok(BaseResponse.success(serviceName, responseList));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER', 'LAB_MANAGER','SERVICE_USER')")
    //Lấy ra 1 cái hóa chất trong kho
    @GetMapping("/inventory/{reagentId}")
    public ResponseEntity<BaseResponse<List<ReagentSupplyHistoryResponse>>> getReagentInventoryById(@PathVariable UUID reagentId) {
        List<ReagentSupplyHistoryResponse> history = supplyService.getReagentInventory(reagentId);
        return ResponseEntity.ok(BaseResponse.success(serviceName, history));
    }
}
