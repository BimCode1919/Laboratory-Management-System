package org.overcode250204.warehouseservice.repositories;

import org.overcode250204.warehouseservice.model.entities.Instrument;
import org.overcode250204.warehouseservice.model.entities.InstrumentReagents;
import org.overcode250204.warehouseservice.model.entities.InstrumentReagentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstrumentReagentsRepository extends JpaRepository<InstrumentReagents, InstrumentReagentId> {
    List<InstrumentReagents> findAllByInstrument(Instrument instrument);
}
