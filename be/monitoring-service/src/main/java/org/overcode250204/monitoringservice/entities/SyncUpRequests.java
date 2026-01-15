package org.overcode250204.monitoringservice.entities;

import lombok.*;
import org.overcode250204.monitoringservice.enums.SyncUpRequestsStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("sync_up_requests")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncUpRequests {

    @Id
    private String syncUpRequestId;

    private String sourceService;
    private String messageId;
    private SyncUpRequestsStatus status;
    private LocalDateTime processedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}