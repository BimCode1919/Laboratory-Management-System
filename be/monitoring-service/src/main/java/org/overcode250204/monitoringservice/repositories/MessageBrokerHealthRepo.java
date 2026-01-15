package org.overcode250204.monitoringservice.repositories;

import org.overcode250204.monitoringservice.entities.MessageBrokerHealth;
import org.overcode250204.monitoringservice.enums.MessageBrokerHealthStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // <-- Bổ sung import

@Repository
public interface MessageBrokerHealthRepo extends MongoRepository<MessageBrokerHealth, String> {

    // Giữ nguyên: Dùng cho API Controller (có phân trang)
    Page<MessageBrokerHealth> findByBrokerName(String brokerName, Pageable pageable);

    // Giữ nguyên: Dùng cho API Controller (có phân trang)
    Page<MessageBrokerHealth> findByStatus(MessageBrokerHealthStatus status, Pageable pageable);

    // --- BỔ SUNG PHƯƠNG THỨC MỚI ---
    /**
     * Tìm bản ghi trạng thái sức khỏe (health record) bằng tên broker.
     * Dùng cho BrokerHealthMonitor (Worker) để tìm bản ghi trạng thái hiện tại.
     * @param brokerName Tên của broker (ví dụ: "primary-kafka-cluster")
     * @return Optional chứa bản ghi nếu tìm thấy
     */
    Optional<MessageBrokerHealth> findByBrokerName(String brokerName);
}