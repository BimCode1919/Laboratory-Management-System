package org.overcode250204.testorderservice.elastic.services.impls;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.elastic.documents.TestOrderDocument;
import org.overcode250204.testorderservice.elastic.repositories.TestOrderDocumentRepository;
import org.overcode250204.testorderservice.elastic.services.TestOrderSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestOrderSearchServiceImpl implements TestOrderSearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final TestOrderDocumentRepository documentRepository;

    @Override
    public Page<TestOrderDocument> getSuggestions(String query) {
        // Luôn giới hạn số lượng gợi ý (ví dụ: 5) để đảm bảo tốc độ
        Pageable suggestPageable = PageRequest.of(0, 5);
        return documentRepository.suggestByPatientFullName(query, suggestPageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TestOrderDocument> searchOrders(String name,
                                                String status,
                                                String testType,
                                                String priority,
                                                String barCode,
                                                String patientCode,
                                                String medicalRecordId,
                                                LocalDate createdAtFrom,
                                                LocalDate createdAtTo,
                                                String resultParamName,
                                                String resultStatus,
                                                Pageable pageable) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // 1. Full-text search (MUST - vì cần tính điểm độ phù hợp cho Patient's name)
        if (StringUtils.hasText(name)) {
            boolQueryBuilder.must(m -> m
                    .match(ma -> ma
                            .field("patientFullName.suggest")
                            .query(name)
                            .operator(Operator.And)
                    )
            );
        } else {
            boolQueryBuilder.must(m -> m.matchAll(ma -> ma));
        }

        // 2. Filters (FILTER - tìm kiếm chính xác, không tính điểm, nhanh hơn)
        if (StringUtils.hasText(status)) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field("status")
                            .value(status)
                    )
            );
        }

        if (StringUtils.hasText(testType)) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field("testType")
                            .value(testType)
                    )
            );
        }

        if (StringUtils.hasText(priority)) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field("priority")
                            .value(priority)
                    )
            );
        }

        if (StringUtils.hasText(barCode)) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field("barCode")
                            .value(barCode)
                    )
            );
        }

        if (StringUtils.hasText(patientCode)) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field("patientCode")
                            .value(patientCode)
                    )
            );
        }

        if (StringUtils.hasText(medicalRecordId)) {
            boolQueryBuilder.filter(f -> f
                    .term(t -> t
                            .field("medicalRecordId")
                            .value(medicalRecordId)
                    )
            );
        }

        if (createdAtFrom != null || createdAtTo != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            RangeQuery rangeQuery = RangeQuery.of(r -> r
                    .date(d -> d
                            .field("createdAt")
                            .gte(createdAtFrom != null ? createdAtFrom.atStartOfDay().format(formatter) : null)
                            .lte(createdAtTo != null ? createdAtTo.plusDays(1).atStartOfDay().format(formatter) : null)
                    )
            );

            boolQueryBuilder.filter(q -> q.range(rangeQuery));
        }

        // 3. Nested Filters
        BoolQuery.Builder nestedBoolQuery = new BoolQuery.Builder();
        boolean hasNestedFilter = false;

        // Test Results
        if (StringUtils.hasText(resultParamName)) {
            nestedBoolQuery.filter(f -> f
                    .term(t -> t
                            .field("results.parameterName")
                            .value(resultParamName)
                    )
            );
            hasNestedFilter = true;
        }

        if (StringUtils.hasText(resultStatus)) {
            nestedBoolQuery.filter(f -> f
                    .term(t -> t
                            .field("results.status")
                            .value(resultStatus)
                    )
            );
            hasNestedFilter = true;
        }

        if (hasNestedFilter) {
            boolQueryBuilder.must(m -> m
                    .nested(n -> n
                            .path("results") // <-- Tên của trường List
                            .query(nestedBoolQuery.build()._toQuery())
                    )
            );
        }

        // 4. Execute
        Query esQuery = boolQueryBuilder.build()._toQuery();
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(esQuery)
                .withPageable(pageable)
                .build();

        SearchHits<TestOrderDocument> searchHits = elasticsearchOperations.search(nativeQuery, TestOrderDocument.class);

        List<TestOrderDocument> content = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }
}