package org.overcode250204.warehouseservice.model.dto.reagent;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentRequest {
    private String name;           // Tên hóa chất
    private String catalogNumber;  // Mã danh mục
    private String manufacturer;   // Nhà sản xuất
    private String casNumber;      // Số CAS (nếu có)
}
