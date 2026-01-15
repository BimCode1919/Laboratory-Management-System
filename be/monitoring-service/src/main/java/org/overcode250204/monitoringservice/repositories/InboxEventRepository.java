package org.overcode250204.monitoringservice.repositories;

import org.overcode250204.monitoringservice.entities.InboxEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InboxEventRepository extends MongoRepository<InboxEvent, String> {

    boolean existsByEventId(UUID eventId);
}
