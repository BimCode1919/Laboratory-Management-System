package org.overcode250204.warehouseservice.services.implement;

import lombok.RequiredArgsConstructor;
import org.overcode250204.warehouseservice.exceptions.ErrorCode;
import org.overcode250204.warehouseservice.exceptions.WarehouseException;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentUsageResponse;
import org.overcode250204.warehouseservice.model.entities.ReagentUsageHistory;
import org.overcode250204.warehouseservice.repositories.ReagentUsageHistoryRepository;
import org.overcode250204.warehouseservice.repositories.ReagentsRepository;
import org.overcode250204.warehouseservice.services.interfaces.ReagentUsageHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReagentUsageHistoryServiceImpl implements ReagentUsageHistoryService {

    private final ReagentUsageHistoryRepository usageRepo;
    private final ReagentsRepository reagentRepo;

    // Lấy ra tất cả lịch sử
    @Override
    public List<ReagentUsageResponse> getAllHistory() {
        List<ReagentUsageHistory> historyList = usageRepo.findAll();
        if (historyList.isEmpty()) {
            throw new WarehouseException(ErrorCode.NO_DATA);
        }
        return historyList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Lấy ra lịch sử của 1 loại thuốc
    @Override
    public List<ReagentUsageResponse> getHistoryByReagent(UUID reagentId) {
        if (!reagentRepo.existsById(reagentId)) {
            throw new WarehouseException(ErrorCode.REAGENT_NOT_FOUND);
        }

        List<ReagentUsageHistory> historyList = usageRepo.findUsageHistoryByReagent_ReagentId(reagentId);
        if (historyList.isEmpty()) {
            throw new WarehouseException(ErrorCode.NO_DATA);
        }

        return historyList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Xóa lịch sử của 1 loại thuốc
    @Override
    public void deleteUsageHistory(UUID usageId) {
        if (!usageRepo.existsById(usageId)) {
            throw new WarehouseException(ErrorCode.CONFIG_NOT_FOUND); // Có thể tạo ErrorCode riêng như USAGE_HISTORY_NOT_FOUND nếu muốn rõ hơn
        }
        usageRepo.deleteById(usageId);
    }

    private ReagentUsageResponse toResponse(ReagentUsageHistory entity) {
        return ReagentUsageResponse.builder()
                .usageId(entity.getUsageId())
                .reagentId(entity.getReagent().getReagentId())
                .reagentName(entity.getReagent().getName())
                .quantityUsed(entity.getQuantityUsed())
                .action(entity.getAction())
                .usedBy(entity.getUsedBy())
                .usageDate(entity.getUsageDate())
                .build();
    }
}
