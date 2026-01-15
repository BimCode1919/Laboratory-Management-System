package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.models.entites.TestResults;

import java.util.List;

public interface AIReviewService {
    List<TestResults> generateReview(List<TestResults> testResults);
}
