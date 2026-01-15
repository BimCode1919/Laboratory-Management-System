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


@Document
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationSyncLog {


    @Id
    private String id;

    private UUID instrumentId;

    private String syncType;

    private UUID performedBy;

    private LocalDateTime timestamp = LocalDateTime.now();

    private String status;

    private JsonNode details;
}
