package org.overcode250204.testorderservice.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.dtos.TestOrderDTO;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.overcode250204.testorderservice.services.TestOrdersService;
import org.overcode250204.testorderservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test-orders")
public class TestOrderController {
    private final TestOrdersService testOrderService;

    private static final String SERVICE_NAME = "TestOrderService";

    @PostMapping
    @PreAuthorize("hasAuthority('TEST_ORDER_CREATE')")
    public ResponseEntity<?> create(@Valid @RequestBody TestOrderDTO request) throws JsonProcessingException {
        String createdByLabUser = AuthUtils.getCurrentUser().getPrincipal().toString();
        TestOrderDTO created = testOrderService.createTestOrder(request, createdByLabUser);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TEST_ORDER_UPDATE')")
    public ResponseEntity<?> update(
            @PathVariable UUID id,
            @Valid @RequestBody TestOrderDTO request) {
        String updatedByLabUser = AuthUtils.getCurrentUser().getPrincipal().toString();
        TestOrderDTO updated = testOrderService.updateTestOrder(id, request, updatedByLabUser);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, updated));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<Page<TestOrderDTO>>> getAll(
            @RequestParam(required = false) String patientName,
            @RequestParam(required = false) TestOrderStatus status,
            Pageable pageable) {
        Page<TestOrderDTO> testOrders = testOrderService.getAllTestOrders(patientName, status, pageable);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, testOrders));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TEST_ORDER_READ')")
    public ResponseEntity<BaseResponse<TestOrderDTO>> getDetail(@PathVariable UUID id) {
        return testOrderService.getTestOrderDetail(id)
                .map(dto -> ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, dto)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(BaseResponse.of(SERVICE_NAME, "404", "Test order not found", null)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TEST_ORDER_DELETE')")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable UUID id) {
        String deletedByLabUser = AuthUtils.getCurrentUser().getPrincipal().toString();
        testOrderService.deleteTestOrder(id, deletedByLabUser);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, null));
    }
}