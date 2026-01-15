package org.overcode250204.warehouseservice.repositories;

import org.overcode250204.warehouseservice.model.entities.Instrument;
import org.overcode250204.warehouseservice.model.entities.InstrumentTestExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface InstrumentTestExecutionRepository extends JpaRepository<InstrumentTestExecution, UUID> {
    Optional<InstrumentTestExecution> findByTestNameAndInstrument(String testName, Instrument instrument);

    Optional<InstrumentTestExecution> findByTestOrderId(UUID testOrderId);
}
