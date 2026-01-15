package org.overcode250204.warehouseservice.repositories;

import org.overcode250204.warehouseservice.model.dto.reagent.ReagentUsageResponse;
import org.overcode250204.warehouseservice.model.entities.ReagentUsageHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReagentUsageHistoryRepository extends JpaRepository<ReagentUsageHistory, UUID> {

    List<ReagentUsageHistory> findUsageHistoryByReagent_ReagentId(UUID reagentId);
    @Query("SELECT COALESCE(SUM(r.quantityUsed), 0) FROM ReagentUsageHistory r WHERE r.reagent.reagentId = :reagentId")
    BigDecimal calculateTotalUsageByReagentId(UUID reagentId);

    ;
}