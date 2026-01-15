package org.overcode250204.instrumentservice.controllers;

import lombok.RequiredArgsConstructor;

import org.overcode250204.base.BaseResponse;
import org.overcode250204.instrumentservice.dtos.ChangeModeCommand;
import org.overcode250204.instrumentservice.dtos.InstrumentDTO;
import org.overcode250204.instrumentservice.dtos.ModeChangeResultDTO;
import org.overcode250204.instrumentservice.service.interfaces.InstrumentService;
import org.overcode250204.instrumentservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    @Value("${spring.application.name}")
    private String serviceName;

    private final InstrumentService instrumentService;


    @PostMapping("/{id}/mode")
    @PreAuthorize("hasAuthority('INSTRUMENT_UPDATE')")
    public ResponseEntity<BaseResponse<ModeChangeResultDTO>> changeMode(@PathVariable UUID id, @RequestBody ChangeModeCommand req) {
        UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        req.setUserId(userId);
        ModeChangeResultDTO res = instrumentService.changeMode(id, req);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<List<InstrumentDTO>>> getAllInstruments() {
        List<InstrumentDTO> res = instrumentService.getAllInstruments();
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<InstrumentDTO>> getInstrumentById(@PathVariable UUID id) {
        InstrumentDTO res = instrumentService.getInstrumentById(id);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @PostMapping("/{instrumentId}/configuration-sync")
    @PreAuthorize("hasAuthority('INSTRUMENT_UPDATE')")
    public ResponseEntity<?> configurationSync(@PathVariable UUID instrumentId) {
        JsonNode inboxNode = instrumentService.requestConfigurationSync(instrumentId);
        if (inboxNode != null) {
            return ResponseEntity.ok(BaseResponse.success(serviceName, inboxNode));
        }
        return ResponseEntity.accepted().body(BaseResponse.success(serviceName, Map.of("status", "request_sent")));
    }

    @PostMapping("/configuration-all-sync")
    @PreAuthorize("hasAuthority('INSTRUMENT_UPDATE')")
    public ResponseEntity<?> configurationAllSync() {
        JsonNode inboxNode = instrumentService.requestConfigurationAllSync();
        if (inboxNode != null) {
            return ResponseEntity.ok(BaseResponse.success(serviceName, inboxNode));
        }
        return ResponseEntity.accepted().body(BaseResponse.success(serviceName, Map.of("status", "request_sent")));
    }

}
