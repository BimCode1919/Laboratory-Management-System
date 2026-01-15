package org.overcode250204.testorderservice.mappers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.TestResultReportDTO;
import org.overcode250204.testorderservice.dtos.TestResultReviewDTO;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestResultsMapper {
    public TestResultReportDTO toReportDTO(TestResults entity) {
        return TestResultReportDTO.builder()
                .id(entity.getId())
                .parameterName(entity.getParameterName())
                .resultValue(entity.getResultValue())
                .unit(entity.getUnit())
                .referenceLow(entity.getReferenceLow())
                .referenceHigh(entity.getReferenceHigh())
                .alertLevel(entity.getAlertLevel() != null ? entity.getAlertLevel().name() : null)
                .aiReviewComment(entity.getAiReviewComment())
                .build();
    }

    public TestResultReviewDTO toReviewDTO(TestResults entity) {
        return TestResultReviewDTO.builder()
                .id(entity.getId())
                .parameterName(entity.getParameterName())
                .resultValue(entity.getResultValue())
                .unit(entity.getUnit())
                .alertLevel(entity.getAlertLevel() != null ? entity.getAlertLevel().name() : "NONE")
                .aiHasIssue(entity.getAiHasIssue())
                .aiReviewComment(entity.getAiReviewComment())
                .build();
    }
}
