package org.overcode250204.warehouseservice.controllers;


import org.overcode250204.warehouseservice.services.interfaces.ReagentSupplyService;
import org.overcode250204.warehouseservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentRequest;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentResponse;
import org.overcode250204.warehouseservice.services.implement.ReagentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.overcode250204.base.BaseResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/reagents")
public class ReagentController {

    @Value("${spring.application.name}")
    private String serviceName;
    @Autowired
    private ReagentSupplyService reagentService;

    @PostMapping
    @PreAuthorize("hasAuthority('REAGENT_CREATE')")
    public ResponseEntity<BaseResponse<ReagentResponse>> createReagent(@RequestBody ReagentRequest request) {
        UUID createdBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        ReagentResponse response = reagentService.createReagent(request, createdBy);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('REAGENT_UPDATE')")
    public ResponseEntity<BaseResponse<ReagentResponse>> updateReagent(
            @PathVariable UUID id,
            @RequestBody ReagentRequest request) {
        UUID updatedBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());

        ReagentResponse response = reagentService.updateReagents(request, id, updatedBy);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('REAGENT_DELETE')")
    public ResponseEntity<BaseResponse<String>> deleteReagent(@PathVariable UUID id) {
        UUID deletedBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        String message = reagentService.deleteReagents(id, deletedBy);
        return ResponseEntity.ok(BaseResponse.success(serviceName, message));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER', 'LAB_MANAGER','SERVICE_USER')")
    public ResponseEntity<BaseResponse<List<ReagentResponse>>> getAllReagents() {
        List<ReagentResponse> response = reagentService.getAllReagents();
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }

}
