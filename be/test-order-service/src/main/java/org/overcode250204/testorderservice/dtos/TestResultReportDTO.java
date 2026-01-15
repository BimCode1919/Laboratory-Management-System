package org.overcode250204.testorderservice.dtos;

import lombok.*;
import org.overcode250204.testorderservice.models.enums.TestResultAlertLevel;
import org.overcode250204.testorderservice.models.enums.TestResultStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultReportDTO {
    private UUID id;
    private String parameterName;
    private Double resultValue;
    private String unit;
    private String referenceLow;
    private String referenceHigh;
    private String alertLevel;
    private String aiReviewComment;
}
