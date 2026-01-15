package org.overcode250204.testorderservice.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultReviewDTO {
    private UUID id;
    private String parameterName;
    private Double resultValue;
    private String unit;
    private String alertLevel;

    private Boolean aiHasIssue;
    private String aiReviewComment;
}
