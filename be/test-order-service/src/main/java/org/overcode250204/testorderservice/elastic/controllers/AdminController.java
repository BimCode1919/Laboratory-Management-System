package org.overcode250204.testorderservice.elastic.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.elastic.services.TestOrderBulkReindexService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Giả lập cho Controller cần phải có quyền ADMIN

@RestController
@RequestMapping("/api/elastic/admin/jobs")
@RequiredArgsConstructor
public class AdminController {
    private final TestOrderBulkReindexService reindexService;

    /**
     * Kích hoạt tác vụ bulk reindex cho TestOrders.
     * Tác vụ này chạy bất đồng bộ.
     */
    @PostMapping("/reindex-test-orders")
    public ResponseEntity<String> triggerTestOrderReindex() {
        // Gọi service bất đồng bộ
        reindexService.startFullReindex();

        // Trả về 202 Accepted ngay lập tức
        return ResponseEntity.accepted()
                .body("Đã chấp nhận yêu cầu: Tác vụ reindex TestOrders đang bắt đầu chạy ngầm...");
    }
}
