package org.overcode250204.instrumentservice.repository;

import org.overcode250204.instrumentservice.entity.PendingTestOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PendingTestOrderRepository extends JpaRepository<PendingTestOrder, UUID>, JpaSpecificationExecutor<PendingTestOrder> {
    PendingTestOrder findByBarCode(String barCode);
}
