package org.overcode250204.monitoringservice.dtos;

import lombok.*;
import org.overcode250204.monitoringservice.entities.HealthEventLogs;
import org.overcode250204.monitoringservice.enums.HealthEventType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthEventLogDTO {
    private String healthEventLogId;
    private String brokerId;
    private HealthEventType healthEventType;
    private String details;
    private LocalDateTime createdAt;

    public static HealthEventLogDTO fromEntity(HealthEventLogs entity) {
        if (entity == null) {
            return null;
        }
        return HealthEventLogDTO.builder()
                .healthEventLogId(entity.getHealthEventLogId())
                .brokerId(entity.getBrokerId())
                .healthEventType(entity.getHealthEventType())
                .details(entity.getDetails())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public HealthEventLogs toEntity() {
        return HealthEventLogs.builder()
                .brokerId(this.brokerId)
                .healthEventType(this.healthEventType)
                .details(this.details)
                .build();
    }
}
