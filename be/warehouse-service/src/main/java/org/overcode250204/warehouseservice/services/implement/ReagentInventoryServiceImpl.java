package org.overcode250204.warehouseservice.services.implement;

import lombok.RequiredArgsConstructor;
import org.overcode250204.warehouseservice.exceptions.ErrorCode;
import org.overcode250204.warehouseservice.exceptions.WarehouseException;
import org.overcode250204.warehouseservice.model.entities.Reagent;
import org.overcode250204.warehouseservice.model.entities.ReagentUsageHistory;
import org.overcode250204.warehouseservice.repositories.ReagentsRepository;
import org.overcode250204.warehouseservice.repositories.ReagentSupplyHistoryRepository;
import org.overcode250204.warehouseservice.repositories.ReagentUsageHistoryRepository;
import org.overcode250204.warehouseservice.services.interfaces.ReagentInventoryService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReagentInventoryServiceImpl implements ReagentInventoryService {

    private final ReagentsRepository reagentsRepository;
    private final ReagentSupplyHistoryRepository supplyHistoryRepository;
    private final ReagentUsageHistoryRepository usageHistoryRepository;

    @Override
    public boolean consumeReagentForInstrument(UUID reagentId, BigDecimal quantityRequested, UUID instrumentId, String installedBy) {
        Optional<Reagent> found = reagentsRepository.findById(reagentId);
        if (found.isEmpty()) {
            throw new WarehouseException(ErrorCode.REAGENT_NOT_FOUND);
        }

        Reagent reagent = found.get();
        BigDecimal totalSupply = supplyHistoryRepository.calculateTotalSupplyByReagentId(reagentId);
        BigDecimal totalUsage = usageHistoryRepository.calculateTotalUsageByReagentId(reagentId);

        if (totalSupply == null) totalSupply = BigDecimal.ZERO;
        if (totalUsage == null) totalUsage = BigDecimal.ZERO;

        BigDecimal availableQuantity = totalSupply.subtract(totalUsage);

        if (availableQuantity.compareTo(quantityRequested) < 0) return false;

        ReagentUsageHistory usage = ReagentUsageHistory.builder()
                .reagent(reagent)
                .quantityUsed(quantityRequested)
                .usageDate(LocalDateTime.now())
                .usedBy(installedBy)
                .action("INSTALL")
                .note("Installed on instrument " + instrumentId)
                .build();

        usageHistoryRepository.save(usage);
        reagentsRepository.save(reagent);

        return true;
    }

    @Override
    public BigDecimal getAvailableQuantity(UUID reagentId) {
        BigDecimal totalSupply = supplyHistoryRepository.calculateTotalSupplyByReagentId(reagentId);
        BigDecimal totalUsage = usageHistoryRepository.calculateTotalUsageByReagentId(reagentId);
        if (totalSupply == null) totalSupply = BigDecimal.ZERO;
        if (totalUsage == null) totalUsage = BigDecimal.ZERO;
        return totalSupply.subtract(totalUsage);
    }
}
