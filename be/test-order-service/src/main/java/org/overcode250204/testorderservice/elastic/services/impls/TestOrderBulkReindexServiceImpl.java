package org.overcode250204.testorderservice.elastic.services.impls;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.elastic.documents.TestOrderDocument;
import org.overcode250204.testorderservice.elastic.mappers.TestOrderDocumentMapper;
import org.overcode250204.testorderservice.elastic.repositories.TestOrderDocumentRepository;
import org.overcode250204.testorderservice.elastic.services.TestOrderBulkReindexService;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.overcode250204.testorderservice.repositories.TestResultsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestOrderBulkReindexServiceImpl implements TestOrderBulkReindexService {

    // Kích thước của mỗi lô (batch)
    private static final int BATCH_SIZE = 500;

    private final TestOrdersRepository testOrderRepository; // JPA Repo (để đọc data từ SQL)
    private final TestOrderDocumentRepository documentRepository; // ES Repo (để ghi data vào Elastic)
    private final TestResultsRepository testResultsRepository;

    private final TestOrderDocumentMapper mapper;


    @Override
    @Async // Rất quan trọng: Chạy tác vụ này trên một thread riêng biệt
    @Transactional(readOnly = true) // Cần Transactional để `findAllWithPatient` hoạt động
    public void startFullReindex() {
        log.info(">>> BẮT ĐẦU BULK REINDEX JOB CHO TEST ORDERS...");

        // Xóa sạch index cũ (tùy chọn, nhưng khuyến khích)
        log.info("Xóa dữ liệu cũ trên index 'test_orders_index'...");
        documentRepository.deleteAll();
        log.info("Đã xóa dữ liệu cũ.");

        Pageable pageable = PageRequest.of(0, BATCH_SIZE);
        Page<TestOrders> page;
        int batchCount = 0;

        try {
            do {
                // 1. ĐỌC TỪ SQL (đã JOIN FETCH)
                page = testOrderRepository.findAllWithPatient(pageable);
                List<TestOrders> orders = page.getContent();

                if (orders.isEmpty()) {
                    log.info("Không tìm thấy record nào, kết thúc.");
                    break;
                }

                log.info("Đang xử lý batch {}... ({} records)",
                        page.getNumber(), orders.size());

                // 2. Lấy dữ liệu con của Test Results
                // Lấy danh sách ID của các order trong batch này
                List<UUID> orderIds = orders.stream()
                        .map(TestOrders::getId)
                        .toList();

                // Thực hiện 1 query để lấy TẤT CẢ results cho batch này
                List<TestResults> allResults = testResultsRepository.findByTestOrder_IdIn(orderIds);

                // Nhóm các results lại theo TestOrder ID để map dễ dàng
                Map<UUID, List<TestResults>> resultsMap = allResults.stream()
                        .collect(Collectors.groupingBy(result -> result.getTestOrder().getId()));

                // 3. BIẾN ĐỔI (MAP)
                List<TestOrderDocument> documents = orders.stream()
                        .map(order -> {
                            // Lấy danh sách results cho order này từ Map
                            List<TestResults> orderResults = resultsMap.getOrDefault(order.getId(), Collections.emptyList());
                            // Gọi mapper với 2 tham số
                            return mapper.mapToDocument(order, orderResults);
                        })
                        .toList();

                // 4. GHI HÀNG LOẠT VÀO ES
                // saveAll() của Spring Data ES chính là một lệnh bulk
                documentRepository.saveAll(documents);

                pageable = page.nextPageable();
                batchCount++;

            } while (page.hasNext());

            log.info(">>> KẾT THÚC BULK REINDEX JOB. Tổng số batch đã xử lý: {}", batchCount);

        } catch (Exception e) {
            log.error("LỖI NGHIÊM TRỌNG trong quá trình reindex: {}", e.getMessage(), e);
        }
    }
}