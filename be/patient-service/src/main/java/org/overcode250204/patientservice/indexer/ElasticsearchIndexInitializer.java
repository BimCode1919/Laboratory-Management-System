package org.overcode250204.patientservice.indexer;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class ElasticsearchIndexInitializer {

    private final ElasticsearchClient client;

    private static final Map<String, String> INDEX_FILES = Map.of(
            "medical_records", "elasticsearch/medical_records.json",
            "medical_record_detail", "elasticsearch/medical_record-detail.json"
    );

    @PostConstruct
    public void init() throws IOException {
        for (var entry : INDEX_FILES.entrySet()) {
            String index = entry.getKey();
            String file = entry.getValue();

            boolean exists = client.indices().exists(e -> e.index(index)).value();
            if (!exists) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(file)) {
                    if (is == null) throw new IOException("File not found: " + file);
                    client.indices().create(c -> c.index(index).withJson(new InputStreamReader(is)));
                    log.info("Created index [{}] with settings [{}]", index, file);
                }
            } else {
                log.info("Index [{}] already exists, skipping creation", index);
            }
        }
    }
}