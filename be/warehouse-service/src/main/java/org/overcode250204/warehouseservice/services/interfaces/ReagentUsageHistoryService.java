package org.overcode250204.warehouseservice.services.interfaces;

import org.overcode250204.warehouseservice.model.dto.reagent.ReagentUsageResponse;
import org.overcode250204.warehouseservice.model.entities.ReagentUsageHistory;

import java.util.List;
import java.util.UUID;

public interface ReagentUsageHistoryService {

    List<ReagentUsageResponse> getAllHistory();

    List<ReagentUsageResponse> getHistoryByReagent(UUID reagentId);


    void deleteUsageHistory(UUID usageId);
}
