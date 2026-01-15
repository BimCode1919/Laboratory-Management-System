package org.overcode250204.monitoringservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.monitoringservice.dtos.HealthEventLogDTO;
import org.overcode250204.monitoringservice.entities.HealthEventLogs;
import org.overcode250204.monitoringservice.enums.HealthEventType;
import org.overcode250204.monitoringservice.services.HealthEventLogsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Health Event Log APIs", description = "APIs to manage health event logs")
@RestController
@RequestMapping("/health-event-logs")
@RequiredArgsConstructor
public class HealthEventLogsController {

    private final HealthEventLogsService healthEventLogsService;
    private static final String SERVICE_NAME = "monitoring-service";

    @Operation(summary = "Get all health event logs with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get list success")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Map<String, Object> data = healthEventLogsService.getAllHealthEventLogs(page, size, sortField, sortOrder);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }

    @Operation(summary = "Get health event log by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found log by id"),
            @ApiResponse(responseCode = "404", description = "Log not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<HealthEventLogDTO>> getById(@PathVariable String id) {
        HealthEventLogs log = healthEventLogsService.getHealthEventLogById(id);
        HealthEventLogDTO dto = HealthEventLogDTO.fromEntity(log);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, dto));
    }

    @Operation(summary = "Get health event logs by broker id with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found logs by broker id")
    })
    @GetMapping("/broker/{brokerId}")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getByBrokerId(
            @PathVariable String brokerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Map<String, Object> data = healthEventLogsService.getHealthEventLogsByBrokerId(brokerId, page, size);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }

    @Operation(summary = "Get health event logs by event type with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found logs by event type"),
            @ApiResponse(responseCode = "400", description = "Invalid event type")
    })
    @GetMapping("/type/{eventType}")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getByEventType(
            @PathVariable String eventType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        HealthEventType type;
        try {
            type = HealthEventType.valueOf(eventType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body((BaseResponse<Map<String, Object>>) (BaseResponse<?>)
                            BaseResponse.error(SERVICE_NAME, "400", "Invalid event type"));

        }

        Map<String, Object> data = healthEventLogsService.getHealthEventLogsByEventType(type, page, size);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }

    @Operation(summary = "Create new health event log")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Create success")})
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<HealthEventLogDTO>> create(@RequestBody HealthEventLogDTO request) {
        HealthEventLogs createdLog = healthEventLogsService.createHealthEventLog(request);
        HealthEventLogDTO dto = HealthEventLogDTO.fromEntity(createdLog);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.of(SERVICE_NAME, "201", "Health event log created successfully", dto));
    }

    @Operation(summary = "Delete health event log by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delete success"),
            @ApiResponse(responseCode = "404", description = "Log not found")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable String id) {
        healthEventLogsService.deleteHealthEventLogById(id);
        return ResponseEntity
                .ok(BaseResponse.of(SERVICE_NAME, "200", "Health event log deleted successfully", null));
    }
}
