package org.overcode250204.monitoringservice.dtos;

import lombok.*;
import org.overcode250204.monitoringservice.entities.MessageBrokerHealth;
import org.overcode250204.monitoringservice.enums.MessageBrokerHealthStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageBrokerHealthDTO {
    private String messageBrokerHealthId;
    private String brokerName;
    private String status; // String để client gửi, service sẽ convert sang Enum
    private LocalDateTime lastCheckedAt;
    private int retryAttempts;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime recoveredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static MessageBrokerHealthDTO fromEntity(MessageBrokerHealth entity) {
        if (entity == null) {
            return null;
        }
        return MessageBrokerHealthDTO.builder()
                .messageBrokerHealthId(entity.getMessageBrokerHealthId())
                .brokerName(entity.getBrokerName())
                .status(entity.getStatus().name())
                .lastCheckedAt(entity.getLastCheckedAt())
                .retryAttempts(entity.getRetryAttempts())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .recoveredAt(entity.getRecoveredAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MessageBrokerHealth toEntity() {
        return MessageBrokerHealth.builder()
                .brokerName(this.brokerName)
                .status(MessageBrokerHealthStatus.valueOf(this.status))
                .errorCode(this.errorCode)
                .errorMessage(this.errorMessage)
                .build();
    }
}
