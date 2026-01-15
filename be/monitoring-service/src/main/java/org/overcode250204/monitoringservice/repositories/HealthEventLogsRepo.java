package org.overcode250204.monitoringservice.repositories;

import org.overcode250204.monitoringservice.entities.HealthEventLogs;
import org.overcode250204.monitoringservice.enums.HealthEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthEventLogsRepo extends MongoRepository<HealthEventLogs, String> {

    Page<HealthEventLogs> findByBrokerId(String brokerId, Pageable pageable);


    Page<HealthEventLogs> findByHealthEventType(HealthEventType healthEventType, Pageable pageable);
}
