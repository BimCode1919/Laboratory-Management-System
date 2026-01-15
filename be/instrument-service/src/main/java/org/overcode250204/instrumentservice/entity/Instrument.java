package org.overcode250204.instrumentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.overcode250204.instrumentservice.enums.InstrumentMode;
import org.overcode250204.instrumentservice.enums.InstrumentStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Instrument {

    @Id
    @Column(name = "instrument_id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "instrument_code", length = 50)
    private String instrumentCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String model;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstrumentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InstrumentMode mode;

    @Column(name = "last_mode_change_at")
    private LocalDateTime lastModeChangeAt;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "config_version", length = 50)
    private String configVersion;

    @Column(name = "last_config_sync_at")
    private LocalDateTime lastConfigSyncAt;

    @Column(name = "is_online")
    private Boolean isOnline = false;

    // New: configurations stored as separate entity linked to instrument
    @OneToMany(mappedBy = "instrument", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<InstrumentConfiguration> configurations;

}