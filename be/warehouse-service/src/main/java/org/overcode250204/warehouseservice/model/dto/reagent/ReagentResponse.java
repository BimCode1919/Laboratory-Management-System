package org.overcode250204.warehouseservice.model.dto.reagent;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentResponse {
    private UUID reagentId; // ID của hóa chất
    private String name;  // Tên hóa chất
    private String catalogNumber;  // Mã danh mục
    private String manufacturer;// Nhà sản xuất
    private String casNumber;// Số CAS (nếu có)
    private UUID createdBy;
    private UUID updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
