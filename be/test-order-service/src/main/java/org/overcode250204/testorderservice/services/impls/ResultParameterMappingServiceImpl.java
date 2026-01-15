package org.overcode250204.testorderservice.services.impls;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.ResultParameterMappingDTO;
import org.overcode250204.testorderservice.mappers.ResultParameterMappingMapper;
import org.overcode250204.testorderservice.models.entites.ResultParameterMapping;
import org.overcode250204.testorderservice.exceptions.ResourceConflictException;
import org.overcode250204.testorderservice.exceptions.ResourceNotFoundException;
import org.overcode250204.testorderservice.repositories.ResultParameterMappingRepository;
import org.overcode250204.testorderservice.services.ResultParameterMappingService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultParameterMappingServiceImpl implements ResultParameterMappingService {
    private final ResultParameterMappingRepository repository;
    private final ResultParameterMappingMapper mapper;

    @Override
    public List<ResultParameterMappingDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResultParameterMappingDTO findById(UUID id) {
        ResultParameterMapping entity = repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cannot find ResultParameterMapping with id: " + id));
        return mapper.toDTO(entity);
    }

    @Override
    public ResultParameterMappingDTO save(ResultParameterMappingDTO dto) {
        ResultParameterMapping entity = mapper.toEntity(dto);
        try {
            ResultParameterMapping saved = repository.save(entity);
            return mapper.toDTO(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException(String.format(
                    "Add Mapping conflict: (External Parameter: %s, Data Source: %s) already exists.",
                    dto.getExternalParamName(), dto.getDataSource()
            ));
        }
    }

    @Override
    public ResultParameterMappingDTO update(UUID id, ResultParameterMappingDTO dto) {
        ResultParameterMapping existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find ResultParameterMapping with id: " + id));

        mapper.updateEntity(existing, dto);

        try {
            ResultParameterMapping updated = repository.save(existing);
            return mapper.toDTO(updated);
        } catch (DataIntegrityViolationException ex) {
            throw new ResourceConflictException(String.format(
                    "Update Mapping conflict: (External Parameter: %s, Data Source: %s) already exists.",
                    dto.getExternalParamName(), dto.getDataSource()
            ));
        }
    }

    @Override
    public ResultParameterMappingDTO disable(UUID id) {
        ResultParameterMapping entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find ResultParameterMapping with id: " + id));

        if (Boolean.FALSE.equals(entity.getIsActivated())) {
            throw new ResourceConflictException("Mapping " + id + " is already disabled.");
        }

        entity.setIsActivated(false);
        ResultParameterMapping saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    public ResultParameterMappingDTO enable(UUID id) {
        ResultParameterMapping entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find ResultParameterMapping with id: " + id));
        if (Boolean.TRUE.equals(entity.getIsActivated())) {
            throw new ResourceConflictException("Mapping " + id + " is already enabled.");
        }

        entity.setIsActivated(true);
        ResultParameterMapping saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    public void delete(UUID id) {
        ResultParameterMapping entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find ResultParameterMapping with id: " + id));
        repository.delete(entity);
    }
}