package org.overcode250204.warehouseservice.repositories;

import org.overcode250204.warehouseservice.model.entities.Configuration;
import org.overcode250204.warehouseservice.model.entities.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConfigurationRepository extends JpaRepository<Configuration, UUID> {
    boolean existsByInstrument(Instrument instrument);
}
