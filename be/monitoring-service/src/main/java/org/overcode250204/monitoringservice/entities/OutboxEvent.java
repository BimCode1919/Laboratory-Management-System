package org.overcode250204.monitoringservice.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "outbox_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    private String id;

    private String aggregateType;

    private String aggregateId;

    private String eventType;

    private String payload;

    private String status;

    @CreatedDate
    private LocalDateTime createdAt;
}
