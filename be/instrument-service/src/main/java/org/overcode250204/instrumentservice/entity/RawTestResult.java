package org.overcode250204.instrumentservice.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "raw_test_results")
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RawTestResult {


    @Id
    private String id;

    private UUID runId;

    private UUID instrumentId;

    private String barcode;

    private UUID createdBy;

    private JsonNode rawData;

    private String hl7Message;

    private String status;

    private LocalDateTime publishedAt;

    private boolean backedUp;

    private String testType;

    private String errorMessage;

    private LocalDateTime createdAt = LocalDateTime.now();

}

