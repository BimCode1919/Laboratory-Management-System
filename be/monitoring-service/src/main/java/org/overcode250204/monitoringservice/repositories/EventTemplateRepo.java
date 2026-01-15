package org.overcode250204.monitoringservice.repositories;

import org.overcode250204.monitoringservice.entities.EventTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EventTemplateRepo extends MongoRepository<EventTemplate, String> {
    Optional<EventTemplate> findByEventName(String eventName);
    boolean existsByEventName(String eventName);
}
