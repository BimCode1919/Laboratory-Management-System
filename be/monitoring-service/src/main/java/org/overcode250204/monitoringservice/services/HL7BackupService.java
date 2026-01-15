package org.overcode250204.monitoringservice.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.monitoringservice.entities.HL7Backup;
import org.overcode250204.monitoringservice.exceptions.AppException;
import org.overcode250204.monitoringservice.repositories.HL7BackupRepo;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HL7BackupService {

    private final HL7BackupRepo repo;

    public Map<String, Object> getAll(int page, int size, String sortField, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortField)
                : Sort.by(Sort.Direction.ASC, sortField);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<HL7Backup> result = repo.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("results", result.getContent());
        response.put("currentPage", result.getNumber());
        response.put("totalItems", result.getTotalElements());
        response.put("totalPages", result.getTotalPages());
        return response;
    }

    public HL7Backup getById(String id) {
        return repo.findById(id)
                .orElseThrow(() -> new AppException(404, "HL7 backup not found with ID: " + id));
    }


    public List<HL7Backup> getByRunId(String runId) {
        return repo.findByRunId(runId);
    }

    public Page<HL7Backup> getByInstrument(String instrumentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findByInstrumentId(instrumentId, pageable);
    }

    public HL7Backup create(HL7Backup request) {
        request.setBackupId(UUID.randomUUID().toString());
        request.setCreatedAt(LocalDateTime.now());
        return repo.save(request);
    }

    public void deleteById(String id) {
        if (!repo.existsById(id)) {
            throw new AppException(404, "HL7 backup not found with ID: " + id);
        }
        repo.deleteById(id);
    }

    public int deleteByRunId(String runId) {
        List<HL7Backup> backups = repo.findByRunId(runId);
        repo.deleteAll(backups);
        log.info("[Cleanup] Deleted {} backups for runId={}", backups.size(), runId);
        return backups.size();
    }
}
