package org.overcode250204.monitoringservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.monitoringservice.entities.HL7Backup;
import org.overcode250204.monitoringservice.services.HL7BackupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/hl7-backups")
@RequiredArgsConstructor
public class HL7BackupController {

    private final HL7BackupService hl7BackupService;
    private static final String SERVICE_NAME = "monitoring-service";

    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        Map<String, Object> data = hl7BackupService.getAll(page, size, sortField, sortOrder);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<HL7Backup>> getById(@PathVariable String id) {
        HL7Backup result = hl7BackupService.getById(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, result));
    }


    @GetMapping("/run/{runId}")
    public ResponseEntity<BaseResponse<?>> getByRunId(@PathVariable String runId) {
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, hl7BackupService.getByRunId(runId)));
    }

    @GetMapping("/instrument/{instrumentId}")
    public ResponseEntity<BaseResponse<?>> getByInstrument(
            @PathVariable String instrumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME,
                hl7BackupService.getByInstrument(instrumentId, page, size)));
    }


    @PostMapping
    public ResponseEntity<BaseResponse<HL7Backup>> create(@RequestBody HL7Backup request) {
        HL7Backup created = hl7BackupService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.of(SERVICE_NAME, "200",
                        "HL7 backup created successfully", created));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        hl7BackupService.deleteById(id);
        return ResponseEntity.ok(BaseResponse.of(SERVICE_NAME,
                "200", "HL7 backup deleted successfully", null));
    }


    @DeleteMapping("/run/{runId}")
    public ResponseEntity<BaseResponse<String>> deleteByRun(@PathVariable String runId) {
        int deleted = hl7BackupService.deleteByRunId(runId);
        return ResponseEntity.ok(BaseResponse.of(SERVICE_NAME,
                "200", String.format("Deleted %d backups for runId=%s", deleted, runId), null));
    }
}
