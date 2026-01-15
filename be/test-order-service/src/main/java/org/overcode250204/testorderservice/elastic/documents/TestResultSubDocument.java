package org.overcode250204.testorderservice.elastic.documents;

import lombok.*;
import org.overcode250204.testorderservice.models.enums.TestResultStatus;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultSubDocument {
    @Field(type = FieldType.Keyword)
    private String parameterName;

    @Field(type = FieldType.Keyword)
    private TestResultStatus status;

    @Field(type = FieldType.Double)
    private Double resultValue;

    @Field(type = FieldType.Keyword)
    private String unit;
}
