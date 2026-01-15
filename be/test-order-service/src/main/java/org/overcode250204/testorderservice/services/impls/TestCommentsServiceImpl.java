package org.overcode250204.testorderservice.services.impls;

import jakarta.validation.Validator;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.dtos.TestCommentDTO;
import org.overcode250204.testorderservice.events.MonitoringPublisher;
import org.overcode250204.testorderservice.mappers.TestCommentsMapper;
import org.overcode250204.testorderservice.models.entites.TestComments;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.overcode250204.testorderservice.repositories.TestCommentsRepository;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.overcode250204.testorderservice.repositories.TestResultsRepository;
import org.overcode250204.testorderservice.services.TestCommentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestCommentsServiceImpl implements TestCommentsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCommentsServiceImpl.class);
    private final TestCommentsRepository testCommentRepository;
    private final TestOrdersRepository testOrderRepository;
    private final TestResultsRepository testResultRepository;
    private final TestCommentsMapper commentsMapper;
    private final Validator validator;
    private final MonitoringPublisher monitoringPublisher;

    @Override
    @Transactional
    public TestCommentDTO addTestComment(UUID resultId, TestCommentDTO request) {
        // Validate DTO
        validator.validate(request);

        TestResults testResult = testResultRepository.findById(resultId)
                .orElseThrow(() -> new NotFoundException("Test result not found"));

        // Check TestResult nếu có
        TestOrders testOrder = testResult.getTestOrder();
        if (testOrder.getIsDeleted()) {
            throw new NotFoundException("Test order associated with this result is deleted.");
        }

        // Tạo TestComment
        TestComments comment = new TestComments();
        comment.setTestOrder(testOrder);
        comment.setTestResult(testResult);
        comment.setUserId(request.getUserId());
        comment.setContent(request.getContent());
        comment.setIsDeleted(false);

        TestComments savedComment = testCommentRepository.save(comment);
        monitoringPublisher.publishMonitoringEvent(
                "TEST_COMMENT_ADDED",
                savedComment.getId().toString(),
                Map.of(
                        "commentId", savedComment.getId(),
                        "testOrderId", testOrder.getId(),
                        "testResultId", testResult.getId(),
                        "userId", savedComment.getUserId(),
                        "content", savedComment.getContent(),
                        "createdAt", savedComment.getCreatedAt()
                )
        );

        LOGGER.info("Added comment ID: {} for test result ID: {}", savedComment.getId(), resultId);

        return commentsMapper.toDTO(savedComment);
    }

    @Override
    @Transactional
    public TestCommentDTO editTestComment(UUID id, TestCommentDTO request) {
        // Validate DTO
        validator.validate(request);

        // Tìm comment
        TestComments comment = testCommentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Comment not found or deleted"));

        String oldContent = comment.getContent();
        LocalDateTime oldUpdatedAt = comment.getUpdatedAt();

        // Cập nhật comment - Chỉ update content, không change association để giữ integrity
        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        TestComments updatedComment = testCommentRepository.save(comment);
        monitoringPublisher.publishMonitoringEvent(
                "TEST_COMMENT_MODIFIED",
                updatedComment.getId().toString(),
                Map.of(
                        "testOrderId", updatedComment.getTestOrder().getId(),
                        "testResultId", updatedComment.getTestResult().getId(),
                        "userId", updatedComment.getUserId(),
                        "oldContent", oldContent,
                        "newContent", updatedComment.getContent(),
                        "oldUpdatedAt", oldUpdatedAt == null ? "NULL" : oldUpdatedAt.toString(),
                        "newUpdatedAt", updatedComment.getUpdatedAt()
                )
        );

        LOGGER.info("Updated comment ID: {} for test order ID: {}", id, updatedComment.getTestOrder().getId());

        return commentsMapper.toDTO(updatedComment);
    }

    @Override
    @Transactional
    public void deleteTestComment(UUID id) {
        TestComments comment = testCommentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found or deleted"));

        UUID testOrderId = comment.getTestOrder().getId();
        UUID testResultId = comment.getTestResult().getId();
        String content = comment.getContent();

        comment.setIsDeleted(true);
        testCommentRepository.save(comment);
        monitoringPublisher.publishMonitoringEvent(
                "TEST_COMMENT_DELETED",
                id.toString(),
                Map.of(
                        "testOrderId", testOrderId,
                        "testResultId", testResultId,
                        "userId", comment.getUserId(),
                        "content", content,
                        "deletedAt", LocalDateTime.now()
                )
        );

        LOGGER.info("Deleted comment ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestComments> getAllTestCommentsByTestOrder(UUID testOrderId) {
        return testCommentRepository.findAllByTestOrder_Id(testOrderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestCommentDTO> getVisibleTestCommentsByTestOrder(UUID testOrderId) {
        List<TestComments> testComments = testCommentRepository.findAllByTestOrder_IdAndIsDeletedFalse(testOrderId);
        return testComments.stream()
                .map(commentsMapper::toDTO)
                .collect(Collectors.toList());
    }
}