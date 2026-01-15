package org.overcode250204.warehouseservice.model.dto.reagent;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentWithAvailableQuantityRespone {
    private UUID reagentId; // ID của hóa chất
    private String name;  // Tên hóa chất
    private String catalogNumber;  // Mã danh mục
    private String manufacturer;// Nhà sản xuất
    private String casNumber;// Số CAS (nếu có)
    private BigDecimal quantity;

}
