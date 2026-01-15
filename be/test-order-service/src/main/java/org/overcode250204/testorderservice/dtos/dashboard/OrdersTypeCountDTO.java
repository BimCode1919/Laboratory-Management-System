package org.overcode250204.testorderservice.dtos.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.overcode250204.testorderservice.models.enums.TestOrderType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdersTypeCountDTO {
    private TestOrderType testOrderType;
    private long count;
}