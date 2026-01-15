package org.overcode250204.instrumentservice.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class AnalyzeCommand {

    private UUID userId;

    private String testType;

    private List<String> barcodes;

    private Boolean autoCreateTestOrder;

    private Integer expectedSamples;

    private String batchCode;

    private String note;
}
