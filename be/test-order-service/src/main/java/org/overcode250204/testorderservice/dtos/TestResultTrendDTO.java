package org.overcode250204.testorderservice.dtos;

import lombok.*;
import org.overcode250204.testorderservice.models.enums.TestResultAlertLevel;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestResultTrendDTO {
    private UUID testOrderId;
    private LocalDateTime orderCreatedAt; // show on x-axis of the chart
    private String parameterName; // use in the tab to select which parameter to show the chart
    private Double resultValue;
    private String unit;
    private String referenceLow; // show on y-axis as lower bound
    private String referenceHigh; // show on y-axis as upper bound
    private TestResultAlertLevel alertLevel;
}
