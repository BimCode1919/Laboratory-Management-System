package org.overcode250204.monitoringservice.services;

import lombok.RequiredArgsConstructor;
import org.overcode250204.monitoringservice.dtos.HealthEventLogDTO;
import org.overcode250204.monitoringservice.entities.HealthEventLogs;
import org.overcode250204.monitoringservice.enums.HealthEventType;
import org.overcode250204.monitoringservice.exceptions.AppException;
import org.overcode250204.monitoringservice.repositories.HealthEventLogsRepo;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HealthEventLogsService {

    private final HealthEventLogsRepo repo;

    // Get all logs with pagination and sorting
    public Map<String, Object> getAllHealthEventLogs(int page, int size, String sortField, String sortOrder) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortField)
                : Sort.by(Sort.Direction.ASC, sortField);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<HealthEventLogs> pageResult = repo.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("logs", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        response.put("pageSize", pageResult.getSize());

        return response;
    }

    // Get log by id
    public HealthEventLogs getHealthEventLogById(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new AppException(404, "Health event log not found with ID " + id));
    }

    // Search by broker id with pagination
    public Map<String, Object> getHealthEventLogsByBrokerId(String brokerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HealthEventLogs> logs = repo.findByBrokerId(brokerId, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs.getContent());
        result.put("totalItems", logs.getTotalElements());
        result.put("totalPages", logs.getTotalPages());
        result.put("currentPage", logs.getNumber());

        return result;
    }

    // Search by event type with pagination
    public Map<String, Object> getHealthEventLogsByEventType(HealthEventType eventType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<HealthEventLogs> logs = repo.findByHealthEventType(eventType, pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs.getContent());
        result.put("totalItems", logs.getTotalElements());
        result.put("totalPages", logs.getTotalPages());
        result.put("currentPage", logs.getNumber());

        return result;
    }

    // Create new log
    public HealthEventLogs createHealthEventLog(HealthEventLogDTO request) {
        HealthEventLogs log = request.toEntity();
        log.setCreatedAt(LocalDateTime.now());
        return repo.save(log);
    }

    // Delete log by id
    public void deleteHealthEventLogById(String id) {
        if (!repo.existsById(id)) {
            throw new AppException(404, "Health event log not found with ID " + id);
        }
        repo.deleteById(id);
    }
}
