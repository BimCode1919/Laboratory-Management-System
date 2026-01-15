package org.overcode250204.warehouseservice.services.interfaces;

import org.overcode250204.warehouseservice.model.dto.reagent.ReagentRequest;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentResponse;

import java.util.List;
import java.util.UUID;

public interface ReagentSupplyService {
    ReagentResponse createReagent(ReagentRequest request, UUID createdBy);
    ReagentResponse updateReagents(ReagentRequest request, UUID id, UUID updatedBy);
    String deleteReagents(UUID id, UUID deleteBy);
    List<ReagentResponse> getAllReagents();
}
