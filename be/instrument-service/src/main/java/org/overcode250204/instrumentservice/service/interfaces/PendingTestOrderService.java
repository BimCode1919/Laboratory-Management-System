package org.overcode250204.instrumentservice.service.interfaces;

import org.overcode250204.instrumentservice.dtos.PendingTestOrderCheckResponse;
import org.overcode250204.instrumentservice.entity.PendingTestOrder;
import org.overcode250204.instrumentservice.enums.Priority;
import org.overcode250204.instrumentservice.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PendingTestOrderService {
    Page<PendingTestOrder> findAll(String type, Status status, Priority priority, Pageable pageable);
    PendingTestOrderCheckResponse checkByBarCodeAndInstrumentId(String barCode, UUID instrumentId);
}