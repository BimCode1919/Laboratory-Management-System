package org.overcode250204.testorderservice.models.entites;

import jakarta.persistence.*;
import lombok.*;
import org.overcode250204.testorderservice.models.enums.ExportFileType;
import org.overcode250204.testorderservice.models.enums.ExportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "export_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportJob {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_order_id")
    private TestOrders testOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_result_id")
    private TestResults testResult;

    @Column(name = "file_name", length = 100)
    private String fileName;

    @Column(name = "file_path", columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "export_file_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ExportFileType exportFileType;

    @Column(name = "exported_by")
    private UUID exportedBy;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private ExportStatus status = ExportStatus.PENDING;
}