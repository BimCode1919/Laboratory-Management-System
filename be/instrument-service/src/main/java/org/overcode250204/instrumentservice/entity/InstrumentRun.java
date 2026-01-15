package org.overcode250204.instrumentservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.overcode250204.instrumentservice.enums.InstrumentRunStatus;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "instrument_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentRun {

    @Id
    private String id;

    private UUID runId = UUID.randomUUID();

    private UUID instrumentId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private InstrumentRunStatus status;

    private Integer totalSamplesExpected;

    private Integer successfulSamples;

    private Integer failedSamples;

    private UUID createdBy;

    private JsonNode reagentSnapshot;

    private String configVersion;

    private String errorMessage;

}
