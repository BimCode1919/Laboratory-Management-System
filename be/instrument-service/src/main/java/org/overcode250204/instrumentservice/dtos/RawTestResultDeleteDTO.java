package org.overcode250204.instrumentservice.dtos;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawTestResultDeleteDTO {
    private String barcode;
    private UUID deletedBy;
    private LocalDateTime deletedAt;
    private String deleteMode;
}