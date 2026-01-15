package org.overcode250204.warehouseservice.services.implement;

import org.overcode250204.warehouseservice.events.publishers.MonitoringEventPublisher;
import org.overcode250204.warehouseservice.exceptions.ErrorCode;
import org.overcode250204.warehouseservice.exceptions.WarehouseException;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentSupplyHistoryRequest;
import org.overcode250204.warehouseservice.model.dto.reagent.ReagentSupplyHistoryResponse;
import org.overcode250204.warehouseservice.model.entities.ReagentSupplyHistory;
import org.overcode250204.warehouseservice.model.entities.Reagent;
import org.overcode250204.warehouseservice.model.entities.Vendor;
import org.overcode250204.warehouseservice.repositories.ReagentSupplyHistoryRepository;
import org.overcode250204.warehouseservice.repositories.ReagentsRepository;
import org.overcode250204.warehouseservice.repositories.VendorRepository;
import org.overcode250204.warehouseservice.services.interfaces.ReagentSupplyHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReagentSupplyHistoryServiceImpl implements ReagentSupplyHistoryService {

    @Autowired
    private ReagentSupplyHistoryRepository reagentSupplyRepository;

    @Autowired
    private ReagentsRepository reagentsRepository;

    @Autowired
    private VendorRepository vendorRepository;
    @Autowired
    private MonitoringEventPublisher monitoringEventPublisher;


    //


    @Override
    public ReagentSupplyHistoryResponse createReagentSupply(ReagentSupplyHistoryRequest request, UUID receivedBy) {
        if (request == null) {
            throw new WarehouseException(ErrorCode.INVALID_REQUEST);
        }
        //Kiểm tra reagent có tồn tại k
        Reagent reagent = reagentsRepository.findById(request.getReagentId())
                .orElseThrow(() -> new WarehouseException(ErrorCode.REAGENT_NOT_FOUND));

        //Kiểm tra vendor có tồn tại k
        Vendor vendor = vendorRepository.findById(request.getVendorID())
                .orElseThrow(() -> new WarehouseException(ErrorCode.VENDOR_NOT_FOUND));
        ReagentSupplyHistory newSupply = ReagentSupplyHistory.builder()
                .reagent(reagent)
                .vendor(vendor)
                .poNumber(request.getPoNumber())
                .orderDate(request.getOrderDate())
                .receiptDate(request.getReceiptDate())
                .quantity(request.getQuantity())
                .unitOfMeasure(request.getUnitOfMeasure())
                .lotNumber(request.getLotNumber())
                .expirationDate(request.getExpirationDate())
                .storageLocation(request.getStorageLocation())
                .status(request.getStatus())
                .note(request.getNote())
                .receiptTimestamp(LocalDateTime.now())
                .receivedBy(receivedBy)
                .build();

        //Lưu xuống DB trước để có ID
        ReagentSupplyHistory savedSupply = reagentSupplyRepository.save(newSupply);

        monitoringEventPublisher.publishEvent("reagent", savedSupply.getReagent().getReagentId().toString(), "REAGENT_SUPPLY_ADDED",
                Map.of("supplyId", savedSupply.getSupplyId().toString(),
                        "quantity", String.valueOf(savedSupply.getQuantity()),
                        "reagentID", savedSupply.getReagent().getReagentId().toString(),
                        "performedBy", savedSupply.getReceivedBy()));

        //Trả về response DTO
        return toResponse(savedSupply);

    }
    //Vendor

    // Lấy toàn bộ kho
    @Override
    public List<ReagentSupplyHistoryResponse> getReagentInventory() {
        List<ReagentSupplyHistory> supplyList = reagentSupplyRepository.findAll();
        if (supplyList.isEmpty()) {
            throw new WarehouseException(ErrorCode.NO_DATA);
        }
        return supplyList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReagentSupplyHistoryResponse> getReagentInventory(UUID reagentId) {
        List<ReagentSupplyHistory> supplyList = reagentSupplyRepository.findByReagent_ReagentId(reagentId);
        if (supplyList.isEmpty()) {
            throw new WarehouseException(ErrorCode.NO_DATA);
        }
        return supplyList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReagentSupplyHistoryResponse> getExpiringWithinOneWeek() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate endDate = today.plusDays(7);
        List<ReagentSupplyHistory> expiring = reagentSupplyRepository.findByExpirationDateBetween(today, endDate);
        return expiring.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }


    //Convert response
    private ReagentSupplyHistoryResponse toResponse(ReagentSupplyHistory entity) {
        return ReagentSupplyHistoryResponse.builder()
                .supplyId(entity.getSupplyId())
                .reagentId(entity.getReagent().getReagentId())
                .reagentName(entity.getReagent().getName())
                .vendorId(entity.getVendor().getVendorId())
                .vendorName(entity.getVendor().getVendorName())
                .poNumber(entity.getPoNumber())
                .orderDate(entity.getOrderDate())
                .receiptDate(entity.getReceiptDate())
                .quantity(entity.getQuantity())
                .unitOfMeasure(entity.getUnitOfMeasure())
                .lotNumber(entity.getLotNumber())
                .expirationDate(entity.getExpirationDate())
                .receivedBy(entity.getReceivedBy())
                .receiptTimestamp(entity.getReceiptTimestamp())
                .storageLocation(entity.getStorageLocation())
                .status(entity.getStatus())
                .note(entity.getNote())
                .build();
    }

}
