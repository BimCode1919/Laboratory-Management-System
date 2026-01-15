package org.overcode250204.testorderservice.elastic.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.elastic.documents.TestOrderDocument;
import org.overcode250204.testorderservice.elastic.mappers.TestOrderDocumentMapper;
import org.overcode250204.testorderservice.elastic.repositories.TestOrderDocumentRepository;
import org.overcode250204.testorderservice.elastic.services.TestOrderIndexingService;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.overcode250204.testorderservice.repositories.TestResultsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestOrderIndexingServiceImpl implements TestOrderIndexingService {
    private final TestOrdersRepository testOrderRepository;
    private final TestOrderDocumentRepository documentRepository;
    private final TestResultsRepository testResultsRepository;
    private final TestOrderDocumentMapper mapper;

    @Override
    @Transactional
    public void deleteOrder(UUID orderId) {
        try {
            // Đồng bộ (ES) - Hard Delete
            documentRepository.deleteById(orderId.toString()); // Phải .toString()
            log.info("Successfully soft-deleted order in SQL and hard-deleted in ES: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to delete order for orderId {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to delete order: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true) // Chỉ đọc từ SQL, không ghi
    public void reindexTestOrder(UUID orderId) {
        log.info("[Re-index] Starting re-index for TestOrder ID: {}", orderId);
        try {
            // 1. Tải dữ liệu cha (Order)
            // (Sử dụng method JOIN FETCH patient nếu bạn có)
            TestOrders order = testOrderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found for re-index: " + orderId));

            // 2. Tải dữ liệu con (Results)
            List<TestResults> results = testResultsRepository.findByTestOrder_Id(orderId);

            // 3. Map và Lưu vào ES
            // (Mapper của bạn cần cả 2 tham số)
            TestOrderDocument document = mapper.mapToDocument(order, results);
            documentRepository.save(document);
            log.info("[Re-index] Successfully re-indexed order: {}", orderId);

        } catch (Exception e) {
            log.error("[Re-index] Failed to re-index order {}: {}", orderId, e.getMessage(), e);
            // Ném lỗi lại để @Async có thể xử lý (hoặc xử lý retry)
            throw new RuntimeException("Failed to re-index order: " + e.getMessage(), e);
        }
    }
}