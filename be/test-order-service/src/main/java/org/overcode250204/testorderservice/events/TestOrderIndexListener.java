    package org.overcode250204.testorderservice.events;

    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.overcode250204.testorderservice.elastic.services.TestOrderIndexingService; // <-- Import service re-index
    import org.springframework.scheduling.annotation.Async;
    import org.springframework.stereotype.Component;
    import org.springframework.transaction.event.TransactionalEventListener; // <-- Import @TransactionalEventListener

    import java.util.UUID;

    @Component
    @RequiredArgsConstructor
    @Slf4j
    public class TestOrderIndexListener {
        // Service chứa logic re-index mà chúng ta đã tạo trước đó
        private final TestOrderIndexingService indexingService;

        /**
         * Lắng nghe sự kiện TestResultChangedEvent.
         * Chỉ chạy SAU KHI transaction (từ TestResultProcessingServiceImpl) đã commit thành công.
         * Chạy bất đồng bộ (@Async) để không block thread chính.
         */
        @Async
        @TransactionalEventListener
        public void handleTestResultChange(TestResultChangedEvent event) {
            if (event == null || event.getTestOrderId() == null) {
                log.warn("[EventListener] Received null or empty TestResultChangedEvent.");
                return;
            }

            UUID orderId = event.getTestOrderId();
            log.info("[EventListener] Received TestResultChangedEvent for order: {}. Triggering re-index.", orderId);

            try {
                // Gọi method re-index mà bạn đã thêm vào TestOrderIndexingService
                indexingService.reindexTestOrder(orderId);
            } catch (Exception e) {
                log.error("[EventListener] Failed to re-index order {}: {}", orderId, e.getMessage(), e);
                // (Trong thực tế, bạn có thể muốn thêm logic retry ở đây)
            }
        }
    }
