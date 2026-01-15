package org.overcode250204.instrumentservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.instrumentservice.dtos.PendingTestOrderCheckResponse;
import org.overcode250204.instrumentservice.entity.PendingTestOrder;
import org.overcode250204.instrumentservice.enums.Priority;
import org.overcode250204.instrumentservice.enums.Status;
import org.overcode250204.instrumentservice.service.interfaces.PendingTestOrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/pending-test-orders")
@RequiredArgsConstructor
public class PendingTestOrderController {

    @Value("${service.application.name:instrument-service}")
    private String serviceName;

    private final PendingTestOrderService service;

    @GetMapping
    @PreAuthorize("hasAuthority('TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<Page<PendingTestOrder>>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDir
    ) {
        Sort sort = Sort.by(sortDir, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PendingTestOrder> data = service.findAll(type, status, priority, pageable);
        return ResponseEntity.ok(BaseResponse.success(serviceName, data));
    }

    @GetMapping("/check")
    @PreAuthorize("hasAnyAuthority('TEST_ORDER_READ','TEST_EXECUTE')")
    public ResponseEntity<BaseResponse<PendingTestOrderCheckResponse>> checkByBarCode(
            @RequestParam String barcode,
            @RequestParam(required = false) UUID instrumentId
    ) {
        PendingTestOrderCheckResponse response = service.checkByBarCodeAndInstrumentId(barcode, instrumentId);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }
}
