package org.overcode250204.warehouseservice.model.dto.reagent;

import lombok.*;
import org.overcode250204.warehouseservice.model.enums.SupplyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentSupplyHistoryResponse {

    private UUID supplyId;            // ID của lần nhập
    private UUID reagentId;           // ID hóa chất (liên kết)
    private String reagentName;       // Tên hóa chất
    private UUID vendorId;            // ID nhà cung cấp (nếu có)
    private String vendorName;        // Tên nhà cung cấp
    private String poNumber;          // Số đơn hàng
    private LocalDate orderDate;      // Ngày đặt hàng
    private LocalDate receiptDate;    // Ngày nhận hàng
    private BigDecimal quantity;      // Số lượng nhận
    private String unitOfMeasure;     // Đơn vị đo lường
    private String lotNumber;         // Số lô
    private LocalDate expirationDate; // Hạn sử dụng
    private UUID receivedBy;        // Người nhận
    private LocalDateTime receiptTimestamp; // Thời điểm ghi nhận nhập hàng
    private String storageLocation;   // Nơi lưu trữ
    private SupplyStatus status;      // Trạng thái nhập hàng
    private String note;              // Ghi chú (nếu có)

}
