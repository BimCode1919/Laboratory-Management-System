package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.dtos.UnitConversionMappingDTO;

import java.util.List;
import java.util.UUID;

public interface UnitConversionMappingService {
    UnitConversionMappingDTO findById(UUID id);

    List<UnitConversionMappingDTO> findAll();

    UnitConversionMappingDTO save(UnitConversionMappingDTO mapping);

    UnitConversionMappingDTO update(UUID id, UnitConversionMappingDTO mapping);

    UnitConversionMappingDTO disable(UUID id);

    UnitConversionMappingDTO enable(UUID id);

    void delete(UUID id);
}