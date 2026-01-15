package org.overcode250204.monitoringservice.services;

import lombok.RequiredArgsConstructor;
import org.overcode250204.monitoringservice.dtos.SyncUpRequestsDTO;
import org.overcode250204.monitoringservice.entities.SyncUpRequests;
import org.overcode250204.monitoringservice.enums.SyncUpRequestsStatus;
import org.overcode250204.monitoringservice.exceptions.AppException;
import org.overcode250204.monitoringservice.repositories.SyncUpRequestsRepo;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SyncUpRequestsService {

    private final SyncUpRequestsRepo repo;

    // Get all with pagination & sorting
    public Map<String, Object> getAll(int page, int size, String sortField, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortField)
                : Sort.by(Sort.Direction.ASC, sortField);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SyncUpRequests> pageResult = repo.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("results", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());
        return response;
    }

    // Search by messageId
    public Map<String, Object> getByMessageId(String messageId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SyncUpRequests> result = repo.findByMessageId(messageId, pageable);

        Map<String, Object> res = new HashMap<>();
        res.put("results", result.getContent());
        res.put("totalItems", result.getTotalElements());
        res.put("totalPages", result.getTotalPages());
        res.put("currentPage", result.getNumber());
        return res;
    }

    // Search by status
    public Map<String, Object> getByStatus(SyncUpRequestsStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SyncUpRequests> result = repo.findByStatus(status, pageable);

        Map<String, Object> res = new HashMap<>();
        res.put("results", result.getContent());
        res.put("totalItems", result.getTotalElements());
        res.put("totalPages", result.getTotalPages());
        res.put("currentPage", result.getNumber());
        return res;
    }

    // Get by ID
    public SyncUpRequests getById(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new AppException(404, "SyncUpRequest not found with ID " + id));
    }

    // Create
    public SyncUpRequests create(SyncUpRequestsDTO request) {
        SyncUpRequests item = request.toEntity();
        item.setProcessedAt(LocalDateTime.now());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        return repo.save(item);
    }

    // Update
    public SyncUpRequests update(String id, SyncUpRequestsDTO request) {
        SyncUpRequests existing = repo.findById(id)
                .orElseThrow(() -> new AppException(404, "SyncUpRequest not found with ID " + id));

        existing.setSourceService(request.getSourceService());
        existing.setMessageId(request.getMessageId());
        existing.setStatus(SyncUpRequestsStatus.valueOf(request.getStatus().toUpperCase()));
        existing.setProcessedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        return repo.save(existing);
    }

    // Delete
    public void deleteById(String id) {
        if (!repo.existsById(id)) {
            throw new AppException(404, "SyncUpRequest not found with ID " + id);
        }
        repo.deleteById(id);
    }
}
