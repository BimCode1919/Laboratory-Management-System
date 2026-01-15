package org.overcode250204.testorderservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.dtos.ResultParameterMappingDTO;
import org.overcode250204.testorderservice.services.ResultParameterMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/config/param-map")
public class ResultParameterMappingController {
    private final ResultParameterMappingService resultParameterMappingService;

    private static final String SERVICE_NAME = "ResultParameterMappingService";

    @GetMapping
    public ResponseEntity<?> getAllParamMapping() {
        List<ResultParameterMappingDTO> mappings = resultParameterMappingService.findAll();
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, mappings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getParamMapping(@PathVariable UUID id) {
        ResultParameterMappingDTO mapping = resultParameterMappingService.findById(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, mapping));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addParamMapping(@RequestBody ResultParameterMappingDTO request) {
        ResultParameterMappingDTO created = resultParameterMappingService.save(request);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, created));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateParamMapping(
            @PathVariable UUID id,
            @RequestBody ResultParameterMappingDTO request) {
        ResultParameterMappingDTO updated = resultParameterMappingService.update(id, request);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, updated));
    }

    @PutMapping("/disable/{id}")
    public ResponseEntity<?> disableParamMapping(@PathVariable UUID id) {
        ResultParameterMappingDTO disabled = resultParameterMappingService.disable(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, disabled));
    }

    @PutMapping("/enable/{id}")
    public ResponseEntity<?> enableParamMapping(@PathVariable UUID id) {
        ResultParameterMappingDTO enabled = resultParameterMappingService.enable(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, enabled));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteParamMapping(@PathVariable UUID id) {
        resultParameterMappingService.delete(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, null));
    }
}