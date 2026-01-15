package org.overcode250204.instrumentservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.instrumentservice.dtos.RawTestResultDeleteDTO;
import org.overcode250204.instrumentservice.entity.RawTestResult;
import org.overcode250204.instrumentservice.service.interfaces.RawTestResultService;
import org.overcode250204.instrumentservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/raw-test-results")
@RequiredArgsConstructor
public class RawTestResultController {

    @Value("${spring.application.name}")
    private String serviceName;

    private final RawTestResultService rawTestResultService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<Page<RawTestResult>>> getAllRawTestResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RawTestResult> res = rawTestResultService.getAllResults(pageable);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @GetMapping("/backup-true")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<Page<RawTestResult>>> getAllRawTestResultsBackupTrue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RawTestResult> res = rawTestResultService.getAllResultsBackupTrue(pageable);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @GetMapping("/backup-false")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<Page<RawTestResult>>> getAllRawTestResultsBackupFalse(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RawTestResult> res = rawTestResultService.getAllResultsBackupfalse(pageable);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @GetMapping("/instrument/{instrumentId}")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<Page<RawTestResult>>> getRawTestResultsByInstrumentId(
            @PathVariable UUID instrumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RawTestResult> res = rawTestResultService.getByInstrument(instrumentId, pageable);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @GetMapping("/instrument/{instrumentId}/backup-true")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<Page<RawTestResult>>> getRawTestResultsByInstrumentIdBackupTrue(
            @PathVariable UUID instrumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RawTestResult> res = rawTestResultService.getByInstrumentBackupTrue(instrumentId, pageable);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @GetMapping("/instrument/{instrumentId}/backup-false")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<Page<RawTestResult>>> getRawTestResultsByInstrumentIdBackupFalse(
            @PathVariable UUID instrumentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<RawTestResult> res = rawTestResultService.getByInstrumentBackupFalse(instrumentId, pageable);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<RawTestResult>> getRawTestResultById(@PathVariable String id) {
        RawTestResult res = rawTestResultService.getRawTestResultById(id);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TEST_EXECUTE')")
    public ResponseEntity<BaseResponse<RawTestResult>> createRawTestResult(@RequestBody RawTestResult rawTestResult) {
        RawTestResult res = rawTestResultService.createRawTestResult(rawTestResult);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_UPDATE')")
    public ResponseEntity<BaseResponse<RawTestResult>> updateRawTestResult(@PathVariable String id, @RequestBody RawTestResult rawTestResult) {
        RawTestResult res = rawTestResultService.updateRawTestResult(id, rawTestResult);
        return ResponseEntity.ok(BaseResponse.success(serviceName, res));
    }


    @DeleteMapping("/{runId}/{barcode}")
    @PreAuthorize("hasAnyAuthority('TEST_EXECUTE','TEST_ORDER_DELETE')")
    public ResponseEntity<BaseResponse<RawTestResultDeleteDTO>> deleteByBarcode(
            @PathVariable UUID runId,
            @PathVariable String barcode) {

        UUID userId = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        rawTestResultService.deleteByRunIdAndBarcode(runId,barcode, userId);

        RawTestResultDeleteDTO data = RawTestResultDeleteDTO.builder()
                .barcode(barcode)
                .deletedBy(userId)
                .deletedAt(LocalDateTime.now())
                .deleteMode("MANUAL")
                .build();

        return ResponseEntity.ok(BaseResponse.success(serviceName, data));
    }
}
