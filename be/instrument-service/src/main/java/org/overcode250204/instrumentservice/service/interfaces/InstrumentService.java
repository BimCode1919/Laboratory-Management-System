package org.overcode250204.instrumentservice.service.interfaces;


import com.fasterxml.jackson.databind.JsonNode;
import org.overcode250204.instrumentservice.dtos.ChangeModeCommand;
import org.overcode250204.instrumentservice.dtos.InstrumentDTO;
import org.overcode250204.instrumentservice.dtos.InstrumentSyncDTO;
import org.overcode250204.instrumentservice.dtos.ModeChangeResultDTO;

import java.util.List;
import java.util.UUID;

public interface InstrumentService {

    ModeChangeResultDTO changeMode(UUID instrumentId, ChangeModeCommand req);

    List<InstrumentDTO> getAllInstruments();

    InstrumentDTO getInstrumentById(UUID id);

    void syncInstrument(InstrumentSyncDTO instrumentSyncDTO);

    JsonNode requestConfigurationSync(UUID instrumentId);

    JsonNode requestConfigurationAllSync();

}
