package org.overcode250204.instrumentservice.repository;

import org.overcode250204.instrumentservice.entity.InboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InboxRepository extends JpaRepository<InboxEvent, UUID> {
    boolean existsByEventId(UUID eventId);

    Optional<InboxEvent> findByEventId(UUID eventId);
}
