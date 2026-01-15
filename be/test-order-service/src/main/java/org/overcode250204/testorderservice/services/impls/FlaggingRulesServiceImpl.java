package org.overcode250204.testorderservice.services.impls;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.FlaggingRulesDTO;
import org.overcode250204.testorderservice.mappers.FlaggingRulesMapper;
import org.overcode250204.testorderservice.models.entites.FlaggingRules;
import org.overcode250204.testorderservice.exceptions.ResourceConflictException;
import org.overcode250204.testorderservice.exceptions.ResourceNotFoundException;
import org.overcode250204.testorderservice.repositories.FlaggingRulesRepository;
import org.overcode250204.testorderservice.services.FlaggingRulesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlaggingRulesServiceImpl implements FlaggingRulesService {
    private final FlaggingRulesRepository repository;

    @Override
    public FlaggingRulesDTO findById(UUID id) {
        FlaggingRules entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find FlaggingRules with id: " + id));
        return FlaggingRulesMapper.toDTO(entity);
    }

    @Override
    public List<FlaggingRulesDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(FlaggingRulesMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public FlaggingRulesDTO create(FlaggingRulesDTO newRule) {
        FlaggingRules entity = FlaggingRulesMapper.toEntity(newRule);
        validateNormalRange(entity);
        FlaggingRules saved = repository.save(entity);
        return FlaggingRulesMapper.toDTO(saved);
    }

    @Override
    public FlaggingRulesDTO update(UUID id, FlaggingRulesDTO updatedRuleDTO) {
        FlaggingRules existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find FlaggingRules with id: " + id));

        existing.setParameterName(updatedRuleDTO.getParameterName());
        existing.setUnit(updatedRuleDTO.getUnit());
        existing.setGender(updatedRuleDTO.getGender());
        existing.setNormalLow(updatedRuleDTO.getNormalLow());
        existing.setNormalHigh(updatedRuleDTO.getNormalHigh());
        existing.setDescription(updatedRuleDTO.getDescription());

        validateNormalRange(existing);

        FlaggingRules saved = repository.save(existing);
        return FlaggingRulesMapper.toDTO(saved);
    }

    @Override
    public FlaggingRulesDTO disable(UUID id) {
        FlaggingRules rule = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find FlaggingRules with id: " + id));

        if (Boolean.FALSE.equals(rule.getIsActivated())) {
            throw new ResourceConflictException("FlaggingRules " + id + " is already disabled.");
        }

        rule.setIsActivated(false);
        FlaggingRules saved = repository.save(rule);
        return FlaggingRulesMapper.toDTO(saved);
    }

    @Override
    public FlaggingRulesDTO enable(UUID id) {
        FlaggingRules rule = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find FlaggingRules with id: " + id));

        if (Boolean.TRUE.equals(rule.getIsActivated())) {
            throw new ResourceConflictException("FlaggingRules " + id + " is already enabled.");
        }

        rule.setIsActivated(true);
        FlaggingRules saved = repository.save(rule);
        return FlaggingRulesMapper.toDTO(saved);
    }

    @Override
    public void delete(UUID id) {
        FlaggingRules existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find FlaggingRules with id: " + id));

        repository.delete(existing);
    }


    // Kiểm tra ràng buộc giá trị bình thường (normalLow < normalHigh)
    private void validateNormalRange(FlaggingRules newRule) {
        if(newRule.getNormalLow() != null && newRule.getNormalHigh() != null
        && newRule.getNormalLow() > newRule.getNormalHigh()) {
            throw new ResourceConflictException("Error: Normal Low should be lower than or at most equal to Normal High");
        }
    }
}