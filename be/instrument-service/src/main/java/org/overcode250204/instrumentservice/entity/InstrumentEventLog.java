package org.overcode250204.instrumentservice.entity;


import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "instrument_event_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentEventLog {

    @Id
    private String id;

    private UUID instrumentId;

    private String eventType;

    private JsonNode details;

    private UUID performedBy;

    private LocalDateTime timestamp = LocalDateTime.now();
}
