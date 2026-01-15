package org.overcode250204.testorderservice.elastic.repositories;

import org.overcode250204.testorderservice.elastic.documents.TestOrderDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestOrderDocumentRepository extends ElasticsearchRepository<TestOrderDocument, String> {
    // Dùng cho gợi ý khi đang gõ dang trong thanh SearchBox
    // Query vào trường ".suggest" và dùng toán tử "and"
    @Query("{\"match\": {\"patientFullName.suggest\": {\"query\": \"?0\"}}}")
    Page<TestOrderDocument> suggestByPatientFullName(String name, Pageable pageable);
}