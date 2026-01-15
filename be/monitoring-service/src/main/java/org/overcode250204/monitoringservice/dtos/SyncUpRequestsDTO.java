package org.overcode250204.monitoringservice.dtos;

import lombok.*;
import org.overcode250204.monitoringservice.entities.SyncUpRequests;
import org.overcode250204.monitoringservice.enums.SyncUpRequestsStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncUpRequestsDTO {
    private String syncUpRequestId;
    private String sourceService;
    private String messageId;
    private String status; // String để client gửi, service sẽ convert sang Enum
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SyncUpRequestsDTO fromEntity(SyncUpRequests entity) {
        if (entity == null) {
            return null;
        }
        return SyncUpRequestsDTO.builder()
                .syncUpRequestId(entity.getSyncUpRequestId())
                .sourceService(entity.getSourceService())
                .messageId(entity.getMessageId())
                .status(entity.getStatus().name())
                .processedAt(entity.getProcessedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SyncUpRequests toEntity() {
        return SyncUpRequests.builder()
                .sourceService(this.sourceService)
                .messageId(this.messageId)
                .status(SyncUpRequestsStatus.valueOf(this.status))
                .build();
    }
}
