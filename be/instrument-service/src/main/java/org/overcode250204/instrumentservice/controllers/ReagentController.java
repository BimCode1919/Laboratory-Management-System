package org.overcode250204.instrumentservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.instrumentservice.dtos.InstallReagentCommand;
import org.overcode250204.instrumentservice.dtos.ReagentDTO;
import org.overcode250204.instrumentservice.dtos.UninstallReagentCommand;
import org.overcode250204.instrumentservice.dtos.UpdateReagentInUseCommand;
import org.overcode250204.instrumentservice.service.interfaces.ReagentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/instruments/{instrumentId}/reagents")
@RequiredArgsConstructor
@Slf4j
public class ReagentController {

    @Value("${spring.application.name}")
    private String serviceName;

    private final ReagentService reagentService;

    @GetMapping
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<List<ReagentDTO>>> getInstalledReagents(@PathVariable UUID instrumentId) {
        List<ReagentDTO> reagents = reagentService.getInstalledReagents(instrumentId);
        return ResponseEntity.ok(BaseResponse.success(serviceName, reagents));
    }

    @GetMapping("/{reagentId}")
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<ReagentDTO>> getReagent(@PathVariable UUID instrumentId, @PathVariable UUID reagentId) {
        ReagentDTO dto = reagentService.getReagent(instrumentId, reagentId);
        return ResponseEntity.ok(BaseResponse.success(serviceName, dto));
    }

    @PatchMapping("/{reagentId}/in-use")
    @PreAuthorize("hasAuthority('REAGENT_UPDATE')")
    public ResponseEntity<BaseResponse<ReagentDTO>> updateInUse(@PathVariable UUID instrumentId, @PathVariable UUID reagentId,
                                                                @RequestBody UpdateReagentInUseCommand cmd) {
        ReagentDTO dto = reagentService.updateReagentInUse(instrumentId, reagentId, cmd);
        return ResponseEntity.ok(BaseResponse.success(serviceName, dto));
    }

    @PostMapping("/install")
    @PreAuthorize("hasAuthority('REAGENT_CREATE')")
    public ResponseEntity<BaseResponse<Object>> installReagent(@PathVariable UUID instrumentId,
                                                               @RequestBody InstallReagentCommand cmd) {
        JsonNode inboxNode = reagentService.requestInstallReagent(instrumentId, cmd);
        if (inboxNode != null) {
            return ResponseEntity.ok(BaseResponse.success(serviceName, inboxNode));
        }
        // Timed out waiting for warehouse response; return accepted with status
        return ResponseEntity.accepted().body(BaseResponse.success(serviceName, Map.of("status", "request_sent")));
    }

    @PostMapping("/uninstall")
    @PreAuthorize("hasAuthority('REAGENT_UPDATE')")
    public ResponseEntity<BaseResponse<Map<String, String>>> uninstallReagent(@PathVariable UUID instrumentId,
                                                                              @RequestBody UninstallReagentCommand cmd) {
        reagentService.requestUninstallReagent(instrumentId, cmd);
        return ResponseEntity.accepted().body(BaseResponse.success(serviceName, Map.of("status", "request_sent")));
    }

    @PostMapping("/sync")
    @PreAuthorize("hasAuthority('INSTRUMENT_UPDATE')")
    public ResponseEntity<BaseResponse<Object>> syncReagent(@PathVariable UUID instrumentId) {
        JsonNode inboxNode = reagentService.requestSyncReagent(instrumentId);
        if (inboxNode != null) {
            return ResponseEntity.ok(BaseResponse.success(serviceName, inboxNode));
        }
        return ResponseEntity.accepted().body(BaseResponse.success(serviceName, Map.of("status", "request_sent")));
    }

}