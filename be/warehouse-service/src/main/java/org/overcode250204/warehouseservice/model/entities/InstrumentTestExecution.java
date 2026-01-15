package org.overcode250204.warehouseservice.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.overcode250204.warehouseservice.model.enums.TestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instrument_test_execution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentTestExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "test_exec_id")
    private UUID testExecId;

    @Column(name = "test_order_id", nullable = false)
    private UUID testOrderId;

    @Column(name = "order_code", length = 100)
    private String orderCode;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id")
    private Instrument instrument;

    @Column(name = "test_name", nullable = false, length = 100)
    private String testName;

    @Column(name = "test_description", columnDefinition = "TEXT")
    private String testDescription;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TestStatus status = TestStatus.PENDING; // Pending | Running | Passed | Failed | Aborted

    @Column(name = "executed_by", nullable = false, length = 50)
    private String executedBy;

    @Column(name = "result_summary", columnDefinition = "TEXT")
    private String resultSummary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
