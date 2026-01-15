package org.overcode250204.monitoringservice.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document("event_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventLogs {

    @Id
    private String eventLogId;
    private String eventName;
    private String sourceService;
    private Map<String, Object> payload;
    private String severity;
    private String performedBy;
    private String message;
    @CreatedDate
    private LocalDateTime createdAt;
}

