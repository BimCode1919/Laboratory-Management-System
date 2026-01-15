package org.overcode250204.testorderservice.elastic.services;

import java.util.UUID;

public interface TestOrderIndexingService {
    /**
     * Xóa một document TestOrder khỏi Elasticsearch.
     * @param orderId ID của order cần xóa
     */
    void deleteOrder(UUID orderId);

    /**
     * Tải lại TestOrder và TestResults từ SQL và re-index nó trong Elasticsearch.
     * @param orderId ID của TestOrder cha cần re-index
     */
    void reindexTestOrder(UUID orderId);
}
