package org.overcode250204.monitoringservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.monitoringservice.dtos.HealthEventLogDTO;
import org.overcode250204.monitoringservice.entities.MessageBrokerHealth;
import org.overcode250204.monitoringservice.enums.HealthEventType;
import org.overcode250204.monitoringservice.enums.MessageBrokerHealthStatus;
import org.overcode250204.monitoringservice.repositories.MessageBrokerHealthRepo;
import org.overcode250204.monitoringservice.services.HealthEventLogsService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class BrokerHealthMonitor {

    private final MessageBrokerHealthRepo healthRepo;
    private final HealthEventLogsService healthLogsService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Tên broker này phải thống nhất trong toàn hệ thống
    private static final String BROKER_NAME = "primary-kafka-cluster";
    private static final int RETRY_COUNT = 3; // Yêu cầu SRS [cite: 493]

    @Scheduled(fixedDelayString = "${app.monitoring.healthCheckInterval:60000}") // 60 giây, có thể cấu hình [cite: 492]
    public void checkBrokerHealth() {
        log.debug("Running Message Broker health check...");
        MessageBrokerHealth currentHealth = findOrCreateHealthRecord();

        boolean isHealthy = performHealthCheck();

        if (isHealthy) {
            // Nếu trước đó đang LỖI, bây giờ KHỎE
            if (currentHealth.getStatus() != MessageBrokerHealthStatus.HEALTHY) {
                log.info("Message Broker has recovered. Status: HEALTHY");
                // Ghi lại "sự kiện phục hồi"
                logHealthEvent(currentHealth, HealthEventType.RECONNECTED, "Broker connection restored.");
                // Cập nhật trạng thái hiện tại
                updateStatus(currentHealth, MessageBrokerHealthStatus.HEALTHY, 0);
            }
            // Nếu vẫn đang KHỎE, chỉ cần cập nhật thời gian kiểm tra
            else {
                updateStatus(currentHealth, MessageBrokerHealthStatus.HEALTHY, 0);
            }
        } else {
            // Nếu VẪN LỖI hoặc MỚI LỖI
            int newRetryCount = currentHealth.getRetryAttempts() + 1;
            log.warn("Message Broker health check failed. Attempt {}", newRetryCount);

            // Chỉ ghi log lỗi chi tiết khi xác nhận thất bại (sau 3 lần thử)
            if (newRetryCount >= RETRY_COUNT) {
                if (currentHealth.getStatus() != MessageBrokerHealthStatus.UNHEALTHY) {
                    // Lần đầu tiên phát hiện lỗi sau 3 lần thử -> Ghi log
                    logHealthEvent(currentHealth, HealthEventType.DISCONNECTED, "Broker unresponsive after " + RETRY_COUNT + " retries.");
                }
                // Cập nhật trạng thái UNHEALTHY
                updateStatus(currentHealth, MessageBrokerHealthStatus.UNHEALTHY, newRetryCount);
            } else {
                // Vẫn đang trong quá trình thử lại, chưa xác nhận UNHEALTHY
                updateStatus(currentHealth, currentHealth.getStatus(), newRetryCount);
            }
        }
    }

    // "Ping" Kafka (an toàn)
    private boolean performHealthCheck() {
        try {
            // Thử lấy metadata 1 topic bất kỳ.
            // Dùng 1 topic chắc chắn tồn tại (vd: hl7.raw.backup)
            kafkaTemplate.partitionsFor("hl7.raw.backup");
            return true;
        } catch (Exception e) {
            log.error("Health check ping failed: {}", e.getMessage());
            return false;
        }
    }

    // Hàm tiện ích
    private void updateStatus(MessageBrokerHealth health, MessageBrokerHealthStatus status, int retryAttempts) {
        health.setStatus(status);
        health.setRetryAttempts(retryAttempts);
        health.setLastCheckedAt(LocalDateTime.now());
        if (status == MessageBrokerHealthStatus.HEALTHY) {
            health.setRecoveredAt(LocalDateTime.now());
        }
        healthRepo.save(health);
    }

    private void logHealthEvent(MessageBrokerHealth health, HealthEventType type, String details) {
        healthLogsService.createHealthEventLog(HealthEventLogDTO.builder()
                .brokerId(health.getMessageBrokerHealthId())
                .healthEventType(type)
                .details(details)
                .build());
    }

    private MessageBrokerHealth findOrCreateHealthRecord() {
        return healthRepo.findByBrokerName(BROKER_NAME)
                .orElseGet(() -> {
                    log.info("No health record found for '{}', creating new one.", BROKER_NAME);
                    return healthRepo.save(MessageBrokerHealth.builder()
                            .brokerName(BROKER_NAME)
                            .status(MessageBrokerHealthStatus.UNKNOWN)
                            .retryAttempts(0)
                            .build());
                });
    }
}