package org.overcode250204.warehouseservice.repositories;


import org.overcode250204.warehouseservice.model.dto.dashboard.ReagentStockLevelDTO;
import org.overcode250204.warehouseservice.model.dto.dashboard.SupplyUsageTrendDTO;
import org.overcode250204.warehouseservice.model.entities.Reagent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReagentsRepository extends JpaRepository<Reagent, UUID> {

    Optional<Reagent> findByNameIgnoreCase(String name);

    @Query("SELECT new org.overcode250204.warehouseservice.model.dto.dashboard.ReagentStockLevelDTO(" +
            "r.name, SUM(ir.quantity)) " +
            "FROM InstrumentReagents ir " +
            "JOIN ir.reagent r " +
            "GROUP BY r.name")
    List<ReagentStockLevelDTO> getReagentStockLevels();

    @Query(value = """
    SELECT TO_CHAR(d.date, 'YYYY-MM-DD') AS date,
           COALESCE(SUM(s.quantity), 0) AS supplyQuantity,
           COALESCE(SUM(u.quantity_used), 0) AS usageQuantity
    FROM (
        SELECT generate_series(
            CURRENT_DATE - CAST(:days || ' days' AS INTERVAL),
            CURRENT_DATE,
            INTERVAL '1 day'
        )::date AS date
    ) d
    LEFT JOIN reagent_supply_history s
           ON s.receipt_date = d.date
    LEFT JOIN reagent_usage_history u
           ON u.usage_date::date = d.date
    GROUP BY d.date
    ORDER BY d.date
    """, nativeQuery = true)
    List<Object[]> getSupplyUsageTrendNative(int days);
}
