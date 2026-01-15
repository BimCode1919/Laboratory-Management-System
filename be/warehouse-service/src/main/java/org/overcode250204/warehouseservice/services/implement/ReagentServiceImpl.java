package org.overcode250204.warehouseservice.services.implement;

import org.overcode250204.warehouseservice.events.publishers.MonitoringEventPublisher;
import org.overcode250204.warehouseservice.exceptions.ErrorCode;
import org.overcode250204.warehouseservice.exceptions.WarehouseException;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentRequest;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentResponse;
import org.overcode250204.warehouseservice.model.entities.Reagent;
import org.overcode250204.warehouseservice.repositories.ReagentsRepository;
import org.overcode250204.warehouseservice.services.interfaces.ReagentSupplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReagentServiceImpl implements ReagentSupplyService {

    @Autowired
    private ReagentsRepository reagentRepository;
    @Autowired
    private MonitoringEventPublisher monitoringEventPublisher;

    @Override
    public ReagentResponse createReagent(ReagentRequest request, UUID createdBy) {
        if (request == null) {
            throw new WarehouseException(ErrorCode.INVALID_REQUEST);
        }
        //Check trùng tên hóa chất
        boolean exists = reagentRepository.findByNameIgnoreCase(request.getName()).isPresent();
        if (exists) {
            throw new WarehouseException(ErrorCode.REAGENT_NAME_ALREADY_EXISTS);
        }
        Reagent newReagents = new Reagent();
        newReagents.setName(request.getName().toUpperCase());
        newReagents.setCatalogNumber(request.getCatalogNumber());
        newReagents.setManufacturer(request.getManufacturer());
        newReagents.setCasNumber(request.getCasNumber());
        newReagents.setCreatedBy(createdBy);
        Reagent save = reagentRepository.save(newReagents);

        monitoringEventPublisher.publishEvent("reagent", save.getReagentId().toString(), "REAGENT_CREATE",
                Map.of("reagentID", save.getReagentId(),
                        "name", save.getName(),
                        "performedBy", createdBy));
        return toResponse(save);
    }

    @Override
    public ReagentResponse updateReagents(ReagentRequest request, UUID id, UUID updatedBy) {
        if (request == null) {
            throw new WarehouseException(ErrorCode.INVALID_REQUEST);
        }
        Reagent updatedReagents = reagentRepository.findById(id).orElse(null);
        if (updatedReagents == null) {
            throw new WarehouseException(ErrorCode.REAGENT_NOT_FOUND);
        }
        boolean exists = reagentRepository.findByNameIgnoreCase(request.getName()).isPresent();
        if (exists) {
            throw new WarehouseException(ErrorCode.REAGENT_NAME_ALREADY_EXISTS);
        }
        updatedReagents.setName(request.getName());
        updatedReagents.setCatalogNumber(request.getCatalogNumber());
        updatedReagents.setManufacturer(request.getManufacturer());
        updatedReagents.setCasNumber(request.getCasNumber());
        updatedReagents.setUpdatedBy(updatedBy);
        // set updatedAt to current time so response includes the update timestamp
        updatedReagents.setUpdatedAt(LocalDateTime.now());
        Reagent save = reagentRepository.save(updatedReagents);
        monitoringEventPublisher.publishEvent("reagent", save.getReagentId().toString(), "REAGENT_UPDATE",
                Map.of("reagentID", save.getReagentId(),
                        "name", save.getName(),
                        "performedBy", updatedBy.toString()));
        return toResponse(save);
    }

    @Override
    public String deleteReagents(UUID id, UUID deletedBy) {
        Reagent reagent = reagentRepository.findById(id)
                .orElseThrow(() -> new WarehouseException(ErrorCode.REAGENT_NOT_FOUND));
        monitoringEventPublisher.publishEvent("reagent", reagent.getReagentId().toString(), "REAGENT_DELETED",
                Map.of("reagentID", reagent.getReagentId(),
                        "name", reagent.getName(),
                        "performedBy", deletedBy.toString()));
        reagentRepository.delete(reagent);
        return "Reagent with id '" + id + "' has been deleted!";
    }

    @Override
    public List<ReagentResponse> getAllReagents() {
        List<Reagent> reagents = reagentRepository.findAll();
        return reagents.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ReagentResponse toResponse(Reagent reagent) {
        return ReagentResponse.builder()
                .reagentId(reagent.getReagentId())
                .name(reagent.getName())
                .catalogNumber(reagent.getCatalogNumber())
                .manufacturer(reagent.getManufacturer())
                .casNumber(reagent.getCasNumber())
                .createdAt(reagent.getCreatedAt())
                .updatedAt(reagent.getUpdatedAt())
                .createdBy(reagent.getCreatedBy())
                .updatedBy(reagent.getUpdatedBy())
                .build();
    }
}
