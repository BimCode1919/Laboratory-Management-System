package org.overcode250204.testorderservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.dtos.TestResultReviewDTO;
import org.overcode250204.testorderservice.mappers.TestResultsMapper;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.repositories.TestResultsRepository;
import org.overcode250204.testorderservice.services.AIReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {
    private final AIReviewService aiReviewService;
    private final TestResultsRepository testResultsRepository;
    private final TestResultsMapper testResultsMapper;
    private static final String SERVICE_NAME = "test-order-service";

    @GetMapping("/{testOrderId}")
    public ResponseEntity<BaseResponse<List<TestResultReviewDTO>>> reviewTestOrder(@PathVariable UUID testOrderId) {
        List<TestResults> results = testResultsRepository.findByTestOrderId(testOrderId);

        if (results == null || results.isEmpty()) {
            return ResponseEntity.ok((BaseResponse<List<TestResultReviewDTO>>)
                    BaseResponse.error(SERVICE_NAME, "404", "No test results found for this order"));
        }

        List<TestResults> reviewed = aiReviewService.generateReview(results);

        List<TestResultReviewDTO> reviewedDTOs = reviewed.stream()
                .map(testResultsMapper::toReviewDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, reviewedDTOs));
    }

}
