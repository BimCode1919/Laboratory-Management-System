package org.overcode250204.monitoringservice.entities;

import lombok.*;
import org.overcode250204.monitoringservice.enums.RawTestResultStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document("hl7_backups")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HL7Backup {

    @Id
    private String backupId;

    private String runId;

    private String barcode;

    private String s3Key;

    private String instrumentId;

    private String hl7Message;

    private RawTestResultStatus status;

    private String reason;

    @CreatedDate
    private LocalDateTime createdAt;
}

