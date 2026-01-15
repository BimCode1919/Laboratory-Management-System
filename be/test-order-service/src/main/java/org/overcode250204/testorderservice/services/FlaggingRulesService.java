package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.dtos.FlaggingRulesDTO;

import java.util.List;
import java.util.UUID;

public interface FlaggingRulesService {
    FlaggingRulesDTO findById(UUID id);

    List<FlaggingRulesDTO> findAll();

    FlaggingRulesDTO create(FlaggingRulesDTO newRule);

    FlaggingRulesDTO update(UUID id, FlaggingRulesDTO newRule);

    FlaggingRulesDTO disable(UUID id);

    FlaggingRulesDTO enable(UUID id);

    void delete(UUID id);
}