package org.overcode250204.monitoringservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.monitoringservice.dtos.SyncUpRequestsDTO;
import org.overcode250204.monitoringservice.entities.SyncUpRequests;
import org.overcode250204.monitoringservice.enums.SyncUpRequestsStatus;
import org.overcode250204.monitoringservice.services.SyncUpRequestsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sync-up-requests")
@RequiredArgsConstructor
@Tag(name = "Sync Up Requests", description = "API quản lý SyncUpRequests")
public class SyncUpRequestsController {

    private final SyncUpRequestsService service;
    private static final String SERVICE_NAME = "monitoring-service";

    @Operation(summary = "Lấy danh sách sync requests (có phân trang)")
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Map<String, Object> data = service.getAll(page, size, sortField, sortOrder);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }

    @Operation(summary = "Lấy chi tiết sync request theo ID")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SyncUpRequestsDTO>> getById(@PathVariable String id) {
        SyncUpRequests item = service.getById(id);
        SyncUpRequestsDTO dto = SyncUpRequestsDTO.fromEntity(item);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, dto));
    }

    @Operation(summary = "Tìm sync request theo trạng thái (PENDING, SUCCESS, FAILED)")
    @GetMapping("/status/{status}")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getByStatus(
            @PathVariable SyncUpRequestsStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Map<String, Object> data = service.getByStatus(status, page, size);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }

    @Operation(summary = "Tạo mới sync request")
    @PostMapping
    public ResponseEntity<BaseResponse<SyncUpRequestsDTO>> create(@RequestBody SyncUpRequestsDTO request) {
        SyncUpRequests created = service.create(request);
        SyncUpRequestsDTO dto = SyncUpRequestsDTO.fromEntity(created);
        return ResponseEntity.ok(BaseResponse.of(SERVICE_NAME, "201", "Sync up request created successfully", dto));
    }

    @Operation(summary = "Cập nhật sync request theo ID")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<SyncUpRequestsDTO>> update(
            @PathVariable String id,
            @RequestBody SyncUpRequestsDTO request
    ) {
        SyncUpRequests updated = service.update(id, request);
        SyncUpRequestsDTO dto = SyncUpRequestsDTO.fromEntity(updated);
        return ResponseEntity.ok(BaseResponse.of(SERVICE_NAME, "200", "Sync up request updated successfully", dto));
    }

    @Operation(summary = "Xóa sync request theo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.ok(BaseResponse.of(SERVICE_NAME, "200", "Sync up request deleted successfully", null));
    }
}
