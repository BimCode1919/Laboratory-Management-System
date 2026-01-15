package org.overcode250204.testorderservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.dtos.FlaggingRulesDTO;
import org.overcode250204.testorderservice.services.FlaggingRulesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/config/flag-rule")
public class FlaggingRulesController {
    private final FlaggingRulesService flaggingRulesService;

    private static final String SERVICE_NAME = "FlaggingRulesService";

    @GetMapping
    public ResponseEntity<?> getAllFlaggingRules() {
        List<FlaggingRulesDTO> rules = flaggingRulesService.findAll();
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, rules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFlaggingRuleById(@PathVariable UUID id) {
        FlaggingRulesDTO rule = flaggingRulesService.findById(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, rule));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addFlaggingRules(@RequestBody FlaggingRulesDTO request) {
        FlaggingRulesDTO created = flaggingRulesService.create(request);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, created));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateFlaggingRules(
            @PathVariable UUID id,
            @RequestBody FlaggingRulesDTO request) {
        FlaggingRulesDTO updated = flaggingRulesService.update(id, request);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, updated));
    }

    @PutMapping("/disable/{id}")
    public ResponseEntity<?> disableFlaggingRules(@PathVariable UUID id) {
        FlaggingRulesDTO disabled = flaggingRulesService.disable(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, disabled));
    }

    @PutMapping("/enable/{id}")
    public ResponseEntity<?> enableFlaggingRules(@PathVariable UUID id) {
        FlaggingRulesDTO enabled = flaggingRulesService.enable(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, enabled));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFlaggingRules(@PathVariable UUID id) {
        flaggingRulesService.delete(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, null));
    }
}