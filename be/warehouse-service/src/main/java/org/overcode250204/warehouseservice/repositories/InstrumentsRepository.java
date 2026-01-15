package org.overcode250204.warehouseservice.repositories;

import org.overcode250204.warehouseservice.model.entities.Instrument;
import org.overcode250204.warehouseservice.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstrumentsRepository extends JpaRepository<Instrument, UUID> {
    Instrument findByName(String name);

    Instrument findBySerialNumber(String serialNumber);

    List<Instrument> findByStatus(Status status);

    List<Instrument> findByCreatedBy(UUID createdBy);

    List<Instrument> findByUpdatedBy(UUID updatedBy);

    List<Instrument> findByCreatedAtBetween(LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);

    boolean existsByNameOrSerialNumber(String name, String serialNumber);

    Optional<Instrument> findInstrumentsByInstrumentId(UUID instrumentId);
}
