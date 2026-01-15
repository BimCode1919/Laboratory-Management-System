package org.overcode250204.instrumentservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.common.grpc.ListReagentsResponse;
import org.overcode250204.common.grpc.ReagentInfo;
import org.overcode250204.instrumentservice.grpc.InstrumentWarehouseClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/internal/grpc/warehouse")
@RequiredArgsConstructor
@Slf4j
public class GrpcWarehouseController {

    @Value("${spring.application.name}")
    private String serviceName;

    private final InstrumentWarehouseClientService warehouseClientService;

    @GetMapping("/reagents")
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<Object>> listReagents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            ListReagentsResponse resp = warehouseClientService.listReagents(page, size);
            List<Map<String, Object>> items = resp.getReagentsList()
                    .stream()
                    .map(this::toMap)
                    .collect(Collectors.toList());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("page", page);
            payload.put("size", size);
            payload.put("items", items);

            return ResponseEntity.ok(BaseResponse.success(serviceName, payload));
        } catch (Exception ex) {
            log.error("gRPC listReagents failed: {}", ex.getMessage(), ex);
            Map<String, Object> err = Map.of("error", "warehouse_unavailable", "message", ex.getMessage());
            return ResponseEntity.status(502).body(BaseResponse.success(serviceName, err));
        }
    }

    @GetMapping("/reagents/{reagentId}")
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<Object>> getReagent(@PathVariable UUID reagentId) {
        try {
            ReagentInfo info = warehouseClientService.getReagent(reagentId);
            if (info == null || info.getReagentId().isEmpty()) {
                Map<String, Object> err = Map.of("error", "not_found");
                return ResponseEntity.status(404).body(BaseResponse.success(serviceName, err));
            }
            return ResponseEntity.ok(BaseResponse.success(serviceName, toMap(info)));
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid reagentId format: {}", reagentId, ex);
            Map<String, Object> err = Map.of("error", "invalid_uuid", "message", ex.getMessage());
            return ResponseEntity.badRequest().body(BaseResponse.success(serviceName, err));
        } catch (Exception ex) {
            log.error("gRPC getReagent failed for {}: {}", reagentId, ex.getMessage(), ex);
            Map<String, Object> err = Map.of("error", "warehouse_unavailable", "message", ex.getMessage());
            return ResponseEntity.status(502).body(BaseResponse.success(serviceName, err));
        }
    }

    private Map<String, Object> toMap(ReagentInfo ri) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("reagentId", ri.getReagentId());
        m.put("name", ri.getName());
        m.put("catalogNumber", ri.getCatalogNumber());
        m.put("manufacturer", ri.getManufacturer());
        m.put("casNumber", ri.getCasNumber());
        m.put("createdBy", ri.getCreatedBy());
        m.put("updatedBy", ri.getUpdatedBy());
        m.put("createdAt", ri.getCreatedAt());
        m.put("updatedAt", ri.getUpdatedAt());
        // parse quantity string (BigDecimal serialized) if present
        String qty = ri.getQuantity();
        if (!qty.isEmpty()) {
            try {
                m.put("quantity", new java.math.BigDecimal(qty));
            } catch (Exception e) {
                m.put("quantity", qty); // fallback to raw string
            }
        } else {
            m.put("quantity", null);
        }

        // parse totalQuantity string (BigDecimal serialized) if present
        String totalQty = ri.getTotalQuantity();
        if (!totalQty.isEmpty()) {
            try {
                m.put("totalQuantity", new java.math.BigDecimal(totalQty));
            } catch (Exception e) {
                m.put("totalQuantity", totalQty); // fallback to raw string
            }
        } else {
            m.put("totalQuantity", null);
        }

        // parse expirationDate (ISO-8601) if present
        String exp = ri.getExpirationDate();
        if (!exp.isEmpty()) {
            try {
                java.time.LocalDate ld = java.time.LocalDate.parse(exp);
                m.put("expirationDate", ld);
            } catch (Exception e) {
                m.put("expirationDate", exp); // fallback to raw string
            }
        } else {
            m.put("expirationDate", null);
        }

        return m;
    }
}