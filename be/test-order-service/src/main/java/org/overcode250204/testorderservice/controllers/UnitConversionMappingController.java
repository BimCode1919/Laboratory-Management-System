package org.overcode250204.testorderservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.dtos.UnitConversionMappingDTO;
import org.overcode250204.testorderservice.services.UnitConversionMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/config/unit-map")
public class UnitConversionMappingController {
    private final UnitConversionMappingService unitConversionMappingService;

    private static final String SERVICE_NAME = "UnitConversionMappingService";

    @GetMapping
    public ResponseEntity<?> getAllUnitConversionMap() {
        List<UnitConversionMappingDTO> mappings = unitConversionMappingService.findAll();
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, mappings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUnitConversionMapping(@PathVariable UUID id) {
        UnitConversionMappingDTO mapping = unitConversionMappingService.findById(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, mapping));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addUnitConversionMapping(@RequestBody UnitConversionMappingDTO request) {
        UnitConversionMappingDTO created = unitConversionMappingService.save(request);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, created));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUnitConversionMapping(
            @PathVariable UUID id,
            @RequestBody UnitConversionMappingDTO request) {
        UnitConversionMappingDTO updated = unitConversionMappingService.update(id, request);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, updated));
    }

    @PutMapping("/disable/{id}")
    public ResponseEntity<?> disableUnitConversionMapping(@PathVariable UUID id) {
        UnitConversionMappingDTO disabled = unitConversionMappingService.disable(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, disabled));
    }

    @PutMapping("/enable/{id}")
    public ResponseEntity<?> enableUnitConversionMapping(@PathVariable UUID id) {
        UnitConversionMappingDTO enabled = unitConversionMappingService.enable(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, enabled));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUnitConversionMapping(@PathVariable UUID id) {
        unitConversionMappingService.delete(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, null));
    }
}