package org.overcode250204.warehouseservice.services.interfaces;

import java.math.BigDecimal;
import java.util.UUID;

public interface ReagentInventoryService {
    boolean consumeReagentForInstrument(UUID reagentId, BigDecimal quantityRequested, UUID instrumentId, String installedBy);
    BigDecimal getAvailableQuantity(UUID reagentId);
}
