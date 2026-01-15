package org.overcode250204.testorderservice.dtos;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestOrderReportDetailDTO {

    private String orderCode;
    private String patientName;
    private String gender;
    private LocalDate dateOfBirth;
    private String phoneNumber;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private LocalDateTime runOn;
    private String notes;

    private List<TestResultReportDTO> testResults;
    private List<TestCommentDTO> testComments;

    public Date getDateOfBirthAsDate() {
        return dateOfBirth != null ? java.sql.Date.valueOf(dateOfBirth) : null;
    }

    public Date getCreatedOnAsDate() {
        return createdOn != null ? java.util.Date.from(createdOn.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }

    public Date getRunOnAsDate() {
        return runOn != null ? java.util.Date.from(runOn.atZone(java.time.ZoneId.systemDefault()).toInstant()) : null;
    }
}
