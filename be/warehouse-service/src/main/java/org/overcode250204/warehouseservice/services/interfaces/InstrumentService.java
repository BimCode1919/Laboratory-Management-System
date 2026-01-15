package org.overcode250204.warehouseservice.services.interfaces;

import org.overcode250204.warehouseservice.model.dto.instrument.CreateInstrumentRequest;
import org.overcode250204.warehouseservice.model.entities.Instrument;
import org.overcode250204.warehouseservice.model.enums.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InstrumentService {
    Instrument createInstrument(CreateInstrumentRequest request, UUID createdBy);

    List<Instrument> getInstruments();

    Instrument getInstrumentById(UUID instrumentId);

    List<Instrument> getInstrumentsByStatus(Status status);

    List<Instrument> getInstrumentsByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // === Status & Mode control ===
    Instrument editInstrumentStatus(UUID id, Status newStatus, String reason, UUID updatedBy);

    String checkInstrumentStatus(UUID id);

    void recheckInstrumentStatus(UUID id);

    Instrument activateInstrument(UUID id, UUID updatedBy);

    Instrument deactivateInstrument(UUID id, UUID updatedBy);

    int autoDeleteDeactivatedInstruments();
}
