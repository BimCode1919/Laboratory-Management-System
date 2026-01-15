package org.overcode250204.warehouseservice.model.dto.reagent;

import lombok.*;
import org.overcode250204.warehouseservice.model.enums.SupplyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentSupplyHistoryRequest {
    private UUID reagentId;           // ID của hóa chất liên quan
    private UUID vendorID;            //ID của nhà cung cấp
    private String poNumber;          // Số PO (Purchase Order)
    private LocalDate orderDate;      // Ngày đặt hàng
    private LocalDate receiptDate;    // Ngày nhận hàng
    private BigDecimal quantity;      // Số lượng nhận
    private String unitOfMeasure;     // Đơn vị đo lường (vd: L, g, ml, ...)
    private String lotNumber;         // Số lô
    private LocalDate expirationDate; // Hạn sử dụng

    private String storageLocation;   // Vị trí lưu trữ ban đầu
    private SupplyStatus status;      // Trạng thái nhập (Received | Partial Shipment | Returned)
    private String note;              // Ghi chú thêm (nếu có)
}
