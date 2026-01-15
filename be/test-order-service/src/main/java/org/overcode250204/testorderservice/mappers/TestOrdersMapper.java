package org.overcode250204.testorderservice.mappers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.TestOrderDTO;
import org.overcode250204.testorderservice.dtos.TestOrderReportDetailDTO;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TestOrdersMapper {
    private final PatientReferenceMapper patientReferenceMapper;
    private final TestResultsMapper testResultsMapper;
    private final TestCommentsMapper testCommentsMapper;

    public TestOrderDTO toDTO(TestOrders entity) {
        if (entity == null) return null;
        return new TestOrderDTO(
                entity.getId(),
                patientReferenceMapper.toDTO(entity.getPatient()),
                entity.getOrderCode(),
                entity.getStatus().toString(),
                entity.getCreatedAt(),
                entity.getPriority(),
                entity.getTestType(),
                entity.getNotes(),
                entity.getIsDeleted()
        );
    }

    public TestOrderReportDetailDTO toReportDetailDTO(TestOrders order) {
        if (order == null || order.getPatient() == null) return null;

        return TestOrderReportDetailDTO.builder()
                .orderCode(order.getOrderCode())
                .patientName(order.getPatient().getFullName())
                .gender(order.getPatient().getGender())
                .dateOfBirth(order.getPatient().getDateOfBirth())
                .phoneNumber(order.getPatient().getPhoneNumber())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .createdBy(order.getCreatedBy().toString())
                .createdOn(order.getCreatedAt())
                .notes(order.getNotes())
                .testResults(order.getResults() != null
                        ? order.getResults().stream().map(testResultsMapper::toReportDTO).toList()
                        : List.of())
                .testComments(order.getComments() != null
                        ? order.getComments().stream().map(testCommentsMapper::toDTO).toList()
                        : List.of())
                .build();
    }

}