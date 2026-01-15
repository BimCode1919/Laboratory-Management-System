package org.overcode250204.warehouseservice.model.dto.reagent;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponse {
    private UUID vendorId;
    private String vendorName;
}
