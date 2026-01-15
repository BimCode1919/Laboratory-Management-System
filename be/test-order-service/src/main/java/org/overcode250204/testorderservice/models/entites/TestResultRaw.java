package org.overcode250204.testorderservice.models.entites;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_result_raw")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResultRaw {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId; // to trace back to the raw hl7 in instrument if needed

    @Column(name = "instrument_id", nullable = false)
    private UUID instrumentId; // data source

    @Column(name = "barcode", nullable = false, length = 100)
    private String barcode;

    @Column(name = "raw_parameter", nullable = false)
    private String rawParameter;

    @Column(name = "raw_value", nullable = false)
    private String rawValue;

    @Column(name = "raw_unit", nullable = true)
    private String rawUnit;

    @Column(name = "raw_flag", length = 50)
    private String rawFlag;

    @Column(name = "instrument_timestamp")
    private LocalDateTime instrumentTimestamp; // also used to traceback raw hl7 or latency track

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt = LocalDateTime.now();

    @Column(name = "is_processed")
    private Boolean isProcessed = false;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "test_order_id", nullable = true)
    private UUID testOrderId;

    @Column(name = "reagent_snapshot_json", columnDefinition = "TEXT")
    private String reagentSnapshotJson;

    @Column(name = "instrument_detail_json", columnDefinition = "TEXT")
    private String instrumentDetailJson;
}