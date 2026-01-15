package org.overcode250204.testorderservice.elastic.services;

// Interface cho dịch vụ reindex hàng loạt (bulk) từ SQL sang Elasticsearch.
public interface TestOrderBulkReindexService {
    /**
     * Bắt đầu quá trình reindex toàn bộ.
     * Tác vụ này nên được chạy bất đồng bộ.
     */
    void startFullReindex();
}
