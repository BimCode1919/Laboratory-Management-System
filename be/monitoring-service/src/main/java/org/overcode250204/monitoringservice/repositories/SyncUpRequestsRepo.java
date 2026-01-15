package org.overcode250204.monitoringservice.repositories;

import org.overcode250204.monitoringservice.entities.SyncUpRequests;
import org.overcode250204.monitoringservice.enums.SyncUpRequestsStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // <-- Bổ sung import

@Repository
public interface SyncUpRequestsRepo extends MongoRepository<SyncUpRequests, String> {

    // Giữ nguyên: Dùng cho API Controller (có phân trang)
    Page<SyncUpRequests> findByMessageId(String messageId, Pageable pageable);

    // Giữ nguyên: Dùng cho API Controller (có phân trang)
    Page<SyncUpRequests> findByStatus(SyncUpRequestsStatus status, Pageable pageable);

    // --- BỔ SUNG PHƯƠNG THỨC MỚI ---
    /**
     * Tìm TẤT CẢ các yêu cầu đồng bộ (sync-up request) theo trạng thái.
     * Dùng cho SyncUpScheduler (Worker) để lấy tất cả các job "PENDING" về xử lý.
     * @param status Trạng thái cần tìm (ví dụ: PENDING)
     * @return Danh sách (List) tất cả các request khớp
     */
    List<SyncUpRequests> findByStatus(SyncUpRequestsStatus status);
}