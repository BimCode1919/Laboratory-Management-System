package org.overcode250204.monitoringservice.entities;



import lombok.*;
import org.overcode250204.monitoringservice.enums.HealthEventType;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document("health_event_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthEventLogs {

    @Id
    private String healthEventLogId;

    @Field("broker_id")
    private String brokerId;        // FK -> message_broker_health.id

    private HealthEventType healthEventType;
    private String details;

    @CreatedDate
    private LocalDateTime createdAt;
}

