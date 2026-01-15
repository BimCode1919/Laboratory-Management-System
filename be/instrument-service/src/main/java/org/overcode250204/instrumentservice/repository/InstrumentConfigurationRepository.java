package org.overcode250204.instrumentservice.repository;

import org.overcode250204.instrumentservice.entity.InstrumentConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InstrumentConfigurationRepository extends JpaRepository<InstrumentConfiguration, UUID> {
}

