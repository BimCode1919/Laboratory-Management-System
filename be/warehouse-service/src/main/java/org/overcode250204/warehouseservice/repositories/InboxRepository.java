package org.overcode250204.warehouseservice.repositories;

import org.overcode250204.warehouseservice.events.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InboxRepository extends JpaRepository<InboxEvent, UUID> {
    boolean existsByEventId(UUID eventId);
}
