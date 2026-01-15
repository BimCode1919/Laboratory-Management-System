package org.overcode250204.testorderservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.dtos.TestCommentDTO;
import org.overcode250204.testorderservice.models.entites.TestComments;
import org.overcode250204.testorderservice.services.TestCommentsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/test-comments")
@RequiredArgsConstructor
public class TestCommentController {
    private final TestCommentsService testCommentService;

    @PostMapping("/{resultId}")
    @PreAuthorize("hasAuthority('COMMENT_CREATE')")
    public ResponseEntity<BaseResponse<TestCommentDTO>> addComment(
            @PathVariable UUID resultId,
            @Valid @RequestBody TestCommentDTO request
    ) {
        TestCommentDTO created = testCommentService.addTestComment(resultId, request);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COMMENT_UPDATE')")
    public ResponseEntity<BaseResponse<TestCommentDTO>> editComment(
            @PathVariable UUID id,
            @Valid @RequestBody TestCommentDTO request) {
        TestCommentDTO updated = testCommentService.editTestComment(id, request);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('COMMENT_DELETE')")
    public ResponseEntity<BaseResponse<Void>> deleteComment(@PathVariable UUID id) {
        testCommentService.deleteTestComment(id);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", null));
    }

    @GetMapping("/all/{testOrderId}")
    public ResponseEntity<BaseResponse<List<TestComments>>> getAllTestCommentsByTestOrder(@PathVariable UUID testOrderId) {
        List<TestComments> comments = testCommentService.getAllTestCommentsByTestOrder(testOrderId);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", comments));
    }

    @GetMapping("/visible/{testOrderId}")
    public ResponseEntity<BaseResponse<List<TestCommentDTO>>> getVisibleTestCommentsByTestOrder(@PathVariable UUID testOrderId) {
        List<TestCommentDTO> comments = testCommentService.getVisibleTestCommentsByTestOrder(testOrderId);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", comments));
    }
}