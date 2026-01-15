package org.overcode250204.patientservice.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.patientservice.documents.MedicalRecordDocument;
import org.overcode250204.patientservice.services.MedicalRecordDetailService;
import org.overcode250204.patientservice.services.MedicalRecordSearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/medical-records")
@RequiredArgsConstructor
public class MedicalRecordSearchController {

    private final MedicalRecordSearchService searchService;
    private final MedicalRecordDetailService detailService;

    @Value("${spring.application.name}")
    private String serviceName;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER') or hasAuthority('READ_ONLY')")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false)
            String endDate,
            @RequestParam(defaultValue = "lastTestDate") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<MedicalRecordDocument> result = searchService.search(
                keyword,
                startDate,
                endDate,
                sortBy,
                sortDirection,
                page,
                size
        );
        return ResponseEntity.ok(BaseResponse.success(serviceName, result));
    }

    @GetMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER') or hasAuthority('READ_ONLY')")
    public ResponseEntity<?> getDetail(
            @PathVariable String recordId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant endDate,
            @RequestParam(required = false) String testType,
            @RequestParam(required = false) String instrumentUsed
    ) {
        return ResponseEntity.ok(BaseResponse.success(serviceName, detailService.getFilteredDetail(recordId, startDate, endDate, testType, instrumentUsed)));
    }

    @GetMapping("/patient-detail")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER') or hasAuthority('READ_ONLY')")
    public ResponseEntity<?> getPatientMedicalRecordDetail(
            @RequestParam(required = true) String sub,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            Instant endDate,
            @RequestParam(required = false) String testType,
            @RequestParam(required = false) String instrumentUsed
    ) {
        return ResponseEntity.ok(BaseResponse.success(serviceName, detailService.getFilteredPatientDetail(sub, startDate, endDate, testType, instrumentUsed)));
    }
}
