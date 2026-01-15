package org.overcode250204.testorderservice.mappers;

import org.overcode250204.testorderservice.dtos.TestCommentDTO;
import org.overcode250204.testorderservice.models.entites.TestComments;
import org.springframework.stereotype.Component;

@Component
public class TestCommentsMapper {
    public TestCommentDTO toDTO(TestComments entity) {
        if (entity == null) return null;
        return new TestCommentDTO(
                entity.getId(),
                entity.getTestOrder() != null ? entity.getTestOrder().getId() : null,
                entity.getTestResult() != null ? entity.getTestResult().getId() : null,
                entity.getUserId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}