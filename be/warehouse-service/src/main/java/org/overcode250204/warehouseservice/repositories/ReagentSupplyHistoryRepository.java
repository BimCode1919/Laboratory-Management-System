package org.overcode250204.warehouseservice.repositories;


import org.overcode250204.warehouseservice.model.entities.Reagent;
import org.overcode250204.warehouseservice.model.entities.ReagentSupplyHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ReagentSupplyHistoryRepository extends JpaRepository<ReagentSupplyHistory, UUID> {
    ReagentSupplyHistory findReagentSupplyHistoriesByReagent(Reagent reagent);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM ReagentSupplyHistory r WHERE r.reagent.reagentId = :reagentId")
    BigDecimal calculateTotalSupplyByReagentId(UUID reagentId);

    List<ReagentSupplyHistory> findByReagent_ReagentId(UUID reagentId);

    List<ReagentSupplyHistory> findByExpirationDateBetween(LocalDate today, LocalDate endDate);
}
