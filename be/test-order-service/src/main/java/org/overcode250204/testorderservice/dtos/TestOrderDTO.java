package org.overcode250204.testorderservice.dtos;

import lombok.*;
import org.overcode250204.testorderservice.models.enums.TestOrderPriority;
import org.overcode250204.testorderservice.models.enums.TestOrderType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestOrderDTO {
    private UUID id;
    private PatientReferenceDTO patient;
    private String orderCode;
    private String status;
    private LocalDateTime createdAt;
    private TestOrderPriority priority;
    private TestOrderType testType;
    private String notes;
    private Boolean isDeleted;
}