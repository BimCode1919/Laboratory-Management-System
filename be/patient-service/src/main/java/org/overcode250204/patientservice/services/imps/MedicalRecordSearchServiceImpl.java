package org.overcode250204.patientservice.services.imps;

import ch.qos.logback.core.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.patientservice.documents.MedicalRecordDocument;
import org.overcode250204.patientservice.repositories.MedicalRecordSearchRepository;
import org.overcode250204.patientservice.services.MedicalRecordSearchService;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordSearchServiceImpl implements MedicalRecordSearchService {


    private final ElasticsearchOperations elasticsearchOperations;
    @Override
    public Page<MedicalRecordDocument> search(
            String keyword,
            String startDate,
            String endDate,
            String sortBy,
            Sort.Direction sortDirection,
            int page,
            int size) {

        if (!StringUtils.hasText(sortBy)) {
            sortBy = "lastTestDate";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Criteria rootCriteria = new Criteria();

        if (StringUtils.hasText(keyword)) {
            String term = keyword.trim();
            Criteria keywordCriteria = new Criteria("fullName").contains(term)
                    .or("patientCode").contains(term);
            rootCriteria = rootCriteria.subCriteria(keywordCriteria);
        }
        if (startDate != null && endDate != null) {
            String startStr = startDate.toString();
            String endStr = endDate.toString();
            log.info("DEBUG DATE FILTER: Searching for lastTestDate BETWEEN '{}' AND '{}'", startStr, endStr);
            rootCriteria = rootCriteria.and("lastTestDate").between(startDate.toString(), endDate.toString());
        } else if (startDate != null) {
            rootCriteria = rootCriteria.and("lastTestDate").greaterThanEqual(startDate.toString());
        } else if (endDate != null) {
            rootCriteria = rootCriteria.and("lastTestDate").lessThanEqual(endDate.toString());
        }

        Query query = new CriteriaQuery(rootCriteria).setPageable(pageable);

        log.info("Executing query: {}", query.toString());
        SearchHits<MedicalRecordDocument> searchHits = elasticsearchOperations.search(query, MedicalRecordDocument.class);
        List<MedicalRecordDocument> list = searchHits.stream()
                .map(SearchHit::getContent)
                .toList();
        return new PageImpl<>(list, pageable, searchHits.getTotalHits());
    }
}