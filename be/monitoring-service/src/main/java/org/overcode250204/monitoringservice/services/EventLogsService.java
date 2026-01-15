package org.overcode250204.monitoringservice.services;

import lombok.RequiredArgsConstructor;
import org.overcode250204.monitoringservice.entities.EventLogs;
import org.overcode250204.monitoringservice.exceptions.AppException;
import org.overcode250204.monitoringservice.repositories.EventLogsRepo;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
// Các import này không còn cần thiết cho phương thức getAllEventLogs
// import org.springframework.data.mongodb.core.query.Criteria;
// import org.springframework.data.mongodb.core.query.Query;
// import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventLogsService {

    private final EventLogsRepo eventLogsRepo;
    // Giữ lại MongoTemplate vì các phương thức khác trong tương lai có thể cần
    private final MongoTemplate mongoTemplate;


    public Map<String, Object> getAllEventLogs(int page, int size, String sortField, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(Sort.Direction.DESC, sortField)
                : Sort.by(Sort.Direction.ASC, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventLogs> pageResult = eventLogsRepo.findAll(pageable);

        Map<String, Object> result = new HashMap<>();
        result.put("totalItems", pageResult.getTotalElements());
        result.put("logs", pageResult.getContent());
        result.put("totalPages", pageResult.getTotalPages());
        result.put("currentPage", pageResult.getNumber());

        return result;
    }


    // get event log by id
    public EventLogs getEventLogById(String id) {
        return eventLogsRepo.findById(id)
                .orElseThrow(() -> new AppException(404, "Event log not found with ID " + id));
    }


    // create
    @Transactional
    public EventLogs create(EventLogs eventLogs) {
        return eventLogsRepo.save(eventLogs);
    }

    // delete
    @Transactional
    public void deleteById(String id) {
        boolean exists = eventLogsRepo.existsById(id);
        if (!exists) {
            throw new AppException(404, "Event log not found with ID " + id);
        }
        eventLogsRepo.deleteById(id);
    }

    public List<EventLogs> getRecentEvents(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return eventLogsRepo.findTopByOrderByCreatedAtDesc(pageable);
    }

}