package org.overcode250204.testorderservice.elastic.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.elastic.documents.TestOrderDocument;
import org.overcode250204.testorderservice.elastic.services.TestOrderSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/elastic/test-orders")
@RequiredArgsConstructor
public class TestOrderSearchController {
    private final TestOrderSearchService searchService;

    //Search mới, để test kết hợp nhiều tham số như name, status, ....
    @GetMapping("/search")
    public ResponseEntity<Page<TestOrderDocument>> search(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String testType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String barCode,
            @RequestParam(required = false) String patientCode,
            @RequestParam(required = false) String medicalRecordId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdAtTo,
            @RequestParam(required = false) String resultParamName,
            @RequestParam(required = false) String resultStatus,
            Pageable pageable) {

        Page<TestOrderDocument> results = searchService.searchOrders(name, status, testType, priority, barCode, patientCode, medicalRecordId, createdAtFrom, createdAtTo, resultParamName, resultStatus, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/suggest")
    public ResponseEntity<Page<TestOrderDocument>> suggest(
            @RequestParam String query) {
        Page<TestOrderDocument> suggestions = searchService.getSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }
}
