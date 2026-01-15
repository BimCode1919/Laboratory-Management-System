package org.overcode250204.warehouseservice.services.interfaces;

import org.overcode250204.warehouseservice.model.dto.dashboard.ReagentStockLevelDTO;
import org.overcode250204.warehouseservice.model.dto.dashboard.SupplyUsageTrendDTO;

import java.util.List;

public interface DashboardService {
    List<ReagentStockLevelDTO> getReagentStockLevels();
    List<SupplyUsageTrendDTO> getSupplyUsageTrend(int days);
}
