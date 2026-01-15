package org.overcode250204.testorderservice.models.entites;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "barcode_sequence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BarcodeSequence {
    @Id
    private LocalDate date;

    private int lastSequence;
}
