package org.overcode250204.warehouseservice.services.interfaces;

import org.overcode250204.warehouseservice.model.dto.reagent.ReagentSupplyHistoryRequest;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentSupplyHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface ReagentSupplyHistoryService {
    ReagentSupplyHistoryResponse createReagentSupply(ReagentSupplyHistoryRequest request, UUID receivedBy);
    List<ReagentSupplyHistoryResponse> getReagentInventory();
    List<ReagentSupplyHistoryResponse> getReagentInventory(UUID reagentId);
    // Get reagents that will expire within the next 7 days (including today)
    List<ReagentSupplyHistoryResponse> getExpiringWithinOneWeek();
}
