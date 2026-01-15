package org.overcode250204.warehouseservice.grpc.mapper;

import org.overcode250204.common.grpc.ReagentInfo;
import org.overcode250204.warehouseservice.model.entities.Reagent;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReagentMapper {

    public static ReagentInfo toGrpc(Reagent r) {
        return toGrpc(r, null, null, null);
    }

    public static ReagentInfo toGrpc(Reagent r, BigDecimal availableQuantity) {
        return toGrpc(r, availableQuantity, null, null);
    }

    public static ReagentInfo toGrpc(Reagent r, BigDecimal availableQuantity, BigDecimal totalQuantity, LocalDate expirationDate) {
        if (r == null) return ReagentInfo.newBuilder().build();
        ReagentInfo.Builder b = ReagentInfo.newBuilder()
                .setReagentId(r.getReagentId() != null ? r.getReagentId().toString() : "")
                .setName(nullToEmpty(r.getName()))
                .setCatalogNumber(nullToEmpty(r.getCatalogNumber()))
                .setManufacturer(nullToEmpty(r.getManufacturer()))
                .setCasNumber(nullToEmpty(r.getCasNumber()))
                .setCreatedBy(r.getCreatedBy() != null ? r.getCreatedBy().toString() : "")
                .setUpdatedBy(r.getUpdatedBy() != null ? r.getUpdatedBy().toString() : "")
                .setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt().toString() : "")
                .setUpdatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : "");

        if (availableQuantity != null) b.setQuantity(availableQuantity.toPlainString());
        if (totalQuantity != null) b.setTotalQuantity(totalQuantity.toPlainString());
        if (expirationDate != null) b.setExpirationDate(expirationDate.toString());

        return b.build();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
