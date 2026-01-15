package org.overcode250204.instrumentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.overcode250204.instrumentservice.enums.Priority;
import org.overcode250204.instrumentservice.enums.Status;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pending_test_order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingTestOrder {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "bar_code", nullable = false)
    private String barCode;

    @Column(name = "test_order_id", nullable = false)
    private UUID testOrderId;

    @Column(name = "test_type", nullable = false)
    private String testType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority;

    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
