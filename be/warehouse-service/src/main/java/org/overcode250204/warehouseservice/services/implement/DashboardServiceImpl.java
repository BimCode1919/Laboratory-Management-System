package org.overcode250204.warehouseservice.services.implement;

import lombok.RequiredArgsConstructor;
import org.overcode250204.warehouseservice.model.dto.dashboard.ReagentStockLevelDTO;
import org.overcode250204.warehouseservice.model.dto.dashboard.SupplyUsageTrendDTO;
import org.overcode250204.warehouseservice.repositories.ReagentsRepository;
import org.overcode250204.warehouseservice.services.interfaces.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final ReagentsRepository reagentsRepository;

    @Override
    public List<ReagentStockLevelDTO> getReagentStockLevels() {
        return reagentsRepository.getReagentStockLevels();
    }

    @Override
    public List<SupplyUsageTrendDTO> getSupplyUsageTrend(int days) {
        List<Object[]> rows = reagentsRepository.getSupplyUsageTrendNative(days);
        return rows.stream()
                .map(r -> new SupplyUsageTrendDTO(
                        (String) r[0],
                        ((Number) r[1]).doubleValue(),
                        ((Number) r[2]).doubleValue()
                ))
                .toList();
    }
}
