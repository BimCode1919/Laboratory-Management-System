package org.overcode250204.monitoringservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.monitoringservice.dtos.EventLogDTO;
import org.overcode250204.monitoringservice.entities.EventLogs;
import org.overcode250204.monitoringservice.services.EventLogsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/event-logs")
@RequiredArgsConstructor
public class EventLogsController {

    private final EventLogsService eventLogsService;
    private static final String SERVICE_NAME = "monitoring-service";

    // GET ALL WITH PAGINATION
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, Object>>> getAllEventLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Map<String, Object> data = eventLogsService.getAllEventLogs(page, size, sortField, sortOrder);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<EventLogDTO>> getEventLogById(@PathVariable String id) {
        EventLogs log = eventLogsService.getEventLogById(id);
        EventLogDTO dto = EventLogDTO.fromEntity(log);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, dto));
    }

    // CREATE
    @PostMapping
    public ResponseEntity<BaseResponse<EventLogDTO>> createEventLog(@RequestBody EventLogDTO eventLogDTO) {
        EventLogs created = eventLogsService.create(eventLogDTO.toEntity());
        EventLogDTO responseDto = EventLogDTO.fromEntity(created);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.of(SERVICE_NAME, "201", "Event log created successfully", responseDto));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteEventLog(@PathVariable String id) {
        eventLogsService.deleteById(id);
        return ResponseEntity.ok(BaseResponse.of(SERVICE_NAME, "200", "Event log deleted successfully", null));
    }

    // FOR DASHBOARD OVERVIEW
    @GetMapping("/recent")
    public ResponseEntity<?> getRecentEvents(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<EventLogs> recentLogs = eventLogsService.getRecentEvents(limit);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, recentLogs));
    }

}
