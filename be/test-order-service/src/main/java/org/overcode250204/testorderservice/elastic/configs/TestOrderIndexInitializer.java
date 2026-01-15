package org.overcode250204.testorderservice.elastic.configs;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class TestOrderIndexInitializer {
    private final ElasticsearchClient esClient;

    @Value("classpath:elastic/test_orders_index.json")
    private Resource indexConfig;

    private static final String INDEX_NAME = "test_orders_index";

    @PostConstruct
    public void initIndex() {
        try {
            // 1. Kiểm tra xem index đã tồn tại chưa
            BooleanResponse exists = esClient.indices().exists(e -> e.index(INDEX_NAME));

            if (exists.value()) {
                log.info("[Elasticsearch] Index '{}' already exists. Skipping creation.", INDEX_NAME);
                return;
            }

            // 2. Nếu chưa, tạo mới từ file JSON
            if (!indexConfig.exists()) {
                log.error("[Elasticsearch] Configuration file not found: {}", indexConfig.getFilename());
                return;
            }

            try (InputStream input = indexConfig.getInputStream()) {
                esClient.indices().create(c -> c
                        .index(INDEX_NAME)
                        .withJson(input) // Phép màu nằm ở đây: Đọc trực tiếp JSON
                );
                log.info("[Elasticsearch] Index '{}' created successfully from JSON config.", INDEX_NAME);
            }

        } catch (IOException e) {
            log.error("[Elasticsearch] Failed to initialize index '{}': {}", INDEX_NAME, e.getMessage(), e);
            // Không ném lỗi để tránh làm sập app, nhưng nên alert
        }
    }
}