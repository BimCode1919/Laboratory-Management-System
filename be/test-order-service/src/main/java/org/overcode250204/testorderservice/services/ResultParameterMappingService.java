package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.dtos.ResultParameterMappingDTO;

import java.util.List;
import java.util.UUID;

public interface ResultParameterMappingService {
    ResultParameterMappingDTO save(ResultParameterMappingDTO mapping);

    ResultParameterMappingDTO update(UUID id, ResultParameterMappingDTO mapping);

    List<ResultParameterMappingDTO> findAll();

    ResultParameterMappingDTO findById(UUID id);

    ResultParameterMappingDTO disable(UUID id);

    ResultParameterMappingDTO enable(UUID id);

    void delete(UUID id);
}