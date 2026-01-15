package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.dtos.TestCommentDTO;
import org.overcode250204.testorderservice.models.entites.TestComments;

import java.util.List;
import java.util.UUID;

public interface TestCommentsService {
    TestCommentDTO addTestComment(UUID resultId,TestCommentDTO request);

    TestCommentDTO editTestComment(UUID id, TestCommentDTO request);

    void deleteTestComment(UUID id);

    List<TestComments> getAllTestCommentsByTestOrder(UUID testOrderId);

    List<TestCommentDTO> getVisibleTestCommentsByTestOrder(UUID testOrderId);
}