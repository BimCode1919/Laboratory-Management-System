package org.overcode250204.testorderservice.services.impls;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.UnitConversionMappingDTO;
import org.overcode250204.testorderservice.mappers.UnitConversionMappingMapper;
import org.overcode250204.testorderservice.models.entites.UnitConversionMapping;
import org.overcode250204.testorderservice.exceptions.ResourceConflictException;
import org.overcode250204.testorderservice.exceptions.ResourceNotFoundException;
import org.overcode250204.testorderservice.repositories.UnitConversionMappingRepository;
import org.overcode250204.testorderservice.services.UnitConversionMappingService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UnitConversionMappingServiceImpl implements UnitConversionMappingService {
    private final UnitConversionMappingRepository repository;
    private final UnitConversionMappingMapper mapper;

    @Override
    public UnitConversionMappingDTO findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find UnitConversionMapping with id: " + id));
    }

    @Override
    public List<UnitConversionMappingDTO> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .toList();
    }

    public List<UnitConversionMappingDTO> findByIsActivatedTrue() {
        return repository.findByIsActivatedTrue().stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public UnitConversionMappingDTO save(UnitConversionMappingDTO dto) {
        UnitConversionMapping entity = mapper.toEntity(dto);
        try {
            UnitConversionMapping saved = repository.save(entity);
            return mapper.toDTO(saved);
        } catch (DataIntegrityViolationException ex) {
            String conflictMessage = String.format(
                    "Add new Unit Conversion Map conflict: The combination (Data Source: %s, Source Unit: %s, Target Unit: %s) already exists.",
                    dto.getDataSource(), dto.getSourceUnit(), dto.getTargetUnit()
            );
            throw new ResourceConflictException(conflictMessage);
        }
    }

    @Override
    @Transactional
    public UnitConversionMappingDTO update(UUID id, UnitConversionMappingDTO dto) {
        UnitConversionMapping existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find UnitConversionMapping with id: " + id));

        mapper.updateFromDto(dto, existing);

        try {
            UnitConversionMapping updated = repository.save(existing);
            return mapper.toDTO(updated);
        } catch (DataIntegrityViolationException ex) {
            String conflictMessage = String.format(
                    "Updating Unit Conversion Map conflict: The combination (Data Source: %s, Source Unit: %s, Target Unit: %s) already exists.",
                    dto.getDataSource(), dto.getSourceUnit(), dto.getTargetUnit()
            );
            throw new ResourceConflictException(conflictMessage);
        }
    }

    @Override
    @Transactional
    public UnitConversionMappingDTO disable(UUID id) {
        UnitConversionMapping existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find UnitConversionMapping with id: " + id));

        if (Boolean.FALSE.equals(existing.getIsActivated())) {
            throw new ResourceConflictException("Unit Conversion Mapping - " + id + " - has already been disabled.");
        }

        existing.setIsActivated(false);
        return mapper.toDTO(repository.save(existing));
    }

    @Override
    @Transactional
    public UnitConversionMappingDTO enable(UUID id) {
        UnitConversionMapping existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find UnitConversionMapping with id: " + id));

        if (Boolean.TRUE.equals(existing.getIsActivated())) {
            throw new ResourceConflictException("Unit Conversion Mapping - " + id + " - has already been enabled.");
        }

        existing.setIsActivated(true);
        return mapper.toDTO(repository.save(existing));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UnitConversionMapping existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find UnitConversionMapping with id: " + id));
        repository.delete(existing);
    }
}