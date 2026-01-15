package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InboxRepository extends JpaRepository<InboxEvent, UUID> {
    boolean existsByEventId(UUID eventId);
}
