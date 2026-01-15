package org.overcode250204.monitoringservice.dtos;

import lombok.*;
import org.overcode250204.monitoringservice.entities.EventLogs;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLogDTO {
    private String id;
    private String eventName;
    private String sourceService;
    private String message;
    private String severity;
    private String performedBy;
    private LocalDateTime createdAt;
    private Map<String, Object> payload;

    // Mapping entity to DTO
    public static EventLogDTO fromEntity(EventLogs entity) {
        if (entity == null) {
            return null;
        }
        return EventLogDTO.builder()
                .id(entity.getEventLogId())
                .eventName(entity.getEventName())
                .sourceService(entity.getSourceService())
                .message(entity.getMessage())
                .severity(entity.getSeverity())
                .performedBy(entity.getPerformedBy())
                .createdAt(entity.getCreatedAt())
                .payload(entity.getPayload())
                .build();
    }

    // Mapping DTO to entity for creation
    public EventLogs toEntity() {
        return EventLogs.builder()
                .eventLogId(this.id)
                .eventName(this.eventName)
                .sourceService(this.sourceService)
                .message(this.message)
                .severity(this.severity)
                .performedBy(this.performedBy)
                .payload(this.payload)
                .createdAt(this.createdAt != null ? this.createdAt : LocalDateTime.now())
                .build();
    }
}
