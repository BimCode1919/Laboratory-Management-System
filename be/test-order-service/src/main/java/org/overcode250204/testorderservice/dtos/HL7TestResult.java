package org.overcode250204.testorderservice.dtos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class HL7TestResult {
    private String barcode;
    private List<TestResultItem> testResults;

    public HL7TestResult(String barcode) {
        this.barcode = barcode;
        this.testResults = new ArrayList<>();
    }

    public void addTestResult(TestResultItem item) {
        if(this.testResults == null){
            this.testResults = new ArrayList<>();
        };
        this.testResults.add(item);
    }
}