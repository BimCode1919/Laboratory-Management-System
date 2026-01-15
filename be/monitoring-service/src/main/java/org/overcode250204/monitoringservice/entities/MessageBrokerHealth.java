package org.overcode250204.monitoringservice.entities;

import lombok.*;
import org.overcode250204.monitoringservice.enums.MessageBrokerHealthStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("message_broker_health")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageBrokerHealth {

    @Id
    private String messageBrokerHealthId;

    private String brokerName;       // e.g., "RabbitMQ", "Kafka"
    private MessageBrokerHealthStatus status;
    private LocalDateTime lastCheckedAt;
    private Integer retryAttempts;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime recoveredAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
