package org.overcode250204.testorderservice.models.entites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.overcode250204.testorderservice.models.enums.TestResultAlertLevel;
import org.overcode250204.testorderservice.models.enums.TestResultStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_results")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TestResults {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JsonIgnore
    @JsonIgnoreProperties("results")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_order_id", nullable = false)
    private TestOrders testOrder;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "raw_test_results_id")
    private TestResultRaw rawTestResults; // for admin tracebacking, checking only, no real value for patients

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "param_map_id")
    private ResultParameterMapping paramMap; // for admin tracebacking, checking only, no real value for patients

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversion_id")
    private UnitConversionMapping conversion; // for admin tracebacking, checking only, no real value for patients

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_rule_id")
    private FlaggingRules flagRule; // for admin tracebacking, checking only, no real value for patients

    @Column(name = "parameter_name", nullable = false, length = 100)
    private String parameterName;

    @Column(name = "result_value")
    private Double resultValue;

    @Column(name = "unit", length = 15)
    private String unit;

    @Column(name = "reference_range_low", length = 20)
    private String referenceLow;

    @Column(name = "reference_range_high", length = 20)
    private String referenceHigh;

    @Column(name = "alert_level", length = 20)
    @Enumerated(EnumType.STRING)
    private TestResultAlertLevel alertLevel;

    @Column(name = "ai_has_issue")
    private Boolean aiHasIssue = false;

    @Column(name = "ai_review_comment", columnDefinition = "TEXT")
    private String aiReviewComment;

    @Column(name = "is_reviewed")
    private Boolean isReviewed = false;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_status")
    @Enumerated(EnumType.STRING)
    private TestResultStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
