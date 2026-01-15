package org.overcode250204.testorderservice.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiReviewResponseDTO {
    private Boolean ai_has_issue;
    private String ai_review_comment;

}
