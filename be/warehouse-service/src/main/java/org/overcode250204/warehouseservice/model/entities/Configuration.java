package org.overcode250204.warehouseservice.model.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "configuration",
        uniqueConstraints = @UniqueConstraint(columnNames = {"instrument_id", "config_key"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "config_id")
    private UUID configId;

    @Column(name = "config_name", nullable = false, length = 100)
    private String configName;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value")
    private String configValue;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrument_id")
    @JsonBackReference
    private Instrument instrument;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private UUID createdBy;

    @Column(name = "updated_by", length = 50)
    private UUID updatedBy;

    @Column(name = "is_global")
    private Boolean isGlobal = false; // true = cấu hình chung, false = cấu hình riêng cho thiết bị

}
