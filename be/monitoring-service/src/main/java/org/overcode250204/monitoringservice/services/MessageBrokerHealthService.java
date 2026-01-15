package org.overcode250204.monitoringservice.services;

import lombok.RequiredArgsConstructor;
import org.overcode250204.monitoringservice.dtos.MessageBrokerHealthDTO;
import org.overcode250204.monitoringservice.entities.MessageBrokerHealth;
import org.overcode250204.monitoringservice.enums.MessageBrokerHealthStatus;
import org.overcode250204.monitoringservice.exceptions.AppException;
import org.overcode250204.monitoringservice.repositories.MessageBrokerHealthRepo;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MessageBrokerHealthService {

    private final MessageBrokerHealthRepo repo;

    // Get all with pagination
    public Map<String, Object> getAllMessageBrokerHealths(int page, int size, String sortField, String sortOrder) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortField)
                : Sort.by(Sort.Direction.ASC, sortField);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MessageBrokerHealth> pageResult = repo.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", pageResult.getContent());
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());

        return response;
    }

    // Get by id
    public MessageBrokerHealth getMessageBrokerHealthById(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new AppException(404, "Message broker health not found with ID " + id));
    }

    // Get by broker name
    public Map<String, Object> getMessageBrokerHealthByBrokerName(String brokerName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MessageBrokerHealth> result = repo.findByBrokerName(brokerName, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getContent());
        response.put("totalItems", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    // Get by status
    public Map<String, Object> getMessageBrokerHealthByStatus(MessageBrokerHealthStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MessageBrokerHealth> result = repo.findByStatus(status, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", result.getContent());
        response.put("totalItems", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    // Create
    public MessageBrokerHealth createMessageBrokerHealth(MessageBrokerHealthDTO request) {
        if (request.getBrokerName() == null || request.getBrokerName().isBlank()) {
            throw new AppException(400, "Broker name is required");
        }

        MessageBrokerHealth health = request.toEntity();
        health.setRetryAttempts(0);
        health.setLastCheckedAt(LocalDateTime.now());
        health.setCreatedAt(LocalDateTime.now());

        return repo.save(health);
    }

    // Update
    public MessageBrokerHealth updateMessageBrokerHealth(String id, MessageBrokerHealthDTO request) {
        MessageBrokerHealth existing = getMessageBrokerHealthById(id);

        existing.setBrokerName(request.getBrokerName());
        existing.setStatus(MessageBrokerHealthStatus.valueOf(request.getStatus().toUpperCase()));
        existing.setErrorCode(request.getErrorCode());
        existing.setErrorMessage(request.getErrorMessage());
        existing.setLastCheckedAt(LocalDateTime.now());

        if (request.getStatus().equalsIgnoreCase("DOWN")) {
            existing.setRetryAttempts(existing.getRetryAttempts() + 1);
        }

        return repo.save(existing);
    }

    // Delete
    public void deleteMessageBrokerHealthById(String id) {
        if (!repo.existsById(id)) {
            throw new AppException(404, "Message broker health not found with ID " + id);
        }
        repo.deleteById(id);
    }
}
