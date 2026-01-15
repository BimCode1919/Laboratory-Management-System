package org.overcode250204.monitoringservice.repositories;

import org.overcode250204.monitoringservice.entities.EventLogs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogsRepo extends MongoRepository<EventLogs, String> {
    List<EventLogs> findTopByOrderByCreatedAtDesc(Pageable pageable);
}
