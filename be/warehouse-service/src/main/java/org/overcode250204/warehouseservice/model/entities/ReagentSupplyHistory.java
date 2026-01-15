package org.overcode250204.warehouseservice.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.overcode250204.warehouseservice.model.enums.SupplyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reagent_supply_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReagentSupplyHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "supply_id")
    private UUID supplyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reagent_id")
    private Reagent reagent;


    @Column(name = "po_number", nullable = false, length = 50)
    private String poNumber;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Column(name = "quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit_of_measure", nullable = false, length = 20)
    private String unitOfMeasure;

    @Column(name = "lot_number", nullable = false, length = 50)
    private String lotNumber;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "received_by", length = 50)
    private UUID receivedBy;

    @Column(name = "receipt_timestamp", nullable = false)
    private LocalDateTime receiptTimestamp = LocalDateTime.now();

    @Column(name = "storage_location", nullable = false, length = 100)
    private String storageLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SupplyStatus status; // Received | Partial Shipment | Returned

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;

}
